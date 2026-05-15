package com.trego.dto.response;


import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class OrderItemDTO {
   private  BigDecimal totalAmount;
   private long itemId;
   private  int qty;
   private BigDecimal mrp;
   private BigDecimal price;
   private Map<String, Object> medicine = new HashMap<>();

}