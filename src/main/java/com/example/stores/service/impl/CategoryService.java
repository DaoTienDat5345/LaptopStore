package com.example.stores.service.impl;

import java.util.List;

public class CategoryService {
    private final CategoryRepository categoryRepository;
    
    public CategoryService() {
        this.categoryRepository = new CategoryRepository();
    }
    
    public List<Category> getAllCategories() {
        return categoryRepository.getAllCategories();
    }
    
    public Category getCategoryById(String categoryId) {
        return categoryRepository.getCategoryById(categoryId);
    }
    
    public boolean addCategory(Category category) {
        return categoryRepository.addCategory(category);
    }
    
    public boolean updateCategory(Category category) {
        return categoryRepository.updateCategory(category);
    }
    
    public boolean deleteCategory(String categoryId) {
        return categoryRepository.deleteCategory(categoryId);
    }
}