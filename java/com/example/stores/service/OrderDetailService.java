package com.example.stores.service;

import java.util.List;

public interface OrderDetailService {
    List<OrderDetail> getOrderDetailsByOrderId(String orderId);
    long countOrderDetailsByProductId(String productId);
}