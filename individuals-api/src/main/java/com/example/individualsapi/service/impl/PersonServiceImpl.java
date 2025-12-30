package com.example.individualsapi.service.impl;

import com.example.individuals.dto.UserRegistrationRequest;
import com.example.individuals.dto.UserUpdateRequest;
import com.example.individualsapi.constant.AuthorizationConstants;
import com.example.individualsapi.mapper.PersonMapper;
import com.example.individualsapi.service.api.PersonService;
import com.example.person.api.PersonApiClient;
import com.example.person.dto.IndividualDto;
import com.example.person.dto.RegistrationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {
    private final PersonApiClient apiClient;
    private final PersonMapper mapper;

    public Mono<String> register(UserRegistrationRequest registrationRequest, String accessToken) {
        return Mono.fromCallable(() -> apiClient.registration(AuthorizationConstants.BEARER_SUFFIX + accessToken, mapper.map(registrationRequest)))
                .mapNotNull(HttpEntity::getBody)
                .map(RegistrationResponse::getUserUid)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(t -> log.info("Person registered id: {}", t));
    }

    public Mono<Void> rollbackRegistration(String innerId, String accessToken) {
        return Mono.fromRunnable(() -> apiClient.compensateFailedRegistration(UUID.fromString(innerId), AuthorizationConstants.BEARER_SUFFIX + accessToken))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(t -> log.info("Compensated registration failure, user with id {} deleted", innerId))
                .then();
    }

    public Mono<IndividualDto> getUserInfo(String innerId, String email, String accessToken) {
        return Mono.fromCallable(() -> apiClient.getIndividualByIdAndEmail(UUID.fromString(innerId), email, AuthorizationConstants.BEARER_SUFFIX + accessToken))
                .mapNotNull(HttpEntity::getBody)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(t -> log.info("Got information about user with innerId: {}", innerId));
    }

    public Mono<Void> deleteUser(String innerId, String accessToken) {
        return Mono.fromRunnable(() -> apiClient.deleteIndividual(UUID.fromString(innerId), AuthorizationConstants.BEARER_SUFFIX + accessToken))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(t -> log.info("Deactivated user with innerId: {}", innerId))
                .then();
    }

    public Mono<Void> updateUser(String innerId, UserUpdateRequest userUpdateRequest, String accessToken) {
        return Mono.fromCallable(() -> apiClient.updateIndividualById(UUID.fromString(innerId), AuthorizationConstants.BEARER_SUFFIX + accessToken, mapper.map(userUpdateRequest)))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(t -> log.info("Person registered id: {}", t))
                .then();
    }
}
