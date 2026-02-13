package com.example.paymentservice.controller;

import com.example.payment.dto.PaymentMethodResponse;
import com.example.paymentservice.config.Container;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentMethodControllerTest extends Container {
    @Autowired
    TestRestTemplate restTemplate;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    static {
        Container.startDBOnly();
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
    @DisplayName("get payment methods 200")
    void getPaymentMethodsOk() {
        String currencyCode = "JPY";
        String countryCode = "JPN";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String getByIdUri = UriComponentsBuilder.fromPath("/api/v1/payment-methods/{currencyCode}/{countryCode}")
                .uriVariables(Map.of("currencyCode", currencyCode, "countryCode", countryCode))
                .toUriString();

        ResponseEntity<List<PaymentMethodResponse>> getById = restTemplate.exchange(getByIdUri, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
        assertEquals(200, getById.getStatusCode().value());

        assertEquals(1, getById.getBody().size());
        assertEquals("card", getById.getBody().get(0).getProviderMethodType());
    }
}