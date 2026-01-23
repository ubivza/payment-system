package com.example.individualsapi.service.impl;

import com.example.currency.api.CurrencyRateApiClient;
import com.example.currency.dto.RateResponse;
import com.example.individualsapi.constant.AuthorizationConstants;
import com.example.individualsapi.service.api.CurrencyRateService;
import com.example.individualsapi.service.api.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyRateServiceImpl implements CurrencyRateService {
    private final CurrencyRateApiClient apiClient;
    private final TokenService tokenService;

    @Override
    public Mono<RateResponse> getActualRates(String from, String to) {
        return tokenService.getAdminToken()
                .flatMap(tokenResponse ->
                        Mono.fromCallable(() ->
                                apiClient.getRate(AuthorizationConstants.BEARER_SUFFIX + tokenResponse.getAccessToken(), from, to, OffsetDateTime.now(ZoneOffset.UTC))))
                .mapNotNull(HttpEntity::getBody)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(t -> log.info("Requested actual rates for: {} to {}", from, to));
    }
}
