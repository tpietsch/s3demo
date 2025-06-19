package com.aptible.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
public class MeRestController {
    @GetMapping("/me")
    @PreAuthorize("hasRole('testing_perm1')")
    public Mono<Principal> user(Mono<Principal> principal) {
        return principal;
    }

    @GetMapping("/me2")
    @PreAuthorize("hasRole('testing_perm2')")
    public Mono<Principal> user2(Mono<Principal> principal) {
        return principal;
    }
}

