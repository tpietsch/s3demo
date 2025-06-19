package com.aptible.database;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.concurrent.TimeUnit;

/**
 * Example of what a customer<>s3 cache might look like
 * */
public class CustomerS3Repository {
    public static Cache<String, S3Client> clientCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();
    public static  Cache<String, S3AsyncClient> asyncClientCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();
    public static void loadCache() {
        for(int i = 0; i< 10; i++) {
            //TODO lookup customer key/token add to cache
            clientCache.put(String.valueOf(i), null);
            asyncClientCache.put(String.valueOf(i), null);
        }
    }
}
