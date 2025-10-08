package com.trego.dto;

import lombok.Data;

@Data
public class BucketItemDTO {
    private Long medicineId;
    private String medicineName;
    private Long vendorId;
    private String vendorName;
    private double price; // Price per unit
    private double discount;
    private int availableQuantity; // Available quantity from vendor
    private int requestedQuantity; // Quantity requested by user
    private double totalPrice; // Total price (price per unit * requested quantity)
}