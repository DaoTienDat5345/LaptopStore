package com.example.stores.model;

public class Category {
    private String categoryID;
    private String categoryCode;
    private String categoryName;
    private String description;
    private String defaultWarrantyGroup; // << THÊM TRƯỜNG MỚI

    public Category() {}

    // Constructor có thể cần cập nhật nếu bạn dùng nó nhiều
    public Category(String categoryID, String categoryCode, String categoryName, String description, String defaultWarrantyGroup) {
        this.categoryID = categoryID;
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.description = description;
        this.defaultWarrantyGroup = defaultWarrantyGroup; // << THÊM VÀO CONSTRUCTOR
    }

    // Getters and Setters
    public String getCategoryID() { return categoryID; }
    public void setCategoryID(String categoryID) { this.categoryID = categoryID; }

    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDefaultWarrantyGroup() { return defaultWarrantyGroup; } // << GETTER MỚI
    public void setDefaultWarrantyGroup(String defaultWarrantyGroup) { this.defaultWarrantyGroup = defaultWarrantyGroup; } // << SETTER MỚI

    @Override
    public String toString() {
        return categoryName != null ? categoryName : categoryID;
    }
}