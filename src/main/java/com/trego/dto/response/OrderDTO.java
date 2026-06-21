package com.trego.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trego.dto.VendorDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderDTO {
    private long orderId;
    private long id;
    private Double totalAmount = 0.0;
    private Double discount = 0.0;
    private String orderStatus = "";
    private String paymentStatus = "";
    private String address = "";
    private String pinCode = "";
    private VendorDTO vendor;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;
    private List<OrderItemDTO> orderItemsList = new ArrayList<>();
    private String cancelReason = "";
    private String cancelReasonId = "";
}