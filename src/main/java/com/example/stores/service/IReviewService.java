package com.example.stores.service;

import com.example.stores.model.ProductReview;
import java.util.List;

public interface IReviewService {
    List<ProductReview> getReviewsForProduct(String productId);
    boolean addReview(ProductReview review);
    boolean updateReview(ProductReview review);
    boolean deleteReview(int reviewId);
    double getAverageRatingForProduct(String productId);
}