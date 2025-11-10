package com.trego.dto;

import jakarta.persistence.Column;
import lombok.Data;

import java.util.List;

@Data
public class PreOrderDTO {

    private long userId;
    private long orderId;
    private long addressId;
    private String mobileNo;
    private double totalCartValue;
    private double amountToPay;
    private double discount;
    private List<CartDTO> carts;

    @Column(name = "selected_vendor_id")
    private Long selectedVendorId;

}