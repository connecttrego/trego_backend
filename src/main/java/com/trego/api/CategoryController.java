package com.trego.api;

import com.trego.dto.CategoryDTO;
import com.trego.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;


    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAll() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping(params = "type")
    public ResponseEntity<List<CategoryDTO>> getByType(@RequestParam String type) {
        return ResponseEntity.ok(categoryService.getByType(type));
    }
}
