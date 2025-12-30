package com.example.transactionservice.service.impl;

import com.example.transaction.dto.DepositInitRequest;
import com.example.transaction.dto.InitTransactionRequest;
import com.example.transactionservice.config.TokenProperties;
import com.example.transactionservice.entity.PaymentType;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.example.transactionservice.constant.CommonClaimsForToken.DEPOSIT_INIT;

@Service
public class DepositTokenServiceImpl extends TokenServiceAbstract {

    @Autowired
    public DepositTokenServiceImpl(TokenProperties tokenProperties) {
        super(tokenProperties);
    }

    @Override
    public String generate(InitTransactionRequest request) {
        DepositInitRequest depositInitRequest = (DepositInitRequest) request;

        Map<String, Object> claims = new HashMap<>();
        claims.put(WALLET_UID, depositInitRequest.getWalletUid());
        claims.put(AMOUNT, depositInitRequest.getAmount().doubleValue());
        claims.put(TIMESTAMP, Instant.now().toString());
        claims.put(USER_UID, depositInitRequest.getUserUid());

        return super.buildToken(claims, DEPOSIT_INIT);
    }

    @Override
    public Claims validateAndGetClaims(String token) {
        return super.validateAndGet(token, DEPOSIT_INIT);
    }

    @Override
    public PaymentType getType() {
        return PaymentType.DEPOSIT;
    }
}
