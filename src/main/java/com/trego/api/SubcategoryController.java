package com.trego.api;

import com.trego.dto.SubcategoryDTO;
import com.trego.service.ISubcategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subcategories")
public class SubcategoryController {
    
    @Autowired
    private ISubcategoryService subcategoryService;
    
    /**
     * Fetch and return a list of all subcategories from the database.
     * 
     * @return List of subcategories
     */
    @GetMapping
    public ResponseEntity<List<SubcategoryDTO>> getAllSubcategories() {
        try {
            List<SubcategoryDTO> subcategories = subcategoryService.getAllSubcategories();
            return new ResponseEntity<>(subcategories, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Fetch and return a list of subcategories by category ID.
     * 
     * @param categoryId The ID of the category
     * @return List of subcategories
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<SubcategoryDTO>> getSubcategoriesByCategoryId(@PathVariable Long categoryId) {
        try {
            if (categoryId == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            
            List<SubcategoryDTO> subcategories = subcategoryService.getSubcategoriesByCategoryId(categoryId);
            return new ResponseEntity<>(subcategories, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}