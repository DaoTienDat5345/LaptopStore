package com.example.stores.dto; // Hoặc com.example.stores.model

import java.math.BigDecimal;

public class EmployeeSalesReportItem {
    private int employeeID;
    private String employeeFullName;
    private long totalProductsSold; // Tổng số lượng sản phẩm bán được
    private double totalRevenue;    // Tổng doanh thu

    public EmployeeSalesReportItem() {
    }

    public EmployeeSalesReportItem(int employeeID, String employeeFullName, long totalProductsSold, double totalRevenue) {
        this.employeeID = employeeID;
        this.employeeFullName = employeeFullName;
        this.totalProductsSold = totalProductsSold;
        this.totalRevenue = totalRevenue;
    }

    // Getters and Setters
    public int getEmployeeID() { return employeeID; }
    public void setEmployeeID(int employeeID) { this.employeeID = employeeID; }

    public String getEmployeeFullName() { return employeeFullName; }
    public void setEmployeeFullName(String employeeFullName) { this.employeeFullName = employeeFullName; }

    public long getTotalProductsSold() { return totalProductsSold; }
    public void setTotalProductsSold(long totalProductsSold) { this.totalProductsSold = totalProductsSold; }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    @Override
    public String toString() {
        return "EmployeeSalesReportItem{" +
                "employeeFullName='" + employeeFullName + '\'' +
                ", totalProductsSold=" + totalProductsSold +
                ", totalRevenue=" + totalRevenue +
                '}';
    }
}