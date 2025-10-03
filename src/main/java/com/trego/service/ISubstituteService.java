package com.trego.service;

import com.trego.dto.MedicineDTO;
import com.trego.dto.MedicineWithStockAndVendorDTO;
import com.trego.dto.SubstituteDTO;
import com.trego.dto.SubstituteDetailDTO;
import com.trego.dto.view.SubstituteDetailView;

import org.springframework.data.domain.Page;

import java.util.List;

public interface ISubstituteService {
    List<SubstituteDetailView> findSubstitute(long id);

}
