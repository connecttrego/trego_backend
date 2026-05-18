package com.trego.dao.impl;

import com.trego.dao.entity.MasterMedicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterMedicineRepository extends JpaRepository<MasterMedicine, Integer> {
    Page<MasterMedicine> findByNameContainingIgnoreCase(String name, Pageable pageable);
    java.util.List<MasterMedicine> findByNameIgnoreCase(String name);
}
