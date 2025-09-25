package com.trego.api;

import com.trego.dto.SubstituteDetailDTO;
import com.trego.service.ISubstituteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SubstituteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ISubstituteService substituteService;

    @InjectMocks
    private SubstituteController substituteController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(substituteController).build();
    }

    @Test
    public void testGetSubstitutesByMedicineId() throws Exception {
        // Create mock substitute DTOs
        SubstituteDetailDTO substitute1 = new SubstituteDetailDTO();
        substitute1.setId(1L);
        substitute1.setName("Substitute Medicine 1");
        substitute1.setManufacturers("Manufacturer 1");
        substitute1.setSaltComposition("Salt 1");
        substitute1.setMedicineType("Tablet");
        substitute1.setMrp(new BigDecimal("100.00"));
        substitute1.setBestPrice(new BigDecimal("80.00"));
        substitute1.setDiscountPercent(new BigDecimal("20.00"));

        SubstituteDetailDTO substitute2 = new SubstituteDetailDTO();
        substitute2.setId(2L);
        substitute2.setName("Substitute Medicine 2");
        substitute2.setManufacturers("Manufacturer 2");
        substitute2.setSaltComposition("Salt 2");
        substitute2.setMedicineType("Capsule");
        substitute2.setMrp(new BigDecimal("150.00"));
        substitute2.setBestPrice(new BigDecimal("120.00"));
        substitute2.setDiscountPercent(new BigDecimal("20.00"));

        List<SubstituteDetailDTO> substitutes = Arrays.asList(substitute1, substitute2);

        // Mock the service method
        when(substituteService.getSubstitutesByMedicineId(anyLong())).thenReturn(substitutes);

        // Perform the GET request and verify the response
        mockMvc.perform(get("/api/medicines/1/substitutes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Substitute Medicine 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Substitute Medicine 2"))
                .andExpect(jsonPath("$.length()").value(2));
    }
    
    @Test
    public void testGetMaxDiscountSubstitutesByMedicineId() throws Exception {
        // Create mock substitute DTOs sorted by discount (highest first)
        SubstituteDetailDTO substitute1 = new SubstituteDetailDTO();
        substitute1.setId(1L);
        substitute1.setName("High Discount Medicine");
        substitute1.setManufacturers("Manufacturer 1");
        substitute1.setSaltComposition("Salt 1");
        substitute1.setMedicineType("Tablet");
        substitute1.setMrp(new BigDecimal("100.00"));
        substitute1.setBestPrice(new BigDecimal("70.00"));
        substitute1.setDiscountPercent(new BigDecimal("30.00")); // Highest discount

        SubstituteDetailDTO substitute2 = new SubstituteDetailDTO();
        substitute2.setId(2L);
        substitute2.setName("Medium Discount Medicine");
        substitute2.setManufacturers("Manufacturer 2");
        substitute2.setSaltComposition("Salt 2");
        substitute2.setMedicineType("Capsule");
        substitute2.setMrp(new BigDecimal("150.00"));
        substitute2.setBestPrice(new BigDecimal("120.00"));
        substitute2.setDiscountPercent(new BigDecimal("20.00")); // Medium discount

        List<SubstituteDetailDTO> substitutes = Arrays.asList(substitute1, substitute2);

        // Mock the service method
        when(substituteService.getSubstitutesByMedicineIdSortedByDiscountDesc(anyLong())).thenReturn(substitutes);

        // Perform the GET request and verify the response
        mockMvc.perform(get("/api/medicines/1/substitutes/max-discount"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("High Discount Medicine"))
                .andExpect(jsonPath("$[0].discountPercent").value(30.00))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Medium Discount Medicine"))
                .andExpect(jsonPath("$[1].discountPercent").value(20.00))
                .andExpect(jsonPath("$.length()").value(2));
    }
    
    @Test
    public void testGetSubstitutesByMedicineIdSortedByDiscount() throws Exception {
        // Create mock substitute DTOs sorted by discount (highest first)
        SubstituteDetailDTO substitute1 = new SubstituteDetailDTO();
        substitute1.setId(1L);
        substitute1.setName("High Discount Medicine");
        substitute1.setManufacturers("Manufacturer 1");
        substitute1.setSaltComposition("Salt 1");
        substitute1.setMedicineType("Tablet");
        substitute1.setMrp(new BigDecimal("100.00"));
        substitute1.setBestPrice(new BigDecimal("70.00"));
        substitute1.setDiscountPercent(new BigDecimal("30.00")); // Highest discount

        SubstituteDetailDTO substitute2 = new SubstituteDetailDTO();
        substitute2.setId(2L);
        substitute2.setName("Medium Discount Medicine");
        substitute2.setManufacturers("Manufacturer 2");
        substitute2.setSaltComposition("Salt 2");
        substitute2.setMedicineType("Capsule");
        substitute2.setMrp(new BigDecimal("150.00"));
        substitute2.setBestPrice(new BigDecimal("120.00"));
        substitute2.setDiscountPercent(new BigDecimal("20.00")); // Medium discount

        List<SubstituteDetailDTO> substitutes = Arrays.asList(substitute1, substitute2);

        // Mock the service method
        when(substituteService.getSubstitutesByMedicineIdSortedByDiscountDesc(anyLong())).thenReturn(substitutes);

        // Perform the GET request and verify the response
        mockMvc.perform(get("/api/medicines/1/substitutes/sorted-by-discount"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("High Discount Medicine"))
                .andExpect(jsonPath("$[0].discountPercent").value(30.00))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Medium Discount Medicine"))
                .andExpect(jsonPath("$[1].discountPercent").value(20.00))
                .andExpect(jsonPath("$.length()").value(2));
    }
}