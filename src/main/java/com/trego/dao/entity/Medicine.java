package com.trego.dao.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

//   `image_1` varchar(255) DEFAULT NULL,
//   `image_2` varchar(255) DEFAULT NULL,
//   `image_3` varchar(255) DEFAULT NULL,
//   `image_4` varchar(255) DEFAULT NULL,
//   `image_5` varchar(255) DEFAULT NULL,
//   `created_at` timestamp NULL DEFAULT current_timestamp(),
//   `alcohol_interaction` varchar(255) DEFAULT NULL,
//   `common_side_effect` longtext DEFAULT NULL,
//   `description` longtext DEFAULT NULL,
//   `driving_interaction` varchar(255) DEFAULT NULL,
//   `how_it_works` longtext DEFAULT NULL,
//   `if_miss` longtext DEFAULT NULL,
//   `introduction` longtext DEFAULT NULL,
//   `kidney_interaction` varchar(255) DEFAULT NULL,
//   `lactation_interaction` varchar(255) DEFAULT NULL,
//   `liver_interaction` varchar(255) DEFAULT NULL,
//   `pregnancy_interaction` varchar(255) DEFAULT NULL,
//   `question_answers` longtext DEFAULT NULL,
//   `safety_advice` longtext DEFAULT NULL,
//   `use_of` varchar(255) DEFAULT NULL,
//   `packing` varchar(255) DEFAULT NULL,
//   `bucket_id` int(11) DEFAULT NULL,
//   `country_of_origin` varchar(255) DEFAULT NULL,
//   `medicine_type` varchar(255) DEFAULT NULL,
//   `name` varchar(255) DEFAULT NULL,
//   `packaging_type` varchar(255) DEFAULT NULL,
//   `photo1` varchar(255) DEFAULT NULL,
//   `photo2` varchar(255) DEFAULT NULL,
//   `photo3` varchar(255) DEFAULT NULL,
//   `photo4` varchar(255) DEFAULT NULL,
//   `prescription_required` varchar(255) DEFAULT NULL,
//   `safety_advise` longtext DEFAULT NULL,
//   `salt_composition` varchar(255) DEFAULT NULL,
//   `storage` varchar(255) DEFAULT NULL,
//   `subcategory_id` bigint(20) DEFAULT NULL,
//   PRIMARY KEY (`medicine_id`),
//   KEY `fk_constraints_bucket` (`bucket_id`),
//   CONSTRAINT `fk_constraints_bucket` FOREIGN KEY (`bucket_id`) REFERENCES `bucket` (`id`)
// ) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

@Data
@Entity(name = "medicines")
public class Medicine {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_id")
    private long id;
    private String name;
    @Column(columnDefinition = "LONGTEXT")
    private String manufacturer;
    private String saltComposition;
    @Column(name = "subcategory_id")
    private Long subcategoryId;
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
