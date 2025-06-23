package com.example.stores.service;

import java.util.List;

public interface IWarrantyService {
    List<Warranty> getWarrantiesForProduct(String productId);
    double calculateWarrantyPrice(String productId, String warrantyType, double productPrice);
    List<String> getAvailableWarrantyTypes(String productId);
    Warranty getWarrantyById(int warrantyId);
    boolean registerWarranty(String orderId, String productId, String warrantyType);
    boolean extendWarranty(int warrantyId, int months);
}