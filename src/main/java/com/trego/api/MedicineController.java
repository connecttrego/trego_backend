package com.trego.api;

import com.trego.dto.MedicineDTO;
import com.trego.dto.MedicineWithStockAndVendorDTO;
import com.trego.dto.SubstituteDetailDTO;
import com.trego.service.IMedicineService;
import com.trego.service.ISubstituteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam(defaultValue = "0") long vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return medicineService.searchMedicines(searchText, vendorId, page, size);
    }

    @GetMapping("/substitute/{id}")
    public List<SubstituteDetailDTO> findSubstitute(
            @RequestParam(defaultValue = "0") long id
    ) {
        return substituteService.findSubstitute(id);
    }


}
