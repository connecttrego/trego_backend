package com.trego.dao.impl;

import com.trego.dao.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("SELECT oi.medicine.id, COUNT(oi) as salesCount FROM OrderItem oi GROUP BY oi.medicine.id ORDER BY salesCount DESC")
    List<Object[]> findTopSellingMedicineIds();
    
    @Query("SELECT oi.medicine.id, COUNT(oi) as salesCount FROM OrderItem oi WHERE oi.order.vendor.id = :vendorId GROUP BY oi.medicine.id ORDER BY salesCount DESC")
    List<Object[]> findTopSellingMedicineIdsByVendor(@Param("vendorId") Long vendorId);
}