package com.example.fakepaymentprovider.repository;

import com.example.fakepaymentprovider.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface PayoutRepository extends JpaRepository<Payout, UUID>, JpaSpecificationExecutor<Payout> {
    Optional<Payout> findByIdAndMerchantId(UUID id, UUID merchantId);
}
