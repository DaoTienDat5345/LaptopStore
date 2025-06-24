package com.example.stores.service.impl;

import com.example.stores.repository.SupplierRepository;
import com.example.stores.service.SupplierService;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    // Regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(03|07|08|09)\\d{8}$"); // Điều chỉnh lại nếu cần, CSDL là 03,07,09

    public SupplierServiceImpl(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Override
    public Supplier addSupplier(Supplier supplier) throws IllegalArgumentException {
        validateSupplierData(supplier);

        // Kiểm tra trùng email (nếu email là UNIQUE)
        if (supplier.getEmail() != null && supplierRepository.findByEmail(supplier.getEmail().trim()).isPresent()) {
            throw new IllegalArgumentException("Email '" + supplier.getEmail() + "' đã được sử dụng.");
        }
        // Kiểm tra trùng phone (nếu phone là UNIQUE)
        if (supplier.getPhone() != null && supplierRepository.findByPhone(supplier.getPhone().trim()).isPresent()) {
            throw new IllegalArgumentException("Số điện thoại '" + supplier.getPhone() + "' đã được sử dụng.");
        }


        Supplier savedSupplier = supplierRepository.save(supplier);
        if (savedSupplier == null) {
            throw new RuntimeException("Không thể lưu nhà cung cấp vào CSDL.");
        }
        return savedSupplier;
    }

    @Override
    public boolean updateSupplier(Supplier supplier) throws IllegalArgumentException {
        if (supplier.getSupplierID() <= 0) {
            throw new IllegalArgumentException("ID nhà cung cấp không hợp lệ để cập nhật.");
        }
        validateSupplierData(supplier);

        Optional<Supplier> existingSupplierOpt = supplierRepository.findById(supplier.getSupplierID());
        if (existingSupplierOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy nhà cung cấp với ID: " + supplier.getSupplierID());
        }
        Supplier existingSupplier = existingSupplierOpt.get();

        // Kiểm tra trùng email nếu email thay đổi
        if (supplier.getEmail() != null && !supplier.getEmail().equalsIgnoreCase(existingSupplier.getEmail())) {
            if (supplierRepository.findByEmail(supplier.getEmail().trim()).isPresent()) {
                throw new IllegalArgumentException("Email mới '" + supplier.getEmail() + "' đã được sử dụng.");
            }
        }
        // Kiểm tra trùng phone nếu phone thay đổi
        if (supplier.getPhone() != null && !supplier.getPhone().equals(existingSupplier.getPhone())) {
            if (supplierRepository.findByPhone(supplier.getPhone().trim()).isPresent()) {
                throw new IllegalArgumentException("Số điện thoại mới '" + supplier.getPhone() + "' đã được sử dụng.");
            }
        }

        return supplierRepository.update(supplier);
    }

    @Override
    public boolean deleteSupplier(int supplierId) throws IllegalArgumentException {
        if (supplierRepository.findById(supplierId).isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy nhà cung cấp với ID: " + supplierId);
        }
        // Kiểm tra xem nhà cung cấp có phiếu nhập nào không
        long receiptCount = supplierRepository.countImportReceiptsBySupplierId(supplierId);
        if (receiptCount > 0) {
            throw new IllegalArgumentException("Không thể xóa nhà cung cấp này vì đang có " + receiptCount +
                    " phiếu nhập hàng liên quan. Vui lòng xóa các phiếu nhập trước.");
        }
        return supplierRepository.deleteById(supplierId);
    }

    @Override
    public Optional<Supplier> getSupplierById(int supplierId) {
        return supplierRepository.findById(supplierId);
    }

    @Override
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    @Override
    public List<Supplier> searchSuppliers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllSuppliers();
        }
        return supplierRepository.searchSuppliers(keyword.trim());
    }

    private void validateSupplierData(Supplier supplier) throws IllegalArgumentException {
        if (supplier == null) {
            throw new IllegalArgumentException("Thông tin nhà cung cấp không được null.");
        }
        if (supplier.getSupplierName() == null || supplier.getSupplierName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống.");
        }
        if (supplier.getEmail() == null || !EMAIL_PATTERN.matcher(supplier.getEmail().trim()).matches()) {
            throw new IllegalArgumentException("Định dạng email không hợp lệ.");
        }
        if (supplier.getPhone() == null || !PHONE_PATTERN.matcher(supplier.getPhone().trim()).matches()) {
            // CSDL của bạn là 03, 07, 09. Pattern hiện tại có 08. Cần điều chỉnh nếu muốn khớp 100% CSDL.
            // Nếu CSDL đã có CHECK constraint, thì lỗi từ DB sẽ xảy ra trước.
            throw new IllegalArgumentException("Định dạng số điện thoại không hợp lệ (10 số, bắt đầu bằng 03, 07, 09).");
        }
        // Address, TaxCode có thể null hoặc trống tùy theo yêu cầu
    }
}