package com.trego.service;

import com.trego.dto.ProductDTO;
import java.util.List;

public interface IProductService {
    List<ProductDTO> getProductsBySubcategoryId(Long subcategoryId);
}