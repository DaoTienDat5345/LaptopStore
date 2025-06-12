package com.example.stores.repository.impl;

import com.example.stores.config.DatabaseConnection;
import com.example.stores.model.ProductReview;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class cho việc truy cập dữ liệu đánh giá sản phẩm
 */
public class ReviewRepository {

    /**
     * Lấy danh sách đánh giá sản phẩm theo ID sản phẩm
     */
    public List<ProductReview> findByProductId(String productId) {
        List<ProductReview> reviews = new ArrayList<>();
        String query = "SELECT PR.*, C.fullName FROM ProductReview PR " +
                "JOIN Customer C ON PR.customerID = C.customerID " +
                "WHERE PR.productID = ?" +
                "ORDER BY PR.reviewDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, productId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ProductReview review = mapResultSetToReview(rs);
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.err.println("Error getting reviews: " + e.getMessage());
            e.printStackTrace();
        }

        return reviews;
    }

    /**
     * Thêm một đánh giá mới cho sản phẩm
     */
    public boolean save(ProductReview review) {
        String query = "INSERT INTO ProductReview (productID, customerID, orderDetailsID, rating, comment, reviewDate, isApproved) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, review.getProductID());
            pstmt.setInt(2, review.getCustomerID());
            pstmt.setString(3, review.getOrderDetailsID());
            pstmt.setInt(4, review.getRating());
            pstmt.setString(5, review.getComment());

            // Nếu không có ngày đánh giá, sử dụng thời gian hiện tại
            LocalDateTime reviewDate = review.getReviewDate();
            if (reviewDate == null) {
                reviewDate = LocalDateTime.now();
            }
            pstmt.setTimestamp(6, java.sql.Timestamp.valueOf(reviewDate));

            pstmt.setBoolean(7, review.isApproved());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding review: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy điểm đánh giá trung bình của sản phẩm
     */
    public double getAverageRating(String productId) {
        String query = "SELECT AVG(rating) AS avgRating FROM ProductReview " +
                "WHERE productID = ? AND isApproved = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, productId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("avgRating");
            }
        } catch (SQLException e) {
            System.err.println("Error getting average rating: " + e.getMessage());
            e.printStackTrace();
        }

        return 0.0;
    }

    /**
     * Đếm số lượng đánh giá của sản phẩm
     */
    public int countByProductId(String productId) {
        String query = "SELECT COUNT(*) AS reviewCount FROM ProductReview " +
                "WHERE productID = ? AND isApproved = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, productId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("reviewCount");
            }
        } catch (SQLException e) {
            System.err.println("Error getting review count: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Helper method để mapping từ ResultSet sang đối tượng ProductReview
     */
    private ProductReview mapResultSetToReview(ResultSet rs) throws SQLException {
        ProductReview review = new ProductReview();
        review.setReviewID(rs.getInt("reviewID"));
        review.setProductID(rs.getString("productID"));
        review.setCustomerID(rs.getInt("customerID"));
        review.setOrderDetailsID(rs.getString("orderDetailsID"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));

        // Lấy timestamp từ cơ sở dữ liệu và chuyển đổi thành LocalDateTime
        java.sql.Timestamp reviewTimestamp = rs.getTimestamp("reviewDate");
        if (reviewTimestamp != null) {
            review.setReviewDate(reviewTimestamp.toLocalDateTime());
        }

        review.setApproved(rs.getBoolean("isApproved"));
        review.setCustomerName(rs.getString("fullName"));

        return review;
    }
}