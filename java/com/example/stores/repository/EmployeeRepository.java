package com.example.stores.repository; // Đảm bảo package đúng

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {
    Employee save(Employee employee);
    boolean update(Employee employee);
    boolean deleteById(int employeeId);
    Optional<Employee> findById(int employeeId);
    Optional<Employee> findByUsername(String username);
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByPhone(String phone);
    List<Employee> findAll();
    List<Employee> searchEmployees(String keyword);
}