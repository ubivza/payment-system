package com.example.fakepaymentprovider.mapper;

import com.example.fake.dto.PayoutRequest;
import com.example.fakepaymentprovider.entity.Payout;
import com.example.fakepaymentprovider.util.DateTimeUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {DateTimeUtils.class})
public interface PayoutMapper {
    @Mapping(target = "status", expression = "java(new String(\"PENDING\"))")
    Payout toEntity(PayoutRequest request, UUID merchantId);
    com.example.fake.dto.Payout toResponse(Payout entity);
}
