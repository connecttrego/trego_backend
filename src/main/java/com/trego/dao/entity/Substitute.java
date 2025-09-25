package com.trego.dao.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity(name = "substitutes")
public class Substitute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "medicine_id")
    private Long medicineId;
    
    @Column(name = "substitute_medicine_id")
    private Long substituteMedicineId;
    
    private String name;
    private String manufacturers;
    private String saltComposition;
    private String medicineType;
    private Integer stock;
    private String introduction;
    private String benefits;
    private String description;
    private String howToUse;
    private String safetyAdvise;
    private String ifMiss;
    private String packaging;
    private String packagingType;
    private BigDecimal mrp;
    
    @Column(name = "best_price")
    private BigDecimal bestPrice;
    
    @Column(name = "discount_percent")
    private BigDecimal discountPercent;
    private Integer views;
    private Integer bought;
    private String prescriptionRequired;
    private String label;
    private String factBox;
    private String primaryUse;
    private String storage;
    private String useOf;
    private String commonSideEffect;
    private String alcoholInteraction;
    private String pregnancyInteraction;
    private String lactationInteraction;
    private String drivingInteraction;
    private String kidneyInteraction;
    private String liverInteraction;
    private String manufacturerAddress;
    private String countryOfOrigin;
    private String forSale;
    private String qa;
}