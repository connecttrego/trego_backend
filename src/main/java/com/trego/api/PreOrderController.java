package com.trego.api;

import com.trego.dto.PreOrderDTO;
import com.trego.dto.response.PreOrderResponseDTO;
import com.trego.dto.response.VandorCartResponseDTO;
import com.trego.exception.InvalidAmountException;
import com.trego.service.IPreOrderService;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("preorders")
public class PreOrderController {

    @Autowired
    private IPreOrderService preOrderService;

    @PostMapping
    public PreOrderResponseDTO createPreOrder(@RequestBody PreOrderDTO preOrder) {

        PreOrderResponseDTO preOrderResponseDTO = preOrderService.savePreOrder(preOrder);
        if (preOrderResponseDTO.getAmountToPay() == null ||
                preOrderResponseDTO.getAmountToPay().compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidAmountException("Amount to pay must be greater than zero.");
        }
        return preOrderResponseDTO;
    }

    @GetMapping("/user/{userId}")
    public PreOrderResponseDTO getOrdersByUserId(@PathVariable Long userId) {
        return preOrderService.getOrdersByUserId(userId);
    }

    @GetMapping("/vendorspecificcarts/order/{orderId}")
    public VandorCartResponseDTO vendorSpecificPrice(@PathVariable Long orderId) {
        return preOrderService.vendorSpecificPrice(orderId);
    }

}