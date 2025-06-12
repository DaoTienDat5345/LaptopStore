package com.example.stores.controller;

import com.example.stores.model.Category;
import com.example.stores.model.Product;
import com.example.stores.repository.impl.ProductRepository;
import com.example.stores.service.impl.CategoryService;
import com.example.stores.service.impl.ProductService;
import com.example.stores.util.AlertUtils;
import com.example.stores.util.ImagePathUtils;
import com.example.stores.util.LanguageManager;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.stage.Popup;

import java.io.InputStream;
import java.util.Timer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.*;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import javafx.scene.Node;

import java.util.*;
import java.util.stream.Collectors;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.NumberFormat;

public class HomeController implements Initializable {

    // FXML Controls
    @FXML private TextField searchField;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private GridPane productGrid;
    @FXML private ComboBox<String> priceCombo;
    @FXML private AnchorPane categoryPane;
    @FXML private Button closeCategoryBtn;
    @FXML private Button menuButton;
    @FXML private ScrollPane centerScrollPane;
    @FXML private ScrollPane categoryScrollPane;
    @FXML private VBox categoryListContainer;
    @FXML private Button searchButton;
    @FXML private ImageView logoIcon;
    @FXML private Label logoLabel;
    @FXML private Label storeLabel;
    @FXML private Label sellerCenterLabel;
    @FXML private GridPane featuredProductGrid;
    @FXML private VBox categoryProductsContainer;
    // Thêm các biến cho các GridPane danh mục
    @FXML private GridPane laptopProductGrid;
    @FXML private GridPane pcProductGrid;
    @FXML private GridPane cpuProductGrid;
    @FXML private GridPane mainboardProductGrid;  //
    @FXML private GridPane ramProductGrid;
    @FXML private GridPane hddProductGrid;        // Mới thêm
    @FXML private GridPane ssdProductGrid;        // Mới thêm
    @FXML private GridPane vgaProductGrid;        // Mới thêm (thay thế gpuProductGrid)
    @FXML private GridPane psuProductGrid;
    @FXML private GridPane coolingProductGrid;
    @FXML private GridPane caseProductGrid;
    @FXML private GridPane monitorProductGrid;
    @FXML private GridPane keyboardProductGrid;
    @FXML private GridPane mouseProductGrid;
    @FXML private GridPane headphoneProductGrid;
    @FXML private MenuItem accountInfoMenuItem;

    // Các nút "Hiển thị thêm"
    @FXML private Button showMoreLaptopBtn;
    @FXML private Button showMorePCBtn;
    @FXML private Button showMoreCPUBtn;
    @FXML private Button showMoreMainboardBtn;    // Tên mới
    @FXML private Button showMoreRAMBtn;
    @FXML private Button showMoreHDDBtn;          // Mới thêm
    @FXML private Button showMoreSSDBtn;          // Mới thêm
    @FXML private Button showMoreVGABtn;          // Mới thêm
    @FXML private Button showMorePSUBtn;
    @FXML private Button showMoreCoolingBtn;
    @FXML private Button showMoreCaseBtn;
    @FXML private Button showMoreMonitorBtn;
    @FXML private Button showMoreKeyboardBtn;
    @FXML private Button showMoreMouseBtn;
    @FXML private Button showMoreHeadphoneBtn;

    // Các nút "Đóng bớt"
    @FXML private Button showLessLaptopBtn;
    @FXML private Button showLessPCBtn;
    @FXML private Button showLessCPUBtn;
    @FXML private Button showLessMainboardBtn;    // Tên mới
    @FXML private Button showLessRAMBtn;
    @FXML private Button showLessHDDBtn;          // Mới thêm
    @FXML private Button showLessSSDBtn;          // Mới thêm
    @FXML private Button showLessVGABtn;          // Mới thêm
    @FXML private Button showLessPSUBtn;
    @FXML private Button showLessCoolingBtn;
    @FXML private Button showLessCaseBtn;
    @FXML private Button showLessMonitorBtn;
    @FXML private Button showLessKeyboardBtn;
    @FXML private Button showLessMouseBtn;
    @FXML private Button showLessHeadphoneBtn;


    @FXML private Button cartButton;
    @FXML private MenuButton userMenuButton;
    @FXML private MenuItem languageSwitchMenuItem;
    @FXML private MenuItem customDesignMenuItem;
    @FXML private MenuItem orderHistoryMenuItem;
    @FXML private MenuItem logoutMenuItem;
    @FXML private MenuButton priceButton;
    @FXML private BorderPane rootPane;

    private VBox searchSuggestionsBox;
    private Popup searchPopup;
    private Timer searchTimer;
    private final long SEARCH_DELAY = 300;
    private List<Product> currentFilteredProducts = null; // Lưu kết quả lọc hiện tại
    private String currentCategory = null;               // Danh mục đang chọn
    private String currentBrand = null;                  // Hãng đang chọn
    private Double currentMinPrice = null;               // Giá tối thiểu đang lọc
    private Double currentMaxPrice = null;               // Giá tối đa đang lọc

    // Services & Utilities
    private CategoryService categoryService;
    private ProductService productService;
    private boolean isVietnamese;
    private Map<String, Integer> categoryDisplayCountMap = new HashMap<>();

    private enum SortOrder { NONE, ASCENDING, DESCENDING }
    private SortOrder sortOrder = SortOrder.NONE;

    // UI State
    private TranslateTransition slideTransition;
    private boolean isCategoryOpen = false;
    private double mouseY = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo services
        categoryService = new CategoryService();
        productService = new ProductService();

        String[] categories = {"Laptop", "PC", "Main", "CPU", "RAM", "ROM",
                "Bàn phím", "Chuột", "Màn hình", "Tản nhiệt",
                "Case", "Nguồn", "Tai nghe"};

        for (String category : categories) {
            categoryDisplayCountMap.put(category, 0);
        }

        // Lấy trạng thái ngôn ngữ từ LanguageManager
        isVietnamese = LanguageManager.isVietnamese();
        System.out.println("HomeController đã khởi tạo với ngôn ngữ: " + (isVietnamese ? "Tiếng Việt" : "English"));

        Platform.runLater(() -> {
            // Kiểm tra nếu có từ khóa tìm kiếm đang chờ
            if (pendingSearchKeyword != null && !pendingSearchKeyword.isEmpty()) {
                String keyword = pendingSearchKeyword;
                pendingSearchKeyword = null; // Reset biến để không xử lý lại

                // Đặt từ khóa vào trường tìm kiếm và thực hiện tìm kiếm
                if (searchField != null) {
                    searchField.setText(keyword);
                    searchProducts(keyword);
                    System.out.println("Đã thực hiện tìm kiếm với từ khóa: " + keyword + " từ ProductDetail");
                }
            }

            // Kiểm tra nếu có danh mục cần lọc
            if (pendingCategoryFilter != null && !pendingCategoryFilter.isEmpty()) {
                String category = pendingCategoryFilter;
                pendingCategoryFilter = null; // Reset biến để không xử lý lại

                // Thực hiện lọc theo danh mục
                filterByCategoryName(category);
                System.out.println("Đã lọc theo danh mục: " + category + " từ ProductDetail");
            }
        });


        // Thiết lập hình ảnh cờ ban đầu dựa trên ngôn ngữ hiện tại
        if (languageSwitchMenuItem != null) {
            try {
                // Đường dẫn tới hình ảnh cờ - hiển thị cờ ngược với ngôn ngữ hiện tại
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
                System.err.println("Lỗi khi cập nhật cờ ban đầu: " + e.getMessage());
            }
        }

        setupLiveSearch();

        // Thiết lập thanh danh mục
        setupCategoryPanel();

        // Thiết lập các sự kiện
        setupEventHandlers();

        setupPriceButton();

        // Cập nhật ngôn ngữ cho toàn bộ giao diện
        updateLanguage();

        // Tải danh mục và sản phẩm
        loadCategories();
        loadProducts();
        loadFeaturedProducts();
        loadAllCategoryProducts();

        // Delay thiết lập responsive layout đến khi scene sẵn sàng
        Platform.runLater(this::setupResponsiveLayout);
        // Thêm vào cuối phương thức initialize()
        Platform.runLater(() -> {
            try {
                List<Product> allProducts = ProductRepository.getAllProducts();
                System.out.println("=== KIỂM TRA ĐƯỜNG DẪN HÌNH ẢNH ===");
                for (Product p : allProducts) {
                    System.out.println("Sản phẩm: " + p.getProductID() +
                            " | " + p.getProductName() +
                            " | Đường dẫn: " + p.getImagePath());
                }
                System.out.println("=== KẾT THÚC KIỂM TRA ===");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static String pendingSearchKeyword = null;
    private static String pendingCategoryFilter = null;

    // Thêm các phương thức static này
    public static void setPendingSearchKeyword(String keyword) {
        pendingSearchKeyword = keyword;
    }

    public static void setPendingCategoryFilter(String category) {
        pendingCategoryFilter = category;
    }


    /**
     * Tải và hiển thị sản phẩm nổi bật
     */
    private void loadFeaturedProducts() {
        try {
            List<Product> featuredProducts = ProductRepository.getFeaturedProducts(4);
            displayFeaturedProducts(featuredProducts);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể tải sản phẩm nổi bật: " + e.getMessage() :
                            "Could not load featured products: " + e.getMessage()
            );
        }
    }

    /**
     * Hiển thị sản phẩm nổi bật lên UI
     */
    private void displayFeaturedProducts(List<Product> products) {
        if (featuredProductGrid != null) {
            featuredProductGrid.getChildren().clear();

            if (products == null || products.isEmpty()) {
                Label emptyLabel = new Label(isVietnamese ? "Không có sản phẩm nổi bật nào" : "No featured products");
                emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
                featuredProductGrid.add(emptyLabel, 0, 0);
                return;
            }

            for (int i = 0; i < products.size() && i < 4; i++) {
                VBox card = createProductCard(products.get(i));
                // Thêm nhãn "Hot!" cho sản phẩm nổi bật
                Label hotLabel = new Label("HOT!");
                hotLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white; " +
                        "-fx-background-color: #E53935; -fx-padding: 2 8; -fx-background-radius: 10;");
                // Thêm nhãn Hot vào card
                ((VBox)card).getChildren().add(0, hotLabel);

                featuredProductGrid.add(card, i, 0);
            }
        }
    }

    /**
     * Tải và hiển thị sản phẩm của tất cả các danh mục
     */
    private void loadAllCategoryProducts() {
        try {
            // Tải sản phẩm cho từng danh mục THEO THỨ TỰ ĐÃ YÊU CẦU
            loadProductsForCategory("Laptop", laptopProductGrid);
            loadProductsForCategory("PC", pcProductGrid);
            loadProductsForCategory("CPU", cpuProductGrid);
            loadProductsForCategory("MainBoard", mainboardProductGrid);
            loadProductsForCategory("RAM", ramProductGrid);
            loadProductsForCategory("HDD", hddProductGrid);
            loadProductsForCategory("SSD", ssdProductGrid);
            loadProductsForCategory("VGA", vgaProductGrid);
            loadProductsForCategory("Nguồn", psuProductGrid);
            loadProductsForCategory("Tản nhiệt", coolingProductGrid);
            loadProductsForCategory("Case", caseProductGrid);
            loadProductsForCategory("Màn hình", monitorProductGrid);
            loadProductsForCategory("Bàn phím", keyboardProductGrid);
            loadProductsForCategory("Chuột", mouseProductGrid);
            loadProductsForCategory("Tai nghe", headphoneProductGrid);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể tải sản phẩm theo loại: " + e.getMessage() :
                            "Could not load products by categories: " + e.getMessage()
            );
        }
    }

    private void loadProductsForCategory(String categoryName, GridPane categoryGrid) {
        if (categoryGrid == null) return;

        try {
            // Khởi tạo giá trị ban đầu cho danh mục nếu chưa có
            if (!categoryDisplayCountMap.containsKey(categoryName)) {
                categoryDisplayCountMap.put(categoryName, 0);
            }

            int displayedCount = categoryDisplayCountMap.get(categoryName);
            categoryGrid.getChildren().clear();

            // Lấy danh sách tất cả sản phẩm theo danh mục
            List<Product> allProducts = ProductRepository.getProductsByCategory(categoryName);

            if (allProducts.isEmpty()) {
                // Hiển thị thông báo "Chưa có sản phẩm" nếu không có sản phẩm nào
                Label emptyLabel = new Label(isVietnamese ?
                        "Chưa có sản phẩm " + categoryName + " nào" :
                        "No " + categoryName + " products available");
                emptyLabel.getStyleClass().add("no-products-label");
                categoryGrid.add(emptyLabel, 0, 0);

                // Ẩn nút "Hiển thị thêm"
                setShowMoreButtonVisibility(categoryName, false);
                return;
            }

            // Giới hạn số lượng hiển thị (4 sản phẩm mỗi lần)
            int maxDisplay = Math.min(displayedCount + 4, allProducts.size());

            // Hiển thị sản phẩm từ vị trí hiện tại đến maxDisplay
            int column = 0;
            int row = 0;

            for (int i = 0; i < maxDisplay; i++) {
                Product product = allProducts.get(i);
                VBox productCard = createProductCard(product);

                // Thêm vào GridPane theo hàng và cột
                categoryGrid.add(productCard, column, row);
                column++;

                // Xuống hàng sau mỗi 4 sản phẩm
                if (column == 4) {
                    column = 0;
                    row++;
                }
            }

            // Cập nhật số lượng sản phẩm đã hiển thị
            categoryDisplayCountMap.put(categoryName, maxDisplay);

            // Tạo BorderPane để chứa các nút "Show more" và "Show less"
            BorderPane buttonContainer = new BorderPane();
            buttonContainer.setPadding(new Insets(10, 20, 10, 20));

            // Đặt nút "Show more" bên trái
            Button showMoreButton = getShowMoreButton(categoryName);
            if (showMoreButton != null) {
                showMoreButton.setText(isVietnamese ? "Hiển thị thêm" : "Show more");
                // Style cho nút
                showMoreButton.setStyle("-fx-background-color: #5CB8E4; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 5px;");
                buttonContainer.setLeft(showMoreButton);

                // Hiển thị nút "Hiển thị thêm" nếu còn sản phẩm chưa hiển thị
                boolean hasMoreProducts = maxDisplay < allProducts.size();
                showMoreButton.setVisible(hasMoreProducts);
            }

            // Đặt nút "Show less" bên phải
            Button showLessButton = getShowLessButton(categoryName);
            if (showLessButton != null) {
                showLessButton.setText(isVietnamese ? "Đóng bớt" : "Show less");
                // Style cho nút
                showLessButton.setStyle("-fx-background-color: #E57373; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 5px;");
                buttonContainer.setRight(showLessButton);

                // Chỉ hiển thị nút "Đóng bớt" khi có nhiều hơn 1 hàng sản phẩm
                showLessButton.setVisible(categoryDisplayCountMap.getOrDefault(categoryName, 0) > 4);
            }

            // Thêm container vào phần tử chứa danh mục sản phẩm
            VBox categoryProductContainer = (VBox) categoryGrid.getParent().getParent();
            categoryProductContainer.getChildren().add(buttonContainer);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi tải sản phẩm danh mục " + categoryName + ": " + e.getMessage());
        }
    }

    /**
     * Hiển thị thêm sản phẩm khi nhấn nút "Hiển thị thêm"
     */
    @FXML
    private void showMoreProducts(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String categoryName = (String) clickedButton.getUserData();

        // Xác định GridPane và nút Đóng bớt tương ứng với danh mục
        GridPane categoryGrid = getCategoryGrid(categoryName);
        Button showLessButton = getShowLessButton(categoryName);

        if (categoryGrid != null && showLessButton != null) {
            // Lấy số lượng sản phẩm hiện tại và thêm 4 sản phẩm mới
            int currentCount = categoryDisplayCountMap.getOrDefault(categoryName, 0);
            loadMoreProductsForCategory(categoryName, categoryGrid, currentCount + 4);

            // Hiển thị nút "Đóng bớt" khi đã có nhiều hơn 4 sản phẩm
            if (categoryDisplayCountMap.get(categoryName) > 4) {
                showLessButton.setVisible(true);
            }
        }
    }

