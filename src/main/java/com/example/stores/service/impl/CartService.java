package com.example.stores.service.impl;

import com.example.stores.repository.impl.CartRepository;
import com.example.stores.repository.impl.ProductRepository;

import java.util.List;

public class CartService {
    private CartRepository cartRepository;
    private ProductRepository productRepository; // Thêm ProductRepository để kiểm tra tồn kho

    public CartService() {
        this.cartRepository = new CartRepository();
        this.productRepository = new ProductRepository(); // Khởi tạo ProductRepository
    }

    // Lấy hoặc tạo giỏ hàng cho khách hàng
    public Cart getOrCreateCart(Customer customer) {
        if (customer == null) {
            return null;
        }

        Cart cart = cartRepository.findCartByCustomerId(customer.getId());

        if (cart == null) {
            cart = cartRepository.createCart(customer.getId());
        }

        return cart;
    }

    // Lấy tất cả sản phẩm trong giỏ hàng
    public List<CartItem> getCartItems(int cartId) {
        return cartRepository.getCartItems(cartId);
    }

    // Xóa sản phẩm khỏi giỏ hàng
    public boolean removeCartItem(int cartItemId) {
        return cartRepository.removeCartItem(cartItemId);
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng - đã cập nhật để kiểm tra tồn kho
    public QuantityCheckResult updateCartItemQuantity(int cartItemId, int quantity) {
        // Lấy thông tin CartItem hiện tại để biết productId
        CartItem currentItem = cartRepository.getCartItemById(cartItemId);
        if (currentItem == null) {
            return new QuantityCheckResult(false, "Không tìm thấy sản phẩm trong giỏ hàng", quantity);
        }

        // Lấy thông tin sản phẩm để kiểm tra tồn kho
        String productId = currentItem.getProduct().getProductID();
        Product product = ProductRepository.getProductById(productId);
        if (product == null) {
            return new QuantityCheckResult(false, "Không tìm thấy thông tin sản phẩm", quantity);
        }

        // Kiểm tra số lượng tồn kho
        int availableStock = product.getQuantity();
        if (quantity > availableStock) {
            // Nếu yêu cầu vượt quá tồn kho, giới hạn lại ở mức tồn kho tối đa
            return new QuantityCheckResult(false, 
                "Sản phẩm " + product.getProductName() + " chỉ có tối đa " + availableStock + " sản phẩm", 
                availableStock);
        }

        // Nếu số lượng hợp lệ, cập nhật
        boolean updated = cartRepository.updateCartItemQuantity(cartItemId, quantity);
        return new QuantityCheckResult(updated, "", quantity);
    }

    // Cập nhật toàn bộ giỏ hàng
    public boolean updateCart(List<CartItem> items) {
        boolean success = true;

        for (CartItem item : items) {
            QuantityCheckResult result = updateCartItemQuantity(item.getCartItemId(), item.getQuantity());
            if (!result.isSuccess()) {
                success = false;
            }
        }

        return success;
    }

    // Xóa tất cả sản phẩm trong giỏ hàng
    public boolean clearCart(int cartId) {
        return cartRepository.clearCart(cartId);
    }

    // Thêm sản phẩm vào giỏ hàng - đã cập nhật để kiểm tra tồn kho
    public QuantityCheckResult addToCart(int cartId, String productId, int quantity) {
        if (quantity <= 0) {
            return new QuantityCheckResult(false, "Số lượng phải lớn hơn 0", 0);
        }

        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
        CartItem existingItem = cartRepository.findCartItemByProductId(cartId, productId);
        
        // Lấy thông tin sản phẩm để kiểm tra tồn kho
        Product product = ProductRepository.getProductById(productId);
        if (product == null) {
            return new QuantityCheckResult(false, "Không tìm thấy thông tin sản phẩm", 0);
        }

        int availableStock = product.getQuantity();
        int currentCartQuantity = (existingItem != null) ? existingItem.getQuantity() : 0;
        int newTotalQuantity = currentCartQuantity + quantity;

        // Kiểm tra nếu tổng số lượng mới vượt quá tồn kho
        if (newTotalQuantity > availableStock) {
            // Nếu vượt quá, giới hạn ở mức tồn kho tối đa
            int allowedToAdd = availableStock - currentCartQuantity;
            if (allowedToAdd <= 0) {
                return new QuantityCheckResult(false, 
                    "Sản phẩm " + product.getProductName() + " đã có tối đa " + availableStock + 
                    " sản phẩm trong giỏ hàng và không thể thêm nữa", 
                    currentCartQuantity);
            } else {
                // Cập nhật với số lượng tối đa có thể thêm
                boolean added = cartRepository.addToCart(cartId, productId, allowedToAdd);
                return new QuantityCheckResult(added, 
                    "Sản phẩm " + product.getProductName() + " chỉ có tối đa " + availableStock + " sản phẩm", 
                    currentCartQuantity + allowedToAdd);
            }
        }

        // Nếu số lượng hợp lệ, thêm vào giỏ hàng
        boolean added = cartRepository.addToCart(cartId, productId, quantity);
        return new QuantityCheckResult(added, "", newTotalQuantity);
    }
    
    // Lớp kết quả cho việc kiểm tra số lượng
    public static class QuantityCheckResult {
        private boolean success;
        private String message;
        private int adjustedQuantity;
        
        public QuantityCheckResult(boolean success, String message, int adjustedQuantity) {
            this.success = success;
            this.message = message;
            this.adjustedQuantity = adjustedQuantity;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public int getAdjustedQuantity() {
            return adjustedQuantity;
        }
    }
    // Phương thức tìm CartItem theo productId
    public CartItem findCartItemByProductId(int cartId, String productId) {
        return cartRepository.findCartItemByProductId(cartId, productId);
    }
}