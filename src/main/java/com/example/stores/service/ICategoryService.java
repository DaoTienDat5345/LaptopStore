package com.example.stores.service;

import java.util.List;

public interface ICategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(String categoryId);
    Category getCategoryByName(String categoryName);
}