package com.trego.service;

import com.trego.dto.BucketOrderRequestDTO;
import com.trego.dto.CancelOrderRequestDTO;
import com.trego.dto.OrderRequestDTO;
import com.trego.dto.OrderValidateRequestDTO;
import com.trego.dto.response.CancelOrderResponseDTO;
import com.trego.dto.response.OrderResponseDTO;
import com.trego.dto.response.OrderValidateResponseDTO;
import org.springframework.data.domain.Page;

public interface IOrderService {

    public OrderResponseDTO placeOrder(OrderRequestDTO orderRequest) throws Exception;
    
    // New method for placing order from a selected bucket
    public OrderResponseDTO placeOrderFromBucket(BucketOrderRequestDTO bucketOrderRequest) throws Exception;

    OrderValidateResponseDTO validateOrder(OrderValidateRequestDTO orderValidateRequestDTO) throws Exception;

    Page<OrderResponseDTO> fetchAllOrders(Long userId, int page, int size);

    CancelOrderResponseDTO cancelOrders(CancelOrderRequestDTO request) throws Exception;
}