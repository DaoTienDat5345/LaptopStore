package com.model;

import java.time.LocalDate;

public class OrderDetail {
    private String orderDetailsID;
    private String orderID;
    private String productID;
    private int quantity;
    private double subtotal;
    private String warrantyType;
    private double warrantyPrice;
    private LocalDate warrantyStartDate;
    private LocalDate warrantyEndDate;
    private String note;

    public OrderDetail(String orderDetailsID, String orderID, String productID, int quantity, double subtotal,
            String warrantyType, double warrantyPrice, LocalDate warrantyStartDate, LocalDate warrantyEndDate,
            String note) {
        super();
        this.orderDetailsID = orderDetailsID;
        this.orderID = orderID;
        this.productID = productID;
        this.quantity = quantity;
        this.subtotal = subtotal;
        this.warrantyType = warrantyType;
        this.warrantyPrice = warrantyPrice;
        this.warrantyStartDate = warrantyStartDate;
        this.warrantyEndDate = warrantyEndDate;
        this.note = note;
    }


	public String getOrderDetailsID() {
        return orderDetailsID;
    }

    public void setOrderDetailsID(String orderDetailsID) {
        this.orderDetailsID = orderDetailsID;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public String getWarrantyType() {
        return warrantyType;
    }

    public void setWarrantyType(String warrantyType) {
        this.warrantyType = warrantyType;
    }

    public double getWarrantyPrice() {
        return warrantyPrice;
    }

    public void setWarrantyPrice(double warrantyPrice) {
        this.warrantyPrice = warrantyPrice;
    }

    public LocalDate getWarrantyStartDate() {
        return warrantyStartDate;
    }

    public void setWarrantyStartDate(LocalDate warrantyStartDate) {
        this.warrantyStartDate = warrantyStartDate;
    }

    public LocalDate getWarrantyEndDate() {
        return warrantyEndDate;
    }

    public void setWarrantyEndDate(LocalDate warrantyEndDate) {
        this.warrantyEndDate = warrantyEndDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "OrderDetail [orderDetailsID=" + orderDetailsID + ", orderID=" + orderID + ", productID=" + productID
                + ", quantity=" + quantity + ", subtotal=" + subtotal + ", warrantyType=" + warrantyType
                + ", warrantyPrice=" + warrantyPrice + ", warrantyStartDate=" + warrantyStartDate + ", warrantyEndDate="
                + warrantyEndDate + ", note=" + note + "]";
    }
}