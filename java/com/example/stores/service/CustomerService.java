package com.example.stores.service;

import com.example.stores.model.Customer;
import java.util.List;
import java.util.Optional;

public interface CustomerService {
    Optional<Customer> getCustomerById(int customerId);
    List<Customer> getAllCustomers(); // Lấy tất cả, kể cả inactive
    List<Customer> searchCustomers(String keyword, Boolean isActiveFilter); // Tìm kiếm và lọc theo trạng thái
    boolean updateCustomerInfo(Customer customer) throws IllegalArgumentException; // Manager cập nhật thông tin
    boolean setCustomerActiveStatus(int customerId, boolean isActive) throws IllegalArgumentException;
    List<Customer> getAllActiveCustomers(); // ĐẢM BẢO PHƯƠNG THỨC NÀY CÓ Ở ĐÂY
}