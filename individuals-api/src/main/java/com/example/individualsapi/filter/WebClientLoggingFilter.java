package com.example.individualsapi.filter;

import com.example.individualsapi.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

@Slf4j
public class WebClientLoggingFilter {

    //log body TODO
    public static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            StringBuilder infoToLog = new StringBuilder();
            infoToLog.append("Request keycloak method: ");

            String method = clientRequest.method().name();
            infoToLog.append(method);

            String uri = clientRequest.url().toString();
            infoToLog.append("; URI: ");
            infoToLog.append(uri);
            infoToLog.append("; Headers: ");

            clientRequest.headers().entrySet().forEach(header -> LoggingUtils.writeHeadersToLog(header, infoToLog));

            clientRequest.body();
            log.info(infoToLog.toString());
            return Mono.just(clientRequest);
        });
    }

    //log body TODO
    public static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            StringBuilder infoToLog = new StringBuilder();

            infoToLog.append("Response keycloak status code: ");
            String httpStatusCode = clientResponse.statusCode().toString();
            infoToLog.append(httpStatusCode);

            infoToLog.append("; Headers: ");
            clientResponse.headers().asHttpHeaders().entrySet().forEach(header -> LoggingUtils.writeHeadersToLog(header, infoToLog));

//            clientResponse.bodyToMono();
            log.info(infoToLog.toString());
            return Mono.just(clientResponse);
        });
    }
}
