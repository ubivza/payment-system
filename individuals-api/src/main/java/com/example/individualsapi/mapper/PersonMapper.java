package com.example.individualsapi.mapper;

import com.example.individuals.dto.UserInfoResponse;
import com.example.individuals.dto.UserRegistrationRequest;
import com.example.individuals.dto.UserUpdateRequest;
import com.example.person.dto.IndividualDto;
import com.example.person.dto.UpdateDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonMapper {
    IndividualDto map(UserRegistrationRequest registrationRequest);
    UserInfoResponse map(IndividualDto individualDto);
    UpdateDto map(UserUpdateRequest userUpdateRequest);
}
