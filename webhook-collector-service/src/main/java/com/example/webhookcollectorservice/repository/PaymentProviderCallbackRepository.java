package com.example.webhookcollectorservice.repository;

import com.example.webhookcollectorservice.entity.PaymentProviderCallback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentProviderCallbackRepository extends JpaRepository<PaymentProviderCallback, UUID> {
}
