package com.example.transactionservice.service.strategy;

import com.example.transactionservice.entity.PaymentType;
import com.example.transactionservice.service.api.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TransactionTypeStrategyResolver extends AbstractStrategyResolver {
    private final Map<PaymentType, TransactionService> strategies;

    @Autowired
    public TransactionTypeStrategyResolver(List<TransactionService> services) {
        this.strategies = services.stream()
                .collect(Collectors.toMap(TransactionService::getType, Function.identity()));
    }

    public TransactionService resolve(String type) {
        return super.resolve(type, strategies);
    }

    public TransactionService getPrimaryService() {
        return strategies.get(PaymentType.DEPOSIT);
    }
}
