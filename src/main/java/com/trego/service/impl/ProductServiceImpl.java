package com.trego.service.impl;

import com.trego.dto.ProductDTO;
import com.trego.service.IProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements IProductService {

    @Override
    public List<ProductDTO> getProductsBySubcategoryId(Long subcategoryId) {
        return new ArrayList<>();
    }

    @Override
    public Page<ProductDTO> getProductsBySubcategoryId(Long subcategoryId, int page, int size) {
        return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);
    }
}
