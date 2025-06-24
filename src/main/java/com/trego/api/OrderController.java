package com.trego.api;

import com.trego.dto.CancelOrderRequestDTO;
import com.trego.dto.OrderRequestDTO;
import com.trego.dto.OrderValidateRequestDTO;
import com.trego.dto.response.CancelOrderResponseDTO;
import com.trego.dto.response.OrderResponseDTO;
import com.trego.dto.response.OrderValidateResponseDTO;
import com.trego.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/validateOrder")
    public OrderValidateResponseDTO validateOrder(@RequestBody OrderValidateRequestDTO orderValidateRequestDTO) throws Exception {
        return orderService.validateOrder(orderValidateRequestDTO);
    }

    @GetMapping("/user/{userId}")
    public Page<OrderResponseDTO> fetchAllOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return orderService.fetchAllOrders(userId,  page, size);
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