package com.example.stores.repository;

import com.example.stores.model.ImportReceipt;
import com.example.stores.model.ImportReceiptDetail;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ImportReceiptRepository {
    /**
     * Lưu một phiếu nhập hàng mới (bao gồm cả các chi tiết).
     * Việc lưu chi tiết thường được thực hiện trong một transaction cùng với phiếu nhập.
     * @param importReceipt Đối tượng ImportReceipt (chưa có receiptID).
     * @return Đối tượng ImportReceipt đã được lưu với receiptID và các receiptDetailID được gán.
     *         Null nếu lỗi.
     */
    ImportReceipt save(ImportReceipt importReceipt); // Sẽ cần xử lý cả details

    // Việc update một phiếu nhập thường bị hạn chế (ví dụ: chỉ update note hoặc không cho update)
    // boolean update(ImportReceipt importReceipt);

    // Việc xóa phiếu nhập cũng cần cẩn thận (có thể phải rollback tồn kho)
    // boolean deleteById(int receiptId);

    Optional<ImportReceipt> findById(int receiptId); // Nên JOIN để lấy details và tên

    List<ImportReceipt> findAllWithDetails(); // Lấy tất cả phiếu nhập với thông tin chi tiết

    List<ImportReceipt> findByDateRangeWithDetails(LocalDate startDate, LocalDate endDate);

    List<ImportReceipt> findBySupplierIdWithDetails(int supplierId);

    List<ImportReceipt> findByWarehouseIdWithDetails(int warehouseId);

    // --- Methods for ImportReceiptDetail ---
    /**
     * Lưu một chi tiết phiếu nhập.
     * @param detail Đối tượng ImportReceiptDetail.
     * @param conn Connection hiện tại (để dùng trong transaction).
     * @return ImportReceiptDetail đã lưu với ID.
     * @throws SQLException nếu có lỗi SQL.
     */
    ImportReceiptDetail saveDetail(ImportReceiptDetail detail, Connection conn) throws SQLException;

    /**
     * Lấy tất cả chi tiết của một phiếu nhập.
     * @param receiptId ID của phiếu nhập.
     * @return List các ImportReceiptDetail.
     */
    List<ImportReceiptDetail> findDetailsByReceiptId(int receiptId);
    boolean deleteReceiptAndItsDetailsById(int receiptId);
}