package com.trego.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface IS3Service {
    String uploadFile(MultipartFile file, String fileName) throws IOException;
    void deleteFile(String fileName);
}