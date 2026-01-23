package com.example.currencyrateservice.service;

import com.example.currency.dto.RateResponse;
import com.example.currencyrateservice.entity.ConversionRate;
import com.example.currencyrateservice.entity.Currency;
import com.example.currencyrateservice.exception.NotFoundException;
import com.example.currencyrateservice.integration.dto.CBRConversionRateResponse;
import com.example.currencyrateservice.mapper.ConversionRateMapper;
import com.example.currencyrateservice.repository.ConversionRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConversionRateService {
    private final ConversionRateRepository repository;
    private final RateProviderService rateProviderService;
    private final ConversionRateMapper mapper;
    private final CurrencyService currencyService;

    public RateResponse getRateFromToForTime(String from, String to, OffsetDateTime timestamp) {
        if (from.equals(to)) {
            return buildOneToOneRate(from);
        }

        Currency source = currencyService.getByCode(from);
        Currency destination = currencyService.getByCode(to);

        if (timestamp == null) {
            return latestRate(source, destination);
        }

        return repository.findCurrentRate(source, destination, timestamp.toInstant())
                .map(rate -> mapper.map(rate, rateProviderService.getAllRateProviders().get(0).getProviderCode()))
                .orElseGet(() -> latestRate(source, destination));
    }

    private RateResponse buildOneToOneRate(String from) {
        RateResponse response = new RateResponse();
        response.setSourceCode(from);
        response.setDestinationCode(from);
        response.setRateTimestamp(mapper.fromInstant(Instant.now()));
        response.setProviderCode(rateProviderService.getAllRateProviders().get(0).getProviderCode());
        response.setRate(BigDecimal.ONE);

        return response;
    }

    @Transactional
    public void saveRates(CBRConversionRateResponse rates) {
        List<ConversionRate> toSave = rates.getCurrencies().stream()
                .map(currency -> {
                    ConversionRate conversionRate = mapper.fromCbr(currency);

                    Instant instant = parseString(rates.getDate());

                    conversionRate.setRateBeginTime(instant);
                    conversionRate.setRateEndTime(instant);

                    conversionRate.setDestinationCode(currencyService.getByCode("RUB"));
                    conversionRate.setSourceCode(currencyService.getByCode(currency.getCharCode()));

                    conversionRate.setRate(new BigDecimal(replaceCommaToDot(currency.getVunitRate())));

                    return conversionRate;
                })
                .collect(Collectors.toList());

        List<ConversionRate> toSaveReversed = rates.getCurrencies().stream()
                .map(currency -> {
                    ConversionRate conversionRate = mapper.fromCbr(currency);

                    Instant instant = parseString(rates.getDate());

                    conversionRate.setRateBeginTime(instant);
                    conversionRate.setRateEndTime(instant);

                    conversionRate.setDestinationCode(currencyService.getByCode(currency.getCharCode()));
                    conversionRate.setSourceCode(currencyService.getByCode("RUB"));

                    conversionRate.setRate(getReversedRate(new BigDecimal(replaceCommaToDot(currency.getVunitRate()))));

                    return conversionRate;
                })
                .toList();

        toSave.addAll(toSaveReversed);

        repository.saveAll(toSave);
    }

    private RateResponse latestRate(Currency source, Currency destination) {
        return repository.findLatestRate(source, destination)
                .map(rate -> mapper.map(rate, rateProviderService.getAllRateProviders().get(0).getProviderCode()))
                .orElseThrow(() -> new NotFoundException(String.format("Latest rate not found for pair %s to %s", source.getCode(), destination.getCode())));
    }

    private Instant parseString(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate localDate = LocalDate.parse(date, formatter);

        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private BigDecimal getReversedRate(BigDecimal rate) {
        return BigDecimal.ONE.divide(rate, 10, RoundingMode.HALF_UP);
    }

    private String replaceCommaToDot(String vunitRate) {
        return vunitRate.replace(',', '.');
    }
}
