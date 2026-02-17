package com.example.fakepaymentprovider.service;

import com.example.fakepaymentprovider.entity.Merchant;
import com.example.fakepaymentprovider.exception.AuthenticationException;
import com.example.fakepaymentprovider.repository.MerchantRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MerchantService {
    private final MerchantRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UUID getCurrentMerchantInnerId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String merchantId = authentication.getName();

        Merchant merchant = repository.findByMerchantId(merchantId)
                .orElseThrow(() -> new AuthenticationException("Unauthorized"));

        return merchant.getId();
    }

    public String getCurrentMerchantId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return authentication.getName();
    }

    @PostConstruct
    @Transactional
    public void createMerchant() {
        if (repository.findAll().size() == 0) {
            log.info("Creating test merchant");

            Merchant merchant = new Merchant();
            merchant.setMerchantId("adidas");
            merchant.setName("OOO Adidas Ru");
            merchant.setSecretKey(passwordEncoder.encode("ya_lublu_adidas12"));

            Merchant merchantTwo = new Merchant();
            merchantTwo.setMerchantId("puma");
            merchantTwo.setName("OOO Puma Ru");
            merchantTwo.setSecretKey(passwordEncoder.encode("ya_lublu_puma12"));

            Merchant merchantPaymentSystem = new Merchant();
            merchantPaymentSystem.setMerchantId("payment-system");
            merchantPaymentSystem.setName("OOO Payment-system");
            merchantPaymentSystem.setSecretKey(passwordEncoder.encode("p@$$w0rD"));

            repository.saveAll(List.of(merchant, merchantTwo, merchantPaymentSystem));
        }
    }
}
