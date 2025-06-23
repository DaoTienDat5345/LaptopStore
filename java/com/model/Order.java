package com.model;

import java.time.LocalDate;

public class Order {
    private String orderID;
    private LocalDate orderDate;
    private double totalAmount;
    private int customerID;
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;

    public Order(String orderID, LocalDate orderDate, double totalAmount, int customerID) {
        this.orderID = orderID;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.customerID = customerID;
    }

    public String getOrderID() { return orderID; }
    public void setOrderID(String orderID) { this.orderID = orderID; }
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public int getCustomerID() { return customerID; }
    public void setCustomerID(int customerID) { this.customerID = customerID; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    public String getRecipientAddress() { return recipientAddress; }
    public void setRecipientAddress(String recipientAddress) { this.recipientAddress = recipientAddress; }
}