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

    List<Stock> findByMedicineId(Long id);

    

    List<Stock> findByVendorId(Integer id);

    // ── BATCH FETCH: saare vendors ke stocks ek hi query mein ──
    // N+1 fix: loop mein findByVendorId() call karne ki jagah
    
    @Query("SELECT s FROM vendor_medicine_price s " +
           "LEFT JOIN FETCH s.medicine " +
           "LEFT JOIN FETCH s.vendor " +
           "WHERE s.vendor.id IN :vendorIds")
    List<Stock> findAllByVendorIds(@Param("vendorIds") List<Integer> vendorIds);


    Page<Stock> findByVendorId(Integer vendorId, Pageable pageable);
    List<Stock> findByMedicineIdAndVendorId(Integer medicineId, Integer vendorId);

    @Query(value = "SELECT * FROM vendor_medicine_price WHERE vendor_id = :externalVendorId",
           countQuery = "SELECT count(*) FROM vendor_medicine_price WHERE vendor_id = :externalVendorId",
           nativeQuery = true)
    Page<Stock> findByExternalVendorId(@Param("externalVendorId") Integer externalVendorId, Pageable pageable);

    // Custom query to handle cases where there might be multiple stocks for the same medicine/vendor combination
    @Query("SELECT s FROM vendor_medicine_price s WHERE s.medicine.id = :medicineId AND s.vendor.id = :vendorId")
    List<Stock> findStocksByMedicineIdAndVendorId(@Param("medicineId") Integer medicineId, @Param("vendorId") Integer vendorId);

      @Query("""
     SELECT s
     FROM vendor_medicine_price s
     WHERE s.medicine.medicineId IN :medicineIds
     """)
     List<Stock> findByMedicineIds(@Param("medicineIds") List<Integer> medicineIds);

    @Query(value = "SELECT * FROM vendor_medicine_price WHERE (vendor_medicine_id = :medicineId OR vendor_medicine_id IN (SELECT vendor_medicine_id FROM vendor_medicine WHERE medicine_id = :medicineId AND (vendor_id = :vendorUserId OR vendor_id = :externalVendorId))) AND (vendor_id = :vendorUserId OR vendor_id = :externalVendorId)", nativeQuery = true)
    List<Stock> findStocksByMedicineIdAndBothVendorIds(@Param("medicineId") Integer medicineId, @Param("vendorUserId") Integer vendorUserId, @Param("externalVendorId") Integer externalVendorId);

    // Lookup by master medicine_id (medicine_master_db_table PK) for vendors like Super Vendor
    // whose vendor_medicine.medicine_id maps to the master table, not vendor_medicine_id
    @Query(value = """
        SELECT vmp.* FROM vendor_medicine_price vmp
        JOIN vendor_medicine vm ON vm.vendor_medicine_id = vmp.vendor_medicine_id
        WHERE vm.medicine_id = :masterMedicineId
          AND (vmp.vendor_id = :vendorUserId OR vmp.vendor_id = :externalVendorId)
        """, nativeQuery = true)
    List<Stock> findStocksByMasterMedicineIdAndBothVendorIds(
            @Param("masterMedicineId") Integer masterMedicineId,
            @Param("vendorUserId") Integer vendorUserId,
            @Param("externalVendorId") Integer externalVendorId);
}