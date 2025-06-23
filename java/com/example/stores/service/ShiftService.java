package com.example.stores.service;

import com.example.stores.model.Shift;
import java.util.List;
import java.util.Optional;

public interface ShiftService {
    Shift addShift(Shift shift) throws IllegalArgumentException; // Chỉ thêm nếu bạn cho phép Manager thêm loại ca mới
    boolean updateShift(Shift shift) throws IllegalArgumentException; // Chỉ thêm nếu cho phép sửa loại ca mặc định
    boolean deleteShift(String shiftId) throws IllegalArgumentException, RuntimeException; // Cẩn thận khi xóa ca mặc định
    Optional<Shift> getShiftById(String shiftId);
    List<Shift> getAllShifts(); // Quan trọng để lấy danh sách ca cho ComboBox
}