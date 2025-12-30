package com.example.individualsapi.util;

import com.example.individuals.dto.TokenRefreshRequest;
import com.example.individuals.dto.TokenResponse;
import com.example.individuals.dto.UserInfoResponse;
import com.example.individuals.dto.UserLoginRequest;
import com.example.individuals.dto.UserRegistrationRequest;
import com.example.individuals.dto.UserUpdateRequest;
import com.example.person.dto.IndividualDto;

public class TestUtils {

    public static UserRegistrationRequest buildMockUserRegistrationRequest() {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest();

        registrationRequest.setEmail("email@mail.ru");
        registrationRequest.setPassword("password123");
        registrationRequest.setConfirmPassword("password123");
        registrationRequest.setPassportNumber("1234567890");
        registrationRequest.setPhoneNumber("+79991234567");
        registrationRequest.setEmail("email@mail.ru");
        registrationRequest.setFirstName("Иван");
        registrationRequest.setLastName("Петров");
        registrationRequest.setAddress("ул. Примерная, д. 123");
        registrationRequest.setZipCode("123456");
        registrationRequest.setCity("Москва");
        registrationRequest.setState("Московская область");
        registrationRequest.setName("Россия");
        registrationRequest.setAlpha2("RU");
        registrationRequest.setAlpha3("RUS");
        registrationRequest.setSecretKey("secretKey");

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

        userInfoResponse.setEmail("email@mail.ru");
        userInfoResponse.setPassportNumber("1234567890");
        userInfoResponse.setPhoneNumber("+79991234567");
        userInfoResponse.setEmail("email@mail.ru");
        userInfoResponse.setFirstName("Иван");
        userInfoResponse.setLastName("Петров");
        userInfoResponse.setAddress("ул. Примерная, д. 123");
        userInfoResponse.setZipCode("123456");
        userInfoResponse.setCity("Москва");
        userInfoResponse.setState("Московская область");
        userInfoResponse.setName("Россия");
        userInfoResponse.setAlpha2("RU");
        userInfoResponse.setAlpha3("RUS");
        userInfoResponse.setSecretKey("secretKey");

        return userInfoResponse;
    }

    public static IndividualDto buildMockIndividualDto() {
        IndividualDto dto = new IndividualDto();

        dto.setPassportNumber("1234567890");
        dto.setPhoneNumber("+79991234567");
        dto.setEmail("email@mail.ru");
        dto.setFirstName("Иван");
        dto.setLastName("Петров");
        dto.setAddress("ул. Примерная, д. 123");
        dto.setZipCode("123456");
        dto.setCity("Москва");
        dto.setState("Московская область");
        dto.setName("Россия");
        dto.setAlpha2("RU");
        dto.setAlpha3("RUS");
        dto.setStatus("ACTIVE");
        dto.setUserStatus("VERIFIED");
        dto.setSecretKey("secretKey");
        dto.setFilled(true);

        return dto;
    }

    public static UserUpdateRequest buildMockUserUpdateRequest() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();

        updateRequest.setPassportNumber("1234567890");
        updateRequest.setPhoneNumber("+79991234567");
        updateRequest.setSecretKey("secretKey");
        updateRequest.setFirstName("Иван");
        updateRequest.setLastName("Петров");
        updateRequest.setAddress("ул. Примерная, д. 123");
        updateRequest.setZipCode("123456");
        updateRequest.setCity("Москва");
        updateRequest.setState("Московская область");
        updateRequest.setName("Россия");
        updateRequest.setAlpha2("RU");
        updateRequest.setAlpha3("RUS");

        return updateRequest;
    }
}
