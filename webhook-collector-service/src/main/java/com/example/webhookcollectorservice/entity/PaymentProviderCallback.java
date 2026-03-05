package com.example.webhookcollectorservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payment_provider_callbacks", schema = "webhook_collector_service")
public class PaymentProviderCallback extends BaseEntity {
    private UUID providerTransactionUid;
    private String type;
    private String provider;
}
