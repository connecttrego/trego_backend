package com.trego.service.impl;

import com.trego.dao.entity.Substitute;
import com.trego.dao.impl.SubstituteRepository;
import com.trego.dto.SubstituteDetailDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class SubstituteServiceImplTest {

    @Mock
    private SubstituteRepository substituteRepository;

    @InjectMocks
    private SubstituteServiceImpl substituteService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetSubstitutesByMedicineIdSortedAndLimited() {
        // Create mock substitutes with different prices
        Substitute substitute1 = new Substitute();
        substitute1.setId(1L);
        substitute1.setName("Expensive Substitute");
        substitute1.setBestPrice(new BigDecimal("100.00"));

        Substitute substitute2 = new Substitute();
        substitute2.setId(2L);
        substitute2.setName("Cheap Substitute");
        substitute2.setBestPrice(new BigDecimal("50.00"));

        Substitute substitute3 = new Substitute();
        substitute3.setId(3L);
        substitute3.setName("Medium Substitute");
        substitute3.setBestPrice(new BigDecimal("75.00"));

        // Order them by price ascending as the repository method would return them
        List<Substitute> substitutes = Arrays.asList(substitute2, substitute3, substitute1);

        // Mock the repository to return the substitutes
        when(substituteRepository.findByMedicineIdOrderByBestPriceAsc(anyLong())).thenReturn(substitutes);

        // Call the service method
        List<SubstituteDetailDTO> result = substituteService.getSubstitutesByMedicineId(1L);

        // Verify that only 2 results are returned
        assertEquals(2, result.size());

        // Verify that results are sorted by price (low to high)
        assertEquals("Cheap Substitute", result.get(0).getName());
        assertEquals("Medium Substitute", result.get(1).getName());
        assertEquals(new BigDecimal("50.00"), result.get(0).getBestPrice());
        assertEquals(new BigDecimal("75.00"), result.get(1).getBestPrice());
    }

    @Test
    public void testGetSubstitutesByMedicineIdWithLessThanTwoSubstitutes() {
        // Create mock substitutes with different prices
        Substitute substitute1 = new Substitute();
        substitute1.setId(1L);
        substitute1.setName("Only Substitute");
        substitute1.setBestPrice(new BigDecimal("50.00"));

        List<Substitute> substitutes = Arrays.asList(substitute1);

        // Mock the repository to return the substitutes
        when(substituteRepository.findByMedicineIdOrderByBestPriceAsc(anyLong())).thenReturn(substitutes);

        // Call the service method
        List<SubstituteDetailDTO> result = substituteService.getSubstitutesByMedicineId(1L);

        // Verify that only 1 result is returned
        assertEquals(1, result.size());

        // Verify the result
        assertEquals("Only Substitute", result.get(0).getName());
        assertEquals(new BigDecimal("50.00"), result.get(0).getBestPrice());
    }
    

    
    @Test
    public void testGetSubstitutesByMedicineIdSortedByDiscountDesc() {
        // Create mock substitutes with different discount percentages
        Substitute substitute1 = new Substitute();
        substitute1.setId(1L);
        substitute1.setName("Low Discount Medicine");
        substitute1.setDiscountPercent(new BigDecimal("10.00"));

        Substitute substitute2 = new Substitute();
        substitute2.setId(2L);
        substitute2.setName("High Discount Medicine");
        substitute2.setDiscountPercent(new BigDecimal("30.00"));

        Substitute substitute3 = new Substitute();
        substitute3.setId(3L);
        substitute3.setName("Medium Discount Medicine");
        substitute3.setDiscountPercent(new BigDecimal("20.00"));

        // Order them by discount descending as the repository method would return them
        List<Substitute> substitutes = Arrays.asList(substitute2, substitute3, substitute1);

        // Mock the repository to return the substitutes sorted by discount descending
        when(substituteRepository.findByMedicineIdOrderByDiscountPercentDesc(anyLong())).thenReturn(substitutes);

        // Call the service method
        List<SubstituteDetailDTO> result = substituteService.getSubstitutesByMedicineIdSortedByDiscountDesc(1L);

        // Verify that only 2 results are returned
        assertEquals(2, result.size());

        // Verify that results are sorted by discount (high to low)
        assertEquals("High Discount Medicine", result.get(0).getName());
        assertEquals("Medium Discount Medicine", result.get(1).getName());
        assertEquals(new BigDecimal("30.00"), result.get(0).getDiscountPercent());
        assertEquals(new BigDecimal("20.00"), result.get(1).getDiscountPercent());
    }
    
    @Test
    public void testGetSubstitutesByMedicineIdSortedByDiscountDescWithLessThanTwoSubstitutes() {
        // Create mock substitutes with different discount percentages
        Substitute substitute1 = new Substitute();
        substitute1.setId(1L);
        substitute1.setName("Only Substitute");
        substitute1.setDiscountPercent(new BigDecimal("15.50"));

        List<Substitute> substitutes = Arrays.asList(substitute1);

        // Mock the repository to return the substitutes
        when(substituteRepository.findByMedicineIdOrderByDiscountPercentDesc(anyLong())).thenReturn(substitutes);

        // Call the service method
        List<SubstituteDetailDTO> result = substituteService.getSubstitutesByMedicineIdSortedByDiscountDesc(1L);

        // Verify that only 1 result is returned
        assertEquals(1, result.size());

        // Verify the result
        assertEquals("Only Substitute", result.get(0).getName());
        assertEquals(new BigDecimal("15.50"), result.get(0).getDiscountPercent());
    }
}