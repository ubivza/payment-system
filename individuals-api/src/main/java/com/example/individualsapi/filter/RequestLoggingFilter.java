package com.example.individualsapi.filter;

import com.example.individualsapi.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class RequestLoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (exchange.getRequest().getPath().toString().equals(LoggingUtils.PROMETHEUS_PATH)) {
            return chain.filter(exchange);
        }



        ServerHttpRequest decoratedRequest = new LoggingRequestDecorator(exchange.getRequest(), exchange);
        ServerWebExchange decoratedExchange = exchange.mutate().request(decoratedRequest).build();

        return chain.filter(decoratedExchange);
    }

    static String getHeaders(ServerWebExchange exchange) {
        StringBuilder sb = new StringBuilder();
        exchange.getRequest().getHeaders().entrySet().forEach(header -> LoggingUtils.writeHeadersToLog(header, sb));
        return sb.toString();
    }

    static String getPath(ServerWebExchange exchange) {
        return exchange.getRequest().getPath().value();
    }

    private static class LoggingRequestDecorator extends ServerHttpRequestDecorator {

        private static final Logger log = LoggerFactory.getLogger(LoggingRequestDecorator.class);
        private final ServerWebExchange exchange;

        public LoggingRequestDecorator(ServerHttpRequest delegate, ServerWebExchange exchange) {
            super(delegate);
            this.exchange = exchange;
        }

        //todo if body empty
        @Override
        public Flux<DataBuffer> getBody() {
            return super.getBody().map(dataBuffer -> {
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        DataBufferUtils.release(dataBuffer);

                        String bodyString = new String(content, StandardCharsets.UTF_8);
                        bodyString = LoggingUtils.maskBodySecrets(bodyString);
                        String headers = RequestLoggingFilter.getHeaders(exchange);
                        String path = RequestLoggingFilter.getPath(exchange);

                        log.info("Request headers: {}, path: {}, body: {}", headers, path, bodyString);

                        return DefaultDataBufferFactory.sharedInstance.wrap(content);
                    });
        }
    }
}
