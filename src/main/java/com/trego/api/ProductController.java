package com.trego.api;

import com.trego.dto.ProductDTO;
import com.trego.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subcategories")
public class ProductController {
    
    @Autowired
    private IProductService productService;
    
    /**
     * Fetch and return all products associated with a specific subcategory ID from the database.
     * 
     * @param subcategoryId The ID of the subcategory
     * @return List of products
     */
    @GetMapping("/{subcategoryId}/products")
    public ResponseEntity<?> getProductsBySubcategory(
            @PathVariable Long subcategoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (subcategoryId == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            
            if (page < 0 || size <= 0) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            
            Page<ProductDTO> productsPage = productService.getProductsBySubcategoryId(subcategoryId, page, size);
            return new ResponseEntity<>(productsPage, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}