package com.example.paymentservice.service;

import com.example.paymentservice.entity.PaymentMethod;
import com.example.paymentservice.entity.PaymentMethodDefinition;
import com.example.paymentservice.entity.PaymentProvider;
import com.example.paymentservice.repository.PaymentMethodDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentMethodDefinitionService {
    private final PaymentMethodDefinitionRepository repository;

    public List<PaymentMethod> getPaymentMethodBy(String currencyCode, String countryCode) {
        return repository.findAllByCurrencyCodeAndCountryAlpha3Code(currencyCode, countryCode).stream()
                .map(PaymentMethodDefinition::getPaymentMethod)
                .toList();
    }

    @Transactional
    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        if (repository.count() == 0) {
            log.info("Saving test payment methods");

            PaymentProvider provider1 = new PaymentProvider();
            provider1.setName("Stripe");
            provider1.setDescription("Stripe global payments");

            PaymentMethod visaGlobal = new PaymentMethod();
            visaGlobal.setPaymentProvider(provider1);
            visaGlobal.setType("CARD");
            visaGlobal.setName("Visa");
            visaGlobal.setIsActive(true);
            visaGlobal.setProviderUniqueId("visa");
            visaGlobal.setProviderMethodType("card");
            visaGlobal.setLogo("https://example.com/logos/visa.png");
            visaGlobal.setProfileType("CONSUMER");

            PaymentMethodDefinition def1 = new PaymentMethodDefinition();
            def1.setPaymentMethod(visaGlobal);
            def1.setCurrencyCode(null);
            def1.setCountryAlpha3Code(null);
            def1.setIsAllCurrencies(true);
            def1.setIsAllCountries(true);
            def1.setIsPriority(true);
            def1.setIsActive(true);

            PaymentProvider provider2 = new PaymentProvider();
            provider2.setName("Apple Pay");
            provider2.setDescription("Apple Pay via Stripe / Adyen");

            PaymentMethod applePay = new PaymentMethod();
            applePay.setPaymentProvider(provider2);
            applePay.setType("WALLET");
            applePay.setName("Apple Pay");
            applePay.setIsActive(true);
            applePay.setProviderUniqueId("apple_pay");
            applePay.setProviderMethodType("apple_pay");
            applePay.setLogo("https://example.com/logos/applepay.svg");
            applePay.setProfileType("CONSUMER");

            PaymentMethodDefinition def2 = new PaymentMethodDefinition();
            def2.setPaymentMethod(applePay);
            def2.setCurrencyCode("USD");
            def2.setCountryAlpha3Code("USA");
            def2.setIsAllCurrencies(false);
            def2.setIsAllCountries(false);
            def2.setIsPriority(false);
            def2.setIsActive(true);

            PaymentProvider provider3 = new PaymentProvider();
            provider3.setName(" GMO Payment Gateway");
            provider3.setDescription("Japanese local acquiring");

            PaymentMethod jcb = new PaymentMethod();
            jcb.setPaymentProvider(provider3);
            jcb.setType("CARD");
            jcb.setName("JCB");
            jcb.setIsActive(true);
            jcb.setProviderUniqueId("jcb");
            jcb.setProviderMethodType("card");
            jcb.setLogo("https://example.com/logos/jcb.png");
            jcb.setProfileType("CONSUMER");

            PaymentMethodDefinition def3 = new PaymentMethodDefinition();
            def3.setPaymentMethod(jcb);
            def3.setCurrencyCode("JPY");
            def3.setCountryAlpha3Code("JPN");
            def3.setIsAllCurrencies(false);
            def3.setIsAllCountries(false);
            def3.setIsPriority(true);
            def3.setIsActive(true);

            repository.saveAll(List.of(def1, def2, def3));
        }
    }
}
