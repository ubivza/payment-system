package com.example.individualsapi.service.api;

import com.example.individuals.dto.TokenRefreshRequest;
import com.example.individuals.dto.TokenResponse;
import com.example.individuals.dto.UserInfoResponse;
import com.example.individuals.dto.UserLoginRequest;
import com.example.individuals.dto.UserRegistrationRequest;
import com.example.individuals.dto.UserUpdateRequest;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<TokenResponse> registerUser(UserRegistrationRequest registrationRequest);
    Mono<TokenResponse> loginUser(UserLoginRequest loginRequest);
    Mono<TokenResponse> refreshUserToken(TokenRefreshRequest refreshRequest);
    Mono<UserInfoResponse> getUserInfo();
    Mono<Void> deleteUser();
    Mono<Void> updateUser(UserUpdateRequest userUpdateRequest);
}
