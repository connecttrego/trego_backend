package com.trego.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class BucketDTO {
    private Long id;
    private String name;
    private List<BucketItemDTO> availableItems; // Available medicines
    private List<UnavailableMedicineDTO> unavailableItems; // Unavailable medicines with substitutes
    private double totalPrice;
    private double totalDiscount; // Total discount across all items in the bucket
    private Long vendorId; // If all items are from the same vendor
    private String vendorName; // If all items are from the same vendor
}