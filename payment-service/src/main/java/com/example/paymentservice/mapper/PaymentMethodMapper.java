package com.example.paymentservice.mapper;

import com.example.payment.dto.PaymentMethodResponse;
import com.example.payment.dto.RequiredField;
import com.example.paymentservice.entity.PaymentMethod;
import com.example.paymentservice.entity.PaymentMethodRequiredField;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMethodMapper {
    @Mapping(target = "imageUrl", ignore = true)
    PaymentMethodResponse toResponse(PaymentMethod paymentMethod);
    @Mapping(target = "uid", source = "id")
    @Mapping(target = "valuesOptions", source = "valuesOptions", qualifiedByName = "mapValuesOptions")
    RequiredField toResponse(PaymentMethodRequiredField entity);
    @Named("mapValuesOptions")
    default List<String> map(String valuesOptions) {
        if (valuesOptions == null) {
            return null;
        }
        return Arrays.stream(valuesOptions.split(",")).toList();
    }
}
