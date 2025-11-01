package com.example.individualsapi.service.impl;

import com.example.dto.TokenResponse;
import com.example.individualsapi.client.KeycloakClient;
import com.example.individualsapi.service.api.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final KeycloakClient client;

    @Override
    public Mono<TokenResponse> requestUserToken(String email, String password) {
        return client.getUserToken(email, password);
    }

    @Override
    public Mono<TokenResponse> refreshUserToken(String refreshToken) {
        return client.refreshToken(refreshToken);
    }

    @Override
    public Mono<TokenResponse> getAdminToken() {
        return client.getAdminToken();
    }
}
