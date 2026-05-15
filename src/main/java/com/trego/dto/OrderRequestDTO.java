package com.trego.dto;

import lombok.Data;

@Data
public class OrderRequestDTO {


    private long userId;
    private long addressId;
    private long preOrderId;
    private Integer selectedVendorId; // Added to support vendor selection for regular orders
}