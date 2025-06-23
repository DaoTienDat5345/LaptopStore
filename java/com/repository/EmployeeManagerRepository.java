package com.repository;

import com.database.QLdatabase;
import com.model.Employees;
import java.math.BigDecimal; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList; 
import java.util.List; 

public class EmployeeManagerRepository {
    public boolean addEmployeeManager(String position, BigDecimal salary, String name, String phone, String sex) throws SQLException {
        String sql = "INSERT INTO Employees (position, salary, employeesName, employeesPhone, employeesSex) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = QLdatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) { throw new SQLException("Không thể kết nối CSDL."); }

            ps.setString(1, position);
            ps.setBigDecimal(2, salary);
            ps.setString(3, name);
            ps.setString(4, phone);
            ps.setString(5, sex);

            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException ex) {
            System.err.println("Repo Error: SQL khi thêm nhân viên: " + ex.getMessage());
            throw ex;
        } catch (Exception e) {
             System.err.println("Repo Error: Lỗi khác khi thêm nhân viên: " + e.getMessage());
            throw new SQLException(e);
        }
    }

    public boolean deleteEmployeeByIDManager(int id) throws SQLException {
        String sql = "DELETE FROM Employees WHERE employeesID = ?";
        try (Connection conn = QLdatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             if (conn == null) { throw new SQLException("Không thể kết nối CSDL."); }
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Repo Error: SQL khi xóa nhân viên: " + e.getMessage());
            throw e;
        } catch (Exception e) {
             System.err.println("Repo Error: Lỗi khác khi xóa nhân viên: " + e.getMessage());
            throw new SQLException(e);
        }
    }

    public boolean updateEmployeeManager(String position, BigDecimal salary, String name, String phone, String sex, int id) throws SQLException {
        String sql = "UPDATE Employees SET position = ?, salary = ?, employeesName = ?, employeesPhone = ?, employeesSex = ? WHERE employeesID = ?";
        try (Connection conn = QLdatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             if (conn == null) { throw new SQLException("Không thể kết nối CSDL."); }

            ps.setString(1, position);
            ps.setBigDecimal(2, salary); 
            ps.setString(3, name);
            ps.setString(4, phone);
            ps.setString(5, sex);
            ps.setInt(6, id);

            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Repo Error: SQL khi cập nhật nhân viên: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Repo Error: Lỗi khác khi cập nhật nhân viên: " + e.getMessage());
            throw new SQLException(e);
        }
    }

    public List<Employees> getAllEmployees() throws SQLException {
        List<Employees> employeeList = new ArrayList<>();
        String sql = "SELECT employeesID, position, salary, employeesName, employeesPhone, employeesSex FROM Employees";
        try (Connection conn = QLdatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (conn == null) { throw new SQLException("Không thể kết nối CSDL."); }

            while (rs.next()) {
                Employees employee = new Employees(
                    rs.getInt("employeesID"),
                    rs.getString("position"),
                    rs.getBigDecimal("salary"), 
                    rs.getString("employeesName"),
                    rs.getString("employeesPhone"),
                    rs.getString("employeesSex")
                );
                employeeList.add(employee);
            }
        } catch (SQLException e) {
             System.err.println("Repo Error: SQL khi lấy danh sách nhân viên: " + e.getMessage());
             throw e;
        } catch (Exception e) {
             System.err.println("Repo Error: Lỗi khác khi lấy danh sách nhân viên: " + e.getMessage());
             throw new SQLException(e);
        }
        return employeeList;
    }
}