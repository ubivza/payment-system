package com.example.individualsapi.controller;

import com.example.dto.*;
import com.example.individualsapi.service.api.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${individuals-api.path}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/registration")
    public Mono<ResponseEntity<TokenResponse>> registration(@RequestBody UserRegistrationRequest registrationRequest) {
        return userService.registerUser(registrationRequest)
                .map(tokenResponse ->
                        ResponseEntity.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(tokenResponse));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> login(@RequestBody UserLoginRequest loginRequest) {
        return userService.loginUser(loginRequest)
                .map(tokenResponse ->
                        ResponseEntity.status(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(tokenResponse));
    }

    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<TokenResponse>> refreshToken(@RequestBody TokenRefreshRequest refreshRequest) {
        return userService.refreshUserToken(refreshRequest)
                .map(tokenResponse ->
                        ResponseEntity.status(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(tokenResponse));
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserInfoResponse>> me() {
        return userService.getUserInfo()
                .map(userInfoResponse ->
                        ResponseEntity.status(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(userInfoResponse));
    }
}
