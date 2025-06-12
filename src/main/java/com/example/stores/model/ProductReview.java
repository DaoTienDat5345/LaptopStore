package com.example.stores.model;

import java.time.LocalDateTime;

public class ProductReview {
    private int reviewID;
    private String productID;
    private int customerID;
    private String orderDetailsID;
    private int rating;
    private String comment;
    private LocalDateTime reviewDate;
    private boolean isApproved;

    // Thêm thuộc tính không nằm trong bảng để tiện hiển thị
    private String customerName;

    // Constructor mặc định
    public ProductReview() {
    }

    // Constructor đầy đủ
    public ProductReview(int reviewID, String productID, int customerID, String orderDetailsID,
                         int rating, String comment, LocalDateTime reviewDate, boolean isApproved) {
        this.reviewID = reviewID;
        this.productID = productID;
        this.customerID = customerID;
        this.orderDetailsID = orderDetailsID;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
        this.isApproved = isApproved;
    }

    // Getters and Setters
    public int getReviewID() {
        return reviewID;
    }

    public void setReviewID(int reviewID) {
        this.reviewID = reviewID;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public String getOrderDetailsID() {
        return orderDetailsID;
    }

    public void setOrderDetailsID(String orderDetailsID) {
        this.orderDetailsID = orderDetailsID;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public String toString() {
        return "ProductReview{" +
                "reviewID=" + reviewID +
                ", productID='" + productID + '\'' +
                ", customerID=" + customerID +
                ", orderDetailsID='" + orderDetailsID + '\'' +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", reviewDate=" + reviewDate +
                ", isApproved=" + isApproved +
                ", customerName='" + customerName + '\'' +
                '}';
    }
}