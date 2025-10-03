package com.trego.service.impl;

import com.trego.dao.entity.Attachment;
import com.trego.dao.impl.AttachmentRepository;
import com.trego.dto.AttachmentDTO;
import com.trego.service.IAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AttachmentServiceImpl implements IAttachmentService {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Value("${file.upload-dir:/tmp/uploads}")
    private String uploadDir;

    @Override
    public AttachmentDTO uploadAttachment(MultipartFile file, Long orderId, Long orderItemId, Long userId, String description) throws Exception {
        // Validate input
        if (file == null) {
            throw new Exception("File is required");
        }
        
        if (file.isEmpty()) {
            throw new Exception("File cannot be empty");
        }
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new Exception("File must have a name");
        }
        
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Save file to disk
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Save attachment metadata to database
        Attachment attachment = new Attachment();
        attachment.setFileName(originalFilename);
        attachment.setFileType(file.getContentType());
        attachment.setFileUrl(filePath.toString());
        attachment.setOrderId(orderId);
        attachment.setOrderItemId(orderItemId);
        attachment.setUserId(userId);
        attachment.setDescription(description);

        Attachment savedAttachment = attachmentRepository.save(attachment);

        return convertToDTO(savedAttachment);
    }

    @Override
    public List<AttachmentDTO> getAttachmentsByOrderId(Long orderId) {
        return attachmentRepository.findByOrderId(orderId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttachmentDTO> getAttachmentsByOrderItemId(Long orderItemId) {
        return attachmentRepository.findByOrderItemId(orderItemId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttachmentDTO> getAttachmentsByUserId(Long userId) {
        return attachmentRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AttachmentDTO getAttachmentById(Long id) {
        Attachment attachment = attachmentRepository.findById(id).orElse(null);
        return attachment != null ? convertToDTO(attachment) : null;
    }

    @Override
    public void deleteAttachment(Long id) throws Exception {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new Exception("Attachment not found with id: " + id));

        // Delete file from disk
        Path filePath = Paths.get(attachment.getFileUrl());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        // Delete from database
        attachmentRepository.deleteById(id);
    }

    private AttachmentDTO convertToDTO(Attachment attachment) {
        AttachmentDTO dto = new AttachmentDTO();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());
        dto.setFileType(attachment.getFileType());
        dto.setFileUrl(attachment.getFileUrl());
        dto.setOrderId(attachment.getOrderId());
        dto.setOrderItemId(attachment.getOrderItemId());
        dto.setUserId(attachment.getUserId());
        dto.setDescription(attachment.getDescription());
        dto.setCreatedAt(attachment.getCreatedAt());
        dto.setUpdatedAt(attachment.getUpdatedAt());
        return dto;
    }
}