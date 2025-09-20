package com.trego.service.impl;

import com.trego.dao.entity.Product;
import com.trego.dao.impl.ProductRepository;
import com.trego.dto.ProductDTO;
import com.trego.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements IProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Override
    public List<ProductDTO> getProductsBySubcategoryId(Long subcategoryId) {
        try {
            if (subcategoryId == null) {
                throw new IllegalArgumentException("Subcategory ID cannot be null");
            }
            
            List<Product> products = productRepository.findBySubcategoryId(subcategoryId);
            return products.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            throw new RuntimeException("Failed to retrieve products by subcategory ID: " + subcategoryId, e);
        }
    }
    
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setTax(product.getTax());
        dto.setTotalPrice(product.getTotalPrice());
        return dto;
    }
}