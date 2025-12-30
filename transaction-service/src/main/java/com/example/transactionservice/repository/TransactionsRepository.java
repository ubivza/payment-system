package com.example.transactionservice.repository;

import com.example.transactionservice.entity.TransactionStatus;
import com.example.transactionservice.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TransactionsRepository extends JpaRepository<Transactions, UUID> {
    @Modifying
    @Query("update Transactions t set t.status = :status, t.failureReason = :failureReason where t.id = :transactionId")
    void updateStatusAndFailureReason(@Param("transactionId") UUID transactionId, @Param("failureReason") String failureReason, @Param("status") TransactionStatus status);
    @Query("select t.wallet.id from Transactions t where t.id = :transactionId")
    UUID findWalletId(@Param("transactionId") UUID transactionId);
    @Query("select t.targetWalletId from Transactions t where t.id = :transactionId")
    UUID findTargetWalletId(@Param("transactionId") UUID transactionId);
}
