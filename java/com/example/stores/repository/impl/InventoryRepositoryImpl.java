package com.example.stores.repository.impl;

import com.example.stores.config.DatabaseConnection;
import com.example.stores.model.Inventory;
import com.example.stores.repository.InventoryRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryRepositoryImpl implements InventoryRepository {

    private Inventory mapRowToInventory(ResultSet rs) throws SQLException {
        Inventory inventory = new Inventory();
        inventory.setInventoryID(rs.getInt("inventoryID"));
        inventory.setWarehouseID(rs.getInt("warehouseID"));
        inventory.setProductID(rs.getString("productID"));
        inventory.setQuantity(rs.getInt("quantity"));
        Timestamp lastUpdateDB = rs.getTimestamp("lastUpdate");
        if (lastUpdateDB != null) {
            inventory.setLastUpdate(lastUpdateDB.toLocalDateTime());
        }

        // Lấy thông tin join nếu có
        if (hasColumn(rs, "warehouseName")) {
            inventory.setWarehouseNameDisplay(rs.getString("warehouseName"));
        }
        if (hasColumn(rs, "productName")) {
            inventory.setProductNameDisplay(rs.getString("productName"));
        }
        if (hasColumn(rs, "productStatus")) { // Lấy từ cột status của Products
            inventory.setProductStatusDisplay(rs.getString("productStatus"));
        }
        return inventory;
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
    public Inventory save(Inventory inventory) {
        String sql = "INSERT INTO Inventory (warehouseID, productID, quantity, lastUpdate) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, inventory.getWarehouseID());
            pstmt.setString(2, inventory.getProductID());
            pstmt.setInt(3, inventory.getQuantity());
            pstmt.setTimestamp(4, Timestamp.valueOf(inventory.getLastUpdate() != null ? inventory.getLastUpdate() : LocalDateTime.now()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        inventory.setInventoryID(generatedKeys.getInt(1));
                        return inventory;
                    }
                }
            }
        } catch (SQLException e) {
            // Có thể do vi phạm UQ_Inv_Unique (warehouseID, productID)
            System.err.println("SQL Error in InventoryRepositoryImpl.save: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Inventory inventory) {
        // Thường chỉ cập nhật quantity và lastUpdate dựa trên inventoryID
        // Hoặc dựa trên warehouseID và productID
        String sql = "UPDATE Inventory SET quantity = ?, lastUpdate = ? WHERE inventoryID = ?";
        // Nếu muốn update bằng warehouseID và productID:
        // String sql = "UPDATE Inventory SET quantity = ?, lastUpdate = ? WHERE warehouseID = ? AND productID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, inventory.getQuantity());
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now())); // Luôn cập nhật lastUpdate
            pstmt.setInt(3, inventory.getInventoryID());
            // Nếu dùng W_ID và P_ID:
            // pstmt.setInt(3, inventory.getWarehouseID());
            // pstmt.setString(4, inventory.getProductID());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in InventoryRepositoryImpl.update: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteById(int inventoryId) {
        String sql = "DELETE FROM Inventory WHERE inventoryID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, inventoryId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Optional<Inventory> findById(int inventoryId) {
        String sql = "SELECT i.*, w.warehouseName, p.productName, p.status as productStatus " +
                "FROM Inventory i " +
                "JOIN Warehouse w ON i.warehouseID = w.warehouseID " +
                "JOIN Products p ON i.productID = p.productID " +
                "WHERE i.inventoryID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, inventoryId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToInventory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Inventory> findByWarehouseIdAndProductId(int warehouseId, String productId) {
        // Không cần JOIN ở đây nếu chỉ để lấy bản ghi gốc cho việc update/insert
        String sql = "SELECT * FROM Inventory WHERE warehouseID = ? AND productID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);
            pstmt.setString(2, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Map không cần details vì mục đích chính là kiểm tra tồn tại hoặc lấy quantity
                Inventory inventory = new Inventory();
                inventory.setInventoryID(rs.getInt("inventoryID"));
                inventory.setWarehouseID(rs.getInt("warehouseID"));
                inventory.setProductID(rs.getString("productID"));
                inventory.setQuantity(rs.getInt("quantity"));
                Timestamp lastUpdateDB = rs.getTimestamp("lastUpdate");
                if (lastUpdateDB != null) {
                    inventory.setLastUpdate(lastUpdateDB.toLocalDateTime());
                }
                return Optional.of(inventory);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Inventory> findAllWithDetails() {
        List<Inventory> inventories = new ArrayList<>();
        String sql = "SELECT i.*, w.warehouseName, p.productName, p.status as productStatus " +
                "FROM Inventory i " +
                "JOIN Warehouse w ON i.warehouseID = w.warehouseID " +
                "JOIN Products p ON i.productID = p.productID " +
                "ORDER BY w.warehouseName, p.productName";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                inventories.add(mapRowToInventory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inventories;
    }

    @Override
    public List<Inventory> findByWarehouseIdWithDetails(int warehouseId) {
        List<Inventory> inventories = new ArrayList<>();
        String sql = "SELECT i.*, w.warehouseName, p.productName, p.status as productStatus " +
                "FROM Inventory i " +
                "JOIN Warehouse w ON i.warehouseID = w.warehouseID " +
                "JOIN Products p ON i.productID = p.productID " +
                "WHERE i.warehouseID = ? ORDER BY p.productName";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                inventories.add(mapRowToInventory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inventories;
    }

    @Override
    public List<Inventory> findByProductIdWithDetails(String productId) {
        List<Inventory> inventories = new ArrayList<>();
        String sql = "SELECT i.*, w.warehouseName, p.productName, p.status as productStatus " +
                "FROM Inventory i " +
                "JOIN Warehouse w ON i.warehouseID = w.warehouseID " +
                "JOIN Products p ON i.productID = p.productID " +
                "WHERE i.productID = ? ORDER BY w.warehouseName";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                inventories.add(mapRowToInventory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inventories;
    }

    @Override
    public boolean updateStockQuantity(int warehouseId, String productId, int quantityChange) {
        Optional<Inventory> existingInventoryOpt = findByWarehouseIdAndProductId(warehouseId, productId);
        if (existingInventoryOpt.isPresent()) {
            // Cập nhật bản ghi hiện có
            Inventory existingInventory = existingInventoryOpt.get();
            int newQuantity = existingInventory.getQuantity() + quantityChange;
            if (newQuantity < 0) { // Không cho phép số lượng âm
                // Service nên xử lý lỗi này và ném exception
                System.err.println("Lỗi: Số lượng tồn kho không thể âm cho sản phẩm " + productId + " tại kho " + warehouseId);
                return false;
            }
            existingInventory.setQuantity(newQuantity);
            existingInventory.setLastUpdate(LocalDateTime.now());
            return update(existingInventory); // Gọi hàm update đã có
        } else {
            // Tạo bản ghi mới nếu chưa có
            if (quantityChange < 0) {
                // Không thể tạo bản ghi mới với số lượng âm khi giảm tồn kho
                System.err.println("Lỗi: Không tìm thấy sản phẩm " + productId + " tại kho " + warehouseId + " để giảm số lượng.");
                return false;
            }
            Inventory newInventory = new Inventory();
            newInventory.setWarehouseID(warehouseId);
            newInventory.setProductID(productId);
            newInventory.setQuantity(quantityChange);
            newInventory.setLastUpdate(LocalDateTime.now());
            return save(newInventory) != null;
        }
    }
}