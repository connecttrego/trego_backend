package com.trego.dto;

import com.trego.dto.response.CartResponseDTO;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {


    private long userId;
    private long addressId;
    private long preOrderId;
}