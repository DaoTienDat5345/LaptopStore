package com.example.stores.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private String productID; // Sẽ được trigger của DB tự sinh
    private String productName;
    private String categoryID; // Khóa ngoại đến Categories.categoryID
    private String description;
    private double price;       // << ĐỔI THÀNH double
    private double priceCost;  // Giá vốn/nhập
    private String imagePath;
    private int quantity;
    private String status;          // Cột tính toán trong DB: "Còn hàng", "Hết hàng" (chỉ đọc)
    private LocalDateTime createdAt;

    private String categoryNameDisplay; // Để hiển thị tên danh mục

    public Product() {}

    // Getters and Setters
    public String getProductID() { return productID; }
    public void setProductID(String productID) { this.productID = productID; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategoryID() { return categoryID; }
    public void setCategoryID(String categoryID) { this.categoryID = categoryID; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

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

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Getter và Setter cho warrantyMonths đã được BỎ
    // public int getWarrantyMonths() { return warrantyMonths; }
    // public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCategoryNameDisplay() { return categoryNameDisplay; }
    public void setCategoryNameDisplay(String categoryNameDisplay) { this.categoryNameDisplay = categoryNameDisplay; }

    @Override
    public String toString() {
        return productName != null ? productName : "Sản phẩm không tên";
    }
}