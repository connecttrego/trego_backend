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
import com.trego.dto.MedicineDTO;
import com.trego.dto.response.CartResponseDTO;
import com.trego.dto.response.VandorCartResponseDTO;
import com.trego.dto.UnavailableMedicineDTO;
import com.trego.dto.view.SubstituteDetailView;
import com.trego.service.IBucketService;
import com.trego.service.ISubstituteService;
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
    
    @Autowired
    private ISubstituteService substituteService;
    
    @Override
    public List<BucketDTO> createOptimizedBuckets(BucketRequestDTO request) {
        Map<Long, Integer> medicineQuantities = request.getMedicineQuantities();
        List<Long> medicineIds = new ArrayList<>(medicineQuantities.keySet());
        
        // Check if medicineIds is empty
        if (medicineIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Get all medicines
        List<Medicine> medicines = medicineRepository.findAllById(medicineIds);
        
        // Get all stocks for these medicines
        List<Stock> allStocks = stockRepository.findAll();
        List<Stock> relevantStocks = allStocks.stream()
                .filter(stock -> medicineIds.contains(stock.getMedicine().getId()))
                .collect(Collectors.toList());
        
        // Filter out medicines that are not available from any vendor
        Set<Long> availableMedicineIds = relevantStocks.stream()
                .map(stock -> stock.getMedicine().getId())
                .collect(Collectors.toSet());
        
        // Identify unavailable medicines
        Set<Long> unavailableMedicineIds = medicineIds.stream()
                .filter(id -> !availableMedicineIds.contains(id))
                .collect(Collectors.toSet());
        
        // Log unavailable medicines
        if (!unavailableMedicineIds.isEmpty()) {
            System.out.println("Unavailable medicines: " + unavailableMedicineIds);
            for (Long medicineId : unavailableMedicineIds) {
                System.out.println("Medicine ID " + medicineId + " is not available from any vendor");
            }
        }
        
        // Remove medicines that are not available from any vendor
        medicineIds.removeIf(id -> !availableMedicineIds.contains(id));
        medicineQuantities.entrySet().removeIf(entry -> !availableMedicineIds.contains(entry.getKey()));
        
        // Update the medicines list to only include available medicines
        medicines = medicines.stream()
                .filter(medicine -> availableMedicineIds.contains(medicine.getId()))
                .collect(Collectors.toList());
        
        // If no medicines are available, return empty list
        if (medicineIds.isEmpty()) {
            System.out.println("No medicines are available from any vendor");
            return new ArrayList<>();
        }
        
        // Group stocks by vendor
        Map<Long, List<Stock>> stocksByVendor = relevantStocks.stream()
                .collect(Collectors.groupingBy(stock -> stock.getVendor().getId()));
        
        // For direct medicine requests, we'll create buckets for all vendors that have the medicines
        // (different from preorder where we only consider user-selected vendors)
        List<BucketDTO> buckets = new ArrayList<>();
        
        for (Map.Entry<Long, List<Stock>> entry : stocksByVendor.entrySet()) {
            Long vendorId = entry.getKey();
            List<Stock> vendorStocks = entry.getValue();
            
            // Create a bucket for this vendor with available medicines only
            BucketDTO bucket = createBucketForVendorWithPartialAvailability(vendorId, vendorStocks, medicines, medicineQuantities, unavailableMedicineIds);
            if (bucket != null && (!bucket.getAvailableItems().isEmpty() || !bucket.getUnavailableItems().isEmpty())) {
                buckets.add(bucket);
            }
        }
        
        // Sort buckets by total price
        buckets.sort(Comparator.comparingDouble(BucketDTO::getTotalPrice));
        
        return buckets;
    }
    
    @Override
    public List<BucketDTO> createOptimizedBucketsFromPreorder(VandorCartResponseDTO preorderData) {
        // Extract medicine IDs and quantities from preorder data
        Map<Long, Integer> medicineQuantities = new HashMap<>();
        List<Long> medicineIds = new ArrayList<>();
        Set<Long> selectedVendorIds = new HashSet<>(); // Track vendors selected by user
        
        System.out.println("Processing preorder data with " + preorderData.getCarts().size() + " carts");
        
        for (CartResponseDTO cart : preorderData.getCarts()) {
            System.out.println("Processing cart with vendor ID: " + cart.getVendorId() + " and " + cart.getMedicine().size() + " medicines");
            // Track which vendors were selected by the user
            selectedVendorIds.add(cart.getVendorId());
            
            for (MedicineDTO medicine : cart.getMedicine()) {
                Long medicineId = medicine.getId();
                int quantity = medicine.getQty();
                
                System.out.println("Medicine ID: " + medicineId + ", Quantity: " + quantity);
                
                // If we already have this medicine, add the quantity
                if (medicineQuantities.containsKey(medicineId)) {
                    quantity += medicineQuantities.get(medicineId);
                }
                
                medicineQuantities.put(medicineId, quantity);
                if (!medicineIds.contains(medicineId)) {
                    medicineIds.add(medicineId);
                }
            }
        }
        
        System.out.println("Total unique medicines: " + medicineIds.size());
        System.out.println("Medicine quantities: " + medicineQuantities);
        System.out.println("User selected vendors: " + selectedVendorIds);
        
        // Check if medicineIds is empty
        if (medicineIds.isEmpty()) {
            System.out.println("No medicines found in preorder");
            return new ArrayList<>();
        }
        
        // Get all medicines
        List<Medicine> medicines = medicineRepository.findAllById(medicineIds);
        System.out.println("Found " + medicines.size() + " medicines in database");
        
        // Get all stocks for these medicines
        List<Stock> allStocks = stockRepository.findAll();
        List<Stock> relevantStocks = allStocks.stream()
                .filter(stock -> medicineIds.contains(stock.getMedicine().getId()))
                .collect(Collectors.toList());
        
        System.out.println("Found " + relevantStocks.size() + " relevant stocks");
        
        // Filter out medicines that are not available from any vendor
        Set<Long> availableMedicineIds = relevantStocks.stream()
                .map(stock -> stock.getMedicine().getId())
                .collect(Collectors.toSet());
        
        // Identify unavailable medicines
        Set<Long> unavailableMedicineIds = medicineIds.stream()
                .filter(id -> !availableMedicineIds.contains(id))
                .collect(Collectors.toSet());
        
        // Log unavailable medicines
        if (!unavailableMedicineIds.isEmpty()) {
            System.out.println("Unavailable medicines: " + unavailableMedicineIds);
            for (Long medicineId : unavailableMedicineIds) {
                System.out.println("Medicine ID " + medicineId + " is not available from any vendor");
            }
        }
        
        // Remove medicines that are not available from any vendor
        medicineIds.removeIf(id -> !availableMedicineIds.contains(id));
        medicineQuantities.entrySet().removeIf(entry -> !availableMedicineIds.contains(entry.getKey()));
        
        // Update the medicines list to only include available medicines
        medicines = medicines.stream()
                .filter(medicine -> availableMedicineIds.contains(medicine.getId()))
                .collect(Collectors.toList());
        
        System.out.println("After filtering, " + medicineIds.size() + " medicines are available from at least one vendor");
        System.out.println("Available medicine quantities: " + medicineQuantities);
        
        // If no medicines are available, return empty list
        if (medicineIds.isEmpty()) {
            System.out.println("No medicines are available from any vendor");
            return new ArrayList<>();
        }
        
        // Group stocks by vendor
        Map<Long, List<Stock>> stocksByVendor = relevantStocks.stream()
                .collect(Collectors.groupingBy(stock -> stock.getVendor().getId()));
        
        System.out.println("Stocks grouped by " + stocksByVendor.size() + " vendors");
        
        // Create buckets only for vendors selected by the user
        List<BucketDTO> buckets = new ArrayList<>();
        
        for (Map.Entry<Long, List<Stock>> entry : stocksByVendor.entrySet()) {
            Long vendorId = entry.getKey();
            List<Stock> vendorStocks = entry.getValue();
            
            // Only create buckets for vendors selected by the user
            if (selectedVendorIds.contains(vendorId)) {
                System.out.println("Creating bucket for user-selected vendor ID: " + vendorId);
                // Create a bucket for this vendor with available medicines only
                BucketDTO bucket = createBucketForVendorWithPartialAvailability(vendorId, vendorStocks, medicines, medicineQuantities, unavailableMedicineIds);
                if (bucket != null && (!bucket.getAvailableItems().isEmpty() || !bucket.getUnavailableItems().isEmpty())) {
                    buckets.add(bucket);
                }
            } else {
                System.out.println("Skipping vendor ID: " + vendorId + " (not selected by user)");
            }
        }
        
        System.out.println("Created " + buckets.size() + " buckets");
        
        // Sort buckets by total price
        buckets.sort(Comparator.comparingDouble(BucketDTO::getTotalPrice));
        
        return buckets;
    }
    
    private BucketDTO createBucketForVendorWithPartialAvailability(Long vendorId, List<Stock> vendorStocks, List<Medicine> medicines, Map<Long, Integer> medicineQuantities, Set<Long> unavailableMedicineIds) {
        System.out.println("Creating bucket for vendor ID: " + vendorId + " with " + medicines.size() + " medicines");
        
        // Check if medicines list is empty
        if (medicines.isEmpty()) {
            System.out.println("No medicines to create bucket for vendor ID: " + vendorId);
            return null;
        }
        
        BucketDTO bucket = new BucketDTO();
        bucket.setId(vendorId); // Use vendor ID as bucket ID
        Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
        bucket.setName(vendor != null ? "Complete bucket from " + vendor.getName() : "Complete bucket from vendor " + vendorId);
        bucket.setVendorId(vendorId);
        bucket.setVendorName(vendor != null ? vendor.getName() : "");
        
        List<BucketItemDTO> availableItems = new ArrayList<>();
        List<UnavailableMedicineDTO> unavailableItems = new ArrayList<>();
        double totalPrice = 0.0;
        double totalDiscount = 0.0; // Track total discount
        
        // Process available medicines
        for (Medicine medicine : medicines) {
            Long medicineId = medicine.getId();
            int requestedQuantity = medicineQuantities.get(medicineId);
            
            System.out.println("Processing medicine ID: " + medicineId + ", requested quantity: " + requestedQuantity);
            
            // Find the stock for this medicine from this vendor
            Optional<Stock> stockOptional = vendorStocks.stream()
                    .filter(s -> s.getMedicine().getId() == medicineId)
                    .findFirst();
            
            if (stockOptional.isPresent()) {
                Stock stock = stockOptional.get();
                
                System.out.println("Found stock for medicine ID: " + medicineId + " with quantity: " + stock.getQty() + ", MRP: " + stock.getMrp() + ", discount: " + stock.getDiscount());
                
                // Check if vendor has enough quantity
                if (stock.getQty() >= requestedQuantity) {
                    BucketItemDTO item = new BucketItemDTO();
                    item.setMedicineId(medicineId);
                    item.setMedicineName(medicine.getName());
                    item.setVendorId(vendorId);
                    item.setVendorName(vendor != null ? vendor.getName() : "");
                    item.setPrice(calculateUnitPrice(stock.getMrp(), stock.getDiscount()));
                    item.setDiscount(stock.getDiscount());
                    item.setAvailableQuantity(stock.getQty());
                    item.setRequestedQuantity(requestedQuantity);
                    double itemTotalPrice = calculateTotalPrice(stock.getMrp(), stock.getDiscount(), requestedQuantity);
                    item.setTotalPrice(itemTotalPrice);
                    
                    availableItems.add(item);
                    totalPrice += itemTotalPrice;
                    // Calculate discount amount for this item and add to total discount
                    double itemDiscountAmount = (stock.getMrp() * stock.getDiscount() / 100) * requestedQuantity;
                    totalDiscount += itemDiscountAmount;
                    
                    System.out.println("Added item to bucket - total price so far: " + totalPrice + ", total discount so far: " + totalDiscount);
                } else {
                    // Vendor doesn't have enough quantity, add to unavailable items
                    System.out.println("Vendor doesn't have enough quantity for medicine ID: " + medicineId + " (required: " + requestedQuantity + ", available: " + stock.getQty() + ")");
                    UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                    unavailableItem.setMedicineId(medicineId);
                    unavailableItem.setMedicineName(medicine.getName() + " (Insufficient quantity available)");
                    unavailableItem.setRequestedQuantity(requestedQuantity);
                    // Get substitutes for this medicine
                    try {
                        List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
                        unavailableItem.setSubstitutes(substitutes);
                    } catch (Exception e) {
                        System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
                    }
                    unavailableItems.add(unavailableItem);
                }
            } else {
                // Vendor doesn't have this medicine, add to unavailable items
                System.out.println("Vendor doesn't have medicine ID: " + medicineId);
                UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                unavailableItem.setMedicineId(medicineId);
                unavailableItem.setMedicineName(medicine.getName() + " (Not available from this vendor)");
                unavailableItem.setRequestedQuantity(requestedQuantity);
                // Get substitutes for this medicine
                try {
                    List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
                    unavailableItem.setSubstitutes(substitutes);
                } catch (Exception e) {
                    System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
                }
                unavailableItems.add(unavailableItem);
            }
        }
        
        // Add unavailable medicines to the bucket with appropriate information
        for (Long unavailableMedicineId : unavailableMedicineIds) {
            Optional<Medicine> medicineOpt = medicines.stream()
                    .filter(m -> m.getId() == unavailableMedicineId)
                    .findFirst();
            
            if (medicineOpt.isPresent()) {
                Medicine medicine = medicineOpt.get();
                int requestedQuantity = medicineQuantities.getOrDefault(unavailableMedicineId, 0);
                
                UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                unavailableItem.setMedicineId(unavailableMedicineId);
                unavailableItem.setMedicineName(medicine.getName() + " (Not available from any vendor)");
                unavailableItem.setRequestedQuantity(requestedQuantity);
                // Get substitutes for this medicine
                try {
                    List<SubstituteDetailView> substitutes = substituteService.findSubstitute(unavailableMedicineId);
                    unavailableItem.setSubstitutes(substitutes);
                } catch (Exception e) {
                    System.out.println("Error fetching substitutes for medicine ID: " + unavailableMedicineId + ", error: " + e.getMessage());
                }
                unavailableItems.add(unavailableItem);
            }
        }
        
        System.out.println("Vendor bucket items - available: " + availableItems.size() + ", unavailable: " + unavailableItems.size());
        
        bucket.setAvailableItems(availableItems);
        bucket.setUnavailableItems(unavailableItems);
        bucket.setTotalPrice(totalPrice);
        bucket.setTotalDiscount(totalDiscount); // Set the total discount
        
        System.out.println("Returning vendor bucket with total price: " + totalPrice + ", total discount: " + totalDiscount);
        return bucket;
    }
    
    private BucketDTO createMixedVendorBucketWithPartialAvailability(List<Medicine> medicines, List<Stock> allStocks, Map<Long, Integer> medicineQuantities, Set<Long> unavailableMedicineIds) {
        System.out.println("Creating mixed vendor bucket for " + medicines.size() + " medicines");
        
        // Check if medicines list is empty
        if (medicines.isEmpty()) {
            System.out.println("No medicines to create mixed vendor bucket");
            return null;
        }
        
        List<BucketItemDTO> availableItems = new ArrayList<>();
        List<UnavailableMedicineDTO> unavailableItems = new ArrayList<>();
        double totalPrice = 0.0;
        double totalDiscount = 0.0; // Track total discount
        
        // Process available medicines
        for (Medicine medicine : medicines) {
            Long medicineId = medicine.getId();
            int requestedQuantity = medicineQuantities.get(medicineId);
            
            System.out.println("Processing medicine ID: " + medicineId + ", requested quantity: " + requestedQuantity);
            
            // Find the best price for this medicine across all vendors
            List<Stock> medicineStocks = allStocks.stream()
                    .filter(stock -> stock.getMedicine().getId() == medicineId)
                    .collect(Collectors.toList());
            
            System.out.println("Found " + medicineStocks.size() + " stocks for medicine ID: " + medicineId);
            
            if (!medicineStocks.isEmpty()) {
                // Filter stocks that have enough quantity
                List<Stock> sufficientStocks = medicineStocks.stream()
                        .filter(stock -> stock.getQty() >= requestedQuantity)
                        .collect(Collectors.toList());
                
                System.out.println("Found " + sufficientStocks.size() + " stocks with sufficient quantity");
                
                if (!sufficientStocks.isEmpty()) {
                    // Find the stock with the lowest final price
                    Optional<Stock> bestStockOptional = sufficientStocks.stream()
                            .min(Comparator.comparingDouble(stock -> calculateTotalPrice(stock.getMrp(), stock.getDiscount(), requestedQuantity)));
                    
                    if (bestStockOptional.isPresent()) {
                        Stock bestStock = bestStockOptional.get();
                        Vendor vendor = bestStock.getVendor();
                        
                        System.out.println("Selected best stock from vendor ID: " + vendor.getId() + " with price: " + bestStock.getMrp() + ", discount: " + bestStock.getDiscount());
                        
                        BucketItemDTO item = new BucketItemDTO();
                        item.setMedicineId(medicineId);
                        item.setMedicineName(medicine.getName());
                        item.setVendorId(vendor.getId());
                        item.setVendorName(vendor.getName());
                        item.setPrice(calculateUnitPrice(bestStock.getMrp(), bestStock.getDiscount()));
                        item.setDiscount(bestStock.getDiscount());
                        item.setAvailableQuantity(bestStock.getQty());
                        item.setRequestedQuantity(requestedQuantity);
                        double itemTotalPrice = calculateTotalPrice(bestStock.getMrp(), bestStock.getDiscount(), requestedQuantity);
                        item.setTotalPrice(itemTotalPrice);
                        
                        availableItems.add(item);
                        totalPrice += itemTotalPrice;
                        // Calculate discount amount for this item and add to total discount
                        double itemDiscountAmount = (bestStock.getMrp() * bestStock.getDiscount() / 100) * requestedQuantity;
                        totalDiscount += itemDiscountAmount;
                    }
                } else {
                    // No stock with sufficient quantity, add to unavailable items
                    System.out.println("No vendor has sufficient quantity for medicine ID: " + medicineId);
                    Stock bestStock = medicineStocks.get(0); // Just take the first one for info
                    Vendor vendor = bestStock.getVendor();
                    
                    UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                    unavailableItem.setMedicineId(medicineId);
                    unavailableItem.setMedicineName(medicine.getName() + " (Insufficient quantity available)");
                    unavailableItem.setRequestedQuantity(requestedQuantity);
                    // Get substitutes for this medicine
                    try {
                        List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
                        unavailableItem.setSubstitutes(substitutes);
                    } catch (Exception e) {
                        System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
                    }
                    unavailableItems.add(unavailableItem);
                }
            } else {
                // Medicine not available from any vendor, add to unavailable items
                System.out.println("Medicine ID: " + medicineId + " not available from any vendor");
                UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                unavailableItem.setMedicineId(medicineId);
                unavailableItem.setMedicineName(medicine.getName() + " (Not available from any vendor)");
                unavailableItem.setRequestedQuantity(requestedQuantity);
                // Get substitutes for this medicine
                try {
                    List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
                    unavailableItem.setSubstitutes(substitutes);
                } catch (Exception e) {
                    System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
                }
                unavailableItems.add(unavailableItem);
            }
        }
        
        // Add unavailable medicines to the bucket with appropriate information
        for (Long unavailableMedicineId : unavailableMedicineIds) {
            Optional<Medicine> medicineOpt = medicines.stream()
                    .filter(m -> m.getId() == unavailableMedicineId)
                    .findFirst();
            
            if (medicineOpt.isPresent()) {
                Medicine medicine = medicineOpt.get();
                int requestedQuantity = medicineQuantities.getOrDefault(unavailableMedicineId, 0);
                
                UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                unavailableItem.setMedicineId(unavailableMedicineId);
                unavailableItem.setMedicineName(medicine.getName() + " (Not available from any vendor)");
                unavailableItem.setRequestedQuantity(requestedQuantity);
                // Get substitutes for this medicine
                try {
                    List<SubstituteDetailView> substitutes = substituteService.findSubstitute(unavailableMedicineId);
                    unavailableItem.setSubstitutes(substitutes);
                } catch (Exception e) {
                    System.out.println("Error fetching substitutes for medicine ID: " + unavailableMedicineId + ", error: " + e.getMessage());
                }
                unavailableItems.add(unavailableItem);
            }
        }
        
        System.out.println("Mixed vendor bucket items - available: " + availableItems.size() + ", unavailable: " + unavailableItems.size());
        
        // Create bucket even if we don't have all medicines
        if (!availableItems.isEmpty() || !unavailableItems.isEmpty()) {
            BucketDTO bucket = new BucketDTO();
            bucket.setId(System.currentTimeMillis()); // Unique ID for mixed bucket
            bucket.setName("Best price mixed vendor bucket");
            bucket.setAvailableItems(availableItems);
            bucket.setUnavailableItems(unavailableItems);
            bucket.setTotalPrice(totalPrice);
            bucket.setTotalDiscount(totalDiscount); // Set the total discount
            System.out.println("Returning mixed vendor bucket with total price: " + totalPrice + ", total discount: " + totalDiscount);
            return bucket;
        }
        
        System.out.println("Not returning mixed vendor bucket - no items");
        return null;
    }
    
    private BucketDTO createBucketForVendor(Long vendorId, List<Stock> vendorStocks, List<Medicine> medicines, Map<Long, Integer> medicineQuantities) {
        System.out.println("Creating bucket for vendor ID: " + vendorId + " with " + medicines.size() + " medicines");
        
        // Check if medicines list is empty
        if (medicines.isEmpty()) {
            System.out.println("No medicines to create bucket for vendor ID: " + vendorId);
            return null;
        }
        
        BucketDTO bucket = new BucketDTO();
        bucket.setId(vendorId); // Use vendor ID as bucket ID
        Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
        bucket.setName(vendor != null ? "Complete bucket from " + vendor.getName() : "Complete bucket from vendor " + vendorId);
        bucket.setVendorId(vendorId);
        bucket.setVendorName(vendor != null ? vendor.getName() : "");
        
        List<BucketItemDTO> availableItems = new ArrayList<>();
        List<UnavailableMedicineDTO> unavailableItems = new ArrayList<>();
        double totalPrice = 0.0;
        double totalDiscount = 0.0; // Track total discount
        
        for (Medicine medicine : medicines) {
            Long medicineId = medicine.getId();
            int requestedQuantity = medicineQuantities.get(medicineId);
            
            System.out.println("Processing medicine ID: " + medicineId + ", requested quantity: " + requestedQuantity);
            
            // Find the stock for this medicine from this vendor
            Optional<Stock> stockOptional = vendorStocks.stream()
                    .filter(s -> s.getMedicine().getId() == medicineId)
                    .findFirst();
            
            if (stockOptional.isPresent()) {
                Stock stock = stockOptional.get();
                
                System.out.println("Found stock for medicine ID: " + medicineId + " with quantity: " + stock.getQty() + ", MRP: " + stock.getMrp() + ", discount: " + stock.getDiscount());
                
                // Check if vendor has enough quantity
                if (stock.getQty() >= requestedQuantity) {
                    BucketItemDTO item = new BucketItemDTO();
                    item.setMedicineId(medicineId);
                    item.setMedicineName(medicine.getName());
                    item.setVendorId(vendorId);
                    item.setVendorName(vendor != null ? vendor.getName() : "");
                    item.setPrice(calculateUnitPrice(stock.getMrp(), stock.getDiscount()));
                    item.setDiscount(stock.getDiscount());
                    item.setAvailableQuantity(stock.getQty());
                    item.setRequestedQuantity(requestedQuantity);
                    double itemTotalPrice = calculateTotalPrice(stock.getMrp(), stock.getDiscount(), requestedQuantity);
                    item.setTotalPrice(itemTotalPrice);
                    
                    availableItems.add(item);
                    totalPrice += itemTotalPrice;
                    // Calculate discount amount for this item and add to total discount
                    double itemDiscountAmount = (stock.getMrp() * stock.getDiscount() / 100) * requestedQuantity;
                    totalDiscount += itemDiscountAmount;
                    
                    System.out.println("Added item to bucket - total price so far: " + totalPrice + ", total discount so far: " + totalDiscount);
                } else {
                    // Vendor doesn't have enough quantity, add to unavailable items
                    System.out.println("Vendor doesn't have enough quantity for medicine ID: " + medicineId + " (required: " + requestedQuantity + ", available: " + stock.getQty() + ")");
                    UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                    unavailableItem.setMedicineId(medicineId);
                    unavailableItem.setMedicineName(medicine.getName() + " (Insufficient quantity available)");
                    unavailableItem.setRequestedQuantity(requestedQuantity);
                    // Get substitutes for this medicine
                    try {
                        List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
                        unavailableItem.setSubstitutes(substitutes);
                    } catch (Exception e) {
                        System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
                    }
                    unavailableItems.add(unavailableItem);
                }
            } else {
                // Vendor doesn't have this medicine, add to unavailable items
                System.out.println("Vendor doesn't have medicine ID: " + medicineId);
                UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                unavailableItem.setMedicineId(medicineId);
                unavailableItem.setMedicineName(medicine.getName() + " (Not available from this vendor)");
                unavailableItem.setRequestedQuantity(requestedQuantity);
                // Get substitutes for this medicine
                try {
                    List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
                    unavailableItem.setSubstitutes(substitutes);
                } catch (Exception e) {
                    System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
                }
                unavailableItems.add(unavailableItem);
            }
        }
        
        System.out.println("Vendor bucket items - available: " + availableItems.size() + ", unavailable: " + unavailableItems.size());
        
        bucket.setAvailableItems(availableItems);
        bucket.setUnavailableItems(unavailableItems);
        bucket.setTotalPrice(totalPrice);
        bucket.setTotalDiscount(totalDiscount); // Set the total discount
        
        System.out.println("Returning vendor bucket with total price: " + totalPrice + ", total discount: " + totalDiscount);
        return bucket;
    }
    
    private BucketDTO createMixedVendorBucket(List<Medicine> medicines, List<Stock> allStocks, Map<Long, Integer> medicineQuantities) {
        System.out.println("Creating mixed vendor bucket for " + medicines.size() + " medicines");
        
        // Check if medicines list is empty
        if (medicines.isEmpty()) {
            System.out.println("No medicines to create mixed vendor bucket");
            return null;
        }
        
        List<BucketItemDTO> availableItems = new ArrayList<>();
        List<UnavailableMedicineDTO> unavailableItems = new ArrayList<>();
        double totalPrice = 0.0;
        double totalDiscount = 0.0; // Track total discount
        
        for (Medicine medicine : medicines) {
            Long medicineId = medicine.getId();
            int requestedQuantity = medicineQuantities.get(medicineId);
            
            System.out.println("Processing medicine ID: " + medicineId + ", requested quantity: " + requestedQuantity);
            
            // Find the best price for this medicine across all vendors
            List<Stock> medicineStocks = allStocks.stream()
                    .filter(stock -> stock.getMedicine().getId() == medicineId)
                    .collect(Collectors.toList());
            
            System.out.println("Found " + medicineStocks.size() + " stocks for medicine ID: " + medicineId);
            
            if (!medicineStocks.isEmpty()) {
                // Filter stocks that have enough quantity
                List<Stock> sufficientStocks = medicineStocks.stream()
                        .filter(stock -> stock.getQty() >= requestedQuantity)
                        .collect(Collectors.toList());
                
                System.out.println("Found " + sufficientStocks.size() + " stocks with sufficient quantity");
                
                if (!sufficientStocks.isEmpty()) {
                    // Find the stock with the lowest final price
                    Optional<Stock> bestStockOptional = sufficientStocks.stream()
                            .min(Comparator.comparingDouble(stock -> calculateTotalPrice(stock.getMrp(), stock.getDiscount(), requestedQuantity)));
                    
                    if (bestStockOptional.isPresent()) {
                        Stock bestStock = bestStockOptional.get();
                        Vendor vendor = bestStock.getVendor();
                        
                        System.out.println("Selected best stock from vendor ID: " + vendor.getId() + " with price: " + bestStock.getMrp() + ", discount: " + bestStock.getDiscount());
                        
                        BucketItemDTO item = new BucketItemDTO();
                        item.setMedicineId(medicineId);
                        item.setMedicineName(medicine.getName());
                        item.setVendorId(vendor.getId());
                        item.setVendorName(vendor.getName());
                        item.setPrice(calculateUnitPrice(bestStock.getMrp(), bestStock.getDiscount()));
                        item.setDiscount(bestStock.getDiscount());
                        item.setAvailableQuantity(bestStock.getQty());
                        item.setRequestedQuantity(requestedQuantity);
                        double itemTotalPrice = calculateTotalPrice(bestStock.getMrp(), bestStock.getDiscount(), requestedQuantity);
                        item.setTotalPrice(itemTotalPrice);
                        
                        availableItems.add(item);
                        totalPrice += itemTotalPrice;
                        // Calculate discount amount for this item and add to total discount
                        double itemDiscountAmount = (bestStock.getMrp() * bestStock.getDiscount() / 100) * requestedQuantity;
                        totalDiscount += itemDiscountAmount;
                    }
                } else {
                    // No stock with sufficient quantity, add to unavailable items
                    System.out.println("No vendor has sufficient quantity for medicine ID: " + medicineId);
                    Stock bestStock = medicineStocks.get(0); // Just take the first one for info
                    Vendor vendor = bestStock.getVendor();
                    
                    UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                    unavailableItem.setMedicineId(medicineId);
                    unavailableItem.setMedicineName(medicine.getName() + " (Insufficient quantity available)");
                    unavailableItem.setRequestedQuantity(requestedQuantity);
                    // Get substitutes for this medicine
                    try {
                        List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
                        unavailableItem.setSubstitutes(substitutes);
                    } catch (Exception e) {
                        System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
                    }
                    unavailableItems.add(unavailableItem);
                }
            } else {
                // Medicine not available from any vendor, add to unavailable items
                System.out.println("Medicine ID: " + medicineId + " not available from any vendor");
                UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                unavailableItem.setMedicineId(medicineId);
                unavailableItem.setMedicineName(medicine.getName() + " (Not available from any vendor)");
                unavailableItem.setRequestedQuantity(requestedQuantity);
                // Get substitutes for this medicine
                try {
                    List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
                    unavailableItem.setSubstitutes(substitutes);
                } catch (Exception e) {
                    System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
                }
                unavailableItems.add(unavailableItem);
            }
        }
        
        System.out.println("Mixed vendor bucket items - available: " + availableItems.size() + ", unavailable: " + unavailableItems.size());
        
        // Only return bucket if we have items
        if (!availableItems.isEmpty() || !unavailableItems.isEmpty()) {
            BucketDTO bucket = new BucketDTO();
            bucket.setId(System.currentTimeMillis()); // Unique ID for mixed bucket
            bucket.setName("Best price mixed vendor bucket");
            bucket.setAvailableItems(availableItems);
            bucket.setUnavailableItems(unavailableItems);
            bucket.setTotalPrice(totalPrice);
            bucket.setTotalDiscount(totalDiscount); // Set the total discount
            System.out.println("Returning mixed vendor bucket with total price: " + totalPrice + ", total discount: " + totalDiscount);
            return bucket;
        }
        
        System.out.println("Not returning mixed vendor bucket - no items");
        return null;
    }
    
    private double calculateUnitPrice(double mrp, double discount) {
        return mrp - (mrp * discount / 100);
    }
    
    private double calculateTotalPrice(double mrp, double discount, int quantity) {
        double unitPrice = calculateUnitPrice(mrp, discount);
        return unitPrice * quantity;
    }

    @Override
    public List<BucketDTO> getAllBuckets() {
        // This would typically retrieve saved buckets from a database
        // For now, we'll return an empty list as we create buckets on-demand
        return new ArrayList<>();
    }
}