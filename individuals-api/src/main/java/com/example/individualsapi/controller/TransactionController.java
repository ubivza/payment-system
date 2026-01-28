package com.example.individualsapi.controller;

import com.example.individuals.dto.ConfirmRequestDto;
import com.example.individuals.dto.InitTransactionRequest;
import com.example.individuals.dto.TransactionConfirmResponseDto;
import com.example.individuals.dto.TransactionInitResponseDto;
import com.example.individuals.dto.TransactionStatusResponseDto;
import com.example.individualsapi.service.api.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${individuals-api.path}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/{type}/init")
    public Mono<ResponseEntity<TransactionInitResponseDto>> init(@PathVariable String type,
                                                                 @RequestBody InitTransactionRequest initTransactionRequest,
                                                                 @RequestParam String from,
                                                                 @RequestParam String to) {
        return transactionService.init(type, initTransactionRequest, from, to)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{type}/confirm")
    public Mono<ResponseEntity<TransactionConfirmResponseDto>> confirm(@PathVariable String type, @RequestBody ConfirmRequestDto confirmRequestDto) {
        return transactionService.confirm(type, confirmRequestDto)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{transactionId}/status")
    public Mono<ResponseEntity<TransactionStatusResponseDto>> getStatus(@PathVariable String transactionId) {
        return transactionService.getStatus(transactionId)
                .map(ResponseEntity::ok);
    }
}
