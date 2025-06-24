package com.example.stores.repository.impl;

import com.example.stores.config.DatabaseConnection;
import com.example.stores.repository.ShiftRepository;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShiftRepositoryImpl implements ShiftRepository {

    private Shift mapRowToShift(ResultSet rs) throws SQLException {
        Shift shift = new Shift();
        shift.setShiftID(rs.getString("shiftID"));
        shift.setShiftName(rs.getString("shiftName"));
        Time startTimeDB = rs.getTime("startTime");
        if (startTimeDB != null) {
            shift.setStartTime(startTimeDB.toLocalTime());
        }
        Time endTimeDB = rs.getTime("endTime");
        if (endTimeDB != null) {
            shift.setEndTime(endTimeDB.toLocalTime());
        }
        // Không còn dayOfWeek ở đây
        return shift;
    }

    @Override
    public Shift save(Shift shift) {
        String sql = "INSERT INTO Shifts (shiftID, shiftName, startTime, endTime) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, shift.getShiftID());
            pstmt.setString(2, shift.getShiftName());
            pstmt.setTime(3, Time.valueOf(shift.getStartTime()));
            pstmt.setTime(4, Time.valueOf(shift.getEndTime()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                return shift;
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in ShiftRepositoryImpl.save: " + e.getMessage());
            e.printStackTrace(); // shiftID có thể đã tồn tại
        }
        return null;
    }

    @Override
    public boolean update(Shift shift) {
        String sql = "UPDATE Shifts SET shiftName = ?, startTime = ?, endTime = ? WHERE shiftID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, shift.getShiftName());
            pstmt.setTime(2, Time.valueOf(shift.getStartTime()));
            pstmt.setTime(3, Time.valueOf(shift.getEndTime()));
            pstmt.setString(4, shift.getShiftID());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in ShiftRepositoryImpl.update: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteById(String shiftId) {
        // Logic kiểm tra ràng buộc hoặc ON DELETE CASCADE sẽ xử lý ở CSDL/Service
        String sql = "DELETE FROM Shifts WHERE shiftID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, shiftId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in ShiftRepositoryImpl.deleteById: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Optional<Shift> findById(String shiftId) {
        String sql = "SELECT * FROM Shifts WHERE shiftID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, shiftId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToShift(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Shift> findAll() {
        List<Shift> shifts = new ArrayList<>();
        String sql = "SELECT * FROM Shifts ORDER BY startTime, shiftName";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                shifts.add(mapRowToShift(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shifts;
    }
}