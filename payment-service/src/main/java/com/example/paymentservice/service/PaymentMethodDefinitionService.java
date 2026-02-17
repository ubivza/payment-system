package com.example.paymentservice.service;

import com.example.paymentservice.entity.PaymentMethod;
import com.example.paymentservice.entity.PaymentMethodDefinition;
import com.example.paymentservice.entity.PaymentMethodRequiredField;
import com.example.paymentservice.entity.PaymentProvider;
import com.example.paymentservice.repository.PaymentMethodDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

            List<PaymentMethodRequiredField> firstList = new ArrayList<>();

            PaymentMethodRequiredField field1 = new PaymentMethodRequiredField();
            field1.setPaymentMethod(visaGlobal);
            field1.setPaymentType("CREDIT_CARD");
            field1.setCountryAlpha3Code("USA");
            field1.setName("cardNumber");
            field1.setDataType("STRING");
            field1.setValidationType("REGEX");
            field1.setValidationRule("^[0-9]{16}$");
            field1.setDefaultValue(null);
            field1.setValuesOptions(null);
            field1.setDescription("Credit card number");
            field1.setPlaceholder("1234 5678 9012 3456");
            field1.setRepresentationName("Card Number");
            field1.setLanguage("en");
            field1.setIsActive(true);
            firstList.add(field1);

            PaymentMethodRequiredField field2 = new PaymentMethodRequiredField();
            field2.setPaymentMethod(visaGlobal);
            field2.setPaymentType("CREDIT_CARD");
            field2.setCountryAlpha3Code("ALL");
            field2.setName("cvv");
            field2.setDataType("STRING");
            field2.setValidationType("REGEX");
            field2.setValidationRule("^[0-9]{3,4}$");
            field2.setDefaultValue(null);
            field2.setValuesOptions(null);
            field2.setDescription("Card verification value");
            field2.setPlaceholder("123");
            field2.setRepresentationName("CVV");
            field2.setLanguage("en");
            field2.setIsActive(true);
            firstList.add(field2);

            visaGlobal.setRequiredFields(firstList);

            List<PaymentMethodRequiredField> secondList = new ArrayList<>();

            PaymentMethodRequiredField field3 = new PaymentMethodRequiredField();
            field3.setPaymentMethod(applePay);
            field3.setPaymentType("CREDIT_CARD");
            field3.setCountryAlpha3Code("FRA");
            field3.setName("expiryDate");
            field3.setDataType("STRING");
            field3.setValidationType("REGEX");
            field3.setValidationRule("^(0[1-9]|1[0-2])\\/[0-9]{2}$");
            field3.setDefaultValue(null);
            field3.setValuesOptions(null);
            field3.setDescription("Card expiration date (MM/YY)");
            field3.setPlaceholder("MM/YY");
            field3.setRepresentationName("Expiration Date");
            field3.setLanguage("fr");
            field3.setIsActive(true);
            secondList.add(field3);

            PaymentMethodRequiredField field4 = new PaymentMethodRequiredField();
            field4.setPaymentMethod(applePay);
            field4.setPaymentType("BANK_TRANSFER");
            field4.setCountryAlpha3Code("DEU");
            field4.setName("iban");
            field4.setDataType("STRING");
            field4.setValidationType("REGEX");
            field4.setValidationRule("^DE[0-9]{20}$");
            field4.setDefaultValue(null);
            field4.setValuesOptions(null);
            field4.setDescription("International Bank Account Number");
            field4.setPlaceholder("DE12 3456 7890 1234 5678 90");
            field4.setRepresentationName("IBAN");
            field4.setLanguage("de");
            field4.setIsActive(true);
            secondList.add(field4);

            applePay.setRequiredFields(secondList);

            List<PaymentMethodRequiredField> thirdList = new ArrayList<>();

            PaymentMethodRequiredField field5 = new PaymentMethodRequiredField();
            field5.setPaymentMethod(jcb);
            field5.setPaymentType("PIX");
            field5.setCountryAlpha3Code("BRA");
            field5.setName("documentType");
            field5.setDataType("ENUM");
            field5.setValidationType("ENUM");
            field5.setValidationRule(null);
            field5.setDefaultValue("CPF");
            field5.setValuesOptions("CPF,CNPJ,EMAIL,PHONE");
            field5.setDescription("Type of document for PIX transfer");
            field5.setPlaceholder("Select document type");
            field5.setRepresentationName("Document Type");
            field5.setLanguage("pt");
            field5.setIsActive(true);
            thirdList.add(field5);

            PaymentMethodRequiredField field6 = new PaymentMethodRequiredField();
            field6.setPaymentMethod(jcb);
            field6.setPaymentType("MOBILE_MONEY");
            field6.setCountryAlpha3Code("KEN");
            field6.setName("phoneNumber");
            field6.setDataType("STRING");
            field6.setValidationType("REGEX");
            field6.setValidationRule("^254[0-9]{9}$");
            field6.setDefaultValue(null);
            field6.setValuesOptions(null);
            field6.setDescription("Mobile phone number for M-PESA");
            field6.setPlaceholder("254712345678");
            field6.setRepresentationName("Phone Number");
            field6.setLanguage("sw");
            field6.setIsActive(true);
            thirdList.add(field6);

            jcb.setRequiredFields(thirdList);

            repository.saveAll(List.of(def1, def2, def3));
        }
    }
}
