package com.trego.api;

import com.trego.dto.CategoryDTO;
import com.trego.dto.MainDTO;
import com.trego.service.IMainService;
import com.trego.service.IMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MasterController {

    @Autowired
    IMasterService masterService;

    @GetMapping("/categories")
    public List<CategoryDTO> allCategories() {
        return masterService.loadCategoriesByType(""); // Pass empty string to load all categories
    }

}