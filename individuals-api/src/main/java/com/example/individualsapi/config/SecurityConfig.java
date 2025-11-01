package com.example.individualsapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@EnableWebFlux
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${individuals-api.path}")
    private String apiPath;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange(authorizeExchangeSpec ->
                authorizeExchangeSpec.pathMatchers(collectAuthenticatedPaths())
                        .authenticated()
                        .anyExchange()
                        .permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable);
        return http.build();
    }

    private String[] collectAuthenticatedPaths() {
        return new String[]{
                apiPath + "/auth/me",
                apiPath + "/individual/delete",
                apiPath + "/individual/update"};
    }
}
