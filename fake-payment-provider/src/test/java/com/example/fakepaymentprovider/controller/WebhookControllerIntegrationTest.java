package com.example.fakepaymentprovider.controller;

import com.example.fake.dto.Payout;
import com.example.fake.dto.PayoutRequest;
import com.example.fake.dto.StatusUpdate;
import com.example.fake.dto.Transaction;
import com.example.fake.dto.TransactionRequest;
import com.example.fakepaymentprovider.config.Container;
import com.example.fakepaymentprovider.repository.MerchantRepository;
import com.example.fakepaymentprovider.repository.PayoutRepository;
import com.example.fakepaymentprovider.repository.TransactionRepository;
import com.example.fakepaymentprovider.service.HMACAuthService;
import com.example.fakepaymentprovider.service.RateLimiterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import org.apache.commons.codec.digest.HmacUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebhookControllerIntegrationTest extends Container {
    @Autowired
    TestRestTemplate restTemplate;
    @MockitoSpyBean
    RateLimiterService rateLimiterService;
    @Autowired
    PayoutRepository payoutRepository;
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    MerchantRepository merchantRepository;
    @Autowired
    HmacUtils hmacUtils;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoSpyBean
    HMACAuthService hmacAuthService;
    @MockitoSpyBean
    Bucket webhookBucket;

    static {
        Container.start();
    }

    @AfterEach
    @Transactional
    void clear() {
        payoutRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    private HttpHeaders getBasicAuthHeader() {
        HttpHeaders authHeader = new HttpHeaders();
        authHeader.setBasicAuth("adidas", "ya_lublu_adidas12");

        return authHeader;
    }

    @Test
    @DisplayName("Test transaction created 200 -> hmac auth -> bucket token consumed -> webhook consumed 200")
    public void createTransactionThanUpdateWithWebhookHappy() throws JsonProcessingException {
        assertEquals(2, merchantRepository.findAll().size());
        HttpEntity<TransactionRequest> createTransaction = new HttpEntity<>(createTransactionRequest(), getBasicAuthHeader());

        String createTransactionUri = "/api/v1/transactions";

        ResponseEntity<Transaction> createTransactionResponse = restTemplate.postForEntity(createTransactionUri, createTransaction, Transaction.class);
        assertEquals(201, createTransactionResponse.getStatusCode().value());

        verify(rateLimiterService, times(1)).tryConsume("adidas");

        List<com.example.fakepaymentprovider.entity.Transaction> entities = transactionRepository.findAll();
        assertEquals(1, entities.size());
        assertEquals(createTransactionResponse.getBody().getMethod(), entities.get(0).getMethod());

        StatusUpdate update = new StatusUpdate();
        update.setId(createTransactionResponse.getBody().getId());
        update.setReason("some reason");
        update.setStatus("SUCCESS");

        String encoded = hmacUtils.hmacHex(objectMapper.writeValueAsString(update));

        HttpHeaders hmacHeader = new HttpHeaders();
        hmacHeader.set("X-Signature", encoded);

        HttpEntity<StatusUpdate> webhookRequest = new HttpEntity<>(update, hmacHeader);

        String webhookUri = "/api/v1/webhook/transaction";

        ResponseEntity<Void> webhookResponse = restTemplate.postForEntity(webhookUri, webhookRequest, Void.class);
        assertEquals(200, webhookResponse.getStatusCode().value());

        verify(hmacAuthService, times(1)).authenticate(encoded, update);
        verify(webhookBucket, times(1)).tryConsume(1);

        assertEquals(update.getStatus(), transactionRepository.findAll().getFirst().getStatus());
    }

    @Test
    @DisplayName("Test payout created 200 -> hmac auth -> bucket token consumed -> webhook consumed 200")
    public void createPayoutThanUpdateWithWebhookHappy() throws JsonProcessingException {
        assertEquals(2, merchantRepository.findAll().size());
        HttpEntity<PayoutRequest> create = new HttpEntity<>(createPayoutRequest(), getBasicAuthHeader());

        String createTransactionUri = "/api/v1/payouts";

        ResponseEntity<Payout> createResponse = restTemplate.postForEntity(createTransactionUri, create, Payout.class);
        assertEquals(201, createResponse.getStatusCode().value());

        verify(rateLimiterService, times(1)).tryConsume("adidas");

        List<com.example.fakepaymentprovider.entity.Payout> entities = payoutRepository.findAll();
        assertEquals(1, entities.size());
        assertEquals(createResponse.getBody().getCurrency(), entities.get(0).getCurrency());

        StatusUpdate update = new StatusUpdate();
        update.setId(createResponse.getBody().getId());
        update.setReason("some reason");
        update.setStatus("SUCCESS");

        String encoded = hmacUtils.hmacHex(objectMapper.writeValueAsString(update));

        HttpHeaders hmacHeader = new HttpHeaders();
        hmacHeader.set("X-Signature", encoded);

        HttpEntity<StatusUpdate> webhookRequest = new HttpEntity<>(update, hmacHeader);

        String webhookUri = "/api/v1/webhook/payout";

        ResponseEntity<Void> webhookResponse = restTemplate.postForEntity(webhookUri, webhookRequest, Void.class);
        assertEquals(200, webhookResponse.getStatusCode().value());

        verify(hmacAuthService, times(1)).authenticate(encoded, update);
        verify(webhookBucket, times(1)).tryConsume(1);

        assertEquals(update.getStatus(), payoutRepository.findAll().getFirst().getStatus());
    }

    private TransactionRequest createTransactionRequest() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(100d);
        request.setDescription("salary");
        request.setCurrency("USD");
        request.setMethod("by card");

        return request;
    }

    private PayoutRequest createPayoutRequest() {
        PayoutRequest request = new PayoutRequest();
        request.setAmount(100d);
        request.setCurrency("USD");

        return request;
    }
}