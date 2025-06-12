package com.example.stores.service;

import com.example.stores.model.Cart;
import com.example.stores.model.CartItem;
import com.example.stores.model.Customer;
import java.util.List;

public interface ICartService {
    Cart getOrCreateCart(Customer customer);
    List<CartItem> getCartItems(int cartId);
    boolean removeCartItem(int cartItemId);
    QuantityCheckResult updateCartItemQuantity(int cartItemId, int quantity);
    boolean updateCart(List<CartItem> items);
    boolean clearCart(int cartId);
    QuantityCheckResult addToCart(int cartId, String productId, int quantity);
    CartItem findCartItemByProductId(int cartId, String productId);

    class QuantityCheckResult {
        private boolean success;
        private String message;
        private int finalQuantity;

        public QuantityCheckResult(boolean success, String message, int finalQuantity) {
            this.success = success;
            this.message = message;
            this.finalQuantity = finalQuantity;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getFinalQuantity() { return finalQuantity; }
    }
}