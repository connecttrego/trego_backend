package com.trego.api;

import com.trego.dto.OtcProductDTO;
import com.trego.service.IOtcProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/otc-subcategories")
public class OtcProductController {
    
    @Autowired
    private IOtcProductService otcProductService;
    
    /**
     * Fetch and return all OTC products associated with a specific subcategory ID from the database.
     * 
     * @param subcategoryId The ID of the subcategory
     * @return List of OTC products
     */
    @GetMapping("/{subcategoryId}/products")
    public ResponseEntity<List<OtcProductDTO>> getProductsBySubcategory(@PathVariable Long subcategoryId) {
        try {
            if (subcategoryId == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            
            List<OtcProductDTO> products = otcProductService.getProductsBySubcategoryId(subcategoryId);
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}