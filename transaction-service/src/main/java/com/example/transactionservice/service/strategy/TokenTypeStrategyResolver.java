package com.example.transactionservice.service.strategy;

import com.example.transactionservice.entity.PaymentType;
import com.example.transactionservice.service.api.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TokenTypeStrategyResolver extends AbstractStrategyResolver {
    private final Map<PaymentType, TokenService> strategies;

    @Autowired
    public TokenTypeStrategyResolver(List<TokenService> services) {
        this.strategies = services.stream()
                .collect(Collectors.toMap(TokenService::getType, Function.identity()));
    }

    public TokenService resolve(String type) {
        return super.resolve(type, strategies);
    }
}
