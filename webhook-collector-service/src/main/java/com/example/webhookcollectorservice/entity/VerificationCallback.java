package com.example.webhookcollectorservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "verification_callbacks", schema = "webhook_collector_service")
public class VerificationCallback extends BaseEntity {
    private UUID transactionUid;
    private UUID profileUid;
    private String status;
    private String type;
}
