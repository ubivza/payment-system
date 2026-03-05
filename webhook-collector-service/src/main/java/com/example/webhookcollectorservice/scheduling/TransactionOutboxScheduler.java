package com.example.webhookcollectorservice.scheduling;

import com.example.webhookcollectorservice.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionOutboxScheduler {
    private final OutboxEventService outboxEventService;

    @Scheduled(fixedDelayString = "${scheduling.transaction_outbox.event-rate}")
    public void sendEvents() {
        outboxEventService.sendAllEvents();
    }
}
