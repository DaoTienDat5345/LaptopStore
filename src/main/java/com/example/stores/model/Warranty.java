// filepath: d:\HK2-nam2\Stores\src\main\java\com\example\stores\model\Warranty.java
package com.example.stores.model;

import java.util.Date;

public class Warranty {
    private int warrantyID;
    private String orderDetailsID;
    private String warrantyType;
    private Date startDate;
    private Date endDate;
    private String status;
    private String notes;

    public Warranty() {
    }

    public Warranty(int warrantyID, String orderDetailsID, String warrantyType, Date startDate, Date endDate, String status, String notes) {
        this.warrantyID = warrantyID;
        this.orderDetailsID = orderDetailsID;
        this.warrantyType = warrantyType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.notes = notes;
    }

    public int getWarrantyID() {
        return warrantyID;
    }

    public void setWarrantyID(int warrantyID) {
        this.warrantyID = warrantyID;
    }

    public String getOrderDetailsID() {
        return orderDetailsID;
    }

    public void setOrderDetailsID(String orderDetailsID) {
        this.orderDetailsID = orderDetailsID;
    }

    public String getWarrantyType() {
        return warrantyType;
    }

    public void setWarrantyType(String warrantyType) {
        this.warrantyType = warrantyType;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}