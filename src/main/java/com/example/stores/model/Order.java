package com.example.stores.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderId;
    private LocalDateTime orderDate;
    private double totalAmount;
    private int customerId;
    private Integer employeeId; // Có thể null
    private String orderStatus;
    private String paymentMethod;
    private String recipientName;  // Thêm field này
    private String recipientPhone;  // Thêm field này
    private String shippingAddress;
    private double shippingFee;
    private String notes;
    private List<OrderDetail> orderDetails;

    public Order() {
        this.orderDate = LocalDateTime.now();
        this.orderStatus = "Đã xác nhận";
        this.shippingFee = 30000; // Mặc định phí ship là 30,000 đồng
        this.orderDetails = new ArrayList<>();
    }

    public Order(String orderId, LocalDateTime orderDate, double totalAmount, int customerId,
                 Integer employeeId, String orderStatus, String paymentMethod,
                 String recipientName, String recipientPhone,  // Thêm vào constructor
                 String shippingAddress, double shippingFee, String notes) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.customerId = customerId;
        this.employeeId = employeeId;
        this.orderStatus = orderStatus;
        this.paymentMethod = paymentMethod;
        this.recipientName = recipientName;  // Gán giá trị
        this.recipientPhone = recipientPhone;  // Gán giá trị
        this.shippingAddress = shippingAddress;
        this.shippingFee = shippingFee;
        this.notes = notes;
        this.orderDetails = new ArrayList<>();
    }

    // Getters và setters hiện tại
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    // Thêm phương thức này hoặc sửa nếu đã có
    public void setOrderStatus(String status) {
        // Nếu truyền vào các trạng thái đặc biệt, tự động chuyển đổi sang trạng thái được phép
        if (status != null) {
            if (status.equals("PAID") || status.equals("Đã thanh toán")) {
                this.orderStatus = "Đã xác nhận";
            } else if (status.equals("CANCELLED") || status.equals("Đã hủy")) {
                this.orderStatus = "Đã hủy";
            } else if (status.equals("Đã xác nhận") || status.equals("Đã hủy")) {
                // Nếu là các trạng thái đúng theo ràng buộc, giữ nguyên
                this.orderStatus = status;
            } else {
                // Mặc định là đã xác nhận
                this.orderStatus = "Đã xác nhận";
            }
            System.out.println("DEBUG: Order status set to: " + this.orderStatus);
        }
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // Thêm getter và setter cho recipientName
    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    // Thêm getter và setter cho recipientPhone
    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        // Sửa lại để đỡ strict hơn
        if (recipientPhone != null && !recipientPhone.isEmpty()) {
            this.recipientPhone = recipientPhone;
        } else {
            System.out.println("WARNING: Empty phone number provided");
        }
    }

    // Thêm phương thức setDirectPhone không kiểm tra để sử dụng trong trường hợp cần


    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(double shippingFee) {
        if (shippingFee >= 0) {
            this.shippingFee = shippingFee;
        } else {
            this.shippingFee = 0;
        }
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public void addOrderDetail(OrderDetail detail) {
        if (this.orderDetails == null) {
            this.orderDetails = new ArrayList<>();
        }
        this.orderDetails.add(detail);
    }

    // Tính toán tổng tiền dựa vào các OrderDetail
    public double calculateTotal() {
        double total = 0;
        if (orderDetails != null) {
            for (OrderDetail detail : orderDetails) {
                total += detail.getSubtotal();
            }
        }
        // Cộng thêm phí vận chuyển
        total += this.shippingFee;
        return total;
    }

    // Cập nhật tổng tiền
    public void updateTotalAmount() {
        this.totalAmount = calculateTotal();
    }
    /**
     * Thiết lập số điện thoại trực tiếp mà không qua validation
     * @param phone Số điện thoại cần thiết lập
     */
    public void setDirectPhone(String phone) {
        this.recipientPhone = phone;
        System.out.println("DEBUG: Direct phone assignment: " + phone);
    }
}