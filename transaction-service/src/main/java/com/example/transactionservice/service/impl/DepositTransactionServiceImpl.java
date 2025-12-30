package com.example.transactionservice.service.impl;

import com.example.api.kafka.DepositCompletedEvent;
import com.example.transaction.dto.ConfirmRequest;
import com.example.transaction.dto.InitTransactionRequest;
import com.example.transaction.dto.TransactionConfirmResponse;
import com.example.transaction.dto.TransactionInitResponse;
import com.example.transactionservice.constant.FailureReason;
import com.example.transactionservice.entity.PaymentType;
import com.example.transactionservice.entity.TransactionStatus;
import com.example.transactionservice.entity.Transactions;
import com.example.transactionservice.kafka.producer.TransactionKafkaSender;
import com.example.transactionservice.mapper.TransactionsMapper;
import com.example.transactionservice.repository.TransactionsRepository;
import com.example.transactionservice.service.api.WalletService;
import com.example.transactionservice.service.strategy.TokenTypeStrategyResolver;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static com.example.transactionservice.service.impl.TokenServiceAbstract.AMOUNT;
import static com.example.transactionservice.service.impl.TokenServiceAbstract.USER_UID;
import static com.example.transactionservice.service.impl.TokenServiceAbstract.WALLET_UID;

@Primary
@Service
@Transactional(readOnly = true)
public class DepositTransactionServiceImpl extends TransactionServiceAbstract {
    private final TransactionsRepository transactionsRepository;
    private final TransactionsMapper mapper;
    private final TokenTypeStrategyResolver tokenTypeStrategyResolver;
    private final WalletService walletService;
    private final TransactionKafkaSender kafkaSender;

    @Autowired
    protected DepositTransactionServiceImpl(TransactionsRepository transactionsRepository, TransactionsMapper mapper,
                                            TokenTypeStrategyResolver tokenTypeStrategyResolver, WalletService walletService,
                                            TransactionKafkaSender kafkaSender) {
        super(transactionsRepository, mapper);
        this.transactionsRepository = transactionsRepository;
        this.mapper = mapper;
        this.tokenTypeStrategyResolver = tokenTypeStrategyResolver;
        this.walletService = walletService;
        this.kafkaSender = kafkaSender;
    }

    @Override
    public TransactionInitResponse init(InitTransactionRequest initTransactionRequest) {
        String token = tokenTypeStrategyResolver.resolve(PaymentType.DEPOSIT.name()).generate(initTransactionRequest);

        TransactionInitResponse response = new TransactionInitResponse();
        response.setAvailable(true);
        response.setFee(new BigDecimal(0));
        response.setToken(token);

        return response;
    }

    @Override
    @Transactional
    public TransactionConfirmResponse confirm(ConfirmRequest confirmRequest) {
        Claims claims = tokenTypeStrategyResolver.resolve(PaymentType.DEPOSIT.name()).validateAndGetClaims(confirmRequest.getToken());

        Transactions transactions = new Transactions();
        transactions.setAmount(BigDecimal.valueOf(claims.get(AMOUNT, Double.class)));
        transactions.setStatus(TransactionStatus.PENDING);
        transactions.setType(PaymentType.DEPOSIT);

        UUID walletUid = UUID.fromString(claims.get(WALLET_UID, String.class));

        transactions.setWallet(walletService.get(walletUid.toString()));
        transactions.setUserId(UUID.fromString(claims.get(USER_UID, String.class)));

        Transactions saved = transactionsRepository.save(transactions);

        kafkaSender.send(mapper.mapToDepositEvent(saved));

        TransactionConfirmResponse response = new TransactionConfirmResponse();
        response.setStatus(TransactionStatus.PENDING.name());
        response.setTransactionId(saved.getId());

        return response;
    }

    @Override
    @Transactional
    public void complete(Object event) {
        DepositCompletedEvent depositCompletedEvent = (DepositCompletedEvent) event;

        transactionsRepository.updateStatusAndFailureReason(depositCompletedEvent.getTransactionId(), null, TransactionStatus.valueOf(depositCompletedEvent.getStatus()));
        UUID walletId = transactionsRepository.findWalletId(depositCompletedEvent.getTransactionId());
        walletService.depositMoney(walletId, depositCompletedEvent.getAmount());
    }

    @Override
    @Transactional
    public void cancelTransaction(UUID transactionId) {
        transactionsRepository.updateStatusAndFailureReason(transactionId, FailureReason.EXTERNAL_SYSTEM_FAILED, TransactionStatus.FAILED);
    }

    @Override
    public PaymentType getType() {
        return PaymentType.DEPOSIT;
    }
}
