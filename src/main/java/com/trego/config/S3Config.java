package com.trego.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${aws.s3.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.s3.secret-access-key:}")
    private String secretAccessKey;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Bean
    public S3Client s3Client() {
        // If no access key is provided, return null or a mock client
        if (accessKeyId == null || accessKeyId.isEmpty() || "YOUR_ACCESS_KEY_ID".equals(accessKeyId)) {
            // Return a default client without credentials for local testing
            return S3Client.builder()
                    .region(Region.of(region))
                    .build();
        }
        
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }
}