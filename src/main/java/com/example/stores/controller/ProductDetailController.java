package com.example.stores.controller;

import com.example.stores.model.*;
import com.example.stores.service.impl.*;
import com.example.stores.util.AlertUtils;
import com.example.stores.util.ImagePathUtils;
import com.example.stores.util.LanguageManager;

import java.io.InputStream;
import java.net.URL;

import java.util.*;

import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Separator;
import javafx.stage.Popup;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.scene.Node;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ProductDetailController implements Initializable {



    @FXML private ImageView logoIcon;
    @FXML private Label sellerCenterLabel;
    @FXML private Label storeLabel;
    @FXML private Label logoLabel;
    @FXML private Button cartButton;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private BorderPane rootPane;
    @FXML private AnchorPane headerPane;
    @FXML private ImageView productImageView;
    @FXML private Label productNameLabel;
    @FXML private Label productPriceLabel;
    @FXML private Label productCategoryLabel;
    @FXML private Label productDescriptionArea;
    @FXML private Label reviewCountLabel;
    @FXML private Label reviewSummaryCountLabel;
    @FXML private Label ratingLabel;
    @FXML private Label summaryRatingLabel;
    @FXML private HBox ratingStarsBox;
    @FXML private HBox summaryStarsBox;
    @FXML private VBox reviewsContainer;
    @FXML private Button addToCartButton;
    @FXML private Button checkoutButton;
    @FXML private Button backButton;
    @FXML private Button searchButton;
    @FXML private Button menuButton;
    @FXML private TextField searchField;
    @FXML private Label pageTitle;
    @FXML private MenuButton userMenuButton;
    @FXML private MenuItem accountInfoMenuItem;
    @FXML private MenuItem languageSwitchMenuItem;
    @FXML private MenuItem customDesignMenuItem;
    @FXML private MenuItem orderHistoryMenuItem;
    @FXML private MenuItem logoutMenuItem;



    // Thêm biến cho danh mục
    private AnchorPane categoryPane;
    private boolean isCategoryOpen = false;
    private TranslateTransition slideTransition;

    private ProductService productService;
    private ReviewService reviewService;
    private CategoryService categoryService;
    private boolean isVietnamese;
    private Product currentProduct;
    private VBox searchSuggestionsBox;
    private Popup searchPopup;
    private Timer searchTimer;
    private final long SEARCH_DELAY = 300;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productService = new ProductService();
        reviewService = new ReviewService();
        categoryService = new CategoryService();
        isVietnamese = LanguageManager.isVietnamese();

        setupLiveSearch();

        // Setup UI elements based on language
        updateLanguage();

        // Setup event handlers cho các chức năng
        setupEventHandlers();

        // Khởi tạo thanh danh mục (sẽ được tạo động)
        initializeCategoryPanel();

        // Thiết lập xử lý cho các nút header
        setupHeaderFunctionality();

        System.out.println("ProductDetailController đã khởi tạo với ngôn ngữ: " +
                (isVietnamese ? "Tiếng Việt" : "English"));
    }

    // Thêm phương thức mới để thiết lập chức năng header
    private void setupHeaderFunctionality() {
        // Thiết lập sự kiện click cho logo và các nhãn
        if (logoIcon != null) {
            logoIcon.setOnMouseClicked(event -> goBackToHome());
        }

        if (logoLabel != null) {
            logoLabel.setOnMouseClicked(event -> goBackToHome());
        }

        if (storeLabel != null) {
            storeLabel.setOnMouseClicked(event -> goBackToHome());
        }

        if (sellerCenterLabel != null) {
            sellerCenterLabel.setOnMouseClicked(event -> goBackToHome());
        }

        // Thiết lập nút danh mục
        if (menuButton != null) {
            menuButton.setOnAction(event -> toggleCategory());
        }

        // Thiết lập nút tìm kiếm
        if (searchButton != null) {
            searchButton.setOnAction(event -> handleSearch());
        }

        // Thiết lập nút giỏ hàng
        if (cartButton != null) {
            cartButton.setOnAction(event -> goToCart());
        }

        // Thiết lập menu user
        setupUserMenu();
    }

    @FXML
    private void goToCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Cart.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/stores/css/Cart.css").toExternalForm());

            Stage stage = (Stage) cartButton.getScene().getWindow();
            stage.setTitle("CELLCOMP STORE - Giỏ hàng");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể mở giỏ hàng");
        }
    }




    private void setupEventHandlers() {
        // Xử lý nút quay lại
        if (backButton != null) {
            backButton.setOnAction(event -> goBackToHome());
            setCursorForInteractiveElements();
        }

        // Xử lý tìm kiếm
        if (searchButton != null) {
            searchButton.setOnAction(event -> handleSearch());
            setCursorForInteractiveElements();
        }

        // Xử lý Enter trong ô tìm kiếm
        if (searchField != null) {
            searchField.setOnAction(event -> handleSearch());
            setCursorForInteractiveElements();
        }

        // Xử lý nút thêm vào giỏ hàng
        if (addToCartButton != null) {
            addToCartButton.setOnAction(event -> addToCart());
            setCursorForInteractiveElements();
        }

        // Xử lý nút thanh toán
        if (checkoutButton != null) {
            checkoutButton.setOnAction(event -> checkout());
            setCursorForInteractiveElements();
        }

        // Xử lý nút danh mục
        if (menuButton != null) {
            menuButton.setOnAction(event -> toggleCategory());
            setCursorForInteractiveElements();
        }

        // Xử lý menu người dùng
        setupUserMenu();
    }

    /**
     * Set hand cursor for all interactive elements
     */
    private void setCursorForInteractiveElements() {
        // Logo elements
        if (logoIcon != null) logoIcon.setCursor(Cursor.HAND);
        if (logoLabel != null) logoLabel.setCursor(Cursor.HAND);
        if (storeLabel != null) storeLabel.setCursor(Cursor.HAND);
        if (sellerCenterLabel != null) sellerCenterLabel.setCursor(Cursor.HAND);

        // Action buttons
        if (menuButton != null) menuButton.setCursor(Cursor.HAND);
        if (cartButton != null) cartButton.setCursor(Cursor.HAND);
        if (searchButton != null) searchButton.setCursor(Cursor.HAND);
        if (addToCartButton != null) addToCartButton.setCursor(Cursor.HAND);
        if (checkoutButton != null) checkoutButton.setCursor(Cursor.HAND);
        if (userMenuButton != null) userMenuButton.setCursor(Cursor.HAND);

        // Menu items will inherit from CSS
    }

    private void setupUserMenu() {
        // Xử lý chuyển đổi ngôn ngữ
        if (languageSwitchMenuItem != null) {
            languageSwitchMenuItem.setOnAction(event -> switchLanguage());

            // Cập nhật icon ngôn ngữ
            ImageView languageIcon = new ImageView(new Image(getClass().getResourceAsStream(
                    isVietnamese ? "/com/example/stores/images/layout/flag_en.png" : "/com/example/stores/images/layout/flag_vn.png")));
            languageIcon.setFitWidth(16);
            languageIcon.setFitHeight(16);
            languageSwitchMenuItem.setGraphic(languageIcon);
            languageSwitchMenuItem.setText(isVietnamese ? "Switch to English" : "Chuyển sang Tiếng Việt");
        }

        // Xử lý thông tin tài khoản
        if (accountInfoMenuItem != null) {
            accountInfoMenuItem.setOnAction(event -> openAccountInfo());
        }

        // Xử lý thiết kế máy tính
        if (customDesignMenuItem != null) {
            customDesignMenuItem.setOnAction(event -> openPCDesign());
        }

        // Xử lý lịch sử mua hàng
        if (orderHistoryMenuItem != null) {
            orderHistoryMenuItem.setOnAction(event -> openOrderHistory());
        }

        // Xử lý đăng xuất
        if (logoutMenuItem != null) {
            logoutMenuItem.setOnAction(event -> handleLogout());
        }
    }

    private void initializeCategoryPanel() {
        // Tạo AnchorPane cho danh mục - giữ nguyên
        categoryPane = new AnchorPane();
        categoryPane.setPrefWidth(225);
        categoryPane.setMaxWidth(225);
        categoryPane.setMaxHeight(420);
        categoryPane.setStyle("-fx-background-color: linear-gradient(to right, #865DFF, #5CB8E4); " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0.5, 0.0, 0.0); " +
                "-fx-padding: 5px 0px 10px 5px; -fx-background-radius: 0 15 15 0; " +
                "-fx-border-radius: 0 15 15 0;");

        // Nút đóng X - thêm kiểm tra tệp tồn tại
        Button closeCategoryBtn = new Button();
        closeCategoryBtn.setLayoutX(-5.0);
        closeCategoryBtn.setLayoutY(0.0);
        closeCategoryBtn.setStyle("-fx-background-color: transparent; -fx-background-radius: 50%; " +
                "-fx-min-width: 55px; -fx-min-height: 55px; -fx-max-width: 55px; -fx-max-height: 55px; " +
                "-fx-padding: 0 0 0 5px; -fx-cursor: hand;");

        // Kiểm tra tệp hình ảnh X tồn tại
        try {
            InputStream iconStream = getClass().getResourceAsStream("/com/example/stores/images/layout/x.png");
            if (iconStream != null) {
                ImageView closeIcon = new ImageView(new Image(iconStream));
                closeIcon.setFitHeight(50);
                closeIcon.setFitWidth(50);
                closeCategoryBtn.setGraphic(closeIcon);
            } else {
                // Nếu không tìm thấy hình ảnh, sử dụng text "X" thay thế
                Label xLabel = new Label("X");
                xLabel.setStyle("-fx-font-size: 30px; -fx-text-fill: white; -fx-font-weight: bold;");
                closeCategoryBtn.setGraphic(xLabel);
                System.err.println("Không tìm thấy hình ảnh nút đóng: /com/example/stores/images/layout/x.png");
            }
        } catch (Exception e) {
            // Xử lý ngoại lệ khi tải hình ảnh thất bại
            Label xLabel = new Label("X");
            xLabel.setStyle("-fx-font-size: 30px; -fx-text-fill: white; -fx-font-weight: bold;");
            closeCategoryBtn.setGraphic(xLabel);
            System.err.println("Lỗi khi tải hình ảnh nút đóng: " + e.getMessage());
        }

        closeCategoryBtn.setOnAction(event -> toggleCategory());

        // Tạo ScrollPane cho danh mục - Cập nhật style
        ScrollPane categoryScrollPane = new ScrollPane();
        categoryScrollPane.setLayoutY(55.0);
        categoryScrollPane.setPrefHeight(335);
        categoryScrollPane.setPrefWidth(225);
        categoryScrollPane.setFitToWidth(true);
        categoryScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        categoryScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        // QUAN TRỌNG: Thêm class CSS để phù hợp với selector trong CSS
        categoryScrollPane.getStyleClass().add("category-scroll-pane");

        // Thêm các style inline trực tiếp để đảm bảo các phần nền hiển thị đúng
        categoryScrollPane.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-background: transparent; " +
                        "-fx-border-color: transparent; " +
                        "-fx-padding: 0 0 2 0; " +
                        "-fx-background-insets: 0; " +
                        "-fx-border-width: 0; " +
                        "-fx-effect: null;" +
                        "-fx-control-inner-background: transparent;" +
                        "-fx-viewport-background: transparent;");

        // LƯU Ý: LOẠI BỎ hoàn toàn cách cũ thêm style qua data URL
        // KHÔNG sử dụng Platform.runLater() với data URL stylesheet

        // Phần còn lại của phương thức giữ nguyên
        VBox categoryListContainer = new VBox(4);
        categoryListContainer.setPrefHeight(410);
        categoryListContainer.setPrefWidth(205);
        categoryListContainer.setAlignment(Pos.TOP_CENTER);
        categoryListContainer.setStyle("-fx-padding: 0 0 2 0; -fx-background-color: transparent;");

        try {
            List<Category> categories = categoryService.getAllCategories();

            for (Category category : categories) {
                Button categoryBtn = createCategoryButton(category.getCategoryName());
                categoryBtn.setId("category-" + category.getCategoryName().toLowerCase().replace(" ", "-"));
                categoryBtn.setOnAction(event -> filterByCategory(category.getCategoryName()));
                categoryListContainer.getChildren().add(categoryBtn);
            }
        } catch (Exception e) {
            System.err.println("Không thể tải danh mục: " + e.getMessage());
            e.printStackTrace();
        }

        categoryScrollPane.setContent(categoryListContainer);
        categoryPane.getChildren().addAll(closeCategoryBtn, categoryScrollPane);
        categoryPane.setTranslateX(-225);
        categoryPane.setTranslateY(65);
        categoryPane.setVisible(false);
        slideTransition = new TranslateTransition(Duration.millis(250), categoryPane);
        rootPane.getChildren().add(categoryPane);
        StackPane.setAlignment(categoryPane, Pos.TOP_LEFT);
    }

    private Button createCategoryButton(String categoryName) {
        Button button = new Button(categoryName);

        // Áp dụng style giống như trong Home.css
        button.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 10; " +
                "-fx-padding: 6 5 6 15; -fx-min-width: 180; -fx-alignment: center-left; " +
                "-fx-cursor: hand; -fx-border-color: black; -fx-border-width: 1; -fx-border-radius: 10;");

        // Thêm hiệu ứng hover
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: linear-gradient(to right, #7B68EE, #48D1CC); " +
                "-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 10; " +
                "-fx-padding: 6 5 6 15; -fx-min-width: 180; -fx-alignment: center-left; -fx-cursor: hand; " +
                "-fx-border-color: white; -fx-border-width: 1.5; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.5), 6, 0, 0, 0);"));

        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 10; " +
                "-fx-padding: 6 5 6 15; -fx-min-width: 180; -fx-alignment: center-left; " +
                "-fx-cursor: hand; -fx-border-color: black; -fx-border-width: 1; -fx-border-radius: 10;"));

        button.setCursor(Cursor.HAND);

        return button;
    }

    // Cập nhật phương thức toggleCategory để hiển thị đúng với danh mục từ Home
    @FXML
    public void toggleCategory() {
        if (categoryPane == null || slideTransition == null) return;

        // Dừng animation hiện tại nếu đang chạy
        slideTransition.stop();

        // Hiển thị danh mục nếu đang ẩn
        if (!categoryPane.isVisible()) {
            categoryPane.setVisible(true);
        }

        // Xử lý animation mở/đóng
        if (isCategoryOpen) {
            // Đóng danh mục
            slideTransition.setToX(-225);
        } else {
            // Mở danh mục
            slideTransition.setToX(30);
        }

        // Chạy animation
        slideTransition.play();

        // Đảo trạng thái
        isCategoryOpen = !isCategoryOpen;
    }


    private void filterByCategory(String categoryName) {
        try {
            // Lưu trạng thái ngôn ngữ
            LanguageManager.setVietnamese(isVietnamese);

            // Đặt tên danh mục vào biến static để Home xử lý sau
            HomeController.setPendingCategoryFilter(categoryName);

            // Tải giao diện Home
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Home.fxml"));
            Parent root = loader.load();

            // Thiết lập scene mới
            Scene scene = new Scene(root);
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("CELLCOMP STORE");

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể lọc theo danh mục" : "Cannot filter by category"
            );
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) return;

        try {
            // Lưu trạng thái ngôn ngữ
            LanguageManager.setVietnamese(isVietnamese);

            // Đặt từ khóa tìm kiếm vào biến static để Home xử lý sau
            HomeController.setPendingSearchKeyword(keyword);

            // Tải giao diện Home
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Home.fxml"));
            Parent root = loader.load();

            // Thiết lập scene mới
            Scene scene = new Scene(root);
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("CELLCOMP STORE");

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể tìm kiếm sản phẩm" : "Could not search for products"
            );
        }
    }


    @FXML
    private void switchLanguage() {
        // Đảo trạng thái ngôn ngữ
        isVietnamese = !isVietnamese;

        // Lưu trạng thái vào LanguageManager để dùng chung
        LanguageManager.setVietnamese(isVietnamese);
        System.out.println("ProductDetailController đã thay đổi ngôn ngữ thành: " + (isVietnamese ? "Tiếng Việt" : "English"));

        // Cập nhật hình ảnh lá cờ và text dựa trên ngôn ngữ hiện tại
        if (languageSwitchMenuItem != null) {
            try {
                // Đường dẫn tới hình ảnh cờ - Hiển thị cờ ngược với ngôn ngữ hiện tại
                String flagPath = isVietnamese
                        ? "/com/example/stores/images/layout/flag_en.png"  // Đang ở tiếng Việt, hiển thị cờ Anh
                        : "/com/example/stores/images/layout/flag_vn.png"; // Đang ở tiếng Anh, hiển thị cờ Việt

                Image flagImage = new Image(getClass().getResourceAsStream(flagPath));

                // Cập nhật ImageView trong MenuItem
                ImageView flagImageView = (ImageView) languageSwitchMenuItem.getGraphic();
                if (flagImageView != null) {
                    flagImageView.setImage(flagImage);
                }

                // Cập nhật text cho menu item
                languageSwitchMenuItem.setText(isVietnamese
                        ? "Switch to English"     // Đang ở tiếng Việt
                        : "Chuyển sang tiếng Việt"); // Đang ở tiếng Anh
            } catch (Exception e) {
                System.err.println("Lỗi khi cập nhật hình ảnh cờ: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Cập nhật giao diện người dùng
        updateLanguage();

        // Cập nhật nội dung sản phẩm
        if (currentProduct != null) {
            displayProductDetails();
            loadProductReviews(currentProduct.getProductID());
        }

        // Cập nhật nội dung category nếu có
        if (categoryPane != null && categoryPane.isVisible()) {
            // Cập nhật danh mục
            initializeCategoryPanel();
        }
    }
    @FXML
    private void openAccountInfo() {
        try {
            // Lưu trạng thái ngôn ngữ
            LanguageManager.setVietnamese(isVietnamese);

            // Lấy Stage hiện tại
            Stage currentStage = (Stage) rootPane.getScene().getWindow();

            // Lưu kích thước và vị trí hiện tại
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();

            // Tải giao diện CustomerChange
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomerChange.fxml"));
            Parent root = loader.load();

            // Thiết lập scene mới
            Scene scene = new Scene(root);

            // Thiết lập tiêu đề
            currentStage.setTitle(isVietnamese ? "CELLCOMP STORE - Thông tin tài khoản" :
                    "CELLCOMP STORE - Account Information");

            // Đặt scene mới
            currentStage.setScene(scene);

            // Giữ nguyên kích thước và vị trí
            currentStage.setWidth(width);
            currentStage.setHeight(height);
            currentStage.setX(x);
            currentStage.setY(y);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở giao diện thông tin tài khoản" : "Cannot open account information screen"
            );
        }
    }
    @FXML
    private void openPCDesign() {
        try {
            // Save language setting
            LanguageManager.setVietnamese(isVietnamese);

            // Get current stage
            Stage currentStage = (Stage) rootPane.getScene().getWindow();

            // Save current window size and position
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();

            // Load the CustomPCBuilder interface
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomPCBuilder.fxml"));
            Parent root = loader.load();

            // Create and set new scene
            Scene scene = new Scene(root);

            // Set title
            currentStage.setTitle(isVietnamese ? "CELLCOMP STORE - Thiết kế máy tính" :
                    "CELLCOMP STORE - PC Builder");

            // Set new scene
            currentStage.setScene(scene);

            // Maintain window size and position
            currentStage.setWidth(width);
            currentStage.setHeight(height);
            currentStage.setX(x);
            currentStage.setY(y);

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở giao diện thiết kế máy tính" :
                            "Cannot open PC Builder interface"
            );
        }
    }

    @FXML
    private void openOrderHistory() {
        try {
            // Save language setting
            LanguageManager.setVietnamese(isVietnamese);

            // Get current stage
            Stage currentStage = (Stage) rootPane.getScene().getWindow();

            // Save current window size and position
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();

            // Load the OrderHistory interface
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/OrderHistory.fxml"));
            Parent root = loader.load();

            // Create and set new scene
            Scene scene = new Scene(root);

            // Set title
            currentStage.setTitle(isVietnamese ? "CELLCOMP STORE - Lịch sử mua hàng" :
                    "CELLCOMP STORE - Order History");

            // Set new scene
            currentStage.setScene(scene);

            // Maintain window size and position
            currentStage.setWidth(width);
            currentStage.setHeight(height);
            currentStage.setX(x);
            currentStage.setY(y);

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở giao diện lịch sử mua hàng" :
                            "Cannot open Order History interface"
            );
        }
    }
    @FXML
    private void handleLogout() {
        try {
            // Tải giao diện đăng nhập
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomerLogin.fxml"));
            Parent root = loader.load();

            // Thiết lập scene mới
            Scene scene = new Scene(root);
            Stage stage = (Stage) rootPane.getScene().getWindow();

            // Đặt scene mới
            stage.setScene(scene);
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Đăng nhập" : "CELLCOMP STORE - Login");
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể đăng xuất" : "Cannot logout"
            );
        }
    }

    public void loadProductDetails(String productId) {
        try {
            currentProduct = productService.getProductById(productId);

            if (currentProduct != null) {
                displayProductDetails();
                loadProductReviews(productId);
            } else {
                AlertUtils.showError(
                        isVietnamese ? "Lỗi" : "Error",
                        isVietnamese ? "Không tìm thấy sản phẩm" : "Product not found"
                );
                goBackToHome();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể tải thông tin sản phẩm" : "Could not load product details"
            );
            goBackToHome();
        }
    }

    // Trong phương thức displayProductDetails()
    private void displayProductDetails() {
        if (currentProduct == null) return;

        // Set product details to UI components
        if (productNameLabel != null) productNameLabel.setText(currentProduct.getProductName());
        if (productPriceLabel != null) productPriceLabel.setText(String.format("%,.0f₫", currentProduct.getPrice()));
        if (productCategoryLabel != null) productCategoryLabel.setText(currentProduct.getCategoryName());
        if (productDescriptionArea != null) productDescriptionArea.setText(currentProduct.getDescription());

        // Load product image
        if (productImageView != null) {
            try {
                String imagePath = currentProduct.getImagePath();

                if (imagePath != null && !imagePath.isEmpty()) {
                    // Sử dụng ImagePathUtils để chuẩn hóa đường dẫn
                    String normalizedPath = ImagePathUtils.normalizeImagePath(imagePath);

                    try {
                        Image image = new Image(getClass().getResourceAsStream(normalizedPath));

                        if (image.isError()) {
                            // Nếu không tìm thấy, thử với đường dẫn dự phòng
                            String fallbackPath = ImagePathUtils.getFallbackPath(imagePath);
                            Image fallbackImage = new Image(getClass().getResourceAsStream(fallbackPath));

                            if (!fallbackImage.isError()) {
                                productImageView.setImage(fallbackImage);
                                System.out.println("Đã tải hình ảnh từ đường dẫn dự phòng: " + fallbackPath);
                            } else {
                                // Nếu vẫn lỗi, hiển thị ảnh mặc định
                                productImageView.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/default_product.png")));
                                System.out.println("Không tìm thấy hình ảnh, sử dụng ảnh mặc định");
                            }
                        } else {
                            productImageView.setImage(image);
                            System.out.println("Đã tải hình ảnh từ đường dẫn: " + normalizedPath);
                        }
                    } catch (Exception e) {
                        productImageView.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/default_product.png")));
                        System.err.println("Lỗi khi tải hình ảnh: " + normalizedPath + " - " + e.getMessage());
                    }
                } else {
                    productImageView.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/default_product.png")));
                }
            } catch (Exception e) {
                System.err.println("Lỗi hiển thị chi tiết sản phẩm: " + e.getMessage());
                productImageView.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/default_product.png")));
            }
        }
    }

    private void loadProductReviews(String productId) {
        try {
            // Lấy danh sách đánh giá từ database thông qua ReviewService
            List<ProductReview> reviews = reviewService.getReviewsByProductId(productId);
            int reviewCount = reviews != null ? reviews.size() : 0;

            // Log số lượng đánh giá để debug
            System.out.println("Loaded " + reviewCount + " reviews for product " + productId);

            // Cập nhật CẢ HAI label hiển thị số đánh giá
            String countText = "(" + reviewCount + " " + (isVietnamese ? "đánh giá" : "reviews") + ")";

            if (reviewCountLabel != null) {
                reviewCountLabel.setText(countText);
            }

            if (reviewSummaryCountLabel != null) {
                reviewSummaryCountLabel.setText(countText);
            }

            // Cập nhật container chứa đánh giá
            if (reviewsContainer != null) {
                reviewsContainer.getChildren().clear();

                if (reviews != null && !reviews.isEmpty()) {
                    // Hiển thị từng đánh giá nếu có
                    for (ProductReview review : reviews) {
                        reviewsContainer.getChildren().add(createReviewItem(review));
                    }

                    // Cập nhật điểm đánh giá trung bình
                    updateAverageRating(reviews);
                } else {
                    // Hiển thị thông báo khi không có đánh giá
                    Label noReviewsLabel = new Label(isVietnamese ?
                            "Chưa có đánh giá nào cho sản phẩm này" :
                            "No reviews for this product yet");
                    noReviewsLabel.getStyleClass().add("no-reviews-label");

                    reviewsContainer.getChildren().add(noReviewsLabel);

                    // Đặt đánh giá mặc định là 5 sao khi chưa có đánh giá
                    setDefaultRating();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading reviews: " + e.getMessage());

            // Hiển thị lỗi trong UI
            if (reviewsContainer != null) {
                reviewsContainer.getChildren().clear();

                Label errorLabel = new Label(isVietnamese ?
                        "Không thể tải đánh giá sản phẩm" :
                        "Could not load product reviews");
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px; -fx-padding: 10px;");

                reviewsContainer.getChildren().add(errorLabel);
            }
        }
    }

    // Đặt đánh giá mặc định là 5 sao khi không có đánh giá
    private void setDefaultRating() {
        // Cập nhật điểm số đánh giá
        if (ratingLabel != null) {
            ratingLabel.setText("5");
        }

        if (summaryRatingLabel != null) {
            summaryRatingLabel.setText("5");
        }

        // Cập nhật sao đánh giá ở phần trên
        if (ratingStarsBox != null) {
            ratingStarsBox.getChildren().clear();
            for (int i = 0; i < 5; i++) {
                ImageView star = new ImageView(new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/star.png")));
                star.setFitHeight(20);
                star.setFitWidth(20);
                ratingStarsBox.getChildren().add(star);
            }
        }

        // Cập nhật sao đánh giá ở phần tổng hợp
        if (summaryStarsBox != null) {
            summaryStarsBox.getChildren().clear();
            for (int i = 0; i < 5; i++) {
                ImageView star = new ImageView(new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/star.png")));
                star.setFitHeight(20);
                star.setFitWidth(20);
                summaryStarsBox.getChildren().add(star);
            }
        }
    }

    private VBox createReviewItem(ProductReview review) {
        // Create reviewer info
        VBox reviewItem = new VBox();
        reviewItem.getStyleClass().add("review-item");

        HBox reviewerInfo = new HBox(10);
        reviewerInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // First letter of customer name as avatar
        Label nameLabel = new Label(review.getCustomerName().substring(0, 1));
        nameLabel.getStyleClass().add("reviewer-name");

        // Customer info
        VBox customerInfo = new VBox();
        Label customerNameLabel = new Label(maskCustomerName(review.getCustomerName()));
        Label reviewDateLabel = new Label(formatDate(review.getReviewDate()));
        reviewDateLabel.getStyleClass().add("review-date");
        customerInfo.getChildren().addAll(customerNameLabel, reviewDateLabel);

        // Verified purchase
        Label verifiedLabel = new Label("✓ " + (isVietnamese ? "Đã mua hàng" : "Verified purchase"));
        verifiedLabel.getStyleClass().add("verified-purchase");

        reviewerInfo.getChildren().addAll(nameLabel, customerInfo, verifiedLabel);

        // Create rating stars and comment
        HBox ratingBox = new HBox(2);
        ratingBox.getStyleClass().add("review-rating");

        // Add stars based on rating
        for (int i = 0; i < review.getRating(); i++) {
            ImageView star = new ImageView(new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/star.png")));
            star.setFitHeight(20);
            star.setFitWidth(20);
            ratingBox.getChildren().add(star);
        }

        // Add comment
        Label commentLabel = new Label(review.getComment());
        commentLabel.getStyleClass().add("review-comment");
        ratingBox.getChildren().add(commentLabel);

        reviewItem.getChildren().addAll(reviewerInfo, ratingBox);
        reviewItem.setCursor(Cursor.HAND);
        return reviewItem;
    }

    private void updateAverageRating(List<ProductReview> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            setDefaultRating();
            return;
        }

        // Calculate average rating
        double totalRating = 0;
        for (ProductReview review : reviews) {
            totalRating += review.getRating();
        }

        double avgRating = totalRating / reviews.size();
        int displayedStars = roundStarsDisplay(avgRating);

        // Cập nhật điểm số đánh giá
        if (ratingLabel != null) {
            ratingLabel.setText(String.format("%.1f", avgRating));
        }

        if (summaryRatingLabel != null) {
            summaryRatingLabel.setText(String.format("%.1f", avgRating));
        }

        // Cập nhật sao đánh giá ở phần trên
        if (ratingStarsBox != null) {
            ratingStarsBox.getChildren().clear();
            for (int i = 0; i < displayedStars; i++) {
                ImageView star = new ImageView(new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/star.png")));
                star.setFitHeight(20);
                star.setFitWidth(20);
                ratingStarsBox.getChildren().add(star);
            }
        }

        // Cập nhật sao đánh giá ở phần tổng hợp
        if (summaryStarsBox != null) {
            summaryStarsBox.getChildren().clear();
            for (int i = 0; i < displayedStars; i++) {
                ImageView star = new ImageView(new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/star.png")));
                star.setFitHeight(20);
                star.setFitWidth(20);
                summaryStarsBox.getChildren().add(star);
            }
        }
    }

    // Round stars display according to the rules
    private int roundStarsDisplay(double rating) {
        // Rule: 1.5-2.0 -> 2 stars, 1.0-1.4 -> 1 star, etc.
        double fractionalPart = rating - Math.floor(rating);
        if (fractionalPart >= 0.5) {
            return (int) Math.ceil(rating);
        } else {
            return (int) Math.floor(rating);
        }
    }

    private String maskCustomerName(String name) {
        if (name == null || name.length() <= 2) return name;

        return name.substring(0, 2) + "*".repeat(Math.min(6, name.length() - 2)) +
                (name.length() > 8 ? name.substring(name.length() - 2) : "");
    }

    private String formatDate(java.time.LocalDateTime date) {
        if (date == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return date.format(formatter);
    }

    private void updateLanguage() {
        // Cập nhật các phần tử UI đơn giản
        if (backButton != null) backButton.setText(isVietnamese ? "Quay lại" : "Back");
        if (pageTitle != null) pageTitle.setText(isVietnamese ? "Thông tin chi tiết sản phẩm" : "Product details");
        if (addToCartButton != null) {
            // Cập nhật cả text của button
            addToCartButton.setText(isVietnamese ? "Thêm vào giỏ hàng" : "Add to cart");
        }
        if (checkoutButton != null) checkoutButton.setText(isVietnamese ? "Đi đến thanh toán" : "Checkout");
        if (searchField != null) searchField.setPromptText(isVietnamese ? "Tìm kiếm..." : "Search...");

        // CẬP NHẬT HEADER
        // 1. Label trong menuButton (Danh mục)
        if (menuButton != null && menuButton.getGraphic() instanceof HBox) {
            HBox menuHBox = (HBox) menuButton.getGraphic();
            for (Node node : menuHBox.getChildren()) {
                if (node instanceof Label) {
                    ((Label) node).setText(isVietnamese ? "Danh mục" : "Categories");
                    break;
                }
            }
        }

        // 2. Label trong cartButton (Giỏ hàng)
        if (cartButton != null && cartButton.getGraphic() instanceof HBox) {
            HBox cartHBox = (HBox) cartButton.getGraphic();
            for (Node node : cartHBox.getChildren()) {
                if (node instanceof Label) {
                    ((Label) node).setText(isVietnamese ? "Giỏ hàng" : "Cart");
                    break;
                }
            }
        }


        // 5. Cập nhật các MenuItem trong menu người dùng
        if (accountInfoMenuItem != null) accountInfoMenuItem.setText(isVietnamese ? "Thông tin tài khoản" : "Account information");
        if (customDesignMenuItem != null) customDesignMenuItem.setText(isVietnamese ? "Thiết kế máy tính theo ý bạn" : "Custom PC design");
        if (orderHistoryMenuItem != null) orderHistoryMenuItem.setText(isVietnamese ? "Lịch sử mua hàng" : "Order history");
        if (logoutMenuItem != null) logoutMenuItem.setText(isVietnamese ? "Đăng xuất" : "Logout");
        if (languageSwitchMenuItem != null) {
            languageSwitchMenuItem.setText(isVietnamese ? "Switch to English" : "Chuyển sang tiếng Việt");

            // Cập nhật hình ảnh cờ cho menu item ngôn ngữ
            try {
                ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(
                        isVietnamese ? "/com/example/stores/images/layout/flag_en.png" : "/com/example/stores/images/layout/flag_vn.png"
                )));
                imageView.setFitHeight(16);
                imageView.setFitWidth(16);
                languageSwitchMenuItem.setGraphic(imageView);
            } catch (Exception e) {
                System.err.println("Không thể tải hình ảnh cờ: " + e.getMessage());
            }
        }

        // 6. Cập nhật các label trong phần mô tả sản phẩm
        for (Node node : rootPane.lookupAll("Label")) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if ("Danh mục:".equals(label.getText())) {
                    label.setText(isVietnamese ? "Danh mục:" : "Category:");
                } else if ("Mô tả:".equals(label.getText())) {
                    label.setText(isVietnamese ? "Mô tả:" : "Description:");
                } else if ("Đánh giá & Nhận xét".equals(label.getText())) {
                    label.setText(isVietnamese ? "Đánh giá & Nhận xét" : "Reviews & Comments");
                }
            }
        }

        // 7. Cập nhật review section if product is loaded
        if (currentProduct != null) {
            loadProductReviews(currentProduct.getProductID());
        }
    }

    private void updateLabelsByText(Parent parent, String viText, String enText) {
        if (parent == null) return;

        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if (viText.equals(label.getText()) || enText.equals(label.getText())) {
                    label.setText(isVietnamese ? viText : enText);
                }
            } else if (node instanceof Parent) {
                updateLabelsByText((Parent) node, viText, enText);
            }
        }
    }

    // Phương thức để quay về trang chủ
    @FXML
    private void goBackToHome() {
        try {
            // Lưu trạng thái ngôn ngữ
            LanguageManager.setVietnamese(isVietnamese);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Home.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(isVietnamese ? "CELLCOMP STORE" : "CELLCOMP STORE");
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể quay lại trang chủ" : "Could not return to home page"
            );
        }
    }


    // Thêm phương thức sau vào ProductDetailController

    @FXML
    private void addToCart() {
        if (currentProduct == null) return;

        try {
            // Kiểm tra đăng nhập
            Customer currentCustomer = AuthService.getCurrentCustomer();
            if (currentCustomer == null) {
                boolean goToLogin = AlertUtils.showConfirm(
                        isVietnamese ? "Yêu cầu đăng nhập" : "Login required",
                        isVietnamese ? "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng" : "Please login to add products to cart",
                        isVietnamese ? "Bạn có muốn chuyển đến trang đăng nhập không?" : "Would you like to go to login page?"
                );

                if (goToLogin) {
                    navigateToLogin();
                }
                return;
            }

            // Khởi tạo service
            CartService cartService = new CartService();

            // Lấy hoặc tạo giỏ hàng cho người dùng
            Cart cart = cartService.getOrCreateCart(currentCustomer);
            if (cart == null) {
                AlertUtils.showError(
                        isVietnamese ? "Lỗi" : "Error",
                        isVietnamese ? "Không thể tạo giỏ hàng" : "Could not create cart"
                );
                return;
            }

            // Lấy số lượng tồn kho hiện có
            int availableStock = currentProduct.getQuantity();

            // Kiểm tra số lượng đã có trong giỏ hàng
            CartItem existingItem = cartService.findCartItemByProductId(cart.getCartId(), currentProduct.getProductID());
            int currentCartQty = existingItem != null ? existingItem.getQuantity() : 0;
            int maxAdditionalQty = availableStock - currentCartQty;

            if (maxAdditionalQty <= 0) {
                AlertUtils.showWarning(
                        isVietnamese ? "Giới hạn số lượng" : "Quantity limit",
                        isVietnamese ?
                                "Bạn đã có tối đa " + availableStock + " sản phẩm " + currentProduct.getProductName() + " trong giỏ hàng" :
                                "You already have the maximum quantity of " + availableStock + " " + currentProduct.getProductName() + " in your cart"
                );
                return;
            }

            // Hiển thị dialog chọn số lượng với số lượng tối đa là tồn kho - số lượng trong giỏ
            Dialog<Integer> dialog = new Dialog<>();
            dialog.setTitle(isVietnamese ? "Chọn số lượng" : "Select quantity");
            dialog.setHeaderText(isVietnamese ?
                    "Chọn số lượng sản phẩm muốn thêm vào giỏ hàng (Tối đa: " + maxAdditionalQty + ")" :
                    "Select product quantity to add to cart (Maximum: " + maxAdditionalQty + ")");

            ButtonType confirmButtonType = new ButtonType(isVietnamese ? "Thêm vào giỏ hàng" : "Add to cart", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

            // Giới hạn spinner theo số lượng có thể thêm vào giỏ
            Spinner<Integer> spinner = new Spinner<>(1, maxAdditionalQty, 1);
            spinner.setEditable(true);

            // Xử lý khi người dùng nhập giá trị không hợp lệ
            spinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
                try {
                    int value = Integer.parseInt(newValue);
                    if (value > maxAdditionalQty) {
                        Platform.runLater(() -> {
                            spinner.getEditor().setText(String.valueOf(maxAdditionalQty));
                            AlertUtils.showWarning(
                                    isVietnamese ? "Giới hạn số lượng" : "Quantity limit",
                                    isVietnamese ?
                                            "Sản phẩm " + currentProduct.getProductName() + " chỉ còn " + maxAdditionalQty + " sản phẩm có thể thêm vào giỏ hàng" :
                                            "Only " + maxAdditionalQty + " " + currentProduct.getProductName() + " available to add to cart"
                            );
                        });
                    } else if (value < 1) {
                        Platform.runLater(() -> spinner.getEditor().setText("1"));
                    }
                } catch (NumberFormatException e) {
                    Platform.runLater(() -> spinner.getEditor().setText("1"));
                }
            });

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            grid.add(new Label(isVietnamese ? "Số lượng:" : "Quantity:"), 0, 0);
            grid.add(spinner, 1, 0);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmButtonType) {
                    return spinner.getValue();
                }
                return null;
            });

            Optional<Integer> result = dialog.showAndWait();
            result.ifPresent(quantity -> {
                // Kiểm tra lại một lần nữa để đảm bảo an toàn
                if (quantity > maxAdditionalQty) {
                    quantity = maxAdditionalQty;
                    AlertUtils.showWarning(
                            isVietnamese ? "Giới hạn số lượng" : "Quantity limit",
                            isVietnamese ?
                                    "Đã điều chỉnh số lượng xuống mức tối đa có thể thêm: " + maxAdditionalQty :
                                    "Quantity adjusted to maximum available: " + maxAdditionalQty
                    );
                }

                // Thêm sản phẩm vào giỏ hàng với số lượng hợp lệ - đây là phần cần sửa
                CartService.QuantityCheckResult addResult = cartService.addToCart(
                        cart.getCartId(), currentProduct.getProductID(), quantity
                );

                if (addResult.isSuccess()) {
                    AlertUtils.showInfo(
                            isVietnamese ? "Thành công" : "Success",
                            isVietnamese ?
                                    "Đã thêm " + quantity + " " + currentProduct.getProductName() + " vào giỏ hàng" :
                                    "Added " + quantity + " " + currentProduct.getProductName() + " to cart"
                    );
                } else if (!addResult.getMessage().isEmpty()) {
                    // Hiển thị thông báo giới hạn số lượng nếu vượt quá tồn kho
                    AlertUtils.showWarning(
                            isVietnamese ? "Giới hạn số lượng" : "Quantity Limit",
                            addResult.getMessage()
                    );

                    // Nếu vẫn có thể thêm một số lượng vào giỏ hàng (ít hơn yêu cầu)
                    if (addResult.getAdjustedQuantity() > 0) {
                        AlertUtils.showInfo(
                                isVietnamese ? "Thông báo" : "Notification",
                                isVietnamese ?
                                        "Đã thêm " + addResult.getAdjustedQuantity() + " " + currentProduct.getProductName() + " vào giỏ hàng" :
                                        "Added " + addResult.getAdjustedQuantity() + " " + currentProduct.getProductName() + " to cart"
                        );
                    }
                } else {
                    AlertUtils.showError(
                            isVietnamese ? "Lỗi" : "Error",
                            isVietnamese ? "Không thể thêm sản phẩm vào giỏ hàng" : "Failed to add product to cart"
                    );
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể thêm sản phẩm vào giỏ hàng" : "Could not add product to cart"
            );
        }
    }


    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomerLogin.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = (Stage) addToCartButton.getScene().getWindow();
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Đăng nhập" : "CELLCOMP STORE - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể chuyển đến trang đăng nhập" : "Cannot navigate to login page"
            );
        }
    }

    @FXML
    private void checkout() {
        if (currentProduct == null) {
            return;
        }

        // Tạo dialog để người dùng chọn số lượng
        Dialog<Integer> quantityDialog = new Dialog<>();
        quantityDialog.setTitle(isVietnamese ? "Chọn số lượng" : "Select quantity");
        quantityDialog.setHeaderText(isVietnamese ? "Vui lòng chọn số lượng cho sản phẩm:" : "Please select quantity for the product:");

        // Thêm icon sản phẩm nếu có
        try {
            if (productImageView != null && productImageView.getImage() != null) {
                ImageView dialogImageView = new ImageView(productImageView.getImage());
                dialogImageView.setFitHeight(50);
                dialogImageView.setFitWidth(50);
                dialogImageView.setPreserveRatio(true);
                quantityDialog.setGraphic(dialogImageView);
            }
        } catch (Exception e) {
            // Bỏ qua lỗi nếu không thể thêm hình ảnh
        }

        // Tạo label hiển thị tên sản phẩm
        Label productLabel = new Label(currentProduct.getProductName());
        productLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Tạo spinner để chọn số lượng
        Spinner<Integer> quantitySpinner = new Spinner<>(1, 99, 1);
        quantitySpinner.setEditable(true);
        quantitySpinner.setPrefWidth(100);

        // Tạo layout cho dialog
        VBox content = new VBox(10);
        content.getChildren().addAll(
                productLabel,
                new HBox(10,
                        new Label(isVietnamese ? "Số lượng:" : "Quantity:"),
                        quantitySpinner
                )
        );
        content.setPadding(new Insets(20, 10, 10, 10));

        quantityDialog.getDialogPane().setContent(content);

        // Thêm các nút
        ButtonType confirmButtonType = new ButtonType(
                isVietnamese ? "Đi đến thanh toán" : "Proceed to checkout",
                ButtonBar.ButtonData.OK_DONE
        );
        ButtonType cancelButtonType = ButtonType.CANCEL;

        quantityDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);

        // Phương thức chuyển đổi kết quả
        quantityDialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return quantitySpinner.getValue();
            }
            return null;
        });

        // Hiển thị dialog và xử lý kết quả
        Optional<Integer> result = quantityDialog.showAndWait();

        result.ifPresent(quantity -> {
            proceedToCheckout(currentProduct, quantity);
        });
    }

    /**
     * Tiến hành chuyển đến trang thanh toán với sản phẩm đã chọn
     * @param product Sản phẩm sẽ thanh toán
     * @param quantity Số lượng sản phẩm
     */
    private void proceedToCheckout(Product product, int quantity) {
        try {
            // Tạo CartItem tạm thời cho sản phẩm hiện tại
            CartItem tempCartItem = new CartItem();
            tempCartItem.setCartItemId(-1); // ID tạm thời
            tempCartItem.setProduct(product);
            tempCartItem.setQuantity(quantity);

            // Lưu trạng thái ngôn ngữ
            LanguageManager.setVietnamese(isVietnamese);

            // Tải giao diện Orders
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Orders.fxml"));
            Parent root = loader.load();

            // Lấy controller và truyền dữ liệu
            OrderController orderController = loader.getController();
            orderController.setCartItems(Collections.singletonList(tempCartItem));

            // Hiển thị giao diện Orders
            Scene scene = new Scene(root);
            Stage stage = (Stage) productNameLabel.getScene().getWindow();
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Đặt hàng" : "CELLCOMP STORE - Order");
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ?
                            "Không thể chuyển đến trang thanh toán: " + e.getMessage() :
                            "Cannot navigate to checkout page: " + e.getMessage()
            );
        }
    }

    private void setupLiveSearch() {
    searchPopup = new Popup();
    searchPopup.setAutoHide(true);

    searchSuggestionsBox = new VBox(2);
    searchSuggestionsBox.setStyle(
        "-fx-background-color: white;" +
        "-fx-border-color: #cccccc;" +
        "-fx-border-width: 1px;" +
        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 1);" +
        "-fx-padding: 5px;" +
        "-fx-max-height: 300px;" +
        "-fx-pref-width: 250px;" +
        "-fx-background-radius: 12px;" +
        "-fx-border-radius: 12px;"
    );
    searchPopup.getContent().add(searchSuggestionsBox);

    if (searchField != null) {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (searchTimer != null) searchTimer.cancel();
            if (newValue == null || newValue.trim().isEmpty()) {
                searchPopup.hide();
                return;
            }
            searchTimer = new Timer();
            searchTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> performLiveSearch(newValue.trim()));
                }
            }, SEARCH_DELAY);
        });

        searchField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !searchField.getText().trim().isEmpty()) {
                performLiveSearch(searchField.getText().trim());
            } else if (!newValue) {
                PauseTransition delay = new PauseTransition(Duration.millis(200));
                delay.setOnFinished(e -> {
                    if (!searchSuggestionsBox.isHover()) {
                        searchPopup.hide();
                    }
                });
                delay.play();
            }
        });
    }
}

    private void performLiveSearch(String keyword) {
        try {
            if (keyword == null || keyword.isEmpty() || keyword.length() < 2) {
                searchPopup.hide();
                return;
            }

            // Tìm kiếm sản phẩm
            List<Product> searchResults = ProductService.searchProductsLive(keyword);
            searchSuggestionsBox.getChildren().clear();

            if (searchResults.isEmpty()) {
                Label noResultLabel = new Label(isVietnamese ?
                        "Không tìm thấy sản phẩm nào" :
                        "No products found");
                noResultLabel.setStyle("-fx-text-fill: #666666; -fx-padding: 5px; -fx-font-style: italic;");
                searchSuggestionsBox.getChildren().add(noResultLabel);
            } else {
                // Thêm tiêu đề kết quả tìm kiếm
                Label resultHeader = new Label(isVietnamese ?
                        "Kết quả tìm kiếm:" :
                        "Search results:");
                resultHeader.setStyle("-fx-font-weight: bold; -fx-padding: 5px; -fx-font-size: 14px;");
                searchSuggestionsBox.getChildren().add(resultHeader);

                // Hiển thị tối đa 5 kết quả đầu tiên
                int count = Math.min(searchResults.size(), 5);
                for (int i = 0; i < count; i++) {
                    Product product = searchResults.get(i);
                    HBox resultItem = createSearchResultItem(product);
                    searchSuggestionsBox.getChildren().add(resultItem);

                    if (i < count - 1) {
                        Separator separator = new Separator();
                        separator.setStyle("-fx-opacity: 0.3;");
                        searchSuggestionsBox.getChildren().add(separator);
                    }
                }

                // Hiển thị nút "Xem thêm" nếu có nhiều hơn 5 kết quả
                if (searchResults.size() > 5) {
                    Separator separator = new Separator();
                    separator.setStyle("-fx-opacity: 0.3;");
                    searchSuggestionsBox.getChildren().add(separator);

                    HBox viewMoreBox = new HBox();
                    viewMoreBox.setAlignment(Pos.CENTER);
                    viewMoreBox.setPadding(new Insets(5));

                    Label viewMoreLabel = new Label(isVietnamese ?
                            "Xem thêm " + (searchResults.size() - 5) + " sản phẩm..." :
                            "View more " + (searchResults.size() - 5) + " products...");
                    viewMoreLabel.setStyle("-fx-text-fill: #1976D2; -fx-font-weight: bold; -fx-cursor: hand;");

                    viewMoreBox.getChildren().add(viewMoreLabel);
                    viewMoreBox.setOnMouseClicked(e -> {
                        searchPopup.hide();
                        searchField.setText(keyword);
                        handleSearch();
                    });

                    searchSuggestionsBox.getChildren().add(viewMoreBox);
                }
            }

            // Hiển thị popup nếu có kết quả
            if (!searchPopup.isShowing() && searchField.getScene() != null && searchField.getScene().getWindow() != null) {
                Bounds bounds = searchField.localToScreen(searchField.getBoundsInLocal());
                searchPopup.show(searchField, bounds.getMinX(), bounds.getMaxY());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi thực hiện tìm kiếm: " + e.getMessage());
        }
    }

    private HBox createSearchResultItem(Product product) {
        // Tạo container chứa thông tin sản phẩm
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(5));
        container.setPrefWidth(480);
        container.setCursor(Cursor.HAND);

        // Hiệu ứng hover
        container.setOnMouseEntered(e ->
                container.setStyle("-fx-background-color: #f0f0f0;"));
        container.setOnMouseExited(e ->
                container.setStyle("-fx-background-color: transparent;"));

        // Khi click vào kết quả, mở trang chi tiết sản phẩm
        container.setOnMouseClicked(e -> {
            searchPopup.hide();
            showProductDetail(product);
        });

        // Tạo ImageView cho hình ảnh sản phẩm
        ImageView imageView = new ImageView();
        imageView.setFitHeight(40);
        imageView.setFitWidth(40);
        imageView.setPreserveRatio(true);

        // Sử dụng ImagePathUtils để xử lý đường dẫn hình ảnh
        try {
            String imagePath = product.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                // Chuẩn hóa đường dẫn
                String normalizedPath = ImagePathUtils.normalizeImagePath(imagePath);

                Image image = new Image(getClass().getResourceAsStream(normalizedPath));

                if (image.isError()) {
                    // Thử đường dẫn dự phòng
                    String fallbackPath = ImagePathUtils.getFallbackPath(imagePath);
                    Image fallbackImage = new Image(getClass().getResourceAsStream(fallbackPath));

                    if (!fallbackImage.isError()) {
                        imageView.setImage(fallbackImage);
                    } else {
                        // Sử dụng ảnh mặc định nếu không tìm thấy
                        imageView.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/default_product.png")));
                    }
                } else {
                    imageView.setImage(image);
                }
            } else {
                // Sử dụng ảnh mặc định nếu không có đường dẫn
                imageView.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/default_product.png")));
            }
        } catch (Exception ex) {
            System.err.println("Lỗi tải hình ảnh cho sản phẩm trong tìm kiếm: " + ex.getMessage());
            imageView.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/default_product.png")));
        }

        // Tạo VBox để chứa thông tin sản phẩm
        VBox infoBox = new VBox(2);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        // Tạo Label cho tên sản phẩm
        Label nameLabel = new Label(product.getProductName());
        nameLabel.setStyle("-fx-font-weight: bold;");

        // Tạo Label cho giá sản phẩm
        Label priceLabel = new Label(String.format("%,.0f₫", product.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #ff6600;");

        // Thêm các thành phần vào container
        infoBox.getChildren().addAll(nameLabel, priceLabel);
        container.getChildren().addAll(imageView, infoBox);
        container.setCursor(Cursor.HAND);
        return container;
    }

    private void showProductDetail(Product product) {
        if (product == null) return;

        // Nếu đang hiển thị sản phẩm này rồi, chỉ cần cập nhật UI
        if (currentProduct != null && currentProduct.getProductID().equals(product.getProductID())) {
            displayProductDetails();
            loadProductReviews(product.getProductID());
            return;
        }

        // Tải thông tin chi tiết sản phẩm
        currentProduct = product;
        displayProductDetails();
        loadProductReviews(product.getProductID());

        // Cập nhật tiêu đề trang
        if (pageTitle != null) {
            pageTitle.setText(isVietnamese ? "Thông tin chi tiết sản phẩm" : "Product details");
        }
    }

}