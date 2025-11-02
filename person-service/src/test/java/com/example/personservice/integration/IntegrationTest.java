package com.example.personservice.integration;

import com.example.person.dto.IndividualDto;
import com.example.person.dto.RegistrationResponse;
import com.example.personservice.config.PGSQLContainer;
import com.example.personservice.entity.Individual;
import com.example.personservice.repository.IndividualRepository;
import com.example.personservice.service.IndividualService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest extends PGSQLContainer {

    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    IndividualRepository individualRepository;
    @Autowired
    IndividualService individualService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @AfterEach
    void clear() {
        individualRepository.deleteAll();
    }

    @BeforeEach
    void stubSecurity() {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("scope", "message:read")
                .build();

        Mockito.when(jwtDecoder.decode(anyString())).thenReturn(jwt);
    }

    @Test
    @DisplayName("Test registration flow success")
    void testRegistration() {
        IndividualDto dto = createTestIndividualDto();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        HttpEntity<IndividualDto> entity = new HttpEntity<>(dto, headers);

        ResponseEntity<RegistrationResponse> response = restTemplate.postForEntity("/v1/individuals", entity, RegistrationResponse.class);

        Individual actual = individualRepository.findAll().get(0);
        assertEquals(201, response.getStatusCode().value());
        assertEquals(UUID.fromString(response.getBody().getUserUid()), actual.getId());
        assertEquals(dto.getPhoneNumber(), actual.getPhoneNumber());
    }

    @Test
    @DisplayName("Test compensate failed registration success")
    void testCompensateFailedRegistration() {
        IndividualDto dto = createTestIndividualDto();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        HttpEntity<IndividualDto> entity = new HttpEntity<>(dto, headers);

        ResponseEntity<RegistrationResponse> response = restTemplate.postForEntity("/v1/individuals", entity, RegistrationResponse.class);

        assertEquals(1, individualRepository.findAll().size());

        ResponseEntity<Void> compensateResponse = restTemplate.exchange(String.format("/v1/individuals/compensate-registration/%s", response.getBody().getUserUid()), HttpMethod.DELETE, entity, Void.class);

        assertEquals(200, compensateResponse.getStatusCode().value());
        assertEquals(0, individualRepository.findAll().size());
    }

    @Test
    @DisplayName("Test delete individual success success")
    void testDeleteIndividual() {
        IndividualDto dto = createTestIndividualDto();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        HttpEntity<IndividualDto> entity = new HttpEntity<>(dto, headers);

        ResponseEntity<RegistrationResponse> response = restTemplate.postForEntity("/v1/individuals", entity, RegistrationResponse.class);

        assertEquals(1, individualRepository.findAll().size());

        String deleteUrl = UriComponentsBuilder.fromPath("/v1/individuals/{id}")
                .uriVariables(Map.of("id", response.getBody().getUserUid()))
                .toUriString();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);

        Individual actual = individualRepository.findAll().get(0);
        assertEquals(200, deleteResponse.getStatusCode().value());
        assertEquals(1, individualRepository.findAll().size());
        assertEquals("INACTIVE", actual.getStatus());
    }

    @Test
    @DisplayName("Test get individual by id and email success")
    void testGetIndividualByIdAndEmail() {
        IndividualDto dto = createTestIndividualDto();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        HttpEntity<IndividualDto> entity = new HttpEntity<>(dto, headers);

        ResponseEntity<RegistrationResponse> response = restTemplate.postForEntity("/v1/individuals", entity, RegistrationResponse.class);

        assertEquals(1, individualRepository.findAll().size());

        HttpEntity<Void> entityGet = new HttpEntity<>(headers);

        String getUrl = UriComponentsBuilder.fromPath("/v1/individuals")
                .queryParam("id", UUID.fromString(response.getBody().getUserUid()))
                .queryParam("email", dto.getEmail())
                .toUriString();

        ResponseEntity<IndividualDto> getResponse = restTemplate.exchange(getUrl, HttpMethod.GET, entityGet, IndividualDto.class);

        IndividualDto actual = getResponse.getBody();
        assertEquals(dto.getPhoneNumber(), actual.getPhoneNumber());
        assertEquals(dto.getAlpha3(), actual.getAlpha3());
    }

    @Test
    @DisplayName("Test update individual by id success")
    void testUpdateById() {
        IndividualDto dto = createTestIndividualDto();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        HttpEntity<IndividualDto> entity = new HttpEntity<>(dto, headers);

        ResponseEntity<RegistrationResponse> response = restTemplate.postForEntity("/v1/individuals", entity, RegistrationResponse.class);

        assertEquals(1, individualRepository.findAll().size());

        IndividualDto dtoUpdate = createTestIndividualDto();
        dtoUpdate.setAddress("new address");
        dtoUpdate.setFirstName("new first name");

        HttpEntity<IndividualDto> entityUpdate = new HttpEntity<>(dtoUpdate, headers);

        ResponseEntity<Void> updateResponse = restTemplate.exchange(String.format("/v1/individuals/%s", response.getBody().getUserUid()), HttpMethod.PUT, entityUpdate, Void.class);

        IndividualDto actual = individualService.getIndividual(UUID.fromString(response.getBody().getUserUid()), dtoUpdate.getEmail());
        assertEquals(200, updateResponse.getStatusCode().value());
        assertEquals(1, individualRepository.findAll().size());
        assertNotEquals(dto.getAddress(), actual.getAddress());
        assertNotEquals(dto.getFirstName(), actual.getFirstName());
        assertEquals(dto.getPhoneNumber(), actual.getPhoneNumber());
        assertEquals(UUID.fromString(response.getBody().getUserUid()), individualRepository.findAll().get(0).getId());
    }

    @Test
    @DisplayName("Test get individual by id and email not found")
    void testGetIndividualByIdAndEmailNotFound() {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        assertEquals(0, individualRepository.findAll().size());

        String getUrl = UriComponentsBuilder.fromPath("/v1/individuals")
                .queryParam("id", UUID.randomUUID())
                .queryParam("email", "email")
                .toUriString();

        ResponseEntity<IndividualDto> getResponse = restTemplate.exchange(getUrl, HttpMethod.GET, entity, IndividualDto.class);

        assertEquals(404, getResponse.getStatusCode().value());
    }

    private static IndividualDto createTestIndividualDto() {
        IndividualDto dto = new IndividualDto();

        dto.setPassportNumber("1234567890");
        dto.setPhoneNumber("+79991234567");
        dto.setEmail("test.user@example.com");
        dto.setFirstName("Иван");
        dto.setLastName("Петров");
        dto.setAddress("ул. Примерная, д. 123");
        dto.setZipCode("123456");
        dto.setCity("Москва");
        dto.setState("Московская область");
        dto.setName("Россия");
        dto.setAlpha2("RU");
        dto.setAlpha3("RUS");
        dto.setStatus("ACTIVE");
        dto.setUserStatus("VERIFIED");
        dto.setSecretKey("secretKey");
        dto.setFilled(true);

        return dto;
    }
}
