package com.example.fakepaymentprovider.repository;

import com.example.fakepaymentprovider.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookRepository extends JpaRepository<Webhook, Long> {
}
