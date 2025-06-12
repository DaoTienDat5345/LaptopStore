package com.example.stores.controller;

import com.example.stores.model.Order;
import com.example.stores.service.impl.AuthService;
import com.example.stores.service.impl.OrderService;
import com.example.stores.util.AlertUtils;
import com.example.stores.util.LanguageManager;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class OrderHistoryController implements Initializable {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> orderIdColumn;
    @FXML private TableColumn<Order, String> orderDateColumn;
    @FXML private TableColumn<Order, String> totalAmountColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> recipientNameColumn;
    @FXML private TableColumn<Order, Void> actionColumn;

    @FXML private Label historyTitle;
    @FXML private Label historyTitleHeading;
    @FXML private MenuButton userMenuButton;
    @FXML private MenuItem accountInfoMenuItem;
    @FXML private MenuItem logoutMenuItem;
    @FXML private MenuItem customDesignMenuItem;
    @FXML private MenuItem languageSwitchMenuItem;

    private OrderService orderService;
    private AuthService authService;
    private boolean isVietnamese = true;
    private ObservableList<Order> ordersList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo services
        orderService = new OrderService();
        authService = AuthService.getInstance();

        // Lấy ngôn ngữ hiện tại từ LanguageManager
        isVietnamese = LanguageManager.isVietnamese();

        // Thiết lập bảng và các cột
        setupTable();

        // Tải danh sách đơn hàng
        loadOrders();

        // Cập nhật giao diện theo ngôn ngữ và điều chỉnh kích thước cửa sổ
        Platform.runLater(() -> {
            updateLanguage();

            // Đảm bảo cửa sổ đủ lớn để hiển thị toàn bộ nội dung
            Scene scene = ordersTable.getScene();
            if (scene != null) {
                Stage stage = (Stage) scene.getWindow();
                // Đặt kích thước tối thiểu của cửa sổ
                stage.setMinHeight(700);
                stage.setHeight(700);

                // Thiết lập lại scroll để hiển thị toàn bộ dữ liệu
                ordersTable.refresh();

                // Đi đến hàng đầu tiên để đảm bảo hiển thị từ đầu
                ordersTable.scrollTo(0);
            }
        });
    }

    private void setupTable() {
        // Thiết lập các cột
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        // Format ngày đặt hàng
        orderDateColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return new SimpleStringProperty(cellData.getValue().getOrderDate().format(formatter));
        });

        // Format tiền tệ
        totalAmountColumn.setCellValueFactory(cellData -> {
            NumberFormat currencyFormat = NumberFormat.getNumberInstance(isVietnamese ? new Locale("vi", "VN") : Locale.US);
            return new SimpleStringProperty(currencyFormat.format(cellData.getValue().getTotalAmount()) + " ₫");
        });

        // Style trạng thái đơn hàng
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        statusColumn.setCellFactory(column -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    // Áp dụng style dựa trên trạng thái
                    if (item.equals("Đã xác nhận") || item.equals("Confirmed")) {
                        getStyleClass().add("status-confirmed");
                    } else if (item.equals("Đã hủy") || item.equals("Canceled")) {
                        getStyleClass().add("status-canceled");
                    }
                }
            }
        });

        recipientNameColumn.setCellValueFactory(new PropertyValueFactory<>("recipientName"));

        // Thiết lập cột action với nút xem và hủy đơn
        actionColumn.setCellFactory(column -> new TableCell<Order, Void>() {
            private final Button viewButton = new Button();
            private final Button cancelButton = new Button();
            private final HBox hbox = new HBox(5);

            {
                // Thiết lập nút xem chi tiết
                viewButton.getStyleClass().add("view-button");
                viewButton.setText(isVietnamese ? "Xem" : "View");
                viewButton.setPrefWidth(70);
                viewButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    viewOrderDetails(order);
                });

                // Thiết lập nút hủy đơn
                cancelButton.getStyleClass().add("cancel-button");
                cancelButton.setText(isVietnamese ? "Hủy" : "Cancel");
                cancelButton.setPrefWidth(70);
                cancelButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    cancelOrder(order);
                });

                hbox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());

                    hbox.getChildren().clear();
                    hbox.getChildren().add(viewButton);

                    // Chỉ hiển thị nút hủy nếu đơn hàng đang ở trạng thái "Đã xác nhận"
                    if ("Đã xác nhận".equals(order.getOrderStatus()) || "Confirmed".equals(order.getOrderStatus())) {
                        hbox.getChildren().add(cancelButton);
                    }

                    setGraphic(hbox);
                }
            }
        });

        // Bấm đúp vào dòng để xem chi tiết
        ordersTable.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    viewOrderDetails(row.getItem());
                }
            });
            return row;
        });

        // Thiết lập dữ liệu cho bảng
        ordersTable.setItems(ordersList);
    }

    private void loadOrders() {
        try {
            // Lấy ID của khách hàng đang đăng nhập
            int customerId = authService.getCurrentCustomer().getId();

            // Lấy danh sách đơn hàng từ service
            List<Order> customerOrders = orderService.getCustomerOrders(customerId);

            // Cập nhật danh sách
            ordersList.clear();
            ordersList.addAll(customerOrders);

            System.out.println("Đã tải " + customerOrders.size() + " đơn hàng");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể tải lịch sử đơn hàng: " + e.getMessage() :
                            "Cannot load order history: " + e.getMessage()
            );
        }
    }

    private void viewOrderDetails(Order order) {
        try {
            // Lưu trạng thái ngôn ngữ hiện tại
            LanguageManager.setVietnamese(isVietnamese);

            // Tải màn hình chi tiết đơn hàng
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/OrderDetails.fxml"));
            Parent root = loader.load();

            // Truyền thông tin đơn hàng
            OrderDetailsController controller = loader.getController();
            controller.setIsVietnamese(isVietnamese);
            controller.setOrder(order);
            controller.loadOrderDetails();

            // Hiển thị scene mới
            Scene scene = new Scene(root);
            Stage stage = (Stage) ordersTable.getScene().getWindow();
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Chi tiết đơn hàng" : "CELLCOMP STORE - Order Details");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở chi tiết đơn hàng: " + e.getMessage() :
                            "Cannot open order details: " + e.getMessage()
            );
        }
    }

    private void cancelOrder(Order order) {
        // Hiện hộp thoại xác nhận
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(isVietnamese ? "Xác nhận hủy đơn" : "Cancel Confirmation");
        confirmAlert.setHeaderText(isVietnamese ? "Bạn có chắc chắn muốn hủy đơn hàng này?" :
                "Are you sure you want to cancel this order?");
        confirmAlert.setContentText(isVietnamese ? "Mã đơn hàng: " + order.getOrderId() :
                "Order ID: " + order.getOrderId());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Gọi service để hủy đơn hàng
                boolean success = orderService.cancelOrder(order.getOrderId());

                if (success) {
                    // Cập nhật trạng thái đơn hàng trong danh sách
                    order.setOrderStatus(isVietnamese ? "Đã hủy" : "Canceled");

                    // THAY ĐỔI: Tải lại toàn bộ danh sách đơn hàng thay vì chỉ refresh bảng
                    loadOrders();

                    // Đảm bảo UI được cập nhật đầy đủ
                    Platform.runLater(() -> {
                        ordersTable.refresh();
                    });

                    AlertUtils.showInfo(
                            isVietnamese ? "Thành công" : "Success",
                            isVietnamese ? "Đã hủy đơn hàng thành công" : "Order canceled successfully"
                    );
                } else {
                    AlertUtils.showError(
                            isVietnamese ? "Lỗi" : "Error",
                            isVietnamese ? "Không thể hủy đơn hàng" : "Could not cancel the order"
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showError(
                        isVietnamese ? "Lỗi" : "Error",
                        isVietnamese ? "Không thể hủy đơn hàng: " + e.getMessage() :
                                "Cannot cancel order: " + e.getMessage()
                );
            }
        }
    }

    @FXML
    private void goBack() {
        try {
            // Lưu trạng thái ngôn ngữ hiện tại
            LanguageManager.setVietnamese(isVietnamese);

            // Tải trang chủ
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Home.fxml"));
            Parent root = loader.load();

            // Hiển thị scene mới
            Scene scene = new Scene(root);
            Stage stage = (Stage) ordersTable.getScene().getWindow();
            stage.setTitle("CELLCOMP STORE");
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

    @FXML
    private void goToHome() {
        goBack(); // Tái sử dụng phương thức goBack
    }

    @FXML
    private void handleLogout() {
        try {
            // Đăng xuất người dùng
            authService.logout();

            // Lưu trạng thái ngôn ngữ hiện tại
            LanguageManager.setVietnamese(isVietnamese);

            // Tải trang đăng nhập
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomerLogin.fxml"));
            Parent root = loader.load();

            // Hiển thị scene mới
            Scene scene = new Scene(root);
            Stage stage = (Stage) ordersTable.getScene().getWindow();
            stage.setTitle("CELLCOMP STORE - Login");
            stage.setScene(scene);
            stage.show();

            // Đồng bộ ngôn ngữ
            CustomerLoginController controller = loader.getController();
            controller.updateInitialLanguage();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể chuyển đến trang đăng nhập: " + e.getMessage() :
                            "Cannot navigate to login page: " + e.getMessage()
            );
        }
    }

    @FXML
    private void openCustomerChangeScreen() {
        try {
            // Lưu trạng thái ngôn ngữ hiện tại
            LanguageManager.setVietnamese(isVietnamese);

            // Tải trang thông tin tài khoản
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomerChange.fxml"));
            Parent root = loader.load();

            // Hiển thị scene mới
            Scene scene = new Scene(root);
            Stage stage = (Stage) ordersTable.getScene().getWindow();
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Thông tin tài khoản" : "CELLCOMP STORE - Account Information");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở trang thông tin tài khoản: " + e.getMessage() :
                            "Cannot open account information page: " + e.getMessage()
            );
        }
    }

    @FXML
    private void openCustomDesignScreen() {
        // Phương thức này sẽ được triển khai sau khi có màn hình thiết kế PC
        AlertUtils.showInfo(
                isVietnamese ? "Thông báo" : "Information",
                isVietnamese ? "Tính năng đang được phát triển" : "This feature is under development"
        );
    }

    @FXML
    private void switchLanguage() {
        // Đảo ngược trạng thái ngôn ngữ
        isVietnamese = !isVietnamese;
        LanguageManager.setVietnamese(isVietnamese);

        // Cập nhật ngôn ngữ giao diện
        updateLanguage();

        // Cập nhật bảng để hiển thị lại ngôn ngữ mới
        setupTable();
        loadOrders();
    }

    private void updateLanguage() {
        // Cập nhật tiêu đề và nhãn
        historyTitle.setText(isVietnamese ? "LỊCH SỬ MUA HÀNG" : "ORDER HISTORY");
        historyTitleHeading.setText(isVietnamese ? "Lịch sử mua hàng" : "Order History");

        // Cập nhật menu người dùng
        if (accountInfoMenuItem != null) {
            accountInfoMenuItem.setText(isVietnamese ? "Thông tin tài khoản" : "Account Information");
        }

        if (languageSwitchMenuItem != null) {
            languageSwitchMenuItem.setText(isVietnamese ? "Switch to English" : "Chuyển sang Tiếng Việt");

            // Cập nhật biểu tượng cờ
            ImageView flagIcon = new ImageView();
            flagIcon.setFitHeight(16.0);
            flagIcon.setFitWidth(16.0);

            String flagPath = isVietnamese ?
                    "/com/example/stores/images/layout/flag_en.png" :
                    "/com/example/stores/images/layout/flag_vn.png";

            try {
                Image flagImage = new Image(getClass().getResourceAsStream(flagPath));
                flagIcon.setImage(flagImage);
                languageSwitchMenuItem.setGraphic(flagIcon);
            } catch (Exception e) {
                System.err.println("Cannot load flag image: " + e.getMessage());
            }
        }

        if (customDesignMenuItem != null) {
            customDesignMenuItem.setText(isVietnamese ? "Thiết kế máy tính theo ý bạn" : "Design your PC");
        }

        if (logoutMenuItem != null) {
            logoutMenuItem.setText(isVietnamese ? "Đăng xuất" : "Logout");
        }

        // Cập nhật tiêu đề cột
        orderIdColumn.setText(isVietnamese ? "Mã đơn hàng" : "Order ID");
        orderDateColumn.setText(isVietnamese ? "Ngày đặt" : "Order Date");
        totalAmountColumn.setText(isVietnamese ? "Tổng tiền" : "Total");
        statusColumn.setText(isVietnamese ? "Trạng thái" : "Status");
        recipientNameColumn.setText(isVietnamese ? "Người nhận" : "Recipient");
        actionColumn.setText(isVietnamese ? "Thao tác" : "Action");

        // ĐIỂM QUAN TRỌNG: Thêm kiểm tra null trước khi gọi getScene()
        if (ordersTable != null && ordersTable.getScene() != null) {
            HBox backButtonBox = (HBox) ordersTable.getScene().lookup("#backButton");
            if (backButtonBox != null) {
                for (javafx.scene.Node node : backButtonBox.getChildren()) {
                    if (node instanceof Label) {
                        ((Label) node).setText(isVietnamese ? "Quay lại" : "Back");
                    }
                }
            }
        }

        // Cập nhật nút quay lại
        HBox backButtonBox = (HBox) ordersTable.getScene().lookup("#backButton");
        if (backButtonBox != null) {
            for (javafx.scene.Node node : backButtonBox.getChildren()) {
                if (node instanceof Label) {
                    ((Label) node).setText(isVietnamese ? "Quay lại" : "Back");
                }
            }
        }
    }

}