package com.example.stores.service;

import com.example.stores.model.Customer;

public interface IAuthService {
    boolean login(String username, String password);
    boolean register(Customer customer);
    static Customer getCurrentCustomer() { return null; }
    static void setCurrentCustomer(Customer customer) { }
    void logout();
    boolean changePassword(int customerId, String newPassword);
    String getEmailByUsername(String username);
    boolean isEmailExists(String email);
    static int getCurrentUserId() { return -1; }
}