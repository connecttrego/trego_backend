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
        VandorCartResponseDTO vandorCartResponseDTO = new VandorCartResponseDTO();
        vandorCartResponseDTO.setUserId(preOrder.getUserId());
        vandorCartResponseDTO.setOrderId(orderId);

        Gson gson = new Gson();
        PreOrderResponseDTO preOrderResponseDTO = gson.fromJson(preOrder.getPayload(), PreOrderResponseDTO.class);
        preOrderResponseDTO.setOrderId(preOrder.getId());

        // Get all unique medicine IDs from all carts
        List<Medicine> allMedicines = medicineRepository.findAllById(
                preOrderResponseDTO.getCarts().stream()
                        .flatMap(cart -> cart.getMedicine().stream())
                        .map(MedicineDTO::getId)
                        .distinct()
                        .collect(Collectors.toList()));

        List<CartResponseDTO> cartDTOs = preOrderResponseDTO.getCarts().stream().map(cart -> {
            List<MedicineDTO> medicines = cart.getMedicine().stream().map(medicine -> {
                // Use the new method that returns a List to handle multiple stocks
                List<Stock> stocks = stockRepository.findStocksByMedicineIdAndVendorId(medicine.getId(),
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
                        // Use the new method that returns a List to handle multiple stocks
                        List<Stock> stocks = stockRepository.findStocksByMedicineIdAndVendorId(medicine.getId(),
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

        Medicine tempMedicine = medicineRepository.findById(medicineDTO.getId()).orElse(null);
        if (tempMedicine == null) return medicineDTO;
        
        medicineDTO.setId(tempMedicine.getId());
        medicineDTO.setMrp(stock.getMrp());
        medicineDTO.setName(tempMedicine.getName());
        medicineDTO.setManufacturer(tempMedicine.getManufacture()); // Fixed rename
        medicineDTO.setMedicineType(tempMedicine.getMedicineType());
        
        MedicineInformation medicineInformation = tempMedicine.getMedicineInformation();
        if (medicineInformation != null) {
            medicineDTO.setUseOf(medicineInformation.getUseOf());
            medicineDTO.setStrip(medicineInformation.getPacking());
            medicineDTO.setImage(Constants.LOGO_BASE_URL + Constants.MEDICINES_BASE_URL + medicineInformation.getPhoto1());
            medicineDTO.setPhoto1(Constants.LOGO_BASE_URL + Constants.MEDICINES_BASE_URL + medicineInformation.getPhoto1());
        } else {
            medicineDTO.setUseOf("");
            medicineDTO.setStrip("");
            medicineDTO.setImage("");
            medicineDTO.setPhoto1("");
        }
        
        medicineDTO.setSaltComposition(tempMedicine.getSaltComposition());
        medicineDTO.setDiscount(stock.getDiscount());
        medicineDTO.setActualPrice(stock.getMrp());
        medicineDTO.setExpiryDate(stock.getExpiryDate());
        return medicineDTO;
    }

    private MedicineDTO populateUnavailableMedicalDTO(MedicineDTO medicineDTO) {
        // Create a copy of the medicine DTO with default values indicating
        // unavailability
        MedicineDTO unavailableMedicine = new MedicineDTO();
        unavailableMedicine.setId(medicineDTO.getId());
        unavailableMedicine.setName(medicineDTO.getName());
        unavailableMedicine.setManufacturer(medicineDTO.getManufacturer());
        unavailableMedicine.setSaltComposition(medicineDTO.getSaltComposition());
        unavailableMedicine.setMedicineType(medicineDTO.getMedicineType());
        unavailableMedicine.setUseOf(medicineDTO.getUseOf());
        unavailableMedicine.setStrip(medicineDTO.getStrip());
        unavailableMedicine.setPhoto1(medicineDTO.getPhoto1());

        // Set default values indicating unavailability
        unavailableMedicine.setMrp(0.0);
        unavailableMedicine.setDiscount(0.0);
        unavailableMedicine.setQty(0);
        unavailableMedicine.setActualPrice(0.0);
        unavailableMedicine.setExpiryDate(null);

        return unavailableMedicine;
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

                            List<Stock> stocks = stockRepository.findStocksByMedicineIdAndVendorId(medicineId,
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

                            List<Stock> stocks = stockRepository.findStocksByMedicineIdAndVendorId(medicineId,
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
        preOrderResponseDTO.setTotalCartValue(totalCartValue);

    }

}