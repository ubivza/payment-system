package com.example.personservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.Instant;

@Getter
@Setter
@Entity
@Audited
@Table(name = "countries", schema = "person")
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Instant created;
    private Instant updated;
    private String name;
    private String alpha2;
    private String alpha3;
    private String status;

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
