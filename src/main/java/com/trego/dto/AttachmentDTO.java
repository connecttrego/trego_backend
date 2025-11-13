package com.trego.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttachmentDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long orderId;
    private Long orderItemId;
    private Long userId;
    private Long medicineId;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}