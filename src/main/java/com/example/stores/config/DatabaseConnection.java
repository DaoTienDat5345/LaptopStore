package com.example.stores.config; // Đảm bảo package đúng

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String DB_ServerName = "ADMIN-PC";
    private static final String DB_login = "sa";
    private static final String DB_password = "root";
    private static final String DB_DatabaseName = "StoreLaptop";

    private static final String DB_URL = "jdbc:sqlserver://" + DB_ServerName + ":1433;databaseName=" + DB_DatabaseName +
            ";encrypt=true;trustServerCertificate=true";

    private static Connection connection = null;

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(DB_URL, DB_login, DB_password);
                // System.out.println("Kết nối CSDL thành công!"); // Bỏ comment nếu muốn test
            } catch (SQLException e) {
                System.err.println("Lỗi kết nối CSDL: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    connection = null;
                    // System.out.println("Đã đóng kết nối CSDL."); // Bỏ comment nếu muốn test
                }
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối CSDL: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}