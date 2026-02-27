package com.example.webhookcollectorservice.repository;

import com.example.webhookcollectorservice.entity.UnknownCallback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UnknownCallbackRepository extends JpaRepository<UnknownCallback, UUID> {
}
