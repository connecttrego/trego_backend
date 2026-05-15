package com.trego.service.impl;

import com.trego.dao.entity.Banner;
import com.trego.dao.entity.Medicine;
import com.trego.dao.entity.Stock;
import com.trego.dao.entity.Vendor;
import com.trego.dao.impl.*;
import com.trego.dto.*;
import com.trego.service.IMainService;
import com.trego.service.IMasterService;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
    public MainDTO loadAll(double lat, double lng) {
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
        mainDTO.setMiddleBanners(middleBannerDTOs);

        mainDTO.setOffLineTopVendor(getTopOfflineVendors("retail"));
        mainDTO.setOnLineTopVendor(getTopOfflineVendors("online"));

        mainDTO.setOffLineTopVendor(getTopVendors("retail"));
        mainDTO.setOnLineTopVendor(getTopVendors("online"));

        mainDTO.setSubCategories(masterService.loadCategoriesByType("")); // Load all categories instead of just
                                                                          // medicine categories
        return mainDTO;
    }

   public List<VendorDTO> getTopOfflineVendors(String type) {
       List<Object[]> salesData = orderItemRepository.findVendorMedicineSales();

       Map<Integer, VendorDTO> vendorMap = new LinkedHashMap<>();

       for (Object[] row : salesData) {
           Integer vendorId = (Integer) row[0];
           Long medicineId = (Long) row[1];
           Long salesCount = ((Number) row[2]).longValue();

           VendorDTO vendor = vendorMap.computeIfAbsent(vendorId, id -> {
               Vendor v = vendorRepository.findById(id).orElseThrow();
               VendorDTO dto = populateVendorDTO(v);
               dto.setMedicines(new ArrayList<>());
               return dto;
           });

           if (vendor.getMedicines().size() < 5) {
               Medicine med = medicineRepository.findById(medicineId).orElseThrow();
               MedicineDTO medDTO = new MedicineDTO();
               medDTO.setId(med.getId());
               medDTO.setName(med.getName());
               medDTO.setManufacturer(med.getManufacture());
               medDTO.setSaltComposition(med.getSaltComposition());
               medDTO.setMedicineType(med.getMedicineType());
               // medDTO.setSalesCount(salesCount.intValue());

               System.out.println("vendorId " + vendorId + "  medicineId " + medicineId);
               // Get stock info - Use the new method that returns a List to handle multiple
               // stocks
               List<Stock> stocks = stockRepository.findStocksByMedicineIdAndVendorId(medicineId, vendorId);
               if (!stocks.isEmpty()) {
                   Stock stock = stocks.get(0);

                   BigDecimal mrp = stock.getMrp();
                   BigDecimal discount = stock.getDiscount();

                   BigDecimal discountAmount = mrp
                           .multiply(discount)
                           .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                   medDTO.setOfferedPrice(mrp.subtract(discountAmount));

                   medDTO.setMrp(stock.getMrp());
                   medDTO.setDiscount(stock.getDiscount());
                   medDTO.setQty(stock.getQty());
               }

               vendor.getMedicines().add(medDTO);
           }
       }

       // pick top 5 vendors
       return vendorMap.values().stream().limit(5).toList();
   }

   // getTopVendors - VendorRepository.findByCategory removed (no category field in vendor_informations table)
   private List<VendorDTO> getTopVendors(String type) {
       List<Vendor> offlineVendors = vendorRepository.findAll();
       List<VendorDTO> topOfflineVendors = new ArrayList<>();
       List<Object[]> topSellingMedicineData = orderItemRepository.findTopSellingMedicineIds(PageRequest.of(0, 5));
       Map<Long, Long> medicineSalesMap = new HashMap<>();
       for (Object[] data : topSellingMedicineData) {
           medicineSalesMap.put((Long) data[0], (Long) data[1]);
       }
       for (Vendor vendor : offlineVendors) {
           VendorDTO vendorDTO = populateVendorDTO(vendor);
           List<Stock> stocks = stockRepository.findByVendorId(vendor.getId());
           stocks.sort((s1, s2) -> {
               Long medicineId1 = s1.getMedicine().getId();
               Long medicineId2 = s2.getMedicine().getId();
               Long sales1 = medicineSalesMap.getOrDefault(medicineId1, 0L);
               Long sales2 = medicineSalesMap.getOrDefault(medicineId2, 0L);
               return sales2.compareTo(sales1);
           });
           List<MedicineDTO> medicineDTOList = populateMedicineDTOs(stocks, medicineSalesMap);
           vendorDTO.setMedicines(medicineDTOList);
           if (topOfflineVendors.size() < 5) {
               topOfflineVendors.add(vendorDTO);
           }
       }
       return topOfflineVendors;
   }

   private VendorDTO populateVendorDTO(Vendor vendor) {
       VendorDTO dto = new VendorDTO();
       dto.setId(vendor.getId());
       dto.setName(vendor.getName());
       dto.setLicence(vendor.getDruglicense());
       dto.setGstNumber(vendor.getGistin());
       dto.setAddress(vendor.getAddress());
       dto.setLogo(vendor.getLogo());
       dto.setLat(vendor.getLat());
       dto.setLng(vendor.getLng());
       dto.setDeliveryTime(vendor.getDeliveryTime());
       dto.setReviews(vendor.getReviews());
       dto.setRating(vendor.getRating());
       return dto;
   }

   private List<MedicineDTO> populateMedicineDTOs(List<Stock> stocks, Map<Long, Long> medicineSalesMap) {
       List<MedicineDTO> medicineDTOList = new ArrayList<>();
       for (Stock stock : stocks) {
           Medicine medicine = stock.getMedicine();
           if (medicine == null) continue;
           MedicineDTO medDTO = new MedicineDTO();
           medDTO.setId(medicine.getId());
           medDTO.setName(medicine.getName());
           medDTO.setManufacturer(medicine.getManufacture());
           medDTO.setSaltComposition(medicine.getSaltComposition());
           medDTO.setMedicineType(medicine.getMedicineType());
           medDTO.setMrp(stock.getMrp());
           medDTO.setDiscount(stock.getDiscount());
           medDTO.setQty(stock.getQty());
           medDTO.setExpiryDate(stock.getExpiryDate());

           BigDecimal mrp = stock.getMrp();
           BigDecimal discount = stock.getDiscount();
           if (mrp != null && discount != null) {
               BigDecimal discountAmount = mrp
                       .multiply(discount)
                       .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
               medDTO.setOfferedPrice(mrp.subtract(discountAmount));
           }

           Long salesCount = medicineSalesMap.getOrDefault(medicine.getId(), 0L);
           medDTO.setSalesCount(salesCount);

           medicineDTOList.add(medDTO);
       }
       return medicineDTOList;
   }


}