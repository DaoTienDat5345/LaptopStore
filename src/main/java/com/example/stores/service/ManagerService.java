package com.example.stores.service; // Bỏ .computerstore

import com.example.stores.model.Manager;
import java.util.Optional;

public interface ManagerService {

    /**
     * Lấy thông tin của Manager (mặc định là Manager duy nhất).
     * @param managerId ID của manager (thường là một giá trị cố định)
     * @return Optional chứa Manager nếu tìm thấy.
     */
    Optional<Manager> getManagerProfile(int managerId);

    /**
     * Cập nhật thông tin hồ sơ của Manager.
     * Bao gồm cả việc hash mật khẩu mới nếu được cung cấp.
     * @param manager Đối tượng Manager chứa thông tin cần cập nhật.
     * @return true nếu cập nhật thành công, false nếu thất bại hoặc có lỗi.
     * @throws IllegalArgumentException nếu dữ liệu đầu vào không hợp lệ.
     */
    boolean updateManagerProfile(Manager manager) throws IllegalArgumentException;
    Optional<Manager> login(String username, String password);
}