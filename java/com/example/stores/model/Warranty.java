package com.example.stores.model;

import java.time.LocalDate;
import java.util.Date;

public class Warranty {
    private int warrantyID; // IDENTITY(1,1) PRIMARY KEY
    private String orderDetailsID; // VARCHAR(20) NOT NULL UNIQUE
    private String warrantyType;   // "Thường", "Vàng"
    private Date startDate;
    private Date endDate;
    private String status;         // Cột tính toán trong DB: "Còn hạn", "Hết hạn" (chỉ đọc)
    private String notes;          // NVARCHAR(MAX)

    public Warranty() {}

    // Getters and Setters
    public int getWarrantyID() { return warrantyID; }
    public void setWarrantyID(int warrantyID) { this.warrantyID = warrantyID; }

    public String getOrderDetailsID() { return orderDetailsID; }
    public void setOrderDetailsID(String orderDetailsID) { this.orderDetailsID = orderDetailsID; }

    public String getWarrantyType() { return warrantyType; }
    public void setWarrantyType(String warrantyType) { this.warrantyType = warrantyType; }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; } // Chỉ để map từ DB

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}