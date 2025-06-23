package com.example.stores.repository.impl;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {
    
    // Find Customer by username
    public Customer findByUsername(String username) {
        String sql = "SELECT * FROM Customer WHERE username = ?";
        
        try {
            // Không sử dụng try-with-resources với Connection để tránh đóng kết nối
            Connection conn = DatabaseConnection.getConnection();
            
            // Check connection
            if (conn == null) {
                System.err.println("Cannot connect to database");
                return null;
            }
            
            // Vẫn sử dụng try-with-resources với PreparedStatement
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    Customer customer = new Customer();
                    customer.setId(rs.getInt("customerID"));
                    customer.setUsername(rs.getString("username"));
                    customer.setPassword(rs.getString("password"));
                    customer.setFullName(rs.getString("fullName"));
                    customer.setEmail(rs.getString("email"));
                    customer.setPhone(rs.getString("phone"));
                    customer.setGender(rs.getString("gender"));
                    
                    Date birthDate = rs.getDate("birthDate");
                    if (birthDate != null) {
                        customer.setBirthDate(birthDate.toLocalDate());
                    }
                    
                    customer.setAddress(rs.getString("address"));
                    
                    Timestamp registeredAt = rs.getTimestamp("registeredAt");
                    if (registeredAt != null) {
                        customer.setRegisteredAt(registeredAt.toLocalDateTime());
                    }
                    
                    customer.setActive(rs.getBoolean("isActive"));
                    
                    return customer;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error querying customer: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Authenticate login
    public Customer authenticate(String username, String password) {
        String sql = "SELECT * FROM Customer WHERE username = ? AND isActive = 1";

        try {
            // Không sử dụng try-with-resources với Connection
            Connection conn = DatabaseConnection.getConnection();

            // Check connection
            if (conn == null) {
                System.err.println("Cannot connect to database");
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // Lấy mật khẩu đã hash từ database
                    String hashedPasswordFromDB = rs.getString("password");

                    // Kiểm tra mật khẩu nhập vào có khớp với hash không
                    if (BCrypt.checkpw(password, hashedPasswordFromDB)) {
                        Customer customer = new Customer();
                        customer.setId(rs.getInt("customerID"));
                        customer.setUsername(rs.getString("username"));
                        // Không lưu mật khẩu vào đối tượng Customer
                        customer.setFullName(rs.getString("fullName"));
                        customer.setEmail(rs.getString("email"));
                        customer.setPhone(rs.getString("phone"));
                        customer.setGender(rs.getString("gender"));

                        Date birthDate = rs.getDate("birthDate");
                        if (birthDate != null) {
                            customer.setBirthDate(birthDate.toLocalDate());
                        }

                        customer.setAddress(rs.getString("address"));

                        Timestamp registeredAt = rs.getTimestamp("registeredAt");
                        if (registeredAt != null) {
                            customer.setRegisteredAt(registeredAt.toLocalDateTime());
                        }

                        customer.setActive(rs.getBoolean("isActive"));

                        return customer;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Register new customer
    public boolean register(Customer customer) {
        String sql = "INSERT INTO Customer (username, password, fullName, email, phone, gender, birthDate, address, isActive) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            // Không sử dụng try-with-resources với Connection
            Connection conn = DatabaseConnection.getConnection();

            // Check connection
            if (conn == null) {
                System.err.println("Cannot connect to database");
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, customer.getUsername());

                // Mã hóa mật khẩu bằng BCrypt
                String hashedPassword = BCrypt.hashpw(customer.getPassword(), BCrypt.gensalt(12));
                stmt.setString(2, hashedPassword);

                stmt.setString(3, customer.getFullName());
                stmt.setString(4, customer.getEmail());
                stmt.setString(5, customer.getPhone());
                stmt.setString(6, customer.getGender());

                if (customer.getBirthDate() != null) {
                    stmt.setDate(7, Date.valueOf(customer.getBirthDate()));
                } else {
                    stmt.setNull(7, Types.DATE);
                }

                stmt.setString(8, customer.getAddress());
                stmt.setBoolean(9, true); // Default to active

                int rowsAffected = stmt.executeUpdate();

                // Log registration outcome
                System.out.println("New customer registered: " + customer.getUsername() + ", result: " + (rowsAffected > 0));

                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error registering customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Kiểm tra xem email đã tồn tại trong cơ sở dữ liệu chưa
     * @param email Email cần kiểm tra
     * @return true nếu email đã tồn tại
     */
    public boolean isEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Lấy connection từ DatabaseConnection
        Connection conn = DatabaseConnection.getConnection();
        
        if (conn == null) {
            System.err.println("Cannot connect to database");
            return false;
        }
        
        String sql = "SELECT COUNT(*) FROM Customer WHERE email = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Kiểm tra xem số điện thoại đã tồn tại trong cơ sở dữ liệu chưa
     * @param phone Số điện thoại cần kiểm tra
     * @return true nếu số điện thoại đã tồn tại
     */
    public boolean isPhoneExists(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        // Lấy connection từ DatabaseConnection
        Connection conn = DatabaseConnection.getConnection();
        
        if (conn == null) {
            System.err.println("Cannot connect to database");
            return false;
        }
        
        String sql = "SELECT COUNT(*) FROM Customer WHERE phone = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra số điện thoại: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // Thêm phương thức updateCustomer vào lớp CustomerRepository

    /**
     * Cập nhật thông tin khách hàng
     * @param customer Đối tượng khách hàng chứa thông tin cần cập nhật
     * @return true nếu cập nhật thành công
     */
    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE Customer SET fullName = ?, email = ?, phone = ?, " +
                "gender = ?, birthDate = ?, address = ? WHERE customerID = ?";

        try {
            Connection conn = DatabaseConnection.getConnection();

            if (conn == null) {
                System.err.println("Cannot connect to database");
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, customer.getFullName());
                stmt.setString(2, customer.getEmail());
                stmt.setString(3, customer.getPhone());
                stmt.setString(4, customer.getGender());

                if (customer.getBirthDate() != null) {
                    stmt.setDate(5, java.sql.Date.valueOf(customer.getBirthDate()));
                } else {
                    stmt.setNull(5, java.sql.Types.DATE);
                }

                stmt.setString(6, customer.getAddress());
                stmt.setInt(7, customer.getId());

                int rowsUpdated = stmt.executeUpdate();

                // Cập nhật currentCustomer trong AuthService
                if (rowsUpdated > 0) {
                    // Không cần thiết lập currentCustomer lại vì đối tượng đã được cập nhật
                    return true;
                }

                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error updating customer information: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // Thêm phương thức changePassword vào CustomerRepository
    public boolean changePassword(int customerId, String newPassword) {
        String sql = "UPDATE Customer SET password = ? WHERE customerID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Mã hóa mật khẩu mới
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, customerId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error changing password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Lấy email của người dùng dựa trên username
     * @param username Tên đăng nhập cần tra cứu
     * @return Email của người dùng hoặc null nếu không tìm thấy
     */
    public String getEmailByUsername(String username) {
        String sql = "SELECT email FROM Customer WHERE username = ? AND isActive = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            System.err.println("Error getting email by username: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}