package com.example.stores.model;

public class Supplier {
    private int supplierID; // IDENTITY(1,1) PRIMARY KEY
    private String supplierName;
    private String email;
    private String phone;
    private String address;
    private String taxCode; // Mã số thuế

    public Supplier() {}

    // Getters and Setters
    public int getSupplierID() { return supplierID; }
    public void setSupplierID(int supplierID) { this.supplierID = supplierID; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }

    @Override
    public String toString() {
        // Dùng để hiển thị trong ComboBox chọn nhà cung cấp
        return supplierName != null ? supplierName : "NCC không tên (ID: " + supplierID + ")";
    }
}