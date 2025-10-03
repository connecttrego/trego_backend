package com.trego.api;

import com.trego.dto.SubstituteDetailDTO;
import com.trego.service.ISubstituteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SubstituteController {

    @Autowired
    private ISubstituteService substituteService;

    /**
     * Get all substitutes for a specific medicine
     * @param medicineId the ID of the medicine
     * @return list of substitute products
     */
    @GetMapping("/medicines/{medicineId}/substitutes")
    public List<SubstituteDetailDTO> getSubstitutesByMedicineId(@PathVariable Long medicineId) {
        return substituteService.getSubstitutesByMedicineId(medicineId);
    }
    
    /**
     * Get all substitutes for a specific medicine sorted by discount percentage in descending order
     * @param medicineId the ID of the medicine
     * @return list of substitute products sorted by discount percentage (highest first)
     */
    @GetMapping("/medicines/{medicineId}/substitutes/sorted-by-discount")
    public List<SubstituteDetailDTO> getSubstitutesByMedicineIdSortedByDiscount(@PathVariable Long medicineId) {
        return substituteService.getSubstitutesByMedicineIdSortedByDiscountDesc(medicineId);
    }
    
    /**
     * Get all substitutes for a specific medicine sorted by discount percentage in descending order
     * @param medicineId the ID of the medicine
     * @return list of substitute products sorted by discount percentage (highest first)
     */
    @GetMapping("/medicines/{medicineId}/substitutes/max-discount")
    public List<SubstituteDetailDTO> getMaxDiscountSubstitutesByMedicineId(@PathVariable Long medicineId) {
        return substituteService.getSubstitutesByMedicineIdSortedByDiscountDesc(medicineId);
    }
}