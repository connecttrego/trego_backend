package com.trego.service;

import com.trego.dto.BucketDTO;
import com.trego.dto.BucketRequestDTO;

import java.util.List;

public interface IBucketService {
    List<BucketDTO> createOptimizedBuckets(BucketRequestDTO request);
    List<BucketDTO> getAllBuckets();
}