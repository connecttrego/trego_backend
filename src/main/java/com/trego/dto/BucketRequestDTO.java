package com.trego.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class BucketRequestDTO {
    // Changed from List<Long> to Map<Long, Integer> to include medicine ID and quantity
    private Map<Long, Integer> medicineQuantities;
    
    public Map<Long, Integer> getMedicineQuantities() {
        return medicineQuantities;
    }
}