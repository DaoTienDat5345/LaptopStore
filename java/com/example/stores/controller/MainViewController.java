package com.example.stores.controller; // Bỏ .computerstore

import com.example.stores.model.Manager; // Cần để hiển thị tên Manager
import com.example.stores.repository.ManagerRepository;
import com.example.stores.repository.impl.ManagerRepositoryImpl;
import com.example.stores.service.ManagerService;
import com.example.stores.service.impl.ManagerServiceImpl;

import com.example.stores.util.LanguageForManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public class MainViewController {

    @FXML private BorderPane mainBorderPane;
    @FXML private VBox sidebar;
    @FXML private AnchorPane contentArea;
    @FXML private Label lblCurrentDateTime;
    @FXML private Label lblManagerName;

    // Các nút trên sidebar
    @FXML private Button btnDashboard;
    @FXML private Button btnManagerProfile;
    @FXML private Button btnEmployeeManagement;
    @FXML private Button btnShiftManagement;
    @FXML private Button btnProductManagement;
    @FXML private Button btnWarehouseManagement;
    @FXML private Button btnSupplierImportManagement; // THÊM DÒNG NÀY
    @FXML private Button btnImportReceipt; // Nếu bạn có nút này riêng
    @FXML private ToggleButton btnToggleLanguageMain;

    private Button lastSelectedButton; // Để quản lý trạng thái active của nút

    private ManagerService managerService;
    private Manager currentLoggedInManager;
    private static final int MANAGER_ID = 1; // ID của Manager

    public MainViewController() {
        ManagerRepository managerRepository = new ManagerRepositoryImpl();
        this.managerService = new ManagerServiceImpl(managerRepository);
    }

    // Trong MainViewController.java, phương thức initialize()

    @FXML
    public void initialize() {
        setupDateTimeUpdater();

        handleShowManagerProfile(null);

        LanguageForManager.getInstance().currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> {
            updateMainViewTexts();
            reloadCurrentViewWithNewLanguage();
        });
        updateMainViewTexts();

        if (btnManagerProfile != null) {
            setActiveButton(btnManagerProfile);
        } else if (btnDashboard != null && btnDashboard.isVisible()) {
        }

    }

    private void updateMainViewTexts() {
        LanguageForManager lm = LanguageForManager.getInstance();
        // Cập nhật tiêu đề cửa sổ chính (nếu MainApp chưa làm)
        if (mainBorderPane.getScene() != null && mainBorderPane.getScene().getWindow() != null) { // Kiểm tra trước khi lấy stage
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            stage.setTitle(lm.getString("main.title"));
        }

        if (btnToggleLanguageMain != null) {
            if (lm.getCurrentLocale().getLanguage().equals("vi")) {
                btnToggleLanguageMain.setText(lm.getString("login.btnEnglish")); // Dùng lại key từ login
                btnToggleLanguageMain.setSelected(false);
            } else {
                btnToggleLanguageMain.setText(lm.getString("login.btnVietnamese"));
                btnToggleLanguageMain.setSelected(true);
            }
        }
    }

    private void setupDateTimeUpdater() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
            lblCurrentDateTime.setText(LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void loadManagerInfo() {
        Optional<Manager> managerOpt = managerService.getManagerProfile(MANAGER_ID);
        managerOpt.ifPresent(manager -> lblManagerName.setText(manager.getFullName()));
        // Nếu không có manager thì có thể hiển thị "Khách" hoặc thông báo lỗi
    }

    private void loadView(String fxmlPath) {
        try {
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                System.err.println("LỖI: Không tìm thấy file FXML tại: " + fxmlPath);
                showAlert(Alert.AlertType.ERROR, "Lỗi Tải Giao Diện", "Không tìm thấy file: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            // TRUYỀN RESOURCE BUNDLE HIỆN TẠI VÀO ĐÂY
            loader.setResources(LanguageForManager.getInstance().getBundle());
            Parent view = loader.load();

            view.setUserData(fxmlPath);

            contentArea.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Giao Diện", "Không thể tải: " + fxmlPath + "\nLỗi: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Không Mong Muốn", "Lỗi khi tải view: " + fxmlPath + "\nLỗi: " + e.getMessage());
        }
    }

    private void setActiveButton(Button activeButton) {
        if (lastSelectedButton != null) {
            lastSelectedButton.getStyleClass().remove("sidebar-button-active");
        }
        if (activeButton != null) {
            activeButton.getStyleClass().add("sidebar-button-active");
            lastSelectedButton = activeButton;
        }
    }

    public void setCurrentManager(Manager manager) {
        this.currentLoggedInManager = manager;
        updateMainViewTexts();
        if (btnManagerProfile != null) {
            handleShowManagerProfile(null);
            setActiveButton(btnManagerProfile);
        }
    }

    @FXML
    void handleShowDashboard(ActionEvent event) {
        // loadView("/com/example/stores/view/DashboardView.fxml"); // Tạo DashboardView.fxml sau
        // Tạm thời có thể load ManagerProfileView hoặc để trống
        contentArea.getChildren().clear(); // Xóa nội dung cũ
        Label tempLabel = new Label("Chào mừng đến Trang chủ!");
        tempLabel.setFont(new Font(24));
        AnchorPane.setTopAnchor(tempLabel, 50.0);
        AnchorPane.setLeftAnchor(tempLabel, 50.0);
        contentArea.getChildren().add(tempLabel);
        if (event != null) setActiveButton((Button) event.getSource());
    }

    @FXML
    void handleShowManagerProfile(ActionEvent event) {
        loadView("/com/example/stores/view/ManagerProfileView.fxml");
        if (event != null) setActiveButton((Button) event.getSource());
    }

    @FXML
    void handleShowEmployeeManagement(ActionEvent event) {
        loadView("/com/example/stores/view/EmployeeManagementView.fxml"); // Đảm bảo đường dẫn đúng
        if (event != null) setActiveButton((Button) event.getSource());
    }

    @FXML
    void handleShowShiftManagement(ActionEvent event) {
        loadView("/com/example/stores/view/ShiftManagementView.fxml"); // Đảm bảo đường dẫn đúng
        if (event != null) setActiveButton((Button) event.getSource());
    }

    @FXML
    void handleShowProductManagement(ActionEvent event) {
        loadView("/com/example/stores/view/ProductManagementView.fxml"); // Hoặc có /computerstore/ tùy cấu trúc resource
        if (event != null) setActiveButton((Button) event.getSource());
    }

    @FXML
    void handleShowWarehouseManagement(ActionEvent event) {
        loadView("/com/example/stores/view/WarehouseManagementView.fxml"); // Hoặc có /computerstore/
        if (event != null) setActiveButton((Button) event.getSource());
    }

    @FXML
    void handleShowSupplierImportManagement(ActionEvent event) {
        // Đảm bảo đường dẫn này khớp với vị trí file SupplierAndImportManagementView.fxml của bạn
        // (có thể cần thêm /computerstore/ nếu cấu trúc resources của bạn như vậy)
        loadView("/com/example/stores/view/SupplierAndImportManagementView.fxml");
        if (event != null) {
            // Kiểm tra null cho btnSupplierImportManagement trước khi dùng làm nguồn
            Button sourceButton = (Button) event.getSource();
            if (sourceButton.equals(btnSupplierImportManagement)) { // Đảm bảo nút được inject đúng
                setActiveButton(btnSupplierImportManagement);
            } else { // Fallback nếu event source không phải là nút này (ít xảy ra)
                setActiveButton((Button) event.getSource());
            }
        } else {
            // Nếu gọi từ initialize, bạn có thể cần set active button trực tiếp
            // Ví dụ: if (btnSupplierImportManagement != null) setActiveButton(btnSupplierImportManagement);
        }
    }
    @FXML
    void handleShowOrderManagement(ActionEvent event) {
        loadView("/com/example/stores/view/OrderManagementView.fxml"); // Điều chỉnh đường dẫn nếu cần
        if (event != null) setActiveButton((Button) event.getSource());
    }
    @FXML
    void handleShowCustomerManagement(ActionEvent event) {
        loadView("/com/example/stores/view/CustomerManagementView.fxml"); // Hoặc có /computerstore/
        if (event != null) setActiveButton((Button) event.getSource());
    }
    @FXML
    void handleShowSalesReport(ActionEvent event) {
        loadView("/com/example/stores/view/SalesReportView.fxml"); // Hoặc có /computerstore/
        if (event != null) setActiveButton((Button) event.getSource());
    }

    // Thêm các phương thức handleShow... cho các nút khác

    @FXML
    void handleLogout(ActionEvent event) {
        // Xử lý đăng xuất (ví dụ: đóng ứng dụng hoặc quay lại màn hình chờ nếu có)
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận đăng xuất");
        alert.setHeaderText("Bạn có chắc chắn muốn đăng xuất/thoát khỏi ứng dụng?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("Đăng xuất...");
            // Platform.exit(); // Thoát hoàn toàn ứng dụng
            // Hoặc nếu đây là cửa sổ chính, có thể lấy stage và close()
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            if (stage != null) {
                stage.close(); // Sẽ trigger hàm stop() trong MainApp
            }
        }
    }
    @FXML
    void handleToggleLanguageMainAction(ActionEvent event) {
        LanguageForManager lm = LanguageForManager.getInstance();
        if (lm.getCurrentLocale().getLanguage().equals("vi")) {
            lm.setLocale(new Locale("en", "US"));
        } else {
            lm.setLocale(new Locale("vi", "VN"));
        }
        // updateMainViewTexts() sẽ được gọi tự động bởi listener
        // Quan trọng: Cần có cơ chế để các View con đang hiển thị trong contentArea cũng được cập nhật
        // Cách 1: Gọi lại loadView cho view hiện tại.
        // Cách 2: View con tự lắng nghe LanguageForManager.currentLocaleProperty()
        reloadCurrentViewWithNewLanguage();
    }

    private void reloadCurrentViewWithNewLanguage() {
        Node currentViewNode = contentArea.getChildren().isEmpty() ? null : contentArea.getChildren().get(0);
        if (currentViewNode != null && currentViewNode.getUserData() instanceof String) {
            String currentViewPath = (String) currentViewNode.getUserData();
            if (currentViewPath != null && !currentViewPath.isEmpty()) {
                System.out.println("Reloading view: " + currentViewPath);
                loadView(currentViewPath); // loadView đã được cập nhật để setResourceBundle
            }
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