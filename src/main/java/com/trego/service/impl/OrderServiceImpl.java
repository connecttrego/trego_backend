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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

@Service
public class OrderServiceImpl implements IOrderService {

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
    private AddressRepository addressRepository;

    @Autowired
    private IPreOrderService preOrderService;

    @Autowired
    private IBucketService bucketService;

    @Override
    public OrderResponseDTO placeOrder(OrderRequestDTO orderRequest) throws Exception {
        OrderResponseDTO orderResponseDTO = new OrderResponseDTO();

        orderResponseDTO.setUserId(orderRequest.getUserId());
        PreOrder preOrder = preOrderRepository.findById(orderRequest.getPreOrderId()).get();

        Gson gson = new Gson();
        PreOrderResponseDTO preOrderResponseDTO = gson.fromJson(preOrder.getPayload(), PreOrderResponseDTO.class);
        preOrderResponseDTO.setOrderId(preOrder.getId());

        // If a specific vendor is selected, filter the carts to only include that vendor
        if (orderRequest.getSelectedVendorId() != null) {
            System.out.println("Filtering carts for selected vendor ID: " + orderRequest.getSelectedVendorId());
            List<CartResponseDTO> filteredCarts = preOrderResponseDTO.getCarts().stream()
                    .filter(cart -> cart.getVendorId().equals(orderRequest.getSelectedVendorId()))
                    .collect(Collectors.toList());
            preOrderResponseDTO.setCarts(filteredCarts);

            // Store the selected vendor ID in the preorder for later use during validation
            preOrder.setSelectedVendorId(orderRequest.getSelectedVendorId());
        }

        String razorpayOrderId = null;
        if (StringUtils.isEmpty(preOrder.getRazorpayOrderId()) || !(preOrderResponseDTO.getAmountToPay() > 0 && Double.compare(preOrderResponseDTO.getAmountToPay(), preOrder.getTotalPayAmount()) == 0)) {
            razorpayOrderId = createRazorPayOrder(orderRequest, preOrderResponseDTO);
            preOrder.setRazorpayOrderId(razorpayOrderId);
            preOrder.setTotalPayAmount(preOrderResponseDTO.getAmountToPay());
            preOrder.setPaymentStatus("unpaid");

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
            VandorCartResponseDTO vendorCartData = preOrderService.vendorSpecificPrice(bucketOrderRequest.getPreOrderId());

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
            double bucketAmount = selectedBucket.getAmountToPay(); // This is the final amount after discount
            double bucketDiscount = selectedBucket.getTotalDiscount(); // Total discount across all items
            double originalTotal = selectedBucket.getTotalPrice(); // Original price before discount

            // Create a PreOrder entity for the bucket-based orde
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
                    // Calculate original price before discount
                    double originalPrice = bucketItem.getDiscount() > 0 ?
                            bucketItem.getPrice() / (1 - bucketItem.getDiscount() / 100) :
                            bucketItem.getPrice();
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
            preOrder.setPayload(payload);

            // Save the preorder 
            //preOrderRepository.save(preOrder);
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
    public OrderValidateResponseDTO validateOrder(OrderValidateRequestDTO orderValidateRequestDTO) throws Exception {
        System.out.println("Validating order with Order ID: " + orderValidateRequestDTO.getOrderId());
        OrderValidateResponseDTO validateResponseDTO = new OrderValidateResponseDTO();
        boolean isValidate = verifyRazorPayOrder(orderValidateRequestDTO);

        if (isValidate) {
            PreOrder preOrder = preOrderRepository.findById(orderValidateRequestDTO.getOrderId()).get();
            System.out.println("Found PreOrder ID: " + preOrder.getId() + " with payment status: " + preOrder.getPaymentStatus());
            preOrder.setPaymentStatus("paid");

            Gson gson = new Gson();
            PreOrderResponseDTO preOrderResponseDTO = gson.fromJson(preOrder.getPayload(), PreOrderResponseDTO.class);
            preOrderResponseDTO.setOrderId(preOrder.getId());

            System.out.println("Preserving original cart data during validation");

            // Ensure address ID preserved
            if (preOrderResponseDTO.getAddressId() == 0) {
                preOrderResponseDTO.setAddressId(preOrder.getAddressId());
            }


            if (preOrder.getSelectedVendorId() != null) {
                System.out.println("Selected Vendor ID found: " + preOrder.getSelectedVendorId() + ". Processing as BUCKET ORDER.");
                processBucketOrder(preOrder, preOrderResponseDTO);
            } else if (preOrderResponseDTO.getCarts() != null && preOrderResponseDTO.getCarts().size() > 1) {
                System.out.println("Detected MULTI-VENDOR cart (" + preOrderResponseDTO.getCarts().size() + " carts). Processing as REGULAR ORDER.");
                processRegularOrder(preOrder, preOrderResponseDTO);
            } else {
                System.out.println("Single vendor but no selectedVendorId found. Processing as BUCKET ORDER.");
                processBucketOrder(preOrder, preOrderResponseDTO);
            }


            preOrderRepository.save(preOrder);
        }

        validateResponseDTO.setValidate(isValidate);
        validateResponseDTO.setRazorpayOrderId(orderValidateRequestDTO.getRazorpayOrderId());
        validateResponseDTO.setRazorpayPaymentId(orderValidateRequestDTO.getRazorpayPaymentId());
        return validateResponseDTO;
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
                        address.getLng(), address.getUser().getId(), address.getMobileNo(), address.getName(), address.getAddressTypeValue());
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
                PreOrder preOrder = preOrderRepository.findById(orderId).get();
                subOrderIds.addAll(preOrder.getOrders().stream()
                        .map(Order::getId) // Assuming Order has a getId() method
                        .collect(Collectors.toList()));
                preOrderRepository.updateOrderStatus(orderIds, "cancelled");

            });

        }
        if (!subOrderIds.isEmpty()) {
            orderRepository.updateOrderStatusAndReason(subOrderIds, "cancelled", request.getReason(), request.getReasonId());
        }
        return new CancelOrderResponseDTO("Orders and sub-orders cancelled successfully", orderIds, subOrderIds);
    }

    private List<OrderDTO> populateOrders(PreOrder preOrder) {
        System.out.println("Populating orders for PreOrder ID: " + preOrder.getId() + " with " + preOrder.getOrders().size() + " orders");
        List<OrderDTO> orderDTOList = new ArrayList<>();
// Iterate over orders in PreOrder
        preOrder.getOrders().forEach(order -> {
            System.out.println("Processing order ID: " + order.getId() + " for vendor ID: " + order.getVendor().getId());
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

            if (order.getVendor().getCategory().equalsIgnoreCase("retail")) {
                vendorDTO.setLogo(Constants.LOGO_BASE_URL + Constants.OFFLINE_BASE_URL + order.getVendor().getLogo());
            } else {
                vendorDTO.setLogo(Constants.LOGO_BASE_URL + Constants.ONLINE_BASE_URL + order.getVendor().getLogo());
            }

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
                medicineDetails.put("medicineId", orderItem.getMedicine().getId());
                medicineDetails.put("medicineName", orderItem.getMedicine().getName());
                medicineDetails.put("packing", orderItem.getMedicine().getPacking());
                medicineDetails.put("medicineLogo",
                        Constants.LOGO_BASE_URL + Constants.MEDICINES_BASE_URL + orderItem.getMedicine().getPhoto1());

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


    public String createRazorPayOrder(OrderRequestDTO orderRequest, PreOrderResponseDTO preOrderResponseDTO) throws Exception {
        String keyId = "rzp_test_oZBGm1luIG1Rpl"; // Replace with actual key
        String keySecret = "S0Pxnueo7AdCYS2HFIa7LXK6"; // Replace with actual key
        String credentials = keyId + ":" + keySecret;
        String encodedAuth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        // API URL
        String url = "https://api.razorpay.com/v1/orders";
        // Create a JSON object
        JsonObject jsonObject = new JsonObject();
        // Add fields to the JSON object
        BigDecimal amount = new BigDecimal(preOrderResponseDTO.getAmountToPay())
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        int convertedAmount = amount.multiply(BigDecimal.valueOf(100)).intValue();


        jsonObject.addProperty("amount", convertedAmount);
        jsonObject.addProperty("currency", "INR");
        jsonObject.addProperty("receipt", "receipt#123");

        // Create a nested JSON object
        JsonObject notes = new JsonObject();
        notes.addProperty("userId", preOrderResponseDTO.getUserId());

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
        System.out.println("Response Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        String orderId = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.body());

            // Extract individual fields
            orderId = rootNode.get("id").asText();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return orderId;
    }

    private void populateCartResponse(PreOrderResponseDTO preOrderResponseDTO) {
        List<CartResponseDTO> cartDTOs = preOrderResponseDTO.getCarts().stream().map(cart -> {
            List<MedicineDTO> medicines = cart.getMedicine().stream()
                    .map(medicine -> {
                        List<Stock> stockList = stockRepository.findByMedicineIdAndVendorId(medicine.getId(), cart.getVendorId());

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
        medicineDTO.setMrp(stock.getMrp());
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

                            List<Stock> stocks = stockRepository.findByMedicineIdAndVendorId(medicineId, vendorId);

                            if (!stocks.isEmpty()) {
                                // You can decide how to handle multiple results
                                // Option 1: Take the first one
                                Stock stock = stocks.get(0);

                                double price = stock.getMrp();
                                double discountPercentage = stock.getDiscount();
                                double totalCartValue = price * qty;
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

                            List<Stock> stocks = stockRepository.findByMedicineIdAndVendorId(medicineId, vendorId);

                            if (!stocks.isEmpty()) {
                                Stock stock = stocks.get(0);

                                double price = stock.getMrp();
                                medicine.setMrp(price);
                                double discountPercentage = stock.getDiscount();
                                double tempTotalCartValue = price * qty;
                                return tempTotalCartValue;
                            } else {
                                return 0.0;
                            }

                        }))
                .mapToDouble(Double::doubleValue) // Map to double for summing
                .sum(); // Calculate total value
        preOrderResponseDTO.setTotalCartValue(totalCartValue);

    }

    private Order populateOrder(PreOrderResponseDTO orderRequest) {


        Address address = addressRepository.findById(orderRequest.getAddressId()).get();
        Order order = new Order();
        User user = userRepository.findById(orderRequest.getUserId()).get();
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

    public String createRazorPayOrderForBucket(BucketOrderRequestDTO bucketOrderRequest, PreOrderResponseDTO preOrderResponseDTO) throws Exception {
        String keyId = "rzp_test_oZBGm1luIG1Rpl"; // Replace with actual key
        String keySecret = "S0Pxnueo7AdCYS2HFIa7LXK6"; // Replace with actual key
        String credentials = keyId + ":" + keySecret;
        String encodedAuth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        // API URL
        String url = "https://api.razorpay.com/v1/orders";
        // Create a JSON object
        JsonObject jsonObject = new JsonObject();
        // Add fields to the JSON object
        BigDecimal amount = new BigDecimal(preOrderResponseDTO.getAmountToPay())
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        int convertedAmount = amount.multiply(BigDecimal.valueOf(100)).intValue();


        jsonObject.addProperty("amount", convertedAmount);
        jsonObject.addProperty("currency", "INR");
        jsonObject.addProperty("receipt", "receipt#123");

        // Create a nested JSON object
        JsonObject notes = new JsonObject();
        notes.addProperty("userId", preOrderResponseDTO.getUserId());

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
        System.out.println("Response Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        String orderId = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.body());

            // Extract individual fields
            orderId = rootNode.get("id").asText();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return orderId;
    }

    // Helper method to determine if an order is a bucket order
    public boolean isBucketOrder(PreOrder preOrder) {
        // Bucket orders are created by "SYSTEM" and have a single cart
        boolean isBucket = "SYSTEM".equals(preOrder.getCreatedBy());
        System.out.println("Checking if PreOrder ID: " + preOrder.getId() + " is bucket order. CreatedBy: " + preOrder.getCreatedBy() + ", IsBucket: " + isBucket);
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
            System.out.println("Saved order ID: " + savedOrder.getId() + " for vendor ID: " + selectedCart.getVendorId());
            selectedCart.setOrderId(savedOrder.getId());

            preOrder.getOrders().add(savedOrder);

            List<OrderItem> orderItems = new ArrayList<>();
            if (selectedCart.getMedicine() != null) {
                System.out.println("Number of medicines in cart: " + selectedCart.getMedicine().size());
                for (MedicineDTO medicine : selectedCart.getMedicine()) {
                    OrderItem item = new OrderItem();
                    Medicine med = new Medicine();
                    med.setId(medicine.getId());
                    item.setMedicine(med);
                    item.setQty(medicine.getQty());
                    item.setMrp(medicine.getMrp());
                    item.setSellingPrice(Constants.calculateUnitPrice(medicine.getMrp(), medicine.getDiscount()));
                    item.setAmount(medicine.getMrp() * medicine.getQty());
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
        if (preOrderResponseDTO.getCarts() != null) {
            System.out.println("Number of carts in regular order: " + preOrderResponseDTO.getCarts().size());
        }

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
                    .map(medicine -> {
                        OrderItem item = new OrderItem();
                        Medicine med = new Medicine();
                        med.setId(medicine.getId());
                        item.setMedicine(med);
                        item.setQty(medicine.getQty());
                        item.setMrp(medicine.getMrp());
                        item.setSellingPrice(Constants.calculateUnitPrice(medicine.getMrp(), medicine.getDiscount()));
                        item.setAmount(medicine.getMrp() * medicine.getQty());
                        item.setOrderStatus("pending");
                        item.setOrder(savedOrder);
                        return item;
                    })
                    .collect(Collectors.toList());

            // Save items
            orderItemRepository.saveAll(orderItems);
            System.out.println("Saved " + orderItems.size() + " order items for order ID: " + savedOrder.getId());
        });

        // Persist the updated PreOrder (with orders added)
        preOrderRepository.save(preOrder);
    }

}