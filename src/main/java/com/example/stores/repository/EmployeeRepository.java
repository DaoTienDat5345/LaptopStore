package com.example.stores.repository; // Đảm bảo package đúng

import com.example.stores.model.Employee;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {
    /**
     * Thêm một nhân viên mới vào CSDL.
     * @param employee Đối tượng Employee chứa thông tin nhân viên mới.
     * @return Đối tượng Employee đã được lưu với employeeID được CSDL tự động sinh.
     *         Trả về null nếu có lỗi.
     */
    Employee save(Employee employee);

    /**
     * Cập nhật thông tin của một nhân viên hiện có.
     * @param employee Đối tượng Employee chứa thông tin cần cập nhật.
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    boolean update(Employee employee);

    /**
     * Xóa một nhân viên khỏi CSDL dựa trên ID.
     * @param employeeId ID của nhân viên cần xóa.
     * @return true nếu xóa thành công, false nếu thất bại.
     */
    boolean deleteById(int employeeId);

    /**
     * Tìm một nhân viên bằng ID.
     * @param employeeId ID của nhân viên.
     * @return Optional chứa Employee nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<Employee> findById(int employeeId);

    /**
     * Tìm một nhân viên bằng username.
     * @param username Tên đăng nhập của nhân viên.
     * @return Optional chứa Employee nếu tìm thấy.
     */
    Optional<Employee> findByUsername(String username);

    /**
     * Tìm một nhân viên bằng email.
     * @param email Email của nhân viên.
     * @return Optional chứa Employee nếu tìm thấy.
     */
    Optional<Employee> findByEmail(String email);

    /**
     * Tìm một nhân viên bằng số điện thoại.
     * @param phone Số điện thoại của nhân viên.
     * @return Optional chứa Employee nếu tìm thấy.
     */
    Optional<Employee> findByPhone(String phone);

    /**
     * Lấy danh sách tất cả nhân viên.
     * @return List các đối tượng Employee.
     */
    List<Employee> findAll();

    /**
     * Tìm kiếm nhân viên dựa trên một từ khóa (có thể tìm theo tên, username, email, phone).
     * (Có thể triển khai phức tạp hơn với nhiều tiêu chí)
     * @param keyword Từ khóa tìm kiếm.
     * @return List các Employee phù hợp.
     */
    List<Employee> searchEmployees(String keyword);
}