package com.trego.dto.response;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class OrderItemDTO {
   private Double totalAmount = 0.0;
   private long itemId;
   private long id;
   private int qty;
   private Double mrp = 0.0;
   private Double price = 0.0;
   private Map<String, Object> medicine = new HashMap<>();
}