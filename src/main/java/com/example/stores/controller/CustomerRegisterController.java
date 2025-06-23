package com.example.stores.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.example.stores.model.Customer;
import com.example.stores.service.impl.AuthService;
import com.example.stores.util.AlertUtils;
import com.example.stores.util.LanguageManager;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Priority;
import java.lang.reflect.Method;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

public class CustomerRegisterController implements Initializable {
    @FXML
    private Button eyeButton;
    @FXML
    private Button confirmEyeButton;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private Rectangle backgroundRect;
    @FXML
    private AnchorPane headerPane;
    @FXML
    private Rectangle headerRect;
    @FXML
    private AnchorPane mainFormPane;
    @FXML
    private VBox personalInfoVBox;
    @FXML
    private VBox accountInfoVBox;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField numberField;
    @FXML
    private ComboBox<Integer> dayCombo;
    @FXML
    private ComboBox<String> monthCombo;
    @FXML
    private ComboBox<Integer> yearCombo;
    @FXML
    private RadioButton maleRadio;
    @FXML
    private RadioButton femaleRadio;
    @FXML
    private RadioButton otherRadio;
    @FXML
    private ToggleGroup genderGroup;
    @FXML
    private ComboBox<String> provinceCombo;
    @FXML
    private ComboBox<String> districtCombo;
    @FXML
    private ComboBox<String> wardCombo;
    @FXML
    private TextField specificAddressField;
    @FXML
    private TextField emailField;
    @FXML
    private ImageView avatarView;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField visiblePasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField visibleConfirmPasswordField;
    @FXML
    private Button registerButton;
    @FXML
    private Button backToLoginButton;
    @FXML
    private Button resetButton;
    @FXML
    private Label statusMessage;
    @FXML
    private Button langButton;
    @FXML
    private ImageView languageFlag;
    @FXML
    private ImageView eyeIconView;
    @FXML
    private ImageView confirmEyeIconView;
    @FXML
    private Label logoLabel;
    @FXML
    private Label storeLabel;
    @FXML
    private Label createAccountLabel;
    @FXML
    private Label registerLabel;
    @FXML
    private Label phoneErrorLabel;
    @FXML
    private Label emailErrorLabel;
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Label confirmPasswordErrorLabel;
    @FXML
    private Label usernameErrorLabel;

    private AuthService authService;
    private boolean isVietnamese = true;
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;
    private File selectedAvatarFile;

    // Validation properties
    private BooleanProperty isPhoneValid = new SimpleBooleanProperty(false);
    private BooleanProperty isEmailValid = new SimpleBooleanProperty(false);
    private BooleanProperty isPasswordValid = new SimpleBooleanProperty(false);
    private BooleanProperty isPasswordMatching = new SimpleBooleanProperty(false);
    private BooleanProperty isUsernameValid = new SimpleBooleanProperty(false);
    // Map of Vietnam provinces, districts, and wards
    private Map<String, List<String>> provinceToDistricts = new HashMap<>();
    private Map<String, List<String>> districtToWards = new HashMap<>();

    // Month names in Vietnamese and English
    private List<String> englishMonths = Arrays.asList(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    );

    private List<String> vietnameseMonths = Arrays.asList(
            "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
            "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    );

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize AuthService
        authService = new AuthService();

        // Setup missing controls if needed
        setupMissingControls();

        // Setup validation listeners
        setupValidation();

        // Initialize date combo boxes
        initDateCombos();

        // Initialize address combo boxes
        initAddressCombos();

        // Get language setting from LanguageManager
        isVietnamese = LanguageManager.isVietnamese();
        System.out.println("Register screen initialized with language: " + (isVietnamese ? "Vietnamese" : "English"));

        // Default select male
        if (genderGroup == null) {
            genderGroup = new ToggleGroup();
            maleRadio.setToggleGroup(genderGroup);
            femaleRadio.setToggleGroup(genderGroup);
            otherRadio.setToggleGroup(genderGroup);
        }
        maleRadio.setSelected(true);

        // Bind register button state
        setupRegisterButtonBinding();

        // Update UI based on language
        updateLanguage();

        // Setup responsive layout
        Platform.runLater(this::setupResponsiveLayout);
    }

    private void setupMissingControls() {
        if (genderGroup == null) {
            genderGroup = new ToggleGroup();

            if (maleRadio != null) maleRadio.setToggleGroup(genderGroup);
            if (femaleRadio != null) femaleRadio.setToggleGroup(genderGroup);
            if (otherRadio != null) otherRadio.setToggleGroup(genderGroup);

            // Create radio buttons if not in fxml
            AnchorPane parent = (AnchorPane) rootPane.getChildren().stream()
                    .filter(node -> node instanceof AnchorPane && !(node.getId() != null && node.getId().equals("headerPane")))
                    .findFirst().orElse(rootPane);

            if (maleRadio == null) {
                maleRadio = new RadioButton("Nam");
                maleRadio.setToggleGroup(genderGroup);
                maleRadio.setLayoutX(65);
                maleRadio.setLayoutY(270);
                parent.getChildren().add(maleRadio);
            }

            if (femaleRadio == null) {
                femaleRadio = new RadioButton("Nữ");
                femaleRadio.setToggleGroup(genderGroup);
                femaleRadio.setLayoutX(130);
                femaleRadio.setLayoutY(270);
                parent.getChildren().add(femaleRadio);
            }

            if (otherRadio == null) {
                otherRadio = new RadioButton("Khác");
                otherRadio.setToggleGroup(genderGroup);
                otherRadio.setLayoutX(180);
                otherRadio.setLayoutY(270);
                parent.getChildren().add(otherRadio);
            }
        }
    }

