package com.example.stores.repository.impl;

import com.example.stores.config.DatabaseConnection;
import com.example.stores.model.OrderDetail;
import com.example.stores.repository.OrderDetailRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailRepositoryImpl implements OrderDetailRepository {

    private OrderDetail mapRowToOrderDetail(ResultSet rs) throws SQLException {
        OrderDetail detail = new OrderDetail();
        detail.setOrderDetailsID(rs.getString("orderDetailsID"));
        detail.setOrderID(rs.getString("orderID"));
        detail.setProductID(rs.getString("productID"));
        detail.setQuantity(rs.getInt("quantity"));
        detail.setUnitPrice(rs.getBigDecimal("unitPrice"));
        detail.setSubtotal(rs.getBigDecimal("subtotal"));
        detail.setWarrantyType(rs.getString("warrantyType"));
        detail.setWarrantyPrice(rs.getBigDecimal("warrantyPrice"));
        detail.setNote(rs.getString("note"));

        if (hasColumn(rs, "productName")) {
            detail.setProductNameDisplay(rs.getString("productName"));
        }
        return detail;
    }

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
    public List<OrderDetail> findByOrderIdWithProductDetails(String orderId) {
        List<OrderDetail> details = new ArrayList<>();
        // subtotal là cột tính toán, có thể SELECT nó
        String sql = "SELECT od.*, p.productName " +
                "FROM OrderDetails od " +
                "JOIN Products p ON od.productID = p.productID " +
                "WHERE od.orderID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, orderId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                details.add(mapRowToOrderDetail(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null);
        }
        return details;
    }

    @Override
    public OrderDetail save(OrderDetail detail, Connection conn) throws SQLException {
        String sql = "INSERT INTO OrderDetails (orderID, productID, quantity, unitPrice, warrantyType, warrantyPrice, note) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, detail.getOrderID());
            pstmt.setString(2, detail.getProductID());
            pstmt.setInt(3, detail.getQuantity());
            pstmt.setBigDecimal(4, detail.getUnitPrice());
            pstmt.setString(5, detail.getWarrantyType());
            pstmt.setBigDecimal(6, detail.getWarrantyPrice());
            pstmt.setString(7, detail.getNote());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {

                return detail;
            }
        } finally {
            // Đóng PreparedStatement trong saveDetail
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return null; // Hoặc ném SQLException nếu insert thất bại
    }

    @Override
    public long countByProductId(String productId) {
        String sql = "SELECT COUNT(*) FROM OrderDetails WHERE productID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null);
        }
        return 0;
    }


    // Helper method để đóng resources
    private void closeResources(ResultSet rs, Statement stmt, Connection connOptionalToClose) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
    }

}