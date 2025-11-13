package com.trego.api;

import com.trego.dto.BucketDTO;
import com.trego.dto.BucketRequestDTO;
import com.trego.dto.SelectedSubstituteDTO;
import com.trego.dto.response.VandorCartResponseDTO;
import com.trego.dto.view.SubstituteDetailView;
import com.trego.service.IBucketService;
import com.trego.service.IPreOrderService;
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
    
    @Autowired
    private IPreOrderService preOrderService;

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
     * Create optimized medicine buckets based on a preorder
     * @param preorderID The ID of the preorder to optimize
     * @return List of bucket options sorted by price (lowest first)
     */
    @GetMapping("/optimize/preorder/{preorderID}")
    public ResponseEntity<List<BucketDTO>> createOptimizedBucketsFromPreorder(@PathVariable Long preorderID) {
        try {
            VandorCartResponseDTO vendorCartData = preOrderService.vendorSpecificPrice(preorderID);
            List<BucketDTO> buckets = bucketService.createOptimizedBucketsFromPreorder(vendorCartData);
            return ResponseEntity.ok(buckets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Add a substitute to a bucket and update the total amount
     * @param bucketId The ID of the bucket (vendor ID)
     * @param originalMedicineId The ID of the original unavailable medicine
     * @param substituteMedicineId The ID of the substitute medicine
     * @param quantity The quantity of the substitute to add
     * @return The updated bucket
     */
    @PostMapping("/{bucketId}/substitutes")
    public ResponseEntity<BucketDTO> addSubstituteToBucket(
            @PathVariable Long bucketId,
            @RequestParam Long originalMedicineId,
            @RequestParam Long substituteMedicineId,
            @RequestParam int quantity) {
        try {
            // In a real implementation, you would:
            // 1. Retrieve the bucket by ID
            // 2. Retrieve the substitute details by ID
            // 3. Call bucketService.addSubstituteToBucket
            // 4. Return the updated bucket
            
            // For now, we're just showing the API structure
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Remove a substitute from a bucket and update the total amount
     * @param bucketId The ID of the bucket (vendor ID)
     * @param substituteMedicineId The ID of the substitute medicine to remove
     * @return The updated bucket
     */
    @DeleteMapping("/{bucketId}/substitutes/{substituteMedicineId}")
    public ResponseEntity<BucketDTO> removeSubstituteFromBucket(
            @PathVariable Long bucketId,
            @PathVariable Long substituteMedicineId) {
        try {
            // In a real implementation, you would:
            // 1. Retrieve the bucket by ID
            // 2. Call bucketService.removeSubstituteFromBucket
            // 3. Return the updated bucket
            
            // For now, we're just showing the API structure
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}