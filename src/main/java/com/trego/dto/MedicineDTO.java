package com.trego.dto;

import com.trego.dao.entity.Stock;
import lombok.Data;

import java.util.List;

@Data
public class MedicineDTO {

    private long id;
    private String name;
    private String manufacturer;
    private String saltComposition;
    private String medicineType;
    private String introduction;
    private String description;

    private String howItWorks;
    private String safetyAdvise;
    private String ifMiss;
    private String useOf;
    private String prescriptionRequired;
    private String storage;


    private String commonSideEffect;
    private String alcoholInteraction;
    private String pregnancyInteraction;
    private String lactationInteraction;
    private String drivingInteraction;
    private String kidneyInteraction;
    private String liverInteraction;
    private String manufacturerAddress;
    private String countryOfOrigin;
    private String questionAnswers;
    private String photo1;


    private List<Stock> offLineStocks;
    private List<Stock> onLineStocks;
    private double mrp;
    private double discount;
    private int qty;
    private String expiryDate;
    private Double actualPrice;
    private String image;
    private String strip;
    private Double offeredPrice;
    private Long salesCount; // Added field for sales count
}