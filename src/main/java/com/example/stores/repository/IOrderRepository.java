package com.example.stores.repository;

import java.util.List;

public interface IOrderRepository {
    Order createOrder(Order order);
    boolean saveOrderDetails(Order order);
    Order getOrderById(String orderId);
    List<OrderDetail> getOrderDetailsForOrder(String orderId);
    List<Order> getOrdersByCustomerId(int customerId);
    boolean updateOrderStatus(String orderId, String status);
    boolean deleteOrder(String orderId);
}