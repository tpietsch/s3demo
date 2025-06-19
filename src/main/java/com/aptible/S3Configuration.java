package com.aptible;

import com.aptible.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
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
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(credentials.accessKeyId(), credentials.secretAccessKey());

        SdkHttpClient apacheHttpClient = ApacheHttpClient.builder().build();

        // Change the region as needed
        return S3Client.builder()
                .region(Region.US_EAST_1) // Change the region as needed
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .httpClient(apacheHttpClient)
                .build();
    }

    @Bean
    public S3AsyncClient getS3AsyncClient(AwsCredentials credentials) {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(credentials.accessKeyId(), credentials.secretAccessKey());
        // Change the region as needed
        return S3AsyncClient.builder()
                .region(Region.US_EAST_1) // Change the region as needed
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }
}
