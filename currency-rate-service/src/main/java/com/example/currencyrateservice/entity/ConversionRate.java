package com.example.currencyrateservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "conversion_rates", schema = "currency_rate")
public class ConversionRate extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "source_code", referencedColumnName = "code")
    private Currency sourceCode;
    @ManyToOne
    @JoinColumn(name = "destination_code", referencedColumnName = "code")
    private Currency destinationCode;
    private Instant rateBeginTime;
    private Instant rateEndTime;
    private BigDecimal rate;
}
