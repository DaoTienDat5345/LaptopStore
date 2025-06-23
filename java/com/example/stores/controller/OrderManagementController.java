package com.example.stores.controller;

import com.example.stores.model.Customer;
import com.example.stores.model.Order;
import com.example.stores.model.OrderDetail;
import com.example.stores.repository.*; // Import các interface repository
import com.example.stores.repository.impl.*; // Import các lớp impl repository
import com.example.stores.service.*; // Import các interface service
import com.example.stores.service.impl.*; // Import các lớp impl service

import com.example.stores.util.LanguageForManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Optional;

public class OrderManagementController {

    //<editor-fold desc="FXML Controls - Lọc Đơn Hàng">
    @FXML private DatePicker dpOrderFilterStartDate;
    @FXML private DatePicker dpOrderFilterEndDate;
    @FXML private PieChart orderRatingPieChart;
    @FXML private ComboBox<Customer> cmbOrderFilterCustomer;
    @FXML private Label lblDetailOrderStatus; // Giữ lại Label này để hiển thị trạng thái hiện tại
    @FXML private ComboBox<String> cmbDetailOrderStatus; // Thêm lại ComboBox
    @FXML private ComboBox<String> cmbOrderFilterStatus;
    @FXML private Button btnUpdateOrderStatus;
    @FXML private Button btnFilterOrders;
    @FXML private Button btnRefreshOrderTable;
    //</editor-fold>

    //<editor-fold desc="FXML Controls - Bảng Danh sách Đơn Hàng">
    @FXML private TableView<Order> orderTableView;
    @FXML private TableColumn<Order, String> colOrderID;
    @FXML private TableColumn<Order, LocalDateTime> colOrderDate; // Sửa lại kiểu thành LocalDateTime
    @FXML private TableColumn<Order, String> colOrderCustomerName;
    //@FXML private TableColumn<Order, String> colOrderEmployeeName;
    @FXML private TableColumn<Order, Double> colOrderTotalAmount;
    @FXML private TableColumn<Order, Integer> colOrderRating;     // << THÊM KHAI BÁO @FXML
    @FXML private TableColumn<Order, String> colOrderStatus;

    // Bỏ cột OrderStatus theo yêu cầu
    //</editor-fold>

    //<editor-fold desc="FXML Controls - Panel Chi Tiết Đơn Hàng">
    @FXML private VBox orderDetailPanel;
    @FXML private Label lblOrderDetailTitle;
    @FXML private Label lblDetailOrderID;
    @FXML private Label lblDetailCustomerName;
    @FXML private Label lblDetailOrderDate;
    @FXML private Label lblDetailEmployeeName;
    @FXML private Label lblDetailTotalAmount;
    // Các control liên quan đến status đã bỏ
    @FXML private TableView<OrderDetail> orderDetailTableView;
    @FXML private TableColumn<OrderDetail, String> colDetailProductName;
    @FXML private TableColumn<OrderDetail, Integer> colDetailQuantity;
    @FXML private TableColumn<OrderDetail, BigDecimal> colDetailUnitPrice;
    @FXML private TableColumn<OrderDetail, BigDecimal> colDetailSubtotal;

    //</editor-fold>

    private OrderService orderService;
    private CustomerService customerService;
    private OrderDetailService orderDetailService; // Được inject vào OrderServiceImpl

    private final ObservableList<Order> orderList = FXCollections.observableArrayList();
    private final ObservableList<Customer> customerListForFilter = FXCollections.observableArrayList();
    private final ObservableList<OrderDetail> currentOrderDetailsList = FXCollections.observableArrayList();

    private Order selectedOrder;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public OrderManagementController() {
        // Khởi tạo Repository
        OrderRepository orderRepository = new OrderRepositoryImpl();
        OrderDetailRepository orderDetailRepository = new OrderDetailRepositoryImpl();
        CustomerRepository customerRepository = new CustomerRepositoryImpl();
        ProductReviewRepository productReviewRepository = new ProductReviewRepositoryImpl(); // << THÊM KHỞI TẠO NÀY

        // Các repo khác có thể cần cho service con
        ProductRepository productRepository = new ProductRepositoryImpl();
        CategoryRepository categoryRepository = new CategoryRepositoryImpl();
        EmployeeRepository employeeRepository = new EmployeeRepositoryImpl();
        WorkShiftScheduleRepository workShiftScheduleRepository = new WorkShiftScheduleRepositoryImpl();
        ShiftRepository shiftRepository = new ShiftRepositoryImpl();

        // Khởi tạo Service
        this.customerService = new CustomerServiceImpl(customerRepository);
        this.orderDetailService = new OrderDetailServiceImpl(orderDetailRepository);
        ProductReviewService localProductReviewService = new ProductReviewServiceImpl(productReviewRepository); // << THÊM KHỞI TẠO NÀY

        // Khởi tạo các service phụ thuộc khác nếu OrderServiceImpl cần chúng
        // EmployeeService localEmployeeService = new EmployeeServiceImpl(employeeRepository,
        // new WorkShiftScheduleServiceImpl(workShiftScheduleRepository, employeeRepository, shiftRepository));

        // Sửa lại dòng khởi tạo OrderService
        this.orderService = new OrderServiceImpl(
                orderRepository,
                this.orderDetailService,
                localProductReviewService, // << TRUYỀN ProductReviewService
                customerRepository
        );
    }

