package com.example.personservice.controller;

import com.example.person.api.PersonApiClient;
import com.example.person.dto.IndividualDto;
import com.example.person.dto.RegistrationResponse;
import com.example.person.dto.UpdateDto;
import com.example.personservice.service.IndividualService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class IndividualController implements PersonApiClient {
    private final IndividualService individualService;

    @Override
    public ResponseEntity<Void> compensateFailedRegistration(UUID id, String authorization) {
        individualService.deleteIndividual(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteIndividual(UUID id, String authorization) {
        individualService.deactivateIndividual(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<IndividualDto> getIndividualById(UUID id, String email, String authorization) {
        return ResponseEntity.ok()
                .body(individualService.getIndividual(id, email));
    }

    @Override
    public ResponseEntity<RegistrationResponse> registration(String authorization, IndividualDto individualDto) {
        return ResponseEntity.ok()
                .body(individualService.registerIndividual(individualDto));
    }

    @Override
    public ResponseEntity<Void> updateIndividualById(UUID id, String authorization, UpdateDto individualDto) {
        individualService.updateIndividual(id, individualDto);
        return ResponseEntity.ok().build();
    }
}
