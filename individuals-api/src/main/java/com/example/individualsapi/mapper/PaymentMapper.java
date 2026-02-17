package com.example.individualsapi.mapper;

import com.example.individuals.dto.PaymentMethodResponseDto;
import com.example.payment.dto.PaymentMethodResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {
    PaymentMethodResponseDto mapPaymentMethodResponse(PaymentMethodResponse response);
}
