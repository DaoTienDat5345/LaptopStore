package com.example.stores.repository.impl;

import com.example.stores.config.DatabaseConnection;
import com.example.stores.model.Warehouse;
import com.example.stores.repository.WarehouseRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WarehouseRepositoryImpl implements WarehouseRepository {

    private Warehouse mapRowToWarehouse(ResultSet rs) throws SQLException {
        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseID(rs.getInt("warehouseID"));
        warehouse.setWarehouseName(rs.getString("warehouseName"));
        warehouse.setAddress(rs.getString("address"));
        warehouse.setPhone(rs.getString("phone"));
        // Lấy managerID, có thể null
        int managerId = rs.getInt("managerID");
        if (!rs.wasNull()) {
            warehouse.setManagerID(managerId);
        } else {
            warehouse.setManagerID(null);
        }

        // Lấy managerFullNameDisplay từ JOIN nếu có
        if (hasColumn(rs, "managerFullName")) {
            warehouse.setManagerFullNameDisplay(rs.getString("managerFullName"));
        }
        return warehouse;
    }

    // Tiện ích kiểm tra cột tồn tại trong ResultSet
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
    public Warehouse save(Warehouse warehouse) {
        String sql = "INSERT INTO Warehouse (warehouseName, address, phone, managerID) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, warehouse.getWarehouseName());
            pstmt.setString(2, warehouse.getAddress());
            pstmt.setString(3, warehouse.getPhone());
            if (warehouse.getManagerID() != null) {
                pstmt.setInt(4, warehouse.getManagerID());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        warehouse.setWarehouseID(generatedKeys.getInt(1));
                        return warehouse;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in WarehouseRepositoryImpl.save: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Warehouse warehouse) {
        String sql = "UPDATE Warehouse SET warehouseName = ?, address = ?, phone = ?, managerID = ? WHERE warehouseID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, warehouse.getWarehouseName());
            pstmt.setString(2, warehouse.getAddress());
            pstmt.setString(3, warehouse.getPhone());
            if (warehouse.getManagerID() != null) {
                pstmt.setInt(4, warehouse.getManagerID());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setInt(5, warehouse.getWarehouseID());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in WarehouseRepositoryImpl.update: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteById(int warehouseId) {
        // Service sẽ kiểm tra ràng buộc với Inventory trước
        String sql = "DELETE FROM Warehouse WHERE warehouseID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in WarehouseRepositoryImpl.deleteById: " + e.getMessage());
            e.printStackTrace(); // Có thể do ràng buộc khóa ngoại
        }
        return false;
    }

    @Override
    public Optional<Warehouse> findById(int warehouseId) {
        String sql = "SELECT w.*, m.fullName as managerFullName " +
                "FROM Warehouse w " +
                "LEFT JOIN Manager m ON w.managerID = m.managerID " + // LEFT JOIN vì managerID có thể null
                "WHERE w.warehouseID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToWarehouse(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Warehouse> findAllWithManagerName() {
        List<Warehouse> warehouses = new ArrayList<>();
        String sql = "SELECT w.*, m.fullName as managerFullName " +
                "FROM Warehouse w " +
                "LEFT JOIN Manager m ON w.managerID = m.managerID " +
                "ORDER BY w.warehouseName";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                warehouses.add(mapRowToWarehouse(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return warehouses;
    }

    @Override
    public List<Warehouse> searchWarehouses(String keyword) {
        List<Warehouse> warehouses = new ArrayList<>();
        String searchPattern = "%" + keyword.toLowerCase() + "%";
        String sql = "SELECT w.*, m.fullName as managerFullName " +
                "FROM Warehouse w " +
                "LEFT JOIN Manager m ON w.managerID = m.managerID " +
                "WHERE LOWER(w.warehouseName) LIKE ? OR LOWER(w.address) LIKE ? " +
                "ORDER BY w.warehouseName";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                warehouses.add(mapRowToWarehouse(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return warehouses;
    }

    @Override
    public long countInventoryItemsByWarehouseId(int warehouseId) {
        String sql = "SELECT COUNT(*) FROM Inventory WHERE warehouseID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}