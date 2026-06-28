package com.trego.service.impl;

import com.trego.dao.entity.MasterMedicine;
import com.trego.dao.entity.Medicine;
import com.trego.dao.entity.MedicineInformation;
import com.trego.dao.entity.Stock;
import com.trego.dao.entity.Vendor;
import com.trego.dao.impl.MasterMedicineRepository;
import com.trego.dao.impl.MedicineRepository;
import com.trego.dao.impl.StockRepository;
import com.trego.dao.impl.VendorRepository;
import com.trego.dto.BucketDTO;
import com.trego.dto.BucketItemDTO;
import com.trego.dto.BucketRequestDTO;
import com.trego.dto.MedicineDTO;
import com.trego.dto.SelectedSubstituteDTO;
import com.trego.dto.response.CartResponseDTO;
import com.trego.dto.response.VandorCartResponseDTO;
import com.trego.dto.UnavailableMedicineDTO;
import com.trego.dto.view.SubstituteDetailView;
import com.trego.service.IBucketService;
import com.trego.service.ISubstituteService;
import com.trego.utils.Constants;

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

    @Autowired
    private MasterMedicineRepository masterMedicineRepository;

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
                .filter(stock -> stock.getMedicine() != null && stock.getMedicine().getId() != null && medicineIds.contains(stock.getMedicine().getId()))
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
        Map<Integer, List<Stock>> stocksByVendor = relevantStocks.stream()
                .collect(Collectors.groupingBy(stock -> stock.getVendor().getId()));

        // For direct medicine requests, we'll create buckets for all vendors that have
        // the medicines
        // (different from preorder where we only consider user-selected vendors)
        List<BucketDTO> buckets = new ArrayList<>();

        for (Map.Entry<Integer, List<Stock>> entry : stocksByVendor.entrySet()) {
            Integer vendorId = entry.getKey();
            List<Stock> vendorStocks = entry.getValue();

            // Create a bucket for this vendor with available medicines only
            BucketDTO bucket = createBucketForVendorWithPartialAvailability(vendorId, vendorStocks, medicines,
                    medicineQuantities, unavailableMedicineIds);
            if (bucket != null && !bucket.getAvailableItems().isEmpty()) {
                buckets.add(bucket);
            }
        }
        // Sort buckets by total price
        buckets.sort(Comparator.comparing(BucketDTO::getAmountToPay));
        return buckets;
    }

    public List<BucketDTO> createOptimizedBucketsFromPreorder(VandorCartResponseDTO preorderData) {
        // Extract medicine IDs and quantities from preorder data
        Map<Long, Integer> medicineQuantities = new HashMap<>();
        List<Long> medicineIds = new ArrayList<>();
        Set<Integer> selectedVendorIds = new HashSet<>(); // Track vendors selected by user

        System.out.println("Processing preorder data with " + preorderData.getCarts().size() + " carts");

        for (CartResponseDTO cart : preorderData.getCarts()) {
            System.out.println("Processing cart with vendor ID: " + cart.getVendorId() + " and "
                    + cart.getMedicine().size() + " medicines");
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
        System.out.println("Medicine quantities (raw cart IDs): " + medicineQuantities);
        System.out.println("User selected vendors: " + selectedVendorIds);

        // Check if medicineIds is empty
        if (medicineIds.isEmpty()) {
            System.out.println("No medicines found in preorder");
            return new ArrayList<>();
        }

        // ── RESOLVE vendor_medicine_id → master medicine_id ──────────────────
        // Cart payload stores vendor_medicine_id (e.g. 133, 1001).
        // We need the master medicine_id (e.g. 1, 2, 3) to search across ALL vendors.
        Map<Long, Long> cartIdToMasterId = new HashMap<>();
        for (Long cartId : medicineIds) {
            // First check if this IS already a master medicine_id (id exists in
            // vendor_medicine.medicine_id)
            com.trego.dao.entity.Medicine vendorMed = medicineRepository.findById(cartId).orElse(null);
            if (vendorMed != null && vendorMed.getMedicineId() != null) {
                // cartId is a vendor_medicine_id — resolve to master
                cartIdToMasterId.put(cartId, vendorMed.getMedicineId().longValue());
                System.out.println(
                        "Resolved vendor_medicine_id " + cartId + " → master medicine_id " + vendorMed.getMedicineId());
            } else {
                // cartId is already a master medicine_id (or not found, keep as-is)
                cartIdToMasterId.put(cartId, cartId);
                System.out.println(
                        "Cart ID " + cartId + " used as-is (already master ID or not found as vendor_medicine_id)");
            }
        }

        // Rebuild medicineQuantities keyed by master medicine_id
        Map<Long, Integer> masterMedicineQuantities = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : medicineQuantities.entrySet()) {
            Long masterMedId = cartIdToMasterId.getOrDefault(entry.getKey(), entry.getKey());
            masterMedicineQuantities.merge(masterMedId, entry.getValue(), Integer::sum);
        }

        // Replace medicineIds and medicineQuantities with master-ID versions
        medicineIds = new ArrayList<>(masterMedicineQuantities.keySet());
        medicineQuantities = masterMedicineQuantities;

        System.out.println("Master medicine IDs: " + medicineIds);
        System.out.println("Medicine quantities (master IDs): " + medicineQuantities);

        // Get all vendor_medicine records for these master IDs
        List<Integer> masterIds = medicineIds.stream().map(Long::intValue).collect(Collectors.toList());
        List<Medicine> medicines = medicineRepository.findByMedicineIdIn(masterIds);
        System.out.println("Found " + medicines.size() + " medicines in database");

        // Get all stocks for these medicines across ALL vendors
        List<Stock> relevantStocks = stockRepository.findByMedicineIds(masterIds);

        // Filter out medicines that are not available from any vendor
        Set<Long> availableMedicineIds = relevantStocks.stream()
                .filter(stock -> stock.getMedicine() != null && stock.getMedicine().getMedicineId() != null)
                .map(stock -> stock.getMedicine().getMedicineId().longValue())
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
                .filter(medicine -> medicine.getMedicineId() != null
                        && availableMedicineIds.contains(medicine.getMedicineId().longValue()))
                .collect(Collectors.toList());

        System.out.println(
                "After filtering, " + medicineIds.size() + " medicines are available from at least one vendor");
        System.out.println("Available medicine quantities: " + medicineQuantities);

        // If no medicines are available, return empty list
        if (medicineIds.isEmpty()) {
            System.out.println("No medicines are available from any vendor");
            return new ArrayList<>();
        }

        // Group stocks by vendor
        Map<Integer, List<Stock>> stocksByVendor = relevantStocks.stream()
                .filter(stock -> stock.getVendor() != null && stock.getVendor().getId() != null)
                .collect(Collectors.groupingBy(stock -> stock.getVendor().getId()));

        System.out.println("Stocks grouped by " + stocksByVendor.size() + " vendors");

        // Create buckets only for vendors selected by the user
        List<BucketDTO> buckets = new ArrayList<>();

        for (Map.Entry<Integer, List<Stock>> entry : stocksByVendor.entrySet()) {
            Integer vendorId = entry.getKey();
            if (!selectedVendorIds.contains(vendorId)) {
                continue;
            }
            List<Stock> vendorStocks = entry.getValue();

            // Create a bucket for this vendor with available medicines only
            BucketDTO bucket = createBucketForVendorWithPartialAvailability(vendorId, vendorStocks, medicines,
                    medicineQuantities, unavailableMedicineIds);
            if (bucket != null && !bucket.getAvailableItems().isEmpty()) {
                buckets.add(bucket);
            }
        }

        System.out.println("Created " + buckets.size() + " buckets");

        // Calculate current cart total (what user is paying across all split vendors)
        double currentCartTotal = 0.0;
        for (CartResponseDTO cart : preorderData.getCarts()) {
            for (MedicineDTO m : cart.getMedicine()) {
                double price = m.getMrp() != null ? m.getMrp() : 0.0;
                int qty = m.getQty();
                // Only count medicines with real prices (mrp > 0 means vendor had real stock)
                if (price > 0 && qty > 0) {
                    currentCartTotal += price * qty;
                }
            }
        }
        // If currentCartTotal is still 0 (qty=0 due to dummy vendor -1), use
        // totalCartValue from preorder
        if (currentCartTotal == 0.0 && preorderData.getTotalCartValue() != null
                && preorderData.getTotalCartValue() > 0) {
            currentCartTotal = preorderData.getTotalCartValue();
        }
        System.out.println("Current cart total: " + currentCartTotal);

        // ── BUILD PER-MEDICINE USER PRICE MAP ──────────────────────────────────
        // Key = master medicine_id, Value = price user is currently paying per unit
        // This lets us do FAIR per-medicine comparison for partial vendors
        Map<Long, Double> userPricePerMedicine = new HashMap<>();
        for (CartResponseDTO cart : preorderData.getCarts()) {
            for (MedicineDTO m : cart.getMedicine()) {
                Long cartId = (long) m.getId();
                Long masterMedId = cartIdToMasterId.getOrDefault(cartId, cartId);
                double price = m.getMrp() != null ? m.getMrp() : 0.0;
                if (price > 0) {
                    userPricePerMedicine.put(masterMedId, price);
                }
            }
        }
        System.out.println("User price per medicine (master IDs): " + userPricePerMedicine);

        // ── SET SAVINGS: compare against USER'S CURRENT PRICE for SAME medicines ─
        // Example:
        // User: Augmentin ₹162 (Jan Aushadhi) + Ascoril ₹98 (MedEase)
        // Delhi Pharma Hub: Augmentin ₹178 + Ascoril ₹121 = ₹299
        // Partial user total for same 2 = ₹162 + ₹98 = ₹260
        // Savings = ₹260 - ₹299 = -₹39 → NOT cheaper → filtered out ✅
        for (BucketDTO bucket : buckets) {
            bucket.setCurrentCartTotal(currentCartTotal);
            double amountToPay = bucket.getAmountToPay() != null ? bucket.getAmountToPay() : 0.0;

            // Calculate what user CURRENTLY pays for the SAME medicines this vendor has
            double partialUserTotal = 0.0;
            if (bucket.getAvailableItems() != null) {
                for (BucketItemDTO item : bucket.getAvailableItems()) {
                    Long medId = item.getMedicineId();
                    int qty = item.getRequestedQuantity() > 0 ? item.getRequestedQuantity() : 1;
                    double userPrice = userPricePerMedicine.getOrDefault(medId, 0.0);
                    partialUserTotal += userPrice * qty;
                }
            }

            // Fair saving = what user pays for those medicines NOW minus what vendor
            // charges
            double saving = partialUserTotal - amountToPay;
            System.out.println("Vendor: " + bucket.getVendorName()
                    + " | amountToPay=" + amountToPay
                    + " | partialUserTotal=" + partialUserTotal
                    + " | saving=" + saving);

            bucket.setSavings(Math.max(0.0, saving));
            bucket.setIsCheaperOption(saving > 0 && amountToPay > 0);
        }

        // Sort buckets by available items count (desc), then by savings (desc)
        buckets.sort(Comparator
                .comparingInt((BucketDTO b) -> b.getAvailableItems().size())
                .reversed()
                .thenComparing(Comparator.comparingDouble(
                        (BucketDTO b) -> b.getSavings() != null ? b.getSavings() : 0.0).reversed()));

        System.out.println("Buckets sorted by available items count (desc) and savings (desc)");

        // ── FILTER: Only return vendors that are GENUINELY cheaper ────────────
        // 1. savings > 0 → vendor actually saves money on the medicines it covers
        // 2. Fully covers the cart (no unavailable items) OR has 2+ medicines available
        // - This allows 1-item carts to be optimized while keeping multi-item partial
        // vendor filtering.
        List<BucketDTO> cheaperBuckets = buckets.stream()
                .filter(b -> b.getSavings() != null
                        && b.getAvailableItems() != null
                        && ((b.getUnavailableItems() == null || b.getUnavailableItems().isEmpty())
                                || b.getAvailableItems().size() > 1))
                .collect(Collectors.toList());

        System.out.println("Filtered cheaper buckets: " + cheaperBuckets.size()
                + " out of " + buckets.size() + " total");

        return cheaperBuckets;

    }

    private BucketDTO createBucketForVendorWithPartialAvailability(Integer vendorId, List<Stock> vendorStocks,
            List<Medicine> medicines, Map<Long, Integer> medicineQuantities, Set<Long> unavailableMedicineIds) {
        System.out.println("Creating bucket for vendor ID: " + vendorId + " with " + medicines.size() + " medicines");

        // Check if medicines list is empty
        if (medicines.isEmpty()) {
            System.out.println("No medicines to create bucket for vendor ID: " + vendorId);
            return null;
        }

        BucketDTO bucket = new BucketDTO();
        bucket.setId(vendorId.longValue()); // Use vendor ID as bucket ID
        Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
        bucket.setName(vendor != null ? "Complete bucket from " + vendor.getName()
                : "Complete bucket from vendor " + vendorId);
        bucket.setVendorId(vendorId);
        bucket.setVendorName(vendor != null ? vendor.getName() : "");
        bucket.setLogo(vendor != null ? Constants.getVendorLogoWithFallback(vendor.getLogo()) : "");

        List<BucketItemDTO> availableItems = new ArrayList<>();
        List<UnavailableMedicineDTO> unavailableItems = new ArrayList<>();
        String deliveryTime = "1 hrs extra";
        double totalPrice = 0.0;
        double deliveryCharges = 0.0;
        double totalDiscount = 0.0; // Track total discount

        // Process available medicines
        for (Long medicineId : medicineQuantities.keySet()) {
            int requestedQuantity = medicineQuantities.get(medicineId);

            System.out.println("Processing medicine ID: " + medicineId + ", requested quantity: " + requestedQuantity);

            Medicine medicine = medicines.stream()
                    .filter(m -> m.getMedicineId() != null && m.getMedicineId().longValue() == medicineId)
                    .findFirst().orElse(null);

            if (medicine == null) {
                System.out.println("Could not find medicine details for ID: " + medicineId);
                continue;
            }

            // Find the stock for this medicine from this vendor
            Optional<Stock> stockOptional = vendorStocks.stream()
                    .filter(s -> s.getMedicine() != null && s.getMedicine().getMedicineId() != null
                            && s.getMedicine().getMedicineId().longValue() == medicineId)
                    .findFirst();

            if (stockOptional.isPresent()) {
                Stock stock = stockOptional.get();

                System.out.println("Found stock for medicine ID: " + medicineId + " with quantity: " + stock.getQty()
                        + ", MRP: " + stock.getMrp() + ", discount: " + stock.getDiscount());

                // Check if vendor has enough quantity
                if (stock.getQty() >= requestedQuantity) {
                    BucketItemDTO item = new BucketItemDTO();
                    item.setMedicineId(medicineId);
                    item.setMedicineName(medicine.getName());

                    // Fetch image from master medicine table (vendor_medicine_information is empty)
                    MasterMedicine masterMed = medicine.getMedicineId() != null
                            ? masterMedicineRepository.findById(medicine.getMedicineId()).orElse(null)
                            : null;
                    if (masterMed != null) {
                        item.setMedicineImage(masterMed.getPhoto1());
                    } else {
                        MedicineInformation medicineInformation = medicine.getMedicineInformation();
                        item.setMedicineImage(medicineInformation != null ? medicineInformation.getPhoto1() : "");
                    }
                    // Set strip/packing info
                    MedicineInformation medicineInformation = medicine.getMedicineInformation();
                    item.setMedicineStrip(
                            medicineInformation != null ? medicineInformation.getPacking() : medicine.getPackingType());
                    // item.setVendorId(vendorId);
                    // item.setVendorName(vendor != null ? vendor.getName() : "");
                    item.setMrp(stock.getMrp());
                    item.setPrice(calculateUnitPrice(stock.getMrp(), stock.getDiscount()));
                    item.setDiscount(stock.getDiscount());
                    item.setAvailableQuantity(stock.getQty());
                    item.setRequestedQuantity(requestedQuantity);
                    double itemTotalPrice = calculateTotalPrice(stock.getMrp(), 0.0, requestedQuantity);
                    double itemDiscountedPrice = calculateTotalPrice(stock.getMrp(), stock.getDiscount(),
                            requestedQuantity);
                    item.setTotalPrice(itemTotalPrice);

                    availableItems.add(item);
                    totalPrice += itemTotalPrice;
                    // Calculate discount amount for this item and add to total discount
                    double itemDiscountAmount = itemTotalPrice - itemDiscountedPrice;
                    totalDiscount += itemDiscountAmount;

                    System.out.println("Added item to bucket - total price so far: " + totalPrice
                            + ", total discount so far: " + totalDiscount);
                } else {
                    // Vendor doesn't have enough quantity, add to unavailable items
                    System.out.println("Vendor doesn't have enough quantity for medicine ID: " + medicineId
                            + " (required: " + requestedQuantity + ", available: " + stock.getQty() + ")");
                    UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                    unavailableItem.setMedicineId(medicineId);
                    unavailableItem.setMedicineName(medicine.getName() + " (Insufficient quantity available)");
                    unavailableItem.setRequestedQuantity(requestedQuantity);
                    // Get substitutes for this medicine
                    try {
                        List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
                        unavailableItem.setSubstitutes(substitutes);
                    } catch (Exception e) {
                        System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: "
                                + e.getMessage());
                    }
                    unavailableItems.add(unavailableItem);
                }
            } else {
                // Vendor doesn't have this medicine, add to unavailable items
                System.out.println("Vendor doesn't have medicine ID: " + medicineId);
                UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                unavailableItem.setMedicineId(medicineId);
                unavailableItem.setMedicineName(medicine.getName());

                MedicineInformation medicineInformation = medicine.getMedicineInformation();
                if (medicineInformation != null) {
                    unavailableItem.setMedicineImage(medicineInformation.getPhoto1());
                    unavailableItem.setMedicineStrip(medicineInformation.getPacking());
                } else {
                    unavailableItem.setMedicineImage("");
                    unavailableItem.setMedicineStrip("");
                }

                unavailableItem.setRequestedQuantity(requestedQuantity);
                // Get substitutes for this medicine
                try {
                    List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
                    unavailableItem.setSubstitutes(substitutes);
                } catch (Exception e) {
                    System.out.println(
                            "Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
                }
                unavailableItems.add(unavailableItem);
            }
        }

        // Add unavailable medicines to the bucket with appropriate information
        for (Long unavailableMedicineId : unavailableMedicineIds) {
            Optional<Medicine> medicineOpt = medicines.stream()
                    .filter(m -> m.getId() != null && m.getId().equals(unavailableMedicineId))
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
                    System.out.println("Error fetching substitutes for medicine ID: " + unavailableMedicineId
                            + ", error: " + e.getMessage());
                }
                unavailableItems.add(unavailableItem);
            }
        }

        System.out.println("Vendor bucket items - available: " + availableItems.size() + ", unavailable: "
                + unavailableItems.size());

        bucket.setAvailableItems(availableItems);
        bucket.setUnavailableItems(unavailableItems);
        bucket.setTotalPrice(totalPrice);
        bucket.setDeliveryTime(deliveryTime);
        bucket.setTotalDiscount(totalDiscount); // Set the total discount
        bucket.setDeliveryCharges(deliveryCharges);
        double amountToPay = totalPrice - totalDiscount + deliveryCharges;
        bucket.setAmountToPay(amountToPay);
        System.out.println(
                "Returning vendor bucket with total price: " + totalPrice + ", total discount: " + totalDiscount);
        return bucket;
    }

    private BucketDTO createBucketForVendorWithSpecificQuantities(Integer vendorId, List<Stock> vendorStocks,
            List<Medicine> medicines, Map<Long, Integer> medicineQuantities, Set<Long> unavailableMedicineIds) {
        System.out.println("Creating bucket for vendor ID: " + vendorId + " with " + medicines.size() + " medicines");

        // Check if medicines list is empty
        if (medicines.isEmpty()) {
            System.out.println("No medicines to create bucket for vendor ID: " + vendorId);
            return null;
        }

        BucketDTO bucket = new BucketDTO();
        bucket.setId(vendorId.longValue()); // Use vendor ID as bucket ID
        Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
        bucket.setName(vendor != null ? "Complete bucket from " + vendor.getName()
                : "Complete bucket from vendor " + vendorId);
        bucket.setVendorId(vendorId);
        bucket.setVendorName(vendor != null ? vendor.getName() : "");
        bucket.setLogo(vendor != null ? Constants.getVendorLogoWithFallback(vendor.getLogo()) : "");
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
                System.out.println("Skipping medicine ID: " + medicineId + " with zero or negative quantity: "
                        + requestedQuantity);
                continue;
            }

            System.out.println("Processing medicine ID: " + medicineId + ", requested quantity: " + requestedQuantity);

            // Find the stock for this medicine from this vendor
            Optional<Stock> stockOptional = vendorStocks.stream()
                    .filter(s -> s.getMedicine() != null && s.getMedicine().getId() != null && s.getMedicine().getId().equals(medicineId))
                    .findFirst();

            if (stockOptional.isPresent()) {
                Stock stock = stockOptional.get();

                System.out.println("Found stock for medicine ID: " + medicineId + " with quantity: " + stock.getQty()
                        + ", MRP: " + stock.getMrp() + ", discount: " + stock.getDiscount());

                // Check if vendor has enough quantity
                if (stock.getQty() >= requestedQuantity) {
                    BucketItemDTO item = new BucketItemDTO();
                    item.setMedicineId(medicineId);
                    item.setMedicineName(medicine.getName());

                    MedicineInformation medicineInformation = medicine.getMedicineInformation();
                    if (medicineInformation != null) {
                        item.setMedicineImage(medicineInformation.getPhoto1());
                        item.setMedicineStrip(medicineInformation.getPacking());
                    } else {
                        item.setMedicineImage("");
                        item.setMedicineStrip("");
                    }
                    // item.setVendorId(vendorId);
                    // item.setVendorName(vendor != null ? vendor.getName() : "");
                    item.setPrice(calculateUnitPrice(stock.getMrp(), stock.getDiscount()));
                    item.setDiscount(stock.getDiscount());
                    item.setAvailableQuantity(stock.getQty());
                    item.setRequestedQuantity(requestedQuantity);
                    double itemTotalPrice = calculateTotalPrice(stock.getMrp(), stock.getDiscount(), requestedQuantity);
                    item.setTotalPrice(itemTotalPrice);

                    availableItems.add(item);
                    totalPrice += itemTotalPrice;
                    // Calculate discount amount for this item and add to total discount
                    double itemDiscountAmount = stock.getMrp() * stock.getDiscount() / 100.0 * requestedQuantity;
                    totalDiscount += itemDiscountAmount;

                    System.out.println("Added item to bucket - total price so far: " + totalPrice
                            + ", total discount so far: " + totalDiscount);
                } else {
                    // Vendor doesn't have enough quantity, add to unavailable items
                    System.out.println("Vendor doesn't have enough quantity for medicine ID: " + medicineId
                            + " (required: " + requestedQuantity + ", available: " + stock.getQty() + ")");
                    UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                    unavailableItem.setMedicineId(medicineId);
                    unavailableItem.setMedicineName(medicine.getName() + " (Insufficient quantity available)");
                    unavailableItem.setRequestedQuantity(requestedQuantity);

                    MedicineInformation medicineInformation = medicine.getMedicineInformation();
                    if (medicineInformation != null) {
                        unavailableItem.setMedicineImage(medicineInformation.getPhoto1());
                        unavailableItem.setMedicineStrip(medicineInformation.getPacking());
                    } else {
                        unavailableItem.setMedicineImage("");
                        unavailableItem.setMedicineStrip("");
                    }
                    // Get substitutes for this medicine
                    try {
                        List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
                        unavailableItem.setSubstitutes(substitutes);
                    } catch (Exception e) {
                        System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: "
                                + e.getMessage());
                    }
                    unavailableItems.add(unavailableItem);
                }
            } else {
                // Vendor doesn't have this medicine, add to unavailable items
                System.out.println("Vendor doesn't have medicine ID: " + medicineId);
                UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                unavailableItem.setMedicineId(medicineId);
                unavailableItem.setMedicineName(medicine.getName());

                MedicineInformation medicineInformation = medicine.getMedicineInformation();
                if (medicineInformation != null) {
                    unavailableItem.setMedicineImage(medicineInformation.getPhoto1());
                    unavailableItem.setMedicineStrip(medicineInformation.getPacking());
                } else {
                    unavailableItem.setMedicineImage("");
                    unavailableItem.setMedicineStrip("");
                }

                unavailableItem.setRequestedQuantity(requestedQuantity);
                // Get substitutes for this medicine
                try {
                    List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId);
                    unavailableItem.setSubstitutes(substitutes);
                } catch (Exception e) {
                    System.out.println(
                            "Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
                }
                unavailableItems.add(unavailableItem);
            }
        }

        // Add unavailable medicines to the bucket with appropriate information
        for (Long unavailableMedicineId : unavailableMedicineIds) {
            // Check if this unavailable medicine was requested from this vendor
            if (!medicineQuantities.containsKey(unavailableMedicineId)) {
                System.out.println("Unavailable medicine ID: " + unavailableMedicineId
                        + " not requested from vendor ID: " + vendorId);
                continue;
            }

            int requestedQuantity = medicineQuantities.getOrDefault(unavailableMedicineId, 0);

            // Skip medicines with zero quantity
            if (requestedQuantity <= 0) {
                System.out.println("Skipping unavailable medicine ID: " + unavailableMedicineId
                        + " with zero or negative quantity: " + requestedQuantity);
                continue;
            }

            Optional<Medicine> medicineOpt = medicines.stream()
                    .filter(m -> m.getId() != null && m.getId().equals(unavailableMedicineId))
                    .findFirst();

            if (medicineOpt.isPresent()) {
                Medicine medicine = medicineOpt.get();

                UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                unavailableItem.setMedicineId(unavailableMedicineId);
                unavailableItem.setMedicineName(medicine.getName() + " (Not available from any vendor)");
                unavailableItem.setRequestedQuantity(requestedQuantity);

                MedicineInformation medicineInformation = medicine.getMedicineInformation();
                if (medicineInformation != null) {
                    unavailableItem.setMedicineImage(medicineInformation.getPhoto1());
                    unavailableItem.setMedicineStrip(medicineInformation.getPacking());
                } else {
                    unavailableItem.setMedicineImage("");
                    unavailableItem.setMedicineStrip("");
                }
                // Get substitutes for this medicine
                try {
                    List<SubstituteDetailView> substitutes = substituteService.findSubstitute(unavailableMedicineId);
                    unavailableItem.setSubstitutes(substitutes);
                } catch (Exception e) {
                    System.out.println("Error fetching substitutes for medicine ID: " + unavailableMedicineId
                            + ", error: " + e.getMessage());
                }
                unavailableItems.add(unavailableItem);
            }
        }

        System.out.println("Vendor bucket items - available: " + availableItems.size() + ", unavailable: "
                + unavailableItems.size());

        bucket.setAvailableItems(availableItems);
        bucket.setUnavailableItems(unavailableItems);
        bucket.setSelectedSubstitutes(selectedSubstitutes); // Set selected substitutes (initially empty)
        bucket.setTotalPrice(totalPrice);
        bucket.setDeliveryTime(deliveryTime);
        bucket.setTotalDiscount(totalDiscount); // Set the total discount
        bucket.setDeliveryCharges(deliveryCharges);
        double amountToPay = totalPrice - totalDiscount + deliveryCharges;
        bucket.setAmountToPay(amountToPay);
        System.out.println(
                "Returning vendor bucket with total price: " + totalPrice + ", total discount: " + totalDiscount);
        return bucket;
    }

    /**
     * Add a selected substitute to a bucket and update the total amount
     * 
     * @param bucket             The bucket to update
     * @param originalMedicineId The ID of the original unavailable medicine
     * @param substitute         The substitute medicine details
     * @param quantity           The quantity of the substitute to add
     * @return The updated bucket
     */
    public BucketDTO addSubstituteToBucket(BucketDTO bucket, Long originalMedicineId, SubstituteDetailView substitute,
            int quantity) {
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
        double totalPrice = calculateTotalPrice(substitute.getBestPrice(), substitute.getDiscount(), quantity);
        selectedSubstitute.setTotalPrice(totalPrice);
        selectedSubstitute.setMedicineImage(substitute.getPhoto1());
        selectedSubstitute.setMedicineStrip(substitute.getPacking());

        // Add to the selected substitutes list
        bucket.getSelectedSubstitutes().add(selectedSubstitute);

        // Update bucket totals
        bucket.setTotalPrice(bucket.getTotalPrice() + totalPrice);
        double discountAmount = substitute.getBestPrice() * substitute.getDiscount() / 100.0 * quantity;
        bucket.setTotalDiscount(bucket.getTotalDiscount() + discountAmount);
        bucket.setAmountToPay(bucket.getTotalPrice() - bucket.getTotalDiscount() + bucket.getDeliveryCharges());

        System.out.println("Updated bucket - total price: " + bucket.getTotalPrice() +
                ", total discount: " + bucket.getTotalDiscount() +
                ", amount to pay: " + bucket.getAmountToPay());

        return bucket;
    }

    /**
     * Remove a selected substitute from a bucket and update the total amount
     * 
     * @param bucket               The bucket to update
     * @param substituteMedicineId The ID of the substitute medicine to remove
     * @return The updated bucket
     */
    public BucketDTO removeSubstituteFromBucket(BucketDTO bucket, Integer substituteMedicineId) {
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
            double discountAmount = substituteToRemove.getUnitPrice() * substituteToRemove.getDiscount() / 100.0
                    * substituteToRemove.getQuantity();
            bucket.setTotalDiscount(bucket.getTotalDiscount() - discountAmount);
            bucket.setAmountToPay(bucket.getTotalPrice() - bucket.getTotalDiscount() + bucket.getDeliveryCharges());

            System.out.println("Updated bucket - total price: " + bucket.getTotalPrice() +
                    ", total discount: " + bucket.getTotalDiscount() +
                    ", amount to pay: " + bucket.getAmountToPay());
        }

        return bucket;
    }

    private BucketDTO createMixedVendorBucketWithPartialAvailability(List<Medicine> medicines, List<Stock> allStocks,
            Map<Long, Integer> medicineQuantities, Set<Long> unavailableMedicineIds) {
        System.out.println("Creating mixed vendor bucket for " + medicines.size() + " medicines");

        // Check if medicines list is empty
        if (medicines.isEmpty()) {
            System.out.println("No medicines to create mixed vendor bucket");
            return null;
        }

        List<BucketItemDTO> availableItems = new ArrayList<>();
        List<UnavailableMedicineDTO> unavailableItems = new ArrayList<>();
        double totalPrice = 0.0;
        double totalDiscount = 0.0;
        double deliveryCharges = 0.0;

        // Process available medicines
        for (Medicine medicine : medicines) {
            Long medicineId = medicine.getId();
            int requestedQuantity = medicineQuantities.get(medicineId);

            System.out.println("Processing medicine ID: " + medicineId + ", requested quantity: " + requestedQuantity);

            // Find the best price for this medicine across all vendors
            List<Stock> medicineStocks = allStocks.stream()
                    .filter(stock -> stock.getMedicine() != null && stock.getMedicine().getId() != null && stock.getMedicine().getId().equals(medicineId))
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
                            .min(Comparator.comparingDouble(stock -> calculateTotalPrice(stock.getMrp(),
                                    stock.getDiscount(), requestedQuantity)));

                    if (bestStockOptional.isPresent()) {
                        Stock bestStock = bestStockOptional.get();
                        Vendor vendor = bestStock.getVendor();

                        System.out.println("Selected best stock from vendor ID: " + vendor.getId() + " with price: "
                                + bestStock.getMrp() + ", discount: " + bestStock.getDiscount());

                        BucketItemDTO item = new BucketItemDTO();
                        item.setMedicineId(medicineId);
                        item.setMedicineName(medicine.getName());
                        MedicineInformation medicineInformation = medicine.getMedicineInformation();
                        if (medicineInformation != null) {
                            item.setMedicineImage(medicineInformation.getPhoto1());
                            item.setMedicineStrip(medicineInformation.getPacking());
                        } else {
                            item.setMedicineImage("");
                            item.setMedicineStrip("");
                        }
                        // item.setVendorId(vendor.getId());
                        // item.setVendorName(vendor.getName());
                        item.setPrice(calculateUnitPrice(bestStock.getMrp(), bestStock.getDiscount()));
                        item.setDiscount(bestStock.getDiscount());
                        item.setAvailableQuantity(bestStock.getQty());
                        item.setRequestedQuantity(requestedQuantity);
                        double itemTotalPrice = calculateTotalPrice(bestStock.getMrp(), bestStock.getDiscount(),
                                requestedQuantity);
                        item.setTotalPrice(itemTotalPrice);

                        availableItems.add(item);
                        totalPrice += itemTotalPrice;
                        // Calculate discount amount for this item and add to total discount
                        double itemDiscountAmount = bestStock.getMrp() * bestStock.getDiscount() / 100.0
                                * requestedQuantity;
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
                        System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: "
                                + e.getMessage());
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
                    System.out.println(
                            "Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
                }
                unavailableItems.add(unavailableItem);
            }
        }

        // Add unavailable medicines to the bucket with appropriate information
        for (Long unavailableMedicineId : unavailableMedicineIds) {
            Optional<Medicine> medicineOpt = medicines.stream()
                    .filter(m -> m.getId() != null && m.getId().longValue() == unavailableMedicineId)
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
                    System.out.println("Error fetching substitutes for medicine ID: " + unavailableMedicineId
                            + ", error: " + e.getMessage());
                }
                unavailableItems.add(unavailableItem);
            }
        }

        System.out.println("Mixed vendor bucket items - available: " + availableItems.size() + ", unavailable: "
                + unavailableItems.size());

        // Create bucket even if we don't have all medicines
        if (!availableItems.isEmpty() || !unavailableItems.isEmpty()) {
            BucketDTO bucket = new BucketDTO();
            bucket.setId(System.currentTimeMillis()); // Unique ID for mixed bucket
            bucket.setName("Best price mixed vendor bucket");
            bucket.setAvailableItems(availableItems);
            bucket.setUnavailableItems(unavailableItems);
            bucket.setTotalPrice(totalPrice);
            bucket.setTotalDiscount(totalDiscount); // Set the total discount
            bucket.setDeliveryCharges(deliveryCharges);
            double amountToPay = totalPrice - totalDiscount + deliveryCharges;
            bucket.setAmountToPay(amountToPay);
            System.out.println("Returning mixed vendor bucket with total price: " + totalPrice + ", total discount: "
                    + totalDiscount + ", amountToPay: " + amountToPay);
            return bucket;
        }

        System.out.println("Not returning mixed vendor bucket - no items");
        return null;
    }

    private BucketDTO createBucketForVendor(Long vendorId, List<Stock> vendorStocks, List<Medicine> medicines,
            Map<Long, Integer> medicineQuantities) {
        System.out.println("Creating bucket for vendor ID: " + vendorId + " with " + medicines.size() + " medicines");

        // Check if medicines list is empty
        if (medicines.isEmpty()) {
            System.out.println("No medicines to create bucket for vendor ID: " + vendorId);
            return null;
        }

        BucketDTO bucket = new BucketDTO();
        bucket.setId(vendorId.longValue()); // Use vendor ID as bucket ID
        Vendor vendor = vendorRepository.findById(vendorId.intValue()).orElse(null);
        bucket.setName(vendor != null ? "Complete bucket from " + vendor.getName()
                : "Complete bucket from vendor " + vendorId);
        bucket.setVendorId(vendorId.intValue());
        bucket.setVendorName(vendor != null ? vendor.getName() : "");
        bucket.setLogo(vendor != null ? Constants.getVendorLogoWithFallback(vendor.getLogo()) : "");

        List<BucketItemDTO> availableItems = new ArrayList<>();
        List<UnavailableMedicineDTO> unavailableItems = new ArrayList<>();
        double totalPrice = 0.0;
        double deliveryCharges = 0.0;
        double totalDiscount = 0.0; // Track total discount

        for (Medicine medicine : medicines) {
            Long medicineId = medicine.getId();
            int requestedQuantity = medicineQuantities.get(medicineId);

            System.out.println("Processing medicine ID: " + medicineId + ", requested quantity: " + requestedQuantity);

            // Find the stock for this medicine from this vendor
            Optional<Stock> stockOptional = vendorStocks.stream()
                    .filter(s -> s.getMedicine() != null && s.getMedicine().getId() != null && s.getMedicine().getId().equals(medicineId))
                    .findFirst();

            if (stockOptional.isPresent()) {
                Stock stock = stockOptional.get();

                System.out.println("Found stock for medicine ID: " + medicineId + " with quantity: " + stock.getQty()
                        + ", MRP: " + stock.getMrp() + ", discount: " + stock.getDiscount());

                // Check if vendor has enough quantity
                if (stock.getQty() >= requestedQuantity) {
                    BucketItemDTO item = new BucketItemDTO();
                    item.setMedicineId(medicineId);
                    item.setMedicineName(medicine.getName());
                    MedicineInformation medicineInformation = medicine.getMedicineInformation();
                    if (medicineInformation != null) {
                        item.setMedicineImage(medicineInformation.getPhoto1());
                        item.setMedicineStrip(medicineInformation.getPacking());
                    } else {
                        item.setMedicineImage("");
                        item.setMedicineStrip("");
                    }
                    // item.setVendorId(vendorId);
                    // item.setVendorName(vendor != null ? vendor.getName() : "");
                    item.setPrice(calculateUnitPrice(stock.getMrp(), stock.getDiscount()));
                    item.setDiscount(stock.getDiscount());
                    item.setAvailableQuantity(stock.getQty());
                    item.setRequestedQuantity(requestedQuantity);
                    double itemTotalPrice = calculateTotalPrice(stock.getMrp(), stock.getDiscount(), requestedQuantity);
                    item.setTotalPrice(itemTotalPrice);

                    availableItems.add(item);
                    totalPrice += itemTotalPrice;
                    // Calculate discount amount for this item and add to total discount
                    double itemDiscountAmount = stock.getMrp() * stock.getDiscount() / 100.0 * requestedQuantity;
                    totalDiscount += itemDiscountAmount;

                    System.out.println("Added item to bucket - total price so far: " + totalPrice
                            + ", total discount so far: " + totalDiscount);
                } else {
                    // Vendor doesn't have enough quantity, add to unavailable items
                    System.out.println("Vendor doesn't have enough quantity for medicine ID: " + medicineId
                            + " (required: " + requestedQuantity + ", available: " + stock.getQty() + ")");
                    UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                    unavailableItem.setMedicineId(medicineId);
                    unavailableItem.setMedicineName(medicine.getName() + " (Insufficient quantity available)");
                    unavailableItem.setRequestedQuantity(requestedQuantity);
                    // Get substitutes for this medicine
                    try {
                        List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId.longValue());
                        unavailableItem.setSubstitutes(substitutes);
                    } catch (Exception e) {
                        System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: "
                                + e.getMessage());
                    }
                    unavailableItems.add(unavailableItem);
                }
            } else {
                // Vendor doesn't have this medicine, add to unavailable items
                System.out.println("Vendor doesn't have medicine ID: " + medicineId);
                UnavailableMedicineDTO unavailableItem = new UnavailableMedicineDTO();
                unavailableItem.setMedicineId(medicineId);
                unavailableItem.setMedicineName(medicine.getName());
                unavailableItem.setRequestedQuantity(requestedQuantity);
                // Get substitutes for this medicine
                try {
                    List<SubstituteDetailView> substitutes = substituteService.findSubstitute(medicineId.longValue());
                    unavailableItem.setSubstitutes(substitutes);
                } catch (Exception e) {
                    System.out.println(
                            "Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
                }
                unavailableItems.add(unavailableItem);
            }
        }

        System.out.println("Vendor bucket items - available: " + availableItems.size() + ", unavailable: "
                + unavailableItems.size());

        bucket.setAvailableItems(availableItems);
        bucket.setUnavailableItems(unavailableItems);
        bucket.setTotalPrice(totalPrice);
        bucket.setTotalDiscount(totalDiscount); // Set the total discount
        bucket.setDeliveryCharges(deliveryCharges);
        double amountToPay = totalPrice - totalDiscount + deliveryCharges;
        bucket.setAmountToPay(amountToPay);
        System.out.println(
                "Returning vendor bucket with total price: " + totalPrice + ", total discount: " + totalDiscount);
        return bucket;
    }

    private BucketDTO createMixedVendorBucket(List<Medicine> medicines, List<Stock> allStocks,
            Map<Long, Integer> medicineQuantities) {
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
                    .filter(stock -> stock.getMedicine().getId().equals(medicineId))
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
                            .min(Comparator.comparingDouble(stock -> calculateTotalPrice(stock.getMrp(),
                                    stock.getDiscount(), requestedQuantity)));

                    if (bestStockOptional.isPresent()) {
                        Stock bestStock = bestStockOptional.get();
                        Vendor vendor = bestStock.getVendor();

                        System.out.println("Selected best stock from vendor ID: " + vendor.getId() + " with price: "
                                + bestStock.getMrp() + ", discount: " + bestStock.getDiscount());

                        BucketItemDTO item = new BucketItemDTO();
                        item.setMedicineId(medicineId);
                        item.setMedicineName(medicine.getName());
                        MedicineInformation medicineInformation = medicine.getMedicineInformation();
                        if (medicineInformation != null) {
                            item.setMedicineImage(medicineInformation.getPhoto1());
                            item.setMedicineStrip(medicineInformation.getPacking());
                        } else {
                            item.setMedicineImage("");
                            item.setMedicineStrip("");
                        }
                        // item.setVendorId(vendor.getId());
                        // item.setVendorName(vendor.getName());
                        item.setPrice(calculateUnitPrice(bestStock.getMrp(), bestStock.getDiscount()));
                        item.setDiscount(bestStock.getDiscount());
                        item.setAvailableQuantity(bestStock.getQty());
                        item.setRequestedQuantity(requestedQuantity);
                        double itemTotalPrice = calculateTotalPrice(bestStock.getMrp(), bestStock.getDiscount(),
                                requestedQuantity);
                        item.setTotalPrice(itemTotalPrice);

                        availableItems.add(item);
                        totalPrice += itemTotalPrice;
                        // Calculate discount amount for this item and add to total discount
                        double itemDiscountAmount = bestStock.getMrp() * bestStock.getDiscount() / 100.0
                                * requestedQuantity;
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
                        System.out.println("Error fetching substitutes for medicine ID: " + medicineId + ", error: "
                                + e.getMessage());
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
                    System.out.println(
                            "Error fetching substitutes for medicine ID: " + medicineId + ", error: " + e.getMessage());
                }
                unavailableItems.add(unavailableItem);
            }
        }

        System.out.println("Mixed vendor bucket items - available: " + availableItems.size() + ", unavailable: "
                + unavailableItems.size());

        // Only return bucket if we have items
        if (!availableItems.isEmpty() || !unavailableItems.isEmpty()) {
            BucketDTO bucket = new BucketDTO();
            bucket.setId(System.currentTimeMillis()); // Unique ID for mixed bucket
            bucket.setName("Best price mixed vendor bucket");
            bucket.setAvailableItems(availableItems);
            bucket.setUnavailableItems(unavailableItems);
            bucket.setTotalPrice(totalPrice);
            bucket.setTotalDiscount(totalDiscount); // Set the total discount
            System.out.println("Returning mixed vendor bucket with total price: " + totalPrice + ", total discount: "
                    + totalDiscount);
            return bucket;
        }

        System.out.println("Not returning mixed vendor bucket - no items");
        return null;
    }

    private double calculateUnitPrice(Double mrp, Double discount) {
        if (mrp == null)
            return 0.0;
        if (discount == null || discount <= 0)
            return mrp;
        double discountAmount = mrp * discount / 100.0;
        return mrp - discountAmount;
    }

    private double calculateTotalPrice(Double mrp, Double discount, int quantity) {
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