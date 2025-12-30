package com.example.individualsapi.service.impl;

import com.example.individuals.dto.CreateWalletRequestDto;
import com.example.individuals.dto.WalletResponseDto;
import com.example.individualsapi.constant.AuthorizationConstants;
import com.example.individualsapi.mapper.WalletMapper;
import com.example.individualsapi.service.api.TokenService;
import com.example.individualsapi.service.api.WalletService;
import com.example.transaction.api.WalletApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletApiClient apiClient;
    private final TokenService tokenService;
    private final ContextUserSubExtractor uidExtractor;
    private final WalletMapper mapper;

    @Override
    public Mono<UUID> create(CreateWalletRequestDto createWalletRequest) {
        return uidExtractor.getCurrentUserRequestData()
                .flatMap(userRequestData ->
                        tokenService.getAdminToken()
                                .flatMap(tokenResponse ->
                                        Mono.fromCallable(() ->
                                                        apiClient.createWallet(AuthorizationConstants.BEARER_SUFFIX + tokenResponse.getAccessToken(), userRequestData.getInnerId(), mapper.mapCreate(createWalletRequest)))
                                                .mapNotNull(HttpEntity::getBody)
                                                .subscribeOn(Schedulers.boundedElastic())
                                                .doOnNext(t -> log.info("Created wallet for user with innerId: {}", userRequestData.getInnerId()))));
    }

    @Override
    public Mono<WalletResponseDto> get(String walletUid) {
        return uidExtractor.getCurrentUserRequestData()
                .flatMap(userRequestData ->
                        tokenService.getAdminToken()
                                .flatMap(tokenResponse ->
                                        Mono.fromCallable(() ->
                                                        apiClient.getWallet(AuthorizationConstants.BEARER_SUFFIX + tokenResponse.getAccessToken(), walletUid, userRequestData.getInnerId()))
                                                .mapNotNull(HttpEntity::getBody)
                                                .subscribeOn(Schedulers.boundedElastic())
                                                .doOnNext(t -> log.info("Got wallet info for user with innerId: {}", userRequestData.getInnerId()))
                                                .map(mapper::mapGetResponse)));
    }
}
