package com.example.fakepaymentprovider.controller;

import com.example.fake.api.TransactionApi;
import com.example.fake.dto.Transaction;
import com.example.fake.dto.TransactionRequest;
import com.example.fakepaymentprovider.service.MerchantService;
import com.example.fakepaymentprovider.service.RateLimiterService;
import com.example.fakepaymentprovider.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TransactionController implements TransactionApi {
    private final TransactionService service;
    private final RateLimiterService limiterService;
    private final MerchantService merchantService;

    @Override
    public ResponseEntity<Transaction> createTransaction(TransactionRequest payoutRequest) {
        if (!limiterService.tryConsume(merchantService.getCurrentMerchantId())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        return ResponseEntity.status(201).body(service.create(payoutRequest));
    }

    @Override
    public ResponseEntity<Transaction> getTransactionById(UUID id) {
        if (!limiterService.tryConsume(merchantService.getCurrentMerchantId())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        return ResponseEntity.ok(service.getById(id));
    }

    @Override
    public ResponseEntity<List<Transaction>> getTransactions(OffsetDateTime startDate, OffsetDateTime endDate) {
        if (!limiterService.tryConsume(merchantService.getCurrentMerchantId())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        return ResponseEntity.ok(service.getByPeriod(startDate, endDate));
    }
}
