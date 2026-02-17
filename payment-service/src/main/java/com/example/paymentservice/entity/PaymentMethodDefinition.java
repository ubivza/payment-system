package com.example.paymentservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "payment_method_definitions", schema = "payment_service")
public class PaymentMethodDefinition extends BaseEntity {
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;
    private String currencyCode;
    @Column(name = "country_alpha3_code")
    private String countryAlpha3Code;
    private Boolean isAllCurrencies;
    private Boolean isAllCountries;
    private Boolean isPriority;
    private Boolean isActive;
}
