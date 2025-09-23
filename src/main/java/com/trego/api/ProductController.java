package com.trego.api;

import com.trego.dto.ProductDTO;
import com.trego.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<ProductDTO>> getProductsBySubcategory(@PathVariable Long subcategoryId) {
        try {
            if (subcategoryId == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            
            List<ProductDTO> products = productService.getProductsBySubcategoryId(subcategoryId);
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}