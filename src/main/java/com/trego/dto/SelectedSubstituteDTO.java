package com.trego.dto;

import lombok.Data;

@Data
public class SelectedSubstituteDTO {
    private Long originalMedicineId;
    private Long substituteMedicineId;
    private String substituteMedicineName;
    private int quantity;
    private double unitPrice;
    private double discount;
    private double totalPrice;
    private String medicineImage;
    private String medicineStrip;
}