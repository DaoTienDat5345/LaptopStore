package controller;

import view.ViewShiftPanel; 
import service.ShiftManagerService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ShiftManagerController {

    private final ViewShiftPanel view;
    private final ShiftManagerService service;
    private String currentShift = "Sáng"; 

    public ShiftManagerController(ViewShiftPanel view) {
        this.view = view;
        this.service = new ShiftManagerService();
        addActionListeners();
        loadShiftData(currentShift, view.getBtnMorning()); 
    }

    private void addActionListeners() {
        ActionListener shiftButtonListener = e -> {
            JButton clickedButton = (JButton) e.getSource();
            currentShift = clickedButton.getText(); 
            loadShiftData(currentShift, clickedButton);
        };
        view.getBtnMorning().addActionListener(shiftButtonListener);
        view.getBtnAfternoon().addActionListener(shiftButtonListener);
        view.getBtnEvening().addActionListener(shiftButtonListener);

        view.getBtnAdd().addActionListener(e -> addEmployeeToShift());

        view.getBtnDelete().addActionListener(e -> deleteEmployeeFromShift());
    }

    private void loadShiftData(String shiftName, JButton clickedButton) {
        List<Object[]> shiftEmployees = service.getEmployeesForShift(shiftName);
        view.displayShiftEmployees(shiftEmployees);
        view.highlightShiftButton(clickedButton); 
    }

    private void addEmployeeToShift() {
        String employeeId = view.getEmployeeIdInput(); 
        String shiftName = view.getSelectedShift();
        String shiftDay = view.getSelectedDay();  

        String errorMessage = service.addEmployeeToShift(employeeId, shiftName, shiftDay);

        if (errorMessage == null) {
            view.showMessage("Thêm nhân viên vào ca thành công!");
            loadShiftData(currentShift, findButtonByText(currentShift)); 
            view.clearEmployeeIdInput(); 
        } else {
            view.showMessage("Lỗi: " + errorMessage);
        }
    }

    private void deleteEmployeeFromShift() {
        int selectedRow = view.getEmployeeTable().getSelectedRow();
        if (selectedRow < 0) {
            view.showMessage("Vui lòng chọn một nhân viên để xóa khỏi lịch.");
            return;
        }
        String employeeId = view.getEmployeeTableModel().getValueAt(selectedRow, 0).toString();
        String employeeName = view.getEmployeeTableModel().getValueAt(selectedRow, 1).toString();


        int confirm = JOptionPane.showConfirmDialog(view,
                "Bạn có chắc muốn xóa " + employeeName + " (ID: " + employeeId + ") khỏi lịch làm việc?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = service.deleteEmployeeFromShift(employeeId);
            if (success) {
                view.showMessage("Xóa nhân viên khỏi lịch thành công!");
                loadShiftData(currentShift, findButtonByText(currentShift));
            } else {
                view.showMessage("Xóa nhân viên khỏi lịch thất bại.");
            }
        }
    }

    private JButton findButtonByText(String text) {
        if ("Sáng".equals(text)) return view.getBtnMorning();
        if ("Chiều".equals(text)) return view.getBtnAfternoon();
        if ("Tối".equals(text)) return view.getBtnEvening();
        return null;
    }
}