package com.example.transactionservice.mapper;

import com.example.api.kafka.DepositRequestedEvent;
import com.example.api.kafka.WithdrawalRequestedEvent;
import com.example.transaction.dto.TransactionStatusResponse;
import com.example.transactionservice.entity.Transactions;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionsMapper {
    TransactionStatusResponse map(Transactions entity);
    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "currency", source = "wallet.walletType.currencyCode")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    DepositRequestedEvent mapToDepositEvent(Transactions entity);
    @Mapping(target = "transactionId", source = "entity.id")
    @Mapping(target = "walletId", source = "entity.wallet.id")
    @Mapping(target = "currency", source = "entity.wallet.walletType.currencyCode")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    @Mapping(target = "destination", source = "destination")
    WithdrawalRequestedEvent mapToWithdrawalEvent(Transactions entity, String destination);
}
