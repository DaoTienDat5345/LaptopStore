package com.example.stores.model;

// import java.math.BigDecimal; // Bỏ
import java.time.LocalDate;

public class OrderDetail {
    private String orderDetailsID;
    private String orderID;
    private String productID;
    private int quantity;
    private double unitPrice; // << ĐỔI THÀNH double (Giá gốc sản phẩm lúc mua)
    private String warrantyType;
    private double warrantyPrice; // << ĐỔI THÀNH double
    private double subtotal;      // << ĐỔI THÀNH double (Sẽ được CSDL tính, hoặc Java tính rồi set)
    private LocalDate warrantyStartDate;
    private LocalDate warrantyEndDate;
    private String note;
    private String productNameDisplay;
    // private double unitPriceAtPurchase; // Có thể gộp vào unitPrice

    public OrderDetail() {}

    // Getters and Setters
    public String getOrderDetailsID() { return orderDetailsID; }
    public void setOrderDetailsID(String orderDetailsID) { this.orderDetailsID = orderDetailsID; }
    // ...
    public double getUnitPrice() { return unitPrice; } // << ĐỔI
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; } // << ĐỔI

    public double getWarrantyPrice() { return warrantyPrice; } // << ĐỔI
    public void setWarrantyPrice(double warrantyPrice) { this.warrantyPrice = warrantyPrice; } // << ĐỔI

    public double getSubtotal() { // << ĐỔI, CSDL đã là cột tính toán
        return subtotal;
    }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; } // Setter này có thể không cần nếu CSDL tự tính và là persisted


    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getWarrantyType() { return warrantyType; }
    public void setWarrantyType(String warrantyType) { this.warrantyType = warrantyType; }
    public LocalDate getWarrantyStartDate() { return warrantyStartDate; }
    public void setWarrantyStartDate(LocalDate warrantyStartDate) { this.warrantyStartDate = warrantyStartDate; }
    public LocalDate getWarrantyEndDate() { return warrantyEndDate; }
    public void setWarrantyEndDate(LocalDate warrantyEndDate) { this.warrantyEndDate = warrantyEndDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getProductID() { return productID; }
    public void setProductID(String productID) { this.productID = productID; }
    public String getOrderID() { return orderID; }
    public void setOrderID(String orderID) { this.orderID = orderID; }
    public String getProductNameDisplay() { return productNameDisplay; }
    public void setProductNameDisplay(String productNameDisplay) { this.productNameDisplay = productNameDisplay; }

}