package com.trego.dao.impl;

import com.trego.dao.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findByMedicineId(long id);

    

    List<Stock> findByVendorId(Integer id);
    Page<Stock> findByVendorId(Integer vendorId, Pageable pageable);
    List<Stock> findByMedicineIdAndVendorId(long medicineId, Integer vendorId);

    @Query(value = "SELECT * FROM vendor_medicine_price WHERE vendor_id = :externalVendorId",
           countQuery = "SELECT count(*) FROM vendor_medicine_price WHERE vendor_id = :externalVendorId",
           nativeQuery = true)
    Page<Stock> findByExternalVendorId(@Param("externalVendorId") Integer externalVendorId, Pageable pageable);

    // Custom query to handle cases where there might be multiple stocks for the same medicine/vendor combination
    @Query("SELECT s FROM vendor_medicine_price s WHERE s.medicine.id = :medicineId AND s.vendor.id = :vendorId")
    List<Stock> findStocksByMedicineIdAndVendorId(@Param("medicineId") long medicineId, @Param("vendorId") Integer vendorId);

      @Query("""
     SELECT s
     FROM vendor_medicine_price s
     WHERE s.medicine.id IN :medicineIds
     """)
     List<Stock> findByMedicineIds(@Param("medicineIds") List<Long> medicineIds);

    @Query(value = "SELECT * FROM vendor_medicine_price WHERE (vendor_medicine_id = :medicineId OR vendor_medicine_id IN (SELECT vendor_medicine_id FROM vendor_medicine WHERE medicine_id = :medicineId AND (vendor_id = :vendorUserId OR vendor_id = :externalVendorId))) AND (vendor_id = :vendorUserId OR vendor_id = :externalVendorId)", nativeQuery = true)
    List<Stock> findStocksByMedicineIdAndBothVendorIds(@Param("medicineId") long medicineId, @Param("vendorUserId") Integer vendorUserId, @Param("externalVendorId") Integer externalVendorId);
}