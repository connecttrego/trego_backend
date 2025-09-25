package com.trego.service.impl;

import com.trego.dao.entity.Substitute;
import com.trego.dao.impl.SubstituteRepository;
import com.trego.dto.SubstituteDetailDTO;
import com.trego.service.ISubstituteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubstituteServiceImpl implements ISubstituteService {

    @Autowired
    private SubstituteRepository substituteRepository;

    @Override
    public List<SubstituteDetailDTO> getSubstitutesByMedicineId(Long medicineId) {
        // Get substitutes ordered by best price (low to high)
        List<Substitute> substitutes = substituteRepository.findByMedicineIdOrderByBestPriceAsc(medicineId);
        List<SubstituteDetailDTO> substituteDTOs = new ArrayList<>();
        
        for (Substitute substitute : substitutes) {
            substituteDTOs.add(convertToDTO(substitute));
        }
        
        // Limit to 2 results
        return substituteDTOs.stream()
                .limit(2)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SubstituteDetailDTO> getSubstitutesByMedicineIdSortedByPrice(Long medicineId) {
        return getSubstitutesByMedicineId(medicineId);
    }
    
    @Override
    public List<SubstituteDetailDTO> getSubstitutesByMedicineIdSortedByDiscountDesc(Long medicineId) {
        // Get substitutes ordered by discount percentage (high to low)
        List<Substitute> substitutes = substituteRepository.findByMedicineIdOrderByDiscountPercentDesc(medicineId);
        List<SubstituteDetailDTO> substituteDTOs = new ArrayList<>();
        
        for (Substitute substitute : substitutes) {
            substituteDTOs.add(convertToDTO(substitute));
        }
        
        // Limit to 2 results
        return substituteDTOs.stream()
                .limit(2)
                .collect(Collectors.toList());
    }
    


    @Override
    public SubstituteDetailDTO convertToDTO(Substitute substitute) {
        SubstituteDetailDTO dto = new SubstituteDetailDTO();
        
        dto.setId(substitute.getId());
        dto.setName(substitute.getName());
        dto.setManufacturers(substitute.getManufacturers());
        dto.setSaltComposition(substitute.getSaltComposition());
        dto.setMedicineType(substitute.getMedicineType());
        dto.setStock(substitute.getStock());
        dto.setIntroduction(substitute.getIntroduction());
        dto.setBenefits(substitute.getBenefits());
        dto.setDescription(substitute.getDescription());
        dto.setHowToUse(substitute.getHowToUse());
        dto.setSafetyAdvise(substitute.getSafetyAdvise());
        dto.setIfMiss(substitute.getIfMiss());
        dto.setPackaging(substitute.getPackaging());
        dto.setPackagingType(substitute.getPackagingType());
        dto.setMrp(substitute.getMrp());
        dto.setBestPrice(substitute.getBestPrice());
        dto.setDiscountPercent(substitute.getDiscountPercent());
        dto.setViews(substitute.getViews());
        dto.setBought(substitute.getBought());
        dto.setPrescriptionRequired(substitute.getPrescriptionRequired());
        dto.setLabel(substitute.getLabel());
        dto.setFactBox(substitute.getFactBox());
        dto.setPrimaryUse(substitute.getPrimaryUse());
        dto.setStorage(substitute.getStorage());
        dto.setUseOf(substitute.getUseOf());
        dto.setCommonSideEffect(substitute.getCommonSideEffect());
        dto.setAlcoholInteraction(substitute.getAlcoholInteraction());
        dto.setPregnancyInteraction(substitute.getPregnancyInteraction());
        dto.setLactationInteraction(substitute.getLactationInteraction());
        dto.setDrivingInteraction(substitute.getDrivingInteraction());
        dto.setKidneyInteraction(substitute.getKidneyInteraction());
        dto.setLiverInteraction(substitute.getLiverInteraction());
        dto.setManufacturerAddress(substitute.getManufacturerAddress());
        dto.setCountryOfOrigin(substitute.getCountryOfOrigin());
        dto.setForSale(substitute.getForSale());
        dto.setQa(substitute.getQa());
        
        return dto;
    }
}