package com.example.stores.repository;

import com.example.stores.model.Cart;
import com.example.stores.model.CartItem;
import java.util.List;

public interface ICartRepository {
    Cart findCartByCustomerId(int customerId);
    Cart createCart(int customerId);
    List<CartItem> getCartItems(int cartId);
    boolean removeCartItem(int cartItemId);
    boolean updateCartItemQuantity(int cartItemId, int quantity);
    boolean clearCart(int cartId);
    boolean addToCart(int cartId, String productId, int quantity);
    CartItem getCartItemById(int cartItemId);
    CartItem findCartItemByProductId(int cartId, String productId);
}