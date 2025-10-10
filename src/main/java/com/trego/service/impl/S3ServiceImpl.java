package com.trego.service.impl;

import com.trego.service.IS3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3ServiceImpl implements IS3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Override
    public String uploadFile(MultipartFile file, String fileName) throws IOException {
        try {
            // Generate a unique file name to avoid conflicts
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            
            // Upload file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueFileName)
                    .contentType(file.getContentType())
                    .build();
                    
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            
            // Return the S3 URL
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, uniqueFileName);
        } catch (S3Exception e) {
            throw new IOException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
                    
            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            // Log the error but don't throw exception as this is a delete operation
            // In a production environment, you might want to log this properly
            System.err.println("Failed to delete file from S3: " + e.getMessage());
        }
    }
}