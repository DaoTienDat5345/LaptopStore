package com.repository;

import com.database.QLdatabase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    public List<Object[]> getProducts(String order, String categoryID) throws SQLException {
        List<Object[]> products = new ArrayList<>();
        try (Connection con = QLdatabase.getConnection()) {
            if (con == null) throw new SQLException("Không thể kết nối đến com.database!");

            String sql = "SELECT productName, price, imagePath FROM Products WHERE 1=1"; 
            if (!categoryID.isEmpty()) {
                sql += " AND categoryID = ?";
            }
            if (!order.isEmpty()) {
                sql += " " + order;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                if (!categoryID.isEmpty()) {
                    ps.setString(1, categoryID);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Object[] row = new Object[3];
                        row[0] = rs.getString("productName");
                        row[1] = rs.getDouble("price");
                        row[2] = rs.getString("imagePath"); 
                        products.add(row);
                    }
                }
            } catch (SQLException e) {
                throw new SQLException("Lỗi khi truy vấn sản phẩm: " + e.getMessage());
            }
        }
        return products;
    }

    public List<Object[]> searchProducts(String keyword) throws SQLException {
        List<Object[]> products = new ArrayList<>();
        try (Connection con = QLdatabase.getConnection()) {
            if (con == null) throw new SQLException("Không thể kết nối đến com.database!");

            String sql = "SELECT productName, price, imagePath FROM Products WHERE productName LIKE ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, "%" + keyword + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Object[] row = new Object[3];
                        row[0] = rs.getString("productName");
                        row[1] = rs.getDouble("price");
                        row[2] = rs.getString("imagePath");
                        products.add(row);
                    }
                }
            } catch (SQLException e) {
                throw new SQLException("Lỗi khi tìm kiếm sản phẩm: " + e.getMessage());
            }
        }
        return products;
    }

    public String getProductDescription(String productName) throws SQLException {
        try (Connection con = QLdatabase.getConnection()) {
            if (con == null) throw new SQLException("Không thể kết nối đến com.database!");

            String sql = "SELECT description FROM Products WHERE productName = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, productName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("description");
                    }
                }
            }
        }
        return null;
    }

    public int getProductQuantity(String productName) throws SQLException {
        try (Connection con = QLdatabase.getConnection()) {
            if (con == null) throw new SQLException("Không thể kết nối đến com.database!");

            String sql = "SELECT Quantity FROM Products WHERE productName = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, productName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("Quantity");
                    }
                }
            }
        }
        throw new SQLException("Không tìm thấy sản phẩm: " + productName);
    }
}