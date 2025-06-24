package com.trego.dto;

import lombok.Data;

@Data
public class OrderValidateRequestDTO {

    private long orderId;
    private long userId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaysignature;
    private String razorpayAmount;
}