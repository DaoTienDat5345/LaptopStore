package com.example.stores.repository.impl;

import com.example.stores.config.DatabaseConnection;
import com.example.stores.model.ImportReceipt;
import com.example.stores.model.ImportReceiptDetail;
import com.example.stores.repository.ImportReceiptRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImportReceiptRepositoryImpl implements ImportReceiptRepository {

    // ... (mapRowToImportReceipt, mapRowToImportReceiptDetail, hasColumn giữ nguyên) ...
    private ImportReceipt mapRowToImportReceipt(ResultSet rs) throws SQLException {
        ImportReceipt receipt = new ImportReceipt();
        receipt.setReceiptID(rs.getInt("receiptID"));
        receipt.setSupplierID(rs.getInt("supplierID"));
        receipt.setEmployeeID(rs.getInt("employeeID"));
        receipt.setWarehouseID(rs.getInt("warehouseID"));
        Timestamp importDateDB = rs.getTimestamp("importDate");
        if (importDateDB != null) {
            receipt.setImportDate(importDateDB.toLocalDateTime());
        }
        receipt.setTotalAmount(rs.getBigDecimal("totalAmount"));
        receipt.setNote(rs.getString("note"));

        if (hasColumn(rs, "supplierName")) {
            receipt.setSupplierNameDisplay(rs.getString("supplierName"));
        }
        if (hasColumn(rs, "employeeFullName")) {
            receipt.setEmployeeNameDisplay(rs.getString("employeeFullName"));
        }
        if (hasColumn(rs, "warehouseName")) {
            receipt.setWarehouseNameDisplay(rs.getString("warehouseName"));
        }
        return receipt;
    }

    private ImportReceiptDetail mapRowToImportReceiptDetail(ResultSet rs) throws SQLException {
        ImportReceiptDetail detail = new ImportReceiptDetail();
        detail.setReceiptDetailID(rs.getInt("receiptDetailID"));
        detail.setReceiptID(rs.getInt("receiptID"));
        detail.setProductID(rs.getString("productID"));
        detail.setQuantity(rs.getInt("quantity"));
        detail.setUnitCost(rs.getBigDecimal("unitCost"));
        if (hasColumn(rs, "productName")) {
            detail.setProductNameDisplay(rs.getString("productName"));
        }
        return detail;
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


    // ... (Phương thức save(ImportReceipt) giữ nguyên cách xử lý transaction với conn riêng) ...
    // Phương thức save() với transaction nên tự quản lý connection của nó (mở và đóng setAutoCommit(true) trong finally)
    // hoặc DatabaseConnection.getConnection() phải trả về connection mới mỗi lần nếu muốn dùng try-with-resources cho conn.
    // Với DatabaseConnection hiện tại, save() nên giữ nguyên.

    // ... (saveDetail giữ nguyên) ...


    @Override
    public ImportReceipt save(ImportReceipt importReceipt) {
        Connection conn = null;
        PreparedStatement pstmtReceipt = null;
        ResultSet generatedKeysReceipt = null;
        String sqlReceipt = "INSERT INTO ImportReceipt (supplierID, employeeID, warehouseID, importDate, totalAmount, note) VALUES (?, ?, ?, ?, ?, ?)";
        boolean originalAutoCommit = true;

        try {
            conn = DatabaseConnection.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            pstmtReceipt = conn.prepareStatement(sqlReceipt, Statement.RETURN_GENERATED_KEYS);
            // ... (set parameters cho pstmtReceipt) ...
            pstmtReceipt.setInt(1, importReceipt.getSupplierID());
            pstmtReceipt.setInt(2, importReceipt.getEmployeeID());
            pstmtReceipt.setInt(3, importReceipt.getWarehouseID());
            pstmtReceipt.setTimestamp(4, Timestamp.valueOf(importReceipt.getImportDate() != null ? importReceipt.getImportDate() : LocalDateTime.now()));
            pstmtReceipt.setBigDecimal(5, importReceipt.getTotalAmount());
            pstmtReceipt.setString(6, importReceipt.getNote());

            int affectedRows = pstmtReceipt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Tạo phiếu nhập thất bại (header), không có dòng nào được thêm.");
            generatedKeysReceipt = pstmtReceipt.getGeneratedKeys();
            if (generatedKeysReceipt.next()) importReceipt.setReceiptID(generatedKeysReceipt.getInt(1));
            else throw new SQLException("Tạo phiếu nhập thất bại (header), không lấy được ID.");
            closeResources(generatedKeysReceipt, pstmtReceipt, null); // Chỉ đóng rs và pstmt của receipt

            if (importReceipt.getDetails() != null && !importReceipt.getDetails().isEmpty()) {
                for (ImportReceiptDetail detail : importReceipt.getDetails()) {
                    detail.setReceiptID(importReceipt.getReceiptID());
                    ImportReceiptDetail savedDetail = saveDetail(detail, conn);
                    if (savedDetail == null) throw new SQLException("Lưu chi tiết phiếu nhập thất bại cho SP ID: " + detail.getProductID());
                }
            }
            conn.commit();
            return importReceipt;
        } catch (SQLException e) {
            System.err.println("--- LỖI SQL TRONG ImportReceiptRepositoryImpl.save ---");
            System.err.println("Message: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("ErrorCode: " + e.getErrorCode());
            e.printStackTrace(System.err); // In stack trace ra System.err
            if (conn != null) {
                try {
                    System.err.println("Đang rollback transaction do lỗi...");
                    conn.rollback();
                } catch (SQLException excep) {
                    System.err.println("Lỗi khi rollback: " + excep.getMessage());
                    excep.printStackTrace(System.err);
                }
            }
            return null;
        } finally {
            closeResources(generatedKeysReceipt, pstmtReceipt, null); // Đóng lại nếu chưa đóng
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (SQLException e) { e.printStackTrace(System.err); }
            }
        }
    }

    // Trong ImportReceiptRepositoryImpl.java
    @Override
    public boolean deleteReceiptAndItsDetailsById(int receiptId) {
        Connection conn = null;
        boolean originalAutoCommit = true;
        // Nếu không có ON DELETE CASCADE cho ImportReceiptDetail, bạn phải xóa details trước
        String sqlDeleteDetails = "DELETE FROM ImportReceiptDetail WHERE receiptID = ?";
        String sqlDeleteReceipt = "DELETE FROM ImportReceipt WHERE receiptID = ?";

        try {
            conn = DatabaseConnection.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Xóa các chi tiết phiếu nhập (chỉ cần nếu không có ON DELETE CASCADE)
            // Nếu có ON DELETE CASCADE, bước này không cần thiết, CSDL tự làm.
            try (PreparedStatement pstmtDetails = conn.prepareStatement(sqlDeleteDetails)) {
                pstmtDetails.setInt(1, receiptId);
                pstmtDetails.executeUpdate(); // Không cần kiểm tra affectedRows ở đây nếu CASCADE
            }

            // 2. Xóa phiếu nhập chính
            int affectedRowsReceipt = 0;
            try (PreparedStatement pstmtReceipt = conn.prepareStatement(sqlDeleteReceipt)) {
                pstmtReceipt.setInt(1, receiptId);
                affectedRowsReceipt = pstmtReceipt.executeUpdate();
            }

            if (affectedRowsReceipt > 0) {
                conn.commit();
                return true;
            } else {
                // Nếu không xóa được phiếu chính (ví dụ: không tìm thấy), rollback
                conn.rollback();
                System.err.println("Không tìm thấy phiếu nhập ID: " + receiptId + " để xóa, hoặc xóa thất bại.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("SQL Transaction Error in ImportReceiptRepositoryImpl.deleteReceiptAndItsDetailsById: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException excep) {
                    excep.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // CẬP NHẬT CÁC PHƯƠNG THỨC find... VÀ findAllWithDetails
    @Override
    public Optional<ImportReceipt> findById(int receiptId) {
        String sql = "SELECT ir.*, s.supplierName, e.fullName as employeeFullName, w.warehouseName " +
                "FROM ImportReceipt ir " +
                "JOIN Supplier s ON ir.supplierID = s.supplierID " +
                "JOIN Employee e ON ir.employeeID = e.employeeID " +
                "JOIN Warehouse w ON ir.warehouseID = w.warehouseID " +
                "WHERE ir.receiptID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection(); // Lấy connection
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, receiptId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                ImportReceipt receipt = mapRowToImportReceipt(rs);
                // Load details cho receipt này, truyền connection hiện tại nếu cần
                // Hoặc để findDetailsByReceiptId tự lấy connection mới (an toàn hơn nếu không muốn truyền conn)
                receipt.setDetails(findDetailsByReceiptId(receipt.getReceiptID()));
                return Optional.of(receipt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null); // Chỉ đóng rs và pstmt, không đóng conn
        }
        return Optional.empty();
    }



    @Override
    public List<ImportReceiptDetail> findDetailsByReceiptId(int receiptId) {
        List<ImportReceiptDetail> details = new ArrayList<>();
        String sql = "SELECT ird.*, p.productName " +
                "FROM ImportReceiptDetail ird " +
                "JOIN Products p ON ird.productID = p.productID " +
                "WHERE ird.receiptID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, receiptId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                details.add(mapRowToImportReceiptDetail(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null);
        }
        return details;
    }

    @Override
    public List<ImportReceipt> findAllWithDetails() {
        List<ImportReceipt> receipts = new ArrayList<>();
        String sql = "SELECT ir.*, s.supplierName, e.fullName as employeeFullName, w.warehouseName " +
                "FROM ImportReceipt ir " +
                "JOIN Supplier s ON ir.supplierID = s.supplierID " +
                "JOIN Employee e ON ir.employeeID = e.employeeID " +
                "JOIN Warehouse w ON ir.warehouseID = w.warehouseID " +
                "ORDER BY ir.importDate DESC";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                ImportReceipt receipt = mapRowToImportReceipt(rs);
                receipt.setDetails(findDetailsByReceiptId(receipt.getReceiptID())); // Gọi hàm đã sửa
                receipts.add(receipt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, null); // Chỉ đóng rs và stmt
        }
        return receipts;
    }

    // Các phương thức findByDateRangeWithDetails, findBySupplierIdWithDetails, findByWarehouseIdWithDetails
    // cũng cần được sửa tương tự như findAllWithDetails và findById:
    // Lấy connection, tạo PreparedStatement, thực thi, xử lý ResultSet,
    // và đóng PreparedStatement, ResultSet trong finally. KHÔNG ĐÓNG CONNECTION.

    // Ví dụ cho findByDateRangeWithDetails:
    @Override
    public List<ImportReceipt> findByDateRangeWithDetails(LocalDate startDate, LocalDate endDate) {
        List<ImportReceipt> receipts = new ArrayList<>();
        String sql = "SELECT ir.*, s.supplierName, e.fullName as employeeFullName, w.warehouseName " +
                "FROM ImportReceipt ir " +
                "JOIN Supplier s ON ir.supplierID = s.supplierID " +
                "JOIN Employee e ON ir.employeeID = e.employeeID " +
                "JOIN Warehouse w ON ir.warehouseID = w.warehouseID " +
                "WHERE CONVERT(date, ir.importDate) BETWEEN ? AND ? " +
                "ORDER BY ir.importDate DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ImportReceipt receipt = mapRowToImportReceipt(rs);
                receipt.setDetails(findDetailsByReceiptId(receipt.getReceiptID()));
                receipts.add(receipt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null);
        }
        return receipts;
    }

    @Override
    public List<ImportReceipt> findBySupplierIdWithDetails(int supplierId) {
        return List.of();
    }

    @Override
    public List<ImportReceipt> findByWarehouseIdWithDetails(int warehouseId) {
        List<ImportReceipt> receipts = new ArrayList<>();
        String sql = "SELECT ir.*, s.supplierName, e.fullName as employeeFullName, w.warehouseName " +
                "FROM ImportReceipt ir " +
                "JOIN Supplier s ON ir.supplierID = s.supplierID " +
                "JOIN Employee e ON ir.employeeID = e.employeeID " +
                "JOIN Warehouse w ON ir.warehouseID = w.warehouseID " +
                "WHERE ir.warehouseID = ? ORDER BY ir.importDate DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, warehouseId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ImportReceipt receipt = mapRowToImportReceipt(rs);
                receipt.setDetails(findDetailsByReceiptId(receipt.getReceiptID()));
                receipts.add(receipt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null);
        }
        return receipts;
    }


    @Override
    public ImportReceiptDetail saveDetail(ImportReceiptDetail detail, Connection conn) throws SQLException {
        // Phương thức này được gọi bên trong transaction của save(ImportReceipt)
        String sqlDetail = "INSERT INTO ImportReceiptDetail (receiptID, productID, quantity, unitCost) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmtDetail = null;
        ResultSet generatedKeysDetail = null;
        try {
            // Connection conn được truyền vào, không get mới ở đây để đảm bảo transaction
            pstmtDetail = conn.prepareStatement(sqlDetail, Statement.RETURN_GENERATED_KEYS);
            pstmtDetail.setInt(1, detail.getReceiptID());
            pstmtDetail.setString(2, detail.getProductID());
            pstmtDetail.setInt(3, detail.getQuantity());
            pstmtDetail.setBigDecimal(4, detail.getUnitCost());

            int affectedRows = pstmtDetail.executeUpdate();
            if (affectedRows > 0) {
                generatedKeysDetail = pstmtDetail.getGeneratedKeys();
                if (generatedKeysDetail.next()) {
                    detail.setReceiptDetailID(generatedKeysDetail.getInt(1));
                    return detail;
                } else {
                    // Ném SQLException để transaction bên ngoài có thể rollback
                    throw new SQLException("Lưu chi tiết phiếu nhập (productID: " + detail.getProductID() + ") thành công nhưng không lấy được ID chi tiết.");
                }
            } else {
                // Ném SQLException để transaction bên ngoài có thể rollback
                throw new SQLException("Lưu chi tiết phiếu nhập (productID: " + detail.getProductID() + ") thất bại, không có dòng nào được thêm.");
            }
        } catch (SQLException e) {
            // Không bắt lỗi ở đây để cho hàm save() chính bắt và rollback
            // Chỉ in ra nếu cần debug sâu hơn trong saveDetail
            // System.err.println("SQL Error in saveDetail for productID " + detail.getProductID() + ": " + e.getMessage());
            throw e; // Ném lại exception để hàm gọi xử lý transaction
        } finally {
            // Chỉ đóng ResultSet và PreparedStatement được tạo trong hàm này
            if (generatedKeysDetail != null) try { generatedKeysDetail.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (pstmtDetail != null) try { pstmtDetail.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        // Dòng này sẽ không bao giờ đạt được nếu có lỗi hoặc thành công (vì đã return hoặc throw)
        // return null; // Chỉ để trình biên dịch không báo lỗi, nhưng logic là sẽ throw hoặc return trước đó
    }

    // Helper method để đóng resources (ResultSet, Statement/PreparedStatement)
    // Connection không được đóng ở đây nếu là static connection từ DatabaseConnection
    private void closeResources(ResultSet rs, Statement stmt, Connection connOptionalToClose) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) { e.printStackTrace(); }
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) { e.printStackTrace(); }
        // Chỉ đóng connection nếu nó được truyền vào và không phải là connection dùng chung
        // Hiện tại, với DatabaseConnection.java của bạn, chúng ta KHÔNG nên đóng conn ở đây.
        // if (connOptionalToClose != null) try { connOptionalToClose.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}