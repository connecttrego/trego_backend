package com.trego.dao.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private String description;
    
    private BigDecimal price;
    
    private BigDecimal tax;
    
    @Column(name = "subcategory_id")
    private Long subcategoryId;
    
    private String image;
    
    private Integer stock;
    
    @Transient
    public BigDecimal getTotalPrice() {
        if (price != null && tax != null) {
            return price.add(tax);
        }
        return BigDecimal.ZERO;
    }
}