package com.trego.dto.view;

import java.math.BigDecimal;

public interface SubstituteDetailView {
    Long getId();
    String getName();
    String getPhoto1();
    String getManufacturer();
    String getVendorName();
    String getVendorId();
    String getVendorLogo();
    String getPacking();
    BigDecimal getMrp();
    BigDecimal getBestPrice();
    BigDecimal getDiscount();
}