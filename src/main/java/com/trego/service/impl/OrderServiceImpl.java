package com.trego.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.trego.dao.entity.*;
import com.trego.dao.impl.*;
import com.trego.dto.*;
import com.trego.dto.response.*;
import com.trego.service.IBucketService;
import com.trego.service.IOrderService;
import com.trego.service.IPreOrderService;
import com.trego.utils.Constants;

import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PreOrderRepository preOrderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private MasterMedicineRepository masterMedicineRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private IPreOrderService preOrderService;

    @Autowired
    private IBucketService bucketService;

    @Override
    public OrderResponseDTO placeOrder(OrderRequestDTO orderRequest) throws Exception {
        OrderResponseDTO orderResponseDTO = new OrderResponseDTO();

        orderResponseDTO.setUserId(orderRequest.getUserId());
        PreOrder preOrder = preOrderRepository.findById(orderRequest.getPreOrderId())
                .orElseThrow(() -> new IllegalArgumentException("PreOrder with ID " + orderRequest.getPreOrderId() + " not found."));

        Gson gson = new Gson();
        PreOrderResponseDTO preOrderResponseDTO = gson.fromJson(preOrder.getPayload(), PreOrderResponseDTO.class);
        preOrderResponseDTO.setOrderId(preOrder.getId());

        // If a specific vendor is selected, filter the carts to only include that
        // vendor
        if (orderRequest.getSelectedVendorId() != null) {
            System.out.println("Filtering carts for selected vendor ID: " + orderRequest.getSelectedVendorId());
            List<CartResponseDTO> filteredCarts = preOrderResponseDTO.getCarts().stream()
                    .filter(cart -> cart.getVendorId().equals(orderRequest.getSelectedVendorId()))
                    .collect(Collectors.toList());
            preOrderResponseDTO.setCarts(filteredCarts);

            // Store the selected vendor ID in the preorder for later use during validation
            preOrder.setSelectedVendorId(orderRequest.getSelectedVendorId());
        }

        // Dynamically recalculate cart response amounts (such as amountToPay) to ensure it is accurate and up-to-date
        populateCartResponse(preOrderResponseDTO);

        String razorpayOrderId = null;
        if (StringUtils.isEmpty(preOrder.getRazorpayOrderId())
                || preOrderResponseDTO.getAmountToPay() == null
                || preOrder.getTotalPayAmount() == null
                || preOrderResponseDTO.getAmountToPay() <= 0.0
                || !preOrderResponseDTO.getAmountToPay()
                        .equals(preOrder.getTotalPayAmount())) {

            razorpayOrderId = createRazorPayOrder(orderRequest, preOrderResponseDTO);
            preOrder.setRazorpayOrderId(razorpayOrderId);

            preOrder.setTotalPayAmount(preOrderResponseDTO.getAmountToPay());
            preOrder.setPaymentStatus("unpaid");
            preOrder.setOrderType(0);
            preOrder.setAddressId(orderRequest.getAddressId());

            preOrderRepository.save(preOrder);

        } else {
            razorpayOrderId = preOrder.getRazorpayOrderId();
        }

        orderResponseDTO.setRazorpayOrderId(razorpayOrderId);
        orderResponseDTO.setAmountToPay(preOrderResponseDTO.getAmountToPay());

        return orderResponseDTO;
    }

    @Override
    public OrderResponseDTO placeOrderFromBucket(BucketOrderRequestDTO bucketOrderRequest) throws Exception {
        System.out.println("Placing bucket order for user ID: " + bucketOrderRequest.getUserId() +
                ", PreOrder ID: " + bucketOrderRequest.getPreOrderId() +
                ", Bucket ID: " + bucketOrderRequest.getBucketId());
        OrderResponseDTO orderResponseDTO = new OrderResponseDTO();

        // Set user ID
        orderResponseDTO.setUserId(bucketOrderRequest.getUserId());

        // Get the original preorder to recreate buckets
        PreOrder originalPreOrder = preOrderRepository.findById(bucketOrderRequest.getPreOrderId()).orElse(null);
        if (originalPreOrder == null) {
            System.out.println("Original preorder not found for ID: " + bucketOrderRequest.getPreOrderId());
            throw new Exception("Original preorder not found");
        }

        try {
            // Recreate buckets from the original preorder
            VandorCartResponseDTO vendorCartData = preOrderService
                    .vendorSpecificPrice(bucketOrderRequest.getPreOrderId());

            // Check if carts is null and handle it
            if (vendorCartData.getCarts() == null) {
                System.out.println("No cart data found in preorder");
                throw new Exception("No cart data found in preorder");
            }

            System.out.println("Number of carts in vendorCartData: " + vendorCartData.getCarts().size());

            List<BucketDTO> buckets = bucketService.createOptimizedBucketsFromPreorder(vendorCartData);
            System.out.println("Created " + buckets.size() + " buckets");

            // Find the selected bucket
            BucketDTO selectedBucket = buckets.stream()
                    .filter(bucket -> bucket.getId().equals(bucketOrderRequest.getBucketId()))
                    .findFirst()
                    .orElse(null);

            if (selectedBucket == null) {
                System.out.println("Selected bucket not found for ID: " + bucketOrderRequest.getBucketId());
                throw new Exception("Selected bucket not found");
            }

            System.out.println("Selected bucket vendor ID: " + selectedBucket.getVendorId());

            // Use the exact amount from the bucket to ensure consistency
            Double bucketAmount = selectedBucket.getAmountToPay(); // This is the final amount after discount
            Double bucketDiscount = selectedBucket.getTotalDiscount(); // Total discount across all items
            Double originalTotal = selectedBucket.getTotalPrice(); // Original price before discount

            // Create a PreOrder entity for the bucket-based order
            PreOrder preOrder = originalPreOrder;
            preOrder.setAddressId(bucketOrderRequest.getAddressId());
            preOrder.setPaymentStatus("unpaid");
            preOrder.setTotalPayAmount(bucketAmount);
            preOrder.setModifiedBy("SYSTEM"); // optional if you track modification
            preOrder.setSelectedVendorId(selectedBucket.getVendorId());

            // Create a payload with bucket information
            PreOrderResponseDTO preOrderResponseDTO = new PreOrderResponseDTO();
            preOrderResponseDTO.setUserId(bucketOrderRequest.getUserId());
            preOrderResponseDTO.setAmountToPay(bucketAmount); // Final amount to pay after discount
            preOrderResponseDTO.setTotalCartValue(originalTotal); // Original price before discount
            preOrderResponseDTO.setDiscount(bucketDiscount); // Total discount amount

            // For bucket orders, we need to create a cart for the selected vendor only
            CartResponseDTO bucketCart = new CartResponseDTO();
            bucketCart.setVendorId(selectedBucket.getVendorId());
            bucketCart.setTotalCartValue(originalTotal);
            bucketCart.setAmountToPay(bucketAmount);
            bucketCart.setDiscount(bucketDiscount);

            // Convert bucket items to medicine DTOs
            List<MedicineDTO> medicineDTOs = new ArrayList<>();
            if (selectedBucket.getAvailableItems() != null) {
                for (BucketItemDTO bucketItem : selectedBucket.getAvailableItems()) {
                    MedicineDTO medicineDTO = new MedicineDTO();
                    medicineDTO.setId(bucketItem.getMedicineId());
                    medicineDTO.setName(bucketItem.getMedicineName());
                    medicineDTO.setStrip(bucketItem.getMedicineStrip());
                    medicineDTO.setMrp(bucketItem.getPrice()); // Discounted price
                    medicineDTO.setDiscount(bucketItem.getDiscount());
                    medicineDTO.setQty(bucketItem.getRequestedQuantity());
                    // Set medicine image from bucket item or fallback
                    String medicineImage = (bucketItem.getMedicineImage() != null && !bucketItem.getMedicineImage().isEmpty())
                            ? bucketItem.getMedicineImage()
                            : Constants.getMedicineImageWithFallback("");
                    medicineDTO.setPhoto1(medicineImage);
                    medicineDTO.setImage(medicineImage);
                    // Calculate original price before discount
                    Double price = bucketItem.getPrice();
                    Double discount = bucketItem.getDiscount();

                    Double originalPrice;

                    if (discount != null && discount > 0.0) {
                        Double discountFactor = 1.0 - (discount / 100.0);
                        if (discountFactor == 0.0) {
                            originalPrice = price; // fallback safety
                        } else {
                            originalPrice = price / discountFactor;
                        }
                    } else {
                        originalPrice = price;
                    }
                    medicineDTO.setActualPrice(originalPrice); // Original price before discount
                    medicineDTOs.add(medicineDTO);
                }
            }
            bucketCart.setMedicine(medicineDTOs);

            preOrderResponseDTO.setCarts(Arrays.asList(bucketCart));

            System.out.println("Created bucket cart with vendor ID: " + bucketCart.getVendorId() +
                    " and " + medicineDTOs.size() + " medicines");

            // Convert to JSON and set as payload BEFORE saving
            Gson gson = new Gson();
            String payload = gson.toJson(preOrderResponseDTO);
            preOrder.setVendorPayload(payload);
            preOrder.setOrderType(1);

            // Save the preorder
            // preOrderRepository.save(preOrder);
            System.out.println("Updated existing PreOrder ID: " + preOrder.getId());

            // Add the order ID to the response DTO
            preOrderResponseDTO.setOrderId(preOrder.getId());

            // Generate RazorPay order using the exact same amount
            String razorpayOrderId = createRazorPayOrderForBucket(bucketOrderRequest, preOrderResponseDTO);
            preOrder.setRazorpayOrderId(razorpayOrderId);

            // Save the updated preorder
            preOrderRepository.save(preOrder);

            orderResponseDTO.setRazorpayOrderId(razorpayOrderId);
            orderResponseDTO.setAmountToPay(bucketAmount);
            orderResponseDTO.setOrderId(preOrder.getId());
        } catch (Exception e) {
            // Log the exception for debugging
            System.err.println("Error in placeOrderFromBucket: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return orderResponseDTO;
    }

    @Override
    @Transactional
    public OrderValidateResponseDTO validateOrder(OrderValidateRequestDTO orderValidateRequestDTO) throws Exception {
        System.out.println("Validating order with Order ID: " + orderValidateRequestDTO.getOrderId());
        OrderValidateResponseDTO validateResponseDTO = new OrderValidateResponseDTO();
        boolean isValidate = verifyRazorPayOrder(orderValidateRequestDTO);

        if (!isValidate) {
            validateResponseDTO.setValidate(false);
            validateResponseDTO.setRazorpayOrderId(orderValidateRequestDTO.getRazorpayOrderId());
            validateResponseDTO.setRazorpayPaymentId(orderValidateRequestDTO.getRazorpayPaymentId());
            return validateResponseDTO;
        }

        // validated by payment gateway
        PreOrder preOrder = preOrderRepository.findById(orderValidateRequestDTO.getOrderId()).orElse(null);
        if (preOrder == null) {
            throw new Exception("PreOrder not found: " + orderValidateRequestDTO.getOrderId());
        }

        Integer orderType = preOrder.getOrderType() != null ? preOrder.getOrderType() : 0;
        System.out.println("Found PreOrder ID: " + preOrder.getId() + " with payment status: "
                + preOrder.getPaymentStatus() + "  OrderType is " + (orderType == 1));
        preOrder.setPaymentStatus("paid");

        Gson gson = new Gson();
        String payload = orderType == 1 ? preOrder.getVendorPayload() : preOrder.getPayload();
        PreOrderResponseDTO preOrderResponseDTO = gson.fromJson(payload, PreOrderResponseDTO.class);
        preOrderResponseDTO.setOrderId(preOrder.getId());

        // Ensure address ID preserved
        if (preOrderResponseDTO.getAddressId() == 0) {
            preOrderResponseDTO.setAddressId(preOrder.getAddressId());
        }

        // Ensure user ID preserved (fixes NoSuchElementException when userId is missing from payload)
        if (preOrderResponseDTO.getUserId() == 0) {
            preOrderResponseDTO.setUserId(preOrder.getUserId());
        }

        if (orderType == 1) {
            System.out.println(
                    "Selected Vendor ID found: " + preOrder.getSelectedVendorId() + ". Processing as BUCKET ORDER.");
            processBucketOrder(preOrder, preOrderResponseDTO);
        } else if (preOrderResponseDTO.getCarts() != null && preOrderResponseDTO.getCarts().size() > 1) {
            System.out.println("Detected MULTI-VENDOR cart (" + preOrderResponseDTO.getCarts().size()
                    + " carts). Processing as REGULAR ORDER.");
            processRegularOrder(preOrder, preOrderResponseDTO);
        } else {
            System.out.println("Single vendor but no selectedVendorId found. Processing as BUCKET ORDER.");
            processRegularOrder(preOrder, preOrderResponseDTO);
        }

        // IMPORTANT: flush order items so subsequent reads (findById) will see saved
        // items
        // orderItemRepository is JpaRepository and has flush() through JpaRepository
        try {
            orderItemRepository.flush();
        } catch (Exception e) {
            // flush may not be necessary in some setups, but best-effort
            System.out.println("Warning: flush failed: " + e.getMessage());
        }

        // If front-end passed a prescriptionUrl directly (maybe mobile attached file
        // url), prefer that:
        if (orderValidateRequestDTO.getPrescriptionUrl() != null
                && !orderValidateRequestDTO.getPrescriptionUrl().isBlank()) {
            System.out.println("Prescription URL provided in request -> saving.");
            saveRxPrescriptionData(preOrder, orderValidateRequestDTO.getPrescriptionUrl());
        } else {
            // Try find uploaded attachment(s) and save records
            // Some clients upload attachment with orderId = preOrderId (as you said). We'll
            // try multiple lookups.
            List<Attachment> attachmentsByOrder = new ArrayList<>();

            // 1) attachments with order_id = each saved order id (preferred)
            if (preOrder.getOrders() != null) {
                for (Order ord : preOrder.getOrders()) {
                    if (ord.getId() != null) {
                        List<Attachment> byOrd = attachmentRepository.findByOrderId(ord.getId());
                        if (byOrd != null && !byOrd.isEmpty()) {
                            attachmentsByOrder.addAll(byOrd);
                        }
                    }
                }
            }

            // 2) if nothing, try attachments where order_id == preOrder.id (frontend may
            // have set that)
            if (attachmentsByOrder.isEmpty()) {
                List<Attachment> byPreOrderId = attachmentRepository.findByOrderId(preOrder.getId());
                if (byPreOrderId != null && !byPreOrderId.isEmpty()) {
                    attachmentsByOrder.addAll(byPreOrderId);
                }
            }

            // 3) fallback: attachments uploaded by user (filter description or recency)
            if (attachmentsByOrder.isEmpty()) {
                List<Attachment> byUser = attachmentRepository.findByUserId(preOrder.getUserId());
                if (byUser != null && !byUser.isEmpty()) {
                    // pick attachments whose description contains PRESCRIPTION or most recent ones
                    List<Attachment> filtered = byUser.stream()
                            .filter(a -> a.getDescription() != null
                                    && a.getDescription().toLowerCase().contains("prescription"))
                            .collect(Collectors.toList());
                    if (!filtered.isEmpty())
                        attachmentsByOrder.addAll(filtered);
                    else {
                        // no explicit description — pick latest 1-2
                        byUser.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
                        if (!byUser.isEmpty())
                            attachmentsByOrder.add(byUser.get(0));
                    }
                }
            }

            // If we found attachments, attach them to orders
            if (attachmentsByOrder.isEmpty()) {
                System.out.println("❌ No attachments found to save as prescription for PreOrder: " + preOrder.getId());
            } else {
                System.out.println("Found attachments: " + attachmentsByOrder.size());
                // Prefer mapping: for each final Order, find attachments with orderId matching
                // order.id OR preOrderId in order field
                // We'll call helper that uses order->items to fill medicine_ids
                saveRxPrescriptionDataUsingAttachments(preOrder, attachmentsByOrder);
            }
        }

        // Save preOrder final state
        preOrderRepository.save(preOrder);

        validateResponseDTO.setValidate(true);
        validateResponseDTO.setRazorpayOrderId(orderValidateRequestDTO.getRazorpayOrderId());
        validateResponseDTO.setRazorpayPaymentId(orderValidateRequestDTO.getRazorpayPaymentId());
        return validateResponseDTO;
    }

    private void saveRxPrescriptionData(PreOrder preOrder, String prescriptionUrl) {

        try {

            if (prescriptionUrl == null || prescriptionUrl.isBlank()) {
                return;
            }

            // FINAL ORDERS (already created)
            List<Order> finalOrders = preOrder.getOrders();

            if (finalOrders == null || finalOrders.isEmpty()) {
                System.out.println("❌ No final orders found to save prescription.");
                return;
            }

            for (Order order : finalOrders) {

                // ⭐⭐ MOST IMPORTANT FIX ⭐⭐
                // Reload order from DB to ensure orderItems are fetched
                Order dbOrder = orderRepository.findById(order.getId()).orElse(null);

                if (dbOrder == null) {
                    continue;
                }

                List<OrderItem> items = dbOrder.getOrderItems();

                if (items == null || items.isEmpty()) {
                    System.out.println("❌ OrderItems EMPTY for OrderId: " + order.getId());
                    continue;
                }

                // Collect Medicine IDs
                List<Long> medIds = items.stream()
                        .map(i -> i.getMedicineId())
                        .toList();

                PrescriptionRecord record = new PrescriptionRecord();
                record.setUserId(preOrder.getUserId());
                record.setOrderId(order.getId());

                record.setPrescriptionUrl(prescriptionUrl);
                record.setMedicineIds(medIds.toString());

                prescriptionRepository.save(record);

                System.out.println("Prescription saved with medicines: " + medIds);

            }

        } catch (Exception e) {
            System.out.println("❌ Error saving prescription: " + e.getMessage());
        }
    }

    private void saveRxPrescriptionDataUsingAttachments(PreOrder preOrder, List<Attachment> attachments) {
        try {
            if (attachments == null || attachments.isEmpty())
                return;

            // For each final order, try to find attachments that belong to that order
            // (orderId matches),
            // otherwise use preOrder-level attachments.
            List<Order> finalOrders = preOrder.getOrders();
            if (finalOrders == null || finalOrders.isEmpty()) {
                System.out.println("No final orders to attach prescriptions");
                return;
            }

            for (Order order : finalOrders) {
                Long orderId = order.getId();

                // pick attachments that match this order first, else those matching preOrder
                // id, else user-level ones
                List<Attachment> chosen = attachments.stream()
                        .filter(a -> a.getOrderId() != null && a.getOrderId().equals(orderId))
                        .collect(Collectors.toList());

                if (chosen.isEmpty()) {
                    chosen = attachments.stream()
                            .filter(a -> a.getOrderId() != null && a.getOrderId().equals(preOrder.getId()))
                            .collect(Collectors.toList());
                }

                if (chosen.isEmpty()) {
                    // finally pick attachments uploaded by same user (already possibly provided)
                    chosen = attachments.stream()
                            .filter(a -> a.getUserId() != null && a.getUserId().equals(preOrder.getUserId()))
                            .collect(Collectors.toList());
                }

                if (chosen.isEmpty()) {
                    System.out.println("No attachment selected for order " + orderId);
                    continue;
                }

                // pick latest attachment from chosen
                chosen.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
                Attachment pick = chosen.get(0);
                String prescriptionUrl = pick.getFileUrl();

                // RELOAD DB ORDER to ensure orderItems exist (and use orderItemRepository as
                // fallback)
                Order dbOrder = orderRepository.findById(orderId).orElse(null);
                List<OrderItem> items = null;
                if (dbOrder != null) {
                    items = dbOrder.getOrderItems();
                }
                if (items == null || items.isEmpty()) {
                    // fallback to repo finder
                    items = orderItemRepository.findByOrderId(orderId);
                }

                if (items == null || items.isEmpty()) {
                    System.out.println("❌ No order items for order " + orderId + ", cannot save medicine IDs");
                    // still save record with URL and preOrder/order mapping so audit exists
                    PrescriptionRecord rec = new PrescriptionRecord();
                    rec.setUserId(preOrder.getUserId());
                    rec.setOrderId(orderId);

                    rec.setPrescriptionUrl(prescriptionUrl);
                    rec.setMedicineIds("[]");
                    prescriptionRepository.save(rec);
                    continue;
                }

                List<Long> medIds = items.stream()
                        .map(i -> i.getMedicineId())
                        .collect(Collectors.toList());

                PrescriptionRecord record = new PrescriptionRecord();
                record.setUserId(preOrder.getUserId());
                record.setOrderId(orderId);

                record.setPrescriptionUrl(prescriptionUrl);
                record.setMedicineIds(medIds.toString());

                prescriptionRepository.save(record);

            }

        } catch (Exception e) {
            System.out.println("Error saving prescriptions: " + e.getMessage());
        }
    }

    @Override
    public Page<OrderResponseDTO> fetchAllOrders(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PreOrder> preOrdersList = preOrderRepository.fetchAllOrdersByUserId(userId, pageable);
        // Map PreOrder entities to OrderResponseDTO
        Page<OrderResponseDTO> responseDTOPage = preOrdersList.map(preOrder -> {
            OrderResponseDTO responseDTO = new OrderResponseDTO();

            Gson gson = new Gson();
            PreOrderResponseDTO preOrderResponseDTO = gson.fromJson(preOrder.getPayload(), PreOrderResponseDTO.class);
            responseDTO.setUserId(preOrder.getUserId());
            responseDTO.setRazorpayOrderId(preOrder.getRazorpayOrderId());
            responseDTO.setOrderId(preOrder.getId());
            responseDTO.setMobileNo(preOrder.getMobileNo());
            Optional<Address> addressOpt = addressRepository.findById(preOrder.getAddressId());

            if (addressOpt.isPresent()) {
                Address address = addressOpt.get();
                AddressDTO addressDTO = new AddressDTO(
                        address.getId(),
                        address.getAddress(),
                        address.getCity(),
                        address.getLandmark(),
                        address.getPincode(),
                        address.getLat(),
                        address.getLng(), address.getUser().getId(), address.getMobileNo(), address.getName(),
                        address.getAddressTypeValue());
                responseDTO.setAddress(addressDTO);
            }

            responseDTO.setAmountToPay(preOrder.getTotalPayAmount());
            responseDTO.setTotalCartValue(preOrderResponseDTO.getTotalCartValue());
            responseDTO.setDiscount(preOrderResponseDTO.getDiscount());
            responseDTO.setPaymentStatus(preOrder.getPaymentStatus());
            responseDTO.setCreateDate(preOrder.getCreatedAt());

            List<OrderDTO> orderDTO = populateOrders(preOrder);
            responseDTO.setOrders(orderDTO);
            return responseDTO;
        });

        return responseDTOPage;
    }

    @Override
    public CancelOrderResponseDTO cancelOrders(CancelOrderRequestDTO request) throws Exception {
        List<Long> orderIds = request.getOrders();
        List<Long> subOrderIds = request.getSubOrders();
        if (orderIds.isEmpty() && subOrderIds.isEmpty()) {
            return new CancelOrderResponseDTO("No orders found to cancel", List.of(), List.of());
        }
        if (!orderIds.isEmpty()) {
            orderIds.forEach(orderId -> {
                System.out.println("Processing order ID: " + orderId);
                // Add logic here to update order status, fetch details, etc.
                PreOrder preOrder = preOrderRepository.findById(orderId).orElse(null);
                if (preOrder == null) {
                    System.out.println("Warning: PreOrder with ID " + orderId + " not found while cancelling orders.");
                    return;
                }
                subOrderIds.addAll(preOrder.getOrders().stream()
                        .map(Order::getId) // Assuming Order has a getId() method
                        .collect(Collectors.toList()));
                preOrderRepository.updateOrderStatus(orderIds, "cancelled");

            });

        }
        if (!subOrderIds.isEmpty()) {
            orderRepository.updateOrderStatusAndReason(subOrderIds, "cancelled", request.getReason(),
                    request.getReasonId());
        }
        return new CancelOrderResponseDTO("Orders and sub-orders cancelled successfully", orderIds, subOrderIds);
    }

    private List<OrderDTO> populateOrders(PreOrder preOrder) {
        System.out.println("Populating orders for PreOrder ID: " + preOrder.getId() + " with "
                + preOrder.getOrders().size() + " orders");
        List<OrderDTO> orderDTOList = new ArrayList<>();
        // Iterate over orders in PreOrder
        preOrder.getOrders().forEach(order -> {
            System.out
                    .println("Processing order ID: " + order.getId() + " for vendor ID: " + order.getVendor().getId());
            OrderDTO orderDTO = new OrderDTO();

            // Populate fields of OrderDTO based on Order entity
            orderDTO.setOrderId(order.getId());
            orderDTO.setPaymentStatus(order.getPaymentStatus());
            orderDTO.setOrderStatus(order.getOrderStatus());
            orderDTO.setTotalAmount(order.getTotalAmount());
            orderDTO.setAddress(order.getAddress());
            orderDTO.setPinCode(order.getPincode());
            orderDTO.setCreateDate(order.getCreatedAt());
            orderDTO.setCancelReason(order.getCancelReason());
            orderDTO.setCancelReasonId(order.getCancelReasonId());
            orderDTO.setDiscount(order.getDiscount());
            VendorDTO vendorDTO = new VendorDTO();
            vendorDTO.setId(order.getVendor().getId());
            vendorDTO.setName(order.getVendor().getName());
            vendorDTO.setLogo(Constants.getVendorLogoWithFallback(order.getVendor().getLogo()));
            vendorDTO.setLicence(order.getVendor().getDruglicense());
            vendorDTO.setGstNumber(order.getVendor().getGistin());
            vendorDTO.setAddress(order.getVendor().getAddress());
            vendorDTO.setLat(order.getVendor().getLat() != null ? order.getVendor().getLat().doubleValue() : null);
            vendorDTO.setLng(order.getVendor().getLng() != null ? order.getVendor().getLng().doubleValue() : null);
            vendorDTO.setDeliveryTime(order.getVendor().getDeliveryTime());
            vendorDTO.setReviews(order.getVendor().getReviews());
            vendorDTO.setRating(order.getVendor().getRating());

            orderDTO.setVendor(vendorDTO);

            // Populate OrderItems list
            List<OrderItemDTO> orderItemsList = new ArrayList<>();
            double totalAmount = 0.0;

            for (var orderItem : order.getOrderItems()) {
                OrderItemDTO orderItemDTO = new OrderItemDTO();
                orderItemDTO.setItemId(orderItem.getId());
                orderItemDTO.setQty(orderItem.getQty());
                orderItemDTO.setMrp(orderItem.getMrp());
                orderItemDTO.setPrice(orderItem.getSellingPrice());
                orderItemDTO.setTotalAmount(orderItem.getAmount());
                totalAmount += orderItem.getAmount();

                Map<String, Object> medicineDetails = new HashMap<>();
                medicineDetails.put("medicineId", orderItem.getMedicineId());

                Medicine medicine = medicineRepository.findById(orderItem.getMedicineId()).orElse(null);
                if (medicine != null) {
                    medicineDetails.put("medicineName", medicine.getName());
                    
                    // Retrieve master medicine for catalog fallback
                    MasterMedicine masterMed = null;
                    if (medicine.getMedicineId() != null) {
                        masterMed = masterMedicineRepository.findById(medicine.getMedicineId()).orElse(null);
                    } else if (medicine.getName() != null && !medicine.getName().trim().isEmpty()) {
                        List<MasterMedicine> matchedMeds = masterMedicineRepository.findByNameIgnoreCase(medicine.getName().trim());
                        if (matchedMeds != null && !matchedMeds.isEmpty()) {
                            masterMed = matchedMeds.get(0);
                        }
                    }

                    MedicineInformation medicineInformation = medicine.getMedicineInformation();
                    String packing = "";
                    String imageUrl = "";
                    if (medicineInformation != null) {
                        packing = medicineInformation.getPacking();
                        imageUrl = medicineInformation.getPhoto1();
                    }
                    // Apply master medicine fallback
                    if ((packing == null || packing.trim().isEmpty()) && masterMed != null && masterMed.getPackaging() != null) {
                        packing = masterMed.getPackaging();
                    }
                    if ((imageUrl == null || imageUrl.trim().isEmpty()) && masterMed != null && masterMed.getPhoto1() != null) {
                        imageUrl = masterMed.getPhoto1();
                    }
                    medicineDetails.put("packing", packing != null ? packing : "");
                    medicineDetails.put("medicineLogo", Constants.getMedicineImageWithFallback(imageUrl));
                } else {
                    medicineDetails.put("medicineName", "");
                    medicineDetails.put("packing", "");
                    medicineDetails.put("medicineLogo", "");
                }

                orderItemDTO.setMedicine(medicineDetails);
                orderItemsList.add(orderItemDTO);
            }

            orderDTO.setOrderItemsList(orderItemsList);
            orderDTO.setTotalAmount(totalAmount);
            orderDTOList.add(orderDTO);
        });

        return orderDTOList;
    }

    public boolean verifyRazorPayOrder(OrderValidateRequestDTO orderValidateRequestDTO) throws Exception {
        String keyId = "rzp_test_oZBGm1luIG1Rpl"; // Replace with actual key
        String keySecret = "S0Pxnueo7AdCYS2HFIa7LXK6"; // Replace with actual key
        String credentials = keyId + ":" + keySecret;
        String encodedAuth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        // API URL
        String url = "https://api.razorpay.com/v1/orders/" + orderValidateRequestDTO.getRazorpayOrderId() + "/payments";

        boolean isValidate = false;
        // Create HTTP Client
        HttpClient client = HttpClient.newHttpClient();
        // Create HTTP Request
        // Create HTTP Request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + encodedAuth)
                .GET() // Change method to GET
                .build();

        // Send Request
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Print Response
        System.out.println("verifyRazorPayOrder Response Code: " + response.statusCode());
        System.out.println("verifyRazorPayOrder Response Body: " + response.body());
        String orderId = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.body());

            // Access the "items" array
            JsonNode itemsNode = rootNode.get("items");
            if (itemsNode == null || !itemsNode.isArray()) {
                System.out.println("No 'items' field found or it's not an array.");
                return false;
            }

            // Iterate through the "items" array
            Iterator<JsonNode> elements = itemsNode.elements();
            while (elements.hasNext()) {
                JsonNode itemNode = elements.next();
                JsonNode idNode = itemNode.get("id");
                JsonNode amountNode = itemNode.get("amount");

                // Check if the "id" matches
                if (idNode != null && orderValidateRequestDTO.getRazorpayPaymentId().equals(idNode.asText())) {
                    System.out.println("Valid 'id' found: " + orderValidateRequestDTO.getRazorpayPaymentId());
                    isValidate = true;
                }
            }

        } catch (Exception e) {
            System.err.println("Error while parsing JSON or validating 'id': " + e.getMessage());

        }
        return isValidate;
    }

    public String createRazorPayOrder(OrderRequestDTO orderRequest, PreOrderResponseDTO preOrderResponseDTO)
            throws Exception {
        Double amount = preOrderResponseDTO.getAmountToPay();
        if (amount == null || amount <= 0) {
            throw new Exception("Invalid amount to pay: " + amount + ". Amount must be greater than 0.");
        }

        String keyId = "rzp_test_oZBGm1luIG1Rpl"; // Replace with actual key
        String keySecret = "S0Pxnueo7AdCYS2HFIa7LXK6"; // Replace with actual key
        String credentials = keyId + ":" + keySecret;
        String encodedAuth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        // API URL
        String url = "https://api.razorpay.com/v1/orders";
        // Create a JSON object
        JsonObject jsonObject = new JsonObject();
        // Add fields to the JSON object
        int convertedAmount = (int) Math.round(amount * 100);

        jsonObject.addProperty("amount", convertedAmount);
        jsonObject.addProperty("currency", "INR");
        jsonObject.addProperty("receipt", "receipt#" + System.currentTimeMillis());

        // Create a nested JSON object
        JsonObject notes = new JsonObject();
        notes.addProperty("userId", preOrderResponseDTO.getUserId());
        notes.addProperty("preOrderId", preOrderResponseDTO.getOrderId());

        // Add the nested JSON object to the main object
        jsonObject.add("notes", notes);

        // Create HTTP Client
        HttpClient client = HttpClient.newHttpClient();
        // Create HTTP Request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + encodedAuth)
                .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                .build();

        // Send Request
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Print Response
        System.out.println("createRazorPayOrder Response Code: " + response.statusCode());
        System.out.println("createRazorPayOrder Response Body: " + response.body());

        if (response.statusCode() != 200) {
            throw new Exception("RazorPay order creation failed with status " + response.statusCode()
                    + ". Response: " + response.body());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response.body());

        // Check for RazorPay error response
        if (rootNode.has("error")) {
            String errorDesc = rootNode.get("error").get("description").asText();
            throw new Exception("RazorPay error: " + errorDesc);
        }

        // Extract individual fields
        String orderId = rootNode.get("id").asText();
        if (orderId == null || orderId.isBlank()) {
            throw new Exception("RazorPay returned null/empty order ID. Response: " + response.body());
        }
        System.out.println("RazorPay Order created successfully. Order ID: " + orderId);
        return orderId;
    }

    private void populateCartResponse(PreOrderResponseDTO preOrderResponseDTO) {
        List<CartResponseDTO> cartDTOs = preOrderResponseDTO.getCarts().stream().map(cart -> {
            List<MedicineDTO> medicines = cart.getMedicine().stream()
                    .map(medicine -> {
                        List<Stock> stockList = getStocksForMedicineAndVendor(medicine.getId(),
                                cart.getVendorId());

                        return stockList.stream()
                                .findFirst() // pick first if multiple records exist
                                .map(stock -> populateMedicalDTO(medicine, stock))
                                .orElse(null);
                    })
                    .filter(Objects::nonNull) // Filters out null values from the stream
                    .collect(Collectors.toList());
            cart.setMedicine(medicines);
            return cart;
        }).collect(Collectors.toList());

        double totalCartValue = getTotalCartValue(cartDTOs);
        preOrderResponseDTO.setTotalCartValue(totalCartValue);
        preOrderResponseDTO.setAmountToPay(totalCartValue - getDiscount(cartDTOs));
        preOrderResponseDTO.setCarts(cartDTOs);
    }

    private MedicineDTO populateMedicalDTO(MedicineDTO medicineDTO, Stock stock) {

        Medicine tempMedicine = medicineRepository.findById(medicineDTO.getId()).orElse(null);
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

        // Apply master medicine fallbacks
        if (useOf == null || useOf.trim().isEmpty()) {
            useOf = (masterMed != null && masterMed.getUseOf() != null) ? masterMed.getUseOf() : "";
        }
        if (strip == null || strip.trim().isEmpty()) {
            strip = (masterMed != null && masterMed.getPackaging() != null) ? masterMed.getPackaging() : "";
        }
        if (rawPhoto == null || rawPhoto.trim().isEmpty()) {
            rawPhoto = (masterMed != null && masterMed.getPhoto1() != null) ? masterMed.getPhoto1() : "";
        }

        String photoUrl = Constants.getMedicineImageWithFallback(rawPhoto);
        medicineDTO.setUseOf(useOf);
        medicineDTO.setStrip(strip);
        medicineDTO.setImage(photoUrl);
        medicineDTO.setPhoto1(photoUrl);
        
        medicineDTO.setSaltComposition(tempMedicine.getSaltComposition() != null && !tempMedicine.getSaltComposition().trim().isEmpty() ? tempMedicine.getSaltComposition() : (masterMed != null ? masterMed.getSaltComposition() : ""));
        medicineDTO.setDiscount(stock.getDiscount());
        medicineDTO.setActualPrice(stock.getMrp());
        medicineDTO.setExpiryDate(stock.getExpiryDate());
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

                            List<Stock> stocks = stockRepository.findByMedicineIdAndVendorId(medicineId, vendorId);

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

                            List<Stock> stocks = stockRepository.findByMedicineIdAndVendorId(medicineId, vendorId);

                            if (!stocks.isEmpty()) {

                                Stock stock = stocks.get(0);

                                Double price = stock.getMrp();
                                medicine.setMrp(price);

                                return price * qty;
                            }

                            return 0.0;
                        }))
                .reduce(0.0, Double::sum);

        preOrderResponseDTO.setTotalCartValue(totalCartValue);
    }

    private Order populateOrder(PreOrderResponseDTO orderRequest) {

        Address address = addressRepository.findById(orderRequest.getAddressId()).orElse(null);
        Order order = new Order();
        User user = userRepository.findById(orderRequest.getUserId()).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found: " + orderRequest.getUserId());
        }
        if (address == null) {
            throw new RuntimeException("Address not found: " + orderRequest.getAddressId());
        }
        order.setEmail(user.getEmail());
        order.setMobile(user.getMobile());
        order.setUser(user);
        order.setPincode(address.getPincode());
        order.setLanmark(address.getLandmark());
        order.setName(user.getName());
        order.setOrderStatus("new");
        order.setCity(address.getCity());
        order.setAddress(address.getAddress()); // Assuming AddressDTO can be converted
        order.setTotalAmount(orderRequest.getAmountToPay());
        order.setPaymentMethod("other");
        order.setPaymentStatus("paid");
        order.setDiscount(orderRequest.getDiscount());

        return order;
    }

    public String createRazorPayOrderForBucket(BucketOrderRequestDTO bucketOrderRequest,
            PreOrderResponseDTO preOrderResponseDTO) throws Exception {
        Double amount = preOrderResponseDTO.getAmountToPay();
        if (amount == null || amount <= 0) {
            throw new Exception("Invalid amount to pay: " + amount + ". Amount must be greater than 0.");
        }

        String keyId = "rzp_test_oZBGm1luIG1Rpl"; // Replace with actual key
        String keySecret = "S0Pxnueo7AdCYS2HFIa7LXK6"; // Replace with actual key
        String credentials = keyId + ":" + keySecret;
        String encodedAuth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        // API URL
        String url = "https://api.razorpay.com/v1/orders";
        // Create a JSON object
        JsonObject jsonObject = new JsonObject();
        // Add fields to the JSON object
        int convertedAmount = (int) Math.round(amount * 100);

        jsonObject.addProperty("amount", convertedAmount);
        jsonObject.addProperty("currency", "INR");
        jsonObject.addProperty("receipt", "receipt#" + System.currentTimeMillis());

        // Create a nested JSON object
        JsonObject notes = new JsonObject();
        notes.addProperty("userId", preOrderResponseDTO.getUserId());
        notes.addProperty("preOrderId", preOrderResponseDTO.getOrderId());

        // Add the nested JSON object to the main object
        jsonObject.add("notes", notes);

        // Create HTTP Client
        HttpClient client = HttpClient.newHttpClient();
        // Create HTTP Request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + encodedAuth)
                .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                .build();

        // Send Request
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Print Response
        System.out.println("createRazorPayOrderForBucket Response Code: " + response.statusCode());
        System.out.println("createRazorPayOrderForBucket Response Body: " + response.body());

        if (response.statusCode() != 200) {
            throw new Exception("RazorPay order creation failed with status " + response.statusCode()
                    + ". Response: " + response.body());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response.body());

        // Check for RazorPay error response
        if (rootNode.has("error")) {
            String errorDesc = rootNode.get("error").get("description").asText();
            throw new Exception("RazorPay error: " + errorDesc);
        }

        // Extract individual fields
        String orderId = rootNode.get("id").asText();
        if (orderId == null || orderId.isBlank()) {
            throw new Exception("RazorPay returned null/empty order ID. Response: " + response.body());
        }
        System.out.println("RazorPay Order created successfully. Order ID: " + orderId);
        return orderId;
    }

    // Helper method to determine if an order is a bucket order
    public boolean isBucketOrder(PreOrder preOrder) {
        // Bucket orders are created by "SYSTEM" and have a single cart
        boolean isBucket = "SYSTEM".equals(preOrder.getCreatedBy());
        System.out.println("Checking if PreOrder ID: " + preOrder.getId() + " is bucket order. CreatedBy: "
                + preOrder.getCreatedBy() + ", IsBucket: " + isBucket);
        return isBucket;
    }

    // Process bucket orders (single vendor)
    private void processBucketOrder(PreOrder preOrder, PreOrderResponseDTO preOrderResponseDTO) {
        System.out.println("Processing bucket order for PreOrder ID: " + preOrder.getId());
        if (preOrderResponseDTO.getCarts() != null && !preOrderResponseDTO.getCarts().isEmpty()) {
            System.out.println("Number of carts in bucket order: " + preOrderResponseDTO.getCarts().size());
            CartResponseDTO selectedCart = preOrderResponseDTO.getCarts().get(0);
            System.out.println("Processing cart for vendor ID: " + selectedCart.getVendorId());

            if (preOrder.getOrders() == null) {
                preOrder.setOrders(new ArrayList<>());
            }

            Order order = populateOrder(preOrderResponseDTO);
            order.setTotalAmount(selectedCart.getAmountToPay());
            order.setDiscount(selectedCart.getDiscount());
            Vendor vendor = new Vendor();
            vendor.setId(selectedCart.getVendorId());
            order.setVendor(vendor);
            order.setPreOrder(preOrder);

            Order savedOrder = orderRepository.save(order);
            System.out
                    .println("Saved order ID: " + savedOrder.getId() + " for vendor ID: " + selectedCart.getVendorId());
            selectedCart.setOrderId(savedOrder.getId());

            preOrder.getOrders().add(savedOrder);

            List<OrderItem> orderItems = new ArrayList<>();
            if (selectedCart.getMedicine() != null) {
                System.out.println("Number of medicines in cart: " + selectedCart.getMedicine().size());
                for (MedicineDTO medicine : selectedCart.getMedicine()) {
                    OrderItem item = new OrderItem();
                    Medicine med = medicineRepository.findById(medicine.getId()).orElse(null);
                    if (med == null) {
                        System.err.println("SKIPPING order item: Medicine not found for vendorMedicineId=" + medicine.getId());
                        continue;
                    }
                    item.setMedicineId(med.getId());
                    item.setQty(medicine.getQty());
                    Double mrp = medicine.getMrp() != null ? medicine.getMrp() : 0.0;
                    Double discount = medicine.getDiscount() != null ? medicine.getDiscount() : 0.0;
                    item.setMrp(mrp);
                    item.setSellingPrice(Constants.calculateUnitPrice(mrp, discount));
                    item.setAmount(mrp * medicine.getQty());
                    item.setOrderStatus("pending");
                    item.setOrder(savedOrder);
                    orderItems.add(item);
                }
            }
            orderItemRepository.saveAll(orderItems);
            System.out.println("Saved " + orderItems.size() + " order items for order ID: " + savedOrder.getId());

            preOrderRepository.save(preOrder);
        } else {
            System.out.println("No carts found in bucket order");
        }
    }

    // Process regular orders (multiple vendors)
    private void processRegularOrder(PreOrder preOrder, PreOrderResponseDTO preOrderResponseDTO) {
        System.out.println("Processing regular order for PreOrder ID: " + preOrder.getId());

        // ✅ Step added: Calculate cart-wise total and amountToPay before saving orders
        for (CartResponseDTO cart : preOrderResponseDTO.getCarts()) {
            double totalCartValue = 0.0;
            double totalDiscount = 0.0;

            if (cart.getMedicine() != null) {
                for (var med : cart.getMedicine()) {

                    Double price = med.getMrp() != null ? med.getMrp() : 0.0;
                    int qty = med.getQty();
                    Double discount = med.getDiscount() != null ? med.getDiscount() : 0.0;

                    double itemTotal = price * qty;

                    double itemDiscount = itemTotal * discount / 100.0;

                    totalCartValue += itemTotal;
                    totalDiscount += itemDiscount;
                }
            }

            double amountToPay = totalCartValue - totalDiscount;
            cart.setTotalCartValue(totalCartValue);
            cart.setDiscount(totalDiscount);
            cart.setAmountToPay(amountToPay);

        }

        if (preOrderResponseDTO.getCarts() == null || preOrderResponseDTO.getCarts().isEmpty()) {
            System.out.println("No carts found in regular order, skipping order creation");
            return;
        }

        System.out.println("Number of carts in regular order: " + preOrderResponseDTO.getCarts().size());

        // Ensure preOrder.orders is initialized (avoid NPE)
        if (preOrder.getOrders() == null) {
            preOrder.setOrders(new ArrayList<>());
        }

        preOrderResponseDTO.getCarts().forEach(cart -> {
            System.out.println("Processing cart for vendor ID: " + cart.getVendorId());

            // Create order from preorder response but adjust totals per cart
            Order order = populateOrder(preOrderResponseDTO);
            // set order totalAmount to cart specific amount (important)
            order.setTotalAmount(cart.getAmountToPay());
            order.setDiscount(cart.getDiscount());

            Vendor vendor = new Vendor();
            vendor.setId(cart.getVendorId());
            order.setVendor(vendor);
            order.setPreOrder(preOrder);

            // Save order
            Order savedOrder = orderRepository.save(order);
            System.out.println("Saved order ID: " + savedOrder.getId() + " for vendor ID: " + cart.getVendorId());
            cart.setOrderId(savedOrder.getId());

            // Add savedOrder to preorder so relation is persisted/visible later
            preOrder.getOrders().add(savedOrder);

            List<OrderItem> orderItems = cart.getMedicine().stream()
                    .filter(medicine -> {
                        Medicine med = medicineRepository.findById(medicine.getId()).orElse(null);
                        if (med == null) {
                            System.err.println("SKIPPING order item: Medicine not found for vendorMedicineId=" + medicine.getId());
                            return false;
                        }
                        return true;
                    })
                    .map(medicine -> {
                        OrderItem item = new OrderItem();
                        Medicine med = medicineRepository.findById(medicine.getId()).orElse(null);
                        item.setMedicineId(med.getId());
                        item.setQty(medicine.getQty());
                        Double mrp = medicine.getMrp() != null ? medicine.getMrp() : 0.0;
                        Double discount = medicine.getDiscount() != null ? medicine.getDiscount() : 0.0;
                        item.setMrp(mrp);
                        item.setSellingPrice(Constants.calculateUnitPrice(mrp, discount));
                        item.setAmount(mrp * medicine.getQty());
                        item.setOrderStatus("pending");
                        item.setOrder(savedOrder);
                        return item;
                    })
                    .collect(Collectors.toList());

            // Save items
            orderItemRepository.saveAll(orderItems);
            System.out.println("Saved " + orderItems.size() + " order items for order ID: " + savedOrder.getId());
        });

        preOrderRepository.save(preOrder);
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
        
        return stockRepository.findStocksByMedicineIdAndBothVendorIds(medicineId, vendorUserId, externalVendorId);
    }

}