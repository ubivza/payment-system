package com.example.fakepaymentprovider.scheduling;

import com.example.fakepaymentprovider.config.WebhookSecretHolder;
import com.example.fakepaymentprovider.entity.Payout;
import com.example.fakepaymentprovider.entity.Transaction;
import com.example.fakepaymentprovider.entity.Webhook;
import com.example.fakepaymentprovider.entity.WebhookPayload;
import com.example.fakepaymentprovider.integration.dto.WebhookRequest;
import com.example.fakepaymentprovider.repository.PayoutRepository;
import com.example.fakepaymentprovider.repository.TransactionRepository;
import com.example.fakepaymentprovider.repository.WebhookRepository;
import com.example.fakepaymentprovider.service.MetricsCollector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookScheduler {
    private static final Set<String> READY_STATUSES = Set.of("SUCCESS", "FAILED");

    private final PayoutRepository payoutRepository;
    private final TransactionRepository transactionRepository;
    private final WebhookRepository webhookRepository;
    private final WebhookSecretHolder webhookProperties;
    private final RestTemplate webhookRestTemplate;
    private final MetricsCollector metricsCollector;
    private final HmacUtils hmacUtils;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${webhook.scheduler.fixed-delay-ms:5000}")
    public void scheduledTick() {
        runOnce();
    }

    public void runOnce() {
        int batchSize = Math.max(1, webhookProperties.getScheduler().getBatchSize());

        sendPayoutWebhooks(batchSize);
        sendTransactionWebhooks(batchSize);
    }

    private void sendPayoutWebhooks(int batchSize) {
        List<Payout> payouts = payoutRepository.findReadyUnsent(READY_STATUSES, PageRequest.of(0, batchSize));
        for (Payout payout : payouts) {
            sendOne("payout", payout.getId(), payout.getStatus(), payout.getAmount());
        }
    }

    private void sendTransactionWebhooks(int batchSize) {
        List<Transaction> transactions = transactionRepository.findReadyUnsent(READY_STATUSES, PageRequest.of(0, batchSize));
        for (Transaction transaction : transactions) {
            sendOne("transaction", transaction.getId(), transaction.getStatus(), transaction.getAmount());
        }
    }

    private void sendOne(String type, UUID entityId, String status, BigDecimal amount) {
        String url = webhookProperties.getOutgoingUrl();
        if (url == null || url.isBlank()) {
            log.warn("Skipping outgoing webhook send: webhook.outgoing-url is not configured");
            return;
        }

        WebhookRequest payload = new WebhookRequest(type, entityId, status, status.equals("FAILED") ? "Because" : null, "fake-payment", amount);

        try {
            HttpHeaders headers = new HttpHeaders();
            try {
                headers.add("X-Signature", hmacUtils.hmacHex(objectMapper.writeValueAsString(payload)));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            HttpEntity<WebhookRequest> requestEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<Void> response = webhookRestTemplate.postForEntity(url, requestEntity, Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                metricsCollector.recordWebhook(false);
                log.warn("Outgoing webhook failed: type={}, id={}, status={}, http={}", type, entityId, status, response.getStatusCode().value());
                return;
            }
        } catch (RestClientException e) {
            metricsCollector.recordWebhook(false);
            log.warn("Outgoing webhook failed: type={}, id={}, status={}, error={}", type, entityId, status, e.toString());
            return;
        }

        try {
            Webhook webhook = new Webhook();
            webhook.setEventType(type);
            webhook.setEntityId(entityId);
            webhook.setNotificationUrl(url);
            webhook.setPayload(WebhookPayload.builder()
                    .type(type)
                    .id(entityId)
                    .status(status)
                    .build());
            webhookRepository.save(webhook);

            metricsCollector.recordWebhook(true);
        } catch (DataIntegrityViolationException e) {
            metricsCollector.recordWebhook(true);
        } catch (Exception e) {
            metricsCollector.recordWebhook(false);
            log.warn("Failed to persist sent webhook: type={}, id={}, status={}, error={}", type, entityId, status, e.toString());
        }
    }
}
