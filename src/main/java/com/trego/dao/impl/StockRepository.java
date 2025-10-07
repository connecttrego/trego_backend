package com.trego.dao.impl;

import com.trego.dao.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findByMedicineId(long id);

    List<Stock> findByVendorId(long id);
    Page<Stock> findByVendorId(Long vendorId, Pageable pageable);
    Optional<Stock> findByMedicineIdAndVendorId(long medicineId, long vendorId);
    
    // Method to get all stocks (inherited from JpaRepository)
    // List<Stock> findAll();
}