package com.example.individualsapi.service.impl;


import com.example.individualsapi.exception.BadCredentialsException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
public class ContextUserSubExtractor {

    private static final String INNER_ID_CLAIM = "inner_id";
    private static final String EMAIL_CLAIM = "email";

    public Mono<String> getCurrentUserSub() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(authentication -> {
                    Jwt jwt = (Jwt) authentication.getPrincipal();
                    return jwt.getSubject();
                });
    }

    public Mono<UserRequestData> getCurrentUserRequestData() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(authentication -> {
                    Jwt jwt = (Jwt) authentication.getPrincipal();
                    Map<String, Object> claims = jwt.getClaims();
                    String innerId = claims.get(INNER_ID_CLAIM).toString();
                    String email = claims.get(EMAIL_CLAIM).toString();

                    return UserRequestData.builder().innerId(innerId).email(email).build();
                })
                .onErrorResume(err -> {
                    log.warn("Got request with token without required claims", err);
                    return Mono.error(new BadCredentialsException("Недействительный токен"));
                });
    }

    @Getter
    @Setter
    @Builder
    public static class UserRequestData {
        private String innerId;
        private String email;
    }
}