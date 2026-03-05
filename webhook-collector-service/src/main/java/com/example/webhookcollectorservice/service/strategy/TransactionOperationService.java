package com.example.webhookcollectorservice.service.strategy;

import com.example.webhook.dto.StatusUpdate;
import com.example.webhookcollectorservice.entity.EventBody;
import com.example.webhookcollectorservice.entity.OutboxEvent;
import com.example.webhookcollectorservice.entity.PaymentProviderCallback;
import com.example.webhookcollectorservice.repository.OutboxEventRepository;
import com.example.webhookcollectorservice.repository.PaymentProviderCallbackRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public abstract class TransactionOperationService {
    private final PaymentProviderCallbackRepository paymentProviderCallbackRepository;
    private final OutboxEventRepository outboxEventRepository;

    public abstract String getTransactionType();
    public abstract void process(StatusUpdate statusUpdate);

    protected void save(StatusUpdate statusUpdate) {
        EventBody eventBody = EventBody.builder()
                .transactionId(statusUpdate.getId())
                .status(statusUpdate.getStatus())
                .amount(statusUpdate.getAmount())
                .timestamp(Instant.now())
                .build();

        PaymentProviderCallback callback = new PaymentProviderCallback();
        callback.setProviderTransactionUid(statusUpdate.getId());
        callback.setType(statusUpdate.getType());
        callback.setProvider(statusUpdate.getProvider());
        callback.setBody(eventBody);

        paymentProviderCallbackRepository.save(callback);

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setTransactionId(statusUpdate.getId());
        outboxEvent.setEventType(statusUpdate.getType());
        outboxEvent.setPayload(eventBody);

        outboxEventRepository.save(outboxEvent);
    }
}
