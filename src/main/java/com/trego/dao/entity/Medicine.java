package com.trego.dao.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity(name = "medicines")
public class Medicine {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private String packing;
    private String packagingType;
    private String prescriptionRequired;
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
    private String questionAnswers;
    private String photo1;
    private String photo2;
    private String photo3;
    private String photo4;


    @OneToMany(mappedBy = "medicine")
    private List<Stock> stocks;  // Related to Stock

}