    private void setupResponsiveLayout() {
        try {
            Scene scene = rootPane.getScene();
            if (scene != null) {
                scene.widthProperty().addListener((obs, oldVal, newVal) -> updateLayout());
                scene.heightProperty().addListener((obs, oldVal, newVal) -> updateLayout());
                updateLayout();
            }
        } catch (Exception e) {
            System.err.println("Could not set up responsive layout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateLayout() {
        try {
            double width = rootPane.getScene().getWidth();
            double height = rootPane.getScene().getHeight();

            // Calculate height ratio
            double heightRatio = height / 600.0;

            // Resize background
            if (backgroundRect != null) {
                backgroundRect.setWidth(width);
                backgroundRect.setHeight(height);
            }

            // Resize header
            if (headerPane != null) {
                headerPane.setPrefWidth(width - 40);
                headerPane.setLayoutX(20);
                if (headerRect != null) {
                    headerRect.setWidth(width - 40 + 30);
                }
            }

            // Calculate main form dimensions
            double horizontalGap = width * 0.05;
            double mainFormWidth = width - 2 * horizontalGap;
            double mainFormHeight = height - 30 - horizontalGap;

            if (mainFormPane != null) {
                mainFormPane.setPrefWidth(mainFormWidth);
                mainFormPane.setPrefHeight(mainFormHeight - 30);
                mainFormPane.setLayoutX(horizontalGap);

                // Adjust gap between header and main form
                mainFormPane.setLayoutY(96 + (height - 600) * 0.05);
            }

            // Adjust elements in form
            double columnWidth = (mainFormWidth - 100) / 2;

            if (personalInfoVBox != null) {
                // Adjust spacing based on height ratio
                personalInfoVBox.setSpacing(8 * heightRatio);
                personalInfoVBox.setLayoutY(80 * heightRatio);
                personalInfoVBox.setPrefWidth(columnWidth);

                // Adjust sizes of elements in personalInfoVBox
                adjustPersonalInfoFields(columnWidth, heightRatio);
            }

            if (accountInfoVBox != null) {
                // Adjust spacing based on height ratio
                accountInfoVBox.setSpacing(8 * heightRatio);
                accountInfoVBox.setLayoutY(80 * heightRatio);
                accountInfoVBox.setPrefWidth(columnWidth);
                accountInfoVBox.setLayoutX(columnWidth + 50);
            }

            // Adjust title
            if (registerLabel != null) {
                double labelWidth = registerLabel.prefWidth(-1);
                registerLabel.setLayoutX((mainFormWidth - labelWidth) / 2);
                registerLabel.setLayoutY(14 * heightRatio);
            }

            // Adjust gender box
            HBox genderBox = (HBox) mainFormPane.lookup("#genderBox");
            if (genderBox != null) {
                genderBox.setLayoutY(261 * heightRatio);
            }

            // Adjust address combo boxes and fields
            if (provinceCombo != null) {
                provinceCombo.setLayoutY(290 * heightRatio);
                provinceCombo.setPrefHeight(26 * heightRatio);
            }

            if (districtCombo != null) {
                districtCombo.setLayoutY(336 * heightRatio);
                districtCombo.setPrefHeight(26 * heightRatio);
            }

            if (wardCombo != null) {
                wardCombo.setLayoutY(384 * heightRatio);
                wardCombo.setPrefHeight(26 * heightRatio);
            }

            if (specificAddressField != null) {
                specificAddressField.setLayoutY(426 * heightRatio);
                specificAddressField.setPrefHeight(26 * heightRatio);
            }

            // Set consistent width for all input fields
            double fieldWidth = columnWidth * 0.9;

            double actualUsernameWidth = fieldWidth - 34;
            double actualPasswordWidth = fieldWidth + 34;

            // Adjust containers and fields
            adjustInputField(usernameField, fieldWidth, actualUsernameWidth, heightRatio);
            adjustPasswordField(passwordField, visiblePasswordField, fieldWidth, actualPasswordWidth, heightRatio);
            adjustPasswordField(confirmPasswordField, visibleConfirmPasswordField, fieldWidth, actualPasswordWidth, heightRatio);

            // Adjust error labels
            adjustErrorLabel(usernameErrorLabel, heightRatio);
            adjustErrorLabel(passwordErrorLabel, heightRatio);
            adjustErrorLabel(confirmPasswordErrorLabel, heightRatio);
            adjustErrorLabel(phoneErrorLabel, heightRatio);
            adjustErrorLabel(emailErrorLabel, heightRatio);

            // Adjust register button
            if (registerButton != null) {
                HBox parent = (HBox) registerButton.getParent();
                if (parent != null) {
                    parent.setPrefWidth(columnWidth);
                    parent.setSpacing(10 * heightRatio);

                    registerButton.setPrefWidth(columnWidth * 0.7);
                    registerButton.setPrefHeight(40 * heightRatio);

                    // Định nghĩa các gradient cho nút đăng ký
                    final String registerGradient = "linear-gradient(to right, #865DFF, #5CB8E4)";
                    final String registerHoverGradient = "linear-gradient(to right, #5CB8E4, #865DFF)"; // Đảo ngược màu gradient

                    // Style cơ bản cho nút đăng ký
                    registerButton.setStyle("-fx-background-radius: 20; -fx-font-size: " + (18 * heightRatio) +
                            "px; -fx-background-color: " + registerGradient + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

                    // Thêm event handler cho hiệu ứng hover
                    registerButton.setOnMouseEntered(mouseEvent -> {
                        registerButton.setStyle("-fx-background-radius: 20; -fx-font-size: " + (18 * heightRatio) +
                                "px; -fx-background-color: " + registerHoverGradient + "; -fx-text-fill: white; -fx-font-weight: bold; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0); -fx-cursor: hand;");
                    });

                    registerButton.setOnMouseExited(mouseEvent -> {
                        registerButton.setStyle("-fx-background-radius: 20; -fx-font-size: " + (18 * heightRatio) +
                                "px; -fx-background-color: " + registerGradient + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                    });
                }
            }

            // Adjust reset button - Thêm hiệu ứng hover chuyển màu tím đậm
            if (resetButton != null) {
                // Màu cơ bản và màu hover cho nút Reset
                final String resetButtonColor = "#47B5FF";
                final String resetButtonHoverColor = "#6A11CB"; // Màu tím đậm khi hover

                // Style cơ bản cho nút Reset
                resetButton.setStyle(
                        "-fx-background-color: " + resetButtonColor + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 15; " +
                                "-fx-font-size: " + (14 * heightRatio) + "px; " +
                                "-fx-cursor: hand;"
                );

                // Hiệu ứng hover: đổi màu tím đậm và thêm bóng đổ
                resetButton.setOnMouseEntered(mouseEvent -> {
                    resetButton.setStyle(
                            "-fx-background-color: " + resetButtonHoverColor + "; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-background-radius: 15; " +
                                    "-fx-font-size: " + (14 * heightRatio) + "px; " +
                                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 0); " +
                                    "-fx-cursor: hand;"
                    );
                });

                // Trở về trạng thái bình thường khi không hover
                resetButton.setOnMouseExited(mouseEvent -> {
                    resetButton.setStyle(
                            "-fx-background-color: " + resetButtonColor + "; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-background-radius: 15; " +
                                    "-fx-font-size: " + (14 * heightRatio) + "px; " +
                                    "-fx-cursor: hand;"
                    );
                });

                // Điều chỉnh kích thước nút nếu cần
                resetButton.setPrefHeight(30 * heightRatio);
                resetButton.setPrefWidth(80 * heightRatio);
            }

            // Adjust back to login button - Thêm biểu tượng và giữ nguyên vị trí
            if (backToLoginButton != null) {
                // Dùng giá trị từ FXML nếu có sẵn, hoặc tính toán dựa trên mainFormWidth/Height
                double buttonHeight = 35 * heightRatio;
                double buttonWidth = 200 * heightRatio;

                // Giữ nguyên vị trí ở góc phải dưới theo AnchorPane constraints
                double rightAnchor = 14.6; // Lấy từ FXML
                double bottomAnchor = 14.0; // Lấy từ FXML

                backToLoginButton.setLayoutX(mainFormWidth - buttonWidth - rightAnchor);
                backToLoginButton.setLayoutY(mainFormHeight - buttonHeight - bottomAnchor);
                backToLoginButton.setPrefWidth(buttonWidth);
                backToLoginButton.setPrefHeight(buttonHeight);

                // Định nghĩa màu cho nút
                final String backButtonColor = "#5D87FF";
                final String backButtonHoverColor = "#3D67DF"; // Màu đậm hơn khi hover

                try {
                    // Lưu lại text hiện tại của nút
                    String buttonText = backToLoginButton.getText();

                    // Tạo ImageView cho biểu tượng logout
                    ImageView logoutIcon = new ImageView();
                    Image image = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/logout_icon.png"));
                    logoutIcon.setImage(image);
                    logoutIcon.setFitHeight(18 * heightRatio);
                    logoutIcon.setFitWidth(18 * heightRatio);

                    // Giữ nguyên text và thêm biểu tượng ở trước text
                    backToLoginButton.setGraphic(logoutIcon);
                    backToLoginButton.setGraphicTextGap(10); // Khoảng cách giữa icon và text

                    // Style cơ bản
                    backToLoginButton.setStyle(
                            "-fx-background-radius: 15; " +
                                    "-fx-font-size: " + (14 * heightRatio) + "px; " +
                                    "-fx-background-color: " + backButtonColor + "; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-content-display: left;"
                    );

                    // Hiệu ứng hover: đổi màu nền và phóng to biểu tượng
                    backToLoginButton.setOnMouseEntered(mouseEvent -> {
                        backToLoginButton.setStyle(
                                "-fx-background-radius: 15; " +
                                        "-fx-font-size: " + (14 * heightRatio) + "px; " +
                                        "-fx-background-color: " + backButtonHoverColor + "; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 0); " +
                                        "-fx-cursor: hand; " +
                                        "-fx-content-display: left;"
                        );

                        // Phóng to icon khi hover
                        logoutIcon.setScaleX(1.2);
                        logoutIcon.setScaleY(1.2);
                    });

                    backToLoginButton.setOnMouseExited(mouseEvent -> {
                        backToLoginButton.setStyle(
                                "-fx-background-radius: 15; " +
                                        "-fx-font-size: " + (14 * heightRatio) + "px; " +
                                        "-fx-background-color: " + backButtonColor + "; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-content-display: left;"
                        );

                        // Trả về kích thước bình thường
                        logoutIcon.setScaleX(1.0);
                        logoutIcon.setScaleY(1.0);
                    });

                } catch (Exception ex) {
                    System.err.println("Không thể tải hình ảnh logout_icon.png: " + ex.getMessage());
                    ex.printStackTrace();

                    // Fallback: Nếu không tìm thấy ảnh, vẫn hiển thị nút bình thường
                    backToLoginButton.setStyle(
                            "-fx-background-radius: 15; " +
                                    "-fx-font-size: " + (14 * heightRatio) + "px; " +
                                    "-fx-background-color: " + backButtonColor + "; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-cursor: hand;"
                    );

                    // Hiệu ứng hover đơn giản nếu không có icon
                    backToLoginButton.setOnMouseEntered(mouseEvent -> {
                        backToLoginButton.setStyle(
                                "-fx-background-radius: 15; " +
                                        "-fx-font-size: " + (14 * heightRatio) + "px; " +
                                        "-fx-background-color: " + backButtonHoverColor + "; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 0); " +
                                        "-fx-cursor: hand;"
                        );
                    });

                    backToLoginButton.setOnMouseExited(mouseEvent -> {
                        backToLoginButton.setStyle(
                                "-fx-background-radius: 15; " +
                                        "-fx-font-size: " + (14 * heightRatio) + "px; " +
                                        "-fx-background-color: " + backButtonColor + "; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-cursor: hand;"
                        );
                    });
                }
            }

            // Thêm hiệu ứng cursor:hand cho các nút eye button
            if (eyeButton != null) {
                eyeButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                // Thêm hiệu ứng hover cho eyeButton
                eyeButton.setOnMouseEntered(mouseEvent -> {
                    eyeIconView.setScaleX(1.2);
                    eyeIconView.setScaleY(1.2);
                });

                eyeButton.setOnMouseExited(mouseEvent -> {
                    eyeIconView.setScaleX(1.0);
                    eyeIconView.setScaleY(1.0);
                });
            }

            if (confirmEyeButton != null) {
                confirmEyeButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                // Thêm hiệu ứng hover cho confirmEyeButton
                confirmEyeButton.setOnMouseEntered(mouseEvent -> {
                    confirmEyeIconView.setScaleX(1.2);
                    confirmEyeIconView.setScaleY(1.2);
                });

                confirmEyeButton.setOnMouseExited(mouseEvent -> {
                    confirmEyeIconView.setScaleX(1.0);
                    confirmEyeIconView.setScaleY(1.0);
                });
            }

        } catch (Exception ex) {
            System.err.println("Error updating layout: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void adjustPersonalInfoFields(double columnWidth, double heightRatio) {
        // Calculate standard width for ALL left-side input fields
        double fullWidth = columnWidth * 0.95;

        // Adjust first name and last name fields - two fields in one row
        if (firstNameField != null && lastNameField != null) {
            double fieldWidth = (fullWidth - 10) / 2;
            firstNameField.setPrefWidth(fieldWidth);
            lastNameField.setPrefWidth(fieldWidth);
            firstNameField.setMaxWidth(fieldWidth);
            lastNameField.setMaxWidth(fieldWidth);

            firstNameField.setPrefHeight(26 * heightRatio);
            lastNameField.setPrefHeight(26 * heightRatio);
        }

        // Adjust single fields (phone, email, address)
        if (numberField != null) {
            numberField.setPrefWidth(fullWidth);
            numberField.setMaxWidth(fullWidth);
            numberField.setPrefHeight(26 * heightRatio);
        }
        
        if (emailField != null) {
            emailField.setPrefWidth(fullWidth);
            emailField.setMaxWidth(fullWidth);
            emailField.setPrefHeight(26 * heightRatio);
        }
        
        if (specificAddressField != null) {
            specificAddressField.setPrefWidth(fullWidth);
            specificAddressField.setMaxWidth(fullWidth);
            specificAddressField.setPrefHeight(26 * heightRatio);
        }

        // Adjust date combo boxes
        if (dayCombo != null && monthCombo != null && yearCombo != null) {
            double totalWidth = fullWidth;
            dayCombo.setPrefWidth(totalWidth * 0.3);
            monthCombo.setPrefWidth(totalWidth * 0.3);
            yearCombo.setPrefWidth(totalWidth * 0.37);

            dayCombo.setMaxWidth(totalWidth * 0.3);
            monthCombo.setMaxWidth(totalWidth * 0.3);
            yearCombo.setMaxWidth(totalWidth * 0.34);

            dayCombo.setPrefHeight(26 * heightRatio);
            monthCombo.setPrefHeight(26 * heightRatio);
            yearCombo.setPrefHeight(26 * heightRatio);
        }

        // Adjust address combo boxes
        if (provinceCombo != null) {
            provinceCombo.setPrefWidth(fullWidth);
            provinceCombo.setMaxWidth(fullWidth);
            provinceCombo.setPrefHeight(26 * heightRatio);
        }
        
        if (districtCombo != null) {
            districtCombo.setPrefWidth(fullWidth);
            districtCombo.setMaxWidth(fullWidth);
            districtCombo.setPrefHeight(26 * heightRatio);
        }
        
        if (wardCombo != null) {
            wardCombo.setPrefWidth(fullWidth);
            wardCombo.setMaxWidth(fullWidth);
            wardCombo.setPrefHeight(26 * heightRatio);
        }
    }

    private void adjustErrorLabel(Label label, double heightRatio) {
        if (label != null) {
            label.setStyle("-fx-text-fill: red; -fx-font-size: " + (10 * heightRatio) + "px; -fx-padding: " + (-5 * heightRatio) + " 0 0 0;");
        }
    }

    private void adjustInputField(TextField field, double containerWidth, double fieldWidth, double heightRatio) {
        if (field != null) {
            HBox parent = (HBox) field.getParent();
            if (parent != null) {
                parent.setPrefWidth(containerWidth);
                field.setPrefWidth(fieldWidth);
                field.setMaxWidth(fieldWidth);
                field.setPrefHeight(26 * heightRatio);
                HBox.setHgrow(field, Priority.NEVER);
            }
        }
    }

    private void adjustPasswordField(PasswordField passwordField, TextField visibleField,
                                     double containerWidth, double fieldWidth, double heightRatio) {
        if (passwordField != null && visibleField != null) {
            HBox parent = (HBox) passwordField.getParent();
            if (parent != null) {
                parent.setPrefWidth(containerWidth);
                passwordField.setPrefWidth(fieldWidth);
                passwordField.setMaxWidth(fieldWidth);
                passwordField.setPrefHeight(30 * heightRatio);

                visibleField.setPrefWidth(fieldWidth);
                visibleField.setMaxWidth(fieldWidth);
                visibleField.setPrefHeight(30 * heightRatio);

                HBox.setHgrow(passwordField, Priority.NEVER);
                HBox.setHgrow(visibleField, Priority.NEVER);
            }
        }
    }

    private void adjustFieldContainer(TextField field, double containerWidth, double offset) {
        if (field != null) {
            HBox parent = (HBox) field.getParent();
            if (parent != null) {
                parent.setPrefWidth(containerWidth);
                field.setPrefWidth(containerWidth - offset);
                field.setMaxWidth(containerWidth - offset);

                HBox.setHgrow(field, Priority.NEVER);
            }
        }
    }
// Thêm các biến để theo dõi loại lỗi
private boolean isEmailDuplicate = false;
private boolean isPhoneDuplicate = false;
private boolean isUsernameDuplicate = false;

private void setupValidation() {
    // Các biến validation
    isPhoneValid = new SimpleBooleanProperty(false);
    isEmailValid = new SimpleBooleanProperty(false);
    isPasswordValid = new SimpleBooleanProperty(false);
    isPasswordMatching = new SimpleBooleanProperty(false);
    isUsernameValid = new SimpleBooleanProperty(false);

    // Validate phone number
    if (numberField != null) {
    numberField.textProperty().addListener((obs, oldVal, newVal) -> {
        // Allow only digits
        if (!newVal.matches("\\d*")) {
            numberField.setText(newVal.replaceAll("[^\\d]", ""));
            return;
        }

        // Nếu chuỗi rỗng, không hiển thị lỗi
        if (newVal.isEmpty()) {
            phoneErrorLabel.setText("");
            isPhoneValid.set(false);
            isPhoneDuplicate = false;
            return;
        }
        
        // Kiểm tra 2 chữ số đầu tiên
        if (newVal.length() >= 1) {
            // Với số đầu tiên, nếu không phải 0 thì báo lỗi ngay
            if (newVal.length() == 1 && !newVal.startsWith("0")) {
                phoneErrorLabel.setText(isVietnamese ? 
                    "Số điện thoại phải bắt đầu bằng số 0" : 
                    "Phone number must start with 0");
                isPhoneValid.set(false);
                isPhoneDuplicate = false;
                return;
            }
            
            // Với 2 số đầu tiên, kiểm tra prefix
            if (newVal.length() >= 2) {
                String prefix = newVal.substring(0, 2);
                if (!prefix.equals("03") && !prefix.equals("07") && !prefix.equals("09")) {
                    phoneErrorLabel.setText(isVietnamese ?
                            "Số điện thoại phải bắt đầu bằng 03, 07 hoặc 09" :
                            "Phone must start with 03, 07 or 09");
                    isPhoneValid.set(false);
                    isPhoneDuplicate = false;
                    return;
                }
                
                // Nếu prefix hợp lệ và độ dài < 10, mới thông báo về độ dài
                if (newVal.length() < 10) {
                    phoneErrorLabel.setText(isVietnamese ?
                            "Vui lòng nhập đủ 10-12 số" :
                            "Please enter 10-12 digits");
                    isPhoneValid.set(false);
                    isPhoneDuplicate = false;
                    return;
                }
            }
        }

        // Kiểm tra độ dài: 10-12 chữ số
        if (newVal.length() > 12) {
            phoneErrorLabel.setText(isVietnamese ?
                    "Số điện thoại không được vượt quá 12 số" :
                    "Phone number should not exceed 12 digits");
            isPhoneValid.set(false);
            isPhoneDuplicate = false;
            return;
        }

        // Nếu qua tất cả các kiểm tra và độ dài từ 10-12, thì số điện thoại hợp lệ
        if (newVal.length() >= 10 && newVal.length() <= 12) {
            // Full validation
            boolean isValid = isValidPhoneNumber(newVal);
            
            if (isValid) {
                // Kiểm tra trong DB khi định dạng hợp lệ
                new Thread(() -> {
                    boolean exists = authService.isPhoneExists(newVal);
                    Platform.runLater(() -> {
                        if (exists) {
                            phoneErrorLabel.setText(isVietnamese ?
                                    "Số điện thoại đã được sử dụng. Vui lòng dùng số khác." :
                                    "Phone number already in use. Please use another one.");
                            isPhoneValid.set(false);
                            isPhoneDuplicate = true;
                        } else {
                            phoneErrorLabel.setText("");
                            isPhoneValid.set(true);
                            isPhoneDuplicate = false;
                        }
                    });
                }).start();
            } else {
                phoneErrorLabel.setText(isVietnamese ?
                        "Số điện thoại không hợp lệ" :
                        "Invalid phone number format");
                isPhoneValid.set(false);
                isPhoneDuplicate = false;
            }
        }
    });
        
        // Kiểm tra lần cuối khi field mất focus
        numberField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !numberField.getText().isEmpty() && isValidPhoneNumber(numberField.getText())) {
                // Mất focus và định dạng hợp lệ -> kiểm tra trùng lặp
                String phone = numberField.getText();
                new Thread(() -> {
                    boolean exists = authService.isPhoneExists(phone);
                    Platform.runLater(() -> {
                        if (exists) {
                            phoneErrorLabel.setText(isVietnamese ?
                                    "Số điện thoại đã được sử dụng. Vui lòng dùng số khác." :
                                    "Phone number already in use. Please use another one.");
                            isPhoneValid.set(false);
                            isPhoneDuplicate = true;
                        } else {
                            phoneErrorLabel.setText("");
                            isPhoneValid.set(true);
                            isPhoneDuplicate = false;
                        }
                    });
                }).start();
            }
        });
    }

    // Validate email
    if (emailField != null) {
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = isValidEmail(newVal);
            
            if (!isValid && !newVal.isEmpty()) {
                if (emailErrorLabel != null) {
                    emailErrorLabel.setText(isVietnamese ?
                            "Email không hợp lệ" : "Invalid email format");
                }
                isEmailValid.set(false);
                isEmailDuplicate = false; // Reset trạng thái trùng lặp
            } else if (isValid && !newVal.isEmpty()) {
                // Chỉ kiểm tra trong DB khi định dạng hợp lệ và không rỗng
                new Thread(() -> {
                    boolean exists = authService.isEmailExists(newVal);
                    Platform.runLater(() -> {
                        if (exists) {
                            emailErrorLabel.setText(isVietnamese ?
                                    "Email đã được sử dụng. Vui lòng dùng email khác." :
                                    "Email already in use. Please use another one.");
                            isEmailValid.set(false);
                            isEmailDuplicate = true; // Đánh dấu là trùng lặp
                        } else {
                            emailErrorLabel.setText("");
                            isEmailValid.set(true);
                            isEmailDuplicate = false;
                        }
                    });
                }).start();
            } else {
                if (emailErrorLabel != null) {
                    emailErrorLabel.setText("");
                }
                isEmailValid.set(newVal.isEmpty() ? false : true);
                isEmailDuplicate = false;
            }
        });
        
        // Kiểm tra lần cuối khi field mất focus
        emailField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !emailField.getText().isEmpty() && isValidEmail(emailField.getText())) {
                // Mất focus và định dạng hợp lệ -> kiểm tra trùng lặp
                String email = emailField.getText();
                new Thread(() -> {
                    boolean exists = authService.isEmailExists(email);
                    Platform.runLater(() -> {
                        if (exists) {
                            emailErrorLabel.setText(isVietnamese ?
                                    "Email đã được sử dụng. Vui lòng dùng email khác." :
                                    "Email already in use. Please use another one.");
                            isEmailValid.set(false);
                            isEmailDuplicate = true;
                        } else {
                            emailErrorLabel.setText("");
                            isEmailValid.set(true);
                            isEmailDuplicate = false;
                        }
                    });
                }).start();
            }
        });
    }

    // THÊM PHẦN KIỂM TRA USERNAME TRÙNG LẶP
    if (usernameField != null) {
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Xác thực độ dài username
            if (newVal.length() < 4 && !newVal.isEmpty()) {
                usernameErrorLabel.setText(isVietnamese ? 
                        "Tên đăng nhập phải có ít nhất 4 ký tự" :
                        "Username must be at least 4 characters");
                isUsernameValid.set(false);
                isUsernameDuplicate = false;
            } else if (newVal.length() >= 4) {
                // Kiểm tra username trùng lặp
                new Thread(() -> {
                    boolean exists = authService.isUsernameExists(newVal);
                    Platform.runLater(() -> {
                        if (exists) {
                            usernameErrorLabel.setText(isVietnamese ?
                                    "Tên đăng nhập đã được sử dụng. Vui lòng chọn tên khác." :
                                    "Username already in use. Please choose another one.");
                            isUsernameValid.set(false);
                            isUsernameDuplicate = true;
                        } else {
                            usernameErrorLabel.setText("");
                            isUsernameValid.set(true);
                            isUsernameDuplicate = false;
                        }
                    });
                }).start();
            } else {
                usernameErrorLabel.setText("");
                isUsernameValid.set(false);
                isUsernameDuplicate = false;
            }
        });
        
        // Kiểm tra lần cuối khi field mất focus
        usernameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && usernameField.getText().length() >= 4) {
                String username = usernameField.getText();
                new Thread(() -> {
                    boolean exists = authService.isUsernameExists(username);
                    Platform.runLater(() -> {
                        if (exists) {
                            usernameErrorLabel.setText(isVietnamese ?
                                    "Tên đăng nhập đã được sử dụng. Vui lòng chọn tên khác." :
                                    "Username already in use. Please choose another one.");
                            isUsernameValid.set(false);
                            isUsernameDuplicate = true;
                        } else {
                            usernameErrorLabel.setText("");
                            isUsernameValid.set(true);
                            isUsernameDuplicate = false;
                        }
                    });
                }).start();
            }
        });
    }

    // Validate password
    if (passwordField != null && visiblePasswordField != null) {
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            checkPassword(newVal);
            checkPasswordsMatch();
        });
    }

    if (visiblePasswordField != null) {
        visiblePasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (passwordVisible) {
                passwordField.setText(newVal);
                checkPassword(newVal);
                checkPasswordsMatch();
            }
        });
    }

    // Validate confirm password
    if (confirmPasswordField != null) {
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            checkPasswordsMatch();
        });
    }

    if (visibleConfirmPasswordField != null) {
        visibleConfirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (confirmPasswordVisible) {
                confirmPasswordField.setText(newVal);
                checkPasswordsMatch();
            }
        });
    }

    // Sync between hidden and visible password fields
    if (passwordField != null && visiblePasswordField != null) {
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!passwordVisible) visiblePasswordField.setText(newVal);
        });
    }

    if (confirmPasswordField != null && visibleConfirmPasswordField != null) {
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!confirmPasswordVisible) visibleConfirmPasswordField.setText(newVal);
        });
    }
}

