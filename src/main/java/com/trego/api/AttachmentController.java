package com.trego.api;

import com.trego.dto.AttachmentDTO;
import com.trego.service.IAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    @Autowired
    private IAttachmentService attachmentService;

    /**
     * Upload a prescription or attachment
     * @param file the file to upload
     * @param orderId optional order ID
     * @param orderItemId optional order item ID
     * @param userId optional user ID
     * @param medicineId optional medicine ID
     * @param description optional description
     * @return the uploaded attachment details
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "orderItemId", required = false) Long orderItemId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "medicineId", required = false) Long medicineId,
            @RequestParam(value = "description", required = false) String description) {
        try {
            // Validate that at least one of orderId, orderItemId, userId, or medicineId is provided
            if (orderId == null && orderItemId == null && userId == null && medicineId == null) {
                return ResponseEntity.badRequest().body("At least one of orderId, orderItemId, userId, or medicineId must be provided");
            }
            
            // Validate that file is not empty
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is required and cannot be empty");
            }
            
            AttachmentDTO attachment = attachmentService.uploadAttachment(file, orderId, orderItemId, userId, medicineId, description);
            return ResponseEntity.ok(attachment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading file: " + e.getMessage());
        }
    }

    /**
     * Get all attachments for an order
     * @param orderId the order ID
     * @return list of attachments
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<AttachmentDTO>> getAttachmentsByOrderId(@PathVariable Long orderId) {
        List<AttachmentDTO> attachments = attachmentService.getAttachmentsByOrderId(orderId);
        return ResponseEntity.ok(attachments);
    }

    /**
     * Get all attachments for an order item
     * @param orderItemId the order item ID
     * @return list of attachments
     */
    @GetMapping("/order-item/{orderItemId}")
    public ResponseEntity<List<AttachmentDTO>> getAttachmentsByOrderItemId(@PathVariable Long orderItemId) {
        List<AttachmentDTO> attachments = attachmentService.getAttachmentsByOrderItemId(orderItemId);
        return ResponseEntity.ok(attachments);
    }

    /**
     * Get all attachments for a user
     * @param userId the user ID
     * @return list of attachments
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AttachmentDTO>> getAttachmentsByUserId(@PathVariable Long userId) {
        List<AttachmentDTO> attachments = attachmentService.getAttachmentsByUserId(userId);
        return ResponseEntity.ok(attachments);
    }
    
    /**
     * Get all attachments for a medicine
     * @param medicineId the medicine ID
     * @return list of attachments
     */
    @GetMapping("/medicine/{medicineId}")
    public ResponseEntity<List<AttachmentDTO>> getAttachmentsByMedicineId(@PathVariable Long medicineId) {
        List<AttachmentDTO> attachments = attachmentService.getAttachmentsByMedicineId(medicineId);
        return ResponseEntity.ok(attachments);
    }

    /**
     * Get an attachment by ID
     * @param id the attachment ID
     * @return the attachment
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAttachmentById(@PathVariable Long id) {
        try {
            AttachmentDTO attachment = attachmentService.getAttachmentById(id);
            if (attachment != null) {
                return ResponseEntity.ok(attachment);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving attachment: " + e.getMessage());
        }
    }

    /**
     * Delete an attachment
     * @param id the attachment ID
     * @return success or failure response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long id) {
        try {
            attachmentService.deleteAttachment(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting attachment: " + e.getMessage());
        }
    }
}