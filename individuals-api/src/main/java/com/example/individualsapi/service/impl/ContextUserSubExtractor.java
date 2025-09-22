package com.example.individualsapi.service.impl;


import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ContextUserSubExtractor {

    public Mono<String> getCurrentUserSub() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(authentication -> {
                    Jwt jwt = (Jwt) authentication.getPrincipal();
                    return jwt.getSubject();
                });
    }
}