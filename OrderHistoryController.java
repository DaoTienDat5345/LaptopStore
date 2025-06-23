package controller;

import view.ViewOrderHistory;
import service.OrderHistoryService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OrderHistoryController {
    private ViewOrderHistory view;
    private OrderHistoryService service;

    public OrderHistoryController(ViewOrderHistory view, OrderHistoryService service) {
        this.view = view;
        this.service = service;

        view.getBtnViewDetails().addActionListener(new ViewDetailsListener());
    }

    class ViewDetailsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = view.getOrderTable().getSelectedRow();
            if (selectedRow >= 0) {
                String orderID = (String) view.getTableModel().getValueAt(selectedRow, 0);
                service.viewSelectedOrderDetails(orderID, view.getCurrentCustomerID());
            } else {
                javax.swing.JOptionPane.showMessageDialog(null, "Vui lòng chọn một đơn hàng để xem chi tiết!");
            }
        }
    }
}