package com.trego.dao.impl;

import com.trego.dao.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySubcategoryId(Long subcategoryId);
    
    Page<Product> findBySubcategoryId(Long subcategoryId, Pageable pageable);
}