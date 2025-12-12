package com.example.transactionservice.service.impl;

import com.example.transaction.dto.CreateWalletRequest;
import com.example.transaction.dto.WalletResponse;
import com.example.transactionservice.entity.Wallets;
import com.example.transactionservice.exception.NotFoundException;
import com.example.transactionservice.mapper.WalletsMapper;
import com.example.transactionservice.repository.WalletsRepository;
import com.example.transactionservice.service.api.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletServiceImpl implements WalletService {
    private final WalletsRepository repository;
    private final WalletsMapper mapper;

    @Override
    @Transactional
    public UUID create(String userUid, CreateWalletRequest createWalletRequest) {
        return repository.save(mapper.map(userUid, createWalletRequest)).getId();
    }

    @Override
    public WalletResponse get(String userUid, String walletUid) {
        Wallets wallet = repository.findByUserIdAndId(UUID.fromString(userUid), UUID.fromString(walletUid))
                .orElseThrow(() -> new NotFoundException(String.format("Wallet with id %s not found among your wallets", walletUid)));

        return mapper.map(wallet);
    }

    @Override
    public Wallets get(String walletUid) {
        return repository.findById(UUID.fromString(walletUid))
                .orElseThrow(() -> new NotFoundException(String.format("Wallet with id %s not found", walletUid)));
    }
}
