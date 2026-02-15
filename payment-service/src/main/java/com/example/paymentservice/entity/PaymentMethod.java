package com.example.paymentservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@NamedEntityGraph(name = "requiredFields", attributeNodes = @NamedAttributeNode("requiredFields"))
@Table(name = "payment_methods", schema = "payment_service")
public class PaymentMethod extends BaseEntity {
    @ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private PaymentProvider paymentProvider;
    @OneToMany(mappedBy = "paymentMethod",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PaymentMethodRequiredField> requiredFields;
    private String type;
    private String name;
    private Boolean isActive;
    private String providerUniqueId;
    private String providerMethodType;
    private String logo;
    private String profileType;
}
