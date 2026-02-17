package com.example.individualsapi.service.impl;

import com.example.individuals.dto.ConfirmRequestDto;
import com.example.individuals.dto.DepositInitRequestDto;
import com.example.individuals.dto.InitTransactionRequest;
import com.example.individuals.dto.TransactionConfirmResponseDto;
import com.example.individuals.dto.TransactionInitResponseDto;
import com.example.individuals.dto.TransactionStatusResponseDto;
import com.example.individuals.dto.TransferInitRequestDto;
import com.example.individuals.dto.WithdrawalInitRequestDto;
import com.example.individualsapi.constant.AuthorizationConstants;
import com.example.individualsapi.exception.InnerServiceException;
import com.example.individualsapi.mapper.TransactionMapper;
import com.example.individualsapi.service.api.CurrencyRateService;
import com.example.individualsapi.service.api.PaymentService;
import com.example.individualsapi.service.api.TokenService;
import com.example.individualsapi.service.api.TransactionService;
import com.example.payment.dto.PaymentResponse;
import com.example.transaction.api.TransactionApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionApiClient apiClient;
    private final TokenService tokenService;
    private final ContextUserSubExtractor uidExtractor;
    private final TransactionMapper mapper;
    private final CurrencyRateService rateService;
    private final PaymentService paymentService;

    @Override
    public Mono<TransactionInitResponseDto> init(String type, InitTransactionRequest initTransactionRequest, String valuteFrom, String valuteTo) {
        return uidExtractor.getCurrentUserRequestData()
                .flatMap(userRequestData -> {
                            Mono<com.example.transaction.dto.InitTransactionRequest> innerInitRequestMono;
                            switch (type) {
                                case "withdrawal" ->
                                        innerInitRequestMono = Mono.just(mapper.mapWithdrawal((WithdrawalInitRequestDto) initTransactionRequest, userRequestData.getInnerId()));
                                case "deposit" ->
                                        innerInitRequestMono = Mono.just(mapper.mapDeposit((DepositInitRequestDto) initTransactionRequest, userRequestData.getInnerId()));
                                case "transfer" -> innerInitRequestMono = rateService.getActualRates(valuteFrom, valuteTo)
                                        .doOnError(request -> {
                                            log.error("Rate service unavailable");
                                            throw new InnerServiceException("Rate service unavailable, try again later");
                                        })
                                        .map(rateResponse -> mapper.mapTransfer((TransferInitRequestDto) initTransactionRequest, userRequestData.getInnerId(), rateResponse.getRate()));
                                default -> {
                                    return Mono.error(new RuntimeException("Unsupported transaction type: " + type));
                                }
                            }
                            return innerInitRequestMono
                                    .flatMap(innerInitRequest ->
                                            tokenService.getAdminToken()
                                                    .flatMap(tokenResponse ->
                                                            Mono.fromCallable(() ->
                                                                            apiClient.initTransaction(AuthorizationConstants.BEARER_SUFFIX + tokenResponse.getAccessToken(), type, innerInitRequest, valuteFrom, valuteTo))
                                                                    .doOnError(request -> {
                                                                        log.error("Transactions service unavailable");
                                                                        throw new InnerServiceException("Transactions service unavailable, try again later");
                                                                    })
                                                                    .mapNotNull(HttpEntity::getBody)
                                                                    .subscribeOn(Schedulers.boundedElastic())
                                                                    .doOnNext(t -> log.info("Got transaction initialization for user with innerId: {}", userRequestData.getInnerId()))
                                                                    .map(mapper::mapInitResponse)));
                        }
                );
    }

    @Override
    public Mono<TransactionConfirmResponseDto> confirm(String type, ConfirmRequestDto confirmRequestDto) {
        return uidExtractor.getCurrentUserRequestData()
                .flatMap(userRequestData ->
                        tokenService.getAdminToken()
                                .flatMap(tokenResponse ->
                                        Mono.fromCallable(() ->
                                                        apiClient.confirmTransaction(AuthorizationConstants.BEARER_SUFFIX + tokenResponse.getAccessToken(), type, mapper.mapConfirm(confirmRequestDto)))
                                                .onErrorMap(request -> {
                                                    log.error("Transactions service unavailable");
                                                    return new InnerServiceException("Transactions service unavailable, try again later");
                                                })
                                                .mapNotNull(HttpEntity::getBody)
                                                .subscribeOn(Schedulers.boundedElastic())
                                                .doOnNext(t -> log.info("Got transaction confirmation for user with innerId: {}", userRequestData.getInnerId()))
                                                .map(mapper::mapConfirmRequest)))
                .flatMap(response ->
                        paymentService.create(response.getTransactionId(), confirmRequestDto.getMethodId(), confirmRequestDto.getAmount().doubleValue(), confirmRequestDto.getCurrency())
                                .onErrorResume(paymentError ->
                                        tokenService.getAdminToken()
                                                .flatMap(tokenResponse ->
                                                        Mono.fromCallable(() ->
                                                                apiClient.compensateFailedTransaction(response.getTransactionId(), type, AuthorizationConstants.BEARER_SUFFIX + tokenResponse.getAccessToken())))
                                                .subscribeOn(Schedulers.boundedElastic())
                                                .then(Mono.just(new PaymentResponse())))
                                .thenReturn(response)
                );
    }

    @Override
    public Mono<TransactionStatusResponseDto> getStatus(String transactionId) {
        return uidExtractor.getCurrentUserRequestData()
                .flatMap(userRequestData ->
                        tokenService.getAdminToken()
                                .flatMap(tokenResponse ->
                                        Mono.fromCallable(() ->
                                                        apiClient.getTransactionStatus(AuthorizationConstants.BEARER_SUFFIX + tokenResponse.getAccessToken(), transactionId))
                                                .doOnError(request -> {
                                                    log.error("Transactions service unavailable");
                                                    throw new InnerServiceException("Transactions service unavailable, try again later");
                                                })
                                                .mapNotNull(HttpEntity::getBody)
                                                .subscribeOn(Schedulers.boundedElastic())
                                                .doOnNext(t -> log.info("Got transaction status for user with innerId: {}", userRequestData.getInnerId()))
                                                .map(mapper::mapStatusRequest)));
    }
}
