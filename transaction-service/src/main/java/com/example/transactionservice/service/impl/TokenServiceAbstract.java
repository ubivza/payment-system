package com.example.transactionservice.service.impl;

import com.example.transactionservice.config.TokenProperties;
import com.example.transactionservice.exception.BadRequest;
import com.example.transactionservice.service.api.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

import java.util.Date;
import java.util.Map;

import static com.example.transactionservice.constant.CommonClaimsForToken.ISSUER_NAME;

public abstract class TokenServiceAbstract implements TokenService {
    public static final String WALLET_UID = "walletUid";
    public static final String USER_UID = "userUid";
    public static final String AMOUNT = "amount";
    public static final String TIMESTAMP = "timestamp";
    public static final String WALLET_FROM_UID = "walletFromUid";
    public static final String WALLET_TO_UID = "walletToUid";
    private final TokenProperties tokenProperties;

    protected TokenServiceAbstract(TokenProperties tokenProperties) {
        this.tokenProperties = tokenProperties;
    }

    protected String buildToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims().add(claims)
                .and()
                .subject(subject)
                .issuer(ISSUER_NAME)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + tokenProperties.getExpirationTime()))
                .signWith(tokenProperties.getEncryptedSecretKey())
                .compact();
    }

    protected Claims validateAndGet(String token, String subject) {
        Claims claims = parse(token);

        if (expired(claims.getExpiration())) {
            throw new BadRequest("Confirmation time expired, initialize new transaction.");
        }

        if (!correctSubject(claims.getSubject(), subject)) {
            throw new BadRequest("Invalid token, initialize new transaction.");
        }

        return claims;
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(tokenProperties.getEncryptedSecretKey())
                .requireIssuer(ISSUER_NAME)
                .build()
                .parse(token)
                .accept(Jws.CLAIMS)
                .getPayload();
    }

    private static boolean expired(Date expiration) {
        if (expiration == null) {
            return true;
        }

        return expiration.before(new Date(System.currentTimeMillis()));
    }

    private static boolean correctSubject(String actual, String expected) {
        return actual.equalsIgnoreCase(expected);
    }
}
