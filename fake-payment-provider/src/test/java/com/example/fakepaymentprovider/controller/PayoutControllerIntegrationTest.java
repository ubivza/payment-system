package com.example.fakepaymentprovider.controller;

import com.example.fake.dto.Payout;
import com.example.fake.dto.PayoutRequest;
import com.example.fakepaymentprovider.config.Container;
import com.example.fakepaymentprovider.repository.MerchantRepository;
import com.example.fakepaymentprovider.repository.PayoutRepository;
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
class PayoutControllerIntegrationTest extends Container {
    @Autowired
    TestRestTemplate restTemplate;
    @MockitoSpyBean
    RateLimiterService rateLimiterService;
    @Autowired
    PayoutRepository payoutRepository;
    @Autowired
    MerchantRepository merchantRepository;

    static {
        Container.start();
    }

    @AfterEach
    @Transactional
    void clear() {
        payoutRepository.deleteAll();
    }

    private HttpHeaders getBasicAuthHeader() {
        HttpHeaders authHeader = new HttpHeaders();
        authHeader.setBasicAuth("adidas", "ya_lublu_adidas12");

        return authHeader;
    }

    @Test
    @DisplayName("Test basic auth -> bucket token consumed -> payout created 200")
    public void createPayoutHappy() {
        assertEquals(2, merchantRepository.findAll().size());
        HttpEntity<PayoutRequest> create = new HttpEntity<>(createPayoutRequest(), getBasicAuthHeader());

        String createUri = "/api/v1/payouts";

        ResponseEntity<Payout> createResponse = restTemplate.postForEntity(createUri, create, Payout.class);
        assertEquals(201, createResponse.getStatusCode().value());

        verify(rateLimiterService, times(1)).tryConsume("adidas");

        List<com.example.fakepaymentprovider.entity.Payout> entities = payoutRepository.findAll();
        assertEquals(1, entities.size());
        assertEquals(createResponse.getBody().getCurrency(), entities.get(0).getCurrency());
    }

    @Test
    @DisplayName("Test payout created -> get by id 200")
    public void getPayoutByIdHappy() {
        assertEquals(2, merchantRepository.findAll().size());
        HttpEntity<PayoutRequest> create = new HttpEntity<>(createPayoutRequest(), getBasicAuthHeader());

        String createTransactionUri = "/api/v1/payouts";

        ResponseEntity<Payout> createResponse = restTemplate.postForEntity(createTransactionUri, create, Payout.class);
        assertEquals(201, createResponse.getStatusCode().value());

        verify(rateLimiterService, times(1)).tryConsume("adidas");

        List<com.example.fakepaymentprovider.entity.Payout> entities = payoutRepository.findAll();
        assertEquals(1, entities.size());
        assertEquals(createResponse.getBody().getCurrency(), entities.get(0).getCurrency());

        HttpEntity<PayoutRequest> getRequest = new HttpEntity<>(getBasicAuthHeader());

        String getByIdUri = UriComponentsBuilder.fromPath("/api/v1/payouts/{id}")
                .uriVariables(Map.of("id", createResponse.getBody().getId()))
                .toUriString();

        ResponseEntity<Payout> getById = restTemplate.exchange(getByIdUri, HttpMethod.GET, getRequest, Payout.class);
        assertEquals(200, getById.getStatusCode().value());

        verify(rateLimiterService, times(2)).tryConsume("adidas");
        assertEquals(createPayoutRequest().getCurrency(), getById.getBody().getCurrency());
    }

    @Test
    @DisplayName("Test payout created -> get all 200")
    public void getPayoutsHappy() {
        assertEquals(2, merchantRepository.findAll().size());
        HttpEntity<PayoutRequest> create = new HttpEntity<>(createPayoutRequest(), getBasicAuthHeader());

        String createTransactionUri = "/api/v1/payouts";

        ResponseEntity<Payout> createResponse = restTemplate.postForEntity(createTransactionUri, create, Payout.class);
        assertEquals(201, createResponse.getStatusCode().value());

        verify(rateLimiterService, times(1)).tryConsume("adidas");

        List<com.example.fakepaymentprovider.entity.Payout> entities = payoutRepository.findAll();
        assertEquals(1, entities.size());
        assertEquals(createResponse.getBody().getCurrency(), entities.get(0).getCurrency());

        HttpEntity<PayoutRequest> getRequest = new HttpEntity<>(getBasicAuthHeader());

        String getByIdUri = "/api/v1/payouts";

        ResponseEntity<List<Payout>> getAll = restTemplate.exchange(getByIdUri, HttpMethod.GET, getRequest, new ParameterizedTypeReference<>() {});
        assertEquals(200, getAll.getStatusCode().value());

        verify(rateLimiterService, times(2)).tryConsume("adidas");
        assertEquals(1, getAll.getBody().size());
        assertEquals(createPayoutRequest().getCurrency(), getAll.getBody().getFirst().getCurrency());
    }

    private PayoutRequest createPayoutRequest() {
        PayoutRequest request = new PayoutRequest();
        request.setAmount(100d);
        request.setCurrency("USD");

        return request;
    }
}