    @FXML
    public void initialize() {
        setupOrderTableColumns();
        setupFilterComboBoxes();
        loadOrdersToTable();
        setupOrderTableSelectionListener();

        orderDetailTableView.setItems(currentOrderDetailsList);
        setupOrderDetailTableColumns();
        ObservableList<String> orderStatusOptions = FXCollections.observableArrayList(
                " xác nhận", "Đã hủy" // Khớp với CHECK constraint trong DB
        );
        cmbDetailOrderStatus.setItems(orderStatusOptions);
        btnUpdateOrderStatus.setDisable(true);
        if (orderDetailPanel != null) { // Kiểm tra null
            orderDetailPanel.setVisible(false);
            orderDetailPanel.setManaged(false);
        }

        if (dpOrderFilterStartDate != null) dpOrderFilterStartDate.setValue(LocalDate.now().minusMonths(1));
        if (dpOrderFilterEndDate != null) dpOrderFilterEndDate.setValue(LocalDate.now());
        LanguageForManager.getInstance().currentLocaleProperty().addListener((obs, oldL, newL) -> updateUITexts());
        updateUITexts(); // Gọi lần đầu
    }

    private void updateUITexts() {
        LanguageForManager lm = LanguageForManager.getInstance();

        // Cập nhật bộ lọc
        // (Label đã dùng %key)
        if (cmbOrderFilterCustomer != null) cmbOrderFilterCustomer.setPromptText(lm.getString("prompt.allCustomers"));
        if (cmbOrderFilterStatus != null) cmbOrderFilterStatus.setPromptText(lm.getString("prompt.allStatuses"));
        if (btnFilterOrders != null) btnFilterOrders.setText(lm.getString("button.filterOrders"));
        if (btnRefreshOrderTable != null) btnRefreshOrderTable.setText(lm.getString("button.refreshTable"));


        // Cập nhật header cột bảng Order
        if (colOrderID != null) colOrderID.setText(lm.getString("table.col.orderId"));
        // ... các cột khác của orderTableView ...
        if (colOrderRating != null) colOrderRating.setText(lm.getString("table.col.rating"));
        if (colOrderStatus != null) colOrderStatus.setText(lm.getString("table.col.orderStatus"));
        if (orderTableView != null) orderTableView.setPlaceholder(new Label(lm.getString("table.placeholder.noOrders")));

        // Cập nhật panel chi tiết đơn hàng
        if (lblOrderDetailTitle != null) lblOrderDetailTitle.setText(selectedOrder == null ? lm.getString("order.detail.title") : MessageFormat.format(lm.getString("order.detail.title.withId"), selectedOrder.getOrderID()));
        // ... các Label tĩnh khác trong panel chi tiết ...
        if (cmbDetailOrderStatus != null) cmbDetailOrderStatus.setPromptText(lm.getString("prompt.selectStatus"));
        if (btnUpdateOrderStatus != null) btnUpdateOrderStatus.setText(lm.getString("button.updateStatus"));
        if (orderDetailTableView != null) orderDetailTableView.setPlaceholder(new Label(lm.getString("table.placeholder.noOrderDetails")));
        // ... các header cột của orderDetailTableView ...

        // Cập nhật TitledPane và PieChart
        // (Giả sử có fx:id cho TitledPane: fx:id="ratingChartTitledPane")
        // if (ratingChartTitledPane != null) ratingChartTitledPane.setText(lm.getString("order.ratingChart.title"));
        if (orderRatingPieChart != null) {
            // Tiêu đề PieChart có thể cần cập nhật lại nếu nó không lấy từ TitledPane
            // Hoặc nếu title của PieChart được set trong FXML là %key thì không cần
            orderRatingPieChart.setTitle(lm.getString("order.ratingChart.pieTitle"));
            // Load lại dữ liệu biểu đồ để legend (chú thích) cũng được dịch nếu cần
            // (nếu text của PieChart.Data là "X Sao")
            loadOrderRatingChartData();
        }


        // Cập nhật ComboBox lọc trạng thái
        if (cmbOrderFilterStatus != null) {
            ObservableList<String> statusOptions = FXCollections.observableArrayList(
                    lm.getString("prompt.allStatuses"), // Key cho "Tất cả trạng thái"
                    lm.getString("order.status.pending"), // Key cho "Chờ xác nhận"
                    lm.getString("order.status.processing"),// Key cho "Đang xử lý"
                    lm.getString("order.status.shipping"),  // Key cho "Đang giao"
                    lm.getString("order.status.completed"), // Key cho "Hoàn thành"
                    lm.getString("order.status.cancelled")  // Key cho "Đã hủy"
            );
            String currentFilterStatus = cmbOrderFilterStatus.getValue();
            cmbOrderFilterStatus.setItems(statusOptions);
            if (currentFilterStatus != null && statusOptions.contains(currentFilterStatus)) {
                cmbOrderFilterStatus.setValue(currentFilterStatus);
            } else {
                cmbOrderFilterStatus.getSelectionModel().selectFirst();
            }
        }
        // Cập nhật ComboBox trạng thái trong panel chi tiết
        if (cmbDetailOrderStatus != null) {
            ObservableList<String> detailStatusOptions = FXCollections.observableArrayList(
                    lm.getString("order.status.pending"), lm.getString("order.status.processing"),
                    lm.getString("order.status.shipping"), lm.getString("order.status.completed"),
                    lm.getString("order.status.cancelled")
            );
            String currentDetailStatus = cmbDetailOrderStatus.getValue();
            cmbDetailOrderStatus.setItems(detailStatusOptions);
            if (currentDetailStatus != null && detailStatusOptions.contains(currentDetailStatus)) {
                cmbDetailOrderStatus.setValue(currentDetailStatus);
            } // Không chọn mặc định ở đây, sẽ được set khi chọn đơn hàng
        }

        // Tải lại bảng chính để áp dụng các thay đổi text (nếu cần thiết, thường FXML reload đã đủ)
        loadOrdersToTable();

        // Nếu có đơn hàng đang được chọn, cập nhật lại thông tin chi tiết
        if (selectedOrder != null) {
            displayOrderDetail(selectedOrder);
        }
    }

