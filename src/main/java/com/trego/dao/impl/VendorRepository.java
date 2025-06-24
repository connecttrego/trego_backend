package com.trego.dao.impl;

import com.trego.dao.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
      List<Vendor> findByCategory(String type);
    // Custom queries can be defined here, if necessary
}