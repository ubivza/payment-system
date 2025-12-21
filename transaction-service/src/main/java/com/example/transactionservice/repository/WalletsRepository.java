package com.example.transactionservice.repository;

import com.example.transactionservice.entity.Wallets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface WalletsRepository extends JpaRepository<Wallets, UUID> {
    Optional<Wallets> findByUserIdAndId(UUID userUid, UUID walletUid);
    @Modifying
    @Query("update Wallets w set w.balance = w.balance + :amount where w.id = :walletId")
    void incrementBalance(@Param("walletId") UUID walletId, @Param("amount") BigDecimal amount);
}
