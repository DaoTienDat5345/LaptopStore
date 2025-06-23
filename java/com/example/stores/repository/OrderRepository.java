package com.example.stores.repository;

import com.example.stores.model.Order;
import com.example.stores.dto.EmployeeSalesReportItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    boolean updateStatus(String orderId, String newStatus);

    Optional<Order> findByIdWithCustomerAndEmployee(String orderId);
    List<Order> findAllWithCustomerAndEmployee();
    List<Order> findByCriteriaWithCustomerAndEmployee(LocalDate startDate, LocalDate endDate, Integer customerId); // Bỏ status
    long countOrdersByProductId(String productId);
    List<EmployeeSalesReportItem> getEmployeeSalesReport(LocalDate startDate, LocalDate endDate);
}