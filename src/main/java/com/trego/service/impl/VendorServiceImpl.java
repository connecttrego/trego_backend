package com.trego.service.impl;

import com.trego.dao.entity.Banner;
import com.trego.dao.entity.Medicine;
import com.trego.dao.entity.MedicineInformation;
import com.trego.dao.entity.Stock;
import com.trego.dao.entity.Vendor;
import com.trego.dao.entity.MasterMedicine;
import com.trego.dao.impl.MasterMedicineRepository;
import com.trego.dao.impl.BannerRepository;
import com.trego.dao.impl.StockRepository;
import com.trego.dao.impl.VendorRepository;
import com.trego.dto.BannerDTO;
import com.trego.dto.MedicineDTO;
import com.trego.dto.VendorDTO;
import com.trego.service.IVendorService;
import com.trego.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
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
    private MasterMedicineRepository masterMedicineRepository;

    public List<VendorDTO> findVendorsByType(String type) {
        List<VendorDTO> vendorDTOs = new ArrayList<>();

        // Remove category condition - fetch all vendors without filtering by type
        List<Vendor> vendors = vendorRepository.findByVendorIdIsNotNull();
        for (Vendor vendor : vendors) {
            VendorDTO vendorDTO = new VendorDTO();
            vendorDTO.setId(vendor.getId());
            vendorDTO.setName((vendor.getName() != null && !vendor.getName().trim().isEmpty()) ? vendor.getName() : "Vendor " + vendor.getId());
            // Set logo with centralized fallback for invalid or missing URLs
            vendorDTO.setLogo(Constants.getVendorLogoWithFallback(vendor.getLogo()));
            vendorDTO.setGstNumber(vendor.getGistin());
            vendorDTO.setLicence(vendor.getDruglicense());
            vendorDTO.setAddress(vendor.getAddress());
            vendorDTO.setLat(vendor.getLat() != null ? vendor.getLat().doubleValue() : null);
            vendorDTO.setLng(vendor.getLng() != null ? vendor.getLng().doubleValue() : null);
            vendorDTO.setDeliveryTime(vendor.getDeliveryTime());
            vendorDTO.setReviews(vendor.getReviews());
            vendorDTO.setRating(vendor.getRating());
            vendorDTOs.add(vendorDTO);
        }
        return vendorDTOs;
    }

    @Override
    public VendorDTO getVendorByIdOrMedicine(Integer id, String searchText, int page, int size) {
        VendorDTO vendorDTO = new VendorDTO();
        Vendor vendor = vendorRepository.findById(id).orElse(null);

        // If vendor not found, return empty DTO
        if (vendor == null) {
            return vendorDTO;
        }

        if (page == 0) {
            vendorDTO.setId(vendor.getId());
            vendorDTO.setName((vendor.getName() != null && !vendor.getName().trim().isEmpty()) ? vendor.getName() : "Vendor " + vendor.getId());
            // Set logo with centralized fallback for invalid or missing URLs
            vendorDTO.setLogo(Constants.getVendorLogoWithFallback(vendor.getLogo()));
            vendorDTO.setGstNumber(vendor.getGistin());
            vendorDTO.setLicence(vendor.getDruglicense());
            vendorDTO.setAddress(vendor.getAddress());
            vendorDTO.setLat(vendor.getLat() != null ? vendor.getLat().doubleValue() : null);
            vendorDTO.setLng(vendor.getLng() != null ? vendor.getLng().doubleValue() : null);
            vendorDTO.setDeliveryTime(vendor.getDeliveryTime());
            vendorDTO.setReviews(vendor.getReviews());
            vendorDTO.setRating(vendor.getRating());

            List<Banner> topBanners = bannerRepository.findByPositionAndVendorId("vendors", id);
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

        // Fetch paginated stocks for this vendor
        Pageable pageable = PageRequest.of(page, size);
        Page<Stock> stocksPage = stockRepository.findByExternalVendorId(vendor.getVendorId(), pageable);
        List<Stock> stocks = stocksPage.getContent();

        // Build medicine list
        List<MedicineDTO> medicineDTOList = new ArrayList<>();
        boolean hasSearchText = searchText != null && !searchText.trim().isEmpty();

        for (Stock stock : stocks) {
            Medicine medicine = stock.getMedicine();
            
            if (medicine != null) {
                // Filter by searchText only if provided; otherwise include all mapped medicines
                if (hasSearchText && !medicine.getName().toLowerCase().contains(searchText.toLowerCase().trim())) {
                    continue;
                }

                // Retrieve master medicine to serve as catalog fallback
                MasterMedicine masterMed = null;
                if (medicine.getMedicineId() != null) {
                    masterMed = masterMedicineRepository.findById(medicine.getMedicineId()).orElse(null);
                } else if (medicine.getName() != null && !medicine.getName().trim().isEmpty()) {
                    List<MasterMedicine> matchedMeds = masterMedicineRepository.findByNameIgnoreCase(medicine.getName().trim());
                    if (matchedMeds != null && !matchedMeds.isEmpty()) {
                        masterMed = matchedMeds.get(0);
                    }
                }

                MedicineDTO medicineDTO = new MedicineDTO();
                medicineDTO.setId(medicine.getVendorMedicineId());
                medicineDTO.setName(medicine.getName() != null && !medicine.getName().trim().isEmpty() ? medicine.getName() : (masterMed != null ? masterMed.getName() : ""));

                MedicineInformation medicineInformation = medicine.getMedicineInformation();
                String photo = "";
                String strip = "";
                String desc = "";

                if (medicineInformation != null) {
                    photo = medicineInformation.getPhoto1();
                    strip = medicineInformation.getPacking();
                    desc = medicineInformation.getDescription();
                }

                // Apply master medicine fallback if fields are empty
                if (photo == null || photo.trim().isEmpty()) {
                    photo = (masterMed != null) ? masterMed.getPhoto1() : "";
                }
                if (strip == null || strip.trim().isEmpty()) {
                    strip = (masterMed != null && masterMed.getPackaging() != null) ? masterMed.getPackaging() : "";
                }
                if (desc == null || desc.trim().isEmpty()) {
                    desc = (masterMed != null && masterMed.getDescription() != null) ? masterMed.getDescription() : "";
                }

                medicineDTO.setPhoto1(Constants.getMedicineImageWithFallback(photo));
                medicineDTO.setStrip(strip);
                medicineDTO.setDescription(desc);

                medicineDTO.setSaltComposition(medicine.getSaltComposition() != null && !medicine.getSaltComposition().trim().isEmpty() ? medicine.getSaltComposition() : (masterMed != null ? masterMed.getSaltComposition() : ""));
                
                // Set stock info for this medicine
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