package com.trego.dao.entity;


import org.checkerframework.checker.units.qual.C;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity(name = "vendor_informations")
public class Vendor {

    @Id
    @Column(name = "vendor_user_id")
    private int id;
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