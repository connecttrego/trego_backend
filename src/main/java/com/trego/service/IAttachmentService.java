package com.trego.service;

import com.trego.dto.AttachmentDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface IAttachmentService {
    AttachmentDTO uploadAttachment(MultipartFile file, Long orderId, Long orderItemId, Long userId, String description) throws Exception;
    AttachmentDTO uploadAttachment(MultipartFile file, Long orderId, Long orderItemId, Long userId, Long medicineId, String description) throws Exception;
    List<AttachmentDTO> getAttachmentsByOrderId(Long orderId);
    List<AttachmentDTO> getAttachmentsByOrderItemId(Long orderItemId);
    List<AttachmentDTO> getAttachmentsByUserId(Long userId);
    List<AttachmentDTO> getAttachmentsByMedicineId(Long medicineId);
    AttachmentDTO getAttachmentById(Long id);
    void deleteAttachment(Long id) throws Exception;
}