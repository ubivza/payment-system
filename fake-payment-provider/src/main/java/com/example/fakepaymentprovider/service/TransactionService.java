package com.example.fakepaymentprovider.service;

import com.example.fake.dto.StatusUpdate;
import com.example.fake.dto.Transaction;
import com.example.fake.dto.TransactionRequest;
import com.example.fakepaymentprovider.exception.NotFoundException;
import com.example.fakepaymentprovider.exception.NotValidException;
import com.example.fakepaymentprovider.mapper.TransactionMapper;
import com.example.fakepaymentprovider.repository.TransactionRepository;
import com.example.fakepaymentprovider.repository.specification.BaseSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionService implements WebhookListener {
    private final TransactionRepository repository;
    private final TransactionMapper mapper;
    private final MerchantService merchantService;

    @Transactional
    public Transaction create(TransactionRequest transactionRequest) {
        if (!(transactionRequest.getAmount() > 0)) {
            throw new NotValidException("Amount must be more than 0");
        }

        com.example.fakepaymentprovider.entity.Transaction saved = repository.save(mapper.toEntity(transactionRequest, merchantService.getCurrentMerchantInnerId()));
        return mapper.toResponse(saved);
    }

    public Transaction getById(UUID id) {
        return repository.findByIdAndMerchantId(id, merchantService.getCurrentMerchantInnerId())
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException(String.format("Not found transaction with id %s", id)));
    }

    public List<Transaction> getByPeriod(OffsetDateTime startDate, OffsetDateTime endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new NotValidException("Start date cannot be after end date");
        }

        BaseSpecification<com.example.fakepaymentprovider.entity.Transaction> spec = BaseSpecification.<com.example.fakepaymentprovider.entity.Transaction>builder()
                .merchantId(merchantService.getCurrentMerchantInnerId())
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return repository.findAll(spec).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void updateStatus(StatusUpdate statusUpdate) {
        com.example.fakepaymentprovider.entity.Transaction transaction = repository.findById(statusUpdate.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Not found transaction with id %s", statusUpdate.getId())));

        transaction.setStatus(statusUpdate.getStatus());

        repository.save(transaction);
    }
}
