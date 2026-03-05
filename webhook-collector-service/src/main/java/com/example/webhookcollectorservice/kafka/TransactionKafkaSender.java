package com.example.webhookcollectorservice.kafka;

import com.example.api.kafka.DepositCompletedEvent;
import com.example.api.kafka.WithdrawalCompletedEvent;
import com.example.api.kafka.WithdrawalFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public class TransactionKafkaSender {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${kafka.topic.transaction-service.transaction.read.name}")
    private String topic;

    public void blockingSend(WithdrawalCompletedEvent event) throws ExecutionException, InterruptedException {
        kafkaTemplate.send(topic, event.getTransactionId().toString(), event).get();
    }

    public void blockingSend(DepositCompletedEvent event) throws ExecutionException, InterruptedException {
        kafkaTemplate.send(topic, event.getTransactionId().toString(), event).get();
    }

    public void blockingSend(WithdrawalFailedEvent event) throws ExecutionException, InterruptedException {
        kafkaTemplate.send(topic, event.getTransactionId().toString(), event).get();
    }
}
