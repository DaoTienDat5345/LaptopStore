package com.example.stores.repository.impl;

import com.example.stores.config.DatabaseConnection;
import com.example.stores.model.WorkShiftSchedule;
import com.example.stores.repository.WorkShiftScheduleRepository;

import java.sql.*;
import java.time.LocalDate; // Vẫn có thể dùng cho các hàm findByDateRange nếu bạn giữ lại
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkShiftScheduleRepositoryImpl implements WorkShiftScheduleRepository {

    // Helper để map ResultSet
    // Tham số includeDetails không còn quá cần thiết nếu các câu query luôn join
    private WorkShiftSchedule mapRowToWorkShiftSchedule(ResultSet rs) throws SQLException {
        WorkShiftSchedule schedule = new WorkShiftSchedule();
        schedule.setScheduleID(rs.getInt("scheduleID"));
        schedule.setEmployeeID(rs.getInt("employeeID"));
        schedule.setShiftID(rs.getString("shiftID"));
        schedule.setDayOfWeek(rs.getString("dayOfWeek"));
        schedule.setNotes(rs.getString("notes"));

        if (hasColumn(rs, "employeeFullName")) {
            schedule.setEmployeeFullName(rs.getString("employeeFullName"));
        }
        if (hasColumn(rs, "shiftDetails")) { // Đảm bảo tên này khớp với alias trong SQL
            schedule.setShiftDetails(rs.getString("shiftDetails"));
        }
        return schedule;
    }

    // Tiện ích kiểm tra cột tồn tại trong ResultSet (giữ nguyên)
    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equalsIgnoreCase(rsmd.getColumnName(x))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public WorkShiftSchedule save(WorkShiftSchedule schedule) {
        String sql = "INSERT INTO WorkShiftSchedule (employeeID, shiftID, dayOfWeek, notes) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, schedule.getEmployeeID());
            pstmt.setString(2, schedule.getShiftID());
            pstmt.setString(3, schedule.getDayOfWeek()); // Lưu dayOfWeek
            pstmt.setString(4, schedule.getNotes());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        schedule.setScheduleID(generatedKeys.getInt(1));
                        return schedule;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in WorkShiftScheduleRepositoryImpl.save: " + e.getMessage());
            e.printStackTrace(); // Có thể do vi phạm UQ_EmployeeWeeklyShift
        }
        return null;
    }

    @Override
    public boolean update(WorkShiftSchedule schedule) {
        // Giả sử chỉ cho phép cập nhật 'notes'.
        // Nếu cho phép cập nhật employeeID, shiftID, dayOfWeek thì cần kiểm tra UNIQUE phức tạp hơn.
        String sql = "UPDATE WorkShiftSchedule SET notes = ? WHERE scheduleID = ?";
        // Nếu bạn muốn cho phép cập nhật các trường khác:
        // String sql = "UPDATE WorkShiftSchedule SET employeeID = ?, shiftID = ?, dayOfWeek = ?, notes = ? WHERE scheduleID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, schedule.getNotes());
            pstmt.setInt(2, schedule.getScheduleID());

            // Nếu cho phép cập nhật các trường khác:
            // pstmt.setInt(1, schedule.getEmployeeID());
            // pstmt.setString(2, schedule.getShiftID());
            // pstmt.setString(3, schedule.getDayOfWeek());
            // pstmt.setString(4, schedule.getNotes());
            // pstmt.setInt(5, schedule.getScheduleID());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in WorkShiftScheduleRepositoryImpl.update: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteById(int scheduleId) {
        String sql = "DELETE FROM WorkShiftSchedule WHERE scheduleID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, scheduleId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Optional<WorkShiftSchedule> findById(int scheduleId) {
        String sql = "SELECT wss.*, e.fullName as employeeFullName, " +
                "ISNULL(s.shiftName, N'') + N' (' + ISNULL(LEFT(CONVERT(varchar(8), s.startTime, 108), 5), N'N/A') + " +
                "N' - ' + ISNULL(LEFT(CONVERT(varchar(8), s.endTime, 108), 5), N'N/A') + N')' as shiftDetails " +
                "FROM WorkShiftSchedule wss " +
                "JOIN Employee e ON wss.employeeID = e.employeeID " +
                "JOIN Shifts s ON wss.shiftID = s.shiftID " +
                "WHERE wss.scheduleID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, scheduleId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToWorkShiftSchedule(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<WorkShiftSchedule> findByEmployeeId(int employeeId) {
        List<WorkShiftSchedule> schedules = new ArrayList<>();
        String sql = "SELECT wss.*, e.fullName as employeeFullName, " +
                "s.shiftName + N' (' + FORMAT(s.startTime, 'HH:mm') + N' - ' + FORMAT(s.endTime, 'HH:mm') + N')' as shiftDetails " +
                "FROM WorkShiftSchedule wss " +
                "JOIN Employee e ON wss.employeeID = e.employeeID " +
                "JOIN Shifts s ON wss.shiftID = s.shiftID " +
                "WHERE wss.employeeID = ? " +
                "ORDER BY CASE wss.dayOfWeek " +
                "            WHEN N'Thứ 2' THEN 1 WHEN N'Thứ 3' THEN 2 WHEN N'Thứ 4' THEN 3 " +
                "            WHEN N'Thứ 5' THEN 4 WHEN N'Thứ 6' THEN 5 WHEN N'Thứ 7' THEN 6 " +
                "            WHEN N'Chủ Nhật' THEN 7 ELSE 8 END, s.startTime";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                schedules.add(mapRowToWorkShiftSchedule(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    @Override
    public List<WorkShiftSchedule> findByShiftIdAndDayOfWeek(String shiftId, String dayOfWeek) {
        List<WorkShiftSchedule> schedules = new ArrayList<>();
        // Câu SQL này không nhất thiết phải JOIN nếu chỉ dùng để đếm hoặc kiểm tra ràng buộc
        // Nhưng nếu muốn hiển thị thông tin khi có lỗi, JOIN sẽ hữu ích
        String sql = "SELECT wss.*, e.fullName as employeeFullName, " +
                "s.shiftName + N' (' + FORMAT(s.startTime, 'HH:mm') + N' - ' + FORMAT(s.endTime, 'HH:mm') + N')' as shiftDetails " +
                "FROM WorkShiftSchedule wss " +
                "JOIN Employee e ON wss.employeeID = e.employeeID " +
                "JOIN Shifts s ON wss.shiftID = s.shiftID " +
                "WHERE wss.shiftID = ? AND wss.dayOfWeek = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, shiftId);
            pstmt.setString(2, dayOfWeek);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                schedules.add(mapRowToWorkShiftSchedule(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    @Override
    public List<WorkShiftSchedule> findAllWithDetails() {
        List<WorkShiftSchedule> schedules = new ArrayList<>();
        String sql = "SELECT wss.*, e.fullName as employeeFullName, " +
                "ISNULL(s.shiftName, N'') + " +
                "N' (' + " +
                "ISNULL(LEFT(CONVERT(varchar(8), s.startTime, 108), 5), N'N/A') + " +
                "N' - ' + " +
                "ISNULL(LEFT(CONVERT(varchar(8), s.endTime, 108), 5), N'N/A') + " +
                "N')' as shiftDetails " +
                "FROM WorkShiftSchedule wss " +
                "JOIN Employee e ON wss.employeeID = e.employeeID " +
                "JOIN Shifts s ON wss.shiftID = s.shiftID " +
                "ORDER BY CASE wss.dayOfWeek " +
                "            WHEN N'Thứ 2' THEN 1 WHEN N'Thứ 3' THEN 2 WHEN N'Thứ 4' THEN 3 " +
                "            WHEN N'Thứ 5' THEN 4 WHEN N'Thứ 6' THEN 5 WHEN N'Thứ 7' THEN 6 " +
                "            WHEN N'Chủ Nhật' THEN 7 ELSE 8 END, s.startTime, e.fullName";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                schedules.add(mapRowToWorkShiftSchedule(rs)); // mapRowToWorkShiftSchedule sẽ đọc cột 'shiftDetails'
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }


    @Override
    public int deleteAllSchedulesForEmployee(int employeeId) {
        String sql = "DELETE FROM WorkShiftSchedule WHERE employeeID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQL Error in WorkShiftScheduleRepositoryImpl.deleteAllSchedulesForEmployee: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int deleteSchedulesByShiftId(String shiftId) {
        String sql = "DELETE FROM WorkShiftSchedule WHERE shiftID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, shiftId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQL Error in WorkShiftScheduleRepositoryImpl.deleteSchedulesByShiftId: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // Các phương thức liên quan đến LocalDate workDate có thể không cần thiết nữa
    // hoặc cần điều chỉnh logic nếu bạn muốn lọc theo một tuần cụ thể.
    // Ví dụ, bạn có thể tạo một phương thức ở Service để lấy tất cả các phân công
    // rồi lọc theo tuần ở tầng Service/Controller.
    // Hoặc nếu CSDL có hàm lấy dayOfWeek từ date, bạn có thể query phức tạp hơn.
}