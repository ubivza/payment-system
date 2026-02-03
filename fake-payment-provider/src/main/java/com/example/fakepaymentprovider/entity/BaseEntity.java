package com.example.fakepaymentprovider.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;
    private UUID merchantId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String externalId;
    private String notificationUrl;

    @PrePersist
    void save() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void update() {
        this.updatedAt = Instant.now();
    }
}