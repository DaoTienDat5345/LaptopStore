package com.example.stores.service;

import com.example.stores.model.Warehouse;
import java.util.List;
import java.util.Optional;

public interface WarehouseService {
    /**
     * Thêm một kho hàng mới.
     * @param warehouse Đối tượng Warehouse.
     * @return Warehouse đã được lưu với ID.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ.
     */
    Warehouse addWarehouse(Warehouse warehouse) throws IllegalArgumentException;

    /**
     * Cập nhật thông tin kho hàng.
     * @param warehouse Đối tượng Warehouse.
     * @return true nếu cập nhật thành công.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ hoặc không tìm thấy kho.
     */
    boolean updateWarehouse(Warehouse warehouse) throws IllegalArgumentException;

    /**
     * Xóa một kho hàng.
     * Sẽ kiểm tra xem kho có đang chứa hàng tồn kho không trước khi xóa.
     * @param warehouseId ID của kho.
     * @return true nếu xóa thành công.
     * @throws IllegalArgumentException nếu không tìm thấy kho hoặc kho đang có hàng.
     */
    boolean deleteWarehouse(int warehouseId) throws IllegalArgumentException;

    Optional<Warehouse> getWarehouseById(int warehouseId);

    List<Warehouse> getAllWarehousesWithManagerName();

    List<Warehouse> searchWarehouses(String keyword);
}