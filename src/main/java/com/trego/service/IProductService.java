package com.trego.service;

import com.trego.dto.ProductDTO;
import org.springframework.data.domain.Page;
import java.util.List;

public interface IProductService {
    List<ProductDTO> getProductsBySubcategoryId(Long subcategoryId);
    
    Page<ProductDTO> getProductsBySubcategoryId(Long subcategoryId, int page, int size);
}