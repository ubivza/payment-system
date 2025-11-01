package com.example.personservice.service;

import com.example.person.dto.IndividualDto;
import com.example.person.dto.RegistrationResponse;
import com.example.person.dto.UpdateDto;
import com.example.personservice.entity.Address;
import com.example.personservice.entity.Country;
import com.example.personservice.entity.Individual;
import com.example.personservice.entity.User;
import com.example.personservice.exception.NotFoundException;
import com.example.personservice.mapper.IndividualMapper;
import com.example.personservice.repository.IndividualRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndividualService {
    private final IndividualRepository individualRepository;
    private final IndividualMapper mapper;

    @Transactional
    public void deleteIndividual(UUID id) {
        findIndividualById(id, "delete");
        individualRepository.deleteById(id);
        log.info("User with id {} successfully deleted", id);
    }

    @Transactional
    public void deactivateIndividual(UUID id) {
        findIndividualById(id, "deactivate");
        individualRepository.setIndividualStatusInactive(id);
        log.info("User with id {} successfully deactivated", id);
    }

    @Transactional(readOnly = true)
    public IndividualDto getIndividual(UUID id, String email) {
        Individual individual = individualRepository.getIndividualByIdAndUserEmail(id, email)
                .orElseThrow(() -> {
                    log.info("Failed to find user with id {}", id);
                    return new NotFoundException("Individual with this id and email not found");
                });
        log.info("User with id {} successfully found", id);
        return mapper.toIndividualDto(individual);
    }

    @Transactional
    public RegistrationResponse registerIndividual(IndividualDto individualDto) {
        Individual individual = mapper.toEntity(individualDto);

        UUID savedIndividualId = individualRepository.save(individual).getId();
        log.info("User with email {} successfully registered", individual.getUser().getEmail());
        return mapper.toRegistrationResponse(savedIndividualId);
    }

    @Transactional
    public void updateIndividual(UUID id, UpdateDto individualDto) {
        Individual individualPersistent = findIndividualById(id, "update");

        updateIndividualEntity(individualPersistent, individualDto);

        individualRepository.save(individualPersistent);
        log.info("User with id {} successfully updated", id);
    }

    private Individual findIndividualById(UUID id, String operation) {
        return individualRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Failed to find individual with id {} while trying to {}", id, operation);
                    return new NotFoundException("Individual with id " + id + " not found");
                });
    }

    private void updateIndividualEntity(Individual individual, UpdateDto dto) {
        updateCountry(individual.getUser().getAddress().getCountry(), dto);
        updateAddress(individual.getUser().getAddress(), dto);
        updateUser(individual.getUser(), dto);
        updateIndividualFields(individual, dto);
    }

    private void updateCountry(Country country, UpdateDto dto) {
        if (isNotEmpty(dto.getName())) {
            country.setName(dto.getName());
        }
        if (isNotEmpty(dto.getAlpha2())) {
            country.setAlpha2(dto.getAlpha2());
        }
        if (isNotEmpty(dto.getAlpha3())) {
            country.setAlpha3(dto.getAlpha3());
        }
    }

    private void updateAddress(Address address, UpdateDto dto) {
        if (isNotBlank(dto.getAddress())) {
            address.setAddress(dto.getAddress());
        }
        if (isNotBlank(dto.getZipCode())) {
            address.setZipCode(dto.getZipCode());
        }
        if (isNotBlank(dto.getCity())) {
            address.setCity(dto.getCity());
        }
        if (isNotBlank(dto.getState())) {
            address.setState(dto.getState());
        }
    }

    private void updateUser(User user, UpdateDto dto) {
        if (isNotBlank(dto.getFirstName())) {
            user.setFirstName(dto.getFirstName());
        }
        if (isNotBlank(dto.getLastName())) {
            user.setLastName(dto.getLastName());
        }
    }

    private void updateIndividualFields(Individual individual, UpdateDto dto) {
        if (isNotBlank(dto.getPassportNumber())) {
            individual.setPassportNumber(dto.getPassportNumber());
        }
        if (isNotBlank(dto.getPhoneNumber())) {
            individual.setPhoneNumber(dto.getPhoneNumber());
        }

        individual.setVerifiedAt(Instant.now());
    }
}