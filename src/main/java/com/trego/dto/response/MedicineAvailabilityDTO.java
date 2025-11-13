package com.trego.dto.response;

import lombok.Data;

@Data
public class MedicineAvailabilityDTO {
    private Long medicineId;
    private String medicineName;
    private Double price;     // null if not available
    private Double discount;  // optional
    private boolean available; // ✅ new field
}