private void updateLanguage() {
    isVietnamese = LanguageManager.isVietnamese();
    
    // Cập nhật tiêu đề và nhãn chính
    if (logoLabel != null) logoLabel.setText("CELLCOMP");
    if (storeLabel != null) storeLabel.setText("STORE");
    if (createAccountLabel != null) createAccountLabel.setText(isVietnamese ? "TẠO TÀI KHOẢN" : "CREATE ACCOUNT");
    if (registerLabel != null) registerLabel.setText(isVietnamese ? "THÔNG TIN TÀI KHOẢN" : "ACCOUNT INFORMATION");
    
    // Cập nhật placeholder cho các trường nhập liệu
    updateFieldPlaceholders();
    
    // Cập nhật text cho các nút
    if (registerButton != null) registerButton.setText(isVietnamese ? "Đăng ký" : "Register");
    if (backToLoginButton != null) backToLoginButton.setText(isVietnamese ? "Quay về đăng nhập" : "Back to Login");
    if (resetButton != null) resetButton.setText(isVietnamese ? "Đặt lại" : "Reset");
    if (langButton != null) langButton.setText(isVietnamese ? "Tiếng Việt" : "English");
    
    // Cập nhật text cho các radio buttons
    if (maleRadio != null) maleRadio.setText(isVietnamese ? "Nam" : "Male");
    if (femaleRadio != null) femaleRadio.setText(isVietnamese ? "Nữ" : "Female");
    if (otherRadio != null) otherRadio.setText(isVietnamese ? "Khác" : "Other");
    
    // Cập nhật các nhãn trong form
    updateFieldLabels();
    
    // Cập nhật thông báo lỗi số điện thoại
    if (numberField != null && phoneErrorLabel != null && !numberField.getText().isEmpty()) {
        if (isPhoneDuplicate) {
            phoneErrorLabel.setText(isVietnamese ?
                    "Số điện thoại đã được sử dụng. Vui lòng dùng số khác." :
                    "Phone number already in use. Please use another one.");
        } else if (!isPhoneValid.get() && numberField.getText().length() >= 2) {
            // Kiểm tra prefix và hiển thị lỗi định dạng tương ứng
            String prefix = numberField.getText().substring(0, 2);
            if (!prefix.equals("03") && !prefix.equals("07") && !prefix.equals("08") && !prefix.equals("09")) {
                phoneErrorLabel.setText(isVietnamese ?
                        "Số điện thoại phải bắt đầu bằng 03, 07, 08 hoặc 09" :
                        "Phone must start with 03, 07, 08, or 09");
            } else if (numberField.getText().length() < 10) {
                phoneErrorLabel.setText(isVietnamese ?
                        "Vui lòng nhập đủ 10-12 số" :
                        "Please enter 10-12 digits");
            } else if (numberField.getText().length() > 12) {
                phoneErrorLabel.setText(isVietnamese ?
                        "Số điện thoại không được vượt quá 12 số" :
                        "Phone number should not exceed 12 digits");
            } else {
                phoneErrorLabel.setText(isVietnamese ?
                        "Số điện thoại không hợp lệ" :
                        "Invalid phone number format");
            }
        }
    }
    
    // Cập nhật thông báo lỗi email
    if (emailField != null && emailErrorLabel != null && !emailField.getText().isEmpty()) {
        if (isEmailDuplicate) {
            emailErrorLabel.setText(isVietnamese ?
                    "Email đã được sử dụng. Vui lòng dùng email khác." :
                    "Email already in use. Please use another one.");
        } else if (!isEmailValid.get()) {
            emailErrorLabel.setText(isVietnamese ?
                    "Email không hợp lệ" :
                    "Invalid email format");
        }
    }
    
    // Cập nhật thông báo lỗi username
    if (usernameField != null && usernameErrorLabel != null && !usernameField.getText().isEmpty()) {
        if (isUsernameDuplicate) {
            usernameErrorLabel.setText(isVietnamese ?
                    "Tên đăng nhập đã được sử dụng. Vui lòng chọn tên khác." :
                    "Username already in use. Please choose another one.");
        } else if (usernameField.getText().length() < 4) {
            usernameErrorLabel.setText(isVietnamese ?
                    "Tên đăng nhập phải có ít nhất 4 ký tự" :
                    "Username must be at least 4 characters");
        }
    }
    
    // Cập nhật thông báo lỗi mật khẩu
    if (passwordField != null && !passwordField.getText().isEmpty() && 
            passwordErrorLabel != null && !isPasswordValid.get()) {
        passwordErrorLabel.setText(isVietnamese ?
                "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ và số" :
                "Password must be at least 8 characters with letters and numbers");
    }
    
    // Cập nhật thông báo lỗi xác nhận mật khẩu
    if (confirmPasswordField != null && !confirmPasswordField.getText().isEmpty() && 
            confirmPasswordErrorLabel != null && !isPasswordMatching.get()) {
        confirmPasswordErrorLabel.setText(isVietnamese ?
                "Mật khẩu không khớp" :
                "Passwords do not match");
    }
    
    // Cập nhật danh sách tháng
    updateMonthsLanguage();
}

