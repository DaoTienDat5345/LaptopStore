package com.example.stores.controller;

import com.example.stores.model.*;
import com.example.stores.service.impl.AuthService;
import com.example.stores.service.impl.CartService;
import com.example.stores.service.impl.OrderService;
import com.example.stores.service.impl.PaymentService;
import com.example.stores.ui.PaymentMethodDialog;
import com.example.stores.ui.PaymentQRDialog;
import com.example.stores.util.AlertUtils;
import com.example.stores.util.LanguageManager;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class OrderController implements Initializable {

    // FXML Elements
    @FXML private Label orderIdLabel;
    @FXML private Label createdDateLabel;
    @FXML private TextField phoneField;
    @FXML
    private Label phoneErrorLabel;
    @FXML private TextField recipientNameField;
    @FXML private ComboBox<String> provinceComboBox;
    @FXML private ComboBox<String> districtComboBox;
    @FXML private ComboBox<String> wardComboBox;
    @FXML private TextField addressDetailField;
    @FXML private TableView<OrderDetail> orderItemsTableView;
    @FXML private TableColumn<OrderDetail, String> productNameColumn;
    @FXML private TableColumn<OrderDetail, Double> priceColumn;
    @FXML private TableColumn<OrderDetail, Integer> quantityColumn;
    @FXML private TableColumn<OrderDetail, String> warrantyTypeColumn;
    @FXML private TableColumn<OrderDetail, Double> subtotalColumn;
    @FXML private Label totalProductsLabel;
    @FXML private Label shippingFeeLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Button paymentButton;
    @FXML private MenuButton userMenuButton;
    @FXML private Label orderIdTitleLabel;
    @FXML private Label createdDateTitleLabel;
    @FXML private Label phoneNumberTitleLabel;
    @FXML private Label recipientNameTitleLabel;
    @FXML private Label addressTitleLabel;
    @FXML private Label orderTitleHeading;
    // Language related
    private boolean isVietnamese;
    // Thêm các biến cho các nhãn tổng kết
    @FXML private Label totalProductsTextLabel;
    @FXML private Label shippingFeeTextLabel;
    @FXML private Label totalPaymentTextLabel;
    @FXML private Label customerNameLabel;   // Không khớp với FXML
    @FXML private Label customerPhoneLabel;
    @FXML private MenuItem customDesignMenuItem;
    @FXML private MenuItem orderHistoryMenuItem;
    @FXML private MenuItem logoutMenuItem;

    // Services
    private OrderService orderService;
    private CartService cartService;
    private AuthService authService;
    @FXML private TextField notesField;
    @FXML private MenuItem accountInfoMenuItem;

    // Data members
    private Customer currentCustomer;
    private List<CartItem> selectedCartItems;
    private ObservableList<OrderDetail> orderDetails;
    private Order currentOrder;
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Initial data for ComboBox
    private final String[] provinces = {"Hồ Chí Minh", "Hà Nội", "Đà Nẵng", "Cần Thơ", "Hải Phòng", "Nha Trang"};
    private final String[] districts = {"Quận 1", "Quận 2", "Quận 3", "Quận 4", "Quận 5", "Quận 6"};
    private final String[] wards = {"Phường 1", "Phường 2", "Phường 3", "Phường 4", "Phường 5", "Phường 6"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        orderService = new OrderService();
        cartService = new CartService();
        authService = new AuthService();

        // Get language setting
        isVietnamese = LanguageManager.isVietnamese();

        // Get current customer
        currentCustomer = AuthService.getCurrentCustomer();
        if (currentCustomer == null) {
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Vui lòng đăng nhập để tiếp tục" : "Please login to continue"
            );
            goBack();
            return;
        }

        if (accountInfoMenuItem != null) {
            accountInfoMenuItem.setOnAction(e -> openCustomerChangeScreen());
        }
        if (customDesignMenuItem != null) {
            customDesignMenuItem.setOnAction(e -> openCustomDesignScreen());
        }

        if (orderHistoryMenuItem != null) {
            orderHistoryMenuItem.setOnAction(e -> openOrderHistoryScreen());
        }

        if (logoutMenuItem != null) {
            logoutMenuItem.setOnAction(e -> handleLogout());
        }

        // Initialize order details list
        orderDetails = FXCollections.observableArrayList();

        // Set up address dropdowns
        setupAddressComboBoxes();

        // Set up table columns
        setupTableColumns();
        // Tạo phoneErrorLabel nếu chưa được tạo trong FXML
        if (phoneErrorLabel == null) {
            phoneErrorLabel = new Label();
            phoneErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        }

        // Đặt phoneErrorLabel dưới phoneField
        GridPane gridPane = (GridPane) phoneField.getParent();
        if (gridPane != null) {
            Integer colIndex = GridPane.getColumnIndex(phoneField);
            Integer rowIndex = GridPane.getRowIndex(phoneField);

            if (colIndex != null && rowIndex != null) {
                // Đảm bảo có đủ dòng
                while (gridPane.getRowConstraints().size() <= rowIndex + 1) {
                    gridPane.getRowConstraints().add(new RowConstraints(20));
                }

                // Xóa phoneErrorLabel khỏi parent cũ nếu có
                Parent oldParent = phoneErrorLabel.getParent();
                if (oldParent instanceof Pane) {
                    ((Pane) oldParent).getChildren().remove(phoneErrorLabel);
                }

                // Thêm vào vị trí mới
                gridPane.add(phoneErrorLabel, colIndex, rowIndex + 1);
                System.out.println("DEBUG: Added phoneErrorLabel at column " + colIndex + ", row " + (rowIndex + 1));
            }
        }
        createAndPositionPhoneErrorLabel();
        // Set up phone validatio
        setupPhoneValidation();

        // Set up initial data
        setupInitialData();

        // Update UI language
        updateLanguage();
    }

    private void setupAddressComboBoxes() {
        // Maps to store the relationship between provinces, districts and wards
        Map<String, List<String>> provinceToDistricts = new HashMap<>();
        Map<String, List<String>> districtToWards = new HashMap<>();

        // 63 provinces of Vietnam (same as in CustomerRegisterController)
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

        // Populate ComboBox with provinces
        provinceComboBox.setItems(FXCollections.observableArrayList(provinces));

        // Initialize sample district and ward data (same as in CustomerRegisterController)
        initSampleDistrictsAndWards(provinceToDistricts, districtToWards);

        // Set default province selection
        if (!provinces.isEmpty()) {
            provinceComboBox.setValue(provinces.get(1)); // Default to HCMC
        }

        // Add listeners for cascading updates
        provinceComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                List<String> districts = provinceToDistricts.getOrDefault(newVal, new ArrayList<>());
                districtComboBox.setItems(FXCollections.observableArrayList(districts));
                districtComboBox.getSelectionModel().selectFirst();

                // Clear ward selection
                wardComboBox.getItems().clear();
            }
        });

        districtComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                List<String> wards = districtToWards.getOrDefault(newVal, new ArrayList<>());
                wardComboBox.setItems(FXCollections.observableArrayList(wards));
                wardComboBox.getSelectionModel().selectFirst();
            }
        });

        // Initialize with default values
        if (provinceComboBox.getValue() != null) {
            List<String> districts = provinceToDistricts.getOrDefault(provinceComboBox.getValue(), new ArrayList<>());
            districtComboBox.setItems(FXCollections.observableArrayList(districts));
            districtComboBox.getSelectionModel().selectFirst();
        }
    }

    // Add this new helper method to initialize sample districts and wards
    private void initSampleDistrictsAndWards(Map<String, List<String>> provinceToDistricts, Map<String, List<String>> districtToWards) {
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
                    List<String> wards = new ArrayList<>();
                    for (int i = 1; i <= 5; i++) {
                        wards.add("Phường " + i);
                    }
                    districtToWards.put(district, wards);
                }
            }
        }

        // Generate sample districts for remaining provinces
        for (String province : Arrays.asList("Hải Phòng", "Cần Thơ", "An Giang", "Bà Rịa - Vũng Tàu")) {
            if (!provinceToDistricts.containsKey(province)) {
                List<String> districts = new ArrayList<>();
                for (int i = 1; i <= 5; i++) {
                    districts.add("Quận/Huyện " + i);
                }
                provinceToDistricts.put(province, districts);
            }
        }
    }

    private void setupTableColumns() {
        // Setup product name column
        productNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct().getProductName()));

        // Setup price column
        priceColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getUnitPrice()).asObject());
        priceColumn.setCellFactory(column -> new TableCell<OrderDetail, Double>() {
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

        // Setup quantity column - sửa để giống với Cart
        quantityColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());

        quantityColumn.setCellFactory(column -> new TableCell<OrderDetail, Integer>() {
            private Spinner<Integer> spinner;

            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);

                if (empty || quantity == null) {
                    setGraphic(null);
                } else {
                    // Lấy thông tin OrderDetail và sản phẩm
                    OrderDetail detail = getTableView().getItems().get(getIndex());

                    // Xác định số lượng tồn kho tối đa (giả sử là 10 nếu không có thông tin)
                    int maxAvailable = 10;
                    if (detail.getProduct() != null) {
                        maxAvailable = Math.max(10, detail.getProduct().getQuantity());
                    }

                    // Tạo Spinner với giới hạn số lượng tồn kho
                    spinner = new Spinner<>(1, maxAvailable, quantity);
                    spinner.getStyleClass().add("quantity-spinner");

                    // Thiết lập kích thước và style
                    spinner.setPrefWidth(80);
                    spinner.setMaxWidth(80);
                    spinner.setMinWidth(80);

                    // Style cho spinner để giống với CartController
                    spinner.setStyle("-fx-background-color: white; -fx-border-color: #7B68EE; -fx-border-radius: 4px;");

                    // Custom style cho nút tăng/giảm
                    // Truy cập các node con để thiết lập style
                    Button incrementButton = null;
                    Button decrementButton = null;

                    for (Node node : spinner.getChildrenUnmodifiable()) {
                        if (node instanceof HBox) {
                            for (Node child : ((HBox) node).getChildren()) {
                                if (child instanceof VBox) {
                                    VBox vbox = (VBox) child;
                                    if (vbox.getChildren().size() == 2) {
                                        incrementButton = (Button) vbox.getChildren().get(0);
                                        decrementButton = (Button) vbox.getChildren().get(1);
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (incrementButton != null) {
                        incrementButton.setStyle("-fx-background-color: #7B68EE; -fx-text-fill: white;");
                    }

                    if (decrementButton != null) {
                        decrementButton.setStyle("-fx-background-color: #7B68EE; -fx-text-fill: white;");
                    }

                    // Xử lý khi giá trị thay đổi
                    spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                        // Cập nhật số lượng trong OrderDetail
                        detail.setQuantity(newValue);

                        // Cập nhật giá trị subtotal của detail
                        detail.updateSubtotal();

                        // Cập nhật tổng tiền đơn hàng
                        updateOrderTotal();
                        setupWarrantyTypeColumn();

                        // Refresh table để cập nhật giá trị subtotal
                        getTableView().refresh();
                    });

                    setGraphic(spinner);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Setup warranty type column
        warrantyTypeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getWarrantyType()));

        // Thay đổi cách tạo ComboBox để đảm bảo text luôn hiển thị rõ ràng
        ObservableList<String> warrantyTypes =
                FXCollections.observableArrayList(isVietnamese ? "Thường" : "Standard",
                        isVietnamese ? "Vàng" : "Gold");

        warrantyTypeColumn.setCellFactory(column -> {
            // Tạo cell với ComboBox, nhưng tùy chỉnh style
            ComboBoxTableCell<OrderDetail, String> cell = new ComboBoxTableCell<>(warrantyTypes) {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    // Đảm bảo text luôn đen dù cell có được chọn hay không
                    setStyle("-fx-text-fill: black;");

                    // Điều chỉnh màu nền khi được chọn để dễ nhận biết
                    if (isSelected()) {
                        setStyle("-fx-background-color: #E8E8E8; -fx-text-fill: black;");
                    }
                }
            };

            // Ghi đè phương thức updateSelected để đảm bảo text vẫn hiển thị rõ khi được chọn
            cell.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    cell.setStyle("-fx-background-color: #E8E8E8; -fx-text-fill: black;");
                } else {
                    cell.setStyle("-fx-text-fill: black;");
                }
            });

            // Thêm tooltip để giải thích về giá bảo hành
            Tooltip tooltip = new Tooltip(isVietnamese ?
                    "Bảo hành Thường: Miễn phí\n" +
                            "Bảo hành Vàng: \n" +
                            "  - Nhóm sản phẩm cao cấp: +1.000.000đ/sản phẩm\n" +
                            "  - Nhóm sản phẩm thông thường: +500.000đ/sản phẩm" :
                    "Standard Warranty: Free\n" +
                            "Gold Warranty: \n" +
                            "  - Premium product group: +1,000,000 VND/item\n" +
                            "  - Standard product group: +500,000 VND/item");

            Tooltip.install(cell, tooltip);

            return cell;
        });

        // Cải thiện phương thức xử lý khi thay đổi loại bảo hành
        warrantyTypeColumn.setOnEditCommit(event -> {
            OrderDetail detail = event.getRowValue();
            String oldType = detail.getWarrantyType();
            String newType = event.getNewValue();

            System.out.println("Thay đổi loại bảo hành: " + oldType + " -> " + newType);

            // Cập nhật loại bảo hành mới
            detail.setWarrantyType(newType);

            // Force tính toán lại giá bảo hành
            detail.updateWarrantyPrice();

            // Hiển thị thông báo nếu có thay đổi về giá
            if (oldType != null && !oldType.equals(newType) && detail.getWarrantyPrice() > 0) {
                String message = isVietnamese ?
                        "Bảo hành Vàng cho sản phẩm này sẽ tính thêm " +
                                currencyFormatter.format(detail.getWarrantyPrice()) :
                        "Gold warranty for this product adds " +
                                currencyFormatter.format(detail.getWarrantyPrice());

                AlertUtils.showInfo(
                        isVietnamese ? "Thông tin bảo hành" : "Warranty Information",
                        message
                );
            }

            // Cập nhật tổng tiền đơn hàng - buộc phải tính lại
            updateOrderTotal();

            // QUAN TRỌNG: Cập nhật bảng để hiển thị đúng giá trị mới
            Platform.runLater(() -> {
                orderItemsTableView.refresh();
                // Force update UI một lần nữa sau 100ms
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(() -> {
                                    orderItemsTableView.refresh();
                                    updateOrderTotal();
                                });
                            }
                        }, 100
                );
            });
        });

        // Enable editing for warranty type
        orderItemsTableView.setEditable(true);
        warrantyTypeColumn.setEditable(true);

        // Setup subtotal column
        subtotalColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getSubtotal()).asObject());
        subtotalColumn.setCellFactory(column -> new TableCell<OrderDetail, Double>() {
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
    }

    private void createAndPositionPhoneErrorLabel() {
        // Tạo label mới nếu chưa có
        if (phoneErrorLabel == null) {
            phoneErrorLabel = new Label();
            phoneErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px; -fx-padding: 6 0 0 0;");
            phoneErrorLabel.setVisible(false);
            phoneErrorLabel.setManaged(false);

            // Đặt vào vị trí thích hợp trong layout
            Node parent = phoneField.getParent();

            if (parent instanceof GridPane) {
                GridPane grid = (GridPane) parent;

                // Lấy vị trí hiện tại của phoneField
                Integer colIndex = GridPane.getColumnIndex(phoneField);
                Integer rowIndex = GridPane.getRowIndex(phoneField);

                if (colIndex != null && rowIndex != null) {
                    // Tạo dòng mới cho thông báo lỗi với kích thước nhỏ hơn
                    RowConstraints errorRowConstraints = new RowConstraints();
                    errorRowConstraints.setPrefHeight(16); // Giảm chiều cao xuống
                    errorRowConstraints.setMinHeight(16);
                    errorRowConstraints.setMaxHeight(16);

                    // Điều chỉnh GridPane để có thêm không gian
                    while (grid.getRowConstraints().size() <= rowIndex + 1) {
                        grid.getRowConstraints().add(new RowConstraints());
                    }

                    // Thay thế constraints tại vị trí cần thiết
                    grid.getRowConstraints().set(rowIndex + 1, errorRowConstraints);

                    // Thêm label lỗi vào dòng dưới phoneField
                    grid.add(phoneErrorLabel, colIndex, rowIndex + 1);

                    // Điều chỉnh vị trí của các phần tử phía dưới nếu cần
                    for (Node node : grid.getChildren()) {
                        Integer nodeRow = GridPane.getRowIndex(node);
                        if (nodeRow != null && nodeRow > rowIndex + 1) {
                            GridPane.setRowIndex(node, nodeRow + 1);
                        }
                    }

                    System.out.println("DEBUG: Đã thêm phoneErrorLabel ở cột " + colIndex + ", dòng " + (rowIndex + 1));
                }
            }
        }
    }

    /**
     * Tách địa chỉ đầy đủ thành các thành phần và đặt vào các trường tương ứng
     */
    private void parseAndSetAddress(String fullAddress) {
        if (fullAddress == null || fullAddress.isEmpty()) {
            return;
        }

        // Tách thành các phần dựa trên dấu phẩy
        String[] parts = fullAddress.split(",");

        // Đảm bảo có ít nhất một phần
        if (parts.length > 0) {
            // Đặt phần đầu tiên vào trường địa chỉ chi tiết (chỉ số nhà, đường phố)
            addressDetailField.setText(parts[0].trim());

            // Đặt Phường/Xã
            if (parts.length > 1) {
                String ward = parts[1].trim();
                if (wardComboBox.getItems().contains(ward)) {
                    wardComboBox.setValue(ward);
                }
            }

            // Đặt Quận/Huyện
            if (parts.length > 2) {
                String district = parts[2].trim();
                if (districtComboBox.getItems().contains(district)) {
                    districtComboBox.setValue(district);
                }
            }

            // Đặt Tỉnh/Thành phố
            if (parts.length > 3) {
                String province = parts[3].trim();
                if (provinceComboBox.getItems().contains(province)) {
                    provinceComboBox.setValue(province);
                }
            }
        }
    }

    private void setupInitialData() {
        // Set current date
        createdDateLabel.setText(dateFormatter.format(LocalDateTime.now()));

        // Generate a temporary order ID
        orderIdLabel.setText("Tạo tự động sau khi đặt hàng");

        // Set customer info
        if (currentCustomer != null) {
            // Set contact info
            phoneField.setText(currentCustomer.getPhone());
            recipientNameField.setText(currentCustomer.getFullName());

            // Set address if available
            if (currentCustomer.getAddress() != null && !currentCustomer.getAddress().isEmpty()) {
                parseAndSetAddress(currentCustomer.getAddress());
            }
        }

        // Default shipping fee
        double shippingFee = 30000; // 30,000 VND
        shippingFeeLabel.setText(currencyFormatter.format(shippingFee));
    }

    /**
     * Set selected cart items to be processed as an order
     * @param selectedItems The items selected from cart
     */
    public void setCartItems(List<CartItem> selectedItems) {
        this.selectedCartItems = new ArrayList<>(selectedItems);

        if (selectedItems == null || selectedItems.isEmpty()) {
            AlertUtils.showWarning(
                    isVietnamese ? "Cảnh báo" : "Warning",
                    isVietnamese ? "Không có sản phẩm nào được chọn" : "No products selected"
            );
            goBack();
            return;
        }

        // Create order details from cart items
        orderDetails.clear();
        for (CartItem item : selectedItems) {
            OrderDetail detail = new OrderDetail(item);

            // Gán giá trị mặc định là "Thường" - giá trị lưu trữ luôn là tiếng Việt
            detail.setWarrantyType("Thường");

            // Đảm bảo tính toán đúng giá bảo hành khi khởi tạo
            detail.updateWarrantyPrice();
            orderDetails.add(detail);
        }

        // Set table items
        orderItemsTableView.setItems(orderDetails);

        // Update totals
        updateOrderTotal();

        // Cập nhật hiển thị warranty type theo ngôn ngữ hiện tại
        Platform.runLater(() -> {
            updateWarrantyTypeDisplay();
        });
    }
    /**
     * Update the order total based on current items and warranty selections
     */
    private void updateOrderTotal() {
        int totalProducts = 0;
        double totalAmount = 0;
        double warrantyTotal = 0;

        for (OrderDetail detail : orderDetails) {
            totalProducts += detail.getQuantity();

            // Đảm bảo giá bảo hành đã được tính đúng
            if ("Vàng".equals(detail.getWarrantyType()) || "Gold".equals(detail.getWarrantyType())) {
                detail.updateWarrantyPrice();
                warrantyTotal += detail.getWarrantyPrice();
            }

            // Force tính lại subtotal mỗi khi số lượng thay đổi
            detail.updateSubtotal();
            totalAmount += detail.getSubtotal();
        }

        // Add shipping fee
        double shippingFee = 30000; // 30,000 VND
        totalAmount += shippingFee;

        // Cập nhật hiển thị
        totalProductsLabel.setText(totalProducts + (isVietnamese ? " sản phẩm" : " items"));
        shippingFeeLabel.setText(currencyFormatter.format(shippingFee));
        totalPriceLabel.setText(currencyFormatter.format(totalAmount));
        totalPriceLabel.setStyle("-fx-text-fill: #865DFF; -fx-font-weight: bold;");

        // Cập nhật order object nếu có
        if (currentOrder != null) {
            currentOrder.setTotalAmount(totalAmount);
        }
    }

    @FXML
    private void processPayment() {
        // Xác thực đầu vào
        if (!validateOrderInput()) {
            return;
        }

        // Kiểm tra riêng số điện thoại
        String phone = phoneField.getText();
        if (!validatePhone(phone)) {
            // Focus vào trường số điện thoại nếu không hợp lệ
            phoneField.requestFocus();
            return;
        }

        try {
            // Hiển thị dialog chọn phương thức thanh toán
            PaymentMethodDialog paymentMethodDialog = new PaymentMethodDialog(isVietnamese);
            String paymentMethod = paymentMethodDialog.showAndWait().orElse(null);

            if (paymentMethod == null) {
                return; // Người dùng đã hủy
            }

            // Nếu chọn COD (Thanh toán khi nhận hàng)
            if ("cod".equals(paymentMethod)) {
                createAndProcessOrder("COD");
                return;
            }

            // Tạo yêu cầu thanh toán qua QR
            createAndProcessQRPayment(paymentMethod);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Có lỗi xảy ra: " + e.getMessage() :
                            "An error occurred: " + e.getMessage()
            );
        }
    }
    // Hàm xác thực số điện thoại
    private boolean validatePhone(String phone) {
        boolean isValid = true;

        // Kiểm tra rỗng
        if (phone == null || phone.trim().isEmpty()) {
            phoneErrorLabel.setText(isVietnamese ?
                    "Vui lòng nhập số điện thoại" :
                    "Please enter a phone number");
            phoneErrorLabel.setVisible(true);
            phoneErrorLabel.setManaged(true);
            isValid = false;
        }
        // Kiểm tra đầu số trước
        else if (!(phone.startsWith("03") || phone.startsWith("07") || phone.startsWith("09"))) {
            // Sai đầu số
            phoneErrorLabel.setText(isVietnamese ?
                    "SĐT phải bắt đầu từ 03, 07, 09" :
                    "Number must start with 03, 07, 09");
            phoneErrorLabel.setVisible(true);
            phoneErrorLabel.setManaged(true);
            isValid = false;
        }
        // Kiểm tra độ dài
        else if (!phone.matches("^(03|07|09)\\d{8}$")) {
            // Đúng đầu số, nhưng chưa đủ 10 số
            int remaining = 10 - phone.length();
            phoneErrorLabel.setText(isVietnamese ?
                    "Vui lòng nhập thêm " + remaining + " số nữa" :
                    "Please enter " + remaining + " more digits");
            phoneErrorLabel.setVisible(true);
            phoneErrorLabel.setManaged(true);
            isValid = false;
        }
        // Đúng định dạng
        else {
            phoneErrorLabel.setVisible(false);
            phoneErrorLabel.setManaged(false);
        }

        return isValid;
    }
    private void setupPhoneValidation() {
        if (phoneErrorLabel == null) {
            System.err.println("WARNING: phoneErrorLabel is null, cannot set up validation");
            return;
        }

        // Điều chỉnh vị trí của phoneErrorLabel
        VBox phoneContainer = new VBox(5); // spacing 5px

        // Lấy parent của phoneField (GridPane)
        Parent parent = phoneField.getParent();
        if (parent instanceof GridPane) {
            GridPane grid = (GridPane) parent;
            Integer colIndex = GridPane.getColumnIndex(phoneField);
            Integer rowIndex = GridPane.getRowIndex(phoneField);

            if (colIndex != null && rowIndex != null) {
                // Xóa phoneField khỏi grid
                grid.getChildren().remove(phoneField);

                // Tạo container chứa cả phoneField và phoneErrorLabel
                phoneContainer.getChildren().addAll(phoneField, phoneErrorLabel);

                // Thêm container vào grid
                grid.add(phoneContainer, colIndex, rowIndex);

                // Style cho phoneContainer
                phoneContainer.setAlignment(Pos.TOP_LEFT);
                phoneContainer.setPrefHeight(55); // Tăng chiều cao để có không gian cho error label
            }
        }

        // Ẩn label ban đầu
        phoneErrorLabel.setVisible(false);
        phoneErrorLabel.setManaged(false);

        // Thêm listener
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean valid = validatePhone(newValue);

            // Điều chỉnh hiển thị error label
            phoneErrorLabel.setTranslateY(-5); // Di chuyển lên trên một chút

            // Cập nhật nút thanh toán
            if (paymentButton != null) {
                paymentButton.setDisable(!valid);
            }
        });

        // Kiểm tra giá trị ban đầu
        if (phoneField.getText() != null) {
            validatePhone(phoneField.getText());
        }
    }
    private void createAndProcessQRPayment(String paymentMethod) {
        try {
            // Kiểm tra số điện thoại trước
            String phoneNumber = phoneField.getText().trim();
            if (phoneNumber.isEmpty() || !validatePhone(phoneNumber)) {
                phoneField.requestFocus();
                return;
            }

            // Tạo đơn hàng trong bộ nhớ (chưa lưu vào DB)
            String fullAddress = buildCompleteAddress();
            currentOrder = new Order();
            currentOrder.setOrderDate(LocalDateTime.now());
            currentOrder.setCustomerId(currentCustomer.getId());

            // Thiết lập số điện thoại - ĐẢM BẢO KHÔNG NULL
            currentOrder.setRecipientPhone(phoneNumber);
            System.out.println("DEBUG: Phone number set: " + phoneNumber);

            // Thiết lập các thuộc tính khác
            currentOrder.setRecipientName(recipientNameField.getText().trim());
            currentOrder.setShippingAddress(fullAddress);
            currentOrder.setPaymentMethod(paymentMethod.toUpperCase());
            currentOrder.setOrderDetails(new ArrayList<>(orderDetails));
            currentOrder.setShippingFee(30000);

            // THAY ĐỔI QUAN TRỌNG: Sử dụng trạng thái được phép trong ràng buộc CHECK
            currentOrder.setOrderStatus("Đã xác nhận"); // Thay vì "PAID"

            if (notesField != null) {
                currentOrder.setNotes(notesField.getText());
            }
            currentOrder.updateTotalAmount();

            // THAY ĐỔI: Sử dụng mã QR tùy chỉnh thay vì tạo mới
            PaymentService paymentService = new PaymentService();
            Image qrCodeImage = paymentService.getCustomPaymentQR(
                    paymentMethod,
                    currentOrder.getTotalAmount(),
                    "ORD" + System.currentTimeMillis() // Mã tạm thời để hiển thị
            );

            // Tạo mã đơn hàng tạm thời để hiển thị
            String tempDisplayOrderId = "ORD" + System.currentTimeMillis();

            // Hiển thị dialog QR với thông tin thanh toán
            PaymentQRDialog qrDialog = new PaymentQRDialog(
                    isVietnamese, paymentMethod, qrCodeImage,
                    currentOrder.getTotalAmount(), tempDisplayOrderId);

            Boolean isPaid = qrDialog.showAndWait().orElse(false);
            System.out.println("DEBUG: Payment result: " + (isPaid ? "PAID" : "CANCELLED"));

            if (isPaid) {
                try {
                    System.out.println("DEBUG: Creating order in database after payment...");

                    // Trực tiếp tạo đơn hàng trong DB qua repository để tránh các logic phức tạp trong service
                    Connection conn = null;
                    PreparedStatement stmt = null;
                    ResultSet rs = null;

                    try {
                        conn = DatabaseConnection.getConnection();

                        // SQL chèn trực tiếp với các giá trị được thiết lập rõ ràng
                        String sql = "INSERT INTO Orders (orderDate, totalAmount, customerID, " +
                                "employeeID, orderStatus, paymentMethod, recipientName, recipientPhone, " +
                                "shippingAddress, shippingFee, notes) VALUES " +
                                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                        stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                        stmt.setTimestamp(1, Timestamp.valueOf(currentOrder.getOrderDate()));
                        stmt.setDouble(2, currentOrder.getTotalAmount());
                        stmt.setInt(3, currentOrder.getCustomerId());
                        stmt.setNull(4, java.sql.Types.INTEGER); // employeeID

                        // THAY ĐỔI QUAN TRỌNG: Set trạng thái đúng với ràng buộc CHECK
                        stmt.setString(5, "Đã xác nhận");  // Thay vì "PAID"

                        stmt.setString(6, currentOrder.getPaymentMethod());
                        stmt.setString(7, currentOrder.getRecipientName());
                        stmt.setString(8, phoneNumber); // Đảm bảo không null
                        stmt.setString(9, currentOrder.getShippingAddress());
                        stmt.setDouble(10, currentOrder.getShippingFee());
                        stmt.setString(11, currentOrder.getNotes());

                        int rowsAffected = stmt.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("Đã chèn đơn hàng thành công!");

                            // Lấy ID đơn hàng được tạo từ trigger
                            rs = stmt.getGeneratedKeys();
                            String orderId = null;
                            if (rs.next()) {
                                orderId = rs.getString(1);
                                currentOrder.setOrderId(orderId);
                                System.out.println("Đã tạo đơn hàng với ID: " + orderId);

                                // Lưu chi tiết đơn hàng
                                boolean detailsSaved = saveOrderDetails(conn, currentOrder);
                                if (detailsSaved) {
                                    // Xóa giỏ hàng và điều hướng đến trang chi tiết đơn hàng
                                    finishOrder();
                                    return;
                                }
                            }
                        } else {
                            throw new Exception("Không thể chèn đơn hàng vào database");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new Exception("Lỗi SQL: " + e.getMessage());
                    } finally {
                        // Đóng tất cả các resource
                        if (rs != null) try { rs.close(); } catch (SQLException e) {}
                        if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
                        if (conn != null) try { conn.close(); } catch (SQLException e) {}
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    AlertUtils.showError(
                            isVietnamese ? "Lỗi" : "Error",
                            isVietnamese ? "Không thể lưu đơn hàng. Thanh toán đã thành công nhưng cần liên hệ CSKH." :
                                    "Could not save order. Payment successful but please contact support."
                    );
                }
            } else {
                // Người dùng đã hủy
                AlertUtils.showError(
                        isVietnamese ? "Hủy thanh toán" : "Payment Cancelled",
                        isVietnamese ? "Bạn đã hủy quá trình thanh toán" :
                                "You have cancelled the payment process"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi thanh toán" : "Payment Error",
                    isVietnamese ? "Có lỗi xảy ra khi xử lý thanh toán: " + e.getMessage() :
                            "An error occurred during payment processing: " + e.getMessage()
            );
        }
    }

    // Phương thức trợ giúp để lưu chi tiết đơn hàng
    private boolean saveOrderDetails(Connection conn, Order order) throws SQLException {
        if (order == null || order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            return false;
        }

        String sql = "INSERT INTO OrderDetails (orderID, productID, quantity, unitPrice, " +
                "warrantyType, warrantyPrice, note) VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);

            // In ra thông tin để debug
            System.out.println("DEBUG: Đang lưu " + order.getOrderDetails().size() + " chi tiết đơn hàng");

            for (OrderDetail detail : order.getOrderDetails()) {
                // *** BẮT BUỘC: Chuẩn hóa giá trị warrantyType thành tiếng Việt ***
                String warrantyType = detail.getWarrantyType();

                // Chuyển đổi từ giá trị tiếng Anh sang tiếng Việt nếu cần
                if (warrantyType == null || warrantyType.isEmpty()) {
                    warrantyType = "Thường";
                } else if (warrantyType.equals("Gold") || warrantyType.equalsIgnoreCase("Vàng")) {
                    warrantyType = "Vàng";
                } else if (warrantyType.equals("Standard") || warrantyType.equalsIgnoreCase("Thường")) {
                    warrantyType = "Thường";
                } else {
                    // Giá trị mặc định an toàn
                    warrantyType = "Thường";
                }
                detail.setWarrantyType(warrantyType);

                // Tính toán giá bảo hành nếu bảo hành vàng
                detail.updateWarrantyPrice();

                // Tính lại tổng tiền
                detail.updateSubtotal();

                System.out.println("DEBUG: Thêm sản phẩm " + detail.getProductId() +
                        " với loại bảo hành: " + detail.getWarrantyType());

                stmt.setString(1, order.getOrderId());
                stmt.setString(2, detail.getProductId());
                stmt.setInt(3, detail.getQuantity());
                stmt.setDouble(4, detail.getUnitPrice());
                stmt.setString(5, warrantyType);
                stmt.setDouble(6, detail.getWarrantyPrice());
                stmt.setString(7, detail.getNote());

                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            System.out.println("Đã lưu " + results.length + " chi tiết đơn hàng");
            return true;
        } finally {
            if (stmt != null) stmt.close();
        }
    }

    private void createAndProcessOrder(String paymentMethod) {
        try {
            // Kiểm tra và chuẩn hóa số điện thoại
            String phoneNumber = phoneField.getText().trim();

            if (phoneNumber.isEmpty()) {
                AlertUtils.showError(
                        isVietnamese ? "Lỗi" : "Error",
                        isVietnamese ? "Vui lòng nhập số điện thoại" : "Please enter a phone number"
                );
                phoneField.requestFocus();
                return;
            }

            // Tạo order với chỉ địa chỉ chi tiết, không ghép thêm
            String address = buildCompleteAddress();
            System.out.println("DEBUG: Địa chỉ đang được lưu: '" + address + "'");

            currentOrder = new Order();
            currentOrder.setOrderDate(LocalDateTime.now());
            currentOrder.setCustomerId(currentCustomer.getId());
            currentOrder.setRecipientName(recipientNameField.getText().trim());
            currentOrder.setRecipientPhone(phoneNumber);
            currentOrder.setShippingAddress(address);
            currentOrder.setPaymentMethod(paymentMethod);

            // Đảm bảo sử dụng orderDetails hiện tại với số lượng đã cập nhật
            currentOrder.setOrderDetails(new ArrayList<>(orderDetails));
            currentOrder.setShippingFee(30000);

            if (notesField != null) {
                currentOrder.setNotes(notesField.getText());
            }

            // Cập nhật lại tổng tiền trước khi lưu
            currentOrder.updateTotalAmount();

            // Lưu vào database
            currentOrder = orderService.createOrder(currentOrder);

            if (currentOrder != null && currentOrder.getOrderId() != null) {
                finishOrder();
            } else {
                AlertUtils.showError(
                        isVietnamese ? "Lỗi" : "Error",
                        isVietnamese ? "Không thể tạo đơn hàng" : "Could not create order"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String savedOrderId;
    private void finishOrder() {
        try {
            // Lưu thông tin đơn hàng để sử dụng cho định danh in ấn nếu cần
            savedOrderId = currentOrder.getOrderId();

            // Hiển thị cảnh báo thành công với lựa chọn phù hợp hơn
            Alert successAlert = new Alert(Alert.AlertType.CONFIRMATION);
            successAlert.setTitle(isVietnamese ? "Đặt hàng thành công" : "Order Successful");
            successAlert.setHeaderText(isVietnamese ? "Đơn hàng đã thanh toán thành công!" : "Order has been paid successfully!");
            successAlert.setContentText(isVietnamese ? "Bạn có muốn xem chi tiết hóa đơn không?" : "Would you like to view order details?");

            // Tùy chỉnh nút
            ButtonType yesButton = new ButtonType(isVietnamese ? "Có" : "Yes", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType(isVietnamese ? "Không" : "No", ButtonBar.ButtonData.NO);
            successAlert.getButtonTypes().setAll(yesButton, noButton);

            // Xử lý phản hồi với logic ngược lại so với trước đây
            Optional<ButtonType> result = successAlert.showAndWait();
            if (result.isPresent() && result.get() == yesButton) {
                // Nếu chọn "Có": Chuyển đến trang chi tiết đơn hàng
                showOrderDetails(currentOrder);
            } else {
                // Nếu chọn "Không": Chuyển về trang chủ
                goToHome();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể hoàn tất đơn hàng: " + e.getMessage() :
                            "Cannot complete order: " + e.getMessage()
            );
        }
    }

    // Thêm phương thức để chuyển đến trang chi tiết đơn hàng
    private void showOrderDetails(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/OrderDetails.fxml"));
            Parent root = loader.load();

            // Truyền thông tin đơn hàng và nguồn truy cập
            OrderDetailsController controller = loader.getController();
            controller.setOrder(order);
            controller.setAccessSource("new_order"); // Đánh dấu là từ đơn hàng mới
            controller.loadOrderDetails();

            Scene scene = new Scene(root);
            Stage stage = (Stage) recipientNameField.getScene().getWindow();
            stage.setTitle(isVietnamese ? "Chi tiết đơn hàng" : "Order Details");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validateOrderInput() {
        // Check phone number
        if (phoneField.getText() == null || phoneField.getText().trim().isEmpty()) {
            AlertUtils.showWarning(
                    isVietnamese ? "Thông tin thiếu" : "Missing Information",
                    isVietnamese ? "Vui lòng nhập số điện thoại" : "Please enter a phone number"
            );
            phoneField.requestFocus();
            return false;
        }

        // Validate Vietnamese phone number format (10 digits, starting with 03, 05, 07, 08, 09)
        if (!phoneField.getText().matches("^(03|05|07|08|09)\\d{8}$")) {
            AlertUtils.showWarning(
                    isVietnamese ? "Thông tin không hợp lệ" : "Invalid Information",
                    isVietnamese ?
                            "Số điện thoại không hợp lệ. Vui lòng nhập số điện thoại Việt Nam (10 số)" :
                            "Invalid phone number. Please enter a valid Vietnamese phone number (10 digits)"
            );
            phoneField.requestFocus();
            return false;
        }

        // Check recipient name
        if (recipientNameField.getText() == null || recipientNameField.getText().trim().isEmpty()) {
            AlertUtils.showWarning(
                    isVietnamese ? "Thông tin thiếu" : "Missing Information",
                    isVietnamese ? "Vui lòng nhập tên người nhận" : "Please enter recipient name"
            );
            recipientNameField.requestFocus();
            return false;
        }

        // Check address detail
        if (addressDetailField.getText() == null || addressDetailField.getText().trim().isEmpty()) {
            AlertUtils.showWarning(
                    isVietnamese ? "Thông tin thiếu" : "Missing Information",
                    isVietnamese ? "Vui lòng nhập địa chỉ chi tiết" : "Please enter detailed address"
            );
            addressDetailField.requestFocus();
            return false;
        }

        return true;
    }

    private String buildCompleteAddress() {
    // Thay đổi để ghép đầy đủ địa chỉ từ tất cả các phần
    StringBuilder address = new StringBuilder();

    // Thêm địa chỉ chi tiết (phần nhập văn bản)
    String addressDetail = addressDetailField.getText().trim();
    address.append(addressDetail);

    // Thêm phường/xã, quận/huyện, tỉnh/thành phố nếu được chọn
    if (wardComboBox.getValue() != null && !wardComboBox.getValue().isEmpty()) {
        address.append(", ").append(wardComboBox.getValue());
    }

    if (districtComboBox.getValue() != null && !districtComboBox.getValue().isEmpty()) {
        address.append(", ").append(districtComboBox.getValue());
    }

    if (provinceComboBox.getValue() != null && !provinceComboBox.getValue().isEmpty()) {
        address.append(", ").append(provinceComboBox.getValue());
    }

    return address.toString();
}

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Cart.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/stores/css/Cart.css").toExternalForm());

            Stage stage = (Stage) orderIdLabel.getScene().getWindow();
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

    private void goToOrderHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/OrderHistory.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/stores/css/OrderHistory.css").toExternalForm());

            Stage stage = (Stage) orderIdLabel.getScene().getWindow();
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Lịch sử đơn hàng" : "CELLCOMP STORE - Order History");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở trang lịch sử đơn hàng" : "Cannot open order history page"
            );
        }
    }


    @FXML
    private void switchLanguage() {
        isVietnamese = !isVietnamese;
        LanguageManager.setVietnamese(isVietnamese);

        // Cập nhật ngôn ngữ và các hiển thị
        updateLanguage();

        // Cập nhật hiển thị cột warranty type
        Platform.runLater(() -> {
            updateWarrantyTypeDisplay();

            // Cập nhật lại tổng tiền để đảm bảo hiển thị đúng định dạng tiền tệ
            updateOrderTotal();
        });
    }

    private void updateLanguage() {
        // Cập nhật tiêu đề chính của trang
        if (orderTitleHeading != null) {
            orderTitleHeading.setText(isVietnamese ? "Đơn hàng" : "Order");
        }

        // Cập nhật các nhãn thông tin đơn hàng
        if (orderIdTitleLabel != null)
            orderIdTitleLabel.setText(isVietnamese ? "Mã đơn hàng:" : "Order ID:");

        if (createdDateTitleLabel != null)
            createdDateTitleLabel.setText(isVietnamese ? "Ngày tạo:" : "Create Date:");

        if (phoneNumberTitleLabel != null)
            phoneNumberTitleLabel.setText(isVietnamese ? "Số điện thoại:" : "Phone Number:");

        if (recipientNameTitleLabel != null)
            recipientNameTitleLabel.setText(isVietnamese ? "Tên người nhận:" : "Recipient Name:");

        if (addressTitleLabel != null)
            addressTitleLabel.setText(isVietnamese ? "Địa chỉ" : "Address");

        // Cập nhật nội dung mặc định
        if (orderIdLabel != null && orderIdLabel.getText() != null &&
                (orderIdLabel.getText().contains("Tạo tự động") || orderIdLabel.getText().contains("Auto-generated"))) {
            orderIdLabel.setText(isVietnamese ? "Tạo tự động sau khi đặt hàng" : "Auto-generated after order placement");
        }

        // Cập nhật các nhãn tổng kết
        if (totalProductsTextLabel != null)
            totalProductsTextLabel.setText(isVietnamese ? "Tổng sản phẩm:" : "Total Products:");

        if (shippingFeeTextLabel != null)
            shippingFeeTextLabel.setText(isVietnamese ? "Phí vận chuyển:" : "Shipping Fee:");

        if (totalPaymentTextLabel != null)
            totalPaymentTextLabel.setText(isVietnamese ? "Tổng thanh toán:" : "Total Payment:");

        // Cập nhật tiêu đề cột bảng
        productNameColumn.setText(isVietnamese ? "Tên sản phẩm" : "Product Name");
        priceColumn.setText(isVietnamese ? "Giá sản phẩm" : "Price");
        quantityColumn.setText(isVietnamese ? "Số lượng" : "Quantity");
        warrantyTypeColumn.setText(isVietnamese ? "Loại bảo hành" : "Warranty Type");
        subtotalColumn.setText(isVietnamese ? "Thành tiền" : "Subtotal");
        setupWarrantyTypeColumn();
        paymentButton.setText(isVietnamese ? "Thanh toán" : "Payment");

        // Cập nhật các loại bảo hành trong ComboBox
        ObservableList<String> warrantyTypes = FXCollections.observableArrayList(
                isVietnamese ? "Thường" : "Standard",
                isVietnamese ? "Vàng" : "Gold"
        );
        warrantyTypeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(warrantyTypes));

        // Cập nhật nút
        paymentButton.setText(isVietnamese ? "Thanh toán" : "Payment");

        // Cập nhật placeholder text cho địa chỉ
        provinceComboBox.setPromptText(isVietnamese ? "Tỉnh/Thành phố" : "Province/City");
        districtComboBox.setPromptText(isVietnamese ? "Quận/Huyện" : "District");
        wardComboBox.setPromptText(isVietnamese ? "Phường/Xã" : "Ward");
        addressDetailField.setPromptText(isVietnamese ? "Số nhà, tên đường..." : "House number, street name...");

        // Cập nhật tổng sản phẩm
        if (totalProductsLabel != null && totalProductsLabel.getText() != null) {
            String text = totalProductsLabel.getText().replaceAll("\\D+", "");
            if (!text.isEmpty()) {
                int totalProducts = Integer.parseInt(text);
                totalProductsLabel.setText(totalProducts + (isVietnamese ? " sản phẩm" : " items"));
            }
        }

        // Cập nhật tiêu đề cửa sổ
        if (orderIdLabel != null && orderIdLabel.getScene() != null && orderIdLabel.getScene().getWindow() != null) {
            Stage stage = (Stage) orderIdLabel.getScene().getWindow();
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Đơn hàng" : "CELLCOMP STORE - Order");
        }

        // Cập nhật menu người dùng
        if (userMenuButton != null) {
            for (MenuItem item : userMenuButton.getItems()) {
                if (item.getText() != null) {
                    switch (item.getText()) {
                        case "Thông tin tài khoản":
                        case "Account Information":
                            item.setText(isVietnamese ? "Thông tin tài khoản" : "Account Information");
                            break;
                        case "Chuyển đổi ngôn ngữ":
                        case "Change Language":
                            // Cập nhật menu ngôn ngữ và biểu tượng cờ
                            // Logic mới: khi ở giao diện tiếng Việt thì hiển thị "Change Language" và cờ Anh
                            // Khi ở giao diện tiếng Anh thì hiển thị "Chuyển đổi ngôn ngữ" và cờ Việt
                            item.setText(isVietnamese ? "Change Language" : "Chuyển đổi ngôn ngữ");

                            // Cập nhật biểu tượng cờ tương ứng
                            ImageView flagIcon = new ImageView(new Image(
                                    getClass().getResourceAsStream(isVietnamese ?
                                            "/com/example/stores/images/layout/flag_en.png" :
                                            "/com/example/stores/images/layout/flag_vn.png")
                            ));
                            flagIcon.setFitWidth(16);
                            flagIcon.setFitHeight(16);
                            item.setGraphic(flagIcon);
                            break;
                        case "Thiết kế máy tính theo ý bạn":
                        case "Design Your Computer":
                            item.setText(isVietnamese ? "Thiết kế máy tính theo ý bạn" : "Design Your Computer");
                            break;
                        case "Lịch sử mua hàng":
                        case "Purchase History":
                            item.setText(isVietnamese ? "Lịch sử mua hàng" : "Purchase History");
                            break;
                        case "Đăng xuất":
                        case "Logout":
                            item.setText(isVietnamese ? "Đăng xuất" : "Logout");
                            break;
                    }
                }
            }
        }
        orderItemsTableView.refresh();
    }

    /**
     * Phương thức cập nhật hiển thị bảo hành trên toàn bảng
     * Gọi sau khi chuyển đổi ngôn ngữ hoặc sau khi load dữ liệu
     */
    private void updateWarrantyTypeDisplay() {
        // Xóa và khởi tạo lại cột warranty type với ngôn ngữ hiện tại
        ObservableList<OrderDetail> currentItems = orderItemsTableView.getItems();

        // Thiết lập lại cell factory cho cột warranty
        setupWarrantyTypeColumn();

        // Đặt lại items để kích hoạt việc tạo cell mới
        orderItemsTableView.setItems(null);
        orderItemsTableView.setItems(currentItems);

        // Refresh để đảm bảo tất cả các cell gọi updateItem()
        Platform.runLater(() -> {
            orderItemsTableView.refresh();
        });
    }

    // Phương thức hỗ trợ để trích xuất số tiền từ chuỗi tiền tệ
    private double extractAmountFromCurrency(String currencyText) {
        try {
            // Loại bỏ tất cả các ký tự không phải số và dấu chấm/phẩy
            String numericText = currencyText.replaceAll("[^0-9,.]", "");

            // Thay thế dấu phẩy bằng dấu chấm nếu dùng định dạng tiền tệ Việt Nam
            numericText = numericText.replace(',', '.');

            // Nếu có nhiều dấu chấm, giữ lại dấu chấm cuối cùng làm dấu thập phân
            int lastDot = numericText.lastIndexOf('.');
            if (lastDot > 0) {
                numericText = numericText.substring(0, lastDot).replace(".", "")
                        + numericText.substring(lastDot);
            }

            return Double.parseDouble(numericText);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    private void setupWarrantyTypeColumn() {
        // Khởi tạo danh sách loại bảo hành theo ngôn ngữ hiện tại
        ObservableList<String> warrantyTypes = FXCollections.observableArrayList(
                isVietnamese ? "Thường" : "Standard",
                isVietnamese ? "Vàng" : "Gold"
        );

        // Tạo cell factory với khả năng hiển thị đúng ngôn ngữ
        warrantyTypeColumn.setCellFactory(column -> {
            ComboBoxTableCell<OrderDetail, String> cell = new ComboBoxTableCell<OrderDetail, String>(warrantyTypes) {
                @Override
                public void updateItem(String value, boolean empty) {
                    // Lưu giá trị gốc
                    String originalValue = value;

                    super.updateItem(value, empty);

                    if (empty || originalValue == null) {
                        setText(null);
                        return;
                    }

                    // QUAN TRỌNG: Chuyển đổi từ giá trị lưu trữ sang giá trị hiển thị
                    String displayText;
                    if (!isVietnamese) {
                        // Khi UI là tiếng Anh
                        if ("Thường".equalsIgnoreCase(originalValue) ||
                                "Standard".equalsIgnoreCase(originalValue)) {
                            displayText = "Standard";
                        } else {
                            displayText = "Gold";
                        }
                    } else {
                        // Khi UI là tiếng Việt
                        if ("Standard".equalsIgnoreCase(originalValue) ||
                                "Thường".equalsIgnoreCase(originalValue)) {
                            displayText = "Thường";
                        } else {
                            displayText = "Vàng";
                        }
                    }

                    // Ghi đè text để hiển thị đúng ngôn ngữ
                    setText(displayText);
                    setStyle("-fx-text-fill: black;");
                }

                @Override
                public void startEdit() {
                    // Trước khi gọi super.startEdit(), hãy cập nhật lại danh sách giá trị cho combobox
                    getItems().clear();
                    getItems().addAll(isVietnamese ? "Thường" : "Standard", isVietnamese ? "Vàng" : "Gold");

                    // Lấy giá trị hiện tại từ dòng đang được chỉnh sửa
                    OrderDetail detail = getTableView().getItems().get(getTableRow().getIndex());

                    // Gọi phương thức của lớp cha để hiển thị combobox
                    super.startEdit();

                    // Sau khi combobox được hiển thị, tìm và cập nhật giá trị hiển thị
                    if (detail != null) {
                        String currentValue = detail.getWarrantyType();
                        String displayValue;

                        // Chuyển đổi giá trị hiển thị theo ngôn ngữ
                        if (!isVietnamese) {
                            // Chuyển từ tiếng Việt sang tiếng Anh
                            displayValue = "Thường".equals(currentValue) ? "Standard" : "Gold";
                        } else {
                            // Giữ nguyên giá trị tiếng Việt
                            displayValue = currentValue;
                        }

                        // Tìm combobox trong scene graph và cập nhật giá trị
                        for (Node node : getChildrenUnmodifiable()) {
                            if (node instanceof ComboBox) {
                                @SuppressWarnings("unchecked")
                                ComboBox<String> comboBox = (ComboBox<String>) node;
                                comboBox.setValue(displayValue);
                                break;
                            }
                        }
                    }
                }

                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    // Đảm bảo hiển thị lại đúng khi hủy chỉnh sửa
                    OrderDetail detail = getTableView().getItems().get(getTableRow().getIndex());
                    if (detail != null) {
                        String value = detail.getWarrantyType();
                        updateItem(value, false);
                    }
                }
            };

            // Thêm tooltip giải thích
            Tooltip tooltip = new Tooltip(isVietnamese ?
                    "Bảo hành Thường: Miễn phí\n" +
                            "Bảo hành Vàng: \n" +
                            "  - Nhóm sản phẩm cao cấp: +1.000.000đ/sản phẩm\n" +
                            "  - Nhóm sản phẩm thông thường: +500.000đ/sản phẩm" :
                    "Standard Warranty: Free\n" +
                            "Gold Warranty: \n" +
                            "  - Premium product group: +1,000,000 VND/item\n" +
                            "  - Standard product group: +500,000 VND/item");

            Tooltip.install(cell, tooltip);
            return cell;
        });


        // Cải thiện xử lý khi edit commit
        warrantyTypeColumn.setOnEditCommit(event -> {
            OrderDetail detail = event.getRowValue();
            String oldValue = detail.getWarrantyType();
            String newValue = event.getNewValue();

            // Chuyển đổi giá trị hiển thị sang giá trị lưu trữ (luôn là tiếng Việt)
            String storageValue;

            if (!isVietnamese) {
                // Đang ở giao diện tiếng Anh, chuyển giá trị về tiếng Việt để lưu
                if ("Standard".equals(newValue)) {
                    storageValue = "Thường";
                } else {
                    storageValue = "Vàng";
                }
            } else {
                // Đã là tiếng Việt, lưu trực tiếp
                storageValue = newValue;
            }

            // Cập nhật model với giá trị lưu trữ
            detail.setWarrantyType(storageValue);
            detail.updateWarrantyPrice();

            // Hiển thị thông báo khi có thay đổi giá
            if (!oldValue.equals(storageValue) && detail.getWarrantyPrice() > 0) {
                String message = isVietnamese ?
                        "Bảo hành Vàng cho sản phẩm này sẽ tính thêm " +
                                currencyFormatter.format(detail.getWarrantyPrice()) :
                        "Gold warranty for this product adds " +
                                currencyFormatter.format(detail.getWarrantyPrice());

                AlertUtils.showInfo(
                        isVietnamese ? "Thông tin bảo hành" : "Warranty Information",
                        message
                );
            }

            // Cập nhật tổng tiền
            updateOrderTotal();

            // Đảm bảo UI được cập nhật chính xác
            Platform.runLater(() -> orderItemsTableView.refresh());
        });

        // Đảm bảo cột và bảng có thể edit
        warrantyTypeColumn.setEditable(true);
        orderItemsTableView.setEditable(true);
    }

    @FXML
    private void handleLogout() {
        try {
            // Đăng xuất người dùng
            authService.logout();

            // Lưu trạng thái ngôn ngữ hiện tại
            LanguageManager.setVietnamese(isVietnamese);

            // Tải FXML đăng nhập
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomerLogin.fxml"));
            Parent root = loader.load();

            // Lấy Stage hiện tại
            Stage stage = (Stage) userMenuButton.getScene().getWindow();

            // Chuyển đến màn hình đăng nhập
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("CELLCOMP STORE - Login");
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

    // Thêm phương thức mới để mở trang thông tin tài khoản
    private void openCustomerChangeScreen() {
        try {
            // Lưu trạng thái ngôn ngữ hiện tại
            LanguageManager.setVietnamese(isVietnamese);

            // Tải FXML thông tin tài khoản
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomerChange.fxml"));
            Parent root = loader.load();

            // Lấy Stage hiện tại
            Stage stage = (Stage) userMenuButton.getScene().getWindow();

            // Chuyển đến màn hình thông tin tài khoản
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Thông tin tài khoản" : "CELLCOMP STORE - Account Information");
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

    /**
     * Phương thức điều hướng về trang Home khi nhấp vào logo hoặc tên cửa hàng
     */
    @FXML
    private void goToHome() {
        try {
            // Lưu trạng thái ngôn ngữ hiện tại
            LanguageManager.setVietnamese(isVietnamese);

            // Tải FXML trang chủ
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Home.fxml"));
            Parent root = loader.load();

            // Lấy Stage hiện tại
            Stage stage = (Stage) userMenuButton.getScene().getWindow();

            // Chuyển sang giao diện Home
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Trang chủ" : "CELLCOMP STORE - Home");
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
    /**
     * Opens the Custom PC Builder interface
     */
    private void openCustomDesignScreen() {
        try {
            // Save language setting
            LanguageManager.setVietnamese(isVietnamese);

            // Load the CustomPCBuilder interface
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomPCBuilder.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/stores/css/CustomPCBuilder.css").toExternalForm());

            Stage stage = (Stage) orderIdLabel.getScene().getWindow();
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
     * Opens the Order History interface
     */
    private void openOrderHistoryScreen() {
        goToOrderHistory(); // Use the existing goToOrderHistory method
    }
}