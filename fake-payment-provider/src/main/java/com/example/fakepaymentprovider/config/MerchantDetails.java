package com.example.fakepaymentprovider.config;

import com.example.fakepaymentprovider.entity.Merchant;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class MerchantDetails implements UserDetails {
    private final Merchant merchant;

    public MerchantDetails(Merchant merchant) {
        this.merchant = merchant;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return merchant.getSecretKey();
    }

    @Override
    public String getUsername() {
        return merchant.getMerchantId();
    }
}
