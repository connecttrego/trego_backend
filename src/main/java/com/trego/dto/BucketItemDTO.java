package com.trego.dto;

import lombok.Data;

@Data
public class BucketItemDTO {
    private Long medicineId;
    private String medicineName;
    private Long vendorId;
    private String vendorName;
    private double price;
    private double discount;
    private int quantity;
}