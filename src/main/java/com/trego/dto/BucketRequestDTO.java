package com.trego.dto;

import lombok.Data;

import java.util.Map;

@Data
public class BucketRequestDTO {
    // Changed from List<Integer> to Map<Integer, Integer> to include medicine ID and quantity
    private Map<Integer, Integer> medicineQuantities;
    
    public Map<Integer, Integer> getMedicineQuantities() {
        return medicineQuantities;
    }
}