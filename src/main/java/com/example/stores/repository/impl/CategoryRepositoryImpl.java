package com.example.stores.repository.impl;

import com.example.stores.config.DatabaseConnection;
import com.example.stores.model.Category;
import com.example.stores.repository.CategoryRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryRepositoryImpl implements CategoryRepository {

    private Category mapRowToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryID(rs.getString("categoryID"));
        category.setCategoryCode(rs.getString("categoryCode"));
        category.setCategoryName(rs.getString("categoryName"));
        category.setDescription(rs.getString("description"));
        category.setDefaultWarrantyGroup(rs.getString("defaultWarrantyGroup"));
        return category;
    }

    @Override
    public Category save(Category category) {
        // Thêm defaultWarrantyGroup vào câu SQL và PreparedStatement
        String sql = "INSERT INTO Categories (categoryID, categoryCode, categoryName, description, defaultWarrantyGroup) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getCategoryID());
            pstmt.setString(2, category.getCategoryCode());
            pstmt.setString(3, category.getCategoryName());
            pstmt.setString(4, category.getDescription());
            pstmt.setString(5, category.getDefaultWarrantyGroup()); // << THÊM DÒNG NÀY
            // ... (phần còn lại của save)
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                return category;
            }
        } catch (SQLException e) { System.err.println("SQL Error in CategoryRepositoryImpl.save: " + e.getMessage());
            e.printStackTrace(); e.printStackTrace(); }

        return null;
    }

    @Override
    public boolean update(Category category) {
        // Thêm defaultWarrantyGroup vào câu SQL và PreparedStatement
        String sql = "UPDATE Categories SET categoryCode = ?, categoryName = ?, description = ?, defaultWarrantyGroup = ? WHERE categoryID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getCategoryCode());
            pstmt.setString(2, category.getCategoryName());
            pstmt.setString(3, category.getDescription());
            pstmt.setString(4, category.getDefaultWarrantyGroup()); // << THÊM DÒNG NÀY
            pstmt.setString(5, category.getCategoryID()); // Index thay đổi
            // ... (phần còn lại của update)
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) { System.err.println("SQL Error in CategoryRepositoryImpl.save: " + e.getMessage());
            e.printStackTrace(); e.printStackTrace();}

        return false;
    }

    @Override
    public boolean deleteById(String categoryId) {
        // Service sẽ kiểm tra ràng buộc trước khi gọi hàm này
        String sql = "DELETE FROM Categories WHERE categoryID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in CategoryRepositoryImpl.deleteById: " + e.getMessage());
            e.printStackTrace(); // Có thể do ràng buộc khóa ngoại với Products
        }
        return false;
    }

    @Override
    public Optional<Category> findById(String categoryId) {
        String sql = "SELECT * FROM Categories WHERE categoryID = ?";
        Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, categoryId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToCategory(rs)); // mapRowToCategory đã được cập nhật
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { closeResources(rs, pstmt, null); }
        return Optional.empty();
    }

    @Override
    public Optional<Category> findByCategoryCode(String categoryCode) {
        String sql = "SELECT * FROM Categories WHERE categoryCode = ?";
        Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, categoryCode);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToCategory(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { closeResources(rs, pstmt, null); }
        return Optional.empty();
    }

    @Override
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM Categories ORDER BY categoryName";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(mapRowToCategory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
    private void closeResources(ResultSet rs, Statement stmt, Connection connOptionalToClose) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}