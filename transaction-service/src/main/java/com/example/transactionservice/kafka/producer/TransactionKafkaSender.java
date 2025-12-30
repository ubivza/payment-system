package com.example.transactionservice.kafka.producer;

import com.example.api.kafka.DepositRequestedEvent;
import com.example.api.kafka.WithdrawalRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionKafkaSender {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${kafka.topic.transaction-service.transaction.insert.name}")
    private String topic;

    public void send(DepositRequestedEvent event) {
        kafkaTemplate.send(topic, event.getTransactionId().toString(), event);
    }

    public void send(WithdrawalRequestedEvent event) {
        kafkaTemplate.send(topic, event.getTransactionId().toString(), event);
    }
}
