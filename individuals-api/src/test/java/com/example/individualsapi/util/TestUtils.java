package com.example.individualsapi.util;

import com.example.dto.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

public class TestUtils {

    public static UserRegistrationRequest buildMockUserRegistrationRequest() {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest();
        registrationRequest.setEmail("email@mail.ru");
        registrationRequest.setPassword("password123");
        registrationRequest.setConfirmPassword("password123");

        return registrationRequest;
    }

    public static TokenResponse buildMockTokenResponse() {
        TokenResponse response = new TokenResponse();
        response.setAccessToken("access token");
        response.setRefreshToken("refresh token");
        response.setExpiresIn(10);
        response.setTokenType("token type");

        return response;
    }

    public static UserLoginRequest buildMockUserLoginRequest() {
        UserLoginRequest userLoginRequest = new UserLoginRequest();
        userLoginRequest.setEmail("email@mail.ru");
        userLoginRequest.setPassword("password123");

        return userLoginRequest;
    }

    public static TokenRefreshRequest buildMockTokenRefreshRequest() {
        TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest();
        tokenRefreshRequest.setRefreshToken("refresh token");

        return tokenRefreshRequest;
    }

    public static UserInfoResponse buildMockUserInfoResponse() {
        UserInfoResponse userInfoResponse = new UserInfoResponse();
        userInfoResponse.setId("user uid");
        userInfoResponse.setEmail("email@mail.ru");
        userInfoResponse.setRoles(Collections.emptyList());
        userInfoResponse.setCreatedAt(OffsetDateTime.of(2000, 01, 01, 01, 01, 01, 01, ZoneOffset.UTC));

        return userInfoResponse;
    }
}
