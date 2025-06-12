package com.example.stores.service.impl;

import com.example.stores.model.Customer;
import com.example.stores.repository.impl.CustomerRepository;

public class AuthService {
    
    private CustomerRepository customerRepository;
    private static Customer currentCustomer;
    private static AuthService instance;
    
    public AuthService() {
        customerRepository = new CustomerRepository();
    }
    
    public boolean login(String username, String password) {
        Customer customer = customerRepository.authenticate(username, password);
        if (customer != null) {
            setCurrentCustomer(customer);
            return true;
        }
        return false;
    }
    
    public boolean register(Customer customer) {
        // Check if username already exists
        Customer existingUser = customerRepository.findByUsername(customer.getUsername());
        if (existingUser != null) {
            return false;
        }
        
        return customerRepository.register(customer);
    }
    
    public static Customer getCurrentCustomer() {
        return currentCustomer;
    }
    
    private static void setCurrentCustomer(Customer customer) {
        currentCustomer = customer;
    }
    
    public void logout() {
        currentCustomer = null;
    }
    
    /**
     * Kiểm tra xem email đã tồn tại trong cơ sở dữ liệu chưa
     * @param email Email cần kiểm tra
     * @return true nếu email đã tồn tại
     */
    public boolean isEmailExists(String email) {
        return customerRepository.isEmailExists(email);
    }
    
    /**
     * Kiểm tra xem số điện thoại đã tồn tại trong cơ sở dữ liệu chưa
     * @param phone Số điện thoại cần kiểm tra
     * @return true nếu số điện thoại đã tồn tại
     */
    public boolean isPhoneExists(String phone) {
        return customerRepository.isPhoneExists(phone);
    }
    
    /**
     * Kiểm tra xem username đã tồn tại trong cơ sở dữ liệu chưa
     * @param username Username cần kiểm tra
     * @return true nếu username đã tồn tại
     */
    public boolean isUsernameExists(String username) {
        Customer existingUser = customerRepository.findByUsername(username);
        return existingUser != null;
    }
    
    /**
     * Cập nhật thông tin khách hàng
     * @param customer Đối tượng khách hàng chứa thông tin cần cập nhật
     * @return true nếu cập nhật thành công
     */
    public boolean updateCustomer(Customer customer) {
        return customerRepository.updateCustomer(customer);
    }
    
    /**
     * Lấy người dùng hiện tại đang đăng nhập (alias cho getCurrentCustomer)
     * @return Đối tượng Customer của người dùng hiện tại, null nếu chưa đăng nhập
     */
    public static Customer getCurrentUser() {
        return currentCustomer;
    }
    
    /**
     * Lấy ID của người dùng hiện tại
     * @return ID của người dùng hiện tại, -1 nếu chưa đăng nhập
     */
    public static int getCurrentUserId() {
        return currentCustomer != null ? currentCustomer.getId() : -1;
    }
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }
    /**
     * Thay đổi mật khẩu người dùng
     * @param customerId ID của khách hàng
     * @param newPassword Mật khẩu mới
     * @return true nếu thay đổi thành công
     */
    public boolean changePassword(int customerId, String newPassword) {
        return customerRepository.changePassword(customerId, newPassword);
    }
    /**
     * Lấy email của người dùng dựa trên username
     * @param username Tên đăng nhập cần tra cứu
     * @return Email của người dùng hoặc null nếu không tìm thấy
     */
    public String getEmailByUsername(String username) {
        return customerRepository.getEmailByUsername(username);
    }
}