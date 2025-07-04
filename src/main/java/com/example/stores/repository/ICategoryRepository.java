package com.example.stores.repository;

import java.util.List;

public interface ICategoryRepository {
    List<Category> getAllCategories();
    Category getCategoryById(String categoryId);
    Category getCategoryByName(String categoryName);
    boolean addCategory(Category category);
    boolean updateCategory(Category category);
    boolean deleteCategory(String categoryId);
}