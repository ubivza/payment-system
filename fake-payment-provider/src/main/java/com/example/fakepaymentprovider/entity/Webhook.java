package com.example.fakepaymentprovider.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "webhooks", schema = "payment_provider")
public class Webhook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventType;
    private UUID entityId;
    @JdbcTypeCode(SqlTypes.JSON)
    private WebhookPayload payload;
    private Instant receivedAt;
    private String notificationUrl;

    @PrePersist
    public void prePersist() {
        this.receivedAt = Instant.now();
    }
}
