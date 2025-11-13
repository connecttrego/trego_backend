package com.trego.service.impl;

import com.trego.dao.entity.Banner;
import com.trego.dao.entity.Medicine;
import com.trego.dao.entity.Stock;
import com.trego.dao.entity.Vendor;
import com.trego.dao.impl.BannerRepository;
import com.trego.dao.impl.MedicineRepository;
import com.trego.dao.impl.StockRepository;
import com.trego.dao.impl.VendorRepository;
import com.trego.dto.BannerDTO;
import com.trego.dto.MedicineDTO;
import com.trego.dto.StockDTO;
import com.trego.dto.VendorDTO;
import com.trego.service.IVendorService;
import com.trego.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class VendorServiceImpl implements IVendorService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    public List<VendorDTO> findVendorsByType(String type) {
        List<VendorDTO> vendorDTOs = new ArrayList<>();

        // Remove category condition - fetch all vendors without filtering by type
        List<Vendor>  vendors = vendorRepository.findAll(); // This will fetch all vendors
        for (Vendor vendor : vendors){
            VendorDTO vendorDTO = new VendorDTO();
            vendorDTO.setId(vendor.getId());
            vendorDTO.setName(vendor.getName());
            // Remove the category-based URL logic
            vendorDTO.setLogo(vendor.getLogo());
            vendorDTO.setGstNumber(vendor.getGistin());
            vendorDTO.setLicence(vendor.getDruglicense());
            vendorDTO.setAddress(vendor.getAddress());
            vendorDTO.setLat(vendor.getLat());
            vendorDTO.setLng(vendor.getLng());
            vendorDTO.setDeliveryTime(vendor.getDeliveryTime());
            vendorDTO.setReviews(vendor.getReviews());
            vendorDTO.setRating(vendor.getRating());
            vendorDTOs.add(vendorDTO);
        }
        return  vendorDTOs;
    }

    @Override
    public VendorDTO getVendorByIdOrMedicine(Long id,  String searchText, int page, int size) {
        VendorDTO vendorDTO = new VendorDTO();
        Vendor vendor = vendorRepository.findById(id).orElse(null);
        if(page == 0) {
            vendorDTO.setId(vendor.getId());
            vendorDTO.setName(vendor.getName());
            // Remove the category-based URL logic
            vendorDTO.setLogo(vendor.getLogo());
            vendorDTO.setGstNumber(vendor.getGistin());
            vendorDTO.setLicence(vendor.getDruglicense());
            vendorDTO.setAddress(vendor.getAddress());
            vendorDTO.setLat(vendor.getLat());
            vendorDTO.setLng(vendor.getLng());
            vendorDTO.setDeliveryTime(vendor.getDeliveryTime());
            vendorDTO.setReviews(vendor.getReviews());
            vendorDTO.setRating(vendor.getRating());
            List<Banner> topBanners = bannerRepository.findByPositionAndVendorId("vendors", id);
            // Convert Banner entities to BannerDTOs and append base path
            List<BannerDTO> topBannerDTOs = topBanners.stream()
                    .map(banner -> {
                        BannerDTO dto = new BannerDTO();
                        dto.setId(banner.getId());
                        dto.setLogo(banner.getLogo());
                        dto.setBannerUrl(banner.getBannerUrl());
                        dto.setPosition(banner.getPosition());
                        dto.setCreatedBy(banner.getCreatedBy());
                        return dto;
                    })
                    .collect(Collectors.toList());

            vendorDTO.setBanners(topBannerDTOs);
        }
        //List<StockDTO> stockDTOS = new ArrayList<>();

        // Create a Pageable object
        Pageable pageable = PageRequest.of(page, size);
        // Fetch paginated stocks
        Page<Stock> stocksPage = stockRepository.findByVendorId(vendor.getId(), pageable);
        // Get the list of stocks from the page
        List<Stock> stocks = stocksPage.getContent();


        //List<Stock> stocks   = stockRepository.findByVendorId(vendor.getId());
        List<MedicineDTO> medicineDTOList = new ArrayList<>();
        for(Stock stock : stocks){
            Medicine medicine = stock.getMedicine();
            Pattern pattern = Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(medicine.getName());
            if (matcher.find()) {
                MedicineDTO medicineDTO = new MedicineDTO();
                medicineDTO.setId(medicine.getId());
                medicineDTO.setName(medicine.getName());
                medicineDTO.setManufacturer(medicine.getManufacturer());
                medicineDTO.setMedicineType(medicine.getMedicineType());
                medicineDTO.setDescription(medicine.getDescription());
                medicineDTO.setSaltComposition(medicine.getSaltComposition());
                medicineDTO.setPhoto1(medicine.getPhoto1());
                medicineDTO.setDiscount(stock.getDiscount());
                medicineDTO.setQty(stock.getQty());
                medicineDTO.setMrp(stock.getMrp());
                medicineDTO.setExpiryDate(stock.getExpiryDate());
                medicineDTOList.add(medicineDTO);
            }
        }
        vendorDTO.setMedicines(medicineDTOList);
        return vendorDTO;

    }
}