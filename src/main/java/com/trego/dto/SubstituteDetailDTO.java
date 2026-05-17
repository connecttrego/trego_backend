package com.trego.dto;

import lombok.Data;

@Data
public class SubstituteDetailDTO {
    private Long id;
    private String name;
    private String photo1;
    private String manufacturer;
    private String vendorName;
    private String vendorLogo;
    private Double mrp;
    private Double bestPrice;
    private Double discount;
}