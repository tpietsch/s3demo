package com.aptible;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
//TODO override app prop config stuff for test use cases...
//TODO better ways and more granular ways to write tests - split unit/spring/integration/...
class SecureControllerTest {

    @Autowired
    private WebTestClient webTestClient;

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