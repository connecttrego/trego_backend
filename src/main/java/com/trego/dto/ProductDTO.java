package com.trego.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private Double price;
    private Double tax;
    private Double totalPrice;
    private String description;
    private String image;
    private Integer stock;
}