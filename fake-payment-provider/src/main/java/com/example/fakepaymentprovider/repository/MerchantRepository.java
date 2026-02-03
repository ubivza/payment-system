package com.example.fakepaymentprovider.repository;

import com.example.fakepaymentprovider.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    Optional<Merchant> findByMerchantId(String merchantId);
}
