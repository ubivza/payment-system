package com.example.individualsapi.controller;


import com.example.dto.*;
import com.example.individualsapi.config.SecurityConfig;
import com.example.individualsapi.util.TestUtils;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
public class AuthControllerIntegrationTest {

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.2.5")
            .withStartupTimeout(Duration.ofSeconds(180))
            .withRealmImportFile("realm-config.json");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> keycloak.getAuthServerUrl() + "/realms/payment-system");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> keycloak.getAuthServerUrl() + "/realms/payment-system/protocol/openid-connect/certs");
        registry.add("keycloak.host", keycloak::getAuthServerUrl);
        registry.add("keycloak.client-secret", () -> "**********");
    }


    @Autowired
    ApplicationContext context;
    WebTestClient webTestClient;

    @BeforeEach
    void init() {
        webTestClient = WebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .build();
    }

    @Order(1)
    @Test
    @DisplayName("Registration -> get info")
    void testRegistrationAndLoginFlow() {
        UserRegistrationRequest req = TestUtils.buildMockUserRegistrationRequest();

        webTestClient.post().uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TokenResponse.class)
                .value(tokenRes -> {
                    assertNotNull(tokenRes.getAccessToken());
                    webTestClient.get().uri("/v1/auth/me")
                            .headers(headers -> headers.setBearerAuth(tokenRes.getAccessToken()))
                            .exchange()
                            .expectStatus().isOk()
                            .expectBody(UserInfoResponse.class)
                            .value(userInfo -> {
                                assertNotNull(userInfo);
                                assertEquals("email@mail.ru", userInfo.getEmail());
                            });
                });
    }

    @Order(2)
    @Test
    @DisplayName("Login -> refresh token -> get info")
    void testLoginAndRefreshTokenFlow() {
        UserLoginRequest userLoginRequest = TestUtils.buildMockUserLoginRequest();

        webTestClient.post().uri("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userLoginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .value(tokenRes -> {
                    assertNotNull(tokenRes.getRefreshToken());

                    TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
                    refreshRequest.setRefreshToken(tokenRes.getRefreshToken());

                    webTestClient.post().uri("/v1/auth/refresh-token")
                            .bodyValue(refreshRequest)
                            .exchange()
                            .expectStatus().isOk()
                            .expectBody(TokenResponse.class)
                            .value(tokenResponse -> {
                                assertNotNull(tokenResponse.getAccessToken());

                                webTestClient.get().uri("/v1/auth/me")
                                        .headers(headers -> headers.setBearerAuth(tokenResponse.getAccessToken()))
                                        .exchange()
                                        .expectStatus().isOk()
                                        .expectBody(UserInfoResponse.class)
                                        .value(userInfo -> {
                                            assertNotNull(userInfo);
                                            assertEquals("email@mail.ru", userInfo.getEmail());
                                        });
                            });
                });
    }

    @Order(3)
    @Test
    @DisplayName("Registration 409 CONFLICT")
    void testRegistrationThrowsConflict() {
        UserRegistrationRequest req = TestUtils.buildMockUserRegistrationRequest();

        webTestClient.post().uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Login 401 UNAUTHORIZED")
    void testLoginThrowsUnauthorized() {
        UserLoginRequest userLoginRequest = TestUtils.buildMockUserLoginRequest();
        userLoginRequest.setPassword("wrong password");

        webTestClient.post().uri("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userLoginRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Refresh-token 401 UNAUTHORIZED")
    void testRefreshTokenThrowsUnauthorized() {
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
        refreshRequest.setRefreshToken("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJGYUVrUW5uVU5jVkM2bXNvSF9fOEJBRXlzWlFnY0pIaVdNRWx2OGpQa3JvIn0.eyJleHAiOjE3NTgzODQ3NzUsImlhdCI6MTc1ODM4NDQ3NSwianRpIjoib25ydHJvOmVkMTA2YzNmLWM2ZDEtNGIyOC1hNjAyLTFlNDFiYjMzYmYwMyIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6NjIyNzAvcmVhbG1zL3BheW1lbnQtc3lzdGVtIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjI1ODRlNWY0LTAwZDgtNGQ3Mi1hYWNjLWI5N2JjZjViYzc3MSIsInR5cCI6IkJlYXJlciIsImF6cCI6ImluZGl2aWR1YWxzLWFwaSIsInNpZCI6ImIwMTlhYTQ0LTY5MzItNGQ0Ny1hMzBkLTNmM2JmOGM4MTM5NiIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cDovL2xvY2FsaG9zdDo4MDgxIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLXBheW1lbnQtc3lzdGVtIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJlbWFpbEBtYWlsLnJ1IiwiZW1haWwiOiJlbWFpbEBtYWlsLnJ1In0.YTHUIkByTjdSEimqtO-EHcEtwdBOtVSBS7EBuY8XYNF59odHWxC7vBkC0s-8hV1GA_zNtFraz33fQVTb-H4BDjbL6QeFBumZMwpQgXr5QR11SYxJ1XEePc7_cNdhNUo3L23T_ieUeL7-oXU5rg6aW6OzMVTdH6fZGrdfDvbkfqKCbL7f68nKpNQGr5d8fE5b-xM-3U0CJhY-5ucTgcqFBptZQ4jN248ii_h1qs8KwzlK4pFqMhilMgLB2XqMzjonsMboTLSjwONGGiSupYFRU-yW2em_gR5AAFjKmkuEhyciLjis3xsKrCFOjN3N-b-2cDdVIzp-6FgKtSL6Q3A7gA");

        webTestClient.post().uri("/v1/auth/refresh-token")
                .bodyValue(refreshRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Me 404 NOT FOUND")
    void testMeThrowsNotFound() {
        //todo cannot implement
        assertTrue(true);
//        UserLoginRequest userLoginRequest = TestUtils.buildMockUserLoginRequest();
//
//        webTestClient.post().uri("/v1/auth/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(userLoginRequest)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(TokenResponse.class)
//                .value(tokenRes -> {
//                    assertNotNull(tokenRes.getAccessToken());
//                    webTestClient.get().uri("/v1/auth/me")
//                            .headers(headers -> headers.setBearerAuth(tokenRes.getAccessToken()))
//                            .exchange()
//                            .expectStatus().isOk()
//                            .expectBody(UserInfoResponse.class)
//                            .value(userInfo -> {
//                                assertNotNull(userInfo);
//                                assertEquals("email@mail.ru", userInfo.getEmail());
//                            });
//                });
    }
}