package com.example.stores.service.impl;

import com.example.stores.model.Order;
import com.example.stores.model.OrderDetail; // Cần cho việc lấy chi tiết
import com.example.stores.repository.CustomerRepository; // Cần để kiểm tra Customer nếu lọc
import com.example.stores.repository.OrderRepository;
import com.example.stores.service.OrderDetailService; // Cần để lấy chi tiết
import com.example.stores.service.OrderService;
import com.example.stores.service.ProductReviewService;
import com.example.stores.dto.EmployeeSalesReportItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailService orderDetailService;
    private final ProductReviewService productReviewService;
    private final CustomerRepository customerRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderDetailService orderDetailService,
                            ProductReviewService productReviewService,
                            CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailService = orderDetailService;
        this.productReviewService = productReviewService;
        this.customerRepository = customerRepository;
    }

    // ĐÃ XÓA updateOrderStatus()

    @Override
    public Optional<Order> getOrderByIdWithDetails(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID không được để trống.");
        }
        Optional<Order> orderOpt = orderRepository.findByIdWithCustomerAndEmployee(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            List<OrderDetail> details = orderDetailService.getOrderDetailsByOrderId(orderId);
            order.setDetails(details);
            productReviewService.getFirstRatingByOrderId(orderId).ifPresent(order::setRepresentativeOrderRating);
            return Optional.of(order);
        }
        return Optional.empty();
    }

    @Override
    public List<Order> getAllOrdersWithDetails() {
        List<Order> orders = orderRepository.findAllWithCustomerAndEmployee();
        for (Order order : orders) {
            List<OrderDetail> details = orderDetailService.getOrderDetailsByOrderId(order.getOrderID());
            order.setDetails(details);
            productReviewService.getFirstRatingByOrderId(order.getOrderID()).ifPresent(order::setRepresentativeOrderRating);
        }
        return orders;
    }

    @Override
    public List<Order> findOrdersByCriteria(LocalDate startDate, LocalDate endDate, Integer customerId) { // Bỏ status
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc.");
        }
        if (customerId != null && customerId > 0) {
            if (customerRepository.findById(customerId).isEmpty()){ // Giả sử CustomerRepository có findById
                throw new IllegalArgumentException("Khách hàng với ID " + customerId + " không tồn tại.");
            }
        }

        List<Order> orders = orderRepository.findByCriteriaWithCustomerAndEmployee(startDate, endDate, customerId /* Bỏ status */);
        for (Order order : orders) {
            List<OrderDetail> details = orderDetailService.getOrderDetailsByOrderId(order.getOrderID());
            order.setDetails(details);
            productReviewService.getFirstRatingByOrderId(order.getOrderID()).ifPresent(order::setRepresentativeOrderRating);
        }
        return orders;
    }
    @Override
    public boolean updateOrderStatus(String orderId, String newStatus) throws IllegalArgumentException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn hàng không được để trống.");
        }
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái mới không được để trống.");
        }
        // Kiểm tra xem newStatus có hợp lệ không (dựa trên CHECK constraint của CSDL)
        List<String> validStatuses = List.of("Chờ xác nhận", "Đang xử lý", "Đang giao", "Hoàn thành", "Đã hủy"); // Lấy từ CSDL
        if (!validStatuses.contains(newStatus)) {
            throw new IllegalArgumentException("Trạng thái '" + newStatus + "' không hợp lệ.");
        }

        // Kiểm tra đơn hàng có tồn tại không
        Optional<Order> orderOpt = orderRepository.findByIdWithCustomerAndEmployee(orderId); // Dùng findById để lấy cả thông tin khác nếu cần
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId);
        }

        // Thực hiện cập nhật status
        boolean success = orderRepository.updateStatus(orderId, newStatus);

        // Logic nghiệp vụ thêm (nếu có):
        // Ví dụ: nếu chuyển sang "Hoàn thành", có thể cần cập nhật ngày hoàn thành, gửi email, v.v.
        // Nếu chuyển sang "Đã hủy", có thể cần hoàn lại số lượng sản phẩm vào tồn kho (phức tạp hơn)
        if (success && "Đã hủy".equalsIgnoreCase(newStatus)) {
            // TODO: Implement logic to restock items if an order is cancelled
            // Cần lấy OrderDetails của đơn hàng này, lặp qua từng sản phẩm và gọi inventoryService.updateStock()
            // với số lượng dương để cộng lại vào tồn kho.
            System.out.println("Đơn hàng " + orderId + " đã bị hủy. Cần xem xét việc hoàn lại hàng vào kho.");
        } else if (success && "Hoàn thành".equalsIgnoreCase(newStatus)) {
            // Logic khi đơn hàng hoàn thành
            System.out.println("Đơn hàng " + orderId + " đã hoàn thành.");
        }
        return success;
    }
    @Override
    public List<EmployeeSalesReportItem> getEmployeeSalesReport(LocalDate startDate, LocalDate endDate) throws IllegalArgumentException {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc cho báo cáo.");
        }

        return orderRepository.getEmployeeSalesReport(startDate, endDate);
    }
    @Override
    public List<Map<String, Object>> getOrderRatingStatistics() {
        // Biểu đồ sẽ thống kê từ ProductReview
        return productReviewService.getReviewRatingStatistics();
    }
}