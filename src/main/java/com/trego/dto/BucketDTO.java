package com.trego.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class BucketDTO {
    private Long id;
    private String name;
    private List<BucketItemDTO> availableItems; // Available medicines
    private List<UnavailableMedicineDTO> unavailableItems; // Unavailable medicines with substitutes
    private List<SelectedSubstituteDTO> selectedSubstitutes; // User-selected substitutes
    private Double totalPrice;
    private Double deliveryCharges;
    private Double amountToPay;
    private Double totalDiscount; // Total discount across all items in the bucket
    private Integer vendorId; // If all items are from the same vendor
    private String vendorName; // If all items are from the same vendor
    private String logo;
    private String deliveryTime;
    private Double currentCartTotal; // What user is currently paying across split vendors
    private Double savings;          // How much cheaper this bucket is vs currentCartTotal
    private Boolean isCheaperOption; // True when this bucket saves money vs current cart

    @Override
    public String toString() {
        return "BucketDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", amountToPay=" + amountToPay +
                '}';
    }
}