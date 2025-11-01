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
@Table(name = "addresses", schema = "person")
public class Address extends BaseEntity {
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", referencedColumnName = "id")
    private Country country;
    private String address;
    private String zipCode;
    private Instant archived;
    private String city;
    private String state;
}
