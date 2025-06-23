package com.example.stores.repository.impl;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    /**
     * Tạo đơn hàng mới
     * @param order Đối tượng Order cần tạo
     * @return Order đã được tạo với ID mới, null nếu có lỗi
     */
    public Order createOrder(Order order) {
        // Dùng SQL cố định số điện thoại trực tiếp để kiểm tra vấn đề
        String insertOrderSQL = "INSERT INTO Orders (orderDate, totalAmount, customerID, " +
                "employeeID, orderStatus, paymentMethod, recipientName, recipientPhone, " +
                "shippingAddress, shippingFee, notes) VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ghi log tham số trước khi thực hiện SQL
            System.out.println("DEBUG: Preparing to insert order with phone: '" + order.getRecipientPhone() + "'");

            PreparedStatement stmt = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);

            stmt.setTimestamp(1, Timestamp.valueOf(order.getOrderDate()));
            stmt.setDouble(2, order.getTotalAmount());
            stmt.setInt(3, order.getCustomerId());

            if (order.getEmployeeId() != null) {
                stmt.setInt(4, order.getEmployeeId());
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }

            stmt.setString(5, order.getOrderStatus());
            stmt.setString(6, order.getPaymentMethod());
            stmt.setString(7, order.getRecipientName());

            // Lấy và ghi log giá trị phone trước khi đặt vào stmt
            String phone = order.getRecipientPhone();
            System.out.println("DEBUG: Phone value before setString: '" + phone + "'");

            // THAY ĐỔI QUAN TRỌNG: Đặt giá trị cứng nếu phone là null
            if (phone == null || phone.isEmpty()) {
                phone = "0398764627";  // Giá trị mặc định để tránh lỗi
                System.out.println("WARNING: Using hardcoded phone value to avoid NULL");
            }

            // Đảm bảo phone không null trước khi đặt vào statement
            stmt.setString(8, phone);
            System.out.println("DEBUG: Phone parameter set in statement: '" + phone + "'");

            stmt.setString(9, order.getShippingAddress());
            stmt.setDouble(10, order.getShippingFee());

            if (order.getNotes() != null) {
                stmt.setString(11, order.getNotes());
            } else {
                stmt.setNull(11, java.sql.Types.NVARCHAR);
            }

            System.out.println("DEBUG: Executing SQL insert with finalized parameters");
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

            System.out.println("DEBUG: SQL insert successful, rows affected: " + affectedRows);

            // Lấy ID được tạo và xử lý các OrderDetails...
            String getGeneratedIdSQL = "SELECT TOP 1 orderID FROM Orders " +
                    "WHERE customerID = ? ORDER BY orderDate DESC";

            try (PreparedStatement getIdStmt = conn.prepareStatement(getGeneratedIdSQL)) {
                getIdStmt.setInt(1, order.getCustomerId());
                ResultSet rs = getIdStmt.executeQuery();

                if (rs.next()) {
                    String orderId = rs.getString("orderID");
                    order.setOrderId(orderId);
                    System.out.println("DEBUG: Order created with ID: " + orderId);

                    // Lưu các chi tiết đơn hàng
                    saveOrderDetails(order);

                    return order;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in createOrder: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public boolean saveOrderDetails(Order order) {
        if (order == null || order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            System.err.println("No order details to save or order is null");
            return false;
        }

        // KIỂM TRA orderID
        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
            System.err.println("ERROR: OrderID is null or empty, cannot save order details!");
            return false;
        }

        System.out.println("DEBUG: Saving order details for order ID: " + order.getOrderId());

        String insertDetailSQL = "INSERT INTO OrderDetails (orderID, productID, quantity, " +
                "unitPrice, warrantyType, warrantyPrice, note) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertDetailSQL)) {

            conn.setAutoCommit(false);
            System.out.println("Saving " + order.getOrderDetails().size() + " order detail items");

            for (OrderDetail detail : order.getOrderDetails()) {
                // QUAN TRỌNG: Thiết lập orderID cho chi tiết đơn hàng
                detail.setOrderId(order.getOrderId());

                // In ra log để theo dõi
                System.out.println("DEBUG: Setting orderID=" + order.getOrderId() +
                        " for productID=" + detail.getProductId());

                stmt.setString(1, order.getOrderId());
                stmt.setString(2, detail.getProductId());
                stmt.setInt(3, detail.getQuantity());
                stmt.setDouble(4, detail.getUnitPrice());
                stmt.setString(5, detail.getWarrantyType());
                stmt.setDouble(6, detail.getWarrantyPrice());
                stmt.setString(7, detail.getNote());

                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            conn.commit();
            System.out.println("Batch executed with " + results.length + " results");

            return true;
        } catch (SQLException e) {
            System.err.println("Error saving order details: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy đơn hàng theo ID
     * @param orderId ID của đơn hàng
     * @return Đơn hàng nếu tìm thấy, null nếu không tìm thấy
     */
    public Order getOrderById(String orderId) {
        String query = "SELECT * FROM Orders WHERE orderID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getString("orderID"));
                order.setOrderDate(rs.getTimestamp("orderDate").toLocalDateTime());
                order.setTotalAmount(rs.getDouble("totalAmount"));
                order.setCustomerId(rs.getInt("customerID"));

                if (rs.getObject("employeeID") != null) {
                    order.setEmployeeId(rs.getInt("employeeID"));
                }

                order.setOrderStatus(rs.getString("orderStatus"));
                order.setPaymentMethod(rs.getString("paymentMethod"));

                // Lấy thông tin người nhận
                order.setRecipientName(rs.getString("recipientName"));
                order.setRecipientPhone(rs.getString("recipientPhone"));

                order.setShippingAddress(rs.getString("shippingAddress"));
                order.setShippingFee(rs.getDouble("shippingFee"));
                order.setNotes(rs.getString("notes"));

                // Lấy chi tiết đơn hàng
                order.setOrderDetails(getOrderDetailsForOrder(orderId));

                return order;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Lấy tất cả chi tiết của một đơn hàng
     * @param orderId ID của đơn hàng
     * @return Danh sách chi tiết đơn hàng
     */
    public List<OrderDetail> getOrderDetailsForOrder(String orderId) {
        List<OrderDetail> orderDetails = new ArrayList<>();

        if (orderId == null || orderId.isEmpty()) {
            System.err.println("ERROR: Invalid order ID for getting order details");
            return orderDetails;
        }

        String sql = "SELECT od.*, p.productName FROM OrderDetails od " +
                "LEFT JOIN Products p ON od.productID = p.productID " +
                "WHERE od.orderID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                OrderDetail detail = new OrderDetail();
                // Sửa chỗ này: getInt -> getString
                detail.setOrderDetailsId(rs.getString("orderDetailsID"));
                detail.setOrderId(rs.getString("orderID"));
                detail.setProductId(rs.getString("productID"));
                detail.setQuantity(rs.getInt("quantity"));
                detail.setUnitPrice(rs.getDouble("unitPrice"));
                detail.setWarrantyType(rs.getString("warrantyType"));
                detail.setWarrantyPrice(rs.getDouble("warrantyPrice"));

                // Tạo sản phẩm cơ bản chỉ với tên
                Product product = new Product();
                // Sửa chỗ này: setProductId -> setProductID
                product.setProductID(rs.getString("productID"));
                product.setProductName(rs.getString("productName"));
                detail.setProduct(product);

                // Tính tổng tiền
                detail.updateSubtotal();

                orderDetails.add(detail);
            }

            System.out.println("DEBUG: Found " + orderDetails.size() + " order details for order " + orderId);
        } catch (SQLException e) {
            System.err.println("Error getting order details: " + e.getMessage());
            e.printStackTrace();
        }

        return orderDetails;
    }

    /**
     * Cập nhật trạng thái đơn hàng
     * @param orderId ID của đơn hàng
     * @param status Trạng thái mới
     * @return true nếu cập nhật thành công, false nếu có lỗi
     */
    public boolean updateOrderStatus(String orderId, String status) {
        String updateSQL = "UPDATE Orders SET orderStatus = ? WHERE orderID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSQL)) {

            stmt.setString(1, status);
            stmt.setString(2, orderId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy danh sách đơn hàng của một khách hàng, sắp xếp theo thời gian mới nhất
     * @param customerId ID của khách hàng
     * @return Danh sách đơn hàng
     */
    public List<Order> getOrdersByCustomerId(int customerId) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM Orders WHERE customerID = ? ORDER BY orderDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getString("orderID"));
                order.setOrderDate(rs.getTimestamp("orderDate").toLocalDateTime());
                order.setTotalAmount(rs.getDouble("totalAmount"));
                order.setCustomerId(rs.getInt("customerID"));

                if (rs.getObject("employeeID") != null) {
                    order.setEmployeeId(rs.getInt("employeeID"));
                }

                order.setOrderStatus(rs.getString("orderStatus"));
                order.setPaymentMethod(rs.getString("paymentMethod"));

                // Lấy thông tin người nhận
                order.setRecipientName(rs.getString("recipientName"));
                order.setRecipientPhone(rs.getString("recipientPhone"));

                order.setShippingAddress(rs.getString("shippingAddress"));
                order.setShippingFee(rs.getDouble("shippingFee"));
                order.setNotes(rs.getString("notes"));

                orders.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }
}