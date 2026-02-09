package com.example.fakepaymentprovider.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "payouts", schema = "payment_provider")
public class Payout extends BaseEntity {
}
