package com.example.transactionservice.config;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "confirmation-token")
public class TokenProperties {
    private String secretKey;
    private Long expirationTime;

    public SecretKey getEncryptedSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}