    /**
     * Xử lý sự kiện khi nhấn nút "Đóng bớt" - đóng từng hàng từ dưới lên
     */
    @FXML
    private void showLessProducts(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String categoryName = (String) clickedButton.getUserData();

        GridPane categoryGrid = getCategoryGrid(categoryName);
        Button showMoreButton = getShowMoreButton(categoryName);

        if (categoryGrid != null) {
            try {
                // Lấy số lượng sản phẩm hiện đang hiển thị
                int currentCount = categoryDisplayCountMap.getOrDefault(categoryName, 0);

                // Tính số hàng hiện tại (mỗi hàng tối đa 4 sản phẩm)
                int currentRows = (int) Math.ceil(currentCount / 4.0);

                // Nếu chỉ còn 1 hàng, không cho đóng nữa
                if (currentRows <= 1) {
                    clickedButton.setVisible(false);
                    return;
                }

                // Tính số sản phẩm mới sau khi bỏ hàng cuối
                int newRows = currentRows - 1;
                int newCount = newRows * 4;

                // Lấy danh sách sản phẩm để biết có bao nhiêu sản phẩm thực tế
                List<Product> allProducts = ProductRepository.getProductsByCategory(categoryName);

                // Đảm bảo không vượt quá số sản phẩm thực có
                newCount = Math.min(newCount, allProducts.size());

                // Hiển thị lại với số lượng mới
                categoryGrid.getChildren().clear();

                // Hiển thị sản phẩm theo grid
                int column = 0;
                int row = 0;

                for (int i = 0; i < newCount; i++) {
                    VBox productCard = createProductCard(allProducts.get(i));
                    categoryGrid.add(productCard, column, row);
                    column++;

                    if (column == 4) {
                        column = 0;
                        row++;
                    }
                }

                // Cập nhật lại số lượng sản phẩm đang hiển thị
                categoryDisplayCountMap.put(categoryName, newCount);

                // Kiểm tra xem có nên ẩn nút "Đóng bớt" không
                if (newRows <= 1) {
                    clickedButton.setVisible(false);
                }

                // Hiển thị nút "Hiển thị thêm" vì chắc chắn còn sản phẩm chưa hiển thị
                if (showMoreButton != null) {
                    showMoreButton.setVisible(true);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Tải một số lượng cố định sản phẩm cho danh mục
     */
    private void loadProductsForCategory(String categoryName, GridPane categoryGrid, int count) {
        try {
            List<Product> products = ProductRepository.getProductsByCategory(categoryName);

            if (products.isEmpty()) {
                // Hiển thị thông báo nếu không có sản phẩm
                Label emptyLabel = new Label(isVietnamese ?
                        "Chưa có sản phẩm " + categoryName + " nào" :
                        "No " + categoryName + " products available");
                emptyLabel.getStyleClass().add("no-products-label");
                categoryGrid.add(emptyLabel, 0, 0);
                return;
            }

            // Giới hạn số lượng sản phẩm hiển thị
            int maxDisplay = Math.min(count, products.size());

            // Hiển thị sản phẩm theo grid
            int column = 0;
            int row = 0;

            for (int i = 0; i < maxDisplay; i++) {
                VBox productCard = createProductCard(products.get(i));
                categoryGrid.add(productCard, column, row);
                column++;

                if (column == 4) {
                    column = 0;
                    row++;
                }
            }

            // Cập nhật số lượng hiển thị
            categoryDisplayCountMap.put(categoryName, maxDisplay);

            // Cập nhật trạng thái nút "Hiển thị thêm"
            Button showMoreButton = getShowMoreButton(categoryName);
            if (showMoreButton != null) {
                showMoreButton.setVisible(maxDisplay < products.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tải thêm sản phẩm đến một số lượng cụ thể
     */
    private void loadMoreProductsForCategory(String categoryName, GridPane categoryGrid, int targetCount) {
        try {
            List<Product> products = ProductRepository.getProductsByCategory(categoryName);
            int currentCount = categoryDisplayCountMap.getOrDefault(categoryName, 0);

            // Giới hạn số lượng sản phẩm hiển thị
            int maxDisplay = Math.min(targetCount, products.size());

            // Thêm sản phẩm mới vào grid
            int column = currentCount % 4;
            int row = currentCount / 4;

            for (int i = currentCount; i < maxDisplay; i++) {
                VBox productCard = createProductCard(products.get(i));
                categoryGrid.add(productCard, column, row);
                column++;

                if (column == 4) {
                    column = 0;
                    row++;
                }
            }

            // Cập nhật số lượng hiển thị
            categoryDisplayCountMap.put(categoryName, maxDisplay);

            // Cập nhật trạng thái nút "Hiển thị thêm"
            Button showMoreButton = getShowMoreButton(categoryName);
            if (showMoreButton != null) {
                showMoreButton.setVisible(maxDisplay < products.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lấy nút "Hiển thị thêm" tương ứng với danh mục
     */
    private Button getShowMoreButton(String categoryName) {
        switch (categoryName) {
            case "Laptop": return showMoreLaptopBtn;
            case "PC": return showMorePCBtn;
            case "CPU": return showMoreCPUBtn;
            case "MainBoard": return showMoreMainboardBtn;
            case "RAM": return showMoreRAMBtn;
            case "HDD": return showMoreHDDBtn;
            case "SSD": return showMoreSSDBtn;
            case "VGA": return showMoreVGABtn;
            case "Nguồn": return showMorePSUBtn;
            case "Tản nhiệt": return showMoreCoolingBtn;
            case "Case": return showMoreCaseBtn;
            case "Màn hình": return showMoreMonitorBtn;
            case "Bàn phím": return showMoreKeyboardBtn;
            case "Chuột": return showMoreMouseBtn;
            case "Tai nghe": return showMoreHeadphoneBtn;
            default: return null;
        }
    }

    /**
     * Lấy nút "Đóng bớt" tương ứng với danh mục
     */
    private Button getShowLessButton(String categoryName) {
        switch (categoryName) {
            case "Laptop": return showLessLaptopBtn;
            case "PC": return showLessPCBtn;
            case "CPU": return showLessCPUBtn;
            case "MainBoard": return showLessMainboardBtn;
            case "RAM": return showLessRAMBtn;
            case "HDD": return showLessHDDBtn;
            case "SSD": return showLessSSDBtn;
            case "VGA": return showLessVGABtn;
            case "Nguồn": return showLessPSUBtn;
            case "Tản nhiệt": return showLessCoolingBtn;
            case "Case": return showLessCaseBtn;
            case "Màn hình": return showLessMonitorBtn;
            case "Bàn phím": return showLessKeyboardBtn;
            case "Chuột": return showLessMouseBtn;
            case "Tai nghe": return showLessHeadphoneBtn;
            default: return null;
        }
    }

    /**
     * Lấy GridPane tương ứng với tên danh mục
     */
    private GridPane getCategoryGrid(String categoryName) {
        switch (categoryName) {
            case "Laptop": return laptopProductGrid;
            case "PC": return pcProductGrid;
            case "CPU": return cpuProductGrid;
            case "MainBoard": return mainboardProductGrid;
            case "RAM": return ramProductGrid;
            case "HDD": return hddProductGrid;
            case "SSD": return ssdProductGrid;
            case "VGA": return vgaProductGrid;
            case "Nguồn": return psuProductGrid;
            case "Tản nhiệt": return coolingProductGrid;
            case "Case": return caseProductGrid;
            case "Màn hình": return monitorProductGrid;
            case "Bàn phím": return keyboardProductGrid;
            case "Chuột": return mouseProductGrid;
            case "Tai nghe": return headphoneProductGrid;
            default: return null;
        }
    }

    /**
     * Cập nhật trạng thái hiển thị của nút "Hiển thị thêm" tương ứng với danh mục
     */
    private void setShowMoreButtonVisibility(String categoryName, boolean visible) {
        Button button = getShowMoreButton(categoryName);

        if (button != null) {
            button.setVisible(visible);
        }
    }
    /**
     * Tải và hiển thị sản phẩm theo loại
     */
    private void loadProductsByCategories() {
        try {
            Map<String, List<Product>> productsByCategory = ProductRepository.getProductsByCategories(4);
            displayProductsByCategories(productsByCategory);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể tải sản phẩm theo loại: " + e.getMessage() :
                            "Could not load products by categories: " + e.getMessage()
            );
        }
    }

    /**
     * Hiển thị sản phẩm theo loại lên UI
     */
    private void displayProductsByCategories(Map<String, List<Product>> productsByCategory) {
        if (categoryProductsContainer != null) {
            categoryProductsContainer.getChildren().clear();

            if (productsByCategory == null || productsByCategory.isEmpty()) {
                Label emptyLabel = new Label(isVietnamese ? "Không có sản phẩm nào theo loại" : "No products by category");
                emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
                categoryProductsContainer.getChildren().add(emptyLabel);
                return;
            }

            // Duyệt qua từng loại sản phẩm và hiển thị
            for (Map.Entry<String, List<Product>> entry : productsByCategory.entrySet()) {
                // Container cho mỗi loại sản phẩm
                VBox categorySection = new VBox(15);

                // Tiêu đề loại sản phẩm
                Label categoryTitle = new Label(entry.getKey());
                categoryTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #333333;");

                // GridPane để hiển thị sản phẩm theo loại
                GridPane categoryGrid = new GridPane();
                categoryGrid.setHgap(20);
                categoryGrid.setVgap(20);

                // Thêm sản phẩm vào grid
                List<Product> products = entry.getValue();
                for (int i = 0; i < products.size() && i < 4; i++) {
                    VBox card = createProductCard(products.get(i));
                    categoryGrid.add(card, i, 0);
                }

                // Thiết lập column constraints cho grid
                for (int i = 0; i < 4; i++) {
                    ColumnConstraints column = new ColumnConstraints();
                    column.setPercentWidth(25);
                    column.setHgrow(Priority.SOMETIMES);
                    categoryGrid.getColumnConstraints().add(column);
                }

                // Thêm tiêu đề và grid vào section
                categorySection.getChildren().addAll(categoryTitle, categoryGrid);

                // Thêm section vào container chính
                categoryProductsContainer.getChildren().add(categorySection);

                // Thêm nút xem thêm cho mỗi danh mục
                Button viewMoreBtn = new Button(isVietnamese ? "Xem thêm " + entry.getKey() : "View more " + entry.getKey());
                viewMoreBtn.setStyle("-fx-background-color: #5CB8E4; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-padding: 8 16; -fx-background-radius: 5;");
                viewMoreBtn.setCursor(Cursor.HAND);

                // Xử lý sự kiện khi nhấp vào nút xem thêm
                final String categoryName = entry.getKey();
                viewMoreBtn.setOnAction(e -> filterByCategoryName(categoryName));

                // Thêm nút vào container
                categoryProductsContainer.getChildren().add(viewMoreBtn);
            }
        }
    }
    /**
     * Lọc sản phẩm theo tên danh mục
     */
    public void filterByCategoryName(String categoryName) {
        System.out.println("Đang lọc theo danh mục: " + categoryName);

        try {
            List<Product> products = ProductRepository.getProductsByCategory(categoryName);
            System.out.println("Tìm thấy " + products.size() + " sản phẩm trong danh mục: " + categoryName);

            if (products.isEmpty()) {
                AlertUtils.showInfo(
                        isVietnamese ? "Thông báo" : "Information",
                        isVietnamese ? "Không tìm thấy sản phẩm nào trong danh mục " + categoryName :
                                "No products found in category " + categoryName
                );
            }

            // Đảm bảo đặt currentPage và currentCategory
            currentPage = Page.CATEGORY;
            currentCategory = categoryName;

            // Hiển thị sản phẩm theo danh mục
            displayCategoryWithFilters(categoryName, products);

            // Cuộn lên đầu để người dùng thấy kết quả
            if (centerScrollPane != null) {
                centerScrollPane.setVvalue(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể lọc sản phẩm theo danh mục: " + e.getMessage() :
                            "Cannot filter products by category: " + e.getMessage()
            );
        }
    }

    private void setupCategoryPanel() {
        if (categoryPane != null) {
            categoryPane.setTranslateX(-225);
            categoryPane.setVisible(false);

            slideTransition = new TranslateTransition(Duration.millis(250), categoryPane);

            if (categoryScrollPane != null) {
                categoryScrollPane.setFitToWidth(true);
                categoryScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                categoryScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            }

            Platform.runLater(() -> {
                if (centerScrollPane != null && centerScrollPane.getScene() != null) {
                    centerScrollPane.getScene().addEventHandler(MouseEvent.MOUSE_MOVED, this::updateMousePosition);
                }
            });
        }
    }

    private void setupEventHandlers() {
        // Xử lý sự kiện cho nút đổi ngôn ngữ
        // Cập nhật để sử dụng menu items mới
        if (languageSwitchMenuItem != null) {
            languageSwitchMenuItem.setOnAction(e -> switchLanguage());
        }

        // Xử lý sự kiện đăng xuất
        if (logoutMenuItem != null) {
            logoutMenuItem.setOnAction(e -> handleLogout());
        }
        if (orderHistoryMenuItem != null) {
            orderHistoryMenuItem.setOnAction(e -> openOrderHistoryScreen());
        }
        // Xử lý sự kiện tìm kiếm
        if (searchButton != null) {
            searchButton.setOnAction(event -> {
                String keyword = searchField.getText().trim();
                if (!keyword.isEmpty()) {
                    searchProducts(keyword);
                } else {
                    loadProducts();
                }
            });
        }

        // Xử lý sự kiện tìm kiếm
        if (searchButton != null) {
            searchButton.setOnAction(event -> {
                String keyword = searchField.getText().trim();
                System.out.println("Đã nhấn nút tìm kiếm với từ khóa: " + keyword);
                searchProducts(keyword);
            });
        }

        // Xử lý sự kiện Enter trên trường tìm kiếm
        if (searchField != null) {
            searchField.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    String keyword = searchField.getText().trim();
                    System.out.println("Đã nhấn Enter để tìm kiếm với từ khóa: " + keyword);
                    searchProducts(keyword);
                }
            });
        }


        // Xử lý sự kiện cho nút sắp xếp giá - sử dụng MenuButton thay vì ComboBox
        if (priceButton != null) {
            // MenuItem đã được kết nối trực tiếp với các phương thức trong FXML
            // Đảm bảo bổ sung kết nối cho các chức năng lọc giá
            System.out.println("Đã thiết lập nút lọc giá");
            priceButton.setOnMouseEntered(event -> {
                priceButton.setStyle("-fx-background-color: linear-gradient(to right, #5CB8E4, #865DFF); " +
                        "-fx-background-radius: 20; -fx-font-weight: bold; -fx-text-fill: white; " +
                        "-fx-font-size: 15px; -fx-border-radius: 20; -fx-border-color: #000000; " +
                        "-fx-border-width: 1.5px; -fx-padding: 0 18 0 18;");
            });

            priceButton.setOnMouseExited(event -> {
                priceButton.setStyle("-fx-background-color: linear-gradient(to right, #865DFF, #5CB8E4); " +
                        "-fx-background-radius: 20; -fx-font-weight: bold; -fx-text-fill: white; " +
                        "-fx-font-size: 15px; -fx-border-radius: 20; -fx-border-color: #000000; " +
                        "-fx-border-width: 1.5px; -fx-padding: 0 18 0 18;");
            });
            // Nếu bạn muốn thêm code để chắc chắn rằng các MenuItem được liên kết đúng:
            if (priceButton.getItems().size() >= 2) {
                MenuItem descendingItem = priceButton.getItems().get(0);
                MenuItem ascendingItem = priceButton.getItems().get(1);

                descendingItem.setOnAction(event -> sortDescending());
                ascendingItem.setOnAction(event -> sortAscending());
            }
        }

        // Xử lý sự kiện cho nút lọc giá
        if (minPriceField != null && maxPriceField != null) {
            // Xử lý sự kiện tìm kiếm khi nhấn Enter
            searchField.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    if (searchButton != null) {
                        searchButton.fire();
                    } else {
                        onSearch();
                    }
                }
            });
        }

        // Thiết lập để click vào logo hoặc tên cửa hàng để quay về trang chủ
        if (logoIcon != null) {
            logoIcon.setOnMouseClicked(event -> {
                System.out.println("Đã nhấp vào LOGO");
                goToHomePage();
            });
            logoIcon.setCursor(javafx.scene.Cursor.HAND);
        }

        if (logoLabel != null) {
            logoLabel.setOnMouseClicked(event -> {
                System.out.println("Đã nhấp vào CELLCOMP");
                goToHomePage();
            });
            logoLabel.setCursor(javafx.scene.Cursor.HAND);
        }

        if (storeLabel != null) {
            storeLabel.setOnMouseClicked(event -> {
                System.out.println("Đã nhấp vào STORE");
                goToHomePage();
            });
            storeLabel.setCursor(javafx.scene.Cursor.HAND);
        }

        if (sellerCenterLabel != null) {
            sellerCenterLabel.setOnMouseClicked(event -> {
                System.out.println("Đã nhấp vào Seller center");
                goToHomePage();
            });
            sellerCenterLabel.setCursor(javafx.scene.Cursor.HAND);
        }


        if (accountInfoMenuItem != null) {
            accountInfoMenuItem.setOnAction(e -> openCustomerChangeScreen());
        }
    }

    /**
     * Mở giao diện Thông tin tài khoản (CustomerChange.fxml)
     */
    private void openCustomerChangeScreen() {
        try {
            // Lưu trạng thái ngôn ngữ hiện tại vào LanguageManager
            LanguageManager.setVietnamese(isVietnamese);
            System.out.println("Mở giao diện Thông tin tài khoản với ngôn ngữ: " +
                    (isVietnamese ? "Tiếng Việt" : "English"));

            // Lấy Stage hiện tại từ bất kỳ control nào đã được khởi tạo
            Stage currentStage = (Stage) userMenuButton.getScene().getWindow();

            // Lưu kích thước và vị trí hiện tại của cửa sổ
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();

            // Load giao diện CustomerChange.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomerChange.fxml"));
            Parent root = loader.load();

            // Thiết lập scene mới
            Scene scene = new Scene(root);

            // Thiết lập tiêu đề dựa trên ngôn ngữ hiện tại
            currentStage.setTitle(isVietnamese ? "CELLCOMP STORE - Thông tin tài khoản" :
                    "CELLCOMP STORE - Account Information");

            // Đặt scene mới vào stage
            currentStage.setScene(scene);

            // Giữ nguyên kích thước và vị trí cửa sổ
            currentStage.setWidth(width);
            currentStage.setHeight(height);
            currentStage.setX(x);
            currentStage.setY(y);

            // Lấy controller - BỎ dòng gọi updateInitialLanguage() không tồn tại
            CustomerChangeController controller = loader.getController();
            // Có thể thêm code khác ở đây để thiết lập controller nếu cần

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở giao diện thông tin tài khoản: " + e.getMessage() :
                            "Cannot open account information screen: " + e.getMessage()
            );
        }
    }

    /**
     * Thiết lập menu nút Giá trên thanh tiêu đề với các ô lọc giá trực tiếp
     */
    private void setupPriceButton() {
        if (priceButton != null) {
            // Xóa tất cả MenuItem hiện có
            priceButton.getItems().clear();

            // Tạo MenuItem sắp xếp giảm dần
            MenuItem descendingItem = new MenuItem(isVietnamese ? "Giá giảm dần" : "Price descending");
            descendingItem.setStyle("-fx-font-size: 13px; -fx-padding: 5px 10px;");
            descendingItem.setOnAction(e -> {
                System.out.println("Đang sắp xếp GIẢM DẦN từ nút trên thanh tiêu đề...");

                // Nếu đang ở trang danh mục, sử dụng phương thức sortCategoryProducts
                if (currentPage == Page.CATEGORY && currentCategory != null) {
                    sortCategoryProducts(currentCategory, false);
                } else {
                    // Nếu không phải trang danh mục, thực hiện sắp xếp toàn cục
                    sortDescending();
                }
            });

            // Tạo MenuItem sắp xếp tăng dần
            MenuItem ascendingItem = new MenuItem(isVietnamese ? "Giá tăng dần" : "Price ascending");
            ascendingItem.setStyle("-fx-font-size: 13px; -fx-padding: 5px 10px;");
            ascendingItem.setOnAction(e -> {
                System.out.println("Đang sắp xếp TĂNG DẦN từ nút trên thanh tiêu đề...");

                // Nếu đang ở trang danh mục, sử dụng phương thức sortCategoryProducts
                if (currentPage == Page.CATEGORY && currentCategory != null) {
                    sortCategoryProducts(currentCategory, true);
                } else {
                    // Nếu không phải trang danh mục, thực hiện sắp xếp toàn cục
                    sortAscending();
                }
            });

            // Thêm một separator giữa phần sắp xếp và lọc giá
            SeparatorMenuItem separator = new SeparatorMenuItem();

            // ------------ PHẦN LỌC GIÁ TRỰC TIẾP ------------

            // 1. Tạo label "Lọc giá"
            Label filterLabel = new Label(isVietnamese ? "Lọc theo giá:" : "Filter by price:");
            filterLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 5px 10px;");
            CustomMenuItem labelItem = new CustomMenuItem(filterLabel);
            labelItem.setHideOnClick(false);

            // 2. Tạo GridPane để căn chỉnh các phần tử "Từ" và "Đến" cho thẳng hàng
            GridPane priceInputGrid = new GridPane();
            priceInputGrid.setHgap(10);
            priceInputGrid.setVgap(10);
            priceInputGrid.setPadding(new Insets(5, 10, 5, 10));

            // Label "Từ:"
            Label fromLabel = new Label(isVietnamese ? "Từ:" : "From:");
            fromLabel.setStyle("-fx-font-size: 12px;");
            priceInputGrid.add(fromLabel, 0, 0);

            // TextField cho giá "Từ"
            TextField fromField = new TextField();
            fromField.setPrefWidth(120);
            if (currentMinPrice != null) {
                fromField.setText(String.valueOf(currentMinPrice));
            }
            fromField.setPromptText("0");
            fromField.setStyle("-fx-font-size: 12px;");
            priceInputGrid.add(fromField, 1, 0);

            // Label "Đến:"
            Label toLabel = new Label(isVietnamese ? "Đến:" : "To:");
            toLabel.setStyle("-fx-font-size: 12px;");
            priceInputGrid.add(toLabel, 0, 1);

            // TextField cho giá "Đến"
            TextField toField = new TextField();
            toField.setPrefWidth(120);
            if (currentMaxPrice != null) {
                toField.setText(String.valueOf(currentMaxPrice));
            }
            toField.setPromptText("1,000,000");
            toField.setStyle("-fx-font-size: 12px;");
            priceInputGrid.add(toField, 1, 1);

            // Đảm bảo các cột trong GridPane có chiều rộng thích hợp
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPrefWidth(40);
            col1.setHalignment(HPos.RIGHT); // Căn phải cho label

            ColumnConstraints col2 = new ColumnConstraints();
            col2.setHgrow(Priority.ALWAYS);
            col2.setFillWidth(true);

            priceInputGrid.getColumnConstraints().addAll(col1, col2);

            CustomMenuItem priceInputItem = new CustomMenuItem(priceInputGrid);
            priceInputItem.setHideOnClick(false);

            // 4. Nút áp dụng lọc giá
            Button applyButton = new Button(isVietnamese ? "Áp dụng" : "Apply");
            applyButton.setPrefWidth(120);
            applyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");

            // Event handler cho nút áp dụng
            applyButton.setOnAction(e -> {
                try {
                    String minText = fromField.getText().trim();
                    String maxText = toField.getText().trim();

                    Double min = minText.isEmpty() ? null : Double.parseDouble(minText.replace(",", ""));
                    Double max = maxText.isEmpty() ? null : Double.parseDouble(maxText.replace(",", ""));

                    if (min != null && max != null && min > max) {
                        AlertUtils.showWarning(
                                isVietnamese ? "Cảnh báo" : "Warning",
                                isVietnamese ? "Giá từ không được lớn hơn giá đến" : "Min price cannot be greater than max price"
                        );
                        return;
                    }

                    // Lưu giá trị lọc hiện tại
                    currentMinPrice = min;
                    currentMaxPrice = max;

                    // Áp dụng lọc giá dựa vào trang hiện tại
                    if (currentPage == Page.CATEGORY && currentCategory != null) {
                        filterCategoryByPrice(currentCategory, min, max);
                    } else {
                        // Áp dụng lọc giá toàn cục
                        applyFiltersToAllProducts();
                    }

                    // Đóng menu
                    priceButton.hide();

                } catch (NumberFormatException ex) {
                    AlertUtils.showWarning(
                            isVietnamese ? "Cảnh báo" : "Warning",
                            isVietnamese ? "Vui lòng nhập giá hợp lệ" : "Please enter valid price values"
                    );
                }
            });

            // Đặt nút vào container
            HBox buttonBox = new HBox();
            buttonBox.setPadding(new Insets(5, 10, 5, 10));
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().add(applyButton);

            CustomMenuItem applyItem = new CustomMenuItem(buttonBox);
            applyItem.setHideOnClick(false);

            // Thêm tất cả các thành phần vào menu
            priceButton.getItems().addAll(
                    descendingItem,
                    ascendingItem,
                    separator,
                    labelItem,
                    priceInputItem,
                    applyItem
            );

            // Điều chỉnh chiều rộng của MenuButton để hiển thị đầy đủ
            priceButton.setPrefWidth(120);

            // Thêm lớp CSS cho hiệu ứng hover
            priceButton.getStyleClass().add("header-price-btn");

            // Xử lý sự kiện click để hiển thị menu
            priceButton.setOnMouseClicked(e -> {
                priceButton.show();
            });
        }
    }

    /**
     * Hiển thị giao diện lọc giá trực tiếp
     */
    private void showDirectPriceFilter() {
        try {
            // 1. Lấy AnchorPane chính từ ScrollPane
            AnchorPane contentPane = (AnchorPane) centerScrollPane.getContent();
            if (contentPane == null) return;
            contentPane.getChildren().clear();

            // Lưu lại trang hiện tại là PRICE_SORT
            currentPage = Page.PRICE_SORT;

            // 2. Tạo container chính
            VBox mainContainer = new VBox(20);
            mainContainer.setPadding(new Insets(20));

            // 3. Hiển thị tiêu đề
            Label titleLabel = new Label(isVietnamese ? "LỌC THEO GIÁ" : "FILTER BY PRICE");
            titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #E53935;");

            // Đặt tiêu đề vào HBox để căn giữa
            HBox titleBox = new HBox();
            titleBox.setAlignment(Pos.CENTER);
            titleBox.getChildren().add(titleLabel);
            mainContainer.getChildren().add(titleBox);

            // 4. Tạo phần lọc giá trực tiếp
            HBox priceFilterBar = new HBox(15);
            priceFilterBar.setAlignment(Pos.CENTER);
            priceFilterBar.setPadding(new Insets(15));
            priceFilterBar.setStyle("-fx-background-color: #F9F9F9; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 0);");

            // Label cho phần lọc giá
            Label priceLabel = new Label(isVietnamese ? "Giá:" : "Price:");
            priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

            // Ô nhập giá từ
            TextField fromField = new TextField();
            if (currentMinPrice != null) {
                fromField.setText(String.valueOf(currentMinPrice));
            }
            fromField.setPromptText(isVietnamese ? "Từ" : "From");
            fromField.setPrefWidth(150);
            fromField.setStyle("-fx-background-radius: 20; -fx-padding: 10; -fx-border-color: #CCCCCC; -fx-border-radius: 20;");

            // Ô nhập giá đến
            TextField toField = new TextField();
            if (currentMaxPrice != null) {
                toField.setText(String.valueOf(currentMaxPrice));
            }
            toField.setPromptText(isVietnamese ? "Đến" : "To");
            toField.setPrefWidth(150);
            toField.setStyle("-fx-background-radius: 20; -fx-padding: 10; -fx-border-color: #CCCCCC; -fx-border-radius: 20;");

            // Nút áp dụng lọc
            Button applyButton = new Button(isVietnamese ? "Áp dụng" : "Apply");
            applyButton.setPrefWidth(120);
            applyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20;");
            applyButton.setCursor(Cursor.HAND);

            // Hiệu ứng hover cho nút áp dụng
            applyButton.setOnMouseEntered(e -> {
                applyButton.setStyle("-fx-background-color: #0D8BF2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20;");
            });

            applyButton.setOnMouseExited(e -> {
                applyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20;");
            });

            // Nút reset
            Button resetButton = new Button(isVietnamese ? "Đặt lại" : "Reset");
            resetButton.setPrefWidth(100);
            resetButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20;");
            resetButton.setCursor(Cursor.HAND);

            // Hiệu ứng hover cho nút reset
            resetButton.setOnMouseEntered(e -> {
                resetButton.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20;");
            });

            resetButton.setOnMouseExited(e -> {
                resetButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20;");
            });

            // Xử lý sự kiện cho nút áp dụng
            applyButton.setOnAction(e -> {
                try {
                    String fromText = fromField.getText().trim();
                    String toText = toField.getText().trim();

                    Double min = fromText.isEmpty() ? null : Double.parseDouble(fromText);
                    Double max = toText.isEmpty() ? null : Double.parseDouble(toText);

                    if (min != null && max != null && min > max) {
                        AlertUtils.showWarning(
                                isVietnamese ? "Cảnh báo" : "Warning",
                                isVietnamese ? "Giá từ không được lớn hơn giá đến" : "Min price cannot be greater than max price"
                        );
                        return;
                    }

                    // Cập nhật giá trị lọc hiện tại
                    currentMinPrice = min;
                    currentMaxPrice = max;

                    // Thực hiện lọc
                    applyDirectPriceFilter(min, max);

                } catch (NumberFormatException ex) {
                    AlertUtils.showWarning(
                            isVietnamese ? "Cảnh báo" : "Warning",
                            isVietnamese ? "Vui lòng nhập giá hợp lệ" : "Please enter valid price values"
                    );
                }
            });

            // Xử lý sự kiện cho nút reset
            resetButton.setOnAction(e -> {
                fromField.clear();
                toField.clear();
                currentMinPrice = null;
                currentMaxPrice = null;

                // Hiển thị lại tất cả sản phẩm
                List<Product> allProducts = ProductRepository.getAllProducts();
                displayAllProducts(allProducts);
            });

            // Thêm các phần tử vào priceFilterBar
            priceFilterBar.getChildren().addAll(priceLabel, fromField, toField, applyButton, resetButton);

            // Thêm priceFilterBar vào container chính
            mainContainer.getChildren().add(priceFilterBar);

            // Hiển thị tất cả sản phẩm ban đầu
            List<Product> allProducts = ProductRepository.getAllProducts();

            // Nếu đã có lọc giá trước đó, áp dụng ngay
            if (currentMinPrice != null || currentMaxPrice != null) {
                allProducts = filterProducts(allProducts);
            }

            // Hiển thị danh sách sản phẩm
            displayAllProducts(allProducts);

            // Hiển thị container trên giao diện
            contentPane.getChildren().add(mainContainer);
            AnchorPane.setTopAnchor(mainContainer, 0.0);
            AnchorPane.setLeftAnchor(mainContainer, 0.0);
            AnchorPane.setRightAnchor(mainContainer, 0.0);

            // Cuộn lên đầu trang
            centerScrollPane.setVvalue(0.0);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể hiển thị giao diện lọc giá: " + e.getMessage() :
                            "Cannot display price filter interface: " + e.getMessage()
            );
        }
    }

    /**
     * Áp dụng bộ lọc giá trực tiếp và hiển thị kết quả
     */
    private void applyDirectPriceFilter(Double min, Double max) {
        try {
            // Lấy tất cả sản phẩm
            List<Product> allProducts = ProductRepository.getAllProducts();
            List<Product> filteredProducts = allProducts;

            // Lọc theo giá tối thiểu
            if (min != null) {
                filteredProducts = filteredProducts.stream()
                        .filter(p -> p.getPrice() >= min)
                        .collect(Collectors.toList());
            }

            // Lọc theo giá tối đa
            if (max != null) {
                filteredProducts = filteredProducts.stream()
                        .filter(p -> p.getPrice() <= max)
                        .collect(Collectors.toList());
            }

            // Hiển thị kết quả lọc
            displayAllProducts(filteredProducts);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể lọc sản phẩm theo giá: " + e.getMessage() :
                            "Cannot filter products by price: " + e.getMessage()
            );
        }
    }

    /**
     * Hiển thị tất cả sản phẩm sau khi lọc
     */
    private void displayAllProducts(List<Product> products) {
        try {
            // Lấy container chính
            AnchorPane contentPane = (AnchorPane) centerScrollPane.getContent();
            if (contentPane == null || contentPane.getChildren().isEmpty()) return;

            VBox mainContainer = (VBox) contentPane.getChildren().get(0);

            // Xóa các phần hiển thị sản phẩm cũ nếu có
            if (mainContainer.getChildren().size() > 2) {
                mainContainer.getChildren().remove(2, mainContainer.getChildren().size());
            }

            // Hiển thị số lượng sản phẩm tìm thấy
            Label countLabel = new Label(String.format(
                    isVietnamese ? "Tìm thấy %d sản phẩm" : "Found %d products",
                    products.size()
            ));
            countLabel.setStyle("-fx-font-size: 16px; -fx-font-style: italic;");
            mainContainer.getChildren().add(countLabel);

            // Tạo FlowPane để hiển thị sản phẩm
            FlowPane productsContainer = new FlowPane();
            productsContainer.setAlignment(Pos.CENTER);
            productsContainer.setHgap(20);
            productsContainer.setVgap(20);
            productsContainer.setPrefWrapLength(contentPane.getWidth() - 40);

            // Thêm từng sản phẩm vào container
            for (Product product : products) {
                VBox productCard = createProductCard(product);
                productsContainer.getChildren().add(productCard);
            }

            mainContainer.getChildren().add(productsContainer);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể hiển thị sản phẩm: " + e.getMessage() :
                            "Cannot display products: " + e.getMessage()
            );
        }
    }
    /**
     * Hiển thị dialog lọc giá
     */
    private void showPriceFilterDialog() {
        // Tạo dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(isVietnamese ? "Lọc theo khoảng giá" : "Filter by price range");
        dialog.setResizable(false);

        // Container chính
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        // Tạo các field nhập giá
        Label fromLabel = new Label(isVietnamese ? "Giá từ:" : "Price from:");
        TextField fromField = new TextField();
        fromField.setPromptText(isVietnamese ? "Từ..." : "From...");

        Label toLabel = new Label(isVietnamese ? "Giá đến:" : "Price to:");
        TextField toField = new TextField();
        toField.setPromptText(isVietnamese ? "Đến..." : "To...");

        // Khu vực nút
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button(isVietnamese ? "Hủy" : "Cancel");
        cancelButton.setOnAction(e -> dialog.close());

        Button applyButton = new Button(isVietnamese ? "Áp dụng" : "Apply");
        applyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        applyButton.setOnAction(e -> {
            try {
                String fromText = fromField.getText().trim();
                String toText = toField.getText().trim();

                Double min = fromText.isEmpty() ? null : Double.parseDouble(fromText);
                Double max = toText.isEmpty() ? null : Double.parseDouble(toText);

                if (min != null && max != null && min > max) {
                    AlertUtils.showWarning(
                            isVietnamese ? "Cảnh báo" : "Warning",
                            isVietnamese ? "Giá từ không được lớn hơn giá đến" : "Min price cannot be greater than max price"
                    );
                    return;
                }

                // Áp dụng lọc giá dựa vào trang hiện tại
                if (currentPage == Page.CATEGORY && currentCategory != null) {
                    filterCategoryByPrice(currentCategory, min, max);
                } else {
                    // Áp dụng lọc giá toàn cục
                    currentMinPrice = min;
                    currentMaxPrice = max;
                    applyFiltersToAllProducts();
                }

                dialog.close();
            } catch (NumberFormatException ex) {
                AlertUtils.showWarning(
                        isVietnamese ? "Cảnh báo" : "Warning",
                        isVietnamese ? "Vui lòng nhập giá hợp lệ" : "Please enter valid price values"
                );
            }
        });

        buttonBox.getChildren().addAll(cancelButton, applyButton);

        vbox.getChildren().addAll(fromLabel, fromField, toLabel, toField, new Separator(), buttonBox);

        Scene scene = new Scene(vbox, 300, 200);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    private void filterProductsByPrice(double minPrice, double maxPrice) {
        try {
            System.out.println("Đang lọc sản phẩm theo khoảng giá: " + minPrice + " - " + maxPrice);

            List<Product> filteredProducts = productService.getProductsByPriceRange(minPrice, maxPrice);
            displayProducts(filteredProducts);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể lọc sản phẩm theo khoảng giá: " + e.getMessage() :
                            "Cannot filter products by price range: " + e.getMessage()
            );
        }
    }
    private void updateLanguage() {
        // ===== Cập nhật các phần tử header =====
        // Cập nhật text cho các controls
        if (searchField != null)
            searchField.setPromptText(isVietnamese ? "Nhập từ khóa tìm kiếm..." : "Enter search keywords...");

        // Cập nhật nút Menu/Danh mục
        if (menuButton != null) {
            HBox menuButtonContent = (HBox) menuButton.getGraphic();
            if (menuButtonContent != null && menuButtonContent.getChildren().size() > 1) {
                Node lastNode = menuButtonContent.getChildren().get(1);
                if (lastNode instanceof Label) {
                    ((Label) lastNode).setText(isVietnamese ? "Danh mục" : "Categories");
                }
            }
        }

        // === CẬP NHẬT NÚT GIỎ HÀNG - PHẦN MỚI ===
        if (rootPane != null) {
            try {
                // Tìm AnchorPane headerPane
                AnchorPane headerPane = (AnchorPane) rootPane.getTop();
                if (headerPane != null) {
                    // Tìm nút giỏ hàng theo vị trí - nút thứ 4 trong headerPane
                    Button cartButton = null;
                    for (Node node : headerPane.getChildren()) {
                        if (node instanceof Button) {
                            Button button = (Button) node;
                            // Lấy HBox graphic nếu có
                            if (button.getGraphic() instanceof HBox) {
                                HBox hbox = (HBox) button.getGraphic();
                                // Kiểm tra xem HBox có chứa Label và ImageView không
                                boolean hasImage = false;
                                boolean hasLabel = false;
                                String labelText = "";

                                for (Node hboxChild : hbox.getChildren()) {
                                    if (hboxChild instanceof ImageView) {
                                        hasImage = true;
                                    }
                                    if (hboxChild instanceof Label) {
                                        hasLabel = true;
                                        labelText = ((Label) hboxChild).getText();
                                    }
                                }

                                if (hasImage && hasLabel && (labelText.equals("Giỏ hàng") || labelText.equals("Cart"))) {
                                    cartButton = button;
                                    break;
                                }
                            }
                        }
                    }

                    // Cập nhật text của nút giỏ hàng
                    if (cartButton != null) {
                        HBox hbox = (HBox) cartButton.getGraphic();
                        for (Node hboxChild : hbox.getChildren()) {
                            if (hboxChild instanceof Label) {
                                ((Label) hboxChild).setText(isVietnamese ? "Giỏ hàng" : "Cart");
                                System.out.println("Đã cập nhật nút giỏ hàng: " + (isVietnamese ? "Giỏ hàng" : "Cart"));
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi cập nhật nút giỏ hàng: " + e.getMessage());
            }
        }

        // Cập nhật nút Giá/Price
        if (priceButton != null) {
            // Cập nhật text trên nút
            Label priceLabel = (Label) priceButton.getGraphic();
            if (priceLabel != null) {
                priceLabel.setText(isVietnamese ? "Giá" : "Price");
            }

            // Cập nhật các menu item trong priceButton
            for (MenuItem item : priceButton.getItems()) {
                if (item instanceof MenuItem && !(item instanceof SeparatorMenuItem)) {
                    String itemText = item.getText();
                    if (itemText != null) {
                        if (itemText.contains("giảm dần") || itemText.contains("descending")) {
                            item.setText(isVietnamese ? "Giá giảm dần" : "Price descending");
                        } else if (itemText.contains("tăng dần") || itemText.contains("ascending")) {
                            item.setText(isVietnamese ? "Giá tăng dần" : "Price ascending");
                        }
                    }
                }

                // Xử lý lọc giá trong CustomMenuItem
                if (item instanceof CustomMenuItem) {
                    Node content = ((CustomMenuItem) item).getContent();
                    if (content instanceof Label) {
                        Label label = (Label) content;
                        String labelText = label.getText();
                        if (labelText != null && (labelText.contains("Lọc theo giá") ||
                                labelText.contains("Filter by price"))) {
                            label.setText(isVietnamese ? "Lọc theo giá:" : "Filter by price:");
                        }
                    }

                    if (content instanceof GridPane) {
                        GridPane grid = (GridPane) content;
                        for (Node node : grid.getChildren()) {
                            if (node instanceof Label) {
                                Label label = (Label) node;
                                String labelText = label.getText();
                                if (labelText != null) {
                                    if (labelText.contains("Từ") || labelText.contains("From")) {
                                        label.setText(isVietnamese ? "Từ:" : "From:");
                                    } else if (labelText.contains("Đến") || labelText.contains("To")) {
                                        label.setText(isVietnamese ? "Đến:" : "To:");
                                    }
                                }
                            }
                        }
                    }

                    if (content instanceof HBox) {
                        HBox hbox = (HBox) content;
                        for (Node node : hbox.getChildren()) {
                            if (node instanceof Button) {
                                Button button = (Button) node;
                                String buttonText = button.getText();
                                if (buttonText != null && (buttonText.contains("Áp dụng") || buttonText.contains("Apply"))) {
                                    button.setText(isVietnamese ? "Áp dụng" : "Apply");
                                }
                            }
                        }
                    }
                }
            }
        }

        // ===== Cập nhật menu người dùng =====
        if (accountInfoMenuItem != null)
            accountInfoMenuItem.setText(isVietnamese ? "Thông tin tài khoản" : "Account Information");
        if (languageSwitchMenuItem != null)
            languageSwitchMenuItem.setText(isVietnamese ? "Switch to English" : "Chuyển sang tiếng Việt");
        if (languageSwitchMenuItem != null)
            languageSwitchMenuItem.setText(isVietnamese ? "Switch to English" : "Chuyển sang tiếng Việt");
        if (customDesignMenuItem != null)
            customDesignMenuItem.setText(isVietnamese ? "Thiết kế máy tính theo ý bạn" : "Custom PC Design");
        if (orderHistoryMenuItem != null)
            orderHistoryMenuItem.setText(isVietnamese ? "Lịch sử mua hàng" : "Order History");
        if (logoutMenuItem != null)
            logoutMenuItem.setText(isVietnamese ? "Đăng xuất" : "Logout");

        // ===== CẬP NHẬT DANH MỤC BÊN TRÁI =====
        if (categoryPane != null && categoryScrollPane != null) {
            VBox categoryVBox = null;

            // Tìm VBox chứa các nút danh mục
            if (categoryScrollPane.getContent() instanceof VBox) {
                categoryVBox = (VBox) categoryScrollPane.getContent();
            }

            if (categoryVBox != null) {
                for (Node node : categoryVBox.getChildren()) {
                    if (node instanceof Button) {
                        Button categoryButton = (Button) node;
                        String buttonId = categoryButton.getId();
                        if (buttonId != null) {
                            // Đổi text cho từng loại danh mục
                            switch (buttonId) {
                                case "category-keyboard":
                                    categoryButton.setText(isVietnamese ? "Bàn phím" : "Keyboard");
                                    break;
                                case "category-mouse":
                                    categoryButton.setText(isVietnamese ? "Chuột" : "Mouse");
                                    break;
                                case "category-monitor":
                                    categoryButton.setText(isVietnamese ? "Màn hình" : "Monitor");
                                    break;
                                case "category-cooling":
                                    categoryButton.setText(isVietnamese ? "Tản nhiệt" : "Cooling");
                                    break;
                                case "category-psu":
                                    categoryButton.setText(isVietnamese ? "Nguồn" : "Power Supply");
                                    break;
                                case "category-headphone":
                                    categoryButton.setText(isVietnamese ? "Tai nghe" : "Headphones");
                                    break;
                                // Các nút khác như laptop, pc, cpu, ram, case giữ nguyên vì là thuật ngữ quốc tế
                            }
                        }
                    }
                }
                System.out.println("Đã cập nhật ngôn ngữ cho danh mục bên trái");
            }
        }

        // ===== CẬP NHẬT DANH SÁCH 15 LOẠI SẢN PHẨM - PHẦN MỚI =====
        if (categoryListContainer != null) {
            for (Node node : categoryListContainer.getChildren()) {
                if (node instanceof CheckBox) {
                    CheckBox categoryCheck = (CheckBox) node;
                    String currentText = categoryCheck.getText();
                    if (currentText != null) {
                        switch (currentText.toLowerCase()) {
                            case "tất cả sản phẩm":
                            case "all products":
                                categoryCheck.setText(isVietnamese ? "Tất cả sản phẩm" : "All products");
                                break;
                            case "laptop": break; // Giữ nguyên
                            case "pc": break; // Giữ nguyên
                            case "cpu": break; // Giữ nguyên
                            case "mainboard": break; // Giữ nguyên
                            case "ram": break; // Giữ nguyên
                            case "hdd": break; // Giữ nguyên
                            case "ssd": break; // Giữ nguyên
                            case "vga": break; // Giữ nguyên
                            case "nguồn":
                            case "power supply":
                                categoryCheck.setText(isVietnamese ? "Nguồn" : "Power Supply");
                                break;
                            case "tản nhiệt":
                            case "cooling":
                                categoryCheck.setText(isVietnamese ? "Tản nhiệt" : "Cooling");
                                break;
                            case "case": break; // Giữ nguyên
                            case "màn hình":
                            case "monitor":
                                categoryCheck.setText(isVietnamese ? "Màn hình" : "Monitor");
                                break;
                            case "bàn phím":
                            case "keyboard":
                                categoryCheck.setText(isVietnamese ? "Bàn phím" : "Keyboard");
                                break;
                            case "chuột":
                            case "mouse":
                                categoryCheck.setText(isVietnamese ? "Chuột" : "Mouse");
                                break;
                            case "tai nghe":
                            case "headphones":
                                categoryCheck.setText(isVietnamese ? "Tai nghe" : "Headphones");
                                break;
                        }
                    }
                }
            }
        }

        // Cập nhật tiêu đề trong categoryProductsContainer
        if (categoryProductsContainer != null) {
            for (Node section : categoryProductsContainer.getChildren()) {
                if (section instanceof VBox) {
                    VBox categoryBox = (VBox) section;
                    for (Node node : categoryBox.getChildren()) {
                        if (node instanceof Label) {
                            Label titleLabel = (Label) node;
                            String currentText = titleLabel.getText();
                            if (currentText != null) {
                                // Dịch tên danh mục
                                switch (currentText) {
                                    case "Nguồn": titleLabel.setText(isVietnamese ? "Nguồn" : "Power Supply"); break;
                                    case "Power Supply": titleLabel.setText(isVietnamese ? "Nguồn" : "Power Supply"); break;
                                    case "Tản nhiệt": titleLabel.setText(isVietnamese ? "Tản nhiệt" : "Cooling"); break;
                                    case "Cooling": titleLabel.setText(isVietnamese ? "Tản nhiệt" : "Cooling"); break;
                                    case "Màn hình": titleLabel.setText(isVietnamese ? "Màn hình" : "Monitor"); break;
                                    case "Monitor": titleLabel.setText(isVietnamese ? "Màn hình" : "Monitor"); break;
                                    case "Bàn phím": titleLabel.setText(isVietnamese ? "Bàn phím" : "Keyboard"); break;
                                    case "Keyboard": titleLabel.setText(isVietnamese ? "Bàn phím" : "Keyboard"); break;
                                    case "Chuột": titleLabel.setText(isVietnamese ? "Chuột" : "Mouse"); break;
                                    case "Mouse": titleLabel.setText(isVietnamese ? "Chuột" : "Mouse"); break;
                                    case "Tai nghe": titleLabel.setText(isVietnamese ? "Tai nghe" : "Headphones"); break;
                                    case "Headphones": titleLabel.setText(isVietnamese ? "Tai nghe" : "Headphones"); break;
                                }
                            }
                        }
                    }
                }
            }
        }

        // ===== Cập nhật các nút "Hiển thị thêm" và "Đóng bớt" =====
        // Show more buttons
        updateButtonText(showMoreLaptopBtn, isVietnamese ? "Hiển thị thêm" : "Show more");
        updateButtonText(showMorePCBtn, isVietnamese ? "Hiển thị thêm" : "Show more");
        updateButtonText(showMoreCPUBtn, isVietnamese ? "Hiển thị thêm" : "Show more");
        updateButtonText(showMoreMainboardBtn, isVietnamese ? "Hiển thị thêm" : "Show more");
        updateButtonText(showMoreRAMBtn, isVietnamese ? "Hiển thị thêm" : "Show more");
        updateButtonText(showMoreHDDBtn, isVietnamese ? "Hiển thị thêm" : "Show more");
        updateButtonText(showMoreSSDBtn, isVietnamese ? "Hiển thị thêm" : "Show more");
        updateButtonText(showMoreVGABtn, isVietnamese ? "Hiển thị thêm" : "Show more");
        updateButtonText(showMorePSUBtn, isVietnamese ? "Hiển thị thêm" : "Show more");
        updateButtonText(showMoreCoolingBtn, isVietnamese ? "Hiển thị thêm" : "Show more");
        updateButtonText(showMoreCaseBtn, isVietnamese ? "Hiển thị thêm" : "Show more");
        updateButtonText(showMoreMonitorBtn, isVietnamese ? "Hiển thị thêm" : "Show more");
        updateButtonText(showMoreKeyboardBtn, isVietnamese ? "Hiển thị thêm" : "Show more");
        updateButtonText(showMoreMouseBtn, isVietnamese ? "Hiển thị thêm" : "Show more");
        updateButtonText(showMoreHeadphoneBtn, isVietnamese ? "Hiển thị thêm" : "Show more");

        // Show less buttons
        updateButtonText(showLessLaptopBtn, isVietnamese ? "Đóng bớt" : "Show less");
        updateButtonText(showLessPCBtn, isVietnamese ? "Đóng bớt" : "Show less");
        updateButtonText(showLessCPUBtn, isVietnamese ? "Đóng bớt" : "Show less");
        updateButtonText(showLessMainboardBtn, isVietnamese ? "Đóng bớt" : "Show less");
        updateButtonText(showLessRAMBtn, isVietnamese ? "Đóng bớt" : "Show less");
        updateButtonText(showLessHDDBtn, isVietnamese ? "Đóng bớt" : "Show less");
        updateButtonText(showLessSSDBtn, isVietnamese ? "Đóng bớt" : "Show less");
        updateButtonText(showLessVGABtn, isVietnamese ? "Đóng bớt" : "Show less");
        updateButtonText(showLessPSUBtn, isVietnamese ? "Đóng bớt" : "Show less");
        updateButtonText(showLessCoolingBtn, isVietnamese ? "Đóng bớt" : "Show less");
        updateButtonText(showLessCaseBtn, isVietnamese ? "Đóng bớt" : "Show less");
        updateButtonText(showLessMonitorBtn, isVietnamese ? "Đóng bớt" : "Show less");
        updateButtonText(showLessKeyboardBtn, isVietnamese ? "Đóng bớt" : "Show less");
        updateButtonText(showLessMouseBtn, isVietnamese ? "Đóng bớt" : "Show less");
        updateButtonText(showLessHeadphoneBtn, isVietnamese ? "Đóng bớt" : "Show less");

        // ===== Cập nhật tiêu đề phần sản phẩm nổi bật =====
        Label featuredTitle = findLabelWithText(rootPane, "Sản phẩm nổi bật", "Featured Products");
        if (featuredTitle != null) {
            featuredTitle.setText(isVietnamese ? "Sản phẩm nổi bật" : "Featured Products");
        }

        // ===== Cập nhật footer =====
        Label footerLabel = findFooterLabel();
        if (footerLabel != null) {
            footerLabel.setText("© 2025 CELLCOMP STORE. " +
                    (isVietnamese ? "Đã đăng ký bản quyền." : "All rights reserved."));
        }
    }


    // Thêm phương thức mới để tìm tất cả các Button
    private List<Button> findAllButtons(Parent parent) {
        List<Button> buttons = new ArrayList<>();
        if (parent == null) return buttons;

        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Button) {
                buttons.add((Button) node);
            }
            if (node instanceof Parent) {
                buttons.addAll(findAllButtons((Parent) node));
            }
        }
        return buttons;
    }

    // Thêm phương thức mới để cập nhật giao diện sắp xếp từ trang chủ
    private void updateSortingInterface() {
        if (rootPane == null) return;

        // Tìm trong AnchorPane của ScrollPane
        if (centerScrollPane != null && centerScrollPane.getContent() instanceof AnchorPane) {
            AnchorPane contentPane = (AnchorPane) centerScrollPane.getContent();
            for (Node node : contentPane.getChildren()) {
                if (node instanceof VBox) {
                    VBox mainContainer = (VBox) node;

                    // Cập nhật tiêu đề sắp xếp
                    for (Node child : mainContainer.getChildren()) {
                        if (child instanceof Label) {
                            Label label = (Label) child;
                            String currentText = label.getText();
                            if (currentText != null) {
                                // Tiêu đề sắp xếp giá giảm dần/tăng dần
                                if (currentText.contains("SẢN PHẨM THEO GIÁ GIẢM DẦN") ||
                                        currentText.contains("PRODUCTS SORTED BY PRICE DESCENDING")) {
                                    label.setText(isVietnamese ? "SẢN PHẨM THEO GIÁ GIẢM DẦN ↓" :
                                            "PRODUCTS SORTED BY PRICE DESCENDING ↓");
                                } else if (currentText.contains("SẢN PHẨM THEO GIÁ TĂNG DẦN") ||
                                        currentText.contains("PRODUCTS SORTED BY PRICE ASCENDING")) {
                                    label.setText(isVietnamese ? "SẢN PHẨM THEO GIÁ TĂNG DẦN ↑" :
                                            "PRODUCTS SORTED BY PRICE ASCENDING ↑");
                                }
                                // Các tiêu đề danh mục
                                else if (currentText.contains("SẢN PHẨM NỔI BẬT") ||
                                        currentText.contains("FEATURED PRODUCTS")) {
                                    if (currentText.contains("GIÁ GIẢM DẦN") || currentText.contains("PRICE DESCENDING")) {
                                        label.setText(isVietnamese ? "SẢN PHẨM NỔI BẬT - GIÁ GIẢM DẦN" :
                                                "FEATURED PRODUCTS - PRICE DESCENDING");
                                    } else if (currentText.contains("GIÁ TĂNG DẦN") ||
                                            currentText.contains("PRICE ASCENDING")) {
                                        label.setText(isVietnamese ? "SẢN PHẨM NỔI BẬT - GIÁ TĂNG DẦN" :
                                                "FEATURED PRODUCTS - PRICE ASCENDING");
                                    } else {
                                        label.setText(isVietnamese ? "SẢN PHẨM NỔI BẬT" : "FEATURED PRODUCTS");
                                    }
                                }
                                // Tiêu đề kết quả lọc giá
                                else if (currentText.contains("SẢN PHẨM TRONG KHOẢNG GIÁ") ||
                                        currentText.contains("PRODUCTS IN PRICE RANGE")) {
                                    // Trích xuất phần khoảng giá
                                    String priceRange = "";
                                    if (currentText.contains(":")) {
                                        priceRange = currentText.substring(currentText.indexOf(":") + 1).trim();
                                    }
                                    label.setText(isVietnamese ? "SẢN PHẨM TRONG KHOẢNG GIÁ: " + priceRange :
                                            "PRODUCTS IN PRICE RANGE: " + priceRange);
                                }
                                // Tiêu đề kết quả lọc
                                else if (currentText.contains("KẾT QUẢ LỌC") ||
                                        currentText.contains("FILTER RESULTS")) {
                                    String remainingText = "";
                                    if (currentText.contains("-")) {
                                        remainingText = currentText.substring(currentText.indexOf("-")).trim();
                                    }
                                    label.setText(isVietnamese ? "KẾT QUẢ LỌC" + remainingText :
                                            "FILTER RESULTS" + remainingText);
                                }
                            }
                        }

                        // Cập nhật các nút và thanh công cụ trong các container
                        if (child instanceof HBox) {
                            HBox hbox = (HBox) child;
                            for (Node toolNode : hbox.getChildren()) {
                                // Cập nhật các nút trong thanh công cụ
                                if (toolNode instanceof Button) {
                                    Button button = (Button) toolNode;
                                    String buttonText = button.getText();
                                    if (buttonText != null) {
                                        if (buttonText.contains("Giá tăng dần") || buttonText.contains("Price ascending")) {
                                            button.setText(isVietnamese ? "Giá tăng dần ↑" : "Price ascending ↑");
                                        } else if (buttonText.contains("Giá giảm dần") || buttonText.contains("Price descending")) {
                                            button.setText(isVietnamese ? "Giá giảm dần ↓" : "Price descending ↓");
                                        } else if (buttonText.contains("Áp dụng") || buttonText.contains("Apply")) {
                                            button.setText(isVietnamese ? "Áp dụng" : "Apply");
                                        } else if (buttonText.contains("Đặt lại") || buttonText.contains("Reset")) {
                                            button.setText(isVietnamese ? "Đặt lại" : "Reset");
                                        } else if (buttonText.contains("Xóa tất cả bộ lọc") || buttonText.contains("Clear all filters")) {
                                            button.setText(isVietnamese ? "Xóa tất cả bộ lọc" : "Clear all filters");
                                        }
                                    }
                                }
                                // Cập nhật các label trong thanh công cụ
                                else if (toolNode instanceof Label) {
                                    Label label = (Label) toolNode;
                                    String labelText = label.getText();
                                    if (labelText != null) {
                                        if (labelText.contains("Sắp xếp:") || labelText.contains("Sort:")) {
                                            label.setText(isVietnamese ? "Sắp xếp:" : "Sort:");
                                        } else if (labelText.contains("Giá:") || labelText.contains("Price:")) {
                                            label.setText(isVietnamese ? "Giá:" : "Price:");
                                        } else if (labelText.contains("Bộ lọc đang áp dụng:") || labelText.contains("Active filters:")) {
                                            label.setText(isVietnamese ? "Bộ lọc đang áp dụng:" : "Active filters:");
                                        } else if (labelText.contains("Thương hiệu") || labelText.contains("Brand")) {
                                            label.setText(isVietnamese ? "Thương hiệu" : "Brand");
                                        } else if (labelText.contains("Không có bộ lọc") || labelText.contains("No filters applied")) {
                                            label.setText(isVietnamese ? "Không có bộ lọc" : "No filters applied");
                                        } else if (labelText.contains("Tìm thấy") || labelText.contains("Found")) {
                                            int productCount = -1;
                                            try {
                                                // Trích xuất số lượng sản phẩm
                                                String countStr = labelText.replaceAll("\\D+", " ").trim().split(" ")[0];
                                                productCount = Integer.parseInt(countStr);
                                            } catch (Exception e) {
                                                // Không lấy được số lượng
                                            }

                                            if (productCount >= 0) {
                                                label.setText(isVietnamese ?
                                                        "Tìm thấy " + productCount + " sản phẩm" :
                                                        "Found " + productCount + " products");
                                            }
                                        }
                                    }
                                }
                                // Xử lý các HBox lồng nhau
                                else if (toolNode instanceof HBox) {
                                    updateLanguageInHBox((HBox) toolNode);
                                }
                                // Xử lý MenuButton trong thanh công cụ
                                else if (toolNode instanceof MenuButton) {
                                    MenuButton menuButton = (MenuButton) toolNode;
                                    String menuText = menuButton.getText();
                                    if (menuText != null) {
                                        if (menuText.contains("Thương hiệu") || menuText.contains("Brand")) {
                                            menuButton.setText(isVietnamese ? "Thương hiệu" : "Brand");
                                        } else if (menuText.contains("Hãng") || menuText.contains("Brand")) {
                                            menuButton.setText(isVietnamese ? "Hãng" : "Brand");
                                        }
                                    }

                                    // Cập nhật các MenuItem trong MenuButton
                                    for (MenuItem menuItem : menuButton.getItems()) {
                                        String itemText = menuItem.getText();
                                        if (itemText != null) {
                                            if (itemText.equals("Tất cả") || itemText.equals("All")) {
                                                menuItem.setText(isVietnamese ? "Tất cả" : "All");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Thêm phương thức để cập nhật ngôn ngữ trong các HBox lồng nhau
    private void updateLanguageInHBox(HBox hbox) {
        for (Node node : hbox.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                String buttonText = button.getText();
                if (buttonText != null) {
                    if (buttonText.contains("Giá tăng dần") || buttonText.contains("Price ascending")) {
                        button.setText(isVietnamese ? "Giá tăng dần ↑" : "Price ascending ↑");
                    } else if (buttonText.contains("Giá giảm dần") || buttonText.contains("Price descending")) {
                        button.setText(isVietnamese ? "Giá giảm dần ↓" : "Price descending ↓");
                    } else if (buttonText.contains("Áp dụng") || buttonText.contains("Apply")) {
                        button.setText(isVietnamese ? "Áp dụng" : "Apply");
                    } else if (buttonText.contains("Đặt lại") || buttonText.contains("Reset")) {
                        button.setText(isVietnamese ? "Đặt lại" : "Reset");
                    }
                }
            } else if (node instanceof Label) {
                Label label = (Label) node;
                String labelText = label.getText();
                if (labelText != null) {
                    if (labelText.contains("Sắp xếp:") || labelText.contains("Sort:")) {
                        label.setText(isVietnamese ? "Sắp xếp:" : "Sort:");
                    } else if (labelText.contains("Giá:") || labelText.contains("Price:")) {
                        label.setText(isVietnamese ? "Giá:" : "Price:");
                    }
                }
            } else if (node instanceof TextField) {
                TextField textField = (TextField) node;
                String promptText = textField.getPromptText();
                if (promptText != null) {
                    if (promptText.contains("Từ") || promptText.contains("From")) {
                        textField.setPromptText(isVietnamese ? "Từ" : "From");
                    } else if (promptText.contains("Đến") || promptText.contains("To")) {
                        textField.setPromptText(isVietnamese ? "Đến" : "To");
                    }
                }
            }
        }
    }

    // Phương thức tìm label theo style class
    private Label findLabelByStyleClass(Parent parent, String viText, String enText) {
        if (parent == null) return null;

        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if (label.getText() != null &&
                        (label.getText().equals(viText) || label.getText().equals(enText))) {
                    return label;
                }
            }
            if (node instanceof Parent) {
                Label result = findLabelByStyleClass((Parent) node, viText, enText);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    // Phương thức tìm footer label
    private Label findFooterLabel() {
        if (rootPane == null) return null;

        Node bottomNode = rootPane.getBottom();
        if (bottomNode instanceof AnchorPane) {
            for (Node node : ((AnchorPane) bottomNode).getChildren()) {
                if (node instanceof Label) {
                    return (Label) node;
                }
            }
        }
        return null;
    }

    // Thêm biến toàn cục để theo dõi trạng thái ngôn ngữ hiện tại
    private static boolean isEnglish = false;

    /**
     * Phương thức chuyển đổi ngôn ngữ và đảm bảo đồng bộ giữa các màn hình
     */
    @FXML
    private void switchLanguage() {
        // Đảo trạng thái ngôn ngữ
        isVietnamese = !isVietnamese;

        // Lưu trạng thái vào LanguageManager để dùng chung
        LanguageManager.setVietnamese(isVietnamese);
        System.out.println("HomeController đã thay đổi ngôn ngữ thành: " + (isVietnamese ? "Tiếng Việt" : "English"));

        // Cập nhật hình ảnh lá cờ và text dựa trên ngôn ngữ hiện tại
        if (languageSwitchMenuItem != null) {
            try {
                // Đường dẫn tới hình ảnh cờ - QUAN TRỌNG: Hiển thị cờ ngược với ngôn ngữ hiện tại
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

        // Cập nhật toàn bộ giao diện người dùng
        updateLanguage();

        // Cập nhật danh mục
        updateCategoriesLanguage();

        // Tải lại danh mục với ngôn ngữ mới
        loadCategories();

        // Tải lại dữ liệu với ngôn ngữ mới
        loadProducts();
    }

    private void loadCategories() {
        // Kiểm tra null trước khi truy cập
        if (categoryListContainer != null) {
            categoryListContainer.getChildren().clear();

            try {
                // Thêm tùy chọn "Tất cả sản phẩm"/"All products"
                CheckBox allCategoryCheckBox = new CheckBox(isVietnamese ? "Tất cả sản phẩm" : "All products");
                allCategoryCheckBox.setSelected(true);
                categoryListContainer.getChildren().add(allCategoryCheckBox);

                // Thêm event handler cho checkbox "tất cả"
                allCategoryCheckBox.setOnAction(event -> {
                    if (allCategoryCheckBox.isSelected()) {
                        // Bỏ chọn các danh mục khác
                        for (int i = 1; i < categoryListContainer.getChildren().size(); i++) {
                            if (categoryListContainer.getChildren().get(i) instanceof CheckBox) {
                                ((CheckBox) categoryListContainer.getChildren().get(i)).setSelected(false);
                            }
                        }
                        loadProducts(); // Tải lại tất cả sản phẩm
                    }
                });

                // Lấy và hiển thị danh sách danh mục với hỗ trợ song ngữ
                List<Category> categories = categoryService.getAllCategories();
                for (Category category : categories) {
                    // Chuyển đổi tên danh mục theo ngôn ngữ
                    String categoryName = translateCategoryName(category.getCategoryName(), isVietnamese);

                    // Tạo CheckBox cho danh mục
                    CheckBox categoryCheckbox = new CheckBox(categoryName);
                    categoryCheckbox.setUserData(category.getCategoryID());

                    // Thêm event handler cho checkbox danh mục
                    categoryCheckbox.setOnAction(event -> {
                        if (categoryCheckbox.isSelected()) {
                            // Bỏ chọn "Tất cả"
                            allCategoryCheckBox.setSelected(false);
                            // Lọc theo danh mục
                            filterProductsByCategory(category.getCategoryID());
                        } else {
                            // Kiểm tra xem có danh mục nào được chọn không
                            boolean anySelected = false;
                            for (int i = 1; i < categoryListContainer.getChildren().size(); i++) {
                                if (categoryListContainer.getChildren().get(i) instanceof CheckBox &&
                                        ((CheckBox) categoryListContainer.getChildren().get(i)).isSelected()) {
                                    anySelected = true;
                                    break;
                                }
                            }

                            // Nếu không có danh mục nào được chọn, chọn lại "Tất cả"
                            if (!anySelected) {
                                allCategoryCheckBox.setSelected(true);
                                loadProducts();
                            }
                        }
                    });

                    categoryListContainer.getChildren().add(categoryCheckbox);
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showError(
                        isVietnamese ? "Lỗi" : "Error",
                        isVietnamese ? "Không thể tải danh mục sản phẩm" : "Could not load categories"
                );
            }
        }
    }

    // Thêm phương thức riêng để cập nhật ngôn ngữ cho các danh mục
    private void updateCategoriesLanguage() {
        // Tìm VBox chứa các danh mục trong categoryScrollPane
        if (categoryScrollPane != null && categoryScrollPane.getContent() instanceof VBox) {
            VBox categoryVBox = (VBox) categoryScrollPane.getContent();

            for (Node node : categoryVBox.getChildren()) {
                if (node instanceof Button) {
                    Button categoryButton = (Button) node;
                    String buttonId = categoryButton.getId();
                    if (buttonId != null) {
                        // Đổi text cho từng loại danh mục
                        switch (buttonId) {
                            case "category-keyboard":
                                categoryButton.setText(isVietnamese ? "Bàn phím" : "Keyboard");
                                break;
                            case "category-mouse":
                                categoryButton.setText(isVietnamese ? "Chuột" : "Mouse");
                                break;
                            case "category-monitor":
                                categoryButton.setText(isVietnamese ? "Màn hình" : "Monitor");
                                break;
                            case "category-cooling":
                                categoryButton.setText(isVietnamese ? "Tản nhiệt" : "Cooling");
                                break;
                            case "category-psu":
                                categoryButton.setText(isVietnamese ? "Nguồn" : "Power Supply");
                                break;
                            case "category-headphone":
                                categoryButton.setText(isVietnamese ? "Tai nghe" : "Headphones");
                                break;
                            // Các nút khác như laptop, pc, cpu, ram, case giữ nguyên vì là thuật ngữ quốc tế
                        }
                    }
                }
            }
        }
    }

    /**
     * Phương thức mới để chuyển đổi tên danh mục theo ngôn ngữ
     */
    private String translateCategoryName(String categoryName, boolean toVietnamese) {
        // Đối chiếu tên danh mục theo ngôn ngữ
        Map<String, String> categoryTranslations = new HashMap<>();

        // Thêm các cặp tên danh mục Anh-Việt
        categoryTranslations.put("Laptop", "Laptop"); // Giữ nguyên vì tên quốc tế
        categoryTranslations.put("PC", "PC"); // Giữ nguyên vì tên quốc tế
        categoryTranslations.put("CPU", "CPU"); // Giữ nguyên vì tên quốc tế
        categoryTranslations.put("Mainboard", "Mainboard"); // Giữ nguyên vì tên quốc tế
        categoryTranslations.put("RAM", "RAM"); // Giữ nguyên vì tên quốc tế
        categoryTranslations.put("HDD", "HDD"); // Giữ nguyên vì tên quốc tế
        categoryTranslations.put("SSD", "SSD"); // Giữ nguyên vì tên quốc tế
        categoryTranslations.put("VGA", "VGA"); // Giữ nguyên vì tên quốc tế
        categoryTranslations.put("Case", "Case"); // Giữ nguyên vì tên quốc tế

        // Các tên cần dịch
        categoryTranslations.put("Power Supply", "Nguồn");
        categoryTranslations.put("Nguồn", "Power Supply");
        categoryTranslations.put("Cooling", "Tản nhiệt");
        categoryTranslations.put("Tản nhiệt", "Cooling");
        categoryTranslations.put("Monitor", "Màn hình");
        categoryTranslations.put("Màn hình", "Monitor");
        categoryTranslations.put("Keyboard", "Bàn phím");
        categoryTranslations.put("Bàn phím", "Keyboard");
        categoryTranslations.put("Mouse", "Chuột");
        categoryTranslations.put("Chuột", "Mouse");
        categoryTranslations.put("Headphones", "Tai nghe");
        categoryTranslations.put("Tai nghe", "Headphones");

        // Nếu là tiếng Việt, kiểm tra xem có tiếng Anh tương ứng không
        if (toVietnamese) {
            // Giả sử categoryName đang là tiếng Anh, tìm tiếng Việt
            for (Map.Entry<String, String> entry : categoryTranslations.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(categoryName)) {
                    return entry.getValue();
                }
            }
        } else {
            // Giả sử categoryName đang là tiếng Việt, tìm tiếng Anh
            for (Map.Entry<String, String> entry : categoryTranslations.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(categoryName)) {
                    return entry.getKey();
                }
            }
        }

        // Nếu không tìm thấy bản dịch, giữ nguyên tên ban đầu
        return categoryName;
    }

    // Thêm phương thức mới để tìm button theo ID
    private Button findButtonById(Parent parent, String id) {
        if (parent == null) return null;

        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Button && id.equals(node.getId())) {
                return (Button) node;
            }

            if (node instanceof Parent) {
                Button result = findButtonById((Parent) node, id);
                if (result != null) return result;
            }
        }
        return null;
    }

    // Thêm phương thức để tìm button theo text
    private Button findButtonWithText(Parent parent, String viText, String enText) {
        if (parent == null) return null;

        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                if (button.getText() != null && (button.getText().contains(viText) || button.getText().contains(enText))) {
                    return button;
                }
            }
            if (node instanceof Parent) {
                Button result = findButtonWithText((Parent) node, viText, enText);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    // Phương thức hỗ trợ cập nhật text cho button
    private void updateButtonText(Button button, String text) {
        if (button != null) {
            button.setText(text);
        }
    }

    @FXML
    private void toggleCategory(ActionEvent event) {
        if (categoryPane == null || slideTransition == null) return;

        // Dừng animation hiện tại nếu đang chạy
        slideTransition.stop();

        // Xử lý animation mở/đóng
        if (isCategoryOpen) {
            // ĐÓNG panel
            slideTransition.setFromX(0);
            slideTransition.setToX(-225);

            // Thiết lập để ẩn panel sau khi animation kết thúc
            slideTransition.setOnFinished(e -> categoryPane.setVisible(false));
        } else {
            // Hiển thị panel trước khi bắt đầu animation
            categoryPane.setVisible(true);

            // MỞ panel
            slideTransition.setFromX(-225);
            slideTransition.setToX(0);

            // Xóa handler khi kết thúc nếu có
            slideTransition.setOnFinished(null);
        }

        // Chạy animation
        slideTransition.play();

        // Đảo trạng thái
        isCategoryOpen = !isCategoryOpen;
    }

    @FXML
    void toggleCategory() {
        toggleCategory(null);
    }

    private void updateMousePosition(MouseEvent event) {
        mouseY = event.getSceneY();
    }

    // Cập nhật phương thức filterByCategory

    @FXML
    private void filterByCategory(ActionEvent event) {
        if (event.getSource() instanceof Button) {
            Button sourceButton = (Button) event.getSource();
            String categoryName = sourceButton.getText();
            System.out.println("Đang lọc theo danh mục: " + categoryName);

            try {
                // Thiết lập trang hiện tại là CATEGORY
                currentPage = Page.CATEGORY;
                currentCategory = categoryName;

                // Lấy danh sách sản phẩm theo danh mục
                List<Product> products = ProductRepository.getProductsByCategory(categoryName);
                System.out.println("Tìm thấy " + products.size() + " sản phẩm trong danh mục: " + categoryName);

                if (products.isEmpty()) {
                    AlertUtils.showWarning(
                            isVietnamese ? "Không có sản phẩm" : "No products",
                            isVietnamese ? "Không tìm thấy sản phẩm nào trong danh mục: " + categoryName :
                                    "No products found in category: " + categoryName
                    );
                    return;
                }

                // Hiển thị giao diện lọc và sắp xếp riêng cho danh mục
                displayCategoryWithFilters(categoryName, products);

                // Đóng thanh danh mục sau khi chọn
                if (isCategoryOpen) {
                    toggleCategory();
                }

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showError(
                        isVietnamese ? "Lỗi" : "Error",
                        isVietnamese ? "Không thể lọc sản phẩm theo danh mục: " + e.getMessage() :
                                "Cannot filter products by category: " + e.getMessage()
                );
            }
        }
    }
    /**
     * Lọc sản phẩm trong danh mục theo brand
     */
    private void filterCategoryByBrand(String categoryName, String brand) {
        try {
            // Get all products in the category
            List<Product> allCategoryProducts = ProductRepository.getProductsByCategory(categoryName);

            // Filter by selected brand
            List<Product> filteredProducts = allCategoryProducts.stream()
                    .filter(p -> p.getProductName().startsWith(brand))
                    .collect(Collectors.toList());

            if (filteredProducts.isEmpty()) {
                AlertUtils.showInfo(
                        isVietnamese ? "Thông báo" : "Information",
                        isVietnamese ? "Không có sản phẩm nào thuộc hãng " + brand + " trong danh mục này." :
                                "No products from brand " + brand + " in this category."
                );
                // Reset brand selection if no products found
                currentBrand = null;
                return;
            }

            // Apply price filter if exists
            if (currentMinPrice != null || currentMaxPrice != null) {
                filteredProducts = filterProducts(filteredProducts);
            }

            // Display filtered results while maintaining all brands in dropdown
            displayCategoryWithFilters(categoryName, filteredProducts);

            // Important: We don't need to rebuild the brand dropdown here
            // It will be rebuilt with all brands in displayCategoryWithFilters

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể lọc sản phẩm theo hãng: " + e.getMessage() :
                            "Cannot filter products by brand: " + e.getMessage()
            );
        }
    }
    /**
     * Lọc sản phẩm trong danh mục theo khoảng giá
     */
    private void filterCategoryByPrice(String categoryName, Double min, Double max) {
        try {
            // Lấy sản phẩm ban đầu (đã lọc theo brand nếu có)
            List<Product> baseProducts;

            if (currentBrand != null) {
                // Nếu đang lọc theo brand, lấy tất cả sản phẩm trong danh mục và lọc theo brand
                List<Product> allCategoryProducts = ProductRepository.getProductsByCategory(categoryName);
                baseProducts = allCategoryProducts.stream()
                        .filter(p -> p.getProductName().startsWith(currentBrand))
                        .collect(Collectors.toList());
            } else {
                // Nếu không lọc theo brand, lấy tất cả sản phẩm trong danh mục
                baseProducts = ProductRepository.getProductsByCategory(categoryName);
            }

            // Lưu giá trị lọc hiện tại
            currentMinPrice = min;
            currentMaxPrice = max;

            // Lọc theo giá
            List<Product> filteredProducts = baseProducts;

            if (min != null) {
                filteredProducts = filteredProducts.stream()
                        .filter(p -> p.getPrice() >= min)
                        .collect(Collectors.toList());
            }

            if (max != null) {
                filteredProducts = filteredProducts.stream()
                        .filter(p -> p.getPrice() <= max)
                        .collect(Collectors.toList());
            }

            if (filteredProducts.isEmpty()) {
                AlertUtils.showWarning(
                        isVietnamese ? "Không có sản phẩm" : "No products",
                        isVietnamese ? "Không tìm thấy sản phẩm nào trong khoảng giá đã chọn" :
                                "No products found in the selected price range"
                );
                return;
            }

            // Hiển thị kết quả lọc
            displayCategoryWithFilters(categoryName, filteredProducts);

            // ĐÃ LOẠI BỎ: Thông báo lọc thành công

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể lọc sản phẩm theo giá: " + e.getMessage() :
                            "Cannot filter products by price: " + e.getMessage()
            );
        }
    }
    /**
     * Sắp xếp sản phẩm trong danh mục theo giá
     */
    private void sortCategoryProducts(String categoryName, boolean ascending) {
        try {
            // Cập nhật trạng thái sắp xếp hiện tại
            sortOrder = ascending ? SortOrder.ASCENDING : SortOrder.DESCENDING;

            // Lấy sản phẩm cơ sở (đã lọc theo brand và giá nếu có)
            List<Product> baseProducts;

            if (currentBrand != null || currentMinPrice != null || currentMaxPrice != null) {
                // Nếu đang có lọc
                List<Product> allCategoryProducts = ProductRepository.getProductsByCategory(categoryName);
                baseProducts = allCategoryProducts;

                // Áp dụng lọc theo brand nếu có
                if (currentBrand != null) {
                    baseProducts = baseProducts.stream()
                            .filter(p -> p.getProductName().startsWith(currentBrand))
                            .collect(Collectors.toList());
                }

                // Áp dụng lọc theo giá nếu có
                if (currentMinPrice != null) {
                    baseProducts = baseProducts.stream()
                            .filter(p -> p.getPrice() >= currentMinPrice)
                            .collect(Collectors.toList());
                }

                if (currentMaxPrice != null) {
                    baseProducts = baseProducts.stream()
                            .filter(p -> p.getPrice() <= currentMaxPrice)
                            .collect(Collectors.toList());
                }
            } else {
                // Nếu không có lọc, lấy tất cả sản phẩm trong danh mục
                baseProducts = ProductRepository.getProductsByCategory(categoryName);
            }

            // Sắp xếp sản phẩm theo giá
            if (ascending) {
                baseProducts.sort(Comparator.comparing(Product::getPrice));
            } else {
                baseProducts.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
            }

            // Hiển thị kết quả đã sắp xếp
            displayCategoryWithFilters(categoryName, baseProducts);

            // ĐÃ LOẠI BỎ: Thông báo sắp xếp thành công

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể sắp xếp sản phẩm: " + e.getMessage() :
                            "Cannot sort products: " + e.getMessage()
            );
        }
    }
    /**
     * Tạo bộ lọc brand riêng cho danh mục, LUÔN hiển thị tất cả các brand
     */
    private MenuButton createCategoryBrandFilter(List<Product> products) {
        // Keep the existing MenuButton styling
        MenuButton brandFilter = new MenuButton(isVietnamese ? "Thương hiệu" : "Brand");
        brandFilter.setStyle("-fx-background-color: linear-gradient(to right, #865DFF, #5CB8E4); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; " +
                "-fx-padding: 8 15; -fx-border-radius: 20; -fx-border-color: #000000; -fx-border-width: 1px;");
        brandFilter.setCursor(Cursor.HAND);

        try {
            // Get ALL brands for this category from the database instead of only from currently filtered products
            String categoryName = currentCategory; // Use the current category being viewed
            List<Product> allCategoryProducts = ProductRepository.getProductsByCategory(categoryName);
            Set<String> categoryBrands = extractBrandsFromProducts(allCategoryProducts);

            // If there's a current brand selected, update the button text
            if (currentBrand != null) {
                brandFilter.setText(isVietnamese ? "Thương hiệu: " + currentBrand : "Brand: " + currentBrand);
            }

            // Add "All" option
            MenuItem allItem = new MenuItem(isVietnamese ? "Tất cả" : "All");
            allItem.setStyle("-fx-font-size: 14px; -fx-padding: 5px 15px;");
            allItem.setOnAction(e -> {
                // Reset brand filter and refresh products
                currentBrand = null;
                brandFilter.setText(isVietnamese ? "Thương hiệu" : "Brand");
                if (currentCategory != null) {
                    filterByCategoryName(currentCategory);
                }
            });
            brandFilter.getItems().add(allItem);

            // Add separator
            brandFilter.getItems().add(new SeparatorMenuItem());

            // Add each brand from the category
            for (String brand : categoryBrands) {
                String menuText = brand;

                // Mark the currently selected brand with a checkmark
                if (brand.equals(currentBrand)) {
                    menuText = "✓ " + brand;
                }

                MenuItem brandItem = new MenuItem(menuText);
                brandItem.setStyle("-fx-font-size: 14px; -fx-padding: 5px 15px;");

                // Highlight the current brand
                if (brand.equals(currentBrand)) {
                    brandItem.setStyle("-fx-font-size: 14px; -fx-padding: 5px 15px; -fx-font-weight: bold; -fx-background-color: #E3F2FD;");
                }

                final String brandName = brand;
                brandItem.setOnAction(e -> {
                    // Update the current brand and refresh with this filter
                    currentBrand = brandName;
                    brandFilter.setText(isVietnamese ? "Thương hiệu: " + brandName : "Brand: " + brandName);
                    filterCategoryByBrand(currentCategory, brandName);
                });
                brandFilter.getItems().add(brandItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle error gracefully - add a disabled menu item showing the error
            MenuItem errorItem = new MenuItem(isVietnamese ? "Lỗi tải thương hiệu" : "Error loading brands");
            errorItem.setDisable(true);
            brandFilter.getItems().add(errorItem);
        }

        return brandFilter;
    }
    @FXML
    private void handleCategoryMouseEntered(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: linear-gradient(to right, #7B68EE, #48D1CC); " +
                "-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 10; " +
                "-fx-padding: 6 5 6 15; -fx-min-width: 180; -fx-alignment: center-left; -fx-cursor: hand; " +
                "-fx-border-color: white; -fx-border-width: 1.5; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.5), 6, 0, 0, 0);");
    }

    @FXML
    private void handleCategoryMouseExited(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 10; " +
                "-fx-padding: 6 5 6 15; -fx-min-width: 180; -fx-alignment: center-left; " +
                "-fx-cursor: hand; -fx-border-color: black; -fx-border-width: 1; -fx-border-radius: 10;");
    }

    /**
     * Trích xuất danh sách brand từ sản phẩm (lấy từ từ đầu tiên trong tên sản phẩm)
     */
    private Set<String> extractBrandsFromProducts(List<Product> products) {
        Set<String> brands = new TreeSet<>(); // Sử dụng TreeSet để sắp xếp theo bảng chữ cái

        for (Product product : products) {
            String productName = product.getProductName();
            if (productName != null && !productName.isEmpty()) {
                // Lấy từ đầu tiên trong tên sản phẩm làm tên brand
                String brand = productName.split("\\s+")[0];
                if (brand != null && !brand.isEmpty()) {
                    brands.add(brand);
                }
            }
        }

        return brands;
    }
    /**
     * Hiển thị sản phẩm theo danh mục với giao diện lọc và sắp xếp
     */
    private void displayCategoryWithFilters(String categoryName, List<Product> products) {
        try {
            System.out.println("Hiển thị danh mục: " + categoryName + " với " + products.size() + " sản phẩm");

            // 1. Lấy AnchorPane chính từ ScrollPane
            AnchorPane contentPane = (AnchorPane) centerScrollPane.getContent();
            if (contentPane == null) return;
            contentPane.getChildren().clear();

            // 2. Tạo container chính
            VBox mainContainer = new VBox(20);
            mainContainer.setPadding(new Insets(20));

            // 3. Hiển thị tiêu đề danh mục ở GIỮA MÀN HÌNH
            Label categoryTitle = new Label(categoryName.toUpperCase());
            categoryTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #E53935;");
            // Đặt tiêu đề vào HBox để căn giữa
            HBox titleBox = new HBox();
            titleBox.setAlignment(Pos.CENTER);
            titleBox.getChildren().add(categoryTitle);
            mainContainer.getChildren().add(titleBox);

            // 4. Tạo container cho thanh công cụ lọc và sắp xếp
            HBox toolBarContainer = new HBox();
            toolBarContainer.setAlignment(Pos.CENTER);
            toolBarContainer.setPrefWidth(contentPane.getWidth() - 40);

            // 5. Tạo thanh công cụ với thiết kế cải tiến
            HBox toolBar = new HBox(15); // Giảm khoảng cách giữa các phần để tiết kiệm không gian
            toolBar.setPadding(new Insets(15, 15, 15, 15)); // Giảm padding để tối ưu không gian
            toolBar.setAlignment(Pos.CENTER);
            toolBar.setStyle("-fx-background-color: #F9F9F9; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 0);");

            // 5.1 Dropdown Brand với thiết kế nổi bật
            MenuButton brandFilter = createCategoryBrandFilter(products);
            brandFilter.setPrefWidth(160); // Điều chỉnh chiều rộng để vừa đủ
            brandFilter.setStyle("-fx-background-color: linear-gradient(to right, #865DFF, #5CB8E4); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; " +
                    "-fx-padding: 8 15; -fx-border-radius: 20; -fx-border-color: #000000; -fx-border-width: 1px;");
            brandFilter.setCursor(Cursor.HAND);

            // Hiệu ứng hover cho brand filter
            brandFilter.setOnMouseEntered(e -> {
                brandFilter.setStyle("-fx-background-color: linear-gradient(to right, #7044DD, #3AA0D8); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; " +
                        "-fx-padding: 8 15; -fx-border-radius: 20; -fx-border-color: #000000; -fx-border-width: 1px;");
            });

            brandFilter.setOnMouseExited(e -> {
                brandFilter.setStyle("-fx-background-color: linear-gradient(to right, #865DFF, #5CB8E4); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; " +
                        "-fx-padding: 8 15; -fx-border-radius: 20; -fx-border-color: #000000; -fx-border-width: 1px;");
            });

            // 5.2 Phần sắp xếp với nút rộng hơn và hiệu ứng màu khi chọn
            HBox sortSection = new HBox(8); // Giảm khoảng cách giữa các nút sắp xếp
            sortSection.setAlignment(Pos.CENTER);

            // Nút sắp xếp tăng dần - sử dụng chữ đầy đủ thay vì mũi tên
            Button ascendingButton = new Button(isVietnamese ? "Giá tăng dần" : "Price ascending");
            ascendingButton.setPrefWidth(130); // Giảm chiều rộng để tiết kiệm không gian
            // Kiểm tra trạng thái sắp xếp hiện tại
            boolean isAscendingActive = (sortOrder == SortOrder.ASCENDING);
            ascendingButton.setStyle("-fx-background-color: " + (isAscendingActive ? "#4CAF50" : "#F0F0F0") +
                    "; -fx-text-fill: " + (isAscendingActive ? "white" : "#333333") +
                    "; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 10;");
            ascendingButton.setCursor(Cursor.HAND);

            // Hiệu ứng hover cho nút tăng dần
            ascendingButton.setOnMouseEntered(e -> {
                if (!isAscendingActive) {
                    ascendingButton.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 10;");
                }
            });

            ascendingButton.setOnMouseExited(e -> {
                if (!isAscendingActive) {
                    ascendingButton.setStyle("-fx-background-color: #F0F0F0; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 10;");
                } else {
                    ascendingButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 10;");
                }
            });

            // Nút sắp xếp giảm dần - sử dụng chữ đầy đủ thay vì mũi tên
            Button descendingButton = new Button(isVietnamese ? "Giá giảm dần" : "Price descending");
            descendingButton.setPrefWidth(130); // Giảm chiều rộng để tiết kiệm không gian
            // Kiểm tra trạng thái sắp xếp hiện tại
            boolean isDescendingActive = (sortOrder == SortOrder.DESCENDING);
            descendingButton.setStyle("-fx-background-color: " + (isDescendingActive ? "#F44336" : "#F0F0F0") +
                    "; -fx-text-fill: " + (isDescendingActive ? "white" : "#333333") +
                    "; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 10;");
            descendingButton.setCursor(Cursor.HAND);

            // Hiệu ứng hover cho nút giảm dần
            descendingButton.setOnMouseEntered(e -> {
                if (!isDescendingActive) {
                    descendingButton.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 10;");
                }
            });

            descendingButton.setOnMouseExited(e -> {
                if (!isDescendingActive) {
                    descendingButton.setStyle("-fx-background-color: #F0F0F0; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 10;");
                } else {
                    descendingButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 10;");
                }
            });

            // Xử lý sự kiện cho nút sắp xếp
            ascendingButton.setOnAction(e -> sortCategoryProducts(categoryName, true));
            descendingButton.setOnAction(e -> sortCategoryProducts(categoryName, false));

            // Thêm các phần tử vào phần sắp xếp
            sortSection.getChildren().addAll(ascendingButton, descendingButton);

            // 5.3 Phần lọc giá với thiết kế đẹp - giữ nguyên dạng cũ
            HBox priceSection = new HBox(8); // Giảm khoảng cách
            priceSection.setAlignment(Pos.CENTER);

            Label priceLabel = new Label(isVietnamese ? "Giá:" : "Price:");
            priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            // Ô nhập giá từ
            TextField fromField = new TextField();
            fromField.setPromptText(isVietnamese ? "Từ" : "From");
            fromField.setPrefWidth(100); // Thu nhỏ lại để tiết kiệm không gian
            fromField.setStyle("-fx-background-radius: 20; -fx-padding: 6; -fx-border-color: #CCCCCC; -fx-border-radius: 20;");

            // Ô nhập giá đến
            TextField toField = new TextField();
            toField.setPromptText(isVietnamese ? "Đến" : "To");
            toField.setPrefWidth(100); // Thu nhỏ lại để tiết kiệm không gian
            toField.setStyle("-fx-background-radius: 20; -fx-padding: 6; -fx-border-color: #CCCCCC; -fx-border-radius: 20;");

            // Nút áp dụng lọc giá với chiều rộng đủ
            Button applyPriceButton = new Button(isVietnamese ? "Áp dụng" : "Apply");
            applyPriceButton.setPrefWidth(80); // Thu nhỏ lại để tiết kiệm không gian
            applyPriceButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 10;");
            applyPriceButton.setCursor(Cursor.HAND);

            // Hiệu ứng hover cho nút áp dụng
            applyPriceButton.setOnMouseEntered(e -> {
                applyPriceButton.setStyle("-fx-background-color: #0D8BF2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 10;");
            });

            applyPriceButton.setOnMouseExited(e -> {
                applyPriceButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 10;");
            });

            // Xử lý sự kiện cho nút áp dụng giá
            applyPriceButton.setOnAction(e -> {
                try {
                    String fromText = fromField.getText().trim().replace(",", "");
                    String toText = toField.getText().trim().replace(",", "");

                    Double min = fromText.isEmpty() ? null : Double.parseDouble(fromText);
                    Double max = toText.isEmpty() ? null : Double.parseDouble(toText);

                    if (min != null && max != null && min > max) {
                        AlertUtils.showWarning(
                                isVietnamese ? "Cảnh báo" : "Warning",
                                isVietnamese ? "Giá từ không được lớn hơn giá đến" : "Min price cannot be greater than max price"
                        );
                        return;
                    }

                    filterCategoryByPrice(categoryName, min, max);
                } catch (NumberFormatException ex) {
                    AlertUtils.showWarning(
                            isVietnamese ? "Cảnh báo" : "Warning",
                            isVietnamese ? "Vui lòng nhập giá hợp lệ" : "Please enter valid price"
                    );
                }
            });

            // Thêm các phần tử vào phần lọc giá
            priceSection.getChildren().addAll(priceLabel, fromField, toField, applyPriceButton);

            // 5.4 Nút reload
            Button reloadButton = new Button();
            reloadButton.setStyle("-fx-background-color: transparent;");
            reloadButton.setCursor(Cursor.HAND);

            ImageView reloadIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/reload.png")));
            reloadIcon.setFitWidth(20); // Thu nhỏ biểu tượng refresh để tiết kiệm không gian
            reloadIcon.setFitHeight(20);
            reloadButton.setGraphic(reloadIcon);
            reloadButton.setTooltip(new Tooltip(isVietnamese ? "Tải lại" : "Reload"));

            // Xử lý sự kiện cho nút reload
            reloadButton.setOnAction(e -> {
                // Reset bộ lọc và hiển thị lại tất cả sản phẩm trong danh mục
                currentBrand = null;
                currentMinPrice = null;
                currentMaxPrice = null;
                sortOrder = SortOrder.NONE;

                // Tải lại tất cả sản phẩm trong danh mục
                List<Product> allCategoryProducts = ProductRepository.getProductsByCategory(categoryName);
                displayCategoryWithFilters(categoryName, allCategoryProducts);
            });

            // Thêm các phần vào toolbar - Sử dụng HBox riêng cho mỗi phần để kiểm soát khoảng cách tốt hơn
            HBox brandSection = new HBox();
            brandSection.setAlignment(Pos.CENTER);
            brandSection.getChildren().add(brandFilter);

            // Thêm Separator mỏng hơn
            Separator sep1 = new Separator(Orientation.VERTICAL);
            sep1.setPrefHeight(30);

            Separator sep2 = new Separator(Orientation.VERTICAL);
            sep2.setPrefHeight(30);

            HBox reloadSection = new HBox();
            reloadSection.setAlignment(Pos.CENTER);
            reloadSection.getChildren().add(reloadButton);

            // Ghép tất cả phần vào toolbar với khoảng cách phù hợp
            toolBar.getChildren().addAll(
                    brandSection,
                    sep1,
                    sortSection,
                    sep2,
                    priceSection,
                    reloadSection
            );

            // Thêm toolbar vào container
            toolBarContainer.getChildren().add(toolBar);
            mainContainer.getChildren().add(toolBarContainer);

            // 6. Hiển thị số lượng sản phẩm tìm thấy
            HBox countBox = new HBox();
            countBox.setAlignment(Pos.CENTER);
            Label countLabel = new Label(String.format(
                    isVietnamese ? "Tìm thấy %d sản phẩm" : "Found %d products",
                    products.size()
            ));
            countLabel.setStyle("-fx-font-size: 16px; -fx-font-style: italic;");
            countBox.getChildren().add(countLabel);
            mainContainer.getChildren().add(countBox);

            // 7. Hiển thị sản phẩm trong FlowPane với căn giữa
            FlowPane productsContainer = new FlowPane();
            productsContainer.setAlignment(Pos.CENTER); // Căn giữa các sản phẩm
            productsContainer.setHgap(20);
            productsContainer.setVgap(20);
            productsContainer.setPrefWrapLength(contentPane.getWidth() - 40);

            // Thêm từng sản phẩm vào container
            for (Product product : products) {
                VBox productCard = createProductCard(product);
                productsContainer.getChildren().add(productCard);
            }

            mainContainer.getChildren().add(productsContainer);

            // 8. Thêm container chính vào contentPane
            contentPane.getChildren().add(mainContainer);
            AnchorPane.setTopAnchor(mainContainer, 0.0);
            AnchorPane.setLeftAnchor(mainContainer, 0.0);
            AnchorPane.setRightAnchor(mainContainer, 0.0);

            // 9. Cuộn lên đầu trang
            centerScrollPane.setVvalue(0.0);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể hiển thị danh mục với bộ lọc: " + e.getMessage() :
                            "Cannot display category with filters: " + e.getMessage()
            );
        }
    }

    private void hideHomePageElements() {
        try {
            // CÁCH 1: Truy cập trực tiếp các biến đã được khai báo
            // Ẩn phần sản phẩm nổi bật nếu đang hiển thị
            if (featuredProductGrid != null) {
                // Tìm parent của featuredProductGrid
                Parent parent = featuredProductGrid.getParent();
                if (parent != null && parent instanceof AnchorPane) {
                    // Ẩn AnchorPane chứa featuredProductGrid
                    parent.setVisible(false);
                }
            }

            // Ẩn container danh mục sản phẩm
            if (categoryProductsContainer != null) {
                categoryProductsContainer.setVisible(false);
            }

            // CÁCH 2: Tìm kiếm động qua DOM (khi các biến không truy cập được trực tiếp)
            if (centerScrollPane != null && centerScrollPane.getContent() != null) {
                // Ẩn các banner (ImageView)
                Parent anchorPane = (Parent) centerScrollPane.getContent().lookup("AnchorPane");
                if (anchorPane != null) {
                    for (Node node : anchorPane.getChildrenUnmodifiable()) {
                        if (node instanceof ImageView) {
                            node.setVisible(false);
                        }
                    }
                }

                // Tìm và ẩn phần sản phẩm nổi bật qua VBox
                VBox vbox = (VBox) centerScrollPane.getContent().lookup("VBox");
                if (vbox != null && vbox.getChildren().size() > 1) {
                    // Tìm label "Sản phẩm nổi bật" và AnchorPane chứa sản phẩm nổi bật
                    for (int i = 0; i < vbox.getChildren().size() - 1; i++) {
                        Node node = vbox.getChildren().get(i);
                        if (node instanceof Label && ((Label) node).getText().contains("Sản phẩm nổi bật")) {
                            node.setVisible(false);
                            // Ẩn AnchorPane ngay sau label
                            if (i + 1 < vbox.getChildren().size() && vbox.getChildren().get(i + 1) instanceof AnchorPane) {
                                vbox.getChildren().get(i + 1).setVisible(false);
                            }
                            break;
                        }
                    }
                }
            }

            System.out.println("Đã ẩn thành công các phần tử trang chủ để hiển thị kết quả sắp xếp");

        } catch (Exception e) {
            System.err.println("Lỗi khi ẩn phần tử trang chủ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hiển thị tiêu đề danh mục đã chọn
     */
    private void showCategoryTitle(String category) {
        Label categoryTitleLabel = new Label(category);
        // Chỉnh sửa style: căn giữa và màu đỏ
        categoryTitleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #FF0000; -fx-alignment: center;");

        // Thay thế nội dung hiện có bằng tiêu đề danh mục
        AnchorPane contentPane = (AnchorPane) centerScrollPane.getContent();
        VBox container = new VBox(20);
        container.setPadding(new Insets(20, 40, 20, 40));
        container.setAlignment(Pos.CENTER); // Căn giữa các phần tử con
        container.getChildren().add(categoryTitleLabel);

        // Đặt VBox mới vào nội dung ScrollPane
        contentPane.getChildren().clear();
        contentPane.getChildren().add(container);
        AnchorPane.setTopAnchor(container, 20.0);
        AnchorPane.setLeftAnchor(container, 0.0);
        AnchorPane.setRightAnchor(container, 0.0);

        // Lưu container để sử dụng cho các phần khác
        filteredContentContainer = container;
    }

    /**
     * Thiết lập giao diện để hiển thị nội dung đã lọc
     */
    private void setupFilteredContentView(String category) {
        // Lấy AnchorPane chính từ ScrollPane
        AnchorPane contentPane = (AnchorPane) centerScrollPane.getContent();
        if (contentPane == null) return;

        // Xóa nội dung hiện tại
        contentPane.getChildren().clear();

        // Tạo container mới cho nội dung đã lọc
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));

        // Tiêu đề danh mục
        Label categoryTitleLabel = new Label(category);
        categoryTitleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #E53935;");
        container.getChildren().add(categoryTitleLabel);

        // Thêm container vào contentPane
        contentPane.getChildren().add(container);
        AnchorPane.setTopAnchor(container, 0.0);
        AnchorPane.setLeftAnchor(container, 0.0);
        AnchorPane.setRightAnchor(container, 0.0);

        // Cập nhật biến filteredContentContainer
        filteredContentContainer = container;
    }

    /**
     * Hiển thị trang sắp xếp giá khi người dùng chọn từ trang chủ
     */
    private void displayPriceSort(boolean ascending) {
        try {
            // 1. Lấy AnchorPane chính từ ScrollPane
            AnchorPane contentPane = (AnchorPane) centerScrollPane.getContent();
            if (contentPane == null) return;
            contentPane.getChildren().clear();

            // 2. Tạo container chính
            VBox mainContainer = new VBox(20);
            mainContainer.setPadding(new Insets(20));

            // 3. Tạo thanh công cụ với thiết kế cân đối
            HBox toolBarContainer = new HBox();
            toolBarContainer.setAlignment(Pos.CENTER); // Căn giữa toàn bộ thanh công cụ
            toolBarContainer.setPrefWidth(contentPane.getWidth() - 40);

            // 4. Tạo thanh công cụ CHỈ với sắp xếp và lọc giá (KHÔNG CÓ BRAND)
            HBox toolBar = new HBox(15);
            toolBar.setPadding(new Insets(15, 20, 15, 20));
            toolBar.setAlignment(Pos.CENTER);
            toolBar.setStyle("-fx-background-color: #F9F9F9; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 0);");

            // 4.1 Phần sắp xếp với chức năng toggle
            HBox sortSection = new HBox(10);
            sortSection.setAlignment(Pos.CENTER);

            Label sortLabel = new Label(isVietnamese ? "Sắp xếp: " : "Sort: ");
            sortLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            // Nút sắp xếp tăng dần với thiết kế mới
            Button ascendingButton = new Button(isVietnamese ? "Giá tăng dần ↑" : "Price ascending ↑");
            ascendingButton.setStyle("-fx-background-color: " + (sortOrder == SortOrder.ASCENDING ? "#4CAF50" : "#F0F0F0") +
                    "; -fx-text-fill: " + (sortOrder == SortOrder.ASCENDING ? "white" : "#333333") +
                    "; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 15;");
            ascendingButton.setCursor(Cursor.HAND); // Thêm con trỏ tay

            // Hiệu ứng hover cho nút tăng dần
            ascendingButton.setOnMouseEntered(e -> {
                if (sortOrder != SortOrder.ASCENDING) {
                    ascendingButton.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 15;");
                }
            });
            ascendingButton.setOnMouseExited(e -> {
                if (sortOrder != SortOrder.ASCENDING) {
                    ascendingButton.setStyle("-fx-background-color: #F0F0F0; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 15;");
                }
            });

            ascendingButton.setOnAction(e -> {
                if (sortOrder == SortOrder.ASCENDING) {
                    sortOrder = SortOrder.NONE;
                    displayPriceSort(true);
                } else {
                    sortOrder = SortOrder.ASCENDING;
                    List<Product> allProducts = ProductRepository.getAllProducts();
                    allProducts.sort(Comparator.comparing(Product::getPrice));
                    displaySortedResults(allProducts, true);
                }
            });

            // Nút sắp xếp giảm dần với thiết kế mới
            Button descendingButton = new Button(isVietnamese ? "Giá giảm dần ↓" : "Price descending ↓");
            descendingButton.setStyle("-fx-background-color: " + (sortOrder == SortOrder.DESCENDING ? "#F44336" : "#F0F0F0") +
                    "; -fx-text-fill: " + (sortOrder == SortOrder.DESCENDING ? "white" : "#333333") +
                    "; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 15;");
            descendingButton.setCursor(Cursor.HAND); // Thêm con trỏ tay

            // Hiệu ứng hover cho nút giảm dần
            descendingButton.setOnMouseEntered(e -> {
                if (sortOrder != SortOrder.DESCENDING) {
                    descendingButton.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 15;");
                }
            });
            descendingButton.setOnMouseExited(e -> {
                if (sortOrder != SortOrder.DESCENDING) {
                    descendingButton.setStyle("-fx-background-color: #F0F0F0; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 15;");
                }
            });

            descendingButton.setOnAction(e -> {
                if (sortOrder == SortOrder.DESCENDING) {
                    sortOrder = SortOrder.NONE;
                    displayPriceSort(true);
                } else {
                    sortOrder = SortOrder.DESCENDING;
                    List<Product> allProducts = ProductRepository.getAllProducts();
                    allProducts.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                    displaySortedResults(allProducts, false);
                }
            });

            // Thêm các phần tử vào phần sắp xếp
            sortSection.getChildren().addAll(sortLabel, ascendingButton, descendingButton);

            // 4.2 Phần lọc giá với thiết kế mới
            HBox priceSection = new HBox(10);
            priceSection.setAlignment(Pos.CENTER);

            Label priceLabel = new Label(isVietnamese ? "Giá: " : "Price: ");
            priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            // Ô nhập giá từ
            TextField fromField = new TextField();
            if (currentMinPrice != null) {
                NumberFormat nf = NumberFormat.getInstance();
                nf.setGroupingUsed(false);
                fromField.setText(nf.format(currentMinPrice));
            }
            fromField.setPromptText(isVietnamese ? "Từ" : "From");
            fromField.setPrefWidth(120);
            fromField.setStyle("-fx-background-radius: 20; -fx-padding: 8; -fx-border-color: #CCCCCC; -fx-border-radius: 20;");

            // Ô nhập giá đến
            TextField toField = new TextField();
            if (currentMaxPrice != null) {
                NumberFormat nf = NumberFormat.getInstance();
                nf.setGroupingUsed(false);
                toField.setText(nf.format(currentMaxPrice));
            }
            toField.setPromptText(isVietnamese ? "Đến" : "To");
            toField.setPrefWidth(120);
            toField.setStyle("-fx-background-radius: 20; -fx-padding: 8; -fx-border-color: #CCCCCC; -fx-border-radius: 20;");

            // Nút áp dụng lọc giá với thiết kế mới
            Button applyPriceButton = new Button(isVietnamese ? "Áp dụng" : "Apply");
            applyPriceButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20;");
            applyPriceButton.setCursor(Cursor.HAND); // Thêm con trỏ tay

            // Hiệu ứng hover cho nút áp dụng
            applyPriceButton.setOnMouseEntered(e -> {
                applyPriceButton.setStyle("-fx-background-color: #0D8BF2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20;");
            });
            applyPriceButton.setOnMouseExited(e -> {
                applyPriceButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20;");
            });

            // 4.3 Tạo nút reload với thiết kế mới
            Button reloadButton = new Button();
            reloadButton.setStyle("-fx-background-color: transparent;");
            reloadButton.setCursor(Cursor.HAND); // Thêm con trỏ tay

            ImageView reloadIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/example/stores/images/reload.png")));
            reloadIcon.setFitWidth(20);
            reloadIcon.setFitHeight(20);
            reloadButton.setGraphic(reloadIcon);
            reloadButton.setTooltip(new Tooltip(isVietnamese ? "Tải lại" : "Reload"));

            // Phần xử lý sự kiện cho nút áp dụng
            applyPriceButton.setOnAction(e -> {
                try {
                    String fromText = fromField.getText().trim().replace(",", "");
                    String toText = toField.getText().trim().replace(",", "");

                    Double min = fromText.isEmpty() ? null : Double.parseDouble(fromText);
                    Double max = toText.isEmpty() ? null : Double.parseDouble(toText);

                    if (min != null && max != null && min > max) {
                        AlertUtils.showWarning(
                                isVietnamese ? "Cảnh báo" : "Warning",
                                isVietnamese ? "Giá từ không được lớn hơn giá đến" : "Min price cannot be greater than max price"
                        );
                        return;
                    }

                    currentMinPrice = min;
                    currentMaxPrice = max;

                    // Hiển thị nút reload sau khi áp dụng lọc giá
                    if (!priceSection.getChildren().contains(reloadButton) && (min != null || max != null)) {
                        priceSection.getChildren().add(reloadButton);
                    }

                    applyFiltersToAllProducts();
                } catch (NumberFormatException ex) {
                    AlertUtils.showWarning(
                            isVietnamese ? "Cảnh báo" : "Warning",
                            isVietnamese ? "Vui lòng nhập giá hợp lệ" : "Please enter valid price"
                    );
                }
            });

            // Xử lý sự kiện cho nút reload
            reloadButton.setOnAction(e -> {
                fromField.clear();
                toField.clear();
                currentMinPrice = null;
                currentMaxPrice = null;

                // Xóa nút reload
                if (priceSection.getChildren().contains(reloadButton)) {
                    priceSection.getChildren().remove(reloadButton);
                }

                // Quay về trạng thái không lọc với kiểu sắp xếp hiện tại
                if (sortOrder != SortOrder.NONE) {
                    List<Product> allProducts = ProductRepository.getAllProducts();
                    if (sortOrder == SortOrder.ASCENDING) {
                        allProducts.sort(Comparator.comparing(Product::getPrice));
                        displaySortedResults(allProducts, true);
                    } else {
                        allProducts.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                        displaySortedResults(allProducts, false);
                    }
                } else {
                    displayPriceSort(true); // Tải lại giao diện không sắp xếp
                }
            });

            // Thêm các phần tử vào phần lọc giá
            priceSection.getChildren().addAll(priceLabel, fromField, toField, applyPriceButton);

            // Chỉ hiển thị nút reload nếu đã áp dụng lọc giá
            if (currentMinPrice != null || currentMaxPrice != null) {
                priceSection.getChildren().add(reloadButton);
            }

            // Thêm các phần vào toolbar
            toolBar.getChildren().addAll(sortSection, new Separator(Orientation.VERTICAL), priceSection);

            // Thêm toolbar vào container
            toolBarContainer.getChildren().add(toolBar);
            mainContainer.getChildren().add(toolBarContainer);


            // 5. Hiển thị số lượng sản phẩm
            List<Product> allProducts = ProductRepository.getAllProducts();

            // Áp dụng sắp xếp nếu cần
            if (sortOrder == SortOrder.ASCENDING) {
                allProducts.sort(Comparator.comparing(Product::getPrice));
            } else if (sortOrder == SortOrder.DESCENDING) {
                allProducts.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
            }

            // Hiển thị số lượng sản phẩm tìm thấy
            Label countLabel = new Label(isVietnamese ?
                    "Tìm thấy " + allProducts.size() + " sản phẩm" :
                    "Found " + allProducts.size() + " products");
            countLabel.setStyle("-fx-font-size: 16px; -fx-font-style: italic;");
            mainContainer.getChildren().add(countLabel);

            // 6. THÊM PHẦN SẢN PHẨM NỔI BẬT theo yêu cầu
            VBox featuredSection = new VBox(15);
            Label featuredTitle = new Label(isVietnamese ? "SẢN PHẨM NỔI BẬT" : "FEATURED PRODUCTS");
            featuredTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFA000;");
            featuredSection.getChildren().add(featuredTitle);

            // Lấy và hiển thị sản phẩm nổi bật
            List<Product> featuredProducts = ProductRepository.getFeaturedProducts(4);

            // Sắp xếp sản phẩm nổi bật nếu có yêu cầu
            if (sortOrder == SortOrder.ASCENDING) {
                featuredProducts.sort(Comparator.comparing(Product::getPrice));
            } else if (sortOrder == SortOrder.DESCENDING) {
                featuredProducts.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
            }

            FlowPane featuredGrid = new FlowPane();
            featuredGrid.setHgap(20);
            featuredGrid.setVgap(20);

            for (Product product : featuredProducts) {
                VBox productCard = createProductCard(product);

                // Thêm nhãn "HOT"
                Label hotLabel = new Label("HOT");
                hotLabel.setStyle(
                        "-fx-background-color: #FF5722; -fx-text-fill: white; " +
                                "-fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10;"
                );
                ((VBox)productCard).getChildren().add(0, hotLabel);

                featuredGrid.getChildren().add(productCard);
            }

            featuredSection.getChildren().add(featuredGrid);
            mainContainer.getChildren().add(featuredSection);

            // 7. THÊM PHẦN CÁC DANH MỤC SẢN PHẨM theo yêu cầu
            // Lấy danh sách categories
            List<Category> allCategories = categoryService.getAllCategories();

            // Hiển thị từng danh mục
            for (Category category : allCategories) {
                String categoryName = category.getCategoryName();

                // Lấy sản phẩm theo danh mục
                List<Product> categoryProducts = allProducts.stream()
                        .filter(p -> p.getCategoryName().equalsIgnoreCase(categoryName))
                        .collect(Collectors.toList());

                if (categoryProducts.isEmpty()) continue;

                // Tạo section cho danh mục
                VBox categorySection = new VBox(15);
                Label categoryTitle = new Label(categoryName.toUpperCase());
                categoryTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1976D2;");
                categorySection.getChildren().add(categoryTitle);

                // Hiển thị grid sản phẩm cho danh mục
                FlowPane categoryGrid = new FlowPane();
                categoryGrid.setHgap(20);
                categoryGrid.setVgap(20);

                for (Product product : categoryProducts) {
                    VBox productCard = createProductCard(product);
                    categoryGrid.getChildren().add(productCard);
                }

                categorySection.getChildren().add(categoryGrid);
                mainContainer.getChildren().add(categorySection);
            }

            // 8. Thêm container vào contentPane
            contentPane.getChildren().add(mainContainer);
            AnchorPane.setTopAnchor(mainContainer, 0.0);
            AnchorPane.setLeftAnchor(mainContainer, 0.0);
            AnchorPane.setRightAnchor(mainContainer, 0.0);

            // 9. Cuộn về đầu trang
            centerScrollPane.setVvalue(0.0);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể hiển thị trang sắp xếp: " + e.getMessage() :
                            "Cannot display sort page: " + e.getMessage()
            );
        }
    }

    // Biến để lưu trữ container cho nội dung được lọc
    private VBox filteredContentContainer;

    /**
     * Hiển thị thanh lọc theo hãng với thiết kế nhất quán
     */
    private void showBrandFilter(List<Product> products, String category) {
        // Tạo dropdown cho hãng - sử dụng style giống với nút giỏ hàng
        MenuButton brandFilter = createBrandFilterButton(products);

        // Tạo thanh sắp xếp
        Button ascendingButton = new Button(isVietnamese ? "Giá tăng dần ↑" : "Price ascending ↑");
        ascendingButton.setStyle("-fx-background-color: #DDDDDD; -fx-text-fill: black;");
        ascendingButton.setOnAction(e -> sortProductsInCurrentView(true));

        Button descendingButton = new Button(isVietnamese ? "Giá giảm dần ↓" : "Price descending ↓");
        descendingButton.setStyle("-fx-background-color: #DDDDDD; -fx-text-fill: black;");
        descendingButton.setOnAction(e -> sortProductsInCurrentView(false));

        // Tạo chức năng lọc giá
        Label priceLabel = new Label(isVietnamese ? "Giá:" : "Price:");
        priceLabel.setStyle("-fx-font-weight: bold;");

        TextField fromField = new TextField();
        fromField.setPromptText(isVietnamese ? "Từ" : "From");
        fromField.setPrefWidth(100);

        TextField toField = new TextField();
        toField.setPromptText(isVietnamese ? "Đến" : "To");
        toField.setPrefWidth(100);

        Button applyPriceButton = new Button(isVietnamese ? "Áp dụng" : "Apply");
        applyPriceButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        applyPriceButton.setOnAction(e -> {
            try {
                Double min = fromField.getText().isEmpty() ? null : Double.parseDouble(fromField.getText());
                Double max = toField.getText().isEmpty() ? null : Double.parseDouble(toField.getText());

                currentMinPrice = min;
                currentMaxPrice = max;
                currentCategory = category;
                applyFiltersInCurrentView();
            } catch (NumberFormatException ex) {
                AlertUtils.showWarning(
                        isVietnamese ? "Cảnh báo" : "Warning",
                        isVietnamese ? "Vui lòng nhập giá hợp lệ" : "Please enter valid price"
                );
            }
        });

        // Tạo HBox chứa các bộ lọc
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(15, 0, 15, 0));
        filterBar.setStyle("-fx-background-color: #F9F9F9; -fx-background-radius: 5; -fx-padding: 10;");

        // Thêm tất cả control vào filter bar
        filterBar.getChildren().addAll(
                brandFilter,
                new Separator(Orientation.VERTICAL),
                new Label(isVietnamese ? "Sắp xếp:" : "Sort:"), ascendingButton, descendingButton,
                new Separator(Orientation.VERTICAL),
                priceLabel, fromField, toField, applyPriceButton
        );

        // Thêm thanh lọc vào container
        filteredContentContainer.getChildren().add(filterBar);
    }
    /**
     * Lấy danh sách các hãng từ tên sản phẩm
     */
    private Set<String> extractBrands(List<Product> products) {
        Set<String> brands = new TreeSet<>(); // TreeSet để sắp xếp theo thứ tự chữ cái

        for (Product product : products) {
            String productName = product.getProductName();
            if (productName != null && !productName.isEmpty()) {
                // Lấy từ đầu tiên trong tên sản phẩm làm tên hãng
                String brand = productName.split(" ")[0];
                if (brand != null && !brand.isEmpty()) {
                    brands.add(brand);
                }
            }
        }

        return brands;
    }

    private void filterProductsByBrand(List<Product> allProducts, String brand, String category) {
        // Lưu các thông tin lọc
        currentBrand = brand;
        currentCategory = category;

        // Tìm các sản phẩm thuộc hãng đã chọn
        List<Product> filteredByBrand = allProducts.stream()
                .filter(product -> product.getProductName().startsWith(brand))
                .collect(Collectors.toList());

        // Lưu danh sách đã lọc
        currentFilteredProducts = filteredByBrand;

        // Hiển thị kết quả lọc
        displayFilteredResults();
    }

    /**
     * Hiển thị danh sách sản phẩm đã lọc
     */
    private void displayFilteredProducts(List<Product> products) {
        // Xóa phần hiển thị sản phẩm cũ (nếu có)
        if (filteredContentContainer.getChildren().size() > 2) {
            filteredContentContainer.getChildren().remove(2, filteredContentContainer.getChildren().size());
        }

        // Tạo GridPane để hiển thị sản phẩm
        GridPane productGrid = new GridPane();
        productGrid.setHgap(20);
        productGrid.setVgap(20);

        int columns = 4; // Số sản phẩm mỗi hàng
        int row = 0;
        int col = 0;

        for (Product product : products) {
            VBox productCard = createProductCard(product);
            productGrid.add(productCard, col, row);

            col++;
            if (col == columns) {
                col = 0;
                row++;
            }
        }

        // Thêm GridPane vào container
        filteredContentContainer.getChildren().add(productGrid);
    }

    @FXML
    private void sortAscending() {
        System.out.println("Đang sắp xếp sản phẩm theo giá tăng dần...");
        sortProductsInCurrentView(true);
    }

    @FXML
    private void sortDescending() {
        System.out.println("Đang sắp xếp sản phẩm theo giá giảm dần...");
        sortProductsInCurrentView(false);
    }

    /**
     * Hiển thị sản phẩm theo danh mục với bộ lọc hãng đúng
     */
    private void showCategoryProducts(String category) {
        try {
            // 1. Chuẩn bị giao diện
            setupFilteredContentView(category);

            // 2. Lấy sản phẩm của danh mục
            List<Product> categoryProducts = ProductRepository.getProductsByCategory(category);

            // 3. Lưu trạng thái danh mục hiện tại
            currentCategory = category;

            // 4. Hiển thị bộ lọc brand với TẤT CẢ các hãng có trong hệ thống
            // (không chỉ các hãng trong danh mục này)
            MenuButton brandFilter = createBrandFilterButton(ProductRepository.getAllProducts());

            // 5. Thêm thanh sắp xếp và lọc giá
            HBox filterBar = new HBox(15);
            filterBar.setPadding(new Insets(10));
            filterBar.setAlignment(Pos.CENTER_LEFT);
            filterBar.setStyle("-fx-background-color: #F9F9F9; -fx-padding: 10; -fx-background-radius: 5;");

            // Nút sắp xếp
            Label sortLabel = new Label(isVietnamese ? "Sắp xếp:" : "Sort:");
            sortLabel.setStyle("-fx-font-weight: bold;");

            Button ascendingButton = new Button(isVietnamese ? "Giá tăng dần ↑" : "Price ascending ↑");
            ascendingButton.setStyle("-fx-background-color: #DDDDDD;");
            ascendingButton.setOnAction(e -> sortProductsInCurrentView(true));

            Button descendingButton = new Button(isVietnamese ? "Giá giảm dần ↓" : "Price descending ↓");
            descendingButton.setStyle("-fx-background-color: #DDDDDD;");
            descendingButton.setOnAction(e -> sortProductsInCurrentView(false));

            // Lọc giá
            Label priceLabel = new Label(isVietnamese ? "Giá:" : "Price:");
            priceLabel.setStyle("-fx-font-weight: bold;");

            TextField fromField = new TextField();
            fromField.setPromptText(isVietnamese ? "Từ" : "From");
            fromField.setPrefWidth(100);

            TextField toField = new TextField();
            toField.setPromptText(isVietnamese ? "Đến" : "To");
            toField.setPrefWidth(100);

            Button applyPriceButton = new Button(isVietnamese ? "Áp dụng" : "Apply");
            applyPriceButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            applyPriceButton.setOnAction(e -> {
                try {
                    Double min = fromField.getText().isEmpty() ? null : Double.parseDouble(fromField.getText());
                    Double max = toField.getText().isEmpty() ? null : Double.parseDouble(toField.getText());

                    currentMinPrice = min;
                    currentMaxPrice = max;
                    applyFiltersInCurrentView();
                } catch (NumberFormatException ex) {
                    AlertUtils.showWarning(
                            isVietnamese ? "Cảnh báo" : "Warning",
                            isVietnamese ? "Vui lòng nhập giá hợp lệ" : "Please enter valid price"
                    );
                }
            });

            // Nút reload
            Button reloadButton = new Button();
            reloadButton.setStyle("-fx-background-color: transparent;");

            ImageView reloadIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/example/stores/images/reload.png")));
            reloadIcon.setFitWidth(16);
            reloadIcon.setFitHeight(16);
            reloadButton.setGraphic(reloadIcon);
            reloadButton.setTooltip(new Tooltip(isVietnamese ? "Tải lại" : "Reload"));

            reloadButton.setOnAction(e -> {
                fromField.clear();
                toField.clear();
                currentMinPrice = null;
                currentMaxPrice = null;
                currentBrand = null;
                sortOrder = SortOrder.NONE;
                showCategoryProducts(category); // Tải lại trang danh mục
            });

            // Thêm tất cả components vào filter bar
            filterBar.getChildren().addAll(
                    brandFilter,
                    new Separator(Orientation.VERTICAL),
                    sortLabel, ascendingButton, descendingButton,
                    new Separator(Orientation.VERTICAL),
                    priceLabel, fromField, toField, applyPriceButton, reloadButton
            );

            filteredContentContainer.getChildren().add(filterBar);

            // 6. Hiển thị danh sách sản phẩm
            Label countLabel = new Label(String.format(
                    isVietnamese ? "Tìm thấy %d sản phẩm" : "Found %d products",
                    categoryProducts.size()
            ));
            countLabel.setStyle("-fx-font-size: 16px; -fx-font-style: italic;");
            filteredContentContainer.getChildren().add(countLabel);

            // Hiển thị sản phẩm
            displayFilteredProducts(categoryProducts);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể hiển thị sản phẩm: " + e.getMessage() :
                            "Cannot display products: " + e.getMessage()
            );
        }
    }
    // Thêm ngay sau enum SortOrder
    private enum Page { HOME, CATEGORY, SEARCH, PRICE_SORT, PRODUCT_DETAIL }

    // Thêm biến theo dõi trang hiện tại
    private Page currentPage = Page.HOME;
    /**
     * Sắp xếp sản phẩm trong view hiện tại với chức năng toggle
     */
    private void sortProductsInCurrentView(boolean ascending) {
        try {
            // Kiểm tra nếu đang ở cùng một trạng thái sắp xếp -> toggle off
            if ((ascending && sortOrder == SortOrder.ASCENDING) ||
                    (!ascending && sortOrder == SortOrder.DESCENDING)) {
                // Toggle off - quay về không sắp xếp
                sortOrder = SortOrder.NONE;

                // Nếu đang ở trang hiển thị sản phẩm theo giá hoặc trang chính
                if (currentPage == Page.HOME || currentPage == Page.PRICE_SORT) {
                    displayPriceSort(true); // Tham số không quan trọng vì sortOrder = NONE
                    return;
                }

                // Nếu đang ở trang danh mục
                if (currentCategory != null) {
                    showCategoryProducts(currentCategory);
                    return;
                }

                // Trở về trang chủ nếu không phải trường hợp nào
                loadProducts();
                return;
            }

            // Nếu không phải toggle off -> thực hiện sắp xếp
            sortOrder = ascending ? SortOrder.ASCENDING : SortOrder.DESCENDING;

            // Sắp xếp danh sách hiện tại
            List<Product> productsToSort;

            if (currentFilteredProducts != null) {
                // Sử dụng danh sách đã lọc nếu có
                productsToSort = new ArrayList<>(currentFilteredProducts);
            } else if (currentCategory != null) {
                // Lọc theo danh mục hiện tại nếu có
                productsToSort = ProductRepository.getProductsByCategory(currentCategory);
            } else {
                // Lấy tất cả sản phẩm
                productsToSort = ProductRepository.getAllProducts();
            }

            // Sắp xếp
            if (ascending) {
                productsToSort.sort(Comparator.comparing(Product::getPrice));
            } else {
                productsToSort.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
            }

            // Lưu lại kết quả đã sắp xếp
            currentFilteredProducts = productsToSort;

            // Hiển thị kết quả đã sắp xếp
            displaySortedResults(productsToSort, ascending);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể sắp xếp sản phẩm: " + e.getMessage() :
                            "Cannot sort products: " + e.getMessage()
            );
        }
    }

    /**
     * Hiển thị kết quả đã sắp xếp, đảm bảo không có nút Brand và có đầy đủ các mục
     */
    private void displaySortedResults(List<Product> sortedProducts, boolean ascending) {
        try {
            // 1. Lấy AnchorPane chính từ ScrollPane
            AnchorPane contentPane = (AnchorPane) centerScrollPane.getContent();
            if (contentPane == null) return;
            contentPane.getChildren().clear();

            // 2. Tạo container chính
            VBox mainContainer = new VBox(20);
            mainContainer.setPadding(new Insets(20));

            // 3. Tạo container cho thanh công cụ - căn giữa
            HBox toolBarContainer = new HBox();
            toolBarContainer.setAlignment(Pos.CENTER); // Căn giữa toàn bộ thanh công cụ
            toolBarContainer.setPrefWidth(contentPane.getWidth() - 40);

            // 4. Tạo thanh công cụ với thiết kế mới
            HBox toolBar = new HBox(15);
            toolBar.setPadding(new Insets(15, 20, 15, 20));
            toolBar.setAlignment(Pos.CENTER);
            toolBar.setStyle("-fx-background-color: #F9F9F9; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 0);");

            // 4.1 Phần sắp xếp
            HBox sortSection = new HBox(10);
            sortSection.setAlignment(Pos.CENTER);

            Label sortLabel = new Label(isVietnamese ? "Sắp xếp:" : "Sort:");
            sortLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            // Nút sắp xếp tăng dần với thiết kế mới
            Button ascendingButton = new Button(isVietnamese ? "Giá tăng dần ↑" : "Price ascending ↑");
            ascendingButton.setStyle("-fx-background-color: " + (ascending && sortOrder != SortOrder.NONE ? "#4CAF50" : "#F0F0F0") +
                    "; -fx-text-fill: " + (ascending && sortOrder != SortOrder.NONE ? "white" : "#333333") +
                    "; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 15;");
            ascendingButton.setCursor(Cursor.HAND); // Thêm con trỏ tay

            // Hiệu ứng hover cho nút tăng dần
            ascendingButton.setOnMouseEntered(e -> {
                if (!(ascending && sortOrder != SortOrder.NONE)) {
                    ascendingButton.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 15;");
                }
            });
            ascendingButton.setOnMouseExited(e -> {
                if (!(ascending && sortOrder != SortOrder.NONE)) {
                    ascendingButton.setStyle("-fx-background-color: #F0F0F0; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 15;");
                }
            });

            // Nút sắp xếp giảm dần với thiết kế mới
            Button descendingButton = new Button(isVietnamese ? "Giá giảm dần ↓" : "Price descending ↓");
            descendingButton.setStyle("-fx-background-color: " + (!ascending && sortOrder != SortOrder.NONE ? "#F44336" : "#F0F0F0") +
                    "; -fx-text-fill: " + (!ascending && sortOrder != SortOrder.NONE ? "white" : "#333333") +
                    "; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 15;");
            descendingButton.setCursor(Cursor.HAND); // Thêm con trỏ tay

            // Hiệu ứng hover cho nút giảm dần
            descendingButton.setOnMouseEntered(e -> {
                if (!(!ascending && sortOrder != SortOrder.NONE)) {
                    descendingButton.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 15;");
                }
            });
            descendingButton.setOnMouseExited(e -> {
                if (!(!ascending && sortOrder != SortOrder.NONE)) {
                    descendingButton.setStyle("-fx-background-color: #F0F0F0; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 15;");
                }
            });

            ascendingButton.setOnAction(e -> {
                if (sortOrder == SortOrder.ASCENDING) {
                    sortOrder = SortOrder.NONE;
                    displayPriceSort(true);
                } else {
                    sortOrder = SortOrder.ASCENDING;
                    List<Product> products = filterProducts(ProductRepository.getAllProducts());
                    products.sort(Comparator.comparing(Product::getPrice));
                    displaySortedResults(products, true);
                }
            });

            descendingButton.setOnAction(e -> {
                if (sortOrder == SortOrder.DESCENDING) {
                    sortOrder = SortOrder.NONE;
                    displayPriceSort(true);
                } else {
                    sortOrder = SortOrder.DESCENDING;
                    List<Product> products = filterProducts(ProductRepository.getAllProducts());
                    products.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                    displaySortedResults(products, false);
                }
            });

            // Thêm các phần tử vào phần sắp xếp
            sortSection.getChildren().addAll(sortLabel, ascendingButton, descendingButton);

            // 4.2 Phần lọc giá với thiết kế mới
            HBox priceSection = new HBox(10);
            priceSection.setAlignment(Pos.CENTER);

            Label priceLabel = new Label(isVietnamese ? "Giá:" : "Price:");
            priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            // Ô nhập giá từ
            TextField fromField = new TextField();
            if (currentMinPrice != null) {
                NumberFormat nf = NumberFormat.getInstance();
                nf.setGroupingUsed(false);
                fromField.setText(nf.format(currentMinPrice));
            }
            fromField.setPromptText(isVietnamese ? "Từ" : "From");
            fromField.setPrefWidth(120);
            fromField.setStyle("-fx-background-radius: 20; -fx-padding: 8; -fx-border-color: #CCCCCC; -fx-border-radius: 20;");

            // Ô nhập giá đến
            TextField toField = new TextField();
            if (currentMaxPrice != null) {
                NumberFormat nf = NumberFormat.getInstance();
                nf.setGroupingUsed(false);
                toField.setText(nf.format(currentMaxPrice));
            }
            toField.setPromptText(isVietnamese ? "Đến" : "To");
            toField.setPrefWidth(120);
            toField.setStyle("-fx-background-radius: 20; -fx-padding: 8; -fx-border-color: #CCCCCC; -fx-border-radius: 20;");

            // Nút áp dụng lọc giá với thiết kế mới
            Button applyPriceButton = new Button(isVietnamese ? "Áp dụng" : "Apply");
            applyPriceButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20;");
            applyPriceButton.setCursor(Cursor.HAND); // Thêm con trỏ tay

            // Hiệu ứng hover cho nút áp dụng
            applyPriceButton.setOnMouseEntered(e -> {
                applyPriceButton.setStyle("-fx-background-color: #0D8BF2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20;");
            });
            applyPriceButton.setOnMouseExited(e -> {
                applyPriceButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20;");
            });

            // 4.3 Tạo nút reload với thiết kế mới
            Button reloadButton = new Button();
            reloadButton.setStyle("-fx-background-color: transparent;");
            reloadButton.setCursor(Cursor.HAND); // Thêm con trỏ tay

            // FIX HERE: Sử dụng phương thức tải hình ảnh linh hoạt thay vì đường dẫn cứng
            ImageView reloadIcon;
            try {
                // Thử tải từ các đường dẫn khác nhau
                InputStream is = null;
                String[] potentialPaths = {
                        "/com/example/stores/images/layout/reload.png",
                        "/com/example/stores/images/products/reload.png",
                        "/com/example/stores/images/reload.png",
                        "/images/layout/reload.png",
                        "/images/products/reload.png",
                        "/images/reload.png"
                };

                for (String path : potentialPaths) {
                    is = getClass().getResourceAsStream(path);
                    if (is != null) {
                        System.out.println("Tìm thấy icon reload tại: " + path);
                        break;
                    }
                }

                // Nếu không tìm thấy icon, tạo icon mặc định
                if (is == null) {
                    System.out.println("Không tìm thấy icon reload, sử dụng icon mặc định");
                    Canvas canvas = new Canvas(20, 20);
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    gc.setFill(Color.LIGHTGRAY);
                    gc.fillOval(0, 0, 20, 20);
                    gc.setStroke(Color.BLACK);
                    gc.strokeOval(0, 0, 20, 20);
                    // Vẽ mũi tên đơn giản
                    gc.setStroke(Color.DARKGRAY);
                    gc.setLineWidth(2);
                    gc.beginPath();
                    gc.moveTo(5, 10);
                    gc.arcTo(10, 5, 15, 10, 5);
                    gc.moveTo(15, 10);
                    gc.lineTo(12, 7);
                    gc.moveTo(15, 10);
                    gc.lineTo(12, 13);
                    gc.stroke();

                    WritableImage img = new WritableImage(20, 20);
                    canvas.snapshot(null, img);
                    reloadIcon = new ImageView(img);
                } else {
                    // Nếu tìm thấy icon, sử dụng nó
                    reloadIcon = new ImageView(new Image(is));
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi tạo icon reload: " + e.getMessage());
                // Fallback nếu có lỗi
                Canvas canvas = new Canvas(20, 20);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setFill(Color.LIGHTGRAY);
                gc.fillOval(0, 0, 20, 20);
                gc.setStroke(Color.BLACK);
                gc.strokeOval(0, 0, 20, 20);
                WritableImage img = new WritableImage(20, 20);
                canvas.snapshot(null, img);
                reloadIcon = new ImageView(img);
            }

            reloadIcon.setFitWidth(20);
            reloadIcon.setFitHeight(20);
            reloadButton.setGraphic(reloadIcon);
            reloadButton.setTooltip(new Tooltip(isVietnamese ? "Tải lại" : "Reload"));

            // Phần xử lý sự kiện cho nút áp dụng
            applyPriceButton.setOnAction(e -> {
                try {
                    String fromText = fromField.getText().trim().replace(",", "");
                    String toText = toField.getText().trim().replace(",", "");

                    Double min = fromText.isEmpty() ? null : Double.parseDouble(fromText);
                    Double max = toText.isEmpty() ? null : Double.parseDouble(toText);

                    if (min != null && max != null && min > max) {
                        AlertUtils.showWarning(
                                isVietnamese ? "Cảnh báo" : "Warning",
                                isVietnamese ? "Giá từ không được lớn hơn giá đến" : "Min price cannot be greater than max price"
                        );
                        return;
                    }

                    currentMinPrice = min;
                    currentMaxPrice = max;

                    // Hiển thị nút reload sau khi áp dụng lọc giá
                    if (!priceSection.getChildren().contains(reloadButton) && (min != null || max != null)) {
                        priceSection.getChildren().add(reloadButton);
                    }

                    applyFiltersToAllProducts();
                } catch (NumberFormatException ex) {
                    AlertUtils.showWarning(
                            isVietnamese ? "Cảnh báo" : "Warning",
                            isVietnamese ? "Vui lòng nhập giá hợp lệ" : "Please enter valid price"
                    );
                }
            });

            // Xử lý sự kiện cho nút reload
            reloadButton.setOnAction(e -> {
                fromField.clear();
                toField.clear();
                currentMinPrice = null;
                currentMaxPrice = null;

                // Xóa nút reload
                if (priceSection.getChildren().contains(reloadButton)) {
                    priceSection.getChildren().remove(reloadButton);
                }

                // Quay về trạng thái không lọc với kiểu sắp xếp hiện tại
                if (sortOrder != SortOrder.NONE) {
                    List<Product> allProducts = ProductRepository.getAllProducts();
                    if (sortOrder == SortOrder.ASCENDING) {
                        allProducts.sort(Comparator.comparing(Product::getPrice));
                        displaySortedResults(allProducts, true);
                    } else {
                        allProducts.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                        displaySortedResults(allProducts, false);
                    }
                } else {
                    displayPriceSort(true);
                }
            });

            // Thêm các phần tử vào phần lọc giá
            priceSection.getChildren().addAll(priceLabel, fromField, toField, applyPriceButton);

            // Chỉ hiển thị nút reload nếu đã áp dụng lọc giá
            if (currentMinPrice != null || currentMaxPrice != null) {
                priceSection.getChildren().add(reloadButton);
            }

            // Thêm các phần vào toolbar
            toolBar.getChildren().addAll(sortSection, new Separator(Orientation.VERTICAL), priceSection);

            // Thêm toolbar vào container
            toolBarContainer.getChildren().add(toolBar);
            mainContainer.getChildren().add(toolBarContainer);

            // 5. Hiển thị số lượng sản phẩm tìm thấy
            Label countLabel = new Label(isVietnamese ?
                    "Tìm thấy " + sortedProducts.size() + " sản phẩm" :
                    "Found " + sortedProducts.size() + " products");
            countLabel.setStyle("-fx-font-size: 16px; -fx-font-style: italic;");
            mainContainer.getChildren().add(countLabel);

            // 6. THÊM PHẦN SẢN PHẨM NỔI BẬT - CHỈ THÊM KHI ĐANG Ở TRANG CHỦ
            if (currentPage == Page.HOME || currentPage == Page.PRICE_SORT) {
                VBox featuredSection = new VBox(15);
                Label featuredTitle = new Label(isVietnamese ? "SẢN PHẨM NỔI BẬT" : "FEATURED PRODUCTS");
                featuredTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFA000;");
                featuredSection.getChildren().add(featuredTitle);

                // Lấy và sắp xếp sản phẩm nổi bật
                List<Product> featuredProducts = ProductRepository.getFeaturedProducts(4);

                // Sắp xếp theo giá nếu cần
                if (sortOrder == SortOrder.ASCENDING) {
                    featuredProducts.sort(Comparator.comparing(Product::getPrice));
                } else if (sortOrder == SortOrder.DESCENDING) {
                    featuredProducts.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                }

                FlowPane featuredGrid = new FlowPane();
                featuredGrid.setHgap(20);
                featuredGrid.setVgap(20);

                for (Product product : featuredProducts) {
                    VBox productCard = createProductCard(product);

                    // Thêm nhãn "HOT"
                    Label hotLabel = new Label("HOT");
                    hotLabel.setStyle(
                            "-fx-background-color: #FF5722; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10;"
                    );
                    ((VBox)productCard).getChildren().add(0, hotLabel);

                    featuredGrid.getChildren().add(productCard);
                }

                featuredSection.getChildren().add(featuredGrid);
                mainContainer.getChildren().add(featuredSection);
            }

            // 7. Kiểm tra và hiển thị các sản phẩm đã sắp xếp
            if (sortedProducts == null || sortedProducts.isEmpty()) {
                Label noProductsLabel = new Label(isVietnamese ?
                        "Không tìm thấy sản phẩm nào phù hợp với điều kiện lọc" :
                        "No products found matching your filter criteria");
                noProductsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #757575; -fx-font-style: italic;");
                mainContainer.getChildren().add(noProductsLabel);
            } else {
                // NHÓM SẢN PHẨM THEO DANH MỤC VÀ HIỂN THỊ THEO DANH MỤC
                Map<String, List<Product>> productsByCategory = new HashMap<>();

                // Phân loại sản phẩm theo danh mục
                for (Product product : sortedProducts) {
                    String category = product.getCategoryName();
                    if (!productsByCategory.containsKey(category)) {
                        productsByCategory.put(category, new ArrayList<>());
                    }
                    productsByCategory.get(category).add(product);
                }

                // Hiển thị từng danh mục
                for (Map.Entry<String, List<Product>> entry : productsByCategory.entrySet()) {
                    String categoryName = entry.getKey();
                    List<Product> categoryProducts = entry.getValue();

                    // Tạo section cho danh mục
                    VBox categorySection = new VBox(15);
                    Label categoryTitle = new Label(categoryName.toUpperCase());
                    categoryTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1976D2;");
                    categorySection.getChildren().add(categoryTitle);

                    // Tạo FlowPane để hiển thị sản phẩm theo danh mục
                    FlowPane categoryProductsContainer = new FlowPane();
                    categoryProductsContainer.setHgap(20);
                    categoryProductsContainer.setVgap(20);
                    categoryProductsContainer.setPrefWrapLength(contentPane.getWidth() - 40);

                    // Thêm sản phẩm vào container với số thứ tự
                    for (int i = 0; i < categoryProducts.size(); i++) {
                        Product product = categoryProducts.get(i);
                        VBox productCard = createProductCard(product);

                        // Thêm badge số thứ tự
                        Label rankLabel = new Label("#" + (i + 1));
                        rankLabel.setStyle(
                                "-fx-background-color: " + (ascending ? "#4CAF50" : "#F44336") + ";" +
                                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10;"
                        );
                        ((VBox)productCard).getChildren().add(0, rankLabel);

                        categoryProductsContainer.getChildren().add(productCard);
                    }

                    categorySection.getChildren().add(categoryProductsContainer);
                    mainContainer.getChildren().add(categorySection);
                }
            }

            // 8. Thêm container vào ContentPane
            contentPane.getChildren().add(mainContainer);
            AnchorPane.setTopAnchor(mainContainer, 0.0);
            AnchorPane.setLeftAnchor(mainContainer, 0.0);
            AnchorPane.setRightAnchor(mainContainer, 0.0);

            // 9. Cuộn về đầu trang
            centerScrollPane.setVvalue(0.0);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể hiển thị kết quả sắp xếp: " + e.getMessage() :
                            "Cannot display sorted results: " + e.getMessage()
            );
        }
    }

    /**
     * Lọc tất cả sản phẩm theo các điều kiện hiện tại và hiển thị kết quả
     */
    private void applyFiltersToAllProducts() {
        try {
            // Lọc tất cả sản phẩm bằng phương thức filterProducts
            List<Product> filteredProducts = filterProducts(ProductRepository.getAllProducts());

            // Sắp xếp theo trạng thái hiện tại nếu cần
            if (sortOrder == SortOrder.ASCENDING) {
                filteredProducts.sort(Comparator.comparing(Product::getPrice));
                displaySortedResults(filteredProducts, true);
            } else if (sortOrder == SortOrder.DESCENDING) {
                filteredProducts.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                displaySortedResults(filteredProducts, false);
            } else {
                displaySortedResults(filteredProducts, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể áp dụng bộ lọc: " + e.getMessage() :
                            "Cannot apply filters: " + e.getMessage()
            );
        }
    }

    /**
     * Lọc danh sách sản phẩm theo giá
     */
    private List<Product> filterProducts(List<Product> products) {
        if (products == null) {
            return new ArrayList<>();
        }

        // Tạo danh sách mới để không ảnh hưởng đến danh sách gốc
        List<Product> filteredProducts = new ArrayList<>(products);

        // Lọc theo giá tối thiểu
        if (currentMinPrice != null) {
            filteredProducts = filteredProducts.stream()
                    .filter(p -> p.getPrice() >= currentMinPrice)
                    .collect(Collectors.toList());
        }

        // Lọc theo giá tối đa
        if (currentMaxPrice != null) {
            filteredProducts = filteredProducts.stream()
                    .filter(p -> p.getPrice() <= currentMaxPrice)
                    .collect(Collectors.toList());
        }

        return filteredProducts;
    }

    // Tìm phương thức createBrandFilterButton hoặc tương tự
    private MenuButton createBrandFilterButton(List<Product> products) {
        MenuButton brandFilter = new MenuButton();
        brandFilter.setText(isVietnamese ? "Hãng" : "Brand");
        brandFilter.setStyle("-fx-background-color: linear-gradient(to right, #865DFF, #5CB8E4); " +
                "-fx-background-radius: 20; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 15px; " +
                "-fx-border-radius: 20; " +
                "-fx-border-color: #000000; " +
                "-fx-border-width: 1.5px; " +
                "-fx-padding: 0 18 0 18;");

        // Lấy danh sách thương hiệu từ sản phẩm
        Set<String> allBrands = extractBrands(ProductRepository.getAllProducts());

        // Tạo và thêm menu item "Tất cả"
        MenuItem allItem = new MenuItem(isVietnamese ? "Tất cả" : "All");
        allItem.setStyle("-fx-font-weight: bold; -fx-padding: 8px 15px;");
        allItem.setOnAction(e -> {
            brandFilter.setText(isVietnamese ? "Hãng: Tất cả" : "Brand: All");
            currentBrand = null;
            applyFiltersInCurrentView();
        });
        brandFilter.getItems().add(allItem);

        // Thêm separator
        brandFilter.getItems().add(new SeparatorMenuItem());

        // Bọc ScrollPane trong CustomMenuItem
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setMaxHeight(200); // Chiều cao tương đương khoảng 5 items
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: white; -fx-padding: 0;");
        scrollPane.setPrefWidth(200);

        // VBox container cho các brand
        VBox brandItems = new VBox();
        brandItems.setPrefWidth(scrollPane.getPrefWidth() - 5);

        // Thêm các brand vào VBox
        for (String brand : allBrands) {
            Label brandLabel = new Label(brand);
            brandLabel.setPadding(new Insets(8, 10, 8, 10));
            brandLabel.setPrefWidth(scrollPane.getPrefWidth() - 5);
            brandLabel.setStyle("-fx-cursor: hand");

            // Highlight nếu đang được chọn
            if (brand.equals(currentBrand)) {
                brandLabel.setStyle(brandLabel.getStyle() + "; -fx-background-color: #e0e0ff;");
            }

            // Hiệu ứng hover
            brandLabel.setOnMouseEntered(e ->
                    brandLabel.setStyle(brandLabel.getStyle() + "; -fx-background-color: #f0f0f0;"));

            brandLabel.setOnMouseExited(e -> {
                if (brand.equals(currentBrand)) {
                    brandLabel.setStyle("-fx-cursor: hand; -fx-background-color: #e0e0ff;");
                } else {
                    brandLabel.setStyle("-fx-cursor: hand;");
                }
            });

            // Xử lý khi click vào brand
            brandLabel.setOnMouseClicked(e -> {
                brandFilter.setText((isVietnamese ? "Hãng: " : "Brand: ") + brand);
                currentBrand = brand;
                applyFiltersInCurrentView();
                brandFilter.hide(); // Đóng menu
            });

            // Thêm label vào container
            brandItems.getChildren().add(brandLabel);
        }

        // Đặt nội dung vào ScrollPane
        scrollPane.setContent(brandItems);

        // Đặt ScrollPane vào CustomMenuItem
        CustomMenuItem scrollableMenu = new CustomMenuItem(scrollPane);
        scrollableMenu.setHideOnClick(false); // Quan trọng: không đóng khi click bên trong

        // Thêm vào menu
        brandFilter.getItems().add(scrollableMenu);

        return brandFilter;
    }

    /**
     * Áp dụng tất cả các bộ lọc trên view hiện tại
     */
    private void applyFiltersInCurrentView() {
        try {
            // Xác định danh sách sản phẩm bắt đầu
            List<Product> baseProducts;

            if (currentCategory != null) {
                // Bắt đầu với sản phẩm trong danh mục hiện tại
                baseProducts = ProductRepository.getProductsByCategory(currentCategory);
            } else {
                // Bắt đầu với tất cả sản phẩm
                baseProducts = ProductRepository.getAllProducts();
            }

            // Áp dụng bộ lọc hãng
            if (currentBrand != null) {
                baseProducts = baseProducts.stream()
                        .filter(p -> p.getProductName().startsWith(currentBrand))
                        .collect(Collectors.toList());
            }

            // Áp dụng bộ lọc giá
            if (currentMinPrice != null) {
                baseProducts = baseProducts.stream()
                        .filter(p -> p.getPrice() >= currentMinPrice)
                        .collect(Collectors.toList());
            }

            if (currentMaxPrice != null) {
                baseProducts = baseProducts.stream()
                        .filter(p -> p.getPrice() <= currentMaxPrice)
                        .collect(Collectors.toList());
            }

            // Lưu kết quả đã lọc
            currentFilteredProducts = baseProducts;

            // Sắp xếp nếu có yêu cầu
            if (sortOrder == SortOrder.ASCENDING) {
                currentFilteredProducts.sort(Comparator.comparing(Product::getPrice));
                displaySortedResults(currentFilteredProducts, true);
            } else if (sortOrder == SortOrder.DESCENDING) {
                currentFilteredProducts.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                displaySortedResults(currentFilteredProducts, false);
            } else {
                // Hiển thị không sắp xếp
                displaySortedResults(currentFilteredProducts, true); // Mặc định là tăng dần
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể áp dụng bộ lọc: " + e.getMessage() :
                            "Cannot apply filters: " + e.getMessage()
            );
        }
    }

    /**
     * Hiển thị kết quả lọc/sắp xếp hiện tại
     */
    private void displayCurrentFiltered(boolean ascending) {
        // Xây dựng tiêu đề dựa trên các bộ lọc hiện tại
        StringBuilder titleBuilder = new StringBuilder();

        // Thêm thông tin sắp xếp
        if (sortOrder == SortOrder.ASCENDING) {
            titleBuilder.append(isVietnamese ? "SẮP XẾP THEO GIÁ TĂNG DẦN" : "PRICE ASCENDING");
        } else {
            titleBuilder.append(isVietnamese ? "SẮP XẾP THEO GIÁ GIẢM DẦN" : "PRICE DESCENDING");
        }

        // Thêm thông tin danh mục
        if (currentCategory != null) {
            titleBuilder.append(" - ").append(currentCategory);
        }

        // Thêm thông tin hãng
        if (currentBrand != null) {
            titleBuilder.append(" - ").append(currentBrand);
        }

        // Thêm thông tin khoảng giá
        if (currentMinPrice != null || currentMaxPrice != null) {
            titleBuilder.append(" (");
            if (currentMinPrice != null && currentMaxPrice != null) {
                titleBuilder.append(String.format("%,.0f₫ - %,.0f₫", currentMinPrice, currentMaxPrice));
            } else if (currentMinPrice != null) {
                titleBuilder.append(String.format("≥ %,.0f₫", currentMinPrice));
            } else {
                titleBuilder.append(String.format("≤ %,.0f₫", currentMaxPrice));
            }
            titleBuilder.append(")");
        }

        // Tạo AnchorPane mới
        AnchorPane contentPane = (AnchorPane) centerScrollPane.getContent();
        contentPane.getChildren().clear();

        // Tạo container chính
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));

        // Thêm tiêu đề
        Label titleLabel = new Label(titleBuilder.toString());
        titleLabel.setStyle(
                "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #E53935; " +
                        "-fx-background-color: #FFEBEE; -fx-padding: 10px; -fx-background-radius: 5px;"
        );
        mainContainer.getChildren().add(titleLabel);

        // Thêm thanh bộ lọc đang áp dụng
        HBox filterBar = createFilterBar();
        mainContainer.getChildren().add(filterBar);

        // Kiểm tra nếu không có sản phẩm
        if (currentFilteredProducts == null || currentFilteredProducts.isEmpty()) {
            Label emptyLabel = new Label(isVietnamese ?
                    "Không tìm thấy sản phẩm nào phù hợp với điều kiện lọc" :
                    "No products match your filter criteria");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #757575; -fx-font-style: italic;");
            mainContainer.getChildren().add(emptyLabel);
        } else {
            // Hiển thị số lượng sản phẩm tìm thấy
            Label countLabel = new Label(isVietnamese ?
                    "Tìm thấy " + currentFilteredProducts.size() + " sản phẩm" :
                    "Found " + currentFilteredProducts.size() + " products");
            countLabel.setStyle("-fx-font-size: 16px; -fx-font-style: italic;");
            mainContainer.getChildren().add(countLabel);

            // Tạo FlowPane để hiển thị sản phẩm
            FlowPane productsContainer = new FlowPane();
            productsContainer.setHgap(20);
            productsContainer.setVgap(20);
            productsContainer.setPrefWrapLength(contentPane.getWidth() - 40);

            // Thêm sản phẩm vào container với số thứ tự
            for (int i = 0; i < currentFilteredProducts.size(); i++) {
                Product product = currentFilteredProducts.get(i);
                VBox productCard = createProductCard(product);

                // Thêm badge số thứ tự
                Label rankLabel = new Label("#" + (i + 1));
                rankLabel.setStyle(
                        "-fx-background-color: " + (ascending ? "#4CAF50" : "#F44336") + ";" +
                                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10;"
                );
                ((VBox)productCard).getChildren().add(0, rankLabel);

                productsContainer.getChildren().add(productCard);
            }

            mainContainer.getChildren().add(productsContainer);
        }

        // Thêm container vào ContentPane
        contentPane.getChildren().add(mainContainer);
        AnchorPane.setTopAnchor(mainContainer, 0.0);
        AnchorPane.setLeftAnchor(mainContainer, 0.0);
        AnchorPane.setRightAnchor(mainContainer, 0.0);

        // Cuộn về đầu trang
        centerScrollPane.setVvalue(0.0);
    }



    /**
     * Tạo thanh lọc với các bộ lọc đang áp dụng
     */
    private HBox createFilterBar() {
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(10));
        filterBar.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 5;");

        // Nhãn "Bộ lọc đang áp dụng"
        Label filterLabel = new Label(isVietnamese ? "Bộ lọc đang áp dụng:" : "Active filters:");
        filterLabel.setStyle("-fx-font-weight: bold;");
        filterBar.getChildren().add(filterLabel);

        // Nếu có danh mục đang lọc
        if (currentCategory != null) {
            HBox categoryChip = createFilterChip(
                    isVietnamese ? "Danh mục: " + currentCategory : "Category: " + currentCategory,
                    e -> {
                        currentCategory = null;
                        // Nếu còn bộ lọc khác, áp dụng lại, nếu không thì tải lại tất cả
                        if (currentBrand != null || currentMinPrice != null || currentMaxPrice != null) {
                            applyCurrentFilters();
                        } else {
                            loadProducts();
                        }
                    }
            );
            filterBar.getChildren().add(categoryChip);
        }

        // Nếu có hãng đang lọc
        if (currentBrand != null) {
            HBox brandChip = createFilterChip(
                    isVietnamese ? "Hãng: " + currentBrand : "Brand: " + currentBrand,
                    e -> {
                        currentBrand = null;
                        applyCurrentFilters();
                    }
            );
            filterBar.getChildren().add(brandChip);
        }

        // Nếu có khoảng giá đang lọc
        if (currentMinPrice != null || currentMaxPrice != null) {
            String priceText;
            if (currentMinPrice != null && currentMaxPrice != null) {
                priceText = String.format("%,.0f₫ - %,.0f₫", currentMinPrice, currentMaxPrice);
            } else if (currentMinPrice != null) {
                priceText = String.format("≥ %,.0f₫", currentMinPrice);
            } else {
                priceText = String.format("≤ %,.0f₫", currentMaxPrice);
            }

            HBox priceChip = createFilterChip(
                    isVietnamese ? "Giá: " + priceText : "Price: " + priceText,
                    e -> {
                        currentMinPrice = null;
                        currentMaxPrice = null;
                        applyCurrentFilters();
                    }
            );
            filterBar.getChildren().add(priceChip);
        }

        // Nếu có ít nhất một bộ lọc, hiển thị nút "Xóa tất cả"
        if (currentCategory != null || currentBrand != null ||
                currentMinPrice != null || currentMaxPrice != null) {
            Button clearAllButton = new Button(isVietnamese ? "Xóa tất cả bộ lọc" : "Clear all filters");
            clearAllButton.setStyle("-fx-background-color: #E53935; -fx-text-fill: white;");
            clearAllButton.setOnAction(e -> {
                // Reset toàn bộ trạng thái lọc
                currentCategory = null;
                currentBrand = null;
                currentMinPrice = null;
                currentMaxPrice = null;
                currentFilteredProducts = null;
                sortOrder = SortOrder.NONE;

                // Làm sạch các trường nhập giá
                if (minPriceField != null) minPriceField.clear();
                if (maxPriceField != null) maxPriceField.clear();

                // Tải lại tất cả sản phẩm và hiển thị trang chủ
                loadProducts();
                loadFeaturedProducts(); // Tải lại sản phẩm nổi bật
                loadAllCategoryProducts(); // Tải lại các danh mục sản phẩm

                // Hiển thị thông báo đã xóa bộ lọc
                AlertUtils.showInfo(
                        isVietnamese ? "Bộ lọc" : "Filters",
                        isVietnamese ? "Đã xóa tất cả bộ lọc" : "All filters have been cleared"
                );
            });
            filterBar.getChildren().add(clearAllButton);
        } else {
            // Nếu không có bộ lọc nào
            Label noFilterLabel = new Label(isVietnamese ? "Không có bộ lọc" : "No filters applied");
            noFilterLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #757575;");
            filterBar.getChildren().add(noFilterLabel);
        }

        return filterBar;
    }

/**
 * Tạo chip hiển thị bộ lọc với nút xóa
 */
    /**
     * Tạo chip hiển thị bộ lọc với nút xóa
     */
    private HBox createFilterChip(String text, EventHandler<ActionEvent> onRemove) {
        HBox chip = new HBox(5);
        chip.setAlignment(Pos.CENTER);
        chip.setPadding(new Insets(5, 10, 5, 10));
        chip.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 15;");

        Label label = new Label(text);
        Button removeBtn = new Button("×");
        removeBtn.setStyle("-fx-background-color: transparent; -fx-font-weight: bold;");
        removeBtn.setOnAction(onRemove);

        chip.getChildren().addAll(label, removeBtn);
        return chip;
    }

    /**
     * Áp dụng tất cả các bộ lọc hiện tại
     */
    private void applyCurrentFilters() {
        try {
            // Bắt đầu với tất cả sản phẩm
            List<Product> filteredProducts = ProductRepository.getAllProducts();

            // Áp dụng lọc theo danh mục
            if (currentCategory != null) {
                filteredProducts = filteredProducts.stream()
                        .filter(p -> p.getCategoryName().equals(currentCategory))
                        .collect(Collectors.toList());
            }

            // Áp dụng lọc theo hãng
            if (currentBrand != null) {
                filteredProducts = filteredProducts.stream()
                        .filter(p -> p.getProductName().startsWith(currentBrand))
                        .collect(Collectors.toList());
            }

            // Áp dụng lọc theo giá tối thiểu
            if (currentMinPrice != null) {
                filteredProducts = filteredProducts.stream()
                        .filter(p -> p.getPrice() >= currentMinPrice)
                        .collect(Collectors.toList());
            }

            // Áp dụng lọc theo giá tối đa
            if (currentMaxPrice != null) {
                filteredProducts = filteredProducts.stream()
                        .filter(p -> p.getPrice() <= currentMaxPrice)
                        .collect(Collectors.toList());
            }

            // Lưu kết quả lọc
            currentFilteredProducts = filteredProducts;

            // Sắp xếp kết quả nếu đã chọn sắp xếp trước đó
            if (sortOrder == SortOrder.ASCENDING) {
                filteredProducts.sort(Comparator.comparing(Product::getPrice));
                displayCurrentFiltered(true);
            } else if (sortOrder == SortOrder.DESCENDING) {
                filteredProducts.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                displayCurrentFiltered(false);
            } else {
                // Hiển thị kết quả không sắp xếp
                displayFilteredResults();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể áp dụng bộ lọc: " + e.getMessage() :
                            "Cannot apply filters: " + e.getMessage()
            );
        }
    }

    /**
     * Sắp xếp tất cả sản phẩm theo danh mục theo giá tăng dần
     */
    private void sortAllProductsByCategoryAscending() {
        try {
            // 1. Lấy AnchorPane chính từ ScrollPane
            AnchorPane contentPane = (AnchorPane) centerScrollPane.getContent();
            if (contentPane == null) return;
            contentPane.getChildren().clear();

            // 2. Tạo container chính
            VBox mainContainer = new VBox(30);
            mainContainer.setPadding(new Insets(20));

            // 3. Thêm tiêu đề chính
            Label titleLabel = new Label(isVietnamese ?
                    "SẢN PHẨM THEO DANH MỤC - GIÁ TĂNG DẦN" :
                    "PRODUCTS BY CATEGORY - PRICE ASCENDING");
            titleLabel.setStyle(
                    "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #E53935; " +
                            "-fx-background-color: #FFEBEE; -fx-padding: 10px; -fx-background-radius: 5px;" +
                            "-fx-text-alignment: center; -fx-alignment: center;"
            );
            mainContainer.getChildren().add(titleLabel);

            // 4. LƯU TRẠNG THÁI SẮP XẾP để các tùy chọn lọc sau này duy trì sắp xếp
            sortOrder = SortOrder.ASCENDING;

            // 5. Phần sản phẩm nổi bật (vẫn giữ nhưng đã sắp xếp)
            if (featuredProductGrid != null) {
                VBox featuredSection = new VBox(15);

                Label featuredTitle = new Label(isVietnamese ? "SẢN PHẨM NỔI BẬT - GIÁ TĂNG DẦN" : "FEATURED PRODUCTS - PRICE ASCENDING");
                featuredTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFA000;");

                // Lấy và sắp xếp sản phẩm nổi bật
                List<Product> featuredProducts = ProductRepository.getFeaturedProducts(4);
                featuredProducts.sort(Comparator.comparing(Product::getPrice));

                // Hiển thị sản phẩm nổi bật đã sắp xếp
                FlowPane featuredContainer = new FlowPane();
                featuredContainer.setHgap(20);
                featuredContainer.setVgap(20);
                featuredContainer.setPrefWrapLength(contentPane.getWidth() - 40);

                for (int i = 0; i < featuredProducts.size(); i++) {
                    Product product = featuredProducts.get(i);
                    VBox productCard = createProductCard(product);

                    // Thêm nhãn HOT và thứ hạng giá
                    HBox badgeContainer = new HBox(5);
                    badgeContainer.setAlignment(Pos.CENTER_LEFT);

                    Label hotLabel = new Label("HOT");
                    hotLabel.setStyle(
                            "-fx-background-color: #FF5722; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10;"
                    );

                    Label rankLabel = new Label("#" + (i + 1));
                    rankLabel.setStyle(
                            "-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10;"
                    );

                    badgeContainer.getChildren().addAll(hotLabel, rankLabel);
                    ((VBox)productCard).getChildren().add(0, badgeContainer);

                    featuredContainer.getChildren().add(productCard);
                }

                featuredSection.getChildren().addAll(featuredTitle, featuredContainer);
                mainContainer.getChildren().add(featuredSection);
            }

            // 6. Lấy danh sách tất cả danh mục
            List<Category> allCategories = categoryService.getAllCategories();

            // 7. Hiển thị sản phẩm theo từng danh mục
            for (Category category : allCategories) {
                String categoryName = category.getCategoryName();

                // Lấy sản phẩm theo danh mục
                List<Product> categoryProducts = ProductRepository.getProductsByCategory(categoryName);

                // Bỏ qua danh mục không có sản phẩm
                if (categoryProducts.isEmpty()) continue;

                // Sắp xếp sản phẩm theo giá tăng dần
                categoryProducts.sort(Comparator.comparing(Product::getPrice));

                // Tạo section cho danh mục
                VBox categorySection = new VBox(15);

                // Tiêu đề danh mục
                Label categoryTitle = new Label(categoryName.toUpperCase() +
                        (isVietnamese ? " - GIÁ TĂNG DẦN" : " - PRICE ASCENDING"));
                categoryTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1976D2;");

                // Container cho sản phẩm
                FlowPane productsContainer = new FlowPane();
                productsContainer.setHgap(20);
                productsContainer.setVgap(20);
                productsContainer.setPrefWrapLength(contentPane.getWidth() - 40);

                // Thêm sản phẩm vào container với thứ hạng
                for (int i = 0; i < categoryProducts.size(); i++) {
                    Product product = categoryProducts.get(i);
                    VBox productCard = createProductCard(product);

                    // Thêm nhãn thứ hạng giá
                    Label rankLabel = new Label("#" + (i + 1));
                    rankLabel.setStyle(
                            "-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10;"
                    );
                    ((VBox)productCard).getChildren().add(0, rankLabel);

                    productsContainer.getChildren().add(productCard);
                }

                categorySection.getChildren().addAll(categoryTitle, productsContainer);
                mainContainer.getChildren().add(categorySection);
            }

            // 8. Thêm container chính vào contentPane
            contentPane.getChildren().add(mainContainer);
            AnchorPane.setTopAnchor(mainContainer, 0.0);
            AnchorPane.setLeftAnchor(mainContainer, 0.0);
            AnchorPane.setRightAnchor(mainContainer, 0.0);

            // 9. Cuộn về đầu trang
            centerScrollPane.setVvalue(0.0);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể sắp xếp sản phẩm: " + e.getMessage() :
                            "Cannot sort products: " + e.getMessage()
            );
        }
    }

/**
 * Sắp xếp tất cả sản phẩm theo danh mục theo giá giảm dần
 */
    /**
     * Sắp xếp tất cả sản phẩm theo danh mục theo giá giảm dần
     */
    private void sortAllProductsByCategoryDescending() {
        try {
            // 1. Lấy AnchorPane chính từ ScrollPane
            AnchorPane contentPane = (AnchorPane) centerScrollPane.getContent();
            if (contentPane == null) return;
            contentPane.getChildren().clear();

            // 2. Tạo container chính
            VBox mainContainer = new VBox(30);
            mainContainer.setPadding(new Insets(20));

            // 3. Thêm tiêu đề chính
            Label titleLabel = new Label(isVietnamese ?
                    "SẢN PHẨM THEO DANH MỤC - GIÁ GIẢM DẦN" :
                    "PRODUCTS BY CATEGORY - PRICE DESCENDING");
            titleLabel.setStyle(
                    "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #E53935; " +
                            "-fx-background-color: #FFEBEE; -fx-padding: 10px; -fx-background-radius: 5px;" +
                            "-fx-text-alignment: center; -fx-alignment: center;"
            );
            mainContainer.getChildren().add(titleLabel);

            // 4. LƯU TRẠNG THÁI SẮP XẾP để các tùy chọn lọc sau này duy trì sắp xếp
            sortOrder = SortOrder.DESCENDING;

            // 5. Phần sản phẩm nổi bật (vẫn giữ nhưng đã sắp xếp)
            try {
                VBox featuredSection = new VBox(15);

                Label featuredTitle = new Label(isVietnamese ? "SẢN PHẨM NỔI BẬT - GIÁ GIẢM DẦN" : "FEATURED PRODUCTS - PRICE DESCENDING");
                featuredTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFA000;");

                // Lấy và sắp xếp sản phẩm nổi bật
                List<Product> featuredProducts = ProductRepository.getFeaturedProducts(4);
                featuredProducts.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));

                // Hiển thị sản phẩm nổi bật đã sắp xếp
                FlowPane featuredContainer = new FlowPane();
                featuredContainer.setHgap(20);
                featuredContainer.setVgap(20);
                featuredContainer.setPrefWrapLength(contentPane.getWidth() - 40);

                for (int i = 0; i < featuredProducts.size(); i++) {
                    Product product = featuredProducts.get(i);
                    VBox productCard = createProductCard(product);

                    // Thêm nhãn HOT và thứ hạng giá
                    HBox badgeContainer = new HBox(5);
                    badgeContainer.setAlignment(Pos.CENTER_LEFT);

                    Label hotLabel = new Label("HOT");
                    hotLabel.setStyle(
                            "-fx-background-color: #FF5722; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10;"
                    );

                    Label rankLabel = new Label("#" + (i + 1));
                    rankLabel.setStyle(
                            "-fx-background-color: #F44336; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10;"
                    );

                    badgeContainer.getChildren().addAll(hotLabel, rankLabel);
                    ((VBox)productCard).getChildren().add(0, badgeContainer);

                    featuredContainer.getChildren().add(productCard);
                }

                featuredSection.getChildren().addAll(featuredTitle, featuredContainer);
                mainContainer.getChildren().add(featuredSection);
            } catch (Exception e) {
                System.err.println("Không thể tải sản phẩm nổi bật: " + e.getMessage());
            }

            // 6. Lấy danh sách tất cả danh mục
            List<Category> allCategories = categoryService.getAllCategories();

            // 7. Hiển thị sản phẩm theo từng danh mục
            for (Category category : allCategories) {
                String categoryName = category.getCategoryName();

                // Lấy sản phẩm theo danh mục
                List<Product> categoryProducts = ProductRepository.getProductsByCategory(categoryName);

                // Bỏ qua danh mục không có sản phẩm
                if (categoryProducts.isEmpty()) continue;

                // Sắp xếp sản phẩm theo giá giảm dần
                categoryProducts.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));

                // Tạo section cho danh mục
                VBox categorySection = new VBox(15);

                // Tiêu đề danh mục
                Label categoryTitle = new Label(categoryName.toUpperCase() +
                        (isVietnamese ? " - GIÁ GIẢM DẦN" : " - PRICE DESCENDING"));
                categoryTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1976D2;");

                // Container cho sản phẩm
                FlowPane productsContainer = new FlowPane();
                productsContainer.setHgap(20);
                productsContainer.setVgap(20);
                productsContainer.setPrefWrapLength(contentPane.getWidth() - 40);

                // Thêm sản phẩm vào container với thứ hạng
                for (int i = 0; i < categoryProducts.size(); i++) {
                    Product product = categoryProducts.get(i);
                    VBox productCard = createProductCard(product);

                    // Thêm nhãn thứ hạng giá
                    Label rankLabel = new Label("#" + (i + 1));
                    rankLabel.setStyle(
                            "-fx-background-color: #F44336; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10;"
                    );
                    ((VBox)productCard).getChildren().add(0, rankLabel);

                    productsContainer.getChildren().add(productCard);
                }

                categorySection.getChildren().addAll(categoryTitle, productsContainer);
                mainContainer.getChildren().add(categorySection);
            }

            // 8. Thêm container chính vào contentPane
            contentPane.getChildren().add(mainContainer);
            AnchorPane.setTopAnchor(mainContainer, 0.0);
            AnchorPane.setLeftAnchor(mainContainer, 0.0);
            AnchorPane.setRightAnchor(mainContainer, 0.0);

            // 9. Cuộn về đầu trang
            centerScrollPane.setVvalue(0.0);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể sắp xếp sản phẩm: " + e.getMessage() :
                            "Cannot sort products: " + e.getMessage()
            );
        }
    }

    /**
     * Hiển thị kết quả lọc không sắp xếp
     */
    private void displayFilteredResults() {
        // 1. Lấy AnchorPane chính từ ScrollPane
        AnchorPane contentPane = (AnchorPane) centerScrollPane.getContent();
        if (contentPane == null) return;
        contentPane.getChildren().clear();

        // 2. Tạo container chính
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));

