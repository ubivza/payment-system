package com.example.transactionservice.repository;

import com.example.transactionservice.entity.Wallets;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletsRepository extends JpaRepository<Wallets, UUID> {
    Optional<Wallets> findByUserIdAndId(UUID userUid, UUID walletUid);
}
