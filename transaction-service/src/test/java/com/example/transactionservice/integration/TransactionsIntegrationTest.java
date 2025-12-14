package com.example.transactionservice.integration;

import com.example.transaction.dto.ConfirmRequest;
import com.example.transaction.dto.CreateWalletRequest;
import com.example.transaction.dto.InitTransactionRequest;
import com.example.transaction.dto.TransactionConfirmResponse;
import com.example.transaction.dto.TransactionInitResponse;
import com.example.transaction.dto.TransferInitRequest;
import com.example.transactionservice.config.Container;
import com.example.transactionservice.entity.TransactionStatus;
import com.example.transactionservice.repository.TransactionsRepository;
import com.example.transactionservice.repository.WalletsRepository;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionsIntegrationTest extends Container {
    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    TransactionsRepository transactionsRepository;
    @Autowired
    WalletsRepository walletsRepository;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    static {
        Container.start();
    }

    @AfterEach
    void clear() {
        transactionsRepository.deleteAll();
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
    @DisplayName("Test create wallet -> init transfer -> confirm transfer flow success")
    void testCreateWalletThanTransfer() {
        UUID userId = UUID.randomUUID();

        CreateWalletRequest createWalletRequest = new CreateWalletRequest();
        createWalletRequest.setName("My wallet");
        createWalletRequest.setCurrencyCode("RUB");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        HttpEntity<CreateWalletRequest> createWalletRequestHttpEntity = new HttpEntity<>(createWalletRequest, headers);

        String createWalletUrl = UriComponentsBuilder.fromPath("/v1/wallets/{userUid}")
                .uriVariables(Map.of("userUid", userId))
                .toUriString();

        ResponseEntity<UUID> createWalletResponse1 = restTemplate.exchange(createWalletUrl, HttpMethod.POST, createWalletRequestHttpEntity, UUID.class);

        assertEquals(1, walletsRepository.findAll().size());

        walletsRepository.findAll().get(0).setBalance(new BigDecimal(10.5));

        UUID wallet1Id = UUID.fromString(createWalletResponse1.getBody().toString());

        ResponseEntity<UUID> createWalletResponse2 = restTemplate.exchange(createWalletUrl, HttpMethod.POST, createWalletRequestHttpEntity, UUID.class);

        assertEquals(2, walletsRepository.findAll().size());

        UUID wallet2Id = UUID.fromString(createWalletResponse2.getBody().toString());

        TransferInitRequest transferInitRequest = new TransferInitRequest();
        transferInitRequest.setType("transfer");
        transferInitRequest.setUserUid(userId);
        transferInitRequest.setWalletFromUid(wallet1Id);
        transferInitRequest.setWalletToUid(wallet2Id);
        transferInitRequest.setAmount(new BigDecimal(0));

        InitTransactionRequest initTransactionRequest = transferInitRequest;

        String initTransferUrl = UriComponentsBuilder.fromPath("/v1/transactions/{type}/init")
                .uriVariables(Map.of("type", "transfer"))
                .toUriString();

        HttpEntity<InitTransactionRequest> initTransactionRequestHttpEntity = new HttpEntity<>(initTransactionRequest, headers);

        ResponseEntity<TransactionInitResponse> initTransferResponse = restTemplate.exchange(initTransferUrl, HttpMethod.POST, initTransactionRequestHttpEntity, TransactionInitResponse.class);

        assertEquals(200, initTransferResponse.getStatusCode().value());
        assertEquals(new BigDecimal(0).doubleValue(), initTransferResponse.getBody().getFee().doubleValue());
        assertNotNull(initTransferResponse.getBody().getToken());

        ConfirmRequest transferConfirmRequest = new ConfirmRequest();
        transferConfirmRequest.setConfirm(true);
        transferConfirmRequest.setToken(initTransferResponse.getBody().getToken());

        String confirmTransferUrl = UriComponentsBuilder.fromPath("/v1/transactions/{type}/confirm")
                .uriVariables(Map.of("type", "transfer"))
                .toUriString();

        HttpEntity<ConfirmRequest> transferConfirmRequestHttpEntity = new HttpEntity<>(transferConfirmRequest, headers);

        ResponseEntity<TransactionConfirmResponse> confirmTransferResponse = restTemplate.exchange(confirmTransferUrl, HttpMethod.POST, transferConfirmRequestHttpEntity, TransactionConfirmResponse.class);

        assertEquals(1, transactionsRepository.findAll().size());
        assertEquals(200, confirmTransferResponse.getStatusCode().value());
        assertEquals(TransactionStatus.COMPLETED.name(), confirmTransferResponse.getBody().getStatus());
    }

//    @Test
//    @DisplayName("Test create wallet -> init deposit -> check status -> receive DepositCompletedEvent flow success")
//    void testCreateWalletThanInitDeposit() {
//        UUID userId = UUID.randomUUID();
//
//        CreateWalletRequest createWalletRequest = new CreateWalletRequest();
//        createWalletRequest.setName("My wallet");
//        createWalletRequest.setCurrencyCode("RUB");
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth("token");
//        HttpEntity<CreateWalletRequest> entity = new HttpEntity<>(createWalletRequest, headers);
//
//        String createWalletUrl = UriComponentsBuilder.fromPath("/v1/wallets/{userUid}")
//                .uriVariables(Map.of("userUid", userId))
//                .toUriString();
//
//        ResponseEntity<UUID> response = restTemplate.exchange(createWalletUrl, HttpMethod.POST, entity, UUID.class);
//
//        assertEquals(1, walletsRepository.findAll().size());
//
//        Wallets actual = walletsRepository.findAll().get(0);
//        assertEquals(200, response.getStatusCode().value());
//        assertEquals(UUID.fromString(response.getBody().toString()), actual.getId());
//    }
//
//    @Test
//    @DisplayName("Test create wallet -> init withdrawal flow success")
//    void testCreateWalletThanInitWithdrawal() {
//        UUID userId = UUID.randomUUID();
//
//        CreateWalletRequest createWalletRequest = new CreateWalletRequest();
//        createWalletRequest.setName("My wallet");
//        createWalletRequest.setCurrencyCode("RUB");
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth("token");
//        HttpEntity<CreateWalletRequest> entity = new HttpEntity<>(createWalletRequest, headers);
//
//        String createWalletUrl = UriComponentsBuilder.fromPath("/v1/wallets/{userUid}")
//                .uriVariables(Map.of("userUid", userId))
//                .toUriString();
//
//        ResponseEntity<UUID> createResponse = restTemplate.exchange(createWalletUrl, HttpMethod.POST, entity, UUID.class);
//
//        assertEquals(1, walletsRepository.findAll().size());
//
//        UUID walletId = UUID.fromString(createResponse.getBody().toString());
//
//        String getWalletUrl = UriComponentsBuilder.fromPath("/v1/wallets/{userUid}/{walletUid}")
//                .uriVariables(Map.of("userUid", userId, "walletUid", walletId))
//                .toUriString();
//
//        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
//
//        ResponseEntity<WalletResponse> getResponse = restTemplate.exchange(getWalletUrl, HttpMethod.GET, getEntity, WalletResponse.class);
//
//        WalletResponse walletResponse = getResponse.getBody();
//        assertEquals(200, getResponse.getStatusCode().value());
//        assertEquals(createWalletRequest.getName(), walletResponse.getName());
//        assertEquals(createWalletRequest.getCurrencyCode(), walletResponse.getCurrencyCode());
//    }
}
