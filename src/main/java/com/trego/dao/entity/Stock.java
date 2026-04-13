package com.trego.dao.entity;


import java.math.BigDecimal;

import org.checkerframework.checker.units.qual.C;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;


//CREATE TABLE `prices` (
//   `price_id` bigint(20) NOT NULL AUTO_INCREMENT,
//   `medicine_id` bigint(20) NOT NULL,
//   `batch_id` bigint(20) NOT NULL,
//   `mrp` decimal(10,2) NOT NULL,
//   `discount` decimal(10,2) DEFAULT 0.00,
//   `selling_price` decimal(10,2) NOT NULL,
//   `offer_percent` decimal(5,2) DEFAULT NULL,
//   `bought` tinyint(1) DEFAULT 0,
//   `cost_price` decimal(10,2) DEFAULT NULL,
//   `created_at` timestamp NULL DEFAULT current_timestamp(),
//   `expiry_date` date DEFAULT NULL,
//   `quantity` int(11) DEFAULT NULL,
//   PRIMARY KEY (`price_id`),
//   KEY `fk_price_medicine` (`medicine_id`),
//   KEY `fk_price_batch` (`batch_id`),
//   CONSTRAINT `fk_price_batch` FOREIGN KEY (`batch_id`) REFERENCES `batches` (`batch_id`) ON DELETE CASCADE,
//   CONSTRAINT `fk_price_medicine` FOREIGN KEY (`medicine_id`) REFERENCES `medicines` (`medicine_id`) ON DELETE CASCADE
// ) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


@Data
@Entity(name = "prices")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private long id;
    private BigDecimal mrp;
    private BigDecimal discount;
    private int qty;

    @Column(name = "expiry_date")
    private String expiryDate;

    @ManyToOne
    @JoinColumn(name = "medicine_id")
    @JsonIgnore
    private Medicine medicine;


    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

}
