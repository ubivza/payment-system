package com.example.individualsapi.controller;

import com.example.dto.*;
import com.example.individualsapi.config.TestSecurityConfig;
import com.example.individualsapi.filter.RequestLoggingFilter;
import com.example.individualsapi.filter.ResponseLoggingFilter;
import com.example.individualsapi.service.api.UserService;
import com.example.individualsapi.util.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@WebFluxTest(controllers = AuthController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RequestLoggingFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ResponseLoggingFilter.class)
})
@Import(TestSecurityConfig.class)
class AuthControllerUnitTest {
    @Autowired
    private WebTestClient webTestClient;
    @MockitoBean
    private UserService userService;


    @Test
    @DisplayName("Registration 201 CREATED")
    void registration() {
        UserRegistrationRequest registrationRequest = TestUtils.buildMockUserRegistrationRequest();
        TokenResponse response = TestUtils.buildMockTokenResponse();

        when(userService.registerUser(registrationRequest))
                .thenReturn(Mono.just(response));

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
    @DisplayName("Login 200 OK")
    void login() {
        UserLoginRequest userLoginRequest = TestUtils.buildMockUserLoginRequest();
        TokenResponse response = TestUtils.buildMockTokenResponse();

        when(userService.loginUser(userLoginRequest))
                .thenReturn(Mono.just(response));

        webTestClient.post().uri("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userLoginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(TokenResponse.class)
                .isEqualTo(response);
    }

    @Test
    @DisplayName("Refresh token 200 OK")
    void refreshToken() {
        TokenRefreshRequest tokenRefreshRequest = TestUtils.buildMockTokenRefreshRequest();
        TokenResponse tokenResponse = TestUtils.buildMockTokenResponse();

        when(userService.refreshUserToken(tokenRefreshRequest))
                .thenReturn(Mono.just(tokenResponse));

        webTestClient.post().uri("/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tokenRefreshRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(TokenResponse.class)
                .isEqualTo(tokenResponse);
    }

    @Test
    @DisplayName("User info 200 OK")
    void me() {
        UserInfoResponse userInfoResponse = TestUtils.buildMockUserInfoResponse();

        when(userService.getUserInfo())
                .thenReturn(Mono.just(userInfoResponse));

        webTestClient.get().uri("/v1/auth/me")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserInfoResponse.class)
                .isEqualTo(userInfoResponse);
    }
}