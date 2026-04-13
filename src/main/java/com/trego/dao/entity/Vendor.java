package com.trego.dao.entity;


import org.checkerframework.checker.units.qual.C;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

// CREATE TABLE `vendor_informations` (
//   `applicant_id` int(11) NOT NULL AUTO_INCREMENT,
//   `vendor_user_id` bigint(20) DEFAULT NULL,
//   `vendor_id` varchar(50) DEFAULT NULL,
//   `ref_name` varchar(100) DEFAULT NULL,
//   `category` enum('pharmacy','pathology','surgery') NOT NULL,
//   `category_type` varchar(50) DEFAULT NULL,
//   `address` text DEFAULT NULL,
//   `druglicense` varchar(100) DEFAULT NULL,
//   `gstin` varchar(50) DEFAULT NULL,
//   `mobile` varchar(20) DEFAULT NULL,
//   `email` varchar(100) DEFAULT NULL,
//   `is_verified` tinyint(1) DEFAULT 0,
//   `active` tinyint(1) DEFAULT 0,
//   `created_at` timestamp NULL DEFAULT current_timestamp(),
//   `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
//   `logo` varchar(255) DEFAULT NULL,
//   `website` varchar(255) DEFAULT NULL,
//   `delivery_time_minutes` int(11) DEFAULT NULL,
//   `delivery_range_km` decimal(5,2) DEFAULT NULL,
//   `lat` decimal(10,6) DEFAULT NULL,
//   `lng` decimal(10,6) DEFAULT NULL,
//   `user_discount` decimal(5,2) DEFAULT 0.00,
//   `company_discount` decimal(5,2) DEFAULT 0.00,
//   `vendor_offer_user` decimal(5,2) DEFAULT 0.00,
//   `company_offer_user` decimal(5,2) DEFAULT 0.00,
//   `offer_start_date` date DEFAULT NULL,
//   `offer_end_date` date DEFAULT NULL,
//   `verified_by` varchar(25) DEFAULT NULL,
//   `pan_card` varchar(255) DEFAULT NULL,
//   `bank_passbook` varchar(255) DEFAULT NULL,
//   `cancelled_cheque` varchar(255) DEFAULT NULL,
//   `rating` varchar(255) DEFAULT NULL,
//   `reviews` varchar(255) DEFAULT NULL,
//   PRIMARY KEY (`applicant_id`),
//   KEY `fk_vendor_user` (`vendor_user_id`)
// ) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

@Data
@Entity(name = "vendor_informations")
public class Vendor {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vendor_user_id")
    private Long id;
    @Column(name = "ref_name")
    private String name;
    @Column(name = "druglicense")
    private  String druglicense;
    @Column(name = "gstin")
    private String gistin;
    private String category;
    private String logo;
    private String lat;
    private String lng;
    private String address;
    @Column(name = "delivery_time_minutes")
    private String deliveryTime;
    private String reviews;
    private String rating;
}