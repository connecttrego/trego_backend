package com.trego.dao.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    @Column(name = "vendor_medicine_id")
    private Long vendorMedicineId;

    @Column(nullable = true)
    private Integer qty;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(nullable = true, columnDefinition = "DOUBLE")
    private Double mrp;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(nullable = true, columnDefinition = "DOUBLE")
    private Double sellingPrice;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(nullable = true, columnDefinition = "DOUBLE")
    private Double amount;

    @Column(nullable = true)
    private String thumbnail;

    @Column(nullable = true)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String orderStatus;

    // Getters and Setters
}