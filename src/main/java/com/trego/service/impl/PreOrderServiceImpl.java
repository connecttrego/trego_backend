package com.trego.service.impl;

import com.google.gson.Gson;
import com.trego.dao.entity.*;
import com.trego.dao.impl.*;
import com.trego.dto.CartDTO;
import com.trego.dto.MedicineDTO;
import com.trego.dto.PreOrderDTO;
import com.trego.dto.response.CartResponseDTO;
import com.trego.dto.response.PreOrderResponseDTO;
import com.trego.dto.response.VandorCartResponseDTO;
import com.trego.service.IPreOrderService;
import com.trego.utils.Constants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PreOrderServiceImpl implements IPreOrderService {

    @Autowired
    private PreOrderRepository preOrderRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private MasterMedicineRepository masterMedicineRepository;

    @Override
    public PreOrderResponseDTO savePreOrder(PreOrderDTO preOrderRequest) {

        calculateTotalCartValue(preOrderRequest);
        calculateAmountToPay(preOrderRequest);

        Gson gson = new Gson();
        List<PreOrder> preOrders = preOrderRepository.findByUserIdAndPaymentStatus(preOrderRequest.getUserId(),
                "unpaid");
        PreOrder preOrder = null;

        // If there are multiple pre-orders, use the first one or create a new one if
        // none exist
        if (preOrders != null && !preOrders.isEmpty()) {
            preOrder = preOrders.get(0); // Use the first one
        }
        if (preOrder == null) {
            preOrder = new PreOrder();
            preOrder.setPaymentStatus("unpaid");
            preOrder.setOrderStatus("new");
            preOrder.setUserId(preOrderRequest.getUserId());
            preOrder.setCreatedBy("SYSTEM");
            preOrder.setPayload(gson.toJson(preOrderRequest));
            preOrder.setMobileNo(preOrderRequest.getMobileNo());
            preOrder.setAddressId(preOrderRequest.getAddressId());

        } else {
            // Reusing existing preorder: update all relevant fields and clear stale payment data
            preOrder.setPayload(gson.toJson(preOrderRequest));
            preOrder.setMobileNo(preOrderRequest.getMobileNo());
            preOrder.setAddressId(preOrderRequest.getAddressId());
            // Clear previous RazorPay order data so a fresh order is always created
            preOrder.setRazorpayOrderId(null);
            preOrder.setTotalPayAmount(null);
            preOrder.setPaymentStatus("unpaid");
        }
        preOrderRepository.save(preOrder);

        PreOrderResponseDTO preOrderResponseDTO = gson.fromJson(preOrder.getPayload(), PreOrderResponseDTO.class);
        preOrderResponseDTO.setOrderId(preOrder.getId());

        populateCartResponse(preOrderResponseDTO);

        return preOrderResponseDTO;
    }

    @Override
    public PreOrderResponseDTO getOrdersByUserId(Long userId) {
        Gson gson = new Gson();
        PreOrderResponseDTO preOrderResponseDTO = new PreOrderResponseDTO();
        List<PreOrder> preOrders = preOrderRepository.findByUserIdAndPaymentStatus(userId, "unpaid");
        if (preOrders != null && !preOrders.isEmpty()) {
            PreOrder tempPreOrder = preOrders.get(0); // Use the first one
            preOrderResponseDTO = gson.fromJson(tempPreOrder.getPayload(), PreOrderResponseDTO.class);
            preOrderResponseDTO.setOrderId(tempPreOrder.getId());
            populateCartResponse(preOrderResponseDTO);
        }
        return preOrderResponseDTO;
    }

    @Override
    public VandorCartResponseDTO vendorSpecificPrice(Long orderId) {
        PreOrder preOrder = preOrderRepository.findById(orderId).orElse(null);
        if (preOrder == null) {
            System.out.println("PreOrder not found for ID: " + orderId);
            return new VandorCartResponseDTO();
        }

        VandorCartResponseDTO vandorCartResponseDTO = new VandorCartResponseDTO();
        vandorCartResponseDTO.setUserId(preOrder.getUserId());
        vandorCartResponseDTO.setOrderId(orderId);

        if (preOrder.getPayload() == null || preOrder.getPayload().trim().isEmpty()) {
            System.out.println("PreOrder payload is null or empty for ID: " + orderId);
            vandorCartResponseDTO.setCarts(new ArrayList<>());
            return vandorCartResponseDTO;
        }

        Gson gson = new Gson();
        PreOrderResponseDTO preOrderResponseDTO = gson.fromJson(preOrder.getPayload(), PreOrderResponseDTO.class);
        if (preOrderResponseDTO == null) {
            System.out.println("Parsed PreOrderResponseDTO is null for ID: " + orderId);
            vandorCartResponseDTO.setCarts(new ArrayList<>());
            return vandorCartResponseDTO;
        }
        preOrderResponseDTO.setOrderId(preOrder.getId());

        // Capture the total cart value from the preorder payload for "Switch to Cheaper" comparison
        if (preOrderResponseDTO.getTotalCartValue() != null && preOrderResponseDTO.getTotalCartValue() > 0) {
            vandorCartResponseDTO.setTotalCartValue(preOrderResponseDTO.getTotalCartValue());
        }

        if (preOrderResponseDTO.getCarts() == null || preOrderResponseDTO.getCarts().isEmpty()) {
            System.out.println("PreOrder carts list is null or empty for ID: " + orderId);
            vandorCartResponseDTO.setCarts(new ArrayList<>());
            return vandorCartResponseDTO;
        }

        // Get all unique medicine IDs from all carts
        List<Medicine> allMedicines = medicineRepository.findAllById(
                preOrderResponseDTO.getCarts().stream()
                        .filter(Objects::nonNull)
                        .flatMap(cart -> {
                            if (cart.getMedicine() == null) return java.util.stream.Stream.empty();
                            return cart.getMedicine().stream();
                        })
                        .filter(Objects::nonNull)
                        .map(m -> (int) m.getId())
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList()));


        List<CartResponseDTO> cartDTOs = preOrderResponseDTO.getCarts().stream().map(cart -> {
            List<MedicineDTO> medicines = cart.getMedicine().stream().map(medicine -> {
                List<Stock> stocks = getStocksForMedicineAndVendor(medicine.getId(),
                        cart.getVendorId());
                Optional<Stock> optionalStock = stocks.isEmpty() ? Optional.empty() : Optional.of(stocks.get(0));
                // Instead of returning null, return the medicine with indication of
                // unavailability
                if (optionalStock.isPresent()) {
                    return populateMedicalDTO(medicine, optionalStock.get());
                } else {
                    // Return medicine with default values indicating unavailability
                    return populateUnavailableMedicalDTO(medicine);
                }
            })
                    // Remove the filter that removes null values since we're not returning null
                    // anymore
                    .collect(Collectors.toList());

            cart.setMedicine(medicines);
            Vendor vendor = vendorRepository.findById(cart.getVendorId()).orElse(null);
            if (vendor != null) {
                cart.setVendorId(vendor.getId());
                cart.setName(vendor.getName());
                cart.setLogo(Constants.getVendorLogoWithFallback(vendor.getLogo()));
                cart.setGstNumber(vendor.getGistin());
                cart.setLicence(vendor.getDruglicense());
                // cart.setAddress(vendor.getAddress());
                cart.setLat(vendor.getLat() != null ? vendor.getLat().doubleValue() : null);
                cart.setLng(vendor.getLng() != null ? vendor.getLng().doubleValue() : null);
                cart.setDeliveryTime(vendor.getDeliveryTime());
                cart.setReviews(vendor.getReviews());
            }

            double totalCartValue = medicines.stream()
                    .mapToDouble(m -> m.getMrp() * m.getQty())
                    .sum();
            double discount = medicines.stream()
                    .mapToDouble(m -> {
                        double price = m.getMrp() * m.getQty();
                        return price * m.getDiscount() / 100.0;
                    })
                    .sum();

            cart.setTotalCartValue(totalCartValue);
            cart.setAmountToPay(totalCartValue - discount);
            return cart;
        }).collect(Collectors.toList());

        vandorCartResponseDTO.setCarts(cartDTOs);

        return vandorCartResponseDTO;
    }

    private void populateCartResponse(PreOrderResponseDTO preOrderResponseDTO) {
        List<CartResponseDTO> cartDTOs = preOrderResponseDTO.getCarts().stream().map(cart -> {

            List<MedicineDTO> medicines = cart.getMedicine().stream()
                    .map(medicine -> {
                        List<Stock> stocks = getStocksForMedicineAndVendor(medicine.getId(),
                                cart.getVendorId());
                        Optional<Stock> optionalStock = stocks.isEmpty() ? Optional.empty()
                                : Optional.of(stocks.get(0));
                        // Instead of returning null, return the medicine with indication of
                        // unavailability
                        if (!stocks.isEmpty()) {
                            Stock stock = stocks.get(0);
                            return populateMedicalDTO(medicine, stock);
                        } else {
                            // Return medicine with default values indicating unavailability
                            return populateUnavailableMedicalDTO(medicine);
                        }
                    })
                    // Remove the filter that removes null values since we're not returning null
                    // anymore
                    .collect(Collectors.toList());

            cart.setMedicine(medicines);

            Vendor vendor = vendorRepository.findById(cart.getVendorId()).orElse(null);
            if (vendor != null) {
                cart.setVendorId(vendor.getId());
                cart.setName(vendor.getName());
                cart.setLogo(Constants.getVendorLogoWithFallback(vendor.getLogo()));
                cart.setGstNumber(vendor.getGistin());
                cart.setLicence(vendor.getDruglicense());
                // cart.setAddress(vendor.getAddress());
                cart.setLat(vendor.getLat() != null ? vendor.getLat().doubleValue() : null);
                cart.setLng(vendor.getLng() != null ? vendor.getLng().doubleValue() : null);
                cart.setDeliveryTime(vendor.getDeliveryTime());
                cart.setReviews(vendor.getReviews());
            }
            return cart;
        }).collect(Collectors.toList());

        double totalCartValue = getTotalCartValue(cartDTOs);
        double deliveryCharges = 0.0;
        preOrderResponseDTO.setTotalCartValue(totalCartValue);
        preOrderResponseDTO.setDeliveryCharges(deliveryCharges);
        preOrderResponseDTO.setAmountToPay(totalCartValue - getDiscount(cartDTOs) + deliveryCharges);
        preOrderResponseDTO.setCarts(cartDTOs);
    }

    private MedicineDTO populateMedicalDTO(MedicineDTO medicineDTO, Stock stock) {

        Medicine tempMedicine = medicineRepository.findById((int) medicineDTO.getId()).orElse(null);
        if (tempMedicine == null) {
            List<Medicine> list = medicineRepository.findByMedicineId((int) medicineDTO.getId());
            if (list != null && !list.isEmpty()) {
                tempMedicine = list.get(0);
            }
        }
        if (tempMedicine == null) return medicineDTO;
        
        // Retrieve master medicine to serve as catalog fallback
        MasterMedicine masterMed = null;
        if (tempMedicine.getMedicineId() != null) {
            masterMed = masterMedicineRepository.findById(tempMedicine.getMedicineId()).orElse(null);
        } else if (tempMedicine.getName() != null && !tempMedicine.getName().trim().isEmpty()) {
            List<MasterMedicine> matchedMeds = masterMedicineRepository.findByNameIgnoreCase(tempMedicine.getName().trim());
            if (matchedMeds != null && !matchedMeds.isEmpty()) {
                masterMed = matchedMeds.get(0);
            }
        }

        medicineDTO.setId(tempMedicine.getId());
        medicineDTO.setMrp(stock.getMrp());
        medicineDTO.setName(tempMedicine.getName() != null && !tempMedicine.getName().trim().isEmpty() ? tempMedicine.getName() : (masterMed != null ? masterMed.getName() : ""));
        medicineDTO.setManufacturer(tempMedicine.getManufacture() != null && !tempMedicine.getManufacture().trim().isEmpty() ? tempMedicine.getManufacture() : (masterMed != null ? masterMed.getManufacture() : ""));
        medicineDTO.setMedicineType(tempMedicine.getMedicineType());
        
        MedicineInformation medicineInformation = tempMedicine.getMedicineInformation();
        String useOf = "";
        String strip = "";
        String rawPhoto = "";

        if (medicineInformation != null) {
            useOf = medicineInformation.getUseOf();
            strip = medicineInformation.getPacking();
            rawPhoto = medicineInformation.getPhoto1();
        }

        // Apply master medicine fallbacks for useOf and strip
        if (useOf == null || useOf.trim().isEmpty()) {
            useOf = (masterMed != null && masterMed.getUseOf() != null) ? masterMed.getUseOf() : "";
        }
        if (strip == null || strip.trim().isEmpty()) {
            strip = (masterMed != null && masterMed.getPackaging() != null) ? masterMed.getPackaging() : "";
        }
        
        // Apply master medicine fallback for photo — only if vendor-specific photo is null/empty
        if (rawPhoto == null || rawPhoto.trim().isEmpty()) {
            rawPhoto = (masterMed != null && masterMed.getPhoto1() != null) ? masterMed.getPhoto1() : "";
        }

        // Use unified fallback (consistent with VendorServiceImpl and OrderServiceImpl)
        String finalPhotoUrl = Constants.getMedicineImageWithFallback(rawPhoto);

        medicineDTO.setUseOf(useOf);
        medicineDTO.setStrip(strip);
        medicineDTO.setImage(finalPhotoUrl);
        medicineDTO.setPhoto1(finalPhotoUrl);
        
        medicineDTO.setSaltComposition(tempMedicine.getSaltComposition() != null && !tempMedicine.getSaltComposition().trim().isEmpty() ? tempMedicine.getSaltComposition() : (masterMed != null ? masterMed.getSaltComposition() : ""));
        medicineDTO.setDiscount(stock.getDiscount());
        medicineDTO.setActualPrice(stock.getMrp());
//        medicineDTO.setExpiryDate(stock.getExpiryDate());
        return medicineDTO;
    }

    private MedicineDTO populateUnavailableMedicalDTO(MedicineDTO medicineDTO) {
        Medicine tempMedicine = medicineRepository.findById((int) medicineDTO.getId()).orElse(null);
        if (tempMedicine == null) {
            List<Medicine> list = medicineRepository.findByMedicineId((int) medicineDTO.getId());
            if (list != null && !list.isEmpty()) {
                tempMedicine = list.get(0);
            }
        }
        if (tempMedicine != null) {
            // Retrieve master medicine to serve as catalog fallback
            MasterMedicine masterMed = null;
            if (tempMedicine.getMedicineId() != null) {
                masterMed = masterMedicineRepository.findById(tempMedicine.getMedicineId()).orElse(null);
            } else if (tempMedicine.getName() != null && !tempMedicine.getName().trim().isEmpty()) {
                List<MasterMedicine> matchedMeds = masterMedicineRepository.findByNameIgnoreCase(tempMedicine.getName().trim());
                if (matchedMeds != null && !matchedMeds.isEmpty()) {
                    masterMed = matchedMeds.get(0);
                }
            }

            medicineDTO.setName(tempMedicine.getName() != null && !tempMedicine.getName().trim().isEmpty() ? tempMedicine.getName() : (masterMed != null ? masterMed.getName() : ""));
            medicineDTO.setManufacturer(tempMedicine.getManufacture() != null && !tempMedicine.getManufacture().trim().isEmpty() ? tempMedicine.getManufacture() : (masterMed != null ? masterMed.getManufacture() : ""));
            medicineDTO.setMedicineType(tempMedicine.getMedicineType());
            
            MedicineInformation medicineInformation = tempMedicine.getMedicineInformation();
            String useOf = "";
            String strip = "";
            String rawPhoto = "";

            if (medicineInformation != null) {
                useOf = medicineInformation.getUseOf();
                strip = medicineInformation.getPacking();
                rawPhoto = medicineInformation.getPhoto1();
            }

            // Apply master medicine fallbacks for useOf and strip
            if (useOf == null || useOf.trim().isEmpty()) {
                useOf = (masterMed != null && masterMed.getUseOf() != null) ? masterMed.getUseOf() : "";
            }
            if (strip == null || strip.trim().isEmpty()) {
                strip = (masterMed != null && masterMed.getPackaging() != null) ? masterMed.getPackaging() : "";
            }
            
            // Apply master medicine fallback for photo — only if vendor-specific photo is null/empty
            if (rawPhoto == null || rawPhoto.trim().isEmpty()) {
                rawPhoto = (masterMed != null && masterMed.getPhoto1() != null) ? masterMed.getPhoto1() : "";
            }

            // Use unified fallback (consistent with VendorServiceImpl and OrderServiceImpl)
            String finalPhotoUrl = Constants.getMedicineImageWithFallback(rawPhoto);

            medicineDTO.setUseOf(useOf);
            medicineDTO.setStrip(strip);
            medicineDTO.setImage(finalPhotoUrl);
            medicineDTO.setPhoto1(finalPhotoUrl);
            medicineDTO.setSaltComposition(tempMedicine.getSaltComposition() != null && !tempMedicine.getSaltComposition().trim().isEmpty() ? tempMedicine.getSaltComposition() : (masterMed != null ? masterMed.getSaltComposition() : ""));
        }

        // Set default values indicating unavailability
        medicineDTO.setMrp(0.0);
        medicineDTO.setDiscount(0.0);
        // Preserve the original requested quantity instead of setting to 0
        medicineDTO.setActualPrice(0.0);
        medicineDTO.setExpiryDate(null);

        return medicineDTO;
    }

    private static double getTotalCartValue(List<CartResponseDTO> cartDTOs) {

        return cartDTOs.stream()
                .flatMap(cart -> cart.getMedicine().stream())
                .mapToDouble(medicine -> medicine.getMrp() * medicine.getQty())
                .sum();
    }

    private static double getDiscount(List<CartResponseDTO> cartDTOs) {

        return cartDTOs.stream()
                .flatMap(cart -> cart.getMedicine().stream())
                .mapToDouble(medicine -> {
                    double totalPrice = medicine.getMrp() * medicine.getQty();
                    return totalPrice * medicine.getDiscount() / 100.0;
                })
                .sum();
    }

    public PreOrderDTO calculateAmountToPay(PreOrderDTO preOrderResponseDTO) {

        List<CartDTO> carts = preOrderResponseDTO.getCarts();

        double amountToPay = carts.stream()
                .flatMap(cart -> cart.getMedicine().stream()
                        .map(medicine -> {

                            Integer vendorId = cart.getVendorId();
                            long medicineId = medicine.getId();
                            int qty = medicine.getQty();

                            List<Stock> stocks = getStocksForMedicineAndVendor(medicineId,
                                    vendorId);

                            if (!stocks.isEmpty()) {

                                Stock stock = stocks.get(0);

                                Double price = stock.getMrp();
                                Double discountPercentage = stock.getDiscount();

                                double totalCartValue = price * qty;

                                double discountAmount = totalCartValue * discountPercentage / 100.0;

                                return totalCartValue - discountAmount;
                            }

                            return 0.0;
                        }))
                .reduce(0.0, Double::sum);

        preOrderResponseDTO.setAmountToPay(amountToPay);

        return preOrderResponseDTO;
    }

    private void calculateTotalCartValue(PreOrderDTO preOrderResponseDTO) {

        double totalCartValue = preOrderResponseDTO.getCarts().stream()
                .flatMap(cart -> cart.getMedicine().stream()
                        .map(medicine -> {

                            Integer vendorId = cart.getVendorId();
                            long medicineId = medicine.getId();
                            int qty = medicine.getQty();

                            List<Stock> stocks = getStocksForMedicineAndVendor(medicineId,
                                    vendorId);

                            if (!stocks.isEmpty()) {

                                Stock stock = stocks.get(0);

                                Double price = stock.getMrp();
                                Double discount = stock.getDiscount();

                                medicine.setMrp(price);
                                medicine.setDiscount(discount);

                                // qty * price
                                return price * qty;

                            } else {
                                return 0.0;
                            }
                        }))
                .reduce(0.0, Double::sum);
    }

    private List<Stock> getStocksForMedicineAndVendor(long medicineId, Integer vendorId) {
        if (vendorId == null) {
            return java.util.Collections.emptyList();
        }
        
        // Find vendor using either internal ID or external ID
        Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
        if (vendor == null) {
            vendor = vendorRepository.findByVendorId(vendorId).orElse(null);
        }
        
        Integer vendorUserId = vendorId;
        Integer externalVendorId = vendorId;
        
        if (vendor != null) {
            vendorUserId = vendor.getId();
            externalVendorId = vendor.getVendorId();
        }
        
        // Primary lookup: by vendor_medicine_id directly
        List<Stock> stocks = stockRepository.findStocksByMedicineIdAndBothVendorIds((int) medicineId, vendorUserId, externalVendorId);
        
        // Fallback: if not found, try by master medicine_id (medicine_id column in vendor_medicine)
        // This handles cases where cart has vendor_medicine_id from vendor A, but selected vendor B
        // uses a different vendor_medicine_id for the same master medicine
        if (stocks.isEmpty()) {
            // Find the master medicine_id for this vendor_medicine_id
            com.trego.dao.entity.Medicine vendorMed = medicineRepository.findById(medicineId.intValue()).orElse(null);
            if (vendorMed != null && vendorMed.getMedicineId() != null) {
                stocks = stockRepository.findStocksByMasterMedicineIdAndBothVendorIds(
                        vendorMed.getMedicineId(), vendorUserId, externalVendorId);
            }
        }
        
        return stocks;
    }

}