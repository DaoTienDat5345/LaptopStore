package com.example.stores; // Package của MainApp

import com.example.stores.config.DatabaseConnection; // Đảm bảo import đúng lớp kết nối
import com.example.stores.controller.MainViewController;
import com.example.stores.model.Manager;
import com.example.stores.repository.ManagerRepository;
import com.example.stores.repository.impl.ManagerRepositoryImpl;
import com.example.stores.service.ManagerService;
import com.example.stores.service.impl.ManagerServiceImpl;
import com.example.stores.util.LanguageForManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL; // Import thêm URL
import java.sql.SQLException;
import java.util.Optional;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Khởi tạo và kiểm tra kết nối CSDL khi ứng dụng bắt đầu
            DatabaseConnection.getConnection();
            System.out.println("Kết nối CSDL thành công từ MainApp!");

            // 2. Load FXML cho màn hình chính (MainView)
            // Sử dụng đường dẫn tuyệt đối từ gốc của classpath (thư mục resources)
            String mainViewPath = "/com/example/stores/view/MainView.fxml"; // Đường dẫn đến MainView
            URL mainViewUrl = getClass().getResource(mainViewPath);

            if (mainViewUrl == null) {
                System.err.println("Không tìm thấy file FXML tại: " + mainViewPath);
                showAlert(Alert.AlertType.ERROR, "Lỗi Tải Giao Diện", "Không tìm thấy file giao diện chính: " + mainViewPath);
                return; // Không thể tiếp tục nếu không có giao diện chính
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/LoginView.fxml"));;
            Parent root = loader.load();

            // 3. Tạo Scene và hiển thị Stage
            Scene scene = new Scene(root);

            // 4. (TÙY CHỌN) Load và áp dụng CSS
            String cssPath = "/com/example/stores/css/styles.css"; // Đường dẫn đến file CSS
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("Đã áp dụng CSS: " + cssPath);
            } else {
                System.out.println("Không tìm thấy file CSS tại: " + cssPath + ". Bỏ qua việc áp dụng CSS.");
                // Bạn có thể hiển thị một cảnh báo nhỏ nếu muốn, nhưng thường thì không cần thiết nếu CSS chỉ là tùy chọn.
            }

            primaryStage.setTitle("Hệ Thống Quản Lý Cửa Hàng Máy Tính");
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(event -> {
                System.out.println("Cửa sổ chính đang đóng, đóng kết nối CSDL...");
                DatabaseConnection.closeConnection();
            });
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Lỗi vào/ra khi tải FXML hoặc CSS: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Ứng Dụng", "Không thể tải giao diện hoặc tài nguyên: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối CSDL: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Kết Nối", "Không thể kết nối đến cơ sở dữ liệu: " + e.getMessage() + "\nVui lòng kiểm tra cấu hình và khởi động lại ứng dụng.");
        } catch (Exception e) { // Bắt các lỗi không mong muốn khác
            System.err.println("Lỗi không xác định trong MainApp: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Không Xác Định", "Đã xảy ra lỗi không mong muốn: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Ứng dụng đang dừng, đóng kết nối CSDL...");
        DatabaseConnection.closeConnection();
        super.stop();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}