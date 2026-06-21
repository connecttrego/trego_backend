package com.trego.api;

import com.trego.dto.BucketOrderRequestDTO;
import com.trego.dto.CancelOrderRequestDTO;
import com.trego.dto.OrderRequestDTO;
import com.trego.dto.OrderValidateRequestDTO;
import com.trego.dto.response.CancelOrderResponseDTO;
import com.trego.dto.response.OrderResponseDTO;
import com.trego.dto.response.OrderValidateResponseDTO;
import com.trego.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @PostMapping
    public OrderResponseDTO placeOrder(@RequestBody OrderRequestDTO orderRequest) throws Exception {
        return orderService.placeOrder(orderRequest);
    }

    // New endpoint for placing order from a selected bucket
    @PostMapping("/fromBucket")
    public OrderResponseDTO placeOrderFromBucket(@RequestBody BucketOrderRequestDTO bucketOrderRequest) throws Exception {
        return orderService.placeOrderFromBucket(bucketOrderRequest);
    }

    @PostMapping("/validateOrder")
    public OrderValidateResponseDTO validateOrder(@RequestBody OrderValidateRequestDTO orderValidateRequestDTO) throws Exception {
        return orderService.validateOrder(orderValidateRequestDTO);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> fetchAllOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            System.out.println(">>> ORDERS REQUEST for userId: " + userId + ", page: " + page + ", size: " + size);
            Page<OrderResponseDTO> result = orderService.fetchAllOrders(userId, page, size);
            System.out.println(">>> ORDERS RESULT count: " + result.getNumberOfElements() + " (total: " + result.getTotalElements() + ") for userId: " + userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println(">>> ORDERS ERROR for userId: " + userId + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<CancelOrderResponseDTO> cancelOrders(@RequestBody CancelOrderRequestDTO request) throws Exception {

        try {
            CancelOrderResponseDTO response = orderService.cancelOrders(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CancelOrderResponseDTO("Failed to cancel orders", List.of(), List.of()));
        }


    }
}