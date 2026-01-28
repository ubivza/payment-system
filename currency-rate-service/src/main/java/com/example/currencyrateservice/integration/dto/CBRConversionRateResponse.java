package com.example.currencyrateservice.integration.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@JsonRootName("ValCurs")
public class CBRConversionRateResponse {
    @JacksonXmlProperty(isAttribute = true, localName = "Date")
    private String date;

    @JacksonXmlProperty(isAttribute = true, localName = "name")
    private String name;

    @JacksonXmlProperty(localName = "Valute")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Currency> currencies;


    @Setter
    @Getter
    public static class Currency {

        @JacksonXmlProperty(isAttribute = true, localName = "ID")
        private String id;

        @JacksonXmlProperty(localName = "NumCode")
        private String numCode;

        @JacksonXmlProperty(localName = "CharCode")
        private String charCode;

        @JacksonXmlProperty(localName = "Nominal")
        private Integer nominal;

        @JacksonXmlProperty(localName = "Name")
        private String name;

        @JacksonXmlProperty(localName = "Value")
        private String value;

        @JacksonXmlProperty(localName = "VunitRate")
        private String vunitRate;
    }
}
