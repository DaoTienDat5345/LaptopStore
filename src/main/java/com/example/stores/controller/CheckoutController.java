package com.example.stores.controller;

import com.example.stores.model.CartItem;
import com.example.stores.util.AlertUtils;
import com.example.stores.util.LanguageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CheckoutController implements Initializable {

    // FXML Controls
    @FXML
    private Button cartButton; // Thêm biến cartButton

    // Các biến dữ liệu
    private List<CartItem> cartItems; // Tất cả các mặt hàng trong giỏ hàng
    private List<CartItem> selectedCartItems; // Các mặt hàng đã chọn để thanh toán
    private boolean isVietnamese; // Biến ngôn ngữ

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo controller
        isVietnamese = LanguageManager.isVietnamese();
        selectedCartItems = new ArrayList<>();
    }

    /**
     * Thiết lập danh sách mặt hàng trong giỏ hàng
     * @param items Danh sách CartItem
     */
    public void setCartItems(List<CartItem> items) {
        this.cartItems = items;
        // Mặc định chọn tất cả các mặt hàng để thanh toán
        this.selectedCartItems = new ArrayList<>(items);

        // Xử lý các mặt hàng để thanh toán
        if (cartItems != null && !cartItems.isEmpty()) {
            loadCheckoutItems();
        }
    }

    /**
     * Hiển thị các mặt hàng đã chọn lên giao diện thanh toán
     */
    private void loadCheckoutItems() {
        // TODO: Hiển thị danh sách các mặt hàng đã chọn lên giao diện
        // Ví dụ: Thiết lập dữ liệu cho TableView hoặc ListView
    }

    /**
     * Cập nhật mặt hàng được chọn để thanh toán
     * @param item Mặt hàng cần cập nhật
     * @param selected True nếu được chọn, false nếu bị bỏ chọn
     */
    public void updateSelectedItem(CartItem item, boolean selected) {
        if (selected && !selectedCartItems.contains(item)) {
            selectedCartItems.add(item);
        } else if (!selected) {
            selectedCartItems.remove(item);
        }
    }

    /**
     * Chuyển đến màn hình đặt hàng với các mặt hàng đã chọn
     */
    @FXML
    private void goToOrderScreen() {
        try {
            if (selectedCartItems == null || selectedCartItems.isEmpty()) {
                AlertUtils.showWarning(
                        isVietnamese ? "Cảnh báo" : "Warning",
                        isVietnamese ? "Vui lòng chọn ít nhất một sản phẩm để thanh toán" : "Please select at least one product to checkout"
                );
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Orders.fxml"));
            Parent root = loader.load();

            // Truyền danh sách sản phẩm đã chọn cho OrderController
            OrderController controller = loader.getController();
            controller.setCartItems(selectedCartItems);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/stores/css/Order.css").toExternalForm());

            Stage stage = (Stage) cartButton.getScene().getWindow();
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Đơn hàng" : "CELLCOMP STORE - Order");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể chuyển đến trang đơn hàng" : "Cannot navigate to order page"
            );
        }
    }

    /**
     * Quay lại trang giỏ hàng
     */
    @FXML
    private void goBackToCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Cart.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/stores/css/Cart.css").toExternalForm());

            Stage stage = (Stage) cartButton.getScene().getWindow();
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Giỏ hàng" : "CELLCOMP STORE - Cart");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể quay lại trang giỏ hàng" : "Cannot return to cart page"
            );
        }
    }
}