package com.service;

import com.repository.ProductRepository;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Vector;

public class ProductService {
    private ProductRepository productRepository = new ProductRepository();

    public void loadProducts(DefaultTableModel tableModel, String order, String categoryID) {
        try {
            List<Object[]> products = productRepository.getProducts(order, categoryID);
            tableModel.setRowCount(0);
            for (Object[] product : products) {
                Vector<Object> row = new Vector<>();
                row.add(product[0]);
                row.add(product[1]); 
                row.add(product[2]); 
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi tải danh sách sản phẩm: " + e.getMessage());
        }
    }

    public void searchProducts(DefaultTableModel tableModel, String keyword) {
        try {
            List<Object[]> products = productRepository.searchProducts(keyword);
            tableModel.setRowCount(0);
            for (Object[] product : products) {
                Vector<Object> row = new Vector<>();
                row.add(product[0]);
                row.add(product[1]); 
                row.add(product[2]); 
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi tìm kiếm sản phẩm: " + e.getMessage());
        }
    }

    public String getProductDescription(String productName) {
        try {
            return productRepository.getProductDescription(productName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean checkProductAvailability(String productName, int quantity) {
        try {
            int availableQuantity = productRepository.getProductQuantity(productName);
            if (availableQuantity < quantity) {
                JOptionPane.showMessageDialog(null, "Sản phẩm " + productName + " chỉ còn " + availableQuantity + " đơn vị!");
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi kiểm tra số lượng sản phẩm: " + e.getMessage());
            return false;
        }
    }
}