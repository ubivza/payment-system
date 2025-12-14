package com.example.individualsapi.controller;

import com.example.individuals.dto.TokenResponse;
import com.example.individuals.dto.UserRegistrationRequest;
import com.example.individuals.dto.UserUpdateRequest;
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

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class IndividualsControllerTest extends Container {

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
    @DisplayName("Registration -> update")
    void testRegistrationAndUpdateFlow() {
        UserRegistrationRequest req = TestUtils.buildMockUserRegistrationRequest();
        UserUpdateRequest updateRequest = TestUtils.buildMockUserUpdateRequest();

        webTestClient.post().uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TokenResponse.class)
                .value(tokenRes -> {
                    assertNotNull(tokenRes.getAccessToken());
                    webTestClient.put().uri("/v1/individual/update")
                            .headers(headers -> headers.setBearerAuth(tokenRes.getAccessToken()))
                            .bodyValue(updateRequest)
                            .exchange()
                            .expectStatus().isOk();
                });
    }

    @Test
    @DisplayName("Registration -> delete")
    void testRegistrationAndDeleteFlow() {
        UserRegistrationRequest req = TestUtils.buildMockUserRegistrationRequest();

        webTestClient.post().uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TokenResponse.class)
                .value(tokenRes -> {
                    assertNotNull(tokenRes.getAccessToken());
                    webTestClient.delete().uri("/v1/individual/delete")
                            .headers(headers -> headers.setBearerAuth(tokenRes.getAccessToken()))
                            .exchange()
                            .expectStatus().isOk();
                });
    }
}