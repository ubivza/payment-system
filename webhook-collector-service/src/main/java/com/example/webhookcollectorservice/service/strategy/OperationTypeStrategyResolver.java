package com.example.webhookcollectorservice.service.strategy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OperationTypeStrategyResolver {
    private final Map<String, TransactionOperationService> strategies;

    public OperationTypeStrategyResolver(List<TransactionOperationService> services) {
        strategies = services.stream()
                .collect(Collectors.toMap(TransactionOperationService::getTransactionType, Function.identity()));
    }

    public TransactionOperationService resolve(String transactionType) {
        return strategies.getOrDefault(transactionType, strategies.get("unknown"));
    }
}
