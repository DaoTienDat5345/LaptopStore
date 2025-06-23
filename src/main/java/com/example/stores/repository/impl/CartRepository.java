package com.example.stores.repository.impl;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CartRepository {

    // Lấy thông tin giỏ hàng của khách hàng
    public Cart findCartByCustomerId(int customerId) {
        String query = "SELECT c.cartID, c.createdAt FROM Cart c " +
                "WHERE c.customerID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int cartId = rs.getInt("cartID");
                LocalDateTime createdAt = rs.getTimestamp("createdAt").toLocalDateTime();
                return new Cart(cartId, customerId, createdAt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Tạo giỏ hàng mới cho khách hàng
    public Cart createCart(int customerId) {
        String insertQuery = "INSERT INTO Cart (customerID, createdAt) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, customerId);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int cartId = rs.getInt(1);
                    return new Cart(cartId, customerId, LocalDateTime.now());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Lấy danh sách sản phẩm trong giỏ hàng
    public List<CartItem> getCartItems(int cartId) {
        List<CartItem> items = new ArrayList<>();
        String query = "SELECT ci.cartItemID, ci.quantity, ci.addedAt, " +
                "p.productID, p.productName, p.price " +
                "FROM CartItem ci " +
                "JOIN Products p ON ci.productID = p.productID " +
                "WHERE ci.cartID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, cartId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Product product = new Product();
                product.setProductID(rs.getString("productID"));
                product.setProductName(rs.getString("productName"));
                product.setPrice(rs.getDouble("price"));

                int cartItemId = rs.getInt("cartItemID");
                int quantity = rs.getInt("quantity");
                LocalDateTime addedAt = rs.getTimestamp("addedAt").toLocalDateTime();

                CartItem item = new CartItem(cartItemId, cartId, product, quantity, addedAt, true);
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    // Xóa sản phẩm từ giỏ hàng
    public boolean removeCartItem(int cartItemId) {
        String query = "DELETE FROM CartItem WHERE cartItemID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, cartItemId);
            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    public boolean updateCartItemQuantity(int cartItemId, int quantity) {
        String query = "UPDATE CartItem SET quantity = ? WHERE cartItemID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, quantity);
            pstmt.setInt(2, cartItemId);
            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Xóa tất cả sản phẩm trong giỏ hàng
    public boolean clearCart(int cartId) {
        String query = "DELETE FROM CartItem WHERE cartID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, cartId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Thêm sản phẩm vào giỏ hàng
    public boolean addToCart(int cartId, String productId, int quantity) {
        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
        String checkQuery = "SELECT cartItemID, quantity FROM CartItem WHERE cartID = ? AND productID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setInt(1, cartId);
            checkStmt.setString(2, productId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Cập nhật số lượng nếu sản phẩm đã tồn tại trong giỏ hàng
                int cartItemId = rs.getInt("cartItemID");
                int currentQuantity = rs.getInt("quantity");
                int newQuantity = currentQuantity + quantity;

                return updateCartItemQuantity(cartItemId, newQuantity);
            } else {
                // Thêm sản phẩm mới vào giỏ hàng
                String insertQuery = "INSERT INTO CartItem (cartID, productID, quantity, addedAt) VALUES (?, ?, ?, ?)";

                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, cartId);
                    insertStmt.setString(2, productId);
                    insertStmt.setInt(3, quantity);
                    insertStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

                    int rowsAffected = insertStmt.executeUpdate();
                    return rowsAffected > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    // Thêm phương thức mới để lấy thông tin CartItem theo ID
public CartItem getCartItemById(int cartItemId) {
    String query = "SELECT ci.cartItemID, ci.cartID, ci.quantity, ci.addedAt, " +
                   "p.productID, p.productName, p.price " +
                   "FROM CartItem ci " +
                   "JOIN Products p ON ci.productID = p.productID " +
                   "WHERE ci.cartItemID = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setInt(1, cartItemId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            Product product = new Product();
            product.setProductID(rs.getString("productID"));
            product.setProductName(rs.getString("productName"));
            product.setPrice(rs.getDouble("price"));

            int cartId = rs.getInt("cartID");
            int quantity = rs.getInt("quantity");
            LocalDateTime addedAt = rs.getTimestamp("addedAt").toLocalDateTime();

            return new CartItem(cartItemId, cartId, product, quantity, addedAt, true);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return null;
}

// Thêm phương thức mới để tìm CartItem theo productId
public CartItem findCartItemByProductId(int cartId, String productId) {
    String query = "SELECT ci.cartItemID, ci.quantity, ci.addedAt, " +
                   "p.productName, p.price " +
                   "FROM CartItem ci " +
                   "JOIN Products p ON ci.productID = p.productID " +
                   "WHERE ci.cartID = ? AND ci.productID = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setInt(1, cartId);
        pstmt.setString(2, productId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            Product product = new Product();
            product.setProductID(productId);
            product.setProductName(rs.getString("productName"));
            product.setPrice(rs.getDouble("price"));

            int cartItemId = rs.getInt("cartItemID");
            int quantity = rs.getInt("quantity");
            LocalDateTime addedAt = rs.getTimestamp("addedAt").toLocalDateTime();

            return new CartItem(cartItemId, cartId, product, quantity, addedAt, true);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return null;
}
}