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

    List<Stock> findByMedicineIdAndVendorId(long medicineId, Long vendorId);

    Optional<Stock> findByVendorIdAndMedicineId(long vendorId, long medicineId);

    @Query("""
    SELECT s.medicine.id, s.vendor.id, SUM(s.qty), AVG(s.discount), SUM(s.mrp)
    FROM Stock s
    WHERE s.vendor.id = :vendorId
    GROUP BY s.medicine.id, s.vendor.id
    """)
    List<Object[]> findAggregatedStockByVendor(@Param("vendorId") long vendorId);
}