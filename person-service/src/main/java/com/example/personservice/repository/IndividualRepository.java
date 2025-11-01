package com.example.personservice.repository;

import com.example.personservice.entity.Individual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IndividualRepository extends JpaRepository<Individual, UUID> {
    @Modifying
    @Query("update Individual i set i.status = 'INACTIVE', i.archivedAt = CURRENT_TIMESTAMP where i.id = :id")
    void setIndividualStatusInactive(UUID id);

    Optional<Individual> getIndividualByIdAndUserEmail(UUID id, String email);
}
