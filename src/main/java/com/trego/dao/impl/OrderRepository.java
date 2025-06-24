package com.trego.dao.impl;

import com.trego.dao.entity.Order;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE orders o SET o.orderStatus = :status, o.cancelReason = :reason, o.cancelReasonId = :reasonId  WHERE o.id IN :ids")
    int updateOrderStatusAndReason(@Param("ids") List<Long> orderIds, @Param("status") String status, @Param("reason") String reason, @Param("reasonId") String reasonId);
}