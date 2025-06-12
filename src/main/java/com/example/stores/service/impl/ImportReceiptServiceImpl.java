package com.example.stores.service.impl;

import com.example.stores.model.*; // Import các model cần thiết
import com.example.stores.repository.*; // Import các repository cần thiết
import com.example.stores.service.ImportReceiptService;
import com.example.stores.service.InventoryService; // Cần để cập nhật tồn kho
import com.example.stores.service.ProductService;   // Cần để cập nhật tổng số lượng sản phẩm

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ImportReceiptServiceImpl implements ImportReceiptService {

    private final ImportReceiptRepository importReceiptRepository;
    private final SupplierRepository supplierRepository;
    private final EmployeeRepository employeeRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository; // Để kiểm tra sản phẩm và cập nhật tổng số lượng
    private final InventoryService inventoryService;   // Để cập nhật tồn kho chi tiết theo kho

    public ImportReceiptServiceImpl(ImportReceiptRepository importReceiptRepository,
                                    SupplierRepository supplierRepository,
                                    EmployeeRepository employeeRepository,
                                    WarehouseRepository warehouseRepository,
                                    ProductRepository productRepository,
                                    InventoryService inventoryService) {
        this.importReceiptRepository = importReceiptRepository;
        this.supplierRepository = supplierRepository;
        this.employeeRepository = employeeRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    // Trong ImportReceiptServiceImpl.java
    @Override
    public ImportReceipt createImportReceipt(ImportReceipt importReceipt) throws IllegalArgumentException, RuntimeException {
        // 1. Validate thông tin cơ bản (giữ nguyên)
        if (importReceipt == null) throw new IllegalArgumentException("Thông tin phiếu nhập không được null.");
        if (supplierRepository.findById(importReceipt.getSupplierID()).isEmpty())
            throw new IllegalArgumentException("Nhà cung cấp ID " + importReceipt.getSupplierID() + " không tồn tại.");
        if (employeeRepository.findById(importReceipt.getEmployeeID()).isEmpty())
            throw new IllegalArgumentException("Nhân viên ID " + importReceipt.getEmployeeID() + " không tồn tại.");
        if (warehouseRepository.findById(importReceipt.getWarehouseID()).isEmpty())
            throw new IllegalArgumentException("Kho hàng ID " + importReceipt.getWarehouseID() + " không tồn tại.");
        if (importReceipt.getDetails() == null || importReceipt.getDetails().isEmpty())
            throw new IllegalArgumentException("Phiếu nhập phải có ít nhất một sản phẩm.");

        // 2. Validate và tính toán chi tiết (giữ nguyên)
        BigDecimal calculatedTotalAmount = BigDecimal.ZERO;
        for (ImportReceiptDetail detail : importReceipt.getDetails()) {
            if (detail.getProductID() == null || detail.getProductID().trim().isEmpty())
                throw new IllegalArgumentException("Mã sản phẩm trong chi tiết không được để trống.");
            Optional<Product> productOpt = productRepository.findById(detail.getProductID());
            if (productOpt.isEmpty())
                throw new IllegalArgumentException("Sản phẩm ID '" + detail.getProductID() + "' không tồn tại.");
            if (detail.getQuantity() <= 0)
                throw new IllegalArgumentException("Số lượng nhập cho SP '" + productOpt.get().getProductName() + "' phải > 0.");
            if (detail.getUnitCost() == null || detail.getUnitCost().compareTo(BigDecimal.ZERO) < 0)
                throw new IllegalArgumentException("Đơn giá nhập cho SP '" + productOpt.get().getProductName() + "' không hợp lệ.");
            calculatedTotalAmount = calculatedTotalAmount.add(detail.getSubtotal());
        }
        importReceipt.setTotalAmount(calculatedTotalAmount);
        if (importReceipt.getImportDate() == null) importReceipt.setImportDate(LocalDateTime.now());

        // 3. Lưu phiếu nhập (NẾU LỖI Ở ĐÂY, REPO SẼ TRẢ VỀ NULL)
        ImportReceipt savedReceipt = importReceiptRepository.save(importReceipt);
        if (savedReceipt == null) {
            // Lỗi này sẽ được hiển thị trên UI nếu Controller bắt RuntimeException
            throw new RuntimeException("Lỗi từ CSDL: Không thể lưu phiếu nhập. Vui lòng kiểm tra console để biết chi tiết lỗi SQL.");
        }

        // 4. Cập nhật tồn kho (NẾU LỖI Ở ĐÂY, CẦN XEM XÉT ROLLBACK PHIẾU NHẬP ĐÃ TẠO)
        try {
            for (ImportReceiptDetail detail : savedReceipt.getDetails()) {
                boolean stockUpdated = inventoryService.updateStock(
                        savedReceipt.getWarehouseID(),
                        detail.getProductID(),
                        detail.getQuantity()
                );
                if (!stockUpdated) {
                    // Đây là tình huống khó: phiếu đã lưu nhưng tồn kho chưa cập nhật.
                    // Cần một cơ chế rollback phức tạp hơn hoặc ghi nhận lỗi để xử lý thủ công.
                    System.err.println("LỖI NGHIÊM TRỌNG: Đã lưu phiếu nhập ID " + savedReceipt.getReceiptID() +
                            " nhưng không thể cập nhật tồn kho cho SP " + detail.getProductID() +
                            " tại kho " + savedReceipt.getWarehouseID());
                    throw new RuntimeException("Lỗi cập nhật tồn kho cho SP ID: " + detail.getProductID() +
                            ". Phiếu nhập ID: " + savedReceipt.getReceiptID() +
                            " đã được tạo nhưng tồn kho có thể chưa đúng.");
                }
                // Cập nhật tổng số lượng trong bảng Products
                Optional<Product> productOpt = productRepository.findById(detail.getProductID());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    product.setQuantity(product.getQuantity() + detail.getQuantity());
                    productRepository.update(product);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi xảy ra SAU KHI lưu phiếu nhập, trong quá trình cập nhật tồn kho: " + e.getMessage());
            e.printStackTrace(); // In lỗi này ra console
            // TODO: Implement robust rollback logic for ImportReceipt if stock update fails.
            throw new RuntimeException("Phiếu nhập ID: " + savedReceipt.getReceiptID() + " đã tạo, nhưng có lỗi khi cập nhật tồn kho: " + e.getMessage(), e);
        }
        return savedReceipt;
    }

    @Override
    public boolean deleteSingleImportReceipt(int receiptId) throws IllegalArgumentException, RuntimeException {
        // 1. Lấy thông tin phiếu nhập đầy đủ (bao gồm chi tiết) để rollback tồn kho
        Optional<ImportReceipt> receiptOpt = importReceiptRepository.findById(receiptId);
        if (receiptOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy phiếu nhập với ID: " + receiptId + " để xóa.");
        }
        ImportReceipt receiptToDelete = receiptOpt.get();

        // 2. Rollback tồn kho cho từng sản phẩm trong chi tiết
        try {
            if (receiptToDelete.getDetails() != null) {
                for (ImportReceiptDetail detail : receiptToDelete.getDetails()) {
                    // Trừ đi số lượng đã nhập (quantityChange sẽ là số âm)
                    boolean stockRolledBack = inventoryService.updateStock(
                            receiptToDelete.getWarehouseID(),
                            detail.getProductID(),
                            -detail.getQuantity() // Trừ đi số lượng
                    );
                    if (!stockRolledBack) {
                        System.err.println("LỖI NGHIÊM TRỌNG: Không thể rollback tồn kho cho SP " +
                                detail.getProductID() + " từ phiếu nhập ID " + receiptId);
                        // Quyết định: Có nên dừng và báo lỗi không? Hay tiếp tục xóa phiếu?
                        // Để an toàn, nên ném lỗi và không xóa phiếu nếu rollback tồn kho thất bại.
                        throw new RuntimeException("Lỗi khi rollback tồn kho cho SP " + detail.getProductID() +
                                ". Thao tác xóa phiếu nhập đã bị hủy.");
                    }
                    // Cập nhật lại tổng số lượng trong bảng Products
                    Optional<Product> productOpt = productRepository.findById(detail.getProductID());
                    if (productOpt.isPresent()) {
                        Product product = productOpt.get();
                        int newTotalQuantity = product.getQuantity() - detail.getQuantity();
                        product.setQuantity(Math.max(0, newTotalQuantity)); // Đảm bảo không âm
                        productRepository.update(product);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi trong quá trình rollback tồn kho khi xóa phiếu nhập ID " + receiptId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Đã xảy ra lỗi khi cố gắng cập nhật lại tồn kho. Thao tác xóa phiếu nhập đã bị hủy: " + e.getMessage(), e);
        }

        // 3. Xóa phiếu nhập và chi tiết của nó từ CSDL
        // (Repository sẽ xử lý transaction cho việc xóa header và details nếu cần)
        boolean deleted = importReceiptRepository.deleteReceiptAndItsDetailsById(receiptId);
        if (!deleted) {
            // Lỗi này không nên xảy ra nếu bước rollback tồn kho đã thành công và phiếu nhập vẫn tồn tại
            throw new RuntimeException("Không thể xóa phiếu nhập ID: " + receiptId + " khỏi CSDL sau khi đã rollback tồn kho.");
        }
        return true;
    }

    @Override
    public Optional<ImportReceipt> getImportReceiptById(int receiptId) {
        return importReceiptRepository.findById(receiptId); // Repository đã JOIN
    }

    @Override
    public List<ImportReceipt> getAllImportReceiptsWithDetails() {
        return importReceiptRepository.findAllWithDetails();
    }

    @Override
    public List<ImportReceipt> getImportReceiptsByDateRangeWithDetails(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Khoảng ngày không hợp lệ.");
        }
        return importReceiptRepository.findByDateRangeWithDetails(startDate, endDate);
    }

    @Override
    public List<ImportReceipt> getImportReceiptsBySupplierIdWithDetails(int supplierId) {
        if (supplierRepository.findById(supplierId).isEmpty()) {
            throw new IllegalArgumentException("Nhà cung cấp với ID " + supplierId + " không tồn tại.");
        }
        return importReceiptRepository.findBySupplierIdWithDetails(supplierId);
    }

    @Override
    public List<ImportReceipt> getImportReceiptsByWarehouseIdWithDetails(int warehouseId) {
        if (warehouseRepository.findById(warehouseId).isEmpty()) {
            throw new IllegalArgumentException("Kho hàng với ID " + warehouseId + " không tồn tại.");
        }
        return importReceiptRepository.findByWarehouseIdWithDetails(warehouseId);
    }
}