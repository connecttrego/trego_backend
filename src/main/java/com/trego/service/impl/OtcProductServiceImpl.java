package com.trego.service.impl;

import com.trego.dao.entity.OtcProduct;
import com.trego.dao.impl.OtcProductRepository;
import com.trego.dto.OtcProductDTO;
import com.trego.service.IOtcProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OtcProductServiceImpl implements IOtcProductService {
    
    @Autowired
    private OtcProductRepository otcProductRepository;
    
    @Override
    public List<OtcProductDTO> getProductsBySubcategoryId(Long subcategoryId) {
        try {
            if (subcategoryId == null) {
                throw new IllegalArgumentException("Subcategory ID cannot be null");
            }
            
            List<OtcProduct> products = otcProductRepository.findBySubcategoryId(subcategoryId);
            return products.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            throw new RuntimeException("Failed to retrieve OTC products by subcategory ID: " + subcategoryId, e);
        }
    }
    
    private OtcProductDTO convertToDTO(OtcProduct product) {
        OtcProductDTO dto = new OtcProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setCategory(product.getCategory());
        dto.setSubCategory(product.getSubCategory());
        dto.setBreadcrum(product.getBreadcrum());
        dto.setDescription(product.getDescription());
        dto.setManufacturers(product.getManufacturers());
        dto.setPackaging(product.getPackaging());
        dto.setPackInfo(product.getPackInfo());
        dto.setPrice(product.getPrice());
        dto.setBestPrice(product.getBestPrice());
        dto.setDiscountPercent(product.getDiscountPercent());
        dto.setPrescriptionRequired(product.getPrescriptionRequired());
        dto.setPrimaryUse(product.getPrimaryUse());
        dto.setSaltSynonmys(product.getSaltSynonmys());
        dto.setStorage(product.getStorage());
        dto.setIntroduction(product.getIntroduction());
        dto.setUseOf(product.getUseOf());
        dto.setBenefits(product.getBenefits());
        dto.setSideEffect(product.getSideEffect());
        dto.setHowToUse(product.getHowToUse());
        dto.setHowWorks(product.getHowWorks());
        dto.setSafetyAdvise(product.getSafetyAdvise());
        dto.setIfMiss(product.getIfMiss());
        dto.setIngredients(product.getIngredients());
        dto.setAlternateBrand(product.getAlternateBrand());
        dto.setManufacturerAddress(product.getManufacturerAddress());
        dto.setForSale(product.getForSale());
        dto.setCountryOfOrigin(product.getCountryOfOrigin());
        dto.setTax(product.getTax());
        dto.setTotalPrice(product.getTotalPrice());
        return dto;
    }
}