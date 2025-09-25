package com.trego.dao.impl;

import com.trego.dao.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByOrderId(Long orderId);
    List<Attachment> findByOrderItemId(Long orderItemId);
    List<Attachment> findByUserId(Long userId);
}