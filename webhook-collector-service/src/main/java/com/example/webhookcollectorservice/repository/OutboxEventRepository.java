package com.example.webhookcollectorservice.repository;

import com.example.webhookcollectorservice.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findTop100ByOrderByIdAsc();
}
