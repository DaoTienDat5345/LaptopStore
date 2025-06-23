package com.example.stores.service.impl;

import com.example.stores.model.*;
import com.example.stores.repository.impl.OrderRepository;
import com.example.stores.repository.impl.ProductRepository;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService() {
        this.orderRepository = new OrderRepository();
        this.productRepository = new ProductRepository();
    }



    public Order createOrder(Order order) {
        // Kiểm tra số điện thoại một lần nữa
        if (order.getRecipientPhone() == null || order.getRecipientPhone().isEmpty()) {
            System.err.println("WARNING: Phone is null or empty, attempting to fix");
            // Thử thiết lập lại
            try {
                Field phoneField = Order.class.getDeclaredField("recipientPhone");
                phoneField.setAccessible(true);
                String phone = (String) phoneField.get(order);

                if (phone == null || phone.isEmpty()) {
                    // Nếu vẫn null, đặt giá trị mặc định
                    phoneField.set(order, "0398764627");
                    System.out.println("DEBUG: Fixed null phone with default value");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Thử dùng phương thức mới có xử lý cố định SĐT
        Order result = createOrderWithFixedPhone(order);

        // Nếu phương thức mới thất bại, quay lại phương thức cũ
        if (result == null) {
            System.out.println("DEBUG: Falling back to regular order creation method");
            result = orderRepository.createOrder(order);
        }

        return result;
    }

    public Order createOrderWithFixedPhone(Order order) {
        // In thông tin để debug
        System.out.println("DEBUG SERVICE - Creating order with phone fix:");
        System.out.println("  - Phone: " + order.getRecipientPhone());
        System.out.println("  - Name: " + order.getRecipientName());

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            // THAY ĐỔI QUAN TRỌNG: Sử dụng dấu ? thay vì hard-code giá trị vào SQL
            String sql = "INSERT INTO Orders (orderDate, totalAmount, customerID, " +
                    "employeeID, orderStatus, paymentMethod, recipientName, recipientPhone, " +
                    "shippingAddress, shippingFee, notes) VALUES " +
                    "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setTimestamp(1, Timestamp.valueOf(order.getOrderDate()));
            stmt.setDouble(2, order.getTotalAmount());
            stmt.setInt(3, order.getCustomerId());

            if (order.getEmployeeId() != null) {
                stmt.setInt(4, order.getEmployeeId());
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }

            stmt.setString(5, order.getOrderStatus());
            stmt.setString(6, order.getPaymentMethod());
            stmt.setString(7, order.getRecipientName());

            // QUAN TRỌNG: Thiết lập trực tiếp giá trị phone
            String phone = order.getRecipientPhone();
            if (phone == null || phone.isEmpty()) {
                phone = "0398764627"; // Giá trị mặc định nếu rỗng
            }
            System.out.println("DEBUG SERVICE - Setting phone parameter to: '" + phone + "'");
            stmt.setString(8, phone);  // Giá trị recipientPhone

            stmt.setString(9, order.getShippingAddress());
            stmt.setDouble(10, order.getShippingFee());

            if (order.getNotes() != null) {
                stmt.setString(11, order.getNotes());
            } else {
                stmt.setNull(11, java.sql.Types.NVARCHAR);
            }

            System.out.println("DEBUG SERVICE - Executing SQL with phone parameter");

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("DEBUG SERVICE - Insert successful");

                // Lấy ID được tạo
                String getGeneratedIdSQL = "SELECT TOP 1 orderID FROM Orders " +
                        "WHERE customerID = ? ORDER BY orderDate DESC";

                try (PreparedStatement getIdStmt = conn.prepareStatement(getGeneratedIdSQL)) {
                    getIdStmt.setInt(1, order.getCustomerId());
                    ResultSet rs = getIdStmt.executeQuery();

                    if (rs.next()) {
                        String orderId = rs.getString("orderID");
                        order.setOrderId(orderId);

                        // Gọi repository để lưu chi tiết đơn hàng
                        OrderRepository repo = new OrderRepository();
                        repo.saveOrderDetails(order);

                        return order;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR SERVICE - SQL Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }

        return null;
    }

    /**
     * Cập nhật giá bảo hành cho đơn hàng dựa trên loại sản phẩm
     * @param orderDetail Chi tiết đơn hàng cần cập nhật
     * @return Giá bảo hành mới
     */
    public double updateWarrantyPrice(OrderDetail orderDetail) {
        if (orderDetail.getProduct() == null ||
                !"Vàng".equals(orderDetail.getWarrantyType())) {
            orderDetail.setWarrantyPrice(0);
            return 0;
        }

        String categoryId = orderDetail.getProduct().getCategoryID();
        int quantity = orderDetail.getQuantity();

        // Lấy thông tin danh mục
        CategoryService categoryService = new CategoryService();
        Category category = categoryService.getCategoryById(categoryId);

        double warrantyPrice = 0;

        if (category != null) {
            // Dựa vào defaultWarrantyGroup để tính giá bảo hành
            switch (category.getDefaultWarrantyGroup()) {
                case 1: // Nhóm cao cấp
                    warrantyPrice = 1000000.0 * quantity;
                    break;
                case 2: // Nhóm thông thường
                default:
                    warrantyPrice = 500000.0 * quantity;
                    break;
            }
        }

        orderDetail.setWarrantyPrice(warrantyPrice);
        return warrantyPrice;
    }

    /**
     * Lấy đơn hàng theo ID
     * @param orderId ID của đơn hàng
     * @return Đơn hàng nếu tìm thấy, null nếu không tìm thấy
     */
    public Order getOrderById(String orderId) {
        return orderRepository.getOrderById(orderId);
    }

    /**
     * Lấy danh sách đơn hàng của một khách hàng
     * @param customerId ID của khách hàng
     * @return Danh sách đơn hàng
     */
    public List<Order> getCustomerOrders(int customerId) {
        return orderRepository.getOrdersByCustomerId(customerId);
    }

    /**
     * Hoàn tất đặt hàng và xoá các sản phẩm đã đặt khỏi giỏ hàng
     * @param order Đơn hàng đã tạo
     * @param cartService Service xử lý giỏ hàng
     * @param cart Giỏ hàng cần cập nhật
     * @param selectedItems Các sản phẩm đã chọn để đặt hàng
     * @return true nếu thành công, false nếu có lỗi
     */
    public boolean finalizeOrder(Order order, CartService cartService, Cart cart, List<CartItem> selectedItems) {
        if (order == null || order.getOrderId() == null) {
            return false;
        }

        // Xóa các sản phẩm đã đặt hàng khỏi giỏ hàng
        for (CartItem item : selectedItems) {
            cartService.removeCartItem(item.getCartItemId());
        }

        // Cập nhật số lượng sản phẩm trong kho và tăng purchaseCount
        for (OrderDetail detail : order.getOrderDetails()) {
            Product product = ProductRepository.getProductById(detail.getProductId());
            if (product != null) {
                int newQuantity = product.getQuantity() - detail.getQuantity();
                if (newQuantity < 0) newQuantity = 0;

                // Gọi phương thức mới để cập nhật cả quantity và purchaseCount
                ProductRepository.updateProductQuantity(
                        detail.getProductId(),
                        newQuantity,
                        detail.getQuantity()  // Số lượng đã mua - tăng purchaseCount
                );
            }
        }

        return true;
    }

    /**
     * Cập nhật số lượng sản phẩm trong kho
     * @param productId ID sản phẩm
     * @param newQuantity Số lượng mới
     * @return true nếu thành công, false nếu có lỗi
     */
    private boolean updateProductQuantity(String productId, int newQuantity) {
        // Gọi đến repository để cập nhật số lượng sản phẩm
        return ProductRepository.updateProductQuantity(productId, newQuantity);
    }

    public boolean updateOrderStatus(String orderId, String status) {
        return orderRepository.updateOrderStatus(orderId, status);
    }
    /**
     * Lấy chi tiết đơn hàng theo ID đơn hàng
     * @param orderId ID của đơn hàng cần lấy chi tiết
     * @return Danh sách chi tiết đơn hàng, hoặc null nếu có lỗi
     */
    public List<OrderDetail> getOrderDetails(String orderId) {
        List<OrderDetail> details = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Sửa query để lấy đầy đủ thông tin
            String sql = "SELECT od.*, p.productName, p.imagePath, p.price, p.categoryID " +
                    "FROM OrderDetails od " +
                    "LEFT JOIN Products p ON od.productID = p.productID " +
                    "WHERE od.orderID = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, orderId);
            rs = stmt.executeQuery();

            System.out.println("DEBUG: Thực hiện query chi tiết đơn hàng cho orderID = " + orderId);

            while (rs.next()) {
                OrderDetail detail = new OrderDetail();
                detail.setOrderDetailsId(rs.getString("orderDetailsID"));
                detail.setOrderId(rs.getString("orderID"));
                detail.setProductId(rs.getString("productID"));
                detail.setQuantity(rs.getInt("quantity"));
                detail.setUnitPrice(rs.getDouble("unitPrice"));
                detail.setWarrantyType(rs.getString("warrantyType"));
                detail.setWarrantyPrice(rs.getDouble("warrantyPrice"));

                // Tạo đối tượng Product đầy đủ
                Product product = new Product();
                product.setProductID(detail.getProductId());
                product.setProductName(rs.getString("productName"));
                product.setImagePath(rs.getString("imagePath"));
                product.setPrice(rs.getDouble("unitPrice"));
                product.setCategoryID(rs.getString("categoryID"));
                detail.setProduct(product);

                // Tính subtotal
                detail.updateSubtotal();

                // Debug log
                System.out.println("DEBUG: Đã load OrderDetail: " +
                        detail.getProductId() + ", SL: " + detail.getQuantity() +
                        ", Đơn giá: " + detail.getUnitPrice() +
                        ", BH: " + detail.getWarrantyPrice() +
                        ", Thành tiền: " + detail.getSubtotal());

                details.add(detail);
            }

            System.out.println("DEBUG: Đã tìm thấy " + details.size() + " chi tiết đơn hàng");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Đóng resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return details;
    }
    /**
     * Lưu chi tiết đơn hàng với kiểm tra tính đúng đắn của dữ liệu
     * @param order Đơn hàng chứa thông tin chi tiết cần lưu
     * @return true nếu lưu thành công, false nếu thất bại
     */
    public boolean saveOrderDetailsForOrder(Order order) {
        if (order == null || order.getOrderId() == null || order.getOrderId().isEmpty()) {
            System.err.println("ERROR: Invalid order or orderID is missing");
            return false;
        }

        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            System.err.println("ERROR: No order details to save");
            return false;
        }

        // Đảm bảo tất cả OrderDetails đều có OrderId đúng
        for (OrderDetail detail : order.getOrderDetails()) {
            detail.setOrderId(order.getOrderId());
            System.out.println("DEBUG: Setting orderID for detail: " + order.getOrderId());
        }

        // Gọi repository để lưu chi tiết
        return orderRepository.saveOrderDetails(order);
    }
    // Thêm phương thức này vào class OrderService

    public boolean cancelOrder(String orderId) {
        System.out.println("DEBUG: Bắt đầu hủy đơn hàng: " + orderId);
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Kiểm tra trạng thái đơn hàng trước khi hủy
            String checkStatusSql = "SELECT orderStatus FROM Orders WHERE orderID = ?";
            stmt = conn.prepareStatement(checkStatusSql);
            stmt.setString(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String currentStatus = rs.getString("orderStatus");
                System.out.println("DEBUG: Trạng thái đơn hàng hiện tại: " + currentStatus);

                if (!"Đã xác nhận".equals(currentStatus)) {
                    System.out.println("DEBUG: Đơn hàng không ở trạng thái 'Đã xác nhận', không thể hủy");
                    return false;
                }
            } else {
                System.out.println("DEBUG: Không tìm thấy đơn hàng với ID: " + orderId);
                return false;
            }

            rs.close();
            stmt.close();

            // 2. Lấy chi tiết đơn hàng TRƯỚC KHI cập nhật trạng thái
            List<OrderDetail> orderDetails = getOrderDetails(orderId);
            System.out.println("DEBUG: Đã lấy " + orderDetails.size() + " chi tiết đơn hàng");

            // 3. Cập nhật trạng thái đơn hàng
            String updateOrderSql = "UPDATE Orders SET orderStatus = N'Đã hủy' WHERE orderID = ?";
            stmt = conn.prepareStatement(updateOrderSql);
            stmt.setString(1, orderId);
            int orderUpdated = stmt.executeUpdate();
            System.out.println("DEBUG: Số đơn hàng cập nhật: " + orderUpdated);

            if (orderUpdated > 0) {
                // 4. Khôi phục số lượng sản phẩm thủ công - SỬA LẠI Ở ĐÂY
                for (OrderDetail detail : orderDetails) {
                    // Đầu tiên, kiểm tra giá trị purchaseCount hiện tại
                    String checkSql = "SELECT purchaseCount FROM Products WHERE productID = ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                        checkStmt.setString(1, detail.getProductId());
                        ResultSet checkRs = checkStmt.executeQuery();

                        if (checkRs.next()) {
                            int currentPurchaseCount = checkRs.getInt("purchaseCount");
                            int deduction = detail.getQuantity();

                            // Đảm bảo không trừ quá giá trị hiện có
                            int newPurchaseCount = Math.max(0, currentPurchaseCount - deduction);
                            int actualDeduction = currentPurchaseCount - newPurchaseCount;

                            System.out.println("DEBUG: Sản phẩm " + detail.getProductId() +
                                    " - purchaseCount hiện tại: " + currentPurchaseCount +
                                    ", cần trừ: " + deduction +
                                    ", sẽ trừ thực tế: " + actualDeduction);

                            // Cập nhật với giá trị đã tính toán
                            String updateProductSql = "UPDATE Products SET " +
                                    "quantity = quantity + ?, " +
                                    "purchaseCount = ? " + // Đặt giá trị trực tiếp thay vì trừ đi
                                    "WHERE productID = ?";

                            try (PreparedStatement pstmt = conn.prepareStatement(updateProductSql)) {
                                pstmt.setInt(1, detail.getQuantity());
                                pstmt.setInt(2, newPurchaseCount);
                                pstmt.setString(3, detail.getProductId());

                                int updated = pstmt.executeUpdate();
                                System.out.println("DEBUG: Đã khôi phục số lượng cho sản phẩm " +
                                        detail.getProductId() + ": " + detail.getQuantity() +
                                        " đơn vị, kết quả: " + (updated > 0));
                            }
                        }
                    }
                }

                conn.commit(); // Xác nhận transaction
                System.out.println("DEBUG: Hủy đơn hàng thành công");
                return true;
            }

            System.out.println("DEBUG: Không cập nhật được đơn hàng");
            conn.rollback(); // Hủy transaction
            return false;

        } catch (SQLException e) {
            System.err.println("ERROR: Lỗi khi hủy đơn hàng: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Hủy transaction nếu có lỗi
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Kiểm tra xem đơn hàng có thể được đánh giá không
     * @param orderId ID của đơn hàng
     * @return true nếu có thể đánh giá, false nếu không
     */
    public boolean canReviewOrder(String orderId) {
        // Đơn hàng chỉ có thể đánh giá khi ở trạng thái "Đã xác nhận"
        String sql = "SELECT o.orderStatus FROM Orders o WHERE o.orderID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, orderId);
            var resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                String status = resultSet.getString("orderStatus");
                // Chỉ cho phép đánh giá khi đơn hàng đã xác nhận
                if (!"Đã xác nhận".equals(status) && !"Confirmed".equals(status)) {
                    return false;
                }
            } else {
                return false; // Không tìm thấy đơn hàng
            }

            // Kiểm tra xem tất cả các sản phẩm trong đơn hàng đã được đánh giá chưa
            ReviewService reviewService = new ReviewService();
            List<OrderDetail> details = getOrderDetails(orderId);

            // Nếu có bất kỳ sản phẩm nào chưa đánh giá thì có thể đánh giá
            for (OrderDetail detail : details) {
                if (!reviewService.isProductReviewed(detail.getOrderDetailsId())) {
                    return true;
                }
            }

            // Tất cả sản phẩm đã được đánh giá
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}