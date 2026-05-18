package com.trego.service;

import com.trego.dto.MedicineDTO;
import com.trego.dto.MedicineWithStockAndVendorDTO;
import com.trego.dto.response.VendorMedicinePriceResponseDTO;
import com.trego.dao.entity.MasterMedicine;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IMedicineService {
    List<MedicineWithStockAndVendorDTO> findAll();

    MedicineDTO getMedicineById(Long id);

    Page<MedicineWithStockAndVendorDTO> searchMedicines(String searchText, Integer vendorId, int page, int size);

    Page<MedicineWithStockAndVendorDTO> getMedicinesBySubcategory(Long subcategoryId, int page, int size);

    /**
     * Search medicines by medicineId and return a list of VendorMedicinePriceResponseDTO,
     * each containing all vendors selling that medicine sorted by cheapest price
     * first.
     */
    List<VendorMedicinePriceResponseDTO> searchMedicineVendorPrices(Long medicineId);

}
