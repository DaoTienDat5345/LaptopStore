package com.example.stores.model;

import java.sql.Timestamp;

public class Product {
    private String productID;
    private String productName;
    private String categoryID;
    private String categoryName;  // Thêm để lưu tên danh mục
    private String description;
    private double price;
    private double priceCost;
    private String imagePath;
    private int quantity;
    private String status;
    private Timestamp createdAt;
    private int purchaseCount;  // Thêm biến đếm số lượng mua

    // Constructors
    public Product() {
    }

    public Product(String productID, String productName, String categoryID, String description,
                   double price, double priceCost, String imagePath, int quantity, String status,
                   Timestamp createdAt, int purchaseCount) {
        this.productID = productID;
        this.productName = productName;
        this.categoryID = categoryID;
        this.description = description;
        this.price = price;
        this.priceCost = priceCost;
        this.imagePath = imagePath;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
        this.purchaseCount = purchaseCount;
    }

    // Constructor cũ (giữ để tương thích với code hiện tại)
    public Product(String productID, String productName, String categoryID, String description,
                   double price, double priceCost, String imagePath, int quantity, String status,
                   Timestamp createdAt) {
        this(productID, productName, categoryID, description, price, priceCost, 
            imagePath, quantity, status, createdAt, 0); // Mặc định purchaseCount = 0
    }

    // Getters and Setters
    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPriceCost() {
        return priceCost;
    }

    public void setPriceCost(double priceCost) {
        this.priceCost = priceCost;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getPurchaseCount() {
        return purchaseCount;
    }

    public void setPurchaseCount(int purchaseCount) {
        this.purchaseCount = purchaseCount;
    }
}