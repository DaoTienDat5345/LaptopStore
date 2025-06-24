package com.example.stores.service;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    /**
     * Thêm một danh mục mới.
     * @param category Đối tượng Category.
     * @return Category đã được lưu.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ, categoryID hoặc categoryCode đã tồn tại.
     */
    Category addCategory(Category category) throws IllegalArgumentException;

    /**
     * Cập nhật một danh mục.
     * @param category Đối tượng Category.
     * @return true nếu cập nhật thành công.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ, không tìm thấy Category, hoặc categoryCode mới bị trùng.
     */
    boolean updateCategory(Category category) throws IllegalArgumentException;

    /**
     * Xóa một danh mục.
     * Sẽ kiểm tra xem danh mục có đang được sản phẩm nào sử dụng không trước khi xóa.
     * @param categoryId ID của danh mục cần xóa.
     * @return true nếu xóa thành công.
     * @throws IllegalArgumentException nếu không tìm thấy Category hoặc danh mục đang được sử dụng.
     */
    boolean deleteCategory(String categoryId) throws IllegalArgumentException;

    Optional<Category> getCategoryById(String categoryId);

    List<Category> getAllCategories();
}