package com.example.personservice.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Getter
@Setter
@Entity
@Audited
@Table(name = "users", schema = "person")
public class User extends BaseEntity {
    private String secretKey;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean filled;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;
}
