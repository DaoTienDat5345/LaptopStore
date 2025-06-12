package com.example.stores.service;

import com.example.stores.model.Cart;
import com.example.stores.model.CartItem;
import com.example.stores.model.Order;
import com.example.stores.model.OrderDetail;
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