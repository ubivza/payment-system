package com.example.individualsapi.service.impl;

import com.example.individuals.dto.PaymentMethodResponseDto;
import com.example.individualsapi.constant.AuthorizationConstants;
import com.example.individualsapi.exception.InnerServiceException;
import com.example.individualsapi.mapper.PaymentMapper;
import com.example.individualsapi.service.api.PaymentService;
import com.example.individualsapi.service.api.TokenService;
import com.example.payment.api.PaymentApiClient;
import com.example.payment.api.PaymentMethodApiClient;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentMethodApiClient paymentMethodApiClient;
    private final PaymentApiClient paymentApiClient;
    private final PaymentMapper mapper;
    private final TokenService tokenService;

    @Override
    public Flux<PaymentMethodResponseDto> getAvailable(String currencyCode, String countryCode) {
        return tokenService.getAdminToken()
                .flatMapMany(tokenResponse ->
                        Mono.fromCallable(() -> paymentMethodApiClient.getPaymentMethods(AuthorizationConstants.BEARER_SUFFIX + tokenResponse.getAccessToken(), currencyCode, countryCode))
                                .mapNotNull(HttpEntity::getBody)
                                .subscribeOn(Schedulers.boundedElastic())
                                .doOnNext(t -> log.info("Got payment method for pair: {} - {}", currencyCode, countryCode))
                                .flatMapMany(Flux::fromIterable)
                                .map(mapper::mapPaymentMethodResponse)
                                .onErrorMap(e -> {
                                    log.error("Payment service unavailable", e);
                                    return new InnerServiceException("Payment service unavailable, try again later");
                                }));
    }

    @Override
    public Mono<PaymentResponse> create(UUID transactionId, UUID methodId, Double amount, String currency) {
        return tokenService.getAdminToken()
                .flatMap(tokenResponse ->
                        Mono.fromCallable(() -> {
                                    PaymentRequest request = new PaymentRequest();
                                    request.setInternalTransactionUid(transactionId);
                                    request.setMethodId(methodId);
                                    request.setAmount(amount);
                                    request.setCurrency(currency);

                                    return paymentApiClient.createPayment(AuthorizationConstants.BEARER_SUFFIX + tokenResponse.getAccessToken(), request);
                                })
                                .mapNotNull(HttpEntity::getBody)
                                .subscribeOn(Schedulers.boundedElastic())
                                .doOnNext(t -> log.info("Got payment status and provider transaction id: {} - {}", t.getStatus(), t.getProviderTransactionId())))
                .onErrorMap(e -> {
                    log.error("Failed to create payment for transaction {}: {}", transactionId, e.getMessage(), e);

                    return new InnerServiceException("Payment service unavailable, try again later");
                });
    }
}
