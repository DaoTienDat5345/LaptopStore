package com.example.stores.controller;

import com.example.stores.repository.impl.ProductRepository;
import com.example.stores.service.impl.AuthService;
import com.example.stores.service.impl.CartService;
import com.example.stores.util.AlertUtils;
import com.example.stores.util.LanguageManager;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CartController implements Initializable {

    // UI elements for Cart Info Section
    @FXML private Label cartIdLabel;
    @FXML private Label customerNameLabel;
    @FXML private Label createdDateLabel;
    @FXML private Label itemCountLabel;
    @FXML private Label totalProductsLabel;
    @FXML private Label cartTitleLabel;

    // UI elements for cart items table
    @FXML private TableView<CartItem> cartItemTableView;
    @FXML private TableColumn<CartItem, Boolean> selectColumn;
    @FXML private TableColumn<CartItem, Integer> cartItemIdColumn;
    @FXML private TableColumn<CartItem, String> productNameColumn;
    @FXML private TableColumn<CartItem, Double> priceColumn;
    @FXML private TableColumn<CartItem, Integer> quantityColumn;
    @FXML private TableColumn<CartItem, Double> subtotalColumn;
    @FXML private TableColumn<CartItem, String> addedDateColumn;
    @FXML private TableColumn<CartItem, Void> deleteColumn;

    // Other UI elements
    @FXML private Button backButton;
    @FXML private Label totalPriceLabel;
    @FXML private Button checkoutButton;
    @FXML private Button clearCartButton;

    // User menu elements
    @FXML private MenuButton userMenuButton;
    @FXML private MenuItem accountInfoMenuItem;
    @FXML private MenuItem languageSwitchMenuItem;
    @FXML private MenuItem customDesignMenuItem;
    @FXML private MenuItem orderHistoryMenuItem;
    @FXML private MenuItem logoutMenuItem;

    // Data objects
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private static final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private Cart currentCart;
    private Customer currentCustomer;

    // Biến theo dõi ngôn ngữ
    private boolean isVietnamese = LanguageManager.isVietnamese();

    // Services
    private CartService cartService;
    private AuthService authService; // Thêm biến AuthService

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo service
        cartService = new CartService();
        authService = new AuthService();

        // Tắt selection model để ngăn dòng bị tối màu khi click vào checkbox
        cartItemTableView.setSelectionModel(null);

        // Lấy khách hàng hiện tại đang đăng nhập
        currentCustomer = AuthService.getCurrentCustomer();
        if (currentCustomer == null) {
            AlertUtils.showError("Lỗi", "Vui lòng đăng nhập để xem giỏ hàng");
            goBack();
            return;
        }

        // Tải dữ liệu giỏ hàng
        loadCartData();

        // Cấu hình các cột trong bảng
        setupTableColumns();

        // Thiết lập chức năng cho user menu
        setupUserMenu();

        // Cập nhật tổng số lượng và giá trị
        updateCartSummary();

        // Cập nhật ngôn ngữ hiển thị dựa trên cài đặt hiện tại
        updateLanguage();
    }
    /**
     * Thiết lập chức năng cho user menu
     */
    private void setupUserMenu() {
        // Thiết lập hình ảnh cờ dựa trên ngôn ngữ hiện tại
        if (languageSwitchMenuItem != null) {
            try {
                ImageView flagView = new ImageView();
                String flagImagePath = isVietnamese ?
                        "/com/example/stores/images/layout/flag_en.png" :
                        "/com/example/stores/images/layout/flag_vn.png";
                flagView.setImage(new Image(getClass().getResourceAsStream(flagImagePath)));
                flagView.setFitHeight(16.0);
                flagView.setFitWidth(16.0);
                languageSwitchMenuItem.setGraphic(flagView);

                // Cập nhật text menu item
                languageSwitchMenuItem.setText(isVietnamese ?
                        "Switch to English" : "Chuyển sang tiếng Việt");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Thiết lập các icon cho menu items
        try {
            // Icon thông tin tài khoản
            if (accountInfoMenuItem != null) {
                ImageView userIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/user_icon.png")));
                userIcon.setFitHeight(16.0);
                userIcon.setFitWidth(16.0);
                accountInfoMenuItem.setGraphic(userIcon);
                accountInfoMenuItem.setOnAction(e -> openCustomerChangeScreen());
            }

            // Icon thiết kế máy tính
            if (customDesignMenuItem != null) {
                ImageView pcDesignIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/pc_design_icon.png")));
                pcDesignIcon.setFitHeight(16.0);
                pcDesignIcon.setFitWidth(16.0);
                customDesignMenuItem.setGraphic(pcDesignIcon);
                customDesignMenuItem.setOnAction(e -> openCustomDesignScreen());
            }

            // Icon lịch sử mua hàng
            if (orderHistoryMenuItem != null) {
                ImageView historyIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/history_icon.png")));
                historyIcon.setFitHeight(16.0);
                historyIcon.setFitWidth(16.0);
                orderHistoryMenuItem.setGraphic(historyIcon);
                orderHistoryMenuItem.setOnAction(e -> openOrderHistoryScreen());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Chuyển đổi ngôn ngữ
     */
    @FXML
    private void switchLanguage() {
        isVietnamese = !isVietnamese;
        LanguageManager.setVietnamese(isVietnamese);

        // Cập nhật giao diện
        updateLanguage();

        // Thiết lập lại user menu với ngôn ngữ mới
        setupUserMenu();
    }

    /**
     * Cập nhật ngôn ngữ hiển thị
     */
    private void updateLanguage() {
        // Cập nhật tiêu đề giỏ hàng
        if (cartTitleLabel != null) {
            cartTitleLabel.setText(isVietnamese ? "Giỏ hàng" : "Shopping Cart");
        }

        // Cập nhật nhãn thông tin giỏ hàng
        GridPane infoGrid = (GridPane) cartIdLabel.getParent();
        if (infoGrid != null) {
            // Tìm và cập nhật các nhãn trong GridPane
            for (javafx.scene.Node node : infoGrid.getChildren()) {
                if (node instanceof Label && !(node.equals(cartIdLabel) ||
                        node.equals(customerNameLabel) || node.equals(createdDateLabel) ||
                        node.equals(itemCountLabel))) {

                    Label label = (Label) node;
                    String labelText = label.getText();

                    // Cập nhật text tùy theo nội dung hiện tại
                    if (labelText.contains("Mã giỏ hàng") || labelText.equals("Cart ID:")) {
                        label.setText(isVietnamese ? "Mã giỏ hàng:" : "Cart ID:");
                    } else if (labelText.contains("Khách hàng") || labelText.equals("Customer:")) {
                        label.setText(isVietnamese ? "Khách hàng:" : "Customer:");
                    } else if (labelText.contains("Ngày tạo") || labelText.equals("Created Date:")) {
                        label.setText(isVietnamese ? "Ngày tạo:" : "Created Date:");
                    } else if (labelText.contains("Số sản phẩm") || labelText.equals("Item Count:")) {
                        label.setText(isVietnamese ? "Số sản phẩm:" : "Item Count:");
                    }
                }
            }
        }

        // Cập nhật labels tổng kết
        HBox orderSummary = (HBox) totalPriceLabel.getParent().getParent();
        if (orderSummary != null) {
            for (javafx.scene.Node vbox : orderSummary.getChildren()) {
                if (vbox instanceof VBox) {
                    for (javafx.scene.Node node : ((VBox)vbox).getChildren()) {
                        if (node instanceof Label && !node.equals(totalPriceLabel) && !node.equals(totalProductsLabel)) {
                            Label label = (Label) node;
                            if (label.getText().contains("Tổng sản phẩm") || label.getText().equals("Total Products:")) {
                                label.setText(isVietnamese ? "Tổng sản phẩm:" : "Total Products:");
                            } else if (label.getText().contains("Tổng thanh toán") || label.getText().equals("Total Payment:")) {
                                label.setText(isVietnamese ? "Tổng thanh toán:" : "Total Payment:");
                            }
                        }
                    }
                }
            }
        }

        // Cập nhật các labels của sản phẩm
        if (itemCountLabel != null) {
            String count = itemCountLabel.getText().replaceAll("[^0-9]", "");
            itemCountLabel.setText(count + (isVietnamese ? " sản phẩm" : " items"));
        }

        if (totalProductsLabel != null) {
            String count = totalProductsLabel.getText().replaceAll("[^0-9]", "");
            totalProductsLabel.setText(count + (isVietnamese ? " sản phẩm" : " items"));
        }

        // Cập nhật các nhãn trong header
        if (languageSwitchMenuItem != null) {
            languageSwitchMenuItem.setText(isVietnamese ? "Switch to English" : "Chuyển sang tiếng Việt");
        }

        // Cập nhật menu items khác giữ nguyên...
        if (accountInfoMenuItem != null) {
            accountInfoMenuItem.setText(isVietnamese ? "Thông tin tài khoản" : "Account Information");
        }

        if (customDesignMenuItem != null) {
            customDesignMenuItem.setText(isVietnamese ? "Thiết kế máy tính theo ý bạn" : "Custom PC Design");
        }

        if (orderHistoryMenuItem != null) {
            orderHistoryMenuItem.setText(isVietnamese ? "Lịch sử mua hàng" : "Order History");
        }

        if (logoutMenuItem != null) {
            logoutMenuItem.setText(isVietnamese ? "Đăng xuất" : "Logout");
        }

        // Cập nhật các nút
        if (backButton != null) {
            backButton.getChildrenUnmodifiable().forEach(node -> {
                if (node instanceof HBox) {
                    ((HBox) node).getChildren().forEach(hboxChild -> {
                        if (hboxChild instanceof Label) {
                            ((Label) hboxChild).setText(isVietnamese ? "Quay lại" : "Back");
                        }
                    });
                }
            });
        }

        if (clearCartButton != null) {
            clearCartButton.setText(isVietnamese ? "Xóa giỏ hàng" : "Clear Cart");
        }

        if (checkoutButton != null) {
            checkoutButton.setText(isVietnamese ? "Đi đến thanh toán" : "Checkout");
        }

        // Cập nhật tiêu đề cột
        if (selectColumn != null) selectColumn.setText(isVietnamese ? "Chọn" : "Select");
        if (cartItemIdColumn != null) cartItemIdColumn.setText(isVietnamese ? "Mã item" : "Item ID");
        if (productNameColumn != null) productNameColumn.setText(isVietnamese ? "Tên sản phẩm" : "Product Name");
        if (priceColumn != null) priceColumn.setText(isVietnamese ? "Đơn giá" : "Price");
        if (quantityColumn != null) quantityColumn.setText(isVietnamese ? "Số lượng" : "Quantity");
        if (subtotalColumn != null) subtotalColumn.setText(isVietnamese ? "Thành tiền" : "Subtotal");
        if (addedDateColumn != null) addedDateColumn.setText(isVietnamese ? "Ngày thêm" : "Added Date");
        if (deleteColumn != null) deleteColumn.setText(isVietnamese ? "Xóa" : "Delete");
    }

    /**
     * Mở màn hình thông tin tài khoản
     */
    private void openCustomerChangeScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomerChange.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = (Stage) userMenuButton.getScene().getWindow();
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Thông tin tài khoản" : "CELLCOMP STORE - Account Information");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở giao diện thông tin tài khoản" : "Cannot open account information screen"
            );
        }
    }

    /**
     * Mở màn hình thiết kế máy tính
     */
    /**
     * Mở màn hình thiết kế máy tính
     */
    private void openCustomDesignScreen() {
        try {
            // Save current language setting
            LanguageManager.setVietnamese(isVietnamese);

            // Load the CustomPCBuilder.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomPCBuilder.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Get CSS if available
            URL cssUrl = getClass().getResource("/com/example/stores/css/CustomPCBuilder.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            // Get the current window and set new scene
            Stage stage = (Stage) userMenuButton.getScene().getWindow();
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Thiết kế máy tính" : "CELLCOMP STORE - PC Builder");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở giao diện thiết kế máy tính: " + e.getMessage() :
                            "Cannot open PC Builder interface: " + e.getMessage()
            );
        }
    }

    /**
     * Mở màn hình lịch sử mua hàng
     */
    private void openOrderHistoryScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/OrderHistory.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = (Stage) userMenuButton.getScene().getWindow();
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Lịch sử mua hàng" : "CELLCOMP STORE - Order History");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở giao diện lịch sử mua hàng" : "Cannot open order history screen"
            );
        }
    }

    /**
     * Xử lý đăng xuất
     */
    @FXML
    private void handleLogout() {
        try {
            // Đăng xuất khỏi hệ thống - sử dụng instance của AuthService
            authService.logout(); // Đã sửa từ gọi static sang gọi instance

            // Chuyển về trang đăng nhập
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomerLogin.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = (Stage) userMenuButton.getScene().getWindow();
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Đăng nhập" : "CELLCOMP STORE - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể đăng xuất" : "Cannot logout"
            );
        }
    }

    private void loadCartData() {
        // Lấy hoặc tạo giỏ hàng mới nếu chưa có
        currentCart = cartService.getOrCreateCart(currentCustomer);
        if (currentCart == null) {
            AlertUtils.showError("Lỗi", "Không thể tải giỏ hàng");
            return;
        }

        // Cập nhật thông tin giỏ hàng lên giao diện
        cartIdLabel.setText("CT" + String.format("%03d", currentCart.getCartId()));
        customerNameLabel.setText(currentCustomer.getFullName());
        createdDateLabel.setText(dateFormatter.format(currentCart.getCreatedAt()));

        // Lấy danh sách sản phẩm trong giỏ hàng
        List<CartItem> items = cartService.getCartItems(currentCart.getCartId());
        cartItems.clear();
        cartItems.addAll(items);
    }

    private void setupTableColumns() {
        // Đặt TableView là editable
        cartItemTableView.setEditable(true);

        // Select column - thay đổi cách triển khai
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectColumn.setCellFactory(column -> new TableCell<CartItem, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(event -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    // Đảo ngược trạng thái selected
                    item.setSelected(!item.isSelected());
                    updateCartSummary();
                    event.consume(); // Ngăn chặn sự kiện lan ra ngoài
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    CartItem cartItem = getTableView().getItems().get(getIndex());
                    checkBox.setSelected(cartItem.isSelected());
                    setGraphic(checkBox);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // CartItem ID column
        cartItemIdColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCartItemId()).asObject());

        // Product name column
        productNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct().getProductName()));

        // Price column
        priceColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getProduct().getPrice()).asObject());
        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(currencyFormatter.format(price));
                }
            }
        });

        // Quantity column - CẬP NHẬT KHI THAY ĐỔI SỐ LƯỢNG
        quantityColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        // Trong phương thức setupTableColumns(), chỉnh sửa phần quantityColumn.setCellFactory
        quantityColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setGraphic(null);
                } else {
                    // Lấy thông tin CartItem và sản phẩm
                    CartItem item = getTableView().getItems().get(getIndex());

                    // Lấy thông tin sản phẩm để kiểm tra tồn kho
                    String productId = item.getProduct().getProductID();
                    Product product = ProductRepository.getProductById(productId);

                    // Xác định số lượng tồn kho tối đa
                    int maxAvailable = product != null ? product.getQuantity() : 1;

                    // Tạo Spinner với giới hạn số lượng tồn kho
                    Spinner<Integer> spinner = new Spinner<>(1, maxAvailable, quantity);
                    spinner.getStyleClass().add("quantity-spinner");

                    // Tạo factory với giới hạn không vượt quá tồn kho
                    SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxAvailable, quantity);
                    spinner.setValueFactory(valueFactory);

                    // Hành vi khi người dùng nhập giá trị không hợp lệ trực tiếp
                    spinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
                        try {
                            int value = Integer.parseInt(newValue);
                            if (value > maxAvailable) {
                                Platform.runLater(() -> {
                                    spinner.getEditor().setText(String.valueOf(maxAvailable));
                                    AlertUtils.showWarning(
                                            isVietnamese ? "Giới hạn số lượng" : "Quantity limit",
                                            isVietnamese ?
                                                    "Sản phẩm " + item.getProduct().getProductName() + " chỉ có tối đa " + maxAvailable + " sản phẩm" :
                                                    "Only " + maxAvailable + " " + item.getProduct().getProductName() + " available"
                                    );
                                });
                            } else if (value < 1) {
                                Platform.runLater(() -> spinner.getEditor().setText("1"));
                            }
                        } catch (NumberFormatException e) {
                            // Nếu không phải số hợp lệ, đặt về giá trị hiện tại
                            Platform.runLater(() -> spinner.getEditor().setText(String.valueOf(quantity)));
                        }
                    });

                    // Xử lý khi người dùng thay đổi giá trị qua các nút tăng/giảm
                    spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                        if (newValue > maxAvailable) {
                            Platform.runLater(() -> {
                                spinner.getValueFactory().setValue(maxAvailable);
                                AlertUtils.showWarning(
                                        isVietnamese ? "Giới hạn số lượng" : "Quantity limit",
                                        isVietnamese ?
                                                "Sản phẩm " + item.getProduct().getProductName() + " chỉ có tối đa " + maxAvailable + " sản phẩm" :
                                                "Only " + maxAvailable + " " + item.getProduct().getProductName() + " available"
                                );
                            });
                        } else {
                            // Cập nhật số lượng trong CartItem và database
                            cartService.updateCartItemQuantity(item.getCartItemId(), newValue);
                            item.setQuantity(newValue);
                            updateCartSummary();
                        }
                    });

                    // Xử lý khi người dùng nhấn Enter sau khi nhập
                    spinner.getEditor().setOnAction(event -> {
                        try {
                            int value = Integer.parseInt(spinner.getEditor().getText());
                            if (value > maxAvailable) {
                                spinner.getValueFactory().setValue(maxAvailable);
                                AlertUtils.showWarning(
                                        isVietnamese ? "Giới hạn số lượng" : "Quantity limit",
                                        isVietnamese ?
                                                "Sản phẩm " + item.getProduct().getProductName() + " chỉ có tối đa " + maxAvailable + " sản phẩm" :
                                                "Only " + maxAvailable + " " + item.getProduct().getProductName() + " available"
                                );
                            } else if (value < 1) {
                                spinner.getValueFactory().setValue(1);
                            } else {
                                spinner.getValueFactory().setValue(value);
                            }
                        } catch (NumberFormatException e) {
                            spinner.getValueFactory().setValue(quantity);
                        }
                    });

                    setGraphic(spinner);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Subtotal column
        subtotalColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getSubtotal()).asObject());
        subtotalColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double subtotal, boolean empty) {
                super.updateItem(subtotal, empty);
                if (empty || subtotal == null) {
                    setText(null);
                } else {
                    setText(currencyFormatter.format(subtotal));
                }
            }
        });

        // Added date column
        addedDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(dateFormatter.format(cellData.getValue().getAddedAt())));

        // Delete column
        deleteColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Tạo ImageView cho icon thùng rác
                    ImageView trashIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/trash.png")));
                    trashIcon.setFitHeight(18);
                    trashIcon.setFitWidth(18);

                    // Tạo nút với icon
                    Button deleteButton = new Button();
                    deleteButton.setGraphic(trashIcon);
                    deleteButton.getStyleClass().add("delete-icon-button");
                    deleteButton.setTooltip(new Tooltip("Xóa sản phẩm"));

                    // Sự kiện xóa vẫn giữ nguyên
                    deleteButton.setOnAction(event -> {
                        CartItem cartItem = getTableView().getItems().get(getIndex());
                        removeFromCart(cartItem);
                    });

                    HBox buttonBox = new HBox(deleteButton);
                    buttonBox.setAlignment(Pos.CENTER);
                    setGraphic(buttonBox);
                }
            }
        });

        // Set items to table
        cartItemTableView.setItems(cartItems);

        // Add listener for changes
        cartItemTableView.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends CartItem> c) -> {
            updateCartSummary();
        });
    }

    private void removeFromCart(CartItem item) {
        if (cartService.removeCartItem(item.getCartItemId())) {
            cartItems.remove(item);
            updateCartSummary();
            AlertUtils.showInfo("Thành công", "Đã xóa sản phẩm khỏi giỏ hàng");
        } else {
            AlertUtils.showError("Lỗi", "Không thể xóa sản phẩm khỏi giỏ hàng");
        }
    }

    private void updateCartSummary() {
        int totalItems = cartItems.size();
        int totalSelectedItems = (int) cartItems.stream().filter(CartItem::isSelected).count();
        double totalPrice = cartItems.stream()
                .filter(CartItem::isSelected)
                .mapToDouble(CartItem::getSubtotal)
                .sum();

        itemCountLabel.setText(totalItems + " sản phẩm");
        totalProductsLabel.setText(totalSelectedItems + " sản phẩm");
        totalPriceLabel.setText(currencyFormatter.format(totalPrice));
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Home.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/stores/css/Home.css").toExternalForm());

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setTitle("CELLCOMP STORE - Trang chủ");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể quay lại trang chủ");
        }
    }

    @FXML
    private void clearCart() {
        if (cartItems.isEmpty()) {
            AlertUtils.showInfo("Thông báo", "Giỏ hàng đã trống");
            return;
        }

        boolean confirm = AlertUtils.showConfirm(
                "Xác nhận",
                "Bạn có chắc chắn muốn xóa tất cả sản phẩm trong giỏ hàng?",
                "Thao tác này không thể hoàn tác"
        );

        if (confirm) {
            if (cartService.clearCart(currentCart.getCartId())) {
                cartItems.clear();
                updateCartSummary();
                AlertUtils.showInfo("Thành công", "Đã xóa tất cả sản phẩm trong giỏ hàng");
            } else {
                AlertUtils.showError("Lỗi", "Không thể xóa giỏ hàng");
            }
        }
    }

    @FXML
    private void goToCheckout() {
        try {
            // Kiểm tra đăng nhập
            if (AuthService.getCurrentCustomer() == null) {
                boolean goToLogin = AlertUtils.showConfirm(
                        isVietnamese ? "Yêu cầu đăng nhập" : "Login required",
                        isVietnamese ? "Vui lòng đăng nhập để thanh toán" : "Please login to checkout",
                        isVietnamese ? "Bạn có muốn chuyển đến trang đăng nhập không?" : "Would you like to go to the login page?"
                );

                if (goToLogin) {
                    // Chuyển đến trang đăng nhập
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomerLogin.fxml"));
                    Parent root = loader.load();
                    Scene scene = new Scene(root);

                    Stage stage = (Stage) checkoutButton.getScene().getWindow();
                    stage.setTitle(isVietnamese ? "CELLCOMP STORE - Đăng nhập" : "CELLCOMP STORE - Login");
                    stage.setScene(scene);
                    stage.show();
                }
                return;
            }

            // Kiểm tra giỏ hàng có trống không
            if (cartItems == null || cartItems.isEmpty()) {
                AlertUtils.showWarning(
                        isVietnamese ? "Giỏ hàng trống" : "Cart is empty",
                        isVietnamese ? "Giỏ hàng của bạn đang trống" : "Your cart is empty"
                );
                return;
            }

            // Kiểm tra xem có sản phẩm nào được chọn không
            boolean anySelected = cartItems.stream().anyMatch(CartItem::isSelected);
            if (!anySelected) {
                AlertUtils.showWarning(
                        isVietnamese ? "Chưa chọn sản phẩm" : "No products selected",
                        isVietnamese ? "Vui lòng chọn ít nhất một sản phẩm để thanh toán" : "Please select at least one product to checkout"
                );
                return;
            }

            // Tạo danh sách các sản phẩm đã chọn
            List<CartItem> selectedItems = cartItems.stream()
                    .filter(CartItem::isSelected)
                    .collect(Collectors.toList());

            // THAY ĐỔI Ở ĐÂY: Đi thẳng đến Orders.fxml thay vì Checkout.fxml
            URL ordersUrl = getClass().getResource("/com/example/stores/view/Orders.fxml");
            if (ordersUrl == null) {
                throw new IOException("Cannot find Orders.fxml");
            }

            FXMLLoader loader = new FXMLLoader(ordersUrl);
            Parent root = loader.load();

            // Truyền danh sách sản phẩm đã chọn cho OrderController
            OrderController controller = loader.getController();
            controller.setCartItems(selectedItems);

            Scene scene = new Scene(root);

            URL cssUrl = getClass().getResource("/com/example/stores/css/Order.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = (Stage) checkoutButton.getScene().getWindow();
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Đơn hàng" : "CELLCOMP STORE - Order");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể chuyển đến trang đơn hàng: " + e.getMessage() :
                            "Cannot navigate to order page: " + e.getMessage()
            );
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi không xác định" : "Unknown error",
                    e.getMessage()
            );
        }
    }
}