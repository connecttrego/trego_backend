package com.trego.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SubstituteDetailDTO {
    private Long id;
    private String name;
    private String photo1;
    private String manufacturer;
    private String vendorName;
    private String vendorLogo;
    private BigDecimal mrp;
    private BigDecimal bestPrice;
    private BigDecimal discount;
}