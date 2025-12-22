package com.example.individualsapi.controller;

import com.example.individuals.dto.ConfirmRequestDto;
import com.example.individuals.dto.CreateWalletRequestDto;
import com.example.individuals.dto.TokenResponse;
import com.example.individuals.dto.TransactionConfirmResponseDto;
import com.example.individuals.dto.TransactionInitResponseDto;
import com.example.individuals.dto.TransactionStatusResponseDto;
import com.example.individuals.dto.UserRegistrationRequest;
import com.example.individuals.dto.WithdrawalInitRequestDto;
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
public class TransactionControllerIntegrationTest extends Container {
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
    @DisplayName("init withdrawal -> confirm -> get status COMPLETED")
    void testWithdrawalHappyFlow() {
        CreateWalletRequestDto req = new CreateWalletRequestDto();
        req.setName("Happy wallet");
        req.currencyCode("RUB");

        TransactionInitResponseDto withdrawalInitResponseDto = new TransactionInitResponseDto();
        withdrawalInitResponseDto.setAvailable(true);
        withdrawalInitResponseDto.setFee(new BigDecimal(10));
        withdrawalInitResponseDto.setToken("init-token-123");

        WithdrawalInitRequestDto initTransactionRequest = new WithdrawalInitRequestDto();
        initTransactionRequest.setAmount(new BigDecimal(10));
        initTransactionRequest.setType("withdrawal");
        initTransactionRequest.setWalletUid(UUID.fromString("4cac87a9-f63f-49ec-88b5-e55fe660268b"));
        initTransactionRequest.setDestination("nalik");

        ConfirmRequestDto confirmRequestDto = new ConfirmRequestDto();
        confirmRequestDto.setConfirm(true);
        confirmRequestDto.setToken("init-token-123");

        TransactionConfirmResponseDto confirmResponseDto = new TransactionConfirmResponseDto();
        confirmResponseDto.setTransactionId(UUID.fromString("6ca59b01-189c-495f-8240-0f72d921429b"));
        confirmResponseDto.setStatus("PENDING");

        TransactionStatusResponseDto statusResponseDto = new TransactionStatusResponseDto();
        statusResponseDto.setStatus("COMPLETED");

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
                            .headers(headers -> headers.setBearerAuth(registrationResponse.getAccessToken()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(req)
                            .exchange()
                            .expectStatus().is2xxSuccessful()
                            .expectBody(UUID.class)
                            .value(uuid -> {
                                assertNotNull(uuid);
                                webTestClient.post().uri("/v1/transactions/withdrawal/init")
                                        .headers(headers -> headers.setBearerAuth(registrationResponse.getAccessToken()))
                                        .bodyValue(initTransactionRequest)
                                        .exchange()
                                        .expectStatus().is2xxSuccessful()
                                        .expectBody(TransactionInitResponseDto.class).isEqualTo(withdrawalInitResponseDto)
                                        .value(initResponse -> {
                                            webTestClient.post().uri("/v1/transactions/withdrawal/confirm")
                                                    .headers(headers -> headers.setBearerAuth(registrationResponse.getAccessToken()))
                                                    .bodyValue(confirmRequestDto)
                                                    .exchange()
                                                    .expectStatus().is2xxSuccessful()
                                                    .expectBody(TransactionConfirmResponseDto.class).isEqualTo(confirmResponseDto)
                                                    .value(confirmResponse -> {
                                                        webTestClient.get().uri("/v1/transactions/6ca59b01-189c-495f-8240-0f72d921429b/status")
                                                                .headers(headers -> headers.setBearerAuth(registrationResponse.getAccessToken()))
                                                                .exchange()
                                                                .expectStatus().is2xxSuccessful()
                                                                .expectBody(TransactionStatusResponseDto.class).isEqualTo(statusResponseDto);
                                                    });
                                        });
                            });
                });
    }
}
