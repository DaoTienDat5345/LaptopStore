package com.example.stores.service;

import java.util.List;
import java.util.Map;

public interface IProductService {
    List<Product> getAllProducts();
    List<Product> getProductsByCategory(String categoryName);
    List<Product> getProductsSortedByPriceAsc();
    List<Product> getProductsSortedByPriceDesc();
    List<Product> searchProducts(String keyword);
    List<Product> searchProductsLive(String keyword);
    List<Product> getProductsByPriceRange(Double min, Double max);
    Product getProductById(String productId);
    List<Product> getFeaturedProducts(int limit);
    List<Product> getRecentProducts(int limit);
    Map<String, List<Product>> getProductsByCategories(int productsPerCategory);
}