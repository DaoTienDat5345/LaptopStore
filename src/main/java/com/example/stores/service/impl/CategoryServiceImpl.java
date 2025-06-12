package com.example.stores.service.impl;

import com.example.stores.model.Category;
import com.example.stores.repository.CategoryRepository;
import com.example.stores.repository.ProductRepository; // Sẽ cần để kiểm tra ràng buộc khi xóa
// import com.example.stores.repository.impl.ProductRepositoryImpl; // Để khởi tạo nếu không dùng DI
import com.example.stores.service.CategoryService;

import java.util.List;
import java.util.Optional;

public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository; // Inject ProductRepository

    // Constructor Injection
    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Category addCategory(Category category) throws IllegalArgumentException {
        validateCategoryData(category);

        if (categoryRepository.findById(category.getCategoryID()).isPresent()) {
            throw new IllegalArgumentException("Mã danh mục (CategoryID) '" + category.getCategoryID() + "' đã tồn tại.");
        }
        if (categoryRepository.findByCategoryCode(category.getCategoryCode()).isPresent()) {
            throw new IllegalArgumentException("Mã code danh mục (CategoryCode) '" + category.getCategoryCode() + "' đã tồn tại.");
        }

        Category savedCategory = categoryRepository.save(category);
        if (savedCategory == null) {
            throw new RuntimeException("Không thể lưu danh mục vào CSDL.");
        }
        return savedCategory;
    }

    @Override
    public boolean updateCategory(Category category) throws IllegalArgumentException {
        validateCategoryData(category);

        Optional<Category> existingCategoryOpt = categoryRepository.findById(category.getCategoryID());
        if (existingCategoryOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy danh mục với ID: " + category.getCategoryID() + " để cập nhật.");
        }
        Category existingCategory = existingCategoryOpt.get();

        // Kiểm tra nếu categoryCode thay đổi và có bị trùng với categoryCode của danh mục khác không
        if (!existingCategory.getCategoryCode().equalsIgnoreCase(category.getCategoryCode())) {
            if (categoryRepository.findByCategoryCode(category.getCategoryCode()).isPresent()) {
                throw new IllegalArgumentException("Mã code danh mục (CategoryCode) '" + category.getCategoryCode() + "' đã được sử dụng bởi danh mục khác.");
            }
        }

        return categoryRepository.update(category);
    }

    @Override
    public boolean deleteCategory(String categoryId) throws IllegalArgumentException {
        if (categoryRepository.findById(categoryId).isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy danh mục với ID: " + categoryId + " để xóa.");
        }

        // Kiểm tra xem có sản phẩm nào đang sử dụng danh mục này không
        // Giả sử productRepository có phương thức countByCategoryId
        long productCount = productRepository.countByCategoryId(categoryId);
        if (productCount > 0) {
            throw new IllegalArgumentException("Không thể xóa danh mục '" + categoryId + "' vì đang có " + productCount + " sản phẩm thuộc danh mục này.");
        }

        return categoryRepository.deleteById(categoryId);
    }

    @Override
    public Optional<Category> getCategoryById(String categoryId) {
        return categoryRepository.findById(categoryId);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    private void validateCategoryData(Category category) throws IllegalArgumentException {
        if (category == null) {
            throw new IllegalArgumentException("Thông tin danh mục không được null.");
        }
        if (category.getCategoryID() == null || category.getCategoryID().trim().isEmpty() || category.getCategoryID().length() > 20) {
            throw new IllegalArgumentException("Mã danh mục (CategoryID) không hợp lệ (không trống, tối đa 20 ký tự).");
        }
        if (category.getCategoryCode() == null || category.getCategoryCode().trim().isEmpty() || category.getCategoryCode().trim().length() != 2) {
            throw new IllegalArgumentException("Mã code danh mục (CategoryCode) phải có đúng 2 ký tự và không được để trống.");
        }
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống.");
        }
        if (category.getDefaultWarrantyGroup() != null && category.getDefaultWarrantyGroup().length() > 50) {
            throw new IllegalArgumentException("Nhóm bảo hành mặc định không được quá 50 ký tự.");
        }
    }
}