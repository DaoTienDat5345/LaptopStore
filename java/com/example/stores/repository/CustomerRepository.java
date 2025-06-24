package com.example.stores.repository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
    // Customer không có chức năng Manager thêm mới trực tiếp, nên save() có thể không cần
    // Nếu có, nó sẽ giống như save của Employee/Supplier
    // Customer save(Customer customer);

    /**
     * Cập nhật thông tin khách hàng (Manager có thể sửa một số thông tin hoặc trạng thái).
     * @param customer Đối tượng Customer với thông tin cập nhật.
     * @return true nếu thành công.
     */
    boolean update(Customer customer);

    // Manager có thể không xóa cứng khách hàng, mà chỉ đổi isActive
    // boolean deleteById(int customerId);

    Optional<Customer> findById(int customerId);
    List<Customer> findAll(); // Lấy tất cả KH, có thể bao gồm cả active và inactive
    List<Customer> searchCustomers(String keyword, Boolean isActiveFilter); // Tìm kiếm với bộ lọc trạng thái

    // Kiểm tra UNIQUE nếu Manager được phép sửa username, email, phone
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByPhone(String phone);
}