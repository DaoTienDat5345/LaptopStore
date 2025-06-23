package com.example.stores.util; // Bỏ .computerstore

import org.mindrot.jbcrypt.BCrypt; // Đảm bảo thư viện jbcrypt đã được import đúng cách

public class PasswordUtil {

    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.isEmpty()) {
            return null; // Hoặc ném lỗi nếu mật khẩu trống không được phép
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null ||
                plainTextPassword.isEmpty() || hashedPassword.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Xảy ra nếu hashedPassword không phải là định dạng BCrypt hợp lệ
            System.err.println("Lỗi khi kiểm tra mật khẩu (Invalid hash format): " + e.getMessage());
            return false;
        }
    }
}