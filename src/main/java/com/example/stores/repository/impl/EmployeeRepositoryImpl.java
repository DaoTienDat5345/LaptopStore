package com.example.stores.repository.impl; // Đảm bảo package đúng

import com.example.stores.config.DatabaseConnection;
import com.example.stores.model.Employee;
import com.example.stores.repository.EmployeeRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeRepositoryImpl implements EmployeeRepository {

    private Employee mapRowToEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setEmployeeID(rs.getInt("employeeID"));
        employee.setUsername(rs.getString("username"));
        employee.setPassword(rs.getString("password"));
        employee.setFullName(rs.getString("fullName"));
        employee.setEmail(rs.getString("email"));
        employee.setPhone(rs.getString("phone"));
        employee.setGender(rs.getString("gender"));
        Date birthDateDB = rs.getDate("birthDate");
        if (birthDateDB != null) {
            employee.setBirthDate(birthDateDB.toLocalDate());
        }
        employee.setAddress(rs.getString("address"));
        employee.setImageUrl(rs.getString("imageUrl"));
        employee.setPosition(rs.getString("position"));
        employee.setSalary(rs.getBigDecimal("salary"));
        employee.setStatus(rs.getString("status"));
        Timestamp createdAtDB = rs.getTimestamp("createdAt");
        if (createdAtDB != null) {
            employee.setCreatedAt(createdAtDB.toLocalDateTime());
        }
        employee.setManagerID(rs.getInt("managerID")); // Lấy managerID
        return employee;
    }

    @Override
    public Employee save(Employee employee) {
        String sql = "INSERT INTO Employee (username, password, fullName, email, phone, gender, birthDate, address, imageUrl, position, salary, status, createdAt, managerID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, employee.getUsername());
            pstmt.setString(2, employee.getPassword()); // Mật khẩu đã được hash ở Service
            pstmt.setString(3, employee.getFullName());
            pstmt.setString(4, employee.getEmail());
            pstmt.setString(5, employee.getPhone());
            pstmt.setString(6, employee.getGender());
            if (employee.getBirthDate() != null) {
                pstmt.setDate(7, Date.valueOf(employee.getBirthDate()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }
            pstmt.setString(8, employee.getAddress());
            pstmt.setString(9, employee.getImageUrl());
            pstmt.setString(10, employee.getPosition());
            pstmt.setBigDecimal(11, employee.getSalary());
            pstmt.setString(12, employee.getStatus() != null ? employee.getStatus() : "Đang làm"); // Mặc định
            pstmt.setTimestamp(13, Timestamp.valueOf(employee.getCreatedAt() != null ? employee.getCreatedAt() : LocalDateTime.now()));
            // Giả sử managerID của Manager duy nhất là 1, hoặc lấy từ một nguồn khác nếu cần
            pstmt.setInt(14, employee.getManagerID() > 0 ? employee.getManagerID() : 1);


            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        employee.setEmployeeID(generatedKeys.getInt(1));
                        return employee;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in EmployeeRepositoryImpl.save: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Trả về null nếu có lỗi hoặc không insert được
    }

    @Override
    public boolean update(Employee employee) {
        // Tương tự Manager, không cho cập nhật username, employeeID, createdAt
        // Mật khẩu chỉ cập nhật nếu được cung cấp (khác null và không rỗng)
        String sql = "UPDATE Employee SET fullName = ?, email = ?, phone = ?, gender = ?, birthDate = ?, " +
                "address = ?, imageUrl = ?, position = ?, salary = ?, status = ?, managerID = ?";
        if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
            sql += ", password = ?";
        }
        sql += " WHERE employeeID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            pstmt.setString(paramIndex++, employee.getFullName());
            pstmt.setString(paramIndex++, employee.getEmail());
            pstmt.setString(paramIndex++, employee.getPhone());
            pstmt.setString(paramIndex++, employee.getGender());
            if (employee.getBirthDate() != null) {
                pstmt.setDate(paramIndex++, Date.valueOf(employee.getBirthDate()));
            } else {
                pstmt.setNull(paramIndex++, Types.DATE);
            }
            pstmt.setString(paramIndex++, employee.getAddress());
            pstmt.setString(paramIndex++, employee.getImageUrl());
            pstmt.setString(paramIndex++, employee.getPosition());
            pstmt.setBigDecimal(paramIndex++, employee.getSalary());
            pstmt.setString(paramIndex++, employee.getStatus());
            pstmt.setInt(paramIndex++, employee.getManagerID() > 0 ? employee.getManagerID() : 1);


            if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
                pstmt.setString(paramIndex++, employee.getPassword()); // Mật khẩu đã hash từ Service
            }
            pstmt.setInt(paramIndex, employee.getEmployeeID());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in EmployeeRepositoryImpl.update: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteById(int employeeId) {
        String sql = "DELETE FROM Employee WHERE employeeID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            // Cần xử lý lỗi nếu nhân viên này có ràng buộc khóa ngoại (ví dụ trong Orders, ImportReceipts)
            // Ví dụ: không cho xóa nếu nhân viên đã có giao dịch.
            System.err.println("SQL Error in EmployeeRepositoryImpl.deleteById: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Optional<Employee> findById(int employeeId) {
        String sql = "SELECT * FROM Employee WHERE employeeID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToEmployee(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Employee> findByUsername(String username) {
        String sql = "SELECT * FROM Employee WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToEmployee(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        String sql = "SELECT * FROM Employee WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToEmployee(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Employee> findByPhone(String phone) {
        String sql = "SELECT * FROM Employee WHERE phone = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToEmployee(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Employee> findAll() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM Employee ORDER BY fullName"; // Sắp xếp theo tên cho dễ nhìn
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                employees.add(mapRowToEmployee(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    @Override
    public List<Employee> searchEmployees(String keyword) {
        List<Employee> employees = new ArrayList<>();
        // Tìm kiếm gần đúng trên nhiều trường
        String sql = "SELECT * FROM Employee WHERE " +
                "LOWER(fullName) LIKE LOWER(?) OR " +
                "LOWER(username) LIKE LOWER(?) OR " +
                "LOWER(email) LIKE LOWER(?) OR " +
                "phone LIKE ? OR " +
                "LOWER(position) LIKE LOWER(?)" +
                "ORDER BY fullName";
        String searchPattern = "%" + keyword + "%";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern); // Phone có thể tìm chính xác hoặc LIKE
            pstmt.setString(5, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                employees.add(mapRowToEmployee(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }
}