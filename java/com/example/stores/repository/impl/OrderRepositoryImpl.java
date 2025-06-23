package com.example.stores.repository.impl;

import com.example.stores.config.DatabaseConnection;
import com.example.stores.model.Order;
import com.example.stores.repository.OrderRepository;
import com.example.stores.dto.EmployeeSalesReportItem;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepositoryImpl implements OrderRepository {

    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderID(rs.getString("orderID"));
        Timestamp orderDateDB = rs.getTimestamp("orderDate");
        if (orderDateDB != null) {
            order.setOrderDate(orderDateDB.toLocalDateTime());
        }
        order.setTotalAmount(rs.getDouble("totalAmount"));
        order.setCustomerID(rs.getInt("customerID"));
        int empId = rs.getInt("employeeID");
        if (!rs.wasNull()) { // employeeID có thể null
            order.setEmployeeID(empId);
        }

        // Lấy các cột mới từ bảng Orders (nếu chúng tồn tại)
        if (hasColumn(rs, "orderStatus")) {
            order.setOrderStatus(rs.getString("orderStatus"));
        }
        if (hasColumn(rs, "shippingAddress")) {
            order.setShippingAddress(rs.getString("shippingAddress"));
        }
        // representativeOrderRating sẽ được Service điền sau

        // Lấy thông tin join
        if (hasColumn(rs, "customerFullName")) {
            order.setCustomerFullNameDisplay(rs.getString("customerFullName"));
        }
        if (hasColumn(rs, "employeeFullName")) {
            order.setEmployeeFullNameDisplay(rs.getString("employeeFullName"));
        }
        return order;
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
    public boolean updateStatus(String orderId, String newStatus) {
        // Phương thức này để cập nhật cột 'orderStatus' trong bảng Orders
        String sql = "UPDATE Orders SET orderStatus = ? WHERE orderID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newStatus);
            pstmt.setString(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in OrderRepositoryImpl.updateStatus: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, null);
        }
        return false;
    }

    @Override
    public Optional<Order> findByIdWithCustomerAndEmployee(String orderId) {
        String sql = "SELECT o.*, c.fullName as customerFullName, e.fullName as employeeFullName " +
                // ", o.orderStatus, o.shippingAddress " + // Đã có trong SELECT o.* nếu tồn tại
                "FROM Orders o " +
                "JOIN Customer c ON o.customerID = c.customerID " +
                "LEFT JOIN Employee e ON o.employeeID = e.employeeID " +
                "WHERE o.orderID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, orderId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null);
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findAllWithCustomerAndEmployee() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, c.fullName as customerFullName, e.fullName as employeeFullName " +
                // ", o.orderStatus, o.shippingAddress " + // Đã có trong SELECT o.* nếu tồn tại
                "FROM Orders o " +
                "JOIN Customer c ON o.customerID = c.customerID " +
                "LEFT JOIN Employee e ON o.employeeID = e.employeeID " +
                "ORDER BY o.orderDate DESC";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                orders.add(mapRowToOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, null);
        }
        return orders;
    }

    @Override
    public List<Order> findByCriteriaWithCustomerAndEmployee(LocalDate startDate, LocalDate endDate, Integer customerId) { // Bỏ status từ tham số
        List<Order> orders = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT o.*, c.fullName as customerFullName, e.fullName as employeeFullName " +
                        // ", o.orderStatus, o.shippingAddress " + // Đã có trong SELECT o.* nếu tồn tại
                        "FROM Orders o " +
                        "JOIN Customer c ON o.customerID = c.customerID " +
                        "LEFT JOIN Employee e ON o.employeeID = e.employeeID " +
                        "WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (startDate != null) {
            sqlBuilder.append("AND CONVERT(date, o.orderDate) >= ? ");
            params.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            sqlBuilder.append("AND CONVERT(date, o.orderDate) <= ? ");
            params.add(Date.valueOf(endDate));
        }
        if (customerId != null && customerId > 0) {
            sqlBuilder.append("AND o.customerID = ? ");
            params.add(customerId);
        }
        // Không còn lọc theo status ở đây nữa

        sqlBuilder.append("ORDER BY o.orderDate DESC");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sqlBuilder.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                orders.add(mapRowToOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null);
        }
        return orders;
    }

    @Override
    public long countOrdersByProductId(String productId) {
        // Đếm số lượng đơn hàng (orderID) riêng biệt chứa sản phẩm này
        String sql = "SELECT COUNT(DISTINCT od.orderID) FROM OrderDetails od WHERE od.productID = ?";
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

    @Override
    public List<EmployeeSalesReportItem> getEmployeeSalesReport(LocalDate startDate, LocalDate endDate) {
        List<EmployeeSalesReportItem> reportItems = new ArrayList<>();
        // Câu SQL này JOIN Orders, OrderDetails, và Employee
        // để tính tổng số lượng sản phẩm bán và tổng doanh thu cho mỗi nhân viên
        // trong khoảng thời gian đã cho.
        String sql = "SELECT " +
                "    e.employeeID, " +
                "    e.fullName AS employeeFullName, " +
                "    ISNULL(SUM(od.quantity), 0) AS totalProductsSold, " + // ISNULL để trả về 0 nếu không có đơn hàng
                "    ISNULL(SUM(od.subtotal), 0) AS totalRevenue " +
                "FROM Employee e " +
                "LEFT JOIN Orders o ON e.employeeID = o.employeeID " +
                "LEFT JOIN OrderDetails od ON o.orderID = od.orderID " +
                "WHERE o.employeeID IS NOT NULL "; // Chỉ lấy các đơn hàng có nhân viên xử lý

        // Thêm điều kiện lọc theo ngày nếu startDate và endDate được cung cấp
        if (startDate != null && endDate != null) {
            sql += "AND CONVERT(date, o.orderDate) BETWEEN ? AND ? ";
        } else if (startDate != null) {
            sql += "AND CONVERT(date, o.orderDate) >= ? ";
        } else if (endDate != null) {
            sql += "AND CONVERT(date, o.orderDate) <= ? ";
        }

        sql += "GROUP BY e.employeeID, e.fullName " +
                "ORDER BY totalRevenue DESC, e.fullName ASC"; // Sắp xếp theo doanh thu giảm dần

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int paramIndex = 1;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            if (startDate != null && endDate != null) {
                pstmt.setDate(paramIndex++, Date.valueOf(startDate));
                pstmt.setDate(paramIndex++, Date.valueOf(endDate));
            } else if (startDate != null) {
                pstmt.setDate(paramIndex++, Date.valueOf(startDate));
            } else if (endDate != null) {
                pstmt.setDate(paramIndex++, Date.valueOf(endDate));
            }
            // Nếu cả startDate và endDate đều null, không có tham số ngày nào được thêm

            rs = pstmt.executeQuery();
            while (rs.next()) {
                int empId = rs.getInt("employeeID");
                String empName = rs.getString("employeeFullName");
                long productsSold = rs.getLong("totalProductsSold");
                double revenue = rs.getDouble("totalRevenue");
                reportItems.add(new EmployeeSalesReportItem(empId, empName, productsSold, revenue));
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in OrderRepositoryImpl.getEmployeeSalesReport: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null);
        }
        return reportItems;
    }

    private void closeResources(ResultSet rs, Statement stmt, Connection connOptionalToClose) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        // KHÔNG đóng connOptionalToClose nếu nó là connection dùng chung từ DatabaseConnection
    }
}