package com.example.paymentservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "payment_providers", schema = "payment_service")
public class PaymentProvider extends BaseEntity {
    private String name;
    private String description;
}
