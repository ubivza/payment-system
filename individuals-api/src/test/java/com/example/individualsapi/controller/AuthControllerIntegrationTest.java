package com.example.individualsapi.controller;


import com.example.dto.*;
import com.example.individualsapi.config.KeycloakTestContainerConfig;
import com.example.individualsapi.util.TestUtils;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@Import(KeycloakTestContainerConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AuthControllerIntegrationTest {

    @Autowired
    ApplicationContext context;
    WebTestClient webTestClient;
    @Value("${keycloak.client-id}")
    String clientId;
    @Value("${keycloak.realm}")
    String realm;
    @Value("${keycloak.client-secret}")
    String clientSecret;
    @Autowired
    Environment environment;

    @BeforeEach
    void init() {
        webTestClient = WebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .build();

//        await().atMost(15, TimeUnit.SECONDS)
//                .pollInterval(1, TimeUnit.SECONDS)
//                .until(() -> {
//                    try {
//                        getKeycloakInstance().realms().findAll();
//                        return true;
//                    } catch (Exception e) {
//                        return false;
//                    }
//                });
    }

//    @AfterEach
//    void tearDown() {
//        for (UserRepresentation userRepresentation : getKeycloakInstance().realm(realm).users().list()) {
//            getKeycloakInstance().realm(realm).users().get(userRepresentation.getId()).remove();
//        }
//    }

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

    @Test
    @DisplayName("Registration -> login -> refresh token -> get info")
    void testLoginAndRefreshTokenFlow() {
        UserRegistrationRequest userRegistrationRequest = TestUtils.buildMockUserRegistrationRequest();

        webTestClient.post().uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRegistrationRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TokenResponse.class)
                .value(registrationResponse -> {
                    assertNotNull(registrationResponse.getAccessToken());

                    UserLoginRequest userLoginRequest = new UserLoginRequest();
                    userLoginRequest.setEmail(userRegistrationRequest.getEmail());
                    userLoginRequest.setPassword(userRegistrationRequest.getPassword());

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
                });
    }

    @Test
    @DisplayName("Registration 409 CONFLICT")
    void testRegistrationThrowsConflict() {
        UserRegistrationRequest req = TestUtils.buildMockUserRegistrationRequest();

        webTestClient.post().uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated();
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
    @DisplayName("Me without header 401 UNAUTHORIZED")
    void testGetUserThrowsUnauthorized() {
        webTestClient.get().uri("/v1/auth/me")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Me with wrong jwt 401 UNAUTHORIZED")
    void testGetUserThrowsUnauthorizedWithToken() {
        webTestClient.get().uri("/v1/auth/me")
                .headers(headers -> headers.setBearerAuth("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJGYUVrUW5uVU5jVkM2bXNvSF9fOEJBRXlzWlFnY0pIaVdNRWx2OGpQa3JvIn0.eyJleHAiOjE3NTgzODQ3NzUsImlhdCI6MTc1ODM4NDQ3NSwianRpIjoib25ydHJvOmVkMTA2YzNmLWM2ZDEtNGIyOC1hNjAyLTFlNDFiYjMzYmYwMyIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6NjIyNzAvcmVhbG1zL3BheW1lbnQtc3lzdGVtIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjI1ODRlNWY0LTAwZDgtNGQ3Mi1hYWNjLWI5N2JjZjViYzc3MSIsInR5cCI6IkJlYXJlciIsImF6cCI6ImluZGl2aWR1YWxzLWFwaSIsInNpZCI6ImIwMTlhYTQ0LTY5MzItNGQ0Ny1hMzBkLTNmM2JmOGM4MTM5NiIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cDovL2xvY2FsaG9zdDo4MDgxIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLXBheW1lbnQtc3lzdGVtIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJlbWFpbEBtYWlsLnJ1IiwiZW1haWwiOiJlbWFpbEBtYWlsLnJ1In0.YTHUIkByTjdSEimqtO-EHcEtwdBOtVSBS7EBuY8XYNF59odHWxC7vBkC0s-8hV1GA_zNtFraz33fQVTb-H4BDjbL6QeFBumZMwpQgXr5QR11SYxJ1XEePc7_cNdhNUo3L23T_ieUeL7-oXU5rg6aW6OzMVTdH6fZGrdfDvbkfqKCbL7f68nKpNQGr5d8fE5b-xM-3U0CJhY-5ucTgcqFBptZQ4jN248ii_h1qs8KwzlK4pFqMhilMgLB2XqMzjonsMboTLSjwONGGiSupYFRU-yW2em_gR5AAFjKmkuEhyciLjis3xsKrCFOjN3N-b-2cDdVIzp-6FgKtSL6Q3A7gA"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

//    private Keycloak getKeycloakInstance() {
//        return KeycloakBuilder.builder()
//                .serverUrl(keycloak.getAuthServerUrl())
//                .realm(realm)
//                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
//                .clientId(clientId)
//                .clientSecret(clientSecret)
//                .build();
//    }
}