    private void loadOrderRatingChartData() {
        if (orderRatingPieChart == null) { // Kiểm tra null đề phòng
            System.err.println("Lỗi: orderRatingPieChart chưa được inject từ FXML.");
            return;
        }

        orderRatingPieChart.getData().clear(); // Xóa dữ liệu cũ trên biểu đồ

        try {
            LanguageForManager lm = LanguageForManager.getInstance(); // Lấy LanguageForManager để dịch text
            List<Map<String, Object>> ratingStats = orderService.getOrderRatingStatistics(); // Gọi service

            if (ratingStats == null || ratingStats.isEmpty()) {
                orderRatingPieChart.setTitle(lm.getString("order.ratingChart.noRating")); // Key cho "Chưa có đánh giá nào"
                // Có thể thêm một PieChart.Data "Trống" nếu muốn biểu đồ không hoàn toàn rỗng
                // PieChart.Data emptySlice = new PieChart.Data(lm.getString("label.empty"), 1);
                // orderRatingPieChart.getData().add(emptySlice);
                return;
            }

            // Đặt lại tiêu đề chính của biểu đồ (nếu nó không được set bằng %key trong FXML)
            orderRatingPieChart.setTitle(lm.getString("order.ratingChart.pieTitle"));

            for (Map<String, Object> stat : ratingStats) {
                Integer ratingValue = (Integer) stat.get("rating"); // Lấy giá trị rating (1-5)
                Integer count = (Integer) stat.get("count");       // Lấy số lượng đơn hàng có rating đó

                if (ratingValue != null && count != null && count > 0) {
                    // Tạo nhãn cho mỗi phần của biểu đồ, ví dụ: "5 Sao (10 đơn)"
                    String sliceLabel = MessageFormat.format(lm.getString("order.ratingChart.starLabel"), ratingValue, count);
                    PieChart.Data slice = new PieChart.Data(sliceLabel, count.doubleValue());
                    orderRatingPieChart.getData().add(slice);
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "alert.title.error", "error.load.ratingChart"); // Sử dụng key
            e.printStackTrace();
            if (orderRatingPieChart != null) { // Đặt tiêu đề lỗi nếu có
                orderRatingPieChart.setTitle(LanguageForManager.getInstance().getString("error.load.ratingChart"));
            }
        }
    }


    private void setupOrderTableColumns() {
        // Cột Mã Đơn Hàng
        colOrderID.setCellValueFactory(new PropertyValueFactory<>("orderID"));

        // Cột Ngày Đặt (Định dạng LocalDateTime)
        colOrderDate.setCellValueFactory(new PropertyValueFactory<>("orderDate")); // PropertyValueFactory trả về LocalDateTime
        colOrderDate.setCellFactory(column -> {
            return new TableCell<Order, LocalDateTime>() { // Cell xử lý LocalDateTime
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(dateTimeFormatter)); // Sử dụng dateTimeFormatter đã khai báo
                    }
                }
            };
        });

        // Cột Tên Khách Hàng
        colOrderCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerFullNameDisplay"));

        // Cột Tổng Tiền (Định dạng tiền tệ)
        colOrderTotalAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colOrderTotalAmount.setCellFactory(tc -> new TableCell<Order, Double>() { // << SỬA KIỂU
            @Override
            protected void updateItem(Double item, boolean empty) { // << SỬA KIỂU
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormatter.format(item)); // currencyFormatter nhận double
                setAlignment(Pos.CENTER_RIGHT);
            }
        });

        // Cột Đánh Giá (MỚI - hiển thị Integer từ representativeOrderRating)
        colOrderRating.setCellValueFactory(new PropertyValueFactory<>("representativeOrderRating"));
        colOrderRating.setCellFactory(column -> new TableCell<Order, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0) { // Coi 0 hoặc null là chưa đánh giá
                    setText("Chưa ĐG");
                } else {
                    setText(item.toString() + " ⭐"); // Hiển thị số sao
                }
                setAlignment(Pos.CENTER); // Căn giữa
            }
        });

        // Cột Trạng Thái ĐH (MỚI - hiển thị String từ orderStatus)
        colOrderStatus.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        // Không cần cellFactory đặc biệt nếu chỉ hiển thị String, nhưng có thể thêm để style
        colOrderStatus.setCellFactory(column -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    // Có thể thêm style dựa trên trạng thái ở đây nếu muốn
                    // Ví dụ:
                    // if ("Hoàn thành".equalsIgnoreCase(item)) {
                    //     setTextFill(javafx.scene.paint.Color.GREEN);
                    // } else if ("Đã hủy".equalsIgnoreCase(item)) {
                    //     setTextFill(javafx.scene.paint.Color.RED);
                    // } else {
                    //     setTextFill(javafx.scene.paint.Color.BLACK);
                    // }
                }
            }
        });
    }

    private void setupFilterComboBoxes() {
        try {
            List<Customer> customers = customerService.getAllActiveCustomers(); // Hoặc getAllCustomers() nếu muốn cả inactive
            customerListForFilter.clear(); // Xóa dữ liệu cũ
            customerListForFilter.add(null); // Tùy chọn "Tất cả khách hàng"
            customerListForFilter.addAll(customers);
            cmbOrderFilterCustomer.setItems(customerListForFilter);
            cmbOrderFilterCustomer.setConverter(new StringConverter<>() {
                @Override public String toString(Customer c) { return c == null ? "Tất cả khách hàng" : c.getFullName();}
                @Override public Customer fromString(String s) { return null;}
            });
            if (!customerListForFilter.isEmpty()) { // Chọn item đầu tiên nếu list không rỗng
                cmbOrderFilterCustomer.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Khách Hàng", "Không thể tải danh sách khách hàng cho bộ lọc.");
            e.printStackTrace();
        }
    }

    private void loadOrdersToTable() {
        LocalDate startDate = dpOrderFilterStartDate.getValue();
        LocalDate endDate = dpOrderFilterEndDate.getValue();
        Customer selectedCustomer = cmbOrderFilterCustomer.getValue();
        Integer customerIdFilter = (selectedCustomer != null) ? selectedCustomer.getCustomerID() : null;

        try {
            List<Order> orders = orderService.findOrdersByCriteria(startDate, endDate, customerIdFilter);
            orderList.setAll(orders);
            orderTableView.setItems(orderList);
            orderTableView.setPlaceholder(new Label(orders.isEmpty() ? "Không có đơn hàng nào khớp điều kiện." : null));
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Lọc Đơn Hàng", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Đơn Hàng", "Không thể tải danh sách đơn hàng.");
            e.printStackTrace();
        }
        clearOrderDetailPanel(); // Xóa chi tiết khi tải lại bảng chính
    }

    private void setupOrderTableSelectionListener() {
        orderTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedOrder = newSelection;
                displayOrderDetail(selectedOrder);
            } else {
                clearOrderDetailPanel();
            }
        });
    }

    private void displayOrderDetail(Order order) {
        if (order == null) {
            clearOrderDetailPanel();
            return;
        }
        if (orderDetailPanel == null) return; // Kiểm tra null cho panel

        orderDetailPanel.setVisible(true);
        orderDetailPanel.setManaged(true);

        lblOrderDetailTitle.setText("Chi Tiết Đơn Hàng ID: " + order.getOrderID());
        lblDetailOrderID.setText(order.getOrderID());
        lblDetailCustomerName.setText(order.getCustomerFullNameDisplay() != null ? order.getCustomerFullNameDisplay() : "N/A");
        lblDetailOrderDate.setText(order.getOrderDate() != null ? order.getOrderDate().format(dateTimeFormatter) : "N/A");
        lblDetailEmployeeName.setText(order.getEmployeeFullNameDisplay() != null ? order.getEmployeeFullNameDisplay() : "N/A");
        lblDetailTotalAmount.setText(currencyFormatter.format(order.getTotalAmount()));

        // Load chi tiết từ đối tượng Order (đã được service điền vào)
        if (order.getDetails() != null) {
            currentOrderDetailsList.setAll(order.getDetails());
        } else {
            currentOrderDetailsList.clear(); // Xóa nếu không có chi tiết
        }
        orderDetailTableView.setPlaceholder(new Label(currentOrderDetailsList.isEmpty() ? "Đơn hàng này không có chi tiết sản phẩm." : null));
    }

    private void clearOrderDetailPanel() {
        if (orderDetailPanel == null) return;
        orderDetailPanel.setVisible(false);
        orderDetailPanel.setManaged(false);
        if (lblOrderDetailTitle != null) lblOrderDetailTitle.setText("Chi Tiết Đơn Hàng");
        // ... clear các label khác ...
        if (currentOrderDetailsList != null) currentOrderDetailsList.clear();
    }

    private void setupOrderDetailTableColumns() {
        colDetailProductName.setCellValueFactory(new PropertyValueFactory<>("productNameDisplay"));
        colDetailQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colDetailQuantity.setStyle("-fx-alignment: CENTER-RIGHT;");

        colDetailUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPriceAtPurchase")); // Dùng getter đã tính
        colDetailUnitPrice.setCellFactory(tc -> new TableCell<OrderDetail, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormatter.format(item.doubleValue()));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
        colDetailSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colDetailSubtotal.setCellFactory(tc -> new TableCell<OrderDetail, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormatter.format(item.doubleValue()));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
    }

    @FXML
    void handleFilterOrdersAction(ActionEvent event) {
        loadOrdersToTable();
    }

    @FXML
    void handleRefreshOrderTableAction(ActionEvent event) {
        if (dpOrderFilterStartDate != null) dpOrderFilterStartDate.setValue(LocalDate.now().minusMonths(1));
        if (dpOrderFilterEndDate != null) dpOrderFilterEndDate.setValue(LocalDate.now());
        if (cmbOrderFilterCustomer != null && !customerListForFilter.isEmpty()) { // Sử dụng customerListForFilter
            cmbOrderFilterCustomer.getSelectionModel().selectFirst();
        }
        loadOrdersToTable();
        showAlert(Alert.AlertType.INFORMATION, "Làm mới", "Đã làm mới danh sách đơn hàng.");
    }
    @FXML
    void handleUpdateOrderStatusAction(ActionEvent event) {
        if (selectedOrder == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn đơn hàng", "Vui lòng chọn một đơn hàng.");
            return;
        }
        String newStatus = cmbDetailOrderStatus.getValue();
        if (newStatus == null || newStatus.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn trạng thái mới.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Cập nhật trạng thái đơn hàng ID: " + selectedOrder.getOrderID() + " thành '" + newStatus + "'?",
                ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Xác nhận cập nhật");
        confirmation.setHeaderText(null);

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                boolean success = orderService.updateOrderStatus(selectedOrder.getOrderID(), newStatus);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật trạng thái đơn hàng.");
                    selectedOrder.setOrderStatus(newStatus); // Cập nhật model local
                    // Tải lại bảng chính để cập nhật trạng thái trên đó
                    loadOrdersToTable();
                    // Cập nhật lại panel chi tiết nếu nó đang hiển thị đơn hàng này
                    if (orderDetailPanel.isVisible() && selectedOrder != null && selectedOrder.getOrderID().equals(lblDetailOrderID.getText())) {
                        cmbDetailOrderStatus.setValue(newStatus); // Cập nhật ComboBox
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật trạng thái đơn hàng.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}