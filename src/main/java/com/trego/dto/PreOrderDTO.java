package com.trego.dto;

import lombok.Data;

import java.util.List;

@Data
public class PreOrderDTO {

    private long userId;
    private long orderId;
    private long addressId;
    private String mobileNo;
    private Double totalCartValue;
    private Double amountToPay;
    private double discount;
    private List<CartDTO> carts;

}