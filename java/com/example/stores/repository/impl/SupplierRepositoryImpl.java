package com.example.stores.repository.impl;

import com.example.stores.config.DatabaseConnection;
import com.example.stores.repository.SupplierRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupplierRepositoryImpl implements SupplierRepository {

    private Supplier mapRowToSupplier(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setSupplierID(rs.getInt("supplierID"));
        supplier.setSupplierName(rs.getString("supplierName"));
        supplier.setEmail(rs.getString("email"));
        supplier.setPhone(rs.getString("phone"));
        supplier.setAddress(rs.getString("address"));
        supplier.setTaxCode(rs.getString("taxCode"));
        return supplier;
    }

    @Override
    public Supplier save(Supplier supplier) {
        String sql = "INSERT INTO Supplier (supplierName, email, phone, address, taxCode) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, supplier.getSupplierName());
            pstmt.setString(2, supplier.getEmail());
            pstmt.setString(3, supplier.getPhone());
            pstmt.setString(4, supplier.getAddress());
            pstmt.setString(5, supplier.getTaxCode());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        supplier.setSupplierID(generatedKeys.getInt(1));
                        return supplier;
                    }
                }
            }
        } catch (SQLException e) {
            // Lỗi có thể do email hoặc phone đã tồn tại (UNIQUE constraint)
            System.err.println("SQL Error in SupplierRepositoryImpl.save: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Supplier supplier) {
        String sql = "UPDATE Supplier SET supplierName = ?, email = ?, phone = ?, address = ?, taxCode = ? WHERE supplierID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, supplier.getSupplierName());
            pstmt.setString(2, supplier.getEmail());
            pstmt.setString(3, supplier.getPhone());
            pstmt.setString(4, supplier.getAddress());
            pstmt.setString(5, supplier.getTaxCode());
            pstmt.setInt(6, supplier.getSupplierID());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in SupplierRepositoryImpl.update: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteById(int supplierId) {
        // Service sẽ kiểm tra ràng buộc với ImportReceipt trước
        String sql = "DELETE FROM Supplier WHERE supplierID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, supplierId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in SupplierRepositoryImpl.deleteById: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Optional<Supplier> findById(int supplierId) {
        String sql = "SELECT * FROM Supplier WHERE supplierID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, supplierId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToSupplier(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM Supplier ORDER BY supplierName";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                suppliers.add(mapRowToSupplier(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    @Override
    public List<Supplier> searchSuppliers(String keyword) {
        List<Supplier> suppliers = new ArrayList<>();
        String searchPattern = "%" + keyword.toLowerCase() + "%";
        String sql = "SELECT * FROM Supplier WHERE " +
                "LOWER(supplierName) LIKE ? OR " +
                "LOWER(email) LIKE ? OR " +
                "phone LIKE ? " + // Phone không cần LOWER nếu CSDL không phân biệt hoa thường cho VARCHAR
                "ORDER BY supplierName";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                suppliers.add(mapRowToSupplier(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    @Override
    public long countImportReceiptsBySupplierId(int supplierId) {
        String sql = "SELECT COUNT(*) FROM ImportReceipt WHERE supplierID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, supplierId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Optional<Supplier> findByEmail(String email) {
        String sql = "SELECT * FROM Supplier WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToSupplier(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Supplier> findByPhone(String phone) {
        String sql = "SELECT * FROM Supplier WHERE phone = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToSupplier(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}