package com.example.stores.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ImportReceipt {
    private int receiptID; // IDENTITY(1,1) PRIMARY KEY
    private int supplierID;
    private int employeeID;
    private int warehouseID;
    private LocalDateTime importDate;
    private double  totalAmount;
    private String note;

    // Để hiển thị thông tin join
    private String supplierNameDisplay;
    private String employeeNameDisplay;
    private String warehouseNameDisplay;

    // Danh sách các chi tiết phiếu nhập
    private List<ImportReceiptDetail> details = new ArrayList<>();

    public ImportReceipt() {
        this.importDate = LocalDateTime.now(); // Mặc định ngày giờ hiện tại
    }

    // Getters and Setters
    public int getReceiptID() { return receiptID; }
    public void setReceiptID(int receiptID) { this.receiptID = receiptID; }

    public int getSupplierID() { return supplierID; }
    public void setSupplierID(int supplierID) { this.supplierID = supplierID; }

    public int getEmployeeID() { return employeeID; }
    public void setEmployeeID(int employeeID) { this.employeeID = employeeID; }

    public int getWarehouseID() { return warehouseID; }
    public void setWarehouseID(int warehouseID) { this.warehouseID = warehouseID; }

    public LocalDateTime getImportDate() { return importDate; }
    public void setImportDate(LocalDateTime importDate) { this.importDate = importDate; }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getSupplierNameDisplay() { return supplierNameDisplay; }
    public void setSupplierNameDisplay(String supplierNameDisplay) { this.supplierNameDisplay = supplierNameDisplay; }

    public String getEmployeeNameDisplay() { return employeeNameDisplay; }
    public void setEmployeeNameDisplay(String employeeNameDisplay) { this.employeeNameDisplay = employeeNameDisplay; }

    public String getWarehouseNameDisplay() { return warehouseNameDisplay; }
    public void setWarehouseNameDisplay(String warehouseNameDisplay) { this.warehouseNameDisplay = warehouseNameDisplay; }

    public List<ImportReceiptDetail> getDetails() { return details; }
    public void setDetails(List<ImportReceiptDetail> details) { this.details = details; }
    public void addDetail(ImportReceiptDetail detail) { this.details.add(detail); }
}