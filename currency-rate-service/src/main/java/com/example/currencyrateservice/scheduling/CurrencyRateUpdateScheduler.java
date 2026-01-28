package com.example.currencyrateservice.scheduling;

import com.example.currencyrateservice.facade.CurrencyRateManagerFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyRateUpdateScheduler {
    private final CurrencyRateManagerFacade currencyRateManagerFacade;

    @Scheduled(fixedDelayString = "${scheduling.update-currency-rate.event-rate}")
    @SchedulerLock(name = "updateRates", lockAtLeastFor = "10s", lockAtMostFor = "1m")
    public void updateCurrencyRates() {
        log.info("Scheduled rate update started");
        currencyRateManagerFacade.fetchAndUpdate();
    }
}
