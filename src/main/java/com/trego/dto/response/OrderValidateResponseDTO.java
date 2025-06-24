package com.trego.dto.response;

import lombok.Data;

@Data
public class OrderValidateResponseDTO {

    
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private boolean isValidate;
}