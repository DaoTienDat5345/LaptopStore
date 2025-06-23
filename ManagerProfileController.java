package com.example.stores.controller; // Bỏ .computerstore

import com.example.stores.model.Manager;
import com.example.stores.repository.impl.ManagerRepositoryImpl;
import com.example.stores.service.impl.ManagerServiceImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class ManagerProfileController {

    @FXML private ImageView imgManagerPhoto;
    @FXML private Button btnChangeImage;
    @FXML private TextField txtUsername;
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private RadioButton rbMale;
    @FXML private RadioButton rbFemale;
    @FXML private RadioButton rbOther;
    @FXML private ToggleGroup genderGroup;
    @FXML private DatePicker dpBirthDate;
    @FXML private TextArea txtAddress;
    @FXML private PasswordField txtNewPassword;
    @FXML private Button btnSaveChanges;
    @FXML private Button btnCancel;
    @FXML private Label lblStatus;

    private ManagerServiceImpl managerService;
    private Manager currentManager;
    private File selectedImageFile; // Lưu file ảnh được chọn
    private static final int MANAGER_ID = 1; // ID cố định của Manager duy nhất
    private static final String DEFAULT_IMAGE_PATH = "/com/example/stores/images/pngtree-modern-abstract-computer-logo-png-image_6511917-removebg-preview.png"; // Đường dẫn ảnh mặc định trong resources
    private static final String USER_IMAGES_DIRECTORY = "user_images"; // Thư mục lưu ảnh người dùng

    public ManagerProfileController() {
        // Khởi tạo service
        // Trong ứng dụng lớn, bạn sẽ dùng Dependency Injection
        ManagerRepositoryImpl managerRepository = new ManagerRepositoryImpl();
        this.managerService = new ManagerServiceImpl(managerRepository);
    }

    @FXML
    public void initialize() {
        // Tạo thư mục lưu ảnh nếu chưa có
        File userImagesDir = new File(USER_IMAGES_DIRECTORY);
        if (!userImagesDir.exists()) {
            userImagesDir.mkdirs();
        }

        loadManagerProfile();
        // Ban đầu, nút Lưu có thể bị vô hiệu hóa cho đến khi có thay đổi
        // btnSaveChanges.setDisable(true);
        // Thêm listener để theo dõi thay đổi và kích hoạt nút Lưu (nâng cao hơn)
    }

    private void loadManagerProfile() {
        Optional<Manager> managerOpt = managerService.getManagerProfile(MANAGER_ID);
        if (managerOpt.isPresent()) {
            currentManager = managerOpt.get();
            populateFields(currentManager);
        } else {
            lblStatus.setText("Lỗi: Không thể tải thông tin Manager.");
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
            // Vô hiệu hóa các trường và nút nếu không load được
            disableAllControls(true);
        }
    }

    private void populateFields(Manager manager) {
        txtUsername.setText(manager.getUsername());
        txtFullName.setText(manager.getFullName());
        txtEmail.setText(manager.getEmail());
        txtPhone.setText(manager.getPhone());

        if (manager.getGender() != null) {
            switch (manager.getGender().toLowerCase()) {
                case "nam":
                    rbMale.setSelected(true);
                    break;
                case "nữ":
                    rbFemale.setSelected(true);
                    break;
                case "khác":
                    rbOther.setSelected(true);
                    break;
            }
        }
        dpBirthDate.setValue(manager.getBirthDate());
        txtAddress.setText(manager.getAddress());
        txtNewPassword.clear(); // Luôn xóa trường mật khẩu khi load

        // Load ảnh
        loadImage(manager.getImageUrl());
    }

    private void loadImage(String imagePathString) {
        try {
            Image image;
            if (imagePathString != null && !imagePathString.isEmpty()) {
                File imageFile = new File(imagePathString);
                if (imageFile.exists()) {
                    image = new Image(imageFile.toURI().toURL().toString());
                } else {
                    // Nếu đường dẫn trong DB không hợp lệ, dùng ảnh mặc định
                    System.err.println("Không tìm thấy ảnh tại: " + imagePathString + ". Sử dụng ảnh mặc định.");
                    image = new Image(getClass().getResourceAsStream(DEFAULT_IMAGE_PATH));
                }
            } else {
                // Nếu không có đường dẫn ảnh trong DB, dùng ảnh mặc định
                image = new Image(getClass().getResourceAsStream(DEFAULT_IMAGE_PATH));
            }
            imgManagerPhoto.setImage(image);
        } catch (MalformedURLException | NullPointerException e) {
            System.err.println("Lỗi tải ảnh: " + e.getMessage() + ". Sử dụng ảnh mặc định.");
            try {
                imgManagerPhoto.setImage(new Image(getClass().getResourceAsStream(DEFAULT_IMAGE_PATH)));
            } catch (Exception ex) {
                System.err.println("Không thể tải ảnh mặc định: " + ex.getMessage());
                // Có thể set một màu nền hoặc không hiển thị gì nếu ảnh mặc định cũng lỗi
            }
        }
    }


    @FXML
    void handleChangeImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh đại diện");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        Stage stage = (Stage) btnChangeImage.getScene().getWindow();
        selectedImageFile = fileChooser.showOpenDialog(stage);

        if (selectedImageFile != null) {
            try {
                Image image = new Image(selectedImageFile.toURI().toURL().toString());
                imgManagerPhoto.setImage(image);
                // Không lưu vào DB ngay, chỉ lưu khi nhấn "Lưu Thay Đổi"
            } catch (MalformedURLException e) {
                lblStatus.setText("Lỗi: Định dạng file ảnh không hợp lệ.");
                lblStatus.setTextFill(javafx.scene.paint.Color.RED);
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleSaveChanges(ActionEvent event) {
        if (currentManager == null) {
            lblStatus.setText("Lỗi: Không có thông tin Manager để cập nhật.");
            return;
        }

        // Lấy dữ liệu từ form
        currentManager.setFullName(txtFullName.getText().trim());
        currentManager.setEmail(txtEmail.getText().trim());
        currentManager.setPhone(txtPhone.getText().trim());

        RadioButton selectedGender = (RadioButton) genderGroup.getSelectedToggle();
        if (selectedGender != null) {
            currentManager.setGender(selectedGender.getText());
        } else {
            currentManager.setGender(null); // Hoặc giá trị mặc định/thông báo lỗi
        }

        currentManager.setBirthDate(dpBirthDate.getValue());
        currentManager.setAddress(txtAddress.getText() != null ? txtAddress.getText().trim() : null);

        String newPassword = txtNewPassword.getText();
        if (newPassword != null && !newPassword.isEmpty()) {
            currentManager.setPassword(newPassword); // Service sẽ hash mật khẩu này
        } else {
            currentManager.setPassword(null); // Gửi null để Service biết không đổi mật khẩu
        }

        // Xử lý lưu ảnh
        if (selectedImageFile != null) {
            try {
                // Tạo tên file duy nhất hoặc dùng tên gốc + timestamp
                String fileName = "manager_" + MANAGER_ID + "_" + System.currentTimeMillis() + "." + getFileExtension(selectedImageFile.getName());
                Path targetPath = Paths.get(USER_IMAGES_DIRECTORY, fileName);
                Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                currentManager.setImageUrl(targetPath.toString()); // Lưu đường dẫn tương đối hoặc tuyệt đối vào DB
                selectedImageFile = null; // Reset sau khi lưu
            } catch (Exception e) {
                lblStatus.setText("Lỗi: Không thể lưu ảnh. " + e.getMessage());
                lblStatus.setTextFill(javafx.scene.paint.Color.RED);
                e.printStackTrace();
                return; // Không tiếp tục nếu lưu ảnh lỗi
            }
        }
        // Nếu không chọn ảnh mới, imageUrl cũ sẽ được giữ nguyên (Service không thay đổi nếu là null)

        try {
            boolean success = managerService.updateManagerProfile(currentManager);
            if (success) {
                lblStatus.setText("Cập nhật thông tin thành công!");
                lblStatus.setTextFill(javafx.scene.paint.Color.GREEN);
                loadManagerProfile(); // Load lại để chắc chắn dữ liệu mới nhất được hiển thị (và reset form)
            } else {
                lblStatus.setText("Lỗi: Cập nhật thông tin thất bại.");
                lblStatus.setTextFill(javafx.scene.paint.Color.RED);
            }
        } catch (IllegalArgumentException e) {
            lblStatus.setText("Lỗi dữ liệu: " + e.getMessage());
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
        } catch (Exception e) {
            lblStatus.setText("Lỗi hệ thống: " + e.getMessage());
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
            e.printStackTrace();
        }
    }

    private String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot == -1) {
            return ""; // Hoặc ném lỗi nếu cần phần mở rộng
        }
        return fileName.substring(lastIndexOfDot + 1);
    }

    @FXML
    void handleCancel(ActionEvent event) {
        // Load lại thông tin gốc từ DB
        loadManagerProfile();
        lblStatus.setText("Đã hủy các thay đổi.");
        lblStatus.setTextFill(javafx.scene.paint.Color.BLUE);
        selectedImageFile = null; // Reset file ảnh đã chọn nếu có
    }

    private void disableAllControls(boolean disable) {
        txtFullName.setDisable(disable);
        txtEmail.setDisable(disable);
        txtPhone.setDisable(disable);
        rbMale.setDisable(disable);
        rbFemale.setDisable(disable);
        rbOther.setDisable(disable);
        dpBirthDate.setDisable(disable);
        txtAddress.setDisable(disable);
        txtNewPassword.setDisable(disable);
        btnChangeImage.setDisable(disable);
        btnSaveChanges.setDisable(disable);
        btnCancel.setDisable(disable);
    }
}