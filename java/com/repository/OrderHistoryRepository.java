package com.repository;

import com.database.QLdatabase;
import com.model.Order;
import com.model.OrderDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderHistoryRepository {

    public List<Object[]> getOrderHistory(int customerID) throws SQLException {
        List<Object[]> orders = new ArrayList<>();
        try (Connection con = QLdatabase.getConnection()) {
            if (con == null) throw new SQLException("Không thể kết nối đến com.database!");

            String sql = "SELECT orderID, orderDate, TotalAmount FROM Orders WHERE customerID = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, customerID);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Object[] row = new Object[3];
                        row[0] = rs.getString("orderID");
                        row[1] = rs.getDate("orderDate");
                        row[2] = rs.getDouble("TotalAmount");
                        orders.add(row);
                    }
                }
            }
        }
        return orders;
    }

    public Order getOrderById(String orderID) throws SQLException {
        try (Connection con = QLdatabase.getConnection()) {
            if (con == null) throw new SQLException("Không thể kết nối đến com.database!");

            String sql = "SELECT orderID, orderDate, TotalAmount, customerID FROM Orders WHERE orderID = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, orderID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Order(
                            rs.getString("orderID"),
                            rs.getDate("orderDate").toLocalDate(),
                            rs.getDouble("TotalAmount"),
                            rs.getInt("customerID")
                        );
                    }
                }
            }
        }
        return null;
    }

    public List<OrderDetail> getOrderDetails(String orderID) throws SQLException {
        List<OrderDetail> details = new ArrayList<>();
        try (Connection con = QLdatabase.getConnection()) {
            if (con == null) throw new SQLException("Không thể kết nối đến com.database!");

            String sql = "SELECT * FROM OrderDetails WHERE orderID = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, orderID);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        OrderDetail detail = new OrderDetail(
                            rs.getString("orderDetailsID"),
                            rs.getString("orderID"),
                            rs.getString("productID"),
                            rs.getInt("Quantity"),
                            rs.getDouble("Subtotal"),
                            rs.getString("warrantyType"),
                            rs.getDouble("warrantyPrice"),
                            rs.getDate("warrantyStartDate").toLocalDate(),
                            rs.getDate("warrantyEndDate").toLocalDate(),
                            rs.getString("note")
                        );
                        details.add(detail);
                    }
                }
            }
        }
        return details;
    }
}