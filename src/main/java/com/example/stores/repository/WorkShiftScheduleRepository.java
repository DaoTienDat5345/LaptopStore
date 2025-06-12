package com.example.stores.repository;

import com.example.stores.model.WorkShiftSchedule;
import java.time.LocalDate; // Vẫn cần cho việc lọc hiển thị theo khoảng ngày nếu bạn muốn
import java.util.List;
import java.util.Optional;

public interface WorkShiftScheduleRepository {
    WorkShiftSchedule save(WorkShiftSchedule schedule);
    boolean update(WorkShiftSchedule schedule); // Có thể chỉ cho update notes
    boolean deleteById(int scheduleId);
    Optional<WorkShiftSchedule> findById(int scheduleId); // Nên JOIN để lấy details
    List<WorkShiftSchedule> findByEmployeeId(int employeeId); // Lấy tất cả các ca trong tuần của NV
    List<WorkShiftSchedule> findByShiftIdAndDayOfWeek(String shiftId, String dayOfWeek); // Để kiểm tra ràng buộc 2NV/ca/thứ
    List<WorkShiftSchedule> findAllWithDetails(); // JOIN để hiển thị
    int deleteAllSchedulesForEmployee(int employeeId); // Khi NV nghỉ việc
    int deleteSchedulesByShiftId(String shiftId); // Khi Loại ca bị xóa (nếu cho phép)

    // Phương thức này vẫn cần nếu bạn muốn hiển thị lịch cho một tuần cụ thể (dựa trên workDate)
    // Hoặc bạn có thể lọc ở tầng service/controller từ danh sách findByEmployeeId
    // List<WorkShiftSchedule> findByEmployeeIdAndDateRange(int employeeId, LocalDate startDate, LocalDate endDate);
}