// Phương thức mới để cập nhật placeholder
private void updateFieldPlaceholders() {
    // Các dòng code hiện tại
    if (firstNameField != null) firstNameField.setPromptText(isVietnamese ? "Tên" : "First name");
    if (lastNameField != null) lastNameField.setPromptText(isVietnamese ? "Họ" : "Last name");
    if (numberField != null) numberField.setPromptText(isVietnamese ? "Số điện thoại" : "Phone number");
    if (emailField != null) emailField.setPromptText(isVietnamese ? "Email" : "Email");
    if (specificAddressField != null) specificAddressField.setPromptText(isVietnamese ? "Địa chỉ cụ thể" : "Specific address");
    if (usernameField != null) usernameField.setPromptText(isVietnamese ? "Tên đăng nhập" : "Username");
    if (passwordField != null) passwordField.setPromptText(isVietnamese ? "Mật khẩu" : "Password");
    if (visiblePasswordField != null) visiblePasswordField.setPromptText(isVietnamese ? "Mật khẩu" : "Password");
    if (confirmPasswordField != null) confirmPasswordField.setPromptText(isVietnamese ? "Xác nhận mật khẩu" : "Confirm password");
    if (visibleConfirmPasswordField != null) visibleConfirmPasswordField.setPromptText(isVietnamese ? "Xác nhận mật khẩu" : "Confirm password");
    
    // Thêm mới: Cập nhật promptText cho các comboBox địa chỉ
    if (provinceCombo != null) provinceCombo.setPromptText(isVietnamese ? "Tỉnh/Thành phố" : "Province/City");
    if (districtCombo != null) districtCombo.setPromptText(isVietnamese ? "Quận/Huyện" : "District");
    if (wardCombo != null) wardCombo.setPromptText(isVietnamese ? "Xã/Phường" : "Ward");
    
    // Cập nhật ngày/tháng/năm
    if (dayCombo != null) dayCombo.setPromptText(isVietnamese ? "Ngày" : "Day");
    if (monthCombo != null) monthCombo.setPromptText(isVietnamese ? "Tháng" : "Month");  
    if (yearCombo != null) yearCombo.setPromptText(isVietnamese ? "Năm" : "Year");
}

