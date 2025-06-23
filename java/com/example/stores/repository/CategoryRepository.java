package com.example.stores.repository;

import com.example.stores.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Category save(Category category);
    boolean update(Category category);
    boolean deleteById(String categoryId);
    Optional<Category> findById(String categoryId);
    Optional<Category> findByCategoryCode(String categoryCode);
    List<Category> findAll();
}