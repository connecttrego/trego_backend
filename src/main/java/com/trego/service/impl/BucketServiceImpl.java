package com.trego.service.impl;

import com.trego.dao.entity.Medicine;
import com.trego.dao.entity.Stock;
import com.trego.dao.entity.Vendor;
import com.trego.dao.impl.MedicineRepository;
import com.trego.dao.impl.StockRepository;
import com.trego.dao.impl.VendorRepository;
import com.trego.dto.BucketDTO;
import com.trego.dto.BucketItemDTO;
import com.trego.dto.BucketRequestDTO;
import com.trego.service.IBucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BucketServiceImpl implements IBucketService {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Override
    public List<BucketDTO> createOptimizedBuckets(BucketRequestDTO request) {
        List<Long> medicineIds = request.getMedicineIds();
        
        // Get all medicines
        List<Medicine> medicines = medicineRepository.findAllById(medicineIds);
        
        // Get all stocks for these medicines
        List<Stock> allStocks = stockRepository.findAll();
        List<Stock> relevantStocks = allStocks.stream()
                .filter(stock -> medicineIds.contains(stock.getMedicine().getId()))
                .collect(Collectors.toList());
        
        // Group stocks by vendor
        Map<Long, List<Stock>> stocksByVendor = relevantStocks.stream()
                .collect(Collectors.groupingBy(stock -> stock.getVendor().getId()));
        
        // Create buckets for each vendor
        List<BucketDTO> buckets = new ArrayList<>();
        
        for (Map.Entry<Long, List<Stock>> entry : stocksByVendor.entrySet()) {
            Long vendorId = entry.getKey();
            List<Stock> vendorStocks = entry.getValue();
            
            // Check if this vendor has all required medicines
            Set<Long> vendorMedicineIds = vendorStocks.stream()
                    .map(stock -> stock.getMedicine().getId())
                    .collect(Collectors.toSet());
            
            if (vendorMedicineIds.containsAll(medicineIds)) {
                // Create a bucket for this vendor
                BucketDTO bucket = createBucketForVendor(vendorId, vendorStocks, medicines);
                buckets.add(bucket);
            }
        }
        
        // Also create mixed vendor buckets (one medicine from each vendor at best price)
        BucketDTO mixedBucket = createMixedVendorBucket(medicines, relevantStocks);
        if (mixedBucket != null) {
            buckets.add(mixedBucket);
        }
        
        // Sort buckets by total price
        buckets.sort(Comparator.comparingDouble(BucketDTO::getTotalPrice));
        
        return buckets;
    }
    
    private BucketDTO createBucketForVendor(Long vendorId, List<Stock> vendorStocks, List<Medicine> medicines) {
        BucketDTO bucket = new BucketDTO();
        bucket.setId(vendorId); // Use vendor ID as bucket ID
        Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
        bucket.setName(vendor != null ? "Complete bucket from " + vendor.getName() : "Complete bucket from vendor " + vendorId);
        bucket.setVendorId(vendorId);
        bucket.setVendorName(vendor != null ? vendor.getName() : "");
        
        List<BucketItemDTO> items = new ArrayList<>();
        double totalPrice = 0.0;
        
        for (Medicine medicine : medicines) {
            // Find the stock for this medicine from this vendor
            Optional<Stock> stockOptional = vendorStocks.stream()
                    .filter(s -> s.getMedicine().getId() == medicine.getId()) // Fixed: using == for primitive comparison
                    .findFirst();
            
            if (stockOptional.isPresent()) {
                Stock stock = stockOptional.get();
                BucketItemDTO item = new BucketItemDTO();
                item.setMedicineId(medicine.getId());
                item.setMedicineName(medicine.getName());
                item.setVendorId(vendorId);
                item.setVendorName(vendor != null ? vendor.getName() : "");
                item.setPrice(calculateFinalPrice(stock.getMrp(), stock.getDiscount()));
                item.setDiscount(stock.getDiscount());
                item.setQuantity(stock.getQty());
                
                items.add(item);
                totalPrice += item.getPrice();
            }
        }
        
        bucket.setItems(items);
        bucket.setTotalPrice(totalPrice);
        
        return bucket;
    }
    
    private BucketDTO createMixedVendorBucket(List<Medicine> medicines, List<Stock> allStocks) {
        BucketDTO bucket = new BucketDTO();
        bucket.setId(System.currentTimeMillis()); // Unique ID for mixed bucket
        bucket.setName("Best price mixed vendor bucket");
        
        List<BucketItemDTO> items = new ArrayList<>();
        double totalPrice = 0.0;
        
        for (Medicine medicine : medicines) {
            // Find the best price for this medicine across all vendors
            List<Stock> medicineStocks = allStocks.stream()
                    .filter(stock -> stock.getMedicine().getId() == medicine.getId()) // Fixed: using == for primitive comparison
                    .collect(Collectors.toList());
            
            if (!medicineStocks.isEmpty()) {
                // Find the stock with the lowest final price
                Optional<Stock> bestStockOptional = medicineStocks.stream()
                        .min(Comparator.comparingDouble(stock -> calculateFinalPrice(stock.getMrp(), stock.getDiscount())));
                
                if (bestStockOptional.isPresent()) {
                    Stock bestStock = bestStockOptional.get();
                    Vendor vendor = bestStock.getVendor();
                    
                    BucketItemDTO item = new BucketItemDTO();
                    item.setMedicineId(medicine.getId());
                    item.setMedicineName(medicine.getName());
                    item.setVendorId(vendor.getId());
                    item.setVendorName(vendor.getName());
                    item.setPrice(calculateFinalPrice(bestStock.getMrp(), bestStock.getDiscount()));
                    item.setDiscount(bestStock.getDiscount());
                    item.setQuantity(bestStock.getQty());
                    
                    items.add(item);
                    totalPrice += item.getPrice();
                }
            }
        }
        
        // Only return bucket if we have all medicines
        if (items.size() == medicines.size()) {
            bucket.setItems(items);
            bucket.setTotalPrice(totalPrice);
            return bucket;
        }
        
        return null;
    }
    
    private double calculateFinalPrice(double mrp, double discount) {
        return mrp - (mrp * discount / 100);
    }

    @Override
    public List<BucketDTO> getAllBuckets() {
        // This would typically retrieve saved buckets from a database
        // For now, we'll return an empty list as we create buckets on-demand
        return new ArrayList<>();
    }
}