package com.trego.service;

import com.trego.dto.MedicineDTO;
import com.trego.dto.MedicineWithStockAndVendorDTO;
import com.trego.dto.SubstituteDTO;
import com.trego.dto.SubstituteDetailDTO;

import org.springframework.data.domain.Page;

import java.util.List;

public interface ISubstituteService {
    List<SubstituteDetailDTO> findSubstitute(long id);

}
