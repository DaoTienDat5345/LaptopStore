package com.example.stores.model;

import java.math.BigDecimal;

public class ImportReceiptDetail {
    private int receiptDetailID; // IDENTITY(1,1) PRIMARY KEY
    private int receiptID;       // Khóa ngoại đến ImportReceipt
    private String productID;
    private int quantity;
    private double  unitCost; // Đơn giá nhập

    // Để hiển thị tên sản phẩm
    private String productNameDisplay;
    // Có thể thêm đơn vị tính nếu cần

    public ImportReceiptDetail() {}

    // Getters and Setters
    public int getReceiptDetailID() { return receiptDetailID; }
    public void setReceiptDetailID(int receiptDetailID) { this.receiptDetailID = receiptDetailID; }

    public int getReceiptID() { return receiptID; }
    public void setReceiptID(int receiptID) { this.receiptID = receiptID; }

    public String getProductID() { return productID; }
    public void setProductID(String productID) { this.productID = productID; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public String getProductNameDisplay() { return productNameDisplay; }
    public void setProductNameDisplay(String productNameDisplay) { this.productNameDisplay = productNameDisplay; }

    public double getSubtotal() { // << ĐỔI KIỂU TRẢ VỀ
        if (quantity > 0) {
            double sub = unitCost * quantity;
            return sub; // Giữ nguyên để đơn giản
        }
        return 0.0;
    }
}