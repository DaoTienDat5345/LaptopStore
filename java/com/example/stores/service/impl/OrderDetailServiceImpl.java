package com.example.stores.service.impl;

import com.example.stores.repository.OrderDetailRepository;
import com.example.stores.service.OrderDetailService;

import java.util.List;

public class OrderDetailServiceImpl implements OrderDetailService {
    private final OrderDetailRepository orderDetailRepository;

    public OrderDetailServiceImpl(OrderDetailRepository orderDetailRepository) {
        this.orderDetailRepository = orderDetailRepository;
    }

    @Override
    public List<OrderDetail> getOrderDetailsByOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID không được để trống.");
        }
        return orderDetailRepository.findByOrderIdWithProductDetails(orderId);
    }

    @Override
    public long countOrderDetailsByProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID không được để trống.");
        }
        return orderDetailRepository.countByProductId(productId);
    }
}