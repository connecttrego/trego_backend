package com.trego.service.impl;

import com.google.gson.Gson;
import com.trego.dao.entity.Medicine;
import com.trego.dao.entity.PreOrder;
import com.trego.dao.entity.Stock;
import com.trego.dao.entity.Vendor;
import com.trego.dao.impl.MedicineRepository;
import com.trego.dao.impl.PreOrderRepository;
import com.trego.dao.impl.StockRepository;
import com.trego.dao.impl.VendorRepository;
import com.trego.dto.BucketDTO;
import com.trego.dto.BucketItemDTO;
import com.trego.dto.BucketRequestDTO;
import com.trego.dto.MedicineDTO;
import com.trego.dto.SelectedSubstituteDTO;
import com.trego.dto.response.CartResponseDTO;
import com.trego.dto.response.PreOrderResponseDTO;
import com.trego.dto.UnavailableMedicineDTO;
import com.trego.dto.response.VendorCartResponseDTO;
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
    private PreOrderRepository preOrderRepository;

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
        buckets.sort(Comparator.comparingDouble(BucketDTO::getAmountToPay));
        return buckets;
    }

    @Override
    public List<BucketDTO> createOptimizedBucketsFromPreorder(VendorCartResponseDTO preorderData) {
        Map<Long, Integer> medicineQuantities = new HashMap<>();
        List<Long> medicineIds = new ArrayList<>();
        Set<Long> selectedVendorIds = new HashSet<>();

        System.out.println("Processing preorder data with " + preorderData.getCarts().size() + " carts");

        for (CartResponseDTO cart : preorderData.getCarts()) {
            selectedVendorIds.add(cart.getVendorId());
            for (MedicineDTO medicine : cart.getMedicine()) {
                medicineQuantities.merge(medicine.getId(), medicine.getQty(), Integer::sum);
                if (!medicineIds.contains(medicine.getId())) medicineIds.add(medicine.getId());
            }
        }

        if (medicineIds.isEmpty()) return new ArrayList<>();

        List<Medicine> medicines = medicineRepository.findAllById(medicineIds);
        List<Stock> allStocks = stockRepository.findAll();

        List<Stock> relevantStocks = allStocks.stream()
                .filter(s -> medicineIds.contains(s.getMedicine().getId()))
                .collect(Collectors.toList());

        Map<Long, List<Stock>> stocksByVendor = relevantStocks.stream()
                .collect(Collectors.groupingBy(s -> s.getVendor().getId()));

        List<BucketDTO> buckets = new ArrayList<>();

        for (Map.Entry<Long, List<Stock>> entry : stocksByVendor.entrySet()) {
            Long vendorId = entry.getKey();
            if (selectedVendorIds.contains(vendorId)) {
                BucketDTO bucket = createBucketForVendorWithPartialAvailability(
                        vendorId, entry.getValue(), medicines, medicineQuantities, new HashSet<>());
                if (bucket != null && (!bucket.getAvailableItems().isEmpty() || !bucket.getUnavailableItems().isEmpty())) {
                    buckets.add(bucket);
                }
            }
        }

        // Sort buckets by total price
        buckets.sort(Comparator
                .comparingInt((BucketDTO b) -> b.getAvailableItems().size())   // 1. by item count
                .reversed()                                                    // 2. max first
                .thenComparingDouble(BucketDTO::getAmountToPay));              // 3. by amountToPay if tie

        System.out.println("Buckets sorted by available items count (desc) and amountToPay (asc)");
// Persist the selected (cheapest) vendor directly to PreOrder table
// NOTE: require a valid preOrderId (non-null and > 0)
        if (!buckets.isEmpty() && preorderData.getPreOrderId() != null && preorderData.getPreOrderId() > 0) {
            BucketDTO cheapestBucket = buckets.get(0);

            // set in response DTO for immediate return (so client sees it)
            preorderData.setSelectedVendorId(cheapestBucket.getVendorId());

            try {
                PreOrder preOrder = preOrderRepository.findById(preorderData.getPreOrderId()).orElse(null);
                if (preOrder != null) {
                    preOrder.setSelectedVendorId(cheapestBucket.getVendorId());

                    // Optional: update payload JSON for UI consistency
                    try {
                        Gson gson = new Gson();
                        PreOrderResponseDTO responsePayload = gson.fromJson(preOrder.getPayload(), PreOrderResponseDTO.class);
                        if (responsePayload == null) responsePayload = new PreOrderResponseDTO();
                        responsePayload.setSelectedVendorId(cheapestBucket.getVendorId());
                        responsePayload.setVendorName(cheapestBucket.getVendorName());
                        responsePayload.setVendorLogo(cheapestBucket.getLogo());
                        responsePayload.setAmountToPay(cheapestBucket.getAmountToPay());
                        preOrder.setPayload(gson.toJson(responsePayload));
                    } catch (Exception ex) {
                        System.out.println(" Could not update payload JSON: " + ex.getMessage());
                    }

                    preOrderRepository.save(preOrder);

                    System.out.println(" PreOrder ID " + preOrder.getId()
                            + " updated with selectedVendorId = " + cheapestBucket.getVendorId());
                } else {
                    System.out.println(" Could not find PreOrder with ID " + preorderData.getPreOrderId());
                }
            } catch (Exception e) {
                System.out.println(" Error updating PreOrder with vendor ID: " + e.getMessage());
            }
        } else {
            System.out.println("Skipping PreOrder DB update - missing valid preOrderId in vendorCartResponse");
        }


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
        bucket.setLogo(vendor != null ? vendor.getLogo() : "");

        List<BucketItemDTO> availableItems = new ArrayList<>();
        List<UnavailableMedicineDTO> unavailableItems = new ArrayList<>();
        String deliveryTime = "1 hrs extra";
        double totalPrice = 0.0;
        double deliveryCharges = 0.0;
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
                    item.setMedicineImage(medicine.getPhoto1());
                    item.setMedicineStrip(medicine.getPacking());
                    //item.setVendorId(vendorId);
                    //item.setVendorName(vendor != null ? vendor.getName() : "");
                    item.setMrp(stock.getMrp());
                    item.setPrice(calculateUnitPrice(stock.getMrp(), stock.getDiscount()));
                    item.setDiscount(stock.getDiscount());
                    item.setAvailableQuantity(stock.getQty());
                    item.setRequestedQuantity(requestedQuantity);
                    double itemTotalPrice = calculateTotalPrice(stock.getMrp(), 0, requestedQuantity);
                    double itemDiscountedPrice = calculateTotalPrice(stock.getMrp(), stock.getDiscount(), requestedQuantity);
                    item.setTotalPrice(itemTotalPrice);

                    availableItems.add(item);
                    totalPrice += itemTotalPrice;
                    // Calculate discount amount for this item and add to total discount
                    double itemDiscountAmount =itemTotalPrice - itemDiscountedPrice;
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
                unavailableItem.setMedicineName(medicine.getName());
                unavailableItem.setMedicineImage(medicine.getPhoto1());
                unavailableItem.setMedicineStrip(medicine.getPacking());
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
        bucket.setDeliveryTime(deliveryTime);
        bucket.setTotalDiscount(totalDiscount); // Set the total discount
        bucket.setDeliveryCharges(deliveryCharges);
        double amountToPay = totalPrice - totalDiscount + deliveryCharges;
        bucket.setAmountToPay(amountToPay);
        System.out.println("Returning vendor bucket with total price: " + totalPrice + ", total discount: " + totalDiscount);
        return bucket;
    }

    private BucketDTO createBucketForVendorWithSpecificQuantities(Long vendorId, List<Stock> vendorStocks, List<Medicine> medicines, Map<Long, Integer> medicineQuantities, Set<Long> unavailableMedicineIds) {
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
        bucket.setLogo(vendor != null ? vendor.getLogo() : "");
        bucket.setSelectedSubstitutes(new ArrayList<>()); // Initialize selected substitutes list

        List<BucketItemDTO> availableItems = new ArrayList<>();
        List<UnavailableMedicineDTO> unavailableItems = new ArrayList<>();
        List<SelectedSubstituteDTO> selectedSubstitutes = new ArrayList<>(); // Track selected substitutes
        String deliveryTime = "1 hrs extra";
        double totalPrice = 0.0;
        double deliveryCharges = 0.0;
        double totalDiscount = 0.0; // Track total discount

        // Process available medicines
        for (Medicine medicine : medicines) {
            Long medicineId = medicine.getId();
            
            // Check if this medicine is requested from this vendor
            if (!medicineQuantities.containsKey(medicineId)) {
                System.out.println("Medicine ID: " + medicineId + " not requested from vendor ID: " + vendorId);
                continue;
            }
            
            int requestedQuantity = medicineQuantities.get(medicineId);

            // Skip medicines with zero quantity
            if (requestedQuantity <= 0) {
                System.out.println("Skipping medicine ID: " + medicineId + " with zero or negative quantity: " + requestedQuantity);
                continue;
            }

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
                    item.setMedicineImage(medicine.getPhoto1());
                    item.setMedicineStrip(medicine.getPacking());
                    //item.setVendorId(vendorId);
                    //item.setVendorName(vendor != null ? vendor.getName() : "");
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
                    unavailableItem.setMedicineImage(medicine.getPhoto1());
                    unavailableItem.setMedicineStrip(medicine.getPacking());
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
                unavailableItem.setMedicineName(medicine.getName());
                unavailableItem.setMedicineImage(medicine.getPhoto1());
                unavailableItem.setMedicineStrip(medicine.getPacking());
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
            // Check if this unavailable medicine was requested from this vendor
            if (!medicineQuantities.containsKey(unavailableMedicineId)) {
                System.out.println("Unavailable medicine ID: " + unavailableMedicineId + " not requested from vendor ID: " + vendorId);
                continue;
            }
            
            int requestedQuantity = medicineQuantities.getOrDefault(unavailableMedicineId, 0);
            
            // Skip medicines with zero quantity
            if (requestedQuantity <= 0) {
                System.out.println("Skipping unavailable medicine ID: " + unavailableMedicineId + " with zero or negative quantity: " + requestedQuantity);
                continue;
            }

            Optional<Medicine> medicineOpt = medicines.stream()
                    .filter(m -> m.getId() == unavailableMedicineId)
                    .findFirst();

            if (medicineOpt.isPresent()) {
                Medicine medicine = medicineOpt.get();

                UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                unavailableItem.setMedicineId(unavailableMedicineId);
                unavailableItem.setMedicineName(medicine.getName() + " (Not available from any vendor)");
                unavailableItem.setRequestedQuantity(requestedQuantity);
                unavailableItem.setMedicineImage(medicine.getPhoto1());
                unavailableItem.setMedicineStrip(medicine.getPacking());
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
        bucket.setSelectedSubstitutes(selectedSubstitutes); // Set selected substitutes (initially empty)
        bucket.setTotalPrice(totalPrice);
        bucket.setDeliveryTime(deliveryTime);
        bucket.setTotalDiscount(totalDiscount); // Set the total discount
        bucket.setDeliveryCharges(deliveryCharges);
        double amountToPay = totalPrice - totalDiscount + deliveryCharges;
        bucket.setAmountToPay(amountToPay);
        System.out.println("Returning vendor bucket with total price: " + totalPrice + ", total discount: " + totalDiscount);
        return bucket;
    }

    /**
     * Add a selected substitute to a bucket and update the total amount
     * @param bucket The bucket to update
     * @param originalMedicineId The ID of the original unavailable medicine
     * @param substitute The substitute medicine details
     * @param quantity The quantity of the substitute to add
     * @return The updated bucket
     */
    public BucketDTO addSubstituteToBucket(BucketDTO bucket, Long originalMedicineId, SubstituteDetailView substitute, int quantity) {
        System.out.println("Adding substitute to bucket - original medicine ID: " + originalMedicineId + 
                          ", substitute ID: " + substitute.getId() + ", quantity: " + quantity);
        
        // Create a selected substitute DTO
        SelectedSubstituteDTO selectedSubstitute = new SelectedSubstituteDTO();
        selectedSubstitute.setOriginalMedicineId(originalMedicineId);
        selectedSubstitute.setSubstituteMedicineId(substitute.getId());
        selectedSubstitute.setSubstituteMedicineName(substitute.getName());
        selectedSubstitute.setQuantity(quantity);
        selectedSubstitute.setUnitPrice(substitute.getBestPrice().doubleValue());
        selectedSubstitute.setDiscount(substitute.getDiscount().doubleValue());
        
        // Calculate total price for this substitute
        double totalPrice = calculateTotalPrice(substitute.getBestPrice().doubleValue(), substitute.getDiscount().doubleValue(), quantity);
        selectedSubstitute.setTotalPrice(totalPrice);
        selectedSubstitute.setMedicineImage(substitute.getPhoto1());
        selectedSubstitute.setMedicineStrip(substitute.getPacking());
        
        // Add to the selected substitutes list
        bucket.getSelectedSubstitutes().add(selectedSubstitute);
        
        // Update bucket totals
        bucket.setTotalPrice(bucket.getTotalPrice() + totalPrice);
        double discountAmount = (substitute.getBestPrice().doubleValue() * substitute.getDiscount().doubleValue() / 100) * quantity;
        bucket.setTotalDiscount(bucket.getTotalDiscount() + discountAmount);
        bucket.setAmountToPay(bucket.getTotalPrice() - bucket.getTotalDiscount() + bucket.getDeliveryCharges());
        
        System.out.println("Updated bucket - total price: " + bucket.getTotalPrice() + 
                          ", total discount: " + bucket.getTotalDiscount() + 
                          ", amount to pay: " + bucket.getAmountToPay());
        
        return bucket;
    }

    /**
     * Remove a selected substitute from a bucket and update the total amount
     * @param bucket The bucket to update
     * @param substituteMedicineId The ID of the substitute medicine to remove
     * @return The updated bucket
     */
    public BucketDTO removeSubstituteFromBucket(BucketDTO bucket, Long substituteMedicineId) {
        System.out.println("Removing substitute from bucket - substitute ID: " + substituteMedicineId);
        
        // Find and remove the substitute
        SelectedSubstituteDTO substituteToRemove = null;
        for (SelectedSubstituteDTO substitute : bucket.getSelectedSubstitutes()) {
            if (substitute.getSubstituteMedicineId().equals(substituteMedicineId)) {
                substituteToRemove = substitute;
                break;
            }
        }
        
        if (substituteToRemove != null) {
            bucket.getSelectedSubstitutes().remove(substituteToRemove);
            
            // Update bucket totals
            bucket.setTotalPrice(bucket.getTotalPrice() - substituteToRemove.getTotalPrice());
            double discountAmount = (substituteToRemove.getUnitPrice() * substituteToRemove.getDiscount() / 100) * substituteToRemove.getQuantity();
            bucket.setTotalDiscount(bucket.getTotalDiscount() - discountAmount);
            bucket.setAmountToPay(bucket.getTotalPrice() - bucket.getTotalDiscount() + bucket.getDeliveryCharges());
            
            System.out.println("Updated bucket - total price: " + bucket.getTotalPrice() + 
                              ", total discount: " + bucket.getTotalDiscount() + 
                              ", amount to pay: " + bucket.getAmountToPay());
        }
        
        return bucket;
    }

//    private BucketDTO createMixedVendorBucketWithPartialAvailability(List<Medicine> medicines, List<Stock> allStocks, Map<Long, Integer> medicineQuantities, Set<Long> unavailableMedicineIds) {
//        System.out.println("Creating mixed vendor bucket for " + medicines.size() + " medicines");
//
//        // Check if medicines list is empty
//        if (medicines.isEmpty()) {
//            System.out.println("No medicines to create mixed vendor bucket");
//            return null;
//        }
//
//        List<BucketItemDTO> availableItems = new ArrayList<>();
//        List<UnavailableMedicineDTO> unavailableItems = new ArrayList<>();
//        double totalPrice = 0.0;
//        double totalDiscount = 0.0;
//        double deliveryCharges = 0.0;// Track total discount
//
//        // Process available medicines
//        for (Medicine medicine : medicines) {
//            Long medicineId = medicine.getId();
//            int requestedQuantity = medicineQuantities.get(medicineId);
//
//            System.out.println("Processing medicine ID: " + medicineId + ", requested quantity: " + requestedQuantity);
//
//            // Find the best price for this medicine across all vendors
//            List<Stock> medicineStocks = allStocks.stream()
//                    .filter(stock -> stock.getMedicine().getId() == medicineId)
//                    .collect(Collectors.toList());
//
//            System.out.println("Found " + medicineStocks.size() + " stocks for medicine ID: " + medicineId);
//
//            if (!medicineStocks.isEmpty()) {
//                // Filter stocks that have enough quantity
//                List<Stock> sufficientStocks = medicineStocks.stream()
//                        .filter(stock -> stock.getQty() >= requestedQuantity)
//                        .collect(Collectors.toList());
//
//                System.out.println("Found " + sufficientStocks.size() + " stocks with sufficient quantity");
//
//                if (!sufficientStocks.isEmpty()) {
//                    // Find the stock with the lowest final price
//                    Optional<Stock> bestStockOptional = sufficientStocks.stream()
//                            .min(Comparator.comparingDouble(stock -> calculateTotalPrice(stock.getMrp(), stock.getDiscount(), requestedQuantity)));
//
//                    if (bestStockOptional.isPresent()) {
//                        Stock bestStock = bestStockOptional.get();
//                        Vendor vendor = bestStock.getVendor();
//
//                        System.out.println("Selected best stock from vendor ID: " + vendor.getId() + " with price: " + bestStock.getMrp() + ", discount: " + bestStock.getDiscount());
//
//                        BucketItemDTO item = new BucketItemDTO();
//                        item.setMedicineId(medicineId);
//                        item.setMedicineName(medicine.getName());
//                        item.setMedicineImage(medicine.getPhoto1());
//                        item.setMedicineStrip(medicine.getPacking());
//                        //item.setVendorId(vendor.getId());
//                        //item.setVendorName(vendor.getName());
//                        item.setPrice(calculateUnitPrice(bestStock.getMrp(), bestStock.getDiscount()));
//                        item.setDiscount(bestStock.getDiscount());
//                        item.setAvailableQuantity(bestStock.getQty());
//                        item.setRequestedQuantity(requestedQuantity);
//                        double itemTotalPrice = calculateTotalPrice(bestStock.getMrp(), bestStock.getDiscount(), requestedQuantity);
//                        item.setTotalPrice(itemTotalPrice);
//
//                        availableItems.add(item);
//                        totalPrice += itemTotalPrice;
//                        // Calculate discount amount for this item and add to total discount
//                        double itemDiscountAmount = (bestStock.getMrp() * bestStock.getDiscount() / 100) * requestedQuantity;
//                        totalDiscount += itemDiscountAmount;
//                    }
//                } else {
//                    // No stock with sufficient quantity, add to unavailable items
//                    System.out.println("No vendor has sufficient quantity for medicine ID: " + medicineId);
//                    Stock bestStock = medicineStocks.get(0); // Just take the first one for info
//                    Vendor vendor = bestStock.getVendor();
//
//                    UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
//                    unavailableItem.setMedicineId(medicineId);
//                    unavailableItem.setMedicineName(medicine.getName() + " (Insufficient quantity available)");
//                    unavailableItem.setRequestedQuantity(requestedQuantity);
//                    // Get substitutes for this medicine
//                    try {
//                        List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
//                        unavailableItem.setSubstitutes(substitutes);
//                    } catch (Exception e) {
//                        System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
//                    }
//                    unavailableItems.add(unavailableItem);
//                }
//            } else {
//                // Medicine not available from any vendor, add to unavailable items
//                System.out.println("Medicine ID: " + medicineId + " not available from any vendor");
//                UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
//                unavailableItem.setMedicineId(medicineId);
//                unavailableItem.setMedicineName(medicine.getName() + " (Not available from any vendor)");
//                unavailableItem.setRequestedQuantity(requestedQuantity);
//                // Get substitutes for this medicine
//                try {
//                    List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
//                    unavailableItem.setSubstitutes(substitutes);
//                } catch (Exception e) {
//                    System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
//                }
//                unavailableItems.add(unavailableItem);
//            }
//        }
//
//        // Add unavailable medicines to the bucket with appropriate information
//        for (Long unavailableMedicineId : unavailableMedicineIds) {
//            Optional<Medicine> medicineOpt = medicines.stream()
//                    .filter(m -> m.getId() == unavailableMedicineId)
//                    .findFirst();
//
//            if (medicineOpt.isPresent()) {
//                Medicine medicine = medicineOpt.get();
//                int requestedQuantity = medicineQuantities.getOrDefault(unavailableMedicineId, 0);
//
//                UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
//                unavailableItem.setMedicineId(unavailableMedicineId);
//                unavailableItem.setMedicineName(medicine.getName() + " (Not available from any vendor)");
//                unavailableItem.setRequestedQuantity(requestedQuantity);
//                // Get substitutes for this medicine
//                try {
//                    List<SubstituteDetailView> substitutes = substituteService.findSubstitute(unavailableMedicineId);
//                    unavailableItem.setSubstitutes(substitutes);
//                } catch (Exception e) {
//                    System.out.println("Error fetching substitutes for medicine ID: " + unavailableMedicineId + ", error: " + e.getMessage());
//                }
//                unavailableItems.add(unavailableItem);
//            }
//        }
//
//        System.out.println("Mixed vendor bucket items - available: " + availableItems.size() + ", unavailable: " + unavailableItems.size());
//
//        // Create bucket even if we don't have all medicines
//        if (!availableItems.isEmpty() || !unavailableItems.isEmpty()) {
//            BucketDTO bucket = new BucketDTO();
//            bucket.setId(System.currentTimeMillis()); // Unique ID for mixed bucket
//            bucket.setName("Best price mixed vendor bucket");
//            bucket.setAvailableItems(availableItems);
//            bucket.setUnavailableItems(unavailableItems);
//            bucket.setTotalPrice(totalPrice);
//            bucket.setTotalDiscount(totalDiscount); // Set the total discount
//            bucket.setDeliveryCharges(deliveryCharges);
//            double amountToPay = totalPrice - totalDiscount + deliveryCharges;
//            bucket.setAmountToPay(amountToPay);
//            System.out.println("Returning mixed vendor bucket with total price: " + totalPrice + ", total discount: " + totalDiscount + ", amountToPay: " + amountToPay );
//            return bucket;
//        }
//
//        System.out.println("Not returning mixed vendor bucket - no items");
//        return null;
//    }

//    private BucketDTO createBucketForVendor(Long vendorId, List<Stock> vendorStocks, List<Medicine> medicines, Map<Long, Integer> medicineQuantities) {
//        System.out.println("Creating bucket for vendor ID: " + vendorId + " with " + medicines.size() + " medicines");
//
//        // Check if medicines list is empty
//        if (medicines.isEmpty()) {
//            System.out.println("No medicines to create bucket for vendor ID: " + vendorId);
//            return null;
//        }
//
//        BucketDTO bucket = new BucketDTO();
//        bucket.setId(vendorId); // Use vendor ID as bucket ID
//        Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
//        bucket.setName(vendor != null ? "Complete bucket from " + vendor.getName() : "Complete bucket from vendor " + vendorId);
//        bucket.setVendorId(vendorId);
//        bucket.setVendorName(vendor != null ? vendor.getName() : "");
//        bucket.setLogo(vendor != null ? vendor.getLogo() : "");
//
//        List<BucketItemDTO> availableItems = new ArrayList<>();
//        List<UnavailableMedicineDTO> unavailableItems = new ArrayList<>();
//        double totalPrice = 0.0;
//        double deliveryCharges = 0.0;
//        double totalDiscount = 0.0; // Track total discount
//
//        for (Medicine medicine : medicines) {
//            Long medicineId = medicine.getId();
//            int requestedQuantity = medicineQuantities.get(medicineId);
//
//            System.out.println("Processing medicine ID: " + medicineId + ", requested quantity: " + requestedQuantity);
//
//            // Find the stock for this medicine from this vendor
//            Optional<Stock> stockOptional = vendorStocks.stream()
//                    .filter(s -> s.getMedicine().getId() == medicineId)
//                    .findFirst();
//
//            if (stockOptional.isPresent()) {
//                Stock stock = stockOptional.get();
//
//                System.out.println("Found stock for medicine ID: " + medicineId + " with quantity: " + stock.getQty() + ", MRP: " + stock.getMrp() + ", discount: " + stock.getDiscount());
//
//                // Check if vendor has enough quantity
//                if (stock.getQty() >= requestedQuantity) {
//                    BucketItemDTO item = new BucketItemDTO();
//                    item.setMedicineId(medicineId);
//                    item.setMedicineName(medicine.getName());
//                    item.setMedicineImage(medicine.getPhoto1());
//                    item.setMedicineStrip(medicine.getPacking());
////                    item.setVendorId(vendorId);
////                    item.setVendorName(vendor != null ? vendor.getName() : "");
//                    item.setPrice(calculateUnitPrice(stock.getMrp(), stock.getDiscount()));
//                    item.setDiscount(stock.getDiscount());
//                    item.setAvailableQuantity(stock.getQty());
//                    item.setRequestedQuantity(requestedQuantity);
//                    double itemTotalPrice = calculateTotalPrice(stock.getMrp(), stock.getDiscount(), requestedQuantity);
//                    item.setTotalPrice(itemTotalPrice);
//
//                    availableItems.add(item);
//                    totalPrice += itemTotalPrice;
//                    // Calculate discount amount for this item and add to total discount
//                    double itemDiscountAmount = (stock.getMrp() * stock.getDiscount() / 100) * requestedQuantity;
//                    totalDiscount += itemDiscountAmount;
//
//                    System.out.println("Added item to bucket - total price so far: " + totalPrice + ", total discount so far: " + totalDiscount);
//                } else {
//                    // Vendor doesn't have enough quantity, add to unavailable items
//                    System.out.println("Vendor doesn't have enough quantity for medicine ID: " + medicineId + " (required: " + requestedQuantity + ", available: " + stock.getQty() + ")");
//                    UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
//                    unavailableItem.setMedicineId(medicineId);
//                    unavailableItem.setMedicineName(medicine.getName() + " (Insufficient quantity available)");
//                    unavailableItem.setRequestedQuantity(requestedQuantity);
//                    // Get substitutes for this medicine
//                    try {
//                        List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
//                        unavailableItem.setSubstitutes(substitutes);
//                    } catch (Exception e) {
//                        System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
//                    }
//                    unavailableItems.add(unavailableItem);
//                }
//            } else {
//                // Vendor doesn't have this medicine, add to unavailable items
//                System.out.println("Vendor doesn't have medicine ID: " + medicineId);
//                UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
//                unavailableItem.setMedicineId(medicineId);
//                unavailableItem.setMedicineName(medicine.getName());
//                unavailableItem.setRequestedQuantity(requestedQuantity);
//                // Get substitutes for this medicine
//                try {
//                    List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
//                    unavailableItem.setSubstitutes(substitutes);
//                } catch (Exception e) {
//                    System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
//                }
//                unavailableItems.add(unavailableItem);
//            }
//        }
//
//        System.out.println("Vendor bucket items - available: " + availableItems.size() + ", unavailable: " + unavailableItems.size());
//
//        bucket.setAvailableItems(availableItems);
//        bucket.setUnavailableItems(unavailableItems);
//        bucket.setTotalPrice(totalPrice);
//        bucket.setTotalDiscount(totalDiscount); // Set the total discount
//        bucket.setDeliveryCharges(deliveryCharges);
//        double amountToPay = totalPrice - totalDiscount + deliveryCharges;
//        bucket.setAmountToPay(amountToPay);
//        System.out.println("Returning vendor bucket with total price: " + totalPrice + ", total discount: " + totalDiscount);
//        return bucket;
//    }

//    private BucketDTO createMixedVendorBucket(List<Medicine> medicines, List<Stock> allStocks, Map<Long, Integer> medicineQuantities) {
//        System.out.println("Creating mixed vendor bucket for " + medicines.size() + " medicines");
//
//        // Check if medicines list is empty
//        if (medicines.isEmpty()) {
//            System.out.println("No medicines to create mixed vendor bucket");
//            return null;
//        }
//
//        List<BucketItemDTO> availableItems = new ArrayList<>();
//        List<UnavailableMedicineDTO> unavailableItems = new ArrayList<>();
//        double totalPrice = 0.0;
//        double totalDiscount = 0.0; // Track total discount
//
//        for (Medicine medicine : medicines) {
//            Long medicineId = medicine.getId();
//            int requestedQuantity = medicineQuantities.get(medicineId);
//
//            System.out.println("Processing medicine ID: " + medicineId + ", requested quantity: " + requestedQuantity);
//
//            // Find the best price for this medicine across all vendors
//            List<Stock> medicineStocks = allStocks.stream()
//                    .filter(stock -> stock.getMedicine().getId() == medicineId)
//                    .collect(Collectors.toList());
//
//            System.out.println("Found " + medicineStocks.size() + " stocks for medicine ID: " + medicineId);
//
//            if (!medicineStocks.isEmpty()) {
//                // Filter stocks that have enough quantity
//                List<Stock> sufficientStocks = medicineStocks.stream()
//                        .filter(stock -> stock.getQty() >= requestedQuantity)
//                        .collect(Collectors.toList());
//
//                System.out.println("Found " + sufficientStocks.size() + " stocks with sufficient quantity");
//
//                if (!sufficientStocks.isEmpty()) {
//                    // Find the stock with the lowest final price
//                    Optional<Stock> bestStockOptional = sufficientStocks.stream()
//                            .min(Comparator.comparingDouble(stock -> calculateTotalPrice(stock.getMrp(), stock.getDiscount(), requestedQuantity)));
//
//                    if (bestStockOptional.isPresent()) {
//                        Stock bestStock = bestStockOptional.get();
//                        Vendor vendor = bestStock.getVendor();
//
//                        System.out.println("Selected best stock from vendor ID: " + vendor.getId() + " with price: " + bestStock.getMrp() + ", discount: " + bestStock.getDiscount());
//
//                        BucketItemDTO item = new BucketItemDTO();
//                        item.setMedicineId(medicineId);
//                        item.setMedicineName(medicine.getName());
//                        item.setMedicineImage(medicine.getPhoto1());
//                        item.setMedicineStrip(medicine.getPacking());
////                        item.setVendorId(vendor.getId());
////                        item.setVendorName(vendor.getName());
//                        item.setPrice(calculateUnitPrice(bestStock.getMrp(), bestStock.getDiscount()));
//                        item.setDiscount(bestStock.getDiscount());
//                        item.setAvailableQuantity(bestStock.getQty());
//                        item.setRequestedQuantity(requestedQuantity);
//                        double itemTotalPrice = calculateTotalPrice(bestStock.getMrp(), bestStock.getDiscount(), requestedQuantity);
//                        item.setTotalPrice(itemTotalPrice);
//
//                        availableItems.add(item);
//                        totalPrice += itemTotalPrice;
//                        // Calculate discount amount for this item and add to total discount
//                        double itemDiscountAmount = (bestStock.getMrp() * bestStock.getDiscount() / 100) * requestedQuantity;
//                        totalDiscount += itemDiscountAmount;
//                    }
//                } else {
//                    // No stock with sufficient quantity, add to unavailable items
//                    System.out.println("No vendor has sufficient quantity for medicine ID: " + medicineId);
//                    Stock bestStock = medicineStocks.get(0); // Just take the first one for info
//                    Vendor vendor = bestStock.getVendor();
//
//                    UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
//                    unavailableItem.setMedicineId(medicineId);
//                    unavailableItem.setMedicineName(medicine.getName() + " (Insufficient quantity available)");
//                    unavailableItem.setRequestedQuantity(requestedQuantity);
//                    // Get substitutes for this medicine
//                    try {
//                        List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
//                        unavailableItem.setSubstitutes(substitutes);
//                    } catch (Exception e) {
//                        System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
//                    }
//                    unavailableItems.add(unavailableItem);
//                }
//            } else {
//                // Medicine not available from any vendor, add to unavailable items
//                System.out.println("Medicine ID: " + medicineId + " not available from any vendor");
//                UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
//                unavailableItem.setMedicineId(medicineId);
//                unavailableItem.setMedicineName(medicine.getName() + " (Not available from any vendor)");
//                unavailableItem.setRequestedQuantity(requestedQuantity);
//                // Get substitutes for this medicine
//                try {
//                    List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
//                    unavailableItem.setSubstitutes(substitutes);
//                } catch (Exception e) {
//                    System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
//                }
//                unavailableItems.add(unavailableItem);
//            }
//        }
//
//        System.out.println("Mixed vendor bucket items - available: " + availableItems.size() + ", unavailable: " + unavailableItems.size());
//
//        // Only return bucket if we have items
//        if (!availableItems.isEmpty() || !unavailableItems.isEmpty()) {
//            BucketDTO bucket = new BucketDTO();
//            bucket.setId(System.currentTimeMillis()); // Unique ID for mixed bucket
//            bucket.setName("Best price mixed vendor bucket");
//            bucket.setAvailableItems(availableItems);
//            bucket.setUnavailableItems(unavailableItems);
//            bucket.setTotalPrice(totalPrice);
//            bucket.setTotalDiscount(totalDiscount); // Set the total discount
//            System.out.println("Returning mixed vendor bucket with total price: " + totalPrice + ", total discount: " + totalDiscount);
//            return bucket;
//        }
//
//        System.out.println("Not returning mixed vendor bucket - no items");
//        return null;
//    }

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