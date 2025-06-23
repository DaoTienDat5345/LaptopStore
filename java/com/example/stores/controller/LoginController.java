package com.example.stores.controller;

import com.example.stores.model.Manager;
import com.example.stores.repository.ManagerRepository;
import com.example.stores.repository.impl.ManagerRepositoryImpl;
import com.example.stores.service.ManagerService;
import com.example.stores.service.impl.ManagerServiceImpl;
import com.example.stores.util.LanguageForManager;
import com.example.stores.util.UTF8Control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController {

    @FXML private ToggleButton btnToggleLanguage;
    @FXML private Label lblWelcome;
    @FXML private Label lblUserLogin;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblLoginStatus;
    @FXML private Button btnLogin;

    private ManagerService managerService;
    private boolean isVietnamese = true; // Giả sử Tiếng Việt là mặc định

    // ResourceBundle cho đa ngôn ngữ
    private ResourceBundle messages;

    public LoginController() {
        ManagerRepository managerRepository = new ManagerRepositoryImpl();
        this.managerService = new ManagerServiceImpl(managerRepository);
    }

    @FXML
    public void initialize() {
        // LanguageForManager sẽ tự load ngôn ngữ mặc định
        LanguageForManager.getInstance().currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> {
            updateTexts(); // Gọi lại updateTexts khi Locale thay đổi
        });
        updateTexts(); // Cập nhật lần đầu
        txtPassword.setOnAction(this::handleLoginAction);
    }

    private void loadLanguage(String language, String country) {
        Locale currentLocale = new Locale(language, country);
        try {
            // Sử dụng UTF8Control để load
            messages = ResourceBundle.getBundle("com.example.stores.lang.messages", currentLocale, new UTF8Control());
        } catch (Exception e) {
            System.err.println("Không tìm thấy resource bundle cho locale: " + currentLocale + " với UTF8Control. Lỗi: " + e.getMessage());
            // Fallback nếu có lỗi, ví dụ không dùng control hoặc dùng resource bundle mặc định
            try {
                messages = ResourceBundle.getBundle("com.example.stores.lang.messages", currentLocale); // Thử load mặc định
            } catch (Exception e2) {
                System.err.println("Cũng không tìm thấy resource bundle mặc định cho locale: " + currentLocale + ". Lỗi: " + e2.getMessage());
                messages = null;
            }
        }
    }

    private void updateTexts() {
        LanguageForManager lm = LanguageForManager.getInstance();
        lblWelcome.setText(lm.getString("login.welcome"));
        lblUserLogin.setText(lm.getString("login.userLogin"));
        txtUsername.setPromptText(lm.getString("login.usernamePrompt"));
        txtPassword.setPromptText(lm.getString("login.passwordPrompt"));
        btnLogin.setText(lm.getString("login.btnLogin"));

        // Cập nhật text cho nút ngôn ngữ dựa trên locale hiện tại của LanguageForManager
        if (lm.getCurrentLocale().getLanguage().equals("vi")) {
            btnToggleLanguage.setText(lm.getString("login.btnEnglish"));
            btnToggleLanguage.setSelected(false); // Giả sử Tiếng Việt là không selected (để khi nhấn sẽ sang English)
        } else {
            btnToggleLanguage.setText(lm.getString("login.btnVietnamese"));
            btnToggleLanguage.setSelected(true); // Giả sử English là selected
        }
        lblLoginStatus.setText("");
    }


    @FXML
    void handleLoginAction(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            lblLoginStatus.setText(messages != null ? messages.getString("login.error.emptyFields") : "Tên đăng nhập và mật khẩu không được để trống.");
            return;
        }

        Optional<Manager> managerOpt = managerService.login(username, password);

        if (managerOpt.isPresent()) {
            lblLoginStatus.setText(messages != null ? messages.getString("login.success") : "Đăng nhập thành công!");
            lblLoginStatus.setTextFill(javafx.scene.paint.Color.GREEN);
            // Chuyển sang MainView
            switchToMainView(managerOpt.get());
        } else {
            lblLoginStatus.setText(messages != null ? messages.getString("login.error.invalidCredentials") : "Tên đăng nhập hoặc mật khẩu không đúng.");
            lblLoginStatus.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    private void switchToMainView(Manager manager) {
        try {
            Stage currentStage = (Stage) btnLogin.getScene().getWindow();
            currentStage.close(); // Đóng cửa sổ login

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/MainView.fxml")); // Hoặc có /computerstore/
            Parent root = loader.load();

            // Truyền thông tin Manager sang MainViewController nếu cần
            MainViewController mainViewController = loader.getController();
            mainViewController.setCurrentManager(manager); // Cần tạo phương thức này trong MainViewController

            Stage mainStage = new Stage();
            mainStage.setTitle(messages != null ? messages.getString("main.title") : "Hệ Thống Quản Lý Cửa Hàng");
            Scene scene = new Scene(root);
            String cssPath = "/com/example/stores/css/styles.css"; // Hoặc có /computerstore/
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            mainStage.setScene(scene);
            mainStage.setMaximized(true); // Mở full màn hình
            mainStage.setOnCloseRequest(event -> {
                System.out.println("Main window is closing, closing DB connection...");
                com.example.stores.config.DatabaseConnection.closeConnection(); // Gọi lớp DatabaseConnection của bạn
            });
            mainStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Chuyển Màn Hình", "Không thể mở giao diện chính.");
        }
    }

    @FXML
    void handleToggleLanguage(ActionEvent event) {
        isVietnamese = !isVietnamese;
        if (isVietnamese) {
            loadLanguage("vi", "VN");
        } else {
            loadLanguage("en", "US");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}