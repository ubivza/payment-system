package com.example.transactionservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "wallet_types", schema = "transaction_service")
public class WalletTypes extends BaseEntity {
    private String name;
    private String currencyCode;
    @Enumerated(value = EnumType.STRING)
    private ActivityStatus status;
    private Instant archivedAt;
    private String userType;
    private String creator;
    private String modifier;
}
