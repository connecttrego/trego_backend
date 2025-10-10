# Trego Backend API

Trego is a comprehensive medicine delivery platform backend built with Spring Boot. This system provides RESTful APIs for managing medicines, users, orders, vendors, and inventory for an online pharmacy service.

## Table of Contents
- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Core Features](#core-features)
- [API Endpoints](#api-endpoints)
  - [Authentication](#authentication)
  - [User Management](#user-management)
  - [Medicine Management](#medicine-management)
  - [Order Management](#order-management)
  - [Vendor Management](#vendor-management)
  - [Inventory Management](#inventory-management)
  - [Address Management](#address-management)
  - [Category Management](#category-management)
  - [Subcategory Management](#subcategory-management)
  - [Product Management](#product-management)
  - [OTC Product Management](#otc-product-management)
  - [Substitute Product Management](#substitute-product-management)
  - [Attachment and Prescription Management](#attachment-and-prescription-management)
- [Data Models](#data-models)
  - [Entity Relationship Diagram](#entity-relationship-diagram)
  - [Core Entities](#core-entities)
    - [User Entity](#user-entity)
    - [Medicine Entity](#medicine-entity)
    - [Vendor Entity](#vendor-entity)
    - [Stock Entity](#stock-entity)
    - [Order Entity](#order-entity)
    - [OrderItem Entity](#orderitem-entity)
    - [PreOrder Entity](#preorder-entity)
    - [Category Entity](#category-entity)
    - [Subcategory Entity](#subcategory-entity)
    - [Product Entity](#product-entity)
    - [OTC Product Entity](#otc-product-entity)
    - [Substitute Entity](#substitute-entity)
    - [Banner Entity](#banner-entity)
- [Business Logic](#business-logic)
  - [Order Processing Flow](#order-processing-flow)
  - [Payment Integration](#payment-integration)
  - [Inventory Management](#inventory-management-1)
  - [User Management](#user-management-1)
- [Service Layer Implementation](#service-layer-implementation)
  - [Order Service](#order-service)
  - [Medicine Service](#medicine-service)
  - [User Service](#user-service)
  - [Vendor Service](#vendor-service)
  - [Main Service](#main-service)
  - [OTC Product Service](#otc-product-service)
  - [Substitute Service](#substitute-service)
- [Repository Layer](#repository-layer)
  - [Custom Queries](#custom-queries)
  - [Relationship Management](#relationship-management)
- [DTO Layer](#dto-layer)
  - [Request DTOs](#request-dtos)
  - [Response DTOs](#response-dtos)
- [API Controllers](#api-controllers)
  - [RESTful Design](#restful-design)
  - [Error Handling](#error-handling)
- [Security Considerations](#security-considerations)
- [Setup and Installation](#setup-and-installuration)
  - [Prerequisites](#prerequisites)
  - [Database Setup](#database-setup)
  - [Installation Steps](#installation-steps)
- [Configuration](#configuration)
  - [Database Configuration](#database-configuration)
  - [Connection Pool Settings](#connection-pool-settings)
  - [Logging Configuration](#logging-configuration)
  - [API Documentation](#api-documentation-1)
- [Database Schema](#database-schema)
  - [Table Relationships](#table-relationships)
  - [Indexing Strategy](#indexing-strategy)
- [Testing](#testing)
  - [Unit Testing](#unit-testing)
  - [Integration Testing](#integration-testing)
- [API Documentation](#api-documentation-2)
- [Deployment](#deployment)
  - [Standalone JAR](#standalone-jar)
  - [Docker Deployment](#docker-deployment)
  - [Google Cloud Deployment](#google-cloud-deployment)
- [Performance Considerations](#performance-considerations)
- [Scalability](#scalability)
- [New Features](#new-features)
  - [Shop by Category](#shop-by-category)
  - [Vendor Delivery Information](#vendor-delivery-information)
  - [Banner Management](#banner-management)
  - [OTC Product Management](#otc-product-management-1)
  - [Substitute Product Management](#substitute-product-management-1)

## Overview

Trego is a medicine delivery platform that enables users to:
- Browse and search for medicines with detailed information
- View medicine availability across different vendors
- Place orders for medicines with multiple vendors in a single transaction
- Manage user profiles and delivery addresses
- Track order status and history
- Handle pre-orders for out-of-stock items
- Process payments through Razorpay integration
- Shop by category (new feature)

The system follows a microservices-inspired architecture with clearly separated layers:
- **Controller Layer**: REST API endpoints that handle HTTP requests and responses
- **Service Layer**: Business logic implementation with transaction management
- **Repository Layer**: Data access objects using Spring Data JPA
- **Entity Layer**: Database entities mapped with JPA annotations
- **DTO Layer**: Data transfer objects for API communication and data transformation

## Technology Stack

- **Java 17**: Primary programming language with modern features like lambda expressions, streams API, and improved type inference
- **Spring Boot 3.1.2**: Framework for building the REST API with auto-configuration and embedded server support
- **Spring Data JPA**: For database operations with Hibernate as the ORM implementation
- **Spring Web MVC**: For RESTful web services with annotation-based controller mapping
- **Spring Transaction Management**: For declarative transaction management using `@Transactional` annotations
- **Hibernate 6.x**: ORM framework for object-relational mapping with lazy loading and caching
- **MySQL 8.0+**: Primary database with support for JSON data types and advanced indexing
- **Google Cloud SQL MySQL Socket Factory**: For secure connection to Google Cloud SQL instances
- **Maven 3.8+**: Build automation tool with dependency management
- **Swagger/OpenAPI (SpringDoc 2.2.0)**: API documentation with interactive testing interface
- **Lombok 1.18.30**: Boilerplate code reduction with annotations like `@Data`, `@Getter`, `@Setter`
- **Gson 2.8.8 & Jackson 2.13.3**: JSON processing libraries for serialization/deserialization
- **Apache Commons Lang 3**: Utility library for string manipulation and other common operations
- **JUnit 5**: Testing framework with parameterized tests and assertions
- **Razorpay API**: Payment processing integration with order creation and verification
- **AWS SDK for Java v2**: For interacting with Amazon S3 cloud storage service

## Project Structure

```
src/main/java/com/trego/
├── TregoApiApplication.java      # Main application entry point with @SpringBootApplication
├── api/                          # REST controllers with @RestController annotations
│   ├── AddressController.java    # Address management endpoints
│   ├── MainController.java       # Main application endpoints
│   ├── MasterController.java     # Master data endpoints (banners, categories)
│   ├── MedicineController.java   # Medicine search and retrieval endpoints
│   ├── OrderController.java      # Order placement and management endpoints
│   ├── PreOrderController.java   # Pre-order management endpoints
│   ├── StockController.java      # Inventory management endpoints
│   ├── UserController.java       # User management endpoints
│   ├── SubcategoryController.java # Subcategory management endpoints
│   ├── ProductController.java    # Product management endpoints
│   ├── OtcProductController.java # OTC Product management endpoints
│   └── AttachmentController.java # Attachment management endpoints
├── dao/                          # Data access layer
│   ├── entity/                   # JPA entities with relationships
│   │   ├── Address.java          # User address entity with geolocation
│   │   ├── Banner.java           # Marketing banner entity
│   │   ├── Category.java         # Medicine category entity
│   │   ├── Medicine.java         # Medicine entity with comprehensive details
│   │   ├── Order.java            # Order entity with status tracking
│   │   ├── OrderItem.java        # Order item entity for individual medicines
│   │   ├── PreOrder.java         # Pre-order entity for complex transactions
│   │   ├── Stock.java            # Inventory entity with pricing
│   │   ├── User.java             # User entity with profile information
│   │   ├── Vendor.java           # Vendor entity with business details
│   │   ├── Subcategory.java      # Subcategory entity
│   │   ├── Product.java          # Product entity
│   │   ├── OtcProduct.java       # OTC Product entity
│   │   └── Attachment.java       # Attachment entity for file uploads
│   └── impl/                     # Repository implementations extending JpaRepository
│       ├── AddressRepository.java
│       ├── BannerRepository.java
│       ├── CategoryRepository.java
│       ├── MedicineRepository.java
│       ├── OrderItemRepository.java
│       ├── OrderRepository.java
│       ├── PreOrderRepository.java
│       ├── StockRepository.java
│       ├── UserRepository.java
│       ├── VendorRepository.java
│       ├── SubcategoryRepository.java
│       ├── ProductRepository.java
│       ├── OtcProductRepository.java
│       └── AttachmentRepository.java
├── dto/                          # Data transfer objects for API communication
│   ├── response/                 # Response DTOs with @JsonInclude annotations
│   │   ├── CancelOrderResponseDTO.java
│   │   ├── CartResponseDTO.java
│   │   ├── OrderDTO.java
│   │   ├── OrderItemDTO.java
│   │   ├── OrderResponseDTO.java
│   │   ├── OrderValidateResponseDTO.java
│   │   ├── PreOrderResponseDTO.java
│   │   └── VandorCartResponseDTO.java
│   ├── AddressDTO.java
│   ├── CancelOrderRequestDTO.java
│   ├── CartDTO.java
│   ├── CategoryDTO.java
│   ├── MainDTO.java
│   ├── MedicineDTO.java
│   ├── MedicinePreOrderDTO.java
│   ├── MedicineWithStockAndVendorDTO.java
│   ├── OrderRequestDTO.java
│   ├── OrderValidateRequestDTO.java
│   ├── PreOrderDTO.java
│   ├── StockDTO.java
│   ├── SubstituteDTO.java
│   ├── UserDTO.java
│   ├── VendorDTO.java
│   ├── SubcategoryDTO.java
│   ├── ProductDTO.java
│   ├── OtcProductDTO.java
│   └── AttachmentDTO.java
├── enums/                        # Enumerations for type safety
│   └── AddressType.java          # Address type enumeration
├── exception/                    # Custom exceptions
│   └── InvalidAmountException.java
├── service/                      # Business logic layer with interfaces and implementations
│   ├── impl/                     # Service implementations with @Service annotation
│   │   ├── AddressServiceImpl.java
│   │   ├── MainServiceImpl.java
│   │   ├── MasterServiceImpl.java
│   │   ├── MedicineServiceImpl.java
│   │   ├── OrderServiceImpl.java
│   │   ├── PreOrderServiceImpl.java
│   │   ├── StockServiceImpl.java
│   │   ├── UserServiceImpl.java
│   │   ├── VendorServiceImpl.java
│   │   ├── SubcategoryServiceImpl.java
│   │   ├── ProductServiceImpl.java
│   │   ├── OtcProductServiceImpl.java
│   │   ├── AttachmentServiceImpl.java
│   │   └── S3ServiceImpl.java      # S3 service implementation for file storage
│   ├── IAddressService.java      # Service interfaces with method declarations
│   ├── IMainService.java
│   ├── IMasterService.java
│   ├── IMedicineService.java
│   ├── IOrderService.java
│   ├── IPreOrderService.java
│   ├── IStockService.java
│   ├── IUserService.java
│   ├── IVendorService.java
│   ├── ISubcategoryService.java
│   ├── IProductService.java
│   ├── IOtcProductService.java
│   ├── IAttachmentService.java
│   └── IS3Service.java           # S3 service interface
├── config/                       # Configuration classes
│   └── S3Config.java             # AWS S3 client configuration
└── utils/                        # Utility classes and constants
    ├── Constants.java            # Application-wide constants
    └── DataSeeder.java           # Data seeder for initial categories

src/test/java/com/trego/          # Test directory
├── TregoApiApplicationTests.java # Main application tests
├── api/                          # REST controller tests
│   ├── BookControllerTest.java   # Book controller tests
│   ├── OtcProductControllerTest.java # OTC Product controller tests
│   └── AttachmentControllerTest.java # Attachment controller tests
└── service/impl/                 # Service implementation tests
    ├── AttachmentServiceImplTest.java # Attachment service implementation tests
    └── S3ServiceImplTest.java         # S3 service implementation tests

## Core Features

1. **Medicine Catalog Management**
   - Detailed medicine information including composition, usage, side effects, and interactions
   - Multiple images per medicine for better visualization
   - Advanced search and filtering capabilities with pagination
   - Medicine substitution suggestions

2. **Multi-Vendor Support**
   - Different vendors can stock the same medicine with different pricing
   - Price comparison across vendors with discount information
   - Vendor categorization (retail/online) with different business models
   - Geolocation-based vendor discovery

3. **Advanced Order Processing**
   - Multi-vendor orders in a single transaction through pre-order mechanism
   - Pre-order functionality for out-of-stock items with future availability
   - Comprehensive order status tracking from placement to delivery
   - Flexible order cancellation with reason tracking

4. **Inventory Management**
   - Real-time stock updates with quantity tracking
   - Expiry date management for medicines
   - Discount management per vendor with percentage-based calculations
   - Vendor-specific inventory with independent pricing

5. **User Management**
   - User profiles with authentication and role-based access
   - Multiple address management per user with geolocation
   - Order history tracking with detailed information
   - Contact information management with mobile number verification

6. **Payment Integration**
   - Razorpay payment gateway integration for secure transactions
   - Two-step payment verification process
   - Payment status tracking with order association
   - Test environment configuration for development

7. **Marketing Features**
   - Banner management for promotional campaigns with proper URL processing
   - Category-based medicine organization
   - Substitute medicine suggestions for better customer experience
   - Vendor delivery time and review information

8. **Shop by Category** (New Feature)
   - Filter medicines by predefined categories
   - Category-based search functionality
   - Automatic category seeding for initial setup

9. **OTC Product Management** (New Feature)
   - Comprehensive database structure for over-the-counter products with detailed medical and e-commerce fields
   - Detailed medical information including ingredients, benefits, side effects, usage instructions, and safety advice
   - E-commerce fields for pricing, discounts, packaging, stock levels, and product images
   - Regulatory information for prescription requirements, storage instructions, and country of origin
   - Manufacturer details including address and alternate brands
   - Navigation breadcrumbs for improved user experience
   - Computed total price (price + tax) for frontend convenience

## API Endpoints

### Authentication
The API does not implement JWT or OAuth authentication. Authentication is handled through user ID references in requests. User creation is idempotent - if a user with the same email exists, the existing user details are returned.

### User Management
- `POST /users` - Create a new user or return existing user if email already exists
- `GET /users/{id}` - Get user by ID with associated addresses
- `PUT /users/{id}` - Update user information
- `DELETE /users/{id}` - Delete a user

### Address Management
- `POST /addresses` - Create a new address for a user
- `GET /addresses/user/{userId}` - Get all addresses for a user
- `PUT /addresses/{id}` - Update an address
- `DELETE /addresses/{id}` - Delete an address

### Medicine Management
- `GET /medicines` - Retrieve all medicines with stock and vendor information
- `GET /medicines/{id}` - Get a specific medicine by ID with detailed information
- `GET /medicines/search` - Search medicines by text with pagination and vendor filtering
- `GET /medicines/category/{category}` - Get medicines by category
- `GET /medicines/category/{category}/page` - Get medicines by category with pagination
- `GET /medicines/search/category/{category}` - Search medicines by name within a category

### Order Management
- `POST /orders` - Place a new order (creates Razorpay order and pre-order)
- `POST /orders/fromBucket` - Place a new order from a selected bucket (creates Razorpay order and pre-order based on bucket selection)
- `POST /orders/validateOrder` - Validate and confirm order payment with Razorpay verification
- `GET /orders/user/{userId}` - Fetch all orders for a user with pagination
- `POST /orders/cancel` - Cancel orders with reason tracking

### Vendor Management
- `GET /vendors` - Retrieve all vendors by type (retail/online)
- `GET /vendors/{id}` - Get vendor details with medicine inventory and search capability

### Inventory Management
- `GET /stocks` - Retrieve stock information (limited implementation)

### Master Data
- `GET /loadAll` - Retrieve all master data including banners, vendors, and categories
- `GET /categories` - Retrieve medicine categories for navigation

### Category Management
- `GET /categories` - Retrieve all categories by type

### Subcategory Management
- `GET /api/subcategories` - Retrieve all subcategories
- `GET /api/subcategories/category/{categoryId}` - Retrieve subcategories by category ID

### Product Management
- `GET /api/subcategories/{id}/products` - Retrieve products by subcategory ID (with optional pagination)
  - Query Parameters:
    - `page` (optional, default: 0) - Page number (0-based)
    - `size` (optional, default: 10) - Number of items per page
  - Response: Returns a Page object with pagination metadata and content array
  - Example: `GET /api/subcategories/2/products?page=0&size=5`

### OTC Product Management
- `GET /api/otc-subcategories/{id}/products` - Retrieve OTC products by subcategory ID

### Substitute Product Management
- `GET /api/medicines/{medicineId}/substitutes` - Retrieve up to 2 substitute products for a medicine, sorted by best price (low to high)
  - Path Parameters:
    - `medicineId` (Long) - The ID of the medicine to find substitutes for
  - Response: Returns an array of SubstituteDetailDTO objects with comprehensive product information
  - Example: `GET /api/medicines/1/substitutes`
  
  Response Fields:
  - `id` (Long) - The substitute product ID
  - `name` (String) - The name of the substitute product
  - `manufacturers` (String) - The manufacturer of the substitute product
  - `saltComposition` (String) - The salt composition
  - `medicineType` (String) - The type of medicine (Tablet, Capsule, etc.)
  - `stock` (Integer) - Available stock quantity
  - `introduction` (String) - Product introduction
  - `benefits` (String) - Product benefits
  - `description` (String) - Detailed product description
  - `howToUse` (String) - Instructions for use
  - `safetyAdvise` (String) - Safety information
  - `ifMiss` (String) - What to do if a dose is missed
  - `packaging` (String) - Packaging information
  - `packagingType` (String) - Type of packaging
  - `mrp` (BigDecimal) - Maximum retail price
  - `bestPrice` (BigDecimal) - Best available price
  - `discountPercent` (BigDecimal) - Discount percentage
  - `views` (Integer) - Number of views
  - `bought` (Integer) - Number of purchases
  - `prescriptionRequired` (String) - Prescription requirement information
  - `label` (String) - Product label
  - `factBox` (String) - Key facts about the product
  - `primaryUse` (String) - Primary use of the medicine
  - `storage` (String) - Storage instructions
  - `useOf` (String) - Usage information
  - `commonSideEffect` (String) - Common side effects
  - `alcoholInteraction` (String) - Alcohol interaction information
  - `pregnancyInteraction` (String) - Pregnancy interaction information
  - `lactationInteraction` (String) - Lactation interaction information
  - `drivingInteraction` (String) - Driving interaction information
  - `kidneyInteraction` (String) - Kidney interaction information
  - `liverInteraction` (String) - Liver interaction information
  - `manufacturerAddress` (String) - Manufacturer address
  - `countryOfOrigin` (String) - Country of origin
  - `forSale` (String) - Availability for sale
  - `qa` (String) - Questions and answers

- `GET /api/medicines/{medicineId}/substitutes/sorted-by-discount` - Retrieve up to 2 substitute products for a medicine, sorted by discount percentage in descending order (highest discount first)
  - Path Parameters:
    - `medicineId` (Long) - The ID of the medicine to find substitutes for
  - Response: Returns an array of SubstituteDetailDTO objects sorted by discount percentage (highest first)
  - Example: `GET /api/medicines/1/substitutes/sorted-by-discount`

- `GET /api/medicines/{medicineId}/substitutes/max-discount` - Retrieve the maximum discount percentage among all substitute products for a medicine
  - Path Parameters:
    - `medicineId` (Long) - The ID of the medicine to find the maximum discount for
  - Response: Returns a BigDecimal value representing the maximum discount percentage
  - Example: `GET /api/medicines/1/substitutes/max-discount`

### Medicine Bucket Optimization
- `POST /api/buckets/optimize` - Create optimized medicine buckets based on requested medicines and quantities
  - Request Body: BucketRequestDTO containing a map of medicine IDs and their required quantities
  - Response: Returns a list of BucketDTO objects sorted by total price (lowest first)
  - Example: `POST /api/buckets/optimize`
  
  Request Body:
  ```json
  {
    "medicineQuantities": {
      "1": 2,
      "2": 1,
      "3": 3
    }
  }
  ```

- `GET /api/buckets/optimize/preorder/{orderId}` - Create optimized medicine buckets based on an existing preorder
  - Path Parameters:
    - `orderId` (Long) - The ID of the preorder to optimize
  - Response: Returns a list of BucketDTO objects sorted by total price (lowest first)
  - Example: `GET /api/buckets/optimize/preorder/11`

  Response Fields:
  - `id` (Long) - The bucket ID (vendor ID for single-vendor buckets, timestamp for mixed buckets)
  - `name` (String) - The bucket name
  - `items` (Array) - List of BucketItemDTO objects
  - `totalPrice` (Double) - Total price of all items in the bucket
  - `vendorId` (Long) - Vendor ID (if all items are from the same vendor)
  - `vendorName` (String) - Vendor name (if all items are from the same vendor)
  
  BucketItemDTO Fields:
  - `medicineId` (Long) - Medicine ID
  - `medicineName` (String) - Medicine name
  - `vendorId` (Long) - Vendor ID
  - `vendorName` (String) - Vendor name
  - `price` (Double) - Price per unit after discount
  - `discount` (Double) - Discount percentage
  - `availableQuantity` (Integer) - Available quantity from vendor
  - `requestedQuantity` (Integer) - Quantity requested by user
  - `totalPrice` (Double) - Total price (price per unit * requested quantity)

### Attachment and Prescription Management
The Attachment and Prescription Management feature allows users to upload prescriptions and other attachments related to their orders. Files are now stored in AWS S3 cloud storage for better scalability and reliability.

**API Endpoints:**

- `POST /api/attachments/upload` - Upload a prescription or attachment
  - **Description**: Uploads a file to AWS S3 and creates an attachment record in the database
  - **Request Parameters**:
    - `file` (MultipartFile) - The file to upload (required)
    - `orderId` (Long) - Order ID to associate with the attachment (optional, but at least one of orderId, orderItemId, or userId must be provided)
    - `orderItemId` (Long) - Order item ID to associate with the attachment (optional)
    - `userId` (Long) - User ID to associate with the attachment (optional)
    - `description` (String) - Description of the attachment (optional)
  - **Response**: Returns an AttachmentDTO with the uploaded file details
  - **Example**: `POST /api/attachments/upload` with form data including file and orderId=1
  - **Success Response**: HTTP 200 OK with AttachmentDTO
  - **Error Responses**: 
    - HTTP 400 Bad Request if validation fails (missing file or association parameters)
    - HTTP 500 Internal Server Error if upload fails

- `GET /api/attachments/order/{orderId}` - Retrieve all attachments for an order
  - **Description**: Gets all attachments associated with a specific order
  - **Path Parameters**:
    - `orderId` (Long) - The ID of the order
  - **Response**: Returns an array of AttachmentDTO objects
  - **Example**: `GET /api/attachments/order/1`
  - **Success Response**: HTTP 200 OK with array of AttachmentDTO objects
  - **Error Responses**: HTTP 500 Internal Server Error if retrieval fails

- `GET /api/attachments/order-item/{orderItemId}` - Retrieve all attachments for an order item
  - **Description**: Gets all attachments associated with a specific order item
  - **Path Parameters**:
    - `orderItemId` (Long) - The ID of the order item
  - **Response**: Returns an array of AttachmentDTO objects
  - **Example**: `GET /api/attachments/order-item/1`
  - **Success Response**: HTTP 200 OK with array of AttachmentDTO objects
  - **Error Responses**: HTTP 500 Internal Server Error if retrieval fails

- `GET /api/attachments/user/{userId}` - Retrieve all attachments for a user
  - **Description**: Gets all attachments associated with a specific user
  - **Path Parameters**:
    - `userId` (Long) - The ID of the user
  - **Response**: Returns an array of AttachmentDTO objects
  - **Example**: `GET /api/attachments/user/1`
  - **Success Response**: HTTP 200 OK with array of AttachmentDTO objects
  - **Error Responses**: HTTP 500 Internal Server Error if retrieval fails

- `GET /api/attachments/{id}` - Retrieve a specific attachment by ID
  - **Description**: Gets a specific attachment by its ID
  - **Path Parameters**:
    - `id` (Long) - The ID of the attachment
  - **Response**: Returns an AttachmentDTO object
  - **Example**: `GET /api/attachments/1`
  - **Success Response**: HTTP 200 OK with AttachmentDTO
  - **Error Responses**: 
    - HTTP 404 Not Found if attachment doesn't exist
    - HTTP 500 Internal Server Error if retrieval fails

- `DELETE /api/attachments/{id}` - Delete an attachment
  - **Description**: Deletes an attachment from both the database and AWS S3 storage
  - **Path Parameters**:
    - `id` (Long) - The ID of the attachment to delete
  - **Response**: HTTP 200 OK on success
  - **Example**: `DELETE /api/attachments/1`
  - **Success Response**: HTTP 200 OK
  - **Error Responses**: HTTP 500 Internal Server Error if deletion fails

**AttachmentDTO Fields:**
- `id` (Long) - The attachment ID
- `fileName` (String) - Original name of the uploaded file
- `fileType` (String) - MIME type of the uploaded file
- `fileUrl` (String) - Public URL to access the file in S3 storage
- `orderId` (Long) - Order ID associated with the attachment (if any)
- `orderItemId` (Long) - Order item ID associated with the attachment (if any)
- `userId` (Long) - User ID associated with the attachment (if any)
- `description` (String) - Description of the attachment
- `createdAt` (LocalDateTime) - Timestamp when the attachment was created
- `updatedAt` (LocalDateTime) - Timestamp when the attachment was last updated

    USER ||--o{ ADDRESS : has
    USER ||--o{ ORDER : places
    USER ||--o{ PREORDER : creates
    VENDOR ||--o{ STOCK : maintains
    VENDOR ||--o{ ORDER : fulfills
    MEDICINE ||--o{ STOCK : available
    MEDICINE ||--o{ ORDERITEM : ordered
    ORDER ||--o{ ORDERITEM : contains
    PREORDER ||--o{ ORDER : generates
    USER ||--o{ ADDRESS : "owns"
    CATEGORY ||--o{ MEDICINE : classifies
    CATEGORY ||--o{ SUBCATEGORY : contains
    SUBCATEGORY ||--o{ PRODUCT : contains
    SUBCATEGORY ||--o{ OTC_PRODUCT : contains
```

### Core Entities

#### User Entity
The User entity represents a customer of the platform with authentication and profile information.

**Fields:**
- `id`: Unique identifier (Long, auto-generated)
- `name`: User's full name (String)
- `email`: Email address (String, unique identifier in business logic)
- `emailVerifiedAt`: Email verification timestamp (LocalDateTime)
- `password`: Encrypted password (String)
- `passwordHint`: Password hint for recovery (String)
- `rememberToken`: Token for "remember me" functionality (String)
- `mobile`: Mobile number (long)
- `role`: User role (String)
- `createdAt`: Account creation timestamp (LocalDateTime)
- `updatedAt`: Last update timestamp (LocalDateTime)

**Relationships:**
- One-to-many relationship with [Address](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/Address.java#L17-L49) entities (cascade all operations, lazy fetch)
- Mapped by the `user` field in the Address entity

**Annotations:**
- `@Data`: Lombok annotation for boilerplate code generation
- `@Entity(name = "users")`: JPA entity mapped to "users" table

#### Medicine Entity
The Medicine entity contains comprehensive information about medicines including medical details, usage instructions, and safety information.

**Fields:**
- `id`: Unique identifier (long, auto-generated)
- `name`: Medicine name (String)
- `manufacturer`: Manufacturing company (String)
- `saltComposition`: Active ingredients (String)
- `medicineType`: Type of medicine (tablet, syrup, etc.) (String)
- `introduction`: Brief description (String)
- `description`: Detailed description (String)
- `howItWorks`: Mechanism of action (String)
- `safetyAdvise`: Safety information (String)
- `ifMiss`: Instructions if dose is missed (String)
- `packing`: Packaging information (String)
- `packagingType`: Type of packaging (String)
- `prescriptionRequired`: Whether prescription is required (String)
- `storage`: Storage instructions (String)
- `useOf`: Usage instructions (String)
- `commonSideEffect`: Common side effects (String)
- `alcoholInteraction`: Alcohol interaction warnings (String)
- `pregnancyInteraction`: Pregnancy interaction warnings (String)
- `lactationInteraction`: Lactation interaction warnings (String)
- `drivingInteraction`: Driving interaction warnings (String)
- `kidneyInteraction`: Kidney interaction warnings (String)
- `liverInteraction`: Liver interaction warnings (String)
- `manufacturerAddress`: Manufacturer address (String)
- `countryOfOrigin`: Country of origin (String)
- `questionAnswers`: FAQ section (String)
- `photo1` to `photo4`: Medicine image URLs (String)
- `category`: Category for "Shop by Category" feature (String)

**Relationships:**
- One-to-many relationship with [Stock](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/Stock.java#L13-L32) entities (mapped by the `medicine` field)

**Annotations:**
- `@Data`: Lombok annotation for boilerplate code generation
- `@Entity(name = "medicines")`: JPA entity mapped to "medicines" table

#### Vendor Entity
The Vendor entity represents a pharmacy or seller with business information.

**Fields:**
- `id`: Unique identifier (Long, auto-generated)
- `name`: Vendor name (String)
- `druglicense`: Drug license number (String)
- `gistin`: GSTIN number (String)
- `category`: Vendor category (retail/online) (String)
- `logo`: Logo URL (String)
- `lat`: Latitude for geolocation (String)
- `lng`: Longitude for geolocation (String)
- `address`: Physical address (String)
- `deliveryTime`: Estimated delivery time (String)
- `reviews`: Vendor reviews and ratings (String)

**Annotations:**
- `@Data`: Lombok annotation for boilerplate code generation
- `@Entity(name = "vendors")`: JPA entity mapped to "vendors" table

#### Stock Entity
The Stock entity represents inventory information for medicines at vendors.

**Fields:**
- `id`: Unique identifier (long, auto-generated)
- `mrp`: Maximum retail price (double)
- `discount`: Discount percentage (double)
- `qty`: Available quantity (int)
- `expiryDate`: Expiry date (String)

**Relationships:**
- Many-to-one relationship with [Medicine](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/Medicine.java#L14-L53) entity (with `@JoinColumn`)
- Many-to-one relationship with [Vendor](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/Vendor.java#L12-L26) entity (with `@JoinColumn`)
- `@JsonIgnore` annotation on medicine relationship to prevent circular references

**Annotations:**
- `@Data`: Lombok annotation for boilerplate code generation
- `@Entity(name = "stocks")`: JPA entity mapped to "stocks" table

#### Order Entity
The Order entity represents a customer order with comprehensive tracking information.

**Fields:**
- `id`: Unique identifier (Long, auto-generated)
- `totalAmount`: Total order amount (double)

#### Substitute Entity
The Substitute entity represents substitute products for medicines with comprehensive medical and e-commerce information.

**Fields:**
- `id`: Unique identifier (Long, auto-generated)
- `medicineId`: Reference to the original medicine (Long)
- `substituteMedicineId`: Reference to the substitute medicine (Long)
- `name`: Product name (String)
- `manufacturers`: Product manufacturers (String)
- `saltComposition`: Active ingredients (String)
- `medicineType`: Type of medicine (tablet, syrup, etc.) (String)
- `stock`: Available stock (Integer)
- `introduction`: Brief description (String)
- `benefits`: Product benefits (String)
- `description`: Detailed description (String)
- `howToUse`: Directions for use (String)
- `safetyAdvise`: Safety information (String)
- `ifMiss`: Instructions if dose is missed (String)
- `packaging`: Packaging information (String)
- `packagingType`: Type of packaging (String)
- `mrp`: Maximum retail price (BigDecimal)
- `bestPrice`: Best available price (BigDecimal)
- `discountPercent`: Discount percentage (BigDecimal)
- `views`: Number of views (Integer)
- `bought`: Number of purchases (Integer)
- `prescriptionRequired`: Whether prescription is required (String)
- `label`: Product label (String)
- `factBox`: Key facts about the product (String)
- `primaryUse`: Primary use of the product (String)
- `storage`: Storage instructions (String)
- `useOf`: How to use the product (String)
- `commonSideEffect`: Common side effects (String)
- `alcoholInteraction`: Alcohol interaction warnings (String)
- `pregnancyInteraction`: Pregnancy interaction warnings (String)
- `lactationInteraction`: Lactation interaction warnings (String)
- `drivingInteraction`: Driving interaction warnings (String)
- `kidneyInteraction`: Kidney interaction warnings (String)
- `liverInteraction`: Liver interaction warnings (String)
- `manufacturerAddress`: Manufacturer address (String)
- `countryOfOrigin`: Country of origin (String)
- `forSale`: Regions where product is for sale (String)
- `qa`: Questions and answers (String)

**Annotations:**
- `@Data`: Lombok annotation for boilerplate code generation
- `@Entity(name = "substitutes")`: JPA entity mapped to "substitutes" table
- `@Column(name = "medicine_id")`: Explicit column mapping for medicineId
- `@Column(name = "substitute_medicine_id")`: Explicit column mapping for substituteMedicineId
- `@Column(name = "best_price")`: Explicit column mapping for bestPrice
- `@Column(name = "discount_percent")`: Explicit column mapping for discountPercent

- `discount`: Discount applied (double)
- `address`: Delivery address details (String)
- `pincode`: Postal code (String)
- `lanmark`: Landmark for delivery (String)
- `city`: City name (String)
- `name`: Recipient name (String)
- `mobile`: Recipient mobile number (long)
- `email`: Recipient email (String)
- `paymentStatus`: Payment status (String)
- `paymentMethod`: Payment method used (String)
- `orderStatus`: Order fulfillment status (String)
- `cancelReason`: Cancellation reason (String)
- `cancelReasonId`: Cancellation reason identifier (String)
- `createdAt`: Order creation timestamp (LocalDateTime)
- `updatedAt`: Last update timestamp (LocalDateTime)

**Relationships:**
- Many-to-one relationship with [User](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/User.java#L17-L49) entity
- Many-to-one relationship with [Vendor](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/Vendor.java#L12-L26) entity
- Many-to-one relationship with [PreOrder](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/PreOrder.java#L16-L63) entity
- One-to-many relationship with [OrderItem](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/OrderItem.java#L18-L52) entities (cascade all operations)

**Lifecycle Methods:**
- `@PrePersist`: Sets `createdAt` timestamp before persisting

**Annotations:**
- `@Data`: Lombok annotation for boilerplate code generation
- `@Entity(name = "orders")`: JPA entity mapped to "orders" table
- `@Table(name = "orders")`: Explicit table name mapping

#### OrderItem Entity
The OrderItem entity represents individual items within an order.

**Fields:**
- `id`: Unique identifier (Long, auto-generated)
- `qty`: Quantity ordered (int)
- `mrp`: Price per unit (double)
- `orderStatus`: Item-specific status (String)

**Relationships:**
- Many-to-one relationship with [Order](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/Order.java#L21-L92) entity (with cascade operations)
- Many-to-one relationship with [Medicine](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/Medicine.java#L14-L53) entity

**Annotations:**
- `@Data`: Lombok annotation for boilerplate code generation
- `@Entity(name = "order_items")`: JPA entity mapped to "order_items" table

#### Attachment Entity
The Attachment entity represents uploaded files such as prescriptions and other documents.

**Fields:**
- `id`: Unique identifier (Long, auto-generated)
- `fileName`: Original name of the uploaded file (String)
- `fileType`: MIME type of the uploaded file (String)
- `fileUrl`: Path to the stored file (String)
- `orderId`: Reference to an order (Long, optional)
- `orderItemId`: Reference to an order item (Long, optional)
- `userId`: Reference to a user (Long, optional)
- `description`: Description of the attachment (String, optional)
- `createdAt`: Timestamp when the attachment was created (LocalDateTime)
- `updatedAt`: Timestamp when the attachment was last updated (LocalDateTime)

**Lifecycle Methods:**
- `@PrePersist`: Sets `createdAt` timestamp before persisting
- `@PreUpdate`: Sets `updatedAt` timestamp before updating

**Annotations:**
- `@Data`: Lombok annotation for boilerplate code generation
- `@Entity(name = "attachments")`: JPA entity mapped to "attachments" table

#### PreOrder Entity
The PreOrder entity represents pre-order information for complex multi-vendor orders.

**Fields:**
- `id`: Unique identifier (Long, auto-generated)
- `userId`: Reference to User (Long)
- `payload`: JSON payload with order details (String)
- `totalPayAmount`: Total amount to pay (double)
- `razorpayOrderId`: Razorpay order ID (String)
- `paymentStatus`: Payment status (String)
- `addressId`: Delivery address reference (Long)
- `mobileNo`: Contact number (Long)
- `orderStatus`: Order status (String)
- `createdAt`: Creation timestamp (LocalDateTime)

**Relationships:**
- One-to-many relationship with [Order](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/Order.java#L21-L92) entities (mapped by `preOrder` field)

**Lifecycle Methods:**
- `@PrePersist`: Sets `createdAt` timestamp before persisting

**Annotations:**
- `@Data`: Lombok annotation for boilerplate code generation
- `@Entity(name = "preorders")`: JPA entity mapped to "preorders" table

#### Category Entity
The Category entity represents medicine categories for the "Shop by Category" feature.

**Fields:**
- `id`: Unique identifier (Long, auto-generated)
- `name`: Category name (String)
- `logo`: Logo URL (String)
- `type`: Category type (String)
- `prescriptionRequired`: Whether prescription is required (String)
- `createdBy`: Creator information (String)

**Annotations:**
- `@Data`: Lombok annotation for boilerplate code generation
- `@Entity(name = "categories")`: JPA entity mapped to "categories" table

#### Subcategory Entity
The Subcategory entity represents subcategories in the product catalog system.

**Fields:**
- `id`: Unique identifier (Long, auto-generated)
- `name`: Subcategory name (String)
- `categoryId`: Reference to Category (Long)
- `description`: Subcategory description (String)
- `image`: Image URL (String)

**Annotations:**
- `@Data`: Lombok annotation for boilerplate code generation
- `@Entity(name = "subcategories")`: JPA entity mapped to "subcategories" table

#### Product Entity
The Product entity represents products in the product catalog system.

**Fields:**
- `id`: Unique identifier (Long, auto-generated)
- `name`: Product name (String)
- `description`: Product description (String)
- `price`: Product price (BigDecimal)
- `tax`: Product tax (BigDecimal)
- `subcategoryId`: Reference to Subcategory (Long)
- `image`: Image URL (String)
- `stock`: Available stock (Integer)

**Computed Fields:**
- `totalPrice`: Computed field (price + tax)

**Annotations:**
- `@Data`: Lombok annotation for boilerplate code generation
- `@Entity(name = "products")`: JPA entity mapped to "products" table
- `@Transient`: For computed totalPrice field

#### OTC Product Entity
The OTC Product entity represents over-the-counter products with comprehensive medical and e-commerce information.

**Fields:**
- `id`: Unique identifier (Long, auto-generated)
- `name`: Product name (String)
- `category`: Product category (String)
- `subCategory`: Product sub-category (String)
- `breadcrum`: Navigation breadcrumb (String)
- `description`: Product description (String)
- `manufacturers`: Product manufacturers (String)
- `packaging`: Packaging type (String)
- `packInfo`: Package information (String)
- `price`: Product price (BigDecimal)
- `bestPrice`: Best available price (BigDecimal)
- `discountPercent`: Discount percentage (BigDecimal)
- `prescriptionRequired`: Whether prescription is required (String)
- `primaryUse`: Primary use of the product (String)
- `saltSynonmys`: Salt synonyms (String)
- `storage`: Storage instructions (String)
- `introduction`: Product introduction (String)
- `useOf`: How to use the product (String)
- `benefits`: Product benefits (String)
- `sideEffect`: Side effects (String)
- `howToUse`: Directions for use (String)
- `howWorks`: How the product works (String)
- `safetyAdvise`: Safety advice (String)
- `ifMiss`: What to do if a dose is missed (String)
- `ingredients`: Product ingredients (String)
- `alternateBrand`: Alternate brands (String)
- `manufacturerAddress`: Manufacturer address (String)
- `forSale`: Regions where product is for sale (String)
- `countryOfOrigin`: Country of origin (String)
- `tax`: Product tax (BigDecimal)
- `subcategoryId`: Reference to Subcategory (Long)
- `image`: Image URL (String)
- `stock`: Available stock (Integer)

**Computed Fields:**
- `totalPrice`: Computed field (price + tax)
- `image`: Image URL (String)
- `stock`: Available stock (Integer)

**Computed Fields:**
- `totalPrice`: Computed field (price + tax)

**Annotations:**
- `@Data`: Lombok annotation for boilerplate code generation
- `@Entity(name = "otc_products")`: JPA entity mapped to "otc_products" table
- `@Column(name = "subcategory_id")`: Explicit column mapping for subcategoryId
- `@Transient`: For computed totalPrice field

#### Substitute Entity
The Substitute entity represents substitute products for medicines.

**Fields:**
- `id`: Unique identifier (Long, auto-generated)
- `medicineId`: Reference to Medicine (Long)
- `substituteId`: Reference to Substitute Medicine (Long)

**Annotations:**
- `@Data`: Lombok annotation for boilerplate code generation
- `@Entity(name = "substitutes")`: JPA entity mapped to "substitutes" table

#### Banner Entity
The Banner entity represents marketing banners for the homepage.

**Fields:**
- `id`: Unique identifier (Long, auto-generated)
- `logo`: Logo URL (String)
- `bannerUrl`: URL to which the banner links (String)
- `position`: Position of the banner (top/middle) (String)
- `createdBy`: Creator information (String)

**Annotations:**
- `@Data`: Lombok annotation for boilerplate code generation
- `@Entity(name = "banner")`: JPA entity mapped to "banner" table

## Business Logic

### Order Processing Flow

The order processing in Trego follows a complex multi-step flow to handle multi-vendor orders:

1. **Cart Creation**: User adds medicines from different vendors to their cart
2. **Pre-order Creation**: 
   - System creates a [PreOrder](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/PreOrder.java#L16-L63) entity with cart details serialized as JSON in the `payload` field
   - Associates the pre-order with the user and delivery address
3. **Razorpay Order Creation**: 
   - System calculates the total amount to pay
   - Creates a Razorpay order using the Razorpay API
   - Stores the Razorpay order ID in the pre-order entity
4. **Payment Processing**: 
   - User completes payment on the frontend using Razorpay checkout
   - Receives payment ID and signature from Razorpay
5. **Payment Verification**: 
   - System verifies payment with Razorpay API using payment ID
   - Updates pre-order payment status to "paid" if verification succeeds
6. **Order Creation**: 
   - For each vendor in the cart, system creates a separate [Order](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/Order.java#L21-L92) entity
   - Creates [OrderItem](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/OrderItem.java#L18-L52) entities for each medicine in the vendor's cart
   - Associates all orders with the pre-order entity
7. **Order Fulfillment**: 
   - Vendors fulfill orders and update status
   - Users can track order status through the pre-order

### Payment Integration

The system integrates with Razorpay for payment processing:

1. **Order Creation**:
   - Uses test API keys (`rzp_test_oZBGm1luIG1Rpl` and `S0Pxnueo7AdCYS2HFIa7LXK6`)
   - Creates orders with amount in paise (multiplied by 100)
   - Sets currency to INR
   - Includes user ID in notes for reference

2. **Payment Verification**:
   - Verifies payments using Razorpay's payment API
   - Checks if the payment ID matches the one received from frontend
   - Updates order status based on verification result

3. **Security Considerations**:
   - API keys are hardcoded (should be moved to environment variables)
   - Basic authentication with Base64 encoded credentials
   - HTTPS communication with Razorpay API

### Inventory Management

The inventory management system handles stock levels across multiple vendors:

1. **Stock Tracking**:
   - Each medicine can have different stock levels at different vendors
   - Real-time quantity updates when orders are placed
   - Expiry date tracking for medicines

2. **Pricing**:
   - Vendor-specific MRP for each medicine
   - Discount percentage that affects final price
   - Automatic calculation of discounted prices

3. **Availability**:
   - System checks stock availability before order placement
   - Handles out-of-stock scenarios through pre-order mechanism

### User Management

The user management system handles customer profiles and authentication:

1. **User Creation**:
   - Idempotent creation based on email address
   - Returns existing user if email already exists
   - Stores basic profile information

2. **Address Management**:
   - Multiple addresses per user
   - Geolocation coordinates for delivery optimization
   - Address type categorization

3. **Order History**:
   - Comprehensive order history tracking
   - Association with pre-orders for complete transaction view

### Attachment Management

The attachment management system handles file uploads and storage for prescriptions and other documents:

1. **File Upload Process**:
   - Users upload files through the `/api/attachments/upload` endpoint
   - Files are stored in AWS S3 cloud storage for scalability and reliability
   - Metadata is stored in the database with references to orders, order items, or users
   - System generates unique filenames to prevent conflicts

2. **File Storage**:
   - Files are stored in S3 with public read access for direct client access
   - S3 URLs are returned in API responses for immediate access
   - Content type is preserved for proper file handling
   - Original filenames are stored in metadata for user reference

3. **File Retrieval**:
   - Users can retrieve attachments by order, order item, user, or attachment ID
   - System returns S3 URLs for direct file access without server proxying
   - Metadata is provided alongside file URLs for context

4. **File Deletion**:
   - Users can delete attachments through the API
   - System removes both database metadata and S3 files
   - Deletion is atomic - if S3 deletion fails, database record is preserved

5. **Security Considerations**:
   - Files are associated with specific users, orders, or order items
   - Access control is managed through the application layer
   - AWS credentials are configured through environment variables
   - Public read access is limited to files with proper URLs

## Service Layer Implementation

### Order Service

The [OrderServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/impl/OrderServiceImpl.java#L35-L526) class handles complex order processing logic:

1. **Order Placement**:
   - Creates Razorpay orders
   - Manages pre-order entities
   - Handles payment integration

2. **Order Validation**:
   - Verifies Razorpay payments
   - Creates actual orders from pre-orders
   - Updates inventory levels

3. **Order Retrieval**:
   - Fetches orders with pagination
   - Maps entity data to DTOs
   - Handles complex joins for order details

4. **Order Cancellation**:
   - Cancels both pre-orders and individual orders
   - Updates status with reason tracking
   - Handles cascading cancellations

### Medicine Service

The [MedicineServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/impl/MedicineServiceImpl.java#L22-L150) class handles medicine catalog operations:

1. **Medicine Retrieval**:
   - Retrieves all medicines with stock information
   - Gets detailed medicine information by ID
   - Handles image URL construction with base URLs

2. **Search Functionality**:
   - Text-based search with pagination
   - Vendor-specific filtering
   - Case-insensitive matching

3. **Category-based Filtering** (New Feature):
   - Retrieves medicines by category
   - Supports pagination for category results
   - Allows searching within categories

### User Service

The [UserServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/impl/UserServiceImpl.java#L16-L92) class handles user management:

1. **User Creation**:
   - Idempotent creation based on email
   - Profile information management

2. **User Retrieval**:
   - Gets user details with associated addresses
   - Maps entity data to DTOs

### Vendor Service

The vendor service handles vendor-related operations including inventory management and medicine availability.

### Subcategory Service

The [SubcategoryServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/impl/SubcategoryServiceImpl.java#L13-L43) class handles subcategory operations:

1. **Subcategory Retrieval**:
   - Retrieves all subcategories
   - Retrieves subcategories by category ID

### Product Service

The [ProductServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/impl/ProductServiceImpl.java#L12-L37) class handles product operations:

1. **Product Retrieval**:
   - Retrieves products by subcategory ID
   - Calculates total price (price + tax) for each product

### Main Service

The [MainServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/impl/MainServiceImpl.java#L12-L24) class handles main application operations:

1. **Banner Retrieval**:
   - Retrieves all banners by position (top/middle)
   - Converts Banner entities to BannerDTOs with proper URL processing
   - Processes banner images with base URLs for proper display

2. **Vendor Information**:
   - Retrieves vendor information with delivery time and reviews
   - Processes vendor logos with base URLs for proper display
   - Populates vendor medicines with proper image URLs

3. **Category Retrieval**:
   - Retrieves medicine categories for navigation

### OTC Product Service

The [OtcProductServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/impl/OtcProductServiceImpl.java#L12-L69) class handles OTC product operations:

1. **OTC Product Retrieval**:
   - Retrieves OTC products by subcategory ID using the repository layer
   - Handles null subcategory ID validation with appropriate error handling
   - Maps entity data to DTOs with comprehensive medical and e-commerce information

2. **Data Transformation**:
   - Converts OtcProduct entities to OtcProductDTOs through the convertToDTO method
   - Handles all the detailed fields for medical products including name, category, subCategory, breadcrum, description, manufacturers, packaging, packInfo, price, bestPrice, discountPercent, prescriptionRequired, primaryUse, saltSynonmys, storage, introduction, useOf, benefits, sideEffect, howToUse, howWorks, safetyAdvise, ifMiss, ingredients, alternateBrand, manufacturerAddress, forSale, countryOfOrigin, tax, subcategoryId, image, and stock
   - Transfers the computed totalPrice field (price + tax) to the DTO

3. **Error Handling**:
   - Wraps all operations in try-catch blocks for proper exception handling
   - Throws RuntimeException with meaningful error messages for debugging
   - Logs exceptions to the console for troubleshooting purposes

4. **Performance Considerations**:
   - Uses Java 8 Streams for efficient data transformation
   - Implements proper validation before database queries
   - Follows Spring Boot best practices for service layer implementation

### Substitute Service

The [SubstituteServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/impl/SubstituteServiceImpl.java#L13-L85) class handles substitute product operations:

1. **Substitute Product Retrieval**:
   - Retrieves substitute products by medicine ID using the repository layer
   - Automatically sorts results by best price in ascending order (low to high)
   - Limits results to a maximum of 2 substitute products per medicine
   - Maps entity data to DTOs with comprehensive medical and e-commerce information

2. **Data Transformation**:
   - Converts Substitute entities to SubstituteDetailDTOs through the convertToDTO method
   - Handles all the detailed fields for substitute products including name, manufacturers, saltComposition, medicineType, stock, introduction, benefits, description, howToUse, safetyAdvise, ifMiss, packaging, packagingType, mrp, bestPrice, discountPercent, views, bought, prescriptionRequired, label, factBox, primaryUse, storage, useOf, commonSideEffect, alcoholInteraction, pregnancyInteraction, lactationInteraction, drivingInteraction, kidneyInteraction, liverInteraction, manufacturerAddress, countryOfOrigin, forSale, and qa

3. **Sorting and Limiting**:
   - Uses repository method with ORDER BY clause for efficient database-level sorting
   - Uses Java Streams for limiting results to maximum 2 items
   - Ensures consistent ordering with low to high price sorting

4. **Error Handling**:
   - Wraps all operations in try-catch blocks for proper exception handling
   - Throws RuntimeException with meaningful error messages for debugging
   - Logs exceptions to the console for troubleshooting purposes

5. **Performance Considerations**:
   - Uses database-level sorting for efficiency
   - Implements proper validation before database queries
   - Follows Spring Boot best practices for service layer implementation
   - Limits results at the service layer to reduce data transfer

### S3 Service

The [S3ServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/src/main/java/com/trego/service/impl/S3ServiceImpl.java) class handles file storage operations with AWS S3:

1. **File Upload**:
   - Uploads files to AWS S3 with unique filenames to prevent conflicts
   - Preserves original content type for proper file handling
   - Generates public URLs for direct file access
   - Handles IOException and S3Exception for robust error handling

2. **File Deletion**:
   - Deletes files from AWS S3 by filename
   - Gracefully handles S3Exception without interrupting the operation
   - Logs errors for monitoring and debugging purposes

3. **Configuration**:
   - Uses AWS SDK v2 for S3 operations
   - Configured through application.properties with bucket name and region
   - Uses static credentials provider with access key and secret key
   - Region configuration for proper S3 endpoint selection

4. **Security Considerations**:
   - AWS credentials are configured through environment variables
   - Files are stored with public read access for direct client access
   - Unique filenames prevent accidental overwrites
   - Proper exception handling prevents information leakage

## Repository Layer

### Custom Queries

The repository layer extends JpaRepository and includes custom queries:

1. **PreOrderRepository**:
   - Custom query to fetch orders by user ID with joins
   - Bulk update operations for order status

2. **OrderRepository**:
   - Bulk update operations for order status and cancellation reasons

3. **MedicineRepository** (Updated for Category Feature):
   - Added methods for category-based filtering
   - Added pagination support for category queries
   - Added search within category functionality

4. **SubcategoryRepository**:
   - Added method to find subcategories by category ID

5. **ProductRepository**:
   - Added method to find products by subcategory ID

6. **OtcProductRepository**:
   - Added [OtcProductRepository](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/impl/OtcProductRepository.java#L9-L11) with method to find OTC products by subcategory ID
   - Extends JpaRepository<OtcProduct, Long> for standard CRUD operations
   - Custom method: findBySubcategoryId(Long subcategoryId) for efficient querying
   - Uses Spring Data JPA naming conventions for automatic query generation
   - Returns List<OtcProduct> for easy integration with service layer

7. **SubstituteRepository**:
   - Added [SubstituteRepository](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/impl/SubstituteRepository.java#L9-L14) with method to find substitute products by medicine ID
   - Extends JpaRepository<Substitute, Long> for standard CRUD operations
   - Custom method: findByMedicineIdOrderByBestPriceAsc(Long medicineId) for efficient querying with sorting
   - Uses Spring Data JPA naming conventions for automatic query generation
   - Returns List<Substitute> for easy integration with service layer
   - Sorts results by best price in ascending order at the database level for efficiency

### Relationship Management

The repository layer handles complex entity relationships:
- Lazy loading for performance optimization
- Cascade operations for data consistency
- Fetch joins for reducing database queries

## DTO Layer

### Request DTOs

Request DTOs handle incoming data from API clients:
- [OrderRequestDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/OrderRequestDTO.java#L5-L9): Order placement requests
- [OrderValidateRequestDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/OrderValidateRequestDTO.java#L5-L10): Payment verification requests
- [CancelOrderRequestDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/CancelOrderRequestDTO.java#L3-L8): Order cancellation requests

### Response DTOs

Response DTOs handle outgoing data to API clients:
- [OrderResponseDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/response/OrderResponseDTO.java#L11-L24): Order details with nested objects
- [PreOrderResponseDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/response/PreOrderResponseDTO.java#L12-L20): Pre-order information
- [MedicineWithStockAndVendorDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/MedicineWithStockAndVendorDTO.java#L9-L24): Medicine information with inventory
- Updated DTOs to include category information
- [SubcategoryDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/SubcategoryDTO.java#L5-L9): Subcategory information
- [ProductDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/ProductDTO.java#L6-L12): Product information with computed total price
- [OtcProductDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/OtcProductDTO.java#L7-L38): OTC Product information with comprehensive medical and e-commerce fields including name, category, subCategory, breadcrum, description, manufacturers, packaging, packInfo, price, bestPrice, discountPercent, prescriptionRequired, primaryUse, saltSynonmys, storage, introduction, useOf, benefits, sideEffect, howToUse, howWorks, safetyAdvise, ifMiss, ingredients, alternateBrand, manufacturerAddress, forSale, countryOfOrigin, tax, and totalPrice
- [SubstituteDetailDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/SubstituteDetailDTO.java#L5-L47): Substitute Product information with comprehensive medical and e-commerce fields including name, manufacturers, saltComposition, medicineType, stock, introduction, benefits, description, howToUse, safetyAdvise, ifMiss, packaging, packagingType, mrp, bestPrice, discountPercent, views, bought, prescriptionRequired, label, factBox, primaryUse, storage, useOf, commonSideEffect, alcoholInteraction, pregnancyInteraction, lactationInteraction, drivingInteraction, kidneyInteraction, liverInteraction, manufacturerAddress, countryOfOrigin, forSale, and qa
- [BannerDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/BannerDTO.java#L3-L12): Banner information with proper URL processing
- [VendorDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/VendorDTO.java#L3-L22): Vendor information with delivery time and reviews

## API Controllers

### RESTful Design

Controllers follow RESTful design principles:
- Proper HTTP methods (GET, POST, PUT, DELETE)
- Resource-based URL structure
- Appropriate HTTP status codes
- JSON request/response format

The OTC Product Controller specifically:
- Uses @RestController annotation for automatic JSON serialization
- Maps to the base URL path: /api/otc-subcategories
- Implements the GET /{subcategoryId}/products endpoint for retrieving OTC products by subcategory ID
- Follows standard Spring Boot controller patterns

The Substitute Controller specifically:
- Uses @RestController annotation for automatic JSON serialization
- Maps to the base URL path: /api/medicines
- Implements the GET /{medicineId}/substitutes endpoint for retrieving substitute products by medicine ID
- Follows standard Spring Boot controller patterns
- Returns properly formatted JSON responses with appropriate HTTP status codes

### Error Handling

Controllers include basic error handling:
- Try-catch blocks for exception handling
- Appropriate HTTP status codes for different scenarios
- Error response DTOs for consistent error format

The OTC Product Controller specifically:
- Returns HttpStatus.BAD_REQUEST (400) for null subcategory IDs
- Returns HttpStatus.INTERNAL_SERVER_ERROR (500) for unexpected exceptions
- Returns HttpStatus.OK (200) with empty list for valid subcategory IDs with no products
- Logs exceptions to console for debugging purposes

The Substitute Controller specifically:
- Returns HttpStatus.INTERNAL_SERVER_ERROR (500) for unexpected exceptions
- Returns HttpStatus.OK (200) with empty list for valid medicine IDs with no substitutes
- Logs exceptions to console for debugging purposes

## Security Considerations

1. **API Security**:
   - Lack of authentication mechanism
   - No rate limiting or request throttling
   - No input validation or sanitization

2. **Data Security**:
   - Hardcoded API keys in source code
   - No encryption for sensitive data
   - No audit logging for critical operations

3. **Payment Security**:
   - Basic authentication for Razorpay API
   - No webhook validation for payment confirmations

## Setup and Installation

### Prerequisites
- Java 17 JDK
- Maven 3.8+
- MySQL 8.0+ (or Google Cloud SQL)
- AWS Account with S3 access (for file storage)
- IDE with Spring Boot support

### Database Setup
1. Install MySQL Server
2. Create the database:
   ```sql
   CREATE DATABASE trego_db;
   ```

### AWS S3 Setup
1. Create an S3 bucket in your AWS account
2. Create an IAM user with programmatic access
3. Attach a policy to the user with S3 permissions:
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Effect": "Allow",
         "Action": [
           "s3:PutObject",
           "s3:GetObject",
           "s3:DeleteObject"
         ],
         "Resource": [
           "arn:aws:s3:::your-bucket-name/*"
         ]
       }
     ]
   }
   ```
4. Note the Access Key ID and Secret Access Key for configuration

### Installation Steps

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd trego_backend
   ```

2. Configure database settings in `src/main/resources/application.properties`

3. Configure AWS S3 settings in `src/main/resources/application.properties`

4. Build the project:
   ```bash
   mvn clean install
   ```

5. Run the application:
   ```bash
   mvn spring-boot:run
   ```

6. Access the application at `http://localhost:8080`

## Configuration

### Database Configuration
The application is configured to connect to a local MySQL database:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/trego_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

For Google Cloud SQL deployment:
```properties
spring.datasource.url=jdbc:mysql://google/trego_db?cloudSqlInstance=trego-451604:us-central1:tregodbprod&socketFactory=com.google.cloud.sql.mysql.SocketFactory
```

### Connection Pool Settings
HikariCP connection pool configuration:
```properties
spring.datasource.hikari.connectionTimeout=20000
spring.datasource.hikari.maximumPoolSize=5
```

### Logging Configuration
Application logging levels:
```properties
logging.level.org.springframework=INFO
logging.level.com.trego=INFO
logging.level.com.zaxxer=ERROR
```

### API Documentation
Swagger UI configuration:
```properties
springdoc.swagger-ui.path=/swagger-ui.html
```

### AWS S3 Configuration
The application now stores file attachments in AWS S3 cloud storage. The following configuration properties must be set:

```properties
# AWS S3 Configuration
aws.s3.bucket-name=your-s3-bucket-name
aws.s3.region=us-east-1
aws.s3.access-key-id=your-access-key-id
aws.s3.secret-access-key=your-secret-access-key
```

**Configuration Details:**
- `aws.s3.bucket-name`: The name of your S3 bucket where files will be stored
- `aws.s3.region`: The AWS region where your S3 bucket is located
- `aws.s3.access-key-id`: Your AWS access key ID for authentication
- `aws.s3.secret-access-key`: Your AWS secret access key for authentication

**Security Note:** Never commit AWS credentials to version control. Use environment variables or secure configuration management systems in production environments.

**S3 Bucket Permissions:**
The AWS credentials must have the following permissions for the specified bucket:
- `s3:PutObject` - To upload files
- `s3:DeleteObject` - To delete files
- `s3:GetObject` - To retrieve files (public read access recommended for direct access)

**File Access:**
Files uploaded to S3 are accessible via public URLs in the format:
`https://{bucket-name}.s3.{region}.amazonaws.com/{filename}`

These URLs are returned in the AttachmentDTO when retrieving attachment information, allowing clients to directly access the files without proxying through the application server.

## Database Schema

### Table Relationships

1. **users** table:
   - Primary key: `id`
   - Foreign key references: None
   - Referenced by: `addresses.user_id`, `orders.user_id`, `preorders.user_id`

2. **addresses** table:
   - Primary key: `id`
   - Foreign key: `user_id` references `users.id`
   - Referenced by: None

3. **medicines** table:
   - Primary key: `id`
   - Foreign key references: None
   - Referenced by: `stocks.medicine_id`, `order_items.medicine_id`
   - New field: `category` for category classification

4. **vendors** table:
   - Primary key: `id`
   - Foreign key references: None
   - Referenced by: `stocks.vendor_id`, `orders.vendor_id`

5. **stocks** table:
   - Primary key: `id`
   - Foreign keys: `medicine_id` references `medicines.id`, `vendor_id` references `vendors.id`
   - Referenced by: None

6. **orders** table:
   - Primary key: `id`
   - Foreign keys: `user_id` references `users.id`, `vendor_id` references `vendors.id`, `pre_order_id` references `preorders.id`
   - Referenced by: `order_items.order_id`

7. **order_items** table:
   - Primary key: `id`
   - Foreign keys: `order_id` references `orders.id`, `medicine_id` references `medicines.id`
   - Referenced by: None

8. **preorders** table:
   - Primary key: `id`
   - Foreign key: `user_id` references `users.id`
   - Referenced by: `orders.pre_order_id`

9. **categories** table:
   - Primary key: `id`
   - Foreign key references: None
   - Referenced by: None (category is stored as a string in medicines table)

10. **subcategories** table:
    - Primary key: `id`
    - Foreign key: `category_id` references `categories.id`
    - Referenced by: `products.subcategory_id`, `otc_products.subcategory_id`

11. **products** table:
    - Primary key: `id`
    - Foreign key: `subcategory_id` references `subcategories.id`
    - Referenced by: None

12. **otc_products** table:
    - Primary key: `id`
    - Foreign key: `subcategory_id` references `subcategories.id`
    - Referenced by: None
    - Fields: `name`, `category`, `sub_category`, `breadcrum`, `description`, `manufacturers`, `packaging`, `pack_info`, `price`, `best_price`, `discount_percent`, `prescription_required`, `primary_use`, `salt_synonmys`, `storage`, `introduction`, `use_of`, `benefits`, `side_effect`, `how_to_use`, `how_works`, `safety_advise`, `if_miss`, `ingredients`, `alternate_brand`, `manufacturer_address`, `for_sale`, `country_of_origin`, `tax`, `image`, `stock`
    - Computed field: `total_price` (price + tax)
    - Appropriate data types for medical and e-commerce information

13. **substitutes** table:
    - Primary key: `id`
    - Foreign key: `medicine_id` references `medicines.id`
    - Referenced by: None
    - Fields: `medicine_id`, `substitute_medicine_id`, `name`, `manufacturers`, `salt_composition`, `medicine_type`, `stock`, `introduction`, `benefits`, `description`, `how_to_use`, `safety_advise`, `if_miss`, `packaging`, `packaging_type`, `mrp`, `best_price`, `discount_percent`, `views`, `bought`, `prescription_required`, `label`, `fact_box`, `primary_use`, `storage`, `use_of`, `common_side_effect`, `alcohol_interaction`, `pregnancy_interaction`, `lactation_interaction`, `driving_interaction`, `kidney_interaction`, `liver_interaction`, `manufacturer_address`, `country_of_origin`, `for_sale`, `qa`
    - Appropriate data types for medical and e-commerce information

14. **banners** table:
    - Primary key: `id`
    - Foreign key references: None
    - Referenced by: None

### Indexing Strategy

The database schema should include appropriate indexes for performance:
- Primary key indexes on all tables
- Foreign key indexes for join operations
- Composite indexes for frequently queried columns
- Full-text indexes for search functionality
- Index on the `category` field in the `medicines` table for efficient category filtering
- Index on `subcategory_id` in the `products` table for efficient product retrieval
- Index on `subcategory_id` in the `otc_products` table for efficient OTC product retrieval
- Index on `medicine_id` in the `substitutes` table for efficient substitute retrieval
- Index on `best_price` in the `substitutes` table for efficient sorting
- Additional indexes on frequently queried fields like `name`, `category`, and `prescription_required` in the `otc_products` table

## Testing

### Unit Testing

The project includes comprehensive unit test structure:
- JUnit 5 for test framework
- Spring Boot Test for integration testing
- TestRestTemplate for REST API testing
- Comprehensive test coverage for OTC Product functionality

### Integration Testing

Integration tests use:
- Random port web environment for testing
- In-memory database (H2) for isolation
- Test property sources for configuration

To run tests:
```bash
mvn test
```

### OTC Product Controller Testing

The OTC Product functionality includes dedicated integration tests:
- [OtcProductControllerTest](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/test/java/com/trego/api/OtcProductControllerTest.java#L35-L146) for testing OTC product endpoints
- Tests cover successful product retrieval by subcategory ID
- Tests handle invalid subcategory IDs gracefully
- Uses in-memory H2 database for test isolation
- Tests verify all OTC product fields are properly serialized

## API Documentation

API documentation is available through Swagger UI:
- Access at `/swagger-ui.html` when the application is running
- Provides interactive API testing interface
- Shows all endpoints with request/response schemas
- Includes example requests and responses

## API Testing with Postman

### Importing the API Collection

1. **Using Swagger UI Export**:
   - Start the application and navigate to `http://localhost:8080/swagger-ui.html`
   - Click on the "OpenAPI 3.0" link or find the export option
   - Download the OpenAPI/Swagger specification file (JSON or YAML format)
   - In Postman, click "Import" and select the downloaded file
   - Postman will automatically create a collection with all API endpoints

2. **Manual Setup**:
   - Open Postman
   - Click "New" > "Collection"
   - Name the collection "Trego API"
   - Click "Add a request" to create individual API requests

### Setting up Requests

#### General Request Structure:
- **Base URL**: `http://localhost:8080` (when running locally)

#### Example Requests:

- **Headers**: 
  - `Content-Type: application/json` (for POST/PUT requests with JSON body)
  - `Accept: application/json` (to specify JSON response format)

#### Example Requests:

1. **Get All Medicines**:
   - **Method**: GET
   - **URL**: `http://localhost:8080/medicines`
   - **Headers**: None required
   - **Body**: Not applicable (GET request)
   - **Expected Response**: 
     - Status Code: 200 OK
     - Response Body: Array of medicine objects with stock and vendor information

2. **Get Medicine by ID**:
   - **Method**: GET
   - **URL**: `http://localhost:8080/medicines/1`
   - **Headers**: None required
   - **Body**: Not applicable (GET request)
   - **Expected Response**: 
     - Status Code: 200 OK
     - Response Body: Detailed medicine object

3. **Get Substitutes for a Medicine**:
   - **Method**: GET
   - **URL**: `http://localhost:8080/api/medicines/1/substitutes`
   - **Headers**: None required
   - **Body**: Not applicable (GET request)
   - **Expected Response**: 
     - Status Code: 200 OK
     - Response Body: Array of up to 2 substitute objects sorted by price

4. **Get Maximum Discount Among Substitutes**:
   - **Method**: GET
   - **URL**: `http://localhost:8080/api/medicines/1/substitutes/max-discount`
   - **Headers**: None required
   - **Body**: Not applicable (GET request)
   - **Expected Response**: 
     - Status Code: 200 OK
     - Response Body: JSON number representing the maximum discount percentage

5. **Search Medicines**:
   - **Method**: GET
   - **URL**: `http://localhost:8080/medicines/search?searchText=paracetamol`
   - **Headers**: None required
   - **Body**: Not applicable (GET request)
   - **Expected Response**: 
     - Status Code: 200 OK
     - Response Body: Paginated results of medicines matching the search term

6. **Create Optimized Medicine Buckets**:
   - **Method**: POST
   - **URL**: `http://localhost:8080/api/buckets/optimize`
   - **Headers**: 
     - `Content-Type: application/json`
     - `Accept: application/json`
   - **Body**:
     ```json
     {
       "medicineQuantities": {
         "1": 2,
         "2": 1,
         "3": 3
       }
     }
     ```
   - **Expected Response**: 
     - Status Code: 200 OK
     - Response Body: Array of BucketDTO objects sorted by total price (lowest first)
     - Each bucket contains items from either a single vendor or a mix of vendors at the best prices
     - Each item includes both available quantity from the vendor and requested quantity from the user

7. **Create Optimized Medicine Buckets from Preorder**:
   - **Method**: GET
   - **URL**: `http://localhost:8080/api/buckets/optimize/preorder/11`
   - **Headers**: None required
   - **Body**: Not applicable (GET request)
   - **Expected Response**: 
     - Status Code: 200 OK
     - Response Body: Array of BucketDTO objects sorted by total price (lowest first)
     - Each bucket contains items from either a single vendor or a mix of vendors at the best prices
     - Each item includes both available quantity from the vendor and requested quantity from the preorder
   - **Solution**: Verify the resource ID exists in the database

8. **Place Order from Selected Bucket**:
   - **Method**: POST
   - **URL**: `http://localhost:8080/orders/fromBucket`
   - **Headers**: 
     - `Content-Type: application/json`
     - `Accept: application/json`
   - **Body**:
     ```json
     {
       "userId": 1,
       "addressId": 1,
       "preOrderId": 11,
       "bucketId": 1,
       "vendorId": 1
     }
     ```
   - **Expected Response**: 
     - Status Code: 200 OK
     - Response Body: OrderResponseDTO with Razorpay order ID and amount to pay
     - Creates a new pre-order and order based on the selected bucket instead of the original cart
   - **Use Case**: Allows users to "switch to cheaper" by selecting a bucket and proceeding with payment for the bucket amount instead of the original cart amount

3. **500 Internal Server Error**:
   - **Cause**: Server-side exception or database error
   - **Example**: Database connection failure or unhandled exception in code
   - **Solution**: Check server logs for detailed error information

4. **401 Unauthorized** (if authentication is implemented):
   - **Cause**: Missing or invalid authentication credentials
   - **Solution**: Provide valid authentication token or credentials

### Response Analysis

1. **Status Codes**:
   - 200: Success
   - 201: Created (for POST requests)
   - 400: Bad Request
   - 404: Not Found
   - 500: Internal Server Error

2. **Response Body**:
   - JSON format for all responses
   - Consistent field naming conventions
   - Proper data types (strings, numbers, booleans, arrays, objects)

3. **Error Responses**:
   - Standardized error format
   - Descriptive error messages
   - Error codes for programmatic handling

## Performance Considerations

1. **Database Optimization**:
   - Proper indexing strategy including index on category field
   - Query optimization with fetch joins
   - Connection pooling with HikariCP

2. **Caching**:
   - No explicit caching implemented
   - Potential for Redis or in-memory caching

3. **Pagination**:
   - Implemented for large result sets
   - Configurable page size

## Scalability

1. **Horizontal Scaling**:
   - Stateless application design
   - Database sharding potential
   - Load balancing support

2. **Vertical Scaling**:
   - JVM tuning options
   - Database optimization
   - Memory management

## New Features

### Shop by Category

The "Shop by Category" feature allows users to filter medicines by predefined categories. This feature includes:

1. **Category Field in Medicine Entity**:
   - Added a `category` field to the [Medicine](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/Medicine.java#L14-L53) entity to classify medicines
   - Updated [MedicineDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/MedicineDTO.java#L11-L66) and [MedicineWithStockAndVendorDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/MedicineWithStockAndVendorDTO.java#L9-L23) to include category information

2. **New API Endpoints**:
   - `GET /medicines/category/{category}` - Get all medicines in a specific category
   - `GET /medicines/category/{category}/page` - Get medicines in a category with pagination
   - `GET /medicines/search/category/{category}` - Search for medicines by name within a specific category

3. **Data Seeding**:
   - Automatic seeding of initial categories (Tablets, Syrups, Antibiotics) on application startup
   - Seeding only occurs if no categories exist in the database

4. **Repository Updates**:
   - Added methods to [MedicineRepository](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/impl/MedicineRepository.java#L12-L32) for category-based queries
   - Added pagination support for category queries

5. **Service Layer Implementation**:
   - Updated [IMedicineService](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/IMedicineService.java#L9-L22) interface with new category-based methods
   - Implemented category filtering in [MedicineServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/impl/MedicineServiceImpl.java#L22-L150)

6. **Required Categories**:
   - Tablets
   - Syrups
   - Antibiotics

This feature is designed to be clean, reusable, and scalable for future categories. New categories can be added through the existing category management endpoints or by updating the data seeder.

### Vendor Delivery Information

The "Vendor Delivery Information" feature provides detailed delivery information for vendors. This feature includes:

1. **Vendor Entity Updates**:
   - Added fields for `deliveryTime` and `reviews` to the [Vendor](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/Vendor.java#L12-L26) entity to store delivery information

2. **Vendor DTO Updates**:
   - Added fields for `deliveryTime` and `reviews` to the [VendorDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/VendorDTO.java#L3-L22) to transfer delivery information to the frontend

3. **Service Layer Implementation**:
   - Updated [MainServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/impl/MainServiceImpl.java#L12-L24) to populate vendor delivery time and reviews fields
   - Updated [VendorServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/impl/VendorServiceImpl.java#L12-L26) to populate vendor delivery time and reviews fields
   - Updated [PreOrderServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/impl/PreOrderServiceImpl.java#L15-L73) to populate vendor delivery time and reviews fields

This feature enhances the user experience by providing clear and detailed delivery information and reviews for each vendor.

### Banner Management

The "Banner Management" feature allows administrators to manage marketing banners for the homepage. This feature includes:

1. **Banner Entity**:
   - Added a [Banner](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/Banner.java#L12-L24) entity to represent banners with fields for logo, banner URL, position, and creator information

2. **Banner DTO**:
   - Added a [BannerDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/BannerDTO.java#L3-L12) to properly transfer banner data to the frontend with processed URLs

3. **Main DTO Updates**:
   - Updated [MainDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/MainDTO.java#L3-L15) to use BannerDTO instead of Banner entity for proper data transfer

4. **Repository Updates**:
   - Added [BannerRepository](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/impl/BannerRepository.java#L9-L13) with methods for banner queries

5. **Service Layer Implementation**:
   - Updated [MainServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/impl/MainServiceImpl.java#L12-L24) to properly convert Banner entities to BannerDTOs with correct URL processing

6. **API Endpoints**:
   - Banners are exposed through the main endpoint: `GET /loadAll` which returns a [MainDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/MainDTO.java#L3-L15) containing top and middle banners

7. **Error Handling**:
   - Proper error handling with appropriate HTTP status codes
   - Exception logging for debugging purposes

This feature provides a simple and effective way to manage marketing banners for the homepage with proper URL processing for display.

### OTC Product Management

The "OTC Product Management" feature allows for comprehensive management of over-the-counter products with detailed medical and e-commerce information. This feature includes:

1. **OTC Product Entity**:
   - Added an [OtcProduct](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/OtcProduct.java#L9-L86) entity to represent OTC products with comprehensive fields for medical information, e-commerce data, regulatory details, and manufacturer information
   - Fields include: name, category, subCategory, breadcrum, description, manufacturers, packaging, packInfo, price, bestPrice, discountPercent, prescriptionRequired, primaryUse, saltSynonmys, storage, introduction, useOf, benefits, sideEffect, howToUse, howWorks, safetyAdvise, ifMiss, ingredients, alternateBrand, manufacturerAddress, forSale, countryOfOrigin, tax, subcategoryId, image, and stock
   - Computed field: totalPrice (price + tax)

2. **OTC Product DTO**:
   - Added an [OtcProductDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/OtcProductDTO.java#L7-L38) to properly transfer OTC product data to the frontend
   - Includes all fields from the entity for complete data transfer
   - Transfers computed totalPrice field for frontend convenience

3. **Repository Updates**:
   - Added [OtcProductRepository](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/impl/OtcProductRepository.java#L9-L11) with methods for OTC product queries including finding products by subcategory ID
   - Extends JpaRepository for standard CRUD operations
   - Custom method: findBySubcategoryId(Long subcategoryId)

4. **Service Layer Implementation**:
   - Added [IOtcProductService](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/IOtcProductService.java#L7-L9) interface for OTC product operations
   - Implemented [OtcProductServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/impl/OtcProductServiceImpl.java#L12-L69) with business logic for retrieving OTC products and mapping entities to DTOs
   - Converts OtcProduct entities to OtcProductDTOs with all fields
   - Calculates and transfers the totalPrice computed field
   - Proper error handling with meaningful exception messages

5. **API Endpoints**:
   - Added [OtcProductController](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/api/OtcProductController.java#L12-L38) with endpoint: `GET /api/otc-subcategories/{id}/products` for retrieving OTC products by subcategory ID
   - Returns List<OtcProductDTO> with proper HTTP status codes
   - Handles null subcategory ID with BAD_REQUEST (400)
   - Handles exceptions with INTERNAL_SERVER_ERROR (500)
   - Returns empty list for valid subcategory IDs with no products

6. **Database Schema**:
   - Added `otc_products` table with all required fields
   - Foreign key relationship with `subcategories` table via `subcategory_id`
   - Index on `subcategory_id` for efficient queries
   - Appropriate data types for all fields (VARCHAR, DECIMAL, LONG, INTEGER)

7. **Testing**:
   - Added [OtcProductControllerTest](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/test/java/com/trego/api/OtcProductControllerTest.java#L35-L146) for integration testing
   - Tests cover successful retrieval and error cases
   - Uses in-memory H2 database for test isolation
   - Verifies all fields are properly serialized

8. **Error Handling**:
   - Proper error handling with appropriate HTTP status codes
   - Exception logging for debugging purposes
   - Graceful handling of invalid subcategory IDs
   - Meaningful error messages for troubleshooting

This feature provides a comprehensive system for managing OTC products with detailed information that is essential for pharmaceutical e-commerce platforms. The implementation follows best practices for REST API design, data transfer, and error handling.

### Substitute Product Management

The "Substitute Product Management" feature allows users to find substitute products for medicines, sorted by price from low to high, with a maximum of 2 substitutes per medicine. This feature includes:

1. **Substitute Entity**:
   - Added a [Substitute](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/entity/Substitute.java#L9-L65) entity to represent substitute products with comprehensive fields for medical information, e-commerce data, regulatory details, and manufacturer information
   - Fields include: name, manufacturers, saltComposition, medicineType, stock, introduction, benefits, description, howToUse, safetyAdvise, ifMiss, packaging, packagingType, mrp, bestPrice, discountPercent, views, bought, prescriptionRequired, label, factBox, primaryUse, storage, useOf, commonSideEffect, alcoholInteraction, pregnancyInteraction, lactationInteraction, drivingInteraction, kidneyInteraction, liverInteraction, manufacturerAddress, countryOfOrigin, forSale, and qa

2. **Substitute DTO**:
   - Added a [SubstituteDetailDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/SubstituteDetailDTO.java#L5-L47) to properly transfer substitute product data to the frontend
   - Includes all fields from the entity for complete data transfer

3. **Repository Updates**:
   - Added [SubstituteRepository](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dao/impl/SubstituteRepository.java#L9-L14) with methods for substitute product queries including finding products by medicine ID
   - Extends JpaRepository for standard CRUD operations
   - Custom method: findByMedicineIdOrderByBestPriceAsc(Long medicineId) for efficient queries with sorting

4. **Service Layer Implementation**:
   - Added [ISubstituteService](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/ISubstituteService.java#L7-L13) interface for substitute product operations
   - Implemented [SubstituteServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/impl/SubstituteServiceImpl.java#L13-L85) with business logic for retrieving substitute products and mapping entities to DTOs
   - Converts Substitute entities to SubstituteDetailDTOs with all fields
   - Automatically sorts substitutes by best price (low to high) or by discount percentage (high to low)
   - Limits results to maximum 2 substitutes per medicine
   - Proper error handling with meaningful exception messages

5. **API Endpoints**:
   - Added [SubstituteController](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/api/SubstituteController.java#L12-L30) with endpoints:
     - `GET /api/medicines/{medicineId}/substitutes` for retrieving substitute products by medicine ID
     - `GET /api/medicines/{medicineId}/substitutes/sorted-by-discount` for retrieving substitute products sorted by discount percentage (highest first)
     - `GET /api/medicines/{medicineId}/substitutes/max-discount` for retrieving substitute products sorted by discount percentage (highest first)
   - Returns List<SubstituteDetailDTO> with proper HTTP status codes
   - Handles exceptions with INTERNAL_SERVER_ERROR (500)
   - Returns empty list for valid medicine IDs with no substitutes

6. **Database Schema**:
   - Added `substitutes` table with all required fields:
     - `id` - Primary key
     - `medicine_id` - Foreign key referencing the medicine
     - `substitute_medicine_id` - Foreign key referencing the substitute medicine
     - All other fields as described in the Substitute entity
   - Foreign key relationship with `medicines` table via `medicine_id`
   - Index on `medicine_id` for efficient queries
   - Index on `best_price` for efficient sorting
   - Appropriate data types for all fields (VARCHAR, DECIMAL, LONG, INTEGER)

7. **Integration with Existing APIs**:
   - Updated [MedicineServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/impl/MedicineServiceImpl.java#L22-L150) to include substitute products in medicine responses
   - Updated [MedicineWithStockAndVendorDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/MedicineWithStockAndVendorDTO.java#L9-L24) to include a list of substitute products

8. **Testing**:
   - Added [SubstituteControllerTest](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/test/java/com/trego/api/SubstituteControllerTest.java#L15-L77) for integration testing
   - Added [SubstituteServiceImplTest](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/test/java/com/trego/service/impl/SubstituteServiceImplTest.java#L15-L105) for unit testing business logic
   - Tests cover successful retrieval, sorting, and limiting of results
   - Tests verify proper error handling
   - Tests cover discount-based sorting functionality
   - Uses mocks for isolation

9. **Error Handling**:
   - Proper error handling with appropriate HTTP status codes
   - Exception logging for debugging purposes
   - Graceful handling of invalid medicine IDs
   - Meaningful error messages for troubleshooting

This feature provides a comprehensive system for managing substitute products with detailed information that is essential for pharmaceutical e-commerce platforms. The implementation follows best practices for REST API design, data transfer, and error handling, with automatic sorting and limiting of results.

### Bucket-Based Order Management

The "Bucket-Based Order Management" feature allows users to "switch to cheaper" by selecting an optimized bucket and proceeding with payment for the bucket amount instead of the original cart amount. This feature includes:

1. **BucketOrderRequestDTO**:
   - Added a [BucketOrderRequestDTO](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/dto/BucketOrderRequestDTO.java#L5-L11) to handle requests for placing orders from selected buckets
   - Fields include: userId, addressId, preOrderId, bucketId, and vendorId

2. **New API Endpoint**:
   - Added `POST /orders/fromBucket` endpoint to place orders from selected buckets
   - Takes a BucketOrderRequestDTO as input
   - Returns an OrderResponseDTO with Razorpay order ID and amount to pay

3. **Service Layer Implementation**:
   - Updated [IOrderService](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/IOrderService.java#L12-L32) interface with new placeOrderFromBucket method
   - Implemented bucket-based order placement in [OrderServiceImpl](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/service/impl/OrderServiceImpl.java#L35-L526)
   - Creates a new pre-order and order based on the selected bucket instead of the original cart
   - Processes payments for the bucket amount instead of the cart amount

4. **Controller Layer**:
   - Added new endpoint in [OrderController](file:///c%3A/Users/ASUS/Downloads/trego_backend/trego_backend/src/main/java/com/trego/api/OrderController.java#L20-L62) to handle bucket-based order requests

5. **Error Handling**:
   - Proper error handling with meaningful exception messages
   - Handles cases where selected buckets are not found
   - Ensures data integrity when creating new pre-orders

This feature enhances the user experience by allowing customers to choose the most cost-effective option when placing orders, providing significant savings compared to the original cart total.

- Shows all endpoints with request/response schemas
- Includes example requests and responses

## Deployment

### Standalone JAR

Build and run as standalone JAR:
```bash
mvn clean package
java -jar target/trego-api-1.0.jar
```

### Docker Deployment

Create a Dockerfile:
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/trego-api-1.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Google Cloud Deployment

The application is configured for Google Cloud SQL deployment:
- Uses Google Cloud SQL MySQL Socket Factory
- Configured for MySQL 8.0+ compatibility
- Environment variables for configuration