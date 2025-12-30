package com.example.individualsapi.controller;

import com.example.individuals.dto.CreateWalletRequestDto;
import com.example.individuals.dto.WalletResponseDto;
import com.example.individualsapi.service.api.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("${individuals-api.path}/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public Mono<ResponseEntity<UUID>> create(@RequestBody CreateWalletRequestDto createWalletRequest) {
        return walletService.create(createWalletRequest)
                .map(response ->
                        ResponseEntity.status(HttpStatus.CREATED)
                                .body(response));
    }

    @GetMapping("/{walletUid}")
    public Mono<ResponseEntity<WalletResponseDto>> get(@PathVariable String walletUid) {
        return walletService.get(walletUid)
                .map(ResponseEntity::ok);
    }
}
