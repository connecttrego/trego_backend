package com.trego.dto;

import lombok.Data;
import java.util.List;

@Data
public class BucketRequestDTO {
    private List<Long> medicineIds;
}