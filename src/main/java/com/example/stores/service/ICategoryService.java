package com.example.stores.service;

import com.example.stores.model.Category;
import java.util.List;

public interface ICategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(String categoryId);
    Category getCategoryByName(String categoryName);
}