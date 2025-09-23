package com.trego.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal tax;
    private BigDecimal totalPrice;
}