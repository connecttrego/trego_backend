package com.trego.service.impl;

import com.trego.dao.entity.Category;
import com.trego.dao.impl.*;
import com.trego.dto.*;
import com.trego.service.IMasterService;
import com.trego.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MasterServiceImpl implements IMasterService {

    @Autowired
    CategoryRepository categoryRepository;

    @Override
    public List<CategoryDTO> loadCategoriesByType(String type) {
        List<CategoryDTO> categoryDTOs = new ArrayList<>();
        List<Category> categories;
        
        // If type is empty, load all categories, otherwise filter by type
        if (type == null || type.isEmpty()) {
            categories = categoryRepository.findAll();
        } else {
            categories = categoryRepository.findByType(type);
        }
        
        for (Category category : categories){
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setId(category.getId());
            categoryDTO.setName(category.getName());
            // Remove the URL prefix
            categoryDTO.setLogo(category.getLogo());
            categoryDTOs.add(categoryDTO);
        }
        return categoryDTOs;
    }
}