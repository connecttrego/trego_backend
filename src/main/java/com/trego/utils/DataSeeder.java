package com.trego.utils;

import com.trego.dao.entity.Category;
import com.trego.dao.entity.Subcategory;
import com.trego.dao.entity.Product;
import com.trego.dao.impl.CategoryRepository;
import com.trego.dao.impl.SubcategoryRepository;
import com.trego.dao.impl.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder {

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private SubcategoryRepository subcategoryRepository;
    
    @Autowired
    private ProductRepository productRepository;

    @PostConstruct
    public void seedCategories() {
        try {
            // Check if categories already exist
            if (categoryRepository.count() == 0) {
                // Create the required categories without type restriction
                List<Category> categories = Arrays.asList(
                );
                
                categoryRepository.saveAll(categories);
                System.out.println("Seeded 3 categories: Tablets, Syrups, Antibiotics");
            }
            
            // Note: Subcategories and products will be managed through the API
            // and fetched from the database rather than seeded
            
        } catch (DataAccessException e) {
            System.err.println("Failed to seed categories: " + e.getMessage());
            System.err.println("This might be due to database connectivity issues. The application will continue to start.");
        } catch (Exception e) {
            System.err.println("Unexpected error while seeding categories: " + e.getMessage());
        }
    }

    private Category createCategory(String name, String logo, String type, String prescriptionRequired, String createdBy) {
        Category category = new Category();
        category.setName(name);
        category.setLogo(logo);
        category.setType(type);
        category.setPrescriptionRequired(prescriptionRequired);
        category.setCreatedBy(createdBy);
        return category;
    }
    
    private Subcategory createSubcategory(String name, Long categoryId, String description, String image) {
        Subcategory subcategory = new Subcategory();
        subcategory.setName(name);
        subcategory.setCategoryId(categoryId);
        subcategory.setDescription(description);
        subcategory.setImage(image);
        return subcategory;
    }
    
    private Product createProduct(String name, String description, BigDecimal price, BigDecimal tax, 
                                 Long subcategoryId, String image, Integer stock) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setTax(tax);
        product.setSubcategoryId(subcategoryId);
        product.setImage(image);
        product.setStock(stock);
        return product;
    }
}