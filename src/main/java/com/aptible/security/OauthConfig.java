package com.aptible.security;

import com.aptible.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

//import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
//import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
//https://docs.spring.io/spring-security/site/docs/5.2.0-gh2567/reference/html/oauth2.html
@Configuration
@RequiredArgsConstructor
public class OauthConfig {
    private final AppProperties appProperties;
    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ReactiveOAuth2AuthorizedClientService authorizedClientService) {

        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider =
                ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    @Bean
    public ReactiveClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("aptible")
                .clientId("BzSKi74tY81hs4Uux4uyjs0cyJWhMpUvD7DSlzcm")
                .clientSecret("sqYIJMUx2HeI6sSROrrf9Vh2P5Hcpp3T0aGPatjEpqXHCgcXwQPknNglA3jfaJ6aQk5UjY50TfCcnUNJd2hHiZUxQ0WDwKcf3DkiQ9npkCeQln6GN7XQH030jbiJ8PJB")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("email", "openid", "profile")
                .registrationId("aptible")
                .jwkSetUri("http://" + appProperties.getSsoHost() + "/application/o/aptible/jwks/")
                .authorizationUri("http://localhost:9000/application/o/authorize/")
                .tokenUri("http://" + appProperties.getSsoHost() + "/application/o/token/")
                .userInfoUri("http://" + appProperties.getSsoHost() + "/application/o/userinfo/")
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .clientName("aptible")
                .userNameAttributeName("email")
                .build();
        return new InMemoryReactiveClientRegistrationRepository(registration);
    }
}

