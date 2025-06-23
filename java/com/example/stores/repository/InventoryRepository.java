package com.example.stores.repository;

import com.example.stores.model.Inventory;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository {
    /**
     * Lưu một bản ghi tồn kho mới (thường khi sản phẩm lần đầu được nhập vào một kho).
     * @param inventory Đối tượng Inventory.
     * @return Đối tượng Inventory đã lưu với ID. Null nếu lỗi.
     */
    Inventory save(Inventory inventory);

    /**
     * Cập nhật số lượng tồn kho cho một sản phẩm trong một kho.
     * @param inventory Đối tượng Inventory với số lượng mới.
     * @return true nếu thành công.
     */
    boolean update(Inventory inventory); // Chủ yếu là update quantity và lastUpdate

    /**
     * Xóa một bản ghi tồn kho (ít khi dùng, trừ khi muốn xóa sạch sản phẩm khỏi kho).
     * @param inventoryId ID của bản ghi tồn kho.
     * @return true nếu thành công.
     */
    boolean deleteById(int inventoryId);

    /**
     * Tìm bản ghi tồn kho bằng ID của nó.
     * @param inventoryId ID bản ghi tồn kho.
     * @return Optional chứa Inventory.
     */
    Optional<Inventory> findById(int inventoryId);

    /**
     * Tìm bản ghi tồn kho dựa trên warehouseID và productID (UNIQUE constraint).
     * @param warehouseId ID kho.
     * @param productId ID sản phẩm.
     * @return Optional chứa Inventory.
     */
    Optional<Inventory> findByWarehouseIdAndProductId(int warehouseId, String productId);

    /**
     * Lấy tất cả bản ghi tồn kho, kèm theo tên kho và tên sản phẩm.
     * @return List các Inventory.
     */
    List<Inventory> findAllWithDetails();

    /**
     * Lấy tất cả bản ghi tồn kho của một kho cụ thể, kèm tên sản phẩm.
     * @param warehouseId ID của kho.
     * @return List các Inventory.
     */
    List<Inventory> findByWarehouseIdWithDetails(int warehouseId);

    /**
     * Lấy tất cả bản ghi tồn kho của một sản phẩm cụ thể, trên tất cả các kho, kèm tên kho.
     * @param productId ID của sản phẩm.
     * @return List các Inventory.
     */
    List<Inventory> findByProductIdWithDetails(String productId);

    /**
     * Cập nhật (tăng/giảm) số lượng tồn kho cho một sản phẩm trong một kho.
     * Nếu bản ghi tồn kho chưa có, sẽ tạo mới.
     * @param warehouseId ID kho.
     * @param productId ID sản phẩm.
     * @param quantityChange Số lượng thay đổi (dương để tăng, âm để giảm).
     * @return true nếu cập nhật/tạo mới thành công.
     */
    boolean updateStockQuantity(int warehouseId, String productId, int quantityChange);
}