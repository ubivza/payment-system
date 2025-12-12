package com.example.transactionservice.service.impl;

import com.example.transaction.dto.TransactionStatusResponse;
import com.example.transactionservice.entity.Transactions;
import com.example.transactionservice.exception.NotFoundException;
import com.example.transactionservice.mapper.TransactionsMapper;
import com.example.transactionservice.repository.TransactionsRepository;
import com.example.transactionservice.service.api.TransactionService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public abstract class TransactionServiceAbstract implements TransactionService {
    private final TransactionsRepository transactionsRepository;
    private final TransactionsMapper mapper;

    protected TransactionServiceAbstract(TransactionsRepository transactionsRepository, TransactionsMapper mapper) {
        this.transactionsRepository = transactionsRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionStatusResponse getStatus(String transactionId) {
        Transactions transactions = transactionsRepository.findById(UUID.fromString(transactionId))
                .orElseThrow(() -> new NotFoundException(String.format("Transaction not found by id %s", transactionId)));

        return mapper.map(transactions);
    }

    protected BigDecimal addFee(BigDecimal addTo, BigDecimal amountToFee) {
        return addTo.add(amountToFee.multiply(BigDecimal.valueOf(0.05)));
    }

    protected BigDecimal getFee(BigDecimal amountToFee) {
        return amountToFee.multiply(BigDecimal.valueOf(0.05));
    }

    protected boolean isBalanceNotEnough(BigDecimal balance, BigDecimal amount) {
        return balance.compareTo(amount) < 0;
    }
}
