package com.example.stores.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderID;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private int customerID;
    private Integer employeeID;

    // --- CÁC TRƯỜNG MỚI TỪ CSDL ---
    private String orderStatus;        // NVARCHAR(50)
    private String paymentMethod;      // NVARCHAR(50)
    private String shippingAddress;    // NVARCHAR(255)
    private BigDecimal shippingFee;    // DECIMAL(10,2)
    private String notes;              // NVARCHAR(MAX)
    // --- KẾT THÚC TRƯỜNG MỚI ---

    // Để hiển thị thông tin join
    private String customerFullNameDisplay;
    private String employeeFullNameDisplay;

    private Integer representativeOrderRating;

    private List<OrderDetail> details = new ArrayList<>();

    public Order() {}

    // Getters and Setters
    // Getters and Setters
    public String getOrderID() { return orderID; }
    public void setOrderID(String orderID) { this.orderID = orderID; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public int getCustomerID() { return customerID; }
    public void setCustomerID(int customerID) { this.customerID = customerID; }

    public Integer getEmployeeID() { return employeeID; }
    public void setEmployeeID(Integer employeeID) { this.employeeID = employeeID; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getCustomerFullNameDisplay() { return customerFullNameDisplay; }
    public void setCustomerFullNameDisplay(String customerFullNameDisplay) { this.customerFullNameDisplay = customerFullNameDisplay; }

    public String getEmployeeFullNameDisplay() { return employeeFullNameDisplay; }
    public void setEmployeeFullNameDisplay(String employeeFullNameDisplay) { this.employeeFullNameDisplay = employeeFullNameDisplay; }

    public Integer getRepresentativeOrderRating() { return representativeOrderRating; } // << GETTER MỚI
    public void setRepresentativeOrderRating(Integer representativeOrderRating) { this.representativeOrderRating = representativeOrderRating; } // << SETTER MỚI

    public List<OrderDetail> getDetails() { return details; }
    public void setDetails(List<OrderDetail> details) { this.details = details; }
    public void addDetail(OrderDetail detail) { this.details.add(detail); }

    @Override
    public String toString() {
        return "Order{orderID='" + orderID + "', customerID=" + customerID + ", totalAmount=" + totalAmount + '}';
    }
}