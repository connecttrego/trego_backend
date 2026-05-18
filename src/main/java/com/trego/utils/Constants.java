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
    public static final String DEFAULT_MEDICINE_IMAGE = "https://res.cloudinary.com/dxoy1r7v8/image/upload/v1779086320/3_2_sdbzzh.jpg";

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

    /**
     * Returns a valid medicine image URL with fallback.
     *
     * @param photo the photo URL
     * @return valid photo URL (never null or empty)
     */
    public static String getMedicineImageWithFallback(String photo) {
        if (photo == null || photo.trim().isEmpty() || !photo.startsWith("http")) {
            return DEFAULT_MEDICINE_IMAGE;
        }
        return photo;
    }

    public static Double calculateUnitPrice(Double mrp, Double discount) {

    if (mrp == null) return 0.0;
    if (discount == null || discount <= 0.0) return mrp;

    double discountAmount = mrp * discount / 100.0;

    return mrp - discountAmount;
}
}