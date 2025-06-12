package com.example.stores.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductReviewService {
    Optional<Integer> getFirstRatingByOrderId(String orderId);
    List<Map<String, Object>> getReviewRatingStatistics();
}