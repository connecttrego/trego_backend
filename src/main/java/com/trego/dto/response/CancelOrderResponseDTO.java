package com.trego.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class CancelOrderResponseDTO {

    private String message;
    private List<Long> orders;
    private List<Long> subOrders;


    public CancelOrderResponseDTO(String message, List<Long> orders, List<Long> subOrders) {
        this.message = message;
        this.orders = orders;
        this.subOrders = subOrders;
    }


}