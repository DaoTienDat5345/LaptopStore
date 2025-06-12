package com.example.stores.repository;

import com.example.stores.model.Product;
import java.util.List;
import java.util.Map;

public interface IProductRepository {
    List<Product> getAllProducts();
    List<Product> getProductsByCategory(String categoryName);
    List<Product> getProductsByCategoryId(String categoryId);
    List<Product> getProductsSortedByPriceAsc();
    List<Product> getProductsSortedByPriceDesc();
    List<Product> searchProducts(String keyword);
    List<Product> searchProductsLive(String keyword);
    List<Product> getProductsByPriceRange(Double min, Double max);
    Product getProductById(String productId);
    List<Product> getFeaturedProducts(int limit);
    List<Product> getRecentProducts(int limit);
    Map<String, List<Product>> getProductsByCategories(int productsPerCategory);
    List<Product> getProductsLimitByCategory(String categoryName, int limit);
    boolean updateProductQuantity(String productId, int newQuantity, int purchaseCount);
}