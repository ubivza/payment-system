package com.example.currencyrateservice.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients("com.example.currencyrateservice.integration")
public class FeignConfig {
}
