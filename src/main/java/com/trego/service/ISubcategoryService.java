package com.trego.service;

import com.trego.dto.SubcategoryDTO;
import java.util.List;

public interface ISubcategoryService {
    List<SubcategoryDTO> getAllSubcategories();
    List<SubcategoryDTO> getSubcategoriesByCategoryId(Long categoryId);
}