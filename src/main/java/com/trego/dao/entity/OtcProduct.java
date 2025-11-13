package com.trego.dao.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity(name = "otc_products")
public class OtcProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private String category;
    
    private String subCategory;
    
    private String breadcrum;
    
    private String description;
    
    private String manufacturers;
    
    private String packaging;
    
    private String packInfo;
    
    private BigDecimal price;
    
    private BigDecimal bestPrice;
    
    private BigDecimal discountPercent;
    
    private String prescriptionRequired;
    
    private String primaryUse;
    
    private String saltSynonmys;
    
    private String storage;
    
    private String introduction;
    
    private String useOf;
    
    private String benefits;
    
    private String sideEffect;
    
    private String howToUse;
    
    private String howWorks;
    
    private String safetyAdvise;
    
    private String ifMiss;
    
    private String ingredients;
    
    private String alternateBrand;
    
    private String manufacturerAddress;
    
    private String forSale;
    
    private String countryOfOrigin;
    
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