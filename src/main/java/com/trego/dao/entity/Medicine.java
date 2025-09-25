package com.trego.dao.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

import com.trego.dao.entity.Stock;


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
    @Column(columnDefinition = "LONGTEXT")
    private String introduction;
    @Column(columnDefinition = "LONGTEXT")
    private String description;
    @Column(columnDefinition = "LONGTEXT")
    private String howItWorks;
    @Column(columnDefinition = "LONGTEXT")
    private String safetyAdvise;
    @Column(columnDefinition = "LONGTEXT")
    private String ifMiss;
    private String packing;
    private String packagingType;
    private String prescriptionRequired;
    private String storage;
    private String useOf;
    @Column(columnDefinition = "LONGTEXT")
    private String commonSideEffect;
    private String alcoholInteraction;
    private String pregnancyInteraction;
    private String lactationInteraction;
    private String drivingInteraction;
    private String kidneyInteraction;
    private String liverInteraction;
    private String manufacturerAddress;
    private String countryOfOrigin;
    @Column(columnDefinition = "LONGTEXT")
    private String questionAnswers;
    private String photo1;
    private String photo2;
    private String photo3;
    private String photo4;


    @OneToMany(mappedBy = "medicine")
    private List<Stock> stocks;  // Related to Stock

}
