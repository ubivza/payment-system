package com.example.transactionservice.controller;

import com.example.transaction.api.WalletApiClient;
import com.example.transaction.dto.CreateWalletRequest;
import com.example.transaction.dto.WalletResponse;
import com.example.transactionservice.service.api.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WalletController implements WalletApiClient {
    private final WalletService walletService;

    @Override
    public ResponseEntity<UUID> createWallet(String authorization, String userUid, CreateWalletRequest createWalletRequest) {
        return ResponseEntity.ok()
                .body(walletService.create(userUid, createWalletRequest));
    }

    @Override
    public ResponseEntity<WalletResponse> getWallet(String authorization, String walletUid, String userUid) {
        return ResponseEntity.ok()
                .body(walletService.get(userUid, walletUid));
    }
}
