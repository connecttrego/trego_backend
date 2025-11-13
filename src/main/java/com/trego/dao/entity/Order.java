package com.trego.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@Entity(name = "orders")
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @Column(nullable = false)
    private double totalAmount;

    @Column(nullable = false)
    private double discount;

    @Column(nullable = false)
    private String address;

    @Column(nullable = true)
    private String pincode;

    @Column(nullable = true)
    private String lanmark;

    @Column(nullable = true)
    private String city;

    @Column(nullable = true)
    private String name;

    @Column(nullable = true)
    private long mobile;

    @Column(nullable = false)
    private String email;

    @Column(nullable = true)
    private String paymentStatus;

    @Column
    private String paymentMethod;

    @Column(nullable = true)
    private String orderStatus;


    @Column(nullable = true)
    private String cancelReason;

    @Column(nullable = true)
    private String cancelReasonId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


    @Column(nullable = true)
    private LocalDateTime updatedAt;


    @ManyToOne
    @JoinColumn(name = "pre_order_id")
    private PreOrder preOrder;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();


    // Getters and Setters
}