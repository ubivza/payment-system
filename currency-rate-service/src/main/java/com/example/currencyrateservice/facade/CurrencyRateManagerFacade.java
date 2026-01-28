package com.example.currencyrateservice.facade;

import com.example.currency.dto.CurrencyResponse;
import com.example.currencyrateservice.integration.CBRClientWrapper;
import com.example.currencyrateservice.integration.dto.CBRConversionRateResponse;
import com.example.currencyrateservice.service.ConversionRateService;
import com.example.currencyrateservice.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyRateManagerFacade {
    private final CBRClientWrapper cbrClientWrapper;
    private final ConversionRateService conversionRateService;
    private final CurrencyService currencyService;

    public void fetchAndUpdate() {
        CBRConversionRateResponse rates = cbrClientWrapper.getRates(formatActualTimeForCbrRequest());

        save(rates);
    }

    @Transactional
    public void save(CBRConversionRateResponse rates) {
        saveNewCurrencies(rates);

        saveNewConversionRates(rates);
    }

    private void saveNewConversionRates(CBRConversionRateResponse rates) {
        conversionRateService.saveRates(rates);
    }

    private String formatActualTimeForCbrRequest() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDateRequest = LocalDate.now().format(formatter);

        log.info("Date request {}", formattedDateRequest);

        return formattedDateRequest;
    }

    private void saveNewCurrencies(CBRConversionRateResponse rates) {
        List<String> requestedValuteCodes = rates.getCurrencies().stream()
                .map(CBRConversionRateResponse.Currency::getCharCode)
                .collect(Collectors.toList());

        List<String> existingValuteCodes = currencyService.getAllCurrencies().stream()
                .map(CurrencyResponse::getCode)
                .toList();

        requestedValuteCodes.removeAll(existingValuteCodes);

        if (requestedValuteCodes.size() != 0) {
            log.info("Got new currencies {}", requestedValuteCodes.size());
            List<CBRConversionRateResponse.Currency> newCurrencies = rates.getCurrencies().stream()
                    .filter(currency -> requestedValuteCodes.contains(currency.getCharCode()))
                    .toList();

            currencyService.saveCurrencies(newCurrencies);
        }
    }
}
