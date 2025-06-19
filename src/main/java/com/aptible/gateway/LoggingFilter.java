package com.aptible.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
public class LoggingFilter implements GatewayFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.debug("Request URI: {}", request.getURI());

        // Log request details
        log.debug("Request Method: {}, Request URI: {}", request.getMethod(), request.getURI());
        log.debug("All Request Headers: {}", request.getHeaders());  // Log all headers

        // Log cookies specifically
        request.getCookies().forEach((key, value) ->
                log.debug("Request Cookie '{}' = {}", key, value)
        );

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();

            // Log response details
            log.debug("Response Status Code: {}", response.getStatusCode());
            log.debug("All Response Headers: {}", response.getHeaders());  // Log all headers

            // Log cookies specifically
            response.getCookies().forEach((key, value) ->
                    log.debug("Response Cookie '{}' = {}", key, value)
            );
        }));
    }
}