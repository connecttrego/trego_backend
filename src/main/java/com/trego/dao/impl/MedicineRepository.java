package com.trego.dao.impl;

import com.trego.dao.entity.Medicine;

import com.trego.dto.view.SubstituteDetailView;
import com.trego.dto.view.VendorMedicinePriceView;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// Spring Data JPA creates CRUD implementation at runtime automatically.
public interface MedicineRepository extends JpaRepository<Medicine, Integer> {

    Page<Medicine> findByNameContainingIgnoreCaseOrNameIgnoreCase(String searchText, String description, Pageable pageable);


    // JPQL query to fetch medicines along with stocks and vendor details
    @Query("SELECT m FROM vendor_medicine m " +
            "JOIN FETCH m.stocks s " +
            "JOIN FETCH s.vendor v " +
            "WHERE m.name LIKE %:name% AND v.id = :vendorId")
    Page<Medicine> findByNameWithVendorId(String name, Integer vendorId, Pageable pageable);

    @Query(value = """
        select
            m.vendor_medicine_id as id,
            m.name as name,
            meds.photo1 as photo1,
            meds.packing as packing,
            m.manufacture as manufacturer,
            v.ref_name as vendorName,
            v.logo as vendorLogo,
            v.vendor_user_id as vendorId,
            s.discount as discount,
            s.mrp as mrp,
            (s.mrp - (s.mrp * s.discount / 100)) as bestPrice
        from vendor_medicine m
        left join medicines meds on meds.medicine_id = m.medicine_id
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
        join vendor_informations v on (v.vendor_user_id = s.vendor_id or v.vendor_id = s.vendor_id)
        where m.salt_composition = (
            select m2.salt_composition
            from vendor_medicine m2
            where m2.vendor_medicine_id = :medicineId
        )
        and m.vendor_medicine_id != :medicineId LIMIT 2
        """, nativeQuery = true)
    List<SubstituteDetailView> findSubstituteByMedicineId(@Param("medicineId") Integer medicineId);
//AND m.manufacturer IN ('Abbott', 'Lupin Ltd', 'Dr. Reddy’s Labs')

    Page<Medicine> findBySubcategoryId(Integer subcategoryId, Pageable pageable);
    
    Page<Medicine> findByVendorId(Integer vendorId, Pageable pageable);

    List<Medicine> findByMedicineId(Integer medicineId);

    /**
     * Given a medicine_id (FK to medicine_master_db_table), return ALL vendors
     * selling that medicine along with their cheapest stock variant,
     * sorted by selling price ASC (cheapest first).
     *
     * The query:
     * 1. Looks up vendor_medicine by medicine_id (FK → medicine_master_db_table)
     * 2. Joins with vendor_medicine_price (stock) to get pricing/qty/expiry
     * 3. Joins with vendor_informations to get vendor name/logo/rating/delivery time
     * 4. Computes selling_price = mrp - (mrp * discount / 100)
     * 5. For each (vendor, medicine) pair, picks the cheapest stock variant
     * 6. Orders all results by selling_price ASC (cheapest first)
     */
    @Query(value = """
        SELECT
            m.medicine_id AS medicineId,
            m.name AS medicineName,
            m.manufacture AS manufacturer,
            m.salt_composition AS saltComposition,
            mi.image_1 AS photo1,
            IFNULL(mi.packing, m.packing_type) AS packing,
            mi.use_of AS useOf,
            s.price_id AS stockId,
            s.mrp AS mrp,
            s.discount AS discount,
            s.quantity AS qty,
            s.expiry_date AS expiryDate,
            (s.mrp - (s.mrp * s.discount / 100)) AS sellingPrice,
            v.vendor_user_id AS vendorId,
            v.ref_name AS vendorName,
            v.logo AS vendorLogo,
            v.rating AS vendorRating,
            v.delivery_time_minutes AS deliveryTime
        FROM vendor_medicine m
        LEFT JOIN vendor_medicine_information mi ON mi.vendor_medicine_id = m.vendor_medicine_id
        JOIN vendor_medicine_price s ON s.vendor_medicine_id = m.vendor_medicine_id
        JOIN vendor_informations v ON (v.vendor_id = s.vendor_id OR v.vendor_user_id = s.vendor_id)
        WHERE m.medicine_id = :medicineId
          AND s.quantity >= 0
          AND s.price_id = (
              SELECT sp.price_id
              FROM vendor_medicine_price sp
              WHERE sp.vendor_medicine_id = m.vendor_medicine_id
                AND (sp.vendor_id = v.vendor_id OR sp.vendor_id = v.vendor_user_id)
                AND sp.quantity >= 0
              ORDER BY (sp.mrp - (sp.mrp * sp.discount / 100)) ASC
              LIMIT 1
          )
        ORDER BY sellingPrice ASC
        """, nativeQuery = true)
    List<VendorMedicinePriceView> searchMedicineVendorPrices(@Param("medicineId") Integer medicineId);

    List<Medicine> findByMedicineIdIn(List<Integer> medicineIds);

}