// Phương thức mới để tìm và cập nhật các nhãn
private void updateFieldLabels() {
    updateLabelIfExists("firstNameLabel", isVietnamese ? "Tên:" : "First Name:");
    updateLabelIfExists("lastNameLabel", isVietnamese ? "Họ:" : "Last Name:");
    updateLabelIfExists("phoneLabel", isVietnamese ? "Số điện thoại:" : "Phone Number:");
    updateLabelIfExists("emailLabel", isVietnamese ? "Email:" : "Email:");
    updateLabelIfExists("birthDateLabel", isVietnamese ? "Ngày sinh:" : "Birth Date:");
    updateLabelIfExists("genderLabel", isVietnamese ? "Giới tính:" : "Gender:");
    updateLabelIfExists("provinceLabel", isVietnamese ? "Tỉnh/Thành phố:" : "Province/City:");
    updateLabelIfExists("districtLabel", isVietnamese ? "Quận/Huyện:" : "District:");
    updateLabelIfExists("wardLabel", isVietnamese ? "Xã/Phường:" : "Ward:");
    updateLabelIfExists("addressLabel", isVietnamese ? "Địa chỉ cụ thể:" : "Specific Address:");
    updateLabelIfExists("usernameLabel", isVietnamese ? "Tên đăng nhập:" : "Username:");
    updateLabelIfExists("passwordLabel", isVietnamese ? "Mật khẩu:" : "Password:");
    updateLabelIfExists("confirmPasswordLabel", isVietnamese ? "Xác nhận mật khẩu:" : "Confirm Password:");
}

