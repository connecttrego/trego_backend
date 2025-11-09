package com.trego.dto.response;

import com.trego.dto.MedicineDTO;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class OrderItemDTO {
   private  double totalAmount;
   private long itemId;
   private  int qty;
   private double mrp;
   private double price;
   private Map<String, Object> medicine = new HashMap<>();

}