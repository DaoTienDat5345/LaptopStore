package com.example.stores.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Database connection info
    private static final String DB_ServerName = "DESKTOP-BSBAAI0";
    private static final String DB_login = "sa";
    private static final String DB_password = "nhan123";
    private static final String DB_NAME = "Computer_Storest";
    
    // Thay đổi phương thức getConnection để không lưu trữ connection tĩnh
    public static Connection getConnection() {
        Connection connection = null;
        try {
            String connectionUrl = "jdbc:sqlserver://" + DB_ServerName +
                    ";databaseName=" + DB_NAME +
                    ";user=" + DB_login +
                    ";password=" + DB_password +
                    ";encrypt=true;trustServerCertificate=true";
            
            connection = DriverManager.getConnection(connectionUrl);
            
            // Debug log - chỉ để xác nhận kết nối thành công
            if (connection != null && !connection.isClosed()) {
                System.out.println("Đã lấy kết nối database mới");
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }
    
    // Sửa phương thức closeConnection để chấp nhận một connection tham số
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Đã đóng kết nối database");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // Test connection method
    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connection successful!");
                return true;
            } else {
                System.err.println("Failed to establish database connection");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Database test connection error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Đóng kết nối test
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}