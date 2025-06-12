package com.example.stores.model;

import com.example.stores.service.impl.CategoryService;

public class OrderDetail {
    private String orderDetailsId;  // Thay đổi từ int sang String và thêm 's'
    private String orderId;         // Thay đổi từ int sang String
    private String productId;
    private Product product;
    private int quantity;
    private double unitPrice;
    private String warrantyType;
    private double warrantyPrice;
    private double subtotal;
    private String note;           // Thêm trường note
    
    // Constructors
    public OrderDetail() {
        this.warrantyType = "Thường";
        this.warrantyPrice = 0.0;
    }
    
    public OrderDetail(String productId, int quantity, double unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.warrantyType = "Thường";
        this.warrantyPrice = 0.0;
        updateSubtotal();
    }
    
    public OrderDetail(CartItem item) {
        this.productId = item.getProduct().getProductID();
        this.product = item.getProduct();
        this.quantity = item.getQuantity();
        this.unitPrice = item.getProduct().getPrice();
        this.warrantyType = "Thường";
        this.warrantyPrice = 0.0;
        updateSubtotal();
    }
    
    // Getters and Setters
    public String getOrderDetailsId() {
        return orderDetailsId;
    }
    
    public void setOrderDetailsId(String orderDetailsId) {
        this.orderDetailsId = orderDetailsId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.productId = product.getProductID();
            this.unitPrice = product.getPrice();
            updateSubtotal();
        }
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        // Cập nhật lại giá bảo hành khi số lượng thay đổi
        updateWarrantyPrice();
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        updateSubtotal();
    }
    
    public String getWarrantyType() {
        return warrantyType;
    }

    public void setWarrantyType(String warrantyType) {
        // Đảm bảo warrantyType luôn có giá trị hợp lệ
        if (warrantyType == null || warrantyType.isEmpty()) {
            this.warrantyType = "Thường";
        } else if (warrantyType.equals("Thường") || warrantyType.equals("Vàng") || warrantyType.equals("Gold")) {
            this.warrantyType = warrantyType;
        } else {
            this.warrantyType = "Thường"; // Giá trị mặc định
            System.out.println("WARNING: Loại bảo hành không hợp lệ '" + warrantyType + "', đã đặt về 'Thường'");
        }
    }
    
    public double getWarrantyPrice() {
        return warrantyPrice;
    }

    public void setWarrantyPrice(double warrantyPrice) {
        this.warrantyPrice = warrantyPrice;
        updateSubtotal();
    }
    
    public double getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }

    // Cải thiện phương thức updateWarrantyPrice()

    public void updateWarrantyPrice() {
        // Reset giá bảo hành
        warrantyPrice = 0.0;

        // Không tính phí nếu không phải bảo hành vàng
        if (product == null || warrantyType == null ||
                !(warrantyType.equals("Vàng") || warrantyType.equals("Gold"))) {
            updateSubtotal();
            return;
        }

        // Lấy thông tin danh mục
        CategoryService categoryService = new CategoryService();
        System.out.println("ProductID: " + product.getProductID() + ", CategoryID: " + product.getCategoryID());

        if (product.getCategoryID() != null) {
            Category category = categoryService.getCategoryById(product.getCategoryID());

            if (category != null) {
                int warrantyGroup = category.getDefaultWarrantyGroup();
                System.out.println("Danh mục: " + category.getCategoryName() + ", Nhóm bảo hành: " + warrantyGroup);

                // Tính giá bảo hành theo nhóm
                switch (warrantyGroup) {
                    case 1: // Nhóm cao cấp
                        warrantyPrice = 1000000.0 * quantity;
                        break;
                    case 2: // Nhóm thông thường
                    default:
                        warrantyPrice = 500000.0 * quantity;
                        break;
                }

                System.out.println("Phí bảo hành: " + warrantyPrice);
            } else {
                // Mặc định nhóm 2 nếu không tìm thấy danh mục
                System.out.println("Không tìm thấy danh mục, áp dụng mặc định nhóm 2");
                warrantyPrice = 500000.0 * quantity;
            }
        } else {
            // Mặc định nhóm 2 nếu không có categoryID
            System.out.println("Không có CategoryID, áp dụng mặc định nhóm 2");
            warrantyPrice = 500000.0 * quantity;
        }

        updateSubtotal();
    }
    
    /**
     * Cập nhật tổng tiền sản phẩm (đơn giá × số lượng + phí bảo hành)
     */
    // Đảm bảo phương thức updateSubtotal() tính toán đúng

    public void updateSubtotal() {
        // Tính lại subtotal: giá sản phẩm * số lượng + giá bảo hành
        double productTotal = unitPrice * quantity;
        subtotal = productTotal + warrantyPrice;

        // Log để debug
        System.out.println("Tính toán thành tiền: " + productTotal + " (Giá SP * SL) + " +
                warrantyPrice + " (Phí bảo hành) = " + subtotal);
    }
}