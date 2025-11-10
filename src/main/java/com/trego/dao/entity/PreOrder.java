package com.trego.dao.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity(name = "preorders")
@Table(name = "pre_orders")
public class PreOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String vendorPayload;

    @Column(nullable = true, name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(nullable = true, name = "total_pay_amount")
    private double totalPayAmount;

    @Column(name = "payment_status", nullable = true)
    private String paymentStatus;

    @Column(name = "order_status", nullable = true)
    private String orderStatus;

    @Column(name = "mobile_no", nullable = true)
    private String mobileNo;


    @Column(name = "address_id", nullable = true)
    private long addressId;

    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    @Column(name = "modified_by", nullable = true)
    private String modifiedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "modified_at", nullable = true)
    private LocalDateTime modifiedAt;
    
    @Column(name = "selected_vendor_id", nullable = true)
    private Long selectedVendorId;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.modifiedAt = LocalDateTime.now(); // Update automatically before every save
    }

    @OneToMany(mappedBy = "preOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    // Getters and Setters
}