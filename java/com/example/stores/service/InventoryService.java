package com.example.stores.service;

import java.util.List;
import java.util.Optional;

public interface InventoryService {
    /**
     * Thêm một bản ghi tồn kho mới. (Ít dùng trực tiếp, thường qua updateStock)
     * @param inventory Đối tượng Inventory.
     * @return Inventory đã lưu.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ.
     */
    Inventory addInventoryRecord(Inventory inventory) throws IllegalArgumentException;

    /**
     * Cập nhật thủ công số lượng tồn kho (ví dụ: sau kiểm kê).
     * @param inventoryId ID của bản ghi tồn kho.
     * @param newQuantity Số lượng mới.
     * @param notes Ghi chú cho việc điều chỉnh (nếu có).
     * @return true nếu cập nhật thành công.
     * @throws IllegalArgumentException nếu không tìm thấy bản ghi hoặc số lượng không hợp lệ.
     */
    boolean adjustStockQuantityManually(int inventoryId, int newQuantity, String notes) throws IllegalArgumentException;

    // Không cần deleteInventoryRecord trực tiếp cho Manager, việc này phức tạp

    Optional<Inventory> getInventoryRecordById(int inventoryId);

    /**
     * Lấy tồn kho của một sản phẩm cụ thể tại một kho cụ thể.
     */
    Optional<Inventory> getStockByWarehouseAndProduct(int warehouseId, String productId);

    /**
     * Lấy tất cả bản ghi tồn kho với thông tin chi tiết.
     */
    List<Inventory> getAllInventoryWithDetails();

    /**
     * Lấy tồn kho của một kho cụ thể với thông tin chi tiết sản phẩm.
     */
    List<Inventory> getInventoryByWarehouseWithDetails(int warehouseId);

    /**
     * Lấy tồn kho của một sản phẩm cụ thể trên tất cả các kho.
     */
    List<Inventory> getInventoryByProductWithDetails(String productId);

    /**
     * Cập nhật (tăng/giảm) số lượng tồn kho cho một sản phẩm trong một kho.
     * Đây là phương thức chính sẽ được gọi bởi các nghiệp vụ Nhập hàng, Bán hàng.
     * @param warehouseId ID kho.
     * @param productId ID sản phẩm.
     * @param quantityChange Số lượng thay đổi (dương để tăng, âm để giảm).
     * @return true nếu thành công.
     * @throws IllegalArgumentException nếu sản phẩm hoặc kho không tồn tại, hoặc số lượng không hợp lệ (ví dụ: cố gắng xuất nhiều hơn tồn kho).
     */
    boolean updateStock(int warehouseId, String productId, int quantityChange) throws IllegalArgumentException;
}