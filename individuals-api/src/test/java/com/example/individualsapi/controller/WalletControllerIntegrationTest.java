package com.example.individualsapi.controller;

import com.example.individuals.dto.CreateWalletRequestDto;
import com.example.individuals.dto.TokenResponse;
import com.example.individuals.dto.UserRegistrationRequest;
import com.example.individuals.dto.WalletResponseDto;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@SpringBootTest
class WalletControllerIntegrationTest extends Container {
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
    @DisplayName("Create -> get")
    void testCreateAndGetFlow() {
        CreateWalletRequestDto req = new CreateWalletRequestDto();
        req.setName("Happy wallet");
        req.currencyCode("RUB");

        WalletResponseDto walletResponseDto = new WalletResponseDto();
        walletResponseDto.setName("Happy wallet");
        walletResponseDto.setBalance(BigDecimal.ZERO);
        walletResponseDto.setStatus("ACTIVE");
        walletResponseDto.setCurrencyCode("RUB");
        walletResponseDto.setWalletUid(UUID.fromString("4cac87a9-f63f-49ec-88b5-e55fe660268b"));

        UserRegistrationRequest userRegistrationRequest = TestUtils.buildMockUserRegistrationRequest();

        webTestClient.post().uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRegistrationRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(TokenResponse.class)
                .value(registrationResponse -> {
                    assertNotNull(registrationResponse.getAccessToken());
                    webTestClient.post().uri("/v1/wallets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .headers(headers -> headers.setBearerAuth(registrationResponse.getAccessToken()))
                            .bodyValue(req)
                            .exchange()
                            .expectStatus().is2xxSuccessful()
                            .expectBody(UUID.class)
                            .value(uuid -> {
                                assertNotNull(uuid);
                                webTestClient.get().uri("/v1/wallets/" + uuid)
                                        .headers(headers -> headers.setBearerAuth(registrationResponse.getAccessToken()))
                                        .exchange()
                                        .expectStatus().is2xxSuccessful()
                                        .expectBody(WalletResponseDto.class).isEqualTo(walletResponseDto);
                            });
                });
    }
}
