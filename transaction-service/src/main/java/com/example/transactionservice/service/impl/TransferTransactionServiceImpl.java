package com.example.transactionservice.service.impl;

import com.example.transaction.dto.ConfirmRequest;
import com.example.transaction.dto.InitTransactionRequest;
import com.example.transaction.dto.TransactionConfirmResponse;
import com.example.transaction.dto.TransactionInitResponse;
import com.example.transaction.dto.TransferInitRequest;
import com.example.transaction.dto.WalletResponse;
import com.example.transactionservice.constant.FailureReason;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static com.example.transactionservice.service.impl.TokenServiceAbstract.AMOUNT;
import static com.example.transactionservice.service.impl.TokenServiceAbstract.RATE;
import static com.example.transactionservice.service.impl.TokenServiceAbstract.USER_UID;
import static com.example.transactionservice.service.impl.TokenServiceAbstract.WALLET_FROM_UID;
import static com.example.transactionservice.service.impl.TokenServiceAbstract.WALLET_TO_UID;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TransferTransactionServiceImpl extends TransactionServiceAbstract {
    private final TransactionsRepository transactionsRepository;
    private final TokenTypeStrategyResolver tokenTypeStrategyResolver;
    private final WalletService walletService;

    @Autowired
    protected TransferTransactionServiceImpl(TransactionsRepository transactionsRepository, TransactionsMapper mapper,
                                             TokenTypeStrategyResolver tokenTypeStrategyResolver, WalletService walletService) {
        super(transactionsRepository, mapper);
        this.transactionsRepository = transactionsRepository;
        this.tokenTypeStrategyResolver = tokenTypeStrategyResolver;
        this.walletService = walletService;
    }

    @Override
    public TransactionInitResponse init(InitTransactionRequest initTransactionRequest, String valuteFrom, String valuteTo) {
        TransferInitRequest transferInitRequest = (TransferInitRequest) initTransactionRequest;

        WalletResponse walletResponse = walletService.get(transferInitRequest.getUserUid().toString(), transferInitRequest.getWalletFromUid().toString());
        Wallets walletTo = walletService.get(transferInitRequest.getWalletToUid().toString());

        if(!walletResponse.getCurrencyCode().equals(valuteFrom) || !walletTo.getWalletType().getCurrencyCode().equals(valuteTo)) {
            throw new BadRequest("Wrong valutes");
        }

        if (isBalanceNotEnough(walletResponse.getBalance().add(super.getFee(transferInitRequest.getAmount())), transferInitRequest.getAmount())) {
            throw new BadRequest(String.format(FailureReason.NOT_ENOUGH_BALANCE, transferInitRequest.getWalletFromUid()));
        }

        if (ActivityStatus.DISABLED.name().equalsIgnoreCase(walletResponse.getStatus())) {
            throw new BadRequest(String.format("Your wallet %s is disabled", transferInitRequest.getWalletFromUid()));
        }

        if (ActivityStatus.DISABLED.equals(walletTo.getStatus())) {
            throw new BadRequest(String.format("Wallet %s you try to transfer to is disabled", transferInitRequest.getWalletToUid()));
        }

        String token = tokenTypeStrategyResolver.resolve(PaymentType.TRANSFER.name()).generate(initTransactionRequest);

        TransactionInitResponse response = new TransactionInitResponse();
        response.setFee(addFee(transferInitRequest.getAmount(), transferInitRequest.getAmount()));
        response.setAvailable(true);
        response.setToken(token);
        response.setAmount(transferInitRequest.getAmount());
        response.setCurrency(valuteFrom);

        return response;
    }

    @Override
    @Transactional
    public TransactionConfirmResponse confirm(ConfirmRequest confirmRequest) {
        Claims claims = tokenTypeStrategyResolver.resolve(PaymentType.TRANSFER.name()).validateAndGetClaims(confirmRequest.getToken());

        BigDecimal amount = new BigDecimal(claims.get(AMOUNT, String.class));
        BigDecimal amountInValuteTo = amount.multiply(new BigDecimal(claims.get(RATE, String.class)));

        Transactions transactions = new Transactions();
        transactions.setAmount(amount);
        transactions.setType(PaymentType.TRANSFER);
        transactions.setFee(getFee(amount));

        UUID walletFromUid = UUID.fromString(claims.get(WALLET_FROM_UID, String.class));
        UUID walletToUid = UUID.fromString(claims.get(WALLET_TO_UID, String.class));

        Wallets walletFrom = walletService.get(walletFromUid.toString());
        Wallets walletTo = walletService.get(walletToUid.toString());

        transactions.setTargetWalletId(UUID.fromString(claims.get(WALLET_TO_UID, String.class)));
        transactions.setWallet(walletFrom);
        transactions.setUserId(UUID.fromString(claims.get(USER_UID, String.class)));

        if (walletFrom.getBalance().subtract(amount.add(getFee(amount))).compareTo(BigDecimal.ZERO) < 0) {
            transactions.setStatus(TransactionStatus.FAILED);
            transactions.setFailureReason(String.format(FailureReason.NOT_ENOUGH_BALANCE, walletFromUid));

            Transactions saved = transactionsRepository.save(transactions);

            TransactionConfirmResponse response = new TransactionConfirmResponse();
            response.setStatus(TransactionStatus.FAILED.name());
            response.setTransactionId(saved.getId());

            return response;
        } else {
            walletService.depositMoney(walletFrom.getId(), amount.add(getFee(amount)).negate());
            walletService.depositMoney(walletTo.getId(), amountInValuteTo);

            transactions.setStatus(TransactionStatus.COMPLETED);

            Transactions saved = transactionsRepository.save(transactions);

            TransactionConfirmResponse response = new TransactionConfirmResponse();
            response.setStatus(TransactionStatus.COMPLETED.name());
            response.setTransactionId(saved.getId());

            return response;
        }
    }

    @Override
    public PaymentType getType() {
        return PaymentType.TRANSFER;
    }
}
