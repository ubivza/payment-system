package com.example.currencyrateservice.mapper;

import com.example.currency.dto.CurrencyResponse;
import com.example.currencyrateservice.entity.Currency;
import com.example.currencyrateservice.integration.dto.CBRConversionRateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CurrencyMapper {
    CurrencyResponse map(Currency entity);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", source = "charCode")
    @Mapping(target = "isoCode", source = "numCode")
    @Mapping(target = "symbol", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "description", source = "name")
    Currency mapFromCbr(CBRConversionRateResponse.Currency currency);
}
