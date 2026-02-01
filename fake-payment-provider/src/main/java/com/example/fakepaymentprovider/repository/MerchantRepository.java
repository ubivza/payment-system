package com.example.fakepaymentprovider.repository;

import com.example.fakepaymentprovider.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
}
