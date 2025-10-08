package com.trego.api;

import com.trego.dto.BucketDTO;
import com.trego.dto.BucketRequestDTO;
import com.trego.service.IBucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buckets")
@CrossOrigin(origins = "*")
public class BucketController {

    @Autowired
    private IBucketService bucketService;

    /**
     * Create optimized medicine buckets based on requested medicines and quantities
     * @param request DTO containing map of medicine IDs and their required quantities
     * @return List of bucket options sorted by price (lowest first)
     */
    @PostMapping("/optimize")
    public ResponseEntity<List<BucketDTO>> createOptimizedBuckets(@RequestBody BucketRequestDTO request) {
        try {
            List<BucketDTO> buckets = bucketService.createOptimizedBuckets(request);
            return ResponseEntity.ok(buckets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all available buckets (placeholder for future implementation)
     * @return List of all buckets
     */
    @GetMapping
    public ResponseEntity<List<BucketDTO>> getAllBuckets() {
        try {
            List<BucketDTO> buckets = bucketService.getAllBuckets();
            return ResponseEntity.ok(buckets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}