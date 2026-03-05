package com.example.webhookcollectorservice.service.strategy;

import com.example.webhook.dto.StatusUpdate;
import com.example.webhookcollectorservice.repository.OutboxEventRepository;
import com.example.webhookcollectorservice.repository.PaymentProviderCallbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositTypeStrategy extends TransactionOperationService {

    public DepositTypeStrategy(PaymentProviderCallbackRepository paymentProviderCallbackRepository,
                               OutboxEventRepository outboxEventRepository) {
        super(paymentProviderCallbackRepository, outboxEventRepository);
    }

    @Override
    @Transactional
    public void process(StatusUpdate statusUpdate) {
        super.save(statusUpdate);
    }

    @Override
    public String getTransactionType() {
        return "deposit";
    }
}