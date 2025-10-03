package com.trego.dto;


import lombok.Data;

import java.util.List;

@Data
public class VendorDTO {

    private Long id;
    private String name;
    private String licence;
    private String gstNumber;
    private String address;
    private String logo;
    private String lat;
    private String lng;
    private String deliveryTime;
    private String reviews;
    private String rating;
    private List<MedicineDTO> medicines;

    private List<BannerDTO> banners;

}