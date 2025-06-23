package com.example.stores.controller;

import com.example.stores.service.impl.OrderService;
import com.example.stores.service.impl.AuthService;
import com.example.stores.service.impl.ReviewService;
import com.example.stores.service.impl.WarrantyService;
import com.example.stores.util.AlertUtils;
import com.example.stores.util.LanguageManager;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class OrderDetailsController implements Initializable {

    // FXML elements - sửa tên để khớp với các phần tử trong file FXML
    @FXML private Label orderIdLabel;
    @FXML private Label orderDateLabel; // Đổi tên từ createDateLabel
    @FXML private Label orderStatusLabel;
    @FXML private Label paymentMethodLabel;
    @FXML private Label recipientNameLabel;  // Sửa thành đúng ID trong FXML
    @FXML private Label phoneNumberLabel;
    @FXML private Label addressLabel;       // Đổi tên thành addressLabel để khớp với FXML
    @FXML private Label totalItemsLabel;
    @FXML private Label subtotalLabel; // Thêm label mới
    @FXML private Label shippingFeeLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Label warrantyFeeLabel; // Thêm cho phí bảo hành nếu có
    @FXML private Button cancelOrderButton;
    @FXML private Label totalProductsLabel;

    // Table elements
    @FXML private TableView<OrderDetail> productTableView;
    @FXML private TableColumn<OrderDetail, String> productNameColumn;
    @FXML private TableColumn<OrderDetail, Double> priceColumn;
    @FXML private TableColumn<OrderDetail, Integer> quantityColumn;
    @FXML private TableColumn<OrderDetail, Double> totalProductPriceColumn;
    @FXML private TableColumn<OrderDetail, String> warrantyTypeColumn;
    @FXML private TableColumn<OrderDetail, Double> warrantyPriceColumn;
    @FXML private TableColumn<OrderDetail, Date> startDateColumn;
    @FXML private TableColumn<OrderDetail, Date> endDateColumn;
    @FXML private TableColumn<OrderDetail, Double> subtotalColumn;
    @FXML private MenuItem accountInfoMenuItem;
    @FXML private MenuItem languageSwitchMenuItem;
    @FXML private MenuItem customDesignMenuItem;
    @FXML private MenuItem orderHistoryMenuItem;
    @FXML private MenuItem logoutMenuItem;
    @FXML private MenuButton userMenuButton;

    // Services
    private OrderService orderService;
    private AuthService authService;
    private WarrantyService warrantyService;

    // Data
    private Order currentOrder;
    private String orderId;
    private boolean isVietnamese = true;
    private ObservableList<OrderDetail> orderDetails; // Thêm khai báo thiếu

    // Formatters
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo services
        orderService = new OrderService();
        authService = new AuthService();
        warrantyService = new WarrantyService();

        // Khởi tạo danh sách chi tiết đơn hàng
        orderDetails = FXCollections.observableArrayList();

        if (accountInfoMenuItem != null) {
            accountInfoMenuItem.setOnAction(e -> openCustomerChangeScreen());
        }

        if (customDesignMenuItem != null) {
            customDesignMenuItem.setOnAction(e -> openCustomDesignScreen());
        }

        if (orderHistoryMenuItem != null) {
            orderHistoryMenuItem.setOnAction(e -> openOrderHistoryScreen());
        }

        Platform.runLater(() -> {
            if (orderIdLabel != null && orderIdLabel.getScene() != null) {
                // Find MenuButton and MenuItems in the scene
                for (Node node : getAllNodes(orderIdLabel.getScene().getRoot())) {
                    if (node instanceof MenuButton && ((MenuButton) node).getId() != null
                            && ((MenuButton) node).getId().equals("userMenuButton")) {
                        MenuButton menuButton = (MenuButton) node;

                        // Find and set up menu items
                        for (MenuItem item : menuButton.getItems()) {
                            if (item.getId() != null) {
                                if (item.getId().equals("customDesignMenuItem")) {
                                    item.setOnAction(e -> openCustomDesignScreen());
                                } else if (item.getId().equals("orderHistoryMenuItem")) {
                                    item.setOnAction(e -> openOrderHistoryScreen());
                                } else if (item.getId().equals("logoutMenuItem")) {
                                    item.setOnAction(e -> handleLogout());
                                }
                            }
                        }
                        break;
                    }
                }
            }
        });




        // Thiết lập các cột trong TableView
        setupTableColumns();
        
        // Khởi tạo định dạng tiền tệ mặc định
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        // Lấy ngôn ngữ từ cài đặt
        isVietnamese = LanguageManager.isVietnamese();
        Platform.runLater(() -> {
            // Áp dụng ngôn ngữ ngay sau khi UI đã được khởi tạo
            updateLanguage();
        });
        System.out.println("OrderDetailsController đã khởi tạo");

        Platform.runLater(() -> {
            updateReviewButtonVisibility();
        });
    }

    private void setupTableColumns() {
        // Kiểm tra xem table columns đã được khởi tạo chưa
        if (productNameColumn == null) {
            System.err.println("WARNING: productNameColumn is null");
            return;
        }

        // Thiết lập cột tên sản phẩm
        productNameColumn.setCellValueFactory(cellData -> {
            OrderDetail detail = cellData.getValue();
            if (detail.getProduct() != null) {
                return new ReadOnlyObjectWrapper<>(detail.getProduct().getProductName());
            }
            return new ReadOnlyObjectWrapper<>("Unknown");
        });

        // Thiết lập cột giá đơn vị
        if (priceColumn != null) {
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
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
        }



        // Thiết lập cột số lượng
        if (quantityColumn != null) {
            quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        }

        // Cột tổng giá sản phẩm (giá x số lượng)
        if (totalProductPriceColumn != null) {
            totalProductPriceColumn.setCellValueFactory(cellData -> {
                OrderDetail detail = cellData.getValue();
                double totalProductPrice = detail.getUnitPrice() * detail.getQuantity();
                return new SimpleObjectProperty<>(totalProductPrice);
            });
            totalProductPriceColumn.setCellFactory(column -> new TableCell<OrderDetail, Double>() {
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
        }

        // Thiết lập cột loại bảo hành
        if (warrantyTypeColumn != null) {
            warrantyTypeColumn.setCellValueFactory(new PropertyValueFactory<>("warrantyType"));
            // Thêm code mới để xử lý hiển thị theo ngôn ngữ
            warrantyTypeColumn.setCellFactory(column -> new TableCell<OrderDetail, String>() {
                @Override
                protected void updateItem(String warrantyType, boolean empty) {
                    super.updateItem(warrantyType, empty);
                    if (empty || warrantyType == null) {
                        setText(null);
                    } else {
                        setText(translateWarrantyType(warrantyType));
                    }
                }
            });
        }

        // Thiết lập cột giá bảo hành
        if (warrantyPriceColumn != null) {
            warrantyPriceColumn.setCellValueFactory(new PropertyValueFactory<>("warrantyPrice"));
            warrantyPriceColumn.setCellFactory(column -> new TableCell<OrderDetail, Double>() {
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
        }

        // Cột ngày bắt đầu bảo hành
        if (startDateColumn != null) {
            startDateColumn.setCellValueFactory(cellData -> {
                OrderDetail detail = cellData.getValue();
                if (detail == null) return new SimpleObjectProperty<>(null);

                // Lấy ngày đặt hàng làm ngày bắt đầu bảo hành
                if (currentOrder != null && currentOrder.getOrderDate() != null) {
                    return new SimpleObjectProperty<>(Date.from(currentOrder.getOrderDate()
                            .atZone(ZoneId.systemDefault()).toInstant()));
                }
                return new SimpleObjectProperty<>(new Date());
            });

            startDateColumn.setCellFactory(column -> new TableCell<OrderDetail, Date>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                @Override
                protected void updateItem(Date date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        setText(formatter.format(localDate));
                    }
                }
            });
        }

        // Cột ngày kết thúc bảo hành
        if (endDateColumn != null) {
            endDateColumn.setCellValueFactory(cellData -> {
                OrderDetail detail = cellData.getValue();
                if (detail == null) return new SimpleObjectProperty<>(null);

                // Lấy ngày đặt hàng
                Date startDate;
                if (currentOrder != null && currentOrder.getOrderDate() != null) {
                    startDate = Date.from(currentOrder.getOrderDate()
                            .atZone(ZoneId.systemDefault()).toInstant());
                } else {
                    startDate = new Date();
                }

                // Tạo calendar để tính toán
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);

                // Thêm thời gian bảo hành tùy theo loại
                if ("Vàng".equals(detail.getWarrantyType()) || "Gold".equals(detail.getWarrantyType())) {
                    // Bảo hành vàng: 1 năm
                    calendar.add(Calendar.YEAR, 1);
                } else {
                    // Bảo hành thường: 6 tháng
                    calendar.add(Calendar.MONTH, 6);
                }

                return new SimpleObjectProperty<>(calendar.getTime());
            });

            endDateColumn.setCellFactory(column -> new TableCell<OrderDetail, Date>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                @Override
                protected void updateItem(Date date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText("-");
                    } else {
                        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        setText(formatter.format(localDate));
                    }
                }
            });
        }

        // Thiết lập cột tổng tiền
        if (subtotalColumn != null) {
            subtotalColumn.setCellValueFactory(cellData -> {
                OrderDetail detail = cellData.getValue();
                // Tính thành tiền = Giá * SL + Tiền bảo hành
                double total = (detail.getUnitPrice() * detail.getQuantity()) + detail.getWarrantyPrice();
                return new SimpleObjectProperty<>(total);
            });
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
    }

    private String accessSource = "history"; // Mặc định là từ lịch sử đơn hàng

    /**
     * Thiết lập nguồn truy cập vào chi tiết đơn hàng
     * @param source "new_order" nếu từ đơn hàng mới, "history" nếu từ lịch sử
     */
    public void setAccessSource(String source) {
        this.accessSource = source;
    }

    private String translateWarrantyType(String warrantyType) {
        if (warrantyType == null) return "";

        // Nếu đang hiển thị tiếng Việt, giữ nguyên
        if (isVietnamese) {
            return warrantyType;
        }

        // Dịch sang tiếng Anh
        switch (warrantyType.toLowerCase()) {
            case "vàng":
                return "Gold";
            case "thường":
                return "Standard";
            case "không":
                return "None";
            default:
                return warrantyType;
        }
    }

    // Thêm phương thức PUBLIC để thiết lập mã đơn hàng
    public void setOrderId(String orderId) {
        this.orderId = orderId;
        // Nếu có orderId nhưng chưa có đơn hàng, tải đơn hàng từ database
        if (orderId != null && !orderId.isEmpty() && currentOrder == null) {
            currentOrder = orderService.getOrderById(orderId);
        }
        loadOrderDetails();
    }

    // Thêm phương thức PUBLIC để thiết lập ngôn ngữ
    public void setIsVietnamese(boolean isVietnamese) {
        // Luôn gán giá trị và cập nhật UI bất kể giá trị có thay đổi hay không
        this.isVietnamese = isVietnamese;

        // Lưu vào LanguageManager để đồng bộ với các màn hình khác
        LanguageManager.setVietnamese(isVietnamese);

        // Cập nhật giao diện người dùng
        if (orderIdLabel != null) { // Kiểm tra xem UI đã được khởi tạo chưa
            Platform.runLater(this::updateLanguage);
        }
    }

    // Thêm phương thức PUBLIC để thiết lập đơn hàng
    public void setOrder(Order order) {
        this.currentOrder = order;
        System.out.println("Order set in OrderDetailsController: " + (order != null ? order.getOrderId() : "null"));
        
        // Cập nhật orderId từ đơn hàng
        if (order != null) {
            this.orderId = order.getOrderId();
        }
    }

    // Sửa loadOrderDetails() từ private sang public 
    // filepath: d:\HK2-nam2\Stores\src\main\java\com\example\stores\controller\OrderDetailsController.java
    public void loadOrderDetails() {
        if (currentOrder == null) {
            System.err.println("ERROR: Cannot load order details - order is null");
            return;
        }

        try {
            // Thiết lập thông tin cơ bản
            if (orderIdLabel != null)
                orderIdLabel.setText(currentOrder.getOrderId());

            if (orderDateLabel != null)
                orderDateLabel.setText(formatDateTime(currentOrder.getOrderDate()));

            if (orderStatusLabel != null)
                orderStatusLabel.setText(getStatusDisplay(currentOrder.getOrderStatus()));

            if (paymentMethodLabel != null)
                paymentMethodLabel.setText(getPaymentMethodDisplay(currentOrder.getPaymentMethod()));

            // Thiết lập thông tin người nhận - ƯU TIÊN dùng thông tin từ currentOrder
            if (recipientNameLabel != null) {
                recipientNameLabel.setText(currentOrder.getRecipientName());
                System.out.println("DEBUG: Hiển thị tên người nhận: " + currentOrder.getRecipientName());
            }

            if (phoneNumberLabel != null) {
                phoneNumberLabel.setText(currentOrder.getRecipientPhone());
                System.out.println("DEBUG: Hiển thị SĐT: " + currentOrder.getRecipientPhone());
            }

            if (addressLabel != null) {  // Thay vì customerAddressLabel
                addressLabel.setText(currentOrder.getShippingAddress());
                System.out.println("DEBUG: Hiển thị địa chỉ: " + currentOrder.getShippingAddress());
            }

            // Tải chi tiết đơn hàng
            List<OrderDetail> details = orderService.getOrderDetails(currentOrder.getOrderId());

            if (details != null && !details.isEmpty()) {
                orderDetails.clear();
                orderDetails.addAll(details);

                if (productTableView != null) {
                    productTableView.setItems(orderDetails);
                }

                // Cập nhật tổng tiền đơn hàng
                updateOrderSummary();
            }
            Platform.runLater(this::updateLanguage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> {
            updateReviewButtonVisibility();
        });
    }

    // Thêm các phương thức phụ trợ cần thiết
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    private String getPaymentMethodDisplay(String paymentMethod) {
        if (paymentMethod == null) return "";
        
        switch (paymentMethod.toUpperCase()) {
            case "COD":
                return isVietnamese ? "Thanh toán khi nhận hàng" : "Cash on Delivery";
            case "MOMO":
                return "MoMo";
            case "ZALOPAY":
                return "ZaloPay";
            case "VIETINBANK":
                return "VietinBank";
            default:
                return paymentMethod;
        }
    }

    private String getStatusDisplay(String status) {
        if (status == null) return "";
        
        switch (status.toUpperCase()) {
            case "PENDING_PAYMENT":
                return isVietnamese ? "Chờ thanh toán" : "Pending Payment";
            case "PAID":
                return isVietnamese ? "Đã thanh toán" : "Paid";
            case "CONFIRMED":
                return isVietnamese ? "Đã xác nhận" : "Confirmed";
            case "SHIPPING":
                return isVietnamese ? "Đang giao hàng" : "Shipping";
            case "COMPLETED":
                return isVietnamese ? "Hoàn thành" : "Completed";
            case "CANCELLED":
                return isVietnamese ? "Đã hủy" : "Cancelled";
            default:
                return status;
        }
    }

    private void updateUIBasedOnStatus(String status) {
        // Các thiết lập UI dựa trên trạng thái đơn hàng
        if (status != null && orderStatusLabel != null) {
            switch (status.toUpperCase()) {
                case "PENDING_PAYMENT":
                    orderStatusLabel.setStyle("-fx-text-fill: #ff9900;");
                    break;
                case "PAID":
                case "CONFIRMED":
                    orderStatusLabel.setStyle("-fx-text-fill: #0066cc;");
                    break;
                case "SHIPPING":
                    orderStatusLabel.setStyle("-fx-text-fill: #9900cc;");
                    break;
                case "COMPLETED":
                    orderStatusLabel.setStyle("-fx-text-fill: #009900;");
                    break;
                case "CANCELLED":
                    orderStatusLabel.setStyle("-fx-text-fill: #cc0000;");
                    break;
            }
        }
    }

    private void updateOrderSummary() {
        if (currentOrder == null || orderDetails == null) return;

        // QUAN TRỌNG: Khởi tạo các biến tính toán
        int totalQuantity = 0;
        double totalProductPrice = 0;
        double totalWarrantyPrice = 0;

        // Tính toán dựa trên giá trị thực tế của orderDetails
        for (OrderDetail detail : orderDetails) {
            // Cộng số lượng sản phẩm
            totalQuantity += detail.getQuantity();

            // Tính tổng giá sản phẩm (giá * số lượng)
            double productCost = detail.getUnitPrice() * detail.getQuantity();
            totalProductPrice += productCost;

            // Cộng phí bảo hành
            totalWarrantyPrice += detail.getWarrantyPrice();

            System.out.println("DEBUG: Chi tiết sản phẩm: " + detail.getProduct().getProductName() +
                    ", SL: " + detail.getQuantity() +
                    ", Giá: " + detail.getUnitPrice() +
                    ", BH: " + detail.getWarrantyPrice());
        }

        // Lấy phí vận chuyển
        double shippingFee = currentOrder.getShippingFee();

        // Tính tổng thanh toán
        double totalAmount = totalProductPrice + totalWarrantyPrice + shippingFee;

        System.out.println("DEBUG: Tổng SL: " + totalQuantity +
                ", Tổng giá SP: " + totalProductPrice +
                ", BH: " + totalWarrantyPrice +
                ", Ship: " + shippingFee +
                " = Tổng: " + totalAmount);

        // Hiển thị kết quả với tên biến đúng theo FXML
        if (totalProductsLabel != null)
            totalProductsLabel.setText(totalQuantity + (isVietnamese ? " sản phẩm" : " items"));

        if (shippingFeeLabel != null)
            shippingFeeLabel.setText(formatCurrency(shippingFee));

        if (totalPriceLabel != null)
            totalPriceLabel.setText(formatCurrency(totalAmount));
    }

    public void updateLanguage() {
        // Cập nhật menu ngôn ngữ và các menu item khác trong userMenuButton
        if (userMenuButton != null) {
            // Cập nhật tất cả các MenuItem trong userMenuButton
            for (MenuItem item : userMenuButton.getItems()) {
                if (item == accountInfoMenuItem) {
                    item.setText(isVietnamese ? "Thông tin tài khoản" : "Account Information");
                } else if (item == customDesignMenuItem) {
                    item.setText(isVietnamese ? "Thiết kế máy tính theo ý bạn" : "Design Your Computer");
                } else if (item == orderHistoryMenuItem) {
                    item.setText(isVietnamese ? "Lịch sử mua hàng" : "Purchase History");
                } else if (item == logoutMenuItem) {
                    item.setText(isVietnamese ? "Đăng xuất" : "Logout");
                } else if (item == languageSwitchMenuItem) {
                    // Khi ở giao diện tiếng Việt, hiển thị "Change Language" và cờ Anh
                    // Khi ở giao diện tiếng Anh, hiển thị "Chuyển đổi ngôn ngữ" và cờ Việt
                    item.setText(isVietnamese ? "Change Language" : "Chuyển đổi ngôn ngữ");

                    try {
                        // Đường dẫn hình ảnh cờ - tiếng Việt hiển thị cờ Anh và ngược lại
                        String flagPath = isVietnamese ?
                                "/com/example/stores/images/layout/flag_en.png" :
                                "/com/example/stores/images/layout/flag_vn.png";

                        // Tạo ImageView với hình ảnh cờ mới
                        ImageView flagIcon = new ImageView(new Image(
                                getClass().getResourceAsStream(flagPath)
                        ));

                        flagIcon.setFitHeight(16);
                        flagIcon.setFitWidth(16);
                        item.setGraphic(flagIcon);
                    } catch (Exception e) {
                        System.err.println("Không thể tải hình ảnh cờ: " + e.getMessage());
                    }
                }
            }
        }

        // Cập nhật các tiêu đề chính
        updateTableHeaders();

        // Cập nhật các nhãn thông tin đơn hàng
        updateOrderInfoLabels();

        // Cập nhật tiêu đề các phần
        updateSectionTitles();

        // Cập nhật text cho các nút
        updateButtonTexts();

        // Cập nhật trạng thái đơn hàng và phương thức thanh toán
        if (currentOrder != null) {
            if (orderStatusLabel != null)
                orderStatusLabel.setText(getStatusDisplay(currentOrder.getOrderStatus()));

            if (paymentMethodLabel != null)
                paymentMethodLabel.setText(getPaymentMethodDisplay(currentOrder.getPaymentMethod()));
        }

        // Cập nhật lại bảng và tổng tiền
        if (productTableView != null) {
            productTableView.refresh();
        }

        // Tải lại dữ liệu để cập nhật định dạng
        updateOrderSummary();
    }

    // Phương thức mới để cập nhật tiêu đề các phần
    private void updateSectionTitles() {
        updateLabelByText("Thông tin chi tiết đơn hàng", "Order Details");
        // Tìm và cập nhật các tiêu đề phần bằng cách tìm kiếm trong scene
        updateLabelByText("Thông tin đơn hàng", "Order Information");
        updateLabelByText("Thông tin người nhận", "Recipient Information");
        updateLabelByText("Thông tin sản phẩm", "Product Information");
    }

    // Phương thức mới để cập nhật các nhãn thông tin đơn hàng
    private void updateOrderInfoLabels() {
        // Cập nhật các label cho các trường thông tin
        updateLabelByText("Mã đơn hàng:", "Order ID:");
        updateLabelByText("Ngày tạo:", "Create Date:");
        updateLabelByText("Trạng thái đơn hàng:", "Order Status:");
        updateLabelByText("Phương thức thanh toán:", "Payment Method:");

        updateLabelByText("Tên người nhận:", "Recipient Name:");
        updateLabelByText("Số điện thoại:", "Phone Number:");
        updateLabelByText("Địa chỉ:", "Address:");

        updateLabelByText("Tổng sản phẩm:", "Total Products:");
        updateLabelByText("Phí vận chuyển:", "Shipping Fee:");
        updateLabelByText("Tổng thanh toán:", "Total Payment:");
        updateLabelByText("Phí bảo hành:", "Warranty Fee:");
    }


    // Thêm phương thức mới để cập nhật các mục trong menu người dùng
    private void updateUserMenuItems() {
        if (accountInfoMenuItem != null) {
            accountInfoMenuItem.setText(isVietnamese ? "Thông tin tài khoản" : "Account Information");
        }

        if (languageSwitchMenuItem != null) {
            languageSwitchMenuItem.setText(isVietnamese ? "Chuyển đổi ngôn ngữ" : "Switch Language");

            // Cập nhật hình ảnh lá cờ
            ImageView flagImage = (ImageView) languageSwitchMenuItem.getGraphic();
            if (flagImage != null) {
                String flagImagePath = isVietnamese ?
                        "/com/example/stores/images/layout/flag_en.png" :
                        "/com/example/stores/images/layout/flag_vn.png";
                Image flag = new Image(getClass().getResourceAsStream(flagImagePath));
                flagImage.setImage(flag);
            }
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
    }

    // Phương thức mới để cập nhật tiêu đề các cột trong bảng
    private void updateTableHeaders() {
        if (productNameColumn != null)
            productNameColumn.setText(isVietnamese ? "Tên sản phẩm" : "Product Name");

        if (priceColumn != null)
            priceColumn.setText(isVietnamese ? "Giá SP" : "Price");

        if (quantityColumn != null)
            quantityColumn.setText(isVietnamese ? "SL" : "Quantity");

        if (totalProductPriceColumn != null)
            totalProductPriceColumn.setText(isVietnamese ? "Tổng giá" : "Total Price");

        if (warrantyTypeColumn != null)
            warrantyTypeColumn.setText(isVietnamese ? "Loại bảo hành" : "Warranty Type");

        if (warrantyPriceColumn != null)
            warrantyPriceColumn.setText(isVietnamese ? "Tiền bảo hành" : "Warranty Price");

        if (startDateColumn != null)
            startDateColumn.setText(isVietnamese ? "Ngày bắt đầu" : "Start Date");

        if (endDateColumn != null)
            endDateColumn.setText(isVietnamese ? "Ngày kết thúc" : "End Date");

        if (subtotalColumn != null)
            subtotalColumn.setText(isVietnamese ? "Thành tiền" : "Subtotal");
    }

    // Phương thức mới để cập nhật text cho các nút
    private void updateButtonTexts() {
        // Tìm và cập nhật các nút
        updateButtonByText("Quay lại", "Back");
        updateButtonByText("Hủy đơn hàng", "Cancel Order");
        updateButtonByText("In hóa đơn", "Print Invoice");
        updateButtonByText("Chuyển đổi ngôn ngữ", "Switch Language");
    }

    // Phương thức tiện ích để cập nhật label dựa trên text hiện tại
    private void updateLabelByText(String vietnameseText, String englishText) {
        // Tìm trong scene
        if (orderIdLabel != null && orderIdLabel.getScene() != null) {
            for (Node node : getAllNodes(orderIdLabel.getScene().getRoot())) {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    if (vietnameseText.equals(label.getText()) && isVietnamese == false) {
                        label.setText(englishText);
                        continue;
                    }
                    if (englishText.equals(label.getText()) && isVietnamese == true) {
                        label.setText(vietnameseText);
                        continue;
                    }
                }
            }
        }
    }

    // Phương thức tiện ích để cập nhật button dựa trên text hiện tại
    private void updateButtonByText(String vietnameseText, String englishText) {
        // Tìm trong scene
        if (orderIdLabel != null && orderIdLabel.getScene() != null) {
            for (Node node : getAllNodes(orderIdLabel.getScene().getRoot())) {
                if (node instanceof Button) {
                    Button button = (Button) node;
                    if (vietnameseText.equals(button.getText()) && isVietnamese == false) {
                        button.setText(englishText);
                        continue;
                    }
                    if (englishText.equals(button.getText()) && isVietnamese == true) {
                        button.setText(vietnameseText);
                        continue;
                    }
                }
            }
        }
    }

    // Phương thức tiện ích để lấy tất cả các node từ rootNode
    private List<Node> getAllNodes(Parent root) {
        List<Node> nodes = new ArrayList<>();
        addAllDescendents(root, nodes);
        return nodes;
    }

    private void addAllDescendents(Parent parent, List<Node> nodes) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
            if (node instanceof Parent)
                addAllDescendents((Parent) node, nodes);
        }
    }

    // Sửa lại phương thức formatCurrency để luôn sử dụng định dạng VND
    private String formatCurrency(double amount) {
        // Luôn sử dụng locale Vietnam cho định dạng tiền tệ
        Locale locale = new Locale("vi", "VN");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        return formatter.format(amount);
    }

    @FXML
    private void cancelOrder() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(isVietnamese ? "Xác nhận hủy đơn hàng" : "Confirm Order Cancellation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText(isVietnamese ?
                "Bạn có chắc chắn muốn hủy đơn hàng này không?" :
                "Are you sure you want to cancel this order?");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = orderService.updateOrderStatus(currentOrder.getOrderId(), "CANCELLED");

            if (success) {
                AlertUtils.showInfo(
                        isVietnamese ? "Thành công" : "Success",
                        isVietnamese ? "Đơn hàng đã được hủy thành công" : "The order has been canceled successfully"
                );

                // Reload order details
                currentOrder.setOrderStatus("CANCELLED");
                loadOrderDetails();
            } else {
                AlertUtils.showError(
                        isVietnamese ? "Lỗi" : "Error",
                        isVietnamese ? "Không thể hủy đơn hàng" : "Cannot cancel the order"
                );
            }
        }
    }

    /**
     * Hàm xử lý sự kiện in hóa đơn - đã được sửa để khắc phục lỗi "Identity-H"
     */
    @FXML
    private void printInvoice() {
        if (currentOrder == null) {
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không có thông tin đơn hàng" : "No order information available"
            );
            return;
        }

        try {
            // Tạo tên file với định dạng: CELLCOMP_Invoice_ORDxxx_ngàytháng.pdf
            String fileName = "CELLCOMP_Invoice_" + currentOrder.getOrderId() + "_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss")) + ".pdf";

            // Thay đổi vị trí lưu file sang D:\BTL
            String outputFolder = "D:\\BTL";
            File directory = new File(outputFolder);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String filePath = outputFolder + File.separator + fileName;

            // Tạo file PDF
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Thêm đoạn code này để hỗ trợ tiếng Việt có dấu
            BaseFont unicodeFont = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font vietnameseFont = new Font(unicodeFont, 12);
            Font vietnameseBold = new Font(unicodeFont, 12, Font.BOLD);
            Font vietnameseHeader = new Font(unicodeFont, 16, Font.BOLD);
            Font vietnameseTitle = new Font(unicodeFont, 18, Font.BOLD);
            Font vietnameseSmall = new Font(unicodeFont, 10);

            // ----- HEADER -----
            Paragraph title = new Paragraph("CELLCOMP STORE", vietnameseTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subTitle = new Paragraph(isVietnamese ? "HÓA ĐƠN BÁN HÀNG" : "SALES INVOICE", vietnameseHeader);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            subTitle.setSpacingAfter(15);
            document.add(subTitle);

            // ----- THÔNG TIN ĐƠN HÀNG -----
            PdfPTable orderInfoTable = new PdfPTable(2);
            orderInfoTable.setWidthPercentage(100);
            orderInfoTable.setSpacingBefore(10);
            orderInfoTable.setSpacingAfter(10);

            // Thêm tiêu đề phần đơn hàng
            PdfPCell orderTitleCell = new PdfPCell(new Phrase(isVietnamese ? "THÔNG TIN ĐƠN HÀNG" :
                    "ORDER INFORMATION", vietnameseBold));
            orderTitleCell.setColspan(2);
            orderTitleCell.setPadding(8);
            orderTitleCell.setBackgroundColor(new BaseColor(230, 230, 250)); // Light purple
            orderInfoTable.addCell(orderTitleCell);

            // Thêm các thông tin đơn hàng - Lấy dữ liệu từ currentOrder
            addTableRow(orderInfoTable, isVietnamese ? "Mã đơn hàng:" : "Order ID:",
                    currentOrder.getOrderId(), vietnameseBold, vietnameseFont);

            // Ngày đặt hàng
            String orderDate = currentOrder.getOrderDate() != null ?
                    currentOrder.getOrderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
            addTableRow(orderInfoTable, isVietnamese ? "Ngày đặt hàng:" : "Order Date:",
                    orderDate, vietnameseBold, vietnameseFont);

            // Trạng thái
            addTableRow(orderInfoTable, isVietnamese ? "Trạng thái:" : "Status:",
                    getStatusDisplay(currentOrder.getOrderStatus()), vietnameseBold, vietnameseFont);

            // Phương thức thanh toán
            addTableRow(orderInfoTable, isVietnamese ? "Phương thức thanh toán:" : "Payment Method:",
                    getPaymentMethodDisplay(currentOrder.getPaymentMethod()), vietnameseBold, vietnameseFont);

            document.add(orderInfoTable);

            // ----- THÔNG TIN NGƯỜI NHẬN -----
            PdfPTable customerInfoTable = new PdfPTable(2);
            customerInfoTable.setWidthPercentage(100);
            customerInfoTable.setSpacingBefore(10);
            customerInfoTable.setSpacingAfter(10);

            // Thêm tiêu đề phần khách hàng
            PdfPCell customerTitleCell = new PdfPCell(new Phrase(isVietnamese ? "THÔNG TIN NGƯỜI NHẬN" :
                    "RECIPIENT INFORMATION", vietnameseBold));
            customerTitleCell.setColspan(2);
            customerTitleCell.setPadding(8);
            customerTitleCell.setBackgroundColor(new BaseColor(230, 230, 250)); // Light purple
            customerInfoTable.addCell(customerTitleCell);

            // Thêm các thông tin người nhận
            addTableRow(customerInfoTable, isVietnamese ? "Họ tên:" : "Full Name:",
                    currentOrder.getRecipientName(), vietnameseBold, vietnameseFont);
            addTableRow(customerInfoTable, isVietnamese ? "Số điện thoại:" : "Phone:",
                    currentOrder.getRecipientPhone(), vietnameseBold, vietnameseFont);
            addTableRow(customerInfoTable, isVietnamese ? "Địa chỉ:" : "Address:",
                    currentOrder.getShippingAddress(), vietnameseBold, vietnameseFont);

            document.add(customerInfoTable);

            // ----- BẢNG SẢN PHẨM -----
            // Thêm tiêu đề phần sản phẩm
            Paragraph productTitle = new Paragraph(isVietnamese ? "CHI TIẾT SẢN PHẨM" :
                    "PRODUCT DETAILS", vietnameseBold);
            productTitle.setSpacingBefore(15);
            document.add(productTitle);

            // Tạo bảng sản phẩm
            PdfPTable productTable = new PdfPTable(6); // 6 cột
            productTable.setWidthPercentage(100);
            productTable.setSpacingBefore(10);
            float[] columnWidths = {3f, 0.7f, 1.5f, 1.5f, 1.3f, 2f};
            try {
                productTable.setWidths(columnWidths);
            } catch (DocumentException e) {
                e.printStackTrace();
            }

            // Header cho bảng
            String[] columnHeaders = isVietnamese ?
                    new String[]{"Tên sản phẩm", "SL", "Đơn giá", "Loại BH", "Giá BH", "Thành tiền"} :
                    new String[]{"Product Name", "Qty", "Unit Price", "Warranty", "Warranty Price", "Subtotal"};

            for (String columnHeader : columnHeaders) {
                PdfPCell cell = new PdfPCell(new Phrase(columnHeader, vietnameseBold));
                cell.setBackgroundColor(new BaseColor(240, 240, 240));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                productTable.addCell(cell);
            }

            // Lấy dữ liệu từ bảng JavaFX
            NumberFormat currencyFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            ObservableList<OrderDetail> items = productTableView.getItems();

            // Dữ liệu sản phẩm
            for (OrderDetail item : items) {
                // Tên sản phẩm
                PdfPCell nameCell = new PdfPCell(new Phrase(item.getProduct().getProductName(), vietnameseFont));
                nameCell.setPadding(5);
                productTable.addCell(nameCell);

                // Số lượng
                PdfPCell quantityCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), vietnameseFont));
                quantityCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                quantityCell.setPadding(5);
                productTable.addCell(quantityCell);

                // Đơn giá
                PdfPCell priceCell = new PdfPCell(new Phrase(currencyFormatter.format(item.getUnitPrice()) + "đ", vietnameseFont));
                priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                priceCell.setPadding(5);
                productTable.addCell(priceCell);

                // Loại bảo hành
                PdfPCell warrantyTypeCell = new PdfPCell(new Phrase(item.getWarrantyType(), vietnameseFont));
                warrantyTypeCell.setPadding(5);
                productTable.addCell(warrantyTypeCell);

                // Giá bảo hành
                PdfPCell warrantyCell = new PdfPCell(new Phrase(currencyFormatter.format(item.getWarrantyPrice()) + "đ", vietnameseFont));
                warrantyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                warrantyCell.setPadding(5);
                productTable.addCell(warrantyCell);

                // Thành tiền
                PdfPCell subtotalCell = new PdfPCell(new Phrase(currencyFormatter.format(item.getSubtotal()) + "đ", vietnameseFont));
                subtotalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                subtotalCell.setPadding(5);
                productTable.addCell(subtotalCell);
            }

            document.add(productTable);

            // ----- PHẦN TỔNG CỘNG -----
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(40);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTable.setSpacingBefore(15);

            // Số lượng sản phẩm
            addTableRow(totalTable, isVietnamese ? "Tổng sản phẩm:" : "Total Products:",
                    totalProductsLabel.getText(), vietnameseBold, vietnameseFont);

            // Phí vận chuyển
            addTableRow(totalTable, isVietnamese ? "Phí vận chuyển:" : "Shipping Fee:",
                    shippingFeeLabel.getText(), vietnameseBold, vietnameseFont);

            // Tổng thanh toán
            PdfPCell totalLabelCell = new PdfPCell(new Phrase(isVietnamese ? "TỔNG THANH TOÁN:" :
                    "TOTAL PAYMENT:", vietnameseBold));
            totalLabelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            totalLabelCell.setPadding(5);
            totalLabelCell.setBorderWidth(0);
            totalTable.addCell(totalLabelCell);

            PdfPCell totalValueCell = new PdfPCell(new Phrase(totalPriceLabel.getText(), vietnameseBold));
            totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalValueCell.setPadding(5);
            totalValueCell.setBorderWidth(0);
            totalTable.addCell(totalValueCell);

            document.add(totalTable);

            // ----- FOOTER -----
            document.add(new Paragraph("\n\n"));

            Paragraph thankYou = new Paragraph(isVietnamese ? "Cảm ơn quý khách đã mua hàng tại CELLCOMP STORE!"
                    : "Thank you for shopping at CELLCOMP STORE!", vietnameseFont);
            thankYou.setAlignment(Element.ALIGN_CENTER);
            document.add(thankYou);

            Paragraph contactInfo = new Paragraph("Hotline: 1900 1234 - Email: support@cellcomp.com - Website: cellcomp.vn", vietnameseSmall);
            contactInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(contactInfo);

            Paragraph dateInfo = new Paragraph((isVietnamese ? "Ngày xuất hóa đơn: " : "Invoice Date: ") +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), vietnameseSmall);
            dateInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(dateInfo);

            // Đóng document
            document.close();

            // Thông báo thành công
            AlertUtils.showInfo(
                    isVietnamese ? "Xuất hóa đơn thành công" : "Invoice exported successfully",
                    isVietnamese ? "Đã lưu hóa đơn tại:\n" + filePath : "Invoice saved at:\n" + filePath
            );

            // Xử lý lỗi khi không có ứng dụng để mở PDF
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(new File(filePath));
                }
            } catch (IOException e) {
                // Chỉ hiển thị thông báo, không gây lỗi
                AlertUtils.showWarning(
                        isVietnamese ? "Lưu ý" : "Note",
                        isVietnamese ? "Đã lưu hóa đơn, nhưng không thể tự động mở do không có ứng dụng đọc PDF.\n" +
                                "Vui lòng mở thủ công tại: " + filePath
                                : "Invoice saved, but cannot open automatically because there's no PDF reader application.\n" +
                                "Please open manually at: " + filePath
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể tạo hóa đơn: " + e.getMessage()
                            : "Cannot create invoice: " + e.getMessage()
            );
        }
        // Sau khi in xong, hiển thị thông báo
        Alert printAlert = new Alert(Alert.AlertType.CONFIRMATION);
        printAlert.setTitle(isVietnamese ? "In hóa đơn" : "Print Invoice");
        printAlert.setHeaderText(isVietnamese ? "Đã in hóa đơn thành công!" : "Invoice printed successfully!");
        printAlert.setContentText(isVietnamese ? "Bạn có muốn quay lại trang chủ không?" : "Would you like to return to home page?");

        // Tùy chỉnh nút
        ButtonType yesButton = new ButtonType(isVietnamese ? "Có" : "Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType(isVietnamese ? "Không" : "No", ButtonBar.ButtonData.NO);
        printAlert.getButtonTypes().setAll(yesButton, noButton);

        // Xử lý phản hồi
        Optional<ButtonType> result = printAlert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            // Chuyển về trang chủ
            goToHome();
        }
    }

    // Thêm phương thức goToHome() để tái sử dụng
    private void goToHome() {
        try {
            // Lưu trạng thái ngôn ngữ hiện tại
            LanguageManager.setVietnamese(isVietnamese);

            // Tải FXML trang chủ
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Home.fxml"));
            Parent root = loader.load();

            // Lấy Stage hiện tại
            Stage stage = (Stage) orderIdLabel.getScene().getWindow();

            // Chuyển sang giao diện Home
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("CELLCOMP STORE");
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
     * Thêm phần header vào PDF - đã sửa để không sử dụng BaseFont với encoding Identity-H
     */
    private void addHeaderToPDF(Document document) throws DocumentException {
        // Sử dụng font hỗ trợ tiếng Việt mà không cần Identity-H
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font subHeaderFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);

        // Tiêu đề
        Paragraph titlePara = new Paragraph("CELLCOMP STORE", headerFont);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        document.add(titlePara);

        Paragraph subTitle = new Paragraph(isVietnamese ? "HOA DON BAN HANG" : "SALES INVOICE", subHeaderFont);
        subTitle.setAlignment(Element.ALIGN_CENTER);
        subTitle.setSpacingAfter(15);
        document.add(subTitle);
    }

    /**
     * Thêm thông tin đơn hàng vào PDF - đã sửa để xử lý biến null
     */
    private void addOrderInfoToPDF(Document document) throws DocumentException {
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);

        // Tạo bảng thông tin đơn hàng
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        // Thêm tiêu đề phần đơn hàng
        PdfPCell titleCell = new PdfPCell(new Phrase(isVietnamese ? "THONG TIN DON HANG" : "ORDER INFORMATION", boldFont));
        titleCell.setColspan(2);
        titleCell.setPadding(8);
        titleCell.setBackgroundColor(new BaseColor(230, 230, 250));
        table.addCell(titleCell);

        // Lấy dữ liệu từ currentOrder thay vì từ label để tránh lỗi null
        addTableRow(table, isVietnamese ? "Ma don hang:" : "Order ID:", currentOrder.getOrderId(), boldFont, normalFont);

        // Định dạng ngày tháng từ thông tin đơn hàng
        String orderDate = currentOrder.getOrderDate() != null ?
                currentOrder.getOrderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
        addTableRow(table, isVietnamese ? "Ngay dat hang:" : "Order Date:", orderDate, boldFont, normalFont);

        addTableRow(table, isVietnamese ? "Trang thai:" : "Status:", currentOrder.getOrderStatus(), boldFont, normalFont);
        addTableRow(table, isVietnamese ? "Phuong thuc thanh toan:" : "Payment Method:", currentOrder.getPaymentMethod(), boldFont, normalFont);

        document.add(table);
    }

    /**
     * Thêm thông tin khách hàng vào PDF - đã sửa để xử lý biến null
     */
    private void addCustomerInfoToPDF(Document document) throws DocumentException {
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        // Thêm tiêu đề phần khách hàng
        PdfPCell titleCell = new PdfPCell(new Phrase(isVietnamese ? "THONG TIN NGUOI NHAN" : "RECIPIENT INFORMATION", boldFont));
        titleCell.setColspan(2);
        titleCell.setPadding(8);
        titleCell.setBackgroundColor(new BaseColor(230, 230, 250));
        table.addCell(titleCell);

        // Lấy dữ liệu từ currentOrder thay vì từ label để tránh lỗi null
        addTableRow(table, isVietnamese ? "Ho ten:" : "Full Name:", currentOrder.getRecipientName(), boldFont, normalFont);
        addTableRow(table, isVietnamese ? "So dien thoai:" : "Phone:", currentOrder.getRecipientPhone(), boldFont, normalFont);
        addTableRow(table, isVietnamese ? "Dia chi:" : "Address:", currentOrder.getShippingAddress(), boldFont, normalFont);

        document.add(table);
    }

    /**
     * Tiện ích để thêm một hàng vào bảng PDF
     */
    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    /**
     * Thêm bảng sản phẩm vào PDF - đã sửa font
     */
    private void addProductTableToPDF(Document document) throws DocumentException {
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

        // Thêm tiêu đề phần sản phẩm
        Paragraph productTitle = new Paragraph(isVietnamese ? "CHI TIET SAN PHAM" : "PRODUCT DETAILS", boldFont);
        productTitle.setSpacingBefore(15);
        document.add(productTitle);

        // Tạo bảng sản phẩm
        PdfPTable table = new PdfPTable(6); // 6 cột
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        float[] columnWidths = {3f, 0.7f, 1.5f, 1.5f, 1.3f, 2f};
        try {
            table.setWidths(columnWidths);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        // Header cho bảng
        String[] columnHeaders = isVietnamese ?
                new String[]{"Ten san pham", "SL", "Don gia", "Loai BH", "Gia BH", "Thanh tien"} :
                new String[]{"Product Name", "Qty", "Unit Price", "Warranty", "Warranty Price", "Subtotal"};

        for (String columnHeader : columnHeaders) {
            PdfPCell cell = new PdfPCell(new Phrase(columnHeader, boldFont));
            cell.setBackgroundColor(new BaseColor(240, 240, 240));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Lấy dữ liệu từ bảng JavaFX
        NumberFormat currencyFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        ObservableList<OrderDetail> items = productTableView.getItems();

        // Dữ liệu sản phẩm
        for (OrderDetail item : items) {
            // Tên sản phẩm
            PdfPCell nameCell = new PdfPCell(new Phrase(item.getProduct().getProductName(), normalFont));
            nameCell.setPadding(5);
            table.addCell(nameCell);

            // Số lượng
            PdfPCell quantityCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
            quantityCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            quantityCell.setPadding(5);
            table.addCell(quantityCell);

            // Đơn giá
            PdfPCell priceCell = new PdfPCell(new Phrase(currencyFormatter.format(item.getUnitPrice()) + "d", normalFont));
            priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            priceCell.setPadding(5);
            table.addCell(priceCell);

            // Loại bảo hành
            PdfPCell warrantyTypeCell = new PdfPCell(new Phrase(item.getWarrantyType(), normalFont));
            warrantyTypeCell.setPadding(5);
            table.addCell(warrantyTypeCell);

            // Giá bảo hành
            PdfPCell warrantyCell = new PdfPCell(new Phrase(currencyFormatter.format(item.getWarrantyPrice()) + "d", normalFont));
            warrantyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            warrantyCell.setPadding(5);
            table.addCell(warrantyCell);

            // Thành tiền
            PdfPCell subtotalCell = new PdfPCell(new Phrase(currencyFormatter.format(item.getSubtotal()) + "d", normalFont));
            subtotalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            subtotalCell.setPadding(5);
            table.addCell(subtotalCell);
        }

        document.add(table);
    }

    /**
     * Thêm phần tổng cộng vào PDF - đã sửa font
     */
    private void addTotalSectionToPDF(Document document) throws DocumentException {
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);
        Font totalFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD);

        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(40);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.setSpacingBefore(15);

        // Số lượng sản phẩm
        addTableRow(totalTable, isVietnamese ? "Tong san pham:" : "Total Products:",
                totalProductsLabel.getText(), boldFont, normalFont);

        // Phí vận chuyển
        addTableRow(totalTable, isVietnamese ? "Phi van chuyen:" : "Shipping Fee:",
                shippingFeeLabel.getText(), boldFont, normalFont);

        // Tổng thanh toán
        PdfPCell totalLabelCell = new PdfPCell(new Phrase(isVietnamese ? "TONG THANH TOAN:" : "TOTAL PAYMENT:", totalFont));
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        totalLabelCell.setPadding(5);
        totalLabelCell.setBorderWidth(0);
        totalTable.addCell(totalLabelCell);

        PdfPCell totalValueCell = new PdfPCell(new Phrase(totalPriceLabel.getText(), totalFont));
        totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValueCell.setPadding(5);
        totalValueCell.setBorderWidth(0);
        totalTable.addCell(totalValueCell);

        document.add(totalTable);
    }

    /**
     * Thêm footer vào PDF - đã sửa font
     */
    private void addFooterToPDF(Document document) throws DocumentException {
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC);

        document.add(new Paragraph("\n\n"));

        Paragraph thankYou = new Paragraph(isVietnamese ? "Cam on quy khach da mua hang tai CELLCOMP STORE!"
                : "Thank you for shopping at CELLCOMP STORE!", normalFont);
        thankYou.setAlignment(Element.ALIGN_CENTER);
        document.add(thankYou);

        Paragraph contactInfo = new Paragraph("Hotline: xxxxxx - Email: xxxxxxx@cellcomp.com - Facebook: xxxxxxx - Youtube: xxxxxxx", smallFont);
        contactInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(contactInfo);

        Paragraph dateInfo = new Paragraph((isVietnamese ? "Ngay xuat hoa don: " : "Invoice Date: ") +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), smallFont);
        dateInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(dateInfo);
    }

    private String previousScreen = "home"; // Mặc định là home

    /**
     * Thiết lập màn hình trước đó
     */
    public void setPreviousScreen(String screen) {
        this.previousScreen = screen;
    }

    @FXML
    private void goBack() {
        try {
            // Lưu trạng thái ngôn ngữ hiện tại
            LanguageManager.setVietnamese(isVietnamese);
            FXMLLoader loader;

            // Kiểm tra màn hình trước đó và điều hướng phù hợp
            if ("orderhistory".equalsIgnoreCase(previousScreen)) {
                // Quay lại màn hình OrderHistory
                loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/OrderHistory.fxml"));
                Parent root = loader.load();

                // Lấy Stage hiện tại
                Stage stage = (Stage) orderIdLabel.getScene().getWindow();

                // Chuyển sang giao diện OrderHistory
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle(isVietnamese ? "CELLCOMP STORE - Lịch sử mua hàng" : "CELLCOMP STORE - Order History");
                stage.show();

                // Đồng bộ ngôn ngữ
                OrderHistoryController controller = loader.getController();
                controller.updateLanguage();
            } else {
                // Quay lại trang chủ (Home)
                loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Home.fxml"));
                Parent root = loader.load();

                // Lấy Stage hiện tại
                Stage stage = (Stage) orderIdLabel.getScene().getWindow();

                // Chuyển sang giao diện Home
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("CELLCOMP STORE");
                stage.show();

                // Đồng bộ ngôn ngữ
                HomeController controller = loader.getController();
                controller.refreshLanguageDisplay();
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể quay lại màn hình trước: " + e.getMessage() :
                            "Cannot return to previous screen: " + e.getMessage()
            );
        }
    }
    private void openCustomerChangeScreen() {
        try {
            // Lưu trạng thái ngôn ngữ hiện tại
            LanguageManager.setVietnamese(isVietnamese);

            // Tải FXML thông tin tài khoản
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomerChange.fxml"));
            Parent root = loader.load();

            // Lấy Stage hiện tại
            Stage stage = (Stage) orderIdLabel.getScene().getWindow();

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

    @FXML
    private void switchLanguage() {
        isVietnamese = !isVietnamese;

        // Lưu trạng thái ngôn ngữ
        LanguageManager.setVietnamese(isVietnamese);

        // Cập nhật giao diện
        updateLanguage();
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Home.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) orderIdLabel.getScene().getWindow();
            stage.setTitle("CELLCOMP STORE");
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                isVietnamese ? "Lỗi" : "Error",
                isVietnamese ? "Không thể quay về trang chủ" : "Could not return to home page"
            );
        }
    }
    @FXML
    private void reviewProducts() {
        try {
            // Kiểm tra nếu đơn hàng đã được đánh giá
            if (!orderService.canReviewOrder(currentOrder.getOrderId())) {
                AlertUtils.showInfo(
                        isVietnamese ? "Thông báo" : "Information",
                        isVietnamese ? "Bạn đã đánh giá các sản phẩm trong đơn hàng này rồi." :
                                "You have already reviewed products in this order."
                );
                return;
            }

            // Tạo Stage mới cho dialog đánh giá
            Stage reviewStage = new Stage();
            reviewStage.initModality(Modality.APPLICATION_MODAL);
            reviewStage.setTitle(isVietnamese ? "Đánh giá sản phẩm" : "Product Review");

            // Tạo layout cho dialog
            VBox dialogVbox = new VBox(10);
            dialogVbox.setPadding(new Insets(20));
            dialogVbox.setAlignment(Pos.CENTER);
            dialogVbox.getStyleClass().add("review-dialog");

            // Tiêu đề
            Label titleLabel = new Label(isVietnamese ? "ĐÁNH GIÁ SẢN PHẨM" : "PRODUCT REVIEW");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            // Tạo TabPane để hiển thị từng sản phẩm trên 1 tab
            TabPane reviewTabPane = new TabPane();
            reviewTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

            // Lặp qua từng sản phẩm để tạo tab đánh giá
            for (OrderDetail detail : orderDetails) {
                // Tạo tab cho sản phẩm
                Tab tab = new Tab(detail.getProduct().getProductName());

                // Layout cho nội dung tab
                VBox tabContent = new VBox(15);
                tabContent.setPadding(new Insets(20));
                tabContent.setAlignment(Pos.CENTER);

                // Thông tin sản phẩm
                HBox productInfoBox = new HBox(10);
                productInfoBox.setAlignment(Pos.CENTER);

                // Thêm hình ảnh sản phẩm nếu có
                try {
                    ImageView productImage = new ImageView(new Image(detail.getProduct().getImagePath()));
                    productImage.setFitHeight(100);
                    productImage.setFitWidth(100);
                    productImage.setPreserveRatio(true);
                    productInfoBox.getChildren().add(productImage);
                } catch (Exception e) {
                    // Nếu không có hình ảnh hoặc lỗi, bỏ qua
                }

                // Tên sản phẩm
                Label productName = new Label(detail.getProduct().getProductName());
                productName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                productInfoBox.getChildren().add(productName);

                // Rating stars (1-5)
                HBox ratingBox = new HBox(5);
                ratingBox.setAlignment(Pos.CENTER);

                Label ratingLabel = new Label(isVietnamese ? "Đánh giá: " : "Rating: ");
                ratingBox.getChildren().add(ratingLabel);

                // Tạo ComboBox cho rating
                ComboBox<Integer> ratingComboBox = new ComboBox<>();
                ratingComboBox.getItems().addAll(1, 2, 3, 4, 5);
                ratingComboBox.setValue(5); // Default giá trị là 5 sao
                ratingComboBox.setPromptText(isVietnamese ? "Chọn số sao" : "Select rating");
                ratingBox.getChildren().add(ratingComboBox);

                // Text Area cho comment
                Label commentLabel = new Label(isVietnamese ? "Nhận xét:" : "Comment:");

                TextArea commentArea = new TextArea();
                commentArea.setPromptText(isVietnamese ? "Nhập nhận xét của bạn về sản phẩm..." :
                        "Enter your comment about the product...");
                commentArea.setPrefRowCount(5);
                commentArea.setPrefWidth(400);
                commentArea.setWrapText(true);

                // Lưu OrderDetail vào các control để sử dụng khi submit
                ratingComboBox.setUserData(detail);
                commentArea.setUserData(detail);

                // Thêm vào tab content
                tabContent.getChildren().addAll(productInfoBox, ratingBox, commentLabel, commentArea);

                // Set nội dung cho tab
                tab.setContent(tabContent);

                // Thêm tab vào TabPane
                reviewTabPane.getTabs().add(tab);
            }

            // Nút Submit
            Button submitButton = new Button(isVietnamese ? "Gửi đánh giá" : "Submit Review");
            submitButton.getStyleClass().add("primary-button");
            submitButton.setPrefWidth(200);
            submitButton.setPrefHeight(40);
            submitButton.setStyle("-fx-background-color: #865DFF; -fx-text-fill: white; -fx-font-weight: bold;");

            // Xử lý sự kiện submit
            submitButton.setOnAction(event -> {
                try {
                    boolean allValid = true;
                    List<ProductReview> reviews = new ArrayList<>();

                    // Lặp qua các tab để lấy đánh giá
                    for (Tab tab : reviewTabPane.getTabs()) {
                        VBox tabContent = (VBox) tab.getContent();

                        // Lấy rating và comment từ mỗi tab
                        ComboBox<Integer> ratingBox = null;
                        TextArea commentArea = null;

                        for (Node node : tabContent.getChildren()) {
                            if (node instanceof HBox && ((HBox) node).getChildren().size() > 1) {
                                Node potentialComboBox = ((HBox) node).getChildren().get(1);
                                if (potentialComboBox instanceof ComboBox) {
                                    ratingBox = (ComboBox<Integer>) potentialComboBox;
                                }
                            } else if (node instanceof TextArea) {
                                commentArea = (TextArea) node;
                            }
                        }

                        if (ratingBox == null || commentArea == null) {
                            continue;
                        }

                        // Lấy OrderDetail từ userData
                        OrderDetail detail = (OrderDetail) ratingBox.getUserData();

                        // Kiểm tra dữ liệu nhập
                        Integer rating = ratingBox.getValue();
                        String comment = commentArea.getText();

                        if (rating == null) {
                            AlertUtils.showWarning(
                                    isVietnamese ? "Cảnh báo" : "Warning",
                                    isVietnamese ? "Vui lòng chọn số sao đánh giá cho sản phẩm: " + detail.getProduct().getProductName()
                                            : "Please select rating for product: " + detail.getProduct().getProductName()
                            );
                            allValid = false;
                            break;
                        }

                        // Tạo đối tượng ProductReview
                        ProductReview review = new ProductReview();
                        review.setProductID(detail.getProduct().getProductID());
                        review.setCustomerID(authService.getCurrentCustomer().getId());
                        review.setOrderDetailsID(detail.getOrderDetailsId());
                        review.setRating(rating);
                        review.setComment(comment);
                        review.setReviewDate(LocalDateTime.now());
                        review.setApproved(false); // Default là chưa duyệt

                        reviews.add(review);
                    }

                    // Nếu tất cả dữ liệu hợp lệ, lưu đánh giá
                    if (allValid && !reviews.isEmpty()) {
                        boolean success = saveProductReviews(reviews);

                        if (success) {
                            AlertUtils.showInfo(
                                    isVietnamese ? "Thành công" : "Success",
                                    isVietnamese ? "Đánh giá của bạn đã được gửi thành công"
                                            : "Your review has been submitted successfully"
                            );
                            reviewStage.close();
                        } else {
                            AlertUtils.showError(
                                    isVietnamese ? "Lỗi" : "Error",
                                    isVietnamese ? "Có lỗi xảy ra khi gửi đánh giá. Vui lòng thử lại sau."
                                            : "An error occurred while submitting reviews. Please try again later."
                            );
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AlertUtils.showError(
                            isVietnamese ? "Lỗi" : "Error",
                            isVietnamese ? "Có lỗi xảy ra: " + e.getMessage()
                                    : "An error occurred: " + e.getMessage()
                    );
                }
            });

            // Thêm tất cả vào dialog
            dialogVbox.getChildren().addAll(titleLabel, reviewTabPane, submitButton);

            // Tạo scene và hiển thị
            Scene dialogScene = new Scene(dialogVbox, 600, 500);
            dialogScene.getStylesheets().add(getClass().getResource("/com/example/stores/css/OrderHistory.css").toExternalForm());

            reviewStage.setScene(dialogScene);
            reviewStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Có lỗi xảy ra khi mở form đánh giá sản phẩm: " + e.getMessage()
                            : "An error occurred while opening product review form: " + e.getMessage()
            );
        }
    }

    // Method hỗ trợ để lưu đánh giá
    private boolean saveProductReviews(List<ProductReview> reviews) {
        try {
            // Tạo service để lưu đánh giá
            // Giả định rằng có một ReviewService để xử lý việc này
            ReviewService reviewService = new ReviewService();

            // Lưu tất cả đánh giá
            for (ProductReview review : reviews) {
                boolean success = reviewService.saveReview(review);
                if (!success) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Opens the Custom PC Builder interface
     */
    private void openCustomDesignScreen() {
        try {
            // Save language setting before navigation
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
        try {
            // Save language setting before navigation
            LanguageManager.setVietnamese(isVietnamese);

            // Load the OrderHistory interface
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
                    isVietnamese ? "Không thể mở trang lịch sử đơn hàng: " + e.getMessage() :
                            "Cannot open order history page: " + e.getMessage()
            );
        }
    }
    // Thêm khai báo biến này vào phần đầu của lớp
    @FXML private Button reviewButton;
    /**
     * Cập nhật hiển thị của nút đánh giá dựa trên nguồn truy cập
     */
    private void updateReviewButtonVisibility() {
        if (reviewButton != null) {
            // Chỉ hiển thị nút đánh giá khi truy cập từ lịch sử đơn hàng
            boolean shouldShowReviewButton = "history".equals(accessSource) && orderService.canReviewOrder(currentOrder.getOrderId());
            reviewButton.setVisible(shouldShowReviewButton);
            reviewButton.setManaged(shouldShowReviewButton); // Để layout tự điều chỉnh khi ẩn nút
        }
    }

}