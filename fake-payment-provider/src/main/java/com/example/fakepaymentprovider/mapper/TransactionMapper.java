package com.example.fakepaymentprovider.mapper;

import com.example.fake.dto.TransactionRequest;
import com.example.fakepaymentprovider.entity.Transaction;
import com.example.fakepaymentprovider.util.DateTimeUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {DateTimeUtils.class})
public interface TransactionMapper {
    @Mapping(target = "status", expression = "java(new String(\"PENDING\"))")
    Transaction toEntity(TransactionRequest request, UUID merchantId);
    com.example.fake.dto.Transaction toResponse(Transaction entity);
}
