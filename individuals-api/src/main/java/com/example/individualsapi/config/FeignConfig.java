package com.example.individualsapi.config;

import com.example.person.api.PersonApiClient;
import com.example.transaction.api.TransactionApiClient;
import com.example.transaction.api.WalletApiClient;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackageClasses = {PersonApiClient.class,
        TransactionApiClient.class,
        WalletApiClient.class})
public class FeignConfig {

    @Bean
    public HttpMessageConverters httpMessageConverters() {
        return new HttpMessageConverters();
    }
}
