package com.trego.service.impl;

import com.trego.dao.entity.Attachment;
import com.trego.dao.impl.AttachmentRepository;
import com.trego.dto.AttachmentDTO;
import com.trego.service.IAttachmentService;
import com.trego.service.IS3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttachmentServiceImpl implements IAttachmentService {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired(required = false)
    private IS3Service s3Service;

    @Override
    public AttachmentDTO uploadAttachment(MultipartFile file, Long orderId, Long orderItemId, Long userId, String description) throws Exception {
        return uploadAttachment(file, orderId, orderItemId, userId, null, description);
    }
    
    @Override
    public AttachmentDTO uploadAttachment(MultipartFile file, Long orderId, Long orderItemId, Long userId, Long medicineId, String description) throws Exception {
        // Validate input
        if (file == null) {
            throw new Exception("File is required");
        }
        
        if (file.isEmpty()) {
            throw new Exception("File cannot be empty");
        }

        // Generate original filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new Exception("File must have a name");
        }

        // Upload file to S3 or local storage
        String fileUrl = originalFilename; // Default to filename
        if (s3Service != null) {
            fileUrl = s3Service.uploadFile(file, originalFilename);
        }

        // Save attachment metadata to database
        Attachment attachment = new Attachment();
        attachment.setFileName(originalFilename);
        attachment.setFileType(file.getContentType());
        attachment.setFileUrl(fileUrl);
        attachment.setOrderId(orderId);
        attachment.setOrderItemId(orderItemId);
        attachment.setUserId(userId);
        attachment.setMedicineId(medicineId);
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
    public List<AttachmentDTO> getAttachmentsByMedicineId(Long medicineId) {
        return attachmentRepository.findByMedicineId(medicineId)
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

        // Delete file from S3 or local storage if S3 service is available
        if (s3Service != null) {
            String fileUrl = attachment.getFileUrl();
            // Extract the file name from the S3 URL
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            s3Service.deleteFile(fileName);
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
        dto.setMedicineId(attachment.getMedicineId());
        dto.setDescription(attachment.getDescription());
        dto.setCreatedAt(attachment.getCreatedAt());
        dto.setUpdatedAt(attachment.getUpdatedAt());
        return dto;
    }
}