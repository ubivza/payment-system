package com.example.individualsapi.service.api;

import com.example.individuals.dto.ConfirmRequestDto;
import com.example.individuals.dto.InitTransactionRequest;
import com.example.individuals.dto.TransactionConfirmResponseDto;
import com.example.individuals.dto.TransactionInitResponseDto;
import com.example.individuals.dto.TransactionStatusResponseDto;
import reactor.core.publisher.Mono;

public interface TransactionService {
    Mono<TransactionInitResponseDto> init(String type, InitTransactionRequest initTransactionRequest);
    Mono<TransactionConfirmResponseDto> confirm(String type, ConfirmRequestDto confirmRequestDto);
    Mono<TransactionStatusResponseDto> getStatus(String transactionId);
}
