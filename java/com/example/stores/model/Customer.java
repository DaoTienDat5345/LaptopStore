package com.example.stores.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Customer {
    private int customerID; // IDENTITY(1,1) PRIMARY KEY
    private String username;
    private String password; // Sẽ được hash nếu khách hàng có chức năng login riêng
    private String fullName;
    private String email;
    private String phone;
    private String gender;
    private LocalDate birthDate;
    private String address;
    private LocalDateTime registeredAt;
    private boolean isActive; // BIT DEFAULT 1

    public Customer() {
    }

    // Getters and Setters
    public int getCustomerID() { return customerID; }
    public void setCustomerID(int customerID) { this.customerID = customerID; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    public boolean isActive() { return isActive; } // Getter cho boolean thường là isPropertyName()
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        // Dùng để hiển thị trong ComboBox chọn khách hàng
        return fullName != null ? fullName + " (ID: " + customerID + ")" : "Khách hàng không tên (ID: " + customerID + ")";
    }
}