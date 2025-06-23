package com.service;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import com.database.QLdatabase;
import com.model.Order;
import com.model.OrderDetail;
import view.ViewOrderDetails;

public class OrderService {
    public void loadOrderDetails(DefaultTableModel tableModel, List<OrderDetail> orderDetails, Order order, DefaultTableModel cartModel) {
        if (cartModel != null) {
            tableModel.setRowCount(0);
            orderDetails.clear();
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                Boolean isSelected = (Boolean) cartModel.getValueAt(i, 0);
                if (isSelected != null && isSelected) {
                    String productName = (String) cartModel.getValueAt(i, 1);
                    int quantity = (Integer) cartModel.getValueAt(i, 3);
                    double price = (Double) cartModel.getValueAt(i, 2);
                    double subtotal = price * quantity;

                    Vector<Object> row = new Vector<>();
                    row.add(productName);
                    row.add(quantity);
                    row.add(subtotal);
                    row.add("Thường");
                    tableModel.addRow(row);

                    Connection con = QLdatabase.getConnection();
                    String sql = "SELECT productID FROM Products WHERE productName = ?";
                    try {
                        PreparedStatement ps = con.prepareStatement(sql);
                        ps.setString(1, productName);
                        ResultSet rs = ps.executeQuery();
                        String productID = "";
                        if (rs.next()) {
                            productID = rs.getString("productID");
                        }
                        rs.close();
                        ps.close();
                        con.close();

                        String warrantyType = "Thường";
                        double warrantyPrice = 0;
                        LocalDate warrantyStartDate = LocalDate.now();
                        LocalDate warrantyEndDate = warrantyType.equals("Vàng") ? warrantyStartDate.plusYears(1) : warrantyStartDate.plusMonths(6);
                        OrderDetail detail = new OrderDetail("", order.getOrderID(), productID, quantity, subtotal,
                                warrantyType, warrantyPrice, warrantyStartDate, warrantyEndDate, "");
                        orderDetails.add(detail);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            tableModel.setRowCount(0);
            for (OrderDetail detail : orderDetails) {
                Vector<Object> row = new Vector<>();
                String productName = getProductNameFromID(detail.getProductID());
                double pricePerUnit = (detail.getSubtotal() - detail.getWarrantyPrice()) / detail.getQuantity();
                row.add(productName);
                row.add(detail.getQuantity());
                row.add(pricePerUnit);
                row.add(detail.getWarrantyType());
                row.add(detail.getWarrantyStartDate().toString());
                row.add(detail.getWarrantyEndDate().toString());
                tableModel.addRow(row);
            }
        }
    }

    public int getAvailableQuantity(String productName) {
        Connection con = QLdatabase.getConnection();
        int quantity = 0;
        try {
            String sql = "SELECT Quantity FROM Products WHERE productName = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, productName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                quantity = rs.getInt("Quantity");
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return quantity;
    }

    public void updateSubtotal(int row, DefaultTableModel tableModel, List<OrderDetail> orderDetails) {
        String productName = (String) tableModel.getValueAt(row, 0);
        Object quantityObj = tableModel.getValueAt(row, 1);
        int quantity = (quantityObj instanceof String) ? Integer.parseInt((String) quantityObj) : (Integer) quantityObj;
        Connection con = QLdatabase.getConnection();
        try {
            String sql = "SELECT price FROM Products WHERE productName = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, productName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double price = rs.getDouble("price");
                double subtotal = price * quantity;
                OrderDetail detail = orderDetails.get(row);
                double warrantyPrice = detail.getWarrantyPrice();
                detail.setQuantity(quantity);
                detail.setSubtotal(subtotal + warrantyPrice);
                tableModel.setValueAt(subtotal + warrantyPrice, row, 2);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTotalAmount(DefaultTableModel tableModel, List<OrderDetail> orderDetails, Order order) {
        double totalAmount = 0;
        for (OrderDetail detail : orderDetails) {
            totalAmount += detail.getSubtotal();
        }
        order.setTotalAmount(totalAmount);
    }

    public void updateOrderDetailQuantity(int row, int newQuantity, DefaultTableModel tableModel, List<OrderDetail> orderDetails) {
        if (row >= 0 && row < orderDetails.size()) {
            OrderDetail detail = orderDetails.get(row);
            detail.setQuantity(newQuantity);
            String productName = (String) tableModel.getValueAt(row, 0);
            Connection con = QLdatabase.getConnection();
            try {
                String sql = "SELECT price FROM Products WHERE productName = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, productName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double price = rs.getDouble("price");
                    double baseSubtotal = price * newQuantity;
                    double warrantyPrice = detail.getWarrantyPrice();
                    detail.setSubtotal(baseSubtotal + warrantyPrice);
                    tableModel.setValueAt(baseSubtotal + warrantyPrice, row, 2);
                }
                rs.close();
                ps.close();
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateOrderDetailWarranty(int row, String warrantyType, DefaultTableModel tableModel, List<OrderDetail> orderDetails) {
        if (row >= 0 && row < orderDetails.size()) {
            OrderDetail detail = orderDetails.get(row);
            detail.setWarrantyType(warrantyType);
            double warrantyPrice = warrantyType.equals("Vàng") ? 300000 : 0;
            detail.setWarrantyPrice(warrantyPrice);
            LocalDate warrantyStartDate = detail.getWarrantyStartDate();
            LocalDate warrantyEndDate = warrantyType.equals("Vàng") ? warrantyStartDate.plusYears(1) : warrantyStartDate.plusMonths(6);
            detail.setWarrantyEndDate(warrantyEndDate);

            String productName = (String) tableModel.getValueAt(row, 0);
            int quantity = (Integer) tableModel.getValueAt(row, 1);
            Connection con = QLdatabase.getConnection();
            try {
                String sql = "SELECT price FROM Products WHERE productName = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, productName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double price = rs.getDouble("price");
                    double baseSubtotal = price * quantity;
                    double newSubtotal = baseSubtotal + warrantyPrice;
                    detail.setSubtotal(newSubtotal);
                    tableModel.setValueAt(newSubtotal, row, 2);
                }
                rs.close();
                ps.close();
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void confirmOrder(Order order, List<OrderDetail> orderDetails, int currentCustomerID, DefaultTableModel tableModel, JFrame viewOrder, Runnable onSuccess) {
        Connection con = null;
        boolean success = false; // Biến để kiểm soát việc gọi onSuccess
        try {
            con = QLdatabase.getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(viewOrder, "Không thể kết nối đến com.database!");
                return;
            }

            con.setAutoCommit(false);

            // Kiểm tra số lượng tồn kho
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String productName = (String) tableModel.getValueAt(i, 0);
                Object quantityObj = tableModel.getValueAt(i, 1);
                int requestedQuantity = (quantityObj instanceof String) ? Integer.parseInt((String) quantityObj) : (Integer) quantityObj;
                int availableQuantity = getAvailableQuantity(productName);

                if (requestedQuantity > availableQuantity) {
                    JOptionPane.showMessageDialog(viewOrder, "Sản phẩm " + productName + " chỉ còn " + availableQuantity + " sản phẩm!");
                    con.rollback();
                    return;
                }
            }

            // Kiểm tra productID có tồn tại trong bảng Products không
            for (OrderDetail detail : orderDetails) {
                String sql = "SELECT productID FROM Products WHERE productID = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, detail.getProductID());
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("productID " + detail.getProductID() + " không tồn tại trong bảng Products!");
                }
                rs.close();
                ps.close();
            }

            // Cập nhật chi tiết đơn hàng và gán thông tin người nhận vào note
            String recipientInfo = "Tên: " + order.getRecipientName() + "; SĐT: " + order.getRecipientPhone() + "; Địa chỉ: " + order.getRecipientAddress();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String productName = (String) tableModel.getValueAt(i, 0);
                Object quantityObj = tableModel.getValueAt(i, 1);
                int requestedQuantity = (quantityObj instanceof String) ? Integer.parseInt((String) quantityObj) : (Integer) quantityObj;
                String warrantyType = (String) tableModel.getValueAt(i, 3);
                OrderDetail detail = orderDetails.get(i);
                detail.setQuantity(requestedQuantity);
                detail.setWarrantyType(warrantyType);
                double warrantyPrice = warrantyType.equals("Vàng") ? 300000 : 0;
                detail.setWarrantyPrice(warrantyPrice);
                LocalDate warrantyStartDate = LocalDate.now();
                LocalDate warrantyEndDate = warrantyType.equals("Vàng") ? warrantyStartDate.plusYears(1) : warrantyStartDate.plusMonths(6);
                detail.setWarrantyStartDate(warrantyStartDate);
                detail.setWarrantyEndDate(warrantyEndDate);
                detail.setNote(recipientInfo); // Lưu thông tin người nhận vào note

                String sql = "SELECT price FROM Products WHERE productName = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, productName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double price = rs.getDouble("price");
                    double subtotal = price * requestedQuantity + warrantyPrice;
                    detail.setSubtotal(subtotal);
                    tableModel.setValueAt(subtotal, i, 2);
                }
                rs.close();
                ps.close();
            }
            updateTotalAmount(tableModel, orderDetails, order);

            // Chèn vào Orders (trigger sẽ sinh orderID)
            String orderSql = "INSERT INTO Orders (orderDate, TotalAmount, customerID) VALUES (?, ?, ?)";
            PreparedStatement psOrder = con.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            psOrder.setObject(1, order.getOrderDate());
            psOrder.setDouble(2, order.getTotalAmount());
            psOrder.setInt(3, currentCustomerID);
            int rowsAffected = psOrder.executeUpdate();
            if (rowsAffected != 1) {
                throw new SQLException("Không thể chèn bản ghi vào bảng Orders!");
            }

            // Lấy orderID từ bảng Orders
            String generatedOrderID = null;
            String selectLastOrderSql = "SELECT TOP 1 orderID FROM Orders WHERE customerID = ? AND orderDate = ? ORDER BY orderID DESC";
            PreparedStatement psSelect = con.prepareStatement(selectLastOrderSql);
            psSelect.setInt(1, currentCustomerID);
            psSelect.setObject(2, order.getOrderDate());
            ResultSet rsSelect = psSelect.executeQuery();
            if (rsSelect.next()) {
                generatedOrderID = rsSelect.getString("orderID");
            }
            rsSelect.close();
            psSelect.close();
            psOrder.close();

            if (generatedOrderID == null) {
                throw new SQLException("Không thể lấy orderID từ Orders!");
            }

            // Gán orderID cho đối tượng Order và OrderDetails
            order.setOrderID(generatedOrderID);
            for (OrderDetail detail : orderDetails) {
                detail.setOrderID(generatedOrderID);
                // Đảm bảo orderDetailsID là duy nhất (nếu cần)
                if (detail.getOrderDetailsID() == null || detail.getOrderDetailsID().isEmpty()) {
                    detail.setOrderDetailsID("OD" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                }
            }

            // Chèn OrderDetails
            String detailSql = "INSERT INTO OrderDetails (orderDetailsID, orderID, productID, Quantity, Subtotal, warrantyType, warrantyPrice, warrantyStartDate, warrantyEndDate, note) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement psDetail = con.prepareStatement(detailSql);
            for (OrderDetail detail : orderDetails) {
                psDetail.setString(1, detail.getOrderDetailsID());
                psDetail.setString(2, detail.getOrderID());
                psDetail.setString(3, detail.getProductID());
                psDetail.setInt(4, detail.getQuantity());
                psDetail.setDouble(5, detail.getSubtotal());
                psDetail.setString(6, detail.getWarrantyType());
                psDetail.setDouble(7, detail.getWarrantyPrice());
                psDetail.setObject(8, detail.getWarrantyStartDate());
                psDetail.setObject(9, detail.getWarrantyEndDate());
                psDetail.setString(10, detail.getNote());
                psDetail.addBatch();
            }
            psDetail.executeBatch();
            psDetail.close();

            // Xóa Cart
            String deleteSql = "DELETE FROM Cart WHERE customerID = ? AND productID = ?";
            PreparedStatement psDelete = con.prepareStatement(deleteSql);
            for (OrderDetail detail : orderDetails) {
                psDelete.setInt(1, currentCustomerID);
                psDelete.setString(2, detail.getProductID());
                psDelete.addBatch();
            }
            psDelete.executeBatch();
            psDelete.close();

            // Cập nhật số lượng trong Products
            String updateQuantitySql = "UPDATE Products SET Quantity = Quantity - ? WHERE productID = ?";
            PreparedStatement psUpdate = con.prepareStatement(updateQuantitySql);
            for (OrderDetail detail : orderDetails) {
                psUpdate.setInt(1, detail.getQuantity());
                psUpdate.setString(2, detail.getProductID());
                psUpdate.addBatch();
            }
            psUpdate.executeBatch();
            psUpdate.close();

            con.commit();
            System.out.println("Transaction committed successfully. Generated OrderID: " + generatedOrderID);
            success = true; // Đánh dấu giao dịch thành công

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                    System.out.println("Rollback successful due to: " + e.getMessage());
                } catch (SQLException ex) {
                    System.out.println("Rollback failed: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            JOptionPane.showMessageDialog(viewOrder, "Lỗi khi tạo đơn hàng: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        // Chỉ gọi onSuccess nếu giao dịch thành công
        if (success && onSuccess != null) {
            onSuccess.run();
        }
    }

    public String getCustomerInfo(int customerID, String field) {
        Connection con = QLdatabase.getConnection();
        String result = "";
        try {
            String sql = "SELECT " + field + " FROM Customer WHERE customerID = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, customerID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getString(field);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getProductNameFromID(String productID) {
        Connection con = QLdatabase.getConnection();
        String name = "";
        try {
            String sql = "SELECT productName FROM Products WHERE productID = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, productID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                name = rs.getString("productName");
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }
}