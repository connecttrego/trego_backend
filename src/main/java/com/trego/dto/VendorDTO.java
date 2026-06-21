package com.trego.dto;


import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class VendorDTO {

    private Integer id = 0;
    private String name = "";
    private String licence = "";
    private String gstNumber = "";
    private String address = "";
    private String logo = "";
    private Double lat = 0.0;
    private Double lng = 0.0;
    private Integer deliveryTime = 0;
    private String reviews = "";
    private String rating = "0.0";
    private List<MedicineDTO> medicines = new ArrayList<>();
    private List<BannerDTO> banners = new ArrayList<>();
}