package com.example.stores.repository.impl;

import com.example.stores.config.DatabaseConnection;
import com.example.stores.model.Product;
import com.example.stores.repository.ProductRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepositoryImpl implements ProductRepository {

    private Product mapRowToProductWithCategory(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductID(rs.getString("productID"));
        product.setProductName(rs.getString("productName"));
        product.setCategoryID(rs.getString("categoryID"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getDouble("price"));           // << SỬA
        product.setPriceCost(rs.getDouble("priceCost"));
        product.setImagePath(rs.getString("imagePath"));
        product.setQuantity(rs.getInt("quantity"));
        product.setStatus(rs.getString("status")); // Lấy từ cột tính toán
        // product.setWarrantyMonths(rs.getInt("warrantyMonths")); // ĐÃ BỎ
        Timestamp createdAtDB = rs.getTimestamp("createdAt");
        if (createdAtDB != null) {
            product.setCreatedAt(createdAtDB.toLocalDateTime());
        }
        // Lấy categoryName từ JOIN
        if (hasColumn(rs, "categoryName")) {
            product.setCategoryNameDisplay(rs.getString("categoryName"));
        }
        return product;
    }

    // Tiện ích kiểm tra cột tồn tại trong ResultSet
    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equalsIgnoreCase(rsmd.getColumnName(x))) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean save(Product product) {
        // productID được sinh bởi trigger, không cần truyền vào đây
        // warrantyMonths đã bị bỏ
        String sql = "INSERT INTO Products (productName, categoryID, description, price, priceCost, imagePath, quantity, createdAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getCategoryID());
            pstmt.setString(3, product.getDescription());
            pstmt.setDouble(4, product.getPrice());       // << SỬA
            pstmt.setDouble(5, product.getPriceCost());
            pstmt.setString(6, product.getImagePath());
            pstmt.setInt(7, product.getQuantity());
            pstmt.setTimestamp(8, product.getCreatedAt() != null ? Timestamp.valueOf(product.getCreatedAt()) : Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            // Vì productID sinh bởi trigger INSTEAD OF, không thể dùng getGeneratedKeys() dễ dàng.
            // Nếu cần ID ngay, service sẽ phải query lại hoặc trigger được thiết kế để OUTPUT ID.
        } catch (SQLException e) {
            System.err.println("SQL Error in ProductRepositoryImpl.save: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Product product) {
        // Không cập nhật productID, createdAt
        String sql = "UPDATE Products SET productName = ?, categoryID = ?, description = ?, price = ?, " +
                "priceCost = ?, imagePath = ?, quantity = ? " + // Bỏ warrantyMonths
                "WHERE productID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getCategoryID());
            pstmt.setString(3, product.getDescription());
            pstmt.setDouble(4, product.getPrice());       // << SỬA
            pstmt.setDouble(5, product.getPriceCost());   // <
            pstmt.setString(6, product.getImagePath());
            pstmt.setInt(7, product.getQuantity());
            // pstmt.setInt(8, product.getWarrantyMonths()); // ĐÃ BỎ
            pstmt.setString(8, product.getProductID()); // Index sẽ là 8 nếu warrantyMonths bị bỏ

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in ProductRepositoryImpl.update: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteById(String productId) {
        // Service sẽ kiểm tra ràng buộc trước
        String sql = "DELETE FROM Products WHERE productID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in ProductRepositoryImpl.deleteById: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Optional<Product> findById(String productId) {
        String sql = "SELECT p.*, c.categoryName FROM Products p " +
                "JOIN Categories c ON p.categoryID = c.categoryID " +
                "WHERE p.productID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToProductWithCategory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAllWithCategoryName() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.categoryName FROM Products p " +
                "JOIN Categories c ON p.categoryID = c.categoryID " +
                "ORDER BY p.productName";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapRowToProductWithCategory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> findByCategoryIdWithCategoryName(String categoryId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.categoryName FROM Products p " +
                "JOIN Categories c ON p.categoryID = c.categoryID " +
                "WHERE p.categoryID = ? ORDER BY p.productName";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                products.add(mapRowToProductWithCategory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> searchProductsWithCategoryName(String keyword) {
        List<Product> products = new ArrayList<>();
        String searchPattern = "%" + keyword.toLowerCase() + "%";
        String sql = "SELECT p.*, c.categoryName FROM Products p " +
                "JOIN Categories c ON p.categoryID = c.categoryID " +
                "WHERE LOWER(p.productName) LIKE ? OR LOWER(p.description) LIKE ? OR LOWER(c.categoryName) LIKE ? " +
                "ORDER BY p.productName";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                products.add(mapRowToProductWithCategory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public long countByCategoryId(String categoryId) {
        String sql = "SELECT COUNT(*) FROM Products WHERE categoryID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public long countReferencesInOrdersOrInventory(String productId) {
        long count = 0;
        String sqlOrderDetails = "SELECT COUNT(*) FROM OrderDetails WHERE productID = ?";
        String sqlInventory = "SELECT COUNT(*) FROM Inventory WHERE productID = ?";
        // Bạn cũng có thể cần kiểm tra trong Cart nếu cấu trúc Cart của bạn lưu productID trực tiếp

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Kiểm tra trong OrderDetails
            try (PreparedStatement pstmt = conn.prepareStatement(sqlOrderDetails)) {
                pstmt.setString(1, productId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    count += rs.getLong(1);
                }
            }
            // Kiểm tra trong Inventory
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInventory)) {
                pstmt.setString(1, productId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    count += rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
}