package com.example.transactionservice.service.impl;

import com.example.transaction.dto.InitTransactionRequest;
import com.example.transaction.dto.WithdrawalInitRequest;
import com.example.transactionservice.config.TokenProperties;
import com.example.transactionservice.entity.PaymentType;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.example.transactionservice.constant.CommonClaimsForToken.WITHDRAWAL_INIT;

@Service
public class WithdrawalTokenServiceImpl extends TokenServiceAbstract {

    @Autowired
    protected WithdrawalTokenServiceImpl(TokenProperties tokenProperties) {
        super(tokenProperties);
    }

    @Override
    public String generate(InitTransactionRequest request) {
        WithdrawalInitRequest withdrawalInitRequest = (WithdrawalInitRequest) request;

        Map<String, Object> claims = new HashMap<>();
        claims.put(AMOUNT, withdrawalInitRequest.getAmount().doubleValue());
        claims.put(TIMESTAMP, Instant.now().toString());
        claims.put(USER_UID, withdrawalInitRequest.getUserUid());
        claims.put(WALLET_UID, withdrawalInitRequest.getWalletUid());
        claims.put(DESTINATION, withdrawalInitRequest.getDestination());

        return super.buildToken(claims, WITHDRAWAL_INIT);
    }

    @Override
    public Claims validateAndGetClaims(String token) {
        return super.validateAndGet(token, WITHDRAWAL_INIT);
    }

    @Override
    public PaymentType getType() {
        return PaymentType.WITHDRAWAL;
    }
}
