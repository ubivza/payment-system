package com.example.fakepaymentprovider.mapper;

import com.example.fake.dto.PayoutRequest;
import com.example.fakepaymentprovider.entity.Payout;
import com.example.fakepaymentprovider.util.DateTimeUtils;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {DateTimeUtils.class})
public interface PayoutMapper {
    Payout toEntity(PayoutRequest request, UUID merchantId);
    com.example.fake.dto.Payout toResponse(Payout entity);
}
