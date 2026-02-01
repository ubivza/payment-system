package com.example.fakepaymentprovider.repository;

import com.example.fakepaymentprovider.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
