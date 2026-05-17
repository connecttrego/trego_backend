package com.trego.dto;


import lombok.Data;
import java.util.List;

@Data
public class VendorDTO {

    private Integer id;
    private String name;
    private String licence;
    private String gstNumber;
    private String address;
    private String logo;
    private Double lat;
    private Double lng;
    private Integer deliveryTime;
    private String reviews;
    private String rating;
    private List<MedicineDTO> medicines;

    private List<BannerDTO> banners;

}