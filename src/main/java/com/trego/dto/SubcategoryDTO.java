package com.trego.dto;

import lombok.Data;

@Data
public class SubcategoryDTO {
    private Long id;
    private String name;
    private Long categoryId;
}