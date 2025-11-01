package com.example.individualsapi.service.impl;

import com.example.dto.UserRegistrationRequest;
import com.example.dto.UserUpdateRequest;
import com.example.individualsapi.mapper.PersonMapper;
import com.example.individualsapi.service.api.PersonService;
import com.example.person.api.PersonApiClient;
import com.example.person.dto.IndividualDto;
import com.example.person.dto.RegistrationResponse;
import com.example.person.dto.UpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {
    private final PersonApiClient apiClient;
    private final PersonMapper mapper;
    private static final String BEARER_SUFFIX = "Bearer ";

    public Mono<String> register(UserRegistrationRequest registrationRequest, String accessToken) {
        IndividualDto individualDto = mapper.map(registrationRequest);

        log.info("Sending user registration request: {}", individualDto);
        ResponseEntity<RegistrationResponse> registrationResult = apiClient.registration(BEARER_SUFFIX + accessToken, individualDto);

        return Mono.just(registrationResult.getBody().getUserUid());
    }

    public Mono<Void> rollbackRegistration(String innerId, String accessToken) {
        apiClient.compensateFailedRegistration(UUID.fromString(innerId), BEARER_SUFFIX + accessToken);
        log.info("Compensated registration failure, user with id {} deleted", innerId);
        return Mono.empty();
    }

    public Mono<IndividualDto> getUserInfo(String innerId, String email, String accessToken) {
        log.info("Getting information about user with innerId: {}", innerId);
        return Mono.just(apiClient.getIndividualById(UUID.fromString(innerId), email, BEARER_SUFFIX + accessToken).getBody());
    }

    public Mono<Void> deleteUser(String innerId, String accessToken) {
        log.info("Deactivating user with innerId: {}", innerId);
        apiClient.deleteIndividual(UUID.fromString(innerId), BEARER_SUFFIX + accessToken);
        return Mono.empty();
    }

    public Mono<Void> updateUser(String innerId, UserUpdateRequest userUpdateRequest, String accessToken) {
        UpdateDto individualDto = mapper.map(userUpdateRequest);
        log.info("Updating information about user: {}", individualDto);
        apiClient.updateIndividualById(UUID.fromString(innerId), BEARER_SUFFIX + accessToken, individualDto);
        return Mono.empty();
    }
}
