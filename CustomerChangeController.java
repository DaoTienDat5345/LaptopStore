package com.example.stores.controller;

import com.example.stores.config.DatabaseConnection;
import com.example.stores.model.Customer;
import com.example.stores.service.impl.AuthService;
import com.example.stores.util.AlertUtils;
import com.example.stores.util.LanguageManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CustomerChangeController implements Initializable {
    // Khai báo các thành phần UI
    @FXML private AnchorPane rootPane, displayContainer, editContainer;
    @FXML private TextField displayFirstNameField, displayGenderField, displayNumberField;
    @FXML private TextField displayDateField, displayAddressField, displayCreationDateField;
    @FXML private TextField firstNameField, lastNameField, numberField, emailField, specificAddressField;
    @FXML private ComboBox<String> dayCombo, monthCombo, yearCombo;
    @FXML private ComboBox<String> provinceCombo, districtCombo, wardCombo;
    @FXML private RadioButton maleRadio, femaleRadio, otherRadio;
    @FXML private ToggleGroup genderGroup;
    @FXML private Button changeButton, changePasswordButton, applyButton, cancelButton;
    @FXML private Label phoneErrorLabel, emailErrorLabel, profileLabel, creationDateValueLabel, genderLabel;
    @FXML private ImageView languageFlag;
    
    // Lưu trữ dữ liệu khách hàng hiện tại
    private Customer currentCustomer;
    private boolean isVietnamese = true; // Mặc định là tiếng Việt
    
    // Map để lưu trữ dữ liệu địa chỉ
    private Map<String, List<String>> provinceToDistricts = new HashMap<>();
    private Map<String, List<String>> districtToWards = new HashMap<>();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        isVietnamese = LanguageManager.isVietnamese();
        // Khởi tạo các ComboBox cho ngày tháng năm
        initializeDateComboBoxes();
        
        // Khởi tạo các ComboBox cho địa chỉ
        initializeAddressComboBoxes();
        
        // Tải thông tin khách hàng từ cơ sở dữ liệu
        loadCustomerData();
        
        // Thiết lập validation cho email và số điện thoại
        setupValidation();
        
        // Khởi tạo trạng thái ngôn ngữ
        updateLanguage();
    }
    
    // Khởi tạo ComboBox ngày tháng năm
    private void initializeDateComboBoxes() {
        // Thêm các ngày từ 1-31
        List<String> days = new ArrayList<>();
        for (int i = 1; i <= 31; i++) {
            days.add(String.format("%02d", i));
        }
        dayCombo.setItems(FXCollections.observableArrayList(days));
        
        // Thêm các tháng từ 1-12
        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(String.format("%02d", i));
        }
        monthCombo.setItems(FXCollections.observableArrayList(months));
        
        // Thêm các năm từ 1950-2010
        List<String> years = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 75; i <= currentYear - 15; i++) {
            years.add(String.valueOf(i));
        }
        yearCombo.setItems(FXCollections.observableArrayList(years));
    }
    
    // Khởi tạo ComboBox cho địa chỉ - Cập nhật để giống CustomerRegister
    private void initializeAddressComboBoxes() {
        // 63 tỉnh thành của Việt Nam
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
        
        // Khởi tạo dữ liệu mẫu cho các quận/huyện theo tỉnh/thành phố
        initSampleDistrictsAndWards();
        
        // Cập nhật danh sách quận/huyện khi chọn tỉnh/thành phố
        provinceCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                List<String> districts = provinceToDistricts.getOrDefault(newVal, new ArrayList<>());
                districtCombo.setItems(FXCollections.observableArrayList(districts));
                districtCombo.getSelectionModel().clearSelection();
                wardCombo.getSelectionModel().clearSelection();
                wardCombo.setItems(FXCollections.observableArrayList());
            }
        });
        
        // Cập nhật danh sách phường/xã khi chọn quận/huyện
        districtCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                List<String> wards = districtToWards.getOrDefault(newVal, new ArrayList<>());
                wardCombo.setItems(FXCollections.observableArrayList(wards));
                wardCombo.getSelectionModel().clearSelection();
            }
        });
    }
    
    // Khởi tạo dữ liệu mẫu cho quận/huyện và phường/xã
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
    
    // Tải thông tin khách hàng từ cơ sở dữ liệu
    private void loadCustomerData() {
        try {
            // Lấy thông tin người dùng hiện tại từ AuthService
            Customer currentUser = AuthService.getCurrentUser();
            if (currentUser == null) {
                AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Vui lòng đăng nhập để xem thông tin tài khoản" : 
                                "Please login to view account information"
                );
                return;
            }
            
            // Hiển thị thông tin từ currentUser
            currentCustomer = currentUser;
            displayFirstNameField.setText(currentCustomer.getFullName());
            displayGenderField.setText(currentCustomer.getGender());
            displayNumberField.setText(currentCustomer.getPhone());
            
            if (currentCustomer.getBirthDate() != null) {
                displayDateField.setText(currentCustomer.getBirthDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            
            displayAddressField.setText(currentCustomer.getAddress());
            
            if (currentCustomer.getRegisteredAt() != null) {
                displayCreationDateField.setText(currentCustomer.getRegisteredAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            }
            
            // Truy vấn database với tên cột đúng - sử dụng customerID thay vì id
            String query = "SELECT * FROM Customer WHERE customerID = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setInt(1, currentUser.getId()); // getId() trả về customerID
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    // Không cần làm gì nếu đã lấy được từ AuthService
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                isVietnamese ? "Lỗi" : "Error",
                isVietnamese ? "Không thể tải thông tin khách hàng: " + e.getMessage() : 
                             "Cannot load customer information: " + e.getMessage()
            );
        }
    }

    // Cập nhật phương thức setupValidation() để thêm kiểm tra trùng lặp
    private void setupValidation() {
        // Validate số điện thoại
        numberField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (phoneErrorLabel != null) {
                // Kiểm tra định dạng số điện thoại
                if (!newValue.isEmpty() && !isValidPhoneNumber(newValue)) {
                    phoneErrorLabel.setText(isVietnamese ?
                            "Số điện thoại không hợp lệ (cần 10 số, bắt đầu bằng 03, 07, 09)" :
                            "Invalid phone number (need 10 digits, starting with 03, 07, 09)");
                    return;
                }

                // Kiểm tra trùng lặp trong database (nếu khác với số điện thoại hiện tại)
                if (currentCustomer != null && !newValue.isEmpty() && !newValue.equals(currentCustomer.getPhone())) {
                    try {
                        String query = "SELECT COUNT(*) FROM Customer WHERE phone = ? AND customerID != ?";
                        try (Connection conn = DatabaseConnection.getConnection();
                             PreparedStatement pstmt = conn.prepareStatement(query)) {

                            pstmt.setString(1, newValue);
                            pstmt.setInt(2, currentCustomer.getId());

                            ResultSet rs = pstmt.executeQuery();
                            if (rs.next() && rs.getInt(1) > 0) {
                                phoneErrorLabel.setText(isVietnamese ?
                                        "Số điện thoại đã được sử dụng bởi tài khoản khác" :
                                        "Phone number is already used by another account");
                                return;
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Lỗi kiểm tra số điện thoại: " + e.getMessage());
                    }
                }

                // Nếu không có lỗi
                phoneErrorLabel.setText("");
            }
        });

        // Validate email
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (emailErrorLabel != null) {
                // Kiểm tra định dạng email
                if (!newValue.isEmpty() && !isValidEmail(newValue)) {
                    emailErrorLabel.setText(isVietnamese ?
                            "Email không hợp lệ (ví dụ: example@mail.com)" :
                            "Invalid email (example: example@mail.com)");
                    return;
                }

                // Kiểm tra trùng lặp trong database (nếu khác với email hiện tại)
                if (currentCustomer != null && !newValue.isEmpty() && !newValue.equals(currentCustomer.getEmail())) {
                    try {
                        String query = "SELECT COUNT(*) FROM Customer WHERE email = ? AND customerID != ?";
                        try (Connection conn = DatabaseConnection.getConnection();
                             PreparedStatement pstmt = conn.prepareStatement(query)) {

                            pstmt.setString(1, newValue);
                            pstmt.setInt(2, currentCustomer.getId());

                            ResultSet rs = pstmt.executeQuery();
                            if (rs.next() && rs.getInt(1) > 0) {
                                emailErrorLabel.setText(isVietnamese ?
                                        "Email đã được sử dụng bởi tài khoản khác" :
                                        "Email is already used by another account");
                                return;
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Lỗi kiểm tra email: " + e.getMessage());
                    }
                }

                // Nếu không có lỗi
                emailErrorLabel.setText("");
            }
        });
    }
    
    // Xử lý khi nhấn nút Thay Đổi
    @FXML
    private void handleChangeAction(ActionEvent event) {
        // Chuẩn bị thông tin từ thông tin hiện tại
        prepareEditForm();
        
        // Hiển thị chế độ chỉnh sửa
        displayContainer.setVisible(false);
        editContainer.setVisible(true);
        
        // Ẩn nút Thay đổi và Đổi mật khẩu, hiện nút Áp dụng và Hủy
        changeButton.setVisible(false);
        changePasswordButton.setVisible(false);
        applyButton.setVisible(true);
        cancelButton.setVisible(true);
    }
    
    // Xử lý khi nhấn nút Áp Dụng để lưu thay đổi
    @FXML
    private void handleApplyAction(ActionEvent event) {
        // Kiểm tra dữ liệu và lưu thay đổi
        if (validateFields() && saveChanges()) {
            // Hiển thị thông báo thành công
            AlertUtils.showInfo(
                isVietnamese ? "Thành công" : "Success",
                isVietnamese ? "Thông tin đã được cập nhật thành công" : "Information has been updated successfully"
            );
            
            // Tải lại dữ liệu và hiển thị chế độ xem
            loadCustomerData();
            displayContainer.setVisible(true);
            editContainer.setVisible(false);
            
            // Hiện lại các nút
            changeButton.setVisible(true);
            changePasswordButton.setVisible(true);
            applyButton.setVisible(false);
            cancelButton.setVisible(false);
        }
    }
    
    // Xử lý khi nhấn nút Hủy
    @FXML
    private void handleCancelAction(ActionEvent event) {
        // Quay lại chế độ hiển thị mà không lưu thay đổi
        displayContainer.setVisible(true);
        editContainer.setVisible(false);
        
        // Hiện lại các nút
        changeButton.setVisible(true);
        changePasswordButton.setVisible(true);
        applyButton.setVisible(false);
        cancelButton.setVisible(false);
    }
    
    // Xử lý khi nhấn nút đổi mật khẩu
    @FXML
    private void handleChangePasswordAction(ActionEvent event) {
        try {
            // Lưu trạng thái ngôn ngữ hiện tại
            LanguageManager.setVietnamese(isVietnamese);

            // Tải FXML đổi mật khẩu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/ChangePassword.fxml"));
            Parent root = loader.load();

            // Lấy Stage hiện tại
            Stage stage = (Stage) changePasswordButton.getScene().getWindow();

            // Chuyển đến màn hình đổi mật khẩu
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Đổi mật khẩu" : "CELLCOMP STORE - Change Password");

            // Lấy controller để cập nhật nguồn vào
            ChangePasswordController controller = loader.getController();
            if (controller != null) {
                // Thiết lập nguồn vào là từ màn hình thông tin cá nhân (profile)
                controller.setSourceScreen("profile");
            }

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở trang đổi mật khẩu: " + e.getMessage() :
                            "Cannot open change password page: " + e.getMessage()
            );
        }
    }
    
    // Xử lý khi nhấn nút đổi ngôn ngữ
    @FXML
    private void handleLanguageButtonAction(ActionEvent event) {
        // Đổi trạng thái ngôn ngữ trong LanguageManager
        LanguageManager.setVietnamese(!LanguageManager.isVietnamese());

        // Cập nhật biến local
        isVietnamese = LanguageManager.isVietnamese();

        // Cập nhật giao diện
        updateLanguage();
    }

    // Cập nhật ngôn ngữ hiển thị
    private void updateLanguage() {
        if (isVietnamese) {
            // Đổi cờ sang Việt Nam
            languageFlag.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/flag_vn.png")));

            // Cập nhật các button
            ((Button) languageFlag.getParent().lookup("#langButton")).setText("Tiếng Việt");
            changeButton.setText("Thay đổi");
            applyButton.setText("Áp dụng");
            cancelButton.setText("Hủy");
            changePasswordButton.setText("Đổi mật khẩu");

            // Cập nhật tiêu đề
            profileLabel.setText("Thông tin cá nhân");

            // Cập nhật các placeholder cho ComboBox địa chỉ
            provinceCombo.setPromptText("Tỉnh/Thành phố");
            districtCombo.setPromptText("Quận/Huyện");
            wardCombo.setPromptText("Phường/Xã");
            specificAddressField.setPromptText("Địa chỉ cụ thể");

            // Cập nhật các label trong display container
            Label nameLabel = (Label) displayContainer.lookup(".info-label");
            if (nameLabel != null) {
                nameLabel.setText("Tên:");
            }

            // Tìm và cập nhật các label khác bằng vị trí
            for (javafx.scene.Node node : displayContainer.getChildren()) {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    switch (label.getText()) {
                        case "Gender:":
                            label.setText("Giới tính:");
                            break;
                        case "Your number:":
                            label.setText("Số điện thoại:");
                            break;
                        case "Date:":
                            label.setText("Ngày sinh:");
                            break;
                        case "Address:":
                            label.setText("Địa chỉ:");
                            break;
                        case "Account creation date:":
                            label.setText("Ngày tạo tài khoản:");
                            break;
                    }
                }
            }

            // Cập nhật placeholders cho các TextField
            displayFirstNameField.setPromptText("Tên");
            displayGenderField.setPromptText("Giới tính");
            displayNumberField.setPromptText("Số điện thoại");
            displayDateField.setPromptText("Ngày/Tháng/Năm");
            displayAddressField.setPromptText("Địa chỉ");

            // Cập nhật label và các RadioButton cho phần giới tính
            genderLabel.setText("Giới tính:");
            maleRadio.setText("Nam");
            femaleRadio.setText("Nữ");
            otherRadio.setText("Khác");

            // Cập nhật ComboBox ngày tháng
            dayCombo.setPromptText("Ngày");
            monthCombo.setPromptText("Tháng");
            yearCombo.setPromptText("Năm");
        } else {
            // Đổi cờ sang Anh
            languageFlag.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/flag_en.png")));

            // Cập nhật các button
            ((Button) languageFlag.getParent().lookup("#langButton")).setText("English");
            changeButton.setText("Change");
            applyButton.setText("Apply");
            cancelButton.setText("Cancel");
            changePasswordButton.setText("Change password");

            // Cập nhật tiêu đề
            profileLabel.setText("Personal Information");

            // Cập nhật các placeholder cho ComboBox địa chỉ
            provinceCombo.setPromptText("Province/City");
            districtCombo.setPromptText("District");
            wardCombo.setPromptText("Ward");
            specificAddressField.setPromptText("Specific address");

            // Cập nhật các label trong display container
            Label nameLabel = (Label) displayContainer.lookup(".info-label");
            if (nameLabel != null) {
                nameLabel.setText("Name:");
            }

            // Tìm và cập nhật các label khác bằng vị trí
            for (javafx.scene.Node node : displayContainer.getChildren()) {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    switch (label.getText()) {
                        case "Giới tính:":
                            label.setText("Gender:");
                            break;
                        case "Số điện thoại:":
                            label.setText("Your number:");
                            break;
                        case "Ngày sinh:":
                            label.setText("Date:");
                            break;
                        case "Địa chỉ:":
                            label.setText("Address:");
                            break;
                        case "Ngày tạo tài khoản:":
                            label.setText("Account creation date:");
                            break;
                    }
                }
            }

            // Cập nhật placeholders cho các TextField
            displayFirstNameField.setPromptText("Name");
            displayGenderField.setPromptText("Gender");
            displayNumberField.setPromptText("Your number");
            displayDateField.setPromptText("Day/Month/Year");
            displayAddressField.setPromptText("Address");

            // Cập nhật label và các RadioButton cho phần giới tính
            genderLabel.setText("Gender:");
            maleRadio.setText("Male");
            femaleRadio.setText("Female");
            otherRadio.setText("Other");

            // Cập nhật ComboBox ngày tháng
            dayCombo.setPromptText("Day");
            monthCombo.setPromptText("Month");
            yearCombo.setPromptText("Year");
        }
    }
    
    // Chuẩn bị form chỉnh sửa từ dữ liệu hiện tại
    private void prepareEditForm() {
        // Lấy dữ liệu từ currentCustomer để điền vào form chỉnh sửa
        if (currentCustomer != null) {
            String fullName = currentCustomer.getFullName();
            String firstName = "";
            String lastName = "";
            
            // Phân tách họ và tên
            if (fullName != null && fullName.contains(" ")) {
                int lastSpaceIndex = fullName.lastIndexOf(" ");
                firstName = fullName.substring(0, lastSpaceIndex).trim();
                lastName = fullName.substring(lastSpaceIndex + 1).trim();
            } else {
                lastName = fullName != null ? fullName : "";
            }
            
            // Điền thông tin vào các trường
            firstNameField.setText(firstName);
            lastNameField.setText(lastName);
            emailField.setText(currentCustomer.getEmail());
            numberField.setText(currentCustomer.getPhone());
            
            // Xử lý địa chỉ (phân tách địa chỉ đã lưu thành thành phố, quận/huyện, phường/xã và địa chỉ cụ thể)
            if (currentCustomer.getAddress() != null) {
                String address = currentCustomer.getAddress();
                try {
                    String[] parts = address.split(", ");
                    if (parts.length >= 4) {
                        // Format: [Địa chỉ cụ thể], [Phường/Xã], [Quận/Huyện], [Tỉnh/Thành phố]
                        specificAddressField.setText(parts[0].trim());
                        
                        // Thiết lập giá trị cho các ComboBox
                        String ward = parts[1].trim();
                        String district = parts[2].trim();
                        String province = parts[3].trim();
                        
                        // Đặt tỉnh/thành phố trước, kích hoạt listener để load quận/huyện
                        provinceCombo.setValue(province);
                        
                        // Sau khi đã có quận/huyện, đặt giá trị quận/huyện, kích hoạt listener để load phường/xã
                        Platform.runLater(() -> {
                            districtCombo.setValue(district);
                            // Sau khi đã load phường/xã, đặt giá trị phường/xã
                            Platform.runLater(() -> {
                                wardCombo.setValue(ward);
                            });
                        });
                    } else {
                        // Nếu định dạng không đúng, hiển thị toàn bộ địa chỉ vào địa chỉ cụ thể
                        specificAddressField.setText(address);
                    }
                } catch (Exception e) {
                    // Nếu không thể phân tích địa chỉ, hiển thị toàn bộ vào địa chỉ cụ thể
                    specificAddressField.setText(address);
                    System.out.println("Không thể phân tích địa chỉ: " + address);
                }
            }
            
            // Thiết lập giới tính
            if (currentCustomer.getGender() != null) {
                switch (currentCustomer.getGender().toLowerCase()) {
                    case "nam":
                    case "male":
                        maleRadio.setSelected(true);
                        break;
                    case "nữ": 
                    case "female":
                        femaleRadio.setSelected(true);
                        break;
                    default:
                        otherRadio.setSelected(true);
                        break;
                }
            }
            
            // Thiết lập ngày tháng năm nếu có
            if (currentCustomer.getBirthDate() != null) {
                LocalDate birthDate = currentCustomer.getBirthDate();
                dayCombo.setValue(String.format("%02d", birthDate.getDayOfMonth()));
                monthCombo.setValue(String.format("%02d", birthDate.getMonthValue()));
                yearCombo.setValue(String.valueOf(birthDate.getYear()));
            }
            
            // Hiển thị ngày tạo tài khoản (chỉ xem, không sửa)
            if (currentCustomer.getRegisteredAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                creationDateValueLabel.setText(isVietnamese ? 
                    "Ngày tạo: " + currentCustomer.getRegisteredAt().format(formatter) :
                    "Created: " + currentCustomer.getRegisteredAt().format(formatter));
            }
        }
    }

    // Thêm biến instance cho AuthService
    private AuthService authService = new AuthService();

    // Cập nhật phương thức validateFields() để kiểm tra email và số điện thoại đã tồn tại
    private boolean validateFields() {
        // Kiểm tra email
        String email = emailField.getText().trim();
        if (email.isEmpty() || !isValidEmail(email)) {
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Email không hợp lệ" : "Invalid email"
            );
            return false;
        }

        // Kiểm tra số điện thoại
        String phone = numberField.getText().trim();
        if (phone.isEmpty() || !isValidPhoneNumber(phone)) {
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Số điện thoại không hợp lệ" : "Invalid phone number"
            );
            return false;
        }

        // Kiểm tra họ và tên
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        if (firstName.isEmpty() || lastName.isEmpty()) {
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Vui lòng nhập đầy đủ họ và tên" : "Please enter both first and last name"
            );
            return false;
        }

        // Kiểm tra email đã tồn tại chưa (nếu khác email hiện tại)
        if (!email.equals(currentCustomer.getEmail())) {
            try {
                // Kiểm tra xem email mới có trùng với email của người dùng khác không
                String query = "SELECT COUNT(*) FROM Customer WHERE email = ? AND customerID != ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {

                    pstmt.setString(1, email);
                    pstmt.setInt(2, currentCustomer.getId());

                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        AlertUtils.showError(
                                isVietnamese ? "Lỗi" : "Error",
                                isVietnamese ? "Email đã được sử dụng bởi tài khoản khác" :
                                        "Email is already used by another account"
                        );
                        return false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showError(
                        isVietnamese ? "Lỗi" : "Error",
                        isVietnamese ? "Không thể kiểm tra email: " + e.getMessage() :
                                "Cannot check email: " + e.getMessage()
                );
                return false;
            }
        }

        // Kiểm tra số điện thoại đã tồn tại chưa (nếu khác số điện thoại hiện tại)
        if (!phone.equals(currentCustomer.getPhone())) {
            try {
                // Kiểm tra xem số điện thoại mới có trùng với số điện thoại của người dùng khác không
                String query = "SELECT COUNT(*) FROM Customer WHERE phone = ? AND customerID != ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {

                    pstmt.setString(1, phone);
                    pstmt.setInt(2, currentCustomer.getId());

                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        AlertUtils.showError(
                                isVietnamese ? "Lỗi" : "Error",
                                isVietnamese ? "Số điện thoại đã được sử dụng bởi tài khoản khác" :
                                        "Phone number is already used by another account"
                        );
                        return false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showError(
                        isVietnamese ? "Lỗi" : "Error",
                        isVietnamese ? "Không thể kiểm tra số điện thoại: " + e.getMessage() :
                                "Cannot check phone number: " + e.getMessage()
                );
                return false;
            }
        }

        return true;
    }

    // Lưu thay đổi vào database
    private boolean saveChanges() {
        try {
            // Lấy dữ liệu từ form
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String fullName = firstName + " " + lastName;

            String email = emailField.getText().trim();
            String phone = numberField.getText().trim();

            // Tạo địa chỉ đầy đủ từ các thành phần
            String fullAddress = "";
            if (specificAddressField.getText() != null && !specificAddressField.getText().isEmpty() &&
                    wardCombo.getValue() != null && districtCombo.getValue() != null && provinceCombo.getValue() != null) {
                // Format: [Địa chỉ cụ thể], [Phường/Xã], [Quận/Huyện], [Tỉnh/Thành phố]
                fullAddress = specificAddressField.getText().trim() + ", " +
                        wardCombo.getValue() + ", " +
                        districtCombo.getValue() + ", " +
                        provinceCombo.getValue();
            }

            // Lấy giới tính và đảm bảo lưu bằng tiếng Việt
            String gender = "Khác";
            if (maleRadio.isSelected()) {
                gender = "Nam";
            } else if (femaleRadio.isSelected()) {
                gender = "Nữ";
            }
            // Nếu UI đang ở tiếng Anh, đã chuyển đổi các giá trị sang tiếng Việt để lưu vào DB

            // Lấy ngày sinh
            LocalDate birthDate = null;
            if (dayCombo.getValue() != null && monthCombo.getValue() != null && yearCombo.getValue() != null) {
                try {
                    int day = Integer.parseInt(dayCombo.getValue());
                    int month = Integer.parseInt(monthCombo.getValue());
                    int year = Integer.parseInt(yearCombo.getValue());
                    birthDate = LocalDate.of(year, month, day);
                } catch (Exception e) {
                    System.out.println("Lỗi ngày tháng: " + e.getMessage());
                }
            }

            // Cập nhật thông tin khách hàng
            if (currentCustomer != null) {
                currentCustomer.setFullName(fullName);
                currentCustomer.setEmail(email);
                currentCustomer.setPhone(phone);
                currentCustomer.setAddress(fullAddress);
                currentCustomer.setGender(gender);  // Luôn lưu bằng tiếng Việt: "Nam", "Nữ", "Khác"
                currentCustomer.setBirthDate(birthDate);

                // Lưu thay đổi vào database với tên cột đúng là customerID thay vì id
                String query = "UPDATE Customer SET fullName = ?, email = ?, phone = ?, address = ?, gender = ?, birthDate = ? WHERE customerID = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {

                    pstmt.setString(1, fullName);
                    pstmt.setString(2, email);
                    pstmt.setString(3, phone);
                    pstmt.setString(4, fullAddress);
                    pstmt.setString(5, gender);  // Luôn lưu "Nam", "Nữ", "Khác" trong database

                    if (birthDate != null) {
                        pstmt.setDate(6, java.sql.Date.valueOf(birthDate));
                    } else {
                        pstmt.setNull(6, java.sql.Types.DATE);
                    }

                    pstmt.setInt(7, currentCustomer.getId());

                    int rowsAffected = pstmt.executeUpdate();
                    return rowsAffected > 0;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Có lỗi xảy ra khi cập nhật thông tin: " + e.getMessage() :
                            "Error updating information: " + e.getMessage()
            );
            return false;
        }
    }
    
    // Kiểm tra số điện thoại hợp lệ
    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("^(03|07|09)\\d{8}$");
    }
    
    // Kiểm tra email hợp lệ
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
    /**
 * Xử lý sự kiện quay trở lại trang chủ
 * @param event Sự kiện kích hoạt
 */
    /**
     * Xử lý sự kiện quay trở lại trang chủ
     * @param event Sự kiện kích hoạt
     */
    @FXML
    private void handleBackToHome(ActionEvent event) {
        try {
            // Lưu ngôn ngữ hiện tại
            LanguageManager.setVietnamese(isVietnamese);

            // Tải FXML trang chủ
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Home.fxml"));
            Parent homeRoot = loader.load();

            // Lấy Stage hiện tại
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Chuyển sang giao diện Home
            Scene scene = new Scene(homeRoot);
            stage.setScene(scene);
            stage.show();

            // Đồng bộ ngôn ngữ
            HomeController controller = loader.getController();
            controller.refreshLanguageDisplay();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể quay lại trang chủ: " + e.getMessage() :
                            "Cannot return to home page: " + e.getMessage()
            );
        }
    }
}