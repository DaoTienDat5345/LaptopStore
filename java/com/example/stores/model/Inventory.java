package com.example.stores.model;

import java.time.LocalDateTime;

public class Inventory {
    private int inventoryID; // IDENTITY(1,1) PRIMARY KEY
    private int warehouseID;
    private String productID;
    private int quantity;
    private LocalDateTime lastUpdate;

    // Thêm các trường để hiển thị thông tin join (tên kho, tên sản phẩm)
    private String warehouseNameDisplay;
    private String productNameDisplay;
    private String productStatusDisplay; // Trạng thái của sản phẩm (Còn hàng/Hết hàng) từ bảng Products

    public Inventory() {}

    // Getters and Setters
    public int getInventoryID() { return inventoryID; }
    public void setInventoryID(int inventoryID) { this.inventoryID = inventoryID; }

    public int getWarehouseID() { return warehouseID; }
    public void setWarehouseID(int warehouseID) { this.warehouseID = warehouseID; }

    public String getProductID() { return productID; }
    public void setProductID(String productID) { this.productID = productID; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }

    public String getWarehouseNameDisplay() { return warehouseNameDisplay; }
    public void setWarehouseNameDisplay(String warehouseNameDisplay) { this.warehouseNameDisplay = warehouseNameDisplay; }

    public String getProductNameDisplay() { return productNameDisplay; }
    public void setProductNameDisplay(String productNameDisplay) { this.productNameDisplay = productNameDisplay; }

    public String getProductStatusDisplay() { return productStatusDisplay; }
    public void setProductStatusDisplay(String productStatusDisplay) { this.productStatusDisplay = productStatusDisplay; }

    @Override
    public String toString() {
        return "Inventory{" +
                "warehouseID=" + warehouseID +
                ", productID='" + productID + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}