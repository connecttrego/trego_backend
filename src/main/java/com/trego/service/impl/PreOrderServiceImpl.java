package com.trego.service.impl;

import com.google.gson.Gson;
import com.trego.dao.entity.*;
import com.trego.dao.impl.*;
import com.trego.dto.MedicineDTO;
import com.trego.dto.PreOrderDTO;
import com.trego.dto.response.CartResponseDTO;
import com.trego.dto.response.PreOrderResponseDTO;
import com.trego.dto.response.VendorCartResponseDTO;
import com.trego.service.IPreOrderService;
import com.trego.utils.Constants;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
    @Transactional
    public PreOrderResponseDTO savePreOrder(PreOrderDTO preOrderRequest) {

        Gson gson = new Gson();

        // ✅ Step 1: Log incoming vendorId
        System.out.println("🎯 Incoming selectedVendorId from frontend: " + preOrderRequest.getSelectedVendorId());

        // ✅ Step 2: If vendorId missing, try reading from cart
        if ((preOrderRequest.getSelectedVendorId() == null || preOrderRequest.getSelectedVendorId() <= 0)
                && preOrderRequest.getCarts() != null && !preOrderRequest.getCarts().isEmpty()) {

            Long vendorIdFromCart = preOrderRequest.getCarts().get(0).getVendorId();
            preOrderRequest.setSelectedVendorId(vendorIdFromCart);
            System.out.println("🟡 Auto-set vendor ID from cart payload: " + vendorIdFromCart);
        }

        // ✅ Step 3: Calculate totals before saving
        calculateTotalCartValue(preOrderRequest);
        calculateAmountToPay(preOrderRequest);

        // ✅ Step 4: Fetch existing unpaid PreOrder
        List<PreOrder> preOrders =
                preOrderRepository.findByUserIdAndPaymentStatus(preOrderRequest.getUserId(), "unpaid");
        PreOrder preOrder = (preOrders != null && !preOrders.isEmpty()) ? preOrders.get(0) : null;

        // ✅ Step 5: Create new PreOrder if not found
        if (preOrder == null) {
            preOrder = new PreOrder();
            preOrder.setPaymentStatus("unpaid");
            preOrder.setOrderStatus("new");
            preOrder.setUserId(preOrderRequest.getUserId());
            preOrder.setCreatedBy("SYSTEM");
            preOrder.setMobileNo(preOrderRequest.getMobileNo());
            preOrder.setAddressId(preOrderRequest.getAddressId());
            System.out.println("🆕 Creating new PreOrder record for user: " + preOrderRequest.getUserId());
        }

        // ✅ Step 6: Vendor selection logic (final priority)
        Long finalVendorId = null;

        if (preOrderRequest.getSelectedVendorId() != null && preOrderRequest.getSelectedVendorId() > 0) {
            finalVendorId = preOrderRequest.getSelectedVendorId();
            System.out.println("🟢 Vendor ID received from frontend/switch: " + finalVendorId);
        } else if (preOrderRequest.getCarts() != null && !preOrderRequest.getCarts().isEmpty()) {
            finalVendorId = preOrderRequest.getCarts().get(0).getVendorId();
            System.out.println("🟡 Vendor ID picked from first cart: " + finalVendorId);
        } else if (preOrder.getSelectedVendorId() != null && preOrder.getSelectedVendorId() > 0) {
            finalVendorId = preOrder.getSelectedVendorId();
            System.out.println("🔵 Vendor ID reused from previous PreOrder: " + finalVendorId);
        } else {
            System.out.println("⚠️ No vendor ID found at all! Saving as NULL");
        }

        // ✅ Step 7: Assign vendorId to DTO and Entity
        preOrderRequest.setSelectedVendorId(finalVendorId);
        preOrder.setSelectedVendorId(finalVendorId);

        // ✅ Step 8: Save payload JSON
        preOrder.setPayload(gson.toJson(preOrderRequest));

        // ✅ Step 9: Save PreOrder in DB
        preOrderRepository.save(preOrder);
        System.out.println("✅ Saved PreOrder ID: " + preOrder.getId() + " | VendorID: " + finalVendorId);

        // ✅ Step 10: Prepare response object
        PreOrderResponseDTO preOrderResponseDTO = gson.fromJson(preOrder.getPayload(), PreOrderResponseDTO.class);
        preOrderResponseDTO.setOrderId(preOrder.getId());

        // ✅ Step 11: Populate cart details for frontend
        populateCartResponse(preOrderResponseDTO);

        return preOrderResponseDTO;
    }



    @Override
    public PreOrderResponseDTO getOrdersByUserId(Long userId) {
        Gson gson = new Gson();
        PreOrderResponseDTO preOrderResponseDTO = new PreOrderResponseDTO();
        List<PreOrder> preOrders = preOrderRepository.findByUserIdAndPaymentStatus(userId, "unpaid");
        if (preOrders != null && !preOrders.isEmpty()) {
            PreOrder tempPreOrder = preOrders.get(0);
            preOrderResponseDTO = gson.fromJson(tempPreOrder.getPayload(), PreOrderResponseDTO.class);
            preOrderResponseDTO.setOrderId(tempPreOrder.getId());
            populateCartResponse(preOrderResponseDTO);
        }
        return preOrderResponseDTO;
    }

    @Override
    public VendorCartResponseDTO vendorSpecificPrice(long orderId) {
        PreOrder preOrder = preOrderRepository.findById(orderId).orElse(null);
        VendorCartResponseDTO vendorCartResponseDTO = new VendorCartResponseDTO();

        if (preOrder == null) {
            // defensive: return empty dto (controller handles empty / bad request)
            System.out.println("⚠️ vendorSpecificPrice: No PreOrder found for id=" + orderId);
            return vendorCartResponseDTO;
        }

        vendorCartResponseDTO.setUserId(preOrder.getUserId());
        vendorCartResponseDTO.setOrderId(orderId);

        // ✅ NEW: set the preOrderId so downstream bucket logic can persist selected vendor
        vendorCartResponseDTO.setPreOrderId(preOrder.getId());

        // ✅ NEW: if PreOrder already has a selected vendor, pass it down
        if (preOrder.getSelectedVendorId() != null && preOrder.getSelectedVendorId() > 0) {
            vendorCartResponseDTO.setSelectedVendorId(preOrder.getSelectedVendorId());
        }

        Gson gson = new Gson();
        PreOrderResponseDTO preOrderResponseDTO = gson.fromJson(preOrder.getPayload(), PreOrderResponseDTO.class);
        preOrderResponseDTO.setOrderId(preOrder.getId());

        // Load medicines (unchanged)
        List<Medicine> allMedicines = medicineRepository.findAllById(
                preOrderResponseDTO.getCarts().stream()
                        .flatMap(cart -> cart.getMedicine().stream())
                        .map(MedicineDTO::getId)
                        .distinct()
                        .collect(Collectors.toList())
        );

        // Build carts with vendor details (unchanged logic)
        List<CartResponseDTO> cartDTOs = preOrderResponseDTO.getCarts().stream().map(cart -> {
            List<MedicineDTO> medicines = cart.getMedicine().stream().map(medicine -> {
                List<Stock> stocks = stockRepository.findStocksByMedicineIdAndVendorId(medicine.getId(), cart.getVendorId());
                if (!stocks.isEmpty()) {
                    return populateMedicalDTO(medicine, stocks.get(0));
                } else {
                    return populateUnavailableMedicalDTO(medicine);
                }
            }).collect(Collectors.toList());

            cart.setMedicine(medicines);
            Vendor vendor = vendorRepository.findById(cart.getVendorId()).orElse(null);
            if (vendor != null) {
                cart.setVendorId(vendor.getId());
                cart.setName(vendor.getName());
                cart.setLogo(Constants.LOGO_BASE_URL + (vendor.getCategory().equalsIgnoreCase("retail")
                        ? Constants.OFFLINE_BASE_URL : Constants.ONLINE_BASE_URL) + vendor.getLogo());
                cart.setGstNumber(vendor.getGistin());
                cart.setLicence(vendor.getDruglicense());
                cart.setLat(vendor.getLat());
                cart.setLng(vendor.getLng());
                cart.setDeliveryTime(vendor.getDeliveryTime());
                cart.setReviews(vendor.getReviews());
            }

            double totalCartValue = medicines.stream()
                    .mapToDouble(med -> med.getMrp() * med.getQty())
                    .sum();
            double discount = medicines.stream()
                    .mapToDouble(med -> (med.getMrp() * med.getQty()) * med.getDiscount() / 100.0)
                    .sum();

            cart.setTotalCartValue(totalCartValue);
            cart.setAmountToPay(totalCartValue - discount);
            return cart;
        }).collect(Collectors.toList());

        vendorCartResponseDTO.setCarts(cartDTOs);
        return vendorCartResponseDTO;
    }


    private void populateCartResponse(PreOrderResponseDTO preOrderResponseDTO) {
        List<CartResponseDTO> cartDTOs = preOrderResponseDTO.getCarts().stream().map(cart -> {
            List<MedicineDTO> medicines = cart.getMedicine().stream().map(medicine -> {
                List<Stock> stocks = stockRepository.findStocksByMedicineIdAndVendorId(medicine.getId(), cart.getVendorId());
                if (!stocks.isEmpty()) {
                    return populateMedicalDTO(medicine, stocks.get(0));
                } else {
                    return populateUnavailableMedicalDTO(medicine);
                }
            }).collect(Collectors.toList());

            cart.setMedicine(medicines);
            Vendor vendor = vendorRepository.findById(cart.getVendorId()).orElse(null);
            if (vendor != null) {
                cart.setVendorId(vendor.getId());
                cart.setName(vendor.getName());
                cart.setLogo(vendor.getLogo());
                cart.setGstNumber(vendor.getGistin());
                cart.setLicence(vendor.getDruglicense());
                cart.setLat(vendor.getLat());
                cart.setLng(vendor.getLng());
                cart.setDeliveryTime(vendor.getDeliveryTime());
                cart.setReviews(vendor.getReviews());
            }
            return cart;
        }).collect(Collectors.toList());

        double totalCartValue = getTotalCartValue(cartDTOs);
        preOrderResponseDTO.setTotalCartValue(totalCartValue);
        preOrderResponseDTO.setAmountToPay(totalCartValue - getDiscount(cartDTOs));
        preOrderResponseDTO.setCarts(cartDTOs);
    }

    private MedicineDTO populateMedicalDTO(MedicineDTO medicineDTO, Stock stock) {
        Medicine tempMedicine = medicineRepository.findById(medicineDTO.getId()).orElse(null);
        medicineDTO.setId(tempMedicine.getId());
        medicineDTO.setMrp(stock.getMrp());
        medicineDTO.setName(tempMedicine.getName());
        medicineDTO.setManufacturer(tempMedicine.getManufacturer());
        medicineDTO.setMedicineType(tempMedicine.getMedicineType());
        medicineDTO.setUseOf(tempMedicine.getUseOf());
        medicineDTO.setStrip(tempMedicine.getPacking());
        medicineDTO.setImage(Constants.LOGO_BASE_URL + Constants.MEDICINES_BASE_URL + tempMedicine.getPhoto1());
        medicineDTO.setDiscount(stock.getDiscount());
        medicineDTO.setActualPrice(stock.getMrp());
        medicineDTO.setExpiryDate(stock.getExpiryDate());
        return medicineDTO;
    }

    private MedicineDTO populateUnavailableMedicalDTO(MedicineDTO medicineDTO) {
        MedicineDTO unavailable = new MedicineDTO();
        unavailable.setId(medicineDTO.getId());
        unavailable.setName(medicineDTO.getName());
        unavailable.setMrp(0.0);
        unavailable.setDiscount(0.0);
        unavailable.setQty(0);
        unavailable.setActualPrice(0.0);
        unavailable.setExpiryDate("Not Available");
        return unavailable;
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
                .mapToDouble(med -> (med.getMrp() * med.getQty()) * med.getDiscount() / 100.0)
                .sum();
    }

    public PreOrderDTO calculateAmountToPay(PreOrderDTO preOrderResponseDTO) {
        double amountToPay = preOrderResponseDTO.getCarts().stream()
                .flatMap(cart -> cart.getMedicine().stream()
                        .map(med -> {
                            List<Stock> stocks = stockRepository.findStocksByMedicineIdAndVendorId(med.getId(), cart.getVendorId());
                            if (!stocks.isEmpty()) {
                                Stock stock = stocks.get(0);
                                double price = stock.getMrp();
                                double discount = stock.getDiscount();
                                double total = price * med.getQty();
                                return total - (total * discount / 100.0);
                            } else {
                                return 0.0;
                            }
                        }))
                .mapToDouble(Double::doubleValue)
                .sum();
        preOrderResponseDTO.setAmountToPay(amountToPay);
        return preOrderResponseDTO;
    }

    private void calculateTotalCartValue(PreOrderDTO preOrderResponseDTO) {
        double total = preOrderResponseDTO.getCarts().stream()
                .flatMap(cart -> cart.getMedicine().stream()
                        .map(med -> {
                            List<Stock> stocks = stockRepository.findStocksByMedicineIdAndVendorId(med.getId(), cart.getVendorId());
                            if (!stocks.isEmpty()) {
                                double price = stocks.get(0).getMrp();
                                return price * med.getQty();
                            } else return 0.0;
                        }))
                .mapToDouble(Double::doubleValue)
                .sum();
        preOrderResponseDTO.setTotalCartValue(total);
    }
}
