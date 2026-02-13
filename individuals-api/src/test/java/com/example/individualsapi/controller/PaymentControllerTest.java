package com.example.individualsapi.controller;

import com.example.individuals.dto.TokenResponse;
import com.example.individuals.dto.UserRegistrationRequest;
import com.example.individualsapi.config.Container;
import com.example.individualsapi.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@SpringBootTest
class PaymentControllerTest extends Container {

    @Autowired
    ApplicationContext context;
    WebTestClient webTestClient;

    static {
        Container.start();
    }

    @BeforeEach
    void init() {
        webTestClient = WebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .build();
    }

    @Test
    @DisplayName("Registration -> get available payment methods")
    void testRegistrationAndGetPaymentMethods() {
        UserRegistrationRequest req = TestUtils.buildMockUserRegistrationRequest();

        webTestClient.post().uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TokenResponse.class)
                .value(tokenRes -> {
                    assertNotNull(tokenRes.getAccessToken());
                    webTestClient.get().uri("/v1/payments/RUB/RU")
                            .headers(headers -> headers.setBearerAuth(tokenRes.getAccessToken()))
                            .exchange()
                            .expectStatus().isOk();
                });
    }
}