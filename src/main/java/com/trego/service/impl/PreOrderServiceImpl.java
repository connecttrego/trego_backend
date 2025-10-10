package com.trego.service.impl;

import com.google.gson.Gson;
import com.trego.dao.entity.*;
import com.trego.dao.impl.*;
import com.trego.dto.CartDTO;
import com.trego.dto.MedicineDTO;
import com.trego.dto.PreOrderDTO;
import com.trego.dto.VendorDTO;
import com.trego.dto.response.CartResponseDTO;
import com.trego.dto.response.PreOrderResponseDTO;
import com.trego.dto.response.VandorCartResponseDTO;
import com.trego.service.IPreOrderService;
import com.trego.utils.Constants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
        List<PreOrder> preOrders = preOrderRepository.findByUserIdAndPaymentStatus(preOrderRequest.getUserId(), "unpaid");
        PreOrder preOrder = null;
        
        // If there are multiple pre-orders, use the first one or create a new one if none exist
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
            preOrder.setPayload(gson.toJson(preOrderRequest));

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
    public VandorCartResponseDTO vendorSpecificPrice( long orderId) {
        PreOrder preOrder = preOrderRepository.findById(orderId).orElse(null);
        VandorCartResponseDTO vandorCartResponseDTO = new VandorCartResponseDTO();
        vandorCartResponseDTO.setUserId(preOrder.getUserId());
        vandorCartResponseDTO.setOrderId(orderId);
        Gson gson = new Gson();
        PreOrderResponseDTO preOrderResponseDTO =  gson.fromJson(preOrder.getPayload(), PreOrderResponseDTO.class);
        preOrderResponseDTO.setOrderId(preOrder.getId());

        // Get all unique medicine IDs from all carts
        List<Medicine> allMedicines = medicineRepository.findAllById(
                preOrderResponseDTO.getCarts().stream()
                        .flatMap(cart -> cart.getMedicine().stream())
                        .map(MedicineDTO::getId)
                        .distinct()
                        .collect(Collectors.toList())
        );


        List<CartResponseDTO> cartDTOs = preOrderResponseDTO.getCarts().stream().map(cart -> {
            List<MedicineDTO> medicines = cart.getMedicine().stream()
                    .map(medicine -> {
                        // Use the new method that returns a List to handle multiple stocks
                        List<Stock> stocks = stockRepository.findStocksByMedicineIdAndVendorId(medicine.getId(), cart.getVendorId());
                        Optional<Stock> optionalStock = stocks.isEmpty() ? Optional.empty() : Optional.of(stocks.get(0));
                        // Instead of returning null, return the medicine with indication of unavailability
                        if (optionalStock.isPresent()) {
                            return populateMedicalDTO(medicine, optionalStock.get());
                        } else {
                            // Return medicine with default values indicating unavailability
                            return populateUnavailableMedicalDTO(medicine);
                        }
                    })
                    // Remove the filter that removes null values since we're not returning null anymore
                    .collect(Collectors.toList());

            cart.setMedicine(medicines);
            Vendor vendor = vendorRepository.findById(cart.getVendorId()).orElse(null);
            if (vendor != null) {
                cart.setVendorId(vendor.getId());
                cart.setName(vendor.getName());
                if(vendor.getCategory().equalsIgnoreCase("retail")) {
                    cart.setLogo(Constants.LOGO_BASE_URL + Constants.OFFLINE_BASE_URL+ vendor.getLogo());
                }else{
                    cart.setLogo(Constants.LOGO_BASE_URL + Constants.ONLINE_BASE_URL+ vendor.getLogo());
                }
                cart.setGstNumber(vendor.getGistin());
                cart.setLicence(vendor.getDruglicense());
                // cart.setAddress(vendor.getAddress());
                cart.setLat(vendor.getLat());
                cart.setLng(vendor.getLng());
                cart.setDeliveryTime(vendor.getDeliveryTime());
                cart.setReviews(vendor.getReviews());
            }
            double totalCartValue = medicines.stream()
                    .mapToDouble(medicine -> medicine.getMrp() * medicine.getQty())
                    .sum();

            double discount   = medicines.stream()
                    .mapToDouble(medicine ->(medicine.getMrp() * medicine.getQty()) * medicine.getDiscount() / 100.0)
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
                        List<Stock> stocks = stockRepository.findStocksByMedicineIdAndVendorId(medicine.getId(), cart.getVendorId());
                        Optional<Stock> optionalStock = stocks.isEmpty() ? Optional.empty() : Optional.of(stocks.get(0));
                        // Instead of returning null, return the medicine with indication of unavailability
                        if (!stocks.isEmpty()) {
                            Stock stock = stocks.get(0);
                            return populateMedicalDTO(medicine, stock);
                        } else {
                            // Return medicine with default values indicating unavailability
                            return populateUnavailableMedicalDTO(medicine);
                        }
                    })
                    // Remove the filter that removes null values since we're not returning null anymore
                    .collect(Collectors.toList());


            cart.setMedicine(medicines);


          Vendor vendor = vendorRepository.findById(cart.getVendorId()).orElse(null);
          if (vendor != null) {
              cart.setVendorId(vendor.getId());
              cart.setName(vendor.getName());
              if(vendor.getCategory().equalsIgnoreCase("retail")) {
                  cart.setLogo(vendor.getLogo());
              }else{
                  cart.setLogo(vendor.getLogo());
              }
              cart.setGstNumber(vendor.getGistin());
              cart.setLicence(vendor.getDruglicense());
             // cart.setAddress(vendor.getAddress());
              cart.setLat(vendor.getLat());
              cart.setLng(vendor.getLng());
              cart.setDeliveryTime(vendor.getDeliveryTime());
              cart.setReviews(vendor.getReviews());
          }
          return cart;
      }).collect(Collectors.toList());

        double totalCartValue = getTotalCartValue(cartDTOs);
        double deliveryCharges = 0;
        preOrderResponseDTO.setTotalCartValue(totalCartValue);
        preOrderResponseDTO.setDeliveryCharges(deliveryCharges);
        preOrderResponseDTO.setAmountToPay(totalCartValue - getDiscount(cartDTOs) + deliveryCharges);
        preOrderResponseDTO.setCarts(cartDTOs);
    }

    private MedicineDTO populateMedicalDTO(MedicineDTO medicineDTO, Stock stock) {

        Medicine tempMedicine = medicineRepository.findById(medicineDTO.getId()).orElse(null);
        medicineDTO.setId(tempMedicine.getId());
        medicineDTO.setMrp(stock.getMrp());
        medicineDTO.setId(tempMedicine.getId());
        medicineDTO.setName(tempMedicine.getName());
        medicineDTO.setManufacturer(tempMedicine.getManufacturer());
        medicineDTO.setMedicineType(tempMedicine.getMedicineType());
        medicineDTO.setUseOf(tempMedicine.getUseOf());
        medicineDTO.setStrip(tempMedicine.getPacking());
        medicineDTO.setImage(Constants.LOGO_BASE_URL + Constants.MEDICINES_BASE_URL + tempMedicine.getPhoto1());
        medicineDTO.setSaltComposition(tempMedicine.getSaltComposition());
        medicineDTO.setPhoto1(Constants.LOGO_BASE_URL + Constants.MEDICINES_BASE_URL + tempMedicine.getPhoto1());
        medicineDTO.setDiscount(stock.getDiscount());
        medicineDTO.setActualPrice(stock.getMrp());
        medicineDTO.setExpiryDate(stock.getExpiryDate());
        return medicineDTO;
    }

    private MedicineDTO populateUnavailableMedicalDTO(MedicineDTO medicineDTO) {
        // Create a copy of the medicine DTO with default values indicating unavailability
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
        unavailableMedicine.setExpiryDate("Not Available");

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
                .mapToDouble(medicine -> (medicine.getMrp() * medicine.getQty()) * medicine.getDiscount() / 100.0)
                .sum();
    }


    public PreOrderDTO calculateAmountToPay(PreOrderDTO preOrderResponseDTO) {
        List<CartDTO> carts = preOrderResponseDTO.getCarts();
        double amountToPay = carts.stream()
                .flatMap(cart -> cart.getMedicine().stream()
                        .map(medicine -> {
                            long vendorId = cart.getVendorId();
                            long medicineId = medicine.getId();
                            int qty = medicine.getQty();

                            // Use the new method that returns a List to handle multiple stocks
                            List<Stock> stocks = stockRepository.findStocksByMedicineIdAndVendorId(medicineId, vendorId);
                            if(!stocks.isEmpty()){
                                Stock stock = stocks.get(0);
                                double price = stock.getMrp();
                                double discountPercentage = stock.getDiscount();
                                double totalCartValue=  price * qty;
                                totalCartValue = totalCartValue - (totalCartValue * discountPercentage / 100.0);

                                return totalCartValue;
                            } else {
                                return 0.0;
                            }
                        }))
                .mapToDouble(Double::doubleValue) // Map to double for summing
                .sum(); // Calculate total value
        preOrderResponseDTO.setAmountToPay(amountToPay);
        return preOrderResponseDTO;
    }

    private void calculateTotalCartValue(PreOrderDTO preOrderResponseDTO) {

        double totalCartValue = preOrderResponseDTO.getCarts().stream()
                .flatMap(cart -> cart.getMedicine().stream()
                        .map(medicine -> {
                            long vendorId = cart.getVendorId();
                            long medicineId = medicine.getId();
                            int qty = medicine.getQty();

                            // Use the new method that returns a List to handle multiple stocks
                            List<Stock> stocks = stockRepository.findStocksByMedicineIdAndVendorId(medicineId, vendorId);
                            if(!stocks.isEmpty()){
                                // Use the first stock if multiple exist
                                Stock stock = stocks.get(0);
                                double price = stock.getMrp();
                                medicine.setMrp(price);
                                double discountPercentage = stock.getDiscount();
                                double tempTotalCartValue =  price * qty;
                                return  tempTotalCartValue;
                            }else {
                                return  0.0;
                            }
                        }))
                .mapToDouble(Double::doubleValue) // Map to double for summing
                .sum(); // Calculate total value
        preOrderResponseDTO.setTotalCartValue(totalCartValue);

    }

}