package com.example.currencyrateservice.mapper;

import com.example.currency.dto.RateResponse;
import com.example.currencyrateservice.entity.ConversionRate;
import com.example.currencyrateservice.integration.dto.CBRConversionRateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ConversionRateMapper {
    @Mapping(target = "sourceCode", source = "entity.sourceCode.code")
    @Mapping(target = "destinationCode", source = "entity.destinationCode.code")
    @Mapping(target = "rateTimestamp", source = "entity.rateBeginTime", qualifiedByName = "fromInstant")
    RateResponse map(ConversionRate entity, String providerCode);
    @Mapping(target = "rate", expression = "java(new java.math.BigDecimal(currency.getVunitRate().replaceAll(\",\", \".\")))")
    @Mapping(target = "id", ignore = true)
    ConversionRate fromCbr(CBRConversionRateResponse.Currency currency);

    @Named("fromInstant")
    default OffsetDateTime fromInstant(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
