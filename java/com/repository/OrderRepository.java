package com.repository;

import com.database.QLdatabase;
import com.model.Order;
import com.model.OrderDetail;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderRepository {

    public List<Object[]> getCartItems(DefaultTableModel cartModel) throws SQLException {
        List<Object[]> items = new ArrayList<>();
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            Boolean isSelected = (Boolean) cartModel.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                String productName = (String) cartModel.getValueAt(i, 1);
                double price = (Double) cartModel.getValueAt(i, 2);
                int quantity = (Integer) cartModel.getValueAt(i, 3);
                items.add(new Object[]{productName, quantity, price});
            }
        }
        return items;
    }

    public List<Object[]> getOrderItems(List<OrderDetail> orderDetails) throws SQLException {
        List<Object[]> items = new ArrayList<>();
        try (Connection con = QLdatabase.getConnection()) {
            if (con == null) throw new SQLException("Không thể kết nối đến com.database!");

            for (OrderDetail detail : orderDetails) {
                String sql = "SELECT p.productName, p.price FROM Products p WHERE p.productID = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, detail.getProductID());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            items.add(new Object[]{
                                rs.getString("productName"),
                                detail.getQuantity(),
                                rs.getDouble("price")
                            });
                        }
                    }
                }
            }
        }
        return items;
    }

    public double getProductPrice(String productName) throws SQLException {
        try (Connection con = QLdatabase.getConnection()) {
            if (con == null) throw new SQLException("Không thể kết nối đến com.database!");

            String sql = "SELECT price FROM Products WHERE productName = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, productName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("price");
                    }
                }
            }
        }
        throw new SQLException("Không tìm thấy sản phẩm: " + productName);
    }

    public boolean checkProductAvailability(String productName, int quantity) throws SQLException {
        try (Connection con = QLdatabase.getConnection()) {
            if (con == null) throw new SQLException("Không thể kết nối đến com.database!");

            String sql = "SELECT Quantity FROM Products WHERE productName = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, productName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int availableQuantity = rs.getInt("Quantity");
                        return availableQuantity >= quantity;
                    }
                }
            }
        }
        return false;
    }

    public void saveOrder(Order order, List<OrderDetail> orderDetails) throws SQLException {
        try (Connection con = QLdatabase.getConnection()) {
            if (con == null) throw new SQLException("Không thể kết nối đến com.database!");

            con.setAutoCommit(false);
            try {
                String orderID = "ORD" + UUID.randomUUID().toString().substring(0, 8);
                order.setOrderID(orderID);

                String sqlOrder = "INSERT INTO Orders (orderID, orderDate, TotalAmount, customerID) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psOrder = con.prepareStatement(sqlOrder)) {
                    psOrder.setString(1, orderID);
                    psOrder.setDate(2, java.sql.Date.valueOf(order.getOrderDate()));
                    psOrder.setDouble(3, order.getTotalAmount());
                    psOrder.setInt(4, order.getCustomerID());
                    psOrder.executeUpdate();
                }

                String sqlDetail = "INSERT INTO OrderDetails (orderDetailsID, orderID, productID, Quantity, Subtotal, warrantyType, warrantyPrice, warrantyStartDate, warrantyEndDate, note) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                for (OrderDetail detail : orderDetails) {
                    String detailID = "OD" + UUID.randomUUID().toString().substring(0, 8);
                    detail.setOrderDetailsID(detailID);
                    detail.setOrderID(orderID);

                    String productID = getProductIDByName(con, detail.getNote().split("Tên:")[0].trim());
                    detail.setProductID(productID);

                    try (PreparedStatement psDetail = con.prepareStatement(sqlDetail)) {
                        psDetail.setString(1, detailID);
                        psDetail.setString(2, orderID);
                        psDetail.setString(3, productID);
                        psDetail.setInt(4, detail.getQuantity());
                        psDetail.setDouble(5, detail.getSubtotal());
                        psDetail.setString(6, detail.getWarrantyType());
                        psDetail.setDouble(7, detail.getWarrantyPrice());
                        psDetail.setDate(8, java.sql.Date.valueOf(detail.getWarrantyStartDate()));
                        psDetail.setDate(9, java.sql.Date.valueOf(detail.getWarrantyEndDate()));
                        psDetail.setString(10, detail.getNote());
                        psDetail.executeUpdate();
                    }
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public void updateProductQuantities(List<OrderDetail> orderDetails) throws SQLException {
        try (Connection con = QLdatabase.getConnection()) {
            if (con == null) throw new SQLException("Không thể kết nối đến com.database!");

            String sql = "UPDATE Products SET Quantity = Quantity - ? WHERE productID = ?";
            for (OrderDetail detail : orderDetails) {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, detail.getQuantity());
                    ps.setString(2, detail.getProductID());
                    ps.executeUpdate();
                }
            }
        }
    }

    public void clearCart(int customerID, DefaultTableModel tableModel) throws SQLException {
        try (Connection con = QLdatabase.getConnection()) {
            if (con == null) throw new SQLException("Không thể kết nối đến com.database!");

            List<String> productNames = new ArrayList<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                productNames.add((String) tableModel.getValueAt(i, 0));
            }

            String sql = "DELETE FROM Cart WHERE customerID = ? AND productID IN (SELECT productID FROM Products WHERE productName = ?)";
            for (String productName : productNames) {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, customerID);
                    ps.setString(2, productName);
                    ps.executeUpdate();
                }
            }
        }
    }

    public String getCustomerInfo(int customerID, String field) throws SQLException {
        System.out.println("Attempting to fetch customer info for customerID: " + customerID + ", field: " + field);
        try (Connection con = QLdatabase.getConnection()) {
            if (con == null) {
                System.err.println("Failed to connect to com.database!");
                throw new SQLException("Không thể kết nối đến com.database!");
            }

            String sql = "SELECT fullName, phone, userAddress FROM Customer WHERE customerID = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, customerID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        switch (field) {
                            case "fullName":
                                return rs.getString("fullName");
                            case "phone":
                                return rs.getString("phone");
                            case "userAddress":
                                return rs.getString("userAddress");
                            default:
                                System.out.println("Invalid field requested: " + field);
                                return "";
                        }
                    } else {
                        System.out.println("No customer found for customerID: " + customerID);
                        return "";
                    }
                }
            } catch (SQLException e) {
                System.err.println("SQL Error: " + e.getMessage());
                throw e;
            }
        }
    }

    private String getProductIDByName(Connection con, String productName) throws SQLException {
        String sql = "SELECT productID FROM Products WHERE productName = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, productName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("productID");
                }
            }
        }
        throw new SQLException("Không tìm thấy sản phẩm: " + productName);
    }
}