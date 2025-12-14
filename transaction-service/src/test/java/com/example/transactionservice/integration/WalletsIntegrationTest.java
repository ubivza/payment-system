package com.example.transactionservice.integration;

import com.example.transaction.dto.CreateWalletRequest;
import com.example.transaction.dto.ErrorResponse;
import com.example.transaction.dto.WalletResponse;
import com.example.transactionservice.config.Container;
import com.example.transactionservice.entity.Wallets;
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

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WalletsIntegrationTest extends Container {
    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    WalletsRepository walletsRepository;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    static {
        Container.start();
    }

    @AfterEach
    void clear() {
        walletsRepository.deleteAll();
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
    @DisplayName("Test create wallet flow success")
    void testCreateWallet() {
        UUID userId = UUID.randomUUID();

        CreateWalletRequest createWalletRequest = new CreateWalletRequest();
        createWalletRequest.setName("My wallet");
        createWalletRequest.setCurrencyCode("RUB");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        HttpEntity<CreateWalletRequest> entity = new HttpEntity<>(createWalletRequest, headers);

        String createWalletUrl = UriComponentsBuilder.fromPath("/v1/wallets/{userUid}")
                .uriVariables(Map.of("userUid", userId))
                .toUriString();

        ResponseEntity<UUID> response = restTemplate.exchange(createWalletUrl, HttpMethod.POST, entity, UUID.class);

        assertEquals(1, walletsRepository.findAll().size());

        Wallets actual = walletsRepository.findAll().get(0);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(UUID.fromString(response.getBody().toString()), actual.getId());
    }

    @Test
    @DisplayName("Test create -> get wallet flow success")
    void testCreateThanGetWallet() {
        UUID userId = UUID.randomUUID();

        CreateWalletRequest createWalletRequest = new CreateWalletRequest();
        createWalletRequest.setName("My wallet");
        createWalletRequest.setCurrencyCode("RUB");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        HttpEntity<CreateWalletRequest> entity = new HttpEntity<>(createWalletRequest, headers);

        String createWalletUrl = UriComponentsBuilder.fromPath("/v1/wallets/{userUid}")
                .uriVariables(Map.of("userUid", userId))
                .toUriString();

        ResponseEntity<UUID> createResponse = restTemplate.exchange(createWalletUrl, HttpMethod.POST, entity, UUID.class);

        assertEquals(1, walletsRepository.findAll().size());

        UUID walletId = UUID.fromString(createResponse.getBody().toString());

        String getWalletUrl = UriComponentsBuilder.fromPath("/v1/wallets/{userUid}/{walletUid}")
                .uriVariables(Map.of("userUid", userId, "walletUid", walletId))
                .toUriString();

        HttpEntity<Void> getEntity = new HttpEntity<>(headers);

        ResponseEntity<WalletResponse> getResponse = restTemplate.exchange(getWalletUrl, HttpMethod.GET, getEntity, WalletResponse.class);

        WalletResponse walletResponse = getResponse.getBody();
        assertEquals(200, getResponse.getStatusCode().value());
        assertEquals(createWalletRequest.getName(), walletResponse.getName());
        assertEquals(createWalletRequest.getCurrencyCode(), walletResponse.getCurrencyCode());
    }

    @Test
    @DisplayName("Test get wallet flow fail")
    void testGetWalletFail() {
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");

        String getWalletUrl = UriComponentsBuilder.fromPath("/v1/wallets/{userUid}/{walletUid}")
                .uriVariables(Map.of("userUid", userId, "walletUid", walletId))
                .toUriString();

        HttpEntity<Void> getEntity = new HttpEntity<>(headers);

        ResponseEntity<ErrorResponse> getResponse = restTemplate.exchange(getWalletUrl, HttpMethod.GET, getEntity, ErrorResponse.class);

        assertEquals(404, getResponse.getStatusCode().value());
    }
}
