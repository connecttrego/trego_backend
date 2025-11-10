package com.trego.service;

import com.trego.dto.BucketDTO;
import com.trego.dto.BucketRequestDTO;
import com.trego.dto.response.VendorCartResponseDTO;

import java.util.List;

public interface IBucketService {
    List<BucketDTO> createOptimizedBuckets(BucketRequestDTO request);
    List<BucketDTO> createOptimizedBucketsFromPreorder(VendorCartResponseDTO preorderData);
    List<BucketDTO> getAllBuckets();
}