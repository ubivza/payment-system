package com.example.currencyrateservice.mapper;

import com.example.currency.dto.RateProviderResponse;
import com.example.currencyrateservice.entity.RateProvider;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RateProviderMapper {
    RateProviderResponse map(RateProvider entity);
}
