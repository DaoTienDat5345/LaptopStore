package com.example.stores.repository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    /**
     * Lưu một sản phẩm mới. productID sẽ được CSDL tự sinh qua trigger.
     * @param product Đối tượng Product (không cần set productID).
     * @return true nếu lưu thành công, false nếu thất bại.
     *         (Hoặc có thể trả về Product đã lưu nếu có cách lấy lại ID dễ dàng sau trigger)
     */
    boolean save(Product product);

    /**
     * Cập nhật thông tin sản phẩm.
     * @param product Đối tượng Product với thông tin đã cập nhật.
     * @return true nếu cập nhật thành công.
     */
    boolean update(Product product);

    /**
     * Xóa sản phẩm bằng productID.
     * @param productId ID của sản phẩm.
     * @return true nếu xóa thành công.
     */
    boolean deleteById(String productId);

    /**
     * Tìm sản phẩm bằng productID.
     * @param productId ID của sản phẩm.
     * @return Optional chứa Product (có thể kèm categoryNameDisplay nếu JOIN).
     */
    Optional<Product> findById(String productId);

    /**
     * Lấy tất cả sản phẩm, kèm theo tên danh mục.
     * @return List các Product.
     */
    List<Product> findAllWithCategoryName();

    /**
     * Lấy các sản phẩm thuộc một danh mục cụ thể, kèm tên danh mục.
     * @param categoryId ID của danh mục.
     * @return List các Product.
     */
    List<Product> findByCategoryIdWithCategoryName(String categoryId);

    /**
     * Tìm kiếm sản phẩm (theo tên, mô tả), kèm tên danh mục.
     * @param keyword Từ khóa tìm kiếm.
     * @return List các Product.
     */
    List<Product> searchProductsWithCategoryName(String keyword);

    /**
     * Đếm số lượng sản phẩm thuộc một danh mục.
     * Dùng để kiểm tra ràng buộc khi xóa Category.
     * @param categoryId ID của danh mục.
     * @return Số lượng sản phẩm.
     */
    long countByCategoryId(String categoryId);

    /**
     * Đếm số lượng sản phẩm có trong chi tiết đơn hàng hoặc tồn kho.
     * Dùng để kiểm tra ràng buộc khi xóa Product.
     * @param productId ID của sản phẩm.
     * @return Số lượng tham chiếu (ví dụ: số dòng trong OrderDetails + số dòng trong Inventory).
     */
    long countReferencesInOrdersOrInventory(String productId);
}