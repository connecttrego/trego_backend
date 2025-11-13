package com.trego.service;

import com.trego.dto.BucketDTO;
import com.trego.dto.BucketRequestDTO;
import com.trego.dto.response.VandorCartResponseDTO;

import java.util.List;

public interface IBucketService {
    List<BucketDTO> createOptimizedBuckets(BucketRequestDTO request);
    List<BucketDTO> createOptimizedBucketsFromPreorder(VandorCartResponseDTO preorderData);
    List<BucketDTO> getAllBuckets();
}