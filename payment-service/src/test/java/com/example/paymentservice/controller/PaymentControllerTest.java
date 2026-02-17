package com.example.paymentservice.controller;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.paymentservice.config.Container;
import com.example.paymentservice.entity.PaymentMethod;
import com.example.paymentservice.service.PaymentMethodDefinitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentControllerTest extends Container {
    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    PaymentMethodDefinitionService paymentMethodDefinitionService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    static {
        Container.startAll();
    }

    @BeforeEach
    void stubSecurity() {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("scope", "message:read")
                .build();

        Mockito.when(jwtDecoder.decode(anyString())).thenReturn(jwt);
    }

    @Test
    @DisplayName("create payment 200")
    void getPaymentMethodsOk() {
        PaymentMethod paymentMethod = paymentMethodDefinitionService.getPaymentMethodBy("JPY", "JPN").get(0);

        PaymentRequest request = new PaymentRequest();
        request.setMethodId(paymentMethod.getId());
        request.setAmount(100d);
        request.setCurrency("JPY");
        request.setInternalTransactionUid(UUID.randomUUID());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        HttpEntity<PaymentRequest> entity = new HttpEntity<>(request, headers);

        String createURI = "/api/v1/payments";

        ResponseEntity<PaymentResponse> createPayment = restTemplate.exchange(createURI, HttpMethod.POST, entity, PaymentResponse.class);
        assertEquals(201, createPayment.getStatusCode().value());

        assertEquals("5300a5f4-1e18-4032-8a27-d309b04ebca5", createPayment.getBody().getProviderTransactionId());
        assertEquals("SUCCESS", createPayment.getBody().getStatus());
    }
}