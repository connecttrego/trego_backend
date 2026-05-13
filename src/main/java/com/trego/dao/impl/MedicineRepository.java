package com.trego.dao.impl;

import com.trego.dao.entity.Medicine;
import com.trego.dto.SubstituteDetailDTO;
import com.trego.dto.view.SubstituteDetailView;

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
    @Query("SELECT m FROM vendor_medicine m " +
            "JOIN FETCH m.stocks s " +
            "JOIN FETCH s.vendor v " +
            "WHERE m.name LIKE %:name% AND v.id = :vendorId")
    Page<Medicine> findByNameWithVendorId(String name, int vendorId, Pageable pageable);

    @Query(value = """
        select
            m.vendor_medicine_id as id,
            m.name as name,
            m.photo1 as photo1,
            m.packing,
            m.manufacture as manufacturer,
            v.ref_name as vendorName,
            v.logo as vendorLogo,
            v.vendor_user_id as vendorId,
            s.discount as discount,
            s.mrp as mrp,
            (s.mrp - (s.mrp * s.discount / 100)) as bestPrice
        from vendor_medicine m
        join (
            select
                s1.vendor_medicine_id,
                s1.vendor_id,
                s1.discount,
                s1.mrp,
                (s1.mrp - (s1.mrp * s1.discount / 100)) as price
            from vendor_medicine_price s1
            join (
                select vendor_medicine_id,
                       min(mrp - (mrp * discount / 100)) as min_price
                from vendor_medicine_price
                group by vendor_medicine_id
            ) sm on sm.vendor_medicine_id = s1.vendor_medicine_id
                and (s1.mrp - (s1.mrp * s1.discount / 100)) = sm.min_price
        ) s on s.vendor_medicine_id = m.vendor_medicine_id
        join vendor_informations v on v.vendor_user_id = s.vendor_id
        where m.salt_composition = (
            select m2.salt_composition
            from vendor_medicine m2
            where m2.vendor_medicine_id = :medicineId
        )
        and m.vendor_medicine_id != :medicineId LIMIT 2
        """, nativeQuery = true)
    List<SubstituteDetailView> findSubstituteByMedicineId(@Param("medicineId") long medicineId);
//AND m.manufacturer IN ('Abbott', 'Lupin Ltd', 'Dr. Reddy’s Labs')

    Page<Medicine> findBySubcategoryId(Integer subcategoryId, Pageable pageable);


}