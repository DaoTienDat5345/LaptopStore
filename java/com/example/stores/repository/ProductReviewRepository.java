package com.example.stores.repository;

import java.util.List;
import java.util.Map; // Cho thống kê biểu đồ
import java.util.Optional;

public interface ProductReviewRepository {
    // Lấy rating của một product review bất kỳ thuộc một orderID
    // (Giả định tất cả sản phẩm trong đơn hàng có cùng rating)
    Optional<Integer> findFirstRatingByOrderId(String orderId);

    // Lấy tất cả product reviews cho một orderDetailsID (nếu cần)
    // List<ProductReview> findByOrderDetailsId(String orderDetailsId);

    // Phương thức để lấy dữ liệu thống kê cho biểu đồ
    List<Map<String, Object>> getReviewRatingStatistics();

    // Phương thức save ProductReview (sẽ được gọi bởi một service khác khi khách hàng đánh giá)
    // ProductReview save(ProductReview review);
}