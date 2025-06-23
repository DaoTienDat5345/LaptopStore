package com.example.stores.model; // Đảm bảo package đúng

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Employee {
    private int employeeID;
    private String username;
    private String password; // Sẽ lưu trữ mật khẩu đã được hash
    private String fullName;
    private String email;
    private String phone;
    private String gender;
    private LocalDate birthDate;
    private String address;
    private String imageUrl; // Đường dẫn đến file ảnh
    private String position;
    private BigDecimal salary;
    private String status; // "Đang làm", "Nghỉ việc"
    private LocalDateTime createdAt;
    private int managerID; // ID của Manager quản lý (có thể không cần thiết nếu chỉ có 1 Manager)

    // Constructors
    public Employee() {
    }

    // Getters and Setters (IntelliJ có thể tự generate)

    public int getEmployeeID() { return employeeID; }
    public void setEmployeeID(int employeeID) { this.employeeID = employeeID; }

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

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getManagerID() { return managerID; }
    public void setManagerID(int managerID) { this.managerID = managerID; }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeID=" + employeeID +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", position='" + position + '\'' +
                '}';
    }
}