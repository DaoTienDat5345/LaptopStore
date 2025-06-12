package com.example.stores.repository.impl; // Đảm bảo package đúng

import com.example.stores.config.DatabaseConnection;
import com.example.stores.model.Manager;
import com.example.stores.repository.ManagerRepository;

import java.sql.*;
import java.time.LocalDate; // THÊM IMPORT NÀY
import java.time.LocalDateTime; // THÊM IMPORT NÀY
import java.util.ArrayList; // THÊM IMPORT NÀY NẾU CÓ findAll()
import java.util.List;    // THÊM IMPORT NÀY NẾU CÓ findAll()
import java.util.Optional;

public class ManagerRepositoryImpl implements ManagerRepository {

    // --- TẠO PHƯƠNG THỨC HELPER mapRowToManager ---
    private Manager mapRowToManager(ResultSet rs) throws SQLException {
        Manager manager = new Manager();
        manager.setManagerID(rs.getInt("managerID"));
        manager.setUsername(rs.getString("username"));
        manager.setPassword(rs.getString("password"));
        manager.setFullName(rs.getString("fullName"));
        manager.setEmail(rs.getString("email"));
        manager.setPhone(rs.getString("phone"));
        manager.setGender(rs.getString("gender"));

        Date birthDateDB = rs.getDate("birthDate");
        if (birthDateDB != null) {
            manager.setBirthDate(birthDateDB.toLocalDate());
        }

        manager.setAddress(rs.getString("address"));
        manager.setImageUrl(rs.getString("imageUrl"));

        Timestamp createdAtDB = rs.getTimestamp("createdAt");
        if (createdAtDB != null) {
            manager.setCreatedAt(createdAtDB.toLocalDateTime());
        }
        return manager;
    }
    // --- KẾT THÚC HELPER ---


    @Override
    public Optional<Manager> findById(int id) {
        String sql = "SELECT * FROM Manager WHERE managerID = ?"; // Đơn giản hóa SELECT *
        Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRowToManager(rs)); // SỬ DỤNG HELPER
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in ManagerRepositoryImpl.findById: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null); // Gọi helper đóng resources
        }
        return Optional.empty();
    }

    @Override
    public boolean update(Manager manager) {
        // ... (Code update giữ nguyên như bạn đã cung cấp, nhưng cũng nên dùng mô hình quản lý Connection mới) ...
        // Không cho phép cập nhật managerID, username, createdAt
        // Mật khẩu chỉ cập nhật nếu được cung cấp (khác null và không rỗng)
        String sql = "UPDATE Manager SET fullName = ?, email = ?, phone = ?, gender = ?, birthDate = ?, address = ?, imageUrl = ?";
        if (manager.getPassword() != null && !manager.getPassword().trim().isEmpty()) { // Sửa .isEmpty() thành .trim().isEmpty()
            sql += ", password = ?";
        }
        sql += " WHERE managerID = ?";

        Connection conn = null; PreparedStatement pstmt = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            int paramIndex = 1;
            pstmt.setString(paramIndex++, manager.getFullName());
            pstmt.setString(paramIndex++, manager.getEmail());
            pstmt.setString(paramIndex++, manager.getPhone());
            pstmt.setString(paramIndex++, manager.getGender());

            if (manager.getBirthDate() != null) {
                pstmt.setDate(paramIndex++, Date.valueOf(manager.getBirthDate()));
            } else {
                pstmt.setNull(paramIndex++, Types.DATE);
            }
            pstmt.setString(paramIndex++, manager.getAddress());
            pstmt.setString(paramIndex++, manager.getImageUrl());

            if (manager.getPassword() != null && !manager.getPassword().trim().isEmpty()) {
                pstmt.setString(paramIndex++, manager.getPassword());
            }
            pstmt.setInt(paramIndex, manager.getManagerID());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in ManagerRepositoryImpl.update: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, null); // Đóng pstmt
        }
        return false;
    }

    @Override
    public Optional<Manager> findByUsername(String username) {
        String sql = "SELECT * FROM Manager WHERE username = ?";
        Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username.trim()); // Nên trim username trước khi query
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToManager(rs)); // SỬ DỤNG HELPER
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null); // Gọi helper đóng resources
        }
        return Optional.empty();
    }

    // Thêm phương thức findAll() nếu ManagerService cần (ví dụ cho ComboBox WarehouseManager)
    // @Override
    // public List<Manager> findAll() {
    //     List<Manager> managers = new ArrayList<>();
    //     String sql = "SELECT * FROM Manager";
    //     Connection conn = null; Statement stmt = null; ResultSet rs = null;
    //     try {
    //         conn = DatabaseConnection.getConnection();
    //         stmt = conn.createStatement();
    //         rs = stmt.executeQuery(sql);
    //         while (rs.next()) {
    //             managers.add(mapRowToManager(rs));
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     } finally {
    //         closeResources(rs, stmt, null);
    //     }
    //     return managers;
    // }


    // Phương thức helper để đóng resources (ResultSet, Statement/PreparedStatement)
    // Connection không được đóng ở đây nếu là static connection từ DatabaseConnection
    private void closeResources(ResultSet rs, Statement stmt, Connection connOptionalToClose) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        // KHÔNG đóng connOptionalToClose ở đây
    }
}