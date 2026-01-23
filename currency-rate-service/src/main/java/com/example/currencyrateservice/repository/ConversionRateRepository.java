package com.example.currencyrateservice.repository;

import com.example.currencyrateservice.entity.ConversionRate;
import com.example.currencyrateservice.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ConversionRateRepository extends JpaRepository<ConversionRate, UUID> {
    @Query("select cr from ConversionRate cr " +
            "where cr.sourceCode = :sourceCode " +
            "and cr.destinationCode = :destinationCode " +
            "and cr.rateBeginTime <= :timestamp " +
            "and (cr.rateEndTime >= :timestamp or cr.rateEndTime is NULL)")
    Optional<ConversionRate> findCurrentRate(@Param("sourceCode") Currency sourceCode,
                                             @Param("destinationCode") Currency destinationCode,
                                             @Param("timestamp") Instant timestamp);

    @Query("select cr from ConversionRate cr " +
            "where cr.sourceCode = :sourceCode " +
            "and cr.destinationCode = :destinationCode " +
            "order by cr.rateBeginTime desc limit 1")
    Optional<ConversionRate> findLatestRate(@Param("sourceCode") Currency sourceCode,
                                            @Param("destinationCode") Currency destinationCode);
}
