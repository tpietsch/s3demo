package com.aptible.api;

import com.aptible.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

    private final JwtService jwtService;

    public LoginController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Mono<ResponseEntity<ApiResponse>> login(@RequestBody Mono<Models.LoginRequest> loginRequestMono) {
        return loginRequestMono.flatMap(loginRequest -> {
            // Dummy authentication logic
            //Bcrypt password checker if storing self users
//            if ("admin".equals(loginRequest.username()) && "password".equals(loginRequest.password())) {
                String token = jwtService.generateToken(new UserDetails() {
                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        var authorities = List.of("ROLE_ORG_1", "ROLE_CAN_UPLOAD", "ROLE_CAN_DOWNLOAD","ROLE_CAN_READ_FILES", "ROLE_CAN_DELETE");
                        return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
                    }

                    @Override
                    public String getPassword() {
                        return loginRequest.password();
                    }

                    @Override
                    public String getUsername() {
                        return loginRequest.username();
                    }
                });
                return Mono.just(ResponseEntity.ok(new Models.LoginResponse(token)));
//            } else {
//                var status = HttpStatus.UNAUTHORIZED;
//                return Mono.just(ResponseEntity
//                        .status(status)
//                        .body(new Models.ErrorMessage("Invalid credentials", status)));
//            }
        });
    }
}