// Phương thức helper để tìm và cập nhật label theo ID
private void updateLabelIfExists(String labelId, String text) {
    Label label = (Label) mainFormPane.lookup("#" + labelId);
    if (label != null) {
        label.setText(text);
    }
}

    private void checkPassword(String password) {
        boolean isValid = isValidPassword(password);
        isPasswordValid.set(isValid);

        if (passwordErrorLabel == null) return;

        if (!password.isEmpty()) {
            if (password.length() < 8) {
                passwordErrorLabel.setText(isVietnamese ?
                        "Mật khẩu phải có ít nhất 8 ký tự" :
                        "Password must have at least 8 characters");
            } else if (!password.matches(".*[A-Z].*")) {
                passwordErrorLabel.setText(isVietnamese ?
                        "Mật khẩu phải có ít nhất 1 chữ hoa" :
                        "Password must have at least 1 uppercase letter");
            } else if (!password.matches(".*[0-9].*")) {
                passwordErrorLabel.setText(isVietnamese ?
                        "Mật khẩu phải có ít nhất 1 số" :
                        "Password must have at least 1 digit");
            } else if (!password.matches(".*[^a-zA-Z0-9].*")) {
                passwordErrorLabel.setText(isVietnamese ?
                        "Mật khẩu phải có ít nhất 1 ký tự đặc biệt" :
                        "Password must have at least 1 special character");
            } else {
                passwordErrorLabel.setText("");
            }
        } else {
            passwordErrorLabel.setText("");
        }
    }

    private void checkPasswordsMatch() {
        if (passwordField == null || confirmPasswordField == null ||
                confirmPasswordErrorLabel == null) return;

        String password = passwordVisible ? visiblePasswordField.getText() : passwordField.getText();
        String confirmPassword = confirmPasswordVisible ? visibleConfirmPasswordField.getText() : confirmPasswordField.getText();

        boolean matching = !password.isEmpty() && password.equals(confirmPassword);
        isPasswordMatching.set(matching);

        if (!confirmPassword.isEmpty() && !matching) {
            confirmPasswordErrorLabel.setText(isVietnamese ?
                    "Mật khẩu không khớp" : "Passwords do not match");
        } else {
            confirmPasswordErrorLabel.setText("");
        }
    }

    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) return false;

        // Check phone number with 10-12 digits starting with 03, 07, or 09
        return phone.matches("^(03|07|09)\\d{8,10}$");
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;

        // Email must match %@%.% format
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.isEmpty() || password.length() < 8) return false;

        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[^a-zA-Z0-9].*");

        return hasUppercase && hasDigit && hasSpecial;
    }

    private void initDateCombos() {
        // Birth years from 100 years ago to current
        int currentYear = LocalDate.now().getYear();
        List<Integer> years = new ArrayList<>();
        for (int year = currentYear; year >= currentYear - 100; year--) {
            years.add(year);
        }
        yearCombo.setItems(FXCollections.observableArrayList(years));

        // Update month list based on language
        updateMonthsLanguage();

        // Update days based on selected month and year
        yearCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateDays());
        monthCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateDays());

        // Default to 31 days
        updateDays();
    }

    private void updateMonthsLanguage() {
        // Save selected month
        String selectedMonth = monthCombo.getValue();
        int selectedIndex = -1;

        if (selectedMonth != null) {
            if (isVietnamese) {
                selectedIndex = englishMonths.indexOf(selectedMonth);
            } else {
                selectedIndex = vietnameseMonths.indexOf(selectedMonth);
            }
        }

        // Update month list
        if (isVietnamese) {
            monthCombo.setItems(FXCollections.observableArrayList(vietnameseMonths));
        } else {
            monthCombo.setItems(FXCollections.observableArrayList(englishMonths));
        }

        // Restore selected month
        if (selectedIndex >= 0) {
            monthCombo.getSelectionModel().select(selectedIndex);
        }
    }

    private void updateDays() {
        int daysInMonth = 31; // Default

        Integer selectedYear = yearCombo.getValue();
        String selectedMonth = monthCombo.getValue();

        if (selectedYear != null && selectedMonth != null) {
            int monthIndex;

            // Handle both language month formats
            if (isVietnamese) {
                monthIndex = vietnameseMonths.indexOf(selectedMonth) + 1;
            } else {
                monthIndex = englishMonths.indexOf(selectedMonth) + 1;
            }

            switch (monthIndex) {
                case 4:
                case 6:
                case 9:
                case 11:
                    daysInMonth = 30;
                    break;
                case 2:
                    // Check leap year
                    if ((selectedYear % 4 == 0 && selectedYear % 100 != 0) || selectedYear % 400 == 0) {
                        daysInMonth = 29;
                    } else {
                        daysInMonth = 28;
                    }
                    break;
            }
        }

        // Save current day
        Integer currentDay = dayCombo.getValue();

        List<Integer> days = new ArrayList<>();
        for (int day = 1; day <= daysInMonth; day++) {
            days.add(day);
        }
        dayCombo.setItems(FXCollections.observableArrayList(days));

        // Restore selected day if valid
        if (currentDay != null && currentDay <= daysInMonth) {
            dayCombo.setValue(currentDay);
        } else if (days.size() > 0) {
            dayCombo.setValue(1); // Default to day 1
        }
    }

    private void initAddressCombos() {
        // 63 provinces of Vietnam
        List<String> provinces = Arrays.asList(
                "Hà Nội", "TP. Hồ Chí Minh", "Hải Phòng", "Đà Nẵng", "Cần Thơ",
                "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu",
                "Bắc Ninh", "Bến Tre", "Bình Định", "Bình Dương", "Bình Phước",
                "Bình Thuận", "Cà Mau", "Cao Bằng", "Đắk Lắk", "Đắk Nông",
                "Điện Biên", "Đồng Nai", "Đồng Tháp", "Gia Lai", "Hà Giang",
                "Hà Nam", "Hà Tĩnh", "Hải Dương", "Hậu Giang", "Hòa Bình",
                "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu",
                "Lâm Đồng", "Lạng Sơn", "Lào Cai", "Long An", "Nam Định",
                "Nghệ An", "Ninh Bình", "Ninh Thuận", "Phú Thọ", "Phú Yên",
                "Quảng Bình", "Quảng Nam", "Quảng Ngãi", "Quảng Ninh", "Quảng Trị",
                "Sóc Trăng", "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên",
                "Thanh Hóa", "Thừa Thiên Huế", "Tiền Giang", "Trà Vinh", "Tuyên Quang",
                "Vĩnh Long", "Vĩnh Phúc", "Yên Bái"
        );
        provinceCombo.setItems(FXCollections.observableArrayList(provinces));

        // Initialize sample district and ward data
        initSampleDistrictsAndWards();

        // Update district list when province is selected
        provinceCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                List<String> districts = provinceToDistricts.getOrDefault(newVal, new ArrayList<>());
                districtCombo.setItems(FXCollections.observableArrayList(districts));
                districtCombo.getSelectionModel().clearSelection();
                wardCombo.getSelectionModel().clearSelection();
                wardCombo.setItems(FXCollections.observableArrayList());
            }
        });

        // Update ward list when district is selected
        districtCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                List<String> wards = districtToWards.getOrDefault(newVal, new ArrayList<>());
                wardCombo.setItems(FXCollections.observableArrayList(wards));
                wardCombo.getSelectionModel().clearSelection();
            }
        });
    }

    private void initSampleDistrictsAndWards() {
        // Initialize sample data for districts by province
        // Hanoi
        provinceToDistricts.put("Hà Nội", Arrays.asList("Ba Đình", "Hoàn Kiếm", "Tây Hồ", "Long Biên", "Cầu Giấy",
                "Đống Đa", "Hai Bà Trưng", "Hoàng Mai", "Thanh Xuân"));

        // Ho Chi Minh City
        provinceToDistricts.put("TP. Hồ Chí Minh", Arrays.asList("Quận 1", "Quận 3", "Quận 4", "Quận 5", "Quận 6",
                "Quận 7", "Quận 8", "Quận 10", "Quận 11", "Quận 12", "Bình Thạnh", "Phú Nhuận", "Tân Bình"));

        // Da Nang
        provinceToDistricts.put("Đà Nẵng", Arrays.asList("Hải Châu", "Thanh Khê", "Sơn Trà", "Ngũ Hành Sơn", "Liên Chiểu"));

        // Sample ward data
        districtToWards.put("Ba Đình", Arrays.asList("Phúc Xá", "Trúc Bạch", "Vĩnh Phúc", "Cống Vị", "Liễu Giai"));
        districtToWards.put("Hoàn Kiếm", Arrays.asList("Phúc Tân", "Đồng Xuân", "Hàng Mã", "Hàng Buồm", "Hàng Đào"));
        districtToWards.put("Quận 1", Arrays.asList("Bến Nghé", "Bến Thành", "Cầu Kho", "Cầu Ông Lãnh", "Đa Kao"));
        districtToWards.put("Hải Châu", Arrays.asList("Hải Châu I", "Hải Châu II", "Thanh Bình", "Thuận Phước", "Hòa Thuận Đông"));

        // Generate sample wards for remaining districts
        for (String province : provinceToDistricts.keySet()) {
            for (String district : provinceToDistricts.get(province)) {
                if (!districtToWards.containsKey(district)) {
                    List<String> sampleWards = new ArrayList<>();
                    for (int i = 1; i <= 5; i++) {
                        sampleWards.add("Phường/Xã " + i);
                    }
                    districtToWards.put(district, sampleWards);
                }
            }
        }
    }

    private void setupRegisterButtonBinding() {
        // Create binding for form validity
        BooleanBinding formValid = Bindings.createBooleanBinding(
                () -> {
                    boolean basicInfoValid = !firstNameField.getText().trim().isEmpty() &&
                            !lastNameField.getText().trim().isEmpty() &&
                            isPhoneValid.get() &&
                            dayCombo.getValue() != null &&
                            monthCombo.getValue() != null &&
                            yearCombo.getValue() != null &&
                            genderGroup.getSelectedToggle() != null;

                    boolean addressValid = provinceCombo.getValue() != null &&
                            districtCombo.getValue() != null &&
                            wardCombo.getValue() != null &&
                            !specificAddressField.getText().trim().isEmpty();

                    boolean accountValid = isEmailValid.get() &&
                            !usernameField.getText().trim().isEmpty() &&
                            usernameField.getText().length() >= 4 &&
                            isPasswordValid.get() &&
                            isPasswordMatching.get();

                    return basicInfoValid && addressValid && accountValid;
                },
                firstNameField.textProperty(),
                lastNameField.textProperty(),
                isPhoneValid,
                dayCombo.valueProperty(),
                monthCombo.valueProperty(),
                yearCombo.valueProperty(),
                provinceCombo.valueProperty(),
                districtCombo.valueProperty(),
                wardCombo.valueProperty(),
                specificAddressField.textProperty(),
                isEmailValid,
                usernameField.textProperty(),
                isPasswordValid,
                isPasswordMatching
        );

        // Enable register button when form is valid
        registerButton.disableProperty().bind(formValid.not());
    }

    /**
     * Handle avatar upload
     */
    @FXML
    private void handleUploadAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(isVietnamese ? "Chọn ảnh đại diện" : "Choose avatar image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                avatarView.setImage(image);
                selectedAvatarFile = file;
            } catch (Exception e) {
                AlertUtils.showError(
                        isVietnamese ? "Lỗi" : "Error",
                        isVietnamese ? "Không thể tải ảnh đại diện" : "Could not load avatar image"
                );
            }
        }
    }

    /**
     * Reset form
     */
    @FXML
    private void handleResetAction(ActionEvent event) {
        // Clear all input data
        firstNameField.clear();
        lastNameField.clear();
        numberField.clear();
        emailField.clear();
        dayCombo.getSelectionModel().clearSelection();
        monthCombo.getSelectionModel().clearSelection();
        yearCombo.getSelectionModel().clearSelection();
        maleRadio.setSelected(true);
        femaleRadio.setSelected(false);
        otherRadio.setSelected(false);
        provinceCombo.getSelectionModel().clearSelection();
        districtCombo.getSelectionModel().clearSelection();
        wardCombo.getSelectionModel().clearSelection();
        specificAddressField.clear();
        usernameField.clear();
        passwordField.clear();
        visiblePasswordField.clear();
        confirmPasswordField.clear();
        visibleConfirmPasswordField.clear();

        // Reset avatar to default
        try {
            Image defaultAvatar = new Image(getClass().getResourceAsStream("/com/example/stores/images/user.png"));
            avatarView.setImage(defaultAvatar);
            selectedAvatarFile = null;
        } catch (Exception e) {
            System.err.println("Could not load default avatar: " + e.getMessage());
        }

        // Clear error messages
        phoneErrorLabel.setText("");
        emailErrorLabel.setText("");
        passwordErrorLabel.setText("");
        confirmPasswordErrorLabel.setText("");

        // Reset validation states
        isPhoneValid.set(false);
        isEmailValid.set(false);
        isPasswordValid.set(false);
        isPasswordMatching.set(false);
    }

    /**
     * Toggle password visibility
     */
    @FXML
    private void handleEyeButtonAction(ActionEvent event) {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            visiblePasswordField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);

            try {
                Image stopEyeIcon = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/stopeye_icon.png"));
                eyeIconView.setImage(stopEyeIcon);
            } catch (Exception e) {
                System.err.println("Could not load stopeye_icon: " + e.getMessage());
            }
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);

            try {
                Image eyeIcon = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/eye_icon.png"));
                eyeIconView.setImage(eyeIcon);
            } catch (Exception e) {
                System.err.println("Could not load eye_icon: " + e.getMessage());
            }
        }
    }

    /**
     * Toggle confirm password visibility
     */
    @FXML
    private void handleConfirmEyeButtonAction(ActionEvent event) {
        confirmPasswordVisible = !confirmPasswordVisible;

        if (confirmPasswordVisible) {
            visibleConfirmPasswordField.setText(confirmPasswordField.getText());
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            visibleConfirmPasswordField.setVisible(true);
            visibleConfirmPasswordField.setManaged(true);

            try {
                Image stopEyeIcon = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/stopeye_icon.png"));
                confirmEyeIconView.setImage(stopEyeIcon);
            } catch (Exception e) {
                System.err.println("Could not load stopeye_icon: " + e.getMessage());
            }
        } else {
            confirmPasswordField.setText(visibleConfirmPasswordField.getText());
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            visibleConfirmPasswordField.setVisible(false);
            visibleConfirmPasswordField.setManaged(false);

            try {
                Image eyeIcon = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/eye_icon.png"));
                confirmEyeIconView.setImage(eyeIcon);
            } catch (Exception e) {
                System.err.println("Could not load eye_icon: " + e.getMessage());
            }
        }
    }

    /**
     * Toggle language
     */
    /**
 * Toggle language
 */
