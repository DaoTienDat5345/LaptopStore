package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import view.ViewHome;
import view.Login;
import view.LoginManager;

public class HomeController {
    private ViewHome view;

    public HomeController(ViewHome view) {
        this.view = view;
        initListeners();
    }

    private void initListeners() {
        // Xử lý sự kiện cho nút Customer
        view.getBtnCustomer().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Login viewLogin = new Login();
                viewLogin.setVisible(true);
                view.dispose();
            }
        });

        // Xử lý sự kiện cho nút Manager
        view.getBtnManager().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoginManager viewLoginManager = new LoginManager();
                viewLoginManager.setVisible(true);
                view.dispose();
            }
        });
    }
}