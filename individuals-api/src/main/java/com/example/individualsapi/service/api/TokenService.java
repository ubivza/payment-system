package com.example.individualsapi.service.api;

import com.example.dto.TokenResponse;
import reactor.core.publisher.Mono;

public interface TokenService {
    Mono<TokenResponse> requestUserToken(String email, String password);
    Mono<TokenResponse> refreshUserToken(String refreshToken);
    Mono<TokenResponse> getAdminToken();
}
