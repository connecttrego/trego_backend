package com.trego.dao.impl;

import com.trego.dao.entity.Medicine;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// Spring Data JPA creates CRUD implementation at runtime automatically.
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Page<Medicine> findByNameContainingIgnoreCaseOrNameIgnoreCase(String searchText, String description, Pageable pageable);


    // JPQL query to fetch medicines along with stocks and vendor details
    @Query("SELECT m FROM medicines m " +
            "JOIN FETCH m.stocks s " +
            "JOIN FETCH s.vendor v " +
            "WHERE m.name LIKE %:name% AND v.id = :vendorId")
    Page<Medicine> findByNameWithVendorId(String name, long vendorId, Pageable pageable);

    @Query("""
        SELECT DISTINCT m
        FROM medicines m
        WHERE m.saltComposition = (
            SELECT m2.saltComposition
            FROM medicines m2
            WHERE m2.id = :medicineId
        )
        AND m.id <> :medicineId
    """)
    //AND m.manufacturer IN ('Abbott', 'Lupin Ltd', 'Dr. Reddy’s Labs')
    List<Medicine> findSubstituteByMedicineId(@Param("medicineId") long medicineId);
}