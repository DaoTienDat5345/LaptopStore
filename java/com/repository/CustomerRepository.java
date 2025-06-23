package com.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import com.database.QLdatabase;

public class CustomerRepository {

    public boolean checkUserName(String userName) throws SQLException {
        try (Connection con = QLdatabase.getConnection();
             PreparedStatement pre = con.prepareStatement("SELECT COUNT(*) FROM Customer WHERE userName = ?")) {
            pre.setString(1, userName);
            try (ResultSet rs = pre.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean checkPhone(String phone) throws SQLException {
        try (Connection con = QLdatabase.getConnection();
             PreparedStatement pre = con.prepareStatement("SELECT COUNT(*) FROM Customer WHERE phone = ?")) {
            pre.setString(1, phone);
            try (ResultSet rs = pre.executeQuery()) {
                if (rs.next()) {	
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean checkEmail(String email) throws SQLException {
        try (Connection con = QLdatabase.getConnection();
             PreparedStatement pre = con.prepareStatement("SELECT COUNT(*) FROM Customer WHERE userEmail = ?")) {
            pre.setString(1, email);
            try (ResultSet rs = pre.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean register(String userName, String userPassword, String phone, String fullName, String userAddress, String userSex, int day, int month, int year, String userEmail) throws SQLException {
        try (Connection con = QLdatabase.getConnection();
             PreparedStatement pre = con.prepareStatement("INSERT INTO Customer (userName, userPassword, phone, fullName, userAddress, userSex, userDate, userEmail) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            pre.setString(1, userName);
            pre.setString(2, userPassword);
            pre.setString(3, phone);
            pre.setString(4, fullName);
            pre.setString(5, userAddress);
            pre.setString(6, userSex);
            Calendar cal = Calendar.getInstance();
            cal.set(year, month - 1, day);
            pre.setDate(7, new java.sql.Date(cal.getTimeInMillis()));
            pre.setString(8, userEmail);

            int kq = pre.executeUpdate();
            return kq > 0;
        }
    }

    public int login(String userName, String userPassword) throws SQLException {
        try (Connection con = QLdatabase.getConnection();
             PreparedStatement pre = con.prepareStatement("SELECT customerID FROM Customer WHERE userName = ? AND userPassword = ?")) {
            pre.setString(1, userName);
            pre.setString(2, userPassword);
            try (ResultSet rs = pre.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("customerID");
                }
            }
        }
        return -1;
    }

    public boolean loginManager(String userManager, String userPasswordManager) throws SQLException {
        try (Connection con = QLdatabase.getConnection();
             PreparedStatement pre = con.prepareStatement("SELECT userPasswordManager FROM Manager WHERE userManager = ?")) {
            pre.setString(1, userManager);
            try (ResultSet rs = pre.executeQuery()) {
                if (rs.next()) {
                    String userPasswordDB = rs.getString("userPasswordManager");
                    return userPasswordManager.equals(userPasswordDB);
                }
            }
        }
        return false;
    }

    public String verifyForgotPassword(String email, String phone) throws SQLException {
        try (Connection con = QLdatabase.getConnection();
             PreparedStatement pre = con.prepareStatement("SELECT PersonalCode FROM Customer WHERE userEmail = ? AND phone = ?")) {
            pre.setString(1, email);
            pre.setString(2, phone);
            try (ResultSet rs = pre.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("PersonalCode");
                }
            }
        }
        return null;
    }

    public boolean changePassword(String email, String personalCode, String newPassword) throws SQLException {
        try (Connection con = QLdatabase.getConnection();
             PreparedStatement pre = con.prepareStatement("UPDATE Customer SET userPassword = ? WHERE userEmail = ? AND PersonalCode = ?")) {
            pre.setString(1, newPassword);
            pre.setString(2, email);
            pre.setString(3, personalCode);
            int rows = pre.executeUpdate();
            return rows > 0;
        }
    }
}