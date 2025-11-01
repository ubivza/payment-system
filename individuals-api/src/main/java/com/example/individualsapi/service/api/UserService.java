package com.example.individualsapi.service.api;

import com.example.dto.*;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<TokenResponse> registerUser(UserRegistrationRequest registrationRequest);
    Mono<TokenResponse> loginUser(UserLoginRequest loginRequest);
    Mono<TokenResponse> refreshUserToken(TokenRefreshRequest refreshRequest);
    Mono<UserInfoResponse> getUserInfo();
    Mono<Void> deleteUser();
    Mono<Void> updateUser(UserUpdateRequest userUpdateRequest);
}
