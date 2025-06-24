package com.example.stores.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.example.stores.dto.EmployeeSalesReportItem;

public interface OrderService {
    // Không còn phương thức updateOrderStatus
    Optional<Order> getOrderByIdWithDetails(String orderId); // Lấy đơn hàng và chi tiết của nó
    List<Order> getAllOrdersWithDetails();
    List<Order> findOrdersByCriteria(LocalDate startDate, LocalDate endDate, Integer customerId); // Bỏ status

    List<Map<String, Object>> getOrderRatingStatistics();
    boolean updateOrderStatus(String orderId, String newStatus) throws IllegalArgumentException;
    List<EmployeeSalesReportItem> getEmployeeSalesReport(LocalDate startDate, LocalDate endDate) throws IllegalArgumentException;
}