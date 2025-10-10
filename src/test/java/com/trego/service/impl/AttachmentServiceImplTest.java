package com.trego.service.impl;

import com.trego.dao.entity.Attachment;
import com.trego.dao.impl.AttachmentRepository;
import com.trego.dto.AttachmentDTO;
import com.trego.service.IS3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AttachmentServiceImplTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private IS3Service s3Service;

    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAttachmentsByOrderId() {
        // Create mock attachments
        Attachment attachment1 = new Attachment();
        attachment1.setId(1L);
        attachment1.setFileName("prescription1.pdf");
        attachment1.setFileType("application/pdf");
        attachment1.setOrderId(1L);
        attachment1.setCreatedAt(LocalDateTime.now());

        Attachment attachment2 = new Attachment();
        attachment2.setId(2L);
        attachment2.setFileName("prescription2.pdf");
        attachment2.setFileType("application/pdf");
        attachment2.setOrderId(1L);
        attachment2.setCreatedAt(LocalDateTime.now());

        List<Attachment> attachments = Arrays.asList(attachment1, attachment2);

        // Mock the repository method
        when(attachmentRepository.findByOrderId(anyLong())).thenReturn(attachments);

        // Call the service method
        List<AttachmentDTO> result = attachmentService.getAttachmentsByOrderId(1L);

        // Verify the results
        assertEquals(2, result.size());
        assertEquals("prescription1.pdf", result.get(0).getFileName());
        assertEquals("prescription2.pdf", result.get(1).getFileName());
    }

    @Test
    public void testGetAttachmentsByOrderItemId() {
        // Create mock attachments
        Attachment attachment1 = new Attachment();
        attachment1.setId(1L);
        attachment1.setFileName("prescription1.pdf");
        attachment1.setFileType("application/pdf");
        attachment1.setOrderItemId(1L);
        attachment1.setCreatedAt(LocalDateTime.now());

        List<Attachment> attachments = Arrays.asList(attachment1);

        // Mock the repository method
        when(attachmentRepository.findByOrderItemId(anyLong())).thenReturn(attachments);

        // Call the service method
        List<AttachmentDTO> result = attachmentService.getAttachmentsByOrderItemId(1L);

        // Verify the results
        assertEquals(1, result.size());
        assertEquals("prescription1.pdf", result.get(0).getFileName());
    }

    @Test
    public void testGetAttachmentsByUserId() {
        // Create mock attachments
        Attachment attachment1 = new Attachment();
        attachment1.setId(1L);
        attachment1.setFileName("prescription1.pdf");
        attachment1.setFileType("application/pdf");
        attachment1.setUserId(1L);
        attachment1.setCreatedAt(LocalDateTime.now());

        List<Attachment> attachments = Arrays.asList(attachment1);

        // Mock the repository method
        when(attachmentRepository.findByUserId(anyLong())).thenReturn(attachments);

        // Call the service method
        List<AttachmentDTO> result = attachmentService.getAttachmentsByUserId(1L);

        // Verify the results
        assertEquals(1, result.size());
        assertEquals("prescription1.pdf", result.get(0).getFileName());
    }

    @Test
    public void testGetAttachmentById() {
        // Create mock attachment
        Attachment attachment = new Attachment();
        attachment.setId(1L);
        attachment.setFileName("prescription.pdf");
        attachment.setFileType("application/pdf");
        attachment.setCreatedAt(LocalDateTime.now());

        // Mock the repository method
        when(attachmentRepository.findById(anyLong())).thenReturn(Optional.of(attachment));

        // Call the service method
        AttachmentDTO result = attachmentService.getAttachmentById(1L);

        // Verify the results
        assertNotNull(result);
        assertEquals("prescription.pdf", result.getFileName());
    }

    @Test
    public void testGetAttachmentByIdNotFound() {
        // Mock the repository method to return empty
        when(attachmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Call the service method
        AttachmentDTO result = attachmentService.getAttachmentById(1L);

        // Verify the results
        assertNull(result);
    }
}