@FXML
private void handleLanguageButtonAction(ActionEvent event) {
    isVietnamese = !isVietnamese;

    // Update language in LanguageManager
    LanguageManager.setVietnamese(isVietnamese);
    
    // Cập nhật hình ảnh cờ ngay lập tức
    if (languageFlag != null) {
        try {
            if (isVietnamese) {
                Image vnFlag = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/flag_vn.png"));
                languageFlag.setImage(vnFlag);
                langButton.setText("Tiếng Việt");
            } else {
                Image enFlag = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/flag_en.png"));
                languageFlag.setImage(enFlag);
                langButton.setText("English");
            }
        } catch (Exception e) {
            System.err.println("Error updating language flag: " + e.getMessage());
        }
    }

    // Debug để theo dõi
    System.out.println("Language changed to: " + (isVietnamese ? "Vietnamese" : "English"));
    
    // Update UI
    updateLanguage();
}

    /**
     * Handle registration
     */
    @FXML
private void handleRegisterAction(ActionEvent event) {
    // Get all form information
    String firstName = firstNameField.getText().trim();
    String lastName = lastNameField.getText().trim();
    String fullName = lastName + " " + firstName; // Vietnamese name format: Last + First
    String phone = numberField.getText().trim();
    String email = emailField.getText().trim();
    String username = usernameField.getText().trim(); // Di chuyển dòng này lên đây
    
    // Kiểm tra lần cuối trước khi đăng ký
    try {
        // Kiểm tra email trùng lặp
        if (authService.isEmailExists(email)) {
            emailErrorLabel.setText(isVietnamese ?
                    "Email đã được sử dụng. Vui lòng dùng email khác." :
                    "Email already in use. Please use another one.");
            isEmailValid.set(false);
            emailField.requestFocus();
            return;
        }
        
        // Kiểm tra số điện thoại trùng lặp
        if (authService.isPhoneExists(phone)) {
            phoneErrorLabel.setText(isVietnamese ?
                    "Số điện thoại đã được sử dụng. Vui lòng dùng số khác." :
                    "Phone number already in use. Please use another one.");
            isPhoneValid.set(false);
            numberField.requestFocus();
            return;
        }
        
        // Kiểm tra username trùng lặp
        if (authService.isUsernameExists(username)) {
            usernameErrorLabel.setText(isVietnamese ?
                    "Tên đăng nhập đã được sử dụng. Vui lòng chọn tên khác." :
                    "Username already in use. Please choose another one.");
            isUsernameValid.set(false);
            usernameField.requestFocus();
            return;
        }
    } catch (Exception e) {
        System.err.println("Lỗi kiểm tra trùng lặp: " + e.getMessage());
    }

    LocalDate birthDate = null;
    if (dayCombo.getValue() != null && monthCombo.getValue() != null && yearCombo.getValue() != null) {
        int day = dayCombo.getValue();
        int month;
        if (isVietnamese) {
            month = vietnameseMonths.indexOf(monthCombo.getValue()) + 1;
        } else {
            month = englishMonths.indexOf(monthCombo.getValue()) + 1;
        }
        int year = yearCombo.getValue();
        birthDate = LocalDate.of(year, month, day);
    }

    // Lấy giá trị giới tính và chuyển đổi sang định dạng chuẩn cho database
    RadioButton selectedGender = (RadioButton) genderGroup.getSelectedToggle();
    String genderText = selectedGender.getText();
    String gender;

    // Chuyển đổi giá trị hiển thị sang giá trị chuẩn cho database
    if (genderText.equals("Nam") || genderText.equals("Male")) {
        gender = "Nam";  // Lưu "Nam" vào database thay vì "Male"
    } else if (genderText.equals("Nữ") || genderText.equals("Female")) {
        gender = "Nữ";  // Lưu "Nữ" vào database thay vì "Female"
    } else {
        gender = "Khác";  // Lưu "Khác" vào database thay vì "Other"
    }
    
    // In ra để debug
    System.out.println("Gender value to be saved: " + gender);

    String province = provinceCombo.getValue();
    String district = districtCombo.getValue();
    String ward = wardCombo.getValue();
    String specificAddress = specificAddressField.getText().trim();
    String address = specificAddress + ", " + ward + ", " + district + ", " + province;

    String password = passwordVisible ? visiblePasswordField.getText() : passwordField.getText();

    // Create Customer object
    Customer customer = new Customer();
    customer.setFullName(fullName);
    customer.setPhone(phone);
    customer.setEmail(email);
    customer.setBirthDate(birthDate);
    customer.setGender(gender);
    customer.setAddress(address);
    customer.setUsername(username);
    customer.setPassword(password);

    // Call service to register
    try {
        boolean success = authService.register(customer);
        if (success) {
            // Show confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(isVietnamese ? "Đăng ký thành công" : "Registration Successful");
            alert.setHeaderText(null);
            alert.setContentText(isVietnamese ?
                    "Đăng ký tài khoản thành công! Bạn có muốn chuyển đến trang đăng nhập không?" :
                    "Account registration successful! Do you want to go to the login page?");

            ButtonType buttonTypeYes = new ButtonType(isVietnamese ? "Có" : "Yes");
            ButtonType buttonTypeNo = new ButtonType(isVietnamese ? "Không" : "No", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == buttonTypeYes) {
                // If user chooses "Yes", go to login page
                handleBackToLogin();
            }
            // If user chooses "No", stay on registration page

        } else {
            AlertUtils.showError(
                    isVietnamese ? "Lỗi đăng ký" : "Registration Error",
                    isVietnamese ? "Tên đăng nhập đã tồn tại. Vui lòng chọn tên đăng nhập khác." :
                            "Username already exists. Please choose a different username."
            );
        }
    } catch (Exception e) {
        AlertUtils.showError(
                isVietnamese ? "Lỗi" : "Error",
                isVietnamese ? "Có lỗi xảy ra khi đăng ký: " + e.getMessage() :
                        "An error occurred during registration: " + e.getMessage()
        );
        e.printStackTrace();
    }
}
        /**
     * Reload all image resources after navigation
     */
    public void reloadResources() {
        try {
            // Reload avatar icon
            if (avatarView != null) {
                Image avatarImage = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/user2.png"));
                avatarView.setImage(avatarImage);
            }
            
            // Reload eye icons
            if (eyeIconView != null) {
                Image eyeIcon = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/eye_icon.png"));
                eyeIconView.setImage(eyeIcon);
            }
            
            if (confirmEyeIconView != null) {
                Image eyeIcon = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/eye_icon.png"));
                confirmEyeIconView.setImage(eyeIcon);
            }
            
            // Reload language flag
            if (languageFlag != null) {
                if (isVietnamese) {
                    Image vnFlag = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/flag_vn.png"));
                    languageFlag.setImage(vnFlag);
                } else {
                    Image enFlag = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/flag_en.png"));
                    languageFlag.setImage(enFlag);
                }
            }
            
            // Update icon in createAccountBox
            ImageView userIconView = (ImageView) mainFormPane.lookup("#userIconView");
            if (userIconView != null) {
                Image userIcon = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/user2.png"));
                userIconView.setImage(userIcon);
            }
            
            // Reload header logo
            ImageView logoIcon = (ImageView) headerPane.lookup("#logoIcon");
            if (logoIcon != null) {
                Image logo = new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/Logo_Comp.png"));
                logoIcon.setImage(logo);
            }
        } catch (Exception e) {
            System.err.println("Error reloading resources: " + e.getMessage());
            e.printStackTrace();
        }
    }
        /**
     * Navigate back to login screen
     */
    @FXML
    private void handleBackToLogin() {
        try {
            // Save language state before navigating
            LanguageManager.setVietnamese(isVietnamese);
    
            // Get current window size
            Stage currentStage = (Stage) rootPane.getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            
            System.out.println("Navigating back to login with dimensions: " + width + "x" + height);
    
            // Use absolute class path to load login screen
            URL loginUrl = getClass().getResource("/com/example/stores/view/CustomerLogin.fxml");
            FXMLLoader loader = new FXMLLoader(loginUrl);
            Parent root = loader.load();
            
            // Create a new scene with the same dimensions
            Scene scene = new Scene(root);
    
            // Set up the stage with the new scene
            currentStage.setScene(scene);
            currentStage.setTitle(isVietnamese ? "CELLCOMP STORE - Đăng nhập" : "CELLCOMP STORE - Login");
            
            // Maintain dimensions and position
            currentStage.setWidth(width);
            currentStage.setHeight(height);
            currentStage.setX(x);
            currentStage.setY(y);
    
            // Get the controller and reload resources if needed
            CustomerLoginController controller = loader.getController();
            if (controller != null) {
                Platform.runLater(() -> {
                    controller.setupResponsiveLayout();
                    try {
                        // Use reflection to call reloadResources if it exists
                        Method reloadMethod = controller.getClass().getDeclaredMethod("reloadResources");
                        if (reloadMethod != null) {
                            reloadMethod.setAccessible(true);
                            reloadMethod.invoke(controller);
                        }
                    } catch (Exception e) {
                        // Method might not exist, which is fine
                        System.out.println("Note: reloadResources method not found in controller");
                    }
                });
            }
        } catch (IOException e) {
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở trang đăng nhập: " + e.getMessage() : 
                           "Could not open login page: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}