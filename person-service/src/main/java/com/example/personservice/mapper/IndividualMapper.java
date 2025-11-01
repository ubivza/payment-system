package com.example.personservice.mapper;

import com.example.person.dto.IndividualDto;
import com.example.person.dto.RegistrationResponse;
import com.example.personservice.entity.Individual;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface IndividualMapper {
    @Mapping(source = "status", target = "userStatus")
    @Mapping(source = "user.secretKey", target = "secretKey")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.filled", target = "filled")
    @Mapping(source = "user.address.address", target = "address")
    @Mapping(source = "user.address.zipCode", target = "zipCode")
    @Mapping(source = "user.address.city", target = "city")
    @Mapping(source = "user.address.state", target = "state")
    @Mapping(source = "user.address.country.name", target = "name")
    @Mapping(source = "user.address.country.alpha2", target = "alpha2")
    @Mapping(source = "user.address.country.alpha3", target = "alpha3")
    @Mapping(source = "user.address.country.status", target = "status")
    IndividualDto toIndividualDto(Individual individual);

    @Mapping(target = "status", source = "userStatus")
    @Mapping(target = "verifiedAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "archivedAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "user.secretKey", source = "secretKey")
    @Mapping(target = "user.email", source = "email")
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.filled", source = "filled")
    @Mapping(target = "user.address.address", source = "address")
    @Mapping(target = "user.address.zipCode", source = "zipCode")
    @Mapping(target = "user.address.city", source = "city")
    @Mapping(target = "user.address.state", source = "state")
    @Mapping(target = "user.address.country.name", source = "name")
    @Mapping(target = "user.address.country.alpha2", source = "alpha2")
    @Mapping(target = "user.address.country.alpha3", source = "alpha3")
    @Mapping(target = "user.address.country.status", source = "status")
    Individual toEntity(IndividualDto individualDto);

    @Mapping(target = "userUid", source = "savedIndividualId")
    RegistrationResponse toRegistrationResponse(UUID savedIndividualId);
}
