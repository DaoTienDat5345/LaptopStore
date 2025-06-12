package com.example.stores.service;

import com.example.stores.model.WorkShiftSchedule;
import java.util.List;
import java.util.Optional;

public interface WorkShiftScheduleService {
    WorkShiftSchedule addSchedule(WorkShiftSchedule schedule) throws IllegalArgumentException;
    boolean updateSchedule(WorkShiftSchedule schedule) throws IllegalArgumentException; // Có thể chỉ cho update notes
    boolean deleteSchedule(int scheduleId) throws IllegalArgumentException;
    Optional<WorkShiftSchedule> getScheduleById(int scheduleId);
    List<WorkShiftSchedule> getSchedulesByEmployeeId(int employeeId);
    List<WorkShiftSchedule> getAllSchedulesWithDetails();
    // Không còn removeFutureSchedulesForEmployee, mà là:
    int removeAllSchedulesForEmployee(int employeeId);
    int removeSchedulesByShiftId(String shiftId); // Giữ lại nếu cho phép xóa Shift
}