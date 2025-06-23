package com.example.stores.controller;

import com.example.stores.service.impl.AuthService;
import com.example.stores.service.impl.EmailService;
import com.example.stores.util.AlertUtils;
import com.example.stores.util.LanguageManager;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class ChangePasswordController implements Initializable {

    @FXML private Button backButton;
    @FXML private Button langButton;
    @FXML private ImageView languageFlag;
    @FXML private Label titleLabel;
    @FXML private Label emailInstructionLabel;
    @FXML private TextField emailField;
    @FXML private Button sendVerificationButton;
    @FXML private TextField verificationCodeField;
    @FXML private Button verifyCodeButton;
    @FXML private Label emailErrorLabel;
    @FXML private Label timerLabel;
    @FXML private ProgressIndicator sendingProgress;

    @FXML private AnchorPane emailVerificationContainer;
    @FXML private AnchorPane newPasswordContainer;
    @FXML private AnchorPane successContainer;

    @FXML private Label passwordInstructionLabel;
    @FXML private PasswordField newPasswordField;
    @FXML private TextField visibleNewPasswordField;
    @FXML private Button toggleNewPasswordVisibility;
    @FXML private ImageView newPasswordEyeIcon;
    @FXML private Label newPasswordErrorLabel;

    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField visibleConfirmPasswordField;
    @FXML private Button toggleConfirmPasswordVisibility;
    @FXML private ImageView confirmPasswordEyeIcon;
    @FXML private Label confirmPasswordErrorLabel;
    @FXML private Button changePasswordButton;

    @FXML private Label successLabel;
    @FXML private Label successDetailLabel;
    @FXML private Button backToProfileButton;

    private EmailService emailService;
    private AuthService authService;
    private boolean isVietnamese;
    private boolean newPasswordVisible = false;
    private boolean confirmPasswordVisible = false;
    private String currentVerificationCode;
    private String userEmail;
    private Timeline countdownTimer;
    private int remainingTimeInSeconds = 300; // 5 phút

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo services
        emailService = new EmailService();
        authService = new AuthService();

        // Tải ngôn ngữ hiện tại
        isVietnamese = LanguageManager.isVietnamese();
        updateLanguage();

        // Lấy thông tin người dùng hiện tại
        Customer currentUser = AuthService.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            emailField.setText(currentUser.getEmail());
            userEmail = currentUser.getEmail();
        }

        // Thiết lập đồng bộ giữa password fields
        setupPasswordFields();

        // Đặt UI ban đầu
        emailVerificationContainer.setVisible(true);
        newPasswordContainer.setVisible(false);
        successContainer.setVisible(false);
        timerLabel.setVisible(false);
    }

    @FXML
    private void handleSendVerification() {
        userEmail = emailField.getText().trim();

        // Kiểm tra email hợp lệ
        if (userEmail.isEmpty()) {
            emailErrorLabel.setText(isVietnamese ? "Vui lòng nhập email" : "Please enter email");
            return;
        }

        if (!isValidEmail(userEmail)) {
            emailErrorLabel.setText(isVietnamese ? "Email không hợp lệ" : "Invalid email format");
            return;
        }

        // Kiểm tra email tồn tại trong hệ thống
        boolean emailExists = authService.isEmailExists(userEmail);
        if (!emailExists) {
            emailErrorLabel.setText(isVietnamese ?
                    "Email không tồn tại trong hệ thống" :
                    "Email doesn't exist in the system");
            return;
        }

        // Hiển thị loading
        sendingProgress.setVisible(true);
        sendVerificationButton.setDisable(true);
        emailErrorLabel.setText("");

        // Gửi mã xác thực
        CompletableFuture<String> future = emailService.sendVerificationCode(userEmail);

        future.thenAccept(code -> {
            Platform.runLater(() -> {
                currentVerificationCode = code;
                sendingProgress.setVisible(false);
                sendVerificationButton.setDisable(false);

                // Hiển thị thông báo
                String message = isVietnamese ?
                        "Mã xác thực đã được gửi đến email của bạn" :
                        "Verification code has been sent to your email";
                emailErrorLabel.setText(message);
                emailErrorLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");

                // Bắt đầu đếm ngược
                startCountdown();
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                sendingProgress.setVisible(false);
                sendVerificationButton.setDisable(false);
                emailErrorLabel.setText(isVietnamese ?
                        "Lỗi: Không thể gửi email. " + ex.getMessage() :
                        "Error: Cannot send email. " + ex.getMessage());
                emailErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
            });
            return null;
        });
    }

    @FXML
    private void handleVerifyCode() {
        String enteredCode = verificationCodeField.getText().trim();

        // Kiểm tra mã xác thực
        if (enteredCode.isEmpty()) {
            emailErrorLabel.setText(isVietnamese ? "Vui lòng nhập mã xác thực" : "Please enter verification code");
            return;
        }

        if (currentVerificationCode == null) {
            emailErrorLabel.setText(isVietnamese ? "Vui lòng gửi mã xác thực trước" : "Please send verification code first");
            return;
        }

        // So sánh mã xác thực
        if (enteredCode.equals(currentVerificationCode)) {
            // Dừng đếm ngược
            stopCountdown();

            // Chuyển sang màn hình đặt mật khẩu mới
            emailVerificationContainer.setVisible(false);
            newPasswordContainer.setVisible(true);
        } else {
            emailErrorLabel.setText(isVietnamese ? "Mã xác thực không đúng" : "Incorrect verification code");
        }
    }

    @FXML
    private void handleChangePassword() {
        String newPassword = newPasswordVisible ? visibleNewPasswordField.getText() : newPasswordField.getText();
        String confirmPassword = confirmPasswordVisible ? visibleConfirmPasswordField.getText() : confirmPasswordField.getText();

        // Kiểm tra mật khẩu mới
        if (newPassword.isEmpty()) {
            newPasswordErrorLabel.setText(isVietnamese ? "Vui lòng nhập mật khẩu mới" : "Please enter new password");
            return;
        }

        // Kiểm tra yêu cầu mật khẩu chi tiết
        if (newPassword.length() < 8) {
            newPasswordErrorLabel.setText(isVietnamese ?
                    "Mật khẩu phải có ít nhất 8 ký tự" :
                    "Password must have at least 8 characters");
            return;
        } else if (!newPassword.matches(".*[A-Z].*")) {
            newPasswordErrorLabel.setText(isVietnamese ?
                    "Mật khẩu phải có ít nhất 1 chữ hoa" :
                    "Password must have at least 1 uppercase letter");
            return;
        } else if (!newPassword.matches(".*[a-z].*")) {
            newPasswordErrorLabel.setText(isVietnamese ?
                    "Mật khẩu phải có ít nhất 1 chữ thường" :
                    "Password must have at least 1 lowercase letter");
            return;
        } else if (!newPassword.matches(".*[0-9].*")) {
            newPasswordErrorLabel.setText(isVietnamese ?
                    "Mật khẩu phải có ít nhất 1 chữ số" :
                    "Password must have at least 1 digit");
            return;
        } else if (!newPassword.matches(".*[^a-zA-Z0-9].*")) {
            newPasswordErrorLabel.setText(isVietnamese ?
                    "Mật khẩu phải có ít nhất 1 ký tự đặc biệt" :
                    "Password must have at least 1 special character");
            return;
        } else {
            newPasswordErrorLabel.setText("");
        }

        // Kiểm tra xác nhận mật khẩu
        if (confirmPassword.isEmpty()) {
            confirmPasswordErrorLabel.setText(isVietnamese ? "Vui lòng xác nhận mật khẩu" : "Please confirm your password");
            return;
        }

        if (!confirmPassword.equals(newPassword)) {
            confirmPasswordErrorLabel.setText(isVietnamese ? "Xác nhận mật khẩu không khớp" : "Password confirmation doesn't match");
            return;
        } else {
            confirmPasswordErrorLabel.setText("");
        }

        // Thực hiện đổi mật khẩu
        Customer currentUser = AuthService.getCurrentUser();
        boolean success = authService.changePassword(currentUser.getId(), newPassword);

        if (success) {
            // Hiển thị màn hình thành công
            newPasswordContainer.setVisible(false);
            successContainer.setVisible(true);

            // Cập nhật nút quay lại theo nguồn vào
            if ("login".equals(sourceScreen)) {
                backToProfileButton.setText(isVietnamese ? "Quay lại đăng nhập" : "Back to Login");
            } else {
                backToProfileButton.setText(isVietnamese ? "Quay lại trang cá nhân" : "Back to Profile");
            }
        } else {
            // Hiển thị lỗi
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể đổi mật khẩu. Vui lòng thử lại sau." :
                            "Cannot change password. Please try again later."
            );
        }
    }

    @FXML
    private void handleBackToProfile() {
        try {
            // Lưu trạng thái ngôn ngữ hiện tại
            LanguageManager.setVietnamese(isVietnamese);

            // Lấy Stage hiện tại
            Stage stage = (Stage) backButton.getScene().getWindow();

            // Chuyển hướng dựa trên nguồn vào
            if ("login".equals(sourceScreen)) {
                // Nếu từ màn hình đăng nhập, quay lại màn hình đăng nhập
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomerLogin.fxml"));
                Parent root = loader.load();

                // Thiết lập scene mới
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle(isVietnamese ? "CELLCOMP STORE - Đăng nhập" : "CELLCOMP STORE - Login");

                // Lấy controller để cập nhật ngôn ngữ
                CustomerLoginController controller = loader.getController();
                if (controller != null) {
                    controller.updateInitialLanguage();
                }
            } else {
                // Nếu từ màn hình thông tin cá nhân, quay lại màn hình đó (giữ nguyên code hiện tại)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomerChange.fxml"));
                Parent root = loader.load();

                // Thiết lập scene mới
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle(isVietnamese ? "CELLCOMP STORE - Thông tin tài khoản" : "CELLCOMP STORE - Account Information");
            }

            // Hiển thị stage
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể quay lại: " + e.getMessage() :
                            "Cannot go back: " + e.getMessage()
            );
        }
    }

    @FXML
    private void handleLanguageButtonAction() {
        isVietnamese = !isVietnamese;
        LanguageManager.setVietnamese(isVietnamese);
        updateLanguage();
    }

    @FXML
    private void toggleNewPasswordVisibility() {
        newPasswordVisible = !newPasswordVisible;

        if (newPasswordVisible) {
            visibleNewPasswordField.setText(newPasswordField.getText());
            newPasswordField.setVisible(false);
            newPasswordField.setManaged(false);
            visibleNewPasswordField.setVisible(true);
            visibleNewPasswordField.setManaged(true);

            // Thay đổi biểu tượng
            try {
                Image stopEyeIcon = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/stopeye_icon.png"));
                newPasswordEyeIcon.setImage(stopEyeIcon);
            } catch (Exception e) {
                System.err.println("Cannot load stopeye_icon: " + e.getMessage());
            }
        } else {
            newPasswordField.setText(visibleNewPasswordField.getText());
            newPasswordField.setVisible(true);
            newPasswordField.setManaged(true);
            visibleNewPasswordField.setVisible(false);
            visibleNewPasswordField.setManaged(false);

            // Thay đổi biểu tượng
            try {
                Image eyeIcon = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/eye_icon.png"));
                newPasswordEyeIcon.setImage(eyeIcon);
            } catch (Exception e) {
                System.err.println("Cannot load eye_icon: " + e.getMessage());
            }
        }
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        confirmPasswordVisible = !confirmPasswordVisible;

        if (confirmPasswordVisible) {
            visibleConfirmPasswordField.setText(confirmPasswordField.getText());
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            visibleConfirmPasswordField.setVisible(true);
            visibleConfirmPasswordField.setManaged(true);

            // Thay đổi biểu tượng
            try {
                Image stopEyeIcon = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/stopeye_icon.png"));
                confirmPasswordEyeIcon.setImage(stopEyeIcon);
            } catch (Exception e) {
                System.err.println("Cannot load stopeye_icon: " + e.getMessage());
            }
        } else {
            confirmPasswordField.setText(visibleConfirmPasswordField.getText());
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            visibleConfirmPasswordField.setVisible(false);
            visibleConfirmPasswordField.setManaged(false);

            // Thay đổi biểu tượng
            try {
                Image eyeIcon = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/eye_icon.png"));
                confirmPasswordEyeIcon.setImage(eyeIcon);
            } catch (Exception e) {
                System.err.println("Cannot load eye_icon: " + e.getMessage());
            }
        }
    }

    // Đồng bộ giữa các trường mật khẩu
    private void setupPasswordFields() {
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newPasswordVisible) {
                visibleNewPasswordField.setText(newVal);
            }
        });

        visibleNewPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newPasswordVisible) {
                newPasswordField.setText(newVal);
            }
        });

        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!confirmPasswordVisible) {
                visibleConfirmPasswordField.setText(newVal);
            }
        });

        visibleConfirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (confirmPasswordVisible) {
                confirmPasswordField.setText(newVal);
            }
        });
        // Thêm listener để kiểm tra mật khẩu khi người dùng nhập
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newPasswordVisible) {
                visibleNewPasswordField.setText(newVal);
                checkPassword(newVal);
            }
        });

        visibleNewPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newPasswordVisible) {
                newPasswordField.setText(newVal);
                checkPassword(newVal);
            }
        });

        // Kiểm tra khớp mật khẩu
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!confirmPasswordVisible) {
                visibleConfirmPasswordField.setText(newVal);
                checkPasswordsMatch();
            }
        });

        visibleConfirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (confirmPasswordVisible) {
                confirmPasswordField.setText(newVal);
                checkPasswordsMatch();
            }
        });
    }

    // Cập nhật ngôn ngữ hiển thị
    private void updateLanguage() {
        // Cập nhật text trên giao diện
        titleLabel.setText(isVietnamese ? "Đổi mật khẩu" : "Change Password");
        emailInstructionLabel.setText(isVietnamese ?
                "Nhập email của bạn để nhận mã xác thực:" :
                "Enter your email to receive a verification code:");
        emailField.setPromptText(isVietnamese ? "Email" : "Email");
        sendVerificationButton.setText(isVietnamese ? "Gửi mã" : "Send code");
        verificationCodeField.setPromptText(isVietnamese ? "Nhập mã xác thực" : "Enter verification code");
        verifyCodeButton.setText(isVietnamese ? "Xác nhận" : "Verify");

        passwordInstructionLabel.setText(isVietnamese ?
                "Nhập mật khẩu mới của bạn:" :
                "Enter your new password:");
        newPasswordField.setPromptText(isVietnamese ? "Mật khẩu mới" : "New password");
        visibleNewPasswordField.setPromptText(isVietnamese ? "Mật khẩu mới" : "New password");
        confirmPasswordField.setPromptText(isVietnamese ? "Xác nhận mật khẩu" : "Confirm password");
        visibleConfirmPasswordField.setPromptText(isVietnamese ? "Xác nhận mật khẩu" : "Confirm password");
        changePasswordButton.setText(isVietnamese ? "Đổi mật khẩu" : "Change Password");

        successLabel.setText(isVietnamese ? "Đổi mật khẩu thành công!" : "Password changed successfully!");
        successDetailLabel.setText(isVietnamese ?
                "Mật khẩu của bạn đã được thay đổi thành công. Bạn có thể sử dụng mật khẩu mới để đăng nhập." :
                "Your password has been changed successfully. You can use the new password to log in.");
        backToProfileButton.setText(isVietnamese ? "Quay lại trang cá nhân" : "Back to Profile");

        // Cập nhật hiển thị đếm ngược
        updateCountdownText();

        // Cập nhật ngôn ngữ button
        langButton.setText(isVietnamese ? "Tiếng Việt" : "English");

        // Cập nhật flag
        String flagUrl = isVietnamese ?
                "/com/example/stores/images/layout/flag_vn.png" :
                "/com/example/stores/images/layout/flag_en.png";
        try {
            languageFlag.setImage(new Image(getClass().getResourceAsStream(flagUrl)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Kiểm tra email hợp lệ
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    // Kiểm tra mật khẩu hợp lệ - cần có ít nhất 8 ký tự, chữ hoa, chữ thường, số, ký tự đặc biệt
    private boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) return false;

        boolean hasMinLength = password.length() >= 8;
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[^a-zA-Z0-9].*");

        return hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }

    // Bắt đầu đếm ngược thời gian hiệu lực của mã
    private void startCountdown() {
        // Reset thời gian
        remainingTimeInSeconds = 300; // 5 phút

        // Hiển thị đếm ngược
        timerLabel.setVisible(true);
        updateCountdownText();

        // Hủy timer cũ nếu có
        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        // Tạo timer mới
        countdownTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    remainingTimeInSeconds--;
                    updateCountdownText();

                    if (remainingTimeInSeconds <= 0) {
                        stopCountdown();
                        currentVerificationCode = null;

                        String expiredMessage = isVietnamese ?
                                "Mã xác thực đã hết hạn. Vui lòng lấy mã mới." :
                                "Verification code expired. Please get a new one.";
                        emailErrorLabel.setText(expiredMessage);
                        emailErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                    }
                })
        );
        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();
    }

    // Dừng đếm ngược
    private void stopCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
            timerLabel.setVisible(false);
        }
    }

    private void checkPasswordsMatch() {
        String password = newPasswordVisible ? visibleNewPasswordField.getText() : newPasswordField.getText();
        String confirmPassword = confirmPasswordVisible ? visibleConfirmPasswordField.getText() : confirmPasswordField.getText();

        if (confirmPasswordErrorLabel == null) return;

        if (!confirmPassword.isEmpty()) {
            if (!confirmPassword.equals(password)) {
                confirmPasswordErrorLabel.setText(isVietnamese ?
                        "Mật khẩu không khớp" :
                        "Passwords do not match");
            } else {
                confirmPasswordErrorLabel.setText("");
            }
        } else {
            confirmPasswordErrorLabel.setText("");
        }
    }

    // Cập nhật hiển thị đếm ngược
    private void updateCountdownText() {
        int minutes = remainingTimeInSeconds / 60;
        int seconds = remainingTimeInSeconds % 60;
        String timeText = String.format("%02d:%02d", minutes, seconds);

        timerLabel.setText(isVietnamese ?
                "Mã xác thực sẽ hết hạn sau: " + timeText :
                "Verification code will expire in: " + timeText);
    }
    private void checkPassword(String password) {
        boolean isValid = isValidPassword(password);

        if (newPasswordErrorLabel == null) return;

        if (!password.isEmpty()) {
            if (password.length() < 8) {
                newPasswordErrorLabel.setText(isVietnamese ?
                        "Mật khẩu phải có ít nhất 8 ký tự" :
                        "Password must have at least 8 characters");
            } else if (!password.matches(".*[A-Z].*")) {
                newPasswordErrorLabel.setText(isVietnamese ?
                        "Mật khẩu phải có ít nhất 1 chữ hoa" :
                        "Password must have at least 1 uppercase letter");
            } else if (!password.matches(".*[a-z].*")) {
                newPasswordErrorLabel.setText(isVietnamese ?
                        "Mật khẩu phải có ít nhất 1 chữ thường" :
                        "Password must have at least 1 lowercase letter");
            } else if (!password.matches(".*[0-9].*")) {
                newPasswordErrorLabel.setText(isVietnamese ?
                        "Mật khẩu phải có ít nhất 1 chữ số" :
                        "Password must have at least 1 digit");
            } else if (!password.matches(".*[^a-zA-Z0-9].*")) {
                newPasswordErrorLabel.setText(isVietnamese ?
                        "Mật khẩu phải có ít nhất 1 ký tự đặc biệt" :
                        "Password must have at least 1 special character");
            } else {
                newPasswordErrorLabel.setText("");
            }
        } else {
            newPasswordErrorLabel.setText("");
        }
    }
    /**
     * Phương thức này được gọi từ CustomerLoginController để điền email từ username
     * @param username Username đã nhập trên màn hình đăng nhập
     */
    public void prefillEmailFromUsername(String username) {
        if (username == null || username.isEmpty()) return;

        // Tìm email tương ứng với username từ cơ sở dữ liệu
        String email = authService.getEmailByUsername(username);

        if (email != null && !email.isEmpty()) {
            emailField.setText(email);
            userEmail = email;
        }
    }
    private String sourceScreen = "profile";
    /**
     * Thiết lập nguồn vào của màn hình (từ đâu chuyển tới)
     * @param source "login" nếu từ màn hình đăng nhập, "profile" nếu từ trang thông tin cá nhân
     */
    public void setSourceScreen(String source) {
        this.sourceScreen = source;

        // Chỉnh sửa nội dung nút quay lại theo nguồn
        if (backToProfileButton != null) {
            if ("login".equals(source)) {
                backToProfileButton.setText(isVietnamese ? "Quay lại đăng nhập" : "Back to Login");
            } else {
                backToProfileButton.setText(isVietnamese ? "Quay lại trang cá nhân" : "Back to Profile");
            }
        }
    }

}