package com.trego.dao.impl;

import com.trego.dao.entity.OtcProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OtcProductRepository extends JpaRepository<OtcProduct, Long> {
    List<OtcProduct> findBySubcategoryId(Long subcategoryId);
}