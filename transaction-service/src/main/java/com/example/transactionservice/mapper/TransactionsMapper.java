package com.example.transactionservice.mapper;

import com.example.transaction.dto.TransactionStatusResponse;
import com.example.transactionservice.entity.Transactions;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionsMapper {
    TransactionStatusResponse map(Transactions entity);
}
