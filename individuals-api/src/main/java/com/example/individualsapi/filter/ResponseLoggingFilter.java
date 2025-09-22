package com.example.individualsapi.filter;

import com.example.individualsapi.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class ResponseLoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (exchange.getRequest().getPath().toString().equals(LoggingUtils.PROMETHEUS_PATH)) {
            return chain.filter(exchange);
        }
        ServerHttpResponse decoratedResponse = new LoggingResponseDecorator(exchange.getResponse(), exchange);
        HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
        if (statusCode != null) {
            exchange.getResponse().setRawStatusCode(statusCode.value());
        }

        ServerWebExchange decoratedExchange = exchange.mutate().response(decoratedResponse).build();

        return chain.filter(decoratedExchange);
    }

    private static class LoggingResponseDecorator extends ServerHttpResponseDecorator {

        private static final Logger log = LoggerFactory.getLogger(LoggingResponseDecorator.class);
        private final ServerWebExchange exchange;

        public LoggingResponseDecorator(ServerHttpResponse delegate, ServerWebExchange exchange) {
            super(delegate);
            this.exchange = exchange;
        }

        //todo if body empty
        @Override
        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
            return super.writeWith(fluxBody
                    .map(dataBuffer -> {
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        DataBufferUtils.release(dataBuffer);

                        String bodyString = new String(content, StandardCharsets.UTF_8);
                        bodyString = LoggingUtils.maskBodySecrets(bodyString);
                        String headers = getHeaders(exchange);
                        String status = getStatus(exchange);

                        log.info("Response headers: {}, status: {}, body: {}", headers, status, bodyString);

                        return getDelegate().bufferFactory().wrap(content);
                    }));
        }

        private String getHeaders(ServerWebExchange exchange) {
            StringBuilder sb = new StringBuilder();
            exchange.getResponse().getHeaders().entrySet().forEach(header -> LoggingUtils.writeHeadersToLog(header, sb));
            return sb.toString();
        }

        private String getStatus(ServerWebExchange exchange) {
            return exchange.getResponse().getStatusCode().toString();
        }
    }
}
