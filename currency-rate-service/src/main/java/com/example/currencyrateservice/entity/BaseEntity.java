package com.example.currencyrateservice.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    private Instant created;
    private Instant updated;

    @PrePersist
    void save() {
        this.created = Instant.now();
        this.updated = Instant.now();
    }

    @PreUpdate
    void update() {
        this.updated = Instant.now();
    }
}