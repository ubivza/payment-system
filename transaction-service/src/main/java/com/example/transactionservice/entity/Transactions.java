package com.example.transactionservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "transactions", schema = "transaction_service")
public class Transactions extends BaseEntity {
    private UUID userId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", referencedColumnName = "id")
    private Wallets wallet;
    private BigDecimal amount;
    @Enumerated(value = EnumType.STRING)
    private PaymentType type;
    @Enumerated(value = EnumType.STRING)
    private TransactionStatus status;
    private String comment;
    private BigDecimal fee;
    private UUID targetWalletId;
    private Long paymentMethodId;
    private String failureReason;
}
