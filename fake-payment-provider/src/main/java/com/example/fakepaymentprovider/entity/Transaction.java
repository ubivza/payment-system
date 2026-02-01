package com.example.fakepaymentprovider.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "transactions", schema = "payment_provider")
public class Transaction extends BaseEntity {
    private String method;
    private String description;
}
