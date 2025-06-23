package com.repository;

import com.database.QLdatabase;
import com.model.Manager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ManagerRepository {
    public Manager findById(int managerID) throws SQLException {
        String sql = "SELECT managerID, userManager, userPasswordManager, imageManager, ManagerName, ManagerAge, ManagerPhone, ManagerDate FROM Manager WHERE managerID = ?";
        Manager manager = null;

        try (Connection conn = QLdatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (conn == null) {
                throw new SQLException("Repository: Không thể kết nối CSDL.");
            }

            ps.setInt(1, managerID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    manager = new Manager(
                            rs.getInt("managerID"),
                            rs.getString("userManager"),
                            rs.getString("userPasswordManager"), 
                            rs.getString("imageManager"),
                            rs.getString("ManagerName"),
                            rs.getInt("ManagerAge"),
                            rs.getString("ManagerPhone"),
                            rs.getDate("ManagerDate") 
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Repository Error: Lỗi SQL khi tìm Manager theo ID - " + e.getMessage());
            throw e; 
        } catch (Exception e) {
             System.err.println("Repository Error: Lỗi không xác định khi tìm Manager theo ID - " + e.getMessage());
             throw new SQLException("Lỗi không xác định: " + e.getMessage(), e);
        }
        return manager;
    }
}