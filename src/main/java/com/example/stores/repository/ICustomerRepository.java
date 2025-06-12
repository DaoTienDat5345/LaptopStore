package com.example.stores.repository;

import com.example.stores.model.Customer;
import java.util.List;

public interface ICustomerRepository {
    Customer findByUsername(String username);
    Customer authenticate(String username, String password);
    boolean register(Customer customer);
    boolean updateCustomer(Customer customer);
    boolean changePassword(int customerId, String newPassword);
    String getEmailByUsername(String username);
    boolean isEmailExists(String email);
    List<Customer> getAllCustomers();
    Customer findById(int id);
    boolean deleteCustomer(int id);
    boolean activateAccount(String activationCode);
}