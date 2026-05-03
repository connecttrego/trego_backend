package com.trego.dao.entity;


import java.math.BigDecimal;
import java.time.LocalDate;

import org.checkerframework.checker.units.qual.C;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
//
//CREATE TABLE `vendor_medicine_price` (
//        `price_id` int(11) NOT NULL AUTO_INCREMENT,
//  `mrp` decimal(10,2) NOT NULL,
//  `discount` decimal(10,2) DEFAULT 0.00,
//        `selling_price` decimal(10,2) NOT NULL,
//  `offer_percent` decimal(5,2) DEFAULT NULL,
//  `bought` tinyint(1) DEFAULT 0,
//        `cost_price` decimal(10,2) DEFAULT NULL,
//  `created_at` timestamp NULL DEFAULT current_timestamp(),
//  `expiry_date` date DEFAULT NULL,
//        `quantity` int(11) DEFAULT NULL,
//  `manufacturer_date` timestamp NULL DEFAULT NULL,
//  `vendor_id` int(11) NOT NULL,
//  `vendor_medicine_id` bigint(20) DEFAULT NULL,
//PRIMARY KEY (`price_id`),
//KEY `fk_vendor_price_id` (`vendor_id`),
//KEY `fk_vendor_medicine` (`vendor_medicine_id`),
//CONSTRAINT `fk_vendor_medicine` FOREIGN KEY (`vendor_medicine_id`) REFERENCES `vendor_medicine` (`vendor_medicine_id`),
//CONSTRAINT `fk_vendor_price_id` FOREIGN KEY (`vendor_id`) REFERENCES `vendor_signup` (`vendor_id`)
//        ) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


@Data
@Entity(name = "vendor_medicine_price")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private long id;
    private BigDecimal mrp;
    private BigDecimal discount;
    @Column(name = "quantity")
    private int qty;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @ManyToOne
    @JoinColumn(name = "vendor_medicine_id")
    @JsonIgnore
    private Medicine medicine;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor = null;

}
