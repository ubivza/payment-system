package com.example.currencyrateservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rate_providers", schema = "currency_rate")
public class RateProvider extends BaseEntity {
    @Id
    private String providerCode;
    private String providerName;
    private String description;
    private Integer priority;
    private Boolean active;
}
