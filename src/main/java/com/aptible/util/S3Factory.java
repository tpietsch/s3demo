package com.aptible.util;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

public class S3Factory {
    public static S3Client getS3Client(AwsCredentials credentials, Region region) {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(credentials.accessKeyId(), credentials.secretAccessKey());

        SdkHttpClient apacheHttpClient = ApacheHttpClient.builder().build();

        // Change the region as needed
        return S3Client.builder()
                .region(region) // Change the region as needed
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .httpClient(apacheHttpClient)
                .build();
    }

    public static S3AsyncClient getS3AsyncClient(AwsCredentials credentials, Region region) {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(credentials.accessKeyId(), credentials.secretAccessKey());
        // Change the region as needed
        return S3AsyncClient.builder()
                .region(region) // Change the region as needed
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }
}
