package com.trego.api;

import com.trego.dto.MedicineDTO;
import com.trego.dto.MedicineWithStockAndVendorDTO;

import com.trego.dto.UnavailableMedicineDTO;
import com.trego.dto.view.SubstituteDetailView;
import com.trego.service.IMedicineService;
import com.trego.service.ISubstituteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

public class MedicineController {

    @Autowired
    IMedicineService medicineService;

    @Autowired
    ISubstituteService substituteService;

    @GetMapping("/medicines")
    public List<MedicineWithStockAndVendorDTO> retrieveMedicines() {
        return medicineService.findAll();
    }

    // Get a specific medicine by ID
    @GetMapping("/medicines/{id}")
    public MedicineDTO getMedicineById(@PathVariable Long id) {
        return medicineService.getMedicineById(id);
    }

    @GetMapping("/medicines/search")
    public Page<MedicineWithStockAndVendorDTO> searchProducts(
            @RequestParam String searchText,
            @RequestParam(defaultValue = "0") Integer vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return medicineService.searchMedicines(searchText, vendorId, page, size);
    }

    @GetMapping("/substitute/{id}")
    public List<SubstituteDetailView> findSubstitute(
            @PathVariable Long id
    ) {
        return substituteService.findSubstitute(id);
    }
    
    /**
     * Get unavailable medicine information including substitutes
     * This endpoint provides information about medicines that are not available 
     * from any vendor and suggests possible substitutes
     */
    @GetMapping("/medicines/{id}/unavailable")
    public UnavailableMedicineDTO getUnavailableMedicineInfo(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int requestedQuantity
    ) {
        // Create an unavailable medicine DTO with the requested information
        UnavailableMedicineDTO unavailableMedicine = new UnavailableMedicineDTO();
        unavailableMedicine.setMedicineId(id);
        unavailableMedicine.setRequestedQuantity(requestedQuantity);
        
        // Try to get substitute information
        try {
            List<SubstituteDetailView> substitutes = substituteService.findSubstitute(id);
            unavailableMedicine.setSubstitutes(substitutes);
        } catch (Exception e) {
            // Log the error but don't fail the request
            System.out.println("Error fetching substitutes for medicine ID: " + id + ", error: " + e.getMessage());
        }
        
        return unavailableMedicine;
    }

    @GetMapping("/subcategories/{subcategoryId}/medicines")
    public Page<MedicineWithStockAndVendorDTO> getMedicinesBySubcategory(
            @PathVariable Long subcategoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return medicineService.getMedicinesBySubcategory(subcategoryId, page, size);
    }

}