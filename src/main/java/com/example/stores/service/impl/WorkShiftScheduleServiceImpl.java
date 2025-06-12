package com.example.stores.service.impl;

// ... imports ...
import com.example.stores.model.Employee;
import com.example.stores.model.Shift;
import com.example.stores.model.WorkShiftSchedule;
import com.example.stores.repository.EmployeeRepository;
import com.example.stores.repository.ShiftRepository;
import com.example.stores.repository.WorkShiftScheduleRepository;
import com.example.stores.service.WorkShiftScheduleService;

import java.util.List;
import java.util.Optional;

public class WorkShiftScheduleServiceImpl implements WorkShiftScheduleService {

    private final WorkShiftScheduleRepository workShiftScheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;

    public WorkShiftScheduleServiceImpl(WorkShiftScheduleRepository workShiftScheduleRepository,
                                        EmployeeRepository employeeRepository,
                                        ShiftRepository shiftRepository) {
        this.workShiftScheduleRepository = workShiftScheduleRepository;
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    public WorkShiftSchedule addSchedule(WorkShiftSchedule schedule) throws IllegalArgumentException {
        if (schedule == null) throw new IllegalArgumentException("Dữ liệu lịch phân công không được null.");
        if (schedule.getEmployeeID() <= 0) throw new IllegalArgumentException("ID nhân viên không hợp lệ.");
        if (schedule.getShiftID() == null || schedule.getShiftID().trim().isEmpty()) throw new IllegalArgumentException("Loại ca không được để trống.");
        if (schedule.getDayOfWeek() == null || schedule.getDayOfWeek().trim().isEmpty()) throw new IllegalArgumentException("Thứ làm việc không được để trống.");

        Optional<Employee> employeeOpt = employeeRepository.findById(schedule.getEmployeeID());
        if (employeeOpt.isEmpty()) {
            throw new IllegalArgumentException("Nhân viên với ID " + schedule.getEmployeeID() + " không tồn tại.");
        }
        if (!"Đang làm".equalsIgnoreCase(employeeOpt.get().getStatus())) {
            throw new IllegalArgumentException("Không thể phân công cho nhân viên '" + employeeOpt.get().getFullName() + "' vì họ không ở trạng thái 'Đang làm'.");
        }

        if (shiftRepository.findById(schedule.getShiftID()).isEmpty()) {
            throw new IllegalArgumentException("Loại ca '" + schedule.getShiftID() + "' không tồn tại.");
        }

        // Kiểm tra trùng lặp (1 NV không thể làm 2 ca cùng loại, cùng thứ) - DB đã có UNIQUE constraint
        // UQ_EmployeeWeeklyShift UNIQUE (employeeID, shiftID, dayOfWeek)
        // Tuy nhiên, chúng ta có thể kiểm tra trước để đưa ra thông báo thân thiện hơn
        // boolean alreadyExists = workShiftScheduleRepository.existsByEmployeeIdAndShiftIdAndDayOfWeek(
        // schedule.getEmployeeID(), schedule.getShiftID(), schedule.getDayOfWeek());
        // if(alreadyExists) {
        // throw new IllegalArgumentException("Nhân viên " + employeeOpt.get().getFullName() +
        // " đã được phân công vào ca " + schedule.getShiftID() + " cho " + schedule.getDayOfWeek() + ".");
        // }


        // Kiểm tra ràng buộc: tối đa 2 nhân viên trong 1 loại ca, 1 thứ
        List<WorkShiftSchedule> existingSchedulesForShiftAndDay =
                workShiftScheduleRepository.findByShiftIdAndDayOfWeek(schedule.getShiftID(), schedule.getDayOfWeek());
        if (existingSchedulesForShiftAndDay.size() >= 2) {
            // Kiểm tra xem nhân viên này đã có trong danh sách đó chưa (trường hợp sửa)
            // Nhưng đây là addSchedule, nên chỉ cần check size >= 2
            throw new IllegalArgumentException("Ca " + schedule.getShiftID() + " vào " + schedule.getDayOfWeek() +
                    " đã có đủ 2 nhân viên. Không thể thêm.");
        }


        WorkShiftSchedule savedSchedule = workShiftScheduleRepository.save(schedule);
        if (savedSchedule == null) {
            // Lỗi có thể do UNIQUE constraint (employeeID, shiftID, dayOfWeek) nếu không check trước
            throw new RuntimeException("Không thể lưu lịch phân công. Có thể nhân viên đã được phân vào ca này trong ngày này.");
        }
        return savedSchedule;
    }

    @Override
    public boolean updateSchedule(WorkShiftSchedule schedule) throws IllegalArgumentException {
        // Khi update, thường chỉ cho phép update "notes".
        // Việc thay đổi employeeID, shiftID, dayOfWeek sẽ giống như tạo một phân công mới và xóa cái cũ.
        // Nếu bạn cho phép sửa các trường đó, cần validate tương tự addSchedule và kiểm tra UNIQUE phức tạp hơn.
        if (schedule == null || schedule.getScheduleID() <= 0) {
            throw new IllegalArgumentException("Lịch phân công không hợp lệ để cập nhật.");
        }
        Optional<WorkShiftSchedule> existingOpt = workShiftScheduleRepository.findById(schedule.getScheduleID());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy lịch phân công với ID: " + schedule.getScheduleID());
        }
        // Giả sử chỉ cho update notes:
        WorkShiftSchedule existingSchedule = existingOpt.get();
        existingSchedule.setNotes(schedule.getNotes());
        return workShiftScheduleRepository.update(existingSchedule);
    }

    @Override
    public boolean deleteSchedule(int scheduleId) throws IllegalArgumentException {
        if (workShiftScheduleRepository.findById(scheduleId).isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy lịch phân công với ID: " + scheduleId);
        }
        return workShiftScheduleRepository.deleteById(scheduleId);
    }

    // ... (giữ nguyên getScheduleById, getSchedulesByEmployeeId, getAllSchedulesWithDetails) ...
    @Override
    public Optional<WorkShiftSchedule> getScheduleById(int scheduleId) {
        return workShiftScheduleRepository.findById(scheduleId);
    }

    @Override
    public List<WorkShiftSchedule> getSchedulesByEmployeeId(int employeeId) {
        return workShiftScheduleRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<WorkShiftSchedule> getAllSchedulesWithDetails() {
        return workShiftScheduleRepository.findAllWithDetails();
    }


    @Override
    public int removeAllSchedulesForEmployee(int employeeId) {
        return workShiftScheduleRepository.deleteAllSchedulesForEmployee(employeeId);
    }

    @Override
    public int removeSchedulesByShiftId(String shiftId) {
        return workShiftScheduleRepository.deleteSchedulesByShiftId(shiftId);
    }
}