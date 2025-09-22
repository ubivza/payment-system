package com.example.individualsapi.service.impl;

import com.example.dto.*;
import com.example.individualsapi.client.KeycloakClient;
import com.example.individualsapi.service.api.TokenService;
import com.example.individualsapi.util.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceImplTest {
    @MockitoBean
    KeycloakClient keycloakClient;
    @MockitoBean
    TokenService tokenService;
    @MockitoBean
    ContextUserSubExtractor uidExtractor;
    @MockitoBean
    MetricsCollector metricsCollector;
    @Autowired
    UserServiceImpl userService;

    @Test
    @DisplayName("User registration happy flow")
    void registerUser() {
        UserRegistrationRequest registrationRequest = TestUtils.buildMockUserRegistrationRequest();
        TokenResponse response = TestUtils.buildMockTokenResponse();

        when(keycloakClient.createUser(registrationRequest))
                .thenReturn(Mono.just(ResponseEntity.status(HttpStatus.CREATED).build()));
        when(tokenService.requestUserToken(registrationRequest.getEmail(), registrationRequest.getPassword()))
                .thenReturn(Mono.just(response));

        Mono<TokenResponse> tokenResponseMono = userService.registerUser(registrationRequest);

        TokenResponse result = tokenResponseMono.block();
        verify(metricsCollector).recordRegistration(true);
        assertNotNull(result);
        assertEquals(response.getAccessToken(), result.getAccessToken());
    }

    @Test
    @DisplayName("User registration error")
    void registerUserError() {
        UserRegistrationRequest registrationRequest = TestUtils.buildMockUserRegistrationRequest();
        TokenResponse response = TestUtils.buildMockTokenResponse();

        when(keycloakClient.createUser(registrationRequest))
                .thenReturn(Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()));
        when(tokenService.requestUserToken(registrationRequest.getEmail(), registrationRequest.getPassword()))
                .thenReturn(Mono.just(response));

        assertThrows(RuntimeException.class, () -> userService.registerUser(registrationRequest).block());
    }

    @Test
    @DisplayName("User login request happy flow")
    void loginUser() {
        UserLoginRequest userLoginRequest = TestUtils.buildMockUserLoginRequest();
        TokenResponse tokenResponse = TestUtils.buildMockTokenResponse();

        when(tokenService.requestUserToken(userLoginRequest.getEmail(), userLoginRequest.getPassword()))
                .thenReturn(Mono.just(tokenResponse));

        Mono<TokenResponse> tokenResponseMono = userService.loginUser(userLoginRequest);

        verify(metricsCollector).recordLogin(true);
        TokenResponse result = tokenResponseMono.block();
        assertNotNull(result);
        assertEquals(tokenResponse.getAccessToken(), result.getAccessToken());
    }

    @Test
    @DisplayName("Refresh token happy flow")
    void refreshUserToken() {
        TokenRefreshRequest tokenRefreshRequest = TestUtils.buildMockTokenRefreshRequest();
        TokenResponse tokenResponse = TestUtils.buildMockTokenResponse();

        when(tokenService.refreshUserToken(tokenRefreshRequest.getRefreshToken()))
                .thenReturn(Mono.just(tokenResponse));

        Mono<TokenResponse> tokenResponseMono = userService.refreshUserToken(tokenRefreshRequest);

        TokenResponse result = tokenResponseMono.block();
        assertNotNull(result);
        assertEquals(tokenResponse.getAccessToken(), result.getAccessToken());
    }

    @Test
    @DisplayName("User get info happy flow")
    void getUserInfo() {
        String userUid = "user uid";
        UserInfoResponse userInfoResponse = TestUtils.buildMockUserInfoResponse();

        when(uidExtractor.getCurrentUserSub())
                .thenReturn(Mono.just(userUid));
        when(keycloakClient.getUserInfo(userUid))
                .thenReturn(Mono.just(userInfoResponse));

        Mono<UserInfoResponse> userInfo = userService.getUserInfo();

        UserInfoResponse response = userInfo.block();
        assertNotNull(response);
        assertEquals(userInfoResponse.getEmail(), response.getEmail());
    }
}