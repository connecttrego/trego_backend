package com.trego.dto;


import lombok.Data;

import java.util.List;

@Data
public class MainDTO {

   List<BannerDTO> topBanners;
   List<VendorDTO> offLineTopVendor;
   List<BannerDTO> middleBanners;
   List<CategoryDTO> subCategories;
   List<VendorDTO> onLineTopVendor;
}