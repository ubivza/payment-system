package com.example.fakepaymentprovider.repository;

import com.example.fakepaymentprovider.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WebhookRepository extends JpaRepository<Webhook, Long> {
    Optional<Webhook> findByEntityId(UUID entityId);
}
