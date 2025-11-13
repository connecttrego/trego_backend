package com.trego.dao.impl;

import com.trego.dao.entity.Substitute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubstituteRepository extends JpaRepository<Substitute, Long> {
    List<Substitute> findByMedicineId(Long medicineId);
    List<Substitute> findByMedicineIdOrderByBestPriceAsc(Long medicineId);
    List<Substitute> findByMedicineIdOrderByDiscountPercentDesc(Long medicineId);
}