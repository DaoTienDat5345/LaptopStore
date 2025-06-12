package com.example.stores.service.impl;

import com.example.stores.config.DatabaseConnection;
import com.example.stores.model.ProductReview;
import com.example.stores.repository.impl.ReviewRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class xử lý logic nghiệp vụ liên quan đến đánh giá sản phẩm
 */
public class ReviewService {

    private ReviewRepository reviewRepository;

    public ReviewService() {
        this.reviewRepository = new ReviewRepository();
    }

    /**
     * Lấy danh sách đánh giá sản phẩm theo ID sản phẩm
     */
    public List<ProductReview> getReviewsByProductId(String productId) {
        return reviewRepository.findByProductId(productId);
    }

    /**
     * Thêm một đánh giá mới cho sản phẩm
     * Thực hiện các kiểm tra nghiệp vụ trước khi lưu đánh giá
     */
    public boolean addReview(ProductReview review) {
        // Kiểm tra dữ liệu hợp lệ
        if (review.getProductID() == null || review.getProductID().isEmpty()) {
            System.err.println("ProductID không thể để trống");
            return false;
        }

        if (review.getRating() < 1 || review.getRating() > 5) {
            System.err.println("Rating phải từ 1-5");
            return false;
        }

        // Đặt ngày đánh giá là hiện tại nếu chưa có
        if (review.getReviewDate() == null) {
            review.setReviewDate(LocalDateTime.now());
        }

        // Mặc định đánh giá chưa được phê duyệt
        if (!review.isApproved()) {
            review.setApproved(false);
        }

        // Lưu đánh giá vào database thông qua repository
        return reviewRepository.save(review);
    }

    /**
     * Lấy điểm đánh giá trung bình của sản phẩm
     */
    public double getAverageRating(String productId) {
        return reviewRepository.getAverageRating(productId);
    }

    /**
     * Đếm số lượng đánh giá của sản phẩm
     */
    public int getReviewCount(String productId) {
        return reviewRepository.countByProductId(productId);
    }

    /**
     * Lưu đánh giá sản phẩm vào database
     * @param review Đối tượng ProductReview cần lưu
     * @return true nếu lưu thành công, false nếu có lỗi
     */
    public boolean saveReview(ProductReview review) {
        String sql = "INSERT INTO ProductReview (productID, customerID, orderDetailsID, rating, comment, reviewDate, isApproved) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, review.getProductID());
            pstmt.setInt(2, review.getCustomerID());
            pstmt.setString(3, review.getOrderDetailsID());
            pstmt.setInt(4, review.getRating());
            pstmt.setString(5, review.getComment());
            pstmt.setTimestamp(6, Timestamp.valueOf(review.getReviewDate()));
            pstmt.setBoolean(7, review.isApproved());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra xem một sản phẩm trong đơn hàng đã được đánh giá chưa
     * @param orderDetailsID ID của chi tiết đơn hàng
     * @return true nếu đã đánh giá, false nếu chưa
     */
    public boolean isProductReviewed(String orderDetailsID) {
        String sql = "SELECT COUNT(*) FROM ProductReview WHERE orderDetailsID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, orderDetailsID);
            var resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }

            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}