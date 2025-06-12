package com.example.stores.model;

public class Category {
    private String categoryID;
    private String categoryCode;
    private String categoryName;
    private String description;
    private int defaultWarrantyGroup; // Thêm trường này

    // Constructors
    public Category() {
    }

    public Category(String categoryID, String categoryCode, String categoryName, String description, int defaultWarrantyGroup) {
        this.categoryID = categoryID;
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.description = description;
        this.defaultWarrantyGroup = defaultWarrantyGroup;
    }


    // Getters and Setters
    public String getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
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
    public int getDefaultWarrantyGroup() {
        return defaultWarrantyGroup;
    }

    public void setDefaultWarrantyGroup(int defaultWarrantyGroup) {
        this.defaultWarrantyGroup = defaultWarrantyGroup;
    }
}