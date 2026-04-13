package com.trego.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trego.dao.entity.Vendor;
import com.trego.dto.VendorDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {


    private long  orderId;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private String orderStatus;
    private String paymentStatus;
    private String address;
    private String pinCode;
    private VendorDTO vendor;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;
    private List<OrderItemDTO> orderItemsList;
    private String cancelReason;
    private String cancelReasonId;



}