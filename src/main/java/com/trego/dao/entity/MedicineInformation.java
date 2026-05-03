package com.trego.dao.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "vendor_medicine_information")
@Data
public class MedicineInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_information_id")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "vendor_medicine_id")
    private Medicine medicine;

    @Column(columnDefinition = "LONGTEXT")
    private String introduction;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Column(columnDefinition = "LONGTEXT")
    private String howItWorks;

    @Column(name = "safety_advice", columnDefinition = "LONGTEXT")
    private String safetyAdvise;

    @Column(columnDefinition = "LONGTEXT")
    private String ifMiss;

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

    @Column(columnDefinition = "LONGTEXT")
    private String questionAnswers;

    @Column(name = "image_1")
    private String photo1;
    @Column(name = "image_2")
    private String photo2;
    @Column(name = "image_3")
    private String photo3;
    @Column(name = "image_4")
    private String photo4;

    private String packing;
}