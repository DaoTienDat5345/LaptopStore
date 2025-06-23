package com.example.stores.controller;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.scene.Cursor;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import com.example.stores.service.impl.AuthService;
import com.example.stores.util.AlertUtils;
import com.example.stores.util.LanguageManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class CustomerLoginController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label userLoginLabel;
    @FXML private TextField usernameField;
    @FXML private HBox usernameBox;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Button eyeButton;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button langButton;
    @FXML private Label statusMessage;
    @FXML private ImageView languageFlag;
    @FXML private AnchorPane rootPane;
    @FXML private Rectangle backgroundRect;
    @FXML private AnchorPane headerPane;
    @FXML private Rectangle headerRect;
    @FXML private ImageView illustrationImage;
    @FXML private AnchorPane loginFormPane;
    @FXML private Label logoLabel;
    @FXML private Label storeLabel;
    @FXML private Label sellerCenterLabel;
    @FXML private HBox languageBox;
    @FXML private HBox passwordBox;
    @FXML private ImageView userIconView;
    @FXML private ImageView lockIconView;
    @FXML private ImageView eyeIconView;
    @FXML private ImageView logoIcon;

    private boolean isVietnamese = true;
    private boolean passwordVisible = false;

    private AuthService authService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        authService = new AuthService();

        // Thêm CSS class cho các button
        loginButton.getStyleClass().add("login-button");
        registerButton.getStyleClass().add("register-button");
        langButton.getStyleClass().add("language-btn");

        // Thiết lập cursor hand cho các phần tử tương tác
        forgotPasswordLink.setCursor(Cursor.HAND);
        eyeButton.setCursor(Cursor.HAND);

        // Đồng bộ giữa hai trường mật khẩu
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!passwordVisible) {
                visiblePasswordField.setText(newVal);
            }
        });

        visiblePasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (passwordVisible) {
                passwordField.setText(newVal);
            }
        });

        // Xóa thông báo lỗi khi người dùng nhập
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            statusMessage.setText("");
            statusMessage.getStyleClass().remove("success-message");
        });

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            statusMessage.setText("");
            statusMessage.getStyleClass().remove("success-message");
        });
        visiblePasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            statusMessage.setText("");
            statusMessage.getStyleClass().remove("success-message");
        });

        // Lấy trạng thái ngôn ngữ trực tiếp từ LanguageManager
        isVietnamese = LanguageManager.isVietnamese();
        System.out.println("LoginController đã khởi tạo với ngôn ngữ: " + (isVietnamese ? "Tiếng Việt" : "English"));

        // Cập nhật UI theo ngôn ngữ
        updateInitialLanguage();

        if (eyeButton != null && eyeIconView != null) {
    // Hiệu ứng phóng to khi hover
    eyeButton.setOnMouseEntered(mouseEvent -> {
        eyeIconView.setScaleX(1.2);
        eyeIconView.setScaleY(1.2);
    });
    
    eyeButton.setOnMouseExited(mouseEvent -> {
        eyeIconView.setScaleX(1.0);
        eyeIconView.setScaleY(1.0);
    });
}

        // Thêm các hiệu ứng màu cho icons
        ColorAdjust gray = new ColorAdjust();
        gray.setSaturation(-1);
        userIconView.setEffect(gray);
        lockIconView.setEffect(gray);
        eyeIconView.setEffect(gray);

        // Responsive - QUAN TRỌNG: Đặt trong Platform.runLater để đảm bảo Scene đã được tạo
        Platform.runLater(this::setupResponsiveLayout);

        // Tải lại các tài nguyên hình ảnh để đảm bảo hiển thị đúng
        Platform.runLater(this::reloadResources);

        if (logoutButton != null && logoutIcon != null) {
            logoutButton.setCursor(Cursor.HAND);

            logoutButton.setOnMouseEntered(e -> {
                // Phóng to icon khi hover
                logoutIcon.setScaleX(1.2);
                logoutIcon.setScaleY(1.2);

                // Thêm hiệu ứng đổ bóng
                logoutButton.setStyle("-fx-background-radius: 30; -fx-background-color: #f0f0f0; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 0);");
            });

            logoutButton.setOnMouseExited(e -> {
                // Khôi phục kích thước
                logoutIcon.setScaleX(1.0);
                logoutIcon.setScaleY(1.0);

                // Khôi phục style ban đầu
                logoutButton.setStyle("-fx-background-radius: 30; -fx-background-color: white; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 0);");
            });
        }
    }

    public void reloadResources() {
        try {
            // Reload icons
            if (userIconView != null) {
                InputStream userIconStream = getClass().getResourceAsStream("/com/example/stores/images/layout/user_icon.png");
                if (userIconStream != null) {
                    Image userIcon = new Image(userIconStream);
                    userIconView.setImage(userIcon);

                    ColorAdjust gray = new ColorAdjust();
                    gray.setSaturation(-1);
                    userIconView.setEffect(gray);
                } else {
                    System.err.println("Không tìm thấy file user_icon.png");
                }
            }

            if (lockIconView != null) {
                InputStream lockIconStream = getClass().getResourceAsStream("/com/example/stores/images/layout/lock_icon.png");
                if (lockIconStream != null) {
                    Image lockIcon = new Image(lockIconStream);
                    lockIconView.setImage(lockIcon);

                    ColorAdjust gray = new ColorAdjust();
                    gray.setSaturation(-1);
                    lockIconView.setEffect(gray);
                } else {
                    System.err.println("Không tìm thấy file lock_icon.png");
                }
            }

            if (eyeIconView != null) {
                InputStream eyeIconStream = getClass().getResourceAsStream("/com/example/stores/images/layout/eye_icon.png");
                if (eyeIconStream != null) {
                    Image eyeIcon = new Image(eyeIconStream);
                    eyeIconView.setImage(eyeIcon);

                    ColorAdjust gray = new ColorAdjust();
                    gray.setSaturation(-1);
                    eyeIconView.setEffect(gray);
                } else {
                    System.err.println("Không tìm thấy file eye_icon.png");
                }
            }

            // Reload logo
            if (logoIcon != null) {
                InputStream logoStream = getClass().getResourceAsStream("/com/example/stores/images/layout/Logo_Comp.png");
                if (logoStream != null) {
                    Image logo = new Image(logoStream);
                    logoIcon.setImage(logo);
                } else {
                    System.err.println("Không tìm thấy file Logo_Comp.png");
                }
            }

            // Reload language flag
            if (languageFlag != null) {
                if (isVietnamese) {
                    InputStream vnFlagStream = getClass().getResourceAsStream("/com/example/stores/images/layout/flag_vn.png");
                    if (vnFlagStream != null) {
                        Image vnFlag = new Image(vnFlagStream, 30, 20, true, true);
                        languageFlag.setImage(vnFlag);
                    } else {
                        System.err.println("Không tìm thấy file flag_vn.png");
                    }
                } else {
                    InputStream enFlagStream = getClass().getResourceAsStream("/com/example/stores/images/layout/flag_en.png");
                    if (enFlagStream != null) {
                        Image enFlag = new Image(enFlagStream, 30, 20, true, true);
                        languageFlag.setImage(enFlag);
                    } else {
                        System.err.println("Không tìm thấy file flag_en.png");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reloading resources: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEyeButtonAction(ActionEvent event) {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            visiblePasswordField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            
            // Đổi biểu tượng thành stopeye_icon khi hiển thị mật khẩu
            try {
                Image stopEyeIcon = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/stopeye_icon.png"));
                eyeIconView.setImage(stopEyeIcon);
                
                // Giữ hiệu ứng màu xám cho icon mới
                ColorAdjust gray = new ColorAdjust();
                gray.setSaturation(-1);
                eyeIconView.setEffect(gray);
            } catch (Exception e) {
                System.err.println("Không thể tải biểu tượng stopeye_icon: " + e.getMessage());
            }
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            
            // Đổi biểu tượng lại thành eye_icon khi ẩn mật khẩu
            try {
                Image eyeIcon = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/eye_icon.png"));
                eyeIconView.setImage(eyeIcon);
                
                // Giữ hiệu ứng màu xám cho icon mới
                ColorAdjust gray = new ColorAdjust();
                gray.setSaturation(-1);
                eyeIconView.setEffect(gray);
            } catch (Exception e) {
                System.err.println("Không thể tải biểu tượng eye_icon: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordVisible ? visiblePasswordField.getText() : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showStatus(isVietnamese ? "Vui lòng nhập đầy đủ thông tin" : "Please enter username and password", true);
            return;
        }

        boolean success = authService.login(username, password);
        if (success) {
            // Hiển thị thông báo đăng nhập thành công
            showStatus(isVietnamese ? "Đăng nhập thành công!" : "Login successful!", false);

            // Thêm CSS class để làm nổi bật thông báo thành công
            statusMessage.getStyleClass().add("success-message");

            // Thêm hiệu ứng fade-in cho thông báo
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), statusMessage);
            fadeTransition.setFromValue(0.0);
            fadeTransition.setToValue(1.0);
            fadeTransition.play();

            // Delay trước khi chuyển trang
            PauseTransition delay = new PauseTransition(Duration.millis(1000));
            delay.setOnFinished(e -> {
                try {
                    // Lưu trạng thái ngôn ngữ hiện tại vào LanguageManager trước khi đăng nhập
                    LanguageManager.setVietnamese(isVietnamese);
                    System.out.println("Đăng nhập với ngôn ngữ: " + (isVietnamese ? "Tiếng Việt" : "English"));

                    // Lấy kích thước và vị trí hiện tại
                    Stage currentStage = (Stage) loginButton.getScene().getWindow();
                    double width = currentStage.getWidth();
                    double height = currentStage.getHeight();
                    double x = currentStage.getX();
                    double y = currentStage.getY();

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Home.fxml"));
                    Parent root = loader.load();

                    // Thiết lập stage với scene mới
                    currentStage.setScene(new Scene(root));
                    currentStage.setTitle(isVietnamese ? "CELLCOMP STORE - Trang chủ" : "CELLCOMP STORE - Home");

                    // Giữ nguyên kích thước và vị trí
                    currentStage.setWidth(width);
                    currentStage.setHeight(height);
                    currentStage.setX(x);
                    currentStage.setY(y);

                    // Lấy controller để đảm bảo ngôn ngữ được cập nhật đúng
                    HomeController controller = loader.getController();
                    if (controller != null) {
                        Platform.runLater(() -> {
                            // Đảm bảo các tài nguyên được tải lại đúng với ngôn ngữ hiện tại
                            controller.refreshLanguageDisplay();
                        });
                    }
                } catch (IOException ex) {
                    showStatus(isVietnamese ? "Lỗi khi tải giao diện chính" : "Error loading Home", true);
                    ex.printStackTrace();
                }
            });
            delay.play();
        } else {
            showStatus(isVietnamese ? "Tên đăng nhập hoặc mật khẩu không đúng" : "Invalid username or password", true);
        }
    }

    @FXML
    private void handleRegisterButtonAction(ActionEvent event) {
        try {
            // Lấy kích thước hiện tại của cửa sổ
            Stage currentStage = (Stage) registerButton.getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            
            // Debug để theo dõi kích thước
            System.out.println("Switching to Register with dimensions: " + width + "x" + height);
            
            // Lưu trữ ngôn ngữ trước khi chuyển trang
            LanguageManager.setVietnamese(isVietnamese);
            
            // Sử dụng đường dẫn tuyệt đối
            URL registerUrl = getClass().getResource("/com/example/stores/view/CustomerRegister.fxml");
            System.out.println("Trying to load CustomerRegister.fxml...");
            FXMLLoader loader = new FXMLLoader(registerUrl);
            Parent root = loader.load();
            
            // Tạo scene mới
            Scene scene = new Scene(root);
            
            // Thiết lập stage với scene mới
            currentStage.setScene(scene);
            currentStage.setTitle(isVietnamese ? "CELLCOMP STORE - Đăng ký" : "CELLCOMP STORE - Register");
            
            // Giữ nguyên kích thước và vị trí
            currentStage.setWidth(width);
            currentStage.setHeight(height);
            currentStage.setX(x);
            currentStage.setY(y);
            
            // Lấy controller để cập nhật UI sau khi chuyển màn hình
            CustomerRegisterController controller = loader.getController();
            if (controller != null) {
                Platform.runLater(controller::reloadResources);
            }
        } catch (Exception e) {
            showStatus(isVietnamese ? "Lỗi khi tải giao diện đăng ký: " + e.getMessage() : 
                       "Error loading registration form: " + e.getMessage(), true);
            System.err.println("Error details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            // Lưu trạng thái ngôn ngữ hiện tại
            LanguageManager.setVietnamese(isVietnamese);

            // Lấy kích thước hiện tại của cửa sổ
            Stage currentStage = (Stage) forgotPasswordLink.getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();

            // Tải FXML đổi mật khẩu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/ChangePassword.fxml"));
            Parent root = loader.load();

            // Thiết lập stage với scene mới
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle(isVietnamese ? "CELLCOMP STORE - Đổi mật khẩu" : "CELLCOMP STORE - Change Password");

            // Giữ nguyên kích thước và vị trí
            currentStage.setWidth(width);
            currentStage.setHeight(height);
            currentStage.setX(x);
            currentStage.setY(y);

            // Lấy controller để cập nhật UI sau khi chuyển màn hình
            ChangePasswordController controller = loader.getController();
            if (controller != null) {
                // Thiết lập nguồn vào là từ màn hình đăng nhập
                controller.setSourceScreen("login");

                // Nếu có thông tin từ trường username, gửi sang trang đổi mật khẩu
                String username = usernameField.getText().trim();
                if (!username.isEmpty()) {
                    // Gọi phương thức trong ChangePasswordController để điền email tương ứng với username
                    Platform.runLater(() -> controller.prefillEmailFromUsername(username));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở trang đổi mật khẩu: " + e.getMessage() :
                            "Cannot open change password page: " + e.getMessage()
            );
        }
    }

    @FXML
    private void handleLanguageButtonAction(ActionEvent event) {
        isVietnamese = !isVietnamese;

        // CẬP NHẬT TRỰC TIẾP vào LanguageManager
        LanguageManager.setVietnamese(isVietnamese);
        System.out.println("LoginController đã thay đổi ngôn ngữ thành: " + (isVietnamese ? "Tiếng Việt" : "English"));

        if (isVietnamese) {
            langButton.setText("Tiếng Việt");
            try {
                Image vnFlag = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/flag_vn.png"), 30, 20, true, true);
                languageFlag.setImage(vnFlag);
            } catch (Exception e) {}
            updateLanguageToVietnamese();
        } else {
            langButton.setText("English");
            try {
                Image enFlag = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/flag_en.png"), 30, 20, true, true);
                languageFlag.setImage(enFlag);
            } catch (Exception e) {}
            updateLanguageToEnglish();
        }
    }

    private void updateLanguageToVietnamese() {
        welcomeLabel.setText("CHÀO MỪNG");
        userLoginLabel.setText("Đăng nhập");
        usernameField.setPromptText("Tên đăng nhập");
        passwordField.setPromptText("Mật khẩu");
        visiblePasswordField.setPromptText("Mật khẩu");
        forgotPasswordLink.setText("Quên mật khẩu?");
        loginButton.setText("Đăng nhập");
        registerButton.setText("Đăng ký");
    }

    private void updateLanguageToEnglish() {
        welcomeLabel.setText("WELCOME");
        userLoginLabel.setText("User Login");
        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");
        visiblePasswordField.setPromptText("Password");
        forgotPasswordLink.setText("Forgot password?");
        loginButton.setText("Log In");
        registerButton.setText("Register");
    }

    // Hàm căn giữa label
    private void centerWelcomeLabel() {
        Platform.runLater(() -> {
            if (loginFormPane != null && welcomeLabel != null) {
                double paneWidth = loginFormPane.getWidth();
                double labelWidth = welcomeLabel.getBoundsInLocal().getWidth();
                double paneHeight = loginFormPane.getHeight();
                double welcomeY = paneHeight * 0.05;
                welcomeLabel.setLayoutX((paneWidth - labelWidth) / 2);
                welcomeLabel.setLayoutY(welcomeY);
            }
        });
    }

    private void showStatus(String message, boolean isError) {
        statusMessage.setText(message);
        statusMessage.setStyle("-fx-text-fill: " + (isError ? "red" : "green"));
    }

    public void setupResponsiveLayout() {
        try {
            Scene scene = rootPane.getScene();
            if (scene != null) {
                // Log để kiểm tra scene đã được tạo
                System.out.println("Scene created with size: " + scene.getWidth() + "x" + scene.getHeight());
                
                // Đăng ký listeners để phát hiện thay đổi kích thước
                scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                    updateLayout();
                });
                
                scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                    updateLayout();
                });
                
                // Gọi ngay lập tức để thiết lập ban đầu
                updateLayout();
            } else {
                System.err.println("Scene chưa được tạo, không thể thiết lập responsive layout");
                
                // Thử lại sau một khoảng thời gian
                Platform.runLater(() -> {
                    if (rootPane.getScene() != null) {
                        setupResponsiveLayout();
                    } else {
                        System.err.println("Không thể thiết lập responsive layout sau khi thử lại");
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi thiết lập responsive layout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateLayout() {
        try {
            if (rootPane == null || rootPane.getScene() == null) {
                System.err.println("rootPane hoặc scene là null, không thể cập nhật layout");
                return;
            }

            double width = rootPane.getScene().getWidth();
            double height = rootPane.getScene().getHeight();

            System.out.println("Updating layout for size: " + width + "x" + height);

            // Đảm bảo background luôn lấp đầy màn hình
            backgroundRect.setWidth(width);
            backgroundRect.setHeight(height);

            // Cập nhật header
            headerPane.setPrefWidth(width - 40);
            headerRect.setWidth(width - 40 + 30);

            // Tính toán các khoảng cách và kích thước tương đối
            double horizontalGap = width * 0.025;
            double availableWidth = width - horizontalGap * 3;
            double partWidth = availableWidth / 2;
            double contentHeight = height - 113 - horizontalGap;

            // Điều chỉnh form đăng nhập
            loginFormPane.setPrefWidth(partWidth);
            loginFormPane.setPrefHeight(contentHeight);
            loginFormPane.setLayoutX(width - partWidth - horizontalGap);
            loginFormPane.setLayoutY(113);

            // Điều chỉnh ảnh minh họa
            double maxIllustrationHeight = contentHeight;
            double maxIllustrationWidth = partWidth;
            double originalRatio = 454.0 / 450.0;
            double illustrationWidth, illustrationHeight;

            if (maxIllustrationWidth / maxIllustrationHeight > originalRatio) {
                illustrationHeight = maxIllustrationHeight;
                illustrationWidth = illustrationHeight * originalRatio;
            } else {
                illustrationWidth = maxIllustrationWidth;
                illustrationHeight = illustrationWidth / originalRatio;
            }

            illustrationImage.setFitWidth(illustrationWidth);
            illustrationImage.setFitHeight(illustrationHeight);
            illustrationImage.setLayoutX(horizontalGap);
            illustrationImage.setLayoutY(113 + (contentHeight - illustrationHeight) / 2);

            // Tính toán các kích thước và vị trí bên trong form đăng nhập
            double formContentWidth = partWidth * 0.8;
            double leftMargin = (partWidth - formContentWidth) / 2;
            double heightRatio = contentHeight / 460.0;

            // Vị trí các phần tử theo tỷ lệ chiều cao
            final double welcomeY = contentHeight * 0.05;
            final double loginLabelY = welcomeY + 50 * heightRatio;
            final double usernameBoxY = loginLabelY + 45 * heightRatio;
            final double passwordBoxY = usernameBoxY + 60 * heightRatio;
            final double forgotLinkY = passwordBoxY + 50 * heightRatio;
            final double loginButtonY = forgotLinkY + 50 * heightRatio;

            // Chiều cao cho các phần tử nhập liệu
            final double fieldHeight = 32 * heightRatio;

            // Cập nhật UI trong JavaFX thread
            Platform.runLater(() -> {
                try {
                    // Căn giữa tiêu đề
                    userLoginLabel.setLayoutX((loginFormPane.getPrefWidth() - userLoginLabel.getBoundsInLocal().getWidth()) / 2);
                    userLoginLabel.setLayoutY(loginLabelY);

                    // Cập nhật vị trí và kích thước cho các trường nhập liệu
                    usernameBox.setLayoutX(leftMargin);
                    usernameBox.setLayoutY(usernameBoxY);
                    usernameBox.setPrefWidth(formContentWidth);
                    usernameBox.setPrefHeight(fieldHeight);

                    passwordBox.setLayoutX(leftMargin);
                    passwordBox.setLayoutY(passwordBoxY);
                    passwordBox.setPrefWidth(formContentWidth);
                    passwordBox.setPrefHeight(fieldHeight);

                    // Điều chỉnh chiều cao cho các trường nhập liệu
                    usernameField.setPrefHeight(fieldHeight);
                    passwordField.setPrefHeight(fieldHeight);
                    visiblePasswordField.setPrefHeight(fieldHeight);

                    // Điều chỉnh độ rộng trường nhập liệu
                    double iconSpace = 34; // 24px icon + 10px spacing
                    double fieldWidth = formContentWidth - iconSpace;
                    if (fieldWidth < 100) fieldWidth = 100;

                    usernameField.setPrefWidth(fieldWidth - 50);
                    passwordField.setPrefWidth(fieldWidth);
                    visiblePasswordField.setPrefWidth(fieldWidth);

                    // Cập nhật vị trí cho các phần tử khác
                    forgotPasswordLink.setLayoutX(leftMargin);
                    forgotPasswordLink.setLayoutY(forgotLinkY);

                    loginButton.setLayoutX(leftMargin + 40);
                    loginButton.setLayoutY(loginButtonY);
                    loginButton.setPrefWidth(formContentWidth - 85);
                    loginButton.setPrefHeight(40 * heightRatio);

                    // Lưu trữ gradient gốc cho login button
                    final String loginGradient = "linear-gradient(to right, #865DFF, #5CB8E4)";
                    final String loginHoverGradient = "linear-gradient(to right, #5CB8E4, #865DFF)";

                    // Lưu trữ gradient gốc cho register button
                    final String registerGradient = "linear-gradient(to right, #5CB8E4, #865DFF)";
                    final String registerHoverGradient = "linear-gradient(to right, #865DFF, #5CB8E4)";

                    // Set font size và các thuộc tính cơ bản
                    loginButton.setStyle(
                            "-fx-background-radius: 20; " +
                                    "-fx-font-size: " + (16 * heightRatio) + "px; " +
                                    "-fx-background-color: " + loginGradient + "; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-cursor: hand;"
                    );

                    registerButton.setStyle(
                            "-fx-background-radius: 12 0 12 0; " +
                                    "-fx-font-size: " + (14 * heightRatio) + "px; " +
                                    "-fx-background-color: " + registerGradient + "; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-cursor: hand;"
                    );

                    // Thêm event handlers cho hover
                    loginButton.setOnMouseEntered(e -> {
                        loginButton.setStyle(
                                "-fx-background-radius: 20; " +
                                        "-fx-font-size: " + (16 * heightRatio) + "px; " +
                                        "-fx-background-color: " + loginHoverGradient + "; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0); " +
                                        "-fx-cursor: hand;"
                        );
                    });

                    loginButton.setOnMouseExited(e -> {
                        loginButton.setStyle(
                                "-fx-background-radius: 20; " +
                                        "-fx-font-size: " + (16 * heightRatio) + "px; " +
                                        "-fx-background-color: " + loginGradient + "; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-cursor: hand;"
                        );
                    });

                    registerButton.setOnMouseEntered(e -> {
                        registerButton.setStyle(
                                "-fx-background-radius: 12 0 12 0; " +
                                        "-fx-font-size: " + (14 * heightRatio) + "px; " +
                                        "-fx-background-color: " + registerHoverGradient + "; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 0); " +
                                        "-fx-cursor: hand;"
                        );
                    });

                    registerButton.setOnMouseExited(e -> {
                        registerButton.setStyle(
                                "-fx-background-radius: 12 0 12 0; " +
                                        "-fx-font-size: " + (14 * heightRatio) + "px; " +
                                        "-fx-background-color: " + registerGradient + "; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-cursor: hand;"
                        );
                    });

                    welcomeLabel.setStyle("-fx-font-size: " + (36 * heightRatio) +
                            "px; -fx-font-family: 'Arial'; -fx-text-fill: #BEA4F2;");

                    userLoginLabel.setStyle("-fx-font-size: " + (22 * heightRatio) +
                            "px; -fx-font-weight: bold; -fx-text-fill: #3498DB;");

                    // Cập nhật vị trí status message
                    statusMessage.setLayoutX(leftMargin);
                    statusMessage.setLayoutY(loginButtonY + 50 * heightRatio);

                    // Điều chỉnh vị trí nút đăng ký
                    double registerBtnWidth = Math.min(formContentWidth * 0.4, 120);
                    registerButton.setPrefWidth(registerBtnWidth);
                    registerButton.setMaxWidth(registerBtnWidth);

                    double rightPadding = 14;
                    double bottomPadding = 30;
                    registerButton.setLayoutX(loginFormPane.getPrefWidth() - registerBtnWidth - rightPadding);

                    double registerBtnHeight = registerButton.getPrefHeight() > 0 ?
                            registerButton.getPrefHeight() : 30 * heightRatio;
                    registerButton.setLayoutY(loginFormPane.getPrefHeight() - registerBtnHeight - bottomPadding);
                    registerButton.setPrefHeight(30 * heightRatio);

                    // Căn giữa welcome label
                    welcomeLabel.setLayoutY(welcomeY);
                    centerWelcomeLabel();

                } catch (Exception e) {
                    System.err.println("Lỗi khi cập nhật vị trí các thành phần: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            // Cập nhật vị trí nút ngôn ngữ và cờ
            double langButtonWidth = langButton.getPrefWidth() > 0 ? langButton.getPrefWidth() : 80;
            double rightMargin = 60;
            langButton.setLayoutX(width - langButtonWidth - rightMargin);
            languageFlag.setLayoutX(langButton.getLayoutX() - 38);

        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật layout: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Phương thức để cập nhật UI theo ngôn ngữ ban đầu từ LanguageManager
     * Được gọi sau khi đăng xuất để đảm bảo đồng bộ ngôn ngữ
     */
    public void updateInitialLanguage() {
        // Lấy trạng thái ngôn ngữ từ LanguageManager
        isVietnamese = LanguageManager.isVietnamese();
        System.out.println("Cập nhật ngôn ngữ ban đầu: " + (isVietnamese ? "Tiếng Việt" : "English"));

        // Cập nhật UI theo ngôn ngữ
        if (isVietnamese) {
            langButton.setText("Tiếng Việt");
            try {
                Image vnFlag = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/flag_vn.png"), 30, 20, true, true);
                languageFlag.setImage(vnFlag);
            } catch (Exception e) {
                System.err.println("Không thể tải hình ảnh cờ VN: " + e.getMessage());
            }
            updateLanguageToVietnamese();
        } else {
            langButton.setText("English");
            try {
                Image enFlag = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/flag_en.png"), 30, 20, true, true);
                languageFlag.setImage(enFlag);
            } catch (Exception e) {
                System.err.println("Không thể tải hình ảnh cờ EN: " + e.getMessage());
            }
            updateLanguageToEnglish();
        }
    }
    // Thêm vào phần khai báo các thành phần @FXML:
@FXML private Button logoutButton;
@FXML private ImageView logoutIcon;

// Thêm phương thức xử lý sự kiện logout sau các phương thức xử lý sự kiện khác:
@FXML
private void handleLogoutButtonAction(ActionEvent event) {
    try {
        // Lấy kích thước và vị trí hiện tại
        Stage currentStage = (Stage) logoutButton.getScene().getWindow();
        double width = currentStage.getWidth();
        double height = currentStage.getHeight();
        double x = currentStage.getX();
        double y = currentStage.getY();
        
        // Tải giao diện RoleSelection
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/RoleSelection.fxml"));
        Parent root = loader.load();
        
        // Thiết lập scene mới
        Scene scene = new Scene(root);
        currentStage.setScene(scene);
        currentStage.setTitle(isVietnamese ? "CELLCOMP STORE - Chọn vai trò" : "CELLCOMP STORE - Role Selection");
        
        // Giữ nguyên kích thước và vị trí
        currentStage.setWidth(width);
        currentStage.setHeight(height);
        currentStage.setX(x);
        currentStage.setY(y);
        
        // Cập nhật controller để tải lại tài nguyên nếu cần
        RoleSelectionController controller = loader.getController();
        if (controller != null) {
            Platform.runLater(() -> {
                // Đảm bảo giao diện được cập nhật sau khi đổi màn hình
                // Cập nhật ngôn ngữ từ LanguageManager
            });
        }
    } catch (IOException e) {
        System.err.println("Không thể chuyển về trang chọn vai trò: " + e.getMessage());
        e.printStackTrace();
        String title = isVietnamese ? "Lỗi" : "Error";
        String message = isVietnamese ? 
            "Không thể quay lại màn hình chọn vai trò: " : 
            "Cannot return to role selection screen: ";
        showStatus(message + e.getMessage(), true);
    }
}

}