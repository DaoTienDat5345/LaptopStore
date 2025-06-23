package com.example.stores.service; // Đảm bảo package đúng

import com.example.stores.model.Employee;
import java.util.List;
import java.util.Optional;

public interface EmployeeService {

    /**
     * Thêm một nhân viên mới.
     * Bao gồm validation dữ liệu và hash mật khẩu.
     * @param employee Đối tượng Employee chứa thông tin nhân viên mới.
     * @return Đối tượng Employee đã được lưu.
     * @throws IllegalArgumentException Nếu dữ liệu không hợp lệ hoặc username/email/phone đã tồn tại.
     */
    Employee addEmployee(Employee employee) throws IllegalArgumentException;

    /**
     * Cập nhật thông tin một nhân viên.
     * Bao gồm validation và hash mật khẩu mới nếu có.
     * @param employee Đối tượng Employee chứa thông tin cập nhật.
     * @return true nếu cập nhật thành công.
     * @throws IllegalArgumentException Nếu dữ liệu không hợp lệ hoặc không tìm thấy nhân viên.
     */
    boolean updateEmployee(Employee employee) throws IllegalArgumentException;

    /**
     * Xóa một nhân viên (hoặc chuyển trạng thái thành 'Nghỉ việc').
     * @param employeeId ID của nhân viên cần xóa/thay đổi trạng thái.
     * @return true nếu thành công.
     * @throws IllegalArgumentException Nếu không tìm thấy nhân viên.
     */
    boolean deleteEmployee(int employeeId) throws IllegalArgumentException; // Hoặc changeEmployeeStatus

    /**
     * Lấy thông tin chi tiết một nhân viên bằng ID.
     * @param employeeId ID của nhân viên.
     * @return Optional chứa Employee.
     */
    Optional<Employee> getEmployeeById(int employeeId);

    /**
     * Lấy danh sách tất cả nhân viên.
     * @return List các Employee.
     */
    List<Employee> getAllEmployees();

    /**
     * Tìm kiếm nhân viên.
     * @param keyword Từ khóa tìm kiếm.
     * @return List các Employee phù hợp.
     */
    List<Employee> searchEmployees(String keyword);
}