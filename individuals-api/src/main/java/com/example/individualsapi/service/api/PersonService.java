package com.example.individualsapi.service.api;

import com.example.dto.UserRegistrationRequest;
import com.example.dto.UserUpdateRequest;
import com.example.person.dto.IndividualDto;
import reactor.core.publisher.Mono;

public interface PersonService {
    Mono<String> register(UserRegistrationRequest registrationRequest, String accessToken);
    Mono<Void> rollbackRegistration(String innerId, String accessToken);
    Mono<IndividualDto> getUserInfo(String innerId, String email, String accessToken);
    Mono<Void> deleteUser(String innerId, String accessToken);
    Mono<Void> updateUser(String innerId, UserUpdateRequest userUpdateRequest, String accessToken);
}
