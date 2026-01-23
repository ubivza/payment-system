package com.example.currencyrateservice.integration;

import com.example.currency.dto.CurrencyResponse;
import com.example.currency.dto.RateProviderResponse;
import com.example.currency.dto.RateResponse;
import com.example.currencyrateservice.config.Container;
import com.example.currencyrateservice.repository.ConversionRateRepository;
import com.example.currencyrateservice.repository.CurrencyRepository;
import com.example.currencyrateservice.repository.RateProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CurrencyRateIntegrationTest extends Container {
    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    ConversionRateRepository conversionRateRepository;
    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    RateProviderRepository rateProviderRepository;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    static {
        Container.start();
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
    @DisplayName("Wait is scheduling work up -> get currencies 200")
    public void isSchedulingWorkingThenGetCurrencies() throws InterruptedException {
        assertEquals(1, rateProviderRepository.findAll().size());

        Thread.sleep(10000);

        assertEquals(5, currencyRepository.findAll().size());
        assertEquals(8, conversionRateRepository.findAll().size());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");

        HttpEntity<Void> getEntity = new HttpEntity<>(headers);

        String getCurrenciesUrl = "/v1/currencies";

        ResponseEntity<List<CurrencyResponse>> getResponse = restTemplate.exchange(getCurrenciesUrl, HttpMethod.GET, getEntity, new ParameterizedTypeReference<>() {});

        List<CurrencyResponse> response = getResponse.getBody();
        assertEquals(200, getResponse.getStatusCode().value());
        assertEquals(5, response.stream().map(CurrencyResponse::getCode).filter(code -> expectedCurrencies().contains(code)).toList().size());
    }

    @Test
    @DisplayName("Get rate providers 200")
    public void isRateProviderCBR() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");

        HttpEntity<Void> getEntity = new HttpEntity<>(headers);

        String getRateProviders = "/v1/rate-providers";

        ResponseEntity<List<RateProviderResponse>> getResponse = restTemplate.exchange(getRateProviders, HttpMethod.GET, getEntity, new ParameterizedTypeReference<>() {});

        assertEquals(200, getResponse.getStatusCode().value());
        List<RateProviderResponse> response = getResponse.getBody();
        assertEquals(1, response.size());
        assertEquals(response.get(0), expectedProvider());
    }

    @Test
    @DisplayName("Get rates 200")
    public void getRates() throws InterruptedException {
        Thread.sleep(10000);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");

        HttpEntity<Void> getEntity = new HttpEntity<>(headers);

        String getRates = "/v1/rates?from=USD&to=RUB";

        ResponseEntity<RateResponse> getResponse = restTemplate.exchange(getRates, HttpMethod.GET, getEntity, RateResponse.class);

        RateResponse response = getResponse.getBody();
        assertEquals(200, getResponse.getStatusCode().value());
        assertEquals(BigDecimal.valueOf(78.2267), response.getRate());

        String getRatesReverse = "/v1/rates?from=RUB&to=USD";

        ResponseEntity<RateResponse> getResponseReverse = restTemplate.exchange(getRatesReverse, HttpMethod.GET, getEntity, RateResponse.class);

        RateResponse responseReverse = getResponseReverse.getBody();
        assertEquals(200, getResponseReverse.getStatusCode().value());
        assertEquals(BigDecimal.ONE.divide(BigDecimal.valueOf(78.2267), 10, RoundingMode.HALF_UP), responseReverse.getRate());
    }

    private List<String> expectedCurrencies() {
        return List.of("RUB", "USD", "EUR", "GBP", "AUD");
    }

    private RateProviderResponse expectedProvider() {

        RateProviderResponse cbr = new RateProviderResponse();
        cbr.setProviderCode("CBR");
        cbr.setDescription("Центральный банк России, обновление раз в сутки");
        cbr.setPriority(10);
        cbr.setActive(true);
        cbr.setProviderName("Центральный банк России");

        return cbr;
    }
}
