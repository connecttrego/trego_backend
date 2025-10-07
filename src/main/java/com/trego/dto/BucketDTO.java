package com.trego.dto;

import lombok.Data;
import java.util.List;

@Data
public class BucketDTO {
    private Long id;
    private String name;
    private List<BucketItemDTO> items;
    private double totalPrice;
    private Long vendorId; // If all items are from the same vendor
    private String vendorName; // If all items are from the same vendor
}