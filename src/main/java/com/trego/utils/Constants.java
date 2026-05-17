package com.trego.utils;

public class Constants {
    public static  final String LOGO_BASE_URL = "";

    public static  final String MEDICINES_BASE_URL = "";
    public static  final String CATEGORIES_MEDICINE_BASE_URL = "";


    public static  final String TOP_BASE_URL = "";

    public static  final String MIDDLE_BASE_URL = "";

    public static  final String OFFLINE_BASE_URL = "";

    public static  final String ONLINE_BASE_URL = "";

    public static final String DEFAULT_VENDOR_LOGO = "https://ik.imagekit.io/kqgqzlxfs/vendor/logo_1.png";

    /**
     * Returns a valid vendor logo URL with fallback.
     * If the provided logo is null, empty, or not a valid HTTP URL,
     * returns the default fallback vendor logo.
     *
     * @param logo the logo URL from DB
     * @return valid logo URL (never null or invalid)
     */
    public static String getVendorLogoWithFallback(String logo) {
        if (logo == null || logo.trim().isEmpty() || !logo.startsWith("http")) {
            return DEFAULT_VENDOR_LOGO;
        }
        return logo;
    }

    public static Double calculateUnitPrice(Double mrp, Double discount) {

    if (mrp == null) return 0.0;
    if (discount == null || discount <= 0.0) return mrp;

    double discountAmount = mrp * discount / 100.0;

    return mrp - discountAmount;
}
}