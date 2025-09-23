package com.trego.service.impl;

import com.trego.dao.entity.Subcategory;
import com.trego.dao.impl.SubcategoryRepository;
import com.trego.dto.SubcategoryDTO;
import com.trego.service.ISubcategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubcategoryServiceImpl implements ISubcategoryService {
    
    @Autowired
    private SubcategoryRepository subcategoryRepository;
    
    @Override
    public List<SubcategoryDTO> getAllSubcategories() {
        try {
            List<Subcategory> subcategories = subcategoryRepository.findAll();
            return subcategories.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            throw new RuntimeException("Failed to retrieve subcategories", e);
        }
    }
    
    @Override
    public List<SubcategoryDTO> getSubcategoriesByCategoryId(Long categoryId) {
        try {
            if (categoryId == null) {
                throw new IllegalArgumentException("Category ID cannot be null");
            }
            
            List<Subcategory> subcategories = subcategoryRepository.findByCategoryId(categoryId);
            return subcategories.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            throw new RuntimeException("Failed to retrieve subcategories by category ID: " + categoryId, e);
        }
    }
    
    private SubcategoryDTO convertToDTO(Subcategory subcategory) {
        SubcategoryDTO dto = new SubcategoryDTO();
        dto.setId(subcategory.getId());
        dto.setName(subcategory.getName());
        dto.setCategoryId(subcategory.getCategoryId());
        return dto;
    }
}