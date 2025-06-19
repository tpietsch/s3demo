package com.aptible;

import com.aptible.config.AppProperties;
import com.aptible.util.S3Factory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
public class S3Configuration {
    private final AppProperties appProperties;

    @Bean
    public AwsCredentials getAwsCredentials() {
        return AwsBasicCredentials.create(
                appProperties.getAwsAccessKey(),
                appProperties.getAwsSecretKey()
        );
    }

    @Bean
    public S3Client getS3Client(AwsCredentials credentials) {
        return S3Factory.getS3Client(credentials, Region.US_EAST_1);
    }

    @Bean
    public S3AsyncClient getS3AsyncClient(AwsCredentials credentials) {
        return S3Factory.getS3AsyncClient(credentials, Region.US_EAST_1);
    }
}
