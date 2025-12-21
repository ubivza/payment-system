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
    @Query("update Transactions t set t.status = :status where t.id = :transactionId")
    void updateStatus(@Param("transactionId") UUID transactionId, @Param("status") TransactionStatus status);
    @Query("select t.wallet.id from Transactions t where t.id = :transactionId")
    UUID findWalletId(@Param("transactionId") UUID transactionId);
    @Modifying
    @Query("update Transactions t set t.failureReason = :failureReason where t.id = :transactionId")
    void updateFailureReason(@Param("transactionId") UUID transactionId, @Param("failureReason") String failureReason);
}
