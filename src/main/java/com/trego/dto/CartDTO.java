package com.trego.dto;


import lombok.Data;

import java.util.List;

@Data
public class CartDTO {
    private Integer vendorId;
    private List<MedicineDTO> medicine;
}
