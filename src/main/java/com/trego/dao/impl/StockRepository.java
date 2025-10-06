package com.trego.dao.impl;

import com.trego.dao.entity.Stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findByMedicineId(Long id);

    List<Stock> findByVendorId(Long id);

    Page<Stock> findByVendorId(Long vendorId, Pageable pageable);

    Optional<Stock> findByMedicineIdAndVendorId(Long medicineId, Long vendorId);

    Optional<Stock> findByVendorIdAndMedicineId(Long vendorId, Long medicineId);
}
