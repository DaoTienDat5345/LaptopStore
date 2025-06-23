package com.example.stores.service;

import java.util.List;

public interface IOrderService {
    Order createOrder(Order order);
    Order createOrderWithFixedPhone(Order order);
    double updateWarrantyPrice(OrderDetail orderDetail);
    Order getOrderById(String orderId);
    List<Order> getCustomerOrders(int customerId);
    boolean finalizeOrder(Order order, ICartService cartService, Cart cart, List<CartItem> selectedItems);
    List<OrderDetail> getOrderDetails(String orderId);
}