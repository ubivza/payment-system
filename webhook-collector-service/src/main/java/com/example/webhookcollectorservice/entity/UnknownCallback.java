package com.example.webhookcollectorservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "unknown_callbacks", schema = "webhook_collector_service")
public class UnknownCallback extends BaseEntity {
}
