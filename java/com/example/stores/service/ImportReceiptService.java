package com.example.stores.service;

import com.example.stores.model.ImportReceipt;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ImportReceiptService {

    /**
     * Tạo một phiếu nhập hàng mới.
     * Bao gồm việc tính toán totalAmount, lưu phiếu và chi tiết phiếu,
     * và cập nhật số lượng tồn kho cho các sản phẩm được nhập.
     * @param importReceipt Đối tượng ImportReceipt chứa thông tin phiếu và danh sách chi tiết.
     *                      totalAmount có thể chưa được tính.
     * @return ImportReceipt đã được lưu với ID và totalAmount đã tính.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ (ví dụ: NCC, NV, Kho không tồn tại,
     *                                  sản phẩm trong chi tiết không tồn tại, số lượng/đơn giá không hợp lệ).
     * @throws RuntimeException nếu có lỗi hệ thống khi lưu hoặc cập nhật tồn kho.
     */
    ImportReceipt createImportReceipt(ImportReceipt importReceipt) throws IllegalArgumentException, RuntimeException;

    Optional<ImportReceipt> getImportReceiptById(int receiptId);

    List<ImportReceipt> getAllImportReceiptsWithDetails();

    List<ImportReceipt> getImportReceiptsByDateRangeWithDetails(LocalDate startDate, LocalDate endDate);

    List<ImportReceipt> getImportReceiptsBySupplierIdWithDetails(int supplierId);

    List<ImportReceipt> getImportReceiptsByWarehouseIdWithDetails(int warehouseId);

    boolean deleteSingleImportReceipt(int receiptId) throws RuntimeException;

    // Các nghiệp vụ khác có thể thêm sau (ví dụ: hủy phiếu nhập - cần xử lý rollback tồn kho)
}