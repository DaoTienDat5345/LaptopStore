package com.example.stores.repository.impl;

import com.example.stores.config.DatabaseConnection;
import com.example.stores.repository.ProductReviewRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap; // Cho Map
import java.util.List;
import java.util.Map;    // Cho Map
import java.util.Optional;

public class ProductReviewRepositoryImpl implements ProductReviewRepository {

    @Override
    public Optional<Integer> findFirstRatingByOrderId(String orderId) {
        // Câu lệnh này lấy rating từ ProductReview dựa trên orderId (thông qua OrderDetails)
        // Nó giả định rằng bạn muốn lấy rating của sản phẩm đầu tiên được tìm thấy trong đơn hàng đó.
        String sql = "SELECT TOP 1 pr.rating " +
                "FROM ProductReview pr " +
                "JOIN OrderDetails od ON pr.orderDetailsID = od.orderDetailsID " +
                "WHERE od.orderID = ?";
        Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, orderId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(rs.getInt("rating"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null);
        }
        return Optional.empty();
    }

    @Override
    public List<Map<String, Object>> getReviewRatingStatistics() {
        List<Map<String, Object>> stats = new ArrayList<>();
        String sql = "SELECT rating, COUNT(*) as ratingCount FROM ProductReview WHERE rating IS NOT NULL GROUP BY rating ORDER BY rating";
        Connection conn = null; Statement stmt = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("rating", rs.getInt("rating"));
                row.put("count", rs.getInt("ratingCount"));
                stats.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { closeResources(rs, stmt, null); }
        return stats;
    }

    private void closeResources(ResultSet rs, Statement stmt, Connection connOptionalToClose) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}