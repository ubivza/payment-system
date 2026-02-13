package com.example.paymentservice.mapper;

import com.example.payment.dto.PaymentMethodResponse;
import com.example.paymentservice.entity.PaymentMethod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMethodMapper {
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "requiredFields", ignore = true)
    PaymentMethodResponse toResponse(PaymentMethod paymentMethod);
}
