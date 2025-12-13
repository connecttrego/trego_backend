package com.trego.service.impl;

import com.trego.dao.entity.PrescriptionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionRepository extends JpaRepository<PrescriptionRecord, Long> {
}
