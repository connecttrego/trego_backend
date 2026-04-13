package com.trego.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trego.dto.CartDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreOrderResponseDTO {

    private long userId;
    private long orderId;
    private long addressId;
    private BigDecimal totalCartValue;
    private BigDecimal amountToPay;
    private BigDecimal discount;
    private BigDecimal deliveryCharges;

    private List<CartResponseDTO> carts;

}