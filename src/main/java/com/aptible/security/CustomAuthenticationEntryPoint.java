package com.aptible.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * handel login via html or not
 */
@Component
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    RedirectServerAuthenticationEntryPoint redirectServerAuthenticationEntryPoint = new RedirectServerAuthenticationEntryPoint("/dlogin");

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, org.springframework.security.core.AuthenticationException e) {
        //TODO prob could be better
        if(exchange.getRequest().getHeaders().getAccept().contains(MediaType.TEXT_HTML)) {
            return redirectServerAuthenticationEntryPoint.commence(exchange, e);
        }
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"error\": \"Unauthorized\", \"message\": \"You are not authorized to access this resource. Login at /dlogin\"}";

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
        );
    }
}