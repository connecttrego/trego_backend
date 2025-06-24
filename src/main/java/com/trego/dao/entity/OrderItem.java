package com.trego.dao.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
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

    @ManyToOne
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

/*    @Column(nullable = true)
    private Integer medicineId;*/

    @Column(nullable = true)
    private Integer qty;

    @Column(nullable = true)
    private double mrp;

    @Column(nullable = true)
    private double sellingPrice;

    @Column(nullable = true)
    private double amount;

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