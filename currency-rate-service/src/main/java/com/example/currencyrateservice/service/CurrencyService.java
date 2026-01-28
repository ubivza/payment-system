package com.example.currencyrateservice.service;

import com.example.currency.dto.CurrencyResponse;
import com.example.currencyrateservice.entity.Currency;
import com.example.currencyrateservice.exception.NotFoundException;
import com.example.currencyrateservice.integration.dto.CBRConversionRateResponse;
import com.example.currencyrateservice.mapper.CurrencyMapper;
import com.example.currencyrateservice.repository.CurrencyRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CurrencyService {
    private final CurrencyRepository repository;
    private final CurrencyMapper mapper;

    @PostConstruct
    @Transactional
    public void addRub() {
        if (repository.findByCode("RUB").isEmpty()) {
            Currency rub = new Currency();
            rub.setDescription("Рубли Российские");
            rub.setCode("RUB");
            rub.setActive(true);
            rub.setIsoCode(643);

            repository.save(rub);
        }
    }

    public List<CurrencyResponse> getAllCurrencies() {
        return repository.findAll().stream().map(mapper::map).toList();
    }

    @Transactional
    public void saveCurrencies(List<CBRConversionRateResponse.Currency> newCurrencies) {
        List<Currency> toSave = newCurrencies.stream()
                .map(mapper::mapFromCbr)
                .toList();
        repository.saveAll(toSave);
    }

    public Currency getByCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new NotFoundException(String.format("Not supported currency %s", code)));
    }
}
