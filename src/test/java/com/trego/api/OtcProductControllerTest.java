package com.trego.api;

import com.trego.TregoApiApplication;
import com.trego.dao.entity.OtcProduct;
import com.trego.dao.entity.Subcategory;
import com.trego.dao.impl.OtcProductRepository;
import com.trego.dao.impl.SubcategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testing OtcProductController with TestRestTemplate
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = TregoApiApplication.class
)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class OtcProductControllerTest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OtcProductRepository otcProductRepository;
    
    @Autowired
    private SubcategoryRepository subcategoryRepository;

    private String BASE_URL;
    
    private Subcategory testSubcategory;

    @BeforeEach
    void setUp() {
        BASE_URL = "http://localhost:" + port + "/api/otc-subcategories";
        
        // Clean up repositories
        otcProductRepository.deleteAll();
        subcategoryRepository.deleteAll();
        
        // Create a test subcategory
        testSubcategory = new Subcategory();
        testSubcategory.setName("Test Subcategory");
        testSubcategory.setDescription("Test Description");
        testSubcategory = subcategoryRepository.save(testSubcategory);
        
        // Create test OTC products
        OtcProduct product1 = new OtcProduct();
        product1.setName("Ayusya Nector Plus Capsule");
        product1.setCategory("Diabetes");
        product1.setSubCategory("Herbal Supplements");
        product1.setBreadcrum("Diabetes > Herbal Supplements");
        product1.setDescription("Ayusya nector plus capsule is herbal support for diabetes and accelerates the recovery of diabetic wounds caused due to infection.");
        product1.setManufacturers("Ayusya Pharma");
        product1.setPackaging("Bottle");
        product1.setPackInfo("10 capsules");
        product1.setPrice(BigDecimal.valueOf(199.00));
        product1.setBestPrice(BigDecimal.valueOf(179.00));
        product1.setDiscountPercent(BigDecimal.valueOf(10.0));
        product1.setPrescriptionRequired("No");
        product1.setPrimaryUse("Diabetes management");
        product1.setSaltSynonmys("Gudmar, Jambhul, Karla");
        product1.setStorage("Store in a cool and dry place away from direct sunlight");
        product1.setIntroduction("Herbal support for diabetes");
        product1.setUseOf("Supports diabetes management and wound healing");
        product1.setBenefits("Reduces diabetic neuropathy, improves kidney function, accelerates wound healing");
        product1.setSideEffect("No known side effects when used as directed");
        product1.setHowToUse("To be taken 1-2 capsule twice a day");
        product1.setHowWorks("Herbal ingredients support pancreatic function and glucose metabolism");
        product1.setSafetyAdvise("Read the label carefully before use. Keep it out of reach of the children. Use under medical supervision");
        product1.setIfMiss("If you miss a dose, take it as soon as you remember. If it's almost time for your next dose, skip the missed dose.");
        product1.setIngredients("Gudmar patra ghan, Jambhul beej ghan, Karla phal ghan, Kadujre ghan, Triphala ghan, Bhavana of neem patra, Bel patra, Methi beej");
        product1.setAlternateBrand("Diabecon, Glucofage");
        product1.setManufacturerAddress("Ayusya Pharma, Mumbai, India");
        product1.setForSale("India");
        product1.setCountryOfOrigin("India");
        product1.setTax(BigDecimal.valueOf(19.90));
        product1.setSubcategoryId(testSubcategory.getId());
        product1.setImage("ayusya_nector_plus.jpg");
        product1.setStock(50);
        
        otcProductRepository.save(product1);
    }

    @Test
    void testGetProductsBySubcategory() {
        // Test the endpoint
        String url = BASE_URL + "/" + testSubcategory.getId() + "/products";
        ResponseEntity<OtcProduct[]> response = restTemplate.getForEntity(url, OtcProduct[].class);
        
        // Check response status
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Check response body
        OtcProduct[] products = response.getBody();
        assertThat(products).isNotNull();
        assertEquals(1, products.length);
        
        // Check product details
        OtcProduct product = products[0];
        assertEquals("Ayusya Nector Plus Capsule", product.getName());
        assertEquals("Diabetes", product.getCategory());
        assertEquals("Herbal Supplements", product.getSubCategory());
        assertEquals("Diabetes > Herbal Supplements", product.getBreadcrum());
        assertEquals("Ayusya nector plus capsule is herbal support for diabetes and accelerates the recovery of diabetic wounds caused due to infection.", product.getDescription());
        assertEquals("Ayusya Pharma", product.getManufacturers());
        assertEquals(BigDecimal.valueOf(199.00), product.getPrice());
        assertEquals(BigDecimal.valueOf(179.00), product.getBestPrice());
        assertEquals("No", product.getPrescriptionRequired());
        assertEquals("Diabetes management", product.getPrimaryUse());
        assertEquals("10 capsules", product.getPackInfo());
        assertEquals("India", product.getForSale());
    }
    
    @Test
    void testGetProductsByInvalidSubcategory() {
        // Test with invalid subcategory ID
        String url = BASE_URL + "/99999/products";
        ResponseEntity<OtcProduct[]> response = restTemplate.getForEntity(url, OtcProduct[].class);
        
        // Should return OK with empty array
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OtcProduct[] products = response.getBody();
        assertThat(products).isNotNull();
        assertEquals(0, products.length);
    }
}