package com.example.stores.service.impl;

import com.example.stores.repository.CustomerRepository;
import com.example.stores.service.CustomerService;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(03|07|09)\\d{8}$"); // Khớp với CSDL

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Optional<Customer> getCustomerById(int customerId) {
        return customerRepository.findById(customerId);
    }


    @Override
    public List<Customer> searchCustomers(String keyword, Boolean isActiveFilter) {
        // Repository sẽ xử lý keyword null/empty
        return customerRepository.searchCustomers(keyword, isActiveFilter);
    }

    @Override
    public boolean updateCustomerInfo(Customer customer) throws IllegalArgumentException {
        if (customer == null || customer.getCustomerID() <= 0) {
            throw new IllegalArgumentException("Thông tin khách hàng hoặc ID không hợp lệ.");
        }
        validateCustomerData(customer); // Validate các trường được phép sửa

        Optional<Customer> existingOpt = customerRepository.findById(customer.getCustomerID());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + customer.getCustomerID());
        }
        Customer existingCustomer = existingOpt.get();

        // Kiểm tra UNIQUE nếu email/phone thay đổi
        if (customer.getEmail() != null && !customer.getEmail().equalsIgnoreCase(existingCustomer.getEmail())) {
            if (customerRepository.findByEmail(customer.getEmail().trim()).isPresent()) {
                throw new IllegalArgumentException("Email mới đã được sử dụng.");
            }
        }
        if (customer.getPhone() != null && !customer.getPhone().equals(existingCustomer.getPhone())) {
            if (customerRepository.findByPhone(customer.getPhone().trim()).isPresent()) {
                throw new IllegalArgumentException("Số điện thoại mới đã được sử dụng.");
            }
        }
        // Giữ lại username và password không đổi từ Manager
        customer.setUsername(existingCustomer.getUsername());
        customer.setPassword(existingCustomer.getPassword());
        customer.setRegisteredAt(existingCustomer.getRegisteredAt()); // Không cho sửa ngày đăng ký

        return customerRepository.update(customer);
    }

    @Override
    public boolean setCustomerActiveStatus(int customerId, boolean isActive) throws IllegalArgumentException {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + customerId);
        }
        Customer customer = customerOpt.get();
        customer.setActive(isActive);
        return customerRepository.update(customer); // Dùng lại hàm update của repo
    }

    private void validateCustomerData(Customer customer) throws IllegalArgumentException {
        if (customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên khách hàng không được để trống.");
        }
        if (customer.getEmail() == null || !EMAIL_PATTERN.matcher(customer.getEmail().trim()).matches()) {
            throw new IllegalArgumentException("Định dạng email không hợp lệ.");
        }
        if (customer.getPhone() == null || !PHONE_PATTERN.matcher(customer.getPhone().trim()).matches()) {
            throw new IllegalArgumentException("Định dạng số điện thoại không hợp lệ.");
        }
        // Các validation khác cho gender, birthDate, address nếu cần
    }

    @Override
    public List<Customer> getAllActiveCustomers() {
        // Nếu CustomerRepository.findAll() đã lọc theo isActive=1 thì dùng trực tiếp
        // return customerRepository.findAll();

        // Nếu CustomerRepository.findAll() lấy tất cả, bạn cần lọc ở đây:
        return customerRepository.findAll().stream()
                .filter(Customer::isActive) // Hoặc .filter(c -> c.isActive())
                .collect(Collectors.toList());
    }

    @Override
    public List<Customer> getAllCustomers() { // Triển khai hàm lấy tất cả
        return customerRepository.findAll();
    }
}