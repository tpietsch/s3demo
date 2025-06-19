package com.aptible.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class LoginPageController {
    public Mono<ServerResponse> login(ServerRequest serverRequest) {
        Map<String, Object> model = new HashMap<>();
        model.put("error", Optional.ofNullable(serverRequest.queryParams().get("error")).isPresent());
        return ServerResponse
                .ok()
                .render("login", model);
    }
}

