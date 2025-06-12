package com.example.stores.model;

public class Warehouse {
    private int warehouseID; // IDENTITY(1,1) PRIMARY KEY
    private String warehouseName;
    private String address;
    private String phone;
    private Integer managerID; // Có thể null, hoặc trỏ đến Manager quản lý kho này

    // Thêm trường để hiển thị tên Manager nếu cần (lấy từ JOIN)
    private String managerFullNameDisplay;

    public Warehouse() {}

    // Getters and Setters
    public int getWarehouseID() { return warehouseID; }
    public void setWarehouseID(int warehouseID) { this.warehouseID = warehouseID; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getManagerID() { return managerID; }
    public void setManagerID(Integer managerID) { this.managerID = managerID; } // Cho phép null

    public String getManagerFullNameDisplay() { return managerFullNameDisplay; }
    public void setManagerFullNameDisplay(String managerFullNameDisplay) { this.managerFullNameDisplay = managerFullNameDisplay; }

    @Override
    public String toString() {
        // Dùng để hiển thị trong ComboBox nếu cần chọn kho
        return warehouseName != null ? warehouseName : "Kho không tên (ID: " + warehouseID + ")";
    }
}