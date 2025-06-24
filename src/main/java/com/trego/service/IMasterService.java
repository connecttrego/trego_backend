package com.trego.service;

import com.trego.dto.CategoryDTO;
import com.trego.dto.MainDTO;

import java.util.List;

public interface IMasterService {

  public List<CategoryDTO> loadCategoriesByType(String type);
}
