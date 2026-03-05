package com.example.webhookcollectorservice.controller;

import com.example.api.kafka.DepositCompletedEvent;
import com.example.api.kafka.DepositRequestedEvent;
import com.example.webhook.dto.StatusUpdate;
import com.example.webhookcollectorservice.config.Container;
import com.example.webhookcollectorservice.config.KafkaTestContainer;
import com.example.webhookcollectorservice.repository.OutboxEventRepository;
import com.example.webhookcollectorservice.repository.PaymentProviderCallbackRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebhookControllerTest extends Container {
    @Value("${kafka.topic.transaction-service.transaction.read.name}")
    String readTopic;
    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    HmacUtils hmacUtils;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    OutboxEventRepository outboxEventRepository;
    @Autowired
    PaymentProviderCallbackRepository paymentProviderCallbackRepository;

    static {
        Container.start();
    }

    @Test
    @DisplayName("Test deposit completed -> sent to kafka succeeded")
    void testCatchDepositCompletedWebhook() throws ExecutionException, InterruptedException {
        UUID transactionId = UUID.randomUUID();

        StatusUpdate request = new StatusUpdate();
        request.setAmount(BigDecimal.valueOf(500));
        request.setId(transactionId);
        request.setType("deposit");
        request.setStatus("COMPLETED");
        request.setProvider("fake-provider");

        HttpHeaders headers = new HttpHeaders();

        try {
            headers.add("X-Signature", hmacUtils.hmacHex(objectMapper.writeValueAsString(request)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpEntity<StatusUpdate> updateStatus = new HttpEntity<>(request, headers);

        String createWalletUrl = UriComponentsBuilder.fromPath("/api/v1/webhooks/payment-provider")
                .toUriString();

        ResponseEntity<Void> response = restTemplate.exchange(createWalletUrl, HttpMethod.POST, updateStatus, Void.class);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1L, outboxEventRepository.count());
        assertEquals(1L, paymentProviderCallbackRepository.count());

        DepositCompletedEvent event = DepositCompletedEvent.builder()
                .amount(BigDecimal.valueOf(500))
                .status("COMPLETED")
                .transactionId(transactionId)
                .build();

        KafkaConsumer<String, Object> consumer = KafkaTestContainer.getKafkaConsumerForTopic(readTopic, DepositRequestedEvent.class);

        ConsumerRecords<String, Object> records = consumer.poll(Duration.ofSeconds(5));

        assertEquals(1, records.count());

        var record = records.iterator().next();

        DepositCompletedEvent sent =
                (DepositCompletedEvent) record.value();

        assertEquals(transactionId.toString(), record.key());
        assertEquals(transactionId, sent.getTransactionId());
        assertEquals("COMPLETED", sent.getStatus());
        assertEquals(BigDecimal.valueOf(500), sent.getAmount());
    }
}