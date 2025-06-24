package com.example.stores.repository; // Đảm bảo package đúng

import java.util.Optional;

public interface ManagerRepository {
    /**
     * Tìm Manager bằng ID. Vì chúng ta chỉ có một Manager, ID này thường sẽ cố định (ví dụ: 1).
     * @param id ID của Manager
     * @return Optional chứa Manager nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<Manager> findById(int id);

    /**
     * Cập nhật thông tin của Manager.
     * @param manager Đối tượng Manager chứa thông tin cần cập nhật.
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    boolean update(Manager manager);

    Optional<Manager> findByUsername(String username);

    // Chúng ta không cần save (vì Manager đã có sẵn) hoặc delete (vì chỉ có 1 Manager)
    // hoặc findAll trong trường hợp này.
}