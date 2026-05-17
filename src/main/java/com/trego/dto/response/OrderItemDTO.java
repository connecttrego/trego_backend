package com.trego.dto.response;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class OrderItemDTO {
   private Double totalAmount;
   private long itemId;
   private int qty;
   private Double mrp;
   private Double price;
   private Map<String, Object> medicine = new HashMap<>();

}