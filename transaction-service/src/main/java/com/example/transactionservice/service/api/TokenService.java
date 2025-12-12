package com.example.transactionservice.service.api;

import com.example.transaction.dto.InitTransactionRequest;
import com.example.transactionservice.entity.PaymentType;
import io.jsonwebtoken.Claims;

public interface TokenService {
    String generate(InitTransactionRequest request);
    Claims validateAndGetClaims(String token);
    PaymentType getType();
}
