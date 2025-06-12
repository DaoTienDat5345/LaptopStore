// Trong OrderDetail.java
package com.example.stores.model;

import java.math.BigDecimal;
// import java.time.LocalDate; // Không còn warrantyStartDate, warrantyEndDate

public class OrderDetail {
    private String orderDetailsID;
    private String orderID;
    private String productID;
    private int quantity;
    private BigDecimal unitPrice; // Giá của sản phẩm tại thời điểm mua
    private BigDecimal subtotal;  // Cột tính toán trong DB, chỉ cần getter, không cần setter
    private String warrantyType;
    private BigDecimal warrantyPrice;
    private String note;

    private String productNameDisplay;

    public OrderDetail() {}

    // Getters
    public String getOrderDetailsID() { return orderDetailsID; }
    public String getOrderID() { return orderID; }
    public String getProductID() { return productID; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; } // Getter vẫn cần để đọc giá trị từ DB
    public String getWarrantyType() { return warrantyType; }
    public BigDecimal getWarrantyPrice() { return warrantyPrice; }
    public String getNote() { return note; }
    public String getProductNameDisplay() { return productNameDisplay; }

    // Setters (Không có setSubtotal)
    public void setOrderDetailsID(String orderDetailsID) { this.orderDetailsID = orderDetailsID; }
    public void setOrderID(String orderID) { this.orderID = orderID; }
    public void setProductID(String productID) { this.productID = productID; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    // KHÔNG CÓ SETTER CHO SUBTOTAL
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public void setWarrantyType(String warrantyType) { this.warrantyType = warrantyType; }
    public void setWarrantyPrice(BigDecimal warrantyPrice) { this.warrantyPrice = warrantyPrice; }
    public void setNote(String note) { this.note = note; }
    public void setProductNameDisplay(String productNameDisplay) { this.productNameDisplay = productNameDisplay; }
}