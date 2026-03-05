package com.example.webhookcollectorservice.service;

import com.example.api.kafka.DepositCompletedEvent;
import com.example.api.kafka.WithdrawalCompletedEvent;
import com.example.api.kafka.WithdrawalFailedEvent;
import com.example.webhookcollectorservice.entity.OutboxEvent;
import com.example.webhookcollectorservice.kafka.TransactionKafkaSender;
import com.example.webhookcollectorservice.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class OutboxEventProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final TransactionKafkaSender kafkaSender;

    @Transactional
    public void processWithdrawalSuccess(OutboxEvent event) throws ExecutionException, InterruptedException {
        kafkaSender.blockingSend(WithdrawalCompletedEvent.builder()
                .transactionId(event.getPayload().getTransactionId())
                .status(event.getPayload().getStatus())
                .amount(event.getPayload().getAmount())
                .timestamp(event.getPayload().getTimestamp())
                .build());
        outboxEventRepository.deleteById(event.getId());
    }

    @Transactional
    public void processWithdrawalFailed(OutboxEvent event) throws ExecutionException, InterruptedException {
        kafkaSender.blockingSend(WithdrawalFailedEvent.builder()
                .transactionId(event.getPayload().getTransactionId())
                .status(event.getPayload().getStatus())
                .failureReason(event.getPayload().getReason())
                .timestamp(event.getPayload().getTimestamp())
                .build());
        outboxEventRepository.deleteById(event.getId());
    }

    @Transactional
    public void processDepositSuccess(OutboxEvent event) throws ExecutionException, InterruptedException {
        kafkaSender.blockingSend(DepositCompletedEvent.builder()
                .transactionId(event.getPayload().getTransactionId())
                .status(event.getPayload().getStatus())
                .amount(event.getPayload().getAmount())
                .timestamp(event.getPayload().getTimestamp())
                .build());
        outboxEventRepository.deleteById(event.getId());
    }
}