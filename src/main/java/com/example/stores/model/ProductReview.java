package com.example.stores.model;

import java.time.LocalDateTime;

public class ProductReview {
    private int reviewID; // IDENTITY(1,1) PRIMARY KEY
    private String productID;
    private int customerID;
    private String orderDetailsID; // Để liên kết với một giao dịch mua cụ thể
    private byte rating; // TINYINT (1-5)
    private String comment;
    private LocalDateTime reviewDate;
    private boolean isApproved; // BIT DEFAULT 0

    // Các trường hiển thị thêm nếu cần (tên sản phẩm, tên khách hàng)
    private String productNameDisplay;
    private String customerNameDisplay;

    public ProductReview() {}

    // Getters and Setters
    public int getReviewID() { return reviewID; }
    public void setReviewID(int reviewID) { this.reviewID = reviewID; }

    public String getProductID() { return productID; }
    public void setProductID(String productID) { this.productID = productID; }

    public int getCustomerID() { return customerID; }
    public void setCustomerID(int customerID) { this.customerID = customerID; }

    public String getOrderDetailsID() { return orderDetailsID; }
    public void setOrderDetailsID(String orderDetailsID) { this.orderDetailsID = orderDetailsID; }

    public byte getRating() { return rating; }
    public void setRating(byte rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDateTime reviewDate) { this.reviewDate = reviewDate; }

    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }

    public String getProductNameDisplay() { return productNameDisplay; }
    public void setProductNameDisplay(String productNameDisplay) { this.productNameDisplay = productNameDisplay; }

    public String getCustomerNameDisplay() { return customerNameDisplay; }
    public void setCustomerNameDisplay(String customerNameDisplay) { this.customerNameDisplay = customerNameDisplay; }
}