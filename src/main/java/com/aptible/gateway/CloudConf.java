package com.aptible.gateway;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;


@Slf4j
//@Configuration
@RequiredArgsConstructor
public class CloudConf implements WebFluxConfigurer {
    private final ObjectMapper objectMapper;
    public final static ObjectMapper objectMapperPretty = new ObjectMapper();

    /*
     * Use this header if you need to debug a specific route
     * Debug logging is required to enable this but debug log volume
     * is so high it may bring down your service if you have it on too many
     * seeing as live traffic is flowing through here
     */
    private final LoggingFilter loggingFilter = new LoggingFilter();

    private void debugJson(String type, String rawInboud) {
        log.debug(type + " Inbound :" + "\n{}", rawInboud);
    }

    private void debugJson(String type, Object mappedInbound, Object mappedOutbound) throws JsonProcessingException {
        log.debug(type + " Inbound :" + "\n{}", objectMapperPretty.writeValueAsString(mappedInbound));
        log.debug(type + " Outbound :" + "\n{}", objectMapperPretty.writeValueAsString(mappedOutbound));
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        objectMapperPretty.enable(SerializationFeature.INDENT_OUTPUT);
        /*
         * !!!!!! WARNING !!!!!!!
         * We have two gateways deployed using this same code based - one will be public facing one is firewalled
         * Check twice on the public one - check twice, three times, ask questions.
         */
        return builder.routes().build();
//        return builder.routes()
//                .route("authentik", r ->
//                        r.host(SecConfig.AUTHENTIK_HOST).and().path("/ws/client/")
//                                .filters(f ->
//                                        //TODO - tired didnt work https://docs.goauthentik.io/docs/security/security-hardening
//                                        f.removeRequestHeader(HttpHeaders.HOST)
//                                                .addRequestHeader(HttpHeaders.UPGRADE, "websocket")
//                                                .addRequestHeader(HttpHeaders.CONNECTION, "upgrade")
//                                                .addRequestHeader(HttpHeaders.HOST, SecConfig.AUTHENTIK_HOST)
//                                                .preserveHostHeader()
//                                                .filter(outlineHeaders)
//                                )
//                                .uri(appConfigProperties.getAuthentikHost()))
//                .route("authentik", r ->
//                        r.host(SecConfig.AUTHENTIK_HOST)
//                                .filters(f ->
//                                        f.removeRequestHeader(HttpHeaders.HOST)
//                                                .addRequestHeader(HttpHeaders.CONNECTION, "keep-alive")
//                                                .addRequestHeader(HttpHeaders.HOST, SecConfig.AUTHENTIK_HOST)
//                                                .preserveHostHeader()
//                                                .filter(outlineHeaders)
//                                )
//                                .uri(appConfigProperties.getAuthentikHost()))
//                .route("llm-embeddings", r ->
//                        r.host(SecConfig.LLM_HOST).and().path("/v1/embeddings")
//                                .filters(f -> f
//                                        .filter(mapAuthHeaderToKeyParam)
//                                        .removeRequestHeader(HttpHeaders.AUTHORIZATION)
//                                        .modifyRequestBody(String.class, GeminiEmbedCall.class, (exchange, rawJson) -> {
//                                            debugJson("Embedding Request", rawJson);
//                                            OpenAIEmbedCall originalJson = extract(rawJson, OpenAIEmbedCall.class, objectMapper);
//                                            GeminiEmbedCall modifiedJson = convert(originalJson);
//                                            try {
//                                                debugJson("Embedding Request", originalJson, modifiedJson);
//                                            } catch (JsonProcessingException e) {
//                                                throw new RuntimeException(e);
//                                            }
//                                            return Mono.just(modifiedJson);
//                                        })
//                                        .addRequestHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                                        .removeRequestHeader(HttpHeaders.HOST)
//                                        .addRequestHeader(HttpHeaders.HOST, appConfigProperties.getGeminiHost().replace("https://", ""))
//                                        .rewritePath("/v1/embeddings", "/v1beta/models/text-embedding-004:embedContent")
//                                        .modifyResponseBody(String.class, OpenAiEmbedResponse.class, (exchange, rawJson) -> {
//                                                    debugJson("Embedding Response", rawJson);
//                                                    GeminiEmbedResponse originalJson = extract(rawJson, GeminiEmbedResponse.class, objectMapper);
//                                                    OpenAiEmbedResponse modifiedJson = convert(originalJson);
//                                                    try {
//                                                        debugJson("Embedding Response", originalJson, modifiedJson);
//                                                    } catch (JsonProcessingException e) {
//                                                        throw new RuntimeException(e);
//                                                    }
//                                                    return Mono.just(modifiedJson);
//                                                }
//                                        ))
//                                .uri(appConfigProperties.getGeminiHost()))
//                .route("llm-chat", r ->
//                        r.host(SecConfig.LLM_HOST).and().path("/v1/chat/completions")
//                                .filters(f -> f
//                                        .filter(mapAuthHeaderToKeyParam)
//                                        .removeRequestHeader(HttpHeaders.AUTHORIZATION)
//                                        .modifyRequestBody(String.class, GeminiChatCall.class, (exchange, rawJson) -> {
//                                            debugJson("Chat Completion Request", rawJson);
//                                            OpenAIChatCall originalJson = extract(rawJson, OpenAIChatCall.class, objectMapper);
//                                            GeminiChatCall modifiedJson = convert(originalJson);
//                                            try {
//                                                debugJson("Chat Completion Request", originalJson, modifiedJson);
//                                            } catch (JsonProcessingException e) {
//                                                throw new RuntimeException(e);
//                                            }
//                                            return Mono.just(modifiedJson);
//                                        })
//                                        .addRequestHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                                        .removeRequestHeader(HttpHeaders.HOST)
//                                        .addRequestHeader(HttpHeaders.HOST, appConfigProperties.getGeminiHost().replace("https://", ""))
//                                        .rewritePath("/v1/chat/completions", "/v1beta/models/gemini-1.5-flash:generateContent")
//                                        .modifyResponseBody(String.class, OpenAIChatResponse.class, (exchange, rawJson) -> {
//                                            debugJson("Chat Completion Response", rawJson);
//                                            GeminiChatCallResponse originalJson = extract(rawJson, GeminiChatCallResponse.class, objectMapper);
//                                            OpenAIChatResponse modifiedJson = convert(originalJson, objectMapper);
//                                            try {
//                                                debugJson("Chat Completion Response", originalJson, modifiedJson);
//                                            } catch (JsonProcessingException e) {
//                                                throw new RuntimeException(e);
//                                            }
//                                            return Mono.just(modifiedJson);
//                                        })
//                                )
//                                .uri(appConfigProperties.getGeminiHost()))
//                .build();
    }
}

