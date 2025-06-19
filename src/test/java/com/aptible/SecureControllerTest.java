package com.aptible;

import com.aptible.api.FileController;
import com.aptible.config.AppProperties;
import com.aptible.controllers.LoginPageController;
import com.aptible.database.FileEntity;
import com.aptible.database.FileRepository;
import com.aptible.security.CustomAuthenticationEntryPoint;
import com.aptible.security.JwtService;
import com.aptible.security.OauthConfig;
import com.aptible.security.SecConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
//TODO override app prop config stuff for test use cases...
//TODO better ways and more granular ways to write tests - split unit/spring/integration/...
class SecureControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    //ignoreing for post body
//    @Test
//    @WithMockUser(username = "trever", roles = {"ORG_1", "CAN_UPLOAD"})
//    void testAuthenticatedAccess() {
//        webTestClient.post()
//                .uri("/api/v1/org/1/file")
//                .exchange()
//                .expectStatus().isOk();
//    }
//
//    @Test
//    @WithMockUser(username = "trever", roles = {"ORG_2", "CAN_UPLOAD"})
//    void testAuthenticatedAccessBadOrg() {
//        webTestClient.post()
//                .uri("/api/v1/org/1/file")
//                .exchange()
//                .expectStatus().isUnauthorized();
//    }

    @Test
    @WithMockUser(username = "trever", roles = {"ORG_1", "CAN_READ_FILES"})
    void testPerm() {
        webTestClient.get()
                .uri("/api/v1/org/1/file")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "trever", roles = {"ORG_1", "CAN_READ_FILES"})
    void testPermDenied() {
        //changing org in URL
        webTestClient.get()
                .uri("/api/v1/org/2/file")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isUnauthorized();
    }

//    TODO ... combinations of perms testing
    @Test
    void testUnauthorizedAccess() {
        webTestClient.get()
                .uri("/api/v1/org/1/file")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}