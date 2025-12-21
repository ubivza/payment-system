package com.example.transactionservice.service.api;

import com.example.transaction.dto.CreateWalletRequest;
import com.example.transaction.dto.WalletResponse;
import com.example.transactionservice.entity.Wallets;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {
    UUID create(String userUid, CreateWalletRequest createWalletRequest);
    WalletResponse get(String userUid, String walletUid);
    Wallets get(String walletUid);
    void depositMoney(UUID walletId, BigDecimal amount);
}
