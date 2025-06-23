package com.example.stores.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {
    
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM Categories";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Category category = new Category(
                        rs.getString("categoryID"),
                        rs.getString("categoryCode"),
                        rs.getString("categoryName"),
                        rs.getString("description"),
                        rs.getInt("defaultWarrantyGroup")
                );
                categories.add(category);
            }

        } catch (SQLException e) {
            System.err.println("Error loading categories: " + e.getMessage());
            e.printStackTrace();
        }

        return categories;
    }
    
    public Category getCategoryById(String categoryId) {
        String sql = "SELECT * FROM Categories WHERE categoryID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                            rs.getString("categoryID"),
                            rs.getString("categoryCode"),
                            rs.getString("categoryName"),
                            rs.getString("description"),
                            rs.getInt("defaultWarrantyGroup")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding category by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
    
    public boolean addCategory(Category category) {
        String sql = "INSERT INTO Categories (categoryID, categoryCode, categoryName, description, defaultWarrantyGroup) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getCategoryID());
            stmt.setString(2, category.getCategoryCode());
            stmt.setString(3, category.getCategoryName());
            stmt.setString(4, category.getDescription());
            stmt.setInt(5, category.getDefaultWarrantyGroup());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error adding category: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateCategory(Category category) {
        String sql = "UPDATE Categories SET categoryCode = ?, categoryName = ?, description = ?, defaultWarrantyGroup = ? WHERE categoryID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getCategoryCode());
            stmt.setString(2, category.getCategoryName());
            stmt.setString(3, category.getDescription());
            stmt.setInt(4, category.getDefaultWarrantyGroup());
            stmt.setString(5, category.getCategoryID());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating category: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteCategory(String categoryId) {
        String sql = "DELETE FROM Categories WHERE categoryID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoryId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}