package com.trego.dto;

import lombok.Data;
import com.trego.dto.view.SubstituteDetailView;
import java.util.List;

@Data
public class UnavailableMedicineDTO {
    private Long medicineId;
    private String medicineName;
    private String medicineImage;
    private String medicineStrip;
    private int requestedQuantity;
    private List<SubstituteDetailView> substitutes;
}