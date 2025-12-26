package com.example.transactionservice.service.api;

import com.example.transaction.dto.ConfirmRequest;
import com.example.transaction.dto.InitTransactionRequest;
import com.example.transaction.dto.TransactionConfirmResponse;
import com.example.transaction.dto.TransactionInitResponse;
import com.example.transaction.dto.TransactionStatusResponse;
import com.example.transactionservice.entity.PaymentType;

import java.util.UUID;

public interface TransactionService {
    TransactionInitResponse init(InitTransactionRequest initTransactionRequest);
    TransactionConfirmResponse confirm(ConfirmRequest confirmRequest);
    TransactionStatusResponse getStatus(String transactionId);
    PaymentType getType();
    void complete(Object event);
    void abort(Object event);
    void cancelTransaction(UUID transactionId);
}
