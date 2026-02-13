package com.example.paymentservice.mapper;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.paymentservice.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {
    @Mapping(target = "providerTransactionId", source = "externalTransactionId")
    PaymentResponse toResponse(Payment entity);
    @Mapping(target = "externalTransactionId", source = "transactionId")
    @Mapping(target = "internalTransactionId", source = "paymentRequest.internalTransactionUid")
    @Mapping(target = "status", expression = "java(new String(\"SUCCESS\"))")
    Payment toEntity(PaymentRequest paymentRequest, UUID transactionId);
}
