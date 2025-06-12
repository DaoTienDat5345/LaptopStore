package com.example.stores.service.impl; // Đảm bảo package đúng

import com.example.stores.model.Employee;
import com.example.stores.repository.EmployeeRepository;
import com.example.stores.repository.WorkShiftScheduleRepository;
import com.example.stores.service.EmployeeService;
import com.example.stores.service.WorkShiftScheduleService;
import com.example.stores.util.PasswordUtil; // Giả sử PasswordUtil ở package util

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final WorkShiftScheduleService workShiftScheduleService; // THÊM BIẾN NÀY


    // Các pattern regex đã định nghĩa (có thể tách ra thành một lớp Util chung nếu cần)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(03|07|08|09)\\d{8}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$"); // Ví dụ: 3-20 ký tự, chữ, số, gạch dưới

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, WorkShiftScheduleService workShiftScheduleService) {
        this.employeeRepository = employeeRepository;
        this.workShiftScheduleService = workShiftScheduleService; // CHÚ Ý: Tham số thứ hai là WorkShiftScheduleService
    }

    @Override
    public Employee addEmployee(Employee employee) throws IllegalArgumentException {
        validateEmployeeData(employee, true); // true để kiểm tra mật khẩu khi thêm mới

        // Kiểm tra trùng lặp username, email, phone
        if (employeeRepository.findByUsername(employee.getUsername().trim()).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
        }
        if (employeeRepository.findByEmail(employee.getEmail().trim()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }
        if (employeeRepository.findByPhone(employee.getPhone().trim()).isPresent()) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại.");
        }

        // Hash mật khẩu
        employee.setPassword(PasswordUtil.hashPassword(employee.getPassword().trim()));
        employee.setCreatedAt(LocalDateTime.now());
        if (employee.getStatus() == null || employee.getStatus().trim().isEmpty()) {
            employee.setStatus("Đang làm"); // Mặc định
        }
        // Giả sử managerID của Manager duy nhất là 1
        employee.setManagerID(1);


        Employee savedEmployee = employeeRepository.save(employee);
        if (savedEmployee == null) {
            throw new RuntimeException("Không thể lưu nhân viên vào CSDL."); // Hoặc một exception cụ thể hơn
        }
        return savedEmployee;
    }

    @Override
    public boolean updateEmployee(Employee employee) throws IllegalArgumentException {
        if (employee == null || employee.getEmployeeID() <= 0) {
            throw new IllegalArgumentException("Thông tin nhân viên hoặc ID không hợp lệ để cập nhật.");
        }

        Optional<Employee> existingEmployeeOpt = employeeRepository.findById(employee.getEmployeeID());
        if (existingEmployeeOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + employee.getEmployeeID());
        }
        Employee existingEmployee = existingEmployeeOpt.get();

        validateEmployeeData(employee, false); // false để không bắt buộc mật khẩu khi cập nhật

        // Kiểm tra trùng lặp nếu username, email, phone thay đổi
        if (!existingEmployee.getUsername().equalsIgnoreCase(employee.getUsername().trim()) &&
                employeeRepository.findByUsername(employee.getUsername().trim()).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập mới đã tồn tại.");
        }
        if (!existingEmployee.getEmail().equalsIgnoreCase(employee.getEmail().trim()) &&
                employeeRepository.findByEmail(employee.getEmail().trim()).isPresent()) {
            throw new IllegalArgumentException("Email mới đã tồn tại.");
        }
        if (!existingEmployee.getPhone().equals(employee.getPhone().trim()) &&
                employeeRepository.findByPhone(employee.getPhone().trim()).isPresent()) {
            throw new IllegalArgumentException("Số điện thoại mới đã tồn tại.");
        }

        // Nếu mật khẩu mới được cung cấp (khác rỗng), thì hash nó
        if (employee.getPassword() != null && !employee.getPassword().trim().isEmpty()) {
            if (employee.getPassword().trim().length() < 6) {
                throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
            }
            employee.setPassword(PasswordUtil.hashPassword(employee.getPassword().trim()));
        } else {
            // Giữ lại mật khẩu cũ nếu không có mật khẩu mới được cung cấp
            employee.setPassword(null); // Repository sẽ không update nếu null/empty
        }

        // Đảm bảo managerID không bị thay đổi linh tinh nếu chỉ có 1 manager
        employee.setManagerID(existingEmployee.getManagerID());
        // Hoặc employee.setManagerID(1); nếu luôn là 1


        return employeeRepository.update(employee);
    }

    @Override
    public boolean deleteEmployee(int employeeId) throws IllegalArgumentException {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (employeeOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + employeeId + " để cập nhật trạng thái.");
        }

        Employee employee = employeeOpt.get();
        employee.setStatus("Nghỉ việc"); // Đổi trạng thái
        boolean statusUpdated = employeeRepository.update(employee); // Lưu trạng thái mới

        if (statusUpdated) {
            // Sau khi cập nhật trạng thái thành công, xóa tất cả lịch làm việc của nhân viên này
            try {
                // Gọi phương thức từ WorkShiftScheduleService
                int schedulesDeleted = workShiftScheduleService.removeAllSchedulesForEmployee(employeeId);
                System.out.println("Đã tự động xóa " + schedulesDeleted + " lịch làm việc của nhân viên ID: " + employeeId + " do nghỉ việc.");
                // Bạn có thể muốn trả về thông tin này hoặc xử lý nó trong Controller để thông báo cho người dùng
            } catch (Exception e) {
                // Ghi log lỗi nếu việc xóa lịch làm việc thất bại, nhưng vẫn coi như việc đổi status nhân viên là thành công
                System.err.println("Lỗi khi tự động xóa lịch làm việc cho nhân viên ID " + employeeId + ": " + e.getMessage());
                e.printStackTrace();
                // Tùy thuộc vào yêu cầu, bạn có thể quyết định ném lại lỗi này hoặc không
                // throw new RuntimeException("Đã cập nhật trạng thái nhân viên nhưng lỗi khi xóa lịch làm việc.", e);
            }
            return true; // Trả về true vì trạng thái nhân viên đã được cập nhật
        }
        return false; // Trả về false nếu cập nhật trạng thái nhân viên thất bại
    }

    @Override
    public Optional<Employee> getEmployeeById(int employeeId) {
        return employeeRepository.findById(employeeId);
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    public List<Employee> searchEmployees(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEmployees(); // Trả về tất cả nếu không có từ khóa
        }
        return employeeRepository.searchEmployees(keyword.trim());
    }

    private void validateEmployeeData(Employee employee, boolean isNew) throws IllegalArgumentException {
        if (employee == null) {
            throw new IllegalArgumentException("Dữ liệu nhân viên không được null.");
        }
        // Username
        if (employee.getUsername() == null || !USERNAME_PATTERN.matcher(employee.getUsername().trim()).matches()) {
            throw new IllegalArgumentException("Tên đăng nhập không hợp lệ (3-20 ký tự, chỉ gồm chữ, số, gạch dưới).");
        }
        // Mật khẩu (chỉ bắt buộc khi isNew là true)
        if (isNew) {
            if (employee.getPassword() == null || employee.getPassword().trim().length() < 6) {
                throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự.");
            }
        }
        // Họ tên
        if (employee.getFullName() == null || employee.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên không được để trống.");
        }
        // Email
        if (employee.getEmail() == null || !EMAIL_PATTERN.matcher(employee.getEmail().trim()).matches()) {
            throw new IllegalArgumentException("Định dạng email không hợp lệ.");
        }
        // Số điện thoại
        if (employee.getPhone() == null || !PHONE_PATTERN.matcher(employee.getPhone().trim()).matches()) {
            throw new IllegalArgumentException("Định dạng số điện thoại không hợp lệ.");
        }
        // Chức vụ
        if (employee.getPosition() == null || employee.getPosition().trim().isEmpty()) {
            throw new IllegalArgumentException("Chức vụ không được để trống.");
        }
        // Lương
        if (employee.getSalary() == null || employee.getSalary().doubleValue() < 0) {
            throw new IllegalArgumentException("Mức lương không hợp lệ.");
        }
        // Các validation khác cho gender, birthDate, address nếu cần
    }
}