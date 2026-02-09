package com.example.fakepaymentprovider.config;

import com.example.fakepaymentprovider.entity.Merchant;
import com.example.fakepaymentprovider.exception.AuthenticationException;
import com.example.fakepaymentprovider.repository.MerchantRepository;
import com.example.fakepaymentprovider.service.MetricsCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantDetailsProvider implements UserDetailsService {
    private final MerchantRepository merchantRepository;
    private final MetricsCollector metricsCollector;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("{} trying to authenticate", username);

        Merchant merchant = merchantRepository.findByMerchantId(username)
                .orElseThrow(() -> {
                    metricsCollector.recordAuthentication(false);
                    throw new AuthenticationException("Unauthorized");
                });

        metricsCollector.recordAuthentication(true);

        return new MerchantDetails(merchant);
    }
}
