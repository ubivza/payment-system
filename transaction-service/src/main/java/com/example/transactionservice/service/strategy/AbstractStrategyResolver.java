package com.example.transactionservice.service.strategy;

import com.example.transactionservice.entity.PaymentType;

import java.util.Map;

public abstract class AbstractStrategyResolver {
    public <T> T resolve(String type, Map<PaymentType, T> strategies) {
        return strategies.get(PaymentType.fromString(type));
    }
}
