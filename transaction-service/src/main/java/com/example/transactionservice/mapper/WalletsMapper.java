package com.example.transactionservice.mapper;

import com.example.transaction.dto.CreateWalletRequest;
import com.example.transaction.dto.WalletResponse;
import com.example.transactionservice.entity.Wallets;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WalletsMapper {
    @Mapping(target = "userId", source = "userUid")
    @Mapping(target = "walletType.currencyCode", source = "createWalletRequest.currencyCode")
    @Mapping(target = "name", source = "createWalletRequest.name")
    @Mapping(target = "walletType.name", constant = "someName")
    @Mapping(target = "walletType.status", expression = "java(com.example.transactionservice.entity.ActivityStatus.ACTIVE)")
    @Mapping(target = "status", expression = "java(com.example.transactionservice.entity.ActivityStatus.ACTIVE)")
    @Mapping(target = "balance", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "walletType.creator", source = "userUid")
    Wallets map(String userUid, CreateWalletRequest createWalletRequest);

    @Mapping(target = "walletUid", source = "id")
    @Mapping(target = "currencyCode", source = "walletType.currencyCode")
    WalletResponse map(Wallets wallet);
}
