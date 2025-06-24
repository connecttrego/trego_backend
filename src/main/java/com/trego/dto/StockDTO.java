package com.trego.dto;


import lombok.Data;


@Data
public class StockDTO {

    private long id;
    private int mrp;
    private int discount;
    private int qty;
    private String expiryDate;
    private MedicineDTO medicine;

}
