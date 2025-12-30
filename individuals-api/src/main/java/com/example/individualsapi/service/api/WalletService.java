package com.example.individualsapi.service.api;

import com.example.individuals.dto.CreateWalletRequestDto;
import com.example.individuals.dto.WalletResponseDto;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WalletService {
    Mono<UUID> create(CreateWalletRequestDto createWalletRequest);
    Mono<WalletResponseDto> get(String walletUid);
}
