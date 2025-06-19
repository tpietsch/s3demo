package com.aptible.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("classpath:app-config.properties")
@ConfigurationProperties("app")
public class AppProperties {
    private Boolean useS3;
    private String awsAccessKey;
    private String awsSecretKey;
    private String ssoHost;
}
