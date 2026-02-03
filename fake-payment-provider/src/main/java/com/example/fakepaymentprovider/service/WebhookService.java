package com.example.fakepaymentprovider.service;

import com.example.fake.dto.StatusUpdate;
import com.example.fakepaymentprovider.entity.Webhook;
import com.example.fakepaymentprovider.exception.RepeatedWebhookException;
import com.example.fakepaymentprovider.mapper.WebhookMapper;
import com.example.fakepaymentprovider.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {
    private final WebhookRepository repository;
    private final WebhookMapper mapper;
    private final PayoutService payoutService;
    private final TransactionService transactionService;
    private final MetricsCollector metricsCollector;

    @Transactional
    public void updatePayout(StatusUpdate statusUpdate) {
        update(statusUpdate, payoutService);
    }

    @Transactional
    public void updateTransaction(StatusUpdate statusUpdate) {
        update(statusUpdate, transactionService);
    }

    private void save(StatusUpdate statusUpdate) {
        if (repository.findByEntityId(statusUpdate.getId()).isPresent()) {
            throw new RepeatedWebhookException(String.format("Got repeated request for entity %s", statusUpdate.getId()));
        }

        Webhook webhook = new Webhook();
        webhook.setPayload(mapper.toPayload(statusUpdate));
        webhook.setEventType("webhook");
        webhook.setEntityId(statusUpdate.getId());

        repository.save(webhook);
    }

    private void update(StatusUpdate statusUpdate, WebhookListener service) {
        try {
            save(statusUpdate);
        } catch (RepeatedWebhookException exception) {
            return;
        }

        try {
            service.updateStatus(statusUpdate);

            metricsCollector.recordWebhook(true);
        } catch (Exception e) {
            metricsCollector.recordWebhook(false);
            throw e;
        }
    }
}
