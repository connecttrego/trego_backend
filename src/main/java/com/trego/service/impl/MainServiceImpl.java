package com.trego.service.impl;

import com.trego.dao.entity.Banner;
import com.trego.dao.entity.Medicine;
import com.trego.dao.entity.Stock;
import com.trego.dao.entity.Vendor;
import com.trego.dao.impl.*;
import com.trego.dto.*;
import com.trego.service.IMainService;
import com.trego.service.IMasterService;
import com.trego.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MainServiceImpl implements IMainService {

    @Autowired
    MedicineRepository medicineRepository;

    @Autowired
    StockRepository stockRepository;

    @Autowired
    VendorRepository vendorRepository;

    @Autowired
    BannerRepository bannerRepository;

    @Autowired
    IMasterService masterService;

    @Override
    public MainDTO loadAll( double lat, double lng) {
        MainDTO mainDTO = new MainDTO();

        List<Banner> topBanners = bannerRepository.findByPosition("top");
        // Convert Banner entities to BannerDTOs and append base path
        List<BannerDTO> topBannerDTOs = topBanners.stream()
                .map(banner -> {
                    BannerDTO dto = new BannerDTO();
                    dto.setId(banner.getId());
                    dto.setLogo(Constants.LOGO_BASE_URL + Constants.TOP_BASE_URL + banner.getLogo());
                    dto.setBannerUrl(banner.getBannerUrl());
                    dto.setPosition(banner.getPosition());
                    dto.setCreatedBy(banner.getCreatedBy());
                    return dto;
                })
                .collect(Collectors.toList());

        List<Banner> middleBanners = bannerRepository.findByPosition("middle");
        List<BannerDTO> middleBannerDTOs = middleBanners.stream()
                .map(banner -> {
                    BannerDTO dto = new BannerDTO();
                    dto.setId(banner.getId());
                    dto.setLogo(Constants.LOGO_BASE_URL + Constants.MIDDLE_BASE_URL + banner.getLogo());
                    dto.setBannerUrl(banner.getBannerUrl());
                    dto.setPosition(banner.getPosition());
                    dto.setCreatedBy(banner.getCreatedBy());
                    return dto;
                })
                .collect(Collectors.toList());

        mainDTO.setTopBanners(topBannerDTOs);
        mainDTO.setMiddleBanners(middleBannerDTOs);

        // Get all vendors
        List<Vendor> vendors = vendorRepository.findAll();
        List<VendorDTO> topOfflineVendors = new ArrayList<>();
        List<VendorDTO> topOnlineVendors = new ArrayList<>();
        for (Vendor vendor : vendors) {
            VendorDTO vendorDTO = populateVendorDTO(vendor);
            // Add to both lists if less than 5 vendors in each list
            if(topOnlineVendors.size() < 5){
                topOfflineVendors.add(vendorDTO);
                topOnlineVendors.add(vendorDTO);
            }
        }

        mainDTO.setSubCategories(masterService.loadCategoriesByType("")); // Load all categories instead of just medicine categories
        mainDTO.setOffLineTopVendor(topOfflineVendors);
        mainDTO.setOnLineTopVendor(topOnlineVendors);
        return mainDTO;
    }

    private static VendorDTO populateVendorDTO(Vendor vendor) {
        VendorDTO vendorDTO = new VendorDTO();
        vendorDTO.setId(vendor.getId());
        vendorDTO.setName(vendor.getName());
        if(vendor.getCategory().equalsIgnoreCase("retail")) {
            vendorDTO.setLogo(Constants.LOGO_BASE_URL + Constants.OFFLINE_BASE_URL+ vendor.getLogo());
        }else{
            vendorDTO.setLogo(Constants.LOGO_BASE_URL + Constants.ONLINE_BASE_URL+ vendor.getLogo());
        }
        vendorDTO.setGstNumber(vendor.getGistin());
        vendorDTO.setLicence(vendor.getDruglicense());
        vendorDTO.setAddress(vendor.getAddress());
        vendorDTO.setLat(vendor.getLat());
        vendorDTO.setLng(vendor.getLng());
        vendorDTO.setDeliveryTime(vendor.getDeliveryTime());
        vendorDTO.setReviews(vendor.getReviews());
        return vendorDTO;
    }

}