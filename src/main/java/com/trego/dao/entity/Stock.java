package com.trego.dao.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity(name = "stocks")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private double mrp;
    private double discount;
    private int qty;

    private String expiryDate;

    @ManyToOne
    @JoinColumn(name = "medicine_id")
    @JsonIgnore
    private Medicine medicine;


    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

}
