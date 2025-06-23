package com.example.stores.service.impl;

import com.example.stores.model.Manager; // Cần để kiểm tra ManagerID nếu có
import com.example.stores.model.Warehouse;
import com.example.stores.repository.ManagerRepository; // Cần để kiểm tra ManagerID
import com.example.stores.repository.WarehouseRepository;
// import com.example.stores.repository.impl.ManagerRepositoryImpl; // Để khởi tạo nếu không dùng DI
import com.example.stores.service.WarehouseService;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final ManagerRepository managerRepository; // Để kiểm tra sự tồn tại của Manager nếu managerID được cung cấp

    // Phone regex pattern (Việt Nam)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(03|07|08|09)\\d{8}$");

    public WarehouseServiceImpl(WarehouseRepository warehouseRepository, ManagerRepository managerRepository) {
        this.warehouseRepository = warehouseRepository;
        this.managerRepository = managerRepository;
    }

    @Override
    public Warehouse addWarehouse(Warehouse warehouse) throws IllegalArgumentException {
        validateWarehouseData(warehouse);

        // Kiểm tra managerID nếu được cung cấp
        if (warehouse.getManagerID() != null && warehouse.getManagerID() > 0) {
            if (managerRepository.findById(warehouse.getManagerID()).isEmpty()) {
                throw new IllegalArgumentException("Manager với ID " + warehouse.getManagerID() + " không tồn tại.");
            }
        } else {
            warehouse.setManagerID(null); // Đảm bảo là null nếu không hợp lệ hoặc không cung cấp
        }

        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        if (savedWarehouse == null) {
            throw new RuntimeException("Không thể lưu kho hàng vào CSDL.");
        }
        return savedWarehouse;
    }

    @Override
    public boolean updateWarehouse(Warehouse warehouse) throws IllegalArgumentException {
        if (warehouse.getWarehouseID() <= 0) {
            throw new IllegalArgumentException("ID kho hàng không hợp lệ để cập nhật.");
        }
        validateWarehouseData(warehouse);

        if (warehouseRepository.findById(warehouse.getWarehouseID()).isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy kho hàng với ID: " + warehouse.getWarehouseID());
        }

        // Kiểm tra managerID nếu được cung cấp và thay đổi
        if (warehouse.getManagerID() != null && warehouse.getManagerID() > 0) {
            if (managerRepository.findById(warehouse.getManagerID()).isEmpty()) {
                throw new IllegalArgumentException("Manager với ID " + warehouse.getManagerID() + " không tồn tại.");
            }
        } else {
            warehouse.setManagerID(null);
        }

        return warehouseRepository.update(warehouse);
    }

    @Override
    public boolean deleteWarehouse(int warehouseId) throws IllegalArgumentException {
        if (warehouseRepository.findById(warehouseId).isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy kho hàng với ID: " + warehouseId + " để xóa.");
        }

        // Kiểm tra xem kho có hàng tồn kho không
        long inventoryItemCount = warehouseRepository.countInventoryItemsByWarehouseId(warehouseId);
        if (inventoryItemCount > 0) {
            throw new IllegalArgumentException("Không thể xóa kho hàng này vì đang có " +
                    inventoryItemCount + " mặt hàng tồn kho trong đó. " +
                    "Vui lòng chuyển hoặc xóa hết hàng tồn kho trước.");
        }

        return warehouseRepository.deleteById(warehouseId);
    }

    @Override
    public Optional<Warehouse> getWarehouseById(int warehouseId) {
        return warehouseRepository.findById(warehouseId); // Repository đã JOIN
    }

    @Override
    public List<Warehouse> getAllWarehousesWithManagerName() {
        return warehouseRepository.findAllWithManagerName();
    }

    @Override
    public List<Warehouse> searchWarehouses(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllWarehousesWithManagerName();
        }
        return warehouseRepository.searchWarehouses(keyword.trim());
    }

    private void validateWarehouseData(Warehouse warehouse) throws IllegalArgumentException {
        if (warehouse == null) {
            throw new IllegalArgumentException("Thông tin kho hàng không được null.");
        }
        if (warehouse.getWarehouseName() == null || warehouse.getWarehouseName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên kho hàng không được để trống.");
        }
        if (warehouse.getAddress() == null || warehouse.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ kho hàng không được để trống.");
        }
        // Phone có thể null, nhưng nếu có thì phải đúng định dạng
        if (warehouse.getPhone() != null && !warehouse.getPhone().trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(warehouse.getPhone().trim()).matches()) {
                throw new IllegalArgumentException("Định dạng số điện thoại kho hàng không hợp lệ.");
            }
        }
    }
}