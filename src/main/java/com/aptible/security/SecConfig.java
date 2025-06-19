package com.aptible.security;

import com.aptible.filters.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.authorization.IpAddressReactiveAuthorizationManager;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
//https://docs.spring.io/spring-security/reference/reactive/configuration/webflux.html
//https://www.baeldung.com/spring-security-5-reactive
//https://docs.spring.io/spring-security/reference/reactive/configuration/webflux.html
//https://docs.spring.io/spring-security/reference/reactive/authentication/concurrent-sessions-control.html
public class SecConfig {
    private static final String[] ALLOW_IPS = {"104.32.243.146"};
    private final JwtService jwtService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final ReactiveClientRegistrationRepository clientRegistrationRepository;

    private static Mono<AuthorizationDecision> checkIps(Mono<Authentication> authentication,
                                                        AuthorizationContext context) {
        //ALB should be behind ips already this is just an extra limiting option
        return Arrays.stream(ALLOW_IPS).map(ip -> IpAddressReactiveAuthorizationManager.hasIpAddress(ip)
                .check(authentication, context)).reduce((acc, curr) ->
                acc.filter(AuthorizationDecision::isGranted).or(curr)).get();
    }

    @Bean
    public ReactiveOAuth2UserService<OidcUserRequest, OidcUser> customOAuth2UserService() {
        return new OidcReactiveOAuth2UserService() {
            @Override
            public Mono<OidcUser> loadUser(OidcUserRequest userRequest) {
                return super.loadUser(userRequest)
                        .flatMap(oidcUser -> Mono.fromCallable(() -> {
                            //TODO custom auth for oauth/overrides if need
                            Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());
                            var g =  oidcUser.getAttribute("groups");
                            if (g != null) {
                                List<GrantedAuthority> res = ((List<Object>)g).stream().map(x -> new SimpleGrantedAuthority("ROLE_" + x))
                                        .collect(Collectors.toList());
                                authorities.addAll(res);
                            }
                            return new CustomOidcUser(authorities, oidcUser.getIdToken());
                        }));
            }
        };
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        //                    csrf.csrfTokenRepository(new CookieServerCsrfTokenRepository());
        http
                .headers()
                .frameOptions()
                .mode(Mode.SAMEORIGIN)
                .and()
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/dlogin",
                                "/api/v1/auth",
                                "/login/oauth2/code/aptible",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/swagger-config",
                                "/v3/api-docs",
                                "/actuator/health")
                        .permitAll()
                        .anyExchange().authenticated()
                ).exceptionHandling(exceptionHandlingSpec -> {
                    exceptionHandlingSpec.authenticationEntryPoint(customAuthenticationEntryPoint);
                })
                //TODO enable csrf/cors
                //TODO stateless/statefull auth
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .addFilterAt(new JwtAuthenticationFilter(jwtService), SecurityWebFiltersOrder.CORS)
                .oauth2Login(oAuth2LoginSpec -> {
                    oAuth2LoginSpec.authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("/me"));
                    oAuth2LoginSpec.clientRegistrationRepository(clientRegistrationRepository);
                    oAuth2LoginSpec.authenticationFailureHandler(new RedirectServerAuthenticationFailureHandler(
                            "/dlogin?error"
                    ));
                })
                .logout(logoutSpec -> logoutSpec.logoutUrl("/dlogout").logoutSuccessHandler(new ServerLogoutSuccessHandler() {
                    @Override
                    public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
                        ServerHttpResponse response = exchange.getExchange().getResponse();
                        response.setStatusCode(HttpStatus.FOUND);
                        response.getHeaders().setLocation(URI.create("/dlogin"));
                        response.getCookies().remove("JSESSIONID");
                        response.getCookies().remove("SESSION");
                        return exchange.getExchange().getSession()
                                .flatMap(WebSession::invalidate);
                    }
                }));
        return http.build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
//        TODO not wildcard for deployed envs
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("*"); // Allow all origins, change to specific origin if needed
        corsConfig.addAllowedMethod("*"); // Allow all HTTP methods (GET, POST, etc.)
        corsConfig.addAllowedHeader("*"); // Allow all headers

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // Apply CORS configuration to all paths

        return new CorsWebFilter(source);
    }

    public Mono<ServerWebExchangeMatcher.MatchResult> matches(ServerWebExchange exchange, String host) {
        ServerHttpRequest request = exchange.getRequest();
        String requestHost = request.getURI().getHost();
        log.debug("request-host: {}, host: {}", requestHost, host);
        if (host.equalsIgnoreCase(requestHost)) {
            return ServerWebExchangeMatcher.MatchResult.match();
        } else {
            return ServerWebExchangeMatcher.MatchResult.notMatch();
        }
    }
}
