package com.trego.dao.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "prescription_records")
public class PrescriptionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    @Column(name = "user_id")
    private Long userId;



    @Column(name = "prescription_url")
    private String prescriptionUrl;

    @Column(name = "medicine_ids", columnDefinition = "TEXT")
    private String medicineIds;
}
