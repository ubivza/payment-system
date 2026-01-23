package com.example.currencyrateservice.service;

import com.example.currency.dto.RateProviderResponse;
import com.example.currencyrateservice.entity.RateProvider;
import com.example.currencyrateservice.mapper.RateProviderMapper;
import com.example.currencyrateservice.repository.RateProviderRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RateProviderService {
    private final RateProviderRepository repository;
    private final RateProviderMapper mapper;

    public List<RateProviderResponse> getAllRateProviders() {
        return repository.findAll().stream().map(mapper::map).toList();
    }

    @PostConstruct
    @Transactional
    public void createProviderIfNotExists() {
        if (repository.findAll().size() == 0) {
            log.info("CBR create provider logic invoked");

            RateProvider cbr = new RateProvider();
            cbr.setProviderCode("CBR");
            cbr.setDescription("Центральный банк России, обновление раз в сутки");
            cbr.setPriority(10);
            cbr.setActive(true);
            cbr.setProviderName("Центральный банк России");

            repository.save(cbr);
        }
    }
}