        // 3. Xây dựng tiêu đề lọc
        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(isVietnamese ? "KẾT QUẢ LỌC" : "FILTER RESULTS");

        // Thêm thông tin danh mục
        if (currentCategory != null) {
            titleBuilder.append(" - ").append(currentCategory);
        }

        // Thêm thông tin hãng
        if (currentBrand != null) {
            titleBuilder.append(" - ").append(currentBrand);
        }

        // Thêm thông tin khoảng giá
        if (currentMinPrice != null || currentMaxPrice != null) {
            titleBuilder.append(" (");
            if (currentMinPrice != null && currentMaxPrice != null) {
                titleBuilder.append(String.format("%,.0f₫ - %,.0f₫", currentMinPrice, currentMaxPrice));
            } else if (currentMinPrice != null) {
                titleBuilder.append(String.format("≥ %,.0f₫", currentMinPrice));
            } else {
                titleBuilder.append(String.format("≤ %,.0f₫", currentMaxPrice));
            }
            titleBuilder.append(")");
        }

        Label titleLabel = new Label(titleBuilder.toString());
        titleLabel.setStyle(
                "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1976D2; " +
                        "-fx-background-color: #E3F2FD; -fx-padding: 10px; -fx-background-radius: 5px;"
        );
        mainContainer.getChildren().add(titleLabel);

