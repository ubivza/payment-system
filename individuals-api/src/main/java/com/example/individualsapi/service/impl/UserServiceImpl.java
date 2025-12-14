package com.example.individualsapi.service.impl;

import com.example.individuals.dto.TokenRefreshRequest;
import com.example.individuals.dto.TokenResponse;
import com.example.individuals.dto.UserInfoResponse;
import com.example.individuals.dto.UserLoginRequest;
import com.example.individuals.dto.UserRegistrationRequest;
import com.example.individuals.dto.UserUpdateRequest;
import com.example.individualsapi.client.KeycloakClient;
import com.example.individualsapi.mapper.PersonMapper;
import com.example.individualsapi.service.api.PersonService;
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
    private final PersonService personService;
    private final PersonMapper personMapper;

    @Override
    public Mono<TokenResponse> registerUser(UserRegistrationRequest registrationRequest) {
        return tokenService.getAdminToken()
                .flatMap(tokenResponse ->
                        personService.register(registrationRequest, tokenResponse.getAccessToken())
                                .flatMap(innerId ->
                                        keycloakClient.createUser(registrationRequest, innerId)
                                                .flatMap(response -> {
                                                    if (response.getStatusCode().equals(HttpStatus.CREATED)) {
                                                        metricsCollector.recordRegistration(true);
                                                        return tokenService.requestUserToken(registrationRequest.getEmail(), registrationRequest.getPassword());
                                                    }

                                                    log.error("Something went wrong while register user in keycloak {}", registrationRequest.getEmail());

                                                    return personService.rollbackRegistration(innerId, tokenResponse.getAccessToken())
                                                            .then(Mono.error(new RuntimeException("Something went wrong")));
                                                })));
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
        return uidExtractor.getCurrentUserRequestData()
                .flatMap(userRequestData ->
                        tokenService.getAdminToken()
                                .flatMap(tokenResponse ->
                                        personService.getUserInfo(userRequestData.getInnerId(), userRequestData.getEmail(), tokenResponse.getAccessToken())
                                                .flatMap(userInfo -> {
                                                    log.info("User info retrieved successfully {}", userInfo);
                                                    return Mono.just(personMapper.map(userInfo));
                                                }))
                );
    }

    @Override
    public Mono<Void> deleteUser() {
        return uidExtractor.getCurrentUserRequestData()
                .flatMap(userRequestData ->
                        tokenService.getAdminToken()
                                .flatMap(tokenResponse ->
                                        personService.deleteUser(userRequestData.getInnerId(), tokenResponse.getAccessToken()))
                );
    }

    @Override
    public Mono<Void> updateUser(UserUpdateRequest userUpdateRequest) {
        return uidExtractor.getCurrentUserRequestData()
                .flatMap(userRequestData ->
                        tokenService.getAdminToken()
                                .flatMap(tokenResponse ->
                                        personService.updateUser(userRequestData.getInnerId(), userUpdateRequest, tokenResponse.getAccessToken()))
                );
    }
}
