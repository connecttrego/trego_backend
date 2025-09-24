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

import java.util.*;
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
    OrderItemRepository orderItemRepository;

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
                    dto.setLogo(banner.getLogo());
                    dto.setBannerUrl(banner.getBannerUrl());
                    dto.setPosition(banner.getPosition());
                    dto.setCreatedBy(banner.getCreatedBy());
                    return dto;
                })
                .collect(Collectors.toList());

        mainDTO.setTopBanners(topBannerDTOs);

        List<Banner> middleBanners = bannerRepository.findByPosition("middle");
        List<BannerDTO> middleBannerDTOs = middleBanners.stream()
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
        // Convert Banner entities to BannerDTOs and append base path

        mainDTO.setTopBanners(topBannerDTOs);
        mainDTO.setMiddleBanners(middleBannerDTOs);

        // Get all vendors
        List<Vendor> vendors = vendorRepository.findAll();
        List<VendorDTO> topOfflineVendors = new ArrayList<>();
        List<VendorDTO> topOnlineVendors = new ArrayList<>();
        
        // Get top selling medicine IDs
        List<Object[]> topSellingMedicineData = orderItemRepository.findTopSellingMedicineIds();
        Map<Long, Long> medicineSalesMap = new HashMap<>();
        for (Object[] data : topSellingMedicineData) {
            medicineSalesMap.put((Long) data[0], (Long) data[1]);
        }
        
        for (Vendor vendor : vendors) {
            VendorDTO vendorDTO = populateVendorDTO(vendor);
            List<Stock> stocks   = stockRepository.findByVendorId(vendor.getId());
            
            // Sort stocks based on sales count (highest first)
            stocks.sort((s1, s2) -> {
                Long medicineId1 = s1.getMedicine().getId();
                Long medicineId2 = s2.getMedicine().getId();
                Long sales1 = medicineSalesMap.getOrDefault(medicineId1, 0L);
                Long sales2 = medicineSalesMap.getOrDefault(medicineId2, 0L);
                return sales2.compareTo(sales1); // Descending order
            });
            
            List<MedicineDTO> medicineDTOList = populateMedicineDTOs(stocks, medicineSalesMap);
            vendorDTO.setMedicines(medicineDTOList);
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
        vendorDTO.setLogo(vendor.getLogo());
        vendorDTO.setGstNumber(vendor.getGistin());
        vendorDTO.setLicence(vendor.getDruglicense());
        vendorDTO.setAddress(vendor.getAddress());
        vendorDTO.setLat(vendor.getLat());
        vendorDTO.setLng(vendor.getLng());
        vendorDTO.setDeliveryTime(vendor.getDeliveryTime());
        vendorDTO.setReviews(vendor.getReviews());
        return vendorDTO;
    }

    private static List<MedicineDTO> populateMedicineDTOs(List<Stock> stocks, Map<Long, Long> medicineSalesMap) {
        int count = 0;
        List<MedicineDTO> medicineDTOList = new ArrayList<>();
        
        // Sort stocks based on sales count (highest first), then by stock ID as fallback
        stocks.sort((s1, s2) -> {
            Long medicineId1 = s1.getMedicine().getId();
            Long medicineId2 = s2.getMedicine().getId();
            Long sales1 = medicineSalesMap.getOrDefault(medicineId1, 0L);
            Long sales2 = medicineSalesMap.getOrDefault(medicineId2, 0L);
            int salesComparison = sales2.compareTo(sales1); // Descending order of sales
            if (salesComparison != 0) {
                return salesComparison;
            }
            // If sales are equal, sort by stock ID to maintain consistent ordering
            return Long.valueOf(s1.getId()).compareTo(Long.valueOf(s2.getId()));
        });
        
        for(Stock stock : stocks){
            count++;
            Medicine medicine = stock.getMedicine();
            MedicineDTO medicineDTO = new MedicineDTO();
            medicineDTO.setId(medicine.getId());
            medicineDTO.setName(medicine.getName());
            medicineDTO.setMedicineType(medicine.getMedicineType());
            medicineDTO.setManufacturer(medicine.getManufacturer());
            medicineDTO.setSaltComposition(medicine.getSaltComposition());
            medicineDTO.setPhoto1(Constants.LOGO_BASE_URL + Constants.MEDICINES_BASE_URL + medicine.getPhoto1());
            medicineDTO.setUseOf(medicine.getUseOf());
            medicineDTO.setStrip(medicine.getPacking());
            medicineDTO.setDiscount(stock.getDiscount());
            medicineDTO.setQty(stock.getQty());
            medicineDTO.setMrp(stock.getMrp());
            medicineDTO.setExpiryDate(stock.getExpiryDate());
            
            // Add sales count information
            Long salesCount = medicineSalesMap.getOrDefault(medicine.getId(), 0L);
            medicineDTO.setSalesCount(salesCount);

            medicineDTOList.add(medicineDTO);
            if(count >= 10) // Changed from > 10 to >= 10 to get exactly 10 medicines
                break;
        }
        return medicineDTOList;
    }
}