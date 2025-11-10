package com.trego.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VendorCartResponseDTO {

    private long userId;
    private long orderId;

    // make this nullable Long so code can check for null / >0
    private Long preOrderId;

    private List<CartResponseDTO> carts;

    private Long selectedVendorId; // nullable for safety

    public void setSelectedVendorId(Long selectedVendorId) {
        this.selectedVendorId = selectedVendorId;
    }

    public Long getSelectedVendorId() {
        return selectedVendorId;
    }

    public Long getPreOrderId() {
        return preOrderId;
    }

    public void setPreOrderId(Long preOrderId) {
        this.preOrderId = preOrderId;
    }
}
