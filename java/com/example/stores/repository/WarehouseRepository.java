package com.example.stores.repository;

import com.example.stores.model.Warehouse;
import java.util.List;
import java.util.Optional;

public interface WarehouseRepository {
    /**
     * Lưu một kho hàng mới. warehouseID sẽ được CSDL tự sinh.
     * @param warehouse Đối tượng Warehouse.
     * @return Đối tượng Warehouse đã được lưu với ID được gán. Null nếu lỗi.
     */
    Warehouse save(Warehouse warehouse);

    /**
     * Cập nhật thông tin kho hàng.
     * @param warehouse Đối tượng Warehouse với thông tin cập nhật.
     * @return true nếu thành công.
     */
    boolean update(Warehouse warehouse);

    /**
     * Xóa một kho hàng bằng ID.
     * @param warehouseId ID của kho.
     * @return true nếu thành công.
     */
    boolean deleteById(int warehouseId);

    /**
     * Tìm kho hàng bằng ID.
     * @param warehouseId ID của kho.
     * @return Optional chứa Warehouse (có thể kèm managerFullNameDisplay nếu JOIN).
     */
    Optional<Warehouse> findById(int warehouseId);

    /**
     * Lấy tất cả kho hàng, kèm theo tên Manager quản lý (nếu có).
     * @return List các Warehouse.
     */
    List<Warehouse> findAllWithManagerName();

    /**
     * Tìm kiếm kho hàng theo tên hoặc địa chỉ.
     * @param keyword Từ khóa.
     * @return List các Warehouse.
     */
    List<Warehouse> searchWarehouses(String keyword);

    /**
     * Đếm số lượng bản ghi tồn kho liên quan đến một kho hàng.
     * Dùng để kiểm tra ràng buộc khi xóa Warehouse.
     * @param warehouseId ID của kho hàng.
     * @return Số lượng bản ghi trong Inventory.
     */
    long countInventoryItemsByWarehouseId(int warehouseId);
}