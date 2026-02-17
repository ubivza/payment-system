package com.example.paymentservice.service;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.paymentservice.client.FakePaymentProviderClient;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentMethod;
import com.example.paymentservice.mapper.PaymentMapper;
import com.example.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    private final FakePaymentProviderClient client;
    private final PaymentMethodService paymentMethodService;

    @Transactional
    public PaymentResponse create(PaymentRequest paymentRequest) {
        UUID transactionId = client.createPayout(paymentRequest.getAmount(), paymentRequest.getCurrency());

        PaymentMethod paymentMethod = paymentMethodService.getEntityById(paymentRequest.getMethodId());

        Payment payment = mapper.toEntity(paymentRequest, transactionId);
        payment.setPaymentMethod(paymentMethod);

        Payment saved = repository.save(payment);

        return mapper.toResponse(saved);
    }
}
