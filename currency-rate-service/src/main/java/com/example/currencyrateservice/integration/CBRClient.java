package com.example.currencyrateservice.integration;

import com.example.currencyrateservice.integration.dto.CBRConversionRateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(url = "${rate.provider.cbr.url}",
        name = "cbr-client")
public interface CBRClient {
    @GetMapping(value = "/scripts/XML_daily.asp", consumes = MediaType.APPLICATION_XML_VALUE)
    CBRConversionRateResponse getRates(@RequestParam("date_req") String dateRequest);
}
