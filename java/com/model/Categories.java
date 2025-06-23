package com.model;

import java.util.Objects;

public class Categories {
    private String categoryID;
    private String categoryCode;
    private String CategoryName;

    public Categories(String categoryID, String categoryCode, String categoryName) {
        super();
        this.categoryID = categoryID;
        this.categoryCode = categoryCode;
        CategoryName = categoryName;
    }

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
        return CategoryName;
    }

    public void setCategoryName(String categoryName) {
        CategoryName = categoryName;
    }

    @Override
    public String toString() {
        return "Categories [categoryID=" + categoryID + ", categoryCode=" + categoryCode + ", CategoryName="
                + CategoryName + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(CategoryName, categoryCode, categoryID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Categories other = (Categories) obj;
        return Objects.equals(CategoryName, other.CategoryName) && Objects.equals(categoryCode, other.categoryCode)
                && Objects.equals(categoryID, other.categoryID);
    }
}