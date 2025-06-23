// package com.example.stores.controller;

// import com.example.stores.model.OrderHistory;
// import com.example.stores.model.OrderDetail;
// import com.example.stores.service.OrderHistoryServiceE;
// import javafx.scene.control.cell.PropertyValueFactory;
// import javafx.scene.control.CheckBox;

// import java.io.IOException;
// import java.io.InputStream;
// import java.net.URL;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.sql.Statement;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.List;
// import java.util.Optional;
// import java.util.logging.Level;
// import java.util.logging.Logger;
// import java.util.stream.Collectors;

// import com.example.stores.util.AlertUtil; // Chú ý: đây là AlertUtil (không có s)
// import com.example.stores.util.WarrantyCalculator;

// import com.example.stores.model.Customer;
// import com.example.stores.service.CustomerServiceE;

// import javafx.scene.control.RadioButton;
// import javafx.scene.control.ToggleGroup;
// import javafx.scene.layout.BorderPane;
// import javafx.scene.layout.GridPane;
// import javafx.scene.control.ScrollPane;
// import javafx.beans.property.SimpleStringProperty;
// import javafx.beans.property.SimpleIntegerProperty;
// import javafx.beans.property.SimpleDoubleProperty;

// import javafx.scene.layout.*;
// import javafx.geometry.Pos;
// import javafx.scene.control.Label;
// import javafx.scene.control.Button;
// import javafx.scene.image.Image;
// import javafx.scene.image.ImageView;
// import javafx.geometry.Insets;

// import javafx.collections.ObservableList;
// import com.example.stores.config.DBConfig;
// import com.example.stores.model.CartItemEmployee;
// import com.example.stores.model.Product;
// import com.example.stores.model.Employee;
// import com.example.stores.model.Warranty; // Thêm import cho Warranty

// import com.example.stores.model.Order;

// import javafx.scene.shape.Circle;
// import javafx.scene.Cursor;
// import javafx.collections.FXCollections;
// import javafx.fxml.FXML;
// import javafx.fxml.FXMLLoader;
// import javafx.print.PrinterJob;
// import javafx.scene.Parent;
// import javafx.scene.Scene;
// import javafx.scene.control.Alert;
// import javafx.scene.control.ButtonType;
// import javafx.scene.control.ComboBox;
// import javafx.scene.control.Separator;
// import javafx.scene.control.TableCell;
// import javafx.scene.control.TableColumn;
// import javafx.scene.control.TableView;
// import javafx.scene.control.TextArea;
// import javafx.scene.control.TextField;
// import javafx.scene.layout.VBox;
// import javafx.stage.Modality;
// import javafx.stage.Stage;

// public class PosOverviewControllerE {
//     private static final Logger LOGGER = Logger.getLogger(PosOverviewControllerE.class.getName());

//     @FXML private FlowPane productFlowPane;
//     @FXML private TableView<CartItemEmployee> cartTable;
//     @FXML private TableColumn<CartItemEmployee, String> colCartName;
//     @FXML private TableColumn<CartItemEmployee, Integer> colCartQty;
//     @FXML private TableColumn<CartItemEmployee, Double> colCartPrice;
//     @FXML private TableColumn<CartItemEmployee, Double> colCartTotal;
//     @FXML private TableColumn<CartItemEmployee, String> colCartWarranty; // Thêm khai báo biến cho cột bảo hành
//     @FXML private Label lblTotal;
//     // Cập nhật ComboBox lọc theo DB mới (bỏ RAM/CPU, giữ lại category)
//     @FXML private ComboBox<String> cbCategory;
//     @FXML private ComboBox<String> cbSort; // Thêm ComboBox sắp xếp
//     @FXML private TextField txtSearch;
//     @FXML private Button btnFilter, btnCheckout;
//     @FXML private VBox cartItemsContainer; // Container cho các item trong giỏ hàng

//     private int productLimit = 20; // Số sản phẩm hiển thị ban đầu
//     private List<Product> currentFilteredProducts = new ArrayList<>();

//     private ObservableList<Product> products = FXCollections.observableArrayList();
//     private ObservableList<CartItemEmployee> cartItems = FXCollections.observableArrayList();
//     private TableColumn<CartItemEmployee, Void> colCartAction; // Cột chứa nút xóa

//     private int employeeId;

//     /**
//      * Thêm sản phẩm vào giỏ hàng - Method công khai cho ProductDetailController gọi
//      * @param item Sản phẩm cần thêm vào giỏ
//      */
//     public void addToCart(CartItemEmployee item) {
//         // Gọi đến phương thức addToCartWithWarranty đã có sẵn
//         addToCartWithWarranty(item);
//         LOGGER.info("✅ Đã thêm sản phẩm " + item.getProductName() + " vào giỏ hàng từ ProductDetailController");
//     }

//     /**
//      * Lấy tên người dùng hiện tại
//      * @return tên đăng nhập người dùng hiện tại
//      */
//     public String getCurrentUser() {
//         return this.currentUser;
//     }

//     // Thêm biến để lưu lịch sử đơn hàng trong session
//     private List<OrderSummary> orderHistory = new ArrayList<>();

//     // Thêm vào class PosOverviewController
//     private void addEmployeeInfoButton() {
//         try {
//             if (currentEmployee == null || btnCheckout == null || btnCheckout.getParent() == null ||
//                     !(btnCheckout.getParent().getParent() instanceof BorderPane)) {
//                 LOGGER.warning("Không thể thêm nút thông tin nhân viên: currentEmployee hoặc btnCheckout null");
//                 return;
//             }

//             BorderPane mainLayout = (BorderPane) btnCheckout.getParent().getParent();
//             if (mainLayout.getTop() instanceof HBox) {
//                 HBox topBar = (HBox) mainLayout.getTop();

//                 Button btnEmployeeInfo = new Button("THÔNG TIN NV");
//                 btnEmployeeInfo.setStyle("-fx-background-color: #FF4081; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12;");

//                 btnEmployeeInfo.setOnMouseEntered(e -> btnEmployeeInfo.setStyle("-fx-background-color: #F50057; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 0);"));
//                 btnEmployeeInfo.setOnMouseExited(e -> btnEmployeeInfo.setStyle("-fx-background-color: #FF4081; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12;"));

//                 btnEmployeeInfo.setOnAction(e -> showEmployeeInfoDialog());

//                 HBox.setMargin(btnEmployeeInfo, new Insets(0, 10, 0, 10));
//                 int infoLabelIndex = topBar.getChildren().size() - 1;
//                 if (infoLabelIndex >= 0) {
//                     topBar.getChildren().add(infoLabelIndex, btnEmployeeInfo);
//                 } else {
//                     topBar.getChildren().add(btnEmployeeInfo);
//                 }

//                 LOGGER.info("✨ Đã thêm nút thông tin nhân viên!");
//             }
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi thêm nút thông tin nhân viên", e);
//         }
//     }

//     // Hàm hiển thị dialog thông tin nhân viên SIÊU XỊNNN
//     @FXML
//     private void showEmployeeInfoDialog() {
//         try {
//             if (currentEmployee == null) {
//                 AlertUtil.showWarning("Thông báo", "Không thể lấy thông tin nhân viên!");
//                 return;
//             }

//             // Tạo stage mới cho dialog
//             Stage infoStage = new Stage();
//             infoStage.initModality(Modality.APPLICATION_MODAL);
//             infoStage.setTitle("Thông Tin Nhân Viên");
//             infoStage.setResizable(false);

//             // Tạo layout chính
//             BorderPane layout = new BorderPane();

//             // Phần header đẹp ngời
//             HBox header = new HBox();
//             header.setAlignment(Pos.CENTER);
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: linear-gradient(to right, #FF4081, #F50057);");

//             Label headerTitle = new Label("THÔNG TIN NHÂN VIÊN");
//             headerTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
//             header.getChildren().add(headerTitle);

//             // Thêm header vào layout
//             layout.setTop(header);

//             // Phần nội dung
//             GridPane content = new GridPane();
//             content.setPadding(new Insets(20));
//             content.setVgap(15);
//             content.setHgap(10);
//             content.setAlignment(Pos.CENTER);

//             // Tạo ImageView cho ảnh đại diện (avatar)
//             ImageView avatarView = new ImageView();

//             // Tải ảnh từ resource đường dẫn đúng
//             try {
//                 // Lấy theo nhân viên đang đăng nhập
//                 String avatarPath = "/com/example/stores/images/employee/img.png"; // mặc định

//                 // Nếu là nv001, dùng ảnh an.png
//                 if (currentEmployee.getUsername() != null && currentEmployee.getUsername().equals("nv001")) {
//                     avatarPath = "/com/example/stores/images/employee/an.png";
//                 }

//                 // Hoặc nếu có imageUrl trong database
//                 if (currentEmployee.getImageUrl() != null && !currentEmployee.getImageUrl().isEmpty()) {
//                     String imageUrl = currentEmployee.getImageUrl();
//                     // Bỏ "resources/" ở đầu nếu có
//                     String resourcePath = imageUrl.startsWith("resources/") ? imageUrl.substring(10) : imageUrl;
//                     // Thay "com.example.stores/" thành "com/example/stores/"
//                     if (resourcePath.startsWith("com.example.stores/")) {
//                         resourcePath = resourcePath.replace("com.example.stores/", "com/example/stores/");
//                     }
//                     // Thêm dấu "/" ở đầu
//                     avatarPath = "/" + resourcePath;
//                 }

//                 // Load ảnh
//                 Image avatarImage = new Image(getClass().getResourceAsStream(avatarPath));
//                 avatarView.setImage(avatarImage);
//             } catch (Exception e) {
//                 // Nếu không có ảnh, hiển thị icon người dùng mặc định
//                 try {
//                     // Đường dẫn default chuẩn
//                     Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/stores/images/employee/img.png"));
//                     avatarView.setImage(defaultImage);
//                 } catch (Exception ex) {
//                     LOGGER.warning("Không thể tải ảnh mặc định cho nhân viên: " + ex.getMessage());
//                 }
//             }

//             // Thiết lập kích thước avatar
//             avatarView.setFitWidth(120);
//             avatarView.setFitHeight(120);
//             avatarView.setPreserveRatio(true);

//             // Bo tròn avatar bằng clip hình tròn
//             Circle clip = new Circle(60, 60, 60); // tâm (60,60), bán kính 60px
//             avatarView.setClip(clip);

//             // Tạo StackPane cho avatar, có viền và padding
//             StackPane avatarContainer = new StackPane(avatarView);
//             avatarContainer.setPadding(new Insets(3));
//             avatarContainer.setStyle("-fx-background-color: white; -fx-border-color: #FF4081; " +
//                     "-fx-border-width: 3; -fx-border-radius: 60; -fx-background-radius: 60;");
//             GridPane.setColumnSpan(avatarContainer, 2);
//             GridPane.setHalignment(avatarContainer, javafx.geometry.HPos.CENTER);

//             // Thêm avatar vào đầu tiên
//             content.add(avatarContainer, 0, 0, 2, 1);

//             // Thêm các thông tin nhân viên
//             addEmployeeInfoField(content, "Mã nhân viên:", currentEmployee.getEmployeeID(), 1);
//             addEmployeeInfoField(content, "Tên đăng nhập:", currentEmployee.getUsername(), 2);
//             addEmployeeInfoField(content, "Họ tên:", currentEmployee.getFullName(), 3);

//             // Thêm thông tin position nếu có
//             String position = "Nhân viên";
//             try {
//                 position = currentEmployee.getPosition();
//                 if (position == null || position.isEmpty()) position = "Nhân viên";
//             } catch (Exception e) {
//                 // Nếu không có thuộc tính position, dùng giá trị mặc định
//                 LOGGER.info("Không có thông tin chức vụ");
//             }
//             addEmployeeInfoField(content, "Chức vụ:", position, 4);

//             addEmployeeInfoField(content, "Email:", currentEmployee.getEmail(), 5);
//             addEmployeeInfoField(content, "Điện thoại:", currentEmployee.getPhone(), 6);
//             addEmployeeInfoField(content, "Thời gian đăng nhập:", currentDateTime, 7);

//             // Button đóng dialog
//             HBox buttonBar = new HBox();
//             buttonBar.setAlignment(Pos.CENTER);
//             buttonBar.setPadding(new Insets(0, 0, 20, 0));

//             Button closeButton = new Button("ĐÓNG");
//             closeButton.setPrefWidth(120);
//             closeButton.setPrefHeight(35);
//             closeButton.setStyle("-fx-background-color: #F50057; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");

//             closeButton.setOnMouseEntered(e ->
//                     closeButton.setStyle("-fx-background-color: #C51162; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;")
//             );

//             closeButton.setOnMouseExited(e ->
//                     closeButton.setStyle("-fx-background-color: #F50057; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;")
//             );

//             closeButton.setOnAction(e -> infoStage.close());

//             buttonBar.getChildren().add(closeButton);

//             // Thêm nội dung và button vào layout
//             VBox mainContainer = new VBox(15);
//             mainContainer.getChildren().addAll(content, buttonBar);
//             layout.setCenter(mainContainer);

//             // Tạo scene và hiển thị
//             Scene scene = new Scene(layout, 400, 520);
//             infoStage.setScene(scene);
//             infoStage.show();

//             LOGGER.info("✨ Đã hiển thị thông tin nhân viên: " + currentEmployee.getFullName());
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị thông tin nhân viên: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị thông tin nhân viên: " + e.getMessage());
//         }
//     }

//     // Hàm hỗ trợ thêm trường thông tin
//     private void addEmployeeInfoField(GridPane grid, String labelText, String value, int row) {
//         // Label tiêu đề
//         Label label = new Label(labelText);
//         label.setStyle("-fx-font-weight: bold; -fx-text-fill: #757575;");
//         grid.add(label, 0, row);

//         // Giá trị
//         Label valueLabel = new Label(value != null ? value : "N/A");
//         valueLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #212121;");
//         grid.add(valueLabel, 1, row);
//     }

//     // Biến để đếm số đơn hàng
//     private int orderCounter = 1;

//     private Button createLoadMoreButton() {
//         Button btnLoadMore = new Button("XEM THÊM SẢN PHẨM");
//         btnLoadMore.setPrefWidth(200);
//         btnLoadMore.setPrefHeight(40);
//         btnLoadMore.setStyle(
//                 "-fx-background-color: linear-gradient(to right, #2196F3, #03A9F4); " +
//                         "-fx-text-fill: white; " +
//                         "-fx-font-weight: bold; " +
//                         "-fx-font-size: 14px; " +
//                         "-fx-background-radius: 5; " +
//                         "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.4), 6, 0, 0, 2);"
//         );

//         // Hiệu ứng khi hover
//         btnLoadMore.setOnMouseEntered(e ->
//                 btnLoadMore.setStyle(
//                         "-fx-background-color: linear-gradient(to right, #1976D2, #2196F3); " +
//                                 "-fx-text-fill: white; " +
//                                 "-fx-font-weight: bold; " +
//                                 "-fx-font-size: 14px; " +
//                                 "-fx-background-radius: 5; " +
//                                 "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.6), 8, 0, 0, 3);"
//                 )
//         );

//         // Trở về style ban đầu khi hết hover
//         btnLoadMore.setOnMouseExited(e ->
//                 btnLoadMore.setStyle(
//                         "-fx-background-color: linear-gradient(to right, #2196F3, #03A9F4); " +
//                                 "-fx-text-fill: white; " +
//                                 "-fx-font-weight: bold; " +
//                                 "-fx-font-size: 14px; " +
//                                 "-fx-background-radius: 5; " +
//                                 "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.4), 6, 0, 0, 2);"
//                 )
//         );

//         // Sự kiện khi click
//         btnLoadMore.setOnAction(e -> {
//             productLimit += 20; // Tăng thêm 20 sản phẩm
//             refreshProductList(); // Làm mới danh sách
//             LOGGER.info("Đã tăng giới hạn hiển thị lên " + productLimit + " sản phẩm");
//         });

//         return btnLoadMore;
//     }

//     /**
//      * Lưu đơn hàng vào lịch sử
//      */
//     public void addToOrderHistory(int orderId, String customerName, String customerPhone,
//                                   String paymentMethod, String orderDateTime, double totalAmount,
//                                   List<CartItemEmployee> items) {
//         Connection conn = null;
//         PreparedStatement pstmtOrder = null;
//         PreparedStatement pstmtDetail = null;

//         try {
//             if (items == null || items.isEmpty()) {
//                 LOGGER.warning("Danh sách sản phẩm rỗng, không thể lưu lịch sử vào DB");
//                 return;
//             }

//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.severe("Không thể kết nối database để lưu order history");
//                 return;
//             }
//             conn.setAutoCommit(false); // Bắt đầu transaction

//             // 1. Insert vào bảng Orders
//             String insertOrder = "INSERT INTO Orders (orderID, orderDate, totalAmount, customerID, employeeID, orderStatus, paymentMethod, recipientName, recipientPhone) "
//                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//             pstmtOrder = conn.prepareStatement(insertOrder);

//             String orderIdStr = String.format("ORD%03d", orderId); // Format lại cho khớp orderID

//             int customerId = getWalkInCustomerId(); // Hoặc lấy đúng customerID nếu phân biệt khách

//             pstmtOrder.setString(1, orderIdStr);
//             pstmtOrder.setString(2, orderDateTime);
//             pstmtOrder.setDouble(3, totalAmount);
//             pstmtOrder.setInt(4, customerId);
//             pstmtOrder.setInt(5, employeeId);
//             pstmtOrder.setString(6, "Đã xác nhận");
//             pstmtOrder.setString(7, paymentMethod);
//             pstmtOrder.setString(8, customerName);
//             pstmtOrder.setString(9, customerPhone);

//             int resultOrder = pstmtOrder.executeUpdate();
//             if (resultOrder == 0) throw new SQLException("Insert Orders thất bại!");

//             // 2. Insert từng sản phẩm vào OrderDetails
//             String insertDetail = "INSERT INTO OrderDetails (orderID, productID, quantity, unitPrice, warrantyType, warrantyPrice) "
//                     + "VALUES (?, ?, ?, ?, ?, ?)";
//             pstmtDetail = conn.prepareStatement(insertDetail);

//             for (CartItemEmployee item : items) {
//                 pstmtDetail.setString(1, orderIdStr);
//                 pstmtDetail.setString(2, item.getProductID());
//                 pstmtDetail.setInt(3, item.getQuantity());
//                 pstmtDetail.setDouble(4, item.getPrice());

//                 // Bảo hành
//                 if (item.hasWarranty()) {
//                     pstmtDetail.setString(5, item.getWarranty().getWarrantyType());
//                     pstmtDetail.setDouble(6, item.getWarranty().getWarrantyPrice());
//                 } else {
//                     pstmtDetail.setString(5, "Thường");
//                     pstmtDetail.setDouble(6, 0.0);
//                 }
//                 pstmtDetail.addBatch();
//             }
//             int[] detailResults = pstmtDetail.executeBatch();

//             conn.commit();
//             LOGGER.info("✅ Đã lưu đơn hàng #" + orderIdStr + " vào database với " + detailResults.length + " sản phẩm");

//         } catch (Exception e) {
//             try {
//                 if (conn != null) conn.rollback();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi rollback khi lưu đơn hàng!", ex);
//             }
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi lưu đơn hàng vào DB: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (pstmtOrder != null) pstmtOrder.close();
//                 if (pstmtDetail != null) pstmtDetail.close();
//                 if (conn != null) conn.setAutoCommit(true);
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.WARNING, "Lỗi khi đóng kết nối DB", ex);
//             }
//         }
//     }
//     /**
//      * Lấy ID của khách hàng "Khách lẻ" (walkin)
//      */
//     private int getWalkInCustomerId() {
//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;
//         int customerId = 1; // Mặc định ID=1 cho khách lẻ

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.warning("Không thể kết nối đến database");
//                 return customerId;
//             }

//             String sql = "SELECT customerID FROM Customer WHERE username = 'walkin'";
//             stmt = conn.prepareStatement(sql);
//             rs = stmt.executeQuery();

//             if (rs.next()) {
//                 customerId = rs.getInt("customerID");
//                 return customerId;
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.WARNING, "Lỗi SQL khi lấy ID khách hàng mặc định: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.WARNING, "Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.WARNING, "Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }

//         return customerId;
//     }
//     // Thông tin user và thời gian
//     private String currentUser = "doanpk";
//     private String currentDateTime = "2025-06-22 10:30:23"; // Cập nhật thời gian hiện tại từ input
//     private Employee currentEmployee; // Biến lưu thông tin nhân viên

//     // Class để lưu thông tin đơn hàng tạm thời
//     private static class OrderSummary {
//         private int id;
//         private String customerName;
//         private String customerPhone;
//         private String paymentMethod;
//         private String orderDate;
//         private double totalAmount;
//         private List<CartItemEmployee> items;

//         public OrderSummary(int id, String customerName, String customerPhone, String paymentMethod,
//                             String orderDate, double totalAmount, List<CartItemEmployee> items) {
//             this.id = id;
//             this.customerName = customerName;
//             this.customerPhone = customerPhone;
//             this.paymentMethod = paymentMethod;
//             this.orderDate = orderDate;
//             this.totalAmount = totalAmount;
//             this.items = new ArrayList<>(items);
//         }

//         // Getters
//         public int getId() { return id; }
//         public String getCustomerName() { return customerName; }
//         public String getCustomerPhone() { return customerPhone; }
//         public String getPaymentMethod() { return paymentMethod; }
//         public String getOrderDate() { return orderDate; }
//         public double getTotalAmount() { return totalAmount; }
//         public List<CartItemEmployee> getItems() { return items; }
//     }

//     @FXML
//     private void initialize() {
//         try {
//             LOGGER.info("Đang khởi tạo POS Overview Controller...");
//             LOGGER.info("Người dùng hiện tại: " + currentUser);
//             LOGGER.info("Thời gian hiện tại: " + currentDateTime);

//             // Set style trực tiếp để đảm bảo nút có màu
//             if (btnCheckout != null) {
//                 btnCheckout.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             }

//             productFlowPane.setPrefWidth(900);
//             productFlowPane.setPrefWrapLength(900);  // DÒNG QUAN TRỌNG NHẤT!!!
//             productFlowPane.setHgap(15);
//             productFlowPane.setVgap(20);

//             // Lấy dữ liệu sản phẩm từ database
//             loadProductsFromDatabase();
//             LOGGER.info("Đã load " + products.size() + " sản phẩm từ database");

//             // Cấu hình TableView giỏ hàng
//             setupCartTable();

//             // Thêm nút xóa vào bảng giỏ hàng
//             addButtonsToTable();

//             cartTable.setItems(cartItems);

//             // Khởi tạo ComboBox filter danh mục
//             List<String> categoryList = getDistinctCategories();
//             if (cbCategory != null) cbCategory.setItems(FXCollections.observableArrayList(categoryList));

//             // Đảm bảo luôn chọn giá trị đầu tiên nếu có
//             if (cbCategory != null && !cbCategory.getItems().isEmpty()) cbCategory.getSelectionModel().select(0);

//             // Khởi tạo ComboBox sắp xếp
//             if (cbSort != null) {
//                 cbSort.getItems().addAll(
//                         "Mặc định",
//                         "Tên A-Z",
//                         "Tên Z-A",
//                         "Giá thấp đến cao",
//                         "Giá cao đến thấp"
//                 );
//                 cbSort.getSelectionModel().select(0);

//                 // Thêm listener cho cbSort
//                 cbSort.setOnAction(e -> refreshProductList());
//             }

//             // Sự kiện lọc, tìm kiếm
//             if (btnFilter != null) {
//                 btnFilter.setOnAction(e -> refreshProductList());
//             }

//             if (txtSearch != null) {
//                 txtSearch.textProperty().addListener((obs, oldVal, newVal) -> refreshProductList());
//             }

//             if (cbCategory != null) {
//                 cbCategory.setOnAction(e -> refreshProductList());
//             }

//             // Thanh toán - gọi handleCheckout để lưu dữ liệu vào DB
//             if (btnCheckout != null) {
//                 btnCheckout.setOnAction(e -> handleCheckout());
//             }

//             // Thêm nút lịch sử
//             addHistoryButton();

//             // Thêm nút đăng xuất
//             addLogoutButton();

//             // THÊM NÚT XEM THÔNG TIN NHÂN VIÊN
//             addEmployeeInfoButton();

//             // Render sản phẩm ban đầu
//             refreshProductList();
//             LOGGER.info("Khởi tạo POS Overview Controller thành công");
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi khởi tạo POS Overview Controller", e);
//         }
//     }

//     /**
//      * Xử lý thanh toán và lưu đơn hàng vào DB
//      */
//     @FXML
//     private void handleCheckout() {
//         try {
//             if (cartItems.isEmpty()) {
//                 AlertUtil.showWarning("Giỏ hàng trống", "Vui lòng thêm sản phẩm vào giỏ hàng trước khi thanh toán!");
//                 return;
//             }

//             // Tạo stage mới cho popup thanh toán
//             Stage confirmStage = new Stage();
//             confirmStage.initModality(Modality.APPLICATION_MODAL);
//             confirmStage.setTitle("Xác nhận thanh toán");
//             confirmStage.setResizable(false);

//             // BorderPane chính
//             BorderPane mainLayout = new BorderPane();

//             // HEADER ĐẸP NGỜI
//             HBox header = new HBox();
//             header.setAlignment(Pos.CENTER);
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: linear-gradient(to right, #4e73df, #224abe);");

//             Label headerLabel = new Label("XÁC NHẬN THANH TOÁN");
//             headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
//             header.getChildren().add(headerLabel);

//             mainLayout.setTop(header);

//             // PHẦN NỘI DUNG CHÍNH
//             VBox content = new VBox(15);
//             content.setPadding(new Insets(20));

//             // Tổng thanh toán hiển thị nổi bật
//             double totalAmount = calculateTotalAmount();
//             Label totalLabel = new Label("Tổng thanh toán: " + String.format("%,.0f", totalAmount) + "đ");
//             totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e91e63;");

//             // BẢNG DANH SÁCH SẢN PHẨM - THÊM VÀO FORM THANH TOÁN
//             Label productsLabel = new Label("Danh sách sản phẩm:");
//             productsLabel.setStyle("-fx-font-weight: bold;");

//             // TableView hiển thị sản phẩm trong giỏ
//             TableView<CartItemEmployee> productsTable = new TableView<>();
//             productsTable.setPrefHeight(150);

//             // Cột tên sản phẩm
//             TableColumn<CartItemEmployee, String> nameColumn = new TableColumn<>("Tên sản phẩm");
//             nameColumn.setCellValueFactory(data ->
//                     new SimpleStringProperty(data.getValue().getProductName()));
//             nameColumn.setPrefWidth(200);

//             // Cột số lượng
//             TableColumn<CartItemEmployee, Integer> quantityColumn = new TableColumn<>("SL");
//             quantityColumn.setCellValueFactory(data ->
//                     new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
//             quantityColumn.setPrefWidth(50);

//             // Cột đơn giá
//             TableColumn<CartItemEmployee, Double> priceColumn = new TableColumn<>("Đơn giá");
//             priceColumn.setCellValueFactory(data ->
//                     new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
//             priceColumn.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double price, boolean empty) {
//                     super.updateItem(price, empty);
//                     if (empty || price == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", price) + "đ");
//                     }
//                 }
//             });
//             priceColumn.setPrefWidth(100);

//             // Cột bảo hành
//             TableColumn<CartItemEmployee, String> warrantyColumn = new TableColumn<>("Bảo hành");
//             warrantyColumn.setCellValueFactory(data -> {
//                 CartItemEmployee item = data.getValue();
//                 if (item.hasWarranty()) {
//                     return new SimpleStringProperty(item.getWarranty().getWarrantyType());
//                 }
//                 return new SimpleStringProperty("Không");
//             });
//             warrantyColumn.setPrefWidth(80);

//             // Cột thành tiền
//             TableColumn<CartItemEmployee, Double> subtotalColumn = new TableColumn<>("T.Tiền");
//             subtotalColumn.setCellValueFactory(data ->
//                     new SimpleDoubleProperty(data.getValue().getTotalPrice()).asObject());
//             subtotalColumn.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double price, boolean empty) {
//                     super.updateItem(price, empty);
//                     if (empty || price == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", price) + "đ");
//                     }
//                 }
//             });
//             subtotalColumn.setPrefWidth(100);

//             productsTable.getColumns().addAll(nameColumn, quantityColumn, priceColumn, warrantyColumn, subtotalColumn);
//             productsTable.setItems(cartItems);

//             // Phần thông tin khách hàng
//             Label customerLabel = new Label("Thông tin khách hàng:");
//             customerLabel.setStyle("-fx-font-weight: bold;");

// // Form thông tin khách hàng
//             GridPane customerForm = new GridPane();
//             customerForm.setVgap(10);
//             customerForm.setHgap(10);

//             Label nameLabel = new Label("Tên khách hàng:");
//             TextField nameField = new TextField("Khách lẻ");
//             nameField.setPrefWidth(300);

//             Label phoneLabel = new Label("Số điện thoại:");
//             TextField phoneField = new TextField("0900000000");
//             phoneField.setPrefWidth(300);

// // ✅ THÊM TRƯỜNG GHI CHÚ
//             Label noteLabel = new Label("Ghi chú:");
//             TextArea noteField = new TextArea();
//             noteField.setPromptText("Nhập ghi chú cho đơn hàng (không bắt buộc)...");
//             noteField.setPrefWidth(300);
//             noteField.setPrefHeight(60);
//             noteField.setWrapText(true);

//             customerForm.add(nameLabel, 0, 0);
//             customerForm.add(nameField, 1, 0);
//             customerForm.add(phoneLabel, 0, 1);
//             customerForm.add(phoneField, 1, 1);
//             customerForm.add(noteLabel, 0, 2);  // ✅ THÊM VÀO DÒNG THỨ 3
//             customerForm.add(noteField, 1, 2);

//             // Phương thức thanh toán - CHỈ CÓ 2 PHƯƠNG THỨC
//             Label paymentLabel = new Label("Phương thức thanh toán:");
//             paymentLabel.setStyle("-fx-font-weight: bold;");

//             ToggleGroup paymentGroup = new ToggleGroup();

//             RadioButton cashRadio = new RadioButton("Tiền mặt");
//             cashRadio.setToggleGroup(paymentGroup);
//             cashRadio.setSelected(true); // Mặc định chọn tiền mặt

//             RadioButton transferRadio = new RadioButton("Chuyển khoản");
//             transferRadio.setToggleGroup(paymentGroup);

//             HBox paymentOptions = new HBox(20);
//             paymentOptions.getChildren().addAll(cashRadio, transferRadio);

//             // Thêm các thành phần vào content
//             content.getChildren().addAll(
//                     totalLabel,
//                     new Separator(),
//                     productsLabel,
//                     productsTable,
//                     new Separator(),
//                     customerLabel,
//                     customerForm,
//                     new Separator(),
//                     paymentLabel,
//                     paymentOptions
//             );

//             mainLayout.setCenter(new ScrollPane(content));

//             // PHẦN FOOTER VỚI CÁC NÚT CHỨC NĂNG
//             HBox footer = new HBox(10);
//             footer.setAlignment(Pos.CENTER_RIGHT);
//             footer.setPadding(new Insets(15, 20, 15, 20));
//             footer.setStyle("-fx-background-color: #f8f9fc; -fx-border-color: #e3e6f0; -fx-border-width: 1 0 0 0;");

//             Button cancelButton = new Button("Hủy");
//             cancelButton.setPrefWidth(100);
//             cancelButton.setStyle("-fx-background-color: #e74a3b; -fx-text-fill: white;");

//             Button confirmButton = new Button("Xác nhận thanh toán");
//             confirmButton.setPrefWidth(200);
//             confirmButton.setStyle("-fx-background-color: #4e73df; -fx-text-fill: white; -fx-font-weight: bold;");

//             footer.getChildren().addAll(cancelButton, confirmButton);
//             mainLayout.setBottom(footer);

//             // Xử lý sự kiện cho nút Hủy
//             cancelButton.setOnAction(e -> confirmStage.close());

//             // Xử lý sự kiện cho nút Xác nhận thanh toán
//             confirmButton.setOnAction(e -> {
//                 try {
//                     // Lấy thông tin khách hàng và phương thức thanh toán
//                     String customerName = nameField.getText().trim();
//                     String customerPhone = phoneField.getText().trim();
//                     String paymentMethod = cashRadio.isSelected() ? "Tiền mặt" : "Chuyển khoản";

//                     // Validate số điện thoại
//                     if (!customerPhone.isEmpty() && customerPhone.length() < 10) {
//                         AlertUtil.showWarning("Lỗi", "Số điện thoại không hợp lệ!");
//                         return;
//                     }

//                     // NẾU CHỌN CHUYỂN KHOẢN - MỞ CỬA SỔ QR CODE
//                     if (transferRadio.isSelected()) {
//                         // Đóng cửa sổ xác nhận
//                         confirmStage.close();

//                         // Mở cửa sổ QR Payment
//                         showQRPaymentWindow(customerName, customerPhone, totalAmount, cartItems);
//                         return;
//                     }

//                     // NẾU THANH TOÁN TIỀN MẶT - XỬ LÝ LUÔN
//                     // Lưu đơn hàng vào DB và trả về orderID
//                     String orderId = saveOrderToDB(customerName, customerPhone, paymentMethod, totalAmount, cartItems);

//                     if (orderId != null) {
//                         // FIX LỖI: Chỉ lấy phần số từ orderId (bỏ phần chữ "ORD")
//                         String numericOrderId = orderId.replaceAll("[^0-9]", "");
//                         int orderIdInt = Integer.parseInt(numericOrderId);

//                         // Lưu vào bộ nhớ (để tương thích với code cũ) - DÙNG ID ĐÃ XỬ LÝ
//                         addToOrderHistory(orderIdInt, customerName, customerPhone,
//                                 paymentMethod, getCurrentDateTime(), totalAmount, cartItems);

//                         // Đóng cửa sổ thanh toán
//                         confirmStage.close();

//                         // Hiển thị thông báo thành công
//                         AlertUtil.showInfo("Thanh toán thành công",
//                                 "Đơn hàng #" + orderId + " đã được tạo thành công!");

//                         // In hóa đơn - DÙNG ID ĐÃ XỬ LÝ
//                         printReceiptWithPaymentMethod(
//                                 orderIdInt,
//                                 cartItems, totalAmount, customerName, customerPhone,
//                                 paymentMethod, getCurrentDateTime(), currentUser);

//                         // Xóa giỏ hàng
//                         clearCart();
//                     } else {
//                         // Thông báo lỗi
//                         AlertUtil.showError("Lỗi thanh toán",
//                                 "Không thể lưu đơn hàng. Vui lòng thử lại!");
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi thanh toán: " + ex.getMessage(), ex);
//                     AlertUtil.showError("Lỗi thanh toán", "Đã xảy ra lỗi: " + ex.getMessage());
//                     confirmStage.close();
//                 }
//             });

//             Scene scene = new Scene(mainLayout, 600, 700);
//             confirmStage.setScene(scene);
//             confirmStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi hiển thị form thanh toán: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể mở trang xác nhận thanh toán: " + e.getMessage());
//         }
//     }

//     /**
//      * Hiển thị cửa sổ thanh toán QR Code
//      */
//     private void showQRPaymentWindow(String customerName, String customerPhone, double totalAmount, ObservableList<CartItemEmployee> items) {
//         try {
//             LOGGER.info("💖 Bắt đầu mở cửa sổ QR Payment nè!");

//             // Tạo đối tượng Order giả
//             Order order = new Order();
//             order.setTotalAmount(totalAmount);

//             // DEBUG: In ra đường dẫn hiện tại
//             LOGGER.info("📂 Working Directory: " + System.getProperty("user.dir"));

//             FXMLLoader loader = null;

//             // THỬ TẤT CẢ CÁC ĐƯỜNG DẪN CÓ THỂ
//             String[] possiblePaths = {
//                     "/com/example/stores/view/qr_payment.fxml",
//                     "com/example/stores/view/qr_payment.fxml",
//                     "/view/qr_payment.fxml",
//                     "view/qr_payment.fxml",
//                     "/qr_payment.fxml",
//                     "qr_payment.fxml"
//             };

//             for (String path : possiblePaths) {
//                 try {
//                     LOGGER.info("🔍 Thử load FXML từ: " + path);
//                     URL fxmlUrl = getClass().getResource(path);

//                     if (fxmlUrl != null) {
//                         LOGGER.info("✅ Tìm thấy file FXML tại: " + fxmlUrl);
//                         loader = new FXMLLoader(fxmlUrl);
//                         break;
//                     } else {
//                         LOGGER.warning("❌ Không tìm thấy FXML tại: " + path);
//                     }
//                 } catch (Exception e) {
//                     LOGGER.warning("❌ Lỗi khi thử path: " + path + " - " + e.getMessage());
//                 }
//             }

//             // Nếu không tìm thấy file FXML nào
//             if (loader == null) {
//                 LOGGER.severe("😭 KHÔNG TÌM THẤY FILE FXML NÀO HẾT!!!");
//                 throw new Exception("Không tìm thấy file FXML cho QR Payment");
//             }

//             // Load FXML
//             Parent root = loader.load();
//             LOGGER.info("✅ Đã load FXML thành công!");

//             // Lấy controller và truyền dữ liệu
//             QRPaymentControllerE controller = loader.getController();
//             LOGGER.info("✅ Đã lấy controller thành công!");

//             // Tạo danh sách OrderDetail giả
//             List<OrderDetail> orderDetails = new ArrayList<>();
//             // Chuyển đổi từ CartItem sang OrderDetail
//             for (CartItemEmployee item : items) {
//                 OrderDetail detail = new OrderDetail();
//                 detail.setProductName(item.getProductName());
//                 detail.setQuantity(item.getQuantity());
//                 detail.setPrice(item.getPrice());
//                 orderDetails.add(detail);
//             }

//             // Set dữ liệu cho Controller
//             controller.setOrderDetails(order, orderDetails);
//             LOGGER.info("✅ Đã set order details!");

//             // Set callback khi thanh toán thành công
//             controller.setOnPaymentSuccess(() -> {
//                 try {
//                     // Tạo đơn hàng với phương thức thanh toán là chuyển khoản
//                     String orderId = saveOrderToDB(customerName, customerPhone, "Chuyển khoản", totalAmount, items);
//                     LOGGER.info("✅ Đã lưu đơn hàng với ID: " + orderId);

//                     if (orderId != null) {
//                         // FIX LỖI: Chỉ lấy phần số từ orderId
//                         String numericOrderId = orderId.replaceAll("[^0-9]", "");
//                         int orderIdInt = Integer.parseInt(numericOrderId);

//                         // Lưu vào bộ nhớ với ID đã xử lý
//                         addToOrderHistory(orderIdInt, customerName, customerPhone,
//                                 "Chuyển khoản", getCurrentDateTime(), totalAmount, items);

//                         // Hiển thị thông báo thành công
//                         AlertUtil.showInfo("Thanh toán thành công",
//                                 "Đơn hàng #" + orderId + " đã được thanh toán thành công!");

//                         // In hóa đơn với ID đã xử lý
//                         printReceiptWithPaymentMethod(
//                                 orderIdInt,
//                                 items, totalAmount, customerName, customerPhone,
//                                 "Chuyển khoản", getCurrentDateTime(), currentUser);

//                         // Xóa giỏ hàng
//                         clearCart();
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi hoàn tất thanh toán QR: " + ex.getMessage(), ex);
//                     AlertUtil.showError("Lỗi thanh toán", "Đã xảy ra lỗi: " + ex.getMessage());
//                 }
//             });

//             // Hiển thị cửa sổ QR
//             Stage qrStage = new Stage();
//             qrStage.initModality(Modality.APPLICATION_MODAL);
//             qrStage.setTitle("Thanh toán bằng mã QR");
//             qrStage.setResizable(false);

//             Scene scene = new Scene(root);
//             qrStage.setScene(scene);

//             LOGGER.info("💯 SẮP HIỆN CỬA SỔ QR PAYMENT RỒI!!!");
//             qrStage.show(); // Dùng show() thay vì showAndWait() để debug
//             LOGGER.info("🎉 ĐÃ HIỆN CỬA SỔ QR PAYMENT!!!");

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi hiển thị cửa sổ thanh toán QR: " + e.getMessage(), e);

//             // In lỗi chi tiết hơn
//             e.printStackTrace();

//             AlertUtil.showError("Lỗi", "Không thể mở cửa sổ thanh toán QR: " + e.getMessage() + "\nVui lòng thanh toán bằng tiền mặt!");

//             // Trong trường hợp lỗi, thử lại với phương thức thanh toán tiền mặt
//             try {
//                 String orderId = saveOrderToDB(customerName, customerPhone, "Tiền mặt", totalAmount, items);
//                 if (orderId != null) {
//                     // FIX LỖI: Chỉ lấy phần số từ orderId
//                     String numericOrderId = orderId.replaceAll("[^0-9]", "");
//                     int orderIdInt = Integer.parseInt(numericOrderId);

//                     addToOrderHistory(orderIdInt, customerName, customerPhone, "Tiền mặt", getCurrentDateTime(), totalAmount, items);

//                     AlertUtil.showInfo("Thanh toán thành công",
//                             "Đã chuyển sang thanh toán tiền mặt.\nĐơn hàng #" + orderId + " đã được tạo thành công!");

//                     printReceiptWithPaymentMethod(orderIdInt, items, totalAmount, customerName, customerPhone,
//                             "Tiền mặt", getCurrentDateTime(), currentUser);

//                     clearCart();
//                 }
//             } catch (Exception ex) {
//                 LOGGER.log(Level.SEVERE, "❌ Lỗi khi thử thanh toán tiền mặt: " + ex.getMessage(), ex);
//             }
//         }
//     }    /**
//      * Lưu đơn hàng vào DB
//      * @return Mã đơn hàng (orderID) nếu lưu thành công, null nếu thất bại
//      */
//     private String saveOrderToDB(String recipientName, String recipientPhone,
//                                  String paymentMethod, double totalAmount,
//                                  List<CartItemEmployee> cartItems) {
//         String orderId = null;
//         Connection conn = null;

//         try {
//             conn = DBConfig.getConnection();
//             conn.setAutoCommit(false);

//             // 1. Tạo đơn hàng mới trong bảng Orders
//             String insertOrderSQL = "INSERT INTO Orders (orderDate, totalAmount, customerID, " +
//                     "recipientPhone, recipientName, orderStatus, paymentMethod) " +
//                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

//             try (PreparedStatement pstmtOrder = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {

//                 pstmtOrder.setString(1, getCurrentDateTime());
//                 pstmtOrder.setDouble(2, totalAmount);

//                 // ==== SỬA ĐOẠN NÀY ĐỂ LƯU KHÁCH HÀNG MỚI ====
//                 CustomerServiceE customerServiceE = new CustomerServiceE();
//                 int customerId = customerServiceE.findCustomerIdByPhone(recipientPhone);
//                 if (customerId == -1) {
//                     Customer newCustomer = new Customer();
//                     newCustomer.setCustomerName(recipientName);
//                     newCustomer.setPhone(recipientPhone);
//                     newCustomer.setAddress(""); // Có thể lấy từ form nếu có
//                     newCustomer.setEmail("");   // Có thể lấy từ form nếu có
//                     customerId = customerServiceE.addCustomerToDB(newCustomer);
//                     if (customerId == -1) {
//                         LOGGER.warning("❌ Không thể tạo khách mới, fallback về ID=1");
//                         customerId = 1; // fallback nếu lỗi
//                     }
//                 }
//                 pstmtOrder.setInt(3, customerId);

//                 pstmtOrder.setString(4, recipientPhone != null ? recipientPhone : "");
//                 pstmtOrder.setString(5, recipientName != null ? recipientName : "Khách lẻ");
//                 pstmtOrder.setString(6, "Đã xác nhận");
//                 pstmtOrder.setString(7, paymentMethod != null ? paymentMethod : "Tiền mặt");

//                 int result = pstmtOrder.executeUpdate();

//                 if (result > 0) {
//                     // Lấy orderID vừa được tạo
//                     ResultSet generatedKeys = pstmtOrder.getGeneratedKeys();
//                     if (generatedKeys.next()) {
//                         orderId = generatedKeys.getString(1);
//                         LOGGER.info("✅ Đã tạo đơn hàng mới với ID: " + orderId);

//                         // 2. Thêm chi tiết đơn hàng
//                         saveOrderDetails(conn, orderId, cartItems);

//                         // 3. Commit transaction
//                         conn.commit();
//                     }
//                 }

//             }

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi lưu đơn hàng vào DB: " + e.getMessage(), e);
//             // Rollback transaction nếu có lỗi
//             if (conn != null) {
//                 try {
//                     conn.rollback();
//                 } catch (SQLException ex) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi rollback transaction: " + ex.getMessage(), ex);
//                 }
//             }

//         } finally {
//             // Đảm bảo đóng connection và reset autoCommit
//             if (conn != null) {
//                 try {
//                     conn.setAutoCommit(true);
//                     conn.close();
//                 } catch (SQLException e) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi đóng connection: " + e.getMessage(), e);
//                 }
//             }
//         }

//         return orderId;
//     }
//     /**
//      * Lưu chi tiết đơn hàng vào DB
//      */
//     private void saveOrderDetails(Connection conn, String orderId, List<CartItemEmployee> cartItems) throws SQLException {
//         String insertDetailSQL = "INSERT INTO OrderDetails (orderID, productID, quantity, unitPrice, warrantyType, warrantyPrice) " +
//                 "VALUES (?, ?, ?, ?, ?, ?)";

//         try (PreparedStatement pstmt = conn.prepareStatement(insertDetailSQL)) {
//             for (CartItemEmployee item : cartItems) {
//                 pstmt.setString(1, orderId);
//                 pstmt.setString(2, item.getProductID());
//                 pstmt.setInt(3, item.getQuantity());
//                 pstmt.setDouble(4, item.getPrice());

//                 // Xử lý thông tin bảo hành
//                 if (item.hasWarranty()) {
//                     pstmt.setString(5, item.getWarranty().getWarrantyType());
//                     pstmt.setDouble(6, item.getWarranty().getWarrantyPrice());
//                 } else {
//                     pstmt.setString(5, "Thường"); // Mặc định
//                     pstmt.setDouble(6, 0.0);
//                 }

//                 pstmt.addBatch();
//             }

//             int[] results = pstmt.executeBatch();
//             LOGGER.info("✅ Đã thêm " + results.length + " chi tiết đơn hàng");

//             // Cập nhật số lượng sản phẩm trong kho
//             updateProductQuantities(conn, cartItems);
//         }
//     }

//     /**
//      * Cập nhật số lượng sản phẩm trong kho sau khi thanh toán
//      */
//     private void updateProductQuantities(Connection conn, List<CartItemEmployee> cartItems) throws SQLException {
//         String updateSQL = "UPDATE Products SET quantity = quantity - ? WHERE productID = ?";

//         try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
//             for (CartItemEmployee item : cartItems) {
//                 pstmt.setInt(1, item.getQuantity());
//                 pstmt.setString(2, item.getProductID());
//                 pstmt.addBatch();
//             }

//             int[] results = pstmt.executeBatch();
//             LOGGER.info("✅ Đã cập nhật số lượng cho " + results.length + " sản phẩm");
//         }
//     }

//     /**
//      * Lấy thời gian hiện tại theo định dạng phù hợp với DB
//      */
//     private String getCurrentDateTime() {
//         LocalDateTime now = LocalDateTime.now();
//         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//         return now.format(formatter);
//     }

//     // Phương thức để nhận thông tin nhân viên từ màn hình login
//     public void initEmployeeData(Employee employee, String loginDateTime) {
//         try {
//             if (employee != null) {
//                 this.currentEmployee = employee;
//                 this.currentDateTime = loginDateTime;
//                 this.currentUser = employee.getUsername();

//                 // Dùng getFullName() - đảm bảo không gọi getName() vì có thể không có method này
//                 LOGGER.info("Đã khởi tạo POS với nhân viên: " + employee.getFullName());
//                 LOGGER.info("Thời gian hiện tại: " + currentDateTime);

//                 // Hiển thị thông tin nhân viên trên giao diện
//                 displayEmployeeInfo();
//             } else {
//                 LOGGER.warning("Lỗi: Employee object truyền vào là null");
//             }
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi khởi tạo dữ liệu nhân viên", e);
//         }
//     }

//     // Phương thức để nhận thông tin nhân viên từ màn hình login
//     public void setEmployeeInfo(int employeeID, String username) {
//         this.employeeId = employeeID; // Lưu employeeID vào biến instance
//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             // ĐÃ SỬA: Bọc trong try-catch để xử lý Exception từ getConnection()
//             try {
//                 conn = DBConfig.getConnection();
//             } catch (Exception ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi kết nối DB", ex);
//                 throw new SQLException("Không thể kết nối đến cơ sở dữ liệu: " + ex.getMessage());
//             }

//             if (conn == null) {
//                 throw new SQLException("Không thể kết nối đến cơ sở dữ liệu");
//             }

//             String query = "SELECT * FROM Employee WHERE employeeID = ? AND username = ?";
//             stmt = conn.prepareStatement(query);
//             stmt.setInt(1, employeeID);
//             stmt.setString(2, username);

//             rs = stmt.executeQuery();
//             if (rs.next()) {
//                 // Tạo đối tượng Employee từ ResultSet
//                 Employee emp = new Employee();
//                 emp.setEmployeeID(String.valueOf(employeeID));  // Chuyển int thành String
//                 emp.setUsername(rs.getString("username"));
//                 emp.setFullName(rs.getString("fullName"));
//                 emp.setEmail(rs.getString("email"));
//                 emp.setPhone(rs.getString("phone"));

//                 // Kiểm tra trước khi gọi setPosition
//                 try {
//                     int columnIndex = rs.findColumn("position");
//                     if (columnIndex > 0) {
//                         emp.setPosition(rs.getString("position"));
//                     }
//                 } catch (SQLException ex) {
//                     // Nếu không có cột position, bỏ qua
//                     LOGGER.info("Cột position không tồn tại trong bảng Employee");
//                 }

//                 // Gọi initEmployeeData với đối tượng Employee đã tạo
//                 String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//                 initEmployeeData(emp, currentTime);
//             } else {
//                 LOGGER.warning("Không tìm thấy nhân viên với ID=" + employeeID + " và username=" + username);
//                 Alert alert = new Alert(Alert.AlertType.WARNING);
//                 alert.setTitle("Cảnh báo");
//                 alert.setHeaderText("Không tìm thấy thông tin nhân viên");
//                 alert.setContentText("Vui lòng đăng nhập lại để tiếp tục.");
//                 alert.showAndWait();
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "Lỗi SQL khi lấy thông tin nhân viên", e);
//             Alert alert = new Alert(Alert.AlertType.ERROR);
//             alert.setTitle("Lỗi");
//             alert.setHeaderText("Không thể lấy thông tin nhân viên");
//             alert.setContentText("Chi tiết lỗi: " + e.getMessage());
//             alert.showAndWait();
//         } finally {
//             // Đóng tất cả các tài nguyên theo thứ tự ngược lại
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 // Không đóng connection ở đây vì có thể được sử dụng ở nơi khác
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi khi đóng tài nguyên SQL", ex);
//             }
//         }
//     }

//     // Hiển thị thông tin nhân viên trên giao diện - ĐÃ SỬA (FIX BUG 243)
//     private void displayEmployeeInfo() {
//         try {
//             if (currentEmployee != null && btnCheckout != null && btnCheckout.getParent() != null
//                     && btnCheckout.getParent().getParent() instanceof BorderPane) {

//                 BorderPane mainLayout = (BorderPane) btnCheckout.getParent().getParent();

//                 if (mainLayout.getTop() instanceof HBox) {
//                     HBox topBar = (HBox) mainLayout.getTop();

//                     // Tạo label hiển thị thông tin nhân viên
//                     Label lblEmployeeInfo = new Label(currentEmployee.getFullName() + " (" + currentUser + ")");
//                     lblEmployeeInfo.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

//                     // Tạo spacer để đẩy thông tin ra góc phải
//                     Region spacer = new Region();
//                     HBox.setHgrow(spacer, Priority.ALWAYS);

//                     // Thêm vào top bar
//                     topBar.getChildren().addAll(spacer, lblEmployeeInfo);
//                 }
//             }
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị thông tin nhân viên", e);
//         }
//     }

//     // Thêm nút đăng xuất
//     private void addLogoutButton() {
//         if (btnCheckout == null) {
//             LOGGER.warning("Lỗi: btnCheckout chưa được khởi tạo");
//             return;
//         }

//         Button btnLogout = new Button("ĐĂNG XUẤT");
//         btnLogout.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//         btnLogout.setPrefWidth(120);
//         btnLogout.setPrefHeight(35);
//         btnLogout.setOnAction(e -> logout());

//         if (btnCheckout.getParent() instanceof HBox) {
//             HBox parent = (HBox) btnCheckout.getParent();
//             parent.getChildren().add(0, btnLogout);
//         } else if (btnCheckout.getParent() instanceof Pane) {
//             Pane parent = (Pane) btnCheckout.getParent();
//             btnLogout.setLayoutX(btnCheckout.getLayoutX() - 130);
//             btnLogout.setLayoutY(btnCheckout.getLayoutY());
//             parent.getChildren().add(btnLogout);
//         }
//     }

//     // Xử lý đăng xuất
//     private void logout() {
//         try {
//             // Hiển thị xác nhận
//             Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
//             confirm.setTitle("Xác nhận đăng xuất");
//             confirm.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
//             confirm.setContentText("Mọi thay đổi chưa lưu sẽ bị mất.");

//             Optional<ButtonType> result = confirm.showAndWait();
//             if (result.isPresent() && result.get() == ButtonType.OK) {
//                 // Load màn hình đăng nhập
//                 URL loginUrl = getClass().getResource("/com/example/stores/view/employee_login.fxml");

//                 if (loginUrl != null) {
//                     FXMLLoader loader = new FXMLLoader(loginUrl);
//                     Parent root = loader.load();

//                     Scene scene = null;
//                     Stage stage = null;

//                     if (btnCheckout != null) {
//                         stage = (Stage) btnCheckout.getScene().getWindow();
//                         scene = new Scene(root);
//                         stage.setTitle("Computer Store - Đăng Nhập");
//                         stage.setScene(scene);
//                         stage.setResizable(false);
//                         stage.show();
//                     } else {
//                         LOGGER.warning("Lỗi: btnCheckout là null hoặc không thuộc Scene");
//                         stage = new Stage();
//                         scene = new Scene(root);
//                         stage.setTitle("Computer Store - Đăng Nhập");
//                         stage.setScene(scene);
//                         stage.setResizable(false);
//                         stage.show();

//                         // Đóng cửa sổ hiện tại nếu có
//                         if (productFlowPane != null && productFlowPane.getScene() != null) {
//                             Stage currentStage = (Stage) productFlowPane.getScene().getWindow();
//                             currentStage.close();
//                         }
//                     }

//                     LOGGER.info("Đã đăng xuất, thời gian: " +
//                             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//                 } else {
//                     throw new IOException("Không tìm thấy file employee_login.fxml");
//                 }
//             }
//         } catch (IOException e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi đăng xuất", e);
//             Alert alert = new Alert(Alert.AlertType.ERROR);
//             alert.setTitle("Lỗi");
//             alert.setContentText("Lỗi khi đăng xuất: " + e.getMessage());
//             alert.showAndWait();
//         }
//     }

//     // Thêm nút lịch sử đơn hàng
//     private void addHistoryButton() {
//         if (btnCheckout == null) {
//             LOGGER.warning("Lỗi: btnCheckout chưa được khởi tạo");
//             return;
//         }

//         Button btnHistory = new Button("LỊCH SỬ");
//         btnHistory.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
//         btnHistory.setPrefWidth(120);
//         btnHistory.setPrefHeight(35);
//         btnHistory.setOnAction(e -> showOrderHistoryInMemory()); // Sử dụng history trong bộ nhớ

//         if (btnCheckout.getParent() instanceof HBox) {
//             HBox parent = (HBox) btnCheckout.getParent();
//             parent.getChildren().add(0, btnHistory);
//         } else if (btnCheckout.getParent() instanceof Pane) {
//             Pane parent = (Pane) btnCheckout.getParent();
//             btnHistory.setLayoutX(btnCheckout.getLayoutX() - 130);
//             btnHistory.setLayoutY(btnCheckout.getLayoutY());
//             parent.getChildren().add(btnHistory);
//         }
//     }

//     // Cấu hình TableView giỏ hàng
//     // Đầu tiên em sửa hàm setupCartTable() để thêm cột bảo hành mới
//     private void setupCartTable() {
//         if (colCartName == null || colCartQty == null || colCartPrice == null || colCartTotal == null) {
//             LOGGER.warning("Lỗi: Các cột của TableView chưa được khởi tạo");
//             return;
//         }

//         // Thiết lập các cột cũ
//         colCartName.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleStringProperty("N/A");
//             }
//             String name = data.getValue().getProductName();
//             return new SimpleStringProperty(name != null ? name : "N/A");
//         });

//         colCartQty.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleIntegerProperty(0).asObject();
//             }
//             int qty = data.getValue().getQuantity();
//             return new SimpleIntegerProperty(qty).asObject();
//         });

//         colCartPrice.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleDoubleProperty(0).asObject();
//             }
//             double price = data.getValue().getPrice();
//             return new SimpleDoubleProperty(price).asObject();
//         });

//         colCartPrice.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//             @Override
//             protected void updateItem(Double price, boolean empty) {
//                 super.updateItem(price, empty);
//                 if (empty || price == null) {
//                     setText(null);
//                 } else {
//                     setText(String.format("%,.0f", price) + "đ");
//                 }
//             }
//         });

//         // THÊM CỘT BẢO HÀNH MỚI
//         colCartWarranty.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleStringProperty("Không");
//             }
//             CartItemEmployee item = data.getValue();
//             if (item.hasWarranty()) {
//                 return new SimpleStringProperty(item.getWarranty().getWarrantyType());
//             } else {
//                 return new SimpleStringProperty("Không");
//             }
//         });

//         // Nút sửa bảo hành
//         colCartWarranty.setCellFactory(tc -> new TableCell<CartItemEmployee, String>() {
//             @Override
//             protected void updateItem(String warrantyType, boolean empty) {
//                 super.updateItem(warrantyType, empty);
//                 if (empty) {
//                     setText(null);
//                     setGraphic(null);
//                 } else {
//                     HBox container = new HBox(5);
//                     container.setAlignment(Pos.CENTER_LEFT);

//                     // Hiển thị loại bảo hành
//                     Label lblType = new Label(warrantyType);

//                     // Nút sửa nhỏ bên cạnh
//                     Button btnEdit = new Button("⚙️");
//                     btnEdit.setStyle("-fx-background-color: transparent; -fx-padding: 0 2;");
//                     btnEdit.setOnAction(event -> {
//                         CartItemEmployee item = getTableView().getItems().get(getIndex());
//                         if (item != null) {
//                             showWarrantyEditDialog(item);
//                         }
//                     });

//                     container.getChildren().addAll(lblType, btnEdit);
//                     setGraphic(container);
//                     setText(null);
//                 }
//             }
//         });

//         colCartTotal.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleDoubleProperty(0).asObject();
//             }
//             double total = data.getValue().getTotalPrice();
//             return new SimpleDoubleProperty(total).asObject();
//         });

//         colCartTotal.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//             @Override
//             protected void updateItem(Double total, boolean empty) {
//                 super.updateItem(total, empty);
//                 if (empty || total == null) {
//                     setText(null);
//                 } else {
//                     setText(String.format("%,.0f", total) + "đ");
//                 }
//             }
//         });
//     }

//     // Sửa lại dialog chỉnh sửa bảo hành trong giỏ hàng
//     private void showWarrantyEditDialog(CartItemEmployee item) {
//         try {
//             // Tìm thông tin sản phẩm từ database để lấy giá
//             Product product = findProductById(item.getProductID());
//             if (product == null) {
//                 AlertUtil.showWarning("Lỗi", "Không tìm thấy thông tin sản phẩm");
//                 return;
//             }

//             Stage dialogStage = new Stage();
//             dialogStage.setTitle("Cập nhật bảo hành");
//             dialogStage.initModality(Modality.APPLICATION_MODAL);

//             VBox dialogContent = new VBox(15);
//             dialogContent.setPadding(new Insets(20));
//             dialogContent.setAlignment(Pos.CENTER);

//             // Tiêu đề và thông tin sản phẩm
//             Label lblTitle = new Label("Chọn gói bảo hành cho " + item.getProductName());
//             lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             // ComboBox chọn loại bảo hành - SỬA LẠI CÒN 2 LOẠI
//             ComboBox<String> cbWarranty = new ComboBox<>();

//             // Kiểm tra điều kiện bảo hành thường
//             boolean isEligibleForStdWarranty = WarrantyCalculator.isEligibleForStandardWarranty(product);

//             if (isEligibleForStdWarranty) {
//                 // Chỉ còn 2 lựa chọn
//                 cbWarranty.getItems().addAll("Không", "Thường", "Vàng");
//             } else {
//                 // Sản phẩm không đủ điều kiện bảo hành
//                 cbWarranty.getItems().add("Không");
//             }

//             // Set giá trị hiện tại
//             if (item.hasWarranty()) {
//                 String currentType = item.getWarranty().getWarrantyType();
//                 // Chuyển đổi các loại bảo hành cũ (nếu có)
//                 if (!currentType.equals("Thường") && !currentType.equals("Vàng")) {
//                     currentType = "Thường"; // Mặc định về Thường
//                 }

//                 if (cbWarranty.getItems().contains(currentType)) {
//                     cbWarranty.setValue(currentType);
//                 } else {
//                     cbWarranty.setValue("Không");
//                 }
//             } else {
//                 cbWarranty.setValue("Không");
//             }

//             // Hiển thị giá bảo hành
//             Label lblWarrantyPrice = new Label("Phí bảo hành: 0đ");
//             Label lblTotalWithWarranty = new Label("Tổng tiền: " + String.format("%,.0f", item.getTotalPrice()) + "đ");
//             lblTotalWithWarranty.setStyle("-fx-font-weight: bold;");

//             // Thêm mô tả bảo hành
//             Label lblWarrantyInfo = new Label("Không bảo hành");
//             lblWarrantyInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #757575;");

//             // Cập nhật giá khi thay đổi loại bảo hành
//             cbWarranty.setOnAction(e -> {
//                 String selectedType = cbWarranty.getValue();

//                 // TH1: Không bảo hành
//                 if (selectedType.equals("Không")) {
//                     lblWarrantyPrice.setText("Phí bảo hành: 0đ");
//                     double basePrice = product.getPrice() * item.getQuantity();
//                     lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,.0f", basePrice) + "đ");
//                     lblWarrantyInfo.setText("Không bảo hành cho sản phẩm này");
//                     lblWarrantyInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #757575;");
//                     return;
//                 }

//                 // TH2: Bảo hành thường
//                 if (selectedType.equals("Thường")) {
//                     lblWarrantyPrice.setText("Phí bảo hành: 0đ");
//                     double basePrice = product.getPrice() * item.getQuantity();
//                     lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,.0f", basePrice) + "đ");
//                     lblWarrantyInfo.setText("Bảo hành thường miễn phí 12 tháng");
//                     lblWarrantyInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #4CAF50;");
//                     return;
//                 }

//                 // TH3: Bảo hành vàng (10% giá gốc)
//                 double warrantyFee = product.getPrice() * 0.1 * item.getQuantity();
//                 lblWarrantyPrice.setText("Phí bảo hành: " + String.format("%,.0f", warrantyFee) + "đ");

//                 // Cập nhật tổng tiền
//                 double totalPrice = (product.getPrice() * item.getQuantity()) + warrantyFee;
//                 lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,.0f", totalPrice) + "đ");

//                 lblWarrantyInfo.setText("✨ Bảo hành Vàng 24 tháng, 1 đổi 1");
//                 lblWarrantyInfo.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF9800;");
//             });

//             // Nút lưu và hủy
//             Button btnSave = new Button("Lưu thay đổi");
//             btnSave.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnSave.setPrefWidth(140);
//             btnSave.setOnAction(e -> {
//                 String selectedType = cbWarranty.getValue();

//                 if ("Không".equals(selectedType)) {
//                     // Xóa bảo hành nếu chọn không bảo hành
//                     item.setWarranty(null);
//                 } else {
//                     // Tạo bảo hành mới với loại đã chọn
//                     Warranty warranty = WarrantyCalculator.createWarranty(product, selectedType);
//                     item.setWarranty(warranty);
//                 }

//                 // Cập nhật hiển thị
//                 updateCartDisplay();
//                 dialogStage.close();
//                 AlertUtil.showInformation("Thành công", "Đã cập nhật bảo hành cho sản phẩm");
//             });

//             Button btnCancel = new Button("Hủy");
//             btnCancel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnCancel.setPrefWidth(80);
//             btnCancel.setOnAction(e -> dialogStage.close());

//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.getChildren().addAll(btnSave, btnCancel);

//             // Thêm các thành phần vào dialog
//             dialogContent.getChildren().addAll(
//                     lblTitle,
//                     new Separator(),
//                     cbWarranty,
//                     lblWarrantyInfo,
//                     lblWarrantyPrice,
//                     lblTotalWithWarranty,
//                     buttonBox
//             );

//             // Hiện dialog
//             Scene scene = new Scene(dialogContent, 350, 320);
//             dialogStage.setScene(scene);
//             dialogStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị dialog chỉnh sửa bảo hành", e);
//             AlertUtil.showError("Lỗi", "Không thể mở cửa sổ chỉnh sửa bảo hành");
//         }
//     }

//     // Thêm nút xóa vào bảng giỏ hàng
//     private void addButtonsToTable() {
//         if (cartTable == null) {
//             LOGGER.warning("Lỗi: cartTable chưa được khởi tạo");
//             return;
//         }

//         colCartAction = new TableColumn<>("Xóa");
//         colCartAction.setCellFactory(param -> new TableCell<CartItemEmployee, Void>() {
//             private final Button btnDelete = new Button("X");

//             {
//                 btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//                 btnDelete.setOnAction(event -> {
//                     CartItemEmployee item = getTableRow().getItem();
//                     if (item != null) {
//                         // Hiện dialog xác nhận trước khi xóa
//                         Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
//                                 "Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?",
//                                 ButtonType.YES, ButtonType.NO);
//                         alert.setTitle("Xác nhận xóa");
//                         alert.setHeaderText("Xóa sản phẩm");

//                         Optional<ButtonType> result = alert.showAndWait();
//                         if (result.isPresent() && result.get() == ButtonType.YES) {
//                             cartItems.remove(item);
//                             updateTotal();
//                         }
//                     }
//                 });
//             }

//             @Override
//             protected void updateItem(Void item, boolean empty) {
//                 super.updateItem(item, empty);
//                 if (empty) {
//                     setGraphic(null);
//                 } else {
//                     setGraphic(btnDelete);
//                 }
//             }
//         });

//         colCartAction.setPrefWidth(50);

//         // Thêm cột vào TableView nếu chưa có
//         if (!cartTable.getColumns().contains(colCartAction)) {
//             cartTable.getColumns().add(colCartAction);
//         }
//     }

//     // Hiển thị thông báo lỗi
//     private void showErrorAlert(String message) {
//         Alert alert = new Alert(Alert.AlertType.WARNING, message);
//         alert.setTitle("Lỗi");
//         alert.setHeaderText("Thông tin không hợp lệ");
//         alert.showAndWait();
//     }


//     // Thêm method mới vào PosOverviewController
//     private void showOrderByIdWindow(String orderIdInput) {
//         try {
//             LOGGER.info("🔍 Tìm kiếm đơn hàng với ID: " + orderIdInput);

//             // Chuẩn hóa orderID (có thể người dùng nhập 1, 2, 3 hoặc ORD001, ORD002)
//             String searchOrderId = normalizeOrderId(orderIdInput);
//             LOGGER.info("📝 OrderID sau khi chuẩn hóa: " + searchOrderId);

//             // Tìm đơn hàng trong database
//             OrderHistoryServiceE.OrderWithDetails orderData = OrderHistoryServiceE.getCompleteOrderById(searchOrderId);

//             if (orderData == null || orderData.getOrderHistory() == null) {
//                 AlertUtil.showWarning("Không tìm thấy",
//                         "Không tìm thấy đơn hàng với mã: " + orderIdInput + "\nĐã thử tìm: " + searchOrderId);
//                 return;
//             }

//             OrderHistory order = orderData.getOrderHistory();
//             ObservableList<OrderDetail> details = orderData.getOrderDetails();

//             LOGGER.info("✅ Tìm thấy đơn hàng: " + order.getOrderID() + " với " + details.size() + " sản phẩm");

//             // Tạo cửa sổ hiển thị chi tiết
//             showSingleOrderDetailWindow(order, details);

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi tìm đơn hàng theo ID: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể tìm đơn hàng: " + e.getMessage());
//         }
//     }

//     // Helper method chuẩn hóa orderID
//     private String normalizeOrderId(String input) {
//         if (input == null || input.trim().isEmpty()) {
//             return input;
//         }

//         String trimmed = input.trim();

//         // Nếu đã có định dạng ORDxxx thì giữ nguyên
//         if (trimmed.toUpperCase().startsWith("ORD")) {
//             return trimmed;
//         }

//         // Nếu là số thuần túy, thử cả 2 cách
//         try {
//             int numericId = Integer.parseInt(trimmed);
//             // Thử format ORD001 trước
//             return String.format("ORD%03d", numericId);
//         } catch (NumberFormatException e) {
//             // Nếu không phải số, trả về nguyên input
//             return trimmed;
//         }
//     }
//     // Thêm method hiển thị chi tiết đơn hàng
//     private void showSingleOrderDetailWindow(OrderHistory order, ObservableList<OrderDetail> details) {
//         try {
//             Stage detailStage = new Stage();
//             detailStage.initModality(Modality.APPLICATION_MODAL);
//             detailStage.setTitle("Chi tiết đơn hàng #" + order.getOrderID());
//             detailStage.setResizable(true);

//             BorderPane mainLayout = new BorderPane();

//             // Header đẹp
//             HBox header = new HBox();
//             header.setAlignment(Pos.CENTER);
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: linear-gradient(to right, #4CAF50, #45a049);");

//             Label headerTitle = new Label("CHI TIẾT ĐƠN HÀNG #" + order.getOrderID());
//             headerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
//             header.getChildren().add(headerTitle);

//             // Content
//             VBox content = new VBox(15);
//             content.setPadding(new Insets(20));

//             // Thông tin đơn hàng
//             GridPane infoGrid = new GridPane();
//             infoGrid.setHgap(15);
//             infoGrid.setVgap(10);
//             infoGrid.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8;");

//             int row = 0;
//             addInfoRow(infoGrid, "Mã đơn hàng:", order.getOrderID(), row++);
//             addInfoRow(infoGrid, "Ngày đặt:", order.getFormattedDate(), row++);
//             addInfoRow(infoGrid, "Khách hàng:", order.getCustomerName(), row++);
//             addInfoRow(infoGrid, "Số điện thoại:", order.getCustomerPhone(), row++);
//             addInfoRow(infoGrid, "Nhân viên:", order.getEmployeeName(), row++);
//             addInfoRow(infoGrid, "Phương thức thanh toán:", order.getPaymentMethod(), row++);
//             addInfoRow(infoGrid, "Trạng thái:", order.getStatus(), row++);

//             // Bảng sản phẩm
//             Label productsLabel = new Label("DANH SÁCH SẢN PHẨM:");
//             productsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             TableView<OrderDetail> productsTable = new TableView<>();
//             productsTable.setPrefHeight(300);
//             productsTable.setItems(details);

//             // Các cột
//             TableColumn<OrderDetail, String> colProductName = new TableColumn<>("Tên sản phẩm");
//             colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
//             colProductName.setPrefWidth(250);

//             TableColumn<OrderDetail, Integer> colQuantity = new TableColumn<>("SL");
//             colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
//             colQuantity.setPrefWidth(50);

//             TableColumn<OrderDetail, String> colUnitPrice = new TableColumn<>("Đơn giá");
//             colUnitPrice.setCellValueFactory(data ->
//                     new SimpleStringProperty(String.format("%,.0f₫", data.getValue().getUnitPrice())));
//             colUnitPrice.setPrefWidth(100);

//             TableColumn<OrderDetail, String> colWarranty = new TableColumn<>("Bảo hành");
//             colWarranty.setCellValueFactory(new PropertyValueFactory<>("warrantyType"));
//             colWarranty.setPrefWidth(100);

//             TableColumn<OrderDetail, String> colSubtotal = new TableColumn<>("Thành tiền");
//             colSubtotal.setCellValueFactory(data ->
//                     new SimpleStringProperty(String.format("%,.0f₫", data.getValue().getSubtotal())));
//             colSubtotal.setPrefWidth(120);

//             productsTable.getColumns().addAll(colProductName, colQuantity, colUnitPrice, colWarranty, colSubtotal);

//             // Tổng tiền
//             Label totalLabel = new Label("TỔNG TIỀN: " + order.getFormattedAmount());
//             totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e91e63;");

//             // Buttons
//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.setPadding(new Insets(10, 0, 0, 0));

//             Button btnPrint = new Button("In hóa đơn");
//             btnPrint.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnPrint.setPrefWidth(120);
//             btnPrint.setOnAction(e -> {
//                 // Gọi method in hóa đơn (sử dụng lại code cũ)
//                 AlertUtil.showInfo("Thông báo", "Tính năng in hóa đơn đang được phát triển!");
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnClose.setPrefWidth(100);
//             btnClose.setOnAction(e -> detailStage.close());

//             buttonBox.getChildren().addAll(btnPrint, btnClose);

//             // Thêm vào content
//             content.getChildren().addAll(infoGrid, productsLabel, productsTable, totalLabel, buttonBox);

//             // Layout chính
//             mainLayout.setTop(header);
//             mainLayout.setCenter(new ScrollPane(content));

//             Scene scene = new Scene(mainLayout, 700, 600);
//             detailStage.setScene(scene);
//             detailStage.show();

//             LOGGER.info("✅ Đã hiển thị chi tiết đơn hàng: " + order.getOrderID());

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi hiển thị chi tiết đơn hàng: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị chi tiết đơn hàng: " + e.getMessage());
//         }
//     }

//     // Helper method thêm dòng thông tin
//     private void addInfoRow(GridPane grid, String label, String value, int row) {
//         Label lblLabel = new Label(label);
//         lblLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");

//         Label lblValue = new Label(value != null ? value : "N/A");
//         lblValue.setStyle("-fx-font-weight: bold;");

//         grid.add(lblLabel, 0, row);
//         grid.add(lblValue, 1, row);
//     }
//     // Method hiển thị tất cả đơn hàng (nếu user chọn checkbox)
//     private void showAllOrdersWindow() {
//         try {
//             LOGGER.info("📋 Hiển thị tất cả đơn hàng...");

//             ObservableList<OrderHistory> allOrders = OrderHistoryServiceE.getOrderHistories();

//             if (allOrders.isEmpty()) {
//                 AlertUtil.showInfo("Thông báo", "Không có đơn hàng nào trong hệ thống!");
//                 return;
//             }

//             // Tạo cửa sổ đơn giản hiển thị danh sách
//             Stage listStage = new Stage();
//             listStage.setTitle("Tất cả đơn hàng (" + allOrders.size() + " đơn)");
//             listStage.setResizable(true);

//             // TableView đơn giản
//             TableView<OrderHistory> table = new TableView<>();
//             table.setItems(allOrders);

//             TableColumn<OrderHistory, String> colId = new TableColumn<>("Mã ĐH");
//             colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrderID()));
//             colId.setPrefWidth(100);

//             TableColumn<OrderHistory, String> colDate = new TableColumn<>("Ngày");
//             colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedDate()));
//             colDate.setPrefWidth(150);

//             TableColumn<OrderHistory, String> colCustomer = new TableColumn<>("Khách hàng");
//             colCustomer.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomerName()));
//             colCustomer.setPrefWidth(150);

//             TableColumn<OrderHistory, String> colTotal = new TableColumn<>("Tổng tiền");
//             colTotal.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedAmount()));
//             colTotal.setPrefWidth(120);

//             TableColumn<OrderHistory, Void> colAction = new TableColumn<>("Hành động");
//             colAction.setCellFactory(tc -> new TableCell<OrderHistory, Void>() {
//                 private final Button btn = new Button("Xem chi tiết");
//                 {
//                     btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
//                     btn.setOnAction(event -> {
//                         OrderHistory selectedOrder = getTableView().getItems().get(getIndex());
//                         if (selectedOrder != null) {
//                             listStage.close();
//                             showOrderByIdWindow(selectedOrder.getOrderID());
//                         }
//                     });
//                 }

//                 @Override
//                 protected void updateItem(Void item, boolean empty) {
//                     super.updateItem(item, empty);
//                     if (empty) {
//                         setGraphic(null);
//                     } else {
//                         setGraphic(btn);
//                     }
//                 }
//             });
//             colAction.setPrefWidth(120);

//             table.getColumns().addAll(colId, colDate, colCustomer, colTotal, colAction);

//             Scene scene = new Scene(new VBox(table), 800, 500);
//             listStage.setScene(scene);
//             listStage.show();

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi hiển thị tất cả đơn hàng: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị danh sách đơn hàng: " + e.getMessage());
//         }
//     }
//     // Hiển thị lịch sử đơn hàng từ bộ nhớ
//     // Thay thế method showOrderHistoryInMemory() cũ
//     private void showOrderHistoryInMemory() {
//         try {
//             // Tạo dialog nhập mã đơn hàng
//             Stage searchStage = new Stage();
//             searchStage.initModality(Modality.APPLICATION_MODAL);
//             searchStage.setTitle("Tìm kiếm đơn hàng");
//             searchStage.setResizable(false);

//             VBox layout = new VBox(15);
//             layout.setPadding(new Insets(20));
//             layout.setAlignment(Pos.CENTER);

//             // Header
//             Label headerLabel = new Label("TÌM KIẾM ĐƠN HÀNG");
//             headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

//             // Input mã đơn hàng
//             Label lblOrderId = new Label("Nhập mã đơn hàng:");
//             lblOrderId.setStyle("-fx-font-weight: bold;");

//             TextField txtOrderId = new TextField();
//             txtOrderId.setPromptText("Ví dụ: 1, 2, 3... hoặc ORD001, ORD002...");
//             txtOrderId.setPrefWidth(300);
//             txtOrderId.setStyle("-fx-font-size: 14px;");

//             // Hoặc xem tất cả
//             CheckBox chkShowAll = new CheckBox("Hiển thị tất cả đơn hàng");
//             chkShowAll.setStyle("-fx-font-size: 12px;");

//             // Buttons
//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);

//             Button btnSearch = new Button("Tìm kiếm");
//             btnSearch.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnSearch.setPrefWidth(100);

//             Button btnCancel = new Button("Hủy");
//             btnCancel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnCancel.setPrefWidth(100);

//             buttonBox.getChildren().addAll(btnSearch, btnCancel);

//             // Events
//             btnCancel.setOnAction(e -> searchStage.close());

//             btnSearch.setOnAction(e -> {
//                 try {
//                     searchStage.close();

//                     if (chkShowAll.isSelected()) {
//                         // Hiển thị tất cả đơn hàng
//                         showAllOrdersWindow();
//                     } else {
//                         // Tìm theo ID cụ thể
//                         String orderId = txtOrderId.getText().trim();
//                         if (orderId.isEmpty()) {
//                             AlertUtil.showWarning("Thông báo", "Vui lòng nhập mã đơn hàng!");
//                             return;
//                         }
//                         showOrderByIdWindow(orderId);
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi tìm kiếm đơn hàng: " + ex.getMessage(), ex);
//                     AlertUtil.showError("Lỗi", "Không thể tìm kiếm đơn hàng: " + ex.getMessage());
//                 }
//             });

//             // Enter để tìm kiếm
//             txtOrderId.setOnKeyPressed(event -> {
//                 if (event.getCode().toString().equals("ENTER")) {
//                     btnSearch.fire();
//                 }
//             });

//             layout.getChildren().addAll(headerLabel, lblOrderId, txtOrderId, chkShowAll, buttonBox);

//             Scene scene = new Scene(layout, 400, 250);
//             searchStage.setScene(scene);
//             searchStage.showAndWait();

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị dialog tìm kiếm: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể mở cửa sổ tìm kiếm: " + e.getMessage());
//         }
//     }

//     // Hiển thị chi tiết đơn hàng từ bộ nhớ
//     private void showOrderDetailsFromMemory(OrderSummary order) {
//         try {
//             if (order == null) {
//                 LOGGER.warning("Lỗi: OrderSummary object là null");
//                 return;
//             }

//             Stage detailStage = new Stage();
//             detailStage.initModality(Modality.APPLICATION_MODAL);
//             detailStage.setTitle("Chi tiết đơn hàng #" + order.getId());

//             BorderPane borderPane = new BorderPane();

//             // Header
//             HBox header = new HBox();
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: #2196F3;");

//             Label headerTitle = new Label("CHI TIẾT ĐƠN HÀNG #" + order.getId());
//             headerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

//             header.getChildren().add(headerTitle);
//             header.setAlignment(Pos.CENTER);

//             borderPane.setTop(header);

//             // Content
//             VBox content = new VBox(15);
//             content.setPadding(new Insets(20));

//             // Thông tin đơn hàng
//             VBox orderInfoBox = new VBox(8);
//             orderInfoBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");

//             Label lblCustomer = new Label("Khách hàng: " + order.getCustomerName());
//             Label lblPhone = new Label("SĐT: " + order.getCustomerPhone());
//             Label lblPayment = new Label("Phương thức thanh toán: " + order.getPaymentMethod());
//             Label lblDate = new Label("Ngày mua: " + order.getOrderDate());

//             orderInfoBox.getChildren().addAll(lblCustomer, lblPhone, lblPayment, lblDate);

//             // Danh sách sản phẩm
//             Label lblProductsTitle = new Label("Danh sách sản phẩm:");
//             lblProductsTitle.setStyle("-fx-font-weight: bold;");

//             TableView<CartItemEmployee> detailTable = new TableView<>();
//             detailTable.setPrefHeight(300);

//             TableColumn<CartItemEmployee, String> colProductName = new TableColumn<>("Tên sản phẩm");
//             colProductName.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleStringProperty("N/A");
//                 }
//                 String productName = data.getValue().getProductName();
//                 return new SimpleStringProperty(productName != null ? productName : "N/A");
//             });
//             colProductName.setPrefWidth(200);

//             TableColumn<CartItemEmployee, Integer> colQuantity = new TableColumn<>("SL");
//             colQuantity.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleIntegerProperty(0).asObject();
//                 }
//                 return new SimpleIntegerProperty(data.getValue().getQuantity()).asObject();
//             });
//             colQuantity.setPrefWidth(50);

//             TableColumn<CartItemEmployee, Double> colPrice = new TableColumn<>("Đơn giá");
//             colPrice.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleDoubleProperty(0).asObject();
//                 }
//                 return new SimpleDoubleProperty(data.getValue().getPrice()).asObject();
//             });
//             colPrice.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double price, boolean empty) {
//                     super.updateItem(price, empty);
//                     if (empty || price == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", price) + "đ");
//                     }
//                 }
//             });
//             colPrice.setPrefWidth(100);

//             // Thêm cột bảo hành
//             TableColumn<CartItemEmployee, String> colWarranty = new TableColumn<>("Bảo hành");
//             colWarranty.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleStringProperty("Không");
//                 }

//                 CartItemEmployee item = data.getValue();
//                 if (item.hasWarranty()) {
//                     return new SimpleStringProperty(item.getWarranty().getWarrantyType());
//                 } else {
//                     return new SimpleStringProperty("Không");
//                 }
//             });
//             colWarranty.setPrefWidth(100);

//             TableColumn<CartItemEmployee, Double> colSubtotal = new TableColumn<>("Thành tiền");
//             colSubtotal.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleDoubleProperty(0).asObject();
//                 }
//                 return new SimpleDoubleProperty(data.getValue().getTotalPrice()).asObject();
//             });
//             colSubtotal.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double total, boolean empty) {
//                     super.updateItem(total, empty);
//                     if (empty || total == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", total) + "đ");
//                     }
//                 }
//             });
//             colSubtotal.setPrefWidth(100);

//             detailTable.getColumns().addAll(colProductName, colQuantity, colPrice, colWarranty, colSubtotal);

//             // Kiểm tra null trước khi thêm items
//             if (order.getItems() != null) {
//                 detailTable.setItems(FXCollections.observableArrayList(order.getItems()));
//             } else {
//                 detailTable.setItems(FXCollections.observableArrayList());
//             }

//             // Hiển thị tổng tiền
//             Label lblTotal = new Label("Tổng tiền: " + String.format("%,.0f", order.getTotalAmount()) + "đ");
//             lblTotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e91e63;");

//             // Button in hóa đơn và đóng
//             Button btnPrint = new Button("In hóa đơn");
//             btnPrint.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnPrint.setPrefWidth(150);

//             // Fix lỗi lambda expression bằng cách sử dụng final variable
//             final int orderId = order.getId();
//             final double totalAmount = order.getTotalAmount();
//             final String customerName2 = order.getCustomerName();
//             final String customerPhone2 = order.getCustomerPhone();
//             final String paymentMethod2 = order.getPaymentMethod();
//             final String orderDateTime = order.getOrderDate();
//             final List<CartItemEmployee> orderItems = order.getItems() != null ? order.getItems() : new ArrayList<>();

//             btnPrint.setOnAction(e -> {
//                 try {
//                     // In hóa đơn với các biến final
//                     printReceiptWithPaymentMethod(
//                             orderId,
//                             orderItems,
//                             totalAmount,
//                             customerName2,
//                             customerPhone2,
//                             paymentMethod2,
//                             orderDateTime,
//                             currentUser
//                     );
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi in hóa đơn", ex);
//                     showErrorAlert("Có lỗi xảy ra: " + ex.getMessage());
//                 }
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnClose.setPrefWidth(100);
//             btnClose.setOnAction(e -> detailStage.close());

//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.getChildren().addAll(btnPrint, btnClose);
//             buttonBox.setPadding(new Insets(10, 0, 0, 0));

//             content.getChildren().addAll(orderInfoBox, lblProductsTitle, detailTable, lblTotal, buttonBox);

//             borderPane.setCenter(content);

//             Scene scene = new Scene(borderPane, 650, 550);
//             detailStage.setScene(scene);
//             detailStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị chi tiết đơn hàng", e);
//             showErrorAlert("Có lỗi xảy ra: " + e.getMessage());
//         }
//     }

//     // Phương thức in hóa đơn có thêm phương thức thanh toán và thông tin bảo hành
//     public void printReceiptWithPaymentMethod(int orderID, List<CartItemEmployee> items, double totalAmount,
//                                               String customerName, String customerPhone, String paymentMethod,
//                                               String orderDateTime, String cashierName) {
//         try {
//             // Kiểm tra danh sách sản phẩm
//             if (items == null || items.isEmpty()) {
//                 Alert alert = new Alert(Alert.AlertType.WARNING);
//                 alert.setTitle("Cảnh báo");
//                 alert.setHeaderText("Không thể in hóa đơn");
//                 alert.setContentText("Không có sản phẩm nào trong đơn hàng.");
//                 alert.showAndWait();
//                 return;
//             }

//             // Tạo cảnh báo để hiển thị trước khi in
//             Alert printingAlert = new Alert(Alert.AlertType.INFORMATION);
//             printingAlert.setTitle("Đang in hóa đơn");
//             printingAlert.setHeaderText("Đang chuẩn bị in hóa đơn");
//             printingAlert.setContentText("Vui lòng đợi trong giây lát...");
//             printingAlert.show();

//             // Tạo nội dung hóa đơn
//             VBox receiptContent = new VBox(5);
//             receiptContent.setPadding(new Insets(20));
//             receiptContent.setStyle("-fx-background-color: white;");

//             // Tiêu đề
//             Label lblTitle = new Label("HÓA ĐƠN THANH TOÁN");
//             lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-alignment: center;");
//             lblTitle.setMaxWidth(Double.MAX_VALUE);
//             lblTitle.setAlignment(Pos.CENTER);

//             // Logo công ty (nếu có)
//             ImageView logo = new ImageView();
//             try {
//                 InputStream is = getClass().getResourceAsStream("/com/example/stores/images/layout/employee_logo.png");
//                 if (is != null) {
//                     logo.setImage(new Image(is));
//                     logo.setFitWidth(100);
//                     logo.setPreserveRatio(true);
//                 }
//             } catch (Exception e) {
//                 LOGGER.log(Level.WARNING, "Không tìm thấy logo", e);
//             }

//             // Thông tin cửa hàng
//             Label lblStoreName = new Label("COMPUTER STORE");
//             lblStoreName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             Label lblStoreAddress = new Label("Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM");
//             Label lblStorePhone = new Label("Điện thoại: 028.1234.5678");

//             // Thông tin hóa đơn
//             Label lblOrderID = new Label("Mã đơn hàng: #" + orderID);
//             lblOrderID.setStyle("-fx-font-weight: bold;");

//             Label lblDateTime = new Label("Ngày: " + orderDateTime);
//             Label lblCashier = new Label("Thu ngân: " + cashierName);
//             Label lblCustomerName = new Label("Khách hàng: " + customerName);
//             Label lblCustomerPhone = new Label("SĐT khách hàng: " + customerPhone);
//             Label lblPaymentMethod = new Label("Phương thức thanh toán: " + paymentMethod);
//             lblPaymentMethod.setStyle("-fx-font-weight: bold;");

//             // Tạo đường kẻ ngăn cách
//             Separator sep1 = new Separator();
//             sep1.setMaxWidth(Double.MAX_VALUE);

//             // Tiêu đề bảng sản phẩm
//             HBox tableHeader = new HBox(10);
//             Label lblProductHeader = new Label("Sản phẩm");
//             lblProductHeader.setPrefWidth(200);
//             lblProductHeader.setStyle("-fx-font-weight: bold;");

//             Label lblQtyHeader = new Label("SL");
//             lblQtyHeader.setPrefWidth(50);
//             lblQtyHeader.setStyle("-fx-font-weight: bold;");

//             Label lblPriceHeader = new Label("Đơn giá");
//             lblPriceHeader.setPrefWidth(100);
//             lblPriceHeader.setStyle("-fx-font-weight: bold;");

//             Label lblWarrantyHeader = new Label("Bảo hành");
//             lblWarrantyHeader.setPrefWidth(100);
//             lblWarrantyHeader.setStyle("-fx-font-weight: bold;");

//             Label lblSubtotalHeader = new Label("Thành tiền");
//             lblSubtotalHeader.setPrefWidth(100);
//             lblSubtotalHeader.setStyle("-fx-font-weight: bold;");

//             tableHeader.getChildren().addAll(lblProductHeader, lblQtyHeader, lblPriceHeader, lblWarrantyHeader, lblSubtotalHeader);

//             // Danh sách sản phẩm
//             VBox productsBox = new VBox(5);
//             double totalWarrantyPrice = 0.0; // Tổng phí bảo hành

//             for (CartItemEmployee item : items) {
//                 if (item == null) continue;

//                 // Dòng sản phẩm
//                 HBox row = new HBox(10);

//                 String productName = item.getProductName();
//                 if (productName == null) productName = "Sản phẩm không tên";

//                 // Tạo VBox để hiển thị tên sản phẩm + bảo hành nếu có
//                 VBox productInfoBox = new VBox(2);
//                 Label lblProduct = new Label(productName);
//                 lblProduct.setPrefWidth(200);
//                 lblProduct.setWrapText(true);
//                 productInfoBox.getChildren().add(lblProduct);

//                 Label lblQty = new Label(String.valueOf(item.getQuantity()));
//                 lblQty.setPrefWidth(50);

//                 Label lblPrice = new Label(String.format("%,.0f", item.getPrice()) + "đ");
//                 lblPrice.setPrefWidth(100);

//                 // Hiển thị thông tin bảo hành
//                 Label lblWarranty;
//                 if (item.hasWarranty()) {
//                     lblWarranty = new Label(item.getWarranty().getWarrantyType());
//                     totalWarrantyPrice += item.getWarranty().getWarrantyPrice();
//                 } else {
//                     lblWarranty = new Label("Không");
//                 }
//                 lblWarranty.setPrefWidth(100);

//                 // Hiển thị tổng giá trị sản phẩm
//                 Label lblSubtotal = new Label(String.format("%,.0f", item.getTotalPrice()) + "đ");
//                 lblSubtotal.setPrefWidth(100);

//                 row.getChildren().addAll(productInfoBox, lblQty, lblPrice, lblWarranty, lblSubtotal);
//                 productsBox.getChildren().add(row);
//             }

//             // Thêm đường kẻ ngăn cách
//             Separator sep2 = new Separator();
//             sep2.setMaxWidth(Double.MAX_VALUE);

//             // Hiển thị tổng phí bảo hành nếu có
//             VBox summaryBox = new VBox(5);

//             if (totalWarrantyPrice > 0) {
//                 HBox warrantyRow = new HBox(10);
//                 warrantyRow.setAlignment(Pos.CENTER_RIGHT);

//                 Label lblWarrantyTotalHeader = new Label("Tổng phí bảo hành:");
//                 Label lblWarrantyValue = new Label(String.format("%,.0f", totalWarrantyPrice) + "đ");
//                 lblWarrantyValue.setStyle("-fx-font-size: 13px;");

//                 warrantyRow.getChildren().addAll(lblWarrantyHeader, lblWarrantyValue);
//                 summaryBox.getChildren().add(warrantyRow);
//             }

//             // Tổng tiền
//             HBox totalRow = new HBox(10);
//             totalRow.setAlignment(Pos.CENTER_RIGHT);

//             Label lblTotalHeader = new Label("Tổng tiền thanh toán:");
//             lblTotalHeader.setStyle("-fx-font-weight: bold;");

//             Label lblTotalValue = new Label(String.format("%,.0f", totalAmount) + "đ");
//             lblTotalValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             totalRow.getChildren().addAll(lblTotalHeader, lblTotalValue);
//             summaryBox.getChildren().add(totalRow);

//             // Thêm thông tin thanh toán chuyển khoản nếu là phương thức chuyển khoản
//             VBox paymentInfoBox = new VBox(10);
//             paymentInfoBox.setAlignment(Pos.CENTER);

//             if ("Chuyển khoản".equals(paymentMethod)) {
//                 // Thêm đường kẻ ngăn cách
//                 Separator sepPayment = new Separator();
//                 sepPayment.setMaxWidth(Double.MAX_VALUE);

//                 Label lblPaymentInfo = new Label("THÔNG TIN CHUYỂN KHOẢN");
//                 lblPaymentInfo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
//                 lblPaymentInfo.setAlignment(Pos.CENTER);
//                 lblPaymentInfo.setMaxWidth(Double.MAX_VALUE);

//                 Label lblBank = new Label("Ngân hàng: TECHCOMBANK");
//                 Label lblAccount = new Label("Số tài khoản: 1903 5552 6789");
//                 Label lblAccountName = new Label("Chủ TK: CÔNG TY COMPUTER STORE");
//                 Label lblContent = new Label("Nội dung CK: " + orderID + " " + customerPhone);

//                 // QR Code cho chuyển khoản
//                 ImageView qrCode = new ImageView();
//                 try {
//                     // Mặc định sử dụng ảnh QR từ resources
//                     InputStream qrIs = getClass().getResourceAsStream("/com/example/stores/images/qr_payment.png");
//                     if (qrIs != null) {
//                         qrCode.setImage(new Image(qrIs));
//                         qrCode.setFitWidth(150);
//                         qrCode.setPreserveRatio(true);
//                     } else {
//                         // QR Code cho chuyển khoản - tạo ảnh trống nếu không tìm thấy
//                         qrCode.setFitWidth(150);
//                         qrCode.setFitHeight(150);
//                         qrCode.setStyle("-fx-background-color: #f0f0f0;");
//                     }
//                 } catch (Exception e) {
//                     LOGGER.log(Level.WARNING, "Không tìm thấy ảnh QR", e);
//                 }

//                 paymentInfoBox.getChildren().addAll(sepPayment, lblPaymentInfo, lblBank, lblAccount, lblAccountName, lblContent, qrCode);
//             }

//             // Thông tin cuối hóa đơn
//             Label lblThankYou = new Label("Cảm ơn quý khách đã mua hàng!");
//             lblThankYou.setAlignment(Pos.CENTER);
//             lblThankYou.setMaxWidth(Double.MAX_VALUE);
//             lblThankYou.setStyle("-fx-font-style: italic; -fx-alignment: center;");

//             Label lblContact = new Label("Hotline: 1800.1234 - Website: www.computerstore.com.vn");
//             lblContact.setAlignment(Pos.CENTER);
//             lblContact.setMaxWidth(Double.MAX_VALUE);
//             lblContact.setStyle("-fx-font-size: 10px; -fx-alignment: center;");

//             // Thêm thông tin chính sách bảo hành
//             Label lblWarrantyPolicy = new Label("Để biết thêm về chính sách bảo hành, vui lòng xem tại website");
//             lblWarrantyPolicy.setAlignment(Pos.CENTER);
//             lblWarrantyPolicy.setMaxWidth(Double.MAX_VALUE);
//             lblWarrantyPolicy.setStyle("-fx-font-size: 10px; -fx-font-style: italic; -fx-alignment: center;");

//             // Thêm tất cả các phần tử vào hóa đơn
//             HBox logoBox = new HBox(10);
//             logoBox.setAlignment(Pos.CENTER);
//             logoBox.getChildren().add(logo);

//             receiptContent.getChildren().addAll(
//                     lblTitle,
//                     logoBox,
//                     lblStoreName,
//                     lblStoreAddress,
//                     lblStorePhone,
//                     new Separator(),
//                     lblOrderID,
//                     lblDateTime,
//                     lblCashier,
//                     lblCustomerName,
//                     lblCustomerPhone,
//                     lblPaymentMethod,
//                     sep1,
//                     tableHeader,
//                     productsBox,
//                     sep2,
//                     summaryBox
//             );

//             // Thêm thông tin thanh toán chuyển khoản nếu có
//             if (!paymentInfoBox.getChildren().isEmpty()) {
//                 receiptContent.getChildren().add(paymentInfoBox);
//             }

//             // Thêm phần kết
//             Separator sepEnd = new Separator();
//             sepEnd.setMaxWidth(Double.MAX_VALUE);

//             receiptContent.getChildren().addAll(
//                     sepEnd,
//                     lblThankYou,
//                     lblContact,
//                     lblWarrantyPolicy
//             );

//             // Định dạng kích thước hóa đơn
//             ScrollPane scrollPane = new ScrollPane(receiptContent);
//             scrollPane.setPrefWidth(550); // Tăng kích thước để hiển thị đủ cột bảo hành
//             scrollPane.setPrefHeight(600);
//             scrollPane.setFitToWidth(true);

//             // Tạo Scene và Stage để hiển thị trước khi in
//             Scene scene = new Scene(scrollPane);
//             Stage printPreviewStage = new Stage();
//             printPreviewStage.setTitle("Xem trước hóa đơn");
//             printPreviewStage.setScene(scene);

//             // Đóng cảnh báo đang in
//             printingAlert.close();

//             // Hiển thị hóa đơn
//             printPreviewStage.show();

//             // Thêm nút in và lưu vào cửa sổ xem trước
//             Button btnPrint = new Button("In");
//             btnPrint.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
//             btnPrint.setOnAction(e -> {
//                 try {
//                     PrinterJob job = PrinterJob.createPrinterJob();
//                     if (job != null) {
//                         boolean success = job.printPage(receiptContent);
//                         if (success) {
//                             job.endJob();
//                             printPreviewStage.close();

//                             Alert printSuccessAlert = new Alert(Alert.AlertType.INFORMATION);
//                             printSuccessAlert.setTitle("In thành công");
//                             printSuccessAlert.setHeaderText("Hóa đơn đã được gửi đến máy in");
//                             printSuccessAlert.setContentText("Vui lòng kiểm tra máy in của bạn.");
//                             printSuccessAlert.showAndWait();
//                         }
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi in hóa đơn", ex);
//                     showErrorAlert("Lỗi khi in hóa đơn: " + ex.getMessage());
//                 }
//             });

//             // Nút lưu PDF (giả định)
//             Button btnSave = new Button("Lưu PDF");
//             btnSave.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
//             btnSave.setOnAction(e -> {
//                 try {
//                     Alert saveAlert = new Alert(Alert.AlertType.INFORMATION);
//                     saveAlert.setTitle("Lưu PDF");
//                     saveAlert.setHeaderText("Hóa đơn đã được lưu");
//                     saveAlert.setContentText("Hóa đơn đã được lưu vào thư mục Documents.");
//                     saveAlert.showAndWait();
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi lưu PDF", ex);
//                     showErrorAlert("Lỗi khi lưu PDF: " + ex.getMessage());
//                 }
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnClose.setOnAction(e -> printPreviewStage.close());

//             HBox buttonBox = new HBox(10, btnPrint, btnSave, btnClose);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.setPadding(new Insets(10));

//             BorderPane borderPane = new BorderPane();
//             borderPane.setCenter(scrollPane);
//             borderPane.setBottom(buttonBox);

//             scene.setRoot(borderPane);

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi in hóa đơn", e);
//             Alert errorAlert = new Alert(Alert.AlertType.ERROR);
//             errorAlert.setTitle("Lỗi in hóa đơn");
//             errorAlert.setHeaderText("Không thể in hóa đơn");
//             errorAlert.setContentText("Chi tiết lỗi: " + e.getMessage());
//             errorAlert.showAndWait();
//         }
//     }

//     /**
//      * Thêm sản phẩm vào giỏ hàng với thông tin bảo hành
//      */
//     private void addToCartWithWarranty(CartItemEmployee item) {
//         if (item == null) {
//             LOGGER.warning("Lỗi: CartItemEmployee là null");
//             return;
//         }

//         // Tìm sản phẩm trong database để kiểm tra tồn kho
//         Product product = findProductById(item.getProductID());
//         if (product == null) {
//             AlertUtil.showWarning("Lỗi", "Không tìm thấy thông tin sản phẩm");
//             return;
//         }

//         // Kiểm tra số lượng tồn kho trước khi thêm
//         if (product.getQuantity() <= 0) {
//             AlertUtil.showWarning("Hết hàng", "Sản phẩm đã hết hàng!");
//             return;
//         }

//         // Tìm kiếm sản phẩm trong giỏ hàng với CÙNG loại bảo hành
//         boolean existingFound = false;
//         for (CartItemEmployee cartItem : cartItems) {
//             if (cartItem.getProductID().equals(item.getProductID())) {
//                 // Phải cùng sản phẩm và cùng loại bảo hành
//                 if (cartItem.hasWarranty() == item.hasWarranty() &&
//                         (!cartItem.hasWarranty() ||
//                                 cartItem.getWarranty().getWarrantyType().equals(item.getWarranty().getWarrantyType()))) {

//                     if (cartItem.getQuantity() < product.getQuantity()) {
//                         // Cập nhật số lượng nếu còn hàng
//                         cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
//                         existingFound = true;
//                         LOGGER.info("Đã tăng số lượng " + cartItem.getProductName() +
//                                 " (BH: " + (cartItem.hasWarranty() ? cartItem.getWarranty().getWarrantyType() : "Không") +
//                                 ") lên " + cartItem.getQuantity());
//                     } else {
//                         AlertUtil.showWarning("Số lượng tối đa",
//                                 "Không thể thêm nữa, số lượng trong kho chỉ còn " + product.getQuantity());
//                     }
//                     break;
//                 }
//             }
//         }

//         // Nếu không tìm thấy sản phẩm đã có trong giỏ với cùng loại bảo hành
//         if (!existingFound) {
//             cartItems.add(item);
//             LOGGER.info("Đã thêm " + item.getProductName() +
//                     " (BH: " + (item.hasWarranty() ? item.getWarranty().getWarrantyType() : "Không") +
//                     ") vào giỏ hàng");
//         }

//         // Cập nhật hiển thị giỏ hàng
//         updateCartDisplay();
//     }

//     // Tìm sản phẩm theo ID
//     private Product findProductById(String productID) {
//         if (productID == null || products == null) {
//             return null;
//         }

//         for (Product product : products) {
//             if (product.getProductID().equals(productID)) {
//                 return product;
//             }
//         }

//         return null;
//     }

//     // Sửa lại phần hiển thị dialog chi tiết sản phẩm trong PosOverviewController
//     private void showProductDetails(Product product) {
//         try {
//             if (product == null) {
//                 LOGGER.warning("Lỗi: Product object là null");
//                 return;
//             }

//             Stage detailStage = new Stage();
//             detailStage.initModality(Modality.APPLICATION_MODAL);
//             detailStage.setTitle("Chi tiết sản phẩm");

//             VBox layout = new VBox(10);
//             layout.setPadding(new Insets(20));
//             layout.setStyle("-fx-background-color: white;");

//             // Hiển thị ảnh sản phẩm (giữ nguyên code cũ)
//             final ImageView productImage = new ImageView();
//             productImage.setFitWidth(200);
//             productImage.setFitHeight(150);
//             productImage.setPreserveRatio(true);

//             // Tải ảnh sản phẩm (giữ nguyên code cũ)
//             String imagePath = product.getImagePath();
//             if (imagePath != null && !imagePath.startsWith("/")) {
//                 imagePath = "/com/example/stores/images/" + imagePath;
//             } else if (imagePath == null) {
//                 imagePath = "/com/example/stores/images/no_image.png";
//             }

//             try {
//                 Image image = new Image(getClass().getResourceAsStream(imagePath));
//                 productImage.setImage(image);
//             } catch (Exception e) {
//                 productImage.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/no_image.png")));
//                 LOGGER.warning("Không tải được ảnh chi tiết sản phẩm: " + e.getMessage());
//             }

//             final HBox imageBox = new HBox();
//             imageBox.setAlignment(Pos.CENTER);
//             imageBox.getChildren().add(productImage);

//             // Tên sản phẩm
//             String productName = (product.getProductName() != null) ? product.getProductName() : "Sản phẩm không có tên";
//             Label lblName = new Label(productName);
//             lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
//             lblName.setWrapText(true);

//             // Giá sản phẩm
//             Label lblPrice = new Label(String.format("Giá: %,d₫", (long)product.getPrice()));
//             lblPrice.setStyle("-fx-text-fill: #e91e63; -fx-font-weight: bold; -fx-font-size: 16px;");

//             // Thông tin cơ bản (giữ nguyên code cũ)
//             VBox specsBox = new VBox(5);
//             specsBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");

//             if (product.getCategoryID() != null) {
//                 Label lblCategory = new Label("Danh mục: " + getCategoryName(product.getCategoryID()));
//                 specsBox.getChildren().add(lblCategory);
//             }

//             Label lblStock = new Label("Tồn kho: " + product.getQuantity() + " sản phẩm");
//             specsBox.getChildren().add(lblStock);

//             String status = product.getStatus();
//             Label lblStatus = new Label("Trạng thái: " + (status != null ? status : "Không xác định"));
//             lblStatus.setStyle(status != null && status.equals("Còn hàng") ?
//                     "-fx-text-fill: #4caf50; -fx-font-weight: bold;" :
//                     "-fx-text-fill: #f44336; -fx-font-weight: bold;");
//             specsBox.getChildren().add(lblStatus);

//             // PHẦN BẢO HÀNH - CẬP NHẬT CHỈ CÒN 2 LOẠI: THƯỜNG VÀ VÀNG
//             VBox warrantyBox = new VBox(5);
//             warrantyBox.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 10; -fx-background-radius: 5;");

//             Label lblWarrantyTitle = new Label("Lựa chọn bảo hành:");
//             lblWarrantyTitle.setStyle("-fx-font-weight: bold;");
//             warrantyBox.getChildren().add(lblWarrantyTitle);

//             // ComboBox để chọn bảo hành
//             ComboBox<String> cbWarranty = new ComboBox<>();

//             // Kiểm tra sản phẩm có đủ điều kiện bảo hành thường không
//             boolean isEligibleForStdWarranty = WarrantyCalculator.isEligibleForStandardWarranty(product);

//             Label lblWarrantyInfo = new Label();

//             // Hiển thị các lựa chọn bảo hành dựa trên điều kiện
//             if (isEligibleForStdWarranty) {
//                 cbWarranty.getItems().addAll("Thường", "Vàng");
//                 cbWarranty.setValue("Thường");

//                 // Miêu tả bảo hành
//                 lblWarrantyInfo.setText("✅ Sản phẩm được bảo hành Thường miễn phí 12 tháng");
//                 lblWarrantyInfo.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 12px;");
//             } else {
//                 cbWarranty.getItems().add("Không");
//                 cbWarranty.setValue("Không");

//                 // Miêu tả không đủ điều kiện
//                 lblWarrantyInfo.setText("❌ Sản phẩm dưới 500.000đ không được bảo hành");
//                 lblWarrantyInfo.setStyle("-fx-text-fill: #f44336; -fx-font-size: 12px;");
//             }

//             warrantyBox.getChildren().addAll(cbWarranty, lblWarrantyInfo);

//             // Hiển thị phí bảo hành
//             Label lblWarrantyPrice = new Label("Phí bảo hành: 0đ");
//             warrantyBox.getChildren().add(lblWarrantyPrice);

//             // Hiển thị tổng tiền kèm bảo hành
//             Label lblTotalWithWarranty = new Label("Tổng tiền: " + String.format("%,d₫", (long)product.getPrice()));
//             lblTotalWithWarranty.setStyle("-fx-font-weight: bold;");
//             warrantyBox.getChildren().add(lblTotalWithWarranty);

//             // Cập nhật giá bảo hành khi thay đổi loại bảo hành
//             cbWarranty.setOnAction(e -> {
//                 String selectedType = cbWarranty.getValue();

//                 if ("Không".equals(selectedType) || "Thường".equals(selectedType)) {
//                     lblWarrantyPrice.setText("Phí bảo hành: 0đ");
//                     lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,d₫", (long)product.getPrice()));

//                     if ("Thường".equals(selectedType)) {
//                         lblWarrantyInfo.setText("✅ Bảo hành Thường miễn phí 12 tháng");
//                         lblWarrantyInfo.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 12px;");
//                     } else {
//                         lblWarrantyInfo.setText("❌ Không bảo hành");
//                         lblWarrantyInfo.setStyle("-fx-text-fill: #f44336; -fx-font-size: 12px;");
//                     }
//                     return;
//                 }

//                 // Tính phí bảo hành Vàng (10% giá sản phẩm)
//                 double warrantyFee = product.getPrice() * 0.1;
//                 lblWarrantyPrice.setText("Phí bảo hành: " + String.format("%,d₫", (long)warrantyFee));

//                 // Cập nhật tổng tiền
//                 double totalPrice = product.getPrice() + warrantyFee;
//                 lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,d₫", (long)totalPrice));

//                 // Thêm giải thích về bảo hành Vàng
//                 lblWarrantyInfo.setText("✨ Bảo hành Vàng 24 tháng, 1 đổi 1 trong 24 tháng");
//                 lblWarrantyInfo.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 12px; -fx-font-weight: bold;");
//             });

//             // Mô tả sản phẩm và nút thêm vào giỏ (giữ nguyên code)
//             Label lblDescTitle = new Label("Mô tả sản phẩm:");
//             lblDescTitle.setStyle("-fx-font-weight: bold;");

//             String description = (product.getDescription() != null) ? product.getDescription() : "Không có thông tin";
//             TextArea txtDescription = new TextArea(description);
//             txtDescription.setWrapText(true);
//             txtDescription.setEditable(false);
//             txtDescription.setPrefHeight(100);

//             // Nút thêm vào giỏ
//             Button btnAddToCart = new Button("Thêm vào giỏ");
//             btnAddToCart.setPrefWidth(200);
//             btnAddToCart.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnAddToCart.setOnAction(e -> {
//                 try {
//                     // Lấy loại bảo hành đã chọn
//                     String selectedWarranty = cbWarranty.getValue();

//                     // Tạo đối tượng CartItemEmployee mới
//                     CartItemEmployee newItem = new CartItemEmployee(
//                             product.getProductID(),
//                             product.getProductName(),
//                             product.getPrice(),
//                             1,
//                             product.getImagePath(),
//                             employeeId,
//                             currentUser != null ? currentUser : "unknown",
//                             product.getCategoryID()
//                     );

//                     // Tạo bảo hành nếu không phải là "Không" bảo hành
//                     if ("Thường".equals(selectedWarranty) || "Vàng".equals(selectedWarranty)) {
//                         // Tạo bảo hành và gán vào sản phẩm
//                         Warranty warranty = WarrantyCalculator.createWarranty(product, selectedWarranty);
//                         newItem.setWarranty(warranty);
//                     }

//                     // Thêm vào giỏ hàng
//                     addToCartWithWarranty(newItem);

//                     detailStage.close(); // Đóng cửa sổ chi tiết
//                     AlertUtil.showInformation("Thành công", "Đã thêm sản phẩm vào giỏ hàng!");
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi thêm sản phẩm vào giỏ hàng", ex);
//                     AlertUtil.showError("Lỗi", "Không thể thêm sản phẩm vào giỏ hàng: " + ex.getMessage());
//                 }
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setPrefWidth(100);
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnClose.setOnAction(e -> detailStage.close());

//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.getChildren().addAll(btnAddToCart, btnClose);

//             // Thêm tất cả vào layout
//             layout.getChildren().addAll(
//                     imageBox,
//                     lblName,
//                     lblPrice,
//                     new Separator(),
//                     specsBox,
//                     new Separator(),
//                     warrantyBox,
//                     new Separator(),
//                     lblDescTitle,
//                     txtDescription,
//                     buttonBox
//             );

//             Scene scene = new Scene(layout, 400, 800);
//             detailStage.setScene(scene);
//             detailStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị chi tiết sản phẩm", e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị chi tiết sản phẩm: " + e.getMessage());
//         }
//     }

//     // Tạo dòng hiển thị cho sản phẩm trong giỏ hàng
//     private HBox createCartItemRow(CartItemEmployee item) {
//         HBox row = new HBox();
//         row.setSpacing(10);
//         row.setPadding(new Insets(5));
//         row.setAlignment(Pos.CENTER_LEFT);

//         // Tên sản phẩm với thông tin bảo hành
//         VBox productInfoBox = new VBox(2);
//         Label lblName = new Label(item.getProductName());
//         lblName.setStyle("-fx-font-weight: bold;");
//         productInfoBox.getChildren().add(lblName);

//         // Thêm thông tin bảo hành nếu có
//         if (item.hasWarranty()) {
//             Label lblWarranty = new Label("BH: " + item.getWarranty().getWarrantyType());
//             lblWarranty.setStyle("-fx-font-size: 11px; -fx-text-fill: #2196F3;");
//             productInfoBox.getChildren().add(lblWarranty);
//         }

//         productInfoBox.setPrefWidth(200);

//         // Số lượng với nút tăng/giảm
//         HBox quantityBox = new HBox(5);
//         quantityBox.setAlignment(Pos.CENTER);

//         Button btnMinus = new Button("-");
//         btnMinus.setMinWidth(30);
//         btnMinus.setOnAction(e -> decreaseQuantity(item));

//         Label lblQuantity = new Label(String.valueOf(item.getQuantity()));
//         lblQuantity.setAlignment(Pos.CENTER);
//         lblQuantity.setMinWidth(30);
//         lblQuantity.setStyle("-fx-font-weight: bold;");

//         Button btnPlus = new Button("+");
//         btnPlus.setMinWidth(30);
//         btnPlus.setOnAction(e -> increaseQuantity(item));

//         quantityBox.getChildren().addAll(btnMinus, lblQuantity, btnPlus);
//         quantityBox.setPrefWidth(120);

//         // Đơn giá
//         Label lblPrice = new Label(String.format("%,.0f", item.getPrice()) + "đ");
//         lblPrice.setPrefWidth(100);
//         lblPrice.setAlignment(Pos.CENTER_RIGHT);

//         // Bảo hành
//         Label lblWarranty = new Label(item.hasWarranty() ? item.getWarranty().getWarrantyType() : "Không");
//         lblWarranty.setPrefWidth(80);
//         lblWarranty.setAlignment(Pos.CENTER);
//         if (item.hasWarranty()) {
//             lblWarranty.setStyle("-fx-text-fill: #4CAF50;");
//         }

//         // Tổng tiền
//         Label lblTotal = new Label(String.format("%,.0f", item.getTotalPrice()) + "đ");
//         lblTotal.setPrefWidth(100);
//         lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #e91e63;");
//         lblTotal.setAlignment(Pos.CENTER_RIGHT);

//         // Nút xóa
//         Button btnRemove = new Button("✖");
//         btnRemove.setStyle("-fx-background-color: transparent; -fx-text-fill: #f44336; -fx-font-weight: bold;");
//         btnRemove.setOnAction(e -> removeFromCart(item));

//         // Thêm tất cả vào dòng
//         row.getChildren().addAll(productInfoBox, quantityBox, lblPrice, lblWarranty, lblTotal, btnRemove);

//         return row;
//     }

//     // Tăng số lượng sản phẩm trong giỏ hàng
//     private void increaseQuantity(CartItemEmployee item) {
//         if (item == null) return;

//         Product product = findProductById(item.getProductID());
//         if (product == null) {
//             AlertUtil.showWarning("Lỗi", "Không tìm thấy thông tin sản phẩm");
//             return;
//         }

//         // Kiểm tra số lượng tồn kho
//         if (item.getQuantity() < product.getQuantity()) {
//             item.setQuantity(item.getQuantity() + 1);
//             updateCartDisplay();
//         } else {
//             AlertUtil.showWarning("Số lượng tối đa",
//                     "Không thể thêm nữa, số lượng trong kho chỉ còn " + product.getQuantity());
//         }
//     }

//     // Giảm số lượng sản phẩm trong giỏ hàng
//     private void decreaseQuantity(CartItemEmployee item) {
//         if (item == null) return;

//         if (item.getQuantity() > 1) {
//             item.setQuantity(item.getQuantity() - 1);
//             updateCartDisplay();
//         } else {
//             // Nếu số lượng là 1, hỏi xem có muốn xóa không
//             Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//             alert.setTitle("Xóa sản phẩm");
//             alert.setHeaderText("Xác nhận xóa");
//             alert.setContentText("Bạn có muốn xóa sản phẩm này khỏi giỏ hàng?");

//             Optional<ButtonType> result = alert.showAndWait();
//             if (result.isPresent() && result.get() == ButtonType.OK) {
//                 removeFromCart(item);
//             }
//         }
//     }

//     // Xóa sản phẩm khỏi giỏ hàng
//     private void removeFromCart(CartItemEmployee item) {
//         if (item != null) {
//             cartItems.remove(item);
//             updateCartDisplay();
//         }
//     }

//     // Cập nhật hiển thị giỏ hàng
//     private void updateCartDisplay() {
//         // Cập nhật tổng tiền
//         updateTotal();

//         // Cập nhật TableView
//         cartTable.refresh();
//     }

//     // Cập nhật tổng tiền giỏ hàng
//     private void updateTotal() {
//         double total = calculateTotalAmount();
//         if (lblTotal != null) {
//             lblTotal.setText("Tổng tiền: " + String.format("%,.0f", total) + "đ");
//         }
//     }

//     // Tính tổng tiền giỏ hàng
//     private double calculateTotalAmount() {
//         double total = 0.0;
//         for (CartItemEmployee item : cartItems) {
//             if (item != null) {
//                 total += item.getTotalPrice();
//             }
//         }
//         return total;
//     }

//     // Xóa toàn bộ giỏ hàng
//     private void clearCart() {
//         cartItems.clear();
//         updateCartDisplay();
//         LOGGER.info("Đã xóa toàn bộ giỏ hàng");
//     }

//     // Lấy tên danh mục từ ID
//     private String getCategoryName(String categoryId) {
//         if (categoryId == null) return "Không xác định";

//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.warning("Không thể kết nối đến database");
//                 return "Không xác định";
//             }

//             // FIX LỖI: Sửa tên bảng từ Category thành Categories và category_name thành categoryName
//             String query = "SELECT categoryName FROM Categories WHERE categoryID = ?";
//             stmt = conn.prepareStatement(query);
//             stmt.setString(1, categoryId);
//             rs = stmt.executeQuery();

//             if (rs.next()) {
//                 return rs.getString("categoryName");
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.WARNING, "Lỗi SQL khi lấy tên danh mục: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.WARNING, "Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.WARNING, "Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }

//         return "Không xác định";
//     }
//     // Lấy danh sách các danh mục phân biệt
//     private List<String> getDistinctCategories() {
//         List<String> categories = new ArrayList<>();
//         categories.add("Tất cả"); // Luôn có tùy chọn "Tất cả"

//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.severe("💀 QUẠC!!! Không thể kết nối đến database");
//                 return categories;
//             }

//             // FIX LỖI: Sửa tên bảng từ Category thành Categories
//             // Sửa tên cột từ category_name thành categoryName - match với schema thực tế
//             String query = "SELECT DISTINCT categoryID, categoryName FROM Categories ORDER BY categoryName";
//             stmt = conn.prepareStatement(query);
//             rs = stmt.executeQuery();

//             int categoryCount = 0;

//             while (rs.next()) {
//                 String categoryName = rs.getString("categoryName");
//                 if (categoryName != null && !categoryName.isEmpty()) {
//                     categories.add(categoryName);
//                     categoryCount++;
//                 }
//             }

//             LOGGER.info("✨✨✨ Đã tìm thấy " + categoryCount + " danh mục từ database slayyy");

//             if (categoryCount == 0) {
//                 LOGGER.warning("🚨🚨 SKSKSK EM hong tìm thấy danh mục nào trong database luôn á!!!");
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi SQL khi lấy danh mục: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "😭😭 Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }

//         return categories;
//     }

//     // Tải dữ liệu sản phẩm từ database
//     // Em sẽ sửa lại hàm loadProductsFromDatabase để FIX LỖI NGAY LAPPPPP
//     private void loadProductsFromDatabase() {
//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.severe("❌❌❌ Không thể kết nối đến database");
//                 return;
//             }

//             // FIX LỖI: Sửa lại câu query SQL - CHÚ Ý KHÔNG DÙNG WHERE NỮA
//             // Trước đây chỉ lấy sản phẩm có status = "Còn hàng" hoặc "Active"
//             // => Sửa lại để lấy TẤT CẢ sản phẩm, sort theo quantity để hiển thị sản phẩm còn hàng lên trên
//             String query = "SELECT * FROM Products ORDER BY quantity DESC";
//             stmt = conn.prepareStatement(query);
//             rs = stmt.executeQuery();

//             products.clear(); // Xóa danh sách cũ

//             int productCount = 0; // Đếm số sản phẩm load được

//             while (rs.next()) {
//                 Product product = new Product();
//                 product.setProductID(rs.getString("productID"));
//                 product.setProductName(rs.getString("productName"));
//                 product.setPrice(rs.getDouble("price"));
//                 product.setQuantity(rs.getInt("quantity"));
//                 product.setDescription(rs.getString("description"));
//                 product.setStatus(rs.getString("status"));
//                 product.setCategoryID(rs.getString("categoryID"));

//                 // Xử lý đường dẫn hình ảnh
//                 String imagePath = rs.getString("imagePath");
//                 if (imagePath != null && !imagePath.startsWith("/")) {
//                     imagePath = "/com/example/stores/images/" + imagePath;
//                 }
//                 product.setImagePath(imagePath);

//                 products.add(product);
//                 productCount++;
//             }

//             LOGGER.info("✅✅✅ Đã load được " + productCount + " sản phẩm từ database");

//             if (productCount == 0) {
//                 // Debug thêm thông tin nếu không load được sản phẩm nào
//                 LOGGER.warning("⚠️⚠️⚠️ Không tìm thấy sản phẩm nào trong database!!!");
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi SQL khi lấy dữ liệu sản phẩm: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }
//     }

//     // Làm mới danh sách sản phẩm trên giao diện
//     private void refreshProductList() {
//         if (productFlowPane == null) {
//             LOGGER.warning("productFlowPane chưa được khởi tạo");
//             return;
//         }

//         // Xóa tất cả sản phẩm hiện tại
//         productFlowPane.getChildren().clear();

//         if (products.isEmpty()) {
//             Label lblEmpty = new Label("Không có sản phẩm nào.");
//             lblEmpty.setStyle("-fx-font-style: italic;");
//             productFlowPane.getChildren().add(lblEmpty);
//             return;
//         }

//         // Lọc sản phẩm theo điều kiện
//         List<Product> filteredProducts = filterProducts();

//         // Sắp xếp sản phẩm theo điều kiện
//         sortProducts(filteredProducts);

//         // Lưu danh sách hiện tại để sử dụng sau này
//         currentFilteredProducts = new ArrayList<>(filteredProducts);

//         // Giới hạn số lượng sản phẩm hiển thị
//         List<Product> displayProducts = filteredProducts.stream()
//                 .limit(productLimit)
//                 .collect(Collectors.toList());

//         // Hiển thị sản phẩm
//         for (Product product : displayProducts) {
//             VBox productBox = createProductBox(product);
//             productFlowPane.getChildren().add(productBox);
//         }

//         // Thêm nút "Xem thêm" nếu còn sản phẩm
//         if (filteredProducts.size() > productLimit) {
//             Button btnLoadMore = createLoadMoreButton();
//             productFlowPane.getChildren().add(btnLoadMore);
//         }
//     }

//     // Lọc sản phẩm theo các điều kiện
//     private List<Product> filterProducts() {
//         List<Product> filteredList = new ArrayList<>(products);

//         // Lọc theo danh mục
//         if (cbCategory != null && cbCategory.getValue() != null && !cbCategory.getValue().equals("Tất cả")) {
//             String selectedCategory = cbCategory.getValue();
//             filteredList = filteredList.stream()
//                     .filter(p -> {
//                         String categoryName = getCategoryName(p.getCategoryID());
//                         return categoryName.equals(selectedCategory);
//                     })
//                     .collect(Collectors.toList());
//         }

//         // Lọc theo từ khóa tìm kiếm
//         if (txtSearch != null && txtSearch.getText() != null && !txtSearch.getText().trim().isEmpty()) {
//             String keyword = txtSearch.getText().trim().toLowerCase();
//             filteredList = filteredList.stream()
//                     .filter(p -> p.getProductName() != null && p.getProductName().toLowerCase().contains(keyword))
//                     .collect(Collectors.toList());
//         }

//         return filteredList;
//     }

//     // Sắp xếp sản phẩm theo điều kiện đã chọn
//     private void sortProducts(List<Product> list) {
//         if (cbSort == null || cbSort.getValue() == null) return;

//         String sortOption = cbSort.getValue();
//         switch (sortOption) {
//             case "Tên A-Z":
//                 // FIX LỖI: Thêm kiểu Product vào lambda để compiler biết đây là Product object
//                 list.sort(Comparator.comparing((Product p) -> p.getProductName() != null ? p.getProductName() : ""));
//                 break;
//             case "Tên Z-A":
//                 // FIX LỖI: Thêm kiểu Product vào lambda tương tự
//                 list.sort(Comparator.comparing((Product p) -> p.getProductName() != null ? p.getProductName() : "").reversed());
//                 break;
//             case "Giá thấp đến cao":
//                 list.sort(Comparator.comparing(Product::getPrice));
//                 break;
//             case "Giá cao đến thấp":
//                 list.sort(Comparator.comparing(Product::getPrice).reversed());
//                 break;
//             // Mặc định không sắp xếp (giữ nguyên thứ tự)
//         }
//     }

//     // Tạo box hiển thị sản phẩm
//     private VBox createProductBox(Product product) {
//         VBox box = new VBox(8); // Khoảng cách giữa các thành phần
//         box.setPrefWidth(160);
//         box.setPrefHeight(260);
//         box.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: white; -fx-padding: 10;");

//         // Tạo hiệu ứng hover
//         box.setOnMouseEntered(e -> {
//             box.setStyle("-fx-border-color: #2196F3; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f5f5f5; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.4), 10, 0, 0, 0);");
//             box.setCursor(Cursor.HAND);
//         });

//         box.setOnMouseExited(e -> {
//             box.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: white; -fx-padding: 10;");
//         });

//         // Xử lý sự kiện click để xem chi tiết sản phẩm
//         box.setOnMouseClicked(e -> showProductDetails(product));

//         // Hiển thị hình ảnh sản phẩm
//         ImageView imageView = new ImageView();
//         imageView.setFitWidth(140);
//         imageView.setFitHeight(105);
//         imageView.setPreserveRatio(true);

//         String imagePath = product.getImagePath();
//         if (imagePath == null) {
//             imagePath = "/com/example/stores/images/no_image.png";
//         }

//         try {
//             Image image = new Image(getClass().getResourceAsStream(imagePath));
//             imageView.setImage(image);
//         } catch (Exception e) {
//             try {
//                 Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/stores/images/no_image.png"));
//                 imageView.setImage(defaultImage);
//             } catch (Exception ex) {
//                 LOGGER.warning("Không tải được ảnh sản phẩm: " + ex.getMessage());
//             }
//         }

//         // Hiển thị tên sản phẩm
//         String productName = product.getProductName();
//         if (productName == null) productName = "Sản phẩm không tên";
//         if (productName.length() > 40) {
//             productName = productName.substring(0, 37) + "...";
//         }

//         Label nameLabel = new Label(productName);
//         nameLabel.setWrapText(true);
//         nameLabel.setPrefHeight(40); // Chiều cao cố định cho tên sản phẩm
//         nameLabel.setStyle("-fx-font-weight: bold;");

//         // Hiển thị giá
//         Label priceLabel = new Label("Giá: " + String.format("%,d", (long) product.getPrice()) + "đ");
//         priceLabel.setStyle("-fx-text-fill: #e91e63; -fx-font-weight: bold;");

//         // Hiển thị số lượng
//         Label stockLabel = new Label("Kho: " + product.getQuantity());

//         // Nút thêm vào giỏ
//         Button addButton = new Button("Thêm vào giỏ");
//         addButton.setPrefWidth(Double.MAX_VALUE);
//         addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

//         // Hiệu ứng hover cho nút
//         addButton.setOnMouseEntered(e ->
//                 addButton.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white; -fx-font-weight: bold;")
//         );

//         addButton.setOnMouseExited(e ->
//                 addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;")
//         );

//         // Sự kiện thêm vào giỏ
//         addButton.setOnAction(e -> {
//             try {
//                 // Kiểm tra số lượng tồn kho
//                 if (product.getQuantity() <= 0) {
//                     AlertUtil.showWarning("Hết hàng", "Sản phẩm đã hết hàng!");
//                     return;
//                 }

//                 // Tạo đối tượng CartItemEmployee
//                 CartItemEmployee item = new CartItemEmployee(
//                         product.getProductID(),
//                         product.getProductName(),
//                         product.getPrice(),
//                         1,
//                         product.getImagePath(),
//                         employeeId,
//                         currentUser != null ? currentUser : "unknown",
//                         product.getCategoryID()
//                 );

//                 // Kiểm tra sản phẩm có đủ điều kiện bảo hành thường không
//                 // Nếu có, thêm bảo hành thường mặc định
//                 if (WarrantyCalculator.isEligibleForStandardWarranty(product)) {
//                     Warranty warranty = WarrantyCalculator.createWarranty(product, "Thường");
//                     item.setWarranty(warranty);
//                 }

//                 // Thêm vào giỏ hàng
//                 addToCartWithWarranty(item);

//             } catch (Exception ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi khi thêm sản phẩm vào giỏ hàng", ex);
//                 AlertUtil.showError("Lỗi", "Không thể thêm sản phẩm vào giỏ hàng");
//             }
//         });

//         // Thêm tất cả vào box
//         VBox imageContainer = new VBox(imageView);
//         imageContainer.setAlignment(Pos.CENTER);

//         box.getChildren().addAll(
//                 imageContainer,
//                 nameLabel,
//                 priceLabel,
//                 stockLabel,
//                 addButton
//         );

//         return box;
//     }
// }package com.example.stores.controller;

// import com.example.stores.model.OrderHistory;
// import com.example.stores.model.OrderDetail;
// import com.example.stores.service.OrderHistoryServiceE;
// import javafx.scene.control.cell.PropertyValueFactory;
// import javafx.scene.control.CheckBox;

// import java.io.IOException;
// import java.io.InputStream;
// import java.net.URL;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.sql.Statement;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.List;
// import java.util.Optional;
// import java.util.logging.Level;
// import java.util.logging.Logger;
// import java.util.stream.Collectors;

// import com.example.stores.util.AlertUtil; // Chú ý: đây là AlertUtil (không có s)
// import com.example.stores.util.WarrantyCalculator;

// import com.example.stores.model.Customer;
// import com.example.stores.service.CustomerServiceE;

// import javafx.scene.control.RadioButton;
// import javafx.scene.control.ToggleGroup;
// import javafx.scene.layout.BorderPane;
// import javafx.scene.layout.GridPane;
// import javafx.scene.control.ScrollPane;
// import javafx.beans.property.SimpleStringProperty;
// import javafx.beans.property.SimpleIntegerProperty;
// import javafx.beans.property.SimpleDoubleProperty;

// import javafx.scene.layout.*;
// import javafx.geometry.Pos;
// import javafx.scene.control.Label;
// import javafx.scene.control.Button;
// import javafx.scene.image.Image;
// import javafx.scene.image.ImageView;
// import javafx.geometry.Insets;

// import javafx.collections.ObservableList;
// import com.example.stores.config.DBConfig;
// import com.example.stores.model.CartItemEmployee;
// import com.example.stores.model.Product;
// import com.example.stores.model.Employee;
// import com.example.stores.model.Warranty; // Thêm import cho Warranty

// import com.example.stores.model.Order;

// import javafx.scene.shape.Circle;
// import javafx.scene.Cursor;
// import javafx.collections.FXCollections;
// import javafx.fxml.FXML;
// import javafx.fxml.FXMLLoader;
// import javafx.print.PrinterJob;
// import javafx.scene.Parent;
// import javafx.scene.Scene;
// import javafx.scene.control.Alert;
// import javafx.scene.control.ButtonType;
// import javafx.scene.control.ComboBox;
// import javafx.scene.control.Separator;
// import javafx.scene.control.TableCell;
// import javafx.scene.control.TableColumn;
// import javafx.scene.control.TableView;
// import javafx.scene.control.TextArea;
// import javafx.scene.control.TextField;
// import javafx.scene.layout.VBox;
// import javafx.stage.Modality;
// import javafx.stage.Stage;

// public class PosOverviewControllerE {
//     private static final Logger LOGGER = Logger.getLogger(PosOverviewControllerE.class.getName());

//     @FXML private FlowPane productFlowPane;
//     @FXML private TableView<CartItemEmployee> cartTable;
//     @FXML private TableColumn<CartItemEmployee, String> colCartName;
//     @FXML private TableColumn<CartItemEmployee, Integer> colCartQty;
//     @FXML private TableColumn<CartItemEmployee, Double> colCartPrice;
//     @FXML private TableColumn<CartItemEmployee, Double> colCartTotal;
//     @FXML private TableColumn<CartItemEmployee, String> colCartWarranty; // Thêm khai báo biến cho cột bảo hành
//     @FXML private Label lblTotal;
//     // Cập nhật ComboBox lọc theo DB mới (bỏ RAM/CPU, giữ lại category)
//     @FXML private ComboBox<String> cbCategory;
//     @FXML private ComboBox<String> cbSort; // Thêm ComboBox sắp xếp
//     @FXML private TextField txtSearch;
//     @FXML private Button btnFilter, btnCheckout;
//     @FXML private VBox cartItemsContainer; // Container cho các item trong giỏ hàng

//     private int productLimit = 20; // Số sản phẩm hiển thị ban đầu
//     private List<Product> currentFilteredProducts = new ArrayList<>();

//     private ObservableList<Product> products = FXCollections.observableArrayList();
//     private ObservableList<CartItemEmployee> cartItems = FXCollections.observableArrayList();
//     private TableColumn<CartItemEmployee, Void> colCartAction; // Cột chứa nút xóa

//     private int employeeId;

//     /**
//      * Thêm sản phẩm vào giỏ hàng - Method công khai cho ProductDetailController gọi
//      * @param item Sản phẩm cần thêm vào giỏ
//      */
//     public void addToCart(CartItemEmployee item) {
//         // Gọi đến phương thức addToCartWithWarranty đã có sẵn
//         addToCartWithWarranty(item);
//         LOGGER.info("✅ Đã thêm sản phẩm " + item.getProductName() + " vào giỏ hàng từ ProductDetailController");
//     }

//     /**
//      * Lấy tên người dùng hiện tại
//      * @return tên đăng nhập người dùng hiện tại
//      */
//     public String getCurrentUser() {
//         return this.currentUser;
//     }

//     // Thêm biến để lưu lịch sử đơn hàng trong session
//     private List<OrderSummary> orderHistory = new ArrayList<>();

//     // Thêm vào class PosOverviewController
//     private void addEmployeeInfoButton() {
//         try {
//             if (currentEmployee == null || btnCheckout == null || btnCheckout.getParent() == null ||
//                     !(btnCheckout.getParent().getParent() instanceof BorderPane)) {
//                 LOGGER.warning("Không thể thêm nút thông tin nhân viên: currentEmployee hoặc btnCheckout null");
//                 return;
//             }

//             BorderPane mainLayout = (BorderPane) btnCheckout.getParent().getParent();
//             if (mainLayout.getTop() instanceof HBox) {
//                 HBox topBar = (HBox) mainLayout.getTop();

//                 Button btnEmployeeInfo = new Button("THÔNG TIN NV");
//                 btnEmployeeInfo.setStyle("-fx-background-color: #FF4081; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12;");

//                 btnEmployeeInfo.setOnMouseEntered(e -> btnEmployeeInfo.setStyle("-fx-background-color: #F50057; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 0);"));
//                 btnEmployeeInfo.setOnMouseExited(e -> btnEmployeeInfo.setStyle("-fx-background-color: #FF4081; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12;"));

//                 btnEmployeeInfo.setOnAction(e -> showEmployeeInfoDialog());

//                 HBox.setMargin(btnEmployeeInfo, new Insets(0, 10, 0, 10));
//                 int infoLabelIndex = topBar.getChildren().size() - 1;
//                 if (infoLabelIndex >= 0) {
//                     topBar.getChildren().add(infoLabelIndex, btnEmployeeInfo);
//                 } else {
//                     topBar.getChildren().add(btnEmployeeInfo);
//                 }

//                 LOGGER.info("✨ Đã thêm nút thông tin nhân viên!");
//             }
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi thêm nút thông tin nhân viên", e);
//         }
//     }

//     // Hàm hiển thị dialog thông tin nhân viên SIÊU XỊNNN
//     @FXML
//     private void showEmployeeInfoDialog() {
//         try {
//             if (currentEmployee == null) {
//                 AlertUtil.showWarning("Thông báo", "Không thể lấy thông tin nhân viên!");
//                 return;
//             }

//             // Tạo stage mới cho dialog
//             Stage infoStage = new Stage();
//             infoStage.initModality(Modality.APPLICATION_MODAL);
//             infoStage.setTitle("Thông Tin Nhân Viên");
//             infoStage.setResizable(false);

//             // Tạo layout chính
//             BorderPane layout = new BorderPane();

//             // Phần header đẹp ngời
//             HBox header = new HBox();
//             header.setAlignment(Pos.CENTER);
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: linear-gradient(to right, #FF4081, #F50057);");

//             Label headerTitle = new Label("THÔNG TIN NHÂN VIÊN");
//             headerTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
//             header.getChildren().add(headerTitle);

//             // Thêm header vào layout
//             layout.setTop(header);

//             // Phần nội dung
//             GridPane content = new GridPane();
//             content.setPadding(new Insets(20));
//             content.setVgap(15);
//             content.setHgap(10);
//             content.setAlignment(Pos.CENTER);

//             // Tạo ImageView cho ảnh đại diện (avatar)
//             ImageView avatarView = new ImageView();

//             // Tải ảnh từ resource đường dẫn đúng
//             try {
//                 // Lấy theo nhân viên đang đăng nhập
//                 String avatarPath = "/com/example/stores/images/employee/img.png"; // mặc định

//                 // Nếu là nv001, dùng ảnh an.png
//                 if (currentEmployee.getUsername() != null && currentEmployee.getUsername().equals("nv001")) {
//                     avatarPath = "/com/example/stores/images/employee/an.png";
//                 }

//                 // Hoặc nếu có imageUrl trong database
//                 if (currentEmployee.getImageUrl() != null && !currentEmployee.getImageUrl().isEmpty()) {
//                     String imageUrl = currentEmployee.getImageUrl();
//                     // Bỏ "resources/" ở đầu nếu có
//                     String resourcePath = imageUrl.startsWith("resources/") ? imageUrl.substring(10) : imageUrl;
//                     // Thay "com.example.stores/" thành "com/example/stores/"
//                     if (resourcePath.startsWith("com.example.stores/")) {
//                         resourcePath = resourcePath.replace("com.example.stores/", "com/example/stores/");
//                     }
//                     // Thêm dấu "/" ở đầu
//                     avatarPath = "/" + resourcePath;
//                 }

//                 // Load ảnh
//                 Image avatarImage = new Image(getClass().getResourceAsStream(avatarPath));
//                 avatarView.setImage(avatarImage);
//             } catch (Exception e) {
//                 // Nếu không có ảnh, hiển thị icon người dùng mặc định
//                 try {
//                     // Đường dẫn default chuẩn
//                     Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/stores/images/employee/img.png"));
//                     avatarView.setImage(defaultImage);
//                 } catch (Exception ex) {
//                     LOGGER.warning("Không thể tải ảnh mặc định cho nhân viên: " + ex.getMessage());
//                 }
//             }

//             // Thiết lập kích thước avatar
//             avatarView.setFitWidth(120);
//             avatarView.setFitHeight(120);
//             avatarView.setPreserveRatio(true);

//             // Bo tròn avatar bằng clip hình tròn
//             Circle clip = new Circle(60, 60, 60); // tâm (60,60), bán kính 60px
//             avatarView.setClip(clip);

//             // Tạo StackPane cho avatar, có viền và padding
//             StackPane avatarContainer = new StackPane(avatarView);
//             avatarContainer.setPadding(new Insets(3));
//             avatarContainer.setStyle("-fx-background-color: white; -fx-border-color: #FF4081; " +
//                     "-fx-border-width: 3; -fx-border-radius: 60; -fx-background-radius: 60;");
//             GridPane.setColumnSpan(avatarContainer, 2);
//             GridPane.setHalignment(avatarContainer, javafx.geometry.HPos.CENTER);

//             // Thêm avatar vào đầu tiên
//             content.add(avatarContainer, 0, 0, 2, 1);

//             // Thêm các thông tin nhân viên
//             addEmployeeInfoField(content, "Mã nhân viên:", currentEmployee.getEmployeeID(), 1);
//             addEmployeeInfoField(content, "Tên đăng nhập:", currentEmployee.getUsername(), 2);
//             addEmployeeInfoField(content, "Họ tên:", currentEmployee.getFullName(), 3);

//             // Thêm thông tin position nếu có
//             String position = "Nhân viên";
//             try {
//                 position = currentEmployee.getPosition();
//                 if (position == null || position.isEmpty()) position = "Nhân viên";
//             } catch (Exception e) {
//                 // Nếu không có thuộc tính position, dùng giá trị mặc định
//                 LOGGER.info("Không có thông tin chức vụ");
//             }
//             addEmployeeInfoField(content, "Chức vụ:", position, 4);

//             addEmployeeInfoField(content, "Email:", currentEmployee.getEmail(), 5);
//             addEmployeeInfoField(content, "Điện thoại:", currentEmployee.getPhone(), 6);
//             addEmployeeInfoField(content, "Thời gian đăng nhập:", currentDateTime, 7);

//             // Button đóng dialog
//             HBox buttonBar = new HBox();
//             buttonBar.setAlignment(Pos.CENTER);
//             buttonBar.setPadding(new Insets(0, 0, 20, 0));

//             Button closeButton = new Button("ĐÓNG");
//             closeButton.setPrefWidth(120);
//             closeButton.setPrefHeight(35);
//             closeButton.setStyle("-fx-background-color: #F50057; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");

//             closeButton.setOnMouseEntered(e ->
//                     closeButton.setStyle("-fx-background-color: #C51162; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;")
//             );

//             closeButton.setOnMouseExited(e ->
//                     closeButton.setStyle("-fx-background-color: #F50057; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;")
//             );

//             closeButton.setOnAction(e -> infoStage.close());

//             buttonBar.getChildren().add(closeButton);

//             // Thêm nội dung và button vào layout
//             VBox mainContainer = new VBox(15);
//             mainContainer.getChildren().addAll(content, buttonBar);
//             layout.setCenter(mainContainer);

//             // Tạo scene và hiển thị
//             Scene scene = new Scene(layout, 400, 520);
//             infoStage.setScene(scene);
//             infoStage.show();

//             LOGGER.info("✨ Đã hiển thị thông tin nhân viên: " + currentEmployee.getFullName());
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị thông tin nhân viên: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị thông tin nhân viên: " + e.getMessage());
//         }
//     }

//     // Hàm hỗ trợ thêm trường thông tin
//     private void addEmployeeInfoField(GridPane grid, String labelText, String value, int row) {
//         // Label tiêu đề
//         Label label = new Label(labelText);
//         label.setStyle("-fx-font-weight: bold; -fx-text-fill: #757575;");
//         grid.add(label, 0, row);

//         // Giá trị
//         Label valueLabel = new Label(value != null ? value : "N/A");
//         valueLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #212121;");
//         grid.add(valueLabel, 1, row);
//     }

//     // Biến để đếm số đơn hàng
//     private int orderCounter = 1;

//     private Button createLoadMoreButton() {
//         Button btnLoadMore = new Button("XEM THÊM SẢN PHẨM");
//         btnLoadMore.setPrefWidth(200);
//         btnLoadMore.setPrefHeight(40);
//         btnLoadMore.setStyle(
//                 "-fx-background-color: linear-gradient(to right, #2196F3, #03A9F4); " +
//                         "-fx-text-fill: white; " +
//                         "-fx-font-weight: bold; " +
//                         "-fx-font-size: 14px; " +
//                         "-fx-background-radius: 5; " +
//                         "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.4), 6, 0, 0, 2);"
//         );

//         // Hiệu ứng khi hover
//         btnLoadMore.setOnMouseEntered(e ->
//                 btnLoadMore.setStyle(
//                         "-fx-background-color: linear-gradient(to right, #1976D2, #2196F3); " +
//                                 "-fx-text-fill: white; " +
//                                 "-fx-font-weight: bold; " +
//                                 "-fx-font-size: 14px; " +
//                                 "-fx-background-radius: 5; " +
//                                 "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.6), 8, 0, 0, 3);"
//                 )
//         );

//         // Trở về style ban đầu khi hết hover
//         btnLoadMore.setOnMouseExited(e ->
//                 btnLoadMore.setStyle(
//                         "-fx-background-color: linear-gradient(to right, #2196F3, #03A9F4); " +
//                                 "-fx-text-fill: white; " +
//                                 "-fx-font-weight: bold; " +
//                                 "-fx-font-size: 14px; " +
//                                 "-fx-background-radius: 5; " +
//                                 "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.4), 6, 0, 0, 2);"
//                 )
//         );

//         // Sự kiện khi click
//         btnLoadMore.setOnAction(e -> {
//             productLimit += 20; // Tăng thêm 20 sản phẩm
//             refreshProductList(); // Làm mới danh sách
//             LOGGER.info("Đã tăng giới hạn hiển thị lên " + productLimit + " sản phẩm");
//         });

//         return btnLoadMore;
//     }

//     /**
//      * Lưu đơn hàng vào lịch sử
//      */
//     public void addToOrderHistory(int orderId, String customerName, String customerPhone,
//                                   String paymentMethod, String orderDateTime, double totalAmount,
//                                   List<CartItemEmployee> items) {
//         Connection conn = null;
//         PreparedStatement pstmtOrder = null;
//         PreparedStatement pstmtDetail = null;

//         try {
//             if (items == null || items.isEmpty()) {
//                 LOGGER.warning("Danh sách sản phẩm rỗng, không thể lưu lịch sử vào DB");
//                 return;
//             }

//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.severe("Không thể kết nối database để lưu order history");
//                 return;
//             }
//             conn.setAutoCommit(false); // Bắt đầu transaction

//             // 1. Insert vào bảng Orders
//             String insertOrder = "INSERT INTO Orders (orderID, orderDate, totalAmount, customerID, employeeID, orderStatus, paymentMethod, recipientName, recipientPhone) "
//                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//             pstmtOrder = conn.prepareStatement(insertOrder);

//             String orderIdStr = String.format("ORD%03d", orderId); // Format lại cho khớp orderID

//             int customerId = getWalkInCustomerId(); // Hoặc lấy đúng customerID nếu phân biệt khách

//             pstmtOrder.setString(1, orderIdStr);
//             pstmtOrder.setString(2, orderDateTime);
//             pstmtOrder.setDouble(3, totalAmount);
//             pstmtOrder.setInt(4, customerId);
//             pstmtOrder.setInt(5, employeeId);
//             pstmtOrder.setString(6, "Đã xác nhận");
//             pstmtOrder.setString(7, paymentMethod);
//             pstmtOrder.setString(8, customerName);
//             pstmtOrder.setString(9, customerPhone);

//             int resultOrder = pstmtOrder.executeUpdate();
//             if (resultOrder == 0) throw new SQLException("Insert Orders thất bại!");

//             // 2. Insert từng sản phẩm vào OrderDetails
//             String insertDetail = "INSERT INTO OrderDetails (orderID, productID, quantity, unitPrice, warrantyType, warrantyPrice) "
//                     + "VALUES (?, ?, ?, ?, ?, ?)";
//             pstmtDetail = conn.prepareStatement(insertDetail);

//             for (CartItemEmployee item : items) {
//                 pstmtDetail.setString(1, orderIdStr);
//                 pstmtDetail.setString(2, item.getProductID());
//                 pstmtDetail.setInt(3, item.getQuantity());
//                 pstmtDetail.setDouble(4, item.getPrice());

//                 // Bảo hành
//                 if (item.hasWarranty()) {
//                     pstmtDetail.setString(5, item.getWarranty().getWarrantyType());
//                     pstmtDetail.setDouble(6, item.getWarranty().getWarrantyPrice());
//                 } else {
//                     pstmtDetail.setString(5, "Thường");
//                     pstmtDetail.setDouble(6, 0.0);
//                 }
//                 pstmtDetail.addBatch();
//             }
//             int[] detailResults = pstmtDetail.executeBatch();

//             conn.commit();
//             LOGGER.info("✅ Đã lưu đơn hàng #" + orderIdStr + " vào database với " + detailResults.length + " sản phẩm");

//         } catch (Exception e) {
//             try {
//                 if (conn != null) conn.rollback();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi rollback khi lưu đơn hàng!", ex);
//             }
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi lưu đơn hàng vào DB: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (pstmtOrder != null) pstmtOrder.close();
//                 if (pstmtDetail != null) pstmtDetail.close();
//                 if (conn != null) conn.setAutoCommit(true);
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.WARNING, "Lỗi khi đóng kết nối DB", ex);
//             }
//         }
//     }
//     /**
//      * Lấy ID của khách hàng "Khách lẻ" (walkin)
//      */
//     private int getWalkInCustomerId() {
//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;
//         int customerId = 1; // Mặc định ID=1 cho khách lẻ

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.warning("Không thể kết nối đến database");
//                 return customerId;
//             }

//             String sql = "SELECT customerID FROM Customer WHERE username = 'walkin'";
//             stmt = conn.prepareStatement(sql);
//             rs = stmt.executeQuery();

//             if (rs.next()) {
//                 customerId = rs.getInt("customerID");
//                 return customerId;
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.WARNING, "Lỗi SQL khi lấy ID khách hàng mặc định: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.WARNING, "Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.WARNING, "Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }

//         return customerId;
//     }
//     // Thông tin user và thời gian
//     private String currentUser = "doanpk";
//     private String currentDateTime = "2025-06-22 10:30:23"; // Cập nhật thời gian hiện tại từ input
//     private Employee currentEmployee; // Biến lưu thông tin nhân viên

//     // Class để lưu thông tin đơn hàng tạm thời
//     private static class OrderSummary {
//         private int id;
//         private String customerName;
//         private String customerPhone;
//         private String paymentMethod;
//         private String orderDate;
//         private double totalAmount;
//         private List<CartItemEmployee> items;

//         public OrderSummary(int id, String customerName, String customerPhone, String paymentMethod,
//                             String orderDate, double totalAmount, List<CartItemEmployee> items) {
//             this.id = id;
//             this.customerName = customerName;
//             this.customerPhone = customerPhone;
//             this.paymentMethod = paymentMethod;
//             this.orderDate = orderDate;
//             this.totalAmount = totalAmount;
//             this.items = new ArrayList<>(items);
//         }

//         // Getters
//         public int getId() { return id; }
//         public String getCustomerName() { return customerName; }
//         public String getCustomerPhone() { return customerPhone; }
//         public String getPaymentMethod() { return paymentMethod; }
//         public String getOrderDate() { return orderDate; }
//         public double getTotalAmount() { return totalAmount; }
//         public List<CartItemEmployee> getItems() { return items; }
//     }

//     @FXML
//     private void initialize() {
//         try {
//             LOGGER.info("Đang khởi tạo POS Overview Controller...");
//             LOGGER.info("Người dùng hiện tại: " + currentUser);
//             LOGGER.info("Thời gian hiện tại: " + currentDateTime);

//             // Set style trực tiếp để đảm bảo nút có màu
//             if (btnCheckout != null) {
//                 btnCheckout.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             }

//             productFlowPane.setPrefWidth(900);
//             productFlowPane.setPrefWrapLength(900);  // DÒNG QUAN TRỌNG NHẤT!!!
//             productFlowPane.setHgap(15);
//             productFlowPane.setVgap(20);

//             // Lấy dữ liệu sản phẩm từ database
//             loadProductsFromDatabase();
//             LOGGER.info("Đã load " + products.size() + " sản phẩm từ database");

//             // Cấu hình TableView giỏ hàng
//             setupCartTable();

//             // Thêm nút xóa vào bảng giỏ hàng
//             addButtonsToTable();

//             cartTable.setItems(cartItems);

//             // Khởi tạo ComboBox filter danh mục
//             List<String> categoryList = getDistinctCategories();
//             if (cbCategory != null) cbCategory.setItems(FXCollections.observableArrayList(categoryList));

//             // Đảm bảo luôn chọn giá trị đầu tiên nếu có
//             if (cbCategory != null && !cbCategory.getItems().isEmpty()) cbCategory.getSelectionModel().select(0);

//             // Khởi tạo ComboBox sắp xếp
//             if (cbSort != null) {
//                 cbSort.getItems().addAll(
//                         "Mặc định",
//                         "Tên A-Z",
//                         "Tên Z-A",
//                         "Giá thấp đến cao",
//                         "Giá cao đến thấp"
//                 );
//                 cbSort.getSelectionModel().select(0);

//                 // Thêm listener cho cbSort
//                 cbSort.setOnAction(e -> refreshProductList());
//             }

//             // Sự kiện lọc, tìm kiếm
//             if (btnFilter != null) {
//                 btnFilter.setOnAction(e -> refreshProductList());
//             }

//             if (txtSearch != null) {
//                 txtSearch.textProperty().addListener((obs, oldVal, newVal) -> refreshProductList());
//             }

//             if (cbCategory != null) {
//                 cbCategory.setOnAction(e -> refreshProductList());
//             }

//             // Thanh toán - gọi handleCheckout để lưu dữ liệu vào DB
//             if (btnCheckout != null) {
//                 btnCheckout.setOnAction(e -> handleCheckout());
//             }

//             // Thêm nút lịch sử
//             addHistoryButton();

//             // Thêm nút đăng xuất
//             addLogoutButton();

//             // THÊM NÚT XEM THÔNG TIN NHÂN VIÊN
//             addEmployeeInfoButton();

//             // Render sản phẩm ban đầu
//             refreshProductList();
//             LOGGER.info("Khởi tạo POS Overview Controller thành công");
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi khởi tạo POS Overview Controller", e);
//         }
//     }

//     /**
//      * Xử lý thanh toán và lưu đơn hàng vào DB
//      */
//     @FXML
//     private void handleCheckout() {
//         try {
//             if (cartItems.isEmpty()) {
//                 AlertUtil.showWarning("Giỏ hàng trống", "Vui lòng thêm sản phẩm vào giỏ hàng trước khi thanh toán!");
//                 return;
//             }

//             // Tạo stage mới cho popup thanh toán
//             Stage confirmStage = new Stage();
//             confirmStage.initModality(Modality.APPLICATION_MODAL);
//             confirmStage.setTitle("Xác nhận thanh toán");
//             confirmStage.setResizable(false);

//             // BorderPane chính
//             BorderPane mainLayout = new BorderPane();

//             // HEADER ĐẸP NGỜI
//             HBox header = new HBox();
//             header.setAlignment(Pos.CENTER);
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: linear-gradient(to right, #4e73df, #224abe);");

//             Label headerLabel = new Label("XÁC NHẬN THANH TOÁN");
//             headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
//             header.getChildren().add(headerLabel);

//             mainLayout.setTop(header);

//             // PHẦN NỘI DUNG CHÍNH
//             VBox content = new VBox(15);
//             content.setPadding(new Insets(20));

//             // Tổng thanh toán hiển thị nổi bật
//             double totalAmount = calculateTotalAmount();
//             Label totalLabel = new Label("Tổng thanh toán: " + String.format("%,.0f", totalAmount) + "đ");
//             totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e91e63;");

//             // BẢNG DANH SÁCH SẢN PHẨM - THÊM VÀO FORM THANH TOÁN
//             Label productsLabel = new Label("Danh sách sản phẩm:");
//             productsLabel.setStyle("-fx-font-weight: bold;");

//             // TableView hiển thị sản phẩm trong giỏ
//             TableView<CartItemEmployee> productsTable = new TableView<>();
//             productsTable.setPrefHeight(150);

//             // Cột tên sản phẩm
//             TableColumn<CartItemEmployee, String> nameColumn = new TableColumn<>("Tên sản phẩm");
//             nameColumn.setCellValueFactory(data ->
//                     new SimpleStringProperty(data.getValue().getProductName()));
//             nameColumn.setPrefWidth(200);

//             // Cột số lượng
//             TableColumn<CartItemEmployee, Integer> quantityColumn = new TableColumn<>("SL");
//             quantityColumn.setCellValueFactory(data ->
//                     new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
//             quantityColumn.setPrefWidth(50);

//             // Cột đơn giá
//             TableColumn<CartItemEmployee, Double> priceColumn = new TableColumn<>("Đơn giá");
//             priceColumn.setCellValueFactory(data ->
//                     new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
//             priceColumn.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double price, boolean empty) {
//                     super.updateItem(price, empty);
//                     if (empty || price == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", price) + "đ");
//                     }
//                 }
//             });
//             priceColumn.setPrefWidth(100);

//             // Cột bảo hành
//             TableColumn<CartItemEmployee, String> warrantyColumn = new TableColumn<>("Bảo hành");
//             warrantyColumn.setCellValueFactory(data -> {
//                 CartItemEmployee item = data.getValue();
//                 if (item.hasWarranty()) {
//                     return new SimpleStringProperty(item.getWarranty().getWarrantyType());
//                 }
//                 return new SimpleStringProperty("Không");
//             });
//             warrantyColumn.setPrefWidth(80);

//             // Cột thành tiền
//             TableColumn<CartItemEmployee, Double> subtotalColumn = new TableColumn<>("T.Tiền");
//             subtotalColumn.setCellValueFactory(data ->
//                     new SimpleDoubleProperty(data.getValue().getTotalPrice()).asObject());
//             subtotalColumn.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double price, boolean empty) {
//                     super.updateItem(price, empty);
//                     if (empty || price == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", price) + "đ");
//                     }
//                 }
//             });
//             subtotalColumn.setPrefWidth(100);

//             productsTable.getColumns().addAll(nameColumn, quantityColumn, priceColumn, warrantyColumn, subtotalColumn);
//             productsTable.setItems(cartItems);

//             // Phần thông tin khách hàng
//             Label customerLabel = new Label("Thông tin khách hàng:");
//             customerLabel.setStyle("-fx-font-weight: bold;");

// // Form thông tin khách hàng
//             GridPane customerForm = new GridPane();
//             customerForm.setVgap(10);
//             customerForm.setHgap(10);

//             Label nameLabel = new Label("Tên khách hàng:");
//             TextField nameField = new TextField("Khách lẻ");
//             nameField.setPrefWidth(300);

//             Label phoneLabel = new Label("Số điện thoại:");
//             TextField phoneField = new TextField("0900000000");
//             phoneField.setPrefWidth(300);

// // ✅ THÊM TRƯỜNG GHI CHÚ
//             Label noteLabel = new Label("Ghi chú:");
//             TextArea noteField = new TextArea();
//             noteField.setPromptText("Nhập ghi chú cho đơn hàng (không bắt buộc)...");
//             noteField.setPrefWidth(300);
//             noteField.setPrefHeight(60);
//             noteField.setWrapText(true);

//             customerForm.add(nameLabel, 0, 0);
//             customerForm.add(nameField, 1, 0);
//             customerForm.add(phoneLabel, 0, 1);
//             customerForm.add(phoneField, 1, 1);
//             customerForm.add(noteLabel, 0, 2);  // ✅ THÊM VÀO DÒNG THỨ 3
//             customerForm.add(noteField, 1, 2);

//             // Phương thức thanh toán - CHỈ CÓ 2 PHƯƠNG THỨC
//             Label paymentLabel = new Label("Phương thức thanh toán:");
//             paymentLabel.setStyle("-fx-font-weight: bold;");

//             ToggleGroup paymentGroup = new ToggleGroup();

//             RadioButton cashRadio = new RadioButton("Tiền mặt");
//             cashRadio.setToggleGroup(paymentGroup);
//             cashRadio.setSelected(true); // Mặc định chọn tiền mặt

//             RadioButton transferRadio = new RadioButton("Chuyển khoản");
//             transferRadio.setToggleGroup(paymentGroup);

//             HBox paymentOptions = new HBox(20);
//             paymentOptions.getChildren().addAll(cashRadio, transferRadio);

//             // Thêm các thành phần vào content
//             content.getChildren().addAll(
//                     totalLabel,
//                     new Separator(),
//                     productsLabel,
//                     productsTable,
//                     new Separator(),
//                     customerLabel,
//                     customerForm,
//                     new Separator(),
//                     paymentLabel,
//                     paymentOptions
//             );

//             mainLayout.setCenter(new ScrollPane(content));

//             // PHẦN FOOTER VỚI CÁC NÚT CHỨC NĂNG
//             HBox footer = new HBox(10);
//             footer.setAlignment(Pos.CENTER_RIGHT);
//             footer.setPadding(new Insets(15, 20, 15, 20));
//             footer.setStyle("-fx-background-color: #f8f9fc; -fx-border-color: #e3e6f0; -fx-border-width: 1 0 0 0;");

//             Button cancelButton = new Button("Hủy");
//             cancelButton.setPrefWidth(100);
//             cancelButton.setStyle("-fx-background-color: #e74a3b; -fx-text-fill: white;");

//             Button confirmButton = new Button("Xác nhận thanh toán");
//             confirmButton.setPrefWidth(200);
//             confirmButton.setStyle("-fx-background-color: #4e73df; -fx-text-fill: white; -fx-font-weight: bold;");

//             footer.getChildren().addAll(cancelButton, confirmButton);
//             mainLayout.setBottom(footer);

//             // Xử lý sự kiện cho nút Hủy
//             cancelButton.setOnAction(e -> confirmStage.close());

//             // Xử lý sự kiện cho nút Xác nhận thanh toán
//             confirmButton.setOnAction(e -> {
//                 try {
//                     // Lấy thông tin khách hàng và phương thức thanh toán
//                     String customerName = nameField.getText().trim();
//                     String customerPhone = phoneField.getText().trim();
//                     String paymentMethod = cashRadio.isSelected() ? "Tiền mặt" : "Chuyển khoản";

//                     // Validate số điện thoại
//                     if (!customerPhone.isEmpty() && customerPhone.length() < 10) {
//                         AlertUtil.showWarning("Lỗi", "Số điện thoại không hợp lệ!");
//                         return;
//                     }

//                     // NẾU CHỌN CHUYỂN KHOẢN - MỞ CỬA SỔ QR CODE
//                     if (transferRadio.isSelected()) {
//                         // Đóng cửa sổ xác nhận
//                         confirmStage.close();

//                         // Mở cửa sổ QR Payment
//                         showQRPaymentWindow(customerName, customerPhone, totalAmount, cartItems);
//                         return;
//                     }

//                     // NẾU THANH TOÁN TIỀN MẶT - XỬ LÝ LUÔN
//                     // Lưu đơn hàng vào DB và trả về orderID
//                     String orderId = saveOrderToDB(customerName, customerPhone, paymentMethod, totalAmount, cartItems);

//                     if (orderId != null) {
//                         // FIX LỖI: Chỉ lấy phần số từ orderId (bỏ phần chữ "ORD")
//                         String numericOrderId = orderId.replaceAll("[^0-9]", "");
//                         int orderIdInt = Integer.parseInt(numericOrderId);

//                         // Lưu vào bộ nhớ (để tương thích với code cũ) - DÙNG ID ĐÃ XỬ LÝ
//                         addToOrderHistory(orderIdInt, customerName, customerPhone,
//                                 paymentMethod, getCurrentDateTime(), totalAmount, cartItems);

//                         // Đóng cửa sổ thanh toán
//                         confirmStage.close();

//                         // Hiển thị thông báo thành công
//                         AlertUtil.showInfo("Thanh toán thành công",
//                                 "Đơn hàng #" + orderId + " đã được tạo thành công!");

//                         // In hóa đơn - DÙNG ID ĐÃ XỬ LÝ
//                         printReceiptWithPaymentMethod(
//                                 orderIdInt,
//                                 cartItems, totalAmount, customerName, customerPhone,
//                                 paymentMethod, getCurrentDateTime(), currentUser);

//                         // Xóa giỏ hàng
//                         clearCart();
//                     } else {
//                         // Thông báo lỗi
//                         AlertUtil.showError("Lỗi thanh toán",
//                                 "Không thể lưu đơn hàng. Vui lòng thử lại!");
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi thanh toán: " + ex.getMessage(), ex);
//                     AlertUtil.showError("Lỗi thanh toán", "Đã xảy ra lỗi: " + ex.getMessage());
//                     confirmStage.close();
//                 }
//             });

//             Scene scene = new Scene(mainLayout, 600, 700);
//             confirmStage.setScene(scene);
//             confirmStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi hiển thị form thanh toán: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể mở trang xác nhận thanh toán: " + e.getMessage());
//         }
//     }

//     /**
//      * Hiển thị cửa sổ thanh toán QR Code
//      */
//     private void showQRPaymentWindow(String customerName, String customerPhone, double totalAmount, ObservableList<CartItemEmployee> items) {
//         try {
//             LOGGER.info("💖 Bắt đầu mở cửa sổ QR Payment nè!");

//             // Tạo đối tượng Order giả
//             Order order = new Order();
//             order.setTotalAmount(totalAmount);

//             // DEBUG: In ra đường dẫn hiện tại
//             LOGGER.info("📂 Working Directory: " + System.getProperty("user.dir"));

//             FXMLLoader loader = null;

//             // THỬ TẤT CẢ CÁC ĐƯỜNG DẪN CÓ THỂ
//             String[] possiblePaths = {
//                     "/com/example/stores/view/qr_payment.fxml",
//                     "com/example/stores/view/qr_payment.fxml",
//                     "/view/qr_payment.fxml",
//                     "view/qr_payment.fxml",
//                     "/qr_payment.fxml",
//                     "qr_payment.fxml"
//             };

//             for (String path : possiblePaths) {
//                 try {
//                     LOGGER.info("🔍 Thử load FXML từ: " + path);
//                     URL fxmlUrl = getClass().getResource(path);

//                     if (fxmlUrl != null) {
//                         LOGGER.info("✅ Tìm thấy file FXML tại: " + fxmlUrl);
//                         loader = new FXMLLoader(fxmlUrl);
//                         break;
//                     } else {
//                         LOGGER.warning("❌ Không tìm thấy FXML tại: " + path);
//                     }
//                 } catch (Exception e) {
//                     LOGGER.warning("❌ Lỗi khi thử path: " + path + " - " + e.getMessage());
//                 }
//             }

//             // Nếu không tìm thấy file FXML nào
//             if (loader == null) {
//                 LOGGER.severe("😭 KHÔNG TÌM THẤY FILE FXML NÀO HẾT!!!");
//                 throw new Exception("Không tìm thấy file FXML cho QR Payment");
//             }

//             // Load FXML
//             Parent root = loader.load();
//             LOGGER.info("✅ Đã load FXML thành công!");

//             // Lấy controller và truyền dữ liệu
//             QRPaymentControllerE controller = loader.getController();
//             LOGGER.info("✅ Đã lấy controller thành công!");

//             // Tạo danh sách OrderDetail giả
//             List<OrderDetail> orderDetails = new ArrayList<>();
//             // Chuyển đổi từ CartItem sang OrderDetail
//             for (CartItemEmployee item : items) {
//                 OrderDetail detail = new OrderDetail();
//                 detail.setProductName(item.getProductName());
//                 detail.setQuantity(item.getQuantity());
//                 detail.setPrice(item.getPrice());
//                 orderDetails.add(detail);
//             }

//             // Set dữ liệu cho Controller
//             controller.setOrderDetails(order, orderDetails);
//             LOGGER.info("✅ Đã set order details!");

//             // Set callback khi thanh toán thành công
//             controller.setOnPaymentSuccess(() -> {
//                 try {
//                     // Tạo đơn hàng với phương thức thanh toán là chuyển khoản
//                     String orderId = saveOrderToDB(customerName, customerPhone, "Chuyển khoản", totalAmount, items);
//                     LOGGER.info("✅ Đã lưu đơn hàng với ID: " + orderId);

//                     if (orderId != null) {
//                         // FIX LỖI: Chỉ lấy phần số từ orderId
//                         String numericOrderId = orderId.replaceAll("[^0-9]", "");
//                         int orderIdInt = Integer.parseInt(numericOrderId);

//                         // Lưu vào bộ nhớ với ID đã xử lý
//                         addToOrderHistory(orderIdInt, customerName, customerPhone,
//                                 "Chuyển khoản", getCurrentDateTime(), totalAmount, items);

//                         // Hiển thị thông báo thành công
//                         AlertUtil.showInfo("Thanh toán thành công",
//                                 "Đơn hàng #" + orderId + " đã được thanh toán thành công!");

//                         // In hóa đơn với ID đã xử lý
//                         printReceiptWithPaymentMethod(
//                                 orderIdInt,
//                                 items, totalAmount, customerName, customerPhone,
//                                 "Chuyển khoản", getCurrentDateTime(), currentUser);

//                         // Xóa giỏ hàng
//                         clearCart();
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi hoàn tất thanh toán QR: " + ex.getMessage(), ex);
//                     AlertUtil.showError("Lỗi thanh toán", "Đã xảy ra lỗi: " + ex.getMessage());
//                 }
//             });

//             // Hiển thị cửa sổ QR
//             Stage qrStage = new Stage();
//             qrStage.initModality(Modality.APPLICATION_MODAL);
//             qrStage.setTitle("Thanh toán bằng mã QR");
//             qrStage.setResizable(false);

//             Scene scene = new Scene(root);
//             qrStage.setScene(scene);

//             LOGGER.info("💯 SẮP HIỆN CỬA SỔ QR PAYMENT RỒI!!!");
//             qrStage.show(); // Dùng show() thay vì showAndWait() để debug
//             LOGGER.info("🎉 ĐÃ HIỆN CỬA SỔ QR PAYMENT!!!");

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi hiển thị cửa sổ thanh toán QR: " + e.getMessage(), e);

//             // In lỗi chi tiết hơn
//             e.printStackTrace();

//             AlertUtil.showError("Lỗi", "Không thể mở cửa sổ thanh toán QR: " + e.getMessage() + "\nVui lòng thanh toán bằng tiền mặt!");

//             // Trong trường hợp lỗi, thử lại với phương thức thanh toán tiền mặt
//             try {
//                 String orderId = saveOrderToDB(customerName, customerPhone, "Tiền mặt", totalAmount, items);
//                 if (orderId != null) {
//                     // FIX LỖI: Chỉ lấy phần số từ orderId
//                     String numericOrderId = orderId.replaceAll("[^0-9]", "");
//                     int orderIdInt = Integer.parseInt(numericOrderId);

//                     addToOrderHistory(orderIdInt, customerName, customerPhone, "Tiền mặt", getCurrentDateTime(), totalAmount, items);

//                     AlertUtil.showInfo("Thanh toán thành công",
//                             "Đã chuyển sang thanh toán tiền mặt.\nĐơn hàng #" + orderId + " đã được tạo thành công!");

//                     printReceiptWithPaymentMethod(orderIdInt, items, totalAmount, customerName, customerPhone,
//                             "Tiền mặt", getCurrentDateTime(), currentUser);

//                     clearCart();
//                 }
//             } catch (Exception ex) {
//                 LOGGER.log(Level.SEVERE, "❌ Lỗi khi thử thanh toán tiền mặt: " + ex.getMessage(), ex);
//             }
//         }
//     }    /**
//      * Lưu đơn hàng vào DB
//      * @return Mã đơn hàng (orderID) nếu lưu thành công, null nếu thất bại
//      */
//     private String saveOrderToDB(String recipientName, String recipientPhone,
//                                  String paymentMethod, double totalAmount,
//                                  List<CartItemEmployee> cartItems) {
//         String orderId = null;
//         Connection conn = null;

//         try {
//             conn = DBConfig.getConnection();
//             conn.setAutoCommit(false);

//             // 1. Tạo đơn hàng mới trong bảng Orders
//             String insertOrderSQL = "INSERT INTO Orders (orderDate, totalAmount, customerID, " +
//                     "recipientPhone, recipientName, orderStatus, paymentMethod) " +
//                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

//             try (PreparedStatement pstmtOrder = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {

//                 pstmtOrder.setString(1, getCurrentDateTime());
//                 pstmtOrder.setDouble(2, totalAmount);

//                 // ==== SỬA ĐOẠN NÀY ĐỂ LƯU KHÁCH HÀNG MỚI ====
//                 CustomerServiceE customerServiceE = new CustomerServiceE();
//                 int customerId = customerServiceE.findCustomerIdByPhone(recipientPhone);
//                 if (customerId == -1) {
//                     Customer newCustomer = new Customer();
//                     newCustomer.setCustomerName(recipientName);
//                     newCustomer.setPhone(recipientPhone);
//                     newCustomer.setAddress(""); // Có thể lấy từ form nếu có
//                     newCustomer.setEmail("");   // Có thể lấy từ form nếu có
//                     customerId = customerServiceE.addCustomerToDB(newCustomer);
//                     if (customerId == -1) {
//                         LOGGER.warning("❌ Không thể tạo khách mới, fallback về ID=1");
//                         customerId = 1; // fallback nếu lỗi
//                     }
//                 }
//                 pstmtOrder.setInt(3, customerId);

//                 pstmtOrder.setString(4, recipientPhone != null ? recipientPhone : "");
//                 pstmtOrder.setString(5, recipientName != null ? recipientName : "Khách lẻ");
//                 pstmtOrder.setString(6, "Đã xác nhận");
//                 pstmtOrder.setString(7, paymentMethod != null ? paymentMethod : "Tiền mặt");

//                 int result = pstmtOrder.executeUpdate();

//                 if (result > 0) {
//                     // Lấy orderID vừa được tạo
//                     ResultSet generatedKeys = pstmtOrder.getGeneratedKeys();
//                     if (generatedKeys.next()) {
//                         orderId = generatedKeys.getString(1);
//                         LOGGER.info("✅ Đã tạo đơn hàng mới với ID: " + orderId);

//                         // 2. Thêm chi tiết đơn hàng
//                         saveOrderDetails(conn, orderId, cartItems);

//                         // 3. Commit transaction
//                         conn.commit();
//                     }
//                 }

//             }

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi lưu đơn hàng vào DB: " + e.getMessage(), e);
//             // Rollback transaction nếu có lỗi
//             if (conn != null) {
//                 try {
//                     conn.rollback();
//                 } catch (SQLException ex) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi rollback transaction: " + ex.getMessage(), ex);
//                 }
//             }

//         } finally {
//             // Đảm bảo đóng connection và reset autoCommit
//             if (conn != null) {
//                 try {
//                     conn.setAutoCommit(true);
//                     conn.close();
//                 } catch (SQLException e) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi đóng connection: " + e.getMessage(), e);
//                 }
//             }
//         }

//         return orderId;
//     }
//     /**
//      * Lưu chi tiết đơn hàng vào DB
//      */
//     private void saveOrderDetails(Connection conn, String orderId, List<CartItemEmployee> cartItems) throws SQLException {
//         String insertDetailSQL = "INSERT INTO OrderDetails (orderID, productID, quantity, unitPrice, warrantyType, warrantyPrice) " +
//                 "VALUES (?, ?, ?, ?, ?, ?)";

//         try (PreparedStatement pstmt = conn.prepareStatement(insertDetailSQL)) {
//             for (CartItemEmployee item : cartItems) {
//                 pstmt.setString(1, orderId);
//                 pstmt.setString(2, item.getProductID());
//                 pstmt.setInt(3, item.getQuantity());
//                 pstmt.setDouble(4, item.getPrice());

//                 // Xử lý thông tin bảo hành
//                 if (item.hasWarranty()) {
//                     pstmt.setString(5, item.getWarranty().getWarrantyType());
//                     pstmt.setDouble(6, item.getWarranty().getWarrantyPrice());
//                 } else {
//                     pstmt.setString(5, "Thường"); // Mặc định
//                     pstmt.setDouble(6, 0.0);
//                 }

//                 pstmt.addBatch();
//             }

//             int[] results = pstmt.executeBatch();
//             LOGGER.info("✅ Đã thêm " + results.length + " chi tiết đơn hàng");

//             // Cập nhật số lượng sản phẩm trong kho
//             updateProductQuantities(conn, cartItems);
//         }
//     }

//     /**
//      * Cập nhật số lượng sản phẩm trong kho sau khi thanh toán
//      */
//     private void updateProductQuantities(Connection conn, List<CartItemEmployee> cartItems) throws SQLException {
//         String updateSQL = "UPDATE Products SET quantity = quantity - ? WHERE productID = ?";

//         try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
//             for (CartItemEmployee item : cartItems) {
//                 pstmt.setInt(1, item.getQuantity());
//                 pstmt.setString(2, item.getProductID());
//                 pstmt.addBatch();
//             }

//             int[] results = pstmt.executeBatch();
//             LOGGER.info("✅ Đã cập nhật số lượng cho " + results.length + " sản phẩm");
//         }
//     }

//     /**
//      * Lấy thời gian hiện tại theo định dạng phù hợp với DB
//      */
//     private String getCurrentDateTime() {
//         LocalDateTime now = LocalDateTime.now();
//         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//         return now.format(formatter);
//     }

//     // Phương thức để nhận thông tin nhân viên từ màn hình login
//     public void initEmployeeData(Employee employee, String loginDateTime) {
//         try {
//             if (employee != null) {
//                 this.currentEmployee = employee;
//                 this.currentDateTime = loginDateTime;
//                 this.currentUser = employee.getUsername();

//                 // Dùng getFullName() - đảm bảo không gọi getName() vì có thể không có method này
//                 LOGGER.info("Đã khởi tạo POS với nhân viên: " + employee.getFullName());
//                 LOGGER.info("Thời gian hiện tại: " + currentDateTime);

//                 // Hiển thị thông tin nhân viên trên giao diện
//                 displayEmployeeInfo();
//             } else {
//                 LOGGER.warning("Lỗi: Employee object truyền vào là null");
//             }
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi khởi tạo dữ liệu nhân viên", e);
//         }
//     }

//     // Phương thức để nhận thông tin nhân viên từ màn hình login
//     public void setEmployeeInfo(int employeeID, String username) {
//         this.employeeId = employeeID; // Lưu employeeID vào biến instance
//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             // ĐÃ SỬA: Bọc trong try-catch để xử lý Exception từ getConnection()
//             try {
//                 conn = DBConfig.getConnection();
//             } catch (Exception ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi kết nối DB", ex);
//                 throw new SQLException("Không thể kết nối đến cơ sở dữ liệu: " + ex.getMessage());
//             }

//             if (conn == null) {
//                 throw new SQLException("Không thể kết nối đến cơ sở dữ liệu");
//             }

//             String query = "SELECT * FROM Employee WHERE employeeID = ? AND username = ?";
//             stmt = conn.prepareStatement(query);
//             stmt.setInt(1, employeeID);
//             stmt.setString(2, username);

//             rs = stmt.executeQuery();
//             if (rs.next()) {
//                 // Tạo đối tượng Employee từ ResultSet
//                 Employee emp = new Employee();
//                 emp.setEmployeeID(String.valueOf(employeeID));  // Chuyển int thành String
//                 emp.setUsername(rs.getString("username"));
//                 emp.setFullName(rs.getString("fullName"));
//                 emp.setEmail(rs.getString("email"));
//                 emp.setPhone(rs.getString("phone"));

//                 // Kiểm tra trước khi gọi setPosition
//                 try {
//                     int columnIndex = rs.findColumn("position");
//                     if (columnIndex > 0) {
//                         emp.setPosition(rs.getString("position"));
//                     }
//                 } catch (SQLException ex) {
//                     // Nếu không có cột position, bỏ qua
//                     LOGGER.info("Cột position không tồn tại trong bảng Employee");
//                 }

//                 // Gọi initEmployeeData với đối tượng Employee đã tạo
//                 String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//                 initEmployeeData(emp, currentTime);
//             } else {
//                 LOGGER.warning("Không tìm thấy nhân viên với ID=" + employeeID + " và username=" + username);
//                 Alert alert = new Alert(Alert.AlertType.WARNING);
//                 alert.setTitle("Cảnh báo");
//                 alert.setHeaderText("Không tìm thấy thông tin nhân viên");
//                 alert.setContentText("Vui lòng đăng nhập lại để tiếp tục.");
//                 alert.showAndWait();
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "Lỗi SQL khi lấy thông tin nhân viên", e);
//             Alert alert = new Alert(Alert.AlertType.ERROR);
//             alert.setTitle("Lỗi");
//             alert.setHeaderText("Không thể lấy thông tin nhân viên");
//             alert.setContentText("Chi tiết lỗi: " + e.getMessage());
//             alert.showAndWait();
//         } finally {
//             // Đóng tất cả các tài nguyên theo thứ tự ngược lại
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 // Không đóng connection ở đây vì có thể được sử dụng ở nơi khác
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi khi đóng tài nguyên SQL", ex);
//             }
//         }
//     }

//     // Hiển thị thông tin nhân viên trên giao diện - ĐÃ SỬA (FIX BUG 243)
//     private void displayEmployeeInfo() {
//         try {
//             if (currentEmployee != null && btnCheckout != null && btnCheckout.getParent() != null
//                     && btnCheckout.getParent().getParent() instanceof BorderPane) {

//                 BorderPane mainLayout = (BorderPane) btnCheckout.getParent().getParent();

//                 if (mainLayout.getTop() instanceof HBox) {
//                     HBox topBar = (HBox) mainLayout.getTop();

//                     // Tạo label hiển thị thông tin nhân viên
//                     Label lblEmployeeInfo = new Label(currentEmployee.getFullName() + " (" + currentUser + ")");
//                     lblEmployeeInfo.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

//                     // Tạo spacer để đẩy thông tin ra góc phải
//                     Region spacer = new Region();
//                     HBox.setHgrow(spacer, Priority.ALWAYS);

//                     // Thêm vào top bar
//                     topBar.getChildren().addAll(spacer, lblEmployeeInfo);
//                 }
//             }
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị thông tin nhân viên", e);
//         }
//     }

//     // Thêm nút đăng xuất
//     private void addLogoutButton() {
//         if (btnCheckout == null) {
//             LOGGER.warning("Lỗi: btnCheckout chưa được khởi tạo");
//             return;
//         }

//         Button btnLogout = new Button("ĐĂNG XUẤT");
//         btnLogout.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//         btnLogout.setPrefWidth(120);
//         btnLogout.setPrefHeight(35);
//         btnLogout.setOnAction(e -> logout());

//         if (btnCheckout.getParent() instanceof HBox) {
//             HBox parent = (HBox) btnCheckout.getParent();
//             parent.getChildren().add(0, btnLogout);
//         } else if (btnCheckout.getParent() instanceof Pane) {
//             Pane parent = (Pane) btnCheckout.getParent();
//             btnLogout.setLayoutX(btnCheckout.getLayoutX() - 130);
//             btnLogout.setLayoutY(btnCheckout.getLayoutY());
//             parent.getChildren().add(btnLogout);
//         }
//     }

//     // Xử lý đăng xuất
//     private void logout() {
//         try {
//             // Hiển thị xác nhận
//             Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
//             confirm.setTitle("Xác nhận đăng xuất");
//             confirm.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
//             confirm.setContentText("Mọi thay đổi chưa lưu sẽ bị mất.");

//             Optional<ButtonType> result = confirm.showAndWait();
//             if (result.isPresent() && result.get() == ButtonType.OK) {
//                 // Load màn hình đăng nhập
//                 URL loginUrl = getClass().getResource("/com/example/stores/view/employee_login.fxml");

//                 if (loginUrl != null) {
//                     FXMLLoader loader = new FXMLLoader(loginUrl);
//                     Parent root = loader.load();

//                     Scene scene = null;
//                     Stage stage = null;

//                     if (btnCheckout != null) {
//                         stage = (Stage) btnCheckout.getScene().getWindow();
//                         scene = new Scene(root);
//                         stage.setTitle("Computer Store - Đăng Nhập");
//                         stage.setScene(scene);
//                         stage.setResizable(false);
//                         stage.show();
//                     } else {
//                         LOGGER.warning("Lỗi: btnCheckout là null hoặc không thuộc Scene");
//                         stage = new Stage();
//                         scene = new Scene(root);
//                         stage.setTitle("Computer Store - Đăng Nhập");
//                         stage.setScene(scene);
//                         stage.setResizable(false);
//                         stage.show();

//                         // Đóng cửa sổ hiện tại nếu có
//                         if (productFlowPane != null && productFlowPane.getScene() != null) {
//                             Stage currentStage = (Stage) productFlowPane.getScene().getWindow();
//                             currentStage.close();
//                         }
//                     }

//                     LOGGER.info("Đã đăng xuất, thời gian: " +
//                             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//                 } else {
//                     throw new IOException("Không tìm thấy file employee_login.fxml");
//                 }
//             }
//         } catch (IOException e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi đăng xuất", e);
//             Alert alert = new Alert(Alert.AlertType.ERROR);
//             alert.setTitle("Lỗi");
//             alert.setContentText("Lỗi khi đăng xuất: " + e.getMessage());
//             alert.showAndWait();
//         }
//     }

//     // Thêm nút lịch sử đơn hàng
//     private void addHistoryButton() {
//         if (btnCheckout == null) {
//             LOGGER.warning("Lỗi: btnCheckout chưa được khởi tạo");
//             return;
//         }

//         Button btnHistory = new Button("LỊCH SỬ");
//         btnHistory.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
//         btnHistory.setPrefWidth(120);
//         btnHistory.setPrefHeight(35);
//         btnHistory.setOnAction(e -> showOrderHistoryInMemory()); // Sử dụng history trong bộ nhớ

//         if (btnCheckout.getParent() instanceof HBox) {
//             HBox parent = (HBox) btnCheckout.getParent();
//             parent.getChildren().add(0, btnHistory);
//         } else if (btnCheckout.getParent() instanceof Pane) {
//             Pane parent = (Pane) btnCheckout.getParent();
//             btnHistory.setLayoutX(btnCheckout.getLayoutX() - 130);
//             btnHistory.setLayoutY(btnCheckout.getLayoutY());
//             parent.getChildren().add(btnHistory);
//         }
//     }

//     // Cấu hình TableView giỏ hàng
//     // Đầu tiên em sửa hàm setupCartTable() để thêm cột bảo hành mới
//     private void setupCartTable() {
//         if (colCartName == null || colCartQty == null || colCartPrice == null || colCartTotal == null) {
//             LOGGER.warning("Lỗi: Các cột của TableView chưa được khởi tạo");
//             return;
//         }

//         // Thiết lập các cột cũ
//         colCartName.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleStringProperty("N/A");
//             }
//             String name = data.getValue().getProductName();
//             return new SimpleStringProperty(name != null ? name : "N/A");
//         });

//         colCartQty.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleIntegerProperty(0).asObject();
//             }
//             int qty = data.getValue().getQuantity();
//             return new SimpleIntegerProperty(qty).asObject();
//         });

//         colCartPrice.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleDoubleProperty(0).asObject();
//             }
//             double price = data.getValue().getPrice();
//             return new SimpleDoubleProperty(price).asObject();
//         });

//         colCartPrice.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//             @Override
//             protected void updateItem(Double price, boolean empty) {
//                 super.updateItem(price, empty);
//                 if (empty || price == null) {
//                     setText(null);
//                 } else {
//                     setText(String.format("%,.0f", price) + "đ");
//                 }
//             }
//         });

//         // THÊM CỘT BẢO HÀNH MỚI
//         colCartWarranty.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleStringProperty("Không");
//             }
//             CartItemEmployee item = data.getValue();
//             if (item.hasWarranty()) {
//                 return new SimpleStringProperty(item.getWarranty().getWarrantyType());
//             } else {
//                 return new SimpleStringProperty("Không");
//             }
//         });

//         // Nút sửa bảo hành
//         colCartWarranty.setCellFactory(tc -> new TableCell<CartItemEmployee, String>() {
//             @Override
//             protected void updateItem(String warrantyType, boolean empty) {
//                 super.updateItem(warrantyType, empty);
//                 if (empty) {
//                     setText(null);
//                     setGraphic(null);
//                 } else {
//                     HBox container = new HBox(5);
//                     container.setAlignment(Pos.CENTER_LEFT);

//                     // Hiển thị loại bảo hành
//                     Label lblType = new Label(warrantyType);

//                     // Nút sửa nhỏ bên cạnh
//                     Button btnEdit = new Button("⚙️");
//                     btnEdit.setStyle("-fx-background-color: transparent; -fx-padding: 0 2;");
//                     btnEdit.setOnAction(event -> {
//                         CartItemEmployee item = getTableView().getItems().get(getIndex());
//                         if (item != null) {
//                             showWarrantyEditDialog(item);
//                         }
//                     });

//                     container.getChildren().addAll(lblType, btnEdit);
//                     setGraphic(container);
//                     setText(null);
//                 }
//             }
//         });

//         colCartTotal.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleDoubleProperty(0).asObject();
//             }
//             double total = data.getValue().getTotalPrice();
//             return new SimpleDoubleProperty(total).asObject();
//         });

//         colCartTotal.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//             @Override
//             protected void updateItem(Double total, boolean empty) {
//                 super.updateItem(total, empty);
//                 if (empty || total == null) {
//                     setText(null);
//                 } else {
//                     setText(String.format("%,.0f", total) + "đ");
//                 }
//             }
//         });
//     }

//     // Sửa lại dialog chỉnh sửa bảo hành trong giỏ hàng
//     private void showWarrantyEditDialog(CartItemEmployee item) {
//         try {
//             // Tìm thông tin sản phẩm từ database để lấy giá
//             Product product = findProductById(item.getProductID());
//             if (product == null) {
//                 AlertUtil.showWarning("Lỗi", "Không tìm thấy thông tin sản phẩm");
//                 return;
//             }

//             Stage dialogStage = new Stage();
//             dialogStage.setTitle("Cập nhật bảo hành");
//             dialogStage.initModality(Modality.APPLICATION_MODAL);

//             VBox dialogContent = new VBox(15);
//             dialogContent.setPadding(new Insets(20));
//             dialogContent.setAlignment(Pos.CENTER);

//             // Tiêu đề và thông tin sản phẩm
//             Label lblTitle = new Label("Chọn gói bảo hành cho " + item.getProductName());
//             lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             // ComboBox chọn loại bảo hành - SỬA LẠI CÒN 2 LOẠI
//             ComboBox<String> cbWarranty = new ComboBox<>();

//             // Kiểm tra điều kiện bảo hành thường
//             boolean isEligibleForStdWarranty = WarrantyCalculator.isEligibleForStandardWarranty(product);

//             if (isEligibleForStdWarranty) {
//                 // Chỉ còn 2 lựa chọn
//                 cbWarranty.getItems().addAll("Không", "Thường", "Vàng");
//             } else {
//                 // Sản phẩm không đủ điều kiện bảo hành
//                 cbWarranty.getItems().add("Không");
//             }

//             // Set giá trị hiện tại
//             if (item.hasWarranty()) {
//                 String currentType = item.getWarranty().getWarrantyType();
//                 // Chuyển đổi các loại bảo hành cũ (nếu có)
//                 if (!currentType.equals("Thường") && !currentType.equals("Vàng")) {
//                     currentType = "Thường"; // Mặc định về Thường
//                 }

//                 if (cbWarranty.getItems().contains(currentType)) {
//                     cbWarranty.setValue(currentType);
//                 } else {
//                     cbWarranty.setValue("Không");
//                 }
//             } else {
//                 cbWarranty.setValue("Không");
//             }

//             // Hiển thị giá bảo hành
//             Label lblWarrantyPrice = new Label("Phí bảo hành: 0đ");
//             Label lblTotalWithWarranty = new Label("Tổng tiền: " + String.format("%,.0f", item.getTotalPrice()) + "đ");
//             lblTotalWithWarranty.setStyle("-fx-font-weight: bold;");

//             // Thêm mô tả bảo hành
//             Label lblWarrantyInfo = new Label("Không bảo hành");
//             lblWarrantyInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #757575;");

//             // Cập nhật giá khi thay đổi loại bảo hành
//             cbWarranty.setOnAction(e -> {
//                 String selectedType = cbWarranty.getValue();

//                 // TH1: Không bảo hành
//                 if (selectedType.equals("Không")) {
//                     lblWarrantyPrice.setText("Phí bảo hành: 0đ");
//                     double basePrice = product.getPrice() * item.getQuantity();
//                     lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,.0f", basePrice) + "đ");
//                     lblWarrantyInfo.setText("Không bảo hành cho sản phẩm này");
//                     lblWarrantyInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #757575;");
//                     return;
//                 }

//                 // TH2: Bảo hành thường
//                 if (selectedType.equals("Thường")) {
//                     lblWarrantyPrice.setText("Phí bảo hành: 0đ");
//                     double basePrice = product.getPrice() * item.getQuantity();
//                     lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,.0f", basePrice) + "đ");
//                     lblWarrantyInfo.setText("Bảo hành thường miễn phí 12 tháng");
//                     lblWarrantyInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #4CAF50;");
//                     return;
//                 }

//                 // TH3: Bảo hành vàng (10% giá gốc)
//                 double warrantyFee = product.getPrice() * 0.1 * item.getQuantity();
//                 lblWarrantyPrice.setText("Phí bảo hành: " + String.format("%,.0f", warrantyFee) + "đ");

//                 // Cập nhật tổng tiền
//                 double totalPrice = (product.getPrice() * item.getQuantity()) + warrantyFee;
//                 lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,.0f", totalPrice) + "đ");

//                 lblWarrantyInfo.setText("✨ Bảo hành Vàng 24 tháng, 1 đổi 1");
//                 lblWarrantyInfo.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF9800;");
//             });

//             // Nút lưu và hủy
//             Button btnSave = new Button("Lưu thay đổi");
//             btnSave.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnSave.setPrefWidth(140);
//             btnSave.setOnAction(e -> {
//                 String selectedType = cbWarranty.getValue();

//                 if ("Không".equals(selectedType)) {
//                     // Xóa bảo hành nếu chọn không bảo hành
//                     item.setWarranty(null);
//                 } else {
//                     // Tạo bảo hành mới với loại đã chọn
//                     Warranty warranty = WarrantyCalculator.createWarranty(product, selectedType);
//                     item.setWarranty(warranty);
//                 }

//                 // Cập nhật hiển thị
//                 updateCartDisplay();
//                 dialogStage.close();
//                 AlertUtil.showInformation("Thành công", "Đã cập nhật bảo hành cho sản phẩm");
//             });

//             Button btnCancel = new Button("Hủy");
//             btnCancel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnCancel.setPrefWidth(80);
//             btnCancel.setOnAction(e -> dialogStage.close());

//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.getChildren().addAll(btnSave, btnCancel);

//             // Thêm các thành phần vào dialog
//             dialogContent.getChildren().addAll(
//                     lblTitle,
//                     new Separator(),
//                     cbWarranty,
//                     lblWarrantyInfo,
//                     lblWarrantyPrice,
//                     lblTotalWithWarranty,
//                     buttonBox
//             );

//             // Hiện dialog
//             Scene scene = new Scene(dialogContent, 350, 320);
//             dialogStage.setScene(scene);
//             dialogStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị dialog chỉnh sửa bảo hành", e);
//             AlertUtil.showError("Lỗi", "Không thể mở cửa sổ chỉnh sửa bảo hành");
//         }
//     }

//     // Thêm nút xóa vào bảng giỏ hàng
//     private void addButtonsToTable() {
//         if (cartTable == null) {
//             LOGGER.warning("Lỗi: cartTable chưa được khởi tạo");
//             return;
//         }

//         colCartAction = new TableColumn<>("Xóa");
//         colCartAction.setCellFactory(param -> new TableCell<CartItemEmployee, Void>() {
//             private final Button btnDelete = new Button("X");

//             {
//                 btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//                 btnDelete.setOnAction(event -> {
//                     CartItemEmployee item = getTableRow().getItem();
//                     if (item != null) {
//                         // Hiện dialog xác nhận trước khi xóa
//                         Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
//                                 "Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?",
//                                 ButtonType.YES, ButtonType.NO);
//                         alert.setTitle("Xác nhận xóa");
//                         alert.setHeaderText("Xóa sản phẩm");

//                         Optional<ButtonType> result = alert.showAndWait();
//                         if (result.isPresent() && result.get() == ButtonType.YES) {
//                             cartItems.remove(item);
//                             updateTotal();
//                         }
//                     }
//                 });
//             }

//             @Override
//             protected void updateItem(Void item, boolean empty) {
//                 super.updateItem(item, empty);
//                 if (empty) {
//                     setGraphic(null);
//                 } else {
//                     setGraphic(btnDelete);
//                 }
//             }
//         });

//         colCartAction.setPrefWidth(50);

//         // Thêm cột vào TableView nếu chưa có
//         if (!cartTable.getColumns().contains(colCartAction)) {
//             cartTable.getColumns().add(colCartAction);
//         }
//     }

//     // Hiển thị thông báo lỗi
//     private void showErrorAlert(String message) {
//         Alert alert = new Alert(Alert.AlertType.WARNING, message);
//         alert.setTitle("Lỗi");
//         alert.setHeaderText("Thông tin không hợp lệ");
//         alert.showAndWait();
//     }


//     // Thêm method mới vào PosOverviewController
//     private void showOrderByIdWindow(String orderIdInput) {
//         try {
//             LOGGER.info("🔍 Tìm kiếm đơn hàng với ID: " + orderIdInput);

//             // Chuẩn hóa orderID (có thể người dùng nhập 1, 2, 3 hoặc ORD001, ORD002)
//             String searchOrderId = normalizeOrderId(orderIdInput);
//             LOGGER.info("📝 OrderID sau khi chuẩn hóa: " + searchOrderId);

//             // Tìm đơn hàng trong database
//             OrderHistoryServiceE.OrderWithDetails orderData = OrderHistoryServiceE.getCompleteOrderById(searchOrderId);

//             if (orderData == null || orderData.getOrderHistory() == null) {
//                 AlertUtil.showWarning("Không tìm thấy",
//                         "Không tìm thấy đơn hàng với mã: " + orderIdInput + "\nĐã thử tìm: " + searchOrderId);
//                 return;
//             }

//             OrderHistory order = orderData.getOrderHistory();
//             ObservableList<OrderDetail> details = orderData.getOrderDetails();

//             LOGGER.info("✅ Tìm thấy đơn hàng: " + order.getOrderID() + " với " + details.size() + " sản phẩm");

//             // Tạo cửa sổ hiển thị chi tiết
//             showSingleOrderDetailWindow(order, details);

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi tìm đơn hàng theo ID: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể tìm đơn hàng: " + e.getMessage());
//         }
//     }

//     // Helper method chuẩn hóa orderID
//     private String normalizeOrderId(String input) {
//         if (input == null || input.trim().isEmpty()) {
//             return input;
//         }

//         String trimmed = input.trim();

//         // Nếu đã có định dạng ORDxxx thì giữ nguyên
//         if (trimmed.toUpperCase().startsWith("ORD")) {
//             return trimmed;
//         }

//         // Nếu là số thuần túy, thử cả 2 cách
//         try {
//             int numericId = Integer.parseInt(trimmed);
//             // Thử format ORD001 trước
//             return String.format("ORD%03d", numericId);
//         } catch (NumberFormatException e) {
//             // Nếu không phải số, trả về nguyên input
//             return trimmed;
//         }
//     }
//     // Thêm method hiển thị chi tiết đơn hàng
//     private void showSingleOrderDetailWindow(OrderHistory order, ObservableList<OrderDetail> details) {
//         try {
//             Stage detailStage = new Stage();
//             detailStage.initModality(Modality.APPLICATION_MODAL);
//             detailStage.setTitle("Chi tiết đơn hàng #" + order.getOrderID());
//             detailStage.setResizable(true);

//             BorderPane mainLayout = new BorderPane();

//             // Header đẹp
//             HBox header = new HBox();
//             header.setAlignment(Pos.CENTER);
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: linear-gradient(to right, #4CAF50, #45a049);");

//             Label headerTitle = new Label("CHI TIẾT ĐƠN HÀNG #" + order.getOrderID());
//             headerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
//             header.getChildren().add(headerTitle);

//             // Content
//             VBox content = new VBox(15);
//             content.setPadding(new Insets(20));

//             // Thông tin đơn hàng
//             GridPane infoGrid = new GridPane();
//             infoGrid.setHgap(15);
//             infoGrid.setVgap(10);
//             infoGrid.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8;");

//             int row = 0;
//             addInfoRow(infoGrid, "Mã đơn hàng:", order.getOrderID(), row++);
//             addInfoRow(infoGrid, "Ngày đặt:", order.getFormattedDate(), row++);
//             addInfoRow(infoGrid, "Khách hàng:", order.getCustomerName(), row++);
//             addInfoRow(infoGrid, "Số điện thoại:", order.getCustomerPhone(), row++);
//             addInfoRow(infoGrid, "Nhân viên:", order.getEmployeeName(), row++);
//             addInfoRow(infoGrid, "Phương thức thanh toán:", order.getPaymentMethod(), row++);
//             addInfoRow(infoGrid, "Trạng thái:", order.getStatus(), row++);

//             // Bảng sản phẩm
//             Label productsLabel = new Label("DANH SÁCH SẢN PHẨM:");
//             productsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             TableView<OrderDetail> productsTable = new TableView<>();
//             productsTable.setPrefHeight(300);
//             productsTable.setItems(details);

//             // Các cột
//             TableColumn<OrderDetail, String> colProductName = new TableColumn<>("Tên sản phẩm");
//             colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
//             colProductName.setPrefWidth(250);

//             TableColumn<OrderDetail, Integer> colQuantity = new TableColumn<>("SL");
//             colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
//             colQuantity.setPrefWidth(50);

//             TableColumn<OrderDetail, String> colUnitPrice = new TableColumn<>("Đơn giá");
//             colUnitPrice.setCellValueFactory(data ->
//                     new SimpleStringProperty(String.format("%,.0f₫", data.getValue().getUnitPrice())));
//             colUnitPrice.setPrefWidth(100);

//             TableColumn<OrderDetail, String> colWarranty = new TableColumn<>("Bảo hành");
//             colWarranty.setCellValueFactory(new PropertyValueFactory<>("warrantyType"));
//             colWarranty.setPrefWidth(100);

//             TableColumn<OrderDetail, String> colSubtotal = new TableColumn<>("Thành tiền");
//             colSubtotal.setCellValueFactory(data ->
//                     new SimpleStringProperty(String.format("%,.0f₫", data.getValue().getSubtotal())));
//             colSubtotal.setPrefWidth(120);

//             productsTable.getColumns().addAll(colProductName, colQuantity, colUnitPrice, colWarranty, colSubtotal);

//             // Tổng tiền
//             Label totalLabel = new Label("TỔNG TIỀN: " + order.getFormattedAmount());
//             totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e91e63;");

//             // Buttons
//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.setPadding(new Insets(10, 0, 0, 0));

//             Button btnPrint = new Button("In hóa đơn");
//             btnPrint.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnPrint.setPrefWidth(120);
//             btnPrint.setOnAction(e -> {
//                 // Gọi method in hóa đơn (sử dụng lại code cũ)
//                 AlertUtil.showInfo("Thông báo", "Tính năng in hóa đơn đang được phát triển!");
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnClose.setPrefWidth(100);
//             btnClose.setOnAction(e -> detailStage.close());

//             buttonBox.getChildren().addAll(btnPrint, btnClose);

//             // Thêm vào content
//             content.getChildren().addAll(infoGrid, productsLabel, productsTable, totalLabel, buttonBox);

//             // Layout chính
//             mainLayout.setTop(header);
//             mainLayout.setCenter(new ScrollPane(content));

//             Scene scene = new Scene(mainLayout, 700, 600);
//             detailStage.setScene(scene);
//             detailStage.show();

//             LOGGER.info("✅ Đã hiển thị chi tiết đơn hàng: " + order.getOrderID());

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi hiển thị chi tiết đơn hàng: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị chi tiết đơn hàng: " + e.getMessage());
//         }
//     }

//     // Helper method thêm dòng thông tin
//     private void addInfoRow(GridPane grid, String label, String value, int row) {
//         Label lblLabel = new Label(label);
//         lblLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");

//         Label lblValue = new Label(value != null ? value : "N/A");
//         lblValue.setStyle("-fx-font-weight: bold;");

//         grid.add(lblLabel, 0, row);
//         grid.add(lblValue, 1, row);
//     }
//     // Method hiển thị tất cả đơn hàng (nếu user chọn checkbox)
//     private void showAllOrdersWindow() {
//         try {
//             LOGGER.info("📋 Hiển thị tất cả đơn hàng...");

//             ObservableList<OrderHistory> allOrders = OrderHistoryServiceE.getOrderHistories();

//             if (allOrders.isEmpty()) {
//                 AlertUtil.showInfo("Thông báo", "Không có đơn hàng nào trong hệ thống!");
//                 return;
//             }

//             // Tạo cửa sổ đơn giản hiển thị danh sách
//             Stage listStage = new Stage();
//             listStage.setTitle("Tất cả đơn hàng (" + allOrders.size() + " đơn)");
//             listStage.setResizable(true);

//             // TableView đơn giản
//             TableView<OrderHistory> table = new TableView<>();
//             table.setItems(allOrders);

//             TableColumn<OrderHistory, String> colId = new TableColumn<>("Mã ĐH");
//             colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrderID()));
//             colId.setPrefWidth(100);

//             TableColumn<OrderHistory, String> colDate = new TableColumn<>("Ngày");
//             colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedDate()));
//             colDate.setPrefWidth(150);

//             TableColumn<OrderHistory, String> colCustomer = new TableColumn<>("Khách hàng");
//             colCustomer.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomerName()));
//             colCustomer.setPrefWidth(150);

//             TableColumn<OrderHistory, String> colTotal = new TableColumn<>("Tổng tiền");
//             colTotal.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedAmount()));
//             colTotal.setPrefWidth(120);

//             TableColumn<OrderHistory, Void> colAction = new TableColumn<>("Hành động");
//             colAction.setCellFactory(tc -> new TableCell<OrderHistory, Void>() {
//                 private final Button btn = new Button("Xem chi tiết");
//                 {
//                     btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
//                     btn.setOnAction(event -> {
//                         OrderHistory selectedOrder = getTableView().getItems().get(getIndex());
//                         if (selectedOrder != null) {
//                             listStage.close();
//                             showOrderByIdWindow(selectedOrder.getOrderID());
//                         }
//                     });
//                 }

//                 @Override
//                 protected void updateItem(Void item, boolean empty) {
//                     super.updateItem(item, empty);
//                     if (empty) {
//                         setGraphic(null);
//                     } else {
//                         setGraphic(btn);
//                     }
//                 }
//             });
//             colAction.setPrefWidth(120);

//             table.getColumns().addAll(colId, colDate, colCustomer, colTotal, colAction);

//             Scene scene = new Scene(new VBox(table), 800, 500);
//             listStage.setScene(scene);
//             listStage.show();

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi hiển thị tất cả đơn hàng: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị danh sách đơn hàng: " + e.getMessage());
//         }
//     }
//     // Hiển thị lịch sử đơn hàng từ bộ nhớ
//     // Thay thế method showOrderHistoryInMemory() cũ
//     private void showOrderHistoryInMemory() {
//         try {
//             // Tạo dialog nhập mã đơn hàng
//             Stage searchStage = new Stage();
//             searchStage.initModality(Modality.APPLICATION_MODAL);
//             searchStage.setTitle("Tìm kiếm đơn hàng");
//             searchStage.setResizable(false);

//             VBox layout = new VBox(15);
//             layout.setPadding(new Insets(20));
//             layout.setAlignment(Pos.CENTER);

//             // Header
//             Label headerLabel = new Label("TÌM KIẾM ĐƠN HÀNG");
//             headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

//             // Input mã đơn hàng
//             Label lblOrderId = new Label("Nhập mã đơn hàng:");
//             lblOrderId.setStyle("-fx-font-weight: bold;");

//             TextField txtOrderId = new TextField();
//             txtOrderId.setPromptText("Ví dụ: 1, 2, 3... hoặc ORD001, ORD002...");
//             txtOrderId.setPrefWidth(300);
//             txtOrderId.setStyle("-fx-font-size: 14px;");

//             // Hoặc xem tất cả
//             CheckBox chkShowAll = new CheckBox("Hiển thị tất cả đơn hàng");
//             chkShowAll.setStyle("-fx-font-size: 12px;");

//             // Buttons
//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);

//             Button btnSearch = new Button("Tìm kiếm");
//             btnSearch.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnSearch.setPrefWidth(100);

//             Button btnCancel = new Button("Hủy");
//             btnCancel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnCancel.setPrefWidth(100);

//             buttonBox.getChildren().addAll(btnSearch, btnCancel);

//             // Events
//             btnCancel.setOnAction(e -> searchStage.close());

//             btnSearch.setOnAction(e -> {
//                 try {
//                     searchStage.close();

//                     if (chkShowAll.isSelected()) {
//                         // Hiển thị tất cả đơn hàng
//                         showAllOrdersWindow();
//                     } else {
//                         // Tìm theo ID cụ thể
//                         String orderId = txtOrderId.getText().trim();
//                         if (orderId.isEmpty()) {
//                             AlertUtil.showWarning("Thông báo", "Vui lòng nhập mã đơn hàng!");
//                             return;
//                         }
//                         showOrderByIdWindow(orderId);
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi tìm kiếm đơn hàng: " + ex.getMessage(), ex);
//                     AlertUtil.showError("Lỗi", "Không thể tìm kiếm đơn hàng: " + ex.getMessage());
//                 }
//             });

//             // Enter để tìm kiếm
//             txtOrderId.setOnKeyPressed(event -> {
//                 if (event.getCode().toString().equals("ENTER")) {
//                     btnSearch.fire();
//                 }
//             });

//             layout.getChildren().addAll(headerLabel, lblOrderId, txtOrderId, chkShowAll, buttonBox);

//             Scene scene = new Scene(layout, 400, 250);
//             searchStage.setScene(scene);
//             searchStage.showAndWait();

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị dialog tìm kiếm: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể mở cửa sổ tìm kiếm: " + e.getMessage());
//         }
//     }

//     // Hiển thị chi tiết đơn hàng từ bộ nhớ
//     private void showOrderDetailsFromMemory(OrderSummary order) {
//         try {
//             if (order == null) {
//                 LOGGER.warning("Lỗi: OrderSummary object là null");
//                 return;
//             }

//             Stage detailStage = new Stage();
//             detailStage.initModality(Modality.APPLICATION_MODAL);
//             detailStage.setTitle("Chi tiết đơn hàng #" + order.getId());

//             BorderPane borderPane = new BorderPane();

//             // Header
//             HBox header = new HBox();
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: #2196F3;");

//             Label headerTitle = new Label("CHI TIẾT ĐƠN HÀNG #" + order.getId());
//             headerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

//             header.getChildren().add(headerTitle);
//             header.setAlignment(Pos.CENTER);

//             borderPane.setTop(header);

//             // Content
//             VBox content = new VBox(15);
//             content.setPadding(new Insets(20));

//             // Thông tin đơn hàng
//             VBox orderInfoBox = new VBox(8);
//             orderInfoBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");

//             Label lblCustomer = new Label("Khách hàng: " + order.getCustomerName());
//             Label lblPhone = new Label("SĐT: " + order.getCustomerPhone());
//             Label lblPayment = new Label("Phương thức thanh toán: " + order.getPaymentMethod());
//             Label lblDate = new Label("Ngày mua: " + order.getOrderDate());

//             orderInfoBox.getChildren().addAll(lblCustomer, lblPhone, lblPayment, lblDate);

//             // Danh sách sản phẩm
//             Label lblProductsTitle = new Label("Danh sách sản phẩm:");
//             lblProductsTitle.setStyle("-fx-font-weight: bold;");

//             TableView<CartItemEmployee> detailTable = new TableView<>();
//             detailTable.setPrefHeight(300);

//             TableColumn<CartItemEmployee, String> colProductName = new TableColumn<>("Tên sản phẩm");
//             colProductName.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleStringProperty("N/A");
//                 }
//                 String productName = data.getValue().getProductName();
//                 return new SimpleStringProperty(productName != null ? productName : "N/A");
//             });
//             colProductName.setPrefWidth(200);

//             TableColumn<CartItemEmployee, Integer> colQuantity = new TableColumn<>("SL");
//             colQuantity.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleIntegerProperty(0).asObject();
//                 }
//                 return new SimpleIntegerProperty(data.getValue().getQuantity()).asObject();
//             });
//             colQuantity.setPrefWidth(50);

//             TableColumn<CartItemEmployee, Double> colPrice = new TableColumn<>("Đơn giá");
//             colPrice.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleDoubleProperty(0).asObject();
//                 }
//                 return new SimpleDoubleProperty(data.getValue().getPrice()).asObject();
//             });
//             colPrice.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double price, boolean empty) {
//                     super.updateItem(price, empty);
//                     if (empty || price == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", price) + "đ");
//                     }
//                 }
//             });
//             colPrice.setPrefWidth(100);

//             // Thêm cột bảo hành
//             TableColumn<CartItemEmployee, String> colWarranty = new TableColumn<>("Bảo hành");
//             colWarranty.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleStringProperty("Không");
//                 }

//                 CartItemEmployee item = data.getValue();
//                 if (item.hasWarranty()) {
//                     return new SimpleStringProperty(item.getWarranty().getWarrantyType());
//                 } else {
//                     return new SimpleStringProperty("Không");
//                 }
//             });
//             colWarranty.setPrefWidth(100);

//             TableColumn<CartItemEmployee, Double> colSubtotal = new TableColumn<>("Thành tiền");
//             colSubtotal.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleDoubleProperty(0).asObject();
//                 }
//                 return new SimpleDoubleProperty(data.getValue().getTotalPrice()).asObject();
//             });
//             colSubtotal.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double total, boolean empty) {
//                     super.updateItem(total, empty);
//                     if (empty || total == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", total) + "đ");
//                     }
//                 }
//             });
//             colSubtotal.setPrefWidth(100);

//             detailTable.getColumns().addAll(colProductName, colQuantity, colPrice, colWarranty, colSubtotal);

//             // Kiểm tra null trước khi thêm items
//             if (order.getItems() != null) {
//                 detailTable.setItems(FXCollections.observableArrayList(order.getItems()));
//             } else {
//                 detailTable.setItems(FXCollections.observableArrayList());
//             }

//             // Hiển thị tổng tiền
//             Label lblTotal = new Label("Tổng tiền: " + String.format("%,.0f", order.getTotalAmount()) + "đ");
//             lblTotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e91e63;");

//             // Button in hóa đơn và đóng
//             Button btnPrint = new Button("In hóa đơn");
//             btnPrint.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnPrint.setPrefWidth(150);

//             // Fix lỗi lambda expression bằng cách sử dụng final variable
//             final int orderId = order.getId();
//             final double totalAmount = order.getTotalAmount();
//             final String customerName2 = order.getCustomerName();
//             final String customerPhone2 = order.getCustomerPhone();
//             final String paymentMethod2 = order.getPaymentMethod();
//             final String orderDateTime = order.getOrderDate();
//             final List<CartItemEmployee> orderItems = order.getItems() != null ? order.getItems() : new ArrayList<>();

//             btnPrint.setOnAction(e -> {
//                 try {
//                     // In hóa đơn với các biến final
//                     printReceiptWithPaymentMethod(
//                             orderId,
//                             orderItems,
//                             totalAmount,
//                             customerName2,
//                             customerPhone2,
//                             paymentMethod2,
//                             orderDateTime,
//                             currentUser
//                     );
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi in hóa đơn", ex);
//                     showErrorAlert("Có lỗi xảy ra: " + ex.getMessage());
//                 }
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnClose.setPrefWidth(100);
//             btnClose.setOnAction(e -> detailStage.close());

//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.getChildren().addAll(btnPrint, btnClose);
//             buttonBox.setPadding(new Insets(10, 0, 0, 0));

//             content.getChildren().addAll(orderInfoBox, lblProductsTitle, detailTable, lblTotal, buttonBox);

//             borderPane.setCenter(content);

//             Scene scene = new Scene(borderPane, 650, 550);
//             detailStage.setScene(scene);
//             detailStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị chi tiết đơn hàng", e);
//             showErrorAlert("Có lỗi xảy ra: " + e.getMessage());
//         }
//     }

//     // Phương thức in hóa đơn có thêm phương thức thanh toán và thông tin bảo hành
//     public void printReceiptWithPaymentMethod(int orderID, List<CartItemEmployee> items, double totalAmount,
//                                               String customerName, String customerPhone, String paymentMethod,
//                                               String orderDateTime, String cashierName) {
//         try {
//             // Kiểm tra danh sách sản phẩm
//             if (items == null || items.isEmpty()) {
//                 Alert alert = new Alert(Alert.AlertType.WARNING);
//                 alert.setTitle("Cảnh báo");
//                 alert.setHeaderText("Không thể in hóa đơn");
//                 alert.setContentText("Không có sản phẩm nào trong đơn hàng.");
//                 alert.showAndWait();
//                 return;
//             }

//             // Tạo cảnh báo để hiển thị trước khi in
//             Alert printingAlert = new Alert(Alert.AlertType.INFORMATION);
//             printingAlert.setTitle("Đang in hóa đơn");
//             printingAlert.setHeaderText("Đang chuẩn bị in hóa đơn");
//             printingAlert.setContentText("Vui lòng đợi trong giây lát...");
//             printingAlert.show();

//             // Tạo nội dung hóa đơn
//             VBox receiptContent = new VBox(5);
//             receiptContent.setPadding(new Insets(20));
//             receiptContent.setStyle("-fx-background-color: white;");

//             // Tiêu đề
//             Label lblTitle = new Label("HÓA ĐƠN THANH TOÁN");
//             lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-alignment: center;");
//             lblTitle.setMaxWidth(Double.MAX_VALUE);
//             lblTitle.setAlignment(Pos.CENTER);

//             // Logo công ty (nếu có)
//             ImageView logo = new ImageView();
//             try {
//                 InputStream is = getClass().getResourceAsStream("/com/example/stores/images/layout/employee_logo.png");
//                 if (is != null) {
//                     logo.setImage(new Image(is));
//                     logo.setFitWidth(100);
//                     logo.setPreserveRatio(true);
//                 }
//             } catch (Exception e) {
//                 LOGGER.log(Level.WARNING, "Không tìm thấy logo", e);
//             }

//             // Thông tin cửa hàng
//             Label lblStoreName = new Label("COMPUTER STORE");
//             lblStoreName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             Label lblStoreAddress = new Label("Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM");
//             Label lblStorePhone = new Label("Điện thoại: 028.1234.5678");

//             // Thông tin hóa đơn
//             Label lblOrderID = new Label("Mã đơn hàng: #" + orderID);
//             lblOrderID.setStyle("-fx-font-weight: bold;");

//             Label lblDateTime = new Label("Ngày: " + orderDateTime);
//             Label lblCashier = new Label("Thu ngân: " + cashierName);
//             Label lblCustomerName = new Label("Khách hàng: " + customerName);
//             Label lblCustomerPhone = new Label("SĐT khách hàng: " + customerPhone);
//             Label lblPaymentMethod = new Label("Phương thức thanh toán: " + paymentMethod);
//             lblPaymentMethod.setStyle("-fx-font-weight: bold;");

//             // Tạo đường kẻ ngăn cách
//             Separator sep1 = new Separator();
//             sep1.setMaxWidth(Double.MAX_VALUE);

//             // Tiêu đề bảng sản phẩm
//             HBox tableHeader = new HBox(10);
//             Label lblProductHeader = new Label("Sản phẩm");
//             lblProductHeader.setPrefWidth(200);
//             lblProductHeader.setStyle("-fx-font-weight: bold;");

//             Label lblQtyHeader = new Label("SL");
//             lblQtyHeader.setPrefWidth(50);
//             lblQtyHeader.setStyle("-fx-font-weight: bold;");

//             Label lblPriceHeader = new Label("Đơn giá");
//             lblPriceHeader.setPrefWidth(100);
//             lblPriceHeader.setStyle("-fx-font-weight: bold;");

//             Label lblWarrantyHeader = new Label("Bảo hành");
//             lblWarrantyHeader.setPrefWidth(100);
//             lblWarrantyHeader.setStyle("-fx-font-weight: bold;");

//             Label lblSubtotalHeader = new Label("Thành tiền");
//             lblSubtotalHeader.setPrefWidth(100);
//             lblSubtotalHeader.setStyle("-fx-font-weight: bold;");

//             tableHeader.getChildren().addAll(lblProductHeader, lblQtyHeader, lblPriceHeader, lblWarrantyHeader, lblSubtotalHeader);

//             // Danh sách sản phẩm
//             VBox productsBox = new VBox(5);
//             double totalWarrantyPrice = 0.0; // Tổng phí bảo hành

//             for (CartItemEmployee item : items) {
//                 if (item == null) continue;

//                 // Dòng sản phẩm
//                 HBox row = new HBox(10);

//                 String productName = item.getProductName();
//                 if (productName == null) productName = "Sản phẩm không tên";

//                 // Tạo VBox để hiển thị tên sản phẩm + bảo hành nếu có
//                 VBox productInfoBox = new VBox(2);
//                 Label lblProduct = new Label(productName);
//                 lblProduct.setPrefWidth(200);
//                 lblProduct.setWrapText(true);
//                 productInfoBox.getChildren().add(lblProduct);

//                 Label lblQty = new Label(String.valueOf(item.getQuantity()));
//                 lblQty.setPrefWidth(50);

//                 Label lblPrice = new Label(String.format("%,.0f", item.getPrice()) + "đ");
//                 lblPrice.setPrefWidth(100);

//                 // Hiển thị thông tin bảo hành
//                 Label lblWarranty;
//                 if (item.hasWarranty()) {
//                     lblWarranty = new Label(item.getWarranty().getWarrantyType());
//                     totalWarrantyPrice += item.getWarranty().getWarrantyPrice();
//                 } else {
//                     lblWarranty = new Label("Không");
//                 }
//                 lblWarranty.setPrefWidth(100);

//                 // Hiển thị tổng giá trị sản phẩm
//                 Label lblSubtotal = new Label(String.format("%,.0f", item.getTotalPrice()) + "đ");
//                 lblSubtotal.setPrefWidth(100);

//                 row.getChildren().addAll(productInfoBox, lblQty, lblPrice, lblWarranty, lblSubtotal);
//                 productsBox.getChildren().add(row);
//             }

//             // Thêm đường kẻ ngăn cách
//             Separator sep2 = new Separator();
//             sep2.setMaxWidth(Double.MAX_VALUE);

//             // Hiển thị tổng phí bảo hành nếu có
//             VBox summaryBox = new VBox(5);

//             if (totalWarrantyPrice > 0) {
//                 HBox warrantyRow = new HBox(10);
//                 warrantyRow.setAlignment(Pos.CENTER_RIGHT);

//                 Label lblWarrantyTotalHeader = new Label("Tổng phí bảo hành:");
//                 Label lblWarrantyValue = new Label(String.format("%,.0f", totalWarrantyPrice) + "đ");
//                 lblWarrantyValue.setStyle("-fx-font-size: 13px;");

//                 warrantyRow.getChildren().addAll(lblWarrantyHeader, lblWarrantyValue);
//                 summaryBox.getChildren().add(warrantyRow);
//             }

//             // Tổng tiền
//             HBox totalRow = new HBox(10);
//             totalRow.setAlignment(Pos.CENTER_RIGHT);

//             Label lblTotalHeader = new Label("Tổng tiền thanh toán:");
//             lblTotalHeader.setStyle("-fx-font-weight: bold;");

//             Label lblTotalValue = new Label(String.format("%,.0f", totalAmount) + "đ");
//             lblTotalValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             totalRow.getChildren().addAll(lblTotalHeader, lblTotalValue);
//             summaryBox.getChildren().add(totalRow);

//             // Thêm thông tin thanh toán chuyển khoản nếu là phương thức chuyển khoản
//             VBox paymentInfoBox = new VBox(10);
//             paymentInfoBox.setAlignment(Pos.CENTER);

//             if ("Chuyển khoản".equals(paymentMethod)) {
//                 // Thêm đường kẻ ngăn cách
//                 Separator sepPayment = new Separator();
//                 sepPayment.setMaxWidth(Double.MAX_VALUE);

//                 Label lblPaymentInfo = new Label("THÔNG TIN CHUYỂN KHOẢN");
//                 lblPaymentInfo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
//                 lblPaymentInfo.setAlignment(Pos.CENTER);
//                 lblPaymentInfo.setMaxWidth(Double.MAX_VALUE);

//                 Label lblBank = new Label("Ngân hàng: TECHCOMBANK");
//                 Label lblAccount = new Label("Số tài khoản: 1903 5552 6789");
//                 Label lblAccountName = new Label("Chủ TK: CÔNG TY COMPUTER STORE");
//                 Label lblContent = new Label("Nội dung CK: " + orderID + " " + customerPhone);

//                 // QR Code cho chuyển khoản
//                 ImageView qrCode = new ImageView();
//                 try {
//                     // Mặc định sử dụng ảnh QR từ resources
//                     InputStream qrIs = getClass().getResourceAsStream("/com/example/stores/images/qr_payment.png");
//                     if (qrIs != null) {
//                         qrCode.setImage(new Image(qrIs));
//                         qrCode.setFitWidth(150);
//                         qrCode.setPreserveRatio(true);
//                     } else {
//                         // QR Code cho chuyển khoản - tạo ảnh trống nếu không tìm thấy
//                         qrCode.setFitWidth(150);
//                         qrCode.setFitHeight(150);
//                         qrCode.setStyle("-fx-background-color: #f0f0f0;");
//                     }
//                 } catch (Exception e) {
//                     LOGGER.log(Level.WARNING, "Không tìm thấy ảnh QR", e);
//                 }

//                 paymentInfoBox.getChildren().addAll(sepPayment, lblPaymentInfo, lblBank, lblAccount, lblAccountName, lblContent, qrCode);
//             }

//             // Thông tin cuối hóa đơn
//             Label lblThankYou = new Label("Cảm ơn quý khách đã mua hàng!");
//             lblThankYou.setAlignment(Pos.CENTER);
//             lblThankYou.setMaxWidth(Double.MAX_VALUE);
//             lblThankYou.setStyle("-fx-font-style: italic; -fx-alignment: center;");

//             Label lblContact = new Label("Hotline: 1800.1234 - Website: www.computerstore.com.vn");
//             lblContact.setAlignment(Pos.CENTER);
//             lblContact.setMaxWidth(Double.MAX_VALUE);
//             lblContact.setStyle("-fx-font-size: 10px; -fx-alignment: center;");

//             // Thêm thông tin chính sách bảo hành
//             Label lblWarrantyPolicy = new Label("Để biết thêm về chính sách bảo hành, vui lòng xem tại website");
//             lblWarrantyPolicy.setAlignment(Pos.CENTER);
//             lblWarrantyPolicy.setMaxWidth(Double.MAX_VALUE);
//             lblWarrantyPolicy.setStyle("-fx-font-size: 10px; -fx-font-style: italic; -fx-alignment: center;");

//             // Thêm tất cả các phần tử vào hóa đơn
//             HBox logoBox = new HBox(10);
//             logoBox.setAlignment(Pos.CENTER);
//             logoBox.getChildren().add(logo);

//             receiptContent.getChildren().addAll(
//                     lblTitle,
//                     logoBox,
//                     lblStoreName,
//                     lblStoreAddress,
//                     lblStorePhone,
//                     new Separator(),
//                     lblOrderID,
//                     lblDateTime,
//                     lblCashier,
//                     lblCustomerName,
//                     lblCustomerPhone,
//                     lblPaymentMethod,
//                     sep1,
//                     tableHeader,
//                     productsBox,
//                     sep2,
//                     summaryBox
//             );

//             // Thêm thông tin thanh toán chuyển khoản nếu có
//             if (!paymentInfoBox.getChildren().isEmpty()) {
//                 receiptContent.getChildren().add(paymentInfoBox);
//             }

//             // Thêm phần kết
//             Separator sepEnd = new Separator();
//             sepEnd.setMaxWidth(Double.MAX_VALUE);

//             receiptContent.getChildren().addAll(
//                     sepEnd,
//                     lblThankYou,
//                     lblContact,
//                     lblWarrantyPolicy
//             );

//             // Định dạng kích thước hóa đơn
//             ScrollPane scrollPane = new ScrollPane(receiptContent);
//             scrollPane.setPrefWidth(550); // Tăng kích thước để hiển thị đủ cột bảo hành
//             scrollPane.setPrefHeight(600);
//             scrollPane.setFitToWidth(true);

//             // Tạo Scene và Stage để hiển thị trước khi in
//             Scene scene = new Scene(scrollPane);
//             Stage printPreviewStage = new Stage();
//             printPreviewStage.setTitle("Xem trước hóa đơn");
//             printPreviewStage.setScene(scene);

//             // Đóng cảnh báo đang in
//             printingAlert.close();

//             // Hiển thị hóa đơn
//             printPreviewStage.show();

//             // Thêm nút in và lưu vào cửa sổ xem trước
//             Button btnPrint = new Button("In");
//             btnPrint.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
//             btnPrint.setOnAction(e -> {
//                 try {
//                     PrinterJob job = PrinterJob.createPrinterJob();
//                     if (job != null) {
//                         boolean success = job.printPage(receiptContent);
//                         if (success) {
//                             job.endJob();
//                             printPreviewStage.close();

//                             Alert printSuccessAlert = new Alert(Alert.AlertType.INFORMATION);
//                             printSuccessAlert.setTitle("In thành công");
//                             printSuccessAlert.setHeaderText("Hóa đơn đã được gửi đến máy in");
//                             printSuccessAlert.setContentText("Vui lòng kiểm tra máy in của bạn.");
//                             printSuccessAlert.showAndWait();
//                         }
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi in hóa đơn", ex);
//                     showErrorAlert("Lỗi khi in hóa đơn: " + ex.getMessage());
//                 }
//             });

//             // Nút lưu PDF (giả định)
//             Button btnSave = new Button("Lưu PDF");
//             btnSave.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
//             btnSave.setOnAction(e -> {
//                 try {
//                     Alert saveAlert = new Alert(Alert.AlertType.INFORMATION);
//                     saveAlert.setTitle("Lưu PDF");
//                     saveAlert.setHeaderText("Hóa đơn đã được lưu");
//                     saveAlert.setContentText("Hóa đơn đã được lưu vào thư mục Documents.");
//                     saveAlert.showAndWait();
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi lưu PDF", ex);
//                     showErrorAlert("Lỗi khi lưu PDF: " + ex.getMessage());
//                 }
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnClose.setOnAction(e -> printPreviewStage.close());

//             HBox buttonBox = new HBox(10, btnPrint, btnSave, btnClose);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.setPadding(new Insets(10));

//             BorderPane borderPane = new BorderPane();
//             borderPane.setCenter(scrollPane);
//             borderPane.setBottom(buttonBox);

//             scene.setRoot(borderPane);

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi in hóa đơn", e);
//             Alert errorAlert = new Alert(Alert.AlertType.ERROR);
//             errorAlert.setTitle("Lỗi in hóa đơn");
//             errorAlert.setHeaderText("Không thể in hóa đơn");
//             errorAlert.setContentText("Chi tiết lỗi: " + e.getMessage());
//             errorAlert.showAndWait();
//         }
//     }

//     /**
//      * Thêm sản phẩm vào giỏ hàng với thông tin bảo hành
//      */
//     private void addToCartWithWarranty(CartItemEmployee item) {
//         if (item == null) {
//             LOGGER.warning("Lỗi: CartItemEmployee là null");
//             return;
//         }

//         // Tìm sản phẩm trong database để kiểm tra tồn kho
//         Product product = findProductById(item.getProductID());
//         if (product == null) {
//             AlertUtil.showWarning("Lỗi", "Không tìm thấy thông tin sản phẩm");
//             return;
//         }

//         // Kiểm tra số lượng tồn kho trước khi thêm
//         if (product.getQuantity() <= 0) {
//             AlertUtil.showWarning("Hết hàng", "Sản phẩm đã hết hàng!");
//             return;
//         }

//         // Tìm kiếm sản phẩm trong giỏ hàng với CÙNG loại bảo hành
//         boolean existingFound = false;
//         for (CartItemEmployee cartItem : cartItems) {
//             if (cartItem.getProductID().equals(item.getProductID())) {
//                 // Phải cùng sản phẩm và cùng loại bảo hành
//                 if (cartItem.hasWarranty() == item.hasWarranty() &&
//                         (!cartItem.hasWarranty() ||
//                                 cartItem.getWarranty().getWarrantyType().equals(item.getWarranty().getWarrantyType()))) {

//                     if (cartItem.getQuantity() < product.getQuantity()) {
//                         // Cập nhật số lượng nếu còn hàng
//                         cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
//                         existingFound = true;
//                         LOGGER.info("Đã tăng số lượng " + cartItem.getProductName() +
//                                 " (BH: " + (cartItem.hasWarranty() ? cartItem.getWarranty().getWarrantyType() : "Không") +
//                                 ") lên " + cartItem.getQuantity());
//                     } else {
//                         AlertUtil.showWarning("Số lượng tối đa",
//                                 "Không thể thêm nữa, số lượng trong kho chỉ còn " + product.getQuantity());
//                     }
//                     break;
//                 }
//             }
//         }

//         // Nếu không tìm thấy sản phẩm đã có trong giỏ với cùng loại bảo hành
//         if (!existingFound) {
//             cartItems.add(item);
//             LOGGER.info("Đã thêm " + item.getProductName() +
//                     " (BH: " + (item.hasWarranty() ? item.getWarranty().getWarrantyType() : "Không") +
//                     ") vào giỏ hàng");
//         }

//         // Cập nhật hiển thị giỏ hàng
//         updateCartDisplay();
//     }

//     // Tìm sản phẩm theo ID
//     private Product findProductById(String productID) {
//         if (productID == null || products == null) {
//             return null;
//         }

//         for (Product product : products) {
//             if (product.getProductID().equals(productID)) {
//                 return product;
//             }
//         }

//         return null;
//     }

//     // Sửa lại phần hiển thị dialog chi tiết sản phẩm trong PosOverviewController
//     private void showProductDetails(Product product) {
//         try {
//             if (product == null) {
//                 LOGGER.warning("Lỗi: Product object là null");
//                 return;
//             }

//             Stage detailStage = new Stage();
//             detailStage.initModality(Modality.APPLICATION_MODAL);
//             detailStage.setTitle("Chi tiết sản phẩm");

//             VBox layout = new VBox(10);
//             layout.setPadding(new Insets(20));
//             layout.setStyle("-fx-background-color: white;");

//             // Hiển thị ảnh sản phẩm (giữ nguyên code cũ)
//             final ImageView productImage = new ImageView();
//             productImage.setFitWidth(200);
//             productImage.setFitHeight(150);
//             productImage.setPreserveRatio(true);

//             // Tải ảnh sản phẩm (giữ nguyên code cũ)
//             String imagePath = product.getImagePath();
//             if (imagePath != null && !imagePath.startsWith("/")) {
//                 imagePath = "/com/example/stores/images/" + imagePath;
//             } else if (imagePath == null) {
//                 imagePath = "/com/example/stores/images/no_image.png";
//             }

//             try {
//                 Image image = new Image(getClass().getResourceAsStream(imagePath));
//                 productImage.setImage(image);
//             } catch (Exception e) {
//                 productImage.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/no_image.png")));
//                 LOGGER.warning("Không tải được ảnh chi tiết sản phẩm: " + e.getMessage());
//             }

//             final HBox imageBox = new HBox();
//             imageBox.setAlignment(Pos.CENTER);
//             imageBox.getChildren().add(productImage);

//             // Tên sản phẩm
//             String productName = (product.getProductName() != null) ? product.getProductName() : "Sản phẩm không có tên";
//             Label lblName = new Label(productName);
//             lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
//             lblName.setWrapText(true);

//             // Giá sản phẩm
//             Label lblPrice = new Label(String.format("Giá: %,d₫", (long)product.getPrice()));
//             lblPrice.setStyle("-fx-text-fill: #e91e63; -fx-font-weight: bold; -fx-font-size: 16px;");

//             // Thông tin cơ bản (giữ nguyên code cũ)
//             VBox specsBox = new VBox(5);
//             specsBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");

//             if (product.getCategoryID() != null) {
//                 Label lblCategory = new Label("Danh mục: " + getCategoryName(product.getCategoryID()));
//                 specsBox.getChildren().add(lblCategory);
//             }

//             Label lblStock = new Label("Tồn kho: " + product.getQuantity() + " sản phẩm");
//             specsBox.getChildren().add(lblStock);

//             String status = product.getStatus();
//             Label lblStatus = new Label("Trạng thái: " + (status != null ? status : "Không xác định"));
//             lblStatus.setStyle(status != null && status.equals("Còn hàng") ?
//                     "-fx-text-fill: #4caf50; -fx-font-weight: bold;" :
//                     "-fx-text-fill: #f44336; -fx-font-weight: bold;");
//             specsBox.getChildren().add(lblStatus);

//             // PHẦN BẢO HÀNH - CẬP NHẬT CHỈ CÒN 2 LOẠI: THƯỜNG VÀ VÀNG
//             VBox warrantyBox = new VBox(5);
//             warrantyBox.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 10; -fx-background-radius: 5;");

//             Label lblWarrantyTitle = new Label("Lựa chọn bảo hành:");
//             lblWarrantyTitle.setStyle("-fx-font-weight: bold;");
//             warrantyBox.getChildren().add(lblWarrantyTitle);

//             // ComboBox để chọn bảo hành
//             ComboBox<String> cbWarranty = new ComboBox<>();

//             // Kiểm tra sản phẩm có đủ điều kiện bảo hành thường không
//             boolean isEligibleForStdWarranty = WarrantyCalculator.isEligibleForStandardWarranty(product);

//             Label lblWarrantyInfo = new Label();

//             // Hiển thị các lựa chọn bảo hành dựa trên điều kiện
//             if (isEligibleForStdWarranty) {
//                 cbWarranty.getItems().addAll("Thường", "Vàng");
//                 cbWarranty.setValue("Thường");

//                 // Miêu tả bảo hành
//                 lblWarrantyInfo.setText("✅ Sản phẩm được bảo hành Thường miễn phí 12 tháng");
//                 lblWarrantyInfo.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 12px;");
//             } else {
//                 cbWarranty.getItems().add("Không");
//                 cbWarranty.setValue("Không");

//                 // Miêu tả không đủ điều kiện
//                 lblWarrantyInfo.setText("❌ Sản phẩm dưới 500.000đ không được bảo hành");
//                 lblWarrantyInfo.setStyle("-fx-text-fill: #f44336; -fx-font-size: 12px;");
//             }

//             warrantyBox.getChildren().addAll(cbWarranty, lblWarrantyInfo);

//             // Hiển thị phí bảo hành
//             Label lblWarrantyPrice = new Label("Phí bảo hành: 0đ");
//             warrantyBox.getChildren().add(lblWarrantyPrice);

//             // Hiển thị tổng tiền kèm bảo hành
//             Label lblTotalWithWarranty = new Label("Tổng tiền: " + String.format("%,d₫", (long)product.getPrice()));
//             lblTotalWithWarranty.setStyle("-fx-font-weight: bold;");
//             warrantyBox.getChildren().add(lblTotalWithWarranty);

//             // Cập nhật giá bảo hành khi thay đổi loại bảo hành
//             cbWarranty.setOnAction(e -> {
//                 String selectedType = cbWarranty.getValue();

//                 if ("Không".equals(selectedType) || "Thường".equals(selectedType)) {
//                     lblWarrantyPrice.setText("Phí bảo hành: 0đ");
//                     lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,d₫", (long)product.getPrice()));

//                     if ("Thường".equals(selectedType)) {
//                         lblWarrantyInfo.setText("✅ Bảo hành Thường miễn phí 12 tháng");
//                         lblWarrantyInfo.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 12px;");
//                     } else {
//                         lblWarrantyInfo.setText("❌ Không bảo hành");
//                         lblWarrantyInfo.setStyle("-fx-text-fill: #f44336; -fx-font-size: 12px;");
//                     }
//                     return;
//                 }

//                 // Tính phí bảo hành Vàng (10% giá sản phẩm)
//                 double warrantyFee = product.getPrice() * 0.1;
//                 lblWarrantyPrice.setText("Phí bảo hành: " + String.format("%,d₫", (long)warrantyFee));

//                 // Cập nhật tổng tiền
//                 double totalPrice = product.getPrice() + warrantyFee;
//                 lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,d₫", (long)totalPrice));

//                 // Thêm giải thích về bảo hành Vàng
//                 lblWarrantyInfo.setText("✨ Bảo hành Vàng 24 tháng, 1 đổi 1 trong 24 tháng");
//                 lblWarrantyInfo.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 12px; -fx-font-weight: bold;");
//             });

//             // Mô tả sản phẩm và nút thêm vào giỏ (giữ nguyên code)
//             Label lblDescTitle = new Label("Mô tả sản phẩm:");
//             lblDescTitle.setStyle("-fx-font-weight: bold;");

//             String description = (product.getDescription() != null) ? product.getDescription() : "Không có thông tin";
//             TextArea txtDescription = new TextArea(description);
//             txtDescription.setWrapText(true);
//             txtDescription.setEditable(false);
//             txtDescription.setPrefHeight(100);

//             // Nút thêm vào giỏ
//             Button btnAddToCart = new Button("Thêm vào giỏ");
//             btnAddToCart.setPrefWidth(200);
//             btnAddToCart.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnAddToCart.setOnAction(e -> {
//                 try {
//                     // Lấy loại bảo hành đã chọn
//                     String selectedWarranty = cbWarranty.getValue();

//                     // Tạo đối tượng CartItemEmployee mới
//                     CartItemEmployee newItem = new CartItemEmployee(
//                             product.getProductID(),
//                             product.getProductName(),
//                             product.getPrice(),
//                             1,
//                             product.getImagePath(),
//                             employeeId,
//                             currentUser != null ? currentUser : "unknown",
//                             product.getCategoryID()
//                     );

//                     // Tạo bảo hành nếu không phải là "Không" bảo hành
//                     if ("Thường".equals(selectedWarranty) || "Vàng".equals(selectedWarranty)) {
//                         // Tạo bảo hành và gán vào sản phẩm
//                         Warranty warranty = WarrantyCalculator.createWarranty(product, selectedWarranty);
//                         newItem.setWarranty(warranty);
//                     }

//                     // Thêm vào giỏ hàng
//                     addToCartWithWarranty(newItem);

//                     detailStage.close(); // Đóng cửa sổ chi tiết
//                     AlertUtil.showInformation("Thành công", "Đã thêm sản phẩm vào giỏ hàng!");
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi thêm sản phẩm vào giỏ hàng", ex);
//                     AlertUtil.showError("Lỗi", "Không thể thêm sản phẩm vào giỏ hàng: " + ex.getMessage());
//                 }
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setPrefWidth(100);
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnClose.setOnAction(e -> detailStage.close());

//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.getChildren().addAll(btnAddToCart, btnClose);

//             // Thêm tất cả vào layout
//             layout.getChildren().addAll(
//                     imageBox,
//                     lblName,
//                     lblPrice,
//                     new Separator(),
//                     specsBox,
//                     new Separator(),
//                     warrantyBox,
//                     new Separator(),
//                     lblDescTitle,
//                     txtDescription,
//                     buttonBox
//             );

//             Scene scene = new Scene(layout, 400, 800);
//             detailStage.setScene(scene);
//             detailStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị chi tiết sản phẩm", e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị chi tiết sản phẩm: " + e.getMessage());
//         }
//     }

//     // Tạo dòng hiển thị cho sản phẩm trong giỏ hàng
//     private HBox createCartItemRow(CartItemEmployee item) {
//         HBox row = new HBox();
//         row.setSpacing(10);
//         row.setPadding(new Insets(5));
//         row.setAlignment(Pos.CENTER_LEFT);

//         // Tên sản phẩm với thông tin bảo hành
//         VBox productInfoBox = new VBox(2);
//         Label lblName = new Label(item.getProductName());
//         lblName.setStyle("-fx-font-weight: bold;");
//         productInfoBox.getChildren().add(lblName);

//         // Thêm thông tin bảo hành nếu có
//         if (item.hasWarranty()) {
//             Label lblWarranty = new Label("BH: " + item.getWarranty().getWarrantyType());
//             lblWarranty.setStyle("-fx-font-size: 11px; -fx-text-fill: #2196F3;");
//             productInfoBox.getChildren().add(lblWarranty);
//         }

//         productInfoBox.setPrefWidth(200);

//         // Số lượng với nút tăng/giảm
//         HBox quantityBox = new HBox(5);
//         quantityBox.setAlignment(Pos.CENTER);

//         Button btnMinus = new Button("-");
//         btnMinus.setMinWidth(30);
//         btnMinus.setOnAction(e -> decreaseQuantity(item));

//         Label lblQuantity = new Label(String.valueOf(item.getQuantity()));
//         lblQuantity.setAlignment(Pos.CENTER);
//         lblQuantity.setMinWidth(30);
//         lblQuantity.setStyle("-fx-font-weight: bold;");

//         Button btnPlus = new Button("+");
//         btnPlus.setMinWidth(30);
//         btnPlus.setOnAction(e -> increaseQuantity(item));

//         quantityBox.getChildren().addAll(btnMinus, lblQuantity, btnPlus);
//         quantityBox.setPrefWidth(120);

//         // Đơn giá
//         Label lblPrice = new Label(String.format("%,.0f", item.getPrice()) + "đ");
//         lblPrice.setPrefWidth(100);
//         lblPrice.setAlignment(Pos.CENTER_RIGHT);

//         // Bảo hành
//         Label lblWarranty = new Label(item.hasWarranty() ? item.getWarranty().getWarrantyType() : "Không");
//         lblWarranty.setPrefWidth(80);
//         lblWarranty.setAlignment(Pos.CENTER);
//         if (item.hasWarranty()) {
//             lblWarranty.setStyle("-fx-text-fill: #4CAF50;");
//         }

//         // Tổng tiền
//         Label lblTotal = new Label(String.format("%,.0f", item.getTotalPrice()) + "đ");
//         lblTotal.setPrefWidth(100);
//         lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #e91e63;");
//         lblTotal.setAlignment(Pos.CENTER_RIGHT);

//         // Nút xóa
//         Button btnRemove = new Button("✖");
//         btnRemove.setStyle("-fx-background-color: transparent; -fx-text-fill: #f44336; -fx-font-weight: bold;");
//         btnRemove.setOnAction(e -> removeFromCart(item));

//         // Thêm tất cả vào dòng
//         row.getChildren().addAll(productInfoBox, quantityBox, lblPrice, lblWarranty, lblTotal, btnRemove);

//         return row;
//     }

//     // Tăng số lượng sản phẩm trong giỏ hàng
//     private void increaseQuantity(CartItemEmployee item) {
//         if (item == null) return;

//         Product product = findProductById(item.getProductID());
//         if (product == null) {
//             AlertUtil.showWarning("Lỗi", "Không tìm thấy thông tin sản phẩm");
//             return;
//         }

//         // Kiểm tra số lượng tồn kho
//         if (item.getQuantity() < product.getQuantity()) {
//             item.setQuantity(item.getQuantity() + 1);
//             updateCartDisplay();
//         } else {
//             AlertUtil.showWarning("Số lượng tối đa",
//                     "Không thể thêm nữa, số lượng trong kho chỉ còn " + product.getQuantity());
//         }
//     }

//     // Giảm số lượng sản phẩm trong giỏ hàng
//     private void decreaseQuantity(CartItemEmployee item) {
//         if (item == null) return;

//         if (item.getQuantity() > 1) {
//             item.setQuantity(item.getQuantity() - 1);
//             updateCartDisplay();
//         } else {
//             // Nếu số lượng là 1, hỏi xem có muốn xóa không
//             Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//             alert.setTitle("Xóa sản phẩm");
//             alert.setHeaderText("Xác nhận xóa");
//             alert.setContentText("Bạn có muốn xóa sản phẩm này khỏi giỏ hàng?");

//             Optional<ButtonType> result = alert.showAndWait();
//             if (result.isPresent() && result.get() == ButtonType.OK) {
//                 removeFromCart(item);
//             }
//         }
//     }

//     // Xóa sản phẩm khỏi giỏ hàng
//     private void removeFromCart(CartItemEmployee item) {
//         if (item != null) {
//             cartItems.remove(item);
//             updateCartDisplay();
//         }
//     }

//     // Cập nhật hiển thị giỏ hàng
//     private void updateCartDisplay() {
//         // Cập nhật tổng tiền
//         updateTotal();

//         // Cập nhật TableView
//         cartTable.refresh();
//     }

//     // Cập nhật tổng tiền giỏ hàng
//     private void updateTotal() {
//         double total = calculateTotalAmount();
//         if (lblTotal != null) {
//             lblTotal.setText("Tổng tiền: " + String.format("%,.0f", total) + "đ");
//         }
//     }

//     // Tính tổng tiền giỏ hàng
//     private double calculateTotalAmount() {
//         double total = 0.0;
//         for (CartItemEmployee item : cartItems) {
//             if (item != null) {
//                 total += item.getTotalPrice();
//             }
//         }
//         return total;
//     }

//     // Xóa toàn bộ giỏ hàng
//     private void clearCart() {
//         cartItems.clear();
//         updateCartDisplay();
//         LOGGER.info("Đã xóa toàn bộ giỏ hàng");
//     }

//     // Lấy tên danh mục từ ID
//     private String getCategoryName(String categoryId) {
//         if (categoryId == null) return "Không xác định";

//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.warning("Không thể kết nối đến database");
//                 return "Không xác định";
//             }

//             // FIX LỖI: Sửa tên bảng từ Category thành Categories và category_name thành categoryName
//             String query = "SELECT categoryName FROM Categories WHERE categoryID = ?";
//             stmt = conn.prepareStatement(query);
//             stmt.setString(1, categoryId);
//             rs = stmt.executeQuery();

//             if (rs.next()) {
//                 return rs.getString("categoryName");
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.WARNING, "Lỗi SQL khi lấy tên danh mục: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.WARNING, "Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.WARNING, "Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }

//         return "Không xác định";
//     }
//     // Lấy danh sách các danh mục phân biệt
//     private List<String> getDistinctCategories() {
//         List<String> categories = new ArrayList<>();
//         categories.add("Tất cả"); // Luôn có tùy chọn "Tất cả"

//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.severe("💀 QUẠC!!! Không thể kết nối đến database");
//                 return categories;
//             }

//             // FIX LỖI: Sửa tên bảng từ Category thành Categories
//             // Sửa tên cột từ category_name thành categoryName - match với schema thực tế
//             String query = "SELECT DISTINCT categoryID, categoryName FROM Categories ORDER BY categoryName";
//             stmt = conn.prepareStatement(query);
//             rs = stmt.executeQuery();

//             int categoryCount = 0;

//             while (rs.next()) {
//                 String categoryName = rs.getString("categoryName");
//                 if (categoryName != null && !categoryName.isEmpty()) {
//                     categories.add(categoryName);
//                     categoryCount++;
//                 }
//             }

//             LOGGER.info("✨✨✨ Đã tìm thấy " + categoryCount + " danh mục từ database slayyy");

//             if (categoryCount == 0) {
//                 LOGGER.warning("🚨🚨 SKSKSK EM hong tìm thấy danh mục nào trong database luôn á!!!");
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi SQL khi lấy danh mục: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "😭😭 Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }

//         return categories;
//     }

//     // Tải dữ liệu sản phẩm từ database
//     // Em sẽ sửa lại hàm loadProductsFromDatabase để FIX LỖI NGAY LAPPPPP
//     private void loadProductsFromDatabase() {
//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.severe("❌❌❌ Không thể kết nối đến database");
//                 return;
//             }

//             // FIX LỖI: Sửa lại câu query SQL - CHÚ Ý KHÔNG DÙNG WHERE NỮA
//             // Trước đây chỉ lấy sản phẩm có status = "Còn hàng" hoặc "Active"
//             // => Sửa lại để lấy TẤT CẢ sản phẩm, sort theo quantity để hiển thị sản phẩm còn hàng lên trên
//             String query = "SELECT * FROM Products ORDER BY quantity DESC";
//             stmt = conn.prepareStatement(query);
//             rs = stmt.executeQuery();

//             products.clear(); // Xóa danh sách cũ

//             int productCount = 0; // Đếm số sản phẩm load được

//             while (rs.next()) {
//                 Product product = new Product();
//                 product.setProductID(rs.getString("productID"));
//                 product.setProductName(rs.getString("productName"));
//                 product.setPrice(rs.getDouble("price"));
//                 product.setQuantity(rs.getInt("quantity"));
//                 product.setDescription(rs.getString("description"));
//                 product.setStatus(rs.getString("status"));
//                 product.setCategoryID(rs.getString("categoryID"));

//                 // Xử lý đường dẫn hình ảnh
//                 String imagePath = rs.getString("imagePath");
//                 if (imagePath != null && !imagePath.startsWith("/")) {
//                     imagePath = "/com/example/stores/images/" + imagePath;
//                 }
//                 product.setImagePath(imagePath);

//                 products.add(product);
//                 productCount++;
//             }

//             LOGGER.info("✅✅✅ Đã load được " + productCount + " sản phẩm từ database");

//             if (productCount == 0) {
//                 // Debug thêm thông tin nếu không load được sản phẩm nào
//                 LOGGER.warning("⚠️⚠️⚠️ Không tìm thấy sản phẩm nào trong database!!!");
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi SQL khi lấy dữ liệu sản phẩm: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }
//     }

//     // Làm mới danh sách sản phẩm trên giao diện
//     private void refreshProductList() {
//         if (productFlowPane == null) {
//             LOGGER.warning("productFlowPane chưa được khởi tạo");
//             return;
//         }

//         // Xóa tất cả sản phẩm hiện tại
//         productFlowPane.getChildren().clear();

//         if (products.isEmpty()) {
//             Label lblEmpty = new Label("Không có sản phẩm nào.");
//             lblEmpty.setStyle("-fx-font-style: italic;");
//             productFlowPane.getChildren().add(lblEmpty);
//             return;
//         }

//         // Lọc sản phẩm theo điều kiện
//         List<Product> filteredProducts = filterProducts();

//         // Sắp xếp sản phẩm theo điều kiện
//         sortProducts(filteredProducts);

//         // Lưu danh sách hiện tại để sử dụng sau này
//         currentFilteredProducts = new ArrayList<>(filteredProducts);

//         // Giới hạn số lượng sản phẩm hiển thị
//         List<Product> displayProducts = filteredProducts.stream()
//                 .limit(productLimit)
//                 .collect(Collectors.toList());

//         // Hiển thị sản phẩm
//         for (Product product : displayProducts) {
//             VBox productBox = createProductBox(product);
//             productFlowPane.getChildren().add(productBox);
//         }

//         // Thêm nút "Xem thêm" nếu còn sản phẩm
//         if (filteredProducts.size() > productLimit) {
//             Button btnLoadMore = createLoadMoreButton();
//             productFlowPane.getChildren().add(btnLoadMore);
//         }
//     }

//     // Lọc sản phẩm theo các điều kiện
//     private List<Product> filterProducts() {
//         List<Product> filteredList = new ArrayList<>(products);

//         // Lọc theo danh mục
//         if (cbCategory != null && cbCategory.getValue() != null && !cbCategory.getValue().equals("Tất cả")) {
//             String selectedCategory = cbCategory.getValue();
//             filteredList = filteredList.stream()
//                     .filter(p -> {
//                         String categoryName = getCategoryName(p.getCategoryID());
//                         return categoryName.equals(selectedCategory);
//                     })
//                     .collect(Collectors.toList());
//         }

//         // Lọc theo từ khóa tìm kiếm
//         if (txtSearch != null && txtSearch.getText() != null && !txtSearch.getText().trim().isEmpty()) {
//             String keyword = txtSearch.getText().trim().toLowerCase();
//             filteredList = filteredList.stream()
//                     .filter(p -> p.getProductName() != null && p.getProductName().toLowerCase().contains(keyword))
//                     .collect(Collectors.toList());
//         }

//         return filteredList;
//     }

//     // Sắp xếp sản phẩm theo điều kiện đã chọn
//     private void sortProducts(List<Product> list) {
//         if (cbSort == null || cbSort.getValue() == null) return;

//         String sortOption = cbSort.getValue();
//         switch (sortOption) {
//             case "Tên A-Z":
//                 // FIX LỖI: Thêm kiểu Product vào lambda để compiler biết đây là Product object
//                 list.sort(Comparator.comparing((Product p) -> p.getProductName() != null ? p.getProductName() : ""));
//                 break;
//             case "Tên Z-A":
//                 // FIX LỖI: Thêm kiểu Product vào lambda tương tự
//                 list.sort(Comparator.comparing((Product p) -> p.getProductName() != null ? p.getProductName() : "").reversed());
//                 break;
//             case "Giá thấp đến cao":
//                 list.sort(Comparator.comparing(Product::getPrice));
//                 break;
//             case "Giá cao đến thấp":
//                 list.sort(Comparator.comparing(Product::getPrice).reversed());
//                 break;
//             // Mặc định không sắp xếp (giữ nguyên thứ tự)
//         }
//     }

//     // Tạo box hiển thị sản phẩm
//     private VBox createProductBox(Product product) {
//         VBox box = new VBox(8); // Khoảng cách giữa các thành phần
//         box.setPrefWidth(160);
//         box.setPrefHeight(260);
//         box.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: white; -fx-padding: 10;");

//         // Tạo hiệu ứng hover
//         box.setOnMouseEntered(e -> {
//             box.setStyle("-fx-border-color: #2196F3; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f5f5f5; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.4), 10, 0, 0, 0);");
//             box.setCursor(Cursor.HAND);
//         });

//         box.setOnMouseExited(e -> {
//             box.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: white; -fx-padding: 10;");
//         });

//         // Xử lý sự kiện click để xem chi tiết sản phẩm
//         box.setOnMouseClicked(e -> showProductDetails(product));

//         // Hiển thị hình ảnh sản phẩm
//         ImageView imageView = new ImageView();
//         imageView.setFitWidth(140);
//         imageView.setFitHeight(105);
//         imageView.setPreserveRatio(true);

//         String imagePath = product.getImagePath();
//         if (imagePath == null) {
//             imagePath = "/com/example/stores/images/no_image.png";
//         }

//         try {
//             Image image = new Image(getClass().getResourceAsStream(imagePath));
//             imageView.setImage(image);
//         } catch (Exception e) {
//             try {
//                 Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/stores/images/no_image.png"));
//                 imageView.setImage(defaultImage);
//             } catch (Exception ex) {
//                 LOGGER.warning("Không tải được ảnh sản phẩm: " + ex.getMessage());
//             }
//         }

//         // Hiển thị tên sản phẩm
//         String productName = product.getProductName();
//         if (productName == null) productName = "Sản phẩm không tên";
//         if (productName.length() > 40) {
//             productName = productName.substring(0, 37) + "...";
//         }

//         Label nameLabel = new Label(productName);
//         nameLabel.setWrapText(true);
//         nameLabel.setPrefHeight(40); // Chiều cao cố định cho tên sản phẩm
//         nameLabel.setStyle("-fx-font-weight: bold;");

//         // Hiển thị giá
//         Label priceLabel = new Label("Giá: " + String.format("%,d", (long) product.getPrice()) + "đ");
//         priceLabel.setStyle("-fx-text-fill: #e91e63; -fx-font-weight: bold;");

//         // Hiển thị số lượng
//         Label stockLabel = new Label("Kho: " + product.getQuantity());

//         // Nút thêm vào giỏ
//         Button addButton = new Button("Thêm vào giỏ");
//         addButton.setPrefWidth(Double.MAX_VALUE);
//         addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

//         // Hiệu ứng hover cho nút
//         addButton.setOnMouseEntered(e ->
//                 addButton.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white; -fx-font-weight: bold;")
//         );

//         addButton.setOnMouseExited(e ->
//                 addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;")
//         );

//         // Sự kiện thêm vào giỏ
//         addButton.setOnAction(e -> {
//             try {
//                 // Kiểm tra số lượng tồn kho
//                 if (product.getQuantity() <= 0) {
//                     AlertUtil.showWarning("Hết hàng", "Sản phẩm đã hết hàng!");
//                     return;
//                 }

//                 // Tạo đối tượng CartItemEmployee
//                 CartItemEmployee item = new CartItemEmployee(
//                         product.getProductID(),
//                         product.getProductName(),
//                         product.getPrice(),
//                         1,
//                         product.getImagePath(),
//                         employeeId,
//                         currentUser != null ? currentUser : "unknown",
//                         product.getCategoryID()
//                 );

//                 // Kiểm tra sản phẩm có đủ điều kiện bảo hành thường không
//                 // Nếu có, thêm bảo hành thường mặc định
//                 if (WarrantyCalculator.isEligibleForStandardWarranty(product)) {
//                     Warranty warranty = WarrantyCalculator.createWarranty(product, "Thường");
//                     item.setWarranty(warranty);
//                 }

//                 // Thêm vào giỏ hàng
//                 addToCartWithWarranty(item);

//             } catch (Exception ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi khi thêm sản phẩm vào giỏ hàng", ex);
//                 AlertUtil.showError("Lỗi", "Không thể thêm sản phẩm vào giỏ hàng");
//             }
//         });

//         // Thêm tất cả vào box
//         VBox imageContainer = new VBox(imageView);
//         imageContainer.setAlignment(Pos.CENTER);

//         box.getChildren().addAll(
//                 imageContainer,
//                 nameLabel,
//                 priceLabel,
//                 stockLabel,
//                 addButton
//         );

//         return box;
//     }
// }package com.example.stores.controller;

// import com.example.stores.model.OrderHistory;
// import com.example.stores.model.OrderDetail;
// import com.example.stores.service.OrderHistoryServiceE;
// import javafx.scene.control.cell.PropertyValueFactory;
// import javafx.scene.control.CheckBox;

// import java.io.IOException;
// import java.io.InputStream;
// import java.net.URL;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.sql.Statement;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.List;
// import java.util.Optional;
// import java.util.logging.Level;
// import java.util.logging.Logger;
// import java.util.stream.Collectors;

// import com.example.stores.util.AlertUtil; // Chú ý: đây là AlertUtil (không có s)
// import com.example.stores.util.WarrantyCalculator;

// import com.example.stores.model.Customer;
// import com.example.stores.service.CustomerServiceE;

// import javafx.scene.control.RadioButton;
// import javafx.scene.control.ToggleGroup;
// import javafx.scene.layout.BorderPane;
// import javafx.scene.layout.GridPane;
// import javafx.scene.control.ScrollPane;
// import javafx.beans.property.SimpleStringProperty;
// import javafx.beans.property.SimpleIntegerProperty;
// import javafx.beans.property.SimpleDoubleProperty;

// import javafx.scene.layout.*;
// import javafx.geometry.Pos;
// import javafx.scene.control.Label;
// import javafx.scene.control.Button;
// import javafx.scene.image.Image;
// import javafx.scene.image.ImageView;
// import javafx.geometry.Insets;

// import javafx.collections.ObservableList;
// import com.example.stores.config.DBConfig;
// import com.example.stores.model.CartItemEmployee;
// import com.example.stores.model.Product;
// import com.example.stores.model.Employee;
// import com.example.stores.model.Warranty; // Thêm import cho Warranty

// import com.example.stores.model.Order;

// import javafx.scene.shape.Circle;
// import javafx.scene.Cursor;
// import javafx.collections.FXCollections;
// import javafx.fxml.FXML;
// import javafx.fxml.FXMLLoader;
// import javafx.print.PrinterJob;
// import javafx.scene.Parent;
// import javafx.scene.Scene;
// import javafx.scene.control.Alert;
// import javafx.scene.control.ButtonType;
// import javafx.scene.control.ComboBox;
// import javafx.scene.control.Separator;
// import javafx.scene.control.TableCell;
// import javafx.scene.control.TableColumn;
// import javafx.scene.control.TableView;
// import javafx.scene.control.TextArea;
// import javafx.scene.control.TextField;
// import javafx.scene.layout.VBox;
// import javafx.stage.Modality;
// import javafx.stage.Stage;

// public class PosOverviewControllerE {
//     private static final Logger LOGGER = Logger.getLogger(PosOverviewControllerE.class.getName());

//     @FXML private FlowPane productFlowPane;
//     @FXML private TableView<CartItemEmployee> cartTable;
//     @FXML private TableColumn<CartItemEmployee, String> colCartName;
//     @FXML private TableColumn<CartItemEmployee, Integer> colCartQty;
//     @FXML private TableColumn<CartItemEmployee, Double> colCartPrice;
//     @FXML private TableColumn<CartItemEmployee, Double> colCartTotal;
//     @FXML private TableColumn<CartItemEmployee, String> colCartWarranty; // Thêm khai báo biến cho cột bảo hành
//     @FXML private Label lblTotal;
//     // Cập nhật ComboBox lọc theo DB mới (bỏ RAM/CPU, giữ lại category)
//     @FXML private ComboBox<String> cbCategory;
//     @FXML private ComboBox<String> cbSort; // Thêm ComboBox sắp xếp
//     @FXML private TextField txtSearch;
//     @FXML private Button btnFilter, btnCheckout;
//     @FXML private VBox cartItemsContainer; // Container cho các item trong giỏ hàng

//     private int productLimit = 20; // Số sản phẩm hiển thị ban đầu
//     private List<Product> currentFilteredProducts = new ArrayList<>();

//     private ObservableList<Product> products = FXCollections.observableArrayList();
//     private ObservableList<CartItemEmployee> cartItems = FXCollections.observableArrayList();
//     private TableColumn<CartItemEmployee, Void> colCartAction; // Cột chứa nút xóa

//     private int employeeId;

//     /**
//      * Thêm sản phẩm vào giỏ hàng - Method công khai cho ProductDetailController gọi
//      * @param item Sản phẩm cần thêm vào giỏ
//      */
//     public void addToCart(CartItemEmployee item) {
//         // Gọi đến phương thức addToCartWithWarranty đã có sẵn
//         addToCartWithWarranty(item);
//         LOGGER.info("✅ Đã thêm sản phẩm " + item.getProductName() + " vào giỏ hàng từ ProductDetailController");
//     }

//     /**
//      * Lấy tên người dùng hiện tại
//      * @return tên đăng nhập người dùng hiện tại
//      */
//     public String getCurrentUser() {
//         return this.currentUser;
//     }

//     // Thêm biến để lưu lịch sử đơn hàng trong session
//     private List<OrderSummary> orderHistory = new ArrayList<>();

//     // Thêm vào class PosOverviewController
//     private void addEmployeeInfoButton() {
//         try {
//             if (currentEmployee == null || btnCheckout == null || btnCheckout.getParent() == null ||
//                     !(btnCheckout.getParent().getParent() instanceof BorderPane)) {
//                 LOGGER.warning("Không thể thêm nút thông tin nhân viên: currentEmployee hoặc btnCheckout null");
//                 return;
//             }

//             BorderPane mainLayout = (BorderPane) btnCheckout.getParent().getParent();
//             if (mainLayout.getTop() instanceof HBox) {
//                 HBox topBar = (HBox) mainLayout.getTop();

//                 Button btnEmployeeInfo = new Button("THÔNG TIN NV");
//                 btnEmployeeInfo.setStyle("-fx-background-color: #FF4081; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12;");

//                 btnEmployeeInfo.setOnMouseEntered(e -> btnEmployeeInfo.setStyle("-fx-background-color: #F50057; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 0);"));
//                 btnEmployeeInfo.setOnMouseExited(e -> btnEmployeeInfo.setStyle("-fx-background-color: #FF4081; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12;"));

//                 btnEmployeeInfo.setOnAction(e -> showEmployeeInfoDialog());

//                 HBox.setMargin(btnEmployeeInfo, new Insets(0, 10, 0, 10));
//                 int infoLabelIndex = topBar.getChildren().size() - 1;
//                 if (infoLabelIndex >= 0) {
//                     topBar.getChildren().add(infoLabelIndex, btnEmployeeInfo);
//                 } else {
//                     topBar.getChildren().add(btnEmployeeInfo);
//                 }

//                 LOGGER.info("✨ Đã thêm nút thông tin nhân viên!");
//             }
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi thêm nút thông tin nhân viên", e);
//         }
//     }

//     // Hàm hiển thị dialog thông tin nhân viên SIÊU XỊNNN
//     @FXML
//     private void showEmployeeInfoDialog() {
//         try {
//             if (currentEmployee == null) {
//                 AlertUtil.showWarning("Thông báo", "Không thể lấy thông tin nhân viên!");
//                 return;
//             }

//             // Tạo stage mới cho dialog
//             Stage infoStage = new Stage();
//             infoStage.initModality(Modality.APPLICATION_MODAL);
//             infoStage.setTitle("Thông Tin Nhân Viên");
//             infoStage.setResizable(false);

//             // Tạo layout chính
//             BorderPane layout = new BorderPane();

//             // Phần header đẹp ngời
//             HBox header = new HBox();
//             header.setAlignment(Pos.CENTER);
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: linear-gradient(to right, #FF4081, #F50057);");

//             Label headerTitle = new Label("THÔNG TIN NHÂN VIÊN");
//             headerTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
//             header.getChildren().add(headerTitle);

//             // Thêm header vào layout
//             layout.setTop(header);

//             // Phần nội dung
//             GridPane content = new GridPane();
//             content.setPadding(new Insets(20));
//             content.setVgap(15);
//             content.setHgap(10);
//             content.setAlignment(Pos.CENTER);

//             // Tạo ImageView cho ảnh đại diện (avatar)
//             ImageView avatarView = new ImageView();

//             // Tải ảnh từ resource đường dẫn đúng
//             try {
//                 // Lấy theo nhân viên đang đăng nhập
//                 String avatarPath = "/com/example/stores/images/employee/img.png"; // mặc định

//                 // Nếu là nv001, dùng ảnh an.png
//                 if (currentEmployee.getUsername() != null && currentEmployee.getUsername().equals("nv001")) {
//                     avatarPath = "/com/example/stores/images/employee/an.png";
//                 }

//                 // Hoặc nếu có imageUrl trong database
//                 if (currentEmployee.getImageUrl() != null && !currentEmployee.getImageUrl().isEmpty()) {
//                     String imageUrl = currentEmployee.getImageUrl();
//                     // Bỏ "resources/" ở đầu nếu có
//                     String resourcePath = imageUrl.startsWith("resources/") ? imageUrl.substring(10) : imageUrl;
//                     // Thay "com.example.stores/" thành "com/example/stores/"
//                     if (resourcePath.startsWith("com.example.stores/")) {
//                         resourcePath = resourcePath.replace("com.example.stores/", "com/example/stores/");
//                     }
//                     // Thêm dấu "/" ở đầu
//                     avatarPath = "/" + resourcePath;
//                 }

//                 // Load ảnh
//                 Image avatarImage = new Image(getClass().getResourceAsStream(avatarPath));
//                 avatarView.setImage(avatarImage);
//             } catch (Exception e) {
//                 // Nếu không có ảnh, hiển thị icon người dùng mặc định
//                 try {
//                     // Đường dẫn default chuẩn
//                     Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/stores/images/employee/img.png"));
//                     avatarView.setImage(defaultImage);
//                 } catch (Exception ex) {
//                     LOGGER.warning("Không thể tải ảnh mặc định cho nhân viên: " + ex.getMessage());
//                 }
//             }

//             // Thiết lập kích thước avatar
//             avatarView.setFitWidth(120);
//             avatarView.setFitHeight(120);
//             avatarView.setPreserveRatio(true);

//             // Bo tròn avatar bằng clip hình tròn
//             Circle clip = new Circle(60, 60, 60); // tâm (60,60), bán kính 60px
//             avatarView.setClip(clip);

//             // Tạo StackPane cho avatar, có viền và padding
//             StackPane avatarContainer = new StackPane(avatarView);
//             avatarContainer.setPadding(new Insets(3));
//             avatarContainer.setStyle("-fx-background-color: white; -fx-border-color: #FF4081; " +
//                     "-fx-border-width: 3; -fx-border-radius: 60; -fx-background-radius: 60;");
//             GridPane.setColumnSpan(avatarContainer, 2);
//             GridPane.setHalignment(avatarContainer, javafx.geometry.HPos.CENTER);

//             // Thêm avatar vào đầu tiên
//             content.add(avatarContainer, 0, 0, 2, 1);

//             // Thêm các thông tin nhân viên
//             addEmployeeInfoField(content, "Mã nhân viên:", currentEmployee.getEmployeeID(), 1);
//             addEmployeeInfoField(content, "Tên đăng nhập:", currentEmployee.getUsername(), 2);
//             addEmployeeInfoField(content, "Họ tên:", currentEmployee.getFullName(), 3);

//             // Thêm thông tin position nếu có
//             String position = "Nhân viên";
//             try {
//                 position = currentEmployee.getPosition();
//                 if (position == null || position.isEmpty()) position = "Nhân viên";
//             } catch (Exception e) {
//                 // Nếu không có thuộc tính position, dùng giá trị mặc định
//                 LOGGER.info("Không có thông tin chức vụ");
//             }
//             addEmployeeInfoField(content, "Chức vụ:", position, 4);

//             addEmployeeInfoField(content, "Email:", currentEmployee.getEmail(), 5);
//             addEmployeeInfoField(content, "Điện thoại:", currentEmployee.getPhone(), 6);
//             addEmployeeInfoField(content, "Thời gian đăng nhập:", currentDateTime, 7);

//             // Button đóng dialog
//             HBox buttonBar = new HBox();
//             buttonBar.setAlignment(Pos.CENTER);
//             buttonBar.setPadding(new Insets(0, 0, 20, 0));

//             Button closeButton = new Button("ĐÓNG");
//             closeButton.setPrefWidth(120);
//             closeButton.setPrefHeight(35);
//             closeButton.setStyle("-fx-background-color: #F50057; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");

//             closeButton.setOnMouseEntered(e ->
//                     closeButton.setStyle("-fx-background-color: #C51162; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;")
//             );

//             closeButton.setOnMouseExited(e ->
//                     closeButton.setStyle("-fx-background-color: #F50057; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;")
//             );

//             closeButton.setOnAction(e -> infoStage.close());

//             buttonBar.getChildren().add(closeButton);

//             // Thêm nội dung và button vào layout
//             VBox mainContainer = new VBox(15);
//             mainContainer.getChildren().addAll(content, buttonBar);
//             layout.setCenter(mainContainer);

//             // Tạo scene và hiển thị
//             Scene scene = new Scene(layout, 400, 520);
//             infoStage.setScene(scene);
//             infoStage.show();

//             LOGGER.info("✨ Đã hiển thị thông tin nhân viên: " + currentEmployee.getFullName());
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị thông tin nhân viên: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị thông tin nhân viên: " + e.getMessage());
//         }
//     }

//     // Hàm hỗ trợ thêm trường thông tin
//     private void addEmployeeInfoField(GridPane grid, String labelText, String value, int row) {
//         // Label tiêu đề
//         Label label = new Label(labelText);
//         label.setStyle("-fx-font-weight: bold; -fx-text-fill: #757575;");
//         grid.add(label, 0, row);

//         // Giá trị
//         Label valueLabel = new Label(value != null ? value : "N/A");
//         valueLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #212121;");
//         grid.add(valueLabel, 1, row);
//     }

//     // Biến để đếm số đơn hàng
//     private int orderCounter = 1;

//     private Button createLoadMoreButton() {
//         Button btnLoadMore = new Button("XEM THÊM SẢN PHẨM");
//         btnLoadMore.setPrefWidth(200);
//         btnLoadMore.setPrefHeight(40);
//         btnLoadMore.setStyle(
//                 "-fx-background-color: linear-gradient(to right, #2196F3, #03A9F4); " +
//                         "-fx-text-fill: white; " +
//                         "-fx-font-weight: bold; " +
//                         "-fx-font-size: 14px; " +
//                         "-fx-background-radius: 5; " +
//                         "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.4), 6, 0, 0, 2);"
//         );

//         // Hiệu ứng khi hover
//         btnLoadMore.setOnMouseEntered(e ->
//                 btnLoadMore.setStyle(
//                         "-fx-background-color: linear-gradient(to right, #1976D2, #2196F3); " +
//                                 "-fx-text-fill: white; " +
//                                 "-fx-font-weight: bold; " +
//                                 "-fx-font-size: 14px; " +
//                                 "-fx-background-radius: 5; " +
//                                 "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.6), 8, 0, 0, 3);"
//                 )
//         );

//         // Trở về style ban đầu khi hết hover
//         btnLoadMore.setOnMouseExited(e ->
//                 btnLoadMore.setStyle(
//                         "-fx-background-color: linear-gradient(to right, #2196F3, #03A9F4); " +
//                                 "-fx-text-fill: white; " +
//                                 "-fx-font-weight: bold; " +
//                                 "-fx-font-size: 14px; " +
//                                 "-fx-background-radius: 5; " +
//                                 "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.4), 6, 0, 0, 2);"
//                 )
//         );

//         // Sự kiện khi click
//         btnLoadMore.setOnAction(e -> {
//             productLimit += 20; // Tăng thêm 20 sản phẩm
//             refreshProductList(); // Làm mới danh sách
//             LOGGER.info("Đã tăng giới hạn hiển thị lên " + productLimit + " sản phẩm");
//         });

//         return btnLoadMore;
//     }

//     /**
//      * Lưu đơn hàng vào lịch sử
//      */
//     public void addToOrderHistory(int orderId, String customerName, String customerPhone,
//                                   String paymentMethod, String orderDateTime, double totalAmount,
//                                   List<CartItemEmployee> items) {
//         Connection conn = null;
//         PreparedStatement pstmtOrder = null;
//         PreparedStatement pstmtDetail = null;

//         try {
//             if (items == null || items.isEmpty()) {
//                 LOGGER.warning("Danh sách sản phẩm rỗng, không thể lưu lịch sử vào DB");
//                 return;
//             }

//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.severe("Không thể kết nối database để lưu order history");
//                 return;
//             }
//             conn.setAutoCommit(false); // Bắt đầu transaction

//             // 1. Insert vào bảng Orders
//             String insertOrder = "INSERT INTO Orders (orderID, orderDate, totalAmount, customerID, employeeID, orderStatus, paymentMethod, recipientName, recipientPhone) "
//                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//             pstmtOrder = conn.prepareStatement(insertOrder);

//             String orderIdStr = String.format("ORD%03d", orderId); // Format lại cho khớp orderID

//             int customerId = getWalkInCustomerId(); // Hoặc lấy đúng customerID nếu phân biệt khách

//             pstmtOrder.setString(1, orderIdStr);
//             pstmtOrder.setString(2, orderDateTime);
//             pstmtOrder.setDouble(3, totalAmount);
//             pstmtOrder.setInt(4, customerId);
//             pstmtOrder.setInt(5, employeeId);
//             pstmtOrder.setString(6, "Đã xác nhận");
//             pstmtOrder.setString(7, paymentMethod);
//             pstmtOrder.setString(8, customerName);
//             pstmtOrder.setString(9, customerPhone);

//             int resultOrder = pstmtOrder.executeUpdate();
//             if (resultOrder == 0) throw new SQLException("Insert Orders thất bại!");

//             // 2. Insert từng sản phẩm vào OrderDetails
//             String insertDetail = "INSERT INTO OrderDetails (orderID, productID, quantity, unitPrice, warrantyType, warrantyPrice) "
//                     + "VALUES (?, ?, ?, ?, ?, ?)";
//             pstmtDetail = conn.prepareStatement(insertDetail);

//             for (CartItemEmployee item : items) {
//                 pstmtDetail.setString(1, orderIdStr);
//                 pstmtDetail.setString(2, item.getProductID());
//                 pstmtDetail.setInt(3, item.getQuantity());
//                 pstmtDetail.setDouble(4, item.getPrice());

//                 // Bảo hành
//                 if (item.hasWarranty()) {
//                     pstmtDetail.setString(5, item.getWarranty().getWarrantyType());
//                     pstmtDetail.setDouble(6, item.getWarranty().getWarrantyPrice());
//                 } else {
//                     pstmtDetail.setString(5, "Thường");
//                     pstmtDetail.setDouble(6, 0.0);
//                 }
//                 pstmtDetail.addBatch();
//             }
//             int[] detailResults = pstmtDetail.executeBatch();

//             conn.commit();
//             LOGGER.info("✅ Đã lưu đơn hàng #" + orderIdStr + " vào database với " + detailResults.length + " sản phẩm");

//         } catch (Exception e) {
//             try {
//                 if (conn != null) conn.rollback();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi rollback khi lưu đơn hàng!", ex);
//             }
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi lưu đơn hàng vào DB: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (pstmtOrder != null) pstmtOrder.close();
//                 if (pstmtDetail != null) pstmtDetail.close();
//                 if (conn != null) conn.setAutoCommit(true);
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.WARNING, "Lỗi khi đóng kết nối DB", ex);
//             }
//         }
//     }
//     /**
//      * Lấy ID của khách hàng "Khách lẻ" (walkin)
//      */
//     private int getWalkInCustomerId() {
//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;
//         int customerId = 1; // Mặc định ID=1 cho khách lẻ

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.warning("Không thể kết nối đến database");
//                 return customerId;
//             }

//             String sql = "SELECT customerID FROM Customer WHERE username = 'walkin'";
//             stmt = conn.prepareStatement(sql);
//             rs = stmt.executeQuery();

//             if (rs.next()) {
//                 customerId = rs.getInt("customerID");
//                 return customerId;
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.WARNING, "Lỗi SQL khi lấy ID khách hàng mặc định: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.WARNING, "Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.WARNING, "Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }

//         return customerId;
//     }
//     // Thông tin user và thời gian
//     private String currentUser = "doanpk";
//     private String currentDateTime = "2025-06-22 10:30:23"; // Cập nhật thời gian hiện tại từ input
//     private Employee currentEmployee; // Biến lưu thông tin nhân viên

//     // Class để lưu thông tin đơn hàng tạm thời
//     private static class OrderSummary {
//         private int id;
//         private String customerName;
//         private String customerPhone;
//         private String paymentMethod;
//         private String orderDate;
//         private double totalAmount;
//         private List<CartItemEmployee> items;

//         public OrderSummary(int id, String customerName, String customerPhone, String paymentMethod,
//                             String orderDate, double totalAmount, List<CartItemEmployee> items) {
//             this.id = id;
//             this.customerName = customerName;
//             this.customerPhone = customerPhone;
//             this.paymentMethod = paymentMethod;
//             this.orderDate = orderDate;
//             this.totalAmount = totalAmount;
//             this.items = new ArrayList<>(items);
//         }

//         // Getters
//         public int getId() { return id; }
//         public String getCustomerName() { return customerName; }
//         public String getCustomerPhone() { return customerPhone; }
//         public String getPaymentMethod() { return paymentMethod; }
//         public String getOrderDate() { return orderDate; }
//         public double getTotalAmount() { return totalAmount; }
//         public List<CartItemEmployee> getItems() { return items; }
//     }

//     @FXML
//     private void initialize() {
//         try {
//             LOGGER.info("Đang khởi tạo POS Overview Controller...");
//             LOGGER.info("Người dùng hiện tại: " + currentUser);
//             LOGGER.info("Thời gian hiện tại: " + currentDateTime);

//             // Set style trực tiếp để đảm bảo nút có màu
//             if (btnCheckout != null) {
//                 btnCheckout.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             }

//             productFlowPane.setPrefWidth(900);
//             productFlowPane.setPrefWrapLength(900);  // DÒNG QUAN TRỌNG NHẤT!!!
//             productFlowPane.setHgap(15);
//             productFlowPane.setVgap(20);

//             // Lấy dữ liệu sản phẩm từ database
//             loadProductsFromDatabase();
//             LOGGER.info("Đã load " + products.size() + " sản phẩm từ database");

//             // Cấu hình TableView giỏ hàng
//             setupCartTable();

//             // Thêm nút xóa vào bảng giỏ hàng
//             addButtonsToTable();

//             cartTable.setItems(cartItems);

//             // Khởi tạo ComboBox filter danh mục
//             List<String> categoryList = getDistinctCategories();
//             if (cbCategory != null) cbCategory.setItems(FXCollections.observableArrayList(categoryList));

//             // Đảm bảo luôn chọn giá trị đầu tiên nếu có
//             if (cbCategory != null && !cbCategory.getItems().isEmpty()) cbCategory.getSelectionModel().select(0);

//             // Khởi tạo ComboBox sắp xếp
//             if (cbSort != null) {
//                 cbSort.getItems().addAll(
//                         "Mặc định",
//                         "Tên A-Z",
//                         "Tên Z-A",
//                         "Giá thấp đến cao",
//                         "Giá cao đến thấp"
//                 );
//                 cbSort.getSelectionModel().select(0);

//                 // Thêm listener cho cbSort
//                 cbSort.setOnAction(e -> refreshProductList());
//             }

//             // Sự kiện lọc, tìm kiếm
//             if (btnFilter != null) {
//                 btnFilter.setOnAction(e -> refreshProductList());
//             }

//             if (txtSearch != null) {
//                 txtSearch.textProperty().addListener((obs, oldVal, newVal) -> refreshProductList());
//             }

//             if (cbCategory != null) {
//                 cbCategory.setOnAction(e -> refreshProductList());
//             }

//             // Thanh toán - gọi handleCheckout để lưu dữ liệu vào DB
//             if (btnCheckout != null) {
//                 btnCheckout.setOnAction(e -> handleCheckout());
//             }

//             // Thêm nút lịch sử
//             addHistoryButton();

//             // Thêm nút đăng xuất
//             addLogoutButton();

//             // THÊM NÚT XEM THÔNG TIN NHÂN VIÊN
//             addEmployeeInfoButton();

//             // Render sản phẩm ban đầu
//             refreshProductList();
//             LOGGER.info("Khởi tạo POS Overview Controller thành công");
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi khởi tạo POS Overview Controller", e);
//         }
//     }

//     /**
//      * Xử lý thanh toán và lưu đơn hàng vào DB
//      */
//     @FXML
//     private void handleCheckout() {
//         try {
//             if (cartItems.isEmpty()) {
//                 AlertUtil.showWarning("Giỏ hàng trống", "Vui lòng thêm sản phẩm vào giỏ hàng trước khi thanh toán!");
//                 return;
//             }

//             // Tạo stage mới cho popup thanh toán
//             Stage confirmStage = new Stage();
//             confirmStage.initModality(Modality.APPLICATION_MODAL);
//             confirmStage.setTitle("Xác nhận thanh toán");
//             confirmStage.setResizable(false);

//             // BorderPane chính
//             BorderPane mainLayout = new BorderPane();

//             // HEADER ĐẸP NGỜI
//             HBox header = new HBox();
//             header.setAlignment(Pos.CENTER);
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: linear-gradient(to right, #4e73df, #224abe);");

//             Label headerLabel = new Label("XÁC NHẬN THANH TOÁN");
//             headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
//             header.getChildren().add(headerLabel);

//             mainLayout.setTop(header);

//             // PHẦN NỘI DUNG CHÍNH
//             VBox content = new VBox(15);
//             content.setPadding(new Insets(20));

//             // Tổng thanh toán hiển thị nổi bật
//             double totalAmount = calculateTotalAmount();
//             Label totalLabel = new Label("Tổng thanh toán: " + String.format("%,.0f", totalAmount) + "đ");
//             totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e91e63;");

//             // BẢNG DANH SÁCH SẢN PHẨM - THÊM VÀO FORM THANH TOÁN
//             Label productsLabel = new Label("Danh sách sản phẩm:");
//             productsLabel.setStyle("-fx-font-weight: bold;");

//             // TableView hiển thị sản phẩm trong giỏ
//             TableView<CartItemEmployee> productsTable = new TableView<>();
//             productsTable.setPrefHeight(150);

//             // Cột tên sản phẩm
//             TableColumn<CartItemEmployee, String> nameColumn = new TableColumn<>("Tên sản phẩm");
//             nameColumn.setCellValueFactory(data ->
//                     new SimpleStringProperty(data.getValue().getProductName()));
//             nameColumn.setPrefWidth(200);

//             // Cột số lượng
//             TableColumn<CartItemEmployee, Integer> quantityColumn = new TableColumn<>("SL");
//             quantityColumn.setCellValueFactory(data ->
//                     new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
//             quantityColumn.setPrefWidth(50);

//             // Cột đơn giá
//             TableColumn<CartItemEmployee, Double> priceColumn = new TableColumn<>("Đơn giá");
//             priceColumn.setCellValueFactory(data ->
//                     new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
//             priceColumn.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double price, boolean empty) {
//                     super.updateItem(price, empty);
//                     if (empty || price == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", price) + "đ");
//                     }
//                 }
//             });
//             priceColumn.setPrefWidth(100);

//             // Cột bảo hành
//             TableColumn<CartItemEmployee, String> warrantyColumn = new TableColumn<>("Bảo hành");
//             warrantyColumn.setCellValueFactory(data -> {
//                 CartItemEmployee item = data.getValue();
//                 if (item.hasWarranty()) {
//                     return new SimpleStringProperty(item.getWarranty().getWarrantyType());
//                 }
//                 return new SimpleStringProperty("Không");
//             });
//             warrantyColumn.setPrefWidth(80);

//             // Cột thành tiền
//             TableColumn<CartItemEmployee, Double> subtotalColumn = new TableColumn<>("T.Tiền");
//             subtotalColumn.setCellValueFactory(data ->
//                     new SimpleDoubleProperty(data.getValue().getTotalPrice()).asObject());
//             subtotalColumn.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double price, boolean empty) {
//                     super.updateItem(price, empty);
//                     if (empty || price == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", price) + "đ");
//                     }
//                 }
//             });
//             subtotalColumn.setPrefWidth(100);

//             productsTable.getColumns().addAll(nameColumn, quantityColumn, priceColumn, warrantyColumn, subtotalColumn);
//             productsTable.setItems(cartItems);

//             // Phần thông tin khách hàng
//             Label customerLabel = new Label("Thông tin khách hàng:");
//             customerLabel.setStyle("-fx-font-weight: bold;");

// // Form thông tin khách hàng
//             GridPane customerForm = new GridPane();
//             customerForm.setVgap(10);
//             customerForm.setHgap(10);

//             Label nameLabel = new Label("Tên khách hàng:");
//             TextField nameField = new TextField("Khách lẻ");
//             nameField.setPrefWidth(300);

//             Label phoneLabel = new Label("Số điện thoại:");
//             TextField phoneField = new TextField("0900000000");
//             phoneField.setPrefWidth(300);

// // ✅ THÊM TRƯỜNG GHI CHÚ
//             Label noteLabel = new Label("Ghi chú:");
//             TextArea noteField = new TextArea();
//             noteField.setPromptText("Nhập ghi chú cho đơn hàng (không bắt buộc)...");
//             noteField.setPrefWidth(300);
//             noteField.setPrefHeight(60);
//             noteField.setWrapText(true);

//             customerForm.add(nameLabel, 0, 0);
//             customerForm.add(nameField, 1, 0);
//             customerForm.add(phoneLabel, 0, 1);
//             customerForm.add(phoneField, 1, 1);
//             customerForm.add(noteLabel, 0, 2);  // ✅ THÊM VÀO DÒNG THỨ 3
//             customerForm.add(noteField, 1, 2);

//             // Phương thức thanh toán - CHỈ CÓ 2 PHƯƠNG THỨC
//             Label paymentLabel = new Label("Phương thức thanh toán:");
//             paymentLabel.setStyle("-fx-font-weight: bold;");

//             ToggleGroup paymentGroup = new ToggleGroup();

//             RadioButton cashRadio = new RadioButton("Tiền mặt");
//             cashRadio.setToggleGroup(paymentGroup);
//             cashRadio.setSelected(true); // Mặc định chọn tiền mặt

//             RadioButton transferRadio = new RadioButton("Chuyển khoản");
//             transferRadio.setToggleGroup(paymentGroup);

//             HBox paymentOptions = new HBox(20);
//             paymentOptions.getChildren().addAll(cashRadio, transferRadio);

//             // Thêm các thành phần vào content
//             content.getChildren().addAll(
//                     totalLabel,
//                     new Separator(),
//                     productsLabel,
//                     productsTable,
//                     new Separator(),
//                     customerLabel,
//                     customerForm,
//                     new Separator(),
//                     paymentLabel,
//                     paymentOptions
//             );

//             mainLayout.setCenter(new ScrollPane(content));

//             // PHẦN FOOTER VỚI CÁC NÚT CHỨC NĂNG
//             HBox footer = new HBox(10);
//             footer.setAlignment(Pos.CENTER_RIGHT);
//             footer.setPadding(new Insets(15, 20, 15, 20));
//             footer.setStyle("-fx-background-color: #f8f9fc; -fx-border-color: #e3e6f0; -fx-border-width: 1 0 0 0;");

//             Button cancelButton = new Button("Hủy");
//             cancelButton.setPrefWidth(100);
//             cancelButton.setStyle("-fx-background-color: #e74a3b; -fx-text-fill: white;");

//             Button confirmButton = new Button("Xác nhận thanh toán");
//             confirmButton.setPrefWidth(200);
//             confirmButton.setStyle("-fx-background-color: #4e73df; -fx-text-fill: white; -fx-font-weight: bold;");

//             footer.getChildren().addAll(cancelButton, confirmButton);
//             mainLayout.setBottom(footer);

//             // Xử lý sự kiện cho nút Hủy
//             cancelButton.setOnAction(e -> confirmStage.close());

//             // Xử lý sự kiện cho nút Xác nhận thanh toán
//             confirmButton.setOnAction(e -> {
//                 try {
//                     // Lấy thông tin khách hàng và phương thức thanh toán
//                     String customerName = nameField.getText().trim();
//                     String customerPhone = phoneField.getText().trim();
//                     String paymentMethod = cashRadio.isSelected() ? "Tiền mặt" : "Chuyển khoản";

//                     // Validate số điện thoại
//                     if (!customerPhone.isEmpty() && customerPhone.length() < 10) {
//                         AlertUtil.showWarning("Lỗi", "Số điện thoại không hợp lệ!");
//                         return;
//                     }

//                     // NẾU CHỌN CHUYỂN KHOẢN - MỞ CỬA SỔ QR CODE
//                     if (transferRadio.isSelected()) {
//                         // Đóng cửa sổ xác nhận
//                         confirmStage.close();

//                         // Mở cửa sổ QR Payment
//                         showQRPaymentWindow(customerName, customerPhone, totalAmount, cartItems);
//                         return;
//                     }

//                     // NẾU THANH TOÁN TIỀN MẶT - XỬ LÝ LUÔN
//                     // Lưu đơn hàng vào DB và trả về orderID
//                     String orderId = saveOrderToDB(customerName, customerPhone, paymentMethod, totalAmount, cartItems);

//                     if (orderId != null) {
//                         // FIX LỖI: Chỉ lấy phần số từ orderId (bỏ phần chữ "ORD")
//                         String numericOrderId = orderId.replaceAll("[^0-9]", "");
//                         int orderIdInt = Integer.parseInt(numericOrderId);

//                         // Lưu vào bộ nhớ (để tương thích với code cũ) - DÙNG ID ĐÃ XỬ LÝ
//                         addToOrderHistory(orderIdInt, customerName, customerPhone,
//                                 paymentMethod, getCurrentDateTime(), totalAmount, cartItems);

//                         // Đóng cửa sổ thanh toán
//                         confirmStage.close();

//                         // Hiển thị thông báo thành công
//                         AlertUtil.showInfo("Thanh toán thành công",
//                                 "Đơn hàng #" + orderId + " đã được tạo thành công!");

//                         // In hóa đơn - DÙNG ID ĐÃ XỬ LÝ
//                         printReceiptWithPaymentMethod(
//                                 orderIdInt,
//                                 cartItems, totalAmount, customerName, customerPhone,
//                                 paymentMethod, getCurrentDateTime(), currentUser);

//                         // Xóa giỏ hàng
//                         clearCart();
//                     } else {
//                         // Thông báo lỗi
//                         AlertUtil.showError("Lỗi thanh toán",
//                                 "Không thể lưu đơn hàng. Vui lòng thử lại!");
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi thanh toán: " + ex.getMessage(), ex);
//                     AlertUtil.showError("Lỗi thanh toán", "Đã xảy ra lỗi: " + ex.getMessage());
//                     confirmStage.close();
//                 }
//             });

//             Scene scene = new Scene(mainLayout, 600, 700);
//             confirmStage.setScene(scene);
//             confirmStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi hiển thị form thanh toán: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể mở trang xác nhận thanh toán: " + e.getMessage());
//         }
//     }

//     /**
//      * Hiển thị cửa sổ thanh toán QR Code
//      */
//     private void showQRPaymentWindow(String customerName, String customerPhone, double totalAmount, ObservableList<CartItemEmployee> items) {
//         try {
//             LOGGER.info("💖 Bắt đầu mở cửa sổ QR Payment nè!");

//             // Tạo đối tượng Order giả
//             Order order = new Order();
//             order.setTotalAmount(totalAmount);

//             // DEBUG: In ra đường dẫn hiện tại
//             LOGGER.info("📂 Working Directory: " + System.getProperty("user.dir"));

//             FXMLLoader loader = null;

//             // THỬ TẤT CẢ CÁC ĐƯỜNG DẪN CÓ THỂ
//             String[] possiblePaths = {
//                     "/com/example/stores/view/qr_payment.fxml",
//                     "com/example/stores/view/qr_payment.fxml",
//                     "/view/qr_payment.fxml",
//                     "view/qr_payment.fxml",
//                     "/qr_payment.fxml",
//                     "qr_payment.fxml"
//             };

//             for (String path : possiblePaths) {
//                 try {
//                     LOGGER.info("🔍 Thử load FXML từ: " + path);
//                     URL fxmlUrl = getClass().getResource(path);

//                     if (fxmlUrl != null) {
//                         LOGGER.info("✅ Tìm thấy file FXML tại: " + fxmlUrl);
//                         loader = new FXMLLoader(fxmlUrl);
//                         break;
//                     } else {
//                         LOGGER.warning("❌ Không tìm thấy FXML tại: " + path);
//                     }
//                 } catch (Exception e) {
//                     LOGGER.warning("❌ Lỗi khi thử path: " + path + " - " + e.getMessage());
//                 }
//             }

//             // Nếu không tìm thấy file FXML nào
//             if (loader == null) {
//                 LOGGER.severe("😭 KHÔNG TÌM THẤY FILE FXML NÀO HẾT!!!");
//                 throw new Exception("Không tìm thấy file FXML cho QR Payment");
//             }

//             // Load FXML
//             Parent root = loader.load();
//             LOGGER.info("✅ Đã load FXML thành công!");

//             // Lấy controller và truyền dữ liệu
//             QRPaymentControllerE controller = loader.getController();
//             LOGGER.info("✅ Đã lấy controller thành công!");

//             // Tạo danh sách OrderDetail giả
//             List<OrderDetail> orderDetails = new ArrayList<>();
//             // Chuyển đổi từ CartItem sang OrderDetail
//             for (CartItemEmployee item : items) {
//                 OrderDetail detail = new OrderDetail();
//                 detail.setProductName(item.getProductName());
//                 detail.setQuantity(item.getQuantity());
//                 detail.setPrice(item.getPrice());
//                 orderDetails.add(detail);
//             }

//             // Set dữ liệu cho Controller
//             controller.setOrderDetails(order, orderDetails);
//             LOGGER.info("✅ Đã set order details!");

//             // Set callback khi thanh toán thành công
//             controller.setOnPaymentSuccess(() -> {
//                 try {
//                     // Tạo đơn hàng với phương thức thanh toán là chuyển khoản
//                     String orderId = saveOrderToDB(customerName, customerPhone, "Chuyển khoản", totalAmount, items);
//                     LOGGER.info("✅ Đã lưu đơn hàng với ID: " + orderId);

//                     if (orderId != null) {
//                         // FIX LỖI: Chỉ lấy phần số từ orderId
//                         String numericOrderId = orderId.replaceAll("[^0-9]", "");
//                         int orderIdInt = Integer.parseInt(numericOrderId);

//                         // Lưu vào bộ nhớ với ID đã xử lý
//                         addToOrderHistory(orderIdInt, customerName, customerPhone,
//                                 "Chuyển khoản", getCurrentDateTime(), totalAmount, items);

//                         // Hiển thị thông báo thành công
//                         AlertUtil.showInfo("Thanh toán thành công",
//                                 "Đơn hàng #" + orderId + " đã được thanh toán thành công!");

//                         // In hóa đơn với ID đã xử lý
//                         printReceiptWithPaymentMethod(
//                                 orderIdInt,
//                                 items, totalAmount, customerName, customerPhone,
//                                 "Chuyển khoản", getCurrentDateTime(), currentUser);

//                         // Xóa giỏ hàng
//                         clearCart();
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi hoàn tất thanh toán QR: " + ex.getMessage(), ex);
//                     AlertUtil.showError("Lỗi thanh toán", "Đã xảy ra lỗi: " + ex.getMessage());
//                 }
//             });

//             // Hiển thị cửa sổ QR
//             Stage qrStage = new Stage();
//             qrStage.initModality(Modality.APPLICATION_MODAL);
//             qrStage.setTitle("Thanh toán bằng mã QR");
//             qrStage.setResizable(false);

//             Scene scene = new Scene(root);
//             qrStage.setScene(scene);

//             LOGGER.info("💯 SẮP HIỆN CỬA SỔ QR PAYMENT RỒI!!!");
//             qrStage.show(); // Dùng show() thay vì showAndWait() để debug
//             LOGGER.info("🎉 ĐÃ HIỆN CỬA SỔ QR PAYMENT!!!");

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi hiển thị cửa sổ thanh toán QR: " + e.getMessage(), e);

//             // In lỗi chi tiết hơn
//             e.printStackTrace();

//             AlertUtil.showError("Lỗi", "Không thể mở cửa sổ thanh toán QR: " + e.getMessage() + "\nVui lòng thanh toán bằng tiền mặt!");

//             // Trong trường hợp lỗi, thử lại với phương thức thanh toán tiền mặt
//             try {
//                 String orderId = saveOrderToDB(customerName, customerPhone, "Tiền mặt", totalAmount, items);
//                 if (orderId != null) {
//                     // FIX LỖI: Chỉ lấy phần số từ orderId
//                     String numericOrderId = orderId.replaceAll("[^0-9]", "");
//                     int orderIdInt = Integer.parseInt(numericOrderId);

//                     addToOrderHistory(orderIdInt, customerName, customerPhone, "Tiền mặt", getCurrentDateTime(), totalAmount, items);

//                     AlertUtil.showInfo("Thanh toán thành công",
//                             "Đã chuyển sang thanh toán tiền mặt.\nĐơn hàng #" + orderId + " đã được tạo thành công!");

//                     printReceiptWithPaymentMethod(orderIdInt, items, totalAmount, customerName, customerPhone,
//                             "Tiền mặt", getCurrentDateTime(), currentUser);

//                     clearCart();
//                 }
//             } catch (Exception ex) {
//                 LOGGER.log(Level.SEVERE, "❌ Lỗi khi thử thanh toán tiền mặt: " + ex.getMessage(), ex);
//             }
//         }
//     }    /**
//      * Lưu đơn hàng vào DB
//      * @return Mã đơn hàng (orderID) nếu lưu thành công, null nếu thất bại
//      */
//     private String saveOrderToDB(String recipientName, String recipientPhone,
//                                  String paymentMethod, double totalAmount,
//                                  List<CartItemEmployee> cartItems) {
//         String orderId = null;
//         Connection conn = null;

//         try {
//             conn = DBConfig.getConnection();
//             conn.setAutoCommit(false);

//             // 1. Tạo đơn hàng mới trong bảng Orders
//             String insertOrderSQL = "INSERT INTO Orders (orderDate, totalAmount, customerID, " +
//                     "recipientPhone, recipientName, orderStatus, paymentMethod) " +
//                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

//             try (PreparedStatement pstmtOrder = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {

//                 pstmtOrder.setString(1, getCurrentDateTime());
//                 pstmtOrder.setDouble(2, totalAmount);

//                 // ==== SỬA ĐOẠN NÀY ĐỂ LƯU KHÁCH HÀNG MỚI ====
//                 CustomerServiceE customerServiceE = new CustomerServiceE();
//                 int customerId = customerServiceE.findCustomerIdByPhone(recipientPhone);
//                 if (customerId == -1) {
//                     Customer newCustomer = new Customer();
//                     newCustomer.setCustomerName(recipientName);
//                     newCustomer.setPhone(recipientPhone);
//                     newCustomer.setAddress(""); // Có thể lấy từ form nếu có
//                     newCustomer.setEmail("");   // Có thể lấy từ form nếu có
//                     customerId = customerServiceE.addCustomerToDB(newCustomer);
//                     if (customerId == -1) {
//                         LOGGER.warning("❌ Không thể tạo khách mới, fallback về ID=1");
//                         customerId = 1; // fallback nếu lỗi
//                     }
//                 }
//                 pstmtOrder.setInt(3, customerId);

//                 pstmtOrder.setString(4, recipientPhone != null ? recipientPhone : "");
//                 pstmtOrder.setString(5, recipientName != null ? recipientName : "Khách lẻ");
//                 pstmtOrder.setString(6, "Đã xác nhận");
//                 pstmtOrder.setString(7, paymentMethod != null ? paymentMethod : "Tiền mặt");

//                 int result = pstmtOrder.executeUpdate();

//                 if (result > 0) {
//                     // Lấy orderID vừa được tạo
//                     ResultSet generatedKeys = pstmtOrder.getGeneratedKeys();
//                     if (generatedKeys.next()) {
//                         orderId = generatedKeys.getString(1);
//                         LOGGER.info("✅ Đã tạo đơn hàng mới với ID: " + orderId);

//                         // 2. Thêm chi tiết đơn hàng
//                         saveOrderDetails(conn, orderId, cartItems);

//                         // 3. Commit transaction
//                         conn.commit();
//                     }
//                 }

//             }

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi lưu đơn hàng vào DB: " + e.getMessage(), e);
//             // Rollback transaction nếu có lỗi
//             if (conn != null) {
//                 try {
//                     conn.rollback();
//                 } catch (SQLException ex) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi rollback transaction: " + ex.getMessage(), ex);
//                 }
//             }

//         } finally {
//             // Đảm bảo đóng connection và reset autoCommit
//             if (conn != null) {
//                 try {
//                     conn.setAutoCommit(true);
//                     conn.close();
//                 } catch (SQLException e) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi đóng connection: " + e.getMessage(), e);
//                 }
//             }
//         }

//         return orderId;
//     }
//     /**
//      * Lưu chi tiết đơn hàng vào DB
//      */
//     private void saveOrderDetails(Connection conn, String orderId, List<CartItemEmployee> cartItems) throws SQLException {
//         String insertDetailSQL = "INSERT INTO OrderDetails (orderID, productID, quantity, unitPrice, warrantyType, warrantyPrice) " +
//                 "VALUES (?, ?, ?, ?, ?, ?)";

//         try (PreparedStatement pstmt = conn.prepareStatement(insertDetailSQL)) {
//             for (CartItemEmployee item : cartItems) {
//                 pstmt.setString(1, orderId);
//                 pstmt.setString(2, item.getProductID());
//                 pstmt.setInt(3, item.getQuantity());
//                 pstmt.setDouble(4, item.getPrice());

//                 // Xử lý thông tin bảo hành
//                 if (item.hasWarranty()) {
//                     pstmt.setString(5, item.getWarranty().getWarrantyType());
//                     pstmt.setDouble(6, item.getWarranty().getWarrantyPrice());
//                 } else {
//                     pstmt.setString(5, "Thường"); // Mặc định
//                     pstmt.setDouble(6, 0.0);
//                 }

//                 pstmt.addBatch();
//             }

//             int[] results = pstmt.executeBatch();
//             LOGGER.info("✅ Đã thêm " + results.length + " chi tiết đơn hàng");

//             // Cập nhật số lượng sản phẩm trong kho
//             updateProductQuantities(conn, cartItems);
//         }
//     }

//     /**
//      * Cập nhật số lượng sản phẩm trong kho sau khi thanh toán
//      */
//     private void updateProductQuantities(Connection conn, List<CartItemEmployee> cartItems) throws SQLException {
//         String updateSQL = "UPDATE Products SET quantity = quantity - ? WHERE productID = ?";

//         try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
//             for (CartItemEmployee item : cartItems) {
//                 pstmt.setInt(1, item.getQuantity());
//                 pstmt.setString(2, item.getProductID());
//                 pstmt.addBatch();
//             }

//             int[] results = pstmt.executeBatch();
//             LOGGER.info("✅ Đã cập nhật số lượng cho " + results.length + " sản phẩm");
//         }
//     }

//     /**
//      * Lấy thời gian hiện tại theo định dạng phù hợp với DB
//      */
//     private String getCurrentDateTime() {
//         LocalDateTime now = LocalDateTime.now();
//         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//         return now.format(formatter);
//     }

//     // Phương thức để nhận thông tin nhân viên từ màn hình login
//     public void initEmployeeData(Employee employee, String loginDateTime) {
//         try {
//             if (employee != null) {
//                 this.currentEmployee = employee;
//                 this.currentDateTime = loginDateTime;
//                 this.currentUser = employee.getUsername();

//                 // Dùng getFullName() - đảm bảo không gọi getName() vì có thể không có method này
//                 LOGGER.info("Đã khởi tạo POS với nhân viên: " + employee.getFullName());
//                 LOGGER.info("Thời gian hiện tại: " + currentDateTime);

//                 // Hiển thị thông tin nhân viên trên giao diện
//                 displayEmployeeInfo();
//             } else {
//                 LOGGER.warning("Lỗi: Employee object truyền vào là null");
//             }
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi khởi tạo dữ liệu nhân viên", e);
//         }
//     }

//     // Phương thức để nhận thông tin nhân viên từ màn hình login
//     public void setEmployeeInfo(int employeeID, String username) {
//         this.employeeId = employeeID; // Lưu employeeID vào biến instance
//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             // ĐÃ SỬA: Bọc trong try-catch để xử lý Exception từ getConnection()
//             try {
//                 conn = DBConfig.getConnection();
//             } catch (Exception ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi kết nối DB", ex);
//                 throw new SQLException("Không thể kết nối đến cơ sở dữ liệu: " + ex.getMessage());
//             }

//             if (conn == null) {
//                 throw new SQLException("Không thể kết nối đến cơ sở dữ liệu");
//             }

//             String query = "SELECT * FROM Employee WHERE employeeID = ? AND username = ?";
//             stmt = conn.prepareStatement(query);
//             stmt.setInt(1, employeeID);
//             stmt.setString(2, username);

//             rs = stmt.executeQuery();
//             if (rs.next()) {
//                 // Tạo đối tượng Employee từ ResultSet
//                 Employee emp = new Employee();
//                 emp.setEmployeeID(String.valueOf(employeeID));  // Chuyển int thành String
//                 emp.setUsername(rs.getString("username"));
//                 emp.setFullName(rs.getString("fullName"));
//                 emp.setEmail(rs.getString("email"));
//                 emp.setPhone(rs.getString("phone"));

//                 // Kiểm tra trước khi gọi setPosition
//                 try {
//                     int columnIndex = rs.findColumn("position");
//                     if (columnIndex > 0) {
//                         emp.setPosition(rs.getString("position"));
//                     }
//                 } catch (SQLException ex) {
//                     // Nếu không có cột position, bỏ qua
//                     LOGGER.info("Cột position không tồn tại trong bảng Employee");
//                 }

//                 // Gọi initEmployeeData với đối tượng Employee đã tạo
//                 String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//                 initEmployeeData(emp, currentTime);
//             } else {
//                 LOGGER.warning("Không tìm thấy nhân viên với ID=" + employeeID + " và username=" + username);
//                 Alert alert = new Alert(Alert.AlertType.WARNING);
//                 alert.setTitle("Cảnh báo");
//                 alert.setHeaderText("Không tìm thấy thông tin nhân viên");
//                 alert.setContentText("Vui lòng đăng nhập lại để tiếp tục.");
//                 alert.showAndWait();
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "Lỗi SQL khi lấy thông tin nhân viên", e);
//             Alert alert = new Alert(Alert.AlertType.ERROR);
//             alert.setTitle("Lỗi");
//             alert.setHeaderText("Không thể lấy thông tin nhân viên");
//             alert.setContentText("Chi tiết lỗi: " + e.getMessage());
//             alert.showAndWait();
//         } finally {
//             // Đóng tất cả các tài nguyên theo thứ tự ngược lại
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 // Không đóng connection ở đây vì có thể được sử dụng ở nơi khác
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi khi đóng tài nguyên SQL", ex);
//             }
//         }
//     }

//     // Hiển thị thông tin nhân viên trên giao diện - ĐÃ SỬA (FIX BUG 243)
//     private void displayEmployeeInfo() {
//         try {
//             if (currentEmployee != null && btnCheckout != null && btnCheckout.getParent() != null
//                     && btnCheckout.getParent().getParent() instanceof BorderPane) {

//                 BorderPane mainLayout = (BorderPane) btnCheckout.getParent().getParent();

//                 if (mainLayout.getTop() instanceof HBox) {
//                     HBox topBar = (HBox) mainLayout.getTop();

//                     // Tạo label hiển thị thông tin nhân viên
//                     Label lblEmployeeInfo = new Label(currentEmployee.getFullName() + " (" + currentUser + ")");
//                     lblEmployeeInfo.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

//                     // Tạo spacer để đẩy thông tin ra góc phải
//                     Region spacer = new Region();
//                     HBox.setHgrow(spacer, Priority.ALWAYS);

//                     // Thêm vào top bar
//                     topBar.getChildren().addAll(spacer, lblEmployeeInfo);
//                 }
//             }
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị thông tin nhân viên", e);
//         }
//     }

//     // Thêm nút đăng xuất
//     private void addLogoutButton() {
//         if (btnCheckout == null) {
//             LOGGER.warning("Lỗi: btnCheckout chưa được khởi tạo");
//             return;
//         }

//         Button btnLogout = new Button("ĐĂNG XUẤT");
//         btnLogout.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//         btnLogout.setPrefWidth(120);
//         btnLogout.setPrefHeight(35);
//         btnLogout.setOnAction(e -> logout());

//         if (btnCheckout.getParent() instanceof HBox) {
//             HBox parent = (HBox) btnCheckout.getParent();
//             parent.getChildren().add(0, btnLogout);
//         } else if (btnCheckout.getParent() instanceof Pane) {
//             Pane parent = (Pane) btnCheckout.getParent();
//             btnLogout.setLayoutX(btnCheckout.getLayoutX() - 130);
//             btnLogout.setLayoutY(btnCheckout.getLayoutY());
//             parent.getChildren().add(btnLogout);
//         }
//     }

//     // Xử lý đăng xuất
//     private void logout() {
//         try {
//             // Hiển thị xác nhận
//             Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
//             confirm.setTitle("Xác nhận đăng xuất");
//             confirm.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
//             confirm.setContentText("Mọi thay đổi chưa lưu sẽ bị mất.");

//             Optional<ButtonType> result = confirm.showAndWait();
//             if (result.isPresent() && result.get() == ButtonType.OK) {
//                 // Load màn hình đăng nhập
//                 URL loginUrl = getClass().getResource("/com/example/stores/view/employee_login.fxml");

//                 if (loginUrl != null) {
//                     FXMLLoader loader = new FXMLLoader(loginUrl);
//                     Parent root = loader.load();

//                     Scene scene = null;
//                     Stage stage = null;

//                     if (btnCheckout != null) {
//                         stage = (Stage) btnCheckout.getScene().getWindow();
//                         scene = new Scene(root);
//                         stage.setTitle("Computer Store - Đăng Nhập");
//                         stage.setScene(scene);
//                         stage.setResizable(false);
//                         stage.show();
//                     } else {
//                         LOGGER.warning("Lỗi: btnCheckout là null hoặc không thuộc Scene");
//                         stage = new Stage();
//                         scene = new Scene(root);
//                         stage.setTitle("Computer Store - Đăng Nhập");
//                         stage.setScene(scene);
//                         stage.setResizable(false);
//                         stage.show();

//                         // Đóng cửa sổ hiện tại nếu có
//                         if (productFlowPane != null && productFlowPane.getScene() != null) {
//                             Stage currentStage = (Stage) productFlowPane.getScene().getWindow();
//                             currentStage.close();
//                         }
//                     }

//                     LOGGER.info("Đã đăng xuất, thời gian: " +
//                             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//                 } else {
//                     throw new IOException("Không tìm thấy file employee_login.fxml");
//                 }
//             }
//         } catch (IOException e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi đăng xuất", e);
//             Alert alert = new Alert(Alert.AlertType.ERROR);
//             alert.setTitle("Lỗi");
//             alert.setContentText("Lỗi khi đăng xuất: " + e.getMessage());
//             alert.showAndWait();
//         }
//     }

//     // Thêm nút lịch sử đơn hàng
//     private void addHistoryButton() {
//         if (btnCheckout == null) {
//             LOGGER.warning("Lỗi: btnCheckout chưa được khởi tạo");
//             return;
//         }

//         Button btnHistory = new Button("LỊCH SỬ");
//         btnHistory.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
//         btnHistory.setPrefWidth(120);
//         btnHistory.setPrefHeight(35);
//         btnHistory.setOnAction(e -> showOrderHistoryInMemory()); // Sử dụng history trong bộ nhớ

//         if (btnCheckout.getParent() instanceof HBox) {
//             HBox parent = (HBox) btnCheckout.getParent();
//             parent.getChildren().add(0, btnHistory);
//         } else if (btnCheckout.getParent() instanceof Pane) {
//             Pane parent = (Pane) btnCheckout.getParent();
//             btnHistory.setLayoutX(btnCheckout.getLayoutX() - 130);
//             btnHistory.setLayoutY(btnCheckout.getLayoutY());
//             parent.getChildren().add(btnHistory);
//         }
//     }

//     // Cấu hình TableView giỏ hàng
//     // Đầu tiên em sửa hàm setupCartTable() để thêm cột bảo hành mới
//     private void setupCartTable() {
//         if (colCartName == null || colCartQty == null || colCartPrice == null || colCartTotal == null) {
//             LOGGER.warning("Lỗi: Các cột của TableView chưa được khởi tạo");
//             return;
//         }

//         // Thiết lập các cột cũ
//         colCartName.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleStringProperty("N/A");
//             }
//             String name = data.getValue().getProductName();
//             return new SimpleStringProperty(name != null ? name : "N/A");
//         });

//         colCartQty.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleIntegerProperty(0).asObject();
//             }
//             int qty = data.getValue().getQuantity();
//             return new SimpleIntegerProperty(qty).asObject();
//         });

//         colCartPrice.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleDoubleProperty(0).asObject();
//             }
//             double price = data.getValue().getPrice();
//             return new SimpleDoubleProperty(price).asObject();
//         });

//         colCartPrice.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//             @Override
//             protected void updateItem(Double price, boolean empty) {
//                 super.updateItem(price, empty);
//                 if (empty || price == null) {
//                     setText(null);
//                 } else {
//                     setText(String.format("%,.0f", price) + "đ");
//                 }
//             }
//         });

//         // THÊM CỘT BẢO HÀNH MỚI
//         colCartWarranty.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleStringProperty("Không");
//             }
//             CartItemEmployee item = data.getValue();
//             if (item.hasWarranty()) {
//                 return new SimpleStringProperty(item.getWarranty().getWarrantyType());
//             } else {
//                 return new SimpleStringProperty("Không");
//             }
//         });

//         // Nút sửa bảo hành
//         colCartWarranty.setCellFactory(tc -> new TableCell<CartItemEmployee, String>() {
//             @Override
//             protected void updateItem(String warrantyType, boolean empty) {
//                 super.updateItem(warrantyType, empty);
//                 if (empty) {
//                     setText(null);
//                     setGraphic(null);
//                 } else {
//                     HBox container = new HBox(5);
//                     container.setAlignment(Pos.CENTER_LEFT);

//                     // Hiển thị loại bảo hành
//                     Label lblType = new Label(warrantyType);

//                     // Nút sửa nhỏ bên cạnh
//                     Button btnEdit = new Button("⚙️");
//                     btnEdit.setStyle("-fx-background-color: transparent; -fx-padding: 0 2;");
//                     btnEdit.setOnAction(event -> {
//                         CartItemEmployee item = getTableView().getItems().get(getIndex());
//                         if (item != null) {
//                             showWarrantyEditDialog(item);
//                         }
//                     });

//                     container.getChildren().addAll(lblType, btnEdit);
//                     setGraphic(container);
//                     setText(null);
//                 }
//             }
//         });

//         colCartTotal.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleDoubleProperty(0).asObject();
//             }
//             double total = data.getValue().getTotalPrice();
//             return new SimpleDoubleProperty(total).asObject();
//         });

//         colCartTotal.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//             @Override
//             protected void updateItem(Double total, boolean empty) {
//                 super.updateItem(total, empty);
//                 if (empty || total == null) {
//                     setText(null);
//                 } else {
//                     setText(String.format("%,.0f", total) + "đ");
//                 }
//             }
//         });
//     }

//     // Sửa lại dialog chỉnh sửa bảo hành trong giỏ hàng
//     private void showWarrantyEditDialog(CartItemEmployee item) {
//         try {
//             // Tìm thông tin sản phẩm từ database để lấy giá
//             Product product = findProductById(item.getProductID());
//             if (product == null) {
//                 AlertUtil.showWarning("Lỗi", "Không tìm thấy thông tin sản phẩm");
//                 return;
//             }

//             Stage dialogStage = new Stage();
//             dialogStage.setTitle("Cập nhật bảo hành");
//             dialogStage.initModality(Modality.APPLICATION_MODAL);

//             VBox dialogContent = new VBox(15);
//             dialogContent.setPadding(new Insets(20));
//             dialogContent.setAlignment(Pos.CENTER);

//             // Tiêu đề và thông tin sản phẩm
//             Label lblTitle = new Label("Chọn gói bảo hành cho " + item.getProductName());
//             lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             // ComboBox chọn loại bảo hành - SỬA LẠI CÒN 2 LOẠI
//             ComboBox<String> cbWarranty = new ComboBox<>();

//             // Kiểm tra điều kiện bảo hành thường
//             boolean isEligibleForStdWarranty = WarrantyCalculator.isEligibleForStandardWarranty(product);

//             if (isEligibleForStdWarranty) {
//                 // Chỉ còn 2 lựa chọn
//                 cbWarranty.getItems().addAll("Không", "Thường", "Vàng");
//             } else {
//                 // Sản phẩm không đủ điều kiện bảo hành
//                 cbWarranty.getItems().add("Không");
//             }

//             // Set giá trị hiện tại
//             if (item.hasWarranty()) {
//                 String currentType = item.getWarranty().getWarrantyType();
//                 // Chuyển đổi các loại bảo hành cũ (nếu có)
//                 if (!currentType.equals("Thường") && !currentType.equals("Vàng")) {
//                     currentType = "Thường"; // Mặc định về Thường
//                 }

//                 if (cbWarranty.getItems().contains(currentType)) {
//                     cbWarranty.setValue(currentType);
//                 } else {
//                     cbWarranty.setValue("Không");
//                 }
//             } else {
//                 cbWarranty.setValue("Không");
//             }

//             // Hiển thị giá bảo hành
//             Label lblWarrantyPrice = new Label("Phí bảo hành: 0đ");
//             Label lblTotalWithWarranty = new Label("Tổng tiền: " + String.format("%,.0f", item.getTotalPrice()) + "đ");
//             lblTotalWithWarranty.setStyle("-fx-font-weight: bold;");

//             // Thêm mô tả bảo hành
//             Label lblWarrantyInfo = new Label("Không bảo hành");
//             lblWarrantyInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #757575;");

//             // Cập nhật giá khi thay đổi loại bảo hành
//             cbWarranty.setOnAction(e -> {
//                 String selectedType = cbWarranty.getValue();

//                 // TH1: Không bảo hành
//                 if (selectedType.equals("Không")) {
//                     lblWarrantyPrice.setText("Phí bảo hành: 0đ");
//                     double basePrice = product.getPrice() * item.getQuantity();
//                     lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,.0f", basePrice) + "đ");
//                     lblWarrantyInfo.setText("Không bảo hành cho sản phẩm này");
//                     lblWarrantyInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #757575;");
//                     return;
//                 }

//                 // TH2: Bảo hành thường
//                 if (selectedType.equals("Thường")) {
//                     lblWarrantyPrice.setText("Phí bảo hành: 0đ");
//                     double basePrice = product.getPrice() * item.getQuantity();
//                     lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,.0f", basePrice) + "đ");
//                     lblWarrantyInfo.setText("Bảo hành thường miễn phí 12 tháng");
//                     lblWarrantyInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #4CAF50;");
//                     return;
//                 }

//                 // TH3: Bảo hành vàng (10% giá gốc)
//                 double warrantyFee = product.getPrice() * 0.1 * item.getQuantity();
//                 lblWarrantyPrice.setText("Phí bảo hành: " + String.format("%,.0f", warrantyFee) + "đ");

//                 // Cập nhật tổng tiền
//                 double totalPrice = (product.getPrice() * item.getQuantity()) + warrantyFee;
//                 lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,.0f", totalPrice) + "đ");

//                 lblWarrantyInfo.setText("✨ Bảo hành Vàng 24 tháng, 1 đổi 1");
//                 lblWarrantyInfo.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF9800;");
//             });

//             // Nút lưu và hủy
//             Button btnSave = new Button("Lưu thay đổi");
//             btnSave.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnSave.setPrefWidth(140);
//             btnSave.setOnAction(e -> {
//                 String selectedType = cbWarranty.getValue();

//                 if ("Không".equals(selectedType)) {
//                     // Xóa bảo hành nếu chọn không bảo hành
//                     item.setWarranty(null);
//                 } else {
//                     // Tạo bảo hành mới với loại đã chọn
//                     Warranty warranty = WarrantyCalculator.createWarranty(product, selectedType);
//                     item.setWarranty(warranty);
//                 }

//                 // Cập nhật hiển thị
//                 updateCartDisplay();
//                 dialogStage.close();
//                 AlertUtil.showInformation("Thành công", "Đã cập nhật bảo hành cho sản phẩm");
//             });

//             Button btnCancel = new Button("Hủy");
//             btnCancel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnCancel.setPrefWidth(80);
//             btnCancel.setOnAction(e -> dialogStage.close());

//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.getChildren().addAll(btnSave, btnCancel);

//             // Thêm các thành phần vào dialog
//             dialogContent.getChildren().addAll(
//                     lblTitle,
//                     new Separator(),
//                     cbWarranty,
//                     lblWarrantyInfo,
//                     lblWarrantyPrice,
//                     lblTotalWithWarranty,
//                     buttonBox
//             );

//             // Hiện dialog
//             Scene scene = new Scene(dialogContent, 350, 320);
//             dialogStage.setScene(scene);
//             dialogStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị dialog chỉnh sửa bảo hành", e);
//             AlertUtil.showError("Lỗi", "Không thể mở cửa sổ chỉnh sửa bảo hành");
//         }
//     }

//     // Thêm nút xóa vào bảng giỏ hàng
//     private void addButtonsToTable() {
//         if (cartTable == null) {
//             LOGGER.warning("Lỗi: cartTable chưa được khởi tạo");
//             return;
//         }

//         colCartAction = new TableColumn<>("Xóa");
//         colCartAction.setCellFactory(param -> new TableCell<CartItemEmployee, Void>() {
//             private final Button btnDelete = new Button("X");

//             {
//                 btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//                 btnDelete.setOnAction(event -> {
//                     CartItemEmployee item = getTableRow().getItem();
//                     if (item != null) {
//                         // Hiện dialog xác nhận trước khi xóa
//                         Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
//                                 "Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?",
//                                 ButtonType.YES, ButtonType.NO);
//                         alert.setTitle("Xác nhận xóa");
//                         alert.setHeaderText("Xóa sản phẩm");

//                         Optional<ButtonType> result = alert.showAndWait();
//                         if (result.isPresent() && result.get() == ButtonType.YES) {
//                             cartItems.remove(item);
//                             updateTotal();
//                         }
//                     }
//                 });
//             }

//             @Override
//             protected void updateItem(Void item, boolean empty) {
//                 super.updateItem(item, empty);
//                 if (empty) {
//                     setGraphic(null);
//                 } else {
//                     setGraphic(btnDelete);
//                 }
//             }
//         });

//         colCartAction.setPrefWidth(50);

//         // Thêm cột vào TableView nếu chưa có
//         if (!cartTable.getColumns().contains(colCartAction)) {
//             cartTable.getColumns().add(colCartAction);
//         }
//     }

//     // Hiển thị thông báo lỗi
//     private void showErrorAlert(String message) {
//         Alert alert = new Alert(Alert.AlertType.WARNING, message);
//         alert.setTitle("Lỗi");
//         alert.setHeaderText("Thông tin không hợp lệ");
//         alert.showAndWait();
//     }


//     // Thêm method mới vào PosOverviewController
//     private void showOrderByIdWindow(String orderIdInput) {
//         try {
//             LOGGER.info("🔍 Tìm kiếm đơn hàng với ID: " + orderIdInput);

//             // Chuẩn hóa orderID (có thể người dùng nhập 1, 2, 3 hoặc ORD001, ORD002)
//             String searchOrderId = normalizeOrderId(orderIdInput);
//             LOGGER.info("📝 OrderID sau khi chuẩn hóa: " + searchOrderId);

//             // Tìm đơn hàng trong database
//             OrderHistoryServiceE.OrderWithDetails orderData = OrderHistoryServiceE.getCompleteOrderById(searchOrderId);

//             if (orderData == null || orderData.getOrderHistory() == null) {
//                 AlertUtil.showWarning("Không tìm thấy",
//                         "Không tìm thấy đơn hàng với mã: " + orderIdInput + "\nĐã thử tìm: " + searchOrderId);
//                 return;
//             }

//             OrderHistory order = orderData.getOrderHistory();
//             ObservableList<OrderDetail> details = orderData.getOrderDetails();

//             LOGGER.info("✅ Tìm thấy đơn hàng: " + order.getOrderID() + " với " + details.size() + " sản phẩm");

//             // Tạo cửa sổ hiển thị chi tiết
//             showSingleOrderDetailWindow(order, details);

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi tìm đơn hàng theo ID: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể tìm đơn hàng: " + e.getMessage());
//         }
//     }

//     // Helper method chuẩn hóa orderID
//     private String normalizeOrderId(String input) {
//         if (input == null || input.trim().isEmpty()) {
//             return input;
//         }

//         String trimmed = input.trim();

//         // Nếu đã có định dạng ORDxxx thì giữ nguyên
//         if (trimmed.toUpperCase().startsWith("ORD")) {
//             return trimmed;
//         }

//         // Nếu là số thuần túy, thử cả 2 cách
//         try {
//             int numericId = Integer.parseInt(trimmed);
//             // Thử format ORD001 trước
//             return String.format("ORD%03d", numericId);
//         } catch (NumberFormatException e) {
//             // Nếu không phải số, trả về nguyên input
//             return trimmed;
//         }
//     }
//     // Thêm method hiển thị chi tiết đơn hàng
//     private void showSingleOrderDetailWindow(OrderHistory order, ObservableList<OrderDetail> details) {
//         try {
//             Stage detailStage = new Stage();
//             detailStage.initModality(Modality.APPLICATION_MODAL);
//             detailStage.setTitle("Chi tiết đơn hàng #" + order.getOrderID());
//             detailStage.setResizable(true);

//             BorderPane mainLayout = new BorderPane();

//             // Header đẹp
//             HBox header = new HBox();
//             header.setAlignment(Pos.CENTER);
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: linear-gradient(to right, #4CAF50, #45a049);");

//             Label headerTitle = new Label("CHI TIẾT ĐƠN HÀNG #" + order.getOrderID());
//             headerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
//             header.getChildren().add(headerTitle);

//             // Content
//             VBox content = new VBox(15);
//             content.setPadding(new Insets(20));

//             // Thông tin đơn hàng
//             GridPane infoGrid = new GridPane();
//             infoGrid.setHgap(15);
//             infoGrid.setVgap(10);
//             infoGrid.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8;");

//             int row = 0;
//             addInfoRow(infoGrid, "Mã đơn hàng:", order.getOrderID(), row++);
//             addInfoRow(infoGrid, "Ngày đặt:", order.getFormattedDate(), row++);
//             addInfoRow(infoGrid, "Khách hàng:", order.getCustomerName(), row++);
//             addInfoRow(infoGrid, "Số điện thoại:", order.getCustomerPhone(), row++);
//             addInfoRow(infoGrid, "Nhân viên:", order.getEmployeeName(), row++);
//             addInfoRow(infoGrid, "Phương thức thanh toán:", order.getPaymentMethod(), row++);
//             addInfoRow(infoGrid, "Trạng thái:", order.getStatus(), row++);

//             // Bảng sản phẩm
//             Label productsLabel = new Label("DANH SÁCH SẢN PHẨM:");
//             productsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             TableView<OrderDetail> productsTable = new TableView<>();
//             productsTable.setPrefHeight(300);
//             productsTable.setItems(details);

//             // Các cột
//             TableColumn<OrderDetail, String> colProductName = new TableColumn<>("Tên sản phẩm");
//             colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
//             colProductName.setPrefWidth(250);

//             TableColumn<OrderDetail, Integer> colQuantity = new TableColumn<>("SL");
//             colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
//             colQuantity.setPrefWidth(50);

//             TableColumn<OrderDetail, String> colUnitPrice = new TableColumn<>("Đơn giá");
//             colUnitPrice.setCellValueFactory(data ->
//                     new SimpleStringProperty(String.format("%,.0f₫", data.getValue().getUnitPrice())));
//             colUnitPrice.setPrefWidth(100);

//             TableColumn<OrderDetail, String> colWarranty = new TableColumn<>("Bảo hành");
//             colWarranty.setCellValueFactory(new PropertyValueFactory<>("warrantyType"));
//             colWarranty.setPrefWidth(100);

//             TableColumn<OrderDetail, String> colSubtotal = new TableColumn<>("Thành tiền");
//             colSubtotal.setCellValueFactory(data ->
//                     new SimpleStringProperty(String.format("%,.0f₫", data.getValue().getSubtotal())));
//             colSubtotal.setPrefWidth(120);

//             productsTable.getColumns().addAll(colProductName, colQuantity, colUnitPrice, colWarranty, colSubtotal);

//             // Tổng tiền
//             Label totalLabel = new Label("TỔNG TIỀN: " + order.getFormattedAmount());
//             totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e91e63;");

//             // Buttons
//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.setPadding(new Insets(10, 0, 0, 0));

//             Button btnPrint = new Button("In hóa đơn");
//             btnPrint.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnPrint.setPrefWidth(120);
//             btnPrint.setOnAction(e -> {
//                 // Gọi method in hóa đơn (sử dụng lại code cũ)
//                 AlertUtil.showInfo("Thông báo", "Tính năng in hóa đơn đang được phát triển!");
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnClose.setPrefWidth(100);
//             btnClose.setOnAction(e -> detailStage.close());

//             buttonBox.getChildren().addAll(btnPrint, btnClose);

//             // Thêm vào content
//             content.getChildren().addAll(infoGrid, productsLabel, productsTable, totalLabel, buttonBox);

//             // Layout chính
//             mainLayout.setTop(header);
//             mainLayout.setCenter(new ScrollPane(content));

//             Scene scene = new Scene(mainLayout, 700, 600);
//             detailStage.setScene(scene);
//             detailStage.show();

//             LOGGER.info("✅ Đã hiển thị chi tiết đơn hàng: " + order.getOrderID());

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi hiển thị chi tiết đơn hàng: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị chi tiết đơn hàng: " + e.getMessage());
//         }
//     }

//     // Helper method thêm dòng thông tin
//     private void addInfoRow(GridPane grid, String label, String value, int row) {
//         Label lblLabel = new Label(label);
//         lblLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");

//         Label lblValue = new Label(value != null ? value : "N/A");
//         lblValue.setStyle("-fx-font-weight: bold;");

//         grid.add(lblLabel, 0, row);
//         grid.add(lblValue, 1, row);
//     }
//     // Method hiển thị tất cả đơn hàng (nếu user chọn checkbox)
//     private void showAllOrdersWindow() {
//         try {
//             LOGGER.info("📋 Hiển thị tất cả đơn hàng...");

//             ObservableList<OrderHistory> allOrders = OrderHistoryServiceE.getOrderHistories();

//             if (allOrders.isEmpty()) {
//                 AlertUtil.showInfo("Thông báo", "Không có đơn hàng nào trong hệ thống!");
//                 return;
//             }

//             // Tạo cửa sổ đơn giản hiển thị danh sách
//             Stage listStage = new Stage();
//             listStage.setTitle("Tất cả đơn hàng (" + allOrders.size() + " đơn)");
//             listStage.setResizable(true);

//             // TableView đơn giản
//             TableView<OrderHistory> table = new TableView<>();
//             table.setItems(allOrders);

//             TableColumn<OrderHistory, String> colId = new TableColumn<>("Mã ĐH");
//             colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrderID()));
//             colId.setPrefWidth(100);

//             TableColumn<OrderHistory, String> colDate = new TableColumn<>("Ngày");
//             colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedDate()));
//             colDate.setPrefWidth(150);

//             TableColumn<OrderHistory, String> colCustomer = new TableColumn<>("Khách hàng");
//             colCustomer.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomerName()));
//             colCustomer.setPrefWidth(150);

//             TableColumn<OrderHistory, String> colTotal = new TableColumn<>("Tổng tiền");
//             colTotal.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedAmount()));
//             colTotal.setPrefWidth(120);

//             TableColumn<OrderHistory, Void> colAction = new TableColumn<>("Hành động");
//             colAction.setCellFactory(tc -> new TableCell<OrderHistory, Void>() {
//                 private final Button btn = new Button("Xem chi tiết");
//                 {
//                     btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
//                     btn.setOnAction(event -> {
//                         OrderHistory selectedOrder = getTableView().getItems().get(getIndex());
//                         if (selectedOrder != null) {
//                             listStage.close();
//                             showOrderByIdWindow(selectedOrder.getOrderID());
//                         }
//                     });
//                 }

//                 @Override
//                 protected void updateItem(Void item, boolean empty) {
//                     super.updateItem(item, empty);
//                     if (empty) {
//                         setGraphic(null);
//                     } else {
//                         setGraphic(btn);
//                     }
//                 }
//             });
//             colAction.setPrefWidth(120);

//             table.getColumns().addAll(colId, colDate, colCustomer, colTotal, colAction);

//             Scene scene = new Scene(new VBox(table), 800, 500);
//             listStage.setScene(scene);
//             listStage.show();

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi hiển thị tất cả đơn hàng: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị danh sách đơn hàng: " + e.getMessage());
//         }
//     }
//     // Hiển thị lịch sử đơn hàng từ bộ nhớ
//     // Thay thế method showOrderHistoryInMemory() cũ
//     private void showOrderHistoryInMemory() {
//         try {
//             // Tạo dialog nhập mã đơn hàng
//             Stage searchStage = new Stage();
//             searchStage.initModality(Modality.APPLICATION_MODAL);
//             searchStage.setTitle("Tìm kiếm đơn hàng");
//             searchStage.setResizable(false);

//             VBox layout = new VBox(15);
//             layout.setPadding(new Insets(20));
//             layout.setAlignment(Pos.CENTER);

//             // Header
//             Label headerLabel = new Label("TÌM KIẾM ĐƠN HÀNG");
//             headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

//             // Input mã đơn hàng
//             Label lblOrderId = new Label("Nhập mã đơn hàng:");
//             lblOrderId.setStyle("-fx-font-weight: bold;");

//             TextField txtOrderId = new TextField();
//             txtOrderId.setPromptText("Ví dụ: 1, 2, 3... hoặc ORD001, ORD002...");
//             txtOrderId.setPrefWidth(300);
//             txtOrderId.setStyle("-fx-font-size: 14px;");

//             // Hoặc xem tất cả
//             CheckBox chkShowAll = new CheckBox("Hiển thị tất cả đơn hàng");
//             chkShowAll.setStyle("-fx-font-size: 12px;");

//             // Buttons
//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);

//             Button btnSearch = new Button("Tìm kiếm");
//             btnSearch.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnSearch.setPrefWidth(100);

//             Button btnCancel = new Button("Hủy");
//             btnCancel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnCancel.setPrefWidth(100);

//             buttonBox.getChildren().addAll(btnSearch, btnCancel);

//             // Events
//             btnCancel.setOnAction(e -> searchStage.close());

//             btnSearch.setOnAction(e -> {
//                 try {
//                     searchStage.close();

//                     if (chkShowAll.isSelected()) {
//                         // Hiển thị tất cả đơn hàng
//                         showAllOrdersWindow();
//                     } else {
//                         // Tìm theo ID cụ thể
//                         String orderId = txtOrderId.getText().trim();
//                         if (orderId.isEmpty()) {
//                             AlertUtil.showWarning("Thông báo", "Vui lòng nhập mã đơn hàng!");
//                             return;
//                         }
//                         showOrderByIdWindow(orderId);
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi tìm kiếm đơn hàng: " + ex.getMessage(), ex);
//                     AlertUtil.showError("Lỗi", "Không thể tìm kiếm đơn hàng: " + ex.getMessage());
//                 }
//             });

//             // Enter để tìm kiếm
//             txtOrderId.setOnKeyPressed(event -> {
//                 if (event.getCode().toString().equals("ENTER")) {
//                     btnSearch.fire();
//                 }
//             });

//             layout.getChildren().addAll(headerLabel, lblOrderId, txtOrderId, chkShowAll, buttonBox);

//             Scene scene = new Scene(layout, 400, 250);
//             searchStage.setScene(scene);
//             searchStage.showAndWait();

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị dialog tìm kiếm: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể mở cửa sổ tìm kiếm: " + e.getMessage());
//         }
//     }

//     // Hiển thị chi tiết đơn hàng từ bộ nhớ
//     private void showOrderDetailsFromMemory(OrderSummary order) {
//         try {
//             if (order == null) {
//                 LOGGER.warning("Lỗi: OrderSummary object là null");
//                 return;
//             }

//             Stage detailStage = new Stage();
//             detailStage.initModality(Modality.APPLICATION_MODAL);
//             detailStage.setTitle("Chi tiết đơn hàng #" + order.getId());

//             BorderPane borderPane = new BorderPane();

//             // Header
//             HBox header = new HBox();
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: #2196F3;");

//             Label headerTitle = new Label("CHI TIẾT ĐƠN HÀNG #" + order.getId());
//             headerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

//             header.getChildren().add(headerTitle);
//             header.setAlignment(Pos.CENTER);

//             borderPane.setTop(header);

//             // Content
//             VBox content = new VBox(15);
//             content.setPadding(new Insets(20));

//             // Thông tin đơn hàng
//             VBox orderInfoBox = new VBox(8);
//             orderInfoBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");

//             Label lblCustomer = new Label("Khách hàng: " + order.getCustomerName());
//             Label lblPhone = new Label("SĐT: " + order.getCustomerPhone());
//             Label lblPayment = new Label("Phương thức thanh toán: " + order.getPaymentMethod());
//             Label lblDate = new Label("Ngày mua: " + order.getOrderDate());

//             orderInfoBox.getChildren().addAll(lblCustomer, lblPhone, lblPayment, lblDate);

//             // Danh sách sản phẩm
//             Label lblProductsTitle = new Label("Danh sách sản phẩm:");
//             lblProductsTitle.setStyle("-fx-font-weight: bold;");

//             TableView<CartItemEmployee> detailTable = new TableView<>();
//             detailTable.setPrefHeight(300);

//             TableColumn<CartItemEmployee, String> colProductName = new TableColumn<>("Tên sản phẩm");
//             colProductName.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleStringProperty("N/A");
//                 }
//                 String productName = data.getValue().getProductName();
//                 return new SimpleStringProperty(productName != null ? productName : "N/A");
//             });
//             colProductName.setPrefWidth(200);

//             TableColumn<CartItemEmployee, Integer> colQuantity = new TableColumn<>("SL");
//             colQuantity.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleIntegerProperty(0).asObject();
//                 }
//                 return new SimpleIntegerProperty(data.getValue().getQuantity()).asObject();
//             });
//             colQuantity.setPrefWidth(50);

//             TableColumn<CartItemEmployee, Double> colPrice = new TableColumn<>("Đơn giá");
//             colPrice.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleDoubleProperty(0).asObject();
//                 }
//                 return new SimpleDoubleProperty(data.getValue().getPrice()).asObject();
//             });
//             colPrice.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double price, boolean empty) {
//                     super.updateItem(price, empty);
//                     if (empty || price == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", price) + "đ");
//                     }
//                 }
//             });
//             colPrice.setPrefWidth(100);

//             // Thêm cột bảo hành
//             TableColumn<CartItemEmployee, String> colWarranty = new TableColumn<>("Bảo hành");
//             colWarranty.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleStringProperty("Không");
//                 }

//                 CartItemEmployee item = data.getValue();
//                 if (item.hasWarranty()) {
//                     return new SimpleStringProperty(item.getWarranty().getWarrantyType());
//                 } else {
//                     return new SimpleStringProperty("Không");
//                 }
//             });
//             colWarranty.setPrefWidth(100);

//             TableColumn<CartItemEmployee, Double> colSubtotal = new TableColumn<>("Thành tiền");
//             colSubtotal.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleDoubleProperty(0).asObject();
//                 }
//                 return new SimpleDoubleProperty(data.getValue().getTotalPrice()).asObject();
//             });
//             colSubtotal.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double total, boolean empty) {
//                     super.updateItem(total, empty);
//                     if (empty || total == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", total) + "đ");
//                     }
//                 }
//             });
//             colSubtotal.setPrefWidth(100);

//             detailTable.getColumns().addAll(colProductName, colQuantity, colPrice, colWarranty, colSubtotal);

//             // Kiểm tra null trước khi thêm items
//             if (order.getItems() != null) {
//                 detailTable.setItems(FXCollections.observableArrayList(order.getItems()));
//             } else {
//                 detailTable.setItems(FXCollections.observableArrayList());
//             }

//             // Hiển thị tổng tiền
//             Label lblTotal = new Label("Tổng tiền: " + String.format("%,.0f", order.getTotalAmount()) + "đ");
//             lblTotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e91e63;");

//             // Button in hóa đơn và đóng
//             Button btnPrint = new Button("In hóa đơn");
//             btnPrint.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnPrint.setPrefWidth(150);

//             // Fix lỗi lambda expression bằng cách sử dụng final variable
//             final int orderId = order.getId();
//             final double totalAmount = order.getTotalAmount();
//             final String customerName2 = order.getCustomerName();
//             final String customerPhone2 = order.getCustomerPhone();
//             final String paymentMethod2 = order.getPaymentMethod();
//             final String orderDateTime = order.getOrderDate();
//             final List<CartItemEmployee> orderItems = order.getItems() != null ? order.getItems() : new ArrayList<>();

//             btnPrint.setOnAction(e -> {
//                 try {
//                     // In hóa đơn với các biến final
//                     printReceiptWithPaymentMethod(
//                             orderId,
//                             orderItems,
//                             totalAmount,
//                             customerName2,
//                             customerPhone2,
//                             paymentMethod2,
//                             orderDateTime,
//                             currentUser
//                     );
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi in hóa đơn", ex);
//                     showErrorAlert("Có lỗi xảy ra: " + ex.getMessage());
//                 }
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnClose.setPrefWidth(100);
//             btnClose.setOnAction(e -> detailStage.close());

//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.getChildren().addAll(btnPrint, btnClose);
//             buttonBox.setPadding(new Insets(10, 0, 0, 0));

//             content.getChildren().addAll(orderInfoBox, lblProductsTitle, detailTable, lblTotal, buttonBox);

//             borderPane.setCenter(content);

//             Scene scene = new Scene(borderPane, 650, 550);
//             detailStage.setScene(scene);
//             detailStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị chi tiết đơn hàng", e);
//             showErrorAlert("Có lỗi xảy ra: " + e.getMessage());
//         }
//     }

//     // Phương thức in hóa đơn có thêm phương thức thanh toán và thông tin bảo hành
//     public void printReceiptWithPaymentMethod(int orderID, List<CartItemEmployee> items, double totalAmount,
//                                               String customerName, String customerPhone, String paymentMethod,
//                                               String orderDateTime, String cashierName) {
//         try {
//             // Kiểm tra danh sách sản phẩm
//             if (items == null || items.isEmpty()) {
//                 Alert alert = new Alert(Alert.AlertType.WARNING);
//                 alert.setTitle("Cảnh báo");
//                 alert.setHeaderText("Không thể in hóa đơn");
//                 alert.setContentText("Không có sản phẩm nào trong đơn hàng.");
//                 alert.showAndWait();
//                 return;
//             }

//             // Tạo cảnh báo để hiển thị trước khi in
//             Alert printingAlert = new Alert(Alert.AlertType.INFORMATION);
//             printingAlert.setTitle("Đang in hóa đơn");
//             printingAlert.setHeaderText("Đang chuẩn bị in hóa đơn");
//             printingAlert.setContentText("Vui lòng đợi trong giây lát...");
//             printingAlert.show();

//             // Tạo nội dung hóa đơn
//             VBox receiptContent = new VBox(5);
//             receiptContent.setPadding(new Insets(20));
//             receiptContent.setStyle("-fx-background-color: white;");

//             // Tiêu đề
//             Label lblTitle = new Label("HÓA ĐƠN THANH TOÁN");
//             lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-alignment: center;");
//             lblTitle.setMaxWidth(Double.MAX_VALUE);
//             lblTitle.setAlignment(Pos.CENTER);

//             // Logo công ty (nếu có)
//             ImageView logo = new ImageView();
//             try {
//                 InputStream is = getClass().getResourceAsStream("/com/example/stores/images/layout/employee_logo.png");
//                 if (is != null) {
//                     logo.setImage(new Image(is));
//                     logo.setFitWidth(100);
//                     logo.setPreserveRatio(true);
//                 }
//             } catch (Exception e) {
//                 LOGGER.log(Level.WARNING, "Không tìm thấy logo", e);
//             }

//             // Thông tin cửa hàng
//             Label lblStoreName = new Label("COMPUTER STORE");
//             lblStoreName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             Label lblStoreAddress = new Label("Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM");
//             Label lblStorePhone = new Label("Điện thoại: 028.1234.5678");

//             // Thông tin hóa đơn
//             Label lblOrderID = new Label("Mã đơn hàng: #" + orderID);
//             lblOrderID.setStyle("-fx-font-weight: bold;");

//             Label lblDateTime = new Label("Ngày: " + orderDateTime);
//             Label lblCashier = new Label("Thu ngân: " + cashierName);
//             Label lblCustomerName = new Label("Khách hàng: " + customerName);
//             Label lblCustomerPhone = new Label("SĐT khách hàng: " + customerPhone);
//             Label lblPaymentMethod = new Label("Phương thức thanh toán: " + paymentMethod);
//             lblPaymentMethod.setStyle("-fx-font-weight: bold;");

//             // Tạo đường kẻ ngăn cách
//             Separator sep1 = new Separator();
//             sep1.setMaxWidth(Double.MAX_VALUE);

//             // Tiêu đề bảng sản phẩm
//             HBox tableHeader = new HBox(10);
//             Label lblProductHeader = new Label("Sản phẩm");
//             lblProductHeader.setPrefWidth(200);
//             lblProductHeader.setStyle("-fx-font-weight: bold;");

//             Label lblQtyHeader = new Label("SL");
//             lblQtyHeader.setPrefWidth(50);
//             lblQtyHeader.setStyle("-fx-font-weight: bold;");

//             Label lblPriceHeader = new Label("Đơn giá");
//             lblPriceHeader.setPrefWidth(100);
//             lblPriceHeader.setStyle("-fx-font-weight: bold;");

//             Label lblWarrantyHeader = new Label("Bảo hành");
//             lblWarrantyHeader.setPrefWidth(100);
//             lblWarrantyHeader.setStyle("-fx-font-weight: bold;");

//             Label lblSubtotalHeader = new Label("Thành tiền");
//             lblSubtotalHeader.setPrefWidth(100);
//             lblSubtotalHeader.setStyle("-fx-font-weight: bold;");

//             tableHeader.getChildren().addAll(lblProductHeader, lblQtyHeader, lblPriceHeader, lblWarrantyHeader, lblSubtotalHeader);

//             // Danh sách sản phẩm
//             VBox productsBox = new VBox(5);
//             double totalWarrantyPrice = 0.0; // Tổng phí bảo hành

//             for (CartItemEmployee item : items) {
//                 if (item == null) continue;

//                 // Dòng sản phẩm
//                 HBox row = new HBox(10);

//                 String productName = item.getProductName();
//                 if (productName == null) productName = "Sản phẩm không tên";

//                 // Tạo VBox để hiển thị tên sản phẩm + bảo hành nếu có
//                 VBox productInfoBox = new VBox(2);
//                 Label lblProduct = new Label(productName);
//                 lblProduct.setPrefWidth(200);
//                 lblProduct.setWrapText(true);
//                 productInfoBox.getChildren().add(lblProduct);

//                 Label lblQty = new Label(String.valueOf(item.getQuantity()));
//                 lblQty.setPrefWidth(50);

//                 Label lblPrice = new Label(String.format("%,.0f", item.getPrice()) + "đ");
//                 lblPrice.setPrefWidth(100);

//                 // Hiển thị thông tin bảo hành
//                 Label lblWarranty;
//                 if (item.hasWarranty()) {
//                     lblWarranty = new Label(item.getWarranty().getWarrantyType());
//                     totalWarrantyPrice += item.getWarranty().getWarrantyPrice();
//                 } else {
//                     lblWarranty = new Label("Không");
//                 }
//                 lblWarranty.setPrefWidth(100);

//                 // Hiển thị tổng giá trị sản phẩm
//                 Label lblSubtotal = new Label(String.format("%,.0f", item.getTotalPrice()) + "đ");
//                 lblSubtotal.setPrefWidth(100);

//                 row.getChildren().addAll(productInfoBox, lblQty, lblPrice, lblWarranty, lblSubtotal);
//                 productsBox.getChildren().add(row);
//             }

//             // Thêm đường kẻ ngăn cách
//             Separator sep2 = new Separator();
//             sep2.setMaxWidth(Double.MAX_VALUE);

//             // Hiển thị tổng phí bảo hành nếu có
//             VBox summaryBox = new VBox(5);

//             if (totalWarrantyPrice > 0) {
//                 HBox warrantyRow = new HBox(10);
//                 warrantyRow.setAlignment(Pos.CENTER_RIGHT);

//                 Label lblWarrantyTotalHeader = new Label("Tổng phí bảo hành:");
//                 Label lblWarrantyValue = new Label(String.format("%,.0f", totalWarrantyPrice) + "đ");
//                 lblWarrantyValue.setStyle("-fx-font-size: 13px;");

//                 warrantyRow.getChildren().addAll(lblWarrantyHeader, lblWarrantyValue);
//                 summaryBox.getChildren().add(warrantyRow);
//             }

//             // Tổng tiền
//             HBox totalRow = new HBox(10);
//             totalRow.setAlignment(Pos.CENTER_RIGHT);

//             Label lblTotalHeader = new Label("Tổng tiền thanh toán:");
//             lblTotalHeader.setStyle("-fx-font-weight: bold;");

//             Label lblTotalValue = new Label(String.format("%,.0f", totalAmount) + "đ");
//             lblTotalValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             totalRow.getChildren().addAll(lblTotalHeader, lblTotalValue);
//             summaryBox.getChildren().add(totalRow);

//             // Thêm thông tin thanh toán chuyển khoản nếu là phương thức chuyển khoản
//             VBox paymentInfoBox = new VBox(10);
//             paymentInfoBox.setAlignment(Pos.CENTER);

//             if ("Chuyển khoản".equals(paymentMethod)) {
//                 // Thêm đường kẻ ngăn cách
//                 Separator sepPayment = new Separator();
//                 sepPayment.setMaxWidth(Double.MAX_VALUE);

//                 Label lblPaymentInfo = new Label("THÔNG TIN CHUYỂN KHOẢN");
//                 lblPaymentInfo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
//                 lblPaymentInfo.setAlignment(Pos.CENTER);
//                 lblPaymentInfo.setMaxWidth(Double.MAX_VALUE);

//                 Label lblBank = new Label("Ngân hàng: TECHCOMBANK");
//                 Label lblAccount = new Label("Số tài khoản: 1903 5552 6789");
//                 Label lblAccountName = new Label("Chủ TK: CÔNG TY COMPUTER STORE");
//                 Label lblContent = new Label("Nội dung CK: " + orderID + " " + customerPhone);

//                 // QR Code cho chuyển khoản
//                 ImageView qrCode = new ImageView();
//                 try {
//                     // Mặc định sử dụng ảnh QR từ resources
//                     InputStream qrIs = getClass().getResourceAsStream("/com/example/stores/images/qr_payment.png");
//                     if (qrIs != null) {
//                         qrCode.setImage(new Image(qrIs));
//                         qrCode.setFitWidth(150);
//                         qrCode.setPreserveRatio(true);
//                     } else {
//                         // QR Code cho chuyển khoản - tạo ảnh trống nếu không tìm thấy
//                         qrCode.setFitWidth(150);
//                         qrCode.setFitHeight(150);
//                         qrCode.setStyle("-fx-background-color: #f0f0f0;");
//                     }
//                 } catch (Exception e) {
//                     LOGGER.log(Level.WARNING, "Không tìm thấy ảnh QR", e);
//                 }

//                 paymentInfoBox.getChildren().addAll(sepPayment, lblPaymentInfo, lblBank, lblAccount, lblAccountName, lblContent, qrCode);
//             }

//             // Thông tin cuối hóa đơn
//             Label lblThankYou = new Label("Cảm ơn quý khách đã mua hàng!");
//             lblThankYou.setAlignment(Pos.CENTER);
//             lblThankYou.setMaxWidth(Double.MAX_VALUE);
//             lblThankYou.setStyle("-fx-font-style: italic; -fx-alignment: center;");

//             Label lblContact = new Label("Hotline: 1800.1234 - Website: www.computerstore.com.vn");
//             lblContact.setAlignment(Pos.CENTER);
//             lblContact.setMaxWidth(Double.MAX_VALUE);
//             lblContact.setStyle("-fx-font-size: 10px; -fx-alignment: center;");

//             // Thêm thông tin chính sách bảo hành
//             Label lblWarrantyPolicy = new Label("Để biết thêm về chính sách bảo hành, vui lòng xem tại website");
//             lblWarrantyPolicy.setAlignment(Pos.CENTER);
//             lblWarrantyPolicy.setMaxWidth(Double.MAX_VALUE);
//             lblWarrantyPolicy.setStyle("-fx-font-size: 10px; -fx-font-style: italic; -fx-alignment: center;");

//             // Thêm tất cả các phần tử vào hóa đơn
//             HBox logoBox = new HBox(10);
//             logoBox.setAlignment(Pos.CENTER);
//             logoBox.getChildren().add(logo);

//             receiptContent.getChildren().addAll(
//                     lblTitle,
//                     logoBox,
//                     lblStoreName,
//                     lblStoreAddress,
//                     lblStorePhone,
//                     new Separator(),
//                     lblOrderID,
//                     lblDateTime,
//                     lblCashier,
//                     lblCustomerName,
//                     lblCustomerPhone,
//                     lblPaymentMethod,
//                     sep1,
//                     tableHeader,
//                     productsBox,
//                     sep2,
//                     summaryBox
//             );

//             // Thêm thông tin thanh toán chuyển khoản nếu có
//             if (!paymentInfoBox.getChildren().isEmpty()) {
//                 receiptContent.getChildren().add(paymentInfoBox);
//             }

//             // Thêm phần kết
//             Separator sepEnd = new Separator();
//             sepEnd.setMaxWidth(Double.MAX_VALUE);

//             receiptContent.getChildren().addAll(
//                     sepEnd,
//                     lblThankYou,
//                     lblContact,
//                     lblWarrantyPolicy
//             );

//             // Định dạng kích thước hóa đơn
//             ScrollPane scrollPane = new ScrollPane(receiptContent);
//             scrollPane.setPrefWidth(550); // Tăng kích thước để hiển thị đủ cột bảo hành
//             scrollPane.setPrefHeight(600);
//             scrollPane.setFitToWidth(true);

//             // Tạo Scene và Stage để hiển thị trước khi in
//             Scene scene = new Scene(scrollPane);
//             Stage printPreviewStage = new Stage();
//             printPreviewStage.setTitle("Xem trước hóa đơn");
//             printPreviewStage.setScene(scene);

//             // Đóng cảnh báo đang in
//             printingAlert.close();

//             // Hiển thị hóa đơn
//             printPreviewStage.show();

//             // Thêm nút in và lưu vào cửa sổ xem trước
//             Button btnPrint = new Button("In");
//             btnPrint.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
//             btnPrint.setOnAction(e -> {
//                 try {
//                     PrinterJob job = PrinterJob.createPrinterJob();
//                     if (job != null) {
//                         boolean success = job.printPage(receiptContent);
//                         if (success) {
//                             job.endJob();
//                             printPreviewStage.close();

//                             Alert printSuccessAlert = new Alert(Alert.AlertType.INFORMATION);
//                             printSuccessAlert.setTitle("In thành công");
//                             printSuccessAlert.setHeaderText("Hóa đơn đã được gửi đến máy in");
//                             printSuccessAlert.setContentText("Vui lòng kiểm tra máy in của bạn.");
//                             printSuccessAlert.showAndWait();
//                         }
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi in hóa đơn", ex);
//                     showErrorAlert("Lỗi khi in hóa đơn: " + ex.getMessage());
//                 }
//             });

//             // Nút lưu PDF (giả định)
//             Button btnSave = new Button("Lưu PDF");
//             btnSave.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
//             btnSave.setOnAction(e -> {
//                 try {
//                     Alert saveAlert = new Alert(Alert.AlertType.INFORMATION);
//                     saveAlert.setTitle("Lưu PDF");
//                     saveAlert.setHeaderText("Hóa đơn đã được lưu");
//                     saveAlert.setContentText("Hóa đơn đã được lưu vào thư mục Documents.");
//                     saveAlert.showAndWait();
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi lưu PDF", ex);
//                     showErrorAlert("Lỗi khi lưu PDF: " + ex.getMessage());
//                 }
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnClose.setOnAction(e -> printPreviewStage.close());

//             HBox buttonBox = new HBox(10, btnPrint, btnSave, btnClose);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.setPadding(new Insets(10));

//             BorderPane borderPane = new BorderPane();
//             borderPane.setCenter(scrollPane);
//             borderPane.setBottom(buttonBox);

//             scene.setRoot(borderPane);

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi in hóa đơn", e);
//             Alert errorAlert = new Alert(Alert.AlertType.ERROR);
//             errorAlert.setTitle("Lỗi in hóa đơn");
//             errorAlert.setHeaderText("Không thể in hóa đơn");
//             errorAlert.setContentText("Chi tiết lỗi: " + e.getMessage());
//             errorAlert.showAndWait();
//         }
//     }

//     /**
//      * Thêm sản phẩm vào giỏ hàng với thông tin bảo hành
//      */
//     private void addToCartWithWarranty(CartItemEmployee item) {
//         if (item == null) {
//             LOGGER.warning("Lỗi: CartItemEmployee là null");
//             return;
//         }

//         // Tìm sản phẩm trong database để kiểm tra tồn kho
//         Product product = findProductById(item.getProductID());
//         if (product == null) {
//             AlertUtil.showWarning("Lỗi", "Không tìm thấy thông tin sản phẩm");
//             return;
//         }

//         // Kiểm tra số lượng tồn kho trước khi thêm
//         if (product.getQuantity() <= 0) {
//             AlertUtil.showWarning("Hết hàng", "Sản phẩm đã hết hàng!");
//             return;
//         }

//         // Tìm kiếm sản phẩm trong giỏ hàng với CÙNG loại bảo hành
//         boolean existingFound = false;
//         for (CartItemEmployee cartItem : cartItems) {
//             if (cartItem.getProductID().equals(item.getProductID())) {
//                 // Phải cùng sản phẩm và cùng loại bảo hành
//                 if (cartItem.hasWarranty() == item.hasWarranty() &&
//                         (!cartItem.hasWarranty() ||
//                                 cartItem.getWarranty().getWarrantyType().equals(item.getWarranty().getWarrantyType()))) {

//                     if (cartItem.getQuantity() < product.getQuantity()) {
//                         // Cập nhật số lượng nếu còn hàng
//                         cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
//                         existingFound = true;
//                         LOGGER.info("Đã tăng số lượng " + cartItem.getProductName() +
//                                 " (BH: " + (cartItem.hasWarranty() ? cartItem.getWarranty().getWarrantyType() : "Không") +
//                                 ") lên " + cartItem.getQuantity());
//                     } else {
//                         AlertUtil.showWarning("Số lượng tối đa",
//                                 "Không thể thêm nữa, số lượng trong kho chỉ còn " + product.getQuantity());
//                     }
//                     break;
//                 }
//             }
//         }

//         // Nếu không tìm thấy sản phẩm đã có trong giỏ với cùng loại bảo hành
//         if (!existingFound) {
//             cartItems.add(item);
//             LOGGER.info("Đã thêm " + item.getProductName() +
//                     " (BH: " + (item.hasWarranty() ? item.getWarranty().getWarrantyType() : "Không") +
//                     ") vào giỏ hàng");
//         }

//         // Cập nhật hiển thị giỏ hàng
//         updateCartDisplay();
//     }

//     // Tìm sản phẩm theo ID
//     private Product findProductById(String productID) {
//         if (productID == null || products == null) {
//             return null;
//         }

//         for (Product product : products) {
//             if (product.getProductID().equals(productID)) {
//                 return product;
//             }
//         }

//         return null;
//     }

//     // Sửa lại phần hiển thị dialog chi tiết sản phẩm trong PosOverviewController
//     private void showProductDetails(Product product) {
//         try {
//             if (product == null) {
//                 LOGGER.warning("Lỗi: Product object là null");
//                 return;
//             }

//             Stage detailStage = new Stage();
//             detailStage.initModality(Modality.APPLICATION_MODAL);
//             detailStage.setTitle("Chi tiết sản phẩm");

//             VBox layout = new VBox(10);
//             layout.setPadding(new Insets(20));
//             layout.setStyle("-fx-background-color: white;");

//             // Hiển thị ảnh sản phẩm (giữ nguyên code cũ)
//             final ImageView productImage = new ImageView();
//             productImage.setFitWidth(200);
//             productImage.setFitHeight(150);
//             productImage.setPreserveRatio(true);

//             // Tải ảnh sản phẩm (giữ nguyên code cũ)
//             String imagePath = product.getImagePath();
//             if (imagePath != null && !imagePath.startsWith("/")) {
//                 imagePath = "/com/example/stores/images/" + imagePath;
//             } else if (imagePath == null) {
//                 imagePath = "/com/example/stores/images/no_image.png";
//             }

//             try {
//                 Image image = new Image(getClass().getResourceAsStream(imagePath));
//                 productImage.setImage(image);
//             } catch (Exception e) {
//                 productImage.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/no_image.png")));
//                 LOGGER.warning("Không tải được ảnh chi tiết sản phẩm: " + e.getMessage());
//             }

//             final HBox imageBox = new HBox();
//             imageBox.setAlignment(Pos.CENTER);
//             imageBox.getChildren().add(productImage);

//             // Tên sản phẩm
//             String productName = (product.getProductName() != null) ? product.getProductName() : "Sản phẩm không có tên";
//             Label lblName = new Label(productName);
//             lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
//             lblName.setWrapText(true);

//             // Giá sản phẩm
//             Label lblPrice = new Label(String.format("Giá: %,d₫", (long)product.getPrice()));
//             lblPrice.setStyle("-fx-text-fill: #e91e63; -fx-font-weight: bold; -fx-font-size: 16px;");

//             // Thông tin cơ bản (giữ nguyên code cũ)
//             VBox specsBox = new VBox(5);
//             specsBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");

//             if (product.getCategoryID() != null) {
//                 Label lblCategory = new Label("Danh mục: " + getCategoryName(product.getCategoryID()));
//                 specsBox.getChildren().add(lblCategory);
//             }

//             Label lblStock = new Label("Tồn kho: " + product.getQuantity() + " sản phẩm");
//             specsBox.getChildren().add(lblStock);

//             String status = product.getStatus();
//             Label lblStatus = new Label("Trạng thái: " + (status != null ? status : "Không xác định"));
//             lblStatus.setStyle(status != null && status.equals("Còn hàng") ?
//                     "-fx-text-fill: #4caf50; -fx-font-weight: bold;" :
//                     "-fx-text-fill: #f44336; -fx-font-weight: bold;");
//             specsBox.getChildren().add(lblStatus);

//             // PHẦN BẢO HÀNH - CẬP NHẬT CHỈ CÒN 2 LOẠI: THƯỜNG VÀ VÀNG
//             VBox warrantyBox = new VBox(5);
//             warrantyBox.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 10; -fx-background-radius: 5;");

//             Label lblWarrantyTitle = new Label("Lựa chọn bảo hành:");
//             lblWarrantyTitle.setStyle("-fx-font-weight: bold;");
//             warrantyBox.getChildren().add(lblWarrantyTitle);

//             // ComboBox để chọn bảo hành
//             ComboBox<String> cbWarranty = new ComboBox<>();

//             // Kiểm tra sản phẩm có đủ điều kiện bảo hành thường không
//             boolean isEligibleForStdWarranty = WarrantyCalculator.isEligibleForStandardWarranty(product);

//             Label lblWarrantyInfo = new Label();

//             // Hiển thị các lựa chọn bảo hành dựa trên điều kiện
//             if (isEligibleForStdWarranty) {
//                 cbWarranty.getItems().addAll("Thường", "Vàng");
//                 cbWarranty.setValue("Thường");

//                 // Miêu tả bảo hành
//                 lblWarrantyInfo.setText("✅ Sản phẩm được bảo hành Thường miễn phí 12 tháng");
//                 lblWarrantyInfo.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 12px;");
//             } else {
//                 cbWarranty.getItems().add("Không");
//                 cbWarranty.setValue("Không");

//                 // Miêu tả không đủ điều kiện
//                 lblWarrantyInfo.setText("❌ Sản phẩm dưới 500.000đ không được bảo hành");
//                 lblWarrantyInfo.setStyle("-fx-text-fill: #f44336; -fx-font-size: 12px;");
//             }

//             warrantyBox.getChildren().addAll(cbWarranty, lblWarrantyInfo);

//             // Hiển thị phí bảo hành
//             Label lblWarrantyPrice = new Label("Phí bảo hành: 0đ");
//             warrantyBox.getChildren().add(lblWarrantyPrice);

//             // Hiển thị tổng tiền kèm bảo hành
//             Label lblTotalWithWarranty = new Label("Tổng tiền: " + String.format("%,d₫", (long)product.getPrice()));
//             lblTotalWithWarranty.setStyle("-fx-font-weight: bold;");
//             warrantyBox.getChildren().add(lblTotalWithWarranty);

//             // Cập nhật giá bảo hành khi thay đổi loại bảo hành
//             cbWarranty.setOnAction(e -> {
//                 String selectedType = cbWarranty.getValue();

//                 if ("Không".equals(selectedType) || "Thường".equals(selectedType)) {
//                     lblWarrantyPrice.setText("Phí bảo hành: 0đ");
//                     lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,d₫", (long)product.getPrice()));

//                     if ("Thường".equals(selectedType)) {
//                         lblWarrantyInfo.setText("✅ Bảo hành Thường miễn phí 12 tháng");
//                         lblWarrantyInfo.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 12px;");
//                     } else {
//                         lblWarrantyInfo.setText("❌ Không bảo hành");
//                         lblWarrantyInfo.setStyle("-fx-text-fill: #f44336; -fx-font-size: 12px;");
//                     }
//                     return;
//                 }

//                 // Tính phí bảo hành Vàng (10% giá sản phẩm)
//                 double warrantyFee = product.getPrice() * 0.1;
//                 lblWarrantyPrice.setText("Phí bảo hành: " + String.format("%,d₫", (long)warrantyFee));

//                 // Cập nhật tổng tiền
//                 double totalPrice = product.getPrice() + warrantyFee;
//                 lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,d₫", (long)totalPrice));

//                 // Thêm giải thích về bảo hành Vàng
//                 lblWarrantyInfo.setText("✨ Bảo hành Vàng 24 tháng, 1 đổi 1 trong 24 tháng");
//                 lblWarrantyInfo.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 12px; -fx-font-weight: bold;");
//             });

//             // Mô tả sản phẩm và nút thêm vào giỏ (giữ nguyên code)
//             Label lblDescTitle = new Label("Mô tả sản phẩm:");
//             lblDescTitle.setStyle("-fx-font-weight: bold;");

//             String description = (product.getDescription() != null) ? product.getDescription() : "Không có thông tin";
//             TextArea txtDescription = new TextArea(description);
//             txtDescription.setWrapText(true);
//             txtDescription.setEditable(false);
//             txtDescription.setPrefHeight(100);

//             // Nút thêm vào giỏ
//             Button btnAddToCart = new Button("Thêm vào giỏ");
//             btnAddToCart.setPrefWidth(200);
//             btnAddToCart.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnAddToCart.setOnAction(e -> {
//                 try {
//                     // Lấy loại bảo hành đã chọn
//                     String selectedWarranty = cbWarranty.getValue();

//                     // Tạo đối tượng CartItemEmployee mới
//                     CartItemEmployee newItem = new CartItemEmployee(
//                             product.getProductID(),
//                             product.getProductName(),
//                             product.getPrice(),
//                             1,
//                             product.getImagePath(),
//                             employeeId,
//                             currentUser != null ? currentUser : "unknown",
//                             product.getCategoryID()
//                     );

//                     // Tạo bảo hành nếu không phải là "Không" bảo hành
//                     if ("Thường".equals(selectedWarranty) || "Vàng".equals(selectedWarranty)) {
//                         // Tạo bảo hành và gán vào sản phẩm
//                         Warranty warranty = WarrantyCalculator.createWarranty(product, selectedWarranty);
//                         newItem.setWarranty(warranty);
//                     }

//                     // Thêm vào giỏ hàng
//                     addToCartWithWarranty(newItem);

//                     detailStage.close(); // Đóng cửa sổ chi tiết
//                     AlertUtil.showInformation("Thành công", "Đã thêm sản phẩm vào giỏ hàng!");
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi thêm sản phẩm vào giỏ hàng", ex);
//                     AlertUtil.showError("Lỗi", "Không thể thêm sản phẩm vào giỏ hàng: " + ex.getMessage());
//                 }
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setPrefWidth(100);
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnClose.setOnAction(e -> detailStage.close());

//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.getChildren().addAll(btnAddToCart, btnClose);

//             // Thêm tất cả vào layout
//             layout.getChildren().addAll(
//                     imageBox,
//                     lblName,
//                     lblPrice,
//                     new Separator(),
//                     specsBox,
//                     new Separator(),
//                     warrantyBox,
//                     new Separator(),
//                     lblDescTitle,
//                     txtDescription,
//                     buttonBox
//             );

//             Scene scene = new Scene(layout, 400, 800);
//             detailStage.setScene(scene);
//             detailStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị chi tiết sản phẩm", e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị chi tiết sản phẩm: " + e.getMessage());
//         }
//     }

//     // Tạo dòng hiển thị cho sản phẩm trong giỏ hàng
//     private HBox createCartItemRow(CartItemEmployee item) {
//         HBox row = new HBox();
//         row.setSpacing(10);
//         row.setPadding(new Insets(5));
//         row.setAlignment(Pos.CENTER_LEFT);

//         // Tên sản phẩm với thông tin bảo hành
//         VBox productInfoBox = new VBox(2);
//         Label lblName = new Label(item.getProductName());
//         lblName.setStyle("-fx-font-weight: bold;");
//         productInfoBox.getChildren().add(lblName);

//         // Thêm thông tin bảo hành nếu có
//         if (item.hasWarranty()) {
//             Label lblWarranty = new Label("BH: " + item.getWarranty().getWarrantyType());
//             lblWarranty.setStyle("-fx-font-size: 11px; -fx-text-fill: #2196F3;");
//             productInfoBox.getChildren().add(lblWarranty);
//         }

//         productInfoBox.setPrefWidth(200);

//         // Số lượng với nút tăng/giảm
//         HBox quantityBox = new HBox(5);
//         quantityBox.setAlignment(Pos.CENTER);

//         Button btnMinus = new Button("-");
//         btnMinus.setMinWidth(30);
//         btnMinus.setOnAction(e -> decreaseQuantity(item));

//         Label lblQuantity = new Label(String.valueOf(item.getQuantity()));
//         lblQuantity.setAlignment(Pos.CENTER);
//         lblQuantity.setMinWidth(30);
//         lblQuantity.setStyle("-fx-font-weight: bold;");

//         Button btnPlus = new Button("+");
//         btnPlus.setMinWidth(30);
//         btnPlus.setOnAction(e -> increaseQuantity(item));

//         quantityBox.getChildren().addAll(btnMinus, lblQuantity, btnPlus);
//         quantityBox.setPrefWidth(120);

//         // Đơn giá
//         Label lblPrice = new Label(String.format("%,.0f", item.getPrice()) + "đ");
//         lblPrice.setPrefWidth(100);
//         lblPrice.setAlignment(Pos.CENTER_RIGHT);

//         // Bảo hành
//         Label lblWarranty = new Label(item.hasWarranty() ? item.getWarranty().getWarrantyType() : "Không");
//         lblWarranty.setPrefWidth(80);
//         lblWarranty.setAlignment(Pos.CENTER);
//         if (item.hasWarranty()) {
//             lblWarranty.setStyle("-fx-text-fill: #4CAF50;");
//         }

//         // Tổng tiền
//         Label lblTotal = new Label(String.format("%,.0f", item.getTotalPrice()) + "đ");
//         lblTotal.setPrefWidth(100);
//         lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #e91e63;");
//         lblTotal.setAlignment(Pos.CENTER_RIGHT);

//         // Nút xóa
//         Button btnRemove = new Button("✖");
//         btnRemove.setStyle("-fx-background-color: transparent; -fx-text-fill: #f44336; -fx-font-weight: bold;");
//         btnRemove.setOnAction(e -> removeFromCart(item));

//         // Thêm tất cả vào dòng
//         row.getChildren().addAll(productInfoBox, quantityBox, lblPrice, lblWarranty, lblTotal, btnRemove);

//         return row;
//     }

//     // Tăng số lượng sản phẩm trong giỏ hàng
//     private void increaseQuantity(CartItemEmployee item) {
//         if (item == null) return;

//         Product product = findProductById(item.getProductID());
//         if (product == null) {
//             AlertUtil.showWarning("Lỗi", "Không tìm thấy thông tin sản phẩm");
//             return;
//         }

//         // Kiểm tra số lượng tồn kho
//         if (item.getQuantity() < product.getQuantity()) {
//             item.setQuantity(item.getQuantity() + 1);
//             updateCartDisplay();
//         } else {
//             AlertUtil.showWarning("Số lượng tối đa",
//                     "Không thể thêm nữa, số lượng trong kho chỉ còn " + product.getQuantity());
//         }
//     }

//     // Giảm số lượng sản phẩm trong giỏ hàng
//     private void decreaseQuantity(CartItemEmployee item) {
//         if (item == null) return;

//         if (item.getQuantity() > 1) {
//             item.setQuantity(item.getQuantity() - 1);
//             updateCartDisplay();
//         } else {
//             // Nếu số lượng là 1, hỏi xem có muốn xóa không
//             Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//             alert.setTitle("Xóa sản phẩm");
//             alert.setHeaderText("Xác nhận xóa");
//             alert.setContentText("Bạn có muốn xóa sản phẩm này khỏi giỏ hàng?");

//             Optional<ButtonType> result = alert.showAndWait();
//             if (result.isPresent() && result.get() == ButtonType.OK) {
//                 removeFromCart(item);
//             }
//         }
//     }

//     // Xóa sản phẩm khỏi giỏ hàng
//     private void removeFromCart(CartItemEmployee item) {
//         if (item != null) {
//             cartItems.remove(item);
//             updateCartDisplay();
//         }
//     }

//     // Cập nhật hiển thị giỏ hàng
//     private void updateCartDisplay() {
//         // Cập nhật tổng tiền
//         updateTotal();

//         // Cập nhật TableView
//         cartTable.refresh();
//     }

//     // Cập nhật tổng tiền giỏ hàng
//     private void updateTotal() {
//         double total = calculateTotalAmount();
//         if (lblTotal != null) {
//             lblTotal.setText("Tổng tiền: " + String.format("%,.0f", total) + "đ");
//         }
//     }

//     // Tính tổng tiền giỏ hàng
//     private double calculateTotalAmount() {
//         double total = 0.0;
//         for (CartItemEmployee item : cartItems) {
//             if (item != null) {
//                 total += item.getTotalPrice();
//             }
//         }
//         return total;
//     }

//     // Xóa toàn bộ giỏ hàng
//     private void clearCart() {
//         cartItems.clear();
//         updateCartDisplay();
//         LOGGER.info("Đã xóa toàn bộ giỏ hàng");
//     }

//     // Lấy tên danh mục từ ID
//     private String getCategoryName(String categoryId) {
//         if (categoryId == null) return "Không xác định";

//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.warning("Không thể kết nối đến database");
//                 return "Không xác định";
//             }

//             // FIX LỖI: Sửa tên bảng từ Category thành Categories và category_name thành categoryName
//             String query = "SELECT categoryName FROM Categories WHERE categoryID = ?";
//             stmt = conn.prepareStatement(query);
//             stmt.setString(1, categoryId);
//             rs = stmt.executeQuery();

//             if (rs.next()) {
//                 return rs.getString("categoryName");
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.WARNING, "Lỗi SQL khi lấy tên danh mục: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.WARNING, "Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.WARNING, "Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }

//         return "Không xác định";
//     }
//     // Lấy danh sách các danh mục phân biệt
//     private List<String> getDistinctCategories() {
//         List<String> categories = new ArrayList<>();
//         categories.add("Tất cả"); // Luôn có tùy chọn "Tất cả"

//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.severe("💀 QUẠC!!! Không thể kết nối đến database");
//                 return categories;
//             }

//             // FIX LỖI: Sửa tên bảng từ Category thành Categories
//             // Sửa tên cột từ category_name thành categoryName - match với schema thực tế
//             String query = "SELECT DISTINCT categoryID, categoryName FROM Categories ORDER BY categoryName";
//             stmt = conn.prepareStatement(query);
//             rs = stmt.executeQuery();

//             int categoryCount = 0;

//             while (rs.next()) {
//                 String categoryName = rs.getString("categoryName");
//                 if (categoryName != null && !categoryName.isEmpty()) {
//                     categories.add(categoryName);
//                     categoryCount++;
//                 }
//             }

//             LOGGER.info("✨✨✨ Đã tìm thấy " + categoryCount + " danh mục từ database slayyy");

//             if (categoryCount == 0) {
//                 LOGGER.warning("🚨🚨 SKSKSK EM hong tìm thấy danh mục nào trong database luôn á!!!");
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi SQL khi lấy danh mục: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "😭😭 Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }

//         return categories;
//     }

//     // Tải dữ liệu sản phẩm từ database
//     // Em sẽ sửa lại hàm loadProductsFromDatabase để FIX LỖI NGAY LAPPPPP
//     private void loadProductsFromDatabase() {
//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.severe("❌❌❌ Không thể kết nối đến database");
//                 return;
//             }

//             // FIX LỖI: Sửa lại câu query SQL - CHÚ Ý KHÔNG DÙNG WHERE NỮA
//             // Trước đây chỉ lấy sản phẩm có status = "Còn hàng" hoặc "Active"
//             // => Sửa lại để lấy TẤT CẢ sản phẩm, sort theo quantity để hiển thị sản phẩm còn hàng lên trên
//             String query = "SELECT * FROM Products ORDER BY quantity DESC";
//             stmt = conn.prepareStatement(query);
//             rs = stmt.executeQuery();

//             products.clear(); // Xóa danh sách cũ

//             int productCount = 0; // Đếm số sản phẩm load được

//             while (rs.next()) {
//                 Product product = new Product();
//                 product.setProductID(rs.getString("productID"));
//                 product.setProductName(rs.getString("productName"));
//                 product.setPrice(rs.getDouble("price"));
//                 product.setQuantity(rs.getInt("quantity"));
//                 product.setDescription(rs.getString("description"));
//                 product.setStatus(rs.getString("status"));
//                 product.setCategoryID(rs.getString("categoryID"));

//                 // Xử lý đường dẫn hình ảnh
//                 String imagePath = rs.getString("imagePath");
//                 if (imagePath != null && !imagePath.startsWith("/")) {
//                     imagePath = "/com/example/stores/images/" + imagePath;
//                 }
//                 product.setImagePath(imagePath);

//                 products.add(product);
//                 productCount++;
//             }

//             LOGGER.info("✅✅✅ Đã load được " + productCount + " sản phẩm từ database");

//             if (productCount == 0) {
//                 // Debug thêm thông tin nếu không load được sản phẩm nào
//                 LOGGER.warning("⚠️⚠️⚠️ Không tìm thấy sản phẩm nào trong database!!!");
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi SQL khi lấy dữ liệu sản phẩm: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }
//     }

//     // Làm mới danh sách sản phẩm trên giao diện
//     private void refreshProductList() {
//         if (productFlowPane == null) {
//             LOGGER.warning("productFlowPane chưa được khởi tạo");
//             return;
//         }

//         // Xóa tất cả sản phẩm hiện tại
//         productFlowPane.getChildren().clear();

//         if (products.isEmpty()) {
//             Label lblEmpty = new Label("Không có sản phẩm nào.");
//             lblEmpty.setStyle("-fx-font-style: italic;");
//             productFlowPane.getChildren().add(lblEmpty);
//             return;
//         }

//         // Lọc sản phẩm theo điều kiện
//         List<Product> filteredProducts = filterProducts();

//         // Sắp xếp sản phẩm theo điều kiện
//         sortProducts(filteredProducts);

//         // Lưu danh sách hiện tại để sử dụng sau này
//         currentFilteredProducts = new ArrayList<>(filteredProducts);

//         // Giới hạn số lượng sản phẩm hiển thị
//         List<Product> displayProducts = filteredProducts.stream()
//                 .limit(productLimit)
//                 .collect(Collectors.toList());

//         // Hiển thị sản phẩm
//         for (Product product : displayProducts) {
//             VBox productBox = createProductBox(product);
//             productFlowPane.getChildren().add(productBox);
//         }

//         // Thêm nút "Xem thêm" nếu còn sản phẩm
//         if (filteredProducts.size() > productLimit) {
//             Button btnLoadMore = createLoadMoreButton();
//             productFlowPane.getChildren().add(btnLoadMore);
//         }
//     }

//     // Lọc sản phẩm theo các điều kiện
//     private List<Product> filterProducts() {
//         List<Product> filteredList = new ArrayList<>(products);

//         // Lọc theo danh mục
//         if (cbCategory != null && cbCategory.getValue() != null && !cbCategory.getValue().equals("Tất cả")) {
//             String selectedCategory = cbCategory.getValue();
//             filteredList = filteredList.stream()
//                     .filter(p -> {
//                         String categoryName = getCategoryName(p.getCategoryID());
//                         return categoryName.equals(selectedCategory);
//                     })
//                     .collect(Collectors.toList());
//         }

//         // Lọc theo từ khóa tìm kiếm
//         if (txtSearch != null && txtSearch.getText() != null && !txtSearch.getText().trim().isEmpty()) {
//             String keyword = txtSearch.getText().trim().toLowerCase();
//             filteredList = filteredList.stream()
//                     .filter(p -> p.getProductName() != null && p.getProductName().toLowerCase().contains(keyword))
//                     .collect(Collectors.toList());
//         }

//         return filteredList;
//     }

//     // Sắp xếp sản phẩm theo điều kiện đã chọn
//     private void sortProducts(List<Product> list) {
//         if (cbSort == null || cbSort.getValue() == null) return;

//         String sortOption = cbSort.getValue();
//         switch (sortOption) {
//             case "Tên A-Z":
//                 // FIX LỖI: Thêm kiểu Product vào lambda để compiler biết đây là Product object
//                 list.sort(Comparator.comparing((Product p) -> p.getProductName() != null ? p.getProductName() : ""));
//                 break;
//             case "Tên Z-A":
//                 // FIX LỖI: Thêm kiểu Product vào lambda tương tự
//                 list.sort(Comparator.comparing((Product p) -> p.getProductName() != null ? p.getProductName() : "").reversed());
//                 break;
//             case "Giá thấp đến cao":
//                 list.sort(Comparator.comparing(Product::getPrice));
//                 break;
//             case "Giá cao đến thấp":
//                 list.sort(Comparator.comparing(Product::getPrice).reversed());
//                 break;
//             // Mặc định không sắp xếp (giữ nguyên thứ tự)
//         }
//     }

//     // Tạo box hiển thị sản phẩm
//     private VBox createProductBox(Product product) {
//         VBox box = new VBox(8); // Khoảng cách giữa các thành phần
//         box.setPrefWidth(160);
//         box.setPrefHeight(260);
//         box.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: white; -fx-padding: 10;");

//         // Tạo hiệu ứng hover
//         box.setOnMouseEntered(e -> {
//             box.setStyle("-fx-border-color: #2196F3; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f5f5f5; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.4), 10, 0, 0, 0);");
//             box.setCursor(Cursor.HAND);
//         });

//         box.setOnMouseExited(e -> {
//             box.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: white; -fx-padding: 10;");
//         });

//         // Xử lý sự kiện click để xem chi tiết sản phẩm
//         box.setOnMouseClicked(e -> showProductDetails(product));

//         // Hiển thị hình ảnh sản phẩm
//         ImageView imageView = new ImageView();
//         imageView.setFitWidth(140);
//         imageView.setFitHeight(105);
//         imageView.setPreserveRatio(true);

//         String imagePath = product.getImagePath();
//         if (imagePath == null) {
//             imagePath = "/com/example/stores/images/no_image.png";
//         }

//         try {
//             Image image = new Image(getClass().getResourceAsStream(imagePath));
//             imageView.setImage(image);
//         } catch (Exception e) {
//             try {
//                 Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/stores/images/no_image.png"));
//                 imageView.setImage(defaultImage);
//             } catch (Exception ex) {
//                 LOGGER.warning("Không tải được ảnh sản phẩm: " + ex.getMessage());
//             }
//         }

//         // Hiển thị tên sản phẩm
//         String productName = product.getProductName();
//         if (productName == null) productName = "Sản phẩm không tên";
//         if (productName.length() > 40) {
//             productName = productName.substring(0, 37) + "...";
//         }

//         Label nameLabel = new Label(productName);
//         nameLabel.setWrapText(true);
//         nameLabel.setPrefHeight(40); // Chiều cao cố định cho tên sản phẩm
//         nameLabel.setStyle("-fx-font-weight: bold;");

//         // Hiển thị giá
//         Label priceLabel = new Label("Giá: " + String.format("%,d", (long) product.getPrice()) + "đ");
//         priceLabel.setStyle("-fx-text-fill: #e91e63; -fx-font-weight: bold;");

//         // Hiển thị số lượng
//         Label stockLabel = new Label("Kho: " + product.getQuantity());

//         // Nút thêm vào giỏ
//         Button addButton = new Button("Thêm vào giỏ");
//         addButton.setPrefWidth(Double.MAX_VALUE);
//         addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

//         // Hiệu ứng hover cho nút
//         addButton.setOnMouseEntered(e ->
//                 addButton.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white; -fx-font-weight: bold;")
//         );

//         addButton.setOnMouseExited(e ->
//                 addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;")
//         );

//         // Sự kiện thêm vào giỏ
//         addButton.setOnAction(e -> {
//             try {
//                 // Kiểm tra số lượng tồn kho
//                 if (product.getQuantity() <= 0) {
//                     AlertUtil.showWarning("Hết hàng", "Sản phẩm đã hết hàng!");
//                     return;
//                 }

//                 // Tạo đối tượng CartItemEmployee
//                 CartItemEmployee item = new CartItemEmployee(
//                         product.getProductID(),
//                         product.getProductName(),
//                         product.getPrice(),
//                         1,
//                         product.getImagePath(),
//                         employeeId,
//                         currentUser != null ? currentUser : "unknown",
//                         product.getCategoryID()
//                 );

//                 // Kiểm tra sản phẩm có đủ điều kiện bảo hành thường không
//                 // Nếu có, thêm bảo hành thường mặc định
//                 if (WarrantyCalculator.isEligibleForStandardWarranty(product)) {
//                     Warranty warranty = WarrantyCalculator.createWarranty(product, "Thường");
//                     item.setWarranty(warranty);
//                 }

//                 // Thêm vào giỏ hàng
//                 addToCartWithWarranty(item);

//             } catch (Exception ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi khi thêm sản phẩm vào giỏ hàng", ex);
//                 AlertUtil.showError("Lỗi", "Không thể thêm sản phẩm vào giỏ hàng");
//             }
//         });

//         // Thêm tất cả vào box
//         VBox imageContainer = new VBox(imageView);
//         imageContainer.setAlignment(Pos.CENTER);

//         box.getChildren().addAll(
//                 imageContainer,
//                 nameLabel,
//                 priceLabel,
//                 stockLabel,
//                 addButton
//         );

//         return box;
//     }
// }package com.example.stores.controller;

// import com.example.stores.model.OrderHistory;
// import com.example.stores.model.OrderDetail;
// import com.example.stores.service.OrderHistoryServiceE;
// import javafx.scene.control.cell.PropertyValueFactory;
// import javafx.scene.control.CheckBox;

// import java.io.IOException;
// import java.io.InputStream;
// import java.net.URL;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.sql.Statement;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.List;
// import java.util.Optional;
// import java.util.logging.Level;
// import java.util.logging.Logger;
// import java.util.stream.Collectors;

// import com.example.stores.util.AlertUtil; // Chú ý: đây là AlertUtil (không có s)
// import com.example.stores.util.WarrantyCalculator;

// import com.example.stores.model.Customer;
// import com.example.stores.service.CustomerServiceE;

// import javafx.scene.control.RadioButton;
// import javafx.scene.control.ToggleGroup;
// import javafx.scene.layout.BorderPane;
// import javafx.scene.layout.GridPane;
// import javafx.scene.control.ScrollPane;
// import javafx.beans.property.SimpleStringProperty;
// import javafx.beans.property.SimpleIntegerProperty;
// import javafx.beans.property.SimpleDoubleProperty;

// import javafx.scene.layout.*;
// import javafx.geometry.Pos;
// import javafx.scene.control.Label;
// import javafx.scene.control.Button;
// import javafx.scene.image.Image;
// import javafx.scene.image.ImageView;
// import javafx.geometry.Insets;

// import javafx.collections.ObservableList;
// import com.example.stores.config.DBConfig;
// import com.example.stores.model.CartItemEmployee;
// import com.example.stores.model.Product;
// import com.example.stores.model.Employee;
// import com.example.stores.model.Warranty; // Thêm import cho Warranty

// import com.example.stores.model.Order;

// import javafx.scene.shape.Circle;
// import javafx.scene.Cursor;
// import javafx.collections.FXCollections;
// import javafx.fxml.FXML;
// import javafx.fxml.FXMLLoader;
// import javafx.print.PrinterJob;
// import javafx.scene.Parent;
// import javafx.scene.Scene;
// import javafx.scene.control.Alert;
// import javafx.scene.control.ButtonType;
// import javafx.scene.control.ComboBox;
// import javafx.scene.control.Separator;
// import javafx.scene.control.TableCell;
// import javafx.scene.control.TableColumn;
// import javafx.scene.control.TableView;
// import javafx.scene.control.TextArea;
// import javafx.scene.control.TextField;
// import javafx.scene.layout.VBox;
// import javafx.stage.Modality;
// import javafx.stage.Stage;

// public class PosOverviewControllerE {
//     private static final Logger LOGGER = Logger.getLogger(PosOverviewControllerE.class.getName());

//     @FXML private FlowPane productFlowPane;
//     @FXML private TableView<CartItemEmployee> cartTable;
//     @FXML private TableColumn<CartItemEmployee, String> colCartName;
//     @FXML private TableColumn<CartItemEmployee, Integer> colCartQty;
//     @FXML private TableColumn<CartItemEmployee, Double> colCartPrice;
//     @FXML private TableColumn<CartItemEmployee, Double> colCartTotal;
//     @FXML private TableColumn<CartItemEmployee, String> colCartWarranty; // Thêm khai báo biến cho cột bảo hành
//     @FXML private Label lblTotal;
//     // Cập nhật ComboBox lọc theo DB mới (bỏ RAM/CPU, giữ lại category)
//     @FXML private ComboBox<String> cbCategory;
//     @FXML private ComboBox<String> cbSort; // Thêm ComboBox sắp xếp
//     @FXML private TextField txtSearch;
//     @FXML private Button btnFilter, btnCheckout;
//     @FXML private VBox cartItemsContainer; // Container cho các item trong giỏ hàng

//     private int productLimit = 20; // Số sản phẩm hiển thị ban đầu
//     private List<Product> currentFilteredProducts = new ArrayList<>();

//     private ObservableList<Product> products = FXCollections.observableArrayList();
//     private ObservableList<CartItemEmployee> cartItems = FXCollections.observableArrayList();
//     private TableColumn<CartItemEmployee, Void> colCartAction; // Cột chứa nút xóa

//     private int employeeId;

//     /**
//      * Thêm sản phẩm vào giỏ hàng - Method công khai cho ProductDetailController gọi
//      * @param item Sản phẩm cần thêm vào giỏ
//      */
//     public void addToCart(CartItemEmployee item) {
//         // Gọi đến phương thức addToCartWithWarranty đã có sẵn
//         addToCartWithWarranty(item);
//         LOGGER.info("✅ Đã thêm sản phẩm " + item.getProductName() + " vào giỏ hàng từ ProductDetailController");
//     }

//     /**
//      * Lấy tên người dùng hiện tại
//      * @return tên đăng nhập người dùng hiện tại
//      */
//     public String getCurrentUser() {
//         return this.currentUser;
//     }

//     // Thêm biến để lưu lịch sử đơn hàng trong session
//     private List<OrderSummary> orderHistory = new ArrayList<>();

//     // Thêm vào class PosOverviewController
//     private void addEmployeeInfoButton() {
//         try {
//             if (currentEmployee == null || btnCheckout == null || btnCheckout.getParent() == null ||
//                     !(btnCheckout.getParent().getParent() instanceof BorderPane)) {
//                 LOGGER.warning("Không thể thêm nút thông tin nhân viên: currentEmployee hoặc btnCheckout null");
//                 return;
//             }

//             BorderPane mainLayout = (BorderPane) btnCheckout.getParent().getParent();
//             if (mainLayout.getTop() instanceof HBox) {
//                 HBox topBar = (HBox) mainLayout.getTop();

//                 Button btnEmployeeInfo = new Button("THÔNG TIN NV");
//                 btnEmployeeInfo.setStyle("-fx-background-color: #FF4081; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12;");

//                 btnEmployeeInfo.setOnMouseEntered(e -> btnEmployeeInfo.setStyle("-fx-background-color: #F50057; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 0);"));
//                 btnEmployeeInfo.setOnMouseExited(e -> btnEmployeeInfo.setStyle("-fx-background-color: #FF4081; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12;"));

//                 btnEmployeeInfo.setOnAction(e -> showEmployeeInfoDialog());

//                 HBox.setMargin(btnEmployeeInfo, new Insets(0, 10, 0, 10));
//                 int infoLabelIndex = topBar.getChildren().size() - 1;
//                 if (infoLabelIndex >= 0) {
//                     topBar.getChildren().add(infoLabelIndex, btnEmployeeInfo);
//                 } else {
//                     topBar.getChildren().add(btnEmployeeInfo);
//                 }

//                 LOGGER.info("✨ Đã thêm nút thông tin nhân viên!");
//             }
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi thêm nút thông tin nhân viên", e);
//         }
//     }

//     // Hàm hiển thị dialog thông tin nhân viên SIÊU XỊNNN
//     @FXML
//     private void showEmployeeInfoDialog() {
//         try {
//             if (currentEmployee == null) {
//                 AlertUtil.showWarning("Thông báo", "Không thể lấy thông tin nhân viên!");
//                 return;
//             }

//             // Tạo stage mới cho dialog
//             Stage infoStage = new Stage();
//             infoStage.initModality(Modality.APPLICATION_MODAL);
//             infoStage.setTitle("Thông Tin Nhân Viên");
//             infoStage.setResizable(false);

//             // Tạo layout chính
//             BorderPane layout = new BorderPane();

//             // Phần header đẹp ngời
//             HBox header = new HBox();
//             header.setAlignment(Pos.CENTER);
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: linear-gradient(to right, #FF4081, #F50057);");

//             Label headerTitle = new Label("THÔNG TIN NHÂN VIÊN");
//             headerTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
//             header.getChildren().add(headerTitle);

//             // Thêm header vào layout
//             layout.setTop(header);

//             // Phần nội dung
//             GridPane content = new GridPane();
//             content.setPadding(new Insets(20));
//             content.setVgap(15);
//             content.setHgap(10);
//             content.setAlignment(Pos.CENTER);

//             // Tạo ImageView cho ảnh đại diện (avatar)
//             ImageView avatarView = new ImageView();

//             // Tải ảnh từ resource đường dẫn đúng
//             try {
//                 // Lấy theo nhân viên đang đăng nhập
//                 String avatarPath = "/com/example/stores/images/employee/img.png"; // mặc định

//                 // Nếu là nv001, dùng ảnh an.png
//                 if (currentEmployee.getUsername() != null && currentEmployee.getUsername().equals("nv001")) {
//                     avatarPath = "/com/example/stores/images/employee/an.png";
//                 }

//                 // Hoặc nếu có imageUrl trong database
//                 if (currentEmployee.getImageUrl() != null && !currentEmployee.getImageUrl().isEmpty()) {
//                     String imageUrl = currentEmployee.getImageUrl();
//                     // Bỏ "resources/" ở đầu nếu có
//                     String resourcePath = imageUrl.startsWith("resources/") ? imageUrl.substring(10) : imageUrl;
//                     // Thay "com.example.stores/" thành "com/example/stores/"
//                     if (resourcePath.startsWith("com.example.stores/")) {
//                         resourcePath = resourcePath.replace("com.example.stores/", "com/example/stores/");
//                     }
//                     // Thêm dấu "/" ở đầu
//                     avatarPath = "/" + resourcePath;
//                 }

//                 // Load ảnh
//                 Image avatarImage = new Image(getClass().getResourceAsStream(avatarPath));
//                 avatarView.setImage(avatarImage);
//             } catch (Exception e) {
//                 // Nếu không có ảnh, hiển thị icon người dùng mặc định
//                 try {
//                     // Đường dẫn default chuẩn
//                     Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/stores/images/employee/img.png"));
//                     avatarView.setImage(defaultImage);
//                 } catch (Exception ex) {
//                     LOGGER.warning("Không thể tải ảnh mặc định cho nhân viên: " + ex.getMessage());
//                 }
//             }

//             // Thiết lập kích thước avatar
//             avatarView.setFitWidth(120);
//             avatarView.setFitHeight(120);
//             avatarView.setPreserveRatio(true);

//             // Bo tròn avatar bằng clip hình tròn
//             Circle clip = new Circle(60, 60, 60); // tâm (60,60), bán kính 60px
//             avatarView.setClip(clip);

//             // Tạo StackPane cho avatar, có viền và padding
//             StackPane avatarContainer = new StackPane(avatarView);
//             avatarContainer.setPadding(new Insets(3));
//             avatarContainer.setStyle("-fx-background-color: white; -fx-border-color: #FF4081; " +
//                     "-fx-border-width: 3; -fx-border-radius: 60; -fx-background-radius: 60;");
//             GridPane.setColumnSpan(avatarContainer, 2);
//             GridPane.setHalignment(avatarContainer, javafx.geometry.HPos.CENTER);

//             // Thêm avatar vào đầu tiên
//             content.add(avatarContainer, 0, 0, 2, 1);

//             // Thêm các thông tin nhân viên
//             addEmployeeInfoField(content, "Mã nhân viên:", currentEmployee.getEmployeeID(), 1);
//             addEmployeeInfoField(content, "Tên đăng nhập:", currentEmployee.getUsername(), 2);
//             addEmployeeInfoField(content, "Họ tên:", currentEmployee.getFullName(), 3);

//             // Thêm thông tin position nếu có
//             String position = "Nhân viên";
//             try {
//                 position = currentEmployee.getPosition();
//                 if (position == null || position.isEmpty()) position = "Nhân viên";
//             } catch (Exception e) {
//                 // Nếu không có thuộc tính position, dùng giá trị mặc định
//                 LOGGER.info("Không có thông tin chức vụ");
//             }
//             addEmployeeInfoField(content, "Chức vụ:", position, 4);

//             addEmployeeInfoField(content, "Email:", currentEmployee.getEmail(), 5);
//             addEmployeeInfoField(content, "Điện thoại:", currentEmployee.getPhone(), 6);
//             addEmployeeInfoField(content, "Thời gian đăng nhập:", currentDateTime, 7);

//             // Button đóng dialog
//             HBox buttonBar = new HBox();
//             buttonBar.setAlignment(Pos.CENTER);
//             buttonBar.setPadding(new Insets(0, 0, 20, 0));

//             Button closeButton = new Button("ĐÓNG");
//             closeButton.setPrefWidth(120);
//             closeButton.setPrefHeight(35);
//             closeButton.setStyle("-fx-background-color: #F50057; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");

//             closeButton.setOnMouseEntered(e ->
//                     closeButton.setStyle("-fx-background-color: #C51162; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;")
//             );

//             closeButton.setOnMouseExited(e ->
//                     closeButton.setStyle("-fx-background-color: #F50057; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;")
//             );

//             closeButton.setOnAction(e -> infoStage.close());

//             buttonBar.getChildren().add(closeButton);

//             // Thêm nội dung và button vào layout
//             VBox mainContainer = new VBox(15);
//             mainContainer.getChildren().addAll(content, buttonBar);
//             layout.setCenter(mainContainer);

//             // Tạo scene và hiển thị
//             Scene scene = new Scene(layout, 400, 520);
//             infoStage.setScene(scene);
//             infoStage.show();

//             LOGGER.info("✨ Đã hiển thị thông tin nhân viên: " + currentEmployee.getFullName());
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị thông tin nhân viên: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị thông tin nhân viên: " + e.getMessage());
//         }
//     }

//     // Hàm hỗ trợ thêm trường thông tin
//     private void addEmployeeInfoField(GridPane grid, String labelText, String value, int row) {
//         // Label tiêu đề
//         Label label = new Label(labelText);
//         label.setStyle("-fx-font-weight: bold; -fx-text-fill: #757575;");
//         grid.add(label, 0, row);

//         // Giá trị
//         Label valueLabel = new Label(value != null ? value : "N/A");
//         valueLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #212121;");
//         grid.add(valueLabel, 1, row);
//     }

//     // Biến để đếm số đơn hàng
//     private int orderCounter = 1;

//     private Button createLoadMoreButton() {
//         Button btnLoadMore = new Button("XEM THÊM SẢN PHẨM");
//         btnLoadMore.setPrefWidth(200);
//         btnLoadMore.setPrefHeight(40);
//         btnLoadMore.setStyle(
//                 "-fx-background-color: linear-gradient(to right, #2196F3, #03A9F4); " +
//                         "-fx-text-fill: white; " +
//                         "-fx-font-weight: bold; " +
//                         "-fx-font-size: 14px; " +
//                         "-fx-background-radius: 5; " +
//                         "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.4), 6, 0, 0, 2);"
//         );

//         // Hiệu ứng khi hover
//         btnLoadMore.setOnMouseEntered(e ->
//                 btnLoadMore.setStyle(
//                         "-fx-background-color: linear-gradient(to right, #1976D2, #2196F3); " +
//                                 "-fx-text-fill: white; " +
//                                 "-fx-font-weight: bold; " +
//                                 "-fx-font-size: 14px; " +
//                                 "-fx-background-radius: 5; " +
//                                 "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.6), 8, 0, 0, 3);"
//                 )
//         );

//         // Trở về style ban đầu khi hết hover
//         btnLoadMore.setOnMouseExited(e ->
//                 btnLoadMore.setStyle(
//                         "-fx-background-color: linear-gradient(to right, #2196F3, #03A9F4); " +
//                                 "-fx-text-fill: white; " +
//                                 "-fx-font-weight: bold; " +
//                                 "-fx-font-size: 14px; " +
//                                 "-fx-background-radius: 5; " +
//                                 "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.4), 6, 0, 0, 2);"
//                 )
//         );

//         // Sự kiện khi click
//         btnLoadMore.setOnAction(e -> {
//             productLimit += 20; // Tăng thêm 20 sản phẩm
//             refreshProductList(); // Làm mới danh sách
//             LOGGER.info("Đã tăng giới hạn hiển thị lên " + productLimit + " sản phẩm");
//         });

//         return btnLoadMore;
//     }

//     /**
//      * Lưu đơn hàng vào lịch sử
//      */
//     public void addToOrderHistory(int orderId, String customerName, String customerPhone,
//                                   String paymentMethod, String orderDateTime, double totalAmount,
//                                   List<CartItemEmployee> items) {
//         Connection conn = null;
//         PreparedStatement pstmtOrder = null;
//         PreparedStatement pstmtDetail = null;

//         try {
//             if (items == null || items.isEmpty()) {
//                 LOGGER.warning("Danh sách sản phẩm rỗng, không thể lưu lịch sử vào DB");
//                 return;
//             }

//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.severe("Không thể kết nối database để lưu order history");
//                 return;
//             }
//             conn.setAutoCommit(false); // Bắt đầu transaction

//             // 1. Insert vào bảng Orders
//             String insertOrder = "INSERT INTO Orders (orderID, orderDate, totalAmount, customerID, employeeID, orderStatus, paymentMethod, recipientName, recipientPhone) "
//                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//             pstmtOrder = conn.prepareStatement(insertOrder);

//             String orderIdStr = String.format("ORD%03d", orderId); // Format lại cho khớp orderID

//             int customerId = getWalkInCustomerId(); // Hoặc lấy đúng customerID nếu phân biệt khách

//             pstmtOrder.setString(1, orderIdStr);
//             pstmtOrder.setString(2, orderDateTime);
//             pstmtOrder.setDouble(3, totalAmount);
//             pstmtOrder.setInt(4, customerId);
//             pstmtOrder.setInt(5, employeeId);
//             pstmtOrder.setString(6, "Đã xác nhận");
//             pstmtOrder.setString(7, paymentMethod);
//             pstmtOrder.setString(8, customerName);
//             pstmtOrder.setString(9, customerPhone);

//             int resultOrder = pstmtOrder.executeUpdate();
//             if (resultOrder == 0) throw new SQLException("Insert Orders thất bại!");

//             // 2. Insert từng sản phẩm vào OrderDetails
//             String insertDetail = "INSERT INTO OrderDetails (orderID, productID, quantity, unitPrice, warrantyType, warrantyPrice) "
//                     + "VALUES (?, ?, ?, ?, ?, ?)";
//             pstmtDetail = conn.prepareStatement(insertDetail);

//             for (CartItemEmployee item : items) {
//                 pstmtDetail.setString(1, orderIdStr);
//                 pstmtDetail.setString(2, item.getProductID());
//                 pstmtDetail.setInt(3, item.getQuantity());
//                 pstmtDetail.setDouble(4, item.getPrice());

//                 // Bảo hành
//                 if (item.hasWarranty()) {
//                     pstmtDetail.setString(5, item.getWarranty().getWarrantyType());
//                     pstmtDetail.setDouble(6, item.getWarranty().getWarrantyPrice());
//                 } else {
//                     pstmtDetail.setString(5, "Thường");
//                     pstmtDetail.setDouble(6, 0.0);
//                 }
//                 pstmtDetail.addBatch();
//             }
//             int[] detailResults = pstmtDetail.executeBatch();

//             conn.commit();
//             LOGGER.info("✅ Đã lưu đơn hàng #" + orderIdStr + " vào database với " + detailResults.length + " sản phẩm");

//         } catch (Exception e) {
//             try {
//                 if (conn != null) conn.rollback();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi rollback khi lưu đơn hàng!", ex);
//             }
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi lưu đơn hàng vào DB: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (pstmtOrder != null) pstmtOrder.close();
//                 if (pstmtDetail != null) pstmtDetail.close();
//                 if (conn != null) conn.setAutoCommit(true);
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.WARNING, "Lỗi khi đóng kết nối DB", ex);
//             }
//         }
//     }
//     /**
//      * Lấy ID của khách hàng "Khách lẻ" (walkin)
//      */
//     private int getWalkInCustomerId() {
//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;
//         int customerId = 1; // Mặc định ID=1 cho khách lẻ

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.warning("Không thể kết nối đến database");
//                 return customerId;
//             }

//             String sql = "SELECT customerID FROM Customer WHERE username = 'walkin'";
//             stmt = conn.prepareStatement(sql);
//             rs = stmt.executeQuery();

//             if (rs.next()) {
//                 customerId = rs.getInt("customerID");
//                 return customerId;
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.WARNING, "Lỗi SQL khi lấy ID khách hàng mặc định: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.WARNING, "Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.WARNING, "Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }

//         return customerId;
//     }
//     // Thông tin user và thời gian
//     private String currentUser = "doanpk";
//     private String currentDateTime = "2025-06-22 10:30:23"; // Cập nhật thời gian hiện tại từ input
//     private Employee currentEmployee; // Biến lưu thông tin nhân viên

//     // Class để lưu thông tin đơn hàng tạm thời
//     private static class OrderSummary {
//         private int id;
//         private String customerName;
//         private String customerPhone;
//         private String paymentMethod;
//         private String orderDate;
//         private double totalAmount;
//         private List<CartItemEmployee> items;

//         public OrderSummary(int id, String customerName, String customerPhone, String paymentMethod,
//                             String orderDate, double totalAmount, List<CartItemEmployee> items) {
//             this.id = id;
//             this.customerName = customerName;
//             this.customerPhone = customerPhone;
//             this.paymentMethod = paymentMethod;
//             this.orderDate = orderDate;
//             this.totalAmount = totalAmount;
//             this.items = new ArrayList<>(items);
//         }

//         // Getters
//         public int getId() { return id; }
//         public String getCustomerName() { return customerName; }
//         public String getCustomerPhone() { return customerPhone; }
//         public String getPaymentMethod() { return paymentMethod; }
//         public String getOrderDate() { return orderDate; }
//         public double getTotalAmount() { return totalAmount; }
//         public List<CartItemEmployee> getItems() { return items; }
//     }

//     @FXML
//     private void initialize() {
//         try {
//             LOGGER.info("Đang khởi tạo POS Overview Controller...");
//             LOGGER.info("Người dùng hiện tại: " + currentUser);
//             LOGGER.info("Thời gian hiện tại: " + currentDateTime);

//             // Set style trực tiếp để đảm bảo nút có màu
//             if (btnCheckout != null) {
//                 btnCheckout.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             }

//             productFlowPane.setPrefWidth(900);
//             productFlowPane.setPrefWrapLength(900);  // DÒNG QUAN TRỌNG NHẤT!!!
//             productFlowPane.setHgap(15);
//             productFlowPane.setVgap(20);

//             // Lấy dữ liệu sản phẩm từ database
//             loadProductsFromDatabase();
//             LOGGER.info("Đã load " + products.size() + " sản phẩm từ database");

//             // Cấu hình TableView giỏ hàng
//             setupCartTable();

//             // Thêm nút xóa vào bảng giỏ hàng
//             addButtonsToTable();

//             cartTable.setItems(cartItems);

//             // Khởi tạo ComboBox filter danh mục
//             List<String> categoryList = getDistinctCategories();
//             if (cbCategory != null) cbCategory.setItems(FXCollections.observableArrayList(categoryList));

//             // Đảm bảo luôn chọn giá trị đầu tiên nếu có
//             if (cbCategory != null && !cbCategory.getItems().isEmpty()) cbCategory.getSelectionModel().select(0);

//             // Khởi tạo ComboBox sắp xếp
//             if (cbSort != null) {
//                 cbSort.getItems().addAll(
//                         "Mặc định",
//                         "Tên A-Z",
//                         "Tên Z-A",
//                         "Giá thấp đến cao",
//                         "Giá cao đến thấp"
//                 );
//                 cbSort.getSelectionModel().select(0);

//                 // Thêm listener cho cbSort
//                 cbSort.setOnAction(e -> refreshProductList());
//             }

//             // Sự kiện lọc, tìm kiếm
//             if (btnFilter != null) {
//                 btnFilter.setOnAction(e -> refreshProductList());
//             }

//             if (txtSearch != null) {
//                 txtSearch.textProperty().addListener((obs, oldVal, newVal) -> refreshProductList());
//             }

//             if (cbCategory != null) {
//                 cbCategory.setOnAction(e -> refreshProductList());
//             }

//             // Thanh toán - gọi handleCheckout để lưu dữ liệu vào DB
//             if (btnCheckout != null) {
//                 btnCheckout.setOnAction(e -> handleCheckout());
//             }

//             // Thêm nút lịch sử
//             addHistoryButton();

//             // Thêm nút đăng xuất
//             addLogoutButton();

//             // THÊM NÚT XEM THÔNG TIN NHÂN VIÊN
//             addEmployeeInfoButton();

//             // Render sản phẩm ban đầu
//             refreshProductList();
//             LOGGER.info("Khởi tạo POS Overview Controller thành công");
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi khởi tạo POS Overview Controller", e);
//         }
//     }

//     /**
//      * Xử lý thanh toán và lưu đơn hàng vào DB
//      */
//     @FXML
//     private void handleCheckout() {
//         try {
//             if (cartItems.isEmpty()) {
//                 AlertUtil.showWarning("Giỏ hàng trống", "Vui lòng thêm sản phẩm vào giỏ hàng trước khi thanh toán!");
//                 return;
//             }

//             // Tạo stage mới cho popup thanh toán
//             Stage confirmStage = new Stage();
//             confirmStage.initModality(Modality.APPLICATION_MODAL);
//             confirmStage.setTitle("Xác nhận thanh toán");
//             confirmStage.setResizable(false);

//             // BorderPane chính
//             BorderPane mainLayout = new BorderPane();

//             // HEADER ĐẸP NGỜI
//             HBox header = new HBox();
//             header.setAlignment(Pos.CENTER);
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: linear-gradient(to right, #4e73df, #224abe);");

//             Label headerLabel = new Label("XÁC NHẬN THANH TOÁN");
//             headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
//             header.getChildren().add(headerLabel);

//             mainLayout.setTop(header);

//             // PHẦN NỘI DUNG CHÍNH
//             VBox content = new VBox(15);
//             content.setPadding(new Insets(20));

//             // Tổng thanh toán hiển thị nổi bật
//             double totalAmount = calculateTotalAmount();
//             Label totalLabel = new Label("Tổng thanh toán: " + String.format("%,.0f", totalAmount) + "đ");
//             totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e91e63;");

//             // BẢNG DANH SÁCH SẢN PHẨM - THÊM VÀO FORM THANH TOÁN
//             Label productsLabel = new Label("Danh sách sản phẩm:");
//             productsLabel.setStyle("-fx-font-weight: bold;");

//             // TableView hiển thị sản phẩm trong giỏ
//             TableView<CartItemEmployee> productsTable = new TableView<>();
//             productsTable.setPrefHeight(150);

//             // Cột tên sản phẩm
//             TableColumn<CartItemEmployee, String> nameColumn = new TableColumn<>("Tên sản phẩm");
//             nameColumn.setCellValueFactory(data ->
//                     new SimpleStringProperty(data.getValue().getProductName()));
//             nameColumn.setPrefWidth(200);

//             // Cột số lượng
//             TableColumn<CartItemEmployee, Integer> quantityColumn = new TableColumn<>("SL");
//             quantityColumn.setCellValueFactory(data ->
//                     new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
//             quantityColumn.setPrefWidth(50);

//             // Cột đơn giá
//             TableColumn<CartItemEmployee, Double> priceColumn = new TableColumn<>("Đơn giá");
//             priceColumn.setCellValueFactory(data ->
//                     new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
//             priceColumn.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double price, boolean empty) {
//                     super.updateItem(price, empty);
//                     if (empty || price == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", price) + "đ");
//                     }
//                 }
//             });
//             priceColumn.setPrefWidth(100);

//             // Cột bảo hành
//             TableColumn<CartItemEmployee, String> warrantyColumn = new TableColumn<>("Bảo hành");
//             warrantyColumn.setCellValueFactory(data -> {
//                 CartItemEmployee item = data.getValue();
//                 if (item.hasWarranty()) {
//                     return new SimpleStringProperty(item.getWarranty().getWarrantyType());
//                 }
//                 return new SimpleStringProperty("Không");
//             });
//             warrantyColumn.setPrefWidth(80);

//             // Cột thành tiền
//             TableColumn<CartItemEmployee, Double> subtotalColumn = new TableColumn<>("T.Tiền");
//             subtotalColumn.setCellValueFactory(data ->
//                     new SimpleDoubleProperty(data.getValue().getTotalPrice()).asObject());
//             subtotalColumn.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double price, boolean empty) {
//                     super.updateItem(price, empty);
//                     if (empty || price == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", price) + "đ");
//                     }
//                 }
//             });
//             subtotalColumn.setPrefWidth(100);

//             productsTable.getColumns().addAll(nameColumn, quantityColumn, priceColumn, warrantyColumn, subtotalColumn);
//             productsTable.setItems(cartItems);

//             // Phần thông tin khách hàng
//             Label customerLabel = new Label("Thông tin khách hàng:");
//             customerLabel.setStyle("-fx-font-weight: bold;");

// // Form thông tin khách hàng
//             GridPane customerForm = new GridPane();
//             customerForm.setVgap(10);
//             customerForm.setHgap(10);

//             Label nameLabel = new Label("Tên khách hàng:");
//             TextField nameField = new TextField("Khách lẻ");
//             nameField.setPrefWidth(300);

//             Label phoneLabel = new Label("Số điện thoại:");
//             TextField phoneField = new TextField("0900000000");
//             phoneField.setPrefWidth(300);

// // ✅ THÊM TRƯỜNG GHI CHÚ
//             Label noteLabel = new Label("Ghi chú:");
//             TextArea noteField = new TextArea();
//             noteField.setPromptText("Nhập ghi chú cho đơn hàng (không bắt buộc)...");
//             noteField.setPrefWidth(300);
//             noteField.setPrefHeight(60);
//             noteField.setWrapText(true);

//             customerForm.add(nameLabel, 0, 0);
//             customerForm.add(nameField, 1, 0);
//             customerForm.add(phoneLabel, 0, 1);
//             customerForm.add(phoneField, 1, 1);
//             customerForm.add(noteLabel, 0, 2);  // ✅ THÊM VÀO DÒNG THỨ 3
//             customerForm.add(noteField, 1, 2);

//             // Phương thức thanh toán - CHỈ CÓ 2 PHƯƠNG THỨC
//             Label paymentLabel = new Label("Phương thức thanh toán:");
//             paymentLabel.setStyle("-fx-font-weight: bold;");

//             ToggleGroup paymentGroup = new ToggleGroup();

//             RadioButton cashRadio = new RadioButton("Tiền mặt");
//             cashRadio.setToggleGroup(paymentGroup);
//             cashRadio.setSelected(true); // Mặc định chọn tiền mặt

//             RadioButton transferRadio = new RadioButton("Chuyển khoản");
//             transferRadio.setToggleGroup(paymentGroup);

//             HBox paymentOptions = new HBox(20);
//             paymentOptions.getChildren().addAll(cashRadio, transferRadio);

//             // Thêm các thành phần vào content
//             content.getChildren().addAll(
//                     totalLabel,
//                     new Separator(),
//                     productsLabel,
//                     productsTable,
//                     new Separator(),
//                     customerLabel,
//                     customerForm,
//                     new Separator(),
//                     paymentLabel,
//                     paymentOptions
//             );

//             mainLayout.setCenter(new ScrollPane(content));

//             // PHẦN FOOTER VỚI CÁC NÚT CHỨC NĂNG
//             HBox footer = new HBox(10);
//             footer.setAlignment(Pos.CENTER_RIGHT);
//             footer.setPadding(new Insets(15, 20, 15, 20));
//             footer.setStyle("-fx-background-color: #f8f9fc; -fx-border-color: #e3e6f0; -fx-border-width: 1 0 0 0;");

//             Button cancelButton = new Button("Hủy");
//             cancelButton.setPrefWidth(100);
//             cancelButton.setStyle("-fx-background-color: #e74a3b; -fx-text-fill: white;");

//             Button confirmButton = new Button("Xác nhận thanh toán");
//             confirmButton.setPrefWidth(200);
//             confirmButton.setStyle("-fx-background-color: #4e73df; -fx-text-fill: white; -fx-font-weight: bold;");

//             footer.getChildren().addAll(cancelButton, confirmButton);
//             mainLayout.setBottom(footer);

//             // Xử lý sự kiện cho nút Hủy
//             cancelButton.setOnAction(e -> confirmStage.close());

//             // Xử lý sự kiện cho nút Xác nhận thanh toán
//             confirmButton.setOnAction(e -> {
//                 try {
//                     // Lấy thông tin khách hàng và phương thức thanh toán
//                     String customerName = nameField.getText().trim();
//                     String customerPhone = phoneField.getText().trim();
//                     String paymentMethod = cashRadio.isSelected() ? "Tiền mặt" : "Chuyển khoản";

//                     // Validate số điện thoại
//                     if (!customerPhone.isEmpty() && customerPhone.length() < 10) {
//                         AlertUtil.showWarning("Lỗi", "Số điện thoại không hợp lệ!");
//                         return;
//                     }

//                     // NẾU CHỌN CHUYỂN KHOẢN - MỞ CỬA SỔ QR CODE
//                     if (transferRadio.isSelected()) {
//                         // Đóng cửa sổ xác nhận
//                         confirmStage.close();

//                         // Mở cửa sổ QR Payment
//                         showQRPaymentWindow(customerName, customerPhone, totalAmount, cartItems);
//                         return;
//                     }

//                     // NẾU THANH TOÁN TIỀN MẶT - XỬ LÝ LUÔN
//                     // Lưu đơn hàng vào DB và trả về orderID
//                     String orderId = saveOrderToDB(customerName, customerPhone, paymentMethod, totalAmount, cartItems);

//                     if (orderId != null) {
//                         // FIX LỖI: Chỉ lấy phần số từ orderId (bỏ phần chữ "ORD")
//                         String numericOrderId = orderId.replaceAll("[^0-9]", "");
//                         int orderIdInt = Integer.parseInt(numericOrderId);

//                         // Lưu vào bộ nhớ (để tương thích với code cũ) - DÙNG ID ĐÃ XỬ LÝ
//                         addToOrderHistory(orderIdInt, customerName, customerPhone,
//                                 paymentMethod, getCurrentDateTime(), totalAmount, cartItems);

//                         // Đóng cửa sổ thanh toán
//                         confirmStage.close();

//                         // Hiển thị thông báo thành công
//                         AlertUtil.showInfo("Thanh toán thành công",
//                                 "Đơn hàng #" + orderId + " đã được tạo thành công!");

//                         // In hóa đơn - DÙNG ID ĐÃ XỬ LÝ
//                         printReceiptWithPaymentMethod(
//                                 orderIdInt,
//                                 cartItems, totalAmount, customerName, customerPhone,
//                                 paymentMethod, getCurrentDateTime(), currentUser);

//                         // Xóa giỏ hàng
//                         clearCart();
//                     } else {
//                         // Thông báo lỗi
//                         AlertUtil.showError("Lỗi thanh toán",
//                                 "Không thể lưu đơn hàng. Vui lòng thử lại!");
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi thanh toán: " + ex.getMessage(), ex);
//                     AlertUtil.showError("Lỗi thanh toán", "Đã xảy ra lỗi: " + ex.getMessage());
//                     confirmStage.close();
//                 }
//             });

//             Scene scene = new Scene(mainLayout, 600, 700);
//             confirmStage.setScene(scene);
//             confirmStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi hiển thị form thanh toán: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể mở trang xác nhận thanh toán: " + e.getMessage());
//         }
//     }

//     /**
//      * Hiển thị cửa sổ thanh toán QR Code
//      */
//     private void showQRPaymentWindow(String customerName, String customerPhone, double totalAmount, ObservableList<CartItemEmployee> items) {
//         try {
//             LOGGER.info("💖 Bắt đầu mở cửa sổ QR Payment nè!");

//             // Tạo đối tượng Order giả
//             Order order = new Order();
//             order.setTotalAmount(totalAmount);

//             // DEBUG: In ra đường dẫn hiện tại
//             LOGGER.info("📂 Working Directory: " + System.getProperty("user.dir"));

//             FXMLLoader loader = null;

//             // THỬ TẤT CẢ CÁC ĐƯỜNG DẪN CÓ THỂ
//             String[] possiblePaths = {
//                     "/com/example/stores/view/qr_payment.fxml",
//                     "com/example/stores/view/qr_payment.fxml",
//                     "/view/qr_payment.fxml",
//                     "view/qr_payment.fxml",
//                     "/qr_payment.fxml",
//                     "qr_payment.fxml"
//             };

//             for (String path : possiblePaths) {
//                 try {
//                     LOGGER.info("🔍 Thử load FXML từ: " + path);
//                     URL fxmlUrl = getClass().getResource(path);

//                     if (fxmlUrl != null) {
//                         LOGGER.info("✅ Tìm thấy file FXML tại: " + fxmlUrl);
//                         loader = new FXMLLoader(fxmlUrl);
//                         break;
//                     } else {
//                         LOGGER.warning("❌ Không tìm thấy FXML tại: " + path);
//                     }
//                 } catch (Exception e) {
//                     LOGGER.warning("❌ Lỗi khi thử path: " + path + " - " + e.getMessage());
//                 }
//             }

//             // Nếu không tìm thấy file FXML nào
//             if (loader == null) {
//                 LOGGER.severe("😭 KHÔNG TÌM THẤY FILE FXML NÀO HẾT!!!");
//                 throw new Exception("Không tìm thấy file FXML cho QR Payment");
//             }

//             // Load FXML
//             Parent root = loader.load();
//             LOGGER.info("✅ Đã load FXML thành công!");

//             // Lấy controller và truyền dữ liệu
//             QRPaymentControllerE controller = loader.getController();
//             LOGGER.info("✅ Đã lấy controller thành công!");

//             // Tạo danh sách OrderDetail giả
//             List<OrderDetail> orderDetails = new ArrayList<>();
//             // Chuyển đổi từ CartItem sang OrderDetail
//             for (CartItemEmployee item : items) {
//                 OrderDetail detail = new OrderDetail();
//                 detail.setProductName(item.getProductName());
//                 detail.setQuantity(item.getQuantity());
//                 detail.setPrice(item.getPrice());
//                 orderDetails.add(detail);
//             }

//             // Set dữ liệu cho Controller
//             controller.setOrderDetails(order, orderDetails);
//             LOGGER.info("✅ Đã set order details!");

//             // Set callback khi thanh toán thành công
//             controller.setOnPaymentSuccess(() -> {
//                 try {
//                     // Tạo đơn hàng với phương thức thanh toán là chuyển khoản
//                     String orderId = saveOrderToDB(customerName, customerPhone, "Chuyển khoản", totalAmount, items);
//                     LOGGER.info("✅ Đã lưu đơn hàng với ID: " + orderId);

//                     if (orderId != null) {
//                         // FIX LỖI: Chỉ lấy phần số từ orderId
//                         String numericOrderId = orderId.replaceAll("[^0-9]", "");
//                         int orderIdInt = Integer.parseInt(numericOrderId);

//                         // Lưu vào bộ nhớ với ID đã xử lý
//                         addToOrderHistory(orderIdInt, customerName, customerPhone,
//                                 "Chuyển khoản", getCurrentDateTime(), totalAmount, items);

//                         // Hiển thị thông báo thành công
//                         AlertUtil.showInfo("Thanh toán thành công",
//                                 "Đơn hàng #" + orderId + " đã được thanh toán thành công!");

//                         // In hóa đơn với ID đã xử lý
//                         printReceiptWithPaymentMethod(
//                                 orderIdInt,
//                                 items, totalAmount, customerName, customerPhone,
//                                 "Chuyển khoản", getCurrentDateTime(), currentUser);

//                         // Xóa giỏ hàng
//                         clearCart();
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi hoàn tất thanh toán QR: " + ex.getMessage(), ex);
//                     AlertUtil.showError("Lỗi thanh toán", "Đã xảy ra lỗi: " + ex.getMessage());
//                 }
//             });

//             // Hiển thị cửa sổ QR
//             Stage qrStage = new Stage();
//             qrStage.initModality(Modality.APPLICATION_MODAL);
//             qrStage.setTitle("Thanh toán bằng mã QR");
//             qrStage.setResizable(false);

//             Scene scene = new Scene(root);
//             qrStage.setScene(scene);

//             LOGGER.info("💯 SẮP HIỆN CỬA SỔ QR PAYMENT RỒI!!!");
//             qrStage.show(); // Dùng show() thay vì showAndWait() để debug
//             LOGGER.info("🎉 ĐÃ HIỆN CỬA SỔ QR PAYMENT!!!");

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi hiển thị cửa sổ thanh toán QR: " + e.getMessage(), e);

//             // In lỗi chi tiết hơn
//             e.printStackTrace();

//             AlertUtil.showError("Lỗi", "Không thể mở cửa sổ thanh toán QR: " + e.getMessage() + "\nVui lòng thanh toán bằng tiền mặt!");

//             // Trong trường hợp lỗi, thử lại với phương thức thanh toán tiền mặt
//             try {
//                 String orderId = saveOrderToDB(customerName, customerPhone, "Tiền mặt", totalAmount, items);
//                 if (orderId != null) {
//                     // FIX LỖI: Chỉ lấy phần số từ orderId
//                     String numericOrderId = orderId.replaceAll("[^0-9]", "");
//                     int orderIdInt = Integer.parseInt(numericOrderId);

//                     addToOrderHistory(orderIdInt, customerName, customerPhone, "Tiền mặt", getCurrentDateTime(), totalAmount, items);

//                     AlertUtil.showInfo("Thanh toán thành công",
//                             "Đã chuyển sang thanh toán tiền mặt.\nĐơn hàng #" + orderId + " đã được tạo thành công!");

//                     printReceiptWithPaymentMethod(orderIdInt, items, totalAmount, customerName, customerPhone,
//                             "Tiền mặt", getCurrentDateTime(), currentUser);

//                     clearCart();
//                 }
//             } catch (Exception ex) {
//                 LOGGER.log(Level.SEVERE, "❌ Lỗi khi thử thanh toán tiền mặt: " + ex.getMessage(), ex);
//             }
//         }
//     }    /**
//      * Lưu đơn hàng vào DB
//      * @return Mã đơn hàng (orderID) nếu lưu thành công, null nếu thất bại
//      */
//     private String saveOrderToDB(String recipientName, String recipientPhone,
//                                  String paymentMethod, double totalAmount,
//                                  List<CartItemEmployee> cartItems) {
//         String orderId = null;
//         Connection conn = null;

//         try {
//             conn = DBConfig.getConnection();
//             conn.setAutoCommit(false);

//             // 1. Tạo đơn hàng mới trong bảng Orders
//             String insertOrderSQL = "INSERT INTO Orders (orderDate, totalAmount, customerID, " +
//                     "recipientPhone, recipientName, orderStatus, paymentMethod) " +
//                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

//             try (PreparedStatement pstmtOrder = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {

//                 pstmtOrder.setString(1, getCurrentDateTime());
//                 pstmtOrder.setDouble(2, totalAmount);

//                 // ==== SỬA ĐOẠN NÀY ĐỂ LƯU KHÁCH HÀNG MỚI ====
//                 CustomerServiceE customerServiceE = new CustomerServiceE();
//                 int customerId = customerServiceE.findCustomerIdByPhone(recipientPhone);
//                 if (customerId == -1) {
//                     Customer newCustomer = new Customer();
//                     newCustomer.setCustomerName(recipientName);
//                     newCustomer.setPhone(recipientPhone);
//                     newCustomer.setAddress(""); // Có thể lấy từ form nếu có
//                     newCustomer.setEmail("");   // Có thể lấy từ form nếu có
//                     customerId = customerServiceE.addCustomerToDB(newCustomer);
//                     if (customerId == -1) {
//                         LOGGER.warning("❌ Không thể tạo khách mới, fallback về ID=1");
//                         customerId = 1; // fallback nếu lỗi
//                     }
//                 }
//                 pstmtOrder.setInt(3, customerId);

//                 pstmtOrder.setString(4, recipientPhone != null ? recipientPhone : "");
//                 pstmtOrder.setString(5, recipientName != null ? recipientName : "Khách lẻ");
//                 pstmtOrder.setString(6, "Đã xác nhận");
//                 pstmtOrder.setString(7, paymentMethod != null ? paymentMethod : "Tiền mặt");

//                 int result = pstmtOrder.executeUpdate();

//                 if (result > 0) {
//                     // Lấy orderID vừa được tạo
//                     ResultSet generatedKeys = pstmtOrder.getGeneratedKeys();
//                     if (generatedKeys.next()) {
//                         orderId = generatedKeys.getString(1);
//                         LOGGER.info("✅ Đã tạo đơn hàng mới với ID: " + orderId);

//                         // 2. Thêm chi tiết đơn hàng
//                         saveOrderDetails(conn, orderId, cartItems);

//                         // 3. Commit transaction
//                         conn.commit();
//                     }
//                 }

//             }

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi lưu đơn hàng vào DB: " + e.getMessage(), e);
//             // Rollback transaction nếu có lỗi
//             if (conn != null) {
//                 try {
//                     conn.rollback();
//                 } catch (SQLException ex) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi rollback transaction: " + ex.getMessage(), ex);
//                 }
//             }

//         } finally {
//             // Đảm bảo đóng connection và reset autoCommit
//             if (conn != null) {
//                 try {
//                     conn.setAutoCommit(true);
//                     conn.close();
//                 } catch (SQLException e) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi đóng connection: " + e.getMessage(), e);
//                 }
//             }
//         }

//         return orderId;
//     }
//     /**
//      * Lưu chi tiết đơn hàng vào DB
//      */
//     private void saveOrderDetails(Connection conn, String orderId, List<CartItemEmployee> cartItems) throws SQLException {
//         String insertDetailSQL = "INSERT INTO OrderDetails (orderID, productID, quantity, unitPrice, warrantyType, warrantyPrice) " +
//                 "VALUES (?, ?, ?, ?, ?, ?)";

//         try (PreparedStatement pstmt = conn.prepareStatement(insertDetailSQL)) {
//             for (CartItemEmployee item : cartItems) {
//                 pstmt.setString(1, orderId);
//                 pstmt.setString(2, item.getProductID());
//                 pstmt.setInt(3, item.getQuantity());
//                 pstmt.setDouble(4, item.getPrice());

//                 // Xử lý thông tin bảo hành
//                 if (item.hasWarranty()) {
//                     pstmt.setString(5, item.getWarranty().getWarrantyType());
//                     pstmt.setDouble(6, item.getWarranty().getWarrantyPrice());
//                 } else {
//                     pstmt.setString(5, "Thường"); // Mặc định
//                     pstmt.setDouble(6, 0.0);
//                 }

//                 pstmt.addBatch();
//             }

//             int[] results = pstmt.executeBatch();
//             LOGGER.info("✅ Đã thêm " + results.length + " chi tiết đơn hàng");

//             // Cập nhật số lượng sản phẩm trong kho
//             updateProductQuantities(conn, cartItems);
//         }
//     }

//     /**
//      * Cập nhật số lượng sản phẩm trong kho sau khi thanh toán
//      */
//     private void updateProductQuantities(Connection conn, List<CartItemEmployee> cartItems) throws SQLException {
//         String updateSQL = "UPDATE Products SET quantity = quantity - ? WHERE productID = ?";

//         try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
//             for (CartItemEmployee item : cartItems) {
//                 pstmt.setInt(1, item.getQuantity());
//                 pstmt.setString(2, item.getProductID());
//                 pstmt.addBatch();
//             }

//             int[] results = pstmt.executeBatch();
//             LOGGER.info("✅ Đã cập nhật số lượng cho " + results.length + " sản phẩm");
//         }
//     }

//     /**
//      * Lấy thời gian hiện tại theo định dạng phù hợp với DB
//      */
//     private String getCurrentDateTime() {
//         LocalDateTime now = LocalDateTime.now();
//         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//         return now.format(formatter);
//     }

//     // Phương thức để nhận thông tin nhân viên từ màn hình login
//     public void initEmployeeData(Employee employee, String loginDateTime) {
//         try {
//             if (employee != null) {
//                 this.currentEmployee = employee;
//                 this.currentDateTime = loginDateTime;
//                 this.currentUser = employee.getUsername();

//                 // Dùng getFullName() - đảm bảo không gọi getName() vì có thể không có method này
//                 LOGGER.info("Đã khởi tạo POS với nhân viên: " + employee.getFullName());
//                 LOGGER.info("Thời gian hiện tại: " + currentDateTime);

//                 // Hiển thị thông tin nhân viên trên giao diện
//                 displayEmployeeInfo();
//             } else {
//                 LOGGER.warning("Lỗi: Employee object truyền vào là null");
//             }
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi khởi tạo dữ liệu nhân viên", e);
//         }
//     }

//     // Phương thức để nhận thông tin nhân viên từ màn hình login
//     public void setEmployeeInfo(int employeeID, String username) {
//         this.employeeId = employeeID; // Lưu employeeID vào biến instance
//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             // ĐÃ SỬA: Bọc trong try-catch để xử lý Exception từ getConnection()
//             try {
//                 conn = DBConfig.getConnection();
//             } catch (Exception ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi kết nối DB", ex);
//                 throw new SQLException("Không thể kết nối đến cơ sở dữ liệu: " + ex.getMessage());
//             }

//             if (conn == null) {
//                 throw new SQLException("Không thể kết nối đến cơ sở dữ liệu");
//             }

//             String query = "SELECT * FROM Employee WHERE employeeID = ? AND username = ?";
//             stmt = conn.prepareStatement(query);
//             stmt.setInt(1, employeeID);
//             stmt.setString(2, username);

//             rs = stmt.executeQuery();
//             if (rs.next()) {
//                 // Tạo đối tượng Employee từ ResultSet
//                 Employee emp = new Employee();
//                 emp.setEmployeeID(String.valueOf(employeeID));  // Chuyển int thành String
//                 emp.setUsername(rs.getString("username"));
//                 emp.setFullName(rs.getString("fullName"));
//                 emp.setEmail(rs.getString("email"));
//                 emp.setPhone(rs.getString("phone"));

//                 // Kiểm tra trước khi gọi setPosition
//                 try {
//                     int columnIndex = rs.findColumn("position");
//                     if (columnIndex > 0) {
//                         emp.setPosition(rs.getString("position"));
//                     }
//                 } catch (SQLException ex) {
//                     // Nếu không có cột position, bỏ qua
//                     LOGGER.info("Cột position không tồn tại trong bảng Employee");
//                 }

//                 // Gọi initEmployeeData với đối tượng Employee đã tạo
//                 String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//                 initEmployeeData(emp, currentTime);
//             } else {
//                 LOGGER.warning("Không tìm thấy nhân viên với ID=" + employeeID + " và username=" + username);
//                 Alert alert = new Alert(Alert.AlertType.WARNING);
//                 alert.setTitle("Cảnh báo");
//                 alert.setHeaderText("Không tìm thấy thông tin nhân viên");
//                 alert.setContentText("Vui lòng đăng nhập lại để tiếp tục.");
//                 alert.showAndWait();
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "Lỗi SQL khi lấy thông tin nhân viên", e);
//             Alert alert = new Alert(Alert.AlertType.ERROR);
//             alert.setTitle("Lỗi");
//             alert.setHeaderText("Không thể lấy thông tin nhân viên");
//             alert.setContentText("Chi tiết lỗi: " + e.getMessage());
//             alert.showAndWait();
//         } finally {
//             // Đóng tất cả các tài nguyên theo thứ tự ngược lại
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 // Không đóng connection ở đây vì có thể được sử dụng ở nơi khác
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi khi đóng tài nguyên SQL", ex);
//             }
//         }
//     }

//     // Hiển thị thông tin nhân viên trên giao diện - ĐÃ SỬA (FIX BUG 243)
//     private void displayEmployeeInfo() {
//         try {
//             if (currentEmployee != null && btnCheckout != null && btnCheckout.getParent() != null
//                     && btnCheckout.getParent().getParent() instanceof BorderPane) {

//                 BorderPane mainLayout = (BorderPane) btnCheckout.getParent().getParent();

//                 if (mainLayout.getTop() instanceof HBox) {
//                     HBox topBar = (HBox) mainLayout.getTop();

//                     // Tạo label hiển thị thông tin nhân viên
//                     Label lblEmployeeInfo = new Label(currentEmployee.getFullName() + " (" + currentUser + ")");
//                     lblEmployeeInfo.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

//                     // Tạo spacer để đẩy thông tin ra góc phải
//                     Region spacer = new Region();
//                     HBox.setHgrow(spacer, Priority.ALWAYS);

//                     // Thêm vào top bar
//                     topBar.getChildren().addAll(spacer, lblEmployeeInfo);
//                 }
//             }
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị thông tin nhân viên", e);
//         }
//     }

//     // Thêm nút đăng xuất
//     private void addLogoutButton() {
//         if (btnCheckout == null) {
//             LOGGER.warning("Lỗi: btnCheckout chưa được khởi tạo");
//             return;
//         }

//         Button btnLogout = new Button("ĐĂNG XUẤT");
//         btnLogout.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//         btnLogout.setPrefWidth(120);
//         btnLogout.setPrefHeight(35);
//         btnLogout.setOnAction(e -> logout());

//         if (btnCheckout.getParent() instanceof HBox) {
//             HBox parent = (HBox) btnCheckout.getParent();
//             parent.getChildren().add(0, btnLogout);
//         } else if (btnCheckout.getParent() instanceof Pane) {
//             Pane parent = (Pane) btnCheckout.getParent();
//             btnLogout.setLayoutX(btnCheckout.getLayoutX() - 130);
//             btnLogout.setLayoutY(btnCheckout.getLayoutY());
//             parent.getChildren().add(btnLogout);
//         }
//     }

//     // Xử lý đăng xuất
//     private void logout() {
//         try {
//             // Hiển thị xác nhận
//             Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
//             confirm.setTitle("Xác nhận đăng xuất");
//             confirm.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
//             confirm.setContentText("Mọi thay đổi chưa lưu sẽ bị mất.");

//             Optional<ButtonType> result = confirm.showAndWait();
//             if (result.isPresent() && result.get() == ButtonType.OK) {
//                 // Load màn hình đăng nhập
//                 URL loginUrl = getClass().getResource("/com/example/stores/view/employee_login.fxml");

//                 if (loginUrl != null) {
//                     FXMLLoader loader = new FXMLLoader(loginUrl);
//                     Parent root = loader.load();

//                     Scene scene = null;
//                     Stage stage = null;

//                     if (btnCheckout != null) {
//                         stage = (Stage) btnCheckout.getScene().getWindow();
//                         scene = new Scene(root);
//                         stage.setTitle("Computer Store - Đăng Nhập");
//                         stage.setScene(scene);
//                         stage.setResizable(false);
//                         stage.show();
//                     } else {
//                         LOGGER.warning("Lỗi: btnCheckout là null hoặc không thuộc Scene");
//                         stage = new Stage();
//                         scene = new Scene(root);
//                         stage.setTitle("Computer Store - Đăng Nhập");
//                         stage.setScene(scene);
//                         stage.setResizable(false);
//                         stage.show();

//                         // Đóng cửa sổ hiện tại nếu có
//                         if (productFlowPane != null && productFlowPane.getScene() != null) {
//                             Stage currentStage = (Stage) productFlowPane.getScene().getWindow();
//                             currentStage.close();
//                         }
//                     }

//                     LOGGER.info("Đã đăng xuất, thời gian: " +
//                             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//                 } else {
//                     throw new IOException("Không tìm thấy file employee_login.fxml");
//                 }
//             }
//         } catch (IOException e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi đăng xuất", e);
//             Alert alert = new Alert(Alert.AlertType.ERROR);
//             alert.setTitle("Lỗi");
//             alert.setContentText("Lỗi khi đăng xuất: " + e.getMessage());
//             alert.showAndWait();
//         }
//     }

//     // Thêm nút lịch sử đơn hàng
//     private void addHistoryButton() {
//         if (btnCheckout == null) {
//             LOGGER.warning("Lỗi: btnCheckout chưa được khởi tạo");
//             return;
//         }

//         Button btnHistory = new Button("LỊCH SỬ");
//         btnHistory.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
//         btnHistory.setPrefWidth(120);
//         btnHistory.setPrefHeight(35);
//         btnHistory.setOnAction(e -> showOrderHistoryInMemory()); // Sử dụng history trong bộ nhớ

//         if (btnCheckout.getParent() instanceof HBox) {
//             HBox parent = (HBox) btnCheckout.getParent();
//             parent.getChildren().add(0, btnHistory);
//         } else if (btnCheckout.getParent() instanceof Pane) {
//             Pane parent = (Pane) btnCheckout.getParent();
//             btnHistory.setLayoutX(btnCheckout.getLayoutX() - 130);
//             btnHistory.setLayoutY(btnCheckout.getLayoutY());
//             parent.getChildren().add(btnHistory);
//         }
//     }

//     // Cấu hình TableView giỏ hàng
//     // Đầu tiên em sửa hàm setupCartTable() để thêm cột bảo hành mới
//     private void setupCartTable() {
//         if (colCartName == null || colCartQty == null || colCartPrice == null || colCartTotal == null) {
//             LOGGER.warning("Lỗi: Các cột của TableView chưa được khởi tạo");
//             return;
//         }

//         // Thiết lập các cột cũ
//         colCartName.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleStringProperty("N/A");
//             }
//             String name = data.getValue().getProductName();
//             return new SimpleStringProperty(name != null ? name : "N/A");
//         });

//         colCartQty.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleIntegerProperty(0).asObject();
//             }
//             int qty = data.getValue().getQuantity();
//             return new SimpleIntegerProperty(qty).asObject();
//         });

//         colCartPrice.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleDoubleProperty(0).asObject();
//             }
//             double price = data.getValue().getPrice();
//             return new SimpleDoubleProperty(price).asObject();
//         });

//         colCartPrice.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//             @Override
//             protected void updateItem(Double price, boolean empty) {
//                 super.updateItem(price, empty);
//                 if (empty || price == null) {
//                     setText(null);
//                 } else {
//                     setText(String.format("%,.0f", price) + "đ");
//                 }
//             }
//         });

//         // THÊM CỘT BẢO HÀNH MỚI
//         colCartWarranty.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleStringProperty("Không");
//             }
//             CartItemEmployee item = data.getValue();
//             if (item.hasWarranty()) {
//                 return new SimpleStringProperty(item.getWarranty().getWarrantyType());
//             } else {
//                 return new SimpleStringProperty("Không");
//             }
//         });

//         // Nút sửa bảo hành
//         colCartWarranty.setCellFactory(tc -> new TableCell<CartItemEmployee, String>() {
//             @Override
//             protected void updateItem(String warrantyType, boolean empty) {
//                 super.updateItem(warrantyType, empty);
//                 if (empty) {
//                     setText(null);
//                     setGraphic(null);
//                 } else {
//                     HBox container = new HBox(5);
//                     container.setAlignment(Pos.CENTER_LEFT);

//                     // Hiển thị loại bảo hành
//                     Label lblType = new Label(warrantyType);

//                     // Nút sửa nhỏ bên cạnh
//                     Button btnEdit = new Button("⚙️");
//                     btnEdit.setStyle("-fx-background-color: transparent; -fx-padding: 0 2;");
//                     btnEdit.setOnAction(event -> {
//                         CartItemEmployee item = getTableView().getItems().get(getIndex());
//                         if (item != null) {
//                             showWarrantyEditDialog(item);
//                         }
//                     });

//                     container.getChildren().addAll(lblType, btnEdit);
//                     setGraphic(container);
//                     setText(null);
//                 }
//             }
//         });

//         colCartTotal.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleDoubleProperty(0).asObject();
//             }
//             double total = data.getValue().getTotalPrice();
//             return new SimpleDoubleProperty(total).asObject();
//         });

//         colCartTotal.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//             @Override
//             protected void updateItem(Double total, boolean empty) {
//                 super.updateItem(total, empty);
//                 if (empty || total == null) {
//                     setText(null);
//                 } else {
//                     setText(String.format("%,.0f", total) + "đ");
//                 }
//             }
//         });
//     }

//     // Sửa lại dialog chỉnh sửa bảo hành trong giỏ hàng
//     private void showWarrantyEditDialog(CartItemEmployee item) {
//         try {
//             // Tìm thông tin sản phẩm từ database để lấy giá
//             Product product = findProductById(item.getProductID());
//             if (product == null) {
//                 AlertUtil.showWarning("Lỗi", "Không tìm thấy thông tin sản phẩm");
//                 return;
//             }

//             Stage dialogStage = new Stage();
//             dialogStage.setTitle("Cập nhật bảo hành");
//             dialogStage.initModality(Modality.APPLICATION_MODAL);

//             VBox dialogContent = new VBox(15);
//             dialogContent.setPadding(new Insets(20));
//             dialogContent.setAlignment(Pos.CENTER);

//             // Tiêu đề và thông tin sản phẩm
//             Label lblTitle = new Label("Chọn gói bảo hành cho " + item.getProductName());
//             lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             // ComboBox chọn loại bảo hành - SỬA LẠI CÒN 2 LOẠI
//             ComboBox<String> cbWarranty = new ComboBox<>();

//             // Kiểm tra điều kiện bảo hành thường
//             boolean isEligibleForStdWarranty = WarrantyCalculator.isEligibleForStandardWarranty(product);

//             if (isEligibleForStdWarranty) {
//                 // Chỉ còn 2 lựa chọn
//                 cbWarranty.getItems().addAll("Không", "Thường", "Vàng");
//             } else {
//                 // Sản phẩm không đủ điều kiện bảo hành
//                 cbWarranty.getItems().add("Không");
//             }

//             // Set giá trị hiện tại
//             if (item.hasWarranty()) {
//                 String currentType = item.getWarranty().getWarrantyType();
//                 // Chuyển đổi các loại bảo hành cũ (nếu có)
//                 if (!currentType.equals("Thường") && !currentType.equals("Vàng")) {
//                     currentType = "Thường"; // Mặc định về Thường
//                 }

//                 if (cbWarranty.getItems().contains(currentType)) {
//                     cbWarranty.setValue(currentType);
//                 } else {
//                     cbWarranty.setValue("Không");
//                 }
//             } else {
//                 cbWarranty.setValue("Không");
//             }

//             // Hiển thị giá bảo hành
//             Label lblWarrantyPrice = new Label("Phí bảo hành: 0đ");
//             Label lblTotalWithWarranty = new Label("Tổng tiền: " + String.format("%,.0f", item.getTotalPrice()) + "đ");
//             lblTotalWithWarranty.setStyle("-fx-font-weight: bold;");

//             // Thêm mô tả bảo hành
//             Label lblWarrantyInfo = new Label("Không bảo hành");
//             lblWarrantyInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #757575;");

//             // Cập nhật giá khi thay đổi loại bảo hành
//             cbWarranty.setOnAction(e -> {
//                 String selectedType = cbWarranty.getValue();

//                 // TH1: Không bảo hành
//                 if (selectedType.equals("Không")) {
//                     lblWarrantyPrice.setText("Phí bảo hành: 0đ");
//                     double basePrice = product.getPrice() * item.getQuantity();
//                     lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,.0f", basePrice) + "đ");
//                     lblWarrantyInfo.setText("Không bảo hành cho sản phẩm này");
//                     lblWarrantyInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #757575;");
//                     return;
//                 }

//                 // TH2: Bảo hành thường
//                 if (selectedType.equals("Thường")) {
//                     lblWarrantyPrice.setText("Phí bảo hành: 0đ");
//                     double basePrice = product.getPrice() * item.getQuantity();
//                     lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,.0f", basePrice) + "đ");
//                     lblWarrantyInfo.setText("Bảo hành thường miễn phí 12 tháng");
//                     lblWarrantyInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #4CAF50;");
//                     return;
//                 }

//                 // TH3: Bảo hành vàng (10% giá gốc)
//                 double warrantyFee = product.getPrice() * 0.1 * item.getQuantity();
//                 lblWarrantyPrice.setText("Phí bảo hành: " + String.format("%,.0f", warrantyFee) + "đ");

//                 // Cập nhật tổng tiền
//                 double totalPrice = (product.getPrice() * item.getQuantity()) + warrantyFee;
//                 lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,.0f", totalPrice) + "đ");

//                 lblWarrantyInfo.setText("✨ Bảo hành Vàng 24 tháng, 1 đổi 1");
//                 lblWarrantyInfo.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF9800;");
//             });

//             // Nút lưu và hủy
//             Button btnSave = new Button("Lưu thay đổi");
//             btnSave.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnSave.setPrefWidth(140);
//             btnSave.setOnAction(e -> {
//                 String selectedType = cbWarranty.getValue();

//                 if ("Không".equals(selectedType)) {
//                     // Xóa bảo hành nếu chọn không bảo hành
//                     item.setWarranty(null);
//                 } else {
//                     // Tạo bảo hành mới với loại đã chọn
//                     Warranty warranty = WarrantyCalculator.createWarranty(product, selectedType);
//                     item.setWarranty(warranty);
//                 }

//                 // Cập nhật hiển thị
//                 updateCartDisplay();
//                 dialogStage.close();
//                 AlertUtil.showInformation("Thành công", "Đã cập nhật bảo hành cho sản phẩm");
//             });

//             Button btnCancel = new Button("Hủy");
//             btnCancel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnCancel.setPrefWidth(80);
//             btnCancel.setOnAction(e -> dialogStage.close());

//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.getChildren().addAll(btnSave, btnCancel);

//             // Thêm các thành phần vào dialog
//             dialogContent.getChildren().addAll(
//                     lblTitle,
//                     new Separator(),
//                     cbWarranty,
//                     lblWarrantyInfo,
//                     lblWarrantyPrice,
//                     lblTotalWithWarranty,
//                     buttonBox
//             );

//             // Hiện dialog
//             Scene scene = new Scene(dialogContent, 350, 320);
//             dialogStage.setScene(scene);
//             dialogStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị dialog chỉnh sửa bảo hành", e);
//             AlertUtil.showError("Lỗi", "Không thể mở cửa sổ chỉnh sửa bảo hành");
//         }
//     }

//     // Thêm nút xóa vào bảng giỏ hàng
//     private void addButtonsToTable() {
//         if (cartTable == null) {
//             LOGGER.warning("Lỗi: cartTable chưa được khởi tạo");
//             return;
//         }

//         colCartAction = new TableColumn<>("Xóa");
//         colCartAction.setCellFactory(param -> new TableCell<CartItemEmployee, Void>() {
//             private final Button btnDelete = new Button("X");

//             {
//                 btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//                 btnDelete.setOnAction(event -> {
//                     CartItemEmployee item = getTableRow().getItem();
//                     if (item != null) {
//                         // Hiện dialog xác nhận trước khi xóa
//                         Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
//                                 "Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?",
//                                 ButtonType.YES, ButtonType.NO);
//                         alert.setTitle("Xác nhận xóa");
//                         alert.setHeaderText("Xóa sản phẩm");

//                         Optional<ButtonType> result = alert.showAndWait();
//                         if (result.isPresent() && result.get() == ButtonType.YES) {
//                             cartItems.remove(item);
//                             updateTotal();
//                         }
//                     }
//                 });
//             }

//             @Override
//             protected void updateItem(Void item, boolean empty) {
//                 super.updateItem(item, empty);
//                 if (empty) {
//                     setGraphic(null);
//                 } else {
//                     setGraphic(btnDelete);
//                 }
//             }
//         });

//         colCartAction.setPrefWidth(50);

//         // Thêm cột vào TableView nếu chưa có
//         if (!cartTable.getColumns().contains(colCartAction)) {
//             cartTable.getColumns().add(colCartAction);
//         }
//     }

//     // Hiển thị thông báo lỗi
//     private void showErrorAlert(String message) {
//         Alert alert = new Alert(Alert.AlertType.WARNING, message);
//         alert.setTitle("Lỗi");
//         alert.setHeaderText("Thông tin không hợp lệ");
//         alert.showAndWait();
//     }


//     // Thêm method mới vào PosOverviewController
//     private void showOrderByIdWindow(String orderIdInput) {
//         try {
//             LOGGER.info("🔍 Tìm kiếm đơn hàng với ID: " + orderIdInput);

//             // Chuẩn hóa orderID (có thể người dùng nhập 1, 2, 3 hoặc ORD001, ORD002)
//             String searchOrderId = normalizeOrderId(orderIdInput);
//             LOGGER.info("📝 OrderID sau khi chuẩn hóa: " + searchOrderId);

//             // Tìm đơn hàng trong database
//             OrderHistoryServiceE.OrderWithDetails orderData = OrderHistoryServiceE.getCompleteOrderById(searchOrderId);

//             if (orderData == null || orderData.getOrderHistory() == null) {
//                 AlertUtil.showWarning("Không tìm thấy",
//                         "Không tìm thấy đơn hàng với mã: " + orderIdInput + "\nĐã thử tìm: " + searchOrderId);
//                 return;
//             }

//             OrderHistory order = orderData.getOrderHistory();
//             ObservableList<OrderDetail> details = orderData.getOrderDetails();

//             LOGGER.info("✅ Tìm thấy đơn hàng: " + order.getOrderID() + " với " + details.size() + " sản phẩm");

//             // Tạo cửa sổ hiển thị chi tiết
//             showSingleOrderDetailWindow(order, details);

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi tìm đơn hàng theo ID: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể tìm đơn hàng: " + e.getMessage());
//         }
//     }

//     // Helper method chuẩn hóa orderID
//     private String normalizeOrderId(String input) {
//         if (input == null || input.trim().isEmpty()) {
//             return input;
//         }

//         String trimmed = input.trim();

//         // Nếu đã có định dạng ORDxxx thì giữ nguyên
//         if (trimmed.toUpperCase().startsWith("ORD")) {
//             return trimmed;
//         }

//         // Nếu là số thuần túy, thử cả 2 cách
//         try {
//             int numericId = Integer.parseInt(trimmed);
//             // Thử format ORD001 trước
//             return String.format("ORD%03d", numericId);
//         } catch (NumberFormatException e) {
//             // Nếu không phải số, trả về nguyên input
//             return trimmed;
//         }
//     }
//     // Thêm method hiển thị chi tiết đơn hàng
//     private void showSingleOrderDetailWindow(OrderHistory order, ObservableList<OrderDetail> details) {
//         try {
//             Stage detailStage = new Stage();
//             detailStage.initModality(Modality.APPLICATION_MODAL);
//             detailStage.setTitle("Chi tiết đơn hàng #" + order.getOrderID());
//             detailStage.setResizable(true);

//             BorderPane mainLayout = new BorderPane();

//             // Header đẹp
//             HBox header = new HBox();
//             header.setAlignment(Pos.CENTER);
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: linear-gradient(to right, #4CAF50, #45a049);");

//             Label headerTitle = new Label("CHI TIẾT ĐƠN HÀNG #" + order.getOrderID());
//             headerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
//             header.getChildren().add(headerTitle);

//             // Content
//             VBox content = new VBox(15);
//             content.setPadding(new Insets(20));

//             // Thông tin đơn hàng
//             GridPane infoGrid = new GridPane();
//             infoGrid.setHgap(15);
//             infoGrid.setVgap(10);
//             infoGrid.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8;");

//             int row = 0;
//             addInfoRow(infoGrid, "Mã đơn hàng:", order.getOrderID(), row++);
//             addInfoRow(infoGrid, "Ngày đặt:", order.getFormattedDate(), row++);
//             addInfoRow(infoGrid, "Khách hàng:", order.getCustomerName(), row++);
//             addInfoRow(infoGrid, "Số điện thoại:", order.getCustomerPhone(), row++);
//             addInfoRow(infoGrid, "Nhân viên:", order.getEmployeeName(), row++);
//             addInfoRow(infoGrid, "Phương thức thanh toán:", order.getPaymentMethod(), row++);
//             addInfoRow(infoGrid, "Trạng thái:", order.getStatus(), row++);

//             // Bảng sản phẩm
//             Label productsLabel = new Label("DANH SÁCH SẢN PHẨM:");
//             productsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             TableView<OrderDetail> productsTable = new TableView<>();
//             productsTable.setPrefHeight(300);
//             productsTable.setItems(details);

//             // Các cột
//             TableColumn<OrderDetail, String> colProductName = new TableColumn<>("Tên sản phẩm");
//             colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
//             colProductName.setPrefWidth(250);

//             TableColumn<OrderDetail, Integer> colQuantity = new TableColumn<>("SL");
//             colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
//             colQuantity.setPrefWidth(50);

//             TableColumn<OrderDetail, String> colUnitPrice = new TableColumn<>("Đơn giá");
//             colUnitPrice.setCellValueFactory(data ->
//                     new SimpleStringProperty(String.format("%,.0f₫", data.getValue().getUnitPrice())));
//             colUnitPrice.setPrefWidth(100);

//             TableColumn<OrderDetail, String> colWarranty = new TableColumn<>("Bảo hành");
//             colWarranty.setCellValueFactory(new PropertyValueFactory<>("warrantyType"));
//             colWarranty.setPrefWidth(100);

//             TableColumn<OrderDetail, String> colSubtotal = new TableColumn<>("Thành tiền");
//             colSubtotal.setCellValueFactory(data ->
//                     new SimpleStringProperty(String.format("%,.0f₫", data.getValue().getSubtotal())));
//             colSubtotal.setPrefWidth(120);

//             productsTable.getColumns().addAll(colProductName, colQuantity, colUnitPrice, colWarranty, colSubtotal);

//             // Tổng tiền
//             Label totalLabel = new Label("TỔNG TIỀN: " + order.getFormattedAmount());
//             totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e91e63;");

//             // Buttons
//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.setPadding(new Insets(10, 0, 0, 0));

//             Button btnPrint = new Button("In hóa đơn");
//             btnPrint.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnPrint.setPrefWidth(120);
//             btnPrint.setOnAction(e -> {
//                 // Gọi method in hóa đơn (sử dụng lại code cũ)
//                 AlertUtil.showInfo("Thông báo", "Tính năng in hóa đơn đang được phát triển!");
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnClose.setPrefWidth(100);
//             btnClose.setOnAction(e -> detailStage.close());

//             buttonBox.getChildren().addAll(btnPrint, btnClose);

//             // Thêm vào content
//             content.getChildren().addAll(infoGrid, productsLabel, productsTable, totalLabel, buttonBox);

//             // Layout chính
//             mainLayout.setTop(header);
//             mainLayout.setCenter(new ScrollPane(content));

//             Scene scene = new Scene(mainLayout, 700, 600);
//             detailStage.setScene(scene);
//             detailStage.show();

//             LOGGER.info("✅ Đã hiển thị chi tiết đơn hàng: " + order.getOrderID());

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi hiển thị chi tiết đơn hàng: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị chi tiết đơn hàng: " + e.getMessage());
//         }
//     }

//     // Helper method thêm dòng thông tin
//     private void addInfoRow(GridPane grid, String label, String value, int row) {
//         Label lblLabel = new Label(label);
//         lblLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");

//         Label lblValue = new Label(value != null ? value : "N/A");
//         lblValue.setStyle("-fx-font-weight: bold;");

//         grid.add(lblLabel, 0, row);
//         grid.add(lblValue, 1, row);
//     }
//     // Method hiển thị tất cả đơn hàng (nếu user chọn checkbox)
//     private void showAllOrdersWindow() {
//         try {
//             LOGGER.info("📋 Hiển thị tất cả đơn hàng...");

//             ObservableList<OrderHistory> allOrders = OrderHistoryServiceE.getOrderHistories();

//             if (allOrders.isEmpty()) {
//                 AlertUtil.showInfo("Thông báo", "Không có đơn hàng nào trong hệ thống!");
//                 return;
//             }

//             // Tạo cửa sổ đơn giản hiển thị danh sách
//             Stage listStage = new Stage();
//             listStage.setTitle("Tất cả đơn hàng (" + allOrders.size() + " đơn)");
//             listStage.setResizable(true);

//             // TableView đơn giản
//             TableView<OrderHistory> table = new TableView<>();
//             table.setItems(allOrders);

//             TableColumn<OrderHistory, String> colId = new TableColumn<>("Mã ĐH");
//             colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrderID()));
//             colId.setPrefWidth(100);

//             TableColumn<OrderHistory, String> colDate = new TableColumn<>("Ngày");
//             colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedDate()));
//             colDate.setPrefWidth(150);

//             TableColumn<OrderHistory, String> colCustomer = new TableColumn<>("Khách hàng");
//             colCustomer.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomerName()));
//             colCustomer.setPrefWidth(150);

//             TableColumn<OrderHistory, String> colTotal = new TableColumn<>("Tổng tiền");
//             colTotal.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedAmount()));
//             colTotal.setPrefWidth(120);

//             TableColumn<OrderHistory, Void> colAction = new TableColumn<>("Hành động");
//             colAction.setCellFactory(tc -> new TableCell<OrderHistory, Void>() {
//                 private final Button btn = new Button("Xem chi tiết");
//                 {
//                     btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
//                     btn.setOnAction(event -> {
//                         OrderHistory selectedOrder = getTableView().getItems().get(getIndex());
//                         if (selectedOrder != null) {
//                             listStage.close();
//                             showOrderByIdWindow(selectedOrder.getOrderID());
//                         }
//                     });
//                 }

//                 @Override
//                 protected void updateItem(Void item, boolean empty) {
//                     super.updateItem(item, empty);
//                     if (empty) {
//                         setGraphic(null);
//                     } else {
//                         setGraphic(btn);
//                     }
//                 }
//             });
//             colAction.setPrefWidth(120);

//             table.getColumns().addAll(colId, colDate, colCustomer, colTotal, colAction);

//             Scene scene = new Scene(new VBox(table), 800, 500);
//             listStage.setScene(scene);
//             listStage.show();

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi hiển thị tất cả đơn hàng: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị danh sách đơn hàng: " + e.getMessage());
//         }
//     }
//     // Hiển thị lịch sử đơn hàng từ bộ nhớ
//     // Thay thế method showOrderHistoryInMemory() cũ
//     private void showOrderHistoryInMemory() {
//         try {
//             // Tạo dialog nhập mã đơn hàng
//             Stage searchStage = new Stage();
//             searchStage.initModality(Modality.APPLICATION_MODAL);
//             searchStage.setTitle("Tìm kiếm đơn hàng");
//             searchStage.setResizable(false);

//             VBox layout = new VBox(15);
//             layout.setPadding(new Insets(20));
//             layout.setAlignment(Pos.CENTER);

//             // Header
//             Label headerLabel = new Label("TÌM KIẾM ĐƠN HÀNG");
//             headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

//             // Input mã đơn hàng
//             Label lblOrderId = new Label("Nhập mã đơn hàng:");
//             lblOrderId.setStyle("-fx-font-weight: bold;");

//             TextField txtOrderId = new TextField();
//             txtOrderId.setPromptText("Ví dụ: 1, 2, 3... hoặc ORD001, ORD002...");
//             txtOrderId.setPrefWidth(300);
//             txtOrderId.setStyle("-fx-font-size: 14px;");

//             // Hoặc xem tất cả
//             CheckBox chkShowAll = new CheckBox("Hiển thị tất cả đơn hàng");
//             chkShowAll.setStyle("-fx-font-size: 12px;");

//             // Buttons
//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);

//             Button btnSearch = new Button("Tìm kiếm");
//             btnSearch.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnSearch.setPrefWidth(100);

//             Button btnCancel = new Button("Hủy");
//             btnCancel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnCancel.setPrefWidth(100);

//             buttonBox.getChildren().addAll(btnSearch, btnCancel);

//             // Events
//             btnCancel.setOnAction(e -> searchStage.close());

//             btnSearch.setOnAction(e -> {
//                 try {
//                     searchStage.close();

//                     if (chkShowAll.isSelected()) {
//                         // Hiển thị tất cả đơn hàng
//                         showAllOrdersWindow();
//                     } else {
//                         // Tìm theo ID cụ thể
//                         String orderId = txtOrderId.getText().trim();
//                         if (orderId.isEmpty()) {
//                             AlertUtil.showWarning("Thông báo", "Vui lòng nhập mã đơn hàng!");
//                             return;
//                         }
//                         showOrderByIdWindow(orderId);
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi tìm kiếm đơn hàng: " + ex.getMessage(), ex);
//                     AlertUtil.showError("Lỗi", "Không thể tìm kiếm đơn hàng: " + ex.getMessage());
//                 }
//             });

//             // Enter để tìm kiếm
//             txtOrderId.setOnKeyPressed(event -> {
//                 if (event.getCode().toString().equals("ENTER")) {
//                     btnSearch.fire();
//                 }
//             });

//             layout.getChildren().addAll(headerLabel, lblOrderId, txtOrderId, chkShowAll, buttonBox);

//             Scene scene = new Scene(layout, 400, 250);
//             searchStage.setScene(scene);
//             searchStage.showAndWait();

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị dialog tìm kiếm: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể mở cửa sổ tìm kiếm: " + e.getMessage());
//         }
//     }

//     // Hiển thị chi tiết đơn hàng từ bộ nhớ
//     private void showOrderDetailsFromMemory(OrderSummary order) {
//         try {
//             if (order == null) {
//                 LOGGER.warning("Lỗi: OrderSummary object là null");
//                 return;
//             }

//             Stage detailStage = new Stage();
//             detailStage.initModality(Modality.APPLICATION_MODAL);
//             detailStage.setTitle("Chi tiết đơn hàng #" + order.getId());

//             BorderPane borderPane = new BorderPane();

//             // Header
//             HBox header = new HBox();
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: #2196F3;");

//             Label headerTitle = new Label("CHI TIẾT ĐƠN HÀNG #" + order.getId());
//             headerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

//             header.getChildren().add(headerTitle);
//             header.setAlignment(Pos.CENTER);

//             borderPane.setTop(header);

//             // Content
//             VBox content = new VBox(15);
//             content.setPadding(new Insets(20));

//             // Thông tin đơn hàng
//             VBox orderInfoBox = new VBox(8);
//             orderInfoBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");

//             Label lblCustomer = new Label("Khách hàng: " + order.getCustomerName());
//             Label lblPhone = new Label("SĐT: " + order.getCustomerPhone());
//             Label lblPayment = new Label("Phương thức thanh toán: " + order.getPaymentMethod());
//             Label lblDate = new Label("Ngày mua: " + order.getOrderDate());

//             orderInfoBox.getChildren().addAll(lblCustomer, lblPhone, lblPayment, lblDate);

//             // Danh sách sản phẩm
//             Label lblProductsTitle = new Label("Danh sách sản phẩm:");
//             lblProductsTitle.setStyle("-fx-font-weight: bold;");

//             TableView<CartItemEmployee> detailTable = new TableView<>();
//             detailTable.setPrefHeight(300);

//             TableColumn<CartItemEmployee, String> colProductName = new TableColumn<>("Tên sản phẩm");
//             colProductName.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleStringProperty("N/A");
//                 }
//                 String productName = data.getValue().getProductName();
//                 return new SimpleStringProperty(productName != null ? productName : "N/A");
//             });
//             colProductName.setPrefWidth(200);

//             TableColumn<CartItemEmployee, Integer> colQuantity = new TableColumn<>("SL");
//             colQuantity.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleIntegerProperty(0).asObject();
//                 }
//                 return new SimpleIntegerProperty(data.getValue().getQuantity()).asObject();
//             });
//             colQuantity.setPrefWidth(50);

//             TableColumn<CartItemEmployee, Double> colPrice = new TableColumn<>("Đơn giá");
//             colPrice.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleDoubleProperty(0).asObject();
//                 }
//                 return new SimpleDoubleProperty(data.getValue().getPrice()).asObject();
//             });
//             colPrice.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double price, boolean empty) {
//                     super.updateItem(price, empty);
//                     if (empty || price == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", price) + "đ");
//                     }
//                 }
//             });
//             colPrice.setPrefWidth(100);

//             // Thêm cột bảo hành
//             TableColumn<CartItemEmployee, String> colWarranty = new TableColumn<>("Bảo hành");
//             colWarranty.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleStringProperty("Không");
//                 }

//                 CartItemEmployee item = data.getValue();
//                 if (item.hasWarranty()) {
//                     return new SimpleStringProperty(item.getWarranty().getWarrantyType());
//                 } else {
//                     return new SimpleStringProperty("Không");
//                 }
//             });
//             colWarranty.setPrefWidth(100);

//             TableColumn<CartItemEmployee, Double> colSubtotal = new TableColumn<>("Thành tiền");
//             colSubtotal.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleDoubleProperty(0).asObject();
//                 }
//                 return new SimpleDoubleProperty(data.getValue().getTotalPrice()).asObject();
//             });
//             colSubtotal.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double total, boolean empty) {
//                     super.updateItem(total, empty);
//                     if (empty || total == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", total) + "đ");
//                     }
//                 }
//             });
//             colSubtotal.setPrefWidth(100);

//             detailTable.getColumns().addAll(colProductName, colQuantity, colPrice, colWarranty, colSubtotal);

//             // Kiểm tra null trước khi thêm items
//             if (order.getItems() != null) {
//                 detailTable.setItems(FXCollections.observableArrayList(order.getItems()));
//             } else {
//                 detailTable.setItems(FXCollections.observableArrayList());
//             }

//             // Hiển thị tổng tiền
//             Label lblTotal = new Label("Tổng tiền: " + String.format("%,.0f", order.getTotalAmount()) + "đ");
//             lblTotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e91e63;");

//             // Button in hóa đơn và đóng
//             Button btnPrint = new Button("In hóa đơn");
//             btnPrint.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnPrint.setPrefWidth(150);

//             // Fix lỗi lambda expression bằng cách sử dụng final variable
//             final int orderId = order.getId();
//             final double totalAmount = order.getTotalAmount();
//             final String customerName2 = order.getCustomerName();
//             final String customerPhone2 = order.getCustomerPhone();
//             final String paymentMethod2 = order.getPaymentMethod();
//             final String orderDateTime = order.getOrderDate();
//             final List<CartItemEmployee> orderItems = order.getItems() != null ? order.getItems() : new ArrayList<>();

//             btnPrint.setOnAction(e -> {
//                 try {
//                     // In hóa đơn với các biến final
//                     printReceiptWithPaymentMethod(
//                             orderId,
//                             orderItems,
//                             totalAmount,
//                             customerName2,
//                             customerPhone2,
//                             paymentMethod2,
//                             orderDateTime,
//                             currentUser
//                     );
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi in hóa đơn", ex);
//                     showErrorAlert("Có lỗi xảy ra: " + ex.getMessage());
//                 }
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnClose.setPrefWidth(100);
//             btnClose.setOnAction(e -> detailStage.close());

//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.getChildren().addAll(btnPrint, btnClose);
//             buttonBox.setPadding(new Insets(10, 0, 0, 0));

//             content.getChildren().addAll(orderInfoBox, lblProductsTitle, detailTable, lblTotal, buttonBox);

//             borderPane.setCenter(content);

//             Scene scene = new Scene(borderPane, 650, 550);
//             detailStage.setScene(scene);
//             detailStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị chi tiết đơn hàng", e);
//             showErrorAlert("Có lỗi xảy ra: " + e.getMessage());
//         }
//     }

//     // Phương thức in hóa đơn có thêm phương thức thanh toán và thông tin bảo hành
//     public void printReceiptWithPaymentMethod(int orderID, List<CartItemEmployee> items, double totalAmount,
//                                               String customerName, String customerPhone, String paymentMethod,
//                                               String orderDateTime, String cashierName) {
//         try {
//             // Kiểm tra danh sách sản phẩm
//             if (items == null || items.isEmpty()) {
//                 Alert alert = new Alert(Alert.AlertType.WARNING);
//                 alert.setTitle("Cảnh báo");
//                 alert.setHeaderText("Không thể in hóa đơn");
//                 alert.setContentText("Không có sản phẩm nào trong đơn hàng.");
//                 alert.showAndWait();
//                 return;
//             }

//             // Tạo cảnh báo để hiển thị trước khi in
//             Alert printingAlert = new Alert(Alert.AlertType.INFORMATION);
//             printingAlert.setTitle("Đang in hóa đơn");
//             printingAlert.setHeaderText("Đang chuẩn bị in hóa đơn");
//             printingAlert.setContentText("Vui lòng đợi trong giây lát...");
//             printingAlert.show();

//             // Tạo nội dung hóa đơn
//             VBox receiptContent = new VBox(5);
//             receiptContent.setPadding(new Insets(20));
//             receiptContent.setStyle("-fx-background-color: white;");

//             // Tiêu đề
//             Label lblTitle = new Label("HÓA ĐƠN THANH TOÁN");
//             lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-alignment: center;");
//             lblTitle.setMaxWidth(Double.MAX_VALUE);
//             lblTitle.setAlignment(Pos.CENTER);

//             // Logo công ty (nếu có)
//             ImageView logo = new ImageView();
//             try {
//                 InputStream is = getClass().getResourceAsStream("/com/example/stores/images/layout/employee_logo.png");
//                 if (is != null) {
//                     logo.setImage(new Image(is));
//                     logo.setFitWidth(100);
//                     logo.setPreserveRatio(true);
//                 }
//             } catch (Exception e) {
//                 LOGGER.log(Level.WARNING, "Không tìm thấy logo", e);
//             }

//             // Thông tin cửa hàng
//             Label lblStoreName = new Label("COMPUTER STORE");
//             lblStoreName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             Label lblStoreAddress = new Label("Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM");
//             Label lblStorePhone = new Label("Điện thoại: 028.1234.5678");

//             // Thông tin hóa đơn
//             Label lblOrderID = new Label("Mã đơn hàng: #" + orderID);
//             lblOrderID.setStyle("-fx-font-weight: bold;");

//             Label lblDateTime = new Label("Ngày: " + orderDateTime);
//             Label lblCashier = new Label("Thu ngân: " + cashierName);
//             Label lblCustomerName = new Label("Khách hàng: " + customerName);
//             Label lblCustomerPhone = new Label("SĐT khách hàng: " + customerPhone);
//             Label lblPaymentMethod = new Label("Phương thức thanh toán: " + paymentMethod);
//             lblPaymentMethod.setStyle("-fx-font-weight: bold;");

//             // Tạo đường kẻ ngăn cách
//             Separator sep1 = new Separator();
//             sep1.setMaxWidth(Double.MAX_VALUE);

//             // Tiêu đề bảng sản phẩm
//             HBox tableHeader = new HBox(10);
//             Label lblProductHeader = new Label("Sản phẩm");
//             lblProductHeader.setPrefWidth(200);
//             lblProductHeader.setStyle("-fx-font-weight: bold;");

//             Label lblQtyHeader = new Label("SL");
//             lblQtyHeader.setPrefWidth(50);
//             lblQtyHeader.setStyle("-fx-font-weight: bold;");

//             Label lblPriceHeader = new Label("Đơn giá");
//             lblPriceHeader.setPrefWidth(100);
//             lblPriceHeader.setStyle("-fx-font-weight: bold;");

//             Label lblWarrantyHeader = new Label("Bảo hành");
//             lblWarrantyHeader.setPrefWidth(100);
//             lblWarrantyHeader.setStyle("-fx-font-weight: bold;");

//             Label lblSubtotalHeader = new Label("Thành tiền");
//             lblSubtotalHeader.setPrefWidth(100);
//             lblSubtotalHeader.setStyle("-fx-font-weight: bold;");

//             tableHeader.getChildren().addAll(lblProductHeader, lblQtyHeader, lblPriceHeader, lblWarrantyHeader, lblSubtotalHeader);

//             // Danh sách sản phẩm
//             VBox productsBox = new VBox(5);
//             double totalWarrantyPrice = 0.0; // Tổng phí bảo hành

//             for (CartItemEmployee item : items) {
//                 if (item == null) continue;

//                 // Dòng sản phẩm
//                 HBox row = new HBox(10);

//                 String productName = item.getProductName();
//                 if (productName == null) productName = "Sản phẩm không tên";

//                 // Tạo VBox để hiển thị tên sản phẩm + bảo hành nếu có
//                 VBox productInfoBox = new VBox(2);
//                 Label lblProduct = new Label(productName);
//                 lblProduct.setPrefWidth(200);
//                 lblProduct.setWrapText(true);
//                 productInfoBox.getChildren().add(lblProduct);

//                 Label lblQty = new Label(String.valueOf(item.getQuantity()));
//                 lblQty.setPrefWidth(50);

//                 Label lblPrice = new Label(String.format("%,.0f", item.getPrice()) + "đ");
//                 lblPrice.setPrefWidth(100);

//                 // Hiển thị thông tin bảo hành
//                 Label lblWarranty;
//                 if (item.hasWarranty()) {
//                     lblWarranty = new Label(item.getWarranty().getWarrantyType());
//                     totalWarrantyPrice += item.getWarranty().getWarrantyPrice();
//                 } else {
//                     lblWarranty = new Label("Không");
//                 }
//                 lblWarranty.setPrefWidth(100);

//                 // Hiển thị tổng giá trị sản phẩm
//                 Label lblSubtotal = new Label(String.format("%,.0f", item.getTotalPrice()) + "đ");
//                 lblSubtotal.setPrefWidth(100);

//                 row.getChildren().addAll(productInfoBox, lblQty, lblPrice, lblWarranty, lblSubtotal);
//                 productsBox.getChildren().add(row);
//             }

//             // Thêm đường kẻ ngăn cách
//             Separator sep2 = new Separator();
//             sep2.setMaxWidth(Double.MAX_VALUE);

//             // Hiển thị tổng phí bảo hành nếu có
//             VBox summaryBox = new VBox(5);

//             if (totalWarrantyPrice > 0) {
//                 HBox warrantyRow = new HBox(10);
//                 warrantyRow.setAlignment(Pos.CENTER_RIGHT);

//                 Label lblWarrantyTotalHeader = new Label("Tổng phí bảo hành:");
//                 Label lblWarrantyValue = new Label(String.format("%,.0f", totalWarrantyPrice) + "đ");
//                 lblWarrantyValue.setStyle("-fx-font-size: 13px;");

//                 warrantyRow.getChildren().addAll(lblWarrantyHeader, lblWarrantyValue);
//                 summaryBox.getChildren().add(warrantyRow);
//             }

//             // Tổng tiền
//             HBox totalRow = new HBox(10);
//             totalRow.setAlignment(Pos.CENTER_RIGHT);

//             Label lblTotalHeader = new Label("Tổng tiền thanh toán:");
//             lblTotalHeader.setStyle("-fx-font-weight: bold;");

//             Label lblTotalValue = new Label(String.format("%,.0f", totalAmount) + "đ");
//             lblTotalValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             totalRow.getChildren().addAll(lblTotalHeader, lblTotalValue);
//             summaryBox.getChildren().add(totalRow);

//             // Thêm thông tin thanh toán chuyển khoản nếu là phương thức chuyển khoản
//             VBox paymentInfoBox = new VBox(10);
//             paymentInfoBox.setAlignment(Pos.CENTER);

//             if ("Chuyển khoản".equals(paymentMethod)) {
//                 // Thêm đường kẻ ngăn cách
//                 Separator sepPayment = new Separator();
//                 sepPayment.setMaxWidth(Double.MAX_VALUE);

//                 Label lblPaymentInfo = new Label("THÔNG TIN CHUYỂN KHOẢN");
//                 lblPaymentInfo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
//                 lblPaymentInfo.setAlignment(Pos.CENTER);
//                 lblPaymentInfo.setMaxWidth(Double.MAX_VALUE);

//                 Label lblBank = new Label("Ngân hàng: TECHCOMBANK");
//                 Label lblAccount = new Label("Số tài khoản: 1903 5552 6789");
//                 Label lblAccountName = new Label("Chủ TK: CÔNG TY COMPUTER STORE");
//                 Label lblContent = new Label("Nội dung CK: " + orderID + " " + customerPhone);

//                 // QR Code cho chuyển khoản
//                 ImageView qrCode = new ImageView();
//                 try {
//                     // Mặc định sử dụng ảnh QR từ resources
//                     InputStream qrIs = getClass().getResourceAsStream("/com/example/stores/images/qr_payment.png");
//                     if (qrIs != null) {
//                         qrCode.setImage(new Image(qrIs));
//                         qrCode.setFitWidth(150);
//                         qrCode.setPreserveRatio(true);
//                     } else {
//                         // QR Code cho chuyển khoản - tạo ảnh trống nếu không tìm thấy
//                         qrCode.setFitWidth(150);
//                         qrCode.setFitHeight(150);
//                         qrCode.setStyle("-fx-background-color: #f0f0f0;");
//                     }
//                 } catch (Exception e) {
//                     LOGGER.log(Level.WARNING, "Không tìm thấy ảnh QR", e);
//                 }

//                 paymentInfoBox.getChildren().addAll(sepPayment, lblPaymentInfo, lblBank, lblAccount, lblAccountName, lblContent, qrCode);
//             }

//             // Thông tin cuối hóa đơn
//             Label lblThankYou = new Label("Cảm ơn quý khách đã mua hàng!");
//             lblThankYou.setAlignment(Pos.CENTER);
//             lblThankYou.setMaxWidth(Double.MAX_VALUE);
//             lblThankYou.setStyle("-fx-font-style: italic; -fx-alignment: center;");

//             Label lblContact = new Label("Hotline: 1800.1234 - Website: www.computerstore.com.vn");
//             lblContact.setAlignment(Pos.CENTER);
//             lblContact.setMaxWidth(Double.MAX_VALUE);
//             lblContact.setStyle("-fx-font-size: 10px; -fx-alignment: center;");

//             // Thêm thông tin chính sách bảo hành
//             Label lblWarrantyPolicy = new Label("Để biết thêm về chính sách bảo hành, vui lòng xem tại website");
//             lblWarrantyPolicy.setAlignment(Pos.CENTER);
//             lblWarrantyPolicy.setMaxWidth(Double.MAX_VALUE);
//             lblWarrantyPolicy.setStyle("-fx-font-size: 10px; -fx-font-style: italic; -fx-alignment: center;");

//             // Thêm tất cả các phần tử vào hóa đơn
//             HBox logoBox = new HBox(10);
//             logoBox.setAlignment(Pos.CENTER);
//             logoBox.getChildren().add(logo);

//             receiptContent.getChildren().addAll(
//                     lblTitle,
//                     logoBox,
//                     lblStoreName,
//                     lblStoreAddress,
//                     lblStorePhone,
//                     new Separator(),
//                     lblOrderID,
//                     lblDateTime,
//                     lblCashier,
//                     lblCustomerName,
//                     lblCustomerPhone,
//                     lblPaymentMethod,
//                     sep1,
//                     tableHeader,
//                     productsBox,
//                     sep2,
//                     summaryBox
//             );

//             // Thêm thông tin thanh toán chuyển khoản nếu có
//             if (!paymentInfoBox.getChildren().isEmpty()) {
//                 receiptContent.getChildren().add(paymentInfoBox);
//             }

//             // Thêm phần kết
//             Separator sepEnd = new Separator();
//             sepEnd.setMaxWidth(Double.MAX_VALUE);

//             receiptContent.getChildren().addAll(
//                     sepEnd,
//                     lblThankYou,
//                     lblContact,
//                     lblWarrantyPolicy
//             );

//             // Định dạng kích thước hóa đơn
//             ScrollPane scrollPane = new ScrollPane(receiptContent);
//             scrollPane.setPrefWidth(550); // Tăng kích thước để hiển thị đủ cột bảo hành
//             scrollPane.setPrefHeight(600);
//             scrollPane.setFitToWidth(true);

//             // Tạo Scene và Stage để hiển thị trước khi in
//             Scene scene = new Scene(scrollPane);
//             Stage printPreviewStage = new Stage();
//             printPreviewStage.setTitle("Xem trước hóa đơn");
//             printPreviewStage.setScene(scene);

//             // Đóng cảnh báo đang in
//             printingAlert.close();

//             // Hiển thị hóa đơn
//             printPreviewStage.show();

//             // Thêm nút in và lưu vào cửa sổ xem trước
//             Button btnPrint = new Button("In");
//             btnPrint.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
//             btnPrint.setOnAction(e -> {
//                 try {
//                     PrinterJob job = PrinterJob.createPrinterJob();
//                     if (job != null) {
//                         boolean success = job.printPage(receiptContent);
//                         if (success) {
//                             job.endJob();
//                             printPreviewStage.close();

//                             Alert printSuccessAlert = new Alert(Alert.AlertType.INFORMATION);
//                             printSuccessAlert.setTitle("In thành công");
//                             printSuccessAlert.setHeaderText("Hóa đơn đã được gửi đến máy in");
//                             printSuccessAlert.setContentText("Vui lòng kiểm tra máy in của bạn.");
//                             printSuccessAlert.showAndWait();
//                         }
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi in hóa đơn", ex);
//                     showErrorAlert("Lỗi khi in hóa đơn: " + ex.getMessage());
//                 }
//             });

//             // Nút lưu PDF (giả định)
//             Button btnSave = new Button("Lưu PDF");
//             btnSave.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
//             btnSave.setOnAction(e -> {
//                 try {
//                     Alert saveAlert = new Alert(Alert.AlertType.INFORMATION);
//                     saveAlert.setTitle("Lưu PDF");
//                     saveAlert.setHeaderText("Hóa đơn đã được lưu");
//                     saveAlert.setContentText("Hóa đơn đã được lưu vào thư mục Documents.");
//                     saveAlert.showAndWait();
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi lưu PDF", ex);
//                     showErrorAlert("Lỗi khi lưu PDF: " + ex.getMessage());
//                 }
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnClose.setOnAction(e -> printPreviewStage.close());

//             HBox buttonBox = new HBox(10, btnPrint, btnSave, btnClose);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.setPadding(new Insets(10));

//             BorderPane borderPane = new BorderPane();
//             borderPane.setCenter(scrollPane);
//             borderPane.setBottom(buttonBox);

//             scene.setRoot(borderPane);

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi in hóa đơn", e);
//             Alert errorAlert = new Alert(Alert.AlertType.ERROR);
//             errorAlert.setTitle("Lỗi in hóa đơn");
//             errorAlert.setHeaderText("Không thể in hóa đơn");
//             errorAlert.setContentText("Chi tiết lỗi: " + e.getMessage());
//             errorAlert.showAndWait();
//         }
//     }

//     /**
//      * Thêm sản phẩm vào giỏ hàng với thông tin bảo hành
//      */
//     private void addToCartWithWarranty(CartItemEmployee item) {
//         if (item == null) {
//             LOGGER.warning("Lỗi: CartItemEmployee là null");
//             return;
//         }

//         // Tìm sản phẩm trong database để kiểm tra tồn kho
//         Product product = findProductById(item.getProductID());
//         if (product == null) {
//             AlertUtil.showWarning("Lỗi", "Không tìm thấy thông tin sản phẩm");
//             return;
//         }

//         // Kiểm tra số lượng tồn kho trước khi thêm
//         if (product.getQuantity() <= 0) {
//             AlertUtil.showWarning("Hết hàng", "Sản phẩm đã hết hàng!");
//             return;
//         }

//         // Tìm kiếm sản phẩm trong giỏ hàng với CÙNG loại bảo hành
//         boolean existingFound = false;
//         for (CartItemEmployee cartItem : cartItems) {
//             if (cartItem.getProductID().equals(item.getProductID())) {
//                 // Phải cùng sản phẩm và cùng loại bảo hành
//                 if (cartItem.hasWarranty() == item.hasWarranty() &&
//                         (!cartItem.hasWarranty() ||
//                                 cartItem.getWarranty().getWarrantyType().equals(item.getWarranty().getWarrantyType()))) {

//                     if (cartItem.getQuantity() < product.getQuantity()) {
//                         // Cập nhật số lượng nếu còn hàng
//                         cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
//                         existingFound = true;
//                         LOGGER.info("Đã tăng số lượng " + cartItem.getProductName() +
//                                 " (BH: " + (cartItem.hasWarranty() ? cartItem.getWarranty().getWarrantyType() : "Không") +
//                                 ") lên " + cartItem.getQuantity());
//                     } else {
//                         AlertUtil.showWarning("Số lượng tối đa",
//                                 "Không thể thêm nữa, số lượng trong kho chỉ còn " + product.getQuantity());
//                     }
//                     break;
//                 }
//             }
//         }

//         // Nếu không tìm thấy sản phẩm đã có trong giỏ với cùng loại bảo hành
//         if (!existingFound) {
//             cartItems.add(item);
//             LOGGER.info("Đã thêm " + item.getProductName() +
//                     " (BH: " + (item.hasWarranty() ? item.getWarranty().getWarrantyType() : "Không") +
//                     ") vào giỏ hàng");
//         }

//         // Cập nhật hiển thị giỏ hàng
//         updateCartDisplay();
//     }

//     // Tìm sản phẩm theo ID
//     private Product findProductById(String productID) {
//         if (productID == null || products == null) {
//             return null;
//         }

//         for (Product product : products) {
//             if (product.getProductID().equals(productID)) {
//                 return product;
//             }
//         }

//         return null;
//     }

//     // Sửa lại phần hiển thị dialog chi tiết sản phẩm trong PosOverviewController
//     private void showProductDetails(Product product) {
//         try {
//             if (product == null) {
//                 LOGGER.warning("Lỗi: Product object là null");
//                 return;
//             }

//             Stage detailStage = new Stage();
//             detailStage.initModality(Modality.APPLICATION_MODAL);
//             detailStage.setTitle("Chi tiết sản phẩm");

//             VBox layout = new VBox(10);
//             layout.setPadding(new Insets(20));
//             layout.setStyle("-fx-background-color: white;");

//             // Hiển thị ảnh sản phẩm (giữ nguyên code cũ)
//             final ImageView productImage = new ImageView();
//             productImage.setFitWidth(200);
//             productImage.setFitHeight(150);
//             productImage.setPreserveRatio(true);

//             // Tải ảnh sản phẩm (giữ nguyên code cũ)
//             String imagePath = product.getImagePath();
//             if (imagePath != null && !imagePath.startsWith("/")) {
//                 imagePath = "/com/example/stores/images/" + imagePath;
//             } else if (imagePath == null) {
//                 imagePath = "/com/example/stores/images/no_image.png";
//             }

//             try {
//                 Image image = new Image(getClass().getResourceAsStream(imagePath));
//                 productImage.setImage(image);
//             } catch (Exception e) {
//                 productImage.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/no_image.png")));
//                 LOGGER.warning("Không tải được ảnh chi tiết sản phẩm: " + e.getMessage());
//             }

//             final HBox imageBox = new HBox();
//             imageBox.setAlignment(Pos.CENTER);
//             imageBox.getChildren().add(productImage);

//             // Tên sản phẩm
//             String productName = (product.getProductName() != null) ? product.getProductName() : "Sản phẩm không có tên";
//             Label lblName = new Label(productName);
//             lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
//             lblName.setWrapText(true);

//             // Giá sản phẩm
//             Label lblPrice = new Label(String.format("Giá: %,d₫", (long)product.getPrice()));
//             lblPrice.setStyle("-fx-text-fill: #e91e63; -fx-font-weight: bold; -fx-font-size: 16px;");

//             // Thông tin cơ bản (giữ nguyên code cũ)
//             VBox specsBox = new VBox(5);
//             specsBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");

//             if (product.getCategoryID() != null) {
//                 Label lblCategory = new Label("Danh mục: " + getCategoryName(product.getCategoryID()));
//                 specsBox.getChildren().add(lblCategory);
//             }

//             Label lblStock = new Label("Tồn kho: " + product.getQuantity() + " sản phẩm");
//             specsBox.getChildren().add(lblStock);

//             String status = product.getStatus();
//             Label lblStatus = new Label("Trạng thái: " + (status != null ? status : "Không xác định"));
//             lblStatus.setStyle(status != null && status.equals("Còn hàng") ?
//                     "-fx-text-fill: #4caf50; -fx-font-weight: bold;" :
//                     "-fx-text-fill: #f44336; -fx-font-weight: bold;");
//             specsBox.getChildren().add(lblStatus);

//             // PHẦN BẢO HÀNH - CẬP NHẬT CHỈ CÒN 2 LOẠI: THƯỜNG VÀ VÀNG
//             VBox warrantyBox = new VBox(5);
//             warrantyBox.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 10; -fx-background-radius: 5;");

//             Label lblWarrantyTitle = new Label("Lựa chọn bảo hành:");
//             lblWarrantyTitle.setStyle("-fx-font-weight: bold;");
//             warrantyBox.getChildren().add(lblWarrantyTitle);

//             // ComboBox để chọn bảo hành
//             ComboBox<String> cbWarranty = new ComboBox<>();

//             // Kiểm tra sản phẩm có đủ điều kiện bảo hành thường không
//             boolean isEligibleForStdWarranty = WarrantyCalculator.isEligibleForStandardWarranty(product);

//             Label lblWarrantyInfo = new Label();

//             // Hiển thị các lựa chọn bảo hành dựa trên điều kiện
//             if (isEligibleForStdWarranty) {
//                 cbWarranty.getItems().addAll("Thường", "Vàng");
//                 cbWarranty.setValue("Thường");

//                 // Miêu tả bảo hành
//                 lblWarrantyInfo.setText("✅ Sản phẩm được bảo hành Thường miễn phí 12 tháng");
//                 lblWarrantyInfo.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 12px;");
//             } else {
//                 cbWarranty.getItems().add("Không");
//                 cbWarranty.setValue("Không");

//                 // Miêu tả không đủ điều kiện
//                 lblWarrantyInfo.setText("❌ Sản phẩm dưới 500.000đ không được bảo hành");
//                 lblWarrantyInfo.setStyle("-fx-text-fill: #f44336; -fx-font-size: 12px;");
//             }

//             warrantyBox.getChildren().addAll(cbWarranty, lblWarrantyInfo);

//             // Hiển thị phí bảo hành
//             Label lblWarrantyPrice = new Label("Phí bảo hành: 0đ");
//             warrantyBox.getChildren().add(lblWarrantyPrice);

//             // Hiển thị tổng tiền kèm bảo hành
//             Label lblTotalWithWarranty = new Label("Tổng tiền: " + String.format("%,d₫", (long)product.getPrice()));
//             lblTotalWithWarranty.setStyle("-fx-font-weight: bold;");
//             warrantyBox.getChildren().add(lblTotalWithWarranty);

//             // Cập nhật giá bảo hành khi thay đổi loại bảo hành
//             cbWarranty.setOnAction(e -> {
//                 String selectedType = cbWarranty.getValue();

//                 if ("Không".equals(selectedType) || "Thường".equals(selectedType)) {
//                     lblWarrantyPrice.setText("Phí bảo hành: 0đ");
//                     lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,d₫", (long)product.getPrice()));

//                     if ("Thường".equals(selectedType)) {
//                         lblWarrantyInfo.setText("✅ Bảo hành Thường miễn phí 12 tháng");
//                         lblWarrantyInfo.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 12px;");
//                     } else {
//                         lblWarrantyInfo.setText("❌ Không bảo hành");
//                         lblWarrantyInfo.setStyle("-fx-text-fill: #f44336; -fx-font-size: 12px;");
//                     }
//                     return;
//                 }

//                 // Tính phí bảo hành Vàng (10% giá sản phẩm)
//                 double warrantyFee = product.getPrice() * 0.1;
//                 lblWarrantyPrice.setText("Phí bảo hành: " + String.format("%,d₫", (long)warrantyFee));

//                 // Cập nhật tổng tiền
//                 double totalPrice = product.getPrice() + warrantyFee;
//                 lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,d₫", (long)totalPrice));

//                 // Thêm giải thích về bảo hành Vàng
//                 lblWarrantyInfo.setText("✨ Bảo hành Vàng 24 tháng, 1 đổi 1 trong 24 tháng");
//                 lblWarrantyInfo.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 12px; -fx-font-weight: bold;");
//             });

//             // Mô tả sản phẩm và nút thêm vào giỏ (giữ nguyên code)
//             Label lblDescTitle = new Label("Mô tả sản phẩm:");
//             lblDescTitle.setStyle("-fx-font-weight: bold;");

//             String description = (product.getDescription() != null) ? product.getDescription() : "Không có thông tin";
//             TextArea txtDescription = new TextArea(description);
//             txtDescription.setWrapText(true);
//             txtDescription.setEditable(false);
//             txtDescription.setPrefHeight(100);

//             // Nút thêm vào giỏ
//             Button btnAddToCart = new Button("Thêm vào giỏ");
//             btnAddToCart.setPrefWidth(200);
//             btnAddToCart.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnAddToCart.setOnAction(e -> {
//                 try {
//                     // Lấy loại bảo hành đã chọn
//                     String selectedWarranty = cbWarranty.getValue();

//                     // Tạo đối tượng CartItemEmployee mới
//                     CartItemEmployee newItem = new CartItemEmployee(
//                             product.getProductID(),
//                             product.getProductName(),
//                             product.getPrice(),
//                             1,
//                             product.getImagePath(),
//                             employeeId,
//                             currentUser != null ? currentUser : "unknown",
//                             product.getCategoryID()
//                     );

//                     // Tạo bảo hành nếu không phải là "Không" bảo hành
//                     if ("Thường".equals(selectedWarranty) || "Vàng".equals(selectedWarranty)) {
//                         // Tạo bảo hành và gán vào sản phẩm
//                         Warranty warranty = WarrantyCalculator.createWarranty(product, selectedWarranty);
//                         newItem.setWarranty(warranty);
//                     }

//                     // Thêm vào giỏ hàng
//                     addToCartWithWarranty(newItem);

//                     detailStage.close(); // Đóng cửa sổ chi tiết
//                     AlertUtil.showInformation("Thành công", "Đã thêm sản phẩm vào giỏ hàng!");
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi thêm sản phẩm vào giỏ hàng", ex);
//                     AlertUtil.showError("Lỗi", "Không thể thêm sản phẩm vào giỏ hàng: " + ex.getMessage());
//                 }
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setPrefWidth(100);
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnClose.setOnAction(e -> detailStage.close());

//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.getChildren().addAll(btnAddToCart, btnClose);

//             // Thêm tất cả vào layout
//             layout.getChildren().addAll(
//                     imageBox,
//                     lblName,
//                     lblPrice,
//                     new Separator(),
//                     specsBox,
//                     new Separator(),
//                     warrantyBox,
//                     new Separator(),
//                     lblDescTitle,
//                     txtDescription,
//                     buttonBox
//             );

//             Scene scene = new Scene(layout, 400, 800);
//             detailStage.setScene(scene);
//             detailStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị chi tiết sản phẩm", e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị chi tiết sản phẩm: " + e.getMessage());
//         }
//     }

//     // Tạo dòng hiển thị cho sản phẩm trong giỏ hàng
//     private HBox createCartItemRow(CartItemEmployee item) {
//         HBox row = new HBox();
//         row.setSpacing(10);
//         row.setPadding(new Insets(5));
//         row.setAlignment(Pos.CENTER_LEFT);

//         // Tên sản phẩm với thông tin bảo hành
//         VBox productInfoBox = new VBox(2);
//         Label lblName = new Label(item.getProductName());
//         lblName.setStyle("-fx-font-weight: bold;");
//         productInfoBox.getChildren().add(lblName);

//         // Thêm thông tin bảo hành nếu có
//         if (item.hasWarranty()) {
//             Label lblWarranty = new Label("BH: " + item.getWarranty().getWarrantyType());
//             lblWarranty.setStyle("-fx-font-size: 11px; -fx-text-fill: #2196F3;");
//             productInfoBox.getChildren().add(lblWarranty);
//         }

//         productInfoBox.setPrefWidth(200);

//         // Số lượng với nút tăng/giảm
//         HBox quantityBox = new HBox(5);
//         quantityBox.setAlignment(Pos.CENTER);

//         Button btnMinus = new Button("-");
//         btnMinus.setMinWidth(30);
//         btnMinus.setOnAction(e -> decreaseQuantity(item));

//         Label lblQuantity = new Label(String.valueOf(item.getQuantity()));
//         lblQuantity.setAlignment(Pos.CENTER);
//         lblQuantity.setMinWidth(30);
//         lblQuantity.setStyle("-fx-font-weight: bold;");

//         Button btnPlus = new Button("+");
//         btnPlus.setMinWidth(30);
//         btnPlus.setOnAction(e -> increaseQuantity(item));

//         quantityBox.getChildren().addAll(btnMinus, lblQuantity, btnPlus);
//         quantityBox.setPrefWidth(120);

//         // Đơn giá
//         Label lblPrice = new Label(String.format("%,.0f", item.getPrice()) + "đ");
//         lblPrice.setPrefWidth(100);
//         lblPrice.setAlignment(Pos.CENTER_RIGHT);

//         // Bảo hành
//         Label lblWarranty = new Label(item.hasWarranty() ? item.getWarranty().getWarrantyType() : "Không");
//         lblWarranty.setPrefWidth(80);
//         lblWarranty.setAlignment(Pos.CENTER);
//         if (item.hasWarranty()) {
//             lblWarranty.setStyle("-fx-text-fill: #4CAF50;");
//         }

//         // Tổng tiền
//         Label lblTotal = new Label(String.format("%,.0f", item.getTotalPrice()) + "đ");
//         lblTotal.setPrefWidth(100);
//         lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #e91e63;");
//         lblTotal.setAlignment(Pos.CENTER_RIGHT);

//         // Nút xóa
//         Button btnRemove = new Button("✖");
//         btnRemove.setStyle("-fx-background-color: transparent; -fx-text-fill: #f44336; -fx-font-weight: bold;");
//         btnRemove.setOnAction(e -> removeFromCart(item));

//         // Thêm tất cả vào dòng
//         row.getChildren().addAll(productInfoBox, quantityBox, lblPrice, lblWarranty, lblTotal, btnRemove);

//         return row;
//     }

//     // Tăng số lượng sản phẩm trong giỏ hàng
//     private void increaseQuantity(CartItemEmployee item) {
//         if (item == null) return;

//         Product product = findProductById(item.getProductID());
//         if (product == null) {
//             AlertUtil.showWarning("Lỗi", "Không tìm thấy thông tin sản phẩm");
//             return;
//         }

//         // Kiểm tra số lượng tồn kho
//         if (item.getQuantity() < product.getQuantity()) {
//             item.setQuantity(item.getQuantity() + 1);
//             updateCartDisplay();
//         } else {
//             AlertUtil.showWarning("Số lượng tối đa",
//                     "Không thể thêm nữa, số lượng trong kho chỉ còn " + product.getQuantity());
//         }
//     }

//     // Giảm số lượng sản phẩm trong giỏ hàng
//     private void decreaseQuantity(CartItemEmployee item) {
//         if (item == null) return;

//         if (item.getQuantity() > 1) {
//             item.setQuantity(item.getQuantity() - 1);
//             updateCartDisplay();
//         } else {
//             // Nếu số lượng là 1, hỏi xem có muốn xóa không
//             Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//             alert.setTitle("Xóa sản phẩm");
//             alert.setHeaderText("Xác nhận xóa");
//             alert.setContentText("Bạn có muốn xóa sản phẩm này khỏi giỏ hàng?");

//             Optional<ButtonType> result = alert.showAndWait();
//             if (result.isPresent() && result.get() == ButtonType.OK) {
//                 removeFromCart(item);
//             }
//         }
//     }

//     // Xóa sản phẩm khỏi giỏ hàng
//     private void removeFromCart(CartItemEmployee item) {
//         if (item != null) {
//             cartItems.remove(item);
//             updateCartDisplay();
//         }
//     }

//     // Cập nhật hiển thị giỏ hàng
//     private void updateCartDisplay() {
//         // Cập nhật tổng tiền
//         updateTotal();

//         // Cập nhật TableView
//         cartTable.refresh();
//     }

//     // Cập nhật tổng tiền giỏ hàng
//     private void updateTotal() {
//         double total = calculateTotalAmount();
//         if (lblTotal != null) {
//             lblTotal.setText("Tổng tiền: " + String.format("%,.0f", total) + "đ");
//         }
//     }

//     // Tính tổng tiền giỏ hàng
//     private double calculateTotalAmount() {
//         double total = 0.0;
//         for (CartItemEmployee item : cartItems) {
//             if (item != null) {
//                 total += item.getTotalPrice();
//             }
//         }
//         return total;
//     }

//     // Xóa toàn bộ giỏ hàng
//     private void clearCart() {
//         cartItems.clear();
//         updateCartDisplay();
//         LOGGER.info("Đã xóa toàn bộ giỏ hàng");
//     }

//     // Lấy tên danh mục từ ID
//     private String getCategoryName(String categoryId) {
//         if (categoryId == null) return "Không xác định";

//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.warning("Không thể kết nối đến database");
//                 return "Không xác định";
//             }

//             // FIX LỖI: Sửa tên bảng từ Category thành Categories và category_name thành categoryName
//             String query = "SELECT categoryName FROM Categories WHERE categoryID = ?";
//             stmt = conn.prepareStatement(query);
//             stmt.setString(1, categoryId);
//             rs = stmt.executeQuery();

//             if (rs.next()) {
//                 return rs.getString("categoryName");
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.WARNING, "Lỗi SQL khi lấy tên danh mục: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.WARNING, "Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.WARNING, "Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }

//         return "Không xác định";
//     }
//     // Lấy danh sách các danh mục phân biệt
//     private List<String> getDistinctCategories() {
//         List<String> categories = new ArrayList<>();
//         categories.add("Tất cả"); // Luôn có tùy chọn "Tất cả"

//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.severe("💀 QUẠC!!! Không thể kết nối đến database");
//                 return categories;
//             }

//             // FIX LỖI: Sửa tên bảng từ Category thành Categories
//             // Sửa tên cột từ category_name thành categoryName - match với schema thực tế
//             String query = "SELECT DISTINCT categoryID, categoryName FROM Categories ORDER BY categoryName";
//             stmt = conn.prepareStatement(query);
//             rs = stmt.executeQuery();

//             int categoryCount = 0;

//             while (rs.next()) {
//                 String categoryName = rs.getString("categoryName");
//                 if (categoryName != null && !categoryName.isEmpty()) {
//                     categories.add(categoryName);
//                     categoryCount++;
//                 }
//             }

//             LOGGER.info("✨✨✨ Đã tìm thấy " + categoryCount + " danh mục từ database slayyy");

//             if (categoryCount == 0) {
//                 LOGGER.warning("🚨🚨 SKSKSK EM hong tìm thấy danh mục nào trong database luôn á!!!");
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi SQL khi lấy danh mục: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "😭😭 Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }

//         return categories;
//     }

//     // Tải dữ liệu sản phẩm từ database
//     // Em sẽ sửa lại hàm loadProductsFromDatabase để FIX LỖI NGAY LAPPPPP
//     private void loadProductsFromDatabase() {
//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.severe("❌❌❌ Không thể kết nối đến database");
//                 return;
//             }

//             // FIX LỖI: Sửa lại câu query SQL - CHÚ Ý KHÔNG DÙNG WHERE NỮA
//             // Trước đây chỉ lấy sản phẩm có status = "Còn hàng" hoặc "Active"
//             // => Sửa lại để lấy TẤT CẢ sản phẩm, sort theo quantity để hiển thị sản phẩm còn hàng lên trên
//             String query = "SELECT * FROM Products ORDER BY quantity DESC";
//             stmt = conn.prepareStatement(query);
//             rs = stmt.executeQuery();

//             products.clear(); // Xóa danh sách cũ

//             int productCount = 0; // Đếm số sản phẩm load được

//             while (rs.next()) {
//                 Product product = new Product();
//                 product.setProductID(rs.getString("productID"));
//                 product.setProductName(rs.getString("productName"));
//                 product.setPrice(rs.getDouble("price"));
//                 product.setQuantity(rs.getInt("quantity"));
//                 product.setDescription(rs.getString("description"));
//                 product.setStatus(rs.getString("status"));
//                 product.setCategoryID(rs.getString("categoryID"));

//                 // Xử lý đường dẫn hình ảnh
//                 String imagePath = rs.getString("imagePath");
//                 if (imagePath != null && !imagePath.startsWith("/")) {
//                     imagePath = "/com/example/stores/images/" + imagePath;
//                 }
//                 product.setImagePath(imagePath);

//                 products.add(product);
//                 productCount++;
//             }

//             LOGGER.info("✅✅✅ Đã load được " + productCount + " sản phẩm từ database");

//             if (productCount == 0) {
//                 // Debug thêm thông tin nếu không load được sản phẩm nào
//                 LOGGER.warning("⚠️⚠️⚠️ Không tìm thấy sản phẩm nào trong database!!!");
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi SQL khi lấy dữ liệu sản phẩm: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }
//     }

//     // Làm mới danh sách sản phẩm trên giao diện
//     private void refreshProductList() {
//         if (productFlowPane == null) {
//             LOGGER.warning("productFlowPane chưa được khởi tạo");
//             return;
//         }

//         // Xóa tất cả sản phẩm hiện tại
//         productFlowPane.getChildren().clear();

//         if (products.isEmpty()) {
//             Label lblEmpty = new Label("Không có sản phẩm nào.");
//             lblEmpty.setStyle("-fx-font-style: italic;");
//             productFlowPane.getChildren().add(lblEmpty);
//             return;
//         }

//         // Lọc sản phẩm theo điều kiện
//         List<Product> filteredProducts = filterProducts();

//         // Sắp xếp sản phẩm theo điều kiện
//         sortProducts(filteredProducts);

//         // Lưu danh sách hiện tại để sử dụng sau này
//         currentFilteredProducts = new ArrayList<>(filteredProducts);

//         // Giới hạn số lượng sản phẩm hiển thị
//         List<Product> displayProducts = filteredProducts.stream()
//                 .limit(productLimit)
//                 .collect(Collectors.toList());

//         // Hiển thị sản phẩm
//         for (Product product : displayProducts) {
//             VBox productBox = createProductBox(product);
//             productFlowPane.getChildren().add(productBox);
//         }

//         // Thêm nút "Xem thêm" nếu còn sản phẩm
//         if (filteredProducts.size() > productLimit) {
//             Button btnLoadMore = createLoadMoreButton();
//             productFlowPane.getChildren().add(btnLoadMore);
//         }
//     }

//     // Lọc sản phẩm theo các điều kiện
//     private List<Product> filterProducts() {
//         List<Product> filteredList = new ArrayList<>(products);

//         // Lọc theo danh mục
//         if (cbCategory != null && cbCategory.getValue() != null && !cbCategory.getValue().equals("Tất cả")) {
//             String selectedCategory = cbCategory.getValue();
//             filteredList = filteredList.stream()
//                     .filter(p -> {
//                         String categoryName = getCategoryName(p.getCategoryID());
//                         return categoryName.equals(selectedCategory);
//                     })
//                     .collect(Collectors.toList());
//         }

//         // Lọc theo từ khóa tìm kiếm
//         if (txtSearch != null && txtSearch.getText() != null && !txtSearch.getText().trim().isEmpty()) {
//             String keyword = txtSearch.getText().trim().toLowerCase();
//             filteredList = filteredList.stream()
//                     .filter(p -> p.getProductName() != null && p.getProductName().toLowerCase().contains(keyword))
//                     .collect(Collectors.toList());
//         }

//         return filteredList;
//     }

//     // Sắp xếp sản phẩm theo điều kiện đã chọn
//     private void sortProducts(List<Product> list) {
//         if (cbSort == null || cbSort.getValue() == null) return;

//         String sortOption = cbSort.getValue();
//         switch (sortOption) {
//             case "Tên A-Z":
//                 // FIX LỖI: Thêm kiểu Product vào lambda để compiler biết đây là Product object
//                 list.sort(Comparator.comparing((Product p) -> p.getProductName() != null ? p.getProductName() : ""));
//                 break;
//             case "Tên Z-A":
//                 // FIX LỖI: Thêm kiểu Product vào lambda tương tự
//                 list.sort(Comparator.comparing((Product p) -> p.getProductName() != null ? p.getProductName() : "").reversed());
//                 break;
//             case "Giá thấp đến cao":
//                 list.sort(Comparator.comparing(Product::getPrice));
//                 break;
//             case "Giá cao đến thấp":
//                 list.sort(Comparator.comparing(Product::getPrice).reversed());
//                 break;
//             // Mặc định không sắp xếp (giữ nguyên thứ tự)
//         }
//     }

//     // Tạo box hiển thị sản phẩm
//     private VBox createProductBox(Product product) {
//         VBox box = new VBox(8); // Khoảng cách giữa các thành phần
//         box.setPrefWidth(160);
//         box.setPrefHeight(260);
//         box.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: white; -fx-padding: 10;");

//         // Tạo hiệu ứng hover
//         box.setOnMouseEntered(e -> {
//             box.setStyle("-fx-border-color: #2196F3; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f5f5f5; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.4), 10, 0, 0, 0);");
//             box.setCursor(Cursor.HAND);
//         });

//         box.setOnMouseExited(e -> {
//             box.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: white; -fx-padding: 10;");
//         });

//         // Xử lý sự kiện click để xem chi tiết sản phẩm
//         box.setOnMouseClicked(e -> showProductDetails(product));

//         // Hiển thị hình ảnh sản phẩm
//         ImageView imageView = new ImageView();
//         imageView.setFitWidth(140);
//         imageView.setFitHeight(105);
//         imageView.setPreserveRatio(true);

//         String imagePath = product.getImagePath();
//         if (imagePath == null) {
//             imagePath = "/com/example/stores/images/no_image.png";
//         }

//         try {
//             Image image = new Image(getClass().getResourceAsStream(imagePath));
//             imageView.setImage(image);
//         } catch (Exception e) {
//             try {
//                 Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/stores/images/no_image.png"));
//                 imageView.setImage(defaultImage);
//             } catch (Exception ex) {
//                 LOGGER.warning("Không tải được ảnh sản phẩm: " + ex.getMessage());
//             }
//         }

//         // Hiển thị tên sản phẩm
//         String productName = product.getProductName();
//         if (productName == null) productName = "Sản phẩm không tên";
//         if (productName.length() > 40) {
//             productName = productName.substring(0, 37) + "...";
//         }

//         Label nameLabel = new Label(productName);
//         nameLabel.setWrapText(true);
//         nameLabel.setPrefHeight(40); // Chiều cao cố định cho tên sản phẩm
//         nameLabel.setStyle("-fx-font-weight: bold;");

//         // Hiển thị giá
//         Label priceLabel = new Label("Giá: " + String.format("%,d", (long) product.getPrice()) + "đ");
//         priceLabel.setStyle("-fx-text-fill: #e91e63; -fx-font-weight: bold;");

//         // Hiển thị số lượng
//         Label stockLabel = new Label("Kho: " + product.getQuantity());

//         // Nút thêm vào giỏ
//         Button addButton = new Button("Thêm vào giỏ");
//         addButton.setPrefWidth(Double.MAX_VALUE);
//         addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

//         // Hiệu ứng hover cho nút
//         addButton.setOnMouseEntered(e ->
//                 addButton.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white; -fx-font-weight: bold;")
//         );

//         addButton.setOnMouseExited(e ->
//                 addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;")
//         );

//         // Sự kiện thêm vào giỏ
//         addButton.setOnAction(e -> {
//             try {
//                 // Kiểm tra số lượng tồn kho
//                 if (product.getQuantity() <= 0) {
//                     AlertUtil.showWarning("Hết hàng", "Sản phẩm đã hết hàng!");
//                     return;
//                 }

//                 // Tạo đối tượng CartItemEmployee
//                 CartItemEmployee item = new CartItemEmployee(
//                         product.getProductID(),
//                         product.getProductName(),
//                         product.getPrice(),
//                         1,
//                         product.getImagePath(),
//                         employeeId,
//                         currentUser != null ? currentUser : "unknown",
//                         product.getCategoryID()
//                 );

//                 // Kiểm tra sản phẩm có đủ điều kiện bảo hành thường không
//                 // Nếu có, thêm bảo hành thường mặc định
//                 if (WarrantyCalculator.isEligibleForStandardWarranty(product)) {
//                     Warranty warranty = WarrantyCalculator.createWarranty(product, "Thường");
//                     item.setWarranty(warranty);
//                 }

//                 // Thêm vào giỏ hàng
//                 addToCartWithWarranty(item);

//             } catch (Exception ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi khi thêm sản phẩm vào giỏ hàng", ex);
//                 AlertUtil.showError("Lỗi", "Không thể thêm sản phẩm vào giỏ hàng");
//             }
//         });

//         // Thêm tất cả vào box
//         VBox imageContainer = new VBox(imageView);
//         imageContainer.setAlignment(Pos.CENTER);

//         box.getChildren().addAll(
//                 imageContainer,
//                 nameLabel,
//                 priceLabel,
//                 stockLabel,
//                 addButton
//         );

//         return box;
//     }
// }package com.example.stores.controller;

// import com.example.stores.model.OrderHistory;
// import com.example.stores.model.OrderDetail;
// import com.example.stores.service.OrderHistoryServiceE;
// import javafx.scene.control.cell.PropertyValueFactory;
// import javafx.scene.control.CheckBox;

// import java.io.IOException;
// import java.io.InputStream;
// import java.net.URL;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.sql.Statement;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.List;
// import java.util.Optional;
// import java.util.logging.Level;
// import java.util.logging.Logger;
// import java.util.stream.Collectors;

// import com.example.stores.util.AlertUtil; // Chú ý: đây là AlertUtil (không có s)
// import com.example.stores.util.WarrantyCalculator;

// import com.example.stores.model.Customer;
// import com.example.stores.service.CustomerServiceE;

// import javafx.scene.control.RadioButton;
// import javafx.scene.control.ToggleGroup;
// import javafx.scene.layout.BorderPane;
// import javafx.scene.layout.GridPane;
// import javafx.scene.control.ScrollPane;
// import javafx.beans.property.SimpleStringProperty;
// import javafx.beans.property.SimpleIntegerProperty;
// import javafx.beans.property.SimpleDoubleProperty;

// import javafx.scene.layout.*;
// import javafx.geometry.Pos;
// import javafx.scene.control.Label;
// import javafx.scene.control.Button;
// import javafx.scene.image.Image;
// import javafx.scene.image.ImageView;
// import javafx.geometry.Insets;

// import javafx.collections.ObservableList;
// import com.example.stores.config.DBConfig;
// import com.example.stores.model.CartItemEmployee;
// import com.example.stores.model.Product;
// import com.example.stores.model.Employee;
// import com.example.stores.model.Warranty; // Thêm import cho Warranty

// import com.example.stores.model.Order;

// import javafx.scene.shape.Circle;
// import javafx.scene.Cursor;
// import javafx.collections.FXCollections;
// import javafx.fxml.FXML;
// import javafx.fxml.FXMLLoader;
// import javafx.print.PrinterJob;
// import javafx.scene.Parent;
// import javafx.scene.Scene;
// import javafx.scene.control.Alert;
// import javafx.scene.control.ButtonType;
// import javafx.scene.control.ComboBox;
// import javafx.scene.control.Separator;
// import javafx.scene.control.TableCell;
// import javafx.scene.control.TableColumn;
// import javafx.scene.control.TableView;
// import javafx.scene.control.TextArea;
// import javafx.scene.control.TextField;
// import javafx.scene.layout.VBox;
// import javafx.stage.Modality;
// import javafx.stage.Stage;

// public class PosOverviewControllerE {
//     private static final Logger LOGGER = Logger.getLogger(PosOverviewControllerE.class.getName());

//     @FXML private FlowPane productFlowPane;
//     @FXML private TableView<CartItemEmployee> cartTable;
//     @FXML private TableColumn<CartItemEmployee, String> colCartName;
//     @FXML private TableColumn<CartItemEmployee, Integer> colCartQty;
//     @FXML private TableColumn<CartItemEmployee, Double> colCartPrice;
//     @FXML private TableColumn<CartItemEmployee, Double> colCartTotal;
//     @FXML private TableColumn<CartItemEmployee, String> colCartWarranty; // Thêm khai báo biến cho cột bảo hành
//     @FXML private Label lblTotal;
//     // Cập nhật ComboBox lọc theo DB mới (bỏ RAM/CPU, giữ lại category)
//     @FXML private ComboBox<String> cbCategory;
//     @FXML private ComboBox<String> cbSort; // Thêm ComboBox sắp xếp
//     @FXML private TextField txtSearch;
//     @FXML private Button btnFilter, btnCheckout;
//     @FXML private VBox cartItemsContainer; // Container cho các item trong giỏ hàng

//     private int productLimit = 20; // Số sản phẩm hiển thị ban đầu
//     private List<Product> currentFilteredProducts = new ArrayList<>();

//     private ObservableList<Product> products = FXCollections.observableArrayList();
//     private ObservableList<CartItemEmployee> cartItems = FXCollections.observableArrayList();
//     private TableColumn<CartItemEmployee, Void> colCartAction; // Cột chứa nút xóa

//     private int employeeId;

//     /**
//      * Thêm sản phẩm vào giỏ hàng - Method công khai cho ProductDetailController gọi
//      * @param item Sản phẩm cần thêm vào giỏ
//      */
//     public void addToCart(CartItemEmployee item) {
//         // Gọi đến phương thức addToCartWithWarranty đã có sẵn
//         addToCartWithWarranty(item);
//         LOGGER.info("✅ Đã thêm sản phẩm " + item.getProductName() + " vào giỏ hàng từ ProductDetailController");
//     }

//     /**
//      * Lấy tên người dùng hiện tại
//      * @return tên đăng nhập người dùng hiện tại
//      */
//     public String getCurrentUser() {
//         return this.currentUser;
//     }

//     // Thêm biến để lưu lịch sử đơn hàng trong session
//     private List<OrderSummary> orderHistory = new ArrayList<>();

//     // Thêm vào class PosOverviewController
//     private void addEmployeeInfoButton() {
//         try {
//             if (currentEmployee == null || btnCheckout == null || btnCheckout.getParent() == null ||
//                     !(btnCheckout.getParent().getParent() instanceof BorderPane)) {
//                 LOGGER.warning("Không thể thêm nút thông tin nhân viên: currentEmployee hoặc btnCheckout null");
//                 return;
//             }

//             BorderPane mainLayout = (BorderPane) btnCheckout.getParent().getParent();
//             if (mainLayout.getTop() instanceof HBox) {
//                 HBox topBar = (HBox) mainLayout.getTop();

//                 Button btnEmployeeInfo = new Button("THÔNG TIN NV");
//                 btnEmployeeInfo.setStyle("-fx-background-color: #FF4081; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12;");

//                 btnEmployeeInfo.setOnMouseEntered(e -> btnEmployeeInfo.setStyle("-fx-background-color: #F50057; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 0);"));
//                 btnEmployeeInfo.setOnMouseExited(e -> btnEmployeeInfo.setStyle("-fx-background-color: #FF4081; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12;"));

//                 btnEmployeeInfo.setOnAction(e -> showEmployeeInfoDialog());

//                 HBox.setMargin(btnEmployeeInfo, new Insets(0, 10, 0, 10));
//                 int infoLabelIndex = topBar.getChildren().size() - 1;
//                 if (infoLabelIndex >= 0) {
//                     topBar.getChildren().add(infoLabelIndex, btnEmployeeInfo);
//                 } else {
//                     topBar.getChildren().add(btnEmployeeInfo);
//                 }

//                 LOGGER.info("✨ Đã thêm nút thông tin nhân viên!");
//             }
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi thêm nút thông tin nhân viên", e);
//         }
//     }

//     // Hàm hiển thị dialog thông tin nhân viên SIÊU XỊNNN
//     @FXML
//     private void showEmployeeInfoDialog() {
//         try {
//             if (currentEmployee == null) {
//                 AlertUtil.showWarning("Thông báo", "Không thể lấy thông tin nhân viên!");
//                 return;
//             }

//             // Tạo stage mới cho dialog
//             Stage infoStage = new Stage();
//             infoStage.initModality(Modality.APPLICATION_MODAL);
//             infoStage.setTitle("Thông Tin Nhân Viên");
//             infoStage.setResizable(false);

//             // Tạo layout chính
//             BorderPane layout = new BorderPane();

//             // Phần header đẹp ngời
//             HBox header = new HBox();
//             header.setAlignment(Pos.CENTER);
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: linear-gradient(to right, #FF4081, #F50057);");

//             Label headerTitle = new Label("THÔNG TIN NHÂN VIÊN");
//             headerTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
//             header.getChildren().add(headerTitle);

//             // Thêm header vào layout
//             layout.setTop(header);

//             // Phần nội dung
//             GridPane content = new GridPane();
//             content.setPadding(new Insets(20));
//             content.setVgap(15);
//             content.setHgap(10);
//             content.setAlignment(Pos.CENTER);

//             // Tạo ImageView cho ảnh đại diện (avatar)
//             ImageView avatarView = new ImageView();

//             // Tải ảnh từ resource đường dẫn đúng
//             try {
//                 // Lấy theo nhân viên đang đăng nhập
//                 String avatarPath = "/com/example/stores/images/employee/img.png"; // mặc định

//                 // Nếu là nv001, dùng ảnh an.png
//                 if (currentEmployee.getUsername() != null && currentEmployee.getUsername().equals("nv001")) {
//                     avatarPath = "/com/example/stores/images/employee/an.png";
//                 }

//                 // Hoặc nếu có imageUrl trong database
//                 if (currentEmployee.getImageUrl() != null && !currentEmployee.getImageUrl().isEmpty()) {
//                     String imageUrl = currentEmployee.getImageUrl();
//                     // Bỏ "resources/" ở đầu nếu có
//                     String resourcePath = imageUrl.startsWith("resources/") ? imageUrl.substring(10) : imageUrl;
//                     // Thay "com.example.stores/" thành "com/example/stores/"
//                     if (resourcePath.startsWith("com.example.stores/")) {
//                         resourcePath = resourcePath.replace("com.example.stores/", "com/example/stores/");
//                     }
//                     // Thêm dấu "/" ở đầu
//                     avatarPath = "/" + resourcePath;
//                 }

//                 // Load ảnh
//                 Image avatarImage = new Image(getClass().getResourceAsStream(avatarPath));
//                 avatarView.setImage(avatarImage);
//             } catch (Exception e) {
//                 // Nếu không có ảnh, hiển thị icon người dùng mặc định
//                 try {
//                     // Đường dẫn default chuẩn
//                     Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/stores/images/employee/img.png"));
//                     avatarView.setImage(defaultImage);
//                 } catch (Exception ex) {
//                     LOGGER.warning("Không thể tải ảnh mặc định cho nhân viên: " + ex.getMessage());
//                 }
//             }

//             // Thiết lập kích thước avatar
//             avatarView.setFitWidth(120);
//             avatarView.setFitHeight(120);
//             avatarView.setPreserveRatio(true);

//             // Bo tròn avatar bằng clip hình tròn
//             Circle clip = new Circle(60, 60, 60); // tâm (60,60), bán kính 60px
//             avatarView.setClip(clip);

//             // Tạo StackPane cho avatar, có viền và padding
//             StackPane avatarContainer = new StackPane(avatarView);
//             avatarContainer.setPadding(new Insets(3));
//             avatarContainer.setStyle("-fx-background-color: white; -fx-border-color: #FF4081; " +
//                     "-fx-border-width: 3; -fx-border-radius: 60; -fx-background-radius: 60;");
//             GridPane.setColumnSpan(avatarContainer, 2);
//             GridPane.setHalignment(avatarContainer, javafx.geometry.HPos.CENTER);

//             // Thêm avatar vào đầu tiên
//             content.add(avatarContainer, 0, 0, 2, 1);

//             // Thêm các thông tin nhân viên
//             addEmployeeInfoField(content, "Mã nhân viên:", currentEmployee.getEmployeeID(), 1);
//             addEmployeeInfoField(content, "Tên đăng nhập:", currentEmployee.getUsername(), 2);
//             addEmployeeInfoField(content, "Họ tên:", currentEmployee.getFullName(), 3);

//             // Thêm thông tin position nếu có
//             String position = "Nhân viên";
//             try {
//                 position = currentEmployee.getPosition();
//                 if (position == null || position.isEmpty()) position = "Nhân viên";
//             } catch (Exception e) {
//                 // Nếu không có thuộc tính position, dùng giá trị mặc định
//                 LOGGER.info("Không có thông tin chức vụ");
//             }
//             addEmployeeInfoField(content, "Chức vụ:", position, 4);

//             addEmployeeInfoField(content, "Email:", currentEmployee.getEmail(), 5);
//             addEmployeeInfoField(content, "Điện thoại:", currentEmployee.getPhone(), 6);
//             addEmployeeInfoField(content, "Thời gian đăng nhập:", currentDateTime, 7);

//             // Button đóng dialog
//             HBox buttonBar = new HBox();
//             buttonBar.setAlignment(Pos.CENTER);
//             buttonBar.setPadding(new Insets(0, 0, 20, 0));

//             Button closeButton = new Button("ĐÓNG");
//             closeButton.setPrefWidth(120);
//             closeButton.setPrefHeight(35);
//             closeButton.setStyle("-fx-background-color: #F50057; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");

//             closeButton.setOnMouseEntered(e ->
//                     closeButton.setStyle("-fx-background-color: #C51162; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;")
//             );

//             closeButton.setOnMouseExited(e ->
//                     closeButton.setStyle("-fx-background-color: #F50057; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;")
//             );

//             closeButton.setOnAction(e -> infoStage.close());

//             buttonBar.getChildren().add(closeButton);

//             // Thêm nội dung và button vào layout
//             VBox mainContainer = new VBox(15);
//             mainContainer.getChildren().addAll(content, buttonBar);
//             layout.setCenter(mainContainer);

//             // Tạo scene và hiển thị
//             Scene scene = new Scene(layout, 400, 520);
//             infoStage.setScene(scene);
//             infoStage.show();

//             LOGGER.info("✨ Đã hiển thị thông tin nhân viên: " + currentEmployee.getFullName());
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị thông tin nhân viên: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị thông tin nhân viên: " + e.getMessage());
//         }
//     }

//     // Hàm hỗ trợ thêm trường thông tin
//     private void addEmployeeInfoField(GridPane grid, String labelText, String value, int row) {
//         // Label tiêu đề
//         Label label = new Label(labelText);
//         label.setStyle("-fx-font-weight: bold; -fx-text-fill: #757575;");
//         grid.add(label, 0, row);

//         // Giá trị
//         Label valueLabel = new Label(value != null ? value : "N/A");
//         valueLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #212121;");
//         grid.add(valueLabel, 1, row);
//     }

//     // Biến để đếm số đơn hàng
//     private int orderCounter = 1;

//     private Button createLoadMoreButton() {
//         Button btnLoadMore = new Button("XEM THÊM SẢN PHẨM");
//         btnLoadMore.setPrefWidth(200);
//         btnLoadMore.setPrefHeight(40);
//         btnLoadMore.setStyle(
//                 "-fx-background-color: linear-gradient(to right, #2196F3, #03A9F4); " +
//                         "-fx-text-fill: white; " +
//                         "-fx-font-weight: bold; " +
//                         "-fx-font-size: 14px; " +
//                         "-fx-background-radius: 5; " +
//                         "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.4), 6, 0, 0, 2);"
//         );

//         // Hiệu ứng khi hover
//         btnLoadMore.setOnMouseEntered(e ->
//                 btnLoadMore.setStyle(
//                         "-fx-background-color: linear-gradient(to right, #1976D2, #2196F3); " +
//                                 "-fx-text-fill: white; " +
//                                 "-fx-font-weight: bold; " +
//                                 "-fx-font-size: 14px; " +
//                                 "-fx-background-radius: 5; " +
//                                 "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.6), 8, 0, 0, 3);"
//                 )
//         );

//         // Trở về style ban đầu khi hết hover
//         btnLoadMore.setOnMouseExited(e ->
//                 btnLoadMore.setStyle(
//                         "-fx-background-color: linear-gradient(to right, #2196F3, #03A9F4); " +
//                                 "-fx-text-fill: white; " +
//                                 "-fx-font-weight: bold; " +
//                                 "-fx-font-size: 14px; " +
//                                 "-fx-background-radius: 5; " +
//                                 "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.4), 6, 0, 0, 2);"
//                 )
//         );

//         // Sự kiện khi click
//         btnLoadMore.setOnAction(e -> {
//             productLimit += 20; // Tăng thêm 20 sản phẩm
//             refreshProductList(); // Làm mới danh sách
//             LOGGER.info("Đã tăng giới hạn hiển thị lên " + productLimit + " sản phẩm");
//         });

//         return btnLoadMore;
//     }

//     /**
//      * Lưu đơn hàng vào lịch sử
//      */
//     public void addToOrderHistory(int orderId, String customerName, String customerPhone,
//                                   String paymentMethod, String orderDateTime, double totalAmount,
//                                   List<CartItemEmployee> items) {
//         Connection conn = null;
//         PreparedStatement pstmtOrder = null;
//         PreparedStatement pstmtDetail = null;

//         try {
//             if (items == null || items.isEmpty()) {
//                 LOGGER.warning("Danh sách sản phẩm rỗng, không thể lưu lịch sử vào DB");
//                 return;
//             }

//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.severe("Không thể kết nối database để lưu order history");
//                 return;
//             }
//             conn.setAutoCommit(false); // Bắt đầu transaction

//             // 1. Insert vào bảng Orders
//             String insertOrder = "INSERT INTO Orders (orderID, orderDate, totalAmount, customerID, employeeID, orderStatus, paymentMethod, recipientName, recipientPhone) "
//                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//             pstmtOrder = conn.prepareStatement(insertOrder);

//             String orderIdStr = String.format("ORD%03d", orderId); // Format lại cho khớp orderID

//             int customerId = getWalkInCustomerId(); // Hoặc lấy đúng customerID nếu phân biệt khách

//             pstmtOrder.setString(1, orderIdStr);
//             pstmtOrder.setString(2, orderDateTime);
//             pstmtOrder.setDouble(3, totalAmount);
//             pstmtOrder.setInt(4, customerId);
//             pstmtOrder.setInt(5, employeeId);
//             pstmtOrder.setString(6, "Đã xác nhận");
//             pstmtOrder.setString(7, paymentMethod);
//             pstmtOrder.setString(8, customerName);
//             pstmtOrder.setString(9, customerPhone);

//             int resultOrder = pstmtOrder.executeUpdate();
//             if (resultOrder == 0) throw new SQLException("Insert Orders thất bại!");

//             // 2. Insert từng sản phẩm vào OrderDetails
//             String insertDetail = "INSERT INTO OrderDetails (orderID, productID, quantity, unitPrice, warrantyType, warrantyPrice) "
//                     + "VALUES (?, ?, ?, ?, ?, ?)";
//             pstmtDetail = conn.prepareStatement(insertDetail);

//             for (CartItemEmployee item : items) {
//                 pstmtDetail.setString(1, orderIdStr);
//                 pstmtDetail.setString(2, item.getProductID());
//                 pstmtDetail.setInt(3, item.getQuantity());
//                 pstmtDetail.setDouble(4, item.getPrice());

//                 // Bảo hành
//                 if (item.hasWarranty()) {
//                     pstmtDetail.setString(5, item.getWarranty().getWarrantyType());
//                     pstmtDetail.setDouble(6, item.getWarranty().getWarrantyPrice());
//                 } else {
//                     pstmtDetail.setString(5, "Thường");
//                     pstmtDetail.setDouble(6, 0.0);
//                 }
//                 pstmtDetail.addBatch();
//             }
//             int[] detailResults = pstmtDetail.executeBatch();

//             conn.commit();
//             LOGGER.info("✅ Đã lưu đơn hàng #" + orderIdStr + " vào database với " + detailResults.length + " sản phẩm");

//         } catch (Exception e) {
//             try {
//                 if (conn != null) conn.rollback();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi rollback khi lưu đơn hàng!", ex);
//             }
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi lưu đơn hàng vào DB: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (pstmtOrder != null) pstmtOrder.close();
//                 if (pstmtDetail != null) pstmtDetail.close();
//                 if (conn != null) conn.setAutoCommit(true);
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.WARNING, "Lỗi khi đóng kết nối DB", ex);
//             }
//         }
//     }
//     /**
//      * Lấy ID của khách hàng "Khách lẻ" (walkin)
//      */
//     private int getWalkInCustomerId() {
//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;
//         int customerId = 1; // Mặc định ID=1 cho khách lẻ

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.warning("Không thể kết nối đến database");
//                 return customerId;
//             }

//             String sql = "SELECT customerID FROM Customer WHERE username = 'walkin'";
//             stmt = conn.prepareStatement(sql);
//             rs = stmt.executeQuery();

//             if (rs.next()) {
//                 customerId = rs.getInt("customerID");
//                 return customerId;
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.WARNING, "Lỗi SQL khi lấy ID khách hàng mặc định: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.WARNING, "Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.WARNING, "Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }

//         return customerId;
//     }
//     // Thông tin user và thời gian
//     private String currentUser = "doanpk";
//     private String currentDateTime = "2025-06-22 10:30:23"; // Cập nhật thời gian hiện tại từ input
//     private Employee currentEmployee; // Biến lưu thông tin nhân viên

//     // Class để lưu thông tin đơn hàng tạm thời
//     private static class OrderSummary {
//         private int id;
//         private String customerName;
//         private String customerPhone;
//         private String paymentMethod;
//         private String orderDate;
//         private double totalAmount;
//         private List<CartItemEmployee> items;

//         public OrderSummary(int id, String customerName, String customerPhone, String paymentMethod,
//                             String orderDate, double totalAmount, List<CartItemEmployee> items) {
//             this.id = id;
//             this.customerName = customerName;
//             this.customerPhone = customerPhone;
//             this.paymentMethod = paymentMethod;
//             this.orderDate = orderDate;
//             this.totalAmount = totalAmount;
//             this.items = new ArrayList<>(items);
//         }

//         // Getters
//         public int getId() { return id; }
//         public String getCustomerName() { return customerName; }
//         public String getCustomerPhone() { return customerPhone; }
//         public String getPaymentMethod() { return paymentMethod; }
//         public String getOrderDate() { return orderDate; }
//         public double getTotalAmount() { return totalAmount; }
//         public List<CartItemEmployee> getItems() { return items; }
//     }

//     @FXML
//     private void initialize() {
//         try {
//             LOGGER.info("Đang khởi tạo POS Overview Controller...");
//             LOGGER.info("Người dùng hiện tại: " + currentUser);
//             LOGGER.info("Thời gian hiện tại: " + currentDateTime);

//             // Set style trực tiếp để đảm bảo nút có màu
//             if (btnCheckout != null) {
//                 btnCheckout.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             }

//             productFlowPane.setPrefWidth(900);
//             productFlowPane.setPrefWrapLength(900);  // DÒNG QUAN TRỌNG NHẤT!!!
//             productFlowPane.setHgap(15);
//             productFlowPane.setVgap(20);

//             // Lấy dữ liệu sản phẩm từ database
//             loadProductsFromDatabase();
//             LOGGER.info("Đã load " + products.size() + " sản phẩm từ database");

//             // Cấu hình TableView giỏ hàng
//             setupCartTable();

//             // Thêm nút xóa vào bảng giỏ hàng
//             addButtonsToTable();

//             cartTable.setItems(cartItems);

//             // Khởi tạo ComboBox filter danh mục
//             List<String> categoryList = getDistinctCategories();
//             if (cbCategory != null) cbCategory.setItems(FXCollections.observableArrayList(categoryList));

//             // Đảm bảo luôn chọn giá trị đầu tiên nếu có
//             if (cbCategory != null && !cbCategory.getItems().isEmpty()) cbCategory.getSelectionModel().select(0);

//             // Khởi tạo ComboBox sắp xếp
//             if (cbSort != null) {
//                 cbSort.getItems().addAll(
//                         "Mặc định",
//                         "Tên A-Z",
//                         "Tên Z-A",
//                         "Giá thấp đến cao",
//                         "Giá cao đến thấp"
//                 );
//                 cbSort.getSelectionModel().select(0);

//                 // Thêm listener cho cbSort
//                 cbSort.setOnAction(e -> refreshProductList());
//             }

//             // Sự kiện lọc, tìm kiếm
//             if (btnFilter != null) {
//                 btnFilter.setOnAction(e -> refreshProductList());
//             }

//             if (txtSearch != null) {
//                 txtSearch.textProperty().addListener((obs, oldVal, newVal) -> refreshProductList());
//             }

//             if (cbCategory != null) {
//                 cbCategory.setOnAction(e -> refreshProductList());
//             }

//             // Thanh toán - gọi handleCheckout để lưu dữ liệu vào DB
//             if (btnCheckout != null) {
//                 btnCheckout.setOnAction(e -> handleCheckout());
//             }

//             // Thêm nút lịch sử
//             addHistoryButton();

//             // Thêm nút đăng xuất
//             addLogoutButton();

//             // THÊM NÚT XEM THÔNG TIN NHÂN VIÊN
//             addEmployeeInfoButton();

//             // Render sản phẩm ban đầu
//             refreshProductList();
//             LOGGER.info("Khởi tạo POS Overview Controller thành công");
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi khởi tạo POS Overview Controller", e);
//         }
//     }

//     /**
//      * Xử lý thanh toán và lưu đơn hàng vào DB
//      */
//     @FXML
//     private void handleCheckout() {
//         try {
//             if (cartItems.isEmpty()) {
//                 AlertUtil.showWarning("Giỏ hàng trống", "Vui lòng thêm sản phẩm vào giỏ hàng trước khi thanh toán!");
//                 return;
//             }

//             // Tạo stage mới cho popup thanh toán
//             Stage confirmStage = new Stage();
//             confirmStage.initModality(Modality.APPLICATION_MODAL);
//             confirmStage.setTitle("Xác nhận thanh toán");
//             confirmStage.setResizable(false);

//             // BorderPane chính
//             BorderPane mainLayout = new BorderPane();

//             // HEADER ĐẸP NGỜI
//             HBox header = new HBox();
//             header.setAlignment(Pos.CENTER);
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: linear-gradient(to right, #4e73df, #224abe);");

//             Label headerLabel = new Label("XÁC NHẬN THANH TOÁN");
//             headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
//             header.getChildren().add(headerLabel);

//             mainLayout.setTop(header);

//             // PHẦN NỘI DUNG CHÍNH
//             VBox content = new VBox(15);
//             content.setPadding(new Insets(20));

//             // Tổng thanh toán hiển thị nổi bật
//             double totalAmount = calculateTotalAmount();
//             Label totalLabel = new Label("Tổng thanh toán: " + String.format("%,.0f", totalAmount) + "đ");
//             totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e91e63;");

//             // BẢNG DANH SÁCH SẢN PHẨM - THÊM VÀO FORM THANH TOÁN
//             Label productsLabel = new Label("Danh sách sản phẩm:");
//             productsLabel.setStyle("-fx-font-weight: bold;");

//             // TableView hiển thị sản phẩm trong giỏ
//             TableView<CartItemEmployee> productsTable = new TableView<>();
//             productsTable.setPrefHeight(150);

//             // Cột tên sản phẩm
//             TableColumn<CartItemEmployee, String> nameColumn = new TableColumn<>("Tên sản phẩm");
//             nameColumn.setCellValueFactory(data ->
//                     new SimpleStringProperty(data.getValue().getProductName()));
//             nameColumn.setPrefWidth(200);

//             // Cột số lượng
//             TableColumn<CartItemEmployee, Integer> quantityColumn = new TableColumn<>("SL");
//             quantityColumn.setCellValueFactory(data ->
//                     new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
//             quantityColumn.setPrefWidth(50);

//             // Cột đơn giá
//             TableColumn<CartItemEmployee, Double> priceColumn = new TableColumn<>("Đơn giá");
//             priceColumn.setCellValueFactory(data ->
//                     new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
//             priceColumn.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double price, boolean empty) {
//                     super.updateItem(price, empty);
//                     if (empty || price == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", price) + "đ");
//                     }
//                 }
//             });
//             priceColumn.setPrefWidth(100);

//             // Cột bảo hành
//             TableColumn<CartItemEmployee, String> warrantyColumn = new TableColumn<>("Bảo hành");
//             warrantyColumn.setCellValueFactory(data -> {
//                 CartItemEmployee item = data.getValue();
//                 if (item.hasWarranty()) {
//                     return new SimpleStringProperty(item.getWarranty().getWarrantyType());
//                 }
//                 return new SimpleStringProperty("Không");
//             });
//             warrantyColumn.setPrefWidth(80);

//             // Cột thành tiền
//             TableColumn<CartItemEmployee, Double> subtotalColumn = new TableColumn<>("T.Tiền");
//             subtotalColumn.setCellValueFactory(data ->
//                     new SimpleDoubleProperty(data.getValue().getTotalPrice()).asObject());
//             subtotalColumn.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double price, boolean empty) {
//                     super.updateItem(price, empty);
//                     if (empty || price == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", price) + "đ");
//                     }
//                 }
//             });
//             subtotalColumn.setPrefWidth(100);

//             productsTable.getColumns().addAll(nameColumn, quantityColumn, priceColumn, warrantyColumn, subtotalColumn);
//             productsTable.setItems(cartItems);

//             // Phần thông tin khách hàng
//             Label customerLabel = new Label("Thông tin khách hàng:");
//             customerLabel.setStyle("-fx-font-weight: bold;");

// // Form thông tin khách hàng
//             GridPane customerForm = new GridPane();
//             customerForm.setVgap(10);
//             customerForm.setHgap(10);

//             Label nameLabel = new Label("Tên khách hàng:");
//             TextField nameField = new TextField("Khách lẻ");
//             nameField.setPrefWidth(300);

//             Label phoneLabel = new Label("Số điện thoại:");
//             TextField phoneField = new TextField("0900000000");
//             phoneField.setPrefWidth(300);

// // ✅ THÊM TRƯỜNG GHI CHÚ
//             Label noteLabel = new Label("Ghi chú:");
//             TextArea noteField = new TextArea();
//             noteField.setPromptText("Nhập ghi chú cho đơn hàng (không bắt buộc)...");
//             noteField.setPrefWidth(300);
//             noteField.setPrefHeight(60);
//             noteField.setWrapText(true);

//             customerForm.add(nameLabel, 0, 0);
//             customerForm.add(nameField, 1, 0);
//             customerForm.add(phoneLabel, 0, 1);
//             customerForm.add(phoneField, 1, 1);
//             customerForm.add(noteLabel, 0, 2);  // ✅ THÊM VÀO DÒNG THỨ 3
//             customerForm.add(noteField, 1, 2);

//             // Phương thức thanh toán - CHỈ CÓ 2 PHƯƠNG THỨC
//             Label paymentLabel = new Label("Phương thức thanh toán:");
//             paymentLabel.setStyle("-fx-font-weight: bold;");

//             ToggleGroup paymentGroup = new ToggleGroup();

//             RadioButton cashRadio = new RadioButton("Tiền mặt");
//             cashRadio.setToggleGroup(paymentGroup);
//             cashRadio.setSelected(true); // Mặc định chọn tiền mặt

//             RadioButton transferRadio = new RadioButton("Chuyển khoản");
//             transferRadio.setToggleGroup(paymentGroup);

//             HBox paymentOptions = new HBox(20);
//             paymentOptions.getChildren().addAll(cashRadio, transferRadio);

//             // Thêm các thành phần vào content
//             content.getChildren().addAll(
//                     totalLabel,
//                     new Separator(),
//                     productsLabel,
//                     productsTable,
//                     new Separator(),
//                     customerLabel,
//                     customerForm,
//                     new Separator(),
//                     paymentLabel,
//                     paymentOptions
//             );

//             mainLayout.setCenter(new ScrollPane(content));

//             // PHẦN FOOTER VỚI CÁC NÚT CHỨC NĂNG
//             HBox footer = new HBox(10);
//             footer.setAlignment(Pos.CENTER_RIGHT);
//             footer.setPadding(new Insets(15, 20, 15, 20));
//             footer.setStyle("-fx-background-color: #f8f9fc; -fx-border-color: #e3e6f0; -fx-border-width: 1 0 0 0;");

//             Button cancelButton = new Button("Hủy");
//             cancelButton.setPrefWidth(100);
//             cancelButton.setStyle("-fx-background-color: #e74a3b; -fx-text-fill: white;");

//             Button confirmButton = new Button("Xác nhận thanh toán");
//             confirmButton.setPrefWidth(200);
//             confirmButton.setStyle("-fx-background-color: #4e73df; -fx-text-fill: white; -fx-font-weight: bold;");

//             footer.getChildren().addAll(cancelButton, confirmButton);
//             mainLayout.setBottom(footer);

//             // Xử lý sự kiện cho nút Hủy
//             cancelButton.setOnAction(e -> confirmStage.close());

//             // Xử lý sự kiện cho nút Xác nhận thanh toán
//             confirmButton.setOnAction(e -> {
//                 try {
//                     // Lấy thông tin khách hàng và phương thức thanh toán
//                     String customerName = nameField.getText().trim();
//                     String customerPhone = phoneField.getText().trim();
//                     String paymentMethod = cashRadio.isSelected() ? "Tiền mặt" : "Chuyển khoản";

//                     // Validate số điện thoại
//                     if (!customerPhone.isEmpty() && customerPhone.length() < 10) {
//                         AlertUtil.showWarning("Lỗi", "Số điện thoại không hợp lệ!");
//                         return;
//                     }

//                     // NẾU CHỌN CHUYỂN KHOẢN - MỞ CỬA SỔ QR CODE
//                     if (transferRadio.isSelected()) {
//                         // Đóng cửa sổ xác nhận
//                         confirmStage.close();

//                         // Mở cửa sổ QR Payment
//                         showQRPaymentWindow(customerName, customerPhone, totalAmount, cartItems);
//                         return;
//                     }

//                     // NẾU THANH TOÁN TIỀN MẶT - XỬ LÝ LUÔN
//                     // Lưu đơn hàng vào DB và trả về orderID
//                     String orderId = saveOrderToDB(customerName, customerPhone, paymentMethod, totalAmount, cartItems);

//                     if (orderId != null) {
//                         // FIX LỖI: Chỉ lấy phần số từ orderId (bỏ phần chữ "ORD")
//                         String numericOrderId = orderId.replaceAll("[^0-9]", "");
//                         int orderIdInt = Integer.parseInt(numericOrderId);

//                         // Lưu vào bộ nhớ (để tương thích với code cũ) - DÙNG ID ĐÃ XỬ LÝ
//                         addToOrderHistory(orderIdInt, customerName, customerPhone,
//                                 paymentMethod, getCurrentDateTime(), totalAmount, cartItems);

//                         // Đóng cửa sổ thanh toán
//                         confirmStage.close();

//                         // Hiển thị thông báo thành công
//                         AlertUtil.showInfo("Thanh toán thành công",
//                                 "Đơn hàng #" + orderId + " đã được tạo thành công!");

//                         // In hóa đơn - DÙNG ID ĐÃ XỬ LÝ
//                         printReceiptWithPaymentMethod(
//                                 orderIdInt,
//                                 cartItems, totalAmount, customerName, customerPhone,
//                                 paymentMethod, getCurrentDateTime(), currentUser);

//                         // Xóa giỏ hàng
//                         clearCart();
//                     } else {
//                         // Thông báo lỗi
//                         AlertUtil.showError("Lỗi thanh toán",
//                                 "Không thể lưu đơn hàng. Vui lòng thử lại!");
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi thanh toán: " + ex.getMessage(), ex);
//                     AlertUtil.showError("Lỗi thanh toán", "Đã xảy ra lỗi: " + ex.getMessage());
//                     confirmStage.close();
//                 }
//             });

//             Scene scene = new Scene(mainLayout, 600, 700);
//             confirmStage.setScene(scene);
//             confirmStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi hiển thị form thanh toán: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể mở trang xác nhận thanh toán: " + e.getMessage());
//         }
//     }

//     /**
//      * Hiển thị cửa sổ thanh toán QR Code
//      */
//     private void showQRPaymentWindow(String customerName, String customerPhone, double totalAmount, ObservableList<CartItemEmployee> items) {
//         try {
//             LOGGER.info("💖 Bắt đầu mở cửa sổ QR Payment nè!");

//             // Tạo đối tượng Order giả
//             Order order = new Order();
//             order.setTotalAmount(totalAmount);

//             // DEBUG: In ra đường dẫn hiện tại
//             LOGGER.info("📂 Working Directory: " + System.getProperty("user.dir"));

//             FXMLLoader loader = null;

//             // THỬ TẤT CẢ CÁC ĐƯỜNG DẪN CÓ THỂ
//             String[] possiblePaths = {
//                     "/com/example/stores/view/qr_payment.fxml",
//                     "com/example/stores/view/qr_payment.fxml",
//                     "/view/qr_payment.fxml",
//                     "view/qr_payment.fxml",
//                     "/qr_payment.fxml",
//                     "qr_payment.fxml"
//             };

//             for (String path : possiblePaths) {
//                 try {
//                     LOGGER.info("🔍 Thử load FXML từ: " + path);
//                     URL fxmlUrl = getClass().getResource(path);

//                     if (fxmlUrl != null) {
//                         LOGGER.info("✅ Tìm thấy file FXML tại: " + fxmlUrl);
//                         loader = new FXMLLoader(fxmlUrl);
//                         break;
//                     } else {
//                         LOGGER.warning("❌ Không tìm thấy FXML tại: " + path);
//                     }
//                 } catch (Exception e) {
//                     LOGGER.warning("❌ Lỗi khi thử path: " + path + " - " + e.getMessage());
//                 }
//             }

//             // Nếu không tìm thấy file FXML nào
//             if (loader == null) {
//                 LOGGER.severe("😭 KHÔNG TÌM THẤY FILE FXML NÀO HẾT!!!");
//                 throw new Exception("Không tìm thấy file FXML cho QR Payment");
//             }

//             // Load FXML
//             Parent root = loader.load();
//             LOGGER.info("✅ Đã load FXML thành công!");

//             // Lấy controller và truyền dữ liệu
//             QRPaymentControllerE controller = loader.getController();
//             LOGGER.info("✅ Đã lấy controller thành công!");

//             // Tạo danh sách OrderDetail giả
//             List<OrderDetail> orderDetails = new ArrayList<>();
//             // Chuyển đổi từ CartItem sang OrderDetail
//             for (CartItemEmployee item : items) {
//                 OrderDetail detail = new OrderDetail();
//                 detail.setProductName(item.getProductName());
//                 detail.setQuantity(item.getQuantity());
//                 detail.setPrice(item.getPrice());
//                 orderDetails.add(detail);
//             }

//             // Set dữ liệu cho Controller
//             controller.setOrderDetails(order, orderDetails);
//             LOGGER.info("✅ Đã set order details!");

//             // Set callback khi thanh toán thành công
//             controller.setOnPaymentSuccess(() -> {
//                 try {
//                     // Tạo đơn hàng với phương thức thanh toán là chuyển khoản
//                     String orderId = saveOrderToDB(customerName, customerPhone, "Chuyển khoản", totalAmount, items);
//                     LOGGER.info("✅ Đã lưu đơn hàng với ID: " + orderId);

//                     if (orderId != null) {
//                         // FIX LỖI: Chỉ lấy phần số từ orderId
//                         String numericOrderId = orderId.replaceAll("[^0-9]", "");
//                         int orderIdInt = Integer.parseInt(numericOrderId);

//                         // Lưu vào bộ nhớ với ID đã xử lý
//                         addToOrderHistory(orderIdInt, customerName, customerPhone,
//                                 "Chuyển khoản", getCurrentDateTime(), totalAmount, items);

//                         // Hiển thị thông báo thành công
//                         AlertUtil.showInfo("Thanh toán thành công",
//                                 "Đơn hàng #" + orderId + " đã được thanh toán thành công!");

//                         // In hóa đơn với ID đã xử lý
//                         printReceiptWithPaymentMethod(
//                                 orderIdInt,
//                                 items, totalAmount, customerName, customerPhone,
//                                 "Chuyển khoản", getCurrentDateTime(), currentUser);

//                         // Xóa giỏ hàng
//                         clearCart();
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi hoàn tất thanh toán QR: " + ex.getMessage(), ex);
//                     AlertUtil.showError("Lỗi thanh toán", "Đã xảy ra lỗi: " + ex.getMessage());
//                 }
//             });

//             // Hiển thị cửa sổ QR
//             Stage qrStage = new Stage();
//             qrStage.initModality(Modality.APPLICATION_MODAL);
//             qrStage.setTitle("Thanh toán bằng mã QR");
//             qrStage.setResizable(false);

//             Scene scene = new Scene(root);
//             qrStage.setScene(scene);

//             LOGGER.info("💯 SẮP HIỆN CỬA SỔ QR PAYMENT RỒI!!!");
//             qrStage.show(); // Dùng show() thay vì showAndWait() để debug
//             LOGGER.info("🎉 ĐÃ HIỆN CỬA SỔ QR PAYMENT!!!");

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi hiển thị cửa sổ thanh toán QR: " + e.getMessage(), e);

//             // In lỗi chi tiết hơn
//             e.printStackTrace();

//             AlertUtil.showError("Lỗi", "Không thể mở cửa sổ thanh toán QR: " + e.getMessage() + "\nVui lòng thanh toán bằng tiền mặt!");

//             // Trong trường hợp lỗi, thử lại với phương thức thanh toán tiền mặt
//             try {
//                 String orderId = saveOrderToDB(customerName, customerPhone, "Tiền mặt", totalAmount, items);
//                 if (orderId != null) {
//                     // FIX LỖI: Chỉ lấy phần số từ orderId
//                     String numericOrderId = orderId.replaceAll("[^0-9]", "");
//                     int orderIdInt = Integer.parseInt(numericOrderId);

//                     addToOrderHistory(orderIdInt, customerName, customerPhone, "Tiền mặt", getCurrentDateTime(), totalAmount, items);

//                     AlertUtil.showInfo("Thanh toán thành công",
//                             "Đã chuyển sang thanh toán tiền mặt.\nĐơn hàng #" + orderId + " đã được tạo thành công!");

//                     printReceiptWithPaymentMethod(orderIdInt, items, totalAmount, customerName, customerPhone,
//                             "Tiền mặt", getCurrentDateTime(), currentUser);

//                     clearCart();
//                 }
//             } catch (Exception ex) {
//                 LOGGER.log(Level.SEVERE, "❌ Lỗi khi thử thanh toán tiền mặt: " + ex.getMessage(), ex);
//             }
//         }
//     }    /**
//      * Lưu đơn hàng vào DB
//      * @return Mã đơn hàng (orderID) nếu lưu thành công, null nếu thất bại
//      */
//     private String saveOrderToDB(String recipientName, String recipientPhone,
//                                  String paymentMethod, double totalAmount,
//                                  List<CartItemEmployee> cartItems) {
//         String orderId = null;
//         Connection conn = null;

//         try {
//             conn = DBConfig.getConnection();
//             conn.setAutoCommit(false);

//             // 1. Tạo đơn hàng mới trong bảng Orders
//             String insertOrderSQL = "INSERT INTO Orders (orderDate, totalAmount, customerID, " +
//                     "recipientPhone, recipientName, orderStatus, paymentMethod) " +
//                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

//             try (PreparedStatement pstmtOrder = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {

//                 pstmtOrder.setString(1, getCurrentDateTime());
//                 pstmtOrder.setDouble(2, totalAmount);

//                 // ==== SỬA ĐOẠN NÀY ĐỂ LƯU KHÁCH HÀNG MỚI ====
//                 CustomerServiceE customerServiceE = new CustomerServiceE();
//                 int customerId = customerServiceE.findCustomerIdByPhone(recipientPhone);
//                 if (customerId == -1) {
//                     Customer newCustomer = new Customer();
//                     newCustomer.setCustomerName(recipientName);
//                     newCustomer.setPhone(recipientPhone);
//                     newCustomer.setAddress(""); // Có thể lấy từ form nếu có
//                     newCustomer.setEmail("");   // Có thể lấy từ form nếu có
//                     customerId = customerServiceE.addCustomerToDB(newCustomer);
//                     if (customerId == -1) {
//                         LOGGER.warning("❌ Không thể tạo khách mới, fallback về ID=1");
//                         customerId = 1; // fallback nếu lỗi
//                     }
//                 }
//                 pstmtOrder.setInt(3, customerId);

//                 pstmtOrder.setString(4, recipientPhone != null ? recipientPhone : "");
//                 pstmtOrder.setString(5, recipientName != null ? recipientName : "Khách lẻ");
//                 pstmtOrder.setString(6, "Đã xác nhận");
//                 pstmtOrder.setString(7, paymentMethod != null ? paymentMethod : "Tiền mặt");

//                 int result = pstmtOrder.executeUpdate();

//                 if (result > 0) {
//                     // Lấy orderID vừa được tạo
//                     ResultSet generatedKeys = pstmtOrder.getGeneratedKeys();
//                     if (generatedKeys.next()) {
//                         orderId = generatedKeys.getString(1);
//                         LOGGER.info("✅ Đã tạo đơn hàng mới với ID: " + orderId);

//                         // 2. Thêm chi tiết đơn hàng
//                         saveOrderDetails(conn, orderId, cartItems);

//                         // 3. Commit transaction
//                         conn.commit();
//                     }
//                 }

//             }

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi lưu đơn hàng vào DB: " + e.getMessage(), e);
//             // Rollback transaction nếu có lỗi
//             if (conn != null) {
//                 try {
//                     conn.rollback();
//                 } catch (SQLException ex) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi rollback transaction: " + ex.getMessage(), ex);
//                 }
//             }

//         } finally {
//             // Đảm bảo đóng connection và reset autoCommit
//             if (conn != null) {
//                 try {
//                     conn.setAutoCommit(true);
//                     conn.close();
//                 } catch (SQLException e) {
//                     LOGGER.log(Level.SEVERE, "❌ Lỗi khi đóng connection: " + e.getMessage(), e);
//                 }
//             }
//         }

//         return orderId;
//     }
//     /**
//      * Lưu chi tiết đơn hàng vào DB
//      */
//     private void saveOrderDetails(Connection conn, String orderId, List<CartItemEmployee> cartItems) throws SQLException {
//         String insertDetailSQL = "INSERT INTO OrderDetails (orderID, productID, quantity, unitPrice, warrantyType, warrantyPrice) " +
//                 "VALUES (?, ?, ?, ?, ?, ?)";

//         try (PreparedStatement pstmt = conn.prepareStatement(insertDetailSQL)) {
//             for (CartItemEmployee item : cartItems) {
//                 pstmt.setString(1, orderId);
//                 pstmt.setString(2, item.getProductID());
//                 pstmt.setInt(3, item.getQuantity());
//                 pstmt.setDouble(4, item.getPrice());

//                 // Xử lý thông tin bảo hành
//                 if (item.hasWarranty()) {
//                     pstmt.setString(5, item.getWarranty().getWarrantyType());
//                     pstmt.setDouble(6, item.getWarranty().getWarrantyPrice());
//                 } else {
//                     pstmt.setString(5, "Thường"); // Mặc định
//                     pstmt.setDouble(6, 0.0);
//                 }

//                 pstmt.addBatch();
//             }

//             int[] results = pstmt.executeBatch();
//             LOGGER.info("✅ Đã thêm " + results.length + " chi tiết đơn hàng");

//             // Cập nhật số lượng sản phẩm trong kho
//             updateProductQuantities(conn, cartItems);
//         }
//     }

//     /**
//      * Cập nhật số lượng sản phẩm trong kho sau khi thanh toán
//      */
//     private void updateProductQuantities(Connection conn, List<CartItemEmployee> cartItems) throws SQLException {
//         String updateSQL = "UPDATE Products SET quantity = quantity - ? WHERE productID = ?";

//         try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
//             for (CartItemEmployee item : cartItems) {
//                 pstmt.setInt(1, item.getQuantity());
//                 pstmt.setString(2, item.getProductID());
//                 pstmt.addBatch();
//             }

//             int[] results = pstmt.executeBatch();
//             LOGGER.info("✅ Đã cập nhật số lượng cho " + results.length + " sản phẩm");
//         }
//     }

//     /**
//      * Lấy thời gian hiện tại theo định dạng phù hợp với DB
//      */
//     private String getCurrentDateTime() {
//         LocalDateTime now = LocalDateTime.now();
//         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//         return now.format(formatter);
//     }

//     // Phương thức để nhận thông tin nhân viên từ màn hình login
//     public void initEmployeeData(Employee employee, String loginDateTime) {
//         try {
//             if (employee != null) {
//                 this.currentEmployee = employee;
//                 this.currentDateTime = loginDateTime;
//                 this.currentUser = employee.getUsername();

//                 // Dùng getFullName() - đảm bảo không gọi getName() vì có thể không có method này
//                 LOGGER.info("Đã khởi tạo POS với nhân viên: " + employee.getFullName());
//                 LOGGER.info("Thời gian hiện tại: " + currentDateTime);

//                 // Hiển thị thông tin nhân viên trên giao diện
//                 displayEmployeeInfo();
//             } else {
//                 LOGGER.warning("Lỗi: Employee object truyền vào là null");
//             }
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi khởi tạo dữ liệu nhân viên", e);
//         }
//     }

//     // Phương thức để nhận thông tin nhân viên từ màn hình login
//     public void setEmployeeInfo(int employeeID, String username) {
//         this.employeeId = employeeID; // Lưu employeeID vào biến instance
//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             // ĐÃ SỬA: Bọc trong try-catch để xử lý Exception từ getConnection()
//             try {
//                 conn = DBConfig.getConnection();
//             } catch (Exception ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi kết nối DB", ex);
//                 throw new SQLException("Không thể kết nối đến cơ sở dữ liệu: " + ex.getMessage());
//             }

//             if (conn == null) {
//                 throw new SQLException("Không thể kết nối đến cơ sở dữ liệu");
//             }

//             String query = "SELECT * FROM Employee WHERE employeeID = ? AND username = ?";
//             stmt = conn.prepareStatement(query);
//             stmt.setInt(1, employeeID);
//             stmt.setString(2, username);

//             rs = stmt.executeQuery();
//             if (rs.next()) {
//                 // Tạo đối tượng Employee từ ResultSet
//                 Employee emp = new Employee();
//                 emp.setEmployeeID(String.valueOf(employeeID));  // Chuyển int thành String
//                 emp.setUsername(rs.getString("username"));
//                 emp.setFullName(rs.getString("fullName"));
//                 emp.setEmail(rs.getString("email"));
//                 emp.setPhone(rs.getString("phone"));

//                 // Kiểm tra trước khi gọi setPosition
//                 try {
//                     int columnIndex = rs.findColumn("position");
//                     if (columnIndex > 0) {
//                         emp.setPosition(rs.getString("position"));
//                     }
//                 } catch (SQLException ex) {
//                     // Nếu không có cột position, bỏ qua
//                     LOGGER.info("Cột position không tồn tại trong bảng Employee");
//                 }

//                 // Gọi initEmployeeData với đối tượng Employee đã tạo
//                 String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//                 initEmployeeData(emp, currentTime);
//             } else {
//                 LOGGER.warning("Không tìm thấy nhân viên với ID=" + employeeID + " và username=" + username);
//                 Alert alert = new Alert(Alert.AlertType.WARNING);
//                 alert.setTitle("Cảnh báo");
//                 alert.setHeaderText("Không tìm thấy thông tin nhân viên");
//                 alert.setContentText("Vui lòng đăng nhập lại để tiếp tục.");
//                 alert.showAndWait();
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "Lỗi SQL khi lấy thông tin nhân viên", e);
//             Alert alert = new Alert(Alert.AlertType.ERROR);
//             alert.setTitle("Lỗi");
//             alert.setHeaderText("Không thể lấy thông tin nhân viên");
//             alert.setContentText("Chi tiết lỗi: " + e.getMessage());
//             alert.showAndWait();
//         } finally {
//             // Đóng tất cả các tài nguyên theo thứ tự ngược lại
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 // Không đóng connection ở đây vì có thể được sử dụng ở nơi khác
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi khi đóng tài nguyên SQL", ex);
//             }
//         }
//     }

//     // Hiển thị thông tin nhân viên trên giao diện - ĐÃ SỬA (FIX BUG 243)
//     private void displayEmployeeInfo() {
//         try {
//             if (currentEmployee != null && btnCheckout != null && btnCheckout.getParent() != null
//                     && btnCheckout.getParent().getParent() instanceof BorderPane) {

//                 BorderPane mainLayout = (BorderPane) btnCheckout.getParent().getParent();

//                 if (mainLayout.getTop() instanceof HBox) {
//                     HBox topBar = (HBox) mainLayout.getTop();

//                     // Tạo label hiển thị thông tin nhân viên
//                     Label lblEmployeeInfo = new Label(currentEmployee.getFullName() + " (" + currentUser + ")");
//                     lblEmployeeInfo.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

//                     // Tạo spacer để đẩy thông tin ra góc phải
//                     Region spacer = new Region();
//                     HBox.setHgrow(spacer, Priority.ALWAYS);

//                     // Thêm vào top bar
//                     topBar.getChildren().addAll(spacer, lblEmployeeInfo);
//                 }
//             }
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị thông tin nhân viên", e);
//         }
//     }

//     // Thêm nút đăng xuất
//     private void addLogoutButton() {
//         if (btnCheckout == null) {
//             LOGGER.warning("Lỗi: btnCheckout chưa được khởi tạo");
//             return;
//         }

//         Button btnLogout = new Button("ĐĂNG XUẤT");
//         btnLogout.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//         btnLogout.setPrefWidth(120);
//         btnLogout.setPrefHeight(35);
//         btnLogout.setOnAction(e -> logout());

//         if (btnCheckout.getParent() instanceof HBox) {
//             HBox parent = (HBox) btnCheckout.getParent();
//             parent.getChildren().add(0, btnLogout);
//         } else if (btnCheckout.getParent() instanceof Pane) {
//             Pane parent = (Pane) btnCheckout.getParent();
//             btnLogout.setLayoutX(btnCheckout.getLayoutX() - 130);
//             btnLogout.setLayoutY(btnCheckout.getLayoutY());
//             parent.getChildren().add(btnLogout);
//         }
//     }

//     // Xử lý đăng xuất
//     private void logout() {
//         try {
//             // Hiển thị xác nhận
//             Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
//             confirm.setTitle("Xác nhận đăng xuất");
//             confirm.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
//             confirm.setContentText("Mọi thay đổi chưa lưu sẽ bị mất.");

//             Optional<ButtonType> result = confirm.showAndWait();
//             if (result.isPresent() && result.get() == ButtonType.OK) {
//                 // Load màn hình đăng nhập
//                 URL loginUrl = getClass().getResource("/com/example/stores/view/employee_login.fxml");

//                 if (loginUrl != null) {
//                     FXMLLoader loader = new FXMLLoader(loginUrl);
//                     Parent root = loader.load();

//                     Scene scene = null;
//                     Stage stage = null;

//                     if (btnCheckout != null) {
//                         stage = (Stage) btnCheckout.getScene().getWindow();
//                         scene = new Scene(root);
//                         stage.setTitle("Computer Store - Đăng Nhập");
//                         stage.setScene(scene);
//                         stage.setResizable(false);
//                         stage.show();
//                     } else {
//                         LOGGER.warning("Lỗi: btnCheckout là null hoặc không thuộc Scene");
//                         stage = new Stage();
//                         scene = new Scene(root);
//                         stage.setTitle("Computer Store - Đăng Nhập");
//                         stage.setScene(scene);
//                         stage.setResizable(false);
//                         stage.show();

//                         // Đóng cửa sổ hiện tại nếu có
//                         if (productFlowPane != null && productFlowPane.getScene() != null) {
//                             Stage currentStage = (Stage) productFlowPane.getScene().getWindow();
//                             currentStage.close();
//                         }
//                     }

//                     LOGGER.info("Đã đăng xuất, thời gian: " +
//                             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//                 } else {
//                     throw new IOException("Không tìm thấy file employee_login.fxml");
//                 }
//             }
//         } catch (IOException e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi đăng xuất", e);
//             Alert alert = new Alert(Alert.AlertType.ERROR);
//             alert.setTitle("Lỗi");
//             alert.setContentText("Lỗi khi đăng xuất: " + e.getMessage());
//             alert.showAndWait();
//         }
//     }

//     // Thêm nút lịch sử đơn hàng
//     private void addHistoryButton() {
//         if (btnCheckout == null) {
//             LOGGER.warning("Lỗi: btnCheckout chưa được khởi tạo");
//             return;
//         }

//         Button btnHistory = new Button("LỊCH SỬ");
//         btnHistory.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
//         btnHistory.setPrefWidth(120);
//         btnHistory.setPrefHeight(35);
//         btnHistory.setOnAction(e -> showOrderHistoryInMemory()); // Sử dụng history trong bộ nhớ

//         if (btnCheckout.getParent() instanceof HBox) {
//             HBox parent = (HBox) btnCheckout.getParent();
//             parent.getChildren().add(0, btnHistory);
//         } else if (btnCheckout.getParent() instanceof Pane) {
//             Pane parent = (Pane) btnCheckout.getParent();
//             btnHistory.setLayoutX(btnCheckout.getLayoutX() - 130);
//             btnHistory.setLayoutY(btnCheckout.getLayoutY());
//             parent.getChildren().add(btnHistory);
//         }
//     }

//     // Cấu hình TableView giỏ hàng
//     // Đầu tiên em sửa hàm setupCartTable() để thêm cột bảo hành mới
//     private void setupCartTable() {
//         if (colCartName == null || colCartQty == null || colCartPrice == null || colCartTotal == null) {
//             LOGGER.warning("Lỗi: Các cột của TableView chưa được khởi tạo");
//             return;
//         }

//         // Thiết lập các cột cũ
//         colCartName.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleStringProperty("N/A");
//             }
//             String name = data.getValue().getProductName();
//             return new SimpleStringProperty(name != null ? name : "N/A");
//         });

//         colCartQty.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleIntegerProperty(0).asObject();
//             }
//             int qty = data.getValue().getQuantity();
//             return new SimpleIntegerProperty(qty).asObject();
//         });

//         colCartPrice.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleDoubleProperty(0).asObject();
//             }
//             double price = data.getValue().getPrice();
//             return new SimpleDoubleProperty(price).asObject();
//         });

//         colCartPrice.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//             @Override
//             protected void updateItem(Double price, boolean empty) {
//                 super.updateItem(price, empty);
//                 if (empty || price == null) {
//                     setText(null);
//                 } else {
//                     setText(String.format("%,.0f", price) + "đ");
//                 }
//             }
//         });

//         // THÊM CỘT BẢO HÀNH MỚI
//         colCartWarranty.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleStringProperty("Không");
//             }
//             CartItemEmployee item = data.getValue();
//             if (item.hasWarranty()) {
//                 return new SimpleStringProperty(item.getWarranty().getWarrantyType());
//             } else {
//                 return new SimpleStringProperty("Không");
//             }
//         });

//         // Nút sửa bảo hành
//         colCartWarranty.setCellFactory(tc -> new TableCell<CartItemEmployee, String>() {
//             @Override
//             protected void updateItem(String warrantyType, boolean empty) {
//                 super.updateItem(warrantyType, empty);
//                 if (empty) {
//                     setText(null);
//                     setGraphic(null);
//                 } else {
//                     HBox container = new HBox(5);
//                     container.setAlignment(Pos.CENTER_LEFT);

//                     // Hiển thị loại bảo hành
//                     Label lblType = new Label(warrantyType);

//                     // Nút sửa nhỏ bên cạnh
//                     Button btnEdit = new Button("⚙️");
//                     btnEdit.setStyle("-fx-background-color: transparent; -fx-padding: 0 2;");
//                     btnEdit.setOnAction(event -> {
//                         CartItemEmployee item = getTableView().getItems().get(getIndex());
//                         if (item != null) {
//                             showWarrantyEditDialog(item);
//                         }
//                     });

//                     container.getChildren().addAll(lblType, btnEdit);
//                     setGraphic(container);
//                     setText(null);
//                 }
//             }
//         });

//         colCartTotal.setCellValueFactory(data -> {
//             if (data == null || data.getValue() == null) {
//                 return new SimpleDoubleProperty(0).asObject();
//             }
//             double total = data.getValue().getTotalPrice();
//             return new SimpleDoubleProperty(total).asObject();
//         });

//         colCartTotal.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//             @Override
//             protected void updateItem(Double total, boolean empty) {
//                 super.updateItem(total, empty);
//                 if (empty || total == null) {
//                     setText(null);
//                 } else {
//                     setText(String.format("%,.0f", total) + "đ");
//                 }
//             }
//         });
//     }

//     // Sửa lại dialog chỉnh sửa bảo hành trong giỏ hàng
//     private void showWarrantyEditDialog(CartItemEmployee item) {
//         try {
//             // Tìm thông tin sản phẩm từ database để lấy giá
//             Product product = findProductById(item.getProductID());
//             if (product == null) {
//                 AlertUtil.showWarning("Lỗi", "Không tìm thấy thông tin sản phẩm");
//                 return;
//             }

//             Stage dialogStage = new Stage();
//             dialogStage.setTitle("Cập nhật bảo hành");
//             dialogStage.initModality(Modality.APPLICATION_MODAL);

//             VBox dialogContent = new VBox(15);
//             dialogContent.setPadding(new Insets(20));
//             dialogContent.setAlignment(Pos.CENTER);

//             // Tiêu đề và thông tin sản phẩm
//             Label lblTitle = new Label("Chọn gói bảo hành cho " + item.getProductName());
//             lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             // ComboBox chọn loại bảo hành - SỬA LẠI CÒN 2 LOẠI
//             ComboBox<String> cbWarranty = new ComboBox<>();

//             // Kiểm tra điều kiện bảo hành thường
//             boolean isEligibleForStdWarranty = WarrantyCalculator.isEligibleForStandardWarranty(product);

//             if (isEligibleForStdWarranty) {
//                 // Chỉ còn 2 lựa chọn
//                 cbWarranty.getItems().addAll("Không", "Thường", "Vàng");
//             } else {
//                 // Sản phẩm không đủ điều kiện bảo hành
//                 cbWarranty.getItems().add("Không");
//             }

//             // Set giá trị hiện tại
//             if (item.hasWarranty()) {
//                 String currentType = item.getWarranty().getWarrantyType();
//                 // Chuyển đổi các loại bảo hành cũ (nếu có)
//                 if (!currentType.equals("Thường") && !currentType.equals("Vàng")) {
//                     currentType = "Thường"; // Mặc định về Thường
//                 }

//                 if (cbWarranty.getItems().contains(currentType)) {
//                     cbWarranty.setValue(currentType);
//                 } else {
//                     cbWarranty.setValue("Không");
//                 }
//             } else {
//                 cbWarranty.setValue("Không");
//             }

//             // Hiển thị giá bảo hành
//             Label lblWarrantyPrice = new Label("Phí bảo hành: 0đ");
//             Label lblTotalWithWarranty = new Label("Tổng tiền: " + String.format("%,.0f", item.getTotalPrice()) + "đ");
//             lblTotalWithWarranty.setStyle("-fx-font-weight: bold;");

//             // Thêm mô tả bảo hành
//             Label lblWarrantyInfo = new Label("Không bảo hành");
//             lblWarrantyInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #757575;");

//             // Cập nhật giá khi thay đổi loại bảo hành
//             cbWarranty.setOnAction(e -> {
//                 String selectedType = cbWarranty.getValue();

//                 // TH1: Không bảo hành
//                 if (selectedType.equals("Không")) {
//                     lblWarrantyPrice.setText("Phí bảo hành: 0đ");
//                     double basePrice = product.getPrice() * item.getQuantity();
//                     lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,.0f", basePrice) + "đ");
//                     lblWarrantyInfo.setText("Không bảo hành cho sản phẩm này");
//                     lblWarrantyInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #757575;");
//                     return;
//                 }

//                 // TH2: Bảo hành thường
//                 if (selectedType.equals("Thường")) {
//                     lblWarrantyPrice.setText("Phí bảo hành: 0đ");
//                     double basePrice = product.getPrice() * item.getQuantity();
//                     lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,.0f", basePrice) + "đ");
//                     lblWarrantyInfo.setText("Bảo hành thường miễn phí 12 tháng");
//                     lblWarrantyInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #4CAF50;");
//                     return;
//                 }

//                 // TH3: Bảo hành vàng (10% giá gốc)
//                 double warrantyFee = product.getPrice() * 0.1 * item.getQuantity();
//                 lblWarrantyPrice.setText("Phí bảo hành: " + String.format("%,.0f", warrantyFee) + "đ");

//                 // Cập nhật tổng tiền
//                 double totalPrice = (product.getPrice() * item.getQuantity()) + warrantyFee;
//                 lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,.0f", totalPrice) + "đ");

//                 lblWarrantyInfo.setText("✨ Bảo hành Vàng 24 tháng, 1 đổi 1");
//                 lblWarrantyInfo.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF9800;");
//             });

//             // Nút lưu và hủy
//             Button btnSave = new Button("Lưu thay đổi");
//             btnSave.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnSave.setPrefWidth(140);
//             btnSave.setOnAction(e -> {
//                 String selectedType = cbWarranty.getValue();

//                 if ("Không".equals(selectedType)) {
//                     // Xóa bảo hành nếu chọn không bảo hành
//                     item.setWarranty(null);
//                 } else {
//                     // Tạo bảo hành mới với loại đã chọn
//                     Warranty warranty = WarrantyCalculator.createWarranty(product, selectedType);
//                     item.setWarranty(warranty);
//                 }

//                 // Cập nhật hiển thị
//                 updateCartDisplay();
//                 dialogStage.close();
//                 AlertUtil.showInformation("Thành công", "Đã cập nhật bảo hành cho sản phẩm");
//             });

//             Button btnCancel = new Button("Hủy");
//             btnCancel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnCancel.setPrefWidth(80);
//             btnCancel.setOnAction(e -> dialogStage.close());

//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.getChildren().addAll(btnSave, btnCancel);

//             // Thêm các thành phần vào dialog
//             dialogContent.getChildren().addAll(
//                     lblTitle,
//                     new Separator(),
//                     cbWarranty,
//                     lblWarrantyInfo,
//                     lblWarrantyPrice,
//                     lblTotalWithWarranty,
//                     buttonBox
//             );

//             // Hiện dialog
//             Scene scene = new Scene(dialogContent, 350, 320);
//             dialogStage.setScene(scene);
//             dialogStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị dialog chỉnh sửa bảo hành", e);
//             AlertUtil.showError("Lỗi", "Không thể mở cửa sổ chỉnh sửa bảo hành");
//         }
//     }

//     // Thêm nút xóa vào bảng giỏ hàng
//     private void addButtonsToTable() {
//         if (cartTable == null) {
//             LOGGER.warning("Lỗi: cartTable chưa được khởi tạo");
//             return;
//         }

//         colCartAction = new TableColumn<>("Xóa");
//         colCartAction.setCellFactory(param -> new TableCell<CartItemEmployee, Void>() {
//             private final Button btnDelete = new Button("X");

//             {
//                 btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//                 btnDelete.setOnAction(event -> {
//                     CartItemEmployee item = getTableRow().getItem();
//                     if (item != null) {
//                         // Hiện dialog xác nhận trước khi xóa
//                         Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
//                                 "Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?",
//                                 ButtonType.YES, ButtonType.NO);
//                         alert.setTitle("Xác nhận xóa");
//                         alert.setHeaderText("Xóa sản phẩm");

//                         Optional<ButtonType> result = alert.showAndWait();
//                         if (result.isPresent() && result.get() == ButtonType.YES) {
//                             cartItems.remove(item);
//                             updateTotal();
//                         }
//                     }
//                 });
//             }

//             @Override
//             protected void updateItem(Void item, boolean empty) {
//                 super.updateItem(item, empty);
//                 if (empty) {
//                     setGraphic(null);
//                 } else {
//                     setGraphic(btnDelete);
//                 }
//             }
//         });

//         colCartAction.setPrefWidth(50);

//         // Thêm cột vào TableView nếu chưa có
//         if (!cartTable.getColumns().contains(colCartAction)) {
//             cartTable.getColumns().add(colCartAction);
//         }
//     }

//     // Hiển thị thông báo lỗi
//     private void showErrorAlert(String message) {
//         Alert alert = new Alert(Alert.AlertType.WARNING, message);
//         alert.setTitle("Lỗi");
//         alert.setHeaderText("Thông tin không hợp lệ");
//         alert.showAndWait();
//     }


//     // Thêm method mới vào PosOverviewController
//     private void showOrderByIdWindow(String orderIdInput) {
//         try {
//             LOGGER.info("🔍 Tìm kiếm đơn hàng với ID: " + orderIdInput);

//             // Chuẩn hóa orderID (có thể người dùng nhập 1, 2, 3 hoặc ORD001, ORD002)
//             String searchOrderId = normalizeOrderId(orderIdInput);
//             LOGGER.info("📝 OrderID sau khi chuẩn hóa: " + searchOrderId);

//             // Tìm đơn hàng trong database
//             OrderHistoryServiceE.OrderWithDetails orderData = OrderHistoryServiceE.getCompleteOrderById(searchOrderId);

//             if (orderData == null || orderData.getOrderHistory() == null) {
//                 AlertUtil.showWarning("Không tìm thấy",
//                         "Không tìm thấy đơn hàng với mã: " + orderIdInput + "\nĐã thử tìm: " + searchOrderId);
//                 return;
//             }

//             OrderHistory order = orderData.getOrderHistory();
//             ObservableList<OrderDetail> details = orderData.getOrderDetails();

//             LOGGER.info("✅ Tìm thấy đơn hàng: " + order.getOrderID() + " với " + details.size() + " sản phẩm");

//             // Tạo cửa sổ hiển thị chi tiết
//             showSingleOrderDetailWindow(order, details);

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi khi tìm đơn hàng theo ID: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể tìm đơn hàng: " + e.getMessage());
//         }
//     }

//     // Helper method chuẩn hóa orderID
//     private String normalizeOrderId(String input) {
//         if (input == null || input.trim().isEmpty()) {
//             return input;
//         }

//         String trimmed = input.trim();

//         // Nếu đã có định dạng ORDxxx thì giữ nguyên
//         if (trimmed.toUpperCase().startsWith("ORD")) {
//             return trimmed;
//         }

//         // Nếu là số thuần túy, thử cả 2 cách
//         try {
//             int numericId = Integer.parseInt(trimmed);
//             // Thử format ORD001 trước
//             return String.format("ORD%03d", numericId);
//         } catch (NumberFormatException e) {
//             // Nếu không phải số, trả về nguyên input
//             return trimmed;
//         }
//     }
//     // Thêm method hiển thị chi tiết đơn hàng
//     private void showSingleOrderDetailWindow(OrderHistory order, ObservableList<OrderDetail> details) {
//         try {
//             Stage detailStage = new Stage();
//             detailStage.initModality(Modality.APPLICATION_MODAL);
//             detailStage.setTitle("Chi tiết đơn hàng #" + order.getOrderID());
//             detailStage.setResizable(true);

//             BorderPane mainLayout = new BorderPane();

//             // Header đẹp
//             HBox header = new HBox();
//             header.setAlignment(Pos.CENTER);
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: linear-gradient(to right, #4CAF50, #45a049);");

//             Label headerTitle = new Label("CHI TIẾT ĐƠN HÀNG #" + order.getOrderID());
//             headerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
//             header.getChildren().add(headerTitle);

//             // Content
//             VBox content = new VBox(15);
//             content.setPadding(new Insets(20));

//             // Thông tin đơn hàng
//             GridPane infoGrid = new GridPane();
//             infoGrid.setHgap(15);
//             infoGrid.setVgap(10);
//             infoGrid.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8;");

//             int row = 0;
//             addInfoRow(infoGrid, "Mã đơn hàng:", order.getOrderID(), row++);
//             addInfoRow(infoGrid, "Ngày đặt:", order.getFormattedDate(), row++);
//             addInfoRow(infoGrid, "Khách hàng:", order.getCustomerName(), row++);
//             addInfoRow(infoGrid, "Số điện thoại:", order.getCustomerPhone(), row++);
//             addInfoRow(infoGrid, "Nhân viên:", order.getEmployeeName(), row++);
//             addInfoRow(infoGrid, "Phương thức thanh toán:", order.getPaymentMethod(), row++);
//             addInfoRow(infoGrid, "Trạng thái:", order.getStatus(), row++);

//             // Bảng sản phẩm
//             Label productsLabel = new Label("DANH SÁCH SẢN PHẨM:");
//             productsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             TableView<OrderDetail> productsTable = new TableView<>();
//             productsTable.setPrefHeight(300);
//             productsTable.setItems(details);

//             // Các cột
//             TableColumn<OrderDetail, String> colProductName = new TableColumn<>("Tên sản phẩm");
//             colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
//             colProductName.setPrefWidth(250);

//             TableColumn<OrderDetail, Integer> colQuantity = new TableColumn<>("SL");
//             colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
//             colQuantity.setPrefWidth(50);

//             TableColumn<OrderDetail, String> colUnitPrice = new TableColumn<>("Đơn giá");
//             colUnitPrice.setCellValueFactory(data ->
//                     new SimpleStringProperty(String.format("%,.0f₫", data.getValue().getUnitPrice())));
//             colUnitPrice.setPrefWidth(100);

//             TableColumn<OrderDetail, String> colWarranty = new TableColumn<>("Bảo hành");
//             colWarranty.setCellValueFactory(new PropertyValueFactory<>("warrantyType"));
//             colWarranty.setPrefWidth(100);

//             TableColumn<OrderDetail, String> colSubtotal = new TableColumn<>("Thành tiền");
//             colSubtotal.setCellValueFactory(data ->
//                     new SimpleStringProperty(String.format("%,.0f₫", data.getValue().getSubtotal())));
//             colSubtotal.setPrefWidth(120);

//             productsTable.getColumns().addAll(colProductName, colQuantity, colUnitPrice, colWarranty, colSubtotal);

//             // Tổng tiền
//             Label totalLabel = new Label("TỔNG TIỀN: " + order.getFormattedAmount());
//             totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e91e63;");

//             // Buttons
//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.setPadding(new Insets(10, 0, 0, 0));

//             Button btnPrint = new Button("In hóa đơn");
//             btnPrint.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnPrint.setPrefWidth(120);
//             btnPrint.setOnAction(e -> {
//                 // Gọi method in hóa đơn (sử dụng lại code cũ)
//                 AlertUtil.showInfo("Thông báo", "Tính năng in hóa đơn đang được phát triển!");
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnClose.setPrefWidth(100);
//             btnClose.setOnAction(e -> detailStage.close());

//             buttonBox.getChildren().addAll(btnPrint, btnClose);

//             // Thêm vào content
//             content.getChildren().addAll(infoGrid, productsLabel, productsTable, totalLabel, buttonBox);

//             // Layout chính
//             mainLayout.setTop(header);
//             mainLayout.setCenter(new ScrollPane(content));

//             Scene scene = new Scene(mainLayout, 700, 600);
//             detailStage.setScene(scene);
//             detailStage.show();

//             LOGGER.info("✅ Đã hiển thị chi tiết đơn hàng: " + order.getOrderID());

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi hiển thị chi tiết đơn hàng: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị chi tiết đơn hàng: " + e.getMessage());
//         }
//     }

//     // Helper method thêm dòng thông tin
//     private void addInfoRow(GridPane grid, String label, String value, int row) {
//         Label lblLabel = new Label(label);
//         lblLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");

//         Label lblValue = new Label(value != null ? value : "N/A");
//         lblValue.setStyle("-fx-font-weight: bold;");

//         grid.add(lblLabel, 0, row);
//         grid.add(lblValue, 1, row);
//     }
//     // Method hiển thị tất cả đơn hàng (nếu user chọn checkbox)
//     private void showAllOrdersWindow() {
//         try {
//             LOGGER.info("📋 Hiển thị tất cả đơn hàng...");

//             ObservableList<OrderHistory> allOrders = OrderHistoryServiceE.getOrderHistories();

//             if (allOrders.isEmpty()) {
//                 AlertUtil.showInfo("Thông báo", "Không có đơn hàng nào trong hệ thống!");
//                 return;
//             }

//             // Tạo cửa sổ đơn giản hiển thị danh sách
//             Stage listStage = new Stage();
//             listStage.setTitle("Tất cả đơn hàng (" + allOrders.size() + " đơn)");
//             listStage.setResizable(true);

//             // TableView đơn giản
//             TableView<OrderHistory> table = new TableView<>();
//             table.setItems(allOrders);

//             TableColumn<OrderHistory, String> colId = new TableColumn<>("Mã ĐH");
//             colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrderID()));
//             colId.setPrefWidth(100);

//             TableColumn<OrderHistory, String> colDate = new TableColumn<>("Ngày");
//             colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedDate()));
//             colDate.setPrefWidth(150);

//             TableColumn<OrderHistory, String> colCustomer = new TableColumn<>("Khách hàng");
//             colCustomer.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomerName()));
//             colCustomer.setPrefWidth(150);

//             TableColumn<OrderHistory, String> colTotal = new TableColumn<>("Tổng tiền");
//             colTotal.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedAmount()));
//             colTotal.setPrefWidth(120);

//             TableColumn<OrderHistory, Void> colAction = new TableColumn<>("Hành động");
//             colAction.setCellFactory(tc -> new TableCell<OrderHistory, Void>() {
//                 private final Button btn = new Button("Xem chi tiết");
//                 {
//                     btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
//                     btn.setOnAction(event -> {
//                         OrderHistory selectedOrder = getTableView().getItems().get(getIndex());
//                         if (selectedOrder != null) {
//                             listStage.close();
//                             showOrderByIdWindow(selectedOrder.getOrderID());
//                         }
//                     });
//                 }

//                 @Override
//                 protected void updateItem(Void item, boolean empty) {
//                     super.updateItem(item, empty);
//                     if (empty) {
//                         setGraphic(null);
//                     } else {
//                         setGraphic(btn);
//                     }
//                 }
//             });
//             colAction.setPrefWidth(120);

//             table.getColumns().addAll(colId, colDate, colCustomer, colTotal, colAction);

//             Scene scene = new Scene(new VBox(table), 800, 500);
//             listStage.setScene(scene);
//             listStage.show();

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌ Lỗi hiển thị tất cả đơn hàng: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị danh sách đơn hàng: " + e.getMessage());
//         }
//     }
//     // Hiển thị lịch sử đơn hàng từ bộ nhớ
//     // Thay thế method showOrderHistoryInMemory() cũ
//     private void showOrderHistoryInMemory() {
//         try {
//             // Tạo dialog nhập mã đơn hàng
//             Stage searchStage = new Stage();
//             searchStage.initModality(Modality.APPLICATION_MODAL);
//             searchStage.setTitle("Tìm kiếm đơn hàng");
//             searchStage.setResizable(false);

//             VBox layout = new VBox(15);
//             layout.setPadding(new Insets(20));
//             layout.setAlignment(Pos.CENTER);

//             // Header
//             Label headerLabel = new Label("TÌM KIẾM ĐƠN HÀNG");
//             headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

//             // Input mã đơn hàng
//             Label lblOrderId = new Label("Nhập mã đơn hàng:");
//             lblOrderId.setStyle("-fx-font-weight: bold;");

//             TextField txtOrderId = new TextField();
//             txtOrderId.setPromptText("Ví dụ: 1, 2, 3... hoặc ORD001, ORD002...");
//             txtOrderId.setPrefWidth(300);
//             txtOrderId.setStyle("-fx-font-size: 14px;");

//             // Hoặc xem tất cả
//             CheckBox chkShowAll = new CheckBox("Hiển thị tất cả đơn hàng");
//             chkShowAll.setStyle("-fx-font-size: 12px;");

//             // Buttons
//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);

//             Button btnSearch = new Button("Tìm kiếm");
//             btnSearch.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnSearch.setPrefWidth(100);

//             Button btnCancel = new Button("Hủy");
//             btnCancel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnCancel.setPrefWidth(100);

//             buttonBox.getChildren().addAll(btnSearch, btnCancel);

//             // Events
//             btnCancel.setOnAction(e -> searchStage.close());

//             btnSearch.setOnAction(e -> {
//                 try {
//                     searchStage.close();

//                     if (chkShowAll.isSelected()) {
//                         // Hiển thị tất cả đơn hàng
//                         showAllOrdersWindow();
//                     } else {
//                         // Tìm theo ID cụ thể
//                         String orderId = txtOrderId.getText().trim();
//                         if (orderId.isEmpty()) {
//                             AlertUtil.showWarning("Thông báo", "Vui lòng nhập mã đơn hàng!");
//                             return;
//                         }
//                         showOrderByIdWindow(orderId);
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi tìm kiếm đơn hàng: " + ex.getMessage(), ex);
//                     AlertUtil.showError("Lỗi", "Không thể tìm kiếm đơn hàng: " + ex.getMessage());
//                 }
//             });

//             // Enter để tìm kiếm
//             txtOrderId.setOnKeyPressed(event -> {
//                 if (event.getCode().toString().equals("ENTER")) {
//                     btnSearch.fire();
//                 }
//             });

//             layout.getChildren().addAll(headerLabel, lblOrderId, txtOrderId, chkShowAll, buttonBox);

//             Scene scene = new Scene(layout, 400, 250);
//             searchStage.setScene(scene);
//             searchStage.showAndWait();

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị dialog tìm kiếm: " + e.getMessage(), e);
//             AlertUtil.showError("Lỗi", "Không thể mở cửa sổ tìm kiếm: " + e.getMessage());
//         }
//     }

//     // Hiển thị chi tiết đơn hàng từ bộ nhớ
//     private void showOrderDetailsFromMemory(OrderSummary order) {
//         try {
//             if (order == null) {
//                 LOGGER.warning("Lỗi: OrderSummary object là null");
//                 return;
//             }

//             Stage detailStage = new Stage();
//             detailStage.initModality(Modality.APPLICATION_MODAL);
//             detailStage.setTitle("Chi tiết đơn hàng #" + order.getId());

//             BorderPane borderPane = new BorderPane();

//             // Header
//             HBox header = new HBox();
//             header.setPadding(new Insets(15, 20, 15, 20));
//             header.setStyle("-fx-background-color: #2196F3;");

//             Label headerTitle = new Label("CHI TIẾT ĐƠN HÀNG #" + order.getId());
//             headerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

//             header.getChildren().add(headerTitle);
//             header.setAlignment(Pos.CENTER);

//             borderPane.setTop(header);

//             // Content
//             VBox content = new VBox(15);
//             content.setPadding(new Insets(20));

//             // Thông tin đơn hàng
//             VBox orderInfoBox = new VBox(8);
//             orderInfoBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");

//             Label lblCustomer = new Label("Khách hàng: " + order.getCustomerName());
//             Label lblPhone = new Label("SĐT: " + order.getCustomerPhone());
//             Label lblPayment = new Label("Phương thức thanh toán: " + order.getPaymentMethod());
//             Label lblDate = new Label("Ngày mua: " + order.getOrderDate());

//             orderInfoBox.getChildren().addAll(lblCustomer, lblPhone, lblPayment, lblDate);

//             // Danh sách sản phẩm
//             Label lblProductsTitle = new Label("Danh sách sản phẩm:");
//             lblProductsTitle.setStyle("-fx-font-weight: bold;");

//             TableView<CartItemEmployee> detailTable = new TableView<>();
//             detailTable.setPrefHeight(300);

//             TableColumn<CartItemEmployee, String> colProductName = new TableColumn<>("Tên sản phẩm");
//             colProductName.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleStringProperty("N/A");
//                 }
//                 String productName = data.getValue().getProductName();
//                 return new SimpleStringProperty(productName != null ? productName : "N/A");
//             });
//             colProductName.setPrefWidth(200);

//             TableColumn<CartItemEmployee, Integer> colQuantity = new TableColumn<>("SL");
//             colQuantity.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleIntegerProperty(0).asObject();
//                 }
//                 return new SimpleIntegerProperty(data.getValue().getQuantity()).asObject();
//             });
//             colQuantity.setPrefWidth(50);

//             TableColumn<CartItemEmployee, Double> colPrice = new TableColumn<>("Đơn giá");
//             colPrice.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleDoubleProperty(0).asObject();
//                 }
//                 return new SimpleDoubleProperty(data.getValue().getPrice()).asObject();
//             });
//             colPrice.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double price, boolean empty) {
//                     super.updateItem(price, empty);
//                     if (empty || price == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", price) + "đ");
//                     }
//                 }
//             });
//             colPrice.setPrefWidth(100);

//             // Thêm cột bảo hành
//             TableColumn<CartItemEmployee, String> colWarranty = new TableColumn<>("Bảo hành");
//             colWarranty.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleStringProperty("Không");
//                 }

//                 CartItemEmployee item = data.getValue();
//                 if (item.hasWarranty()) {
//                     return new SimpleStringProperty(item.getWarranty().getWarrantyType());
//                 } else {
//                     return new SimpleStringProperty("Không");
//                 }
//             });
//             colWarranty.setPrefWidth(100);

//             TableColumn<CartItemEmployee, Double> colSubtotal = new TableColumn<>("Thành tiền");
//             colSubtotal.setCellValueFactory(data -> {
//                 if (data == null || data.getValue() == null) {
//                     return new SimpleDoubleProperty(0).asObject();
//                 }
//                 return new SimpleDoubleProperty(data.getValue().getTotalPrice()).asObject();
//             });
//             colSubtotal.setCellFactory(tc -> new TableCell<CartItemEmployee, Double>() {
//                 @Override
//                 protected void updateItem(Double total, boolean empty) {
//                     super.updateItem(total, empty);
//                     if (empty || total == null) {
//                         setText(null);
//                     } else {
//                         setText(String.format("%,.0f", total) + "đ");
//                     }
//                 }
//             });
//             colSubtotal.setPrefWidth(100);

//             detailTable.getColumns().addAll(colProductName, colQuantity, colPrice, colWarranty, colSubtotal);

//             // Kiểm tra null trước khi thêm items
//             if (order.getItems() != null) {
//                 detailTable.setItems(FXCollections.observableArrayList(order.getItems()));
//             } else {
//                 detailTable.setItems(FXCollections.observableArrayList());
//             }

//             // Hiển thị tổng tiền
//             Label lblTotal = new Label("Tổng tiền: " + String.format("%,.0f", order.getTotalAmount()) + "đ");
//             lblTotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e91e63;");

//             // Button in hóa đơn và đóng
//             Button btnPrint = new Button("In hóa đơn");
//             btnPrint.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnPrint.setPrefWidth(150);

//             // Fix lỗi lambda expression bằng cách sử dụng final variable
//             final int orderId = order.getId();
//             final double totalAmount = order.getTotalAmount();
//             final String customerName2 = order.getCustomerName();
//             final String customerPhone2 = order.getCustomerPhone();
//             final String paymentMethod2 = order.getPaymentMethod();
//             final String orderDateTime = order.getOrderDate();
//             final List<CartItemEmployee> orderItems = order.getItems() != null ? order.getItems() : new ArrayList<>();

//             btnPrint.setOnAction(e -> {
//                 try {
//                     // In hóa đơn với các biến final
//                     printReceiptWithPaymentMethod(
//                             orderId,
//                             orderItems,
//                             totalAmount,
//                             customerName2,
//                             customerPhone2,
//                             paymentMethod2,
//                             orderDateTime,
//                             currentUser
//                     );
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi in hóa đơn", ex);
//                     showErrorAlert("Có lỗi xảy ra: " + ex.getMessage());
//                 }
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnClose.setPrefWidth(100);
//             btnClose.setOnAction(e -> detailStage.close());

//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.getChildren().addAll(btnPrint, btnClose);
//             buttonBox.setPadding(new Insets(10, 0, 0, 0));

//             content.getChildren().addAll(orderInfoBox, lblProductsTitle, detailTable, lblTotal, buttonBox);

//             borderPane.setCenter(content);

//             Scene scene = new Scene(borderPane, 650, 550);
//             detailStage.setScene(scene);
//             detailStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị chi tiết đơn hàng", e);
//             showErrorAlert("Có lỗi xảy ra: " + e.getMessage());
//         }
//     }

//     // Phương thức in hóa đơn có thêm phương thức thanh toán và thông tin bảo hành
//     public void printReceiptWithPaymentMethod(int orderID, List<CartItemEmployee> items, double totalAmount,
//                                               String customerName, String customerPhone, String paymentMethod,
//                                               String orderDateTime, String cashierName) {
//         try {
//             // Kiểm tra danh sách sản phẩm
//             if (items == null || items.isEmpty()) {
//                 Alert alert = new Alert(Alert.AlertType.WARNING);
//                 alert.setTitle("Cảnh báo");
//                 alert.setHeaderText("Không thể in hóa đơn");
//                 alert.setContentText("Không có sản phẩm nào trong đơn hàng.");
//                 alert.showAndWait();
//                 return;
//             }

//             // Tạo cảnh báo để hiển thị trước khi in
//             Alert printingAlert = new Alert(Alert.AlertType.INFORMATION);
//             printingAlert.setTitle("Đang in hóa đơn");
//             printingAlert.setHeaderText("Đang chuẩn bị in hóa đơn");
//             printingAlert.setContentText("Vui lòng đợi trong giây lát...");
//             printingAlert.show();

//             // Tạo nội dung hóa đơn
//             VBox receiptContent = new VBox(5);
//             receiptContent.setPadding(new Insets(20));
//             receiptContent.setStyle("-fx-background-color: white;");

//             // Tiêu đề
//             Label lblTitle = new Label("HÓA ĐƠN THANH TOÁN");
//             lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-alignment: center;");
//             lblTitle.setMaxWidth(Double.MAX_VALUE);
//             lblTitle.setAlignment(Pos.CENTER);

//             // Logo công ty (nếu có)
//             ImageView logo = new ImageView();
//             try {
//                 InputStream is = getClass().getResourceAsStream("/com/example/stores/images/layout/employee_logo.png");
//                 if (is != null) {
//                     logo.setImage(new Image(is));
//                     logo.setFitWidth(100);
//                     logo.setPreserveRatio(true);
//                 }
//             } catch (Exception e) {
//                 LOGGER.log(Level.WARNING, "Không tìm thấy logo", e);
//             }

//             // Thông tin cửa hàng
//             Label lblStoreName = new Label("COMPUTER STORE");
//             lblStoreName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             Label lblStoreAddress = new Label("Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM");
//             Label lblStorePhone = new Label("Điện thoại: 028.1234.5678");

//             // Thông tin hóa đơn
//             Label lblOrderID = new Label("Mã đơn hàng: #" + orderID);
//             lblOrderID.setStyle("-fx-font-weight: bold;");

//             Label lblDateTime = new Label("Ngày: " + orderDateTime);
//             Label lblCashier = new Label("Thu ngân: " + cashierName);
//             Label lblCustomerName = new Label("Khách hàng: " + customerName);
//             Label lblCustomerPhone = new Label("SĐT khách hàng: " + customerPhone);
//             Label lblPaymentMethod = new Label("Phương thức thanh toán: " + paymentMethod);
//             lblPaymentMethod.setStyle("-fx-font-weight: bold;");

//             // Tạo đường kẻ ngăn cách
//             Separator sep1 = new Separator();
//             sep1.setMaxWidth(Double.MAX_VALUE);

//             // Tiêu đề bảng sản phẩm
//             HBox tableHeader = new HBox(10);
//             Label lblProductHeader = new Label("Sản phẩm");
//             lblProductHeader.setPrefWidth(200);
//             lblProductHeader.setStyle("-fx-font-weight: bold;");

//             Label lblQtyHeader = new Label("SL");
//             lblQtyHeader.setPrefWidth(50);
//             lblQtyHeader.setStyle("-fx-font-weight: bold;");

//             Label lblPriceHeader = new Label("Đơn giá");
//             lblPriceHeader.setPrefWidth(100);
//             lblPriceHeader.setStyle("-fx-font-weight: bold;");

//             Label lblWarrantyHeader = new Label("Bảo hành");
//             lblWarrantyHeader.setPrefWidth(100);
//             lblWarrantyHeader.setStyle("-fx-font-weight: bold;");

//             Label lblSubtotalHeader = new Label("Thành tiền");
//             lblSubtotalHeader.setPrefWidth(100);
//             lblSubtotalHeader.setStyle("-fx-font-weight: bold;");

//             tableHeader.getChildren().addAll(lblProductHeader, lblQtyHeader, lblPriceHeader, lblWarrantyHeader, lblSubtotalHeader);

//             // Danh sách sản phẩm
//             VBox productsBox = new VBox(5);
//             double totalWarrantyPrice = 0.0; // Tổng phí bảo hành

//             for (CartItemEmployee item : items) {
//                 if (item == null) continue;

//                 // Dòng sản phẩm
//                 HBox row = new HBox(10);

//                 String productName = item.getProductName();
//                 if (productName == null) productName = "Sản phẩm không tên";

//                 // Tạo VBox để hiển thị tên sản phẩm + bảo hành nếu có
//                 VBox productInfoBox = new VBox(2);
//                 Label lblProduct = new Label(productName);
//                 lblProduct.setPrefWidth(200);
//                 lblProduct.setWrapText(true);
//                 productInfoBox.getChildren().add(lblProduct);

//                 Label lblQty = new Label(String.valueOf(item.getQuantity()));
//                 lblQty.setPrefWidth(50);

//                 Label lblPrice = new Label(String.format("%,.0f", item.getPrice()) + "đ");
//                 lblPrice.setPrefWidth(100);

//                 // Hiển thị thông tin bảo hành
//                 Label lblWarranty;
//                 if (item.hasWarranty()) {
//                     lblWarranty = new Label(item.getWarranty().getWarrantyType());
//                     totalWarrantyPrice += item.getWarranty().getWarrantyPrice();
//                 } else {
//                     lblWarranty = new Label("Không");
//                 }
//                 lblWarranty.setPrefWidth(100);

//                 // Hiển thị tổng giá trị sản phẩm
//                 Label lblSubtotal = new Label(String.format("%,.0f", item.getTotalPrice()) + "đ");
//                 lblSubtotal.setPrefWidth(100);

//                 row.getChildren().addAll(productInfoBox, lblQty, lblPrice, lblWarranty, lblSubtotal);
//                 productsBox.getChildren().add(row);
//             }

//             // Thêm đường kẻ ngăn cách
//             Separator sep2 = new Separator();
//             sep2.setMaxWidth(Double.MAX_VALUE);

//             // Hiển thị tổng phí bảo hành nếu có
//             VBox summaryBox = new VBox(5);

//             if (totalWarrantyPrice > 0) {
//                 HBox warrantyRow = new HBox(10);
//                 warrantyRow.setAlignment(Pos.CENTER_RIGHT);

//                 Label lblWarrantyTotalHeader = new Label("Tổng phí bảo hành:");
//                 Label lblWarrantyValue = new Label(String.format("%,.0f", totalWarrantyPrice) + "đ");
//                 lblWarrantyValue.setStyle("-fx-font-size: 13px;");

//                 warrantyRow.getChildren().addAll(lblWarrantyHeader, lblWarrantyValue);
//                 summaryBox.getChildren().add(warrantyRow);
//             }

//             // Tổng tiền
//             HBox totalRow = new HBox(10);
//             totalRow.setAlignment(Pos.CENTER_RIGHT);

//             Label lblTotalHeader = new Label("Tổng tiền thanh toán:");
//             lblTotalHeader.setStyle("-fx-font-weight: bold;");

//             Label lblTotalValue = new Label(String.format("%,.0f", totalAmount) + "đ");
//             lblTotalValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

//             totalRow.getChildren().addAll(lblTotalHeader, lblTotalValue);
//             summaryBox.getChildren().add(totalRow);

//             // Thêm thông tin thanh toán chuyển khoản nếu là phương thức chuyển khoản
//             VBox paymentInfoBox = new VBox(10);
//             paymentInfoBox.setAlignment(Pos.CENTER);

//             if ("Chuyển khoản".equals(paymentMethod)) {
//                 // Thêm đường kẻ ngăn cách
//                 Separator sepPayment = new Separator();
//                 sepPayment.setMaxWidth(Double.MAX_VALUE);

//                 Label lblPaymentInfo = new Label("THÔNG TIN CHUYỂN KHOẢN");
//                 lblPaymentInfo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
//                 lblPaymentInfo.setAlignment(Pos.CENTER);
//                 lblPaymentInfo.setMaxWidth(Double.MAX_VALUE);

//                 Label lblBank = new Label("Ngân hàng: TECHCOMBANK");
//                 Label lblAccount = new Label("Số tài khoản: 1903 5552 6789");
//                 Label lblAccountName = new Label("Chủ TK: CÔNG TY COMPUTER STORE");
//                 Label lblContent = new Label("Nội dung CK: " + orderID + " " + customerPhone);

//                 // QR Code cho chuyển khoản
//                 ImageView qrCode = new ImageView();
//                 try {
//                     // Mặc định sử dụng ảnh QR từ resources
//                     InputStream qrIs = getClass().getResourceAsStream("/com/example/stores/images/qr_payment.png");
//                     if (qrIs != null) {
//                         qrCode.setImage(new Image(qrIs));
//                         qrCode.setFitWidth(150);
//                         qrCode.setPreserveRatio(true);
//                     } else {
//                         // QR Code cho chuyển khoản - tạo ảnh trống nếu không tìm thấy
//                         qrCode.setFitWidth(150);
//                         qrCode.setFitHeight(150);
//                         qrCode.setStyle("-fx-background-color: #f0f0f0;");
//                     }
//                 } catch (Exception e) {
//                     LOGGER.log(Level.WARNING, "Không tìm thấy ảnh QR", e);
//                 }

//                 paymentInfoBox.getChildren().addAll(sepPayment, lblPaymentInfo, lblBank, lblAccount, lblAccountName, lblContent, qrCode);
//             }

//             // Thông tin cuối hóa đơn
//             Label lblThankYou = new Label("Cảm ơn quý khách đã mua hàng!");
//             lblThankYou.setAlignment(Pos.CENTER);
//             lblThankYou.setMaxWidth(Double.MAX_VALUE);
//             lblThankYou.setStyle("-fx-font-style: italic; -fx-alignment: center;");

//             Label lblContact = new Label("Hotline: 1800.1234 - Website: www.computerstore.com.vn");
//             lblContact.setAlignment(Pos.CENTER);
//             lblContact.setMaxWidth(Double.MAX_VALUE);
//             lblContact.setStyle("-fx-font-size: 10px; -fx-alignment: center;");

//             // Thêm thông tin chính sách bảo hành
//             Label lblWarrantyPolicy = new Label("Để biết thêm về chính sách bảo hành, vui lòng xem tại website");
//             lblWarrantyPolicy.setAlignment(Pos.CENTER);
//             lblWarrantyPolicy.setMaxWidth(Double.MAX_VALUE);
//             lblWarrantyPolicy.setStyle("-fx-font-size: 10px; -fx-font-style: italic; -fx-alignment: center;");

//             // Thêm tất cả các phần tử vào hóa đơn
//             HBox logoBox = new HBox(10);
//             logoBox.setAlignment(Pos.CENTER);
//             logoBox.getChildren().add(logo);

//             receiptContent.getChildren().addAll(
//                     lblTitle,
//                     logoBox,
//                     lblStoreName,
//                     lblStoreAddress,
//                     lblStorePhone,
//                     new Separator(),
//                     lblOrderID,
//                     lblDateTime,
//                     lblCashier,
//                     lblCustomerName,
//                     lblCustomerPhone,
//                     lblPaymentMethod,
//                     sep1,
//                     tableHeader,
//                     productsBox,
//                     sep2,
//                     summaryBox
//             );

//             // Thêm thông tin thanh toán chuyển khoản nếu có
//             if (!paymentInfoBox.getChildren().isEmpty()) {
//                 receiptContent.getChildren().add(paymentInfoBox);
//             }

//             // Thêm phần kết
//             Separator sepEnd = new Separator();
//             sepEnd.setMaxWidth(Double.MAX_VALUE);

//             receiptContent.getChildren().addAll(
//                     sepEnd,
//                     lblThankYou,
//                     lblContact,
//                     lblWarrantyPolicy
//             );

//             // Định dạng kích thước hóa đơn
//             ScrollPane scrollPane = new ScrollPane(receiptContent);
//             scrollPane.setPrefWidth(550); // Tăng kích thước để hiển thị đủ cột bảo hành
//             scrollPane.setPrefHeight(600);
//             scrollPane.setFitToWidth(true);

//             // Tạo Scene và Stage để hiển thị trước khi in
//             Scene scene = new Scene(scrollPane);
//             Stage printPreviewStage = new Stage();
//             printPreviewStage.setTitle("Xem trước hóa đơn");
//             printPreviewStage.setScene(scene);

//             // Đóng cảnh báo đang in
//             printingAlert.close();

//             // Hiển thị hóa đơn
//             printPreviewStage.show();

//             // Thêm nút in và lưu vào cửa sổ xem trước
//             Button btnPrint = new Button("In");
//             btnPrint.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
//             btnPrint.setOnAction(e -> {
//                 try {
//                     PrinterJob job = PrinterJob.createPrinterJob();
//                     if (job != null) {
//                         boolean success = job.printPage(receiptContent);
//                         if (success) {
//                             job.endJob();
//                             printPreviewStage.close();

//                             Alert printSuccessAlert = new Alert(Alert.AlertType.INFORMATION);
//                             printSuccessAlert.setTitle("In thành công");
//                             printSuccessAlert.setHeaderText("Hóa đơn đã được gửi đến máy in");
//                             printSuccessAlert.setContentText("Vui lòng kiểm tra máy in của bạn.");
//                             printSuccessAlert.showAndWait();
//                         }
//                     }
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi in hóa đơn", ex);
//                     showErrorAlert("Lỗi khi in hóa đơn: " + ex.getMessage());
//                 }
//             });

//             // Nút lưu PDF (giả định)
//             Button btnSave = new Button("Lưu PDF");
//             btnSave.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
//             btnSave.setOnAction(e -> {
//                 try {
//                     Alert saveAlert = new Alert(Alert.AlertType.INFORMATION);
//                     saveAlert.setTitle("Lưu PDF");
//                     saveAlert.setHeaderText("Hóa đơn đã được lưu");
//                     saveAlert.setContentText("Hóa đơn đã được lưu vào thư mục Documents.");
//                     saveAlert.showAndWait();
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi lưu PDF", ex);
//                     showErrorAlert("Lỗi khi lưu PDF: " + ex.getMessage());
//                 }
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnClose.setOnAction(e -> printPreviewStage.close());

//             HBox buttonBox = new HBox(10, btnPrint, btnSave, btnClose);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.setPadding(new Insets(10));

//             BorderPane borderPane = new BorderPane();
//             borderPane.setCenter(scrollPane);
//             borderPane.setBottom(buttonBox);

//             scene.setRoot(borderPane);

//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi in hóa đơn", e);
//             Alert errorAlert = new Alert(Alert.AlertType.ERROR);
//             errorAlert.setTitle("Lỗi in hóa đơn");
//             errorAlert.setHeaderText("Không thể in hóa đơn");
//             errorAlert.setContentText("Chi tiết lỗi: " + e.getMessage());
//             errorAlert.showAndWait();
//         }
//     }

//     /**
//      * Thêm sản phẩm vào giỏ hàng với thông tin bảo hành
//      */
//     private void addToCartWithWarranty(CartItemEmployee item) {
//         if (item == null) {
//             LOGGER.warning("Lỗi: CartItemEmployee là null");
//             return;
//         }

//         // Tìm sản phẩm trong database để kiểm tra tồn kho
//         Product product = findProductById(item.getProductID());
//         if (product == null) {
//             AlertUtil.showWarning("Lỗi", "Không tìm thấy thông tin sản phẩm");
//             return;
//         }

//         // Kiểm tra số lượng tồn kho trước khi thêm
//         if (product.getQuantity() <= 0) {
//             AlertUtil.showWarning("Hết hàng", "Sản phẩm đã hết hàng!");
//             return;
//         }

//         // Tìm kiếm sản phẩm trong giỏ hàng với CÙNG loại bảo hành
//         boolean existingFound = false;
//         for (CartItemEmployee cartItem : cartItems) {
//             if (cartItem.getProductID().equals(item.getProductID())) {
//                 // Phải cùng sản phẩm và cùng loại bảo hành
//                 if (cartItem.hasWarranty() == item.hasWarranty() &&
//                         (!cartItem.hasWarranty() ||
//                                 cartItem.getWarranty().getWarrantyType().equals(item.getWarranty().getWarrantyType()))) {

//                     if (cartItem.getQuantity() < product.getQuantity()) {
//                         // Cập nhật số lượng nếu còn hàng
//                         cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
//                         existingFound = true;
//                         LOGGER.info("Đã tăng số lượng " + cartItem.getProductName() +
//                                 " (BH: " + (cartItem.hasWarranty() ? cartItem.getWarranty().getWarrantyType() : "Không") +
//                                 ") lên " + cartItem.getQuantity());
//                     } else {
//                         AlertUtil.showWarning("Số lượng tối đa",
//                                 "Không thể thêm nữa, số lượng trong kho chỉ còn " + product.getQuantity());
//                     }
//                     break;
//                 }
//             }
//         }

//         // Nếu không tìm thấy sản phẩm đã có trong giỏ với cùng loại bảo hành
//         if (!existingFound) {
//             cartItems.add(item);
//             LOGGER.info("Đã thêm " + item.getProductName() +
//                     " (BH: " + (item.hasWarranty() ? item.getWarranty().getWarrantyType() : "Không") +
//                     ") vào giỏ hàng");
//         }

//         // Cập nhật hiển thị giỏ hàng
//         updateCartDisplay();
//     }

//     // Tìm sản phẩm theo ID
//     private Product findProductById(String productID) {
//         if (productID == null || products == null) {
//             return null;
//         }

//         for (Product product : products) {
//             if (product.getProductID().equals(productID)) {
//                 return product;
//             }
//         }

//         return null;
//     }

//     // Sửa lại phần hiển thị dialog chi tiết sản phẩm trong PosOverviewController
//     private void showProductDetails(Product product) {
//         try {
//             if (product == null) {
//                 LOGGER.warning("Lỗi: Product object là null");
//                 return;
//             }

//             Stage detailStage = new Stage();
//             detailStage.initModality(Modality.APPLICATION_MODAL);
//             detailStage.setTitle("Chi tiết sản phẩm");

//             VBox layout = new VBox(10);
//             layout.setPadding(new Insets(20));
//             layout.setStyle("-fx-background-color: white;");

//             // Hiển thị ảnh sản phẩm (giữ nguyên code cũ)
//             final ImageView productImage = new ImageView();
//             productImage.setFitWidth(200);
//             productImage.setFitHeight(150);
//             productImage.setPreserveRatio(true);

//             // Tải ảnh sản phẩm (giữ nguyên code cũ)
//             String imagePath = product.getImagePath();
//             if (imagePath != null && !imagePath.startsWith("/")) {
//                 imagePath = "/com/example/stores/images/" + imagePath;
//             } else if (imagePath == null) {
//                 imagePath = "/com/example/stores/images/no_image.png";
//             }

//             try {
//                 Image image = new Image(getClass().getResourceAsStream(imagePath));
//                 productImage.setImage(image);
//             } catch (Exception e) {
//                 productImage.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/no_image.png")));
//                 LOGGER.warning("Không tải được ảnh chi tiết sản phẩm: " + e.getMessage());
//             }

//             final HBox imageBox = new HBox();
//             imageBox.setAlignment(Pos.CENTER);
//             imageBox.getChildren().add(productImage);

//             // Tên sản phẩm
//             String productName = (product.getProductName() != null) ? product.getProductName() : "Sản phẩm không có tên";
//             Label lblName = new Label(productName);
//             lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
//             lblName.setWrapText(true);

//             // Giá sản phẩm
//             Label lblPrice = new Label(String.format("Giá: %,d₫", (long)product.getPrice()));
//             lblPrice.setStyle("-fx-text-fill: #e91e63; -fx-font-weight: bold; -fx-font-size: 16px;");

//             // Thông tin cơ bản (giữ nguyên code cũ)
//             VBox specsBox = new VBox(5);
//             specsBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");

//             if (product.getCategoryID() != null) {
//                 Label lblCategory = new Label("Danh mục: " + getCategoryName(product.getCategoryID()));
//                 specsBox.getChildren().add(lblCategory);
//             }

//             Label lblStock = new Label("Tồn kho: " + product.getQuantity() + " sản phẩm");
//             specsBox.getChildren().add(lblStock);

//             String status = product.getStatus();
//             Label lblStatus = new Label("Trạng thái: " + (status != null ? status : "Không xác định"));
//             lblStatus.setStyle(status != null && status.equals("Còn hàng") ?
//                     "-fx-text-fill: #4caf50; -fx-font-weight: bold;" :
//                     "-fx-text-fill: #f44336; -fx-font-weight: bold;");
//             specsBox.getChildren().add(lblStatus);

//             // PHẦN BẢO HÀNH - CẬP NHẬT CHỈ CÒN 2 LOẠI: THƯỜNG VÀ VÀNG
//             VBox warrantyBox = new VBox(5);
//             warrantyBox.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 10; -fx-background-radius: 5;");

//             Label lblWarrantyTitle = new Label("Lựa chọn bảo hành:");
//             lblWarrantyTitle.setStyle("-fx-font-weight: bold;");
//             warrantyBox.getChildren().add(lblWarrantyTitle);

//             // ComboBox để chọn bảo hành
//             ComboBox<String> cbWarranty = new ComboBox<>();

//             // Kiểm tra sản phẩm có đủ điều kiện bảo hành thường không
//             boolean isEligibleForStdWarranty = WarrantyCalculator.isEligibleForStandardWarranty(product);

//             Label lblWarrantyInfo = new Label();

//             // Hiển thị các lựa chọn bảo hành dựa trên điều kiện
//             if (isEligibleForStdWarranty) {
//                 cbWarranty.getItems().addAll("Thường", "Vàng");
//                 cbWarranty.setValue("Thường");

//                 // Miêu tả bảo hành
//                 lblWarrantyInfo.setText("✅ Sản phẩm được bảo hành Thường miễn phí 12 tháng");
//                 lblWarrantyInfo.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 12px;");
//             } else {
//                 cbWarranty.getItems().add("Không");
//                 cbWarranty.setValue("Không");

//                 // Miêu tả không đủ điều kiện
//                 lblWarrantyInfo.setText("❌ Sản phẩm dưới 500.000đ không được bảo hành");
//                 lblWarrantyInfo.setStyle("-fx-text-fill: #f44336; -fx-font-size: 12px;");
//             }

//             warrantyBox.getChildren().addAll(cbWarranty, lblWarrantyInfo);

//             // Hiển thị phí bảo hành
//             Label lblWarrantyPrice = new Label("Phí bảo hành: 0đ");
//             warrantyBox.getChildren().add(lblWarrantyPrice);

//             // Hiển thị tổng tiền kèm bảo hành
//             Label lblTotalWithWarranty = new Label("Tổng tiền: " + String.format("%,d₫", (long)product.getPrice()));
//             lblTotalWithWarranty.setStyle("-fx-font-weight: bold;");
//             warrantyBox.getChildren().add(lblTotalWithWarranty);

//             // Cập nhật giá bảo hành khi thay đổi loại bảo hành
//             cbWarranty.setOnAction(e -> {
//                 String selectedType = cbWarranty.getValue();

//                 if ("Không".equals(selectedType) || "Thường".equals(selectedType)) {
//                     lblWarrantyPrice.setText("Phí bảo hành: 0đ");
//                     lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,d₫", (long)product.getPrice()));

//                     if ("Thường".equals(selectedType)) {
//                         lblWarrantyInfo.setText("✅ Bảo hành Thường miễn phí 12 tháng");
//                         lblWarrantyInfo.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 12px;");
//                     } else {
//                         lblWarrantyInfo.setText("❌ Không bảo hành");
//                         lblWarrantyInfo.setStyle("-fx-text-fill: #f44336; -fx-font-size: 12px;");
//                     }
//                     return;
//                 }

//                 // Tính phí bảo hành Vàng (10% giá sản phẩm)
//                 double warrantyFee = product.getPrice() * 0.1;
//                 lblWarrantyPrice.setText("Phí bảo hành: " + String.format("%,d₫", (long)warrantyFee));

//                 // Cập nhật tổng tiền
//                 double totalPrice = product.getPrice() + warrantyFee;
//                 lblTotalWithWarranty.setText("Tổng tiền: " + String.format("%,d₫", (long)totalPrice));

//                 // Thêm giải thích về bảo hành Vàng
//                 lblWarrantyInfo.setText("✨ Bảo hành Vàng 24 tháng, 1 đổi 1 trong 24 tháng");
//                 lblWarrantyInfo.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 12px; -fx-font-weight: bold;");
//             });

//             // Mô tả sản phẩm và nút thêm vào giỏ (giữ nguyên code)
//             Label lblDescTitle = new Label("Mô tả sản phẩm:");
//             lblDescTitle.setStyle("-fx-font-weight: bold;");

//             String description = (product.getDescription() != null) ? product.getDescription() : "Không có thông tin";
//             TextArea txtDescription = new TextArea(description);
//             txtDescription.setWrapText(true);
//             txtDescription.setEditable(false);
//             txtDescription.setPrefHeight(100);

//             // Nút thêm vào giỏ
//             Button btnAddToCart = new Button("Thêm vào giỏ");
//             btnAddToCart.setPrefWidth(200);
//             btnAddToCart.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//             btnAddToCart.setOnAction(e -> {
//                 try {
//                     // Lấy loại bảo hành đã chọn
//                     String selectedWarranty = cbWarranty.getValue();

//                     // Tạo đối tượng CartItemEmployee mới
//                     CartItemEmployee newItem = new CartItemEmployee(
//                             product.getProductID(),
//                             product.getProductName(),
//                             product.getPrice(),
//                             1,
//                             product.getImagePath(),
//                             employeeId,
//                             currentUser != null ? currentUser : "unknown",
//                             product.getCategoryID()
//                     );

//                     // Tạo bảo hành nếu không phải là "Không" bảo hành
//                     if ("Thường".equals(selectedWarranty) || "Vàng".equals(selectedWarranty)) {
//                         // Tạo bảo hành và gán vào sản phẩm
//                         Warranty warranty = WarrantyCalculator.createWarranty(product, selectedWarranty);
//                         newItem.setWarranty(warranty);
//                     }

//                     // Thêm vào giỏ hàng
//                     addToCartWithWarranty(newItem);

//                     detailStage.close(); // Đóng cửa sổ chi tiết
//                     AlertUtil.showInformation("Thành công", "Đã thêm sản phẩm vào giỏ hàng!");
//                 } catch (Exception ex) {
//                     LOGGER.log(Level.SEVERE, "Lỗi khi thêm sản phẩm vào giỏ hàng", ex);
//                     AlertUtil.showError("Lỗi", "Không thể thêm sản phẩm vào giỏ hàng: " + ex.getMessage());
//                 }
//             });

//             Button btnClose = new Button("Đóng");
//             btnClose.setPrefWidth(100);
//             btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
//             btnClose.setOnAction(e -> detailStage.close());

//             HBox buttonBox = new HBox(10);
//             buttonBox.setAlignment(Pos.CENTER);
//             buttonBox.getChildren().addAll(btnAddToCart, btnClose);

//             // Thêm tất cả vào layout
//             layout.getChildren().addAll(
//                     imageBox,
//                     lblName,
//                     lblPrice,
//                     new Separator(),
//                     specsBox,
//                     new Separator(),
//                     warrantyBox,
//                     new Separator(),
//                     lblDescTitle,
//                     txtDescription,
//                     buttonBox
//             );

//             Scene scene = new Scene(layout, 400, 800);
//             detailStage.setScene(scene);
//             detailStage.showAndWait();
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị chi tiết sản phẩm", e);
//             AlertUtil.showError("Lỗi", "Không thể hiển thị chi tiết sản phẩm: " + e.getMessage());
//         }
//     }

//     // Tạo dòng hiển thị cho sản phẩm trong giỏ hàng
//     private HBox createCartItemRow(CartItemEmployee item) {
//         HBox row = new HBox();
//         row.setSpacing(10);
//         row.setPadding(new Insets(5));
//         row.setAlignment(Pos.CENTER_LEFT);

//         // Tên sản phẩm với thông tin bảo hành
//         VBox productInfoBox = new VBox(2);
//         Label lblName = new Label(item.getProductName());
//         lblName.setStyle("-fx-font-weight: bold;");
//         productInfoBox.getChildren().add(lblName);

//         // Thêm thông tin bảo hành nếu có
//         if (item.hasWarranty()) {
//             Label lblWarranty = new Label("BH: " + item.getWarranty().getWarrantyType());
//             lblWarranty.setStyle("-fx-font-size: 11px; -fx-text-fill: #2196F3;");
//             productInfoBox.getChildren().add(lblWarranty);
//         }

//         productInfoBox.setPrefWidth(200);

//         // Số lượng với nút tăng/giảm
//         HBox quantityBox = new HBox(5);
//         quantityBox.setAlignment(Pos.CENTER);

//         Button btnMinus = new Button("-");
//         btnMinus.setMinWidth(30);
//         btnMinus.setOnAction(e -> decreaseQuantity(item));

//         Label lblQuantity = new Label(String.valueOf(item.getQuantity()));
//         lblQuantity.setAlignment(Pos.CENTER);
//         lblQuantity.setMinWidth(30);
//         lblQuantity.setStyle("-fx-font-weight: bold;");

//         Button btnPlus = new Button("+");
//         btnPlus.setMinWidth(30);
//         btnPlus.setOnAction(e -> increaseQuantity(item));

//         quantityBox.getChildren().addAll(btnMinus, lblQuantity, btnPlus);
//         quantityBox.setPrefWidth(120);

//         // Đơn giá
//         Label lblPrice = new Label(String.format("%,.0f", item.getPrice()) + "đ");
//         lblPrice.setPrefWidth(100);
//         lblPrice.setAlignment(Pos.CENTER_RIGHT);

//         // Bảo hành
//         Label lblWarranty = new Label(item.hasWarranty() ? item.getWarranty().getWarrantyType() : "Không");
//         lblWarranty.setPrefWidth(80);
//         lblWarranty.setAlignment(Pos.CENTER);
//         if (item.hasWarranty()) {
//             lblWarranty.setStyle("-fx-text-fill: #4CAF50;");
//         }

//         // Tổng tiền
//         Label lblTotal = new Label(String.format("%,.0f", item.getTotalPrice()) + "đ");
//         lblTotal.setPrefWidth(100);
//         lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #e91e63;");
//         lblTotal.setAlignment(Pos.CENTER_RIGHT);

//         // Nút xóa
//         Button btnRemove = new Button("✖");
//         btnRemove.setStyle("-fx-background-color: transparent; -fx-text-fill: #f44336; -fx-font-weight: bold;");
//         btnRemove.setOnAction(e -> removeFromCart(item));

//         // Thêm tất cả vào dòng
//         row.getChildren().addAll(productInfoBox, quantityBox, lblPrice, lblWarranty, lblTotal, btnRemove);

//         return row;
//     }

//     // Tăng số lượng sản phẩm trong giỏ hàng
//     private void increaseQuantity(CartItemEmployee item) {
//         if (item == null) return;

//         Product product = findProductById(item.getProductID());
//         if (product == null) {
//             AlertUtil.showWarning("Lỗi", "Không tìm thấy thông tin sản phẩm");
//             return;
//         }

//         // Kiểm tra số lượng tồn kho
//         if (item.getQuantity() < product.getQuantity()) {
//             item.setQuantity(item.getQuantity() + 1);
//             updateCartDisplay();
//         } else {
//             AlertUtil.showWarning("Số lượng tối đa",
//                     "Không thể thêm nữa, số lượng trong kho chỉ còn " + product.getQuantity());
//         }
//     }

//     // Giảm số lượng sản phẩm trong giỏ hàng
//     private void decreaseQuantity(CartItemEmployee item) {
//         if (item == null) return;

//         if (item.getQuantity() > 1) {
//             item.setQuantity(item.getQuantity() - 1);
//             updateCartDisplay();
//         } else {
//             // Nếu số lượng là 1, hỏi xem có muốn xóa không
//             Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//             alert.setTitle("Xóa sản phẩm");
//             alert.setHeaderText("Xác nhận xóa");
//             alert.setContentText("Bạn có muốn xóa sản phẩm này khỏi giỏ hàng?");

//             Optional<ButtonType> result = alert.showAndWait();
//             if (result.isPresent() && result.get() == ButtonType.OK) {
//                 removeFromCart(item);
//             }
//         }
//     }

//     // Xóa sản phẩm khỏi giỏ hàng
//     private void removeFromCart(CartItemEmployee item) {
//         if (item != null) {
//             cartItems.remove(item);
//             updateCartDisplay();
//         }
//     }

//     // Cập nhật hiển thị giỏ hàng
//     private void updateCartDisplay() {
//         // Cập nhật tổng tiền
//         updateTotal();

//         // Cập nhật TableView
//         cartTable.refresh();
//     }

//     // Cập nhật tổng tiền giỏ hàng
//     private void updateTotal() {
//         double total = calculateTotalAmount();
//         if (lblTotal != null) {
//             lblTotal.setText("Tổng tiền: " + String.format("%,.0f", total) + "đ");
//         }
//     }

//     // Tính tổng tiền giỏ hàng
//     private double calculateTotalAmount() {
//         double total = 0.0;
//         for (CartItemEmployee item : cartItems) {
//             if (item != null) {
//                 total += item.getTotalPrice();
//             }
//         }
//         return total;
//     }

//     // Xóa toàn bộ giỏ hàng
//     private void clearCart() {
//         cartItems.clear();
//         updateCartDisplay();
//         LOGGER.info("Đã xóa toàn bộ giỏ hàng");
//     }

//     // Lấy tên danh mục từ ID
//     private String getCategoryName(String categoryId) {
//         if (categoryId == null) return "Không xác định";

//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.warning("Không thể kết nối đến database");
//                 return "Không xác định";
//             }

//             // FIX LỖI: Sửa tên bảng từ Category thành Categories và category_name thành categoryName
//             String query = "SELECT categoryName FROM Categories WHERE categoryID = ?";
//             stmt = conn.prepareStatement(query);
//             stmt.setString(1, categoryId);
//             rs = stmt.executeQuery();

//             if (rs.next()) {
//                 return rs.getString("categoryName");
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.WARNING, "Lỗi SQL khi lấy tên danh mục: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.WARNING, "Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.WARNING, "Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }

//         return "Không xác định";
//     }
//     // Lấy danh sách các danh mục phân biệt
//     private List<String> getDistinctCategories() {
//         List<String> categories = new ArrayList<>();
//         categories.add("Tất cả"); // Luôn có tùy chọn "Tất cả"

//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.severe("💀 QUẠC!!! Không thể kết nối đến database");
//                 return categories;
//             }

//             // FIX LỖI: Sửa tên bảng từ Category thành Categories
//             // Sửa tên cột từ category_name thành categoryName - match với schema thực tế
//             String query = "SELECT DISTINCT categoryID, categoryName FROM Categories ORDER BY categoryName";
//             stmt = conn.prepareStatement(query);
//             rs = stmt.executeQuery();

//             int categoryCount = 0;

//             while (rs.next()) {
//                 String categoryName = rs.getString("categoryName");
//                 if (categoryName != null && !categoryName.isEmpty()) {
//                     categories.add(categoryName);
//                     categoryCount++;
//                 }
//             }

//             LOGGER.info("✨✨✨ Đã tìm thấy " + categoryCount + " danh mục từ database slayyy");

//             if (categoryCount == 0) {
//                 LOGGER.warning("🚨🚨 SKSKSK EM hong tìm thấy danh mục nào trong database luôn á!!!");
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi SQL khi lấy danh mục: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "😭😭 Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }

//         return categories;
//     }

//     // Tải dữ liệu sản phẩm từ database
//     // Em sẽ sửa lại hàm loadProductsFromDatabase để FIX LỖI NGAY LAPPPPP
//     private void loadProductsFromDatabase() {
//         Connection conn = null;
//         PreparedStatement stmt = null;
//         ResultSet rs = null;

//         try {
//             conn = DBConfig.getConnection();
//             if (conn == null) {
//                 LOGGER.severe("❌❌❌ Không thể kết nối đến database");
//                 return;
//             }

//             // FIX LỖI: Sửa lại câu query SQL - CHÚ Ý KHÔNG DÙNG WHERE NỮA
//             // Trước đây chỉ lấy sản phẩm có status = "Còn hàng" hoặc "Active"
//             // => Sửa lại để lấy TẤT CẢ sản phẩm, sort theo quantity để hiển thị sản phẩm còn hàng lên trên
//             String query = "SELECT * FROM Products ORDER BY quantity DESC";
//             stmt = conn.prepareStatement(query);
//             rs = stmt.executeQuery();

//             products.clear(); // Xóa danh sách cũ

//             int productCount = 0; // Đếm số sản phẩm load được

//             while (rs.next()) {
//                 Product product = new Product();
//                 product.setProductID(rs.getString("productID"));
//                 product.setProductName(rs.getString("productName"));
//                 product.setPrice(rs.getDouble("price"));
//                 product.setQuantity(rs.getInt("quantity"));
//                 product.setDescription(rs.getString("description"));
//                 product.setStatus(rs.getString("status"));
//                 product.setCategoryID(rs.getString("categoryID"));

//                 // Xử lý đường dẫn hình ảnh
//                 String imagePath = rs.getString("imagePath");
//                 if (imagePath != null && !imagePath.startsWith("/")) {
//                     imagePath = "/com/example/stores/images/" + imagePath;
//                 }
//                 product.setImagePath(imagePath);

//                 products.add(product);
//                 productCount++;
//             }

//             LOGGER.info("✅✅✅ Đã load được " + productCount + " sản phẩm từ database");

//             if (productCount == 0) {
//                 // Debug thêm thông tin nếu không load được sản phẩm nào
//                 LOGGER.warning("⚠️⚠️⚠️ Không tìm thấy sản phẩm nào trong database!!!");
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi SQL khi lấy dữ liệu sản phẩm: " + e.getMessage(), e);
//         } catch (Exception e) {
//             LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi không xác định: " + e.getMessage(), e);
//         } finally {
//             try {
//                 if (rs != null) rs.close();
//                 if (stmt != null) stmt.close();
//                 if (conn != null) conn.close();
//             } catch (SQLException ex) {
//                 LOGGER.log(Level.SEVERE, "❌❌❌ Lỗi khi đóng kết nối: " + ex.getMessage(), ex);
//             }
//         }
//     }

//     // Làm mới danh sách sản phẩm trên giao diện
//     private void refreshProductList() {
//         if (productFlowPane == null) {
//             LOGGER.warning("productFlowPane chưa được khởi tạo");
//             return;
//         }

//         // Xóa tất cả sản phẩm hiện tại
//         productFlowPane.getChildren().clear();

//         if (products.isEmpty()) {
//             Label lblEmpty = new Label("Không có sản phẩm nào.");
//             lblEmpty.setStyle("-fx-font-style: italic;");
//             productFlowPane.getChildren().add(lblEmpty);
//             return;
//         }

//         // Lọc sản phẩm theo điều kiện
//         List<Product> filteredProducts = filterProducts();

//         // Sắp xếp sản phẩm theo điều kiện
//         sortProducts(filteredProducts);

//         // Lưu danh sách hiện tại để sử dụng sau này
//         currentFilteredProducts = new ArrayList<>(filteredProducts);

//         // Giới hạn số lượng sản phẩm hiển thị
//         List<Product> displayProducts = filteredProducts.stream()
//                 .limit(productLimit)
//                 .collect(Collectors.toList());

//         // Hiển thị sản phẩm
//         for (Product product : displayProducts) {
//             VBox productBox = createProductBox(product);
//             productFlowPane.getChildren().add(productBox);
//         }

//         // Thêm nút "Xem thêm" nếu còn sản phẩm
//         if (filteredProducts.size() > productLimit) {
//             Button btnLoadMore = createLoadMoreButton();
//             productFlowPane.getChildren().add(btnLoadMore);
//         }
//     }

//     // Lọc sản phẩm theo các điều kiện
//     private List<Product> filterProducts() {
//         List<Product> filteredList = new ArrayList<>(products);

//         // Lọc theo danh mục
//         if (cbCategory != null && cbCategory.getValue() != null && !cbCategory.getValue().equals("Tất cả")) {
//             String selectedCategory = cbCategory.getValue();
//             filteredList = filteredList.stream()
//                     .filter(p -> {
//                         String categoryName = getCategoryName(p.getCategoryID());
//                         return categoryName.equals(selectedCategory);
//                     })
//                     .collect(Collectors.toList());
//         }

//         // Lọc theo từ khóa tìm kiếm
//         if (txtSearch != null && txtSearch.getText() != null && !txtSearch.getText().trim().isEmpty()) {
//             String keyword = txtSearch.getText().trim().toLowerCase();
//             filteredList = filteredList.stream()
//                     .filter(p -> p.getProductName() != null && p.getProductName().toLowerCase().contains(keyword))
//                     .collect(Collectors.toList());
//         }

//         return filteredList;
//     }

//     // Sắp xếp sản phẩm theo điều kiện đã chọn
//     private void sortProducts(List<Product> list) {
//         if (cbSort == null || cbSort.getValue() == null) return;

//         String sortOption = cbSort.getValue();
//         switch (sortOption) {
//             case "Tên A-Z":
//                 // FIX LỖI: Thêm kiểu Product vào lambda để compiler biết đây là Product object
//                 list.sort(Comparator.comparing((Product p) -> p.getProductName() != null ? p.getProductName() : ""));
//                 break;
//             case "Tên Z-A":
//                 // FIX LỖI: Thêm kiểu Product vào lambda tương tự
//                 list.sort(Comparator.comparing((Product p) -> p.getProductName() != null ? p.getProductName() : "").reversed());
//                 break;
//             case "Giá thấp đến cao":
//                 list.sort(Comparator.comparing(Product::getPrice));
//                 break;
//             case "Giá cao đến thấp":
//                 list.sort(Comparator.comparing(Product::getPrice).reversed());
//                 break;
//             // Mặc định không sắp xếp (giữ nguyên thứ tự)
//         }
//     }

//     // Tạo box hiển thị sản phẩm
//     private VBox createProductBox(Product product) {
//         VBox box = new VBox(8); // Khoảng cách giữa các thành phần
//         box.setPrefWidth(160);
//         box.setPrefHeight(260);
//         box.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: white; -fx-padding: 10;");

//         // Tạo hiệu ứng hover
//         box.setOnMouseEntered(e -> {
//             box.setStyle("-fx-border-color: #2196F3; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f5f5f5; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.4), 10, 0, 0, 0);");
//             box.setCursor(Cursor.HAND);
//         });

//         box.setOnMouseExited(e -> {
//             box.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: white; -fx-padding: 10;");
//         });

//         // Xử lý sự kiện click để xem chi tiết sản phẩm
//         box.setOnMouseClicked(e -> showProductDetails(product));

//         // Hiển thị hình ảnh sản phẩm
//         ImageView imageView = new ImageView();
//         imageView.setFitWidth(140);
//         imageView.setFitHeight(105);
//         imageView.setPreserveRatio(true);

//         String imagePath = product.getImagePath();
//         if (imagePath == null) {
//             imagePath = "/com/example/stores/images/no_image.png";
//         }

//         try {
//             Image image = new Image(getClass().getResourceAsStream(imagePath));
//             imageView.setImage(image);
//         } catch (Exception e) {
//             try {
//                 Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/stores/images/no_image.png"));
//                 imageView.setImage(defaultImage);
//             } catch (Exception ex) {
//                 LOGGER.warning("Không tải được ảnh sản phẩm: " + ex.getMessage());
//             }
//         }

//         // Hiển thị tên sản phẩm
//         String productName = product.getProductName();
//         if (productName == null) productName = "Sản phẩm không tên";
//         if (productName.length() > 40) {
//             productName = productName.substring(0, 37) + "...";
//         }

//         Label nameLabel = new Label(productName);
//         nameLabel.setWrapText(true);
//         nameLabel.setPrefHeight(40); // Chiều cao cố định cho tên sản phẩm
//         nameLabel.setStyle("-fx-font-weight: bold;");

//         // Hiển thị giá
//         Label priceLabel = new Label("Giá: " + String.format("%,d", (long) product.getPrice()) + "đ");
//         priceLabel.setStyle("-fx-text-fill: #e91e63; -fx-font-weight: bold;");

//         // Hiển thị số lượng
//         Label stockLabel = new Label("Kho: " + product.getQuantity());

//         // Nút thêm vào giỏ
//         Button addButton = new Button("Thêm vào giỏ");
//         addButton.setPrefWidth(Double.MAX_VALUE);
//         addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

//         // Hiệu ứng hover cho nút
//         addButton.setOnMouseEntered(e ->
//                 addButton.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white; -fx-font-weight: bold;")
//         );

//         addButton.setOnMouseExited(e ->
//                 addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;")
//         );

//         // Sự kiện thêm vào giỏ
//         addButton.setOnAction(e -> {
//             try {
//                 // Kiểm tra số lượng tồn kho
//                 if (product.getQuantity() <= 0) {
//                     AlertUtil.showWarning("Hết hàng", "Sản phẩm đã hết hàng!");
//                     return;
//                 }

//                 // Tạo đối tượng CartItemEmployee
//                 CartItemEmployee item = new CartItemEmployee(
//                         product.getProductID(),
//                         product.getProductName(),
//                         product.getPrice(),
//                         1,
//                         product.getImagePath(),
//                         employeeId,
//                         currentUser != null ? currentUser : "unknown",
//                         product.getCategoryID()
//                 );

//                 // Kiểm tra sản phẩm có đủ điều kiện bảo hành thường không
//                 // Nếu có, thêm bảo hành thường mặc định
//                 if (WarrantyCalculator.isEligibleForStandardWarranty(product)) {
//                     Warranty warranty = WarrantyCalculator.createWarranty(product, "Thường");
//                     item.setWarranty(warranty);
//                 }

//                 // Thêm vào giỏ hàng
//                 addToCartWithWarranty(item);

//             } catch (Exception ex) {
//                 LOGGER.log(Level.SEVERE, "Lỗi khi thêm sản phẩm vào giỏ hàng", ex);
//                 AlertUtil.showError("Lỗi", "Không thể thêm sản phẩm vào giỏ hàng");
//             }
//         });

//         // Thêm tất cả vào box
//         VBox imageContainer = new VBox(imageView);
//         imageContainer.setAlignment(Pos.CENTER);

//         box.getChildren().addAll(hugbiuvgiuygv


//sfsfsfwf
//                 imageContainer,
//                 nameLabel,
//                 priceLabel,
//                 stockLabel,
//                 addButton
//         );

//         return box;
//     }
// }