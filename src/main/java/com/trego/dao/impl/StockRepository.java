package com.trego.dao.impl;

import com.trego.dao.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findByMedicineId(long id);

    List<Stock> findByVendorId(long id);
    Page<Stock> findByVendorId(Long vendorId, Pageable pageable);
    Optional<Stock> findByMedicineIdAndVendorId(long medicineId, long vendorId);

    // Custom query to handle cases where there might be multiple stocks for the same medicine/vendor combination
    @Query("SELECT s FROM stocks s WHERE s.medicine.id = :medicineId AND s.vendor.id = :vendorId")
    List<Stock> findStocksByMedicineIdAndVendorId(@Param("medicineId") long medicineId, @Param("vendorId") long vendorId);
}