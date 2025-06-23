package com.example.stores.repository;

import com.example.stores.model.OrderDetail;
import java.sql.Connection; // Cần nếu saveDetail được gọi từ bên ngoài với transaction
import java.sql.SQLException; // Cần nếu saveDetail được gọi từ bên ngoài với transaction
import java.time.LocalDate;
import java.util.List;

public interface OrderDetailRepository {
    List<OrderDetail> findByOrderIdWithProductDetails(String orderId);
    OrderDetail save(OrderDetail detail, Connection conn) throws SQLException;
    long countByProductId(String productId);
}