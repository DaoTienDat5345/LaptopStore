package com.model;

import java.util.Objects;

public class Products {
    private String productID;
    private String productName;
    private String categoryID;
    private String description;
    private double price;
    private double priceCost;
    private String imagePath;
    private int quantity;
    private String status;

    public Products(String productID, String productName, String categoryID, String description, double price,
            double priceCost, String imagePath, int quantity, String status) {
        super();
        this.productID = productID;
        this.productName = productName;
        this.categoryID = categoryID;
        this.description = description;
        this.price = price;
        this.priceCost = priceCost;
        this.imagePath = imagePath;
        this.quantity = quantity;
        this.status = status;
    }

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

    @Override
    public String toString() {
        return "Products [productID=" + productID + ", productName=" + productName + ", categoryID=" + categoryID
                + ", description=" + description + ", price=" + price + ", priceCost=" + priceCost + ", imagePath="
                + imagePath + ", quantity=" + quantity + ", status=" + status + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryID, description, imagePath, price, priceCost, productID, productName, quantity,
                status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Products other = (Products) obj;
        return Objects.equals(categoryID, other.categoryID) && Objects.equals(description, other.description)
                && Objects.equals(imagePath, other.imagePath)
                && Double.doubleToLongBits(price) == Double.doubleToLongBits(other.price)
                && Double.doubleToLongBits(priceCost) == Double.doubleToLongBits(other.priceCost)
                && Objects.equals(productID, other.productID) && Objects.equals(productName, other.productName)
                && quantity == other.quantity && Objects.equals(status, other.status);
    }
}