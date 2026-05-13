package com.trego.dao.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity(name = "vendor_medicine")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vendor_medicine_id")
    private Long vendorMedicineId;

    @Column(columnDefinition = "TEXT")
    private String name;

    @Column(name = "salt_composition")
    private String saltComposition;

    @Column(name = "medicine_type")
    private String medicineType;

    @Column(name = "packing_type")
    private String packingType;

    @Column(name = "country_of_origin")
    private String countryOfOrigin;

    @Column(name = "prescription_required")
    private String prescriptionRequired;

    @Column(columnDefinition = "TEXT")
    private String storage;

    @Column(name = "manufacture")
    private String manufacture;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "bucket_id")
    private Integer subcategoryId;

    @Column(name = "vendor_id")
    private Integer vendorId;

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "price_id")
    private Integer priceId;

    @Column(name = "medicine_owner", columnDefinition = "ENUM('super_admin','vendor')")
    private String medicineOwner;

    @Column(name = "id")
    private Long id;

    @Column(name = "medicine_id")
    private Long medicineId;

    @Column(name = "category")
    private String category;

    @Column(name = "sub_category")
    private String subCategory;

    @OneToMany(mappedBy = "medicine")
    private List<Stock> stocks;

    @OneToOne(mappedBy = "medicine")
    private MedicineInformation medicineInformation;
}