package com.trego.service.impl;

import com.trego.dao.entity.Banner;
import com.trego.dao.entity.Medicine;
import com.trego.dao.entity.MedicineInformation;
import com.trego.dao.entity.Stock;
import com.trego.dao.entity.Vendor;
import com.trego.dao.impl.BannerRepository;
import com.trego.dao.impl.StockRepository;
import com.trego.dao.impl.VendorRepository;
import com.trego.dto.BannerDTO;
import com.trego.dto.MedicineDTO;
import com.trego.dto.VendorDTO;
import com.trego.service.IVendorService;
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
    private com.trego.dao.impl.MedicineRepository medicineRepository;

    public List<VendorDTO> findVendorsByType(String type) {
        List<VendorDTO> vendorDTOs = new ArrayList<>();

        // Remove category condition - fetch all vendors without filtering by type
        List<Vendor> vendors = vendorRepository.findAll(); // This will fetch all vendors
        for (Vendor vendor : vendors) {
            VendorDTO vendorDTO = new VendorDTO();
            vendorDTO.setId(vendor.getId());
            vendorDTO.setName((vendor.getName() != null && !vendor.getName().trim().isEmpty()) ? vendor.getName() : "Vendor " + vendor.getId());
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

        // Fetch paginated medicines for this vendor instead of stocks
        Pageable pageable = PageRequest.of(page, size);
        Page<Medicine> medicinePage = medicineRepository.findByVendorId(vendor.getId(), pageable);
        List<Medicine> medicines = medicinePage.getContent();

        // Build medicine list
        List<MedicineDTO> medicineDTOList = new ArrayList<>();
        boolean hasSearchText = searchText != null && !searchText.trim().isEmpty();

        for (Medicine medicine : medicines) {
            // Filter by searchText only if provided; otherwise include all medicines
            if (hasSearchText && !medicine.getName().toLowerCase().contains(searchText.toLowerCase().trim())) {
                continue;
            }

            MedicineDTO medicineDTO = new MedicineDTO();
            medicineDTO.setId(medicine.getVendorMedicineId());
            medicineDTO.setName(medicine.getName());

            MedicineInformation medicineInformation = medicine.getMedicineInformation();
            if (medicineInformation != null) {
                medicineDTO.setPhoto1(medicineInformation.getPhoto1());
                medicineDTO.setStrip(medicineInformation.getPacking());
                medicineDTO.setDescription(medicineInformation.getDescription());
            } else {
                medicineDTO.setDescription("");
                medicineDTO.setPhoto1("");
                medicineDTO.setStrip("");
            }

            medicineDTO.setSaltComposition(medicine.getSaltComposition());
            
            // Try to find stock info for this medicine and vendor to populate price/discount
            List<Stock> medicineStocks = stockRepository.findByMedicineIdAndVendorId(medicine.getVendorMedicineId(), vendor.getId());
            if (medicineStocks != null && !medicineStocks.isEmpty()) {
                Stock stock = medicineStocks.get(0);
                medicineDTO.setDiscount(stock.getDiscount());
                medicineDTO.setQty(stock.getQty());
                medicineDTO.setMrp(stock.getMrp());
                medicineDTO.setExpiryDate(stock.getExpiryDate());
            } else {
                // Default values if no stock entry exists
                medicineDTO.setDiscount(java.math.BigDecimal.ZERO);
                medicineDTO.setQty(0);
                medicineDTO.setMrp(java.math.BigDecimal.ZERO);
                medicineDTO.setExpiryDate("N/A");
            }
            
            medicineDTOList.add(medicineDTO);
        }

        vendorDTO.setMedicines(medicineDTOList);
        return vendorDTO;
    }
}