package controller;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import service.OrderService;
import view.ViewOrder;
import view.ViewOrderDetails;

import java.util.ArrayList;

public class OrderController {
    private ViewOrder view;
    private OrderService service;
    private TableModelListener tableModelListener;

    public OrderController(ViewOrder view, OrderService service) {
        this.view = view;
        this.service = service;
        initListeners();
    }

    private void initListeners() {
        view.getBtnConfirm().addActionListener(e -> confirmOrderAsync());
        view.getBtnCancel().addActionListener(e -> view.dispose());
        view.getBtnBack().addActionListener(e -> {
            if (view.getViewCart() != null) {
                view.getViewCart().setVisible(true);
            }
            view.dispose();
        });

        tableModelListener = new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE && (e.getColumn() == 1 || e.getColumn() == 3)) {
                    int row = e.getFirstRow();
                    if (row < 0 || row >= view.getTableModel().getRowCount()) return;

                    // Vô hiệu hóa listener để tránh vòng lặp
                    view.getTableModel().removeTableModelListener(this);

                    try {
                        if (e.getColumn() == 1) {
                            handleQuantityChange(row);
                        } else if (e.getColumn() == 3) {
                            handleWarrantyChange(row);
                        }
                    } finally {
                        // Kích hoạt lại listener
                        view.getTableModel().addTableModelListener(this);
                    }
                }
            }
        };
        view.getTableModel().addTableModelListener(tableModelListener);
    }

    private void handleQuantityChange(int row) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private String message;
            private Integer newQuantity;

            @Override
            protected Void doInBackground() {
                Object quantityObj = view.getTableModel().getValueAt(row, 1);
                int quantity;
                try {
                    quantity = (quantityObj instanceof String) ? Integer.parseInt((String) quantityObj) : (Integer) quantityObj;
                } catch (NumberFormatException ex) {
                    quantity = 1;
                    message = "Số lượng không hợp lệ! Đặt lại thành 1.";
                }

                String productName = (String) view.getTableModel().getValueAt(row, 0);
                int availableQuantity = service.getAvailableQuantity(productName);

                if (quantity <= 0) {
                    quantity = 1;
                    message = "Số lượng phải lớn hơn 0!";
                } else if (quantity > availableQuantity) {
                    quantity = availableQuantity;
                    message = "Sản phẩm " + productName + " chỉ còn " + availableQuantity + " sản phẩm!";
                }

                newQuantity = quantity;
                service.updateOrderDetailQuantity(row, newQuantity, view.getTableModel(), view.getOrderDetails());
                service.updateTotalAmount(view.getTableModel(), view.getOrderDetails(), view.getOrder());
                return null;
            }

            @Override
            protected void done() {
                if (newQuantity != null) {
                    view.getTableModel().setValueAt(newQuantity, row, 1);
                }
                if (message != null) {
                    JOptionPane.showMessageDialog(view, message);
                }
                view.updateTotalLabel();
            }
        };
        worker.execute();
    }

    private void handleWarrantyChange(int row) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                String warrantyType = (String) view.getTableModel().getValueAt(row, 3);
                service.updateOrderDetailWarranty(row, warrantyType, view.getTableModel(), view.getOrderDetails());
                service.updateTotalAmount(view.getTableModel(), view.getOrderDetails(), view.getOrder());
                return null;
            }

            @Override
            protected void done() {
                view.updateTotalLabel();
            }
        };
        worker.execute();
    }

    private void confirmOrderAsync() {
        view.getBtnConfirm().setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                String recipientName = view.getRecipientName();
                String recipientPhone = view.getRecipientPhone();
                String recipientAddress = view.getRecipientAddress();

                if (recipientName.isEmpty() || recipientPhone.isEmpty() || recipientAddress == null) {
                    JOptionPane.showMessageDialog(view, "Vui lòng nhập đầy đủ thông tin người nhận!");
                    return null;
                }

                if (!recipientPhone.matches("^0\\d{8,11}$")) {
                    JOptionPane.showMessageDialog(view, "Số điện thoại không hợp lệ! Phải bắt đầu bằng 0 và có từ 9 đến 12 chữ số.");
                    return null;
                }

                view.getOrder().setRecipientName(recipientName);
                view.getOrder().setRecipientPhone(recipientPhone);
                view.getOrder().setRecipientAddress(recipientAddress);

                service.confirmOrder(view.getOrder(), view.getOrderDetails(), view.getCurrentCustomerID(),
                    view.getTableModel(), view, () -> {
                        SwingUtilities.invokeLater(() -> {
                            int response = JOptionPane.showConfirmDialog(view,
                                "Thanh toán thành công!\nBạn có muốn xem chi tiết hóa đơn không?",
                                "Xác nhận", JOptionPane.YES_NO_OPTION);
                            if (response == JOptionPane.YES_OPTION) {
                                ViewOrderDetails detailView = new ViewOrderDetails(
                                    view.getCurrentCustomerID(),
                                    view.getOrder(),
                                    new ArrayList<>(view.getOrderDetails())
                                );
                                detailView.setVisible(true);
                            }
                            view.dispose();
                        });
                    });
                return null;
            }

            @Override
            protected void done() {
                view.getBtnConfirm().setEnabled(true);
            }
        };
        worker.execute();
    }
}