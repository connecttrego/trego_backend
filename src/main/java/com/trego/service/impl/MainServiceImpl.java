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

           // Skip if vendor was deleted from DB
           Vendor vendorEntity = vendorRepository.findById(vendorId).orElse(null);
           if (vendorEntity == null) {
               System.out.println("Skipping deleted vendor ID: " + vendorId);
               continue;
           }

           VendorDTO vendor = vendorMap.computeIfAbsent(vendorId, id -> {
               VendorDTO dto = populateVendorDTO(vendorEntity);
               dto.setMedicines(new ArrayList<>());
               return dto;
           });

           if (vendor.getMedicines().size() < 5) {
               Medicine med = medicineRepository.findById(medicineId).orElse(null);
               if (med == null) {
                   System.out.println("Skipping deleted medicine ID: " + medicineId);
                   continue;
               }
               MedicineDTO medDTO = new MedicineDTO();
               medDTO.setId(med.getId());
               medDTO.setName(med.getName());
               medDTO.setManufacturer(med.getManufacture());
               medDTO.setSaltComposition(med.getSaltComposition());
               medDTO.setMedicineType(med.getMedicineType());

               System.out.println("vendorId " + vendorId + "  medicineId " + medicineId);
               Integer externalVendorId = vendorEntity.getVendorId() != null ? vendorEntity.getVendorId() : vendorId;
               List<Stock> stocks = stockRepository.findStocksByMedicineIdAndBothVendorIds(medicineId, vendorId, externalVendorId);
               if (!stocks.isEmpty()) {
                   Stock stock = stocks.get(0);
                   Double mrp = stock.getMrp();
                   Double discount = stock.getDiscount();
                   Double discountAmount = mrp * discount / 100.0;
                   medDTO.setOfferedPrice(mrp - discountAmount);
                   medDTO.setMrp(stock.getMrp());
                   medDTO.setDiscount(stock.getDiscount());
                   medDTO.setQty(stock.getQty());
               }
               vendor.getMedicines().add(medDTO);
           }
       }

       // If no order history exists yet, fall back to showing current active vendors
       if (vendorMap.isEmpty()) {
           System.out.println("No order history found, showing all active vendors as top vendors");
           return getTopVendors(type);
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

        // Fix N+1 queries: Batch fetch all stocks for all vendor IDs in a single query
        List<Integer> vendorIds = offlineVendors.stream()
                .map(Vendor::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Stock> allStocks = new ArrayList<>();
        if (!vendorIds.isEmpty()) {
            allStocks = stockRepository.findAllByVendorIds(vendorIds);
        }

        // Group stocks by vendor ID in-memory
        Map<Integer, List<Stock>> stocksByVendor = allStocks.stream()
                .filter(s -> s.getVendor() != null && s.getVendor().getId() != null)
                .collect(Collectors.groupingBy(s -> s.getVendor().getId()));

        for (Vendor vendor : offlineVendors) {
            VendorDTO vendorDTO = populateVendorDTO(vendor);
            List<Stock> stocks = new ArrayList<>(stocksByVendor.getOrDefault(vendor.getId(), new ArrayList<>()));
            stocks.sort((s1, s2) -> {
                if (s1.getMedicine() == null || s2.getMedicine() == null) return 0;
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
        dto.setLogo(Constants.getVendorLogoWithFallback(vendor.getLogo()));
        dto.setLat(vendor.getLat() != null ? vendor.getLat().doubleValue() : null);
        dto.setLng(vendor.getLng() != null ? vendor.getLng().doubleValue() : null);
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

            Double mrp = stock.getMrp();
            Double discount = stock.getDiscount();
            if (mrp != null && discount != null) {
                Double discountAmount = mrp * discount / 100.0;
                medDTO.setOfferedPrice(mrp - discountAmount);
            }

            Long salesCount = medicineSalesMap.getOrDefault(medicine.getId(), 0L);
            medDTO.setSalesCount(salesCount);

            medicineDTOList.add(medDTO);
        }
        return medicineDTOList;
    }


}