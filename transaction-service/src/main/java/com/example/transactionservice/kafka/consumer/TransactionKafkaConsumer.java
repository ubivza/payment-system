package com.example.transactionservice.kafka.consumer;

import com.example.transactionservice.entity.PaymentType;
import com.example.transactionservice.kafka.api.DepositCompletedEvent;
import com.example.transactionservice.kafka.api.WithdrawalCompletedEvent;
import com.example.transactionservice.kafka.api.WithdrawalFailedEvent;
import com.example.transactionservice.service.strategy.TransactionTypeStrategyResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@KafkaListener(
        topics = "${kafka.topic.transaction-service.transaction.read.name}",
        groupId = "${spring.kafka.consumer.group-id}",
        autoStartup = "${kafka.topic.transaction-service.transaction.read.auto-startup}")
public class TransactionKafkaConsumer {
    private final TransactionTypeStrategyResolver strategyResolver;

    @KafkaHandler
    public void handleWithdrawalCompleted(Acknowledgment acknowledgment, @Payload WithdrawalCompletedEvent event) {
        strategyResolver.resolve(PaymentType.WITHDRAWAL.name())
                .complete(event);

        acknowledgment.acknowledge();
    }

    @KafkaHandler
    public void handleDepositCompleted(@Payload DepositCompletedEvent event,
                                       Acknowledgment acknowledgment) {
        strategyResolver.resolve(PaymentType.DEPOSIT.name())
                .complete(event);

        acknowledgment.acknowledge();
    }

    @KafkaHandler
    public void handleWithdrawalFailedEvent(@Payload WithdrawalFailedEvent event,
                                            Acknowledgment acknowledgment) {
        strategyResolver.resolve(PaymentType.DEPOSIT.name())
                .abort(event);

        acknowledgment.acknowledge();
    }
}
