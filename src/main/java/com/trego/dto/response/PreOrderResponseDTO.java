package com.trego.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreOrderResponseDTO {

    private long userId;
    private long orderId;
    private long addressId;
    private Double totalCartValue;
    private Double amountToPay;
    private Double discount;
    private Double deliveryCharges;

    private List<CartResponseDTO> carts;

}