package com.example.transactionservice.service.impl;

import com.example.transaction.dto.InitTransactionRequest;
import com.example.transaction.dto.TransferInitRequest;
import com.example.transactionservice.config.TokenProperties;
import com.example.transactionservice.entity.PaymentType;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.example.transactionservice.constant.CommonClaimsForToken.TRANSFER_INIT;

@Service
public class TransferTokenServiceImpl extends TokenServiceAbstract {

    @Autowired
    public TransferTokenServiceImpl(TokenProperties tokenProperties) {
        super(tokenProperties);
    }

    @Override
    public String generate(InitTransactionRequest request) {
        TransferInitRequest transferInitRequest = (TransferInitRequest) request;

        Map<String, Object> claims = new HashMap<>();
        claims.put(WALLET_FROM_UID, transferInitRequest.getWalletFromUid());
        claims.put(WALLET_TO_UID, transferInitRequest.getWalletToUid());
        claims.put(AMOUNT, transferInitRequest.getAmount().doubleValue());
        claims.put(TIMESTAMP, Instant.now().toString());
        claims.put(USER_UID, transferInitRequest.getUserUid());

        return super.buildToken(claims, TRANSFER_INIT);
    }

    @Override
    public Claims validateAndGetClaims(String token) {
        return super.validateAndGet(token, TRANSFER_INIT);
    }

    @Override
    public PaymentType getType() {
        return PaymentType.TRANSFER;
    }
}
