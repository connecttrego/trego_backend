package com.trego.dto;

import lombok.Data;

@Data
public class BucketOrderRequestDTO {
    private long userId;
    private long addressId;
    private long preOrderId; // The original preorder ID
    private Long bucketId; // The selected bucket ID
    private Long vendorId; // The vendor ID from the bucket
}