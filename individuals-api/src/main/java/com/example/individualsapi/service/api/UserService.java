package com.example.individualsapi.service.api;

import com.example.dto.*;
import reactor.core.publisher.Mono;

import java.util.stream.DoubleStream;

public interface UserService {
    Mono<TokenResponse> registerUser(UserRegistrationRequest registrationRequest);
    Mono<TokenResponse> loginUser(UserLoginRequest loginRequest);
    Mono<TokenResponse> refreshUserToken(TokenRefreshRequest refreshRequest);
    Mono<UserInfoResponse> getUserInfo();
}
