package com.example.stores.service.impl;

import com.example.stores.repository.ProductReviewRepository;
import com.example.stores.service.ProductReviewService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProductReviewServiceImpl implements ProductReviewService {
    private final ProductReviewRepository productReviewRepository;

    public ProductReviewServiceImpl(ProductReviewRepository productReviewRepository) {
        this.productReviewRepository = productReviewRepository;
    }

    @Override
    public Optional<Integer> getFirstRatingByOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            // Hoặc trả về Optional.empty() trực tiếp
            throw new IllegalArgumentException("Order ID không được để trống khi lấy đánh giá.");
        }
        return productReviewRepository.findFirstRatingByOrderId(orderId);
    }

    @Override
    public List<Map<String, Object>> getReviewRatingStatistics() {
        return productReviewRepository.getReviewRatingStatistics();
    }
}