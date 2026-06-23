package com.trego.dto;

import lombok.Data;

@Data
public class SelectedSubstituteDTO {
    private Integer originalMedicineId;
    private Integer substituteMedicineId;
    private String substituteMedicineName;
    private int quantity;
    private double unitPrice;
    private double discount;
    private double totalPrice;
    private String medicineImage;
    private String medicineStrip;
}