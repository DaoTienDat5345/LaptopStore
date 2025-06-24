package com.example.stores.repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ImportReceiptRepository {
    ImportReceipt save(ImportReceipt importReceipt); // Sẽ cần xử lý cả details
    Optional<ImportReceipt> findById(int receiptId); // Nên JOIN để lấy details và tên
    List<ImportReceipt> findAllWithDetails(); // Lấy tất cả phiếu nhập với thông tin chi tiết
    List<ImportReceipt> findByDateRangeWithDetails(LocalDate startDate, LocalDate endDate);
    List<ImportReceipt> findBySupplierIdWithDetails(int supplierId);
    List<ImportReceipt> findByWarehouseIdWithDetails(int warehouseId);
    ImportReceiptDetail saveDetail(ImportReceiptDetail detail, Connection conn) throws SQLException;
    List<ImportReceiptDetail> findDetailsByReceiptId(int receiptId);
    boolean deleteReceiptAndItsDetailsById(int receiptId);
}