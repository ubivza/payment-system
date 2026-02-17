package com.example.paymentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "payment_method_required_fields", schema = "payment_service",
        uniqueConstraints = @UniqueConstraint(columnNames = {
                "language",
                "name",
                "payment_method_id",
                "payment_type",
                "country_alpha3_code"
            }
        )
)
public class PaymentMethodRequiredField extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;
    private String paymentType;
    @Column(name = "country_alpha3_code")
    private String countryAlpha3Code;
    private String name;
    private String dataType;
    private String validationType;
    private String validationRule;
    private String defaultValue;
    private String valuesOptions;
    private String description;
    private String placeholder;
    private String representationName;
    private String language;
    private Boolean isActive;
}