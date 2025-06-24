package com.trego.service;

import com.trego.dto.VendorDTO;

import java.util.List;

public interface IVendorService {
    List<VendorDTO> findVendorsByType(String type);

    VendorDTO getVendorByIdOrMedicine(Long id, String searchText , int page, int size);
}