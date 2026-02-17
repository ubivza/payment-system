package com.example.individualsapi.service.api;

import com.example.individuals.dto.PaymentMethodResponseDto;
import com.example.payment.dto.PaymentResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentService {
    Flux<PaymentMethodResponseDto> getAvailable(String currencyCode, String countryCode);
    Mono<PaymentResponse> create(UUID transactionId, UUID methodId, Double amount, String currency);
    Mono<PaymentMethodResponseDto> getById(String methodId);
}
