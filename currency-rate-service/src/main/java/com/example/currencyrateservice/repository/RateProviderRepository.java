package com.example.currencyrateservice.repository;

import com.example.currencyrateservice.entity.RateProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RateProviderRepository extends JpaRepository<RateProvider, String> {
}
