package com.example.webhookcollectorservice.service.strategy;

import com.example.webhook.dto.StatusUpdate;
import com.example.webhookcollectorservice.entity.EventBody;
import com.example.webhookcollectorservice.entity.UnknownCallback;
import com.example.webhookcollectorservice.repository.OutboxEventRepository;
import com.example.webhookcollectorservice.repository.PaymentProviderCallbackRepository;
import com.example.webhookcollectorservice.repository.UnknownCallbackRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
public class UnknownTypeStrategy extends TransactionOperationService {
    private final UnknownCallbackRepository unknownCallbackRepository;

    public UnknownTypeStrategy(PaymentProviderCallbackRepository paymentProviderCallbackRepository,
                               OutboxEventRepository outboxEventRepository,
                               UnknownCallbackRepository unknownCallbackRepository) {
        super(paymentProviderCallbackRepository, outboxEventRepository);
        this.unknownCallbackRepository = unknownCallbackRepository;
    }

    @Override
    @Transactional
    public void process(StatusUpdate statusUpdate) {
        log.warn("Got unknown callback");
        UnknownCallback unknownCallback = new UnknownCallback();
        unknownCallback.setBody(EventBody.builder()
                .transactionId(statusUpdate.getId())
                .status(statusUpdate.getStatus())
                .amount(statusUpdate.getAmount())
                .timestamp(Instant.now())
                .build());
        unknownCallbackRepository.save(unknownCallback);
    }

    @Override
    public String getTransactionType() {
        return "unknown";
    }
}
