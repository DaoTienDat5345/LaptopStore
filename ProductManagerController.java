package controller;

import view.ViewProductPanel; 
import view.ViewAddProduct;
import view.ViewEditProduct;
import service.ProductManagerService;
import model.Products;

import javax.swing.*;
import java.util.List;

public class ProductManagerController {

    private final ViewProductPanel view;
    private final ProductManagerService service;

    public ProductManagerController(ViewProductPanel view) {
        this.view = view;
        this.service = new ProductManagerService(); 
        addActionListeners();
        loadProductData(); 
    }

    private void addActionListeners() {
        view.getBtnAdd().addActionListener(e -> showAddProductDialog());
        view.getBtnEdit().addActionListener(e -> showEditProductDialog());
        view.getBtnDelete().addActionListener(e -> deleteProduct());
    }

    public void loadProductData() {
        List<Products> products = service.getAllProducts();
        view.displayProducts(products); 
    }

    private void showAddProductDialog() {
        ViewAddProduct addDialog = new ViewAddProduct(this::loadProductData);
        addDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addDialog.setVisible(true);
    }

    private void showEditProductDialog() {
        int selectedRow = view.getProductTable().getSelectedRow();
        if (selectedRow < 0) {
            view.showMessage("Vui lòng chọn một sản phẩm để sửa.");
            return;
        }

        String id = view.getProductTableModel().getValueAt(selectedRow, 0).toString();
        String name = view.getProductTableModel().getValueAt(selectedRow, 1).toString();
        String cat = view.getProductTableModel().getValueAt(selectedRow, 2).toString();
        String desc = view.getProductTableModel().getValueAt(selectedRow, 3).toString();
        double price = Double.parseDouble(view.getProductTableModel().getValueAt(selectedRow, 4).toString());
        double priceCost = Double.parseDouble(view.getProductTableModel().getValueAt(selectedRow, 5).toString());
        String img = view.getProductTableModel().getValueAt(selectedRow, 6).toString();
        int qty = Integer.parseInt(view.getProductTableModel().getValueAt(selectedRow, 7).toString());


        ViewEditProduct editDialog = new ViewEditProduct(id, name, cat, desc, price, priceCost, img, qty, this::loadProductData);
        editDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editDialog.setVisible(true);
    }

    private void deleteProduct() {
        int selectedRow = view.getProductTable().getSelectedRow();
        if (selectedRow < 0) {
            view.showMessage("Vui lòng chọn một sản phẩm để xóa.");
            return;
        }
        String productId = view.getProductTableModel().getValueAt(selectedRow, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(view, 
                "Bạn có chắc chắn muốn xóa sản phẩm ID: " + productId + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = service.deleteProduct(productId);
            if (success) {
                view.showMessage("Xóa sản phẩm thành công!");
                loadProductData(); 
            } else {
                view.showMessage("Xóa sản phẩm thất bại. Có thể sản phẩm đang được sử dụng.");
            }
        }
    }
}