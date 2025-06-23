package com.repository;

import com.database.QLdatabase;
import com.model.Products; // Import com.model Products (assuming it exists and is correct)

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductManagerRepository {

    public List<Products> getAllProductsForManager() throws SQLException {
        List<Products> list = new ArrayList<>();
        // Lấy đủ các cột cần thiết cho quản lý
        String sql = "SELECT productID, productName, categoryID, description, price, priceCost, imagePath, quantity, status FROM Products";
        try (Connection conn = QLdatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (conn == null) {
                 throw new SQLException("Không thể kết nối đến com.database!");
            }

            while (rs.next()) {
                Products p = new Products(
                        rs.getString("productID"),
                        rs.getString("productName"),
                        rs.getString("categoryID"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getDouble("priceCost"),
                        rs.getString("imagePath"),
                        rs.getInt("quantity"),
                        rs.getString("status") // Lấy cả status
                );
                list.add(p);
            }
        } catch (SQLException e) {
             // Ném lại lỗi để tầng Service xử lý
            System.err.println("Lỗi khi tải danh sách sản phẩm cho Manager: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            // Bắt các lỗi khác (như ClassNotFoundException từ QLdatabase)
            System.err.println("Lỗi không xác định khi tải sản phẩm cho Manager: " + e.getMessage());
            throw new SQLException("Lỗi không xác định: " + e.getMessage(), e);
        }
        return list;
    }

    
    public boolean addProductManager(String id, String name, String categoryID, String description,
                                   double price, double priceCost, String imagePath, int quantity) throws SQLException {
        String sql = "INSERT INTO Products (productID, productName, categoryID, description, price, priceCost, imagePath, quantity) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = QLdatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

             if (conn == null) {
                 throw new SQLException("Không thể kết nối đến com.database!");
            }

            ps.setString(1, id);
            ps.setString(2, name);
            ps.setString(3, categoryID);
            ps.setString(4, description);
            ps.setDouble(5, price);
            ps.setDouble(6, priceCost);
            ps.setString(7, imagePath);
            ps.setInt(8, quantity);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // JOptionPane.showMessageDialog(null, "Lỗi khi thêm sản phẩm."); // Tầng Service xử lý
            System.err.println("Lỗi SQL khi thêm sản phẩm (Manager): " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi thêm sản phẩm (Manager): " + e.getMessage());
            throw new SQLException("Lỗi không xác định: " + e.getMessage(), e);
        }
    }

    
    public boolean updateProductManager(String productID, String name, String categoryID, String description,
                                      double price, double priceCost, String imagePath, int quantity) throws SQLException {
        String sql = "UPDATE Products SET productName = ?, categoryID = ?, description = ?, price = ?, priceCost = ?, imagePath = ?, quantity = ? WHERE productID = ?";
        try (Connection conn = QLdatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

             if (conn == null) {
                 throw new SQLException("Không thể kết nối đến com.database!");
            }

            ps.setString(1, name);
            ps.setString(2, categoryID);
            ps.setString(3, description);
            ps.setDouble(4, price);
            ps.setDouble(5, priceCost);
            ps.setString(6, imagePath);
            ps.setInt(7, quantity);
            ps.setString(8, productID);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi cập nhật sản phẩm (Manager): " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi cập nhật sản phẩm (Manager): " + e.getMessage());
            throw new SQLException("Lỗi không xác định: " + e.getMessage(), e);
        }
    }

    
    public boolean deleteProductManager(String productID) throws SQLException {
        // Nên kiểm tra ràng buộc khóa ngoại ở tầng Service nếu cần
        String sql = "DELETE FROM Products WHERE productID = ?";
        try (Connection conn = QLdatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

             if (conn == null) {
                 throw new SQLException("Không thể kết nối đến com.database!");
            }

            ps.setString(1, productID);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
             // JOptionPane.showMessageDialog(null, "Lỗi khi xóa sản phẩm: " + e.getMessage()); // Tầng Service xử lý
            System.err.println("Lỗi SQL khi xóa sản phẩm (Manager): " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi xóa sản phẩm (Manager): " + e.getMessage());
            throw new SQLException("Lỗi không xác định: " + e.getMessage(), e);
        }
    }
}