package com.example.individualsapi.controller;

import com.example.individuals.dto.UserUpdateRequest;
import com.example.individualsapi.service.api.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${individuals-api.path}/individual")
@RequiredArgsConstructor
public class IndividualsController {

    private final UserService userService;

    @DeleteMapping("/delete")
    public Mono<ResponseEntity<Void>> deleteUser() {
        return userService.deleteUser()
                .map(response ->
                        ResponseEntity.ok().build());
    }

    @PutMapping("/update")
    public Mono<ResponseEntity<Void>> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        return userService.updateUser(userUpdateRequest)
                .map(response ->
                        ResponseEntity.ok().build());
    }
}
