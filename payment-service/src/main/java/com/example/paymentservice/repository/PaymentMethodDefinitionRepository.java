package com.example.paymentservice.repository;

import com.example.paymentservice.entity.PaymentMethodDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentMethodDefinitionRepository extends JpaRepository<PaymentMethodDefinition, UUID> {
    List<PaymentMethodDefinition> findAllByCurrencyCodeAndCountryAlpha3Code(String currencyCode, String countryCode);
}
