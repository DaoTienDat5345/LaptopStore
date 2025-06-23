package com.example.stores.service.impl;

import java.util.ArrayList;
import java.util.List;

public class ProductService {
    private ProductRepository productRepository;

    public ProductService() {
        this.productRepository = new ProductRepository();
    }

    public List<Product> getAllProducts() {
        return ProductRepository.getAllProducts();
    }

    public Product getProductById(String productId) {
        return productRepository.getProductById(productId);
    }

    public List<Product> getProductsByCategoryId(String categoryId) {
        return productRepository.getProductsByCategoryId(categoryId);
    }

    public List<Product> searchProducts(String keyword) {
        return ProductRepository.searchProducts(keyword);
    }

    public List<Product> getProductsByPriceRange(double min, double max) {
        return ProductRepository.getProductsByPriceRange(min, max);
    }

    public List<Product> getProductsSortedByPrice(boolean ascending) {
        if (ascending) {
            return ProductRepository.getProductsSortedByPriceAsc();
        } else {
            return ProductRepository.getProductsSortedByPriceDesc();
        }
    }
    public static List<Product> searchProductsLive(String keyword) {
        try {
            // Gọi phương thức từ repository
            return ProductRepository.searchProductsLive(keyword);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}