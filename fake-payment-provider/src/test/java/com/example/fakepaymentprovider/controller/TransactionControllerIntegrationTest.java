package com.example.fakepaymentprovider.controller;

import com.example.fake.dto.Transaction;
import com.example.fake.dto.TransactionRequest;
import com.example.fakepaymentprovider.config.Container;
import com.example.fakepaymentprovider.repository.MerchantRepository;
import com.example.fakepaymentprovider.repository.TransactionRepository;
import com.example.fakepaymentprovider.service.RateLimiterService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionControllerIntegrationTest extends Container {
    @Autowired
    TestRestTemplate restTemplate;
    @MockitoSpyBean
    RateLimiterService rateLimiterService;
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    MerchantRepository merchantRepository;

    static {
        Container.start();
    }

    @AfterEach
    @Transactional
    void clear() {
        transactionRepository.deleteAll();
    }

    private HttpHeaders getBasicAuthHeader() {
        HttpHeaders authHeader = new HttpHeaders();
        authHeader.setBasicAuth("adidas", "ya_lublu_adidas12");

        return authHeader;
    }

    @Test
    @DisplayName("Test basic auth -> bucket token consumed -> transaction created 200")
    public void createTransactionHappy() {
        assertEquals(2, merchantRepository.findAll().size());
        HttpEntity<TransactionRequest> createTransaction = new HttpEntity<>(createTransactionRequest(), getBasicAuthHeader());

        String createTransactionUri = "/api/v1/transactions";

        ResponseEntity<Transaction> createTransactionResponse = restTemplate.postForEntity(createTransactionUri, createTransaction, Transaction.class);
        assertEquals(201, createTransactionResponse.getStatusCode().value());

        verify(rateLimiterService, times(1)).tryConsume("adidas");

        List<com.example.fakepaymentprovider.entity.Transaction> entities = transactionRepository.findAll();
        assertEquals(1, entities.size());
        assertEquals(createTransactionResponse.getBody().getMethod(), entities.get(0).getMethod());
    }

    @Test
    @DisplayName("Test transaction created -> get by id 200")
    public void getTransactionByIdHappy() {
        assertEquals(2, merchantRepository.findAll().size());
        HttpEntity<TransactionRequest> createTransaction = new HttpEntity<>(createTransactionRequest(), getBasicAuthHeader());

        String createTransactionUri = "/api/v1/transactions";

        ResponseEntity<Transaction> createTransactionResponse = restTemplate.postForEntity(createTransactionUri, createTransaction, Transaction.class);
        assertEquals(201, createTransactionResponse.getStatusCode().value());

        verify(rateLimiterService, times(1)).tryConsume("adidas");

        List<com.example.fakepaymentprovider.entity.Transaction> entities = transactionRepository.findAll();
        assertEquals(1, entities.size());
        assertEquals(createTransactionResponse.getBody().getMethod(), entities.get(0).getMethod());

        HttpEntity<TransactionRequest> getTransaction = new HttpEntity<>(getBasicAuthHeader());

        String getByIdUri = UriComponentsBuilder.fromPath("/api/v1/transactions/{id}")
                .uriVariables(Map.of("id", createTransactionResponse.getBody().getId()))
                .toUriString();

        ResponseEntity<Transaction> getById = restTemplate.exchange(getByIdUri, HttpMethod.GET, getTransaction, Transaction.class);
        assertEquals(200, getById.getStatusCode().value());

        verify(rateLimiterService, times(2)).tryConsume("adidas");
        assertEquals(createTransactionRequest().getMethod(), getById.getBody().getMethod());
    }

    @Test
    @DisplayName("Test transaction created -> get all 200")
    public void getTransactionsHappy() {
        assertEquals(2, merchantRepository.findAll().size());
        HttpEntity<TransactionRequest> createTransaction = new HttpEntity<>(createTransactionRequest(), getBasicAuthHeader());

        String createTransactionUri = "/api/v1/transactions";

        ResponseEntity<Transaction> createTransactionResponse = restTemplate.postForEntity(createTransactionUri, createTransaction, Transaction.class);
        assertEquals(201, createTransactionResponse.getStatusCode().value());

        verify(rateLimiterService, times(1)).tryConsume("adidas");

        List<com.example.fakepaymentprovider.entity.Transaction> entities = transactionRepository.findAll();
        assertEquals(1, entities.size());
        assertEquals(createTransactionResponse.getBody().getMethod(), entities.get(0).getMethod());

        HttpEntity<TransactionRequest> getTransaction = new HttpEntity<>(getBasicAuthHeader());

        String getByIdUri = "/api/v1/transactions";

        ResponseEntity<List<Transaction>> getAll = restTemplate.exchange(getByIdUri, HttpMethod.GET, getTransaction, new ParameterizedTypeReference<>() {});
        assertEquals(200, getAll.getStatusCode().value());

        verify(rateLimiterService, times(2)).tryConsume("adidas");
        assertEquals(1, getAll.getBody().size());
        assertEquals(createTransactionRequest().getMethod(), getAll.getBody().getFirst().getMethod());
    }

    private TransactionRequest createTransactionRequest() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(100d);
        request.setDescription("salary");
        request.setCurrency("USD");
        request.setMethod("by card");

        return request;
    }
}