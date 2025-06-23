package com.example.stores.repository.impl;

import com.example.stores.config.DatabaseConnection;
import com.example.stores.model.Customer;
import com.example.stores.repository.CustomerRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerRepositoryImpl implements CustomerRepository {

    private Customer mapRowToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerID(rs.getInt("customerID"));
        customer.setUsername(rs.getString("username"));
        // Không lấy password
        customer.setFullName(rs.getString("fullName"));
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("phone"));
        customer.setGender(rs.getString("gender"));
        Date birthDateDB = rs.getDate("birthDate");
        if (birthDateDB != null) {
            customer.setBirthDate(birthDateDB.toLocalDate());
        }
        customer.setAddress(rs.getString("address"));
        Timestamp registeredAtDB = rs.getTimestamp("registeredAt");
        if (registeredAtDB != null) {
            customer.setRegisteredAt(registeredAtDB.toLocalDateTime());
        }
        customer.setActive(rs.getBoolean("isActive"));
        return customer;
    }

    @Override
    public boolean update(Customer customer) {
        // Manager có thể được phép sửa: fullName, email, phone, gender, birthDate, address, isActive
        // Username và password thường không cho Manager sửa trực tiếp
        String sql = "UPDATE Customer SET fullName = ?, email = ?, phone = ?, gender = ?, " +
                "birthDate = ?, address = ?, isActive = ? WHERE customerID = ?";
        Connection conn = null; PreparedStatement pstmt = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, customer.getFullName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getPhone());
            pstmt.setString(4, customer.getGender());
            if (customer.getBirthDate() != null) {
                pstmt.setDate(5, Date.valueOf(customer.getBirthDate()));
            } else {
                pstmt.setNull(5, Types.DATE);
            }
            pstmt.setString(6, customer.getAddress());
            pstmt.setBoolean(7, customer.isActive());
            pstmt.setInt(8, customer.getCustomerID());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace(); // Lỗi có thể do email, phone trùng nếu chúng là UNIQUE và bị sửa
        } finally {
            closeResources(null, pstmt, null);
        }
        return false;
    }


    @Override
    public Optional<Customer> findById(int customerId) { /* Giữ nguyên */
        String sql = "SELECT * FROM Customer WHERE customerID = ?";
        Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection(); pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, customerId); rs = pstmt.executeQuery();
            if (rs.next()) return Optional.of(mapRowToCustomer(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { closeResources(rs, pstmt, null); }
        return Optional.empty();
    }


    @Override
    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM Customer WHERE isActive = 1 ORDER BY fullName"; // Chỉ lấy KH active
        Connection conn = null; Statement stmt = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                customers.add(mapRowToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, null);
        }
        return customers;
    }

    @Override
    public List<Customer> searchCustomers(String keyword, Boolean isActiveFilter) {
        List<Customer> customers = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM Customer WHERE (1=1) ");
        List<Object> params = new ArrayList<>();
        String searchPattern = "%" + (keyword != null ? keyword.toLowerCase() : "") + "%";

        if (keyword != null && !keyword.trim().isEmpty()) {
            sqlBuilder.append("AND (LOWER(fullName) LIKE ? OR LOWER(username) LIKE ? OR LOWER(email) LIKE ? OR phone LIKE ?) ");
            for(int i=0; i<4; i++) params.add(searchPattern);
        }
        if (isActiveFilter != null) {
            sqlBuilder.append("AND isActive = ? ");
            params.add(isActiveFilter);
        }
        sqlBuilder.append("ORDER BY fullName");

        Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sqlBuilder.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                customers.add(mapRowToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null);
        }
        return customers;
    }

    @Override
    public Optional<Customer> findByUsername(String username) {
        String sql = "SELECT * FROM Customer WHERE username = ?";
        Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection(); pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username); rs = pstmt.executeQuery();
            if (rs.next()) return Optional.of(mapRowToCustomer(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { closeResources(rs, pstmt, null); }
        return Optional.empty();
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        String sql = "SELECT * FROM Customer WHERE email = ?";
        Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection(); pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email); rs = pstmt.executeQuery();
            if (rs.next()) return Optional.of(mapRowToCustomer(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { closeResources(rs, pstmt, null); }
        return Optional.empty();
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
        String sql = "SELECT * FROM Customer WHERE phone = ?";
        Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection(); pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phone); rs = pstmt.executeQuery();
            if (rs.next()) return Optional.of(mapRowToCustomer(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { closeResources(rs, pstmt, null); }
        return Optional.empty();
    }

    private void closeResources(ResultSet rs, Statement stmt, Connection connOptionalToClose) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}