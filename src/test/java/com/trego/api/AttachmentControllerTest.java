package com.trego.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trego.dto.AttachmentDTO;
import com.trego.service.IAttachmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttachmentController.class)
public class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAttachmentService attachmentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testUploadAttachment() throws Exception {
        // Create mock attachment DTO
        AttachmentDTO attachmentDTO = new AttachmentDTO();
        attachmentDTO.setId(1L);
        attachmentDTO.setFileName("prescription.pdf");
        attachmentDTO.setFileType("application/pdf");
        attachmentDTO.setFileUrl("https://your-s3-bucket.s3.region.amazonaws.com/uuid_prescription.pdf");
        attachmentDTO.setOrderId(1L);
        attachmentDTO.setUserId(1L);
        attachmentDTO.setDescription("Prescription for order");
        attachmentDTO.setCreatedAt(LocalDateTime.now());

        // Create mock file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "prescription.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        // Mock the service method
        when(attachmentService.uploadAttachment(any(), anyLong(), anyLong(), anyLong(), anyString()))
                .thenReturn(attachmentDTO);

        // Perform the POST request and verify the response
        mockMvc.perform(multipart("/api/attachments/upload")
                .file(file)
                .param("orderId", "1")
                .param("userId", "1")
                .param("description", "Prescription for order"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fileName").value("prescription.pdf"))
                .andExpect(jsonPath("$.fileType").value("application/pdf"));
    }

    @Test
    public void testGetAttachmentsByOrderId() throws Exception {
        // Create mock attachment DTOs
        AttachmentDTO attachment1 = new AttachmentDTO();
        attachment1.setId(1L);
        attachment1.setFileName("prescription1.pdf");
        attachment1.setFileType("application/pdf");
        attachment1.setFileUrl("https://your-s3-bucket.s3.region.amazonaws.com/uuid_prescription1.pdf");
        attachment1.setOrderId(1L);
        attachment1.setCreatedAt(LocalDateTime.now());

        AttachmentDTO attachment2 = new AttachmentDTO();
        attachment2.setId(2L);
        attachment2.setFileName("prescription2.pdf");
        attachment2.setFileType("application/pdf");
        attachment2.setFileUrl("https://your-s3-bucket.s3.region.amazonaws.com/uuid_prescription2.pdf");
        attachment2.setOrderId(1L);
        attachment2.setCreatedAt(LocalDateTime.now());

        List<AttachmentDTO> attachments = Arrays.asList(attachment1, attachment2);

        // Mock the service method
        when(attachmentService.getAttachmentsByOrderId(anyLong())).thenReturn(attachments);

        // Perform the GET request and verify the response
        mockMvc.perform(get("/api/attachments/order/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    public void testGetAttachmentById() throws Exception {
        // Create mock attachment DTO
        AttachmentDTO attachmentDTO = new AttachmentDTO();
        attachmentDTO.setId(1L);
        attachmentDTO.setFileName("prescription.pdf");
        attachmentDTO.setFileType("application/pdf");
        attachmentDTO.setFileUrl("https://your-s3-bucket.s3.region.amazonaws.com/uuid_prescription.pdf");
        attachmentDTO.setOrderId(1L);
        attachmentDTO.setCreatedAt(LocalDateTime.now());

        // Mock the service method
        when(attachmentService.getAttachmentById(anyLong())).thenReturn(attachmentDTO);

        // Perform the GET request and verify the response
        mockMvc.perform(get("/api/attachments/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fileName").value("prescription.pdf"));
    }

    @Test
    public void testGetAttachmentByIdNotFound() throws Exception {
        // Mock the service method to return null
        when(attachmentService.getAttachmentById(anyLong())).thenReturn(null);

        // Perform the GET request and verify the response
        mockMvc.perform(get("/api/attachments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUploadAttachmentValidationFailure() throws Exception {
        // Create mock file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "prescription.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        // Perform the POST request without required parameters and verify the response
        mockMvc.perform(multipart("/api/attachments/upload")
                .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUploadAttachmentEmptyFile() throws Exception {
        // Create empty mock file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "prescription.pdf",
                "application/pdf",
                new byte[0]
        );

        // Perform the POST request with empty file and verify the response
        mockMvc.perform(multipart("/api/attachments/upload")
                .file(file)
                .param("orderId", "1"))
                .andExpect(status().isBadRequest());
    }
}