package com.example.transactionservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "wallets", schema = "transaction_service")
public class Wallets extends BaseEntity {
    private String name;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "wallet_type_id", referencedColumnName = "id")
    private WalletTypes walletType;
    private UUID userId;
    @Enumerated(value = EnumType.STRING)
    private ActivityStatus status;
    private BigDecimal balance;
    private Instant archivedAt;
}
