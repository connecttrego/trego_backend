package com.trego.dao.impl;

import com.trego.dao.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface VendorRepository extends JpaRepository<Vendor, Integer> {
      // findByCategory removed - Vendor entity (vendor_informations) has no category field in ap-db-change schema
    // Custom queries can be defined here, if necessary
    java.util.Optional<Vendor> findByVendorId(Integer vendorId);
}