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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class S3ServiceImpl implements IS3Service {

    @Autowired(required = false)
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name:trackome-bucket}")
    private String bucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public String uploadFile(MultipartFile file, String fileName) throws IOException {
        // If S3 client is not available, fall back to local file storage
        if (s3Client == null) {
            return saveFileLocally(file, fileName);
        }
        
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
            // If S3 upload fails, fall back to local storage
            System.err.println("Failed to upload to S3, falling back to local storage: " + e.getMessage());
            return saveFileLocally(file, fileName);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        if (s3Client == null) {
            // Delete local file
            deleteLocalFile(fileName);
            return;
        }
        
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
                    
            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            // If S3 delete fails, try to delete local file
            System.err.println("Failed to delete from S3: " + e.getMessage());
            deleteLocalFile(fileName);
        }
    }
    
    private String saveFileLocally(MultipartFile file, String fileName) throws IOException {
        // Ensure upload directory exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate a unique file name to avoid conflicts
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
        Path filePath = uploadPath.resolve(uniqueFileName);
        
        // Save file locally
        Files.write(filePath, file.getBytes());
        
        // Return local file path
        return filePath.toString();
    }
    
    private void deleteLocalFile(String fileName) {
        try {
            Path filePath = Paths.get(fileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to delete local file: " + e.getMessage());
        }
    }
}