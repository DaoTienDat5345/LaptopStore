package com.example.stores.service.impl;

import com.example.stores.model.Inventory;
import com.example.stores.model.Product; // Cần để kiểm tra sản phẩm
import com.example.stores.model.Warehouse; // Cần để kiểm tra kho
import com.example.stores.repository.InventoryRepository;
import com.example.stores.repository.ProductRepository;   // Inject để kiểm tra sản phẩm
import com.example.stores.repository.WarehouseRepository; // Inject để kiểm tra kho
import com.example.stores.service.InventoryService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                ProductRepository productRepository,
                                WarehouseRepository warehouseRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public Inventory addInventoryRecord(Inventory inventory) throws IllegalArgumentException {
        validateInventoryData(inventory);
        // Kiểm tra sự tồn tại của Product và Warehouse
        if (productRepository.findById(inventory.getProductID()).isEmpty()) {
            throw new IllegalArgumentException("Sản phẩm với ID " + inventory.getProductID() + " không tồn tại.");
        }
        if (warehouseRepository.findById(inventory.getWarehouseID()).isEmpty()) {
            throw new IllegalArgumentException("Kho hàng với ID " + inventory.getWarehouseID() + " không tồn tại.");
        }
        // Kiểm tra xem bản ghi đã tồn tại chưa (warehouseId, productId là UNIQUE)
        if(inventoryRepository.findByWarehouseIdAndProductId(inventory.getWarehouseID(), inventory.getProductID()).isPresent()){
            throw new IllegalArgumentException("Đã có bản ghi tồn kho cho sản phẩm " + inventory.getProductID() +
                    " tại kho " + inventory.getWarehouseID() + ". Sử dụng chức năng cập nhật số lượng.");
        }

        inventory.setLastUpdate(LocalDateTime.now());
        Inventory savedInventory = inventoryRepository.save(inventory);
        if (savedInventory == null) {
            throw new RuntimeException("Không thể lưu bản ghi tồn kho vào CSDL.");
        }
        return savedInventory;
    }

    @Override
    public boolean adjustStockQuantityManually(int inventoryId, int newQuantity, String notes) throws IllegalArgumentException {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Số lượng mới không thể âm.");
        }
        Optional<Inventory> inventoryOpt = inventoryRepository.findById(inventoryId);
        if (inventoryOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy bản ghi tồn kho với ID: " + inventoryId);
        }
        Inventory inventory = inventoryOpt.get();
        inventory.setQuantity(newQuantity);
        // inventory.setNotes(notes); // Nếu model Inventory có trường notes cho việc điều chỉnh
        inventory.setLastUpdate(LocalDateTime.now());
        // Ghi lại lịch sử điều chỉnh nếu cần
        return inventoryRepository.update(inventory);
    }

    @Override
    public Optional<Inventory> getInventoryRecordById(int inventoryId) {
        return inventoryRepository.findById(inventoryId); // Repo đã JOIN
    }

    @Override
    public Optional<Inventory> getStockByWarehouseAndProduct(int warehouseId, String productId) {
        // Phương thức này trong repo không join, chỉ lấy dữ liệu thô
        return inventoryRepository.findByWarehouseIdAndProductId(warehouseId, productId);
    }


    @Override
    public List<Inventory> getAllInventoryWithDetails() {
        return inventoryRepository.findAllWithDetails();
    }

    @Override
    public List<Inventory> getInventoryByWarehouseWithDetails(int warehouseId) {
        if (warehouseRepository.findById(warehouseId).isEmpty()) {
            throw new IllegalArgumentException("Kho hàng với ID " + warehouseId + " không tồn tại.");
        }
        return inventoryRepository.findByWarehouseIdWithDetails(warehouseId);
    }

    @Override
    public List<Inventory> getInventoryByProductWithDetails(String productId) {
        if (productRepository.findById(productId).isEmpty()) {
            throw new IllegalArgumentException("Sản phẩm với ID " + productId + " không tồn tại.");
        }
        return inventoryRepository.findByProductIdWithDetails(productId);
    }

    @Override
    public boolean updateStock(int warehouseId, String productId, int quantityChange) throws IllegalArgumentException {
        // Kiểm tra sự tồn tại của Product và Warehouse
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Sản phẩm với ID " + productId + " không tồn tại để cập nhật tồn kho.");
        }
        if (warehouseRepository.findById(warehouseId).isEmpty()) {
            throw new IllegalArgumentException("Kho hàng với ID " + warehouseId + " không tồn tại để cập nhật tồn kho.");
        }

        // Logic cập nhật hoặc tạo mới bản ghi tồn kho
        Optional<Inventory> existingInventoryOpt = inventoryRepository.findByWarehouseIdAndProductId(warehouseId, productId);

        if (existingInventoryOpt.isPresent()) {
            Inventory existingInventory = existingInventoryOpt.get();
            int currentQuantity = existingInventory.getQuantity();
            int newQuantity = currentQuantity + quantityChange;

            if (newQuantity < 0) {
                throw new IllegalArgumentException("Số lượng tồn kho không thể âm. Sản phẩm '" + productId +
                        "' tại kho " + warehouseId + " hiện có " + currentQuantity +
                        ", không thể giảm " + Math.abs(quantityChange) + ".");
            }
            existingInventory.setQuantity(newQuantity);
            // lastUpdate sẽ được tự động cập nhật trong repository.update() hoặc ở đây
            // existingInventory.setLastUpdate(LocalDateTime.now());
            return inventoryRepository.update(existingInventory);
        } else {
            // Nếu sản phẩm chưa có trong kho này, chỉ cho phép thêm nếu quantityChange là dương (nhập hàng)
            if (quantityChange < 0) {
                throw new IllegalArgumentException("Sản phẩm '" + productId + "' không có trong kho " +
                        warehouseId + " để thực hiện xuất kho.");
            }
            Inventory newInventory = new Inventory();
            newInventory.setWarehouseID(warehouseId);
            newInventory.setProductID(productId);
            newInventory.setQuantity(quantityChange);
            newInventory.setLastUpdate(LocalDateTime.now());
            return inventoryRepository.save(newInventory) != null;
        }
    }

    private void validateInventoryData(Inventory inventory) throws IllegalArgumentException {
        if (inventory == null) {
            throw new IllegalArgumentException("Dữ liệu tồn kho không được null.");
        }
        if (inventory.getWarehouseID() <= 0) {
            throw new IllegalArgumentException("ID kho hàng không hợp lệ.");
        }
        if (inventory.getProductID() == null || inventory.getProductID().trim().isEmpty()) {
            throw new IllegalArgumentException("ID sản phẩm không được để trống.");
        }
        if (inventory.getQuantity() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm.");
        }
    }
}