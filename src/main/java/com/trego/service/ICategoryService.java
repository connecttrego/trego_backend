package com.trego.service;

import com.trego.dto.CategoryDTO;
import java.util.List;

public interface ICategoryService {
    List<CategoryDTO> getAllCategories();
    List<CategoryDTO> getByType(String type);
}
