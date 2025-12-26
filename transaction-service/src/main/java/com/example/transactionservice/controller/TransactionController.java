package com.example.transactionservice.controller;

import com.example.transaction.api.TransactionApiClient;
import com.example.transaction.dto.ConfirmRequest;
import com.example.transaction.dto.InitTransactionRequest;
import com.example.transaction.dto.TransactionConfirmResponse;
import com.example.transaction.dto.TransactionInitResponse;
import com.example.transaction.dto.TransactionStatusResponse;
import com.example.transactionservice.service.strategy.TransactionTypeStrategyResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TransactionController implements TransactionApiClient {
    private final TransactionTypeStrategyResolver strategyResolver;

    @Override
    public ResponseEntity<TransactionInitResponse> initTransaction(String authorization, String type, InitTransactionRequest initTransactionRequest) {
        return ResponseEntity.ok()
                .body(strategyResolver.resolve(type).init(initTransactionRequest));
    }

    @Override
    public ResponseEntity<Void> compensateFailedTransaction(UUID transactionId, String type, String authorization) {
        strategyResolver.resolve(type).cancelTransaction(transactionId);
        return ResponseEntity.ok()
                .build();
    }

    @Override
    public ResponseEntity<TransactionConfirmResponse> confirmTransaction(String authorization, String type, ConfirmRequest confirmRequest) {
        return ResponseEntity.ok()
                .body(strategyResolver.resolve(type).confirm(confirmRequest));
    }

    @Override
    public ResponseEntity<TransactionStatusResponse> getTransactionStatus(String authorization, String transactionId) {
        return ResponseEntity.ok()
                .body(strategyResolver.getPrimaryService().getStatus(transactionId));
    }
}
