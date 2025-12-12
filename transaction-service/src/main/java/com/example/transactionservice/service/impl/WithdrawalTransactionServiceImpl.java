package com.example.transactionservice.service.impl;

import com.example.transaction.dto.ConfirmRequest;
import com.example.transaction.dto.InitTransactionRequest;
import com.example.transaction.dto.TransactionConfirmResponse;
import com.example.transaction.dto.TransactionInitResponse;
import com.example.transaction.dto.WalletResponse;
import com.example.transaction.dto.WithdrawalInitRequest;
import com.example.transactionservice.entity.ActivityStatus;
import com.example.transactionservice.entity.PaymentType;
import com.example.transactionservice.entity.TransactionStatus;
import com.example.transactionservice.entity.Transactions;
import com.example.transactionservice.entity.Wallets;
import com.example.transactionservice.exception.BadRequest;
import com.example.transactionservice.mapper.TransactionsMapper;
import com.example.transactionservice.repository.TransactionsRepository;
import com.example.transactionservice.service.api.WalletService;
import com.example.transactionservice.service.strategy.TokenTypeStrategyResolver;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static com.example.transactionservice.service.impl.TokenServiceAbstract.AMOUNT;
import static com.example.transactionservice.service.impl.TokenServiceAbstract.USER_UID;
import static com.example.transactionservice.service.impl.TokenServiceAbstract.WALLET_UID;

@Service
@Transactional(readOnly = true)
public class WithdrawalTransactionServiceImpl extends TransactionServiceAbstract {
    private final TransactionsRepository transactionsRepository;
    private final TransactionsMapper mapper;
    private final TokenTypeStrategyResolver tokenTypeStrategyResolver;
    private final WalletService walletService;

    @Autowired
    protected WithdrawalTransactionServiceImpl(TransactionsRepository transactionsRepository, TransactionsMapper mapper,
                                               TokenTypeStrategyResolver tokenTypeStrategyResolver, WalletService walletService) {
        super(transactionsRepository, mapper);
        this.transactionsRepository = transactionsRepository;
        this.mapper = mapper;
        this.tokenTypeStrategyResolver = tokenTypeStrategyResolver;
        this.walletService = walletService;
    }

    @Override
    public TransactionInitResponse init(InitTransactionRequest initTransactionRequest) {
        WithdrawalInitRequest withdrawalInitRequest = (WithdrawalInitRequest) initTransactionRequest;

        WalletResponse walletResponse = walletService.get(withdrawalInitRequest.getUserUid().toString(), withdrawalInitRequest.getWalletUid().toString());

        if (isBalanceNotEnough(walletResponse.getBalance().add(super.getFee(withdrawalInitRequest.getAmount())), withdrawalInitRequest.getAmount())) {
            throw new BadRequest(String.format("Your wallet %s dont have enough balance to perform this operation!", withdrawalInitRequest.getWalletUid()));
        }

        if (ActivityStatus.DISABLED.name().equalsIgnoreCase(walletResponse.getStatus())) {
            throw new BadRequest(String.format("Your wallet %s is disabled", withdrawalInitRequest.getWalletUid()));
        }

        String token = tokenTypeStrategyResolver.resolve(PaymentType.WITHDRAWAL.name()).generate(initTransactionRequest);


        TransactionInitResponse response = new TransactionInitResponse();
        response.setFee(addFee(withdrawalInitRequest.getAmount(), withdrawalInitRequest.getAmount()));
        response.setAvailable(true);
        response.setToken(token);

        return response;
    }

    @Override
    @Transactional
    public TransactionConfirmResponse confirm(ConfirmRequest confirmRequest) {
        Claims claims = tokenTypeStrategyResolver.resolve(PaymentType.WITHDRAWAL.name()).validateAndGetClaims(confirmRequest.getToken());

        BigDecimal amount = BigDecimal.valueOf(claims.get(AMOUNT, Double.class));

        Transactions transactions = new Transactions();
        transactions.setAmount(amount);
        transactions.setType(PaymentType.WITHDRAWAL);
        transactions.setFee(getFee(amount));

        UUID walletFromUid = UUID.fromString(claims.get(WALLET_UID, String.class));

        Wallets walletFrom = walletService.get(walletFromUid.toString());

        transactions.setWallet(walletFrom);
        transactions.setUserId(UUID.fromString(claims.get(USER_UID, String.class)));

        if (walletFrom.getBalance().subtract(amount.add(getFee(amount))).compareTo(BigDecimal.ZERO) < 0) {
            transactions.setStatus(TransactionStatus.FAILED);
            transactions.setFailureReason(String.format("Your wallet %s dont have enough balance to perform this operation!", walletFromUid));

            Transactions saved = transactionsRepository.save(transactions);

            TransactionConfirmResponse response = new TransactionConfirmResponse();
            response.setStatus(TransactionStatus.FAILED.name());
            response.setTransactionId(saved.getId());

            return response;
        } else {
            walletFrom.setBalance(walletFrom.getBalance().subtract(amount.add(getFee(amount))));

            //TODO send kafka

            transactions.setStatus(TransactionStatus.PENDING);

            Transactions saved = transactionsRepository.save(transactions);

            TransactionConfirmResponse response = new TransactionConfirmResponse();
            response.setStatus(TransactionStatus.PENDING.name());
            response.setTransactionId(saved.getId());

            return response;
        }
    }

    @Override
    public PaymentType getType() {
        return PaymentType.WITHDRAWAL;
    }
}