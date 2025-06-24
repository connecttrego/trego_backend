package com.trego.service;

import com.trego.dto.PreOrderDTO;
import com.trego.dto.response.PreOrderResponseDTO;
import com.trego.dto.response.VandorCartResponseDTO;

public interface IPreOrderService {
    public PreOrderResponseDTO savePreOrder(PreOrderDTO preOrder);

    PreOrderResponseDTO getOrdersByUserId(Long userId);

    VandorCartResponseDTO vendorSpecificPrice(long orderId);
}
