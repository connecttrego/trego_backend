package com.trego.dto;


import com.trego.dao.entity.Banner;
import lombok.Data;

import java.util.List;

@Data
public class MainDTO {

   List<Banner> topBanners;
   List<VendorDTO> offLineTopVendor;
   List<Banner> middleBanners;
   List<CategoryDTO> subCategories;
   List<VendorDTO> onLineTopVendor;
}

