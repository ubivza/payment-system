package com.example.fakepaymentprovider.repository;

import com.example.fakepaymentprovider.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutRepository extends JpaRepository<Payout, Long> {
}
