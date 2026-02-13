package com.example.paymentservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "payment_methods", schema = "payment_service")
public class PaymentMethod extends BaseEntity {
    @ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private PaymentProvider paymentProvider;
    private String type;
    private String name;
    private Boolean isActive;
    private String providerUniqueId;
    private String providerMethodType;
    private String logo;
    private String profileType;
}
