package com.example.stores.controller;

import com.example.stores.util.AlertUtils;
import com.example.stores.util.LanguageManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class RoleSelectionController implements Initializable {

    @FXML private AnchorPane rootPane;
    @FXML private Button managerButton;
    @FXML private Button employeeButton;
    @FXML private Button customerButton;
    @FXML private Label welcomeLabel;
    @FXML private Label storeLabel;
    @FXML private Label logoLabel;
    @FXML private Button langButton;
    @FXML private ImageView languageFlag;

    private boolean isVietnamese;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Lấy trạng thái ngôn ngữ từ LanguageManager
        isVietnamese = LanguageManager.isVietnamese();

        // Cập nhật giao diện theo ngôn ngữ ban đầu
        updateLanguage();

        // Thiết lập hiệu ứng hover cho các button
        setupButtonHoverEffects();
    }

    private void updateLanguage() {
        if (isVietnamese) {
            updateToVietnamese();
        } else {
            updateToEnglish();
        }

        // Cập nhật cờ và text trên nút ngôn ngữ
        if (langButton != null) {
            langButton.setText(isVietnamese ? "Tiếng Việt" : "English");
        }

        // Cập nhật hình ảnh cờ
        if (languageFlag != null) {
            try {
                String flagPath = isVietnamese ?
                        "/com/example/stores/images/layout/flag_vn.png" :
                        "/com/example/stores/images/layout/flag_en.png";

                InputStream flagStream = getClass().getResourceAsStream(flagPath);
                if (flagStream != null) {
                    Image flag = new Image(flagStream, 30, 20, true, true);
                    languageFlag.setImage(flag);
                }
            } catch (Exception e) {
                System.err.println("Không thể tải hình ảnh cờ: " + e.getMessage());
            }
        }
    }

    private void updateToVietnamese() {
        welcomeLabel.setText("Chọn Vai Trò");
        managerButton.setText("Quản Lý");
        employeeButton.setText("Nhân Viên");
        customerButton.setText("Khách Hàng");
    }

    private void updateToEnglish() {
        welcomeLabel.setText("Role Selection");
        managerButton.setText("Manager");
        employeeButton.setText("Employee");
        customerButton.setText("Customer");
    }

    @FXML
    public void handleLanguageButtonAction(ActionEvent event) {
        // Đổi ngôn ngữ
        isVietnamese = !isVietnamese;

        // Cập nhật trạng thái trong LanguageManager
        LanguageManager.setVietnamese(isVietnamese);

        // Cập nhật giao diện
        updateLanguage();
    }

    private void setupButtonHoverEffects() {
        // Hiệu ứng hover cho Manager button
        managerButton.setCursor(Cursor.HAND); // Thêm con trỏ tay
        managerButton.setOnMouseEntered(e -> {
            managerButton.setStyle("-fx-background-color: linear-gradient(to right, #5CB8E4, #865DFF); " +
                    "-fx-background-radius: 15; -fx-text-fill: white; -fx-font-size: 18px; " + // Tăng font-size từ 16px lên 18px
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        });

        managerButton.setOnMouseExited(e -> {
            managerButton.setStyle("-fx-background-color: linear-gradient(to right, #865DFF, #5CB8E4); " +
                    "-fx-background-radius: 15; -fx-text-fill: white; -fx-font-size: 16px;");
        });

        // Hiệu ứng hover cho Employee button
        employeeButton.setCursor(Cursor.HAND); // Thêm con trỏ tay
        employeeButton.setOnMouseEntered(e -> {
            employeeButton.setStyle("-fx-background-color: linear-gradient(to right, #865DFF, #5CB8E4); " +
                    "-fx-background-radius: 15; -fx-text-fill: white; -fx-font-size: 18px; " + // Tăng font-size từ 16px lên 18px
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        });

        employeeButton.setOnMouseExited(e -> {
            employeeButton.setStyle("-fx-background-color: linear-gradient(to right, #5CB8E4, #865DFF); " +
                    "-fx-background-radius: 15; -fx-text-fill: white; -fx-font-size: 16px;");
        });

        // Hiệu ứng hover cho Customer button
        customerButton.setCursor(Cursor.HAND); // Thêm con trỏ tay
        customerButton.setOnMouseEntered(e -> {
            customerButton.setStyle("-fx-background-color: linear-gradient(to right, #5CB8E4, #865DFF); " +
                    "-fx-background-radius: 15; -fx-text-fill: white; -fx-font-size: 18px; " + // Tăng font-size từ 16px lên 18px
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        });

        customerButton.setOnMouseExited(e -> {
            customerButton.setStyle("-fx-background-color: linear-gradient(to right, #865DFF, #5CB8E4); " +
                    "-fx-background-radius: 15; -fx-text-fill: white; -fx-font-size: 16px;");
        });

        // Thêm hiệu ứng cho nút ngôn ngữ
        langButton.setCursor(Cursor.HAND);
    }

    @FXML
    public void handleManagerButtonAction(ActionEvent event) {
        // Hiển thị thông báo bằng ngôn ngữ phù hợp
        String title = isVietnamese ? "Thông báo" : "Notice";
        String message = isVietnamese ?
                "Chức năng đăng nhập cho Quản lý đang được phát triển" :
                "Manager login function is under development";
        AlertUtils.showInfo(title, message);
    }

    @FXML
    public void handleEmployeeButtonAction(ActionEvent event) {
        // Hiển thị thông báo bằng ngôn ngữ phù hợp
        String title = isVietnamese ? "Thông báo" : "Notice";
        String message = isVietnamese ?
                "Chức năng đăng nhập cho Nhân viên đang được phát triển" :
                "Employee login function is under development";
        AlertUtils.showInfo(title, message);
    }

    @FXML
    public void handleCustomerButtonAction(ActionEvent event) {
        try {
            // Lấy kích thước và vị trí hiện tại của cửa sổ
            Stage currentStage = (Stage) customerButton.getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();

            // Tải giao diện CustomerLogin.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomerLogin.fxml"));
            Parent root = loader.load();

            // Thiết lập scene mới
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle(isVietnamese ? "CELLCOMP STORE - Đăng nhập" : "CELLCOMP STORE - Login");

            // Giữ nguyên kích thước và vị trí cửa sổ
            currentStage.setWidth(width);
            currentStage.setHeight(height);
            currentStage.setX(x);
            currentStage.setY(y);

            // Lấy controller để tải lại tài nguyên và cập nhật ngôn ngữ
            CustomerLoginController controller = loader.getController();
            if (controller != null) {
                Platform.runLater(() -> {
                    controller.updateInitialLanguage();
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
            String errorTitle = isVietnamese ? "Lỗi" : "Error";
            String errorMsg = isVietnamese ?
                    "Không thể mở giao diện đăng nhập: " :
                    "Cannot open login interface: ";
            AlertUtils.showError(errorTitle, errorMsg + e.getMessage());
        }
    }
}