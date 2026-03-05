package com.example.fakepaymentprovider.service;

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
import java.util.Random;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository repository;
    private final TransactionMapper mapper;
    private final MerchantService merchantService;
    private final Random random = new Random();

    @Transactional
    public Transaction create(TransactionRequest transactionRequest) {
        if (!(transactionRequest.getAmount() > 0)) {
            throw new NotValidException("Amount must be more than 0");
        }

        com.example.fakepaymentprovider.entity.Transaction entity = mapper.toEntity(transactionRequest, merchantService.getCurrentMerchantInnerId());
        entity.setStatus(random.nextBoolean() ? "COMPLETED" : "FAILED");
        com.example.fakepaymentprovider.entity.Transaction saved = repository.save(entity);
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
}
