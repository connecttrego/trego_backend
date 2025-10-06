package com.trego.dao.impl;

import com.trego.dao.entity.OrderItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("SELECT oi.medicine.id, COUNT(oi) as salesCount " +
            "FROM OrderItem oi " +
            "GROUP BY oi.medicine.id " +
            "ORDER BY COUNT(oi) DESC")
    List<Object[]> findTopSellingMedicineIds(Pageable pageable);

//    @Query("SELECT oi.medicine.id, COUNT(oi) as salesCount FROM OrderItem oi WHERE oi.order.vendor.id = :vendorId GROUP BY oi.medicine.id ORDER BY salesCount DESC")
//    List<Object[]> findTopSellingMedicineIdsByVendor(@Param("vendorId") Long vendorId);

    @Query("""
                SELECT oi.order.vendor.id as vendorId, oi.medicine.id as medicineId, 
                       COUNT(oi.id) as salesCount, MAX(oi.mrp) as mrp, MAX(oi.qty) as qty
                FROM OrderItem oi
                GROUP BY oi.order.vendor.id, oi.medicine.id
                ORDER BY salesCount DESC
            """)
    List<Object[]> findVendorMedicineSales();

    @Query("""
                SELECT oi.order.vendor.id as vendorId, oi.medicine.id as medicineId, 
                       COUNT(oi.id) as salesCount, MAX(oi.mrp) as mrp, MAX(oi.qty) as qty
                FROM OrderItem oi
                JOIN oi.order.vendor v
                WHERE v.category = :category
                GROUP BY oi.order.vendor.id, oi.medicine.id
                ORDER BY salesCount DESC
            """)
    List<Object[]> findVendorMedicineSalesByCategory(@Param("category") String category);

}