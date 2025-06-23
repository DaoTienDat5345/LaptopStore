package com.example.stores.service.impl;

import com.example.stores.model.Shift;
import com.example.stores.repository.ShiftRepository;
import com.example.stores.repository.WorkShiftScheduleRepository;
import com.example.stores.service.ShiftService;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final WorkShiftScheduleRepository workShiftScheduleRepository;

    public ShiftServiceImpl(ShiftRepository shiftRepository, WorkShiftScheduleRepository workShiftScheduleRepository) {
        this.shiftRepository = shiftRepository;
        this.workShiftScheduleRepository = workShiftScheduleRepository;
    }

    @Override
    public Shift addShift(Shift shift) throws IllegalArgumentException {
        validateShiftData(shift);
        if (shiftRepository.findById(shift.getShiftID()).isPresent()) {
            throw new IllegalArgumentException("Mã ca làm việc (Shift ID) '" + shift.getShiftID() + "' đã tồn tại.");
        }
        Shift savedShift = shiftRepository.save(shift);
        if (savedShift == null) {
            throw new RuntimeException("Không thể lưu ca làm việc vào CSDL.");
        }
        return savedShift;
    }

    @Override
    public boolean updateShift(Shift shift) throws IllegalArgumentException {
        validateShiftData(shift);
        if (shiftRepository.findById(shift.getShiftID()).isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy ca làm việc với ID: " + shift.getShiftID() + " để cập nhật.");
        }
        return shiftRepository.update(shift);
    }

    @Override
    public boolean deleteShift(String shiftId) throws IllegalArgumentException, RuntimeException {
        if (shiftRepository.findById(shiftId).isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy ca làm việc với ID: " + shiftId + " để xóa.");
        }
        // Logic kiểm tra ràng buộc với WorkShiftSchedule (ví dụ, sử dụng ON DELETE CASCADE trong DB)
        // Hoặc nếu bạn muốn xử lý ở đây:
        // List<WorkShiftSchedule> schedulesUsingShift = workShiftScheduleRepository.findByShiftId(shiftId);
        // if (!schedulesUsingShift.isEmpty()) {
        //     throw new IllegalArgumentException("Không thể xóa loại ca '" + shiftId + "' vì đang có nhân viên được phân công vào ca này.");
        // }
        // Hoặc xóa các WorkShiftSchedule liên quan:
        // workShiftScheduleRepository.deleteSchedulesByShiftId(shiftId);

        return shiftRepository.deleteById(shiftId);
    }

    @Override
    public Optional<Shift> getShiftById(String shiftId) {
        return shiftRepository.findById(shiftId);
    }

    @Override
    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    private void validateShiftData(Shift shift) throws IllegalArgumentException {
        if (shift == null) {
            throw new IllegalArgumentException("Thông tin ca làm việc không được null.");
        }
        if (shift.getShiftID() == null || shift.getShiftID().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã ca làm việc (Shift ID) không được để trống.");
        }
        if (shift.getShiftName() == null || shift.getShiftName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên ca làm việc không được để trống.");
        }
        if (shift.getStartTime() == null || shift.getEndTime() == null) {
            throw new IllegalArgumentException("Giờ bắt đầu và giờ kết thúc không được để trống.");
        }
        if (shift.getStartTime().isAfter(shift.getEndTime()) && !shift.getEndTime().equals(LocalTime.MIDNIGHT)) {
            if(!(shift.getEndTime().equals(LocalTime.MIDNIGHT) && shift.getStartTime().isAfter(LocalTime.NOON))) {
                throw new IllegalArgumentException("Giờ bắt đầu không thể sau giờ kết thúc (trừ ca qua đêm).");
            }
        }
        // Không còn validate dayOfWeek ở đây
    }
}