package com.example.individualsapi.controller;

import com.example.dto.TokenResponse;
import com.example.dto.UserRegistrationRequest;
import com.example.individualsapi.config.TestSecurityConfig;
import com.example.individualsapi.service.api.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {
    @Autowired
    private WebTestClient webTestClient;
    @MockitoBean
    private UserService userService;


    @Test
    @DisplayName("Registration 201 CREATED")
    void registrationHappyFlow() {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest();
        registrationRequest.setEmail("email");
        registrationRequest.setPassword("password");
        registrationRequest.setConfirmPassword("password");

        TokenResponse response = new TokenResponse();
        response.setAccessToken("access token");
        response.setRefreshToken("refresh token");
        response.setExpiresIn(10);
        response.setTokenType("token type");

        Mockito.when(userService.registerUser(registrationRequest)).thenReturn(Mono.just(response));

        webTestClient.post().uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registrationRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(TokenResponse.class)
                .isEqualTo(response);
    }

    @Test
    void login() {
    }

    @Test
    void refreshToken() {
    }

    @Test
    void me() {
    }
}