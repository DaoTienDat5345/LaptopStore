package com.example.stores.repository;

import java.util.List;

public interface IReviewRepository {
    List<ProductReview> getReviewsByProductId(String productId);
    boolean addReview(ProductReview review);
    boolean updateReview(ProductReview review);
    boolean deleteReview(int reviewId);
    double getAverageRatingForProduct(String productId);
    int getReviewCountForProduct(String productId);
}