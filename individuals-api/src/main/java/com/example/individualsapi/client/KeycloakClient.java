package com.example.individualsapi.client;

import com.example.dto.TokenResponse;
import com.example.dto.UserInfoResponse;
import com.example.dto.UserRegistrationRequest;
import com.example.individualsapi.exception.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class KeycloakClient {

    private final WebClient keycloakClient;

    private final String REALM;
    private final String CLIENT_ID;
    private final String CLIENT_SECRET;

    public KeycloakClient(@Value("${keycloak.host}") String keycloakUrl, @Value("${keycloak.realm}") String realm,
                          @Value("${keycloak.client-id}") String clientId, @Value("${keycloak.client-secret}") String clientSecret) {
        this.keycloakClient = WebClient.builder()
                .baseUrl(keycloakUrl)
                .build();
        this.REALM = realm;
        this.CLIENT_ID = clientId;
        this.CLIENT_SECRET = clientSecret;
    }

    public Mono<ResponseEntity<Void>> createUser(UserRegistrationRequest request) {
        return getAdminToken().flatMap(adminToken -> sendCreateUserRequest(request, adminToken));
    }

    public Mono<TokenResponse> getUserToken(String email, String password) {
        return keycloakClient.post().uri("/realms/{realm}/protocol/openid-connect/token", REALM)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("username", email)
                        .with("password", password)
                        .with("client_id", CLIENT_ID)
                        .with("client_secret", CLIENT_SECRET))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new BadCredentialsException("Неверный логин или пароль")))
                .bodyToMono(TokenResponse.class);
    }

    public Mono<TokenResponse> refreshToken(String refreshToken) {
        return keycloakClient.post().uri("/realms/{realm}/protocol/openid-connect/token", REALM)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("refresh_token", refreshToken)
                        .with("client_id", CLIENT_ID)
                        .with("client_secret", CLIENT_SECRET))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new BadCredentialsException("Недействительный или просроченный refresh token")))
                .bodyToMono(TokenResponse.class);
    }

    public Mono<UserInfoResponse> getUserInfo(String currentUserUid) {
        return getAdminToken().flatMap(adminToken -> getUserInfo(currentUserUid, adminToken));
    }

    private Mono<UserInfoResponse> getUserInfo(String currentUserId, TokenResponse adminToken) {
        return keycloakClient.get().uri("/admin/realms/{realm}/users/{id}", REALM, currentUserId)
                .header("Authorization", "Bearer " + adminToken.getAccessToken())
                .retrieve()
                .onStatus(HttpStatus.UNAUTHORIZED::equals, response -> Mono.error(new BadCredentialsException("Недействительный токен")))
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> Mono.error(new NotFoundException("Пользователь не найден")))
                .bodyToMono(UserInfoResponse.class);
    }

    private Mono<TokenResponse> getAdminToken() {
        return keycloakClient.post().uri("/realms/{realm}/protocol/openid-connect/token", REALM)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", CLIENT_ID)
                        .with("client_secret", CLIENT_SECRET))
                .retrieve()
                .bodyToMono(TokenResponse.class);
    }

    private Mono<ResponseEntity<Void>> sendCreateUserRequest(UserRegistrationRequest request, TokenResponse adminToken) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", request.getEmail());
        user.put("email", request.getEmail());
        user.put("emailVerified", true);
        user.put("enabled", true);

        List<Map<String, Object>> credentials = new ArrayList<>();
        Map<String, Object> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", request.getPassword());
        credential.put("temporary", false);
        credentials.add(credential);

        user.put("credentials", credentials);

        return keycloakClient.post().uri("/admin/realms/{realm}/users", REALM)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken.getAccessToken())
                .body(BodyInserters.fromValue(user))
                .retrieve()
                .onStatus(HttpStatus.BAD_REQUEST::equals, response -> Mono.error(new NotValidException("Ошибка валидации")))
                .onStatus(HttpStatus.CONFLICT::equals, response ->
                        response.bodyToMono(KeycloakErrorResponse.class)
                                .map(keycloakErr -> new UserAlreadyExistsException(keycloakErr.getErrorMessage()))
                                .flatMap(Mono::error))
                .toBodilessEntity();
    }
}
