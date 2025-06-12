package com.example.stores.service;

import com.example.stores.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    /**
     * Thêm một sản phẩm mới.
     * @param product Đối tượng Product (không cần productID).
     * @return true nếu thêm thành công.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ hoặc categoryID không tồn tại.
     */
    boolean addProduct(Product product) throws IllegalArgumentException;

    /**
     * Cập nhật thông tin sản phẩm.
     * @param product Đối tượng Product.
     * @return true nếu cập nhật thành công.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ hoặc không tìm thấy sản phẩm.
     */
    boolean updateProduct(Product product) throws IllegalArgumentException;

    /**
     * Xóa một sản phẩm.
     * Sẽ kiểm tra ràng buộc với OrderDetails, Inventory trước khi xóa.
     * @param productId ID của sản phẩm.
     * @return true nếu xóa thành công.
     * @throws IllegalArgumentException nếu không tìm thấy sản phẩm hoặc sản phẩm đang được sử dụng.
     */
    boolean deleteProduct(String productId) throws IllegalArgumentException;

    Optional<Product> getProductById(String productId);

    /**
     * Lấy tất cả sản phẩm, đã bao gồm tên danh mục.
     */
    List<Product> getAllProductsWithCategoryName();

    List<Product> getProductsByCategoryIdWithCategoryName(String categoryId);

    List<Product> searchProductsWithCategoryName(String keyword);
}