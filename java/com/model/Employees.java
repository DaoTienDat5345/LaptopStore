package com.model;

import java.math.BigDecimal;

public class Employees {
    private int employeesID;
    private String position;
    private BigDecimal salary;
    private String employeesName;
    private String employeesPhone;
    private String employeesSex;
    
    public Employees() { 
    }

    public Employees(int employeesID, String position, BigDecimal salary, String employeesName, String employeesPhone, String employeesSex) {
        this.employeesID = employeesID;
        this.position = position;
        this.salary = salary;
        this.employeesName = employeesName;
        this.employeesPhone = employeesPhone;
        this.employeesSex = employeesSex;
    }
    
    public int getEmployeesID() { return employeesID; }
    public String getPosition() { return position; }
    public BigDecimal getSalary() { return salary; }
    public String getEmployeesName() { return employeesName; }
    public String getEmployeesPhone() { return employeesPhone; }
    public String getEmployeesSex() { return employeesSex; }
    
    // Setters
    public void setEmployeesID(int employeesID) { this.employeesID = employeesID; }
    public void setPosition(String position) { this.position = position; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public void setEmployeesName(String employeesName) { this.employeesName = employeesName; }
    public void setEmployeesPhone(String employeesPhone) { this.employeesPhone = employeesPhone; }
    public void setEmployeesSex(String employeesSex) { this.employeesSex = employeesSex; }

    @Override
    public String toString() {
        return "Employees [employeesID=" + employeesID + ", employeesName=" + employeesName + ", position=" + position + "]";
    }
}