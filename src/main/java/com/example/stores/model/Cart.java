package com.example.stores.model;

import java.time.LocalDateTime;

/**
 * Đại diện cho giỏ hàng trong hệ thống
 */
public class Cart {
    private int cartId;
    private int customerId;
    private LocalDateTime createdAt;

    public Cart() {
    }

    public Cart(int cartId, int customerId, LocalDateTime createdAt) {
        this.cartId = cartId;
        this.customerId = customerId;
        this.createdAt = createdAt;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}