        // 4. Thêm thanh bộ lọc với các bộ lọc đang áp dụng
        HBox filterBar = createFilterBar();
        mainContainer.getChildren().add(filterBar);

        // 5. Kiểm tra nếu không có sản phẩm
        if (currentFilteredProducts == null || currentFilteredProducts.isEmpty()) {
            Label emptyLabel = new Label(isVietnamese ?
                    "Không tìm thấy sản phẩm nào phù hợp với điều kiện lọc" :
                    "No products match your filter criteria");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #757575; -fx-font-style: italic;");
            mainContainer.getChildren().add(emptyLabel);
        } else {
            // 6. Hiển thị số lượng sản phẩm tìm thấy
            Label countLabel = new Label(isVietnamese ?
                    "Tìm thấy " + currentFilteredProducts.size() + " sản phẩm" :
                    "Found " + currentFilteredProducts.size() + " products");
            countLabel.setStyle("-fx-font-size: 16px; -fx-font-style: italic;");
            mainContainer.getChildren().add(countLabel);

            // 7. Thêm thanh công cụ sắp xếp
            HBox sortBar = new HBox(10);
            sortBar.setPadding(new Insets(10, 0, 10, 0));

            Label sortLabel = new Label(isVietnamese ? "Sắp xếp:" : "Sort:");

            Button ascButton = new Button(isVietnamese ? "Giá tăng dần ↑" : "Price ascending ↑");
            ascButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            ascButton.setOnAction(e -> sortAscending());

            Button descButton = new Button(isVietnamese ? "Giá giảm dần ↓" : "Price descending ↓");
            descButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
            descButton.setOnAction(e -> sortDescending());

            sortBar.getChildren().addAll(sortLabel, ascButton, descButton);
            mainContainer.getChildren().add(sortBar);

            // 8. Tạo FlowPane để hiển thị sản phẩm
            FlowPane productsContainer = new FlowPane();
            productsContainer.setHgap(20);
            productsContainer.setVgap(20);
            productsContainer.setPrefWrapLength(contentPane.getWidth() - 40);

            // 9. Thêm sản phẩm vào container
            for (Product product : currentFilteredProducts) {
                VBox productCard = createProductCard(product);
                productsContainer.getChildren().add(productCard);
            }

            mainContainer.getChildren().add(productsContainer);
        }

        // 10. Thêm container vào ContentPane
        contentPane.getChildren().add(mainContainer);
        AnchorPane.setTopAnchor(mainContainer, 0.0);
        AnchorPane.setLeftAnchor(mainContainer, 0.0);
        AnchorPane.setRightAnchor(mainContainer, 0.0);

        // 11. Cuộn về đầu trang
        centerScrollPane.setVvalue(0.0);
    }

    /**
     * PHƯƠNG THỨC MỚI: Thay thế toàn bộ nội dung hiển thị bằng sản phẩm đã sắp xếp
     */
    private void replaceContentWithSortedProducts(List<Product> sortedProducts, boolean ascending) {
        try {
            // 1. Lấy AnchorPane chính từ ScrollPane
            AnchorPane contentPane = (AnchorPane) centerScrollPane.getContent();
            if (contentPane == null) return;

            // 2. Xóa sạch nội dung hiện tại
            contentPane.getChildren().clear();

            // 3. Tạo container mới để hiển thị kết quả
            VBox mainContainer = new VBox(15);
            mainContainer.setPadding(new Insets(20));

            // 4. Tạo tiêu đề sắp xếp với màu nổi bật
            String titleText = ascending ?
                    (isVietnamese ? "SẢN PHẨM THEO GIÁ TĂNG DẦN ↑" : "PRODUCTS SORTED BY PRICE ASCENDING ↑") :
                    (isVietnamese ? "SẢN PHẨM THEO GIÁ GIẢM DẦN ↓" : "PRODUCTS SORTED BY PRICE DESCENDING ↓");

            Label titleLabel = new Label(titleText);
            titleLabel.setStyle(
                    "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #E53935; " +
                            "-fx-background-color: #FFEBEE; -fx-padding: 10px; -fx-background-radius: 5px;"
            );

            // 5. Tạo FlowPane để hiển thị sản phẩm
            FlowPane productsContainer = new FlowPane();
            productsContainer.setHgap(20);
            productsContainer.setVgap(20);
            productsContainer.setPrefWrapLength(contentPane.getWidth() - 40); // -40 để tính padding

            // 6. Thêm sản phẩm vào FlowPane
            for (Product product : sortedProducts) {
                VBox productCard = createProductCard(product);

                // Thêm badge chỉ thị thứ hạng giá (tùy chọn, tạo sự khác biệt rõ ràng)
                int rank = sortedProducts.indexOf(product) + 1;
                Label rankLabel = new Label("#" + rank);
                rankLabel.setStyle(
                        "-fx-background-color: " + (ascending ? "#4CAF50" : "#F44336") + ";" +
                                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10;"
                );
                ((VBox)productCard).getChildren().add(0, rankLabel);

                productsContainer.getChildren().add(productCard);
            }

            // 7. Thêm các thành phần vào container chính
            mainContainer.getChildren().addAll(titleLabel, productsContainer);

            // 8. Thêm container chính vào AnchorPane và thiết lập các constraints
            contentPane.getChildren().add(mainContainer);
            AnchorPane.setTopAnchor(mainContainer, 0.0);
            AnchorPane.setLeftAnchor(mainContainer, 0.0);
            AnchorPane.setRightAnchor(mainContainer, 0.0);

            // 9. Cuộn về đầu trang
            centerScrollPane.setVvalue(0.0);

            // 10. Kích hoạt cập nhật layout
            Platform.runLater(() -> {
                mainContainer.requestLayout();
                contentPane.requestLayout();
                centerScrollPane.requestLayout();
            });

            // 11. Hiển thị thông báo xác nhận cho người dùng
            String message = ascending ?
                    (isVietnamese ? "Đã sắp xếp sản phẩm theo giá tăng dần" : "Products sorted by price ascending") :
                    (isVietnamese ? "Đã sắp xếp sản phẩm theo giá giảm dần" : "Products sorted by price descending");

            AlertUtils.showInfo(
                    isVietnamese ? "Đã sắp xếp" : "Sorted",
                    message
            );

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi hiển thị sản phẩm đã sắp xếp: " + e.getMessage());
            AlertUtils.showError(
                    isVietnamese ? "Lỗi hiển thị" : "Display Error",
                    isVietnamese ? "Không thể hiển thị sản phẩm đã sắp xếp: " + e.getMessage() :
                            "Cannot display sorted products: " + e.getMessage()
            );
        }
    }
    // Thêm phương thức mới chỉ để hiển thị sản phẩm đã sắp xếp
    private void displaySortedProducts(List<Product> products) {
        if (productGrid != null) {
            // Xóa tất cả hiện tại
            productGrid.getChildren().clear();
            productGrid.getRowConstraints().clear();
            productGrid.getColumnConstraints().clear();

            // Tạo tiêu đề
            String titleText = sortOrder == SortOrder.ASCENDING ?
                    (isVietnamese ? "Sản phẩm theo giá tăng dần" : "Products sorted by price ascending") :
                    (isVietnamese ? "Sản phẩm theo giá giảm dần" : "Products sorted by price descending");

            Label titleLabel = new Label(titleText);
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FF0000;");

            // Tạo container mới để hiển thị kết quả sắp xếp
            AnchorPane contentPane = (AnchorPane) centerScrollPane.getContent();
            contentPane.getChildren().clear();

            VBox container = new VBox(20);
            container.setPadding(new Insets(20, 40, 20, 40));
            container.setAlignment(Pos.CENTER_LEFT);
            container.getChildren().add(titleLabel);

            // Tạo FlowPane để hiển thị sản phẩm
            FlowPane flowPane = new FlowPane();
            flowPane.setHgap(20);
            flowPane.setVgap(20);
            flowPane.setPrefWrapLength(900); // Điều chỉnh độ rộng để hiển thị 4 sản phẩm/hàng

            // Hiển thị sản phẩm đã sắp xếp
            for (Product product : products) {
                VBox card = createProductCard(product);
                flowPane.getChildren().add(card);
            }

            container.getChildren().add(flowPane);
            contentPane.getChildren().add(container);
            AnchorPane.setTopAnchor(container, 0.0);
            AnchorPane.setLeftAnchor(container, 0.0);
            AnchorPane.setRightAnchor(container, 0.0);

            // Đảm bảo cuộn lên đầu
            centerScrollPane.setVvalue(0);
        }
    }

    private void filterProductsByCategory(String categoryId) {
        try {
            List<Product> products = productService.getProductsByCategoryId(categoryId);
            displayProducts(products);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể lọc sản phẩm theo danh mục" : "Could not filter products by category"
            );
        }
    }

    // Cập nhật phương thức loadProducts

    private void loadProducts() {
        System.out.println("Đang tải danh sách sản phẩm...");

        try {
            List<Product> products = ProductRepository.getAllProducts();
            System.out.println("Đã tải: " + products.size() + " sản phẩm");

            if (products.isEmpty()) {
                System.err.println("Cảnh báo: Không tìm thấy sản phẩm nào từ database");
                AlertUtils.showWarning(
                        isVietnamese ? "Cảnh báo" : "Warning",
                        isVietnamese ? "Không tìm thấy sản phẩm nào trong cơ sở dữ liệu" : "No products found in database"
                );
            } else {
                System.out.println("Sản phẩm đầu tiên: " + products.get(0).getProductName());
                System.out.println("Đường dẫn ảnh: " + products.get(0).getImagePath());
            }

            displayProducts(products);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể tải danh sách sản phẩm: " + e.getMessage() : "Cannot load products: " + e.getMessage()
            );
        }
    }

    private void displayProducts(List<Product> products) {
        if (productGrid != null) {
            // XÓA TẤT CẢ nội dung hiện tại của productGrid
            productGrid.getChildren().clear();
            productGrid.getRowConstraints().clear();
            productGrid.getColumnConstraints().clear();

            // Kiểm tra danh sách rỗng
            if (products == null || products.isEmpty()) {
                Label emptyLabel = new Label(isVietnamese ? "Không tìm thấy sản phẩm nào" : "No products found");
                emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
                productGrid.add(emptyLabel, 0, 0);
                return;
            }

            // Thiết lập ColumnConstraints để đảm bảo bố cục đồng đều
            int columns = 4;
            for (int i = 0; i < columns; i++) {
                ColumnConstraints column = new ColumnConstraints();
                column.setPercentWidth(100.0 / columns);
                column.setHgrow(Priority.SOMETIMES);
                productGrid.getColumnConstraints().add(column);
            }

            // Thiết lập row 0 cho tiêu đề
            int row = 0;
            int col = 0;

            // Ẩn các phần tử trang chủ (banner, sản phẩm nổi bật) để hiển thị kết quả sắp xếp
            hideHomePageElements();

            // Hiển thị tiêu đề "Sản phẩm đã sắp xếp" hoặc "Kết quả lọc giá"
            String titleText;
            // Xác định tiêu đề dựa trên nguồn gốc lệnh (sắp xếp hoặc lọc)
            if (products == ProductRepository.getProductsSortedByPriceAsc()) {
                titleText = isVietnamese ? "Sản phẩm theo giá tăng dần" : "Products sorted by price ascending";
            } else if (products == ProductRepository.getProductsSortedByPriceDesc()) {
                titleText = isVietnamese ? "Sản phẩm theo giá giảm dần" : "Products sorted by price descending";
            } else {
                titleText = isVietnamese ? "Kết quả lọc sản phẩm" : "Filtered Products";
            }

            Label titleLabel = new Label(titleText);
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");
            productGrid.add(titleLabel, 0, row, columns, 1);
            row++;

            // QUAN TRỌNG: Log chi tiết sản phẩm để kiểm tra thứ tự
            for (int i = 0; i < products.size(); i++) {
                System.out.println("Hiển thị: #" + i + ": " + products.get(i).getProductName() +
                        " - Giá: " + products.get(i).getPrice());
            }

            // Thêm sản phẩm vào grid theo thứ tự trong danh sách đã sắp xếp
            for (Product product : products) {
                VBox card = createProductCard(product);
                productGrid.add(card, col, row);
                col++;
                if (col == columns) {
                    col = 0;
                    row++;
                }
            }

            // Đảm bảo cuộn lên đầu để thấy kết quả
            if (centerScrollPane != null) {
                centerScrollPane.setVvalue(0);
            }

            // Force refresh UI
            Platform.runLater(() -> {
                productGrid.requestLayout();
                if (productGrid.getScene() != null) {
                    productGrid.getScene().getWindow().sizeToScene();
                }
            });
        }
    }

    // Cập nhật phương thức goToHomePage

    @FXML
    private void goToHomePage() {
        System.out.println("ĐANG QUAY VỀ TRANG CHỦ...");

        try {
            // Đặt ScrollPane về đầu trang
            if (centerScrollPane != null) {
                centerScrollPane.setVvalue(0);
            }

            // Reset toàn bộ giao diện - phải tạo lại giao diện mới hoàn toàn
            if (centerScrollPane != null && centerScrollPane.getContent() != null) {
                // Tải lại toàn bộ nội dung trang chủ
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Home.fxml"));
                    Parent root = loader.load();

                    Scene currentScene = centerScrollPane.getScene();
                    Stage stage = (Stage) currentScene.getWindow();

                    // Giữ nguyên kích thước cửa sổ
                    Scene newScene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
                    stage.setScene(newScene);

                    System.out.println("ĐÃ TẢI LẠI GIAO DIỆN THÀNH CÔNG");

                } catch (IOException e) {
                    e.printStackTrace();
                    AlertUtils.showError(
                            isVietnamese ? "Lỗi" : "Error",
                            isVietnamese ? "Không thể tải lại giao diện: " + e.getMessage() :
                                    "Cannot reload interface: " + e.getMessage()
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể quay về trang chủ: " + e.getMessage() :
                            "Cannot return to home page: " + e.getMessage()
            );
        }
    }
    //**
    private VBox createProductCard(Product product) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(200);
        box.setPrefHeight(220);

        // Hình ảnh sản phẩm
        ImageView img = new ImageView();

        try {
            String imagePath = product.getImagePath();
            System.out.println("Đường dẫn hình ảnh gốc: " + imagePath);

            // Tải hình ảnh sử dụng phương thức linh hoạt
            Image image = loadProductImage(imagePath);
            if (image != null) {
                img.setImage(image);
                System.out.println("Đã tải thành công hình ảnh sản phẩm");
            } else {
                // Nếu không tải được, sử dụng ảnh mặc định
                Image defaultImage = loadDefaultImage();
                img.setImage(defaultImage);
                System.out.println("Không thể tải hình ảnh, sử dụng hình mặc định");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi tải hình ảnh: " + e.getMessage());
            // Sử dụng ảnh mặc định trong trường hợp lỗi
            img.setImage(loadDefaultImage());
        }

        img.setFitWidth(150);
        img.setFitHeight(120);
        img.setPreserveRatio(true);

        // Tên sản phẩm
        Label name = new Label(product.getProductName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        name.setAlignment(Pos.CENTER);
        name.setWrapText(true);
        name.setMaxWidth(180);

        // Giá sản phẩm với StackPane để thêm hiệu ứng hover
        Label price = new Label(String.format("%,.0f₫", product.getPrice()));
        price.setStyle("-fx-text-fill: #ff6600; -fx-font-weight: bold; -fx-font-size: 16px;");

        // Đặt price trong StackPane để tạo hiệu ứng hover độc lập
        StackPane priceContainer = new StackPane(price);
        priceContainer.setAlignment(Pos.CENTER);

        // Thêm hiệu ứng hover riêng cho giá
        priceContainer.setOnMouseEntered(e -> {
            price.setStyle("-fx-text-fill: #ff3300; -fx-font-weight: bold; -fx-font-size: 17px; " +
                    "-fx-padding: 3 8; -fx-background-color: #FFF3E0; -fx-background-radius: 4;");
        });

        priceContainer.setOnMouseExited(e -> {
            price.setStyle("-fx-text-fill: #ff6600; -fx-font-weight: bold; -fx-font-size: 16px;");
        });

        // Danh mục
        Label category = new Label(product.getCategoryName());
        category.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");

        // Thêm các thành phần vào card
        box.getChildren().addAll(img, name, priceContainer, category);
        box.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 8; -fx-padding: 10;");

        // Hiệu ứng hover
        box.setOnMouseEntered(e -> box.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #5CB8E4; -fx-border-radius: 8; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);"));
        box.setOnMouseExited(e -> box.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 8; -fx-padding: 10;"));

        // Đặt con trỏ mặc định
        box.setCursor(Cursor.HAND);

        // Xử lý click vào sản phẩm
        box.setOnMouseClicked(e -> showProductDetail(product));

        return box;
    }

    @FXML
    private void onSearch() {
        String keyword = searchField.getText().trim();
        System.out.println("Đang xử lý tìm kiếm với từ khóa: " + keyword);
        searchProducts(keyword);
    }

    public void searchProducts(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            AlertUtils.showWarning(
                    isVietnamese ? "Tìm kiếm" : "Search",
                    isVietnamese ? "Vui lòng nhập từ khóa tìm kiếm" : "Please enter search keywords"
            );
            loadProducts();
            return;
        }

        try {
            System.out.println("Đang tìm kiếm sản phẩm với từ khóa: " + keyword);

            // Đặt từ khóa vào trường tìm kiếm nếu nó chưa có
            if (searchField != null && !searchField.getText().equals(keyword)) {
                searchField.setText(keyword);
            }

            // Thay đổi trạng thái để biết đang ở trang tìm kiếm
            currentPage = Page.SEARCH;

            // Lấy kết quả tìm kiếm từ repository
            List<Product> searchResults = ProductRepository.searchProducts(keyword);
            System.out.println("Tìm thấy " + searchResults.size() + " sản phẩm cho từ khóa: " + keyword);

            // Hiển thị giao diện tìm kiếm với kết quả
            displaySearchResults(keyword, searchResults);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể tìm kiếm sản phẩm: " + e.getMessage() :
                            "Could not search products: " + e.getMessage()
            );
        }
    }
    private void setupLiveSearch() {
        // Tạo popup để hiển thị gợi ý
        searchPopup = new Popup();
        searchPopup.setAutoHide(true);

        // Tạo container cho các gợi ý tìm kiếm
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

        // Thêm container vào popup
        searchPopup.getContent().add(searchSuggestionsBox);

        // Xử lý sự kiện khi nhập vào thanh tìm kiếm
        if (searchField != null) {
            // Thêm listener cho sự kiện thay đổi text
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (searchTimer != null) {
                    searchTimer.cancel();
                }

                // Nếu text rỗng, ẩn popup
                if (newValue == null || newValue.trim().isEmpty()) {
                    searchPopup.hide();
                    return;
                }

                // Sử dụng timer để debounce (tránh quá nhiều truy vấn)
                searchTimer = new Timer();
                searchTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> performLiveSearch(newValue.trim()));
                    }
                }, SEARCH_DELAY);
            });

            // Xử lý sự kiện click vào searchField
            searchField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue && !searchField.getText().trim().isEmpty()) {
                    // Nếu searchField nhận focus và có text, hiển thị lại popup
                    performLiveSearch(searchField.getText().trim());
                } else if (!newValue) {
                    // Thêm delay nhỏ trước khi ẩn để không ảnh hưởng đến việc chọn kết quả
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

            // Thực hiện tìm kiếm sản phẩm
            List<Product> searchResults = ProductRepository.searchProductsLive(keyword);

            // Xóa các gợi ý cũ
            searchSuggestionsBox.getChildren().clear();

            if (searchResults.isEmpty()) {
                // Hiển thị thông báo không tìm thấy
                Label noResultLabel = new Label(isVietnamese ?
                        "Không tìm thấy sản phẩm nào" :
                        "No products found");
                noResultLabel.setStyle(
                        "-fx-text-fill: #666666;" +
                                "-fx-padding: 5px;"
                );
                searchSuggestionsBox.getChildren().add(noResultLabel);
            } else {
                // Hiển thị tối đa 5 kết quả
                int count = Math.min(searchResults.size(), 5);
                for (int i = 0; i < count; i++) {
                    Product product = searchResults.get(i);
                    HBox resultItem = createSearchResultItem(product);
                    searchSuggestionsBox.getChildren().add(resultItem);

                    // Thêm đường gạch ngang ngăn cách trừ item cuối cùng
                    if (i < count - 1) {
                        Separator separator = new Separator();
                        separator.setStyle("-fx-opacity: 0.3;");
                        searchSuggestionsBox.getChildren().add(separator);
                    }
                }

                // Nếu có nhiều kết quả hơn, hiển thị nút "Xem thêm..."
                if (searchResults.size() > 5) {
                    Separator separator = new Separator();
                    separator.setStyle("-fx-opacity: 0.3;");
                    searchSuggestionsBox.getChildren().add(separator);

                    Label viewMoreLabel = new Label(isVietnamese ?
                            "Xem thêm " + (searchResults.size() - 5) + " sản phẩm..." :
                            "View more " + (searchResults.size() - 5) + " products...");
                    viewMoreLabel.setStyle(
                            "-fx-text-fill: #1976D2;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-padding: 5px;" +
                                    "-fx-cursor: hand;"
                    );

                    // Khi click vào "Xem thêm", thực hiện tìm kiếm đầy đủ
                    viewMoreLabel.setOnMouseClicked(e -> {
                        searchPopup.hide();
                        searchProducts(keyword);
                    });

                    searchSuggestionsBox.getChildren().add(viewMoreLabel);
                }
            }

            // Hiển thị popup dưới thanh tìm kiếm
            if (!searchPopup.isShowing() && searchField.getScene() != null && searchField.getScene().getWindow() != null) {
                Bounds bounds = searchField.localToScreen(searchField.getBoundsInLocal());
                searchPopup.show(searchField,
                        bounds.getMinX(),
                        bounds.getMaxY());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi thực hiện tìm kiếm trực tiếp: " + e.getMessage());
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

        return container;
    }
    /**
     * Hiển thị kết quả tìm kiếm với giao diện chuyên dụng
     */
    private void displaySearchResults(String keyword, List<Product> products) {
        try {
            // 1. Lấy AnchorPane chính từ ScrollPane
            AnchorPane contentPane = (AnchorPane) centerScrollPane.getContent();
            if (contentPane == null) return;
            contentPane.getChildren().clear();

            // 2. Tạo container chính
            VBox mainContainer = new VBox(20);
            mainContainer.setPadding(new Insets(20));

            // 3. Hiển thị tiêu đề tìm kiếm với từ khóa được highlight
            Label searchTitle = new Label(isVietnamese ?
                    "KẾT QUẢ TÌM KIẾM CHO: \"" + keyword + "\"" :
                    "SEARCH RESULTS FOR: \"" + keyword + "\"");
            searchTitle.setStyle(
                    "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1976D2; " +
                            "-fx-background-color: white; -fx-padding: 10px; -fx-background-radius: 5px;"
            );

            HBox titleBox = new HBox();
            titleBox.setAlignment(Pos.CENTER);
            titleBox.getChildren().add(searchTitle);
            mainContainer.getChildren().add(titleBox);

            // 4. Hiển thị số lượng kết quả tìm thấy
            Label countLabel = new Label(String.format(
                    isVietnamese ? "Tìm thấy %d sản phẩm" : "Found %d products",
                    products.size()
            ));
            countLabel.setStyle("-fx-font-size: 16px; -fx-font-style: italic; -fx-text-alignment: center;");

            HBox countBox = new HBox();
            countBox.setAlignment(Pos.CENTER);
            countBox.getChildren().add(countLabel);
            mainContainer.getChildren().add(countBox);

            if (products.isEmpty()) {
                // 5a. Nếu không có kết quả, hiển thị thông báo
                Label noResultsLabel = new Label(isVietnamese ?
                        "Không tìm thấy sản phẩm nào phù hợp với từ khóa \"" + keyword + "\"" :
                        "No products found matching \"" + keyword + "\"");
                noResultsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #757575; -fx-font-style: italic;");

                HBox noResultsBox = new HBox();
                noResultsBox.setAlignment(Pos.CENTER);
                noResultsBox.getChildren().add(noResultsLabel);
                mainContainer.getChildren().add(noResultsBox);

                // Thêm gợi ý
                Label suggestionLabel = new Label(isVietnamese ?
                        "Gợi ý: Thử tìm kiếm với từ khóa khác hoặc kiểm tra chính tả" :
                        "Suggestion: Try searching with different keywords or check your spelling");
                suggestionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #9E9E9E;");

                HBox suggestionBox = new HBox();
                suggestionBox.setAlignment(Pos.CENTER);
                suggestionBox.getChildren().add(suggestionLabel);
                mainContainer.getChildren().add(suggestionBox);
            } else {
                // 5b. Hiển thị sản phẩm trong FlowPane với căn giữa
                FlowPane productsContainer = new FlowPane();
                productsContainer.setAlignment(Pos.CENTER);
                productsContainer.setHgap(20);
                productsContainer.setVgap(20);
                productsContainer.setPrefWrapLength(contentPane.getWidth() - 40);

                // Thêm từng sản phẩm vào container
                for (Product product : products) {
                    VBox productCard = createProductCard(product);

                    // Highlight sản phẩm nếu tên chứa từ khóa chính xác
                    if (product.getProductName().toLowerCase().contains(keyword.toLowerCase())) {
                        productCard.setStyle(productCard.getStyle() +
                                "-fx-border-color: #2196F3; -fx-border-width: 2px;");
                    }

                    productsContainer.getChildren().add(productCard);
                }

                mainContainer.getChildren().add(productsContainer);
            }

            // 6. Thêm container chính vào contentPane
            contentPane.getChildren().add(mainContainer);
            AnchorPane.setTopAnchor(mainContainer, 0.0);
            AnchorPane.setLeftAnchor(mainContainer, 0.0);
            AnchorPane.setRightAnchor(mainContainer, 0.0);

            // 7. Cuộn lên đầu trang
            centerScrollPane.setVvalue(0.0);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể hiển thị kết quả tìm kiếm: " + e.getMessage() :
                            "Cannot display search results: " + e.getMessage()
            );
        }
    }



    @FXML
    private void applyPriceFilter() {
        try {
            String minText = minPriceField.getText().trim();
            String maxText = maxPriceField.getText().trim();

            if (minText.isEmpty() && maxText.isEmpty()) {
                AlertUtils.showWarning(
                        isVietnamese ? "Cảnh báo" : "Warning",
                        isVietnamese ? "Vui lòng nhập ít nhất một khoảng giá" : "Please enter at least one price value"
                );
                return;
            }

            Double min = minText.isEmpty() ? null : Double.parseDouble(minText);
            Double max = maxText.isEmpty() ? null : Double.parseDouble(maxText);

            // Kiểm tra điều kiện hợp lệ
            if (min != null && max != null && min > max) {
                AlertUtils.showWarning(
                        isVietnamese ? "Lọc giá" : "Price Filter",
                        isVietnamese ? "Giá thấp nhất không được lớn hơn giá cao nhất" :
                                "Minimum price cannot be greater than maximum price"
                );
                return;
            }

            // Lưu thông tin lọc giá
            currentMinPrice = min;
            currentMaxPrice = max;

            // Áp dụng tất cả bộ lọc (kết hợp với danh mục và hãng nếu có)
            applyCurrentFilters();

            // Hiện thông báo thành công
            AlertUtils.showInfo(
                    isVietnamese ? "Lọc giá" : "Price Filter",
                    isVietnamese ? "Đã áp dụng lọc giá" : "Price filter has been applied"
            );

        } catch (NumberFormatException e) {
            AlertUtils.showWarning(
                    isVietnamese ? "Lọc giá" : "Price Filter",
                    isVietnamese ? "Vui lòng nhập giá trị hợp lệ" : "Please enter valid price values"
            );
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Lỗi khi lọc giá: " + e.getMessage() : "Error filtering by price: " + e.getMessage()
            );
        }
    }

    /**
     * PHƯƠNG THỨC MỚI: Hiển thị sản phẩm đã lọc theo khoảng giá
     */
    private void replaceContentWithFilteredProducts(List<Product> products, Double min, Double max) {
        try {
            // 1. Lấy AnchorPane chính từ ScrollPane
            AnchorPane contentPane = (AnchorPane) centerScrollPane.getContent();
            if (contentPane == null) return;

            // 2. Xóa sạch nội dung hiện tại
            contentPane.getChildren().clear();

            // 3. Tạo container mới để hiển thị kết quả
            VBox mainContainer = new VBox(15);
            mainContainer.setPadding(new Insets(20));

            // 4. Tạo tiêu đề lọc giá với màu nổi bật
            String priceRangeText;
            if (min != null && max != null) {
                priceRangeText = String.format("%,.0f₫ - %,.0f₫", min, max);
            } else if (min != null) {
                priceRangeText = String.format("≥ %,.0f₫", min);
            } else {
                priceRangeText = String.format("≤ %,.0f₫", max);
            }

            String titleText = isVietnamese ?
                    "SẢN PHẨM TRONG KHOẢNG GIÁ: " + priceRangeText :
                    "PRODUCTS IN PRICE RANGE: " + priceRangeText;

            Label titleLabel = new Label(titleText);
            titleLabel.setStyle(
                    "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1976D2; " +
                            "-fx-background-color: #E3F2FD; -fx-padding: 10px; -fx-background-radius: 5px;"
            );

            // 5. Hiển thị số lượng sản phẩm tìm thấy
            Label countLabel = new Label(isVietnamese ?
                    "Tìm thấy " + products.size() + " sản phẩm" :
                    "Found " + products.size() + " products");
            countLabel.setStyle("-fx-font-size: 16px; -fx-font-style: italic;");

            // 6. Tạo FlowPane để hiển thị sản phẩm
            FlowPane productsContainer = new FlowPane();
            productsContainer.setHgap(20);
            productsContainer.setVgap(20);
            productsContainer.setPrefWrapLength(contentPane.getWidth() - 40);

            // 7. Thêm sản phẩm vào FlowPane
            for (Product product : products) {
                VBox productCard = createProductCard(product);
                productsContainer.getChildren().add(productCard);
            }

            // 8. Thêm các thành phần vào container chính
            mainContainer.getChildren().addAll(titleLabel, countLabel, productsContainer);

            // 9. Thêm container chính vào AnchorPane và thiết lập các constraints
            contentPane.getChildren().add(mainContainer);
            AnchorPane.setTopAnchor(mainContainer, 0.0);
            AnchorPane.setLeftAnchor(mainContainer, 0.0);
            AnchorPane.setRightAnchor(mainContainer, 0.0);

            // 10. Cuộn về đầu trang
            centerScrollPane.setVvalue(0.0);

            // 11. Kích hoạt cập nhật layout
            Platform.runLater(() -> {
                mainContainer.requestLayout();
                contentPane.requestLayout();
                centerScrollPane.requestLayout();
            });

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi hiển thị sản phẩm đã lọc: " + e.getMessage());
            AlertUtils.showError(
                    isVietnamese ? "Lỗi hiển thị" : "Display Error",
                    isVietnamese ? "Không thể hiển thị sản phẩm đã lọc: " + e.getMessage() :
                            "Cannot display filtered products: " + e.getMessage()
            );
        }
    }

    // Phương thức mới để hiển thị sản phẩm đã lọc theo giá
    private void displayFilteredProducts(List<Product> products, Double min, Double max) {
        if (centerScrollPane != null) {
            // Tạo tiêu đề cho kết quả lọc
            String priceRangeText;
            if (min != null && max != null) {
                priceRangeText = String.format("%,.0f₫ - %,.0f₫", min, max);
            } else if (min != null) {
                priceRangeText = String.format("≥ %,.0f₫", min);
            } else {
                priceRangeText = String.format("≤ %,.0f₫", max);
            }

            String titleText = isVietnamese ?
                    "Sản phẩm trong khoảng giá: " + priceRangeText :
                    "Products in price range: " + priceRangeText;

            Label titleLabel = new Label(titleText);
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FF0000;");

            // Tạo container mới để hiển thị kết quả lọc
            AnchorPane contentPane = (AnchorPane) centerScrollPane.getContent();
            contentPane.getChildren().clear();

            VBox container = new VBox(20);
            container.setPadding(new Insets(20, 40, 20, 40));
            container.setAlignment(Pos.CENTER_LEFT);
            container.getChildren().add(titleLabel);

            // Tạo FlowPane để hiển thị sản phẩm
            FlowPane flowPane = new FlowPane();
            flowPane.setHgap(20);
            flowPane.setVgap(20);
            flowPane.setPrefWrapLength(900); // Điều chỉnh độ rộng

            // Hiển thị sản phẩm đã lọc
            for (Product product : products) {
                VBox card = createProductCard(product);
                flowPane.getChildren().add(card);
            }

            container.getChildren().add(flowPane);
            contentPane.getChildren().add(container);
            AnchorPane.setTopAnchor(container, 0.0);
            AnchorPane.setLeftAnchor(container, 0.0);
            AnchorPane.setRightAnchor(container, 0.0);

            // Đảm bảo cuộn lên đầu
            centerScrollPane.setVvalue(0);
        }
    }
    @FXML
    private void openYoutube() {
        openLink("https://www.youtube.com");
    }

    @FXML
    private void openFacebook() {
        openLink("https://www.facebook.com");
    }

    private void openLink(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở liên kết: " + e.getMessage() : "Could not open link: " + e.getMessage()
            );
        }
    }


    @FXML
    private void handleLogout() {
        try {
            // Đảm bảo lưu trạng thái ngôn ngữ hiện tại vào LanguageManager trước khi đăng xuất
            LanguageManager.setVietnamese(isVietnamese);
            System.out.println("Đăng xuất với ngôn ngữ: " + (isVietnamese ? "Tiếng Việt" : "English"));

            // Lấy stage từ bất kỳ control nào đã được khởi tạo
            Stage stage = null;

            // Thử lấy stage từ các control khác nhau
            if (userMenuButton != null && userMenuButton.getScene() != null) {
                stage = (Stage) userMenuButton.getScene().getWindow();
            } else if (logoIcon != null && logoIcon.getScene() != null) {
                stage = (Stage) logoIcon.getScene().getWindow();
            } else if (menuButton != null && menuButton.getScene() != null) {
                stage = (Stage) menuButton.getScene().getWindow();
            }

            // Nếu không thể lấy stage từ control nào, thông báo lỗi
            if (stage == null) {
                AlertUtils.showError(
                        isVietnamese ? "Lỗi" : "Error",
                        isVietnamese ? "Không thể tìm thấy cửa sổ ứng dụng để đăng xuất" :
                                "Cannot find application window to logout"
                );
                return;
            }

            // Lưu kích thước và vị trí hiện tại của cửa sổ
            double width = stage.getWidth();
            double height = stage.getHeight();
            double x = stage.getX();
            double y = stage.getY();

            // Tải màn hình đăng nhập
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomerLogin.fxml"));
            Parent root = loader.load();

            // Thiết lập scene mới
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Giữ nguyên kích thước và vị trí
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setX(x);
            stage.setY(y);

            // Lấy controller và gọi phương thức public để cập nhật ngôn ngữ
            CustomerLoginController controller = loader.getController();
            if (controller != null) {
                Platform.runLater(() -> {
                    controller.updateInitialLanguage(); // Chỉ gọi phương thức public này
                });
            }

            System.out.println("Đã đăng xuất thành công");
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể tải màn hình đăng nhập: " + e.getMessage() :
                            "Could not load login screen: " + e.getMessage()
            );
        }
    }

    /**
     * Phương thức để làm mới hiển thị ngôn ngữ từ LanguageManager
     * Được gọi sau khi đăng nhập để đảm bảo đồng bộ ngôn ngữ
     */
    /**
     * Phương thức public để cập nhật ngôn ngữ khi quay lại từ các màn hình khác
     */
    public void refreshLanguageDisplay() {
        // Lấy ngôn ngữ hiện tại từ LanguageManager
        isVietnamese = LanguageManager.isVietnamese();
        System.out.println("Đã cập nhật ngôn ngữ từ màn hình khác: " + (isVietnamese ? "Tiếng Việt" : "English"));

        // Cập nhật giao diện theo ngôn ngữ mới
        updateLanguage();

        // Cập nhật danh mục với ngôn ngữ mới
        updateCategoriesLanguage();

        // Tải lại các danh mục sản phẩm với ngôn ngữ mới
        Platform.runLater(() -> {
            loadProducts();
            loadFeaturedProducts();
            loadAllCategoryProducts();
        });
    }

// Sửa phương thức setupResponsiveLayout để tránh NullPointerException

    private void setupResponsiveLayout() {
        Platform.runLater(() -> {
            if (rootPane != null && rootPane.getScene() != null) {
                Scene scene = rootPane.getScene();

                // Kích thước thiết kế gốc trong Home.fxml
                final double originalWidth = 1000;
                final double originalHeight = 650;

                // Xử lý thay đổi kích thước cửa sổ
                scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                    // Tính toán tỷ lệ
                    double scaleX = newVal.doubleValue() / originalWidth;

                    // Điều chỉnh rootPane
                    rootPane.setPrefWidth(newVal.doubleValue());

                    // Điều chỉnh categoryPane để tránh bị biến dạng khi phóng to
                    if (categoryPane != null) {
                        // Giữ nguyên kích thước thực của categoryPane
                        categoryPane.setScaleX(1.0);
                        categoryPane.setScaleY(1.0);

                        // Điều chỉnh vị trí của categoryPane theo tỷ lệ mới
                        if (categoryPane.isVisible() && categoryPane.getTranslateX() > -225) {
                            categoryPane.setTranslateY(65 * scaleX);
                        }
                    }
                });

                scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                    double scaleY = newVal.doubleValue() / originalHeight;
                    rootPane.setPrefHeight(newVal.doubleValue());

                    // Điều chỉnh các phần tử theo chiều cao
                    if (centerScrollPane != null) {
                        centerScrollPane.setPrefHeight(newVal.doubleValue() - 100);
                    }
                });

                // QUAN TRỌNG: Ghi đè phương thức hiển thị cho menu popup
                if (priceButton != null) {
                    priceButton.showingProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal) {
                            // Điều chỉnh kích thước menu popup khi hiển thị
                            Platform.runLater(() -> {
                                if (priceButton.getContextMenu() != null) {
                                    priceButton.getContextMenu().setStyle(
                                            "-fx-font-size: 14px;" +
                                                    "-fx-min-width: 200px;" +
                                                    "-fx-pref-width: 250px;" +
                                                    "-fx-max-width: 300px;" +
                                                    "-fx-background-color: white;" +
                                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0.5, 0.0, 0.0);"
                                    );
                                }
                            });
                        }
                    });
                }

                // Thiết lập xử lý đặc biệt cho categoryPane
                setupCategoryPaneResponsive();

                System.out.println("Đã thiết lập tính năng phóng to/thu nhỏ theo tỷ lệ màn hình");
            } else {
                // Thử lại sau khi scene đã được khởi tạo
                PauseTransition delay = new PauseTransition(Duration.millis(100));
                delay.setOnFinished(e -> setupResponsiveLayout());
                delay.play();
            }
        });
    }

    /**
     * Thiết lập xử lý riêng cho categoryPane để tránh biến dạng khi phóng to
     */
    private void setupCategoryPaneResponsive() {
        if (categoryPane == null) return;

        // Ghi đè hàm toggleCategory để kiểm soát việc hiển thị danh mục
        if (menuButton != null) {
            menuButton.setOnAction(e -> {
                toggleCategoryResponsive();
            });
        }

        if (closeCategoryBtn != null) {
            closeCategoryBtn.setOnAction(e -> {
                toggleCategoryResponsive();
            });
        }
    }

    /**
     * Toggle category với xử lý responsive
     */
    private void toggleCategoryResponsive() {
        if (categoryPane == null || slideTransition == null) return;

        // Dừng animation hiện tại nếu đang chạy
        slideTransition.stop();

        // Xử lý animation mở/đóng
        if (isCategoryOpen) {
            // ĐÓNG panel
            slideTransition.setFromX(0);
            slideTransition.setToX(-225);

            // Thiết lập để ẩn panel sau khi animation kết thúc
            slideTransition.setOnFinished(e -> categoryPane.setVisible(false));
        } else {
            // Đảm bảo categoryPane có kích thước cố định
            categoryPane.setScaleX(1.0);
            categoryPane.setScaleY(1.0);
            categoryPane.setMaxHeight(420);
            categoryPane.setMaxWidth(225);

            // Điều chỉnh vị trí hiển thị theo kích thước hiện tại của cửa sổ
            double scaleY = rootPane.getScene().getHeight() / 650;
            categoryPane.setTranslateY(65 * scaleY);

            // Hiển thị panel trước khi bắt đầu animation
            categoryPane.setVisible(true);

            // MỞ panel
            slideTransition.setFromX(-225);
            slideTransition.setToX(0);

            // Xóa handler khi kết thúc nếu có
            slideTransition.setOnFinished(null);
        }

        // Chạy animation
        slideTransition.play();

        // Đảo trạng thái
        isCategoryOpen = !isCategoryOpen;
    }

    /**
     * Cải thiện cách hiển thị menu giá
     */
    private void setupImprovedPriceButton() {
        if (priceButton != null) {
            // Điều chỉnh style menu popup
            priceButton.setPopupSide(Side.BOTTOM);

            // Xử lý khi click vào nút giá
            priceButton.setOnShowing(e -> {
                // Đặt lại kích thước và style cho menu popup
                Platform.runLater(() -> {
                    if (priceButton.getContextMenu() != null) {
                        priceButton.getContextMenu().setStyle(
                                "-fx-font-size: 14px;" +
                                        "-fx-pref-width: 250px;" +
                                        "-fx-background-color: white;" +
                                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0.5, 0.0, 0.0);"
                        );
                    }
                });
            });
        }
    }
    // Phương thức tìm label theo text
    private Label findLabelWithText(Parent parent, String viText, String enText) {
        if (parent == null) return null;

        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                String labelText = label.getText();
                if (labelText != null && (labelText.contains(viText) || labelText.contains(enText))) {
                    return label;
                }
            }
            if (node instanceof Parent) {
                Label result = findLabelWithText((Parent) node, viText, enText);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    // Trong phần showProductDetail(Product product) của HomeController.java:

    private void showProductDetail(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/ProductDetail.fxml"));
            Parent root = loader.load();

            ProductDetailController controller = loader.getController();
            controller.loadProductDetails(product.getProductID());

            Scene scene = new Scene(root);
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở trang chi tiết sản phẩm" : "Could not open product detail page"
            );
        }
    }
    /**
     * Phương thức thống nhất để tải hình ảnh sản phẩm từ nhiều dạng đường dẫn khác nhau
     * @param imagePath Đường dẫn hình ảnh từ database
     * @return Image đã tải hoặc null nếu không tìm thấy
     */
    private Image loadProductImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            System.out.println("Đường dẫn hình ảnh null hoặc rỗng, sử dụng hình mặc định");
            return loadDefaultImage();
        }

        System.out.println("Đang tải hình ảnh từ đường dẫn: " + imagePath);

        // Chuẩn hóa đường dẫn (thay \ thành /)
        String normalizedPath = imagePath.replace('\\', '/');

        // 1. Xử lý đường dẫn chính xác hơn
        if (normalizedPath.startsWith("/")) {
            try {
                InputStream is = getClass().getResourceAsStream(normalizedPath);
                if (is != null) {
                    Image image = new Image(is);
                    System.out.println("Đã tải thành công từ đường dẫn tuyệt đối: " + normalizedPath);
                    return image;
                }
            } catch (Exception e) {
                System.out.println("Lỗi khi tải từ đường dẫn tuyệt đối: " + e.getMessage());
            }
        }

        // 2. Trích xuất tên file
        String fileName = extractFileName(normalizedPath);
        System.out.println("Tên file trích xuất: " + fileName);

        // 3. Thử các đường dẫn khác nhau
        String[] pathsToTry = {
                "/com/example/stores/images/products/" + fileName,
                "/com/example/stores/images/" + fileName,
                "/images/products/" + fileName,
                "/images/" + fileName
        };

        for (String path : pathsToTry) {
            try {
                System.out.println("Thử tải từ: " + path);
                InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    Image image = new Image(is);
                    System.out.println("Đã tải thành công từ: " + path);
                    return image;
                }
            } catch (Exception e) {
                System.out.println("Không thể tải từ " + path + ": " + e.getMessage());
            }
        }

        // 4. Thử tải trực tiếp từ file system
        try {
            File file = new File(imagePath);
            if (file.exists() && file.isFile()) {
                Image image = new Image(file.toURI().toString());
                System.out.println("Đã tải thành công từ file system: " + file.getAbsolutePath());
                return image;
            } else {
                System.out.println("File không tồn tại trong hệ thống: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi tải từ file system: " + e.getMessage());
        }

        // 5. Nếu mọi cách đều thất bại, tải hình theo tên danh mục
        try {
            // Thử tải hình ảnh theo danh mục sản phẩm (ví dụ: hdd1.jpg)
            String categoryPrefix = fileName.replaceAll("\\d+.*$", "");
            if (!categoryPrefix.isEmpty()) {
                String categoryImagePath = "/com/example/stores/images/products/" + categoryPrefix + "1.jpg";
                System.out.println("Thử tải hình từ danh mục: " + categoryImagePath);

                InputStream is = getClass().getResourceAsStream(categoryImagePath);
                if (is != null) {
                    Image image = new Image(is);
                    System.out.println("Đã tải hình danh mục: " + categoryImagePath);
                    return image;
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi tải hình theo danh mục: " + e.getMessage());
        }

        // 6. Trả về hình mặc định nếu tất cả đều thất bại
        System.out.println("Không tìm thấy hình, sử dụng hình mặc định");
        return loadDefaultImage();
    }

    /**
     * Tải hình ảnh mặc định khi không tìm thấy hình ảnh sản phẩm
     * @return Image mặc định
     */
    private Image loadDefaultImage() {
        // Thử các đường dẫn hình ảnh mặc định
        String[] defaultPaths = {
                "/com/example/stores/images/layout/computer_illustration.jpg",
                "/com/example/stores/images/products/lap1.jpg",
                "/com/example/stores/images/default_product.png",
                "/images/products/lap1.jpg",
                "/images/default_product.png"
        };

        for (String path : defaultPaths) {
            try {
                InputStream inputStream = getClass().getResourceAsStream(path);
                if (inputStream != null) {
                    Image image = new Image(inputStream);
                    System.out.println("Đã tải hình ảnh mặc định từ: " + path);
                    return image;
                }
            } catch (Exception e) {
                // Tiếp tục thử đường dẫn khác
            }
        }

        // Tạo hình ảnh trống nếu không tìm được hình ảnh mặc định
        System.out.println("Tạo hình ảnh trống");
        Canvas canvas = new Canvas(150, 120);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, 150, 120);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(new Font(14));
        gc.fillText("No Image", 75, 60);

        WritableImage writableImage = new WritableImage(150, 120);
        canvas.snapshot(null, writableImage);
        return writableImage;
    }
    /**
     * Trích xuất tên file từ đường dẫn
     * @param path Đường dẫn đầy đủ
     * @return Tên file
     */
    private String extractFileName(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        // Chuẩn hóa đường dẫn
        String normalizedPath = path.replace('\\', '/');

        // Lấy phần sau dấu / cuối cùng
        int lastSlashIndex = normalizedPath.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            return normalizedPath.substring(lastSlashIndex + 1);
        }

        // Trường hợp không có dấu /, trả về nguyên chuỗi
        return normalizedPath;
    }

    // Thêm phương thức này vào HomeController
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
    /**
     * Mở giao diện Lịch sử mua hàng (OrderHistory.fxml)
     */
    private void openOrderHistoryScreen() {
        try {
            // Lưu trạng thái ngôn ngữ hiện tại vào LanguageManager
            LanguageManager.setVietnamese(isVietnamese);
            System.out.println("Mở giao diện Lịch sử mua hàng với ngôn ngữ: " +
                    (isVietnamese ? "Tiếng Việt" : "English"));

            // Lấy Stage hiện tại từ bất kỳ control nào đã được khởi tạo
            Stage currentStage = (Stage) userMenuButton.getScene().getWindow();

            // Lưu kích thước và vị trí hiện tại của cửa sổ
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();

            // Load giao diện OrderHistory.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/OrderHistory.fxml"));
            Parent root = loader.load();

            // Thiết lập scene mới
            Scene scene = new Scene(root);

            // Thiết lập tiêu đề dựa trên ngôn ngữ hiện tại
            currentStage.setTitle(isVietnamese ? "CELLCOMP STORE - Lịch sử mua hàng" :
                    "CELLCOMP STORE - Order History");

            // Đặt scene mới vào stage
            currentStage.setScene(scene);

            // Giữ nguyên kích thước và vị trí cửa sổ
            currentStage.setWidth(width);
            currentStage.setHeight(height);
            currentStage.setX(x);
            currentStage.setY(y);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở trang lịch sử mua hàng: " + e.getMessage() :
                            "Cannot open order history page: " + e.getMessage()
            );
        }
    }
    // Add this method to the HomeController class

    @FXML
    private void openCustomDesignScreen() {
        try {
            // Lưu trạng thái ngôn ngữ hiện tại
            LanguageManager.setVietnamese(isVietnamese);

            // Thêm logging để debug
            System.out.println("Đang cố gắng mở Custom PC Builder...");

            // Tải FXML - kiểm tra đường dẫn này có chính xác không
            String fxmlPath = "/com/example/stores/view/CustomPCBuilder.fxml";
            System.out.println("Đang tải FXML từ: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Lấy Stage hiện tại
            Stage stage = (Stage) userMenuButton.getScene().getWindow();

            // Chuyển sang giao diện PC Builder
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Thiết kế máy tính" : "CELLCOMP STORE - PC Builder");
            stage.show();

            System.out.println("Đã mở giao diện Custom PC Builder thành công");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi mở giao diện Custom PC Builder: " + e.getMessage());
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở trang thiết kế máy tính: " + e.getMessage()
                            : "Cannot open PC Builder page: " + e.getMessage()
            );
        }
    }
}
