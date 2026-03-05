package com.example.webhookcollectorservice.service;

import com.example.webhookcollectorservice.entity.OutboxEvent;
import com.example.webhookcollectorservice.entity.TransactionStatusType;
import com.example.webhookcollectorservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventService {
    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventProcessor eventProcessor;

    public void sendAllEvents() {
        while (true) {
            List<OutboxEvent> events = outboxEventRepository.findTop100ByOrderByIdAsc();

            if (events.isEmpty()) {
                break;
            }

            for (OutboxEvent event : events) {
                TransactionStatusType statusType = TransactionStatusType.fromTypeAndStatus(event.getEventType(), event.getPayload().getStatus());

                try {
                    switch (statusType) {
                        case DEPOSIT_SUCCESS:
                            eventProcessor.processDepositSuccess(event);
                            break;
                        case WITHDRAWAL_FAILED:
                            eventProcessor.processWithdrawalFailed(event);
                            break;
                        case WITHDRAWAL_SUCCESS:
                            eventProcessor.processWithdrawalSuccess(event);
                            break;
                        default:
                            log.warn("Got broken record {}", event.getId());
                            break;
                    }
                } catch (Exception e) {
                    log.error("Failed to send event {} to kafka", event.getId());
                }
            }
        }
    }
}
