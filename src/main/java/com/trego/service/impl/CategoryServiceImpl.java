package com.trego.service.impl;

import com.trego.dao.impl.CategoryRepository;
import com.trego.dao.entity.Category;
import com.trego.dto.CategoryDTO;
import com.trego.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CategoryDTO> getByType(String type) {
        List<Category> list = categoryRepository.findByType(type);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    private CategoryDTO toDto(Category c) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setLogo(c.getLogo());
        return dto;
    }
}
