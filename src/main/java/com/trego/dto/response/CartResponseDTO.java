package com.trego.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trego.dto.MedicineDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartResponseDTO {
    private Integer vendorId;
    private BigDecimal totalCartValue;
    private BigDecimal amountToPay;
    private BigDecimal discount;
    private  long orderId;
    private String name;
    private String licence;
    private String gstNumber;
   // private String address;
    private String logo;
    private BigDecimal lat;
    private BigDecimal lng;
    private Integer deliveryTime;
    private String reviews;
    private String rating;

    private List<MedicineDTO> medicine;
}