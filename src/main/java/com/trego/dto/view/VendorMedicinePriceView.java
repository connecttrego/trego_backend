package com.trego.dto.view;

/**
 * Projection interface for the medicine search query that returns
 * all vendors selling a medicine sorted by selling price (cheapest first).
 */
public interface VendorMedicinePriceView {
    Long getMedicineId();
    String getMedicineName();
    String getManufacturer();
    String getSaltComposition();
    String getPhoto1();
    String getPacking();
    String getUseOf();
    Integer getStockId();
    Double getMrp();
    Double getDiscount();
    Integer getQty();
    String getExpiryDate();
    Double getSellingPrice();
    Integer getVendorId();
    String getVendorName();
    String getVendorLogo();
    String getVendorRating();
    Integer getDeliveryTime();
}