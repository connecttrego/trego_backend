package com.trego.service;

import com.trego.dto.MedicineDTO;
import com.trego.dto.MedicineWithStockAndVendorDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IMedicineService {
    List<MedicineWithStockAndVendorDTO> findAll();

    MedicineDTO getMedicineById(Long id);

    Page<MedicineWithStockAndVendorDTO> searchMedicines(String searchText, long vendorId, int page, int size);

}
