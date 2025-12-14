package com.example.transactionservice.integration;

import com.example.transaction.dto.CreateWalletRequest;
import com.example.transactionservice.config.PGSQLContainer;
import com.example.transactionservice.entity.Wallets;
import com.example.transactionservice.repository.WalletsRepository;
import com.example.transactionservice.service.api.WalletService;
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
public class IntegrationWalletsTest extends PGSQLContainer {

    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    WalletsRepository walletsRepository;
    @Autowired
    WalletService walletService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

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
    void testRegistration() {
        String userId = UUID.randomUUID().toString();

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
        assertEquals(201, response.getStatusCode().value());
        assertEquals(UUID.fromString(response.getBody().toString()), actual.getId());
    }

//    @Test
//    @DisplayName("Test get wallet flow success")
//    void testCompensateFailedRegistration() {
//        IndividualDto dto = createTestIndividualDto();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth("token");
//        HttpEntity<IndividualDto> entity = new HttpEntity<>(dto, headers);
//
//        ResponseEntity<RegistrationResponse> response = restTemplate.postForEntity("/v1/individuals", entity, RegistrationResponse.class);
//
//        assertEquals(1, individualRepository.findAll().size());
//
//        ResponseEntity<Void> compensateResponse = restTemplate.exchange(String.format("/v1/individuals/compensate-registration/%s", response.getBody().getUserUid()), HttpMethod.DELETE, entity, Void.class);
//
//        assertEquals(200, compensateResponse.getStatusCode().value());
//        assertEquals(0, individualRepository.findAll().size());
//    }
//
//    @Test
//    @DisplayName("Test get wallet flow fail")
//    void testDeleteIndividual() {
//        IndividualDto dto = createTestIndividualDto();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth("token");
//        HttpEntity<IndividualDto> entity = new HttpEntity<>(dto, headers);
//
//        ResponseEntity<RegistrationResponse> response = restTemplate.postForEntity("/v1/individuals", entity, RegistrationResponse.class);
//
//        assertEquals(1, individualRepository.findAll().size());
//
//        String deleteUrl = UriComponentsBuilder.fromPath("/v1/individuals/{id}")
//                .uriVariables(Map.of("id", response.getBody().getUserUid()))
//                .toUriString();
//
//        ResponseEntity<Void> deleteResponse = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);
//
//        Individual actual = individualRepository.findAll().get(0);
//        assertEquals(200, deleteResponse.getStatusCode().value());
//        assertEquals(1, individualRepository.findAll().size());
//        assertEquals("INACTIVE", actual.getStatus());
//    }
}
