package com.example.paymentservice.service;

import com.example.payment.dto.PaymentMethodResponse;
import com.example.paymentservice.entity.PaymentMethod;
import com.example.paymentservice.exception.NotFoundException;
import com.example.paymentservice.mapper.PaymentMethodMapper;
import com.example.paymentservice.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentMethodService {
    private final PaymentMethodRepository repository;
    private final PaymentMethodDefinitionService paymentMethodDefinitionService;
    private final PaymentMethodMapper mapper;

    public List<PaymentMethodResponse> get(String currencyCode, String countryCode) {
        return paymentMethodDefinitionService.getPaymentMethodBy(currencyCode, countryCode).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public PaymentMethod getById(UUID methodId) {
        return repository
                .findById(methodId).orElseThrow(() -> new NotFoundException(String.format("Not found payment method with id %s", methodId)));
    }
}
