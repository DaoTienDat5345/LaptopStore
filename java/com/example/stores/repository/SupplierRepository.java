package com.example.stores.repository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository {
    /**
     * Lưu một nhà cung cấp mới. supplierID sẽ được CSDL tự sinh.
     * @param supplier Đối tượng Supplier.
     * @return Đối tượng Supplier đã được lưu với ID được gán. Null nếu lỗi.
     */
    Supplier save(Supplier supplier);

    /**
     * Cập nhật thông tin nhà cung cấp.
     * @param supplier Đối tượng Supplier với thông tin cập nhật.
     * @return true nếu thành công.
     */
    boolean update(Supplier supplier);

    /**
     * Xóa một nhà cung cấp bằng ID.
     * @param supplierId ID của nhà cung cấp.
     * @return true nếu thành công.
     */
    boolean deleteById(int supplierId);

    /**
     * Tìm nhà cung cấp bằng ID.
     * @param supplierId ID của nhà cung cấp.
     * @return Optional chứa Supplier.
     */
    Optional<Supplier> findById(int supplierId);

    /**
     * Lấy tất cả nhà cung cấp.
     * @return List các Supplier.
     */
    List<Supplier> findAll();

    /**
     * Tìm kiếm nhà cung cấp theo tên, email, hoặc SĐT.
     * @param keyword Từ khóa.
     * @return List các Supplier.
     */
    List<Supplier> searchSuppliers(String keyword);

    /**
     * Đếm số lượng phiếu nhập hàng liên quan đến một nhà cung cấp.
     * Dùng để kiểm tra ràng buộc khi xóa Supplier.
     * @param supplierId ID của nhà cung cấp.
     * @return Số lượng phiếu nhập.
     */
    long countImportReceiptsBySupplierId(int supplierId);

    // Có thể thêm các phương thức tìm theo email, phone nếu cần để kiểm tra UNIQUE ở Service
    Optional<Supplier> findByEmail(String email);
    Optional<Supplier> findByPhone(String phone);
}