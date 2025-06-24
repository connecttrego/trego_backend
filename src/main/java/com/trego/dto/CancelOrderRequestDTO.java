package com.trego.dto;

import lombok.Data;

import java.util.List;

@Data
public class CancelOrderRequestDTO {
    private List<Long> orders;
    private List<Long> subOrders;
    private String reasonId;
    private String reason;

}