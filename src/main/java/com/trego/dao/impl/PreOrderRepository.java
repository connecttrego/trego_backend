package com.trego.dao.impl;

import com.trego.dao.entity.Medicine;
import com.trego.dao.entity.PreOrder;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PreOrderRepository extends JpaRepository<PreOrder, Long> {
    PreOrder findByUserId(long userId);
    List<PreOrder> findByUserIdAndPaymentStatus(Long userId, String paymentStatus);
    PreOrder findByIdAndPaymentStatus(Long id, String paymentStatus);

    @Query("SELECT p FROM preorders p JOIN FETCH p.orders o WHERE p.userId = :userId ORDER BY p.createdAt desc")
    Page<PreOrder> fetchAllOrdersByUserId(@Param("userId") Long userId, Pageable pageable);


    @Transactional
    @Modifying
    @Query("UPDATE preorders p SET p.orderStatus = :status WHERE p.id IN :ids")
    int updateOrderStatus(@Param("ids") List<Long> orderIds, @Param("status") String status);


}