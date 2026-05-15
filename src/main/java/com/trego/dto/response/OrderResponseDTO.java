package com.trego.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trego.dto.AddressDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {
    private Long userId;
    private Long orderId;
    private String razorpayOrderId;
    private BigDecimal totalCartValue;
    private BigDecimal amountToPay;
    private BigDecimal discount;
    private String mobileNo;
    private AddressDTO address;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;
    private String paymentStatus;
    private List<OrderDTO> orders;
}
