package com.example.personservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Audited
@Table(name = "individuals", schema = "person")
public class Individual {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;
    private String passportNumber;
    private String phoneNumber;
    private Instant verifiedAt;
    private Instant archivedAt;
    private String status;
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
