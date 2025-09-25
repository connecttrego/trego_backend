package com.trego.service;

import com.trego.dto.SubstituteDetailDTO;
import com.trego.dao.entity.Substitute;

import java.math.BigDecimal;
import java.util.List;

public interface ISubstituteService {
    List<SubstituteDetailDTO> getSubstitutesByMedicineId(Long medicineId);
    List<SubstituteDetailDTO> getSubstitutesByMedicineIdSortedByPrice(Long medicineId);
    List<SubstituteDetailDTO> getSubstitutesByMedicineIdSortedByDiscountDesc(Long medicineId);
    SubstituteDetailDTO convertToDTO(Substitute substitute);
}