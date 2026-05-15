package com.trego.dao.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity(name = "vendor_informations")
public class Vendor {

    @Id
    @Column(name = "vendor_user_id")
    private Integer id;

    @Column(name = "ref_name")
    private String name;

    @Column(name = "druglicense")
    private String druglicense;

    @Column(name = "gstin")
    private String gistin;

    private String logo;

    @Column(precision = 10, scale = 6)
    private BigDecimal lat;

    @Column(precision = 10, scale = 6)
    private BigDecimal lng;

    private String address;

    @Column(name = "delivery_time_minutes")
    private Integer deliveryTime;

    private String reviews;

    private String rating;
}