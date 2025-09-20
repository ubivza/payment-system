package com.example.individualsapi.service.impl;

import com.example.dto.*;
import com.example.individualsapi.client.KeycloakClient;
import com.example.individualsapi.service.api.TokenService;
import com.example.individualsapi.service.api.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final TokenService tokenService;
    private final KeycloakClient keycloakClient;
    private final ContextUserSubExtractor uidExtractor;
    private final MetricsCollector metricsCollector;

    @Override
    public Mono<TokenResponse> registerUser(UserRegistrationRequest registrationRequest) {
        System.out.println();
        return keycloakClient.createUser(registrationRequest)
                .flatMap(response -> {
                    if (response.getStatusCode().equals(HttpStatus.CREATED)) {
                        metricsCollector.recordRegistration(true);
                        return tokenService.requestUserToken(registrationRequest.getEmail(), registrationRequest.getPassword());
                    }
                    log.error("Something went wrong while register user {}", registrationRequest.getEmail());
                    return Mono.error(new RuntimeException("Something went wrong"));
                });
    }

    @Override
    public Mono<TokenResponse> loginUser(UserLoginRequest loginRequest) {
        Mono<TokenResponse> tokenResponseMono = tokenService.requestUserToken(loginRequest.getEmail(), loginRequest.getPassword());
        metricsCollector.recordLogin(true);
        return tokenResponseMono;
    }

    @Override
    public Mono<TokenResponse> refreshUserToken(TokenRefreshRequest refreshRequest) {
        return tokenService.refreshUserToken(refreshRequest.getRefreshToken());
    }

    @Override
    public Mono<UserInfoResponse> getUserInfo() {
        return uidExtractor.getCurrentUserSub().flatMap(keycloakClient::getUserInfo);
    }
}
