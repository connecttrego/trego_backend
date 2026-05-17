package com.trego.dto.response;

import lombok.Data;

/**
 * DTO representing a single vendor's price for a specific medicine.
 * Contains vendor info, stock/pricing details, and the computed selling price.
 */
@Data
public class VendorPriceDTO {
    private Integer stockId;
    private Integer vendorId;
    private String vendorName;
    private String vendorLogo;
    private String vendorRating;
    private Integer deliveryTimeMinutes;
    private Double mrp;
    private Double discount;
    private Double sellingPrice;
    private Integer qty;
    private String expiryDate;
    private String packing;
}