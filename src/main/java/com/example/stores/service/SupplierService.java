package com.example.stores.service;

import com.example.stores.model.Supplier;
import java.util.List;
import java.util.Optional;

public interface SupplierService {
    Supplier addSupplier(Supplier supplier) throws IllegalArgumentException;
    boolean updateSupplier(Supplier supplier) throws IllegalArgumentException;
    boolean deleteSupplier(int supplierId) throws IllegalArgumentException;
    Optional<Supplier> getSupplierById(int supplierId);
    List<Supplier> getAllSuppliers();
    List<Supplier> searchSuppliers(String keyword);
}