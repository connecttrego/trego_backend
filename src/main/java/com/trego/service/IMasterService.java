package com.trego.service;

import com.trego.dto.CategoryDTO;
import com.trego.dto.MainDTO;

import java.util.List;

public interface IMasterService {

  public List<CategoryDTO> loadCategoriesByType(String type);
  
  // Default method to load all categories
  public default List<CategoryDTO> loadAllCategories() {
      return loadCategoriesByType("");
  }
}
