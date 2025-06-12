package com.example.stores.repository;

import com.example.stores.model.Shift;
import java.util.List;
import java.util.Optional;

public interface ShiftRepository {
    Shift save(Shift shift); // Giả sử shiftID là duy nhất và được cung cấp
    boolean update(Shift shift);
    boolean deleteById(String shiftId); // Cần cẩn thận với ràng buộc khóa ngoại
    Optional<Shift> findById(String shiftId);
    List<Shift> findAll();
}