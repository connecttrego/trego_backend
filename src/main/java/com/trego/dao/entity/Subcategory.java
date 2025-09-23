package com.trego.dao.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity(name = "subcategories")
public class Subcategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Column(name = "category_id")
    private Long categoryId;
    
    private String description;
    
    private String image;
}