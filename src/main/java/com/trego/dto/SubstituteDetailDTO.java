package com.trego.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SubstituteDetailDTO {
    private Long id;
    private String name;
    private String manufacturers;
    private String saltComposition;
    private String medicineType;
    private Integer stock;
//    private String introduction;
    private String benefits;
//    private String description;
    //private String howToUse;
    //private String safetyAdvise;
    private String ifMiss;
    private String packaging;
    private String packagingType;
    private BigDecimal mrp;
    private BigDecimal bestPrice;
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