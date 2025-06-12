package com.example.stores.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.time.LocalDateTime;

/**
 * Đại diện cho mỗi mục trong giỏ hàng
 */
public class CartItem {
    private int cartItemId;
    private int cartId;
    private Product product;
    private int quantity;
    private LocalDateTime addedAt;
    private BooleanProperty selected = new SimpleBooleanProperty(false);

    public CartItem() {
    }

    public CartItem(Product product, int quantity, boolean selected) {
        this.product = product;
        this.quantity = quantity;
        setSelected(selected);
    }

    public CartItem(int cartItemId, int cartId, Product product, int quantity, LocalDateTime addedAt, boolean selected) {
        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.product = product;
        this.quantity = quantity;
        this.addedAt = addedAt;
        setSelected(selected);
    }

    public int getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public double getSubtotal() {
        return product.getPrice() * quantity;
    }
}