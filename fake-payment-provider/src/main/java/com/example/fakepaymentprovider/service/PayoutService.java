package com.example.fakepaymentprovider.service;

import com.example.fake.dto.Payout;
import com.example.fake.dto.PayoutRequest;
import com.example.fakepaymentprovider.exception.NotFoundException;
import com.example.fakepaymentprovider.exception.NotValidException;
import com.example.fakepaymentprovider.mapper.PayoutMapper;
import com.example.fakepaymentprovider.repository.PayoutRepository;
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
public class PayoutService {
    private final PayoutRepository repository;
    private final PayoutMapper mapper;
    private final MerchantService merchantService;
    private final Random random = new Random();

    @Transactional
    public Payout create(PayoutRequest payoutRequest) {
        if (!(payoutRequest.getAmount() > 0)) {
            throw new NotValidException("Amount must be more than 0");
        }

        com.example.fakepaymentprovider.entity.Payout entity = mapper.toEntity(payoutRequest, merchantService.getCurrentMerchantInnerId());
        entity.setStatus(random.nextBoolean() ? "COMPLETED" : "FAILED");
        com.example.fakepaymentprovider.entity.Payout saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    public Payout getById(UUID id) {
        return repository.findByIdAndMerchantId(id, merchantService.getCurrentMerchantInnerId())
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException(String.format("Not found payout with id %s", id)));
    }

    public List<Payout> getByPeriod(OffsetDateTime startDate, OffsetDateTime endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new NotValidException("Start date cannot be after end date");
        }

        BaseSpecification<com.example.fakepaymentprovider.entity.Payout> spec = BaseSpecification.<com.example.fakepaymentprovider.entity.Payout>builder()
                .merchantId(merchantService.getCurrentMerchantInnerId())
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return repository.findAll(spec).stream()
                .map(mapper::toResponse)
                .toList();
    }
}
