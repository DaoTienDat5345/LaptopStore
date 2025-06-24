package com.example.stores.service.impl;

import com.example.stores.repository.CategoryRepository; // Cần để kiểm tra categoryID tồn tại
import com.example.stores.repository.ProductRepository;
import com.example.stores.service.ProductService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository; // Để kiểm tra Category tồn tại

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public boolean addProduct(Product product) throws IllegalArgumentException {
        validateProductData(product);

        // Kiểm tra CategoryID có tồn tại không
        if (categoryRepository.findById(product.getCategoryID()).isEmpty()) {
            throw new IllegalArgumentException("Mã danh mục (CategoryID) '" + product.getCategoryID() + "' không tồn tại.");
        }

        product.setCreatedAt(LocalDateTime.now()); // Set thời gian tạo
        // productID sẽ được trigger tự sinh

        return productRepository.save(product);
        // Lưu ý: Sau khi save, đối tượng 'product' sẽ không có productID được cập nhật từ DB
        // vì trigger là INSTEAD OF. Nếu cần ID ngay, phải query lại.
    }

    @Override
    public boolean updateProduct(Product product) throws IllegalArgumentException {
        if (product.getProductID() == null || product.getProductID().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sản phẩm (ProductID) không được để trống khi cập nhật.");
        }
        validateProductData(product);

        if (productRepository.findById(product.getProductID()).isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + product.getProductID() + " để cập nhật.");
        }
        // Kiểm tra CategoryID có tồn tại không nếu nó được thay đổi
        if (categoryRepository.findById(product.getCategoryID()).isEmpty()) {
            throw new IllegalArgumentException("Mã danh mục (CategoryID) '" + product.getCategoryID() + "' không tồn tại.");
        }

        return productRepository.update(product);
    }

    @Override
    public boolean deleteProduct(String productId) throws IllegalArgumentException {
        if (productRepository.findById(productId).isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId + " để xóa.");
        }

        // Kiểm tra ràng buộc: sản phẩm có trong đơn hàng hoặc tồn kho không
        long references = productRepository.countReferencesInOrdersOrInventory(productId);
        if (references > 0) {
            throw new IllegalArgumentException("Không thể xóa sản phẩm '" + productId + "' vì nó đang được tham chiếu trong " +
                    references + " đơn hàng hoặc bản ghi tồn kho.");
        }

        return productRepository.deleteById(productId);
    }

    @Override
    public Optional<Product> getProductById(String productId) {
        return productRepository.findById(productId); // Repository đã JOIN để lấy categoryName
    }

    @Override
    public List<Product> getAllProductsWithCategoryName() {
        return productRepository.findAllWithCategoryName();
    }

    @Override
    public List<Product> getProductsByCategoryIdWithCategoryName(String categoryId) {
        if (categoryRepository.findById(categoryId).isEmpty()) {
            // Hoặc trả về danh sách rỗng nếu categoryId không tồn tại
            throw new IllegalArgumentException("Mã danh mục (CategoryID) '" + categoryId + "' không tồn tại.");
        }
        return productRepository.findByCategoryIdWithCategoryName(categoryId);
    }

    @Override
    public List<Product> searchProductsWithCategoryName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProductsWithCategoryName();
        }
        return productRepository.searchProductsWithCategoryName(keyword.trim());
    }

    private void validateProductData(Product product) throws IllegalArgumentException {
        if (product == null) {
            throw new IllegalArgumentException("Thông tin sản phẩm không được null.");
        }
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        }
        if (product.getCategoryID() == null || product.getCategoryID().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã danh mục (CategoryID) không được để trống.");
        }
        if (product.getPrice() < 0) { // << SỬA: So sánh double
            throw new IllegalArgumentException("Giá bán không hợp lệ (phải lớn hơn hoặc bằng 0).");
        }
        // Giá vốn
        if (product.getPriceCost() < 0) { // << SỬA: So sánh double
            throw new IllegalArgumentException("Giá vốn không hợp lệ (phải lớn hơn hoặc bằng 0).");
        }
        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ (phải lớn hơn hoặc bằng 0).");
        }
        // warrantyMonths đã bị bỏ
    }
}