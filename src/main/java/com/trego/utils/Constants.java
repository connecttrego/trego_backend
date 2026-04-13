package com.trego.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Constants {
    public static  final String LOGO_BASE_URL = "";

    public static  final String MEDICINES_BASE_URL = "";
    public static  final String CATEGORIES_MEDICINE_BASE_URL = "";


    public static  final String TOP_BASE_URL = "";

    public static  final String MIDDLE_BASE_URL = "";

    public static  final String OFFLINE_BASE_URL = "";

    public static  final String ONLINE_BASE_URL = "";

    public static BigDecimal calculateUnitPrice(BigDecimal mrp, BigDecimal discount) {

    if (mrp == null) return BigDecimal.ZERO;
    if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) return mrp;

    BigDecimal discountAmount = mrp
            .multiply(discount)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

    return mrp.subtract(discountAmount);
}
}