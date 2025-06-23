package com.example.stores.controller;

import com.example.stores.model.Category;
import com.example.stores.model.Product;
import com.example.stores.repository.CategoryRepository;
import com.example.stores.repository.ProductRepository;
import com.example.stores.repository.impl.CategoryRepositoryImpl;
import com.example.stores.repository.impl.ProductRepositoryImpl;
import com.example.stores.service.CategoryService;
import com.example.stores.service.ProductService;
import com.example.stores.service.impl.CategoryServiceImpl;
import com.example.stores.service.impl.ProductServiceImpl;

import com.example.stores.util.LanguageForManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ProductManagementController {

    //<editor-fold desc="FXML Controls">
    @FXML private ImageView imgProductPhoto;
    @FXML private Button btnChangeProductImage;
    @FXML private TextField txtProductID;
    @FXML private TextField txtProductName;
    @FXML private ComboBox<Category> cmbCategory; // Hiển thị Category objects
    @FXML private TextField txtPrice;
    @FXML private TextField txtPriceCost;
    @FXML private TextField txtQuantity;
    @FXML private TextArea txtDescription;

    @FXML private Button btnAddProduct;
    @FXML private Button btnUpdateProduct;
    @FXML private Button btnDeleteProduct;
    @FXML private Button btnClearForm;

    @FXML private TextField txtSearchProductKeyword;
    @FXML private Button btnSearchProduct;
    @FXML private ComboBox<Category> cmbFilterCategory; // Lọc theo Category objects
    @FXML private Button btnRefreshProductTable;

    @FXML private TableView<Product> productTableView;
    @FXML private TableColumn<Product, String> colProductID;
    @FXML private TableColumn<Product, String> colProductName;
    @FXML private TableColumn<Product, String> colCategoryName; // Hiển thị categoryNameDisplay
    @FXML private TableColumn<Product, Double> colPrice;       // << SỬA KIỂU
    @FXML private TableColumn<Product, Double> colPriceCost;   // << SỬA KIỂU
    @FXML private TableColumn<Product, Integer> colQuantity;
    @FXML private TableColumn<Product, String> colStatus;
    //</editor-fold>

    private ProductService productService;
    private CategoryService categoryService;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<Category> categoryObservableList = FXCollections.observableArrayList(); // DS Category cho ComboBoxes

    private Product selectedProduct;
    private File selectedImageFile;

    private static final String DEFAULT_PRODUCT_IMAGE_PATH = "/com/example/stores/images/img.png"; // Ảnh mặc định cho sản phẩm
    private static final String PRODUCT_IMAGES_DIRECTORY = "user_images/products"; // Thư mục lưu ảnh sản phẩm

    private static final String APP_EXECUTION_DIRECTORY = System.getProperty("user.dir");
    private static final String USER_UPLOADED_PRODUCT_IMAGES_SUBDIR = "user_images" + File.separator + "products";

    // Định dạng tiền tệ
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));


    public ProductManagementController() {
        ProductRepository productRepository = new ProductRepositoryImpl();
        CategoryRepository categoryRepository = new CategoryRepositoryImpl(); // Khởi tạo CategoryRepo

        this.categoryService = new CategoryServiceImpl(categoryRepository, productRepository); // CategoryService cần ProductRepo
        this.productService = new ProductServiceImpl(productRepository, categoryRepository);   // ProductService cần CategoryRepo

        // Khởi tạo numberInputFormatter (không có dấu phẩy/chấm phân cách nhóm)
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US); // Dùng US để dấu '.' là thập phân

    }

    @FXML
    public void initialize() {
        File productImagesDir = new File(PRODUCT_IMAGES_DIRECTORY);
        if (!productImagesDir.exists()) {
            productImagesDir.mkdirs();
        }

        setupTableColumns(); // Sẽ được gọi lại trong updateUITexts nếu cần cập nhật header cột
        setupComboBoxes();   // Sẽ được gọi lại trong updateUITexts để cập nhật prompt text / items
        loadProductsToTable();
        setupTableViewSelectionListener();
        // clearForm(); // Sẽ được gọi trong updateUITexts lần đầu

        // Lắng nghe sự thay đổi ngôn ngữ từ LanguageForManager
        LanguageForManager.getInstance().currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> {
            updateUITexts();
            // Khi ngôn ngữ thay đổi, có thể cần load lại dữ liệu cho ComboBoxes
            // nếu text của item trong ComboBox cần được dịch (ví dụ "Tất cả danh mục")
            // Hoặc reload lại bảng để các cellFactory (nếu có text tĩnh) được cập nhật
            loadProductsToTable(); // Tải lại bảng để đảm bảo cell factories (nếu có) được cập nhật
        });
        updateUITexts(); // Gọi lần đầu để thiết lập ngôn ngữ ban đầu
    }

    private void updateUITexts() {
        LanguageForManager lm = LanguageForManager.getInstance();

        // Cập nhật các nút (FXML đã dùng %key, nhưng để chắc chắn hoặc nếu có nút tạo bằng code)
        if(btnAddProduct != null) btnAddProduct.setText(lm.getString("button.addProduct"));
        if(btnUpdateProduct != null) btnUpdateProduct.setText(lm.getString("button.updateProduct"));
        if(btnDeleteProduct != null) btnDeleteProduct.setText(lm.getString("button.deleteProduct"));
        if(btnClearForm != null) btnClearForm.setText(lm.getString("button.clearForm"));
        if(btnChangeProductImage != null) btnChangeProductImage.setText(lm.getString("button.changePhoto"));
        if(btnSearchProduct != null) btnSearchProduct.setText(lm.getString("button.search"));
        if(btnRefreshProductTable != null) btnRefreshProductTable.setText(lm.getString("button.refreshTable"));

        // Cập nhật PromptText (FXML đã dùng %key, nhưng để chắc chắn)
        if(txtProductID != null) txtProductID.setPromptText(lm.getString("prompt.autoGenerated"));
        if(txtProductName != null) txtProductName.setPromptText(lm.getString("prompt.productName"));
        if(cmbCategory != null) cmbCategory.setPromptText(lm.getString("prompt.selectCategory"));
        if(txtPrice != null) txtPrice.setPromptText(lm.getString("prompt.price"));
        if(txtPriceCost != null) txtPriceCost.setPromptText(lm.getString("prompt.priceCost"));
        if(txtQuantity != null) txtQuantity.setPromptText(lm.getString("prompt.quantity"));
        if(txtDescription != null) txtDescription.setPromptText(lm.getString("prompt.description"));
        if(txtSearchProductKeyword != null) txtSearchProductKeyword.setPromptText(lm.getString("search.prompt.product"));
        if(cmbFilterCategory != null) cmbFilterCategory.setPromptText(lm.getString("prompt.allCategories"));


        // Cập nhật Header của TableColumn (FXML đã dùng %key, nhưng làm ở đây để chắc chắn)
        // Hoặc nếu bạn muốn thay đổi text của cột mà không reload FXML
        if(colProductID != null) colProductID.setText(lm.getString("table.col.productId"));
        if(colProductName != null) colProductName.setText(lm.getString("table.col.productName"));
        if(colCategoryName != null) colCategoryName.setText(lm.getString("table.col.category"));
        if(colPrice != null) colPrice.setText(lm.getString("table.col.price"));
        if(colPriceCost != null) colPriceCost.setText(lm.getString("table.col.priceCost"));
        if(colQuantity != null) colQuantity.setText(lm.getString("table.col.quantity"));
        if(colStatus != null) colStatus.setText(lm.getString("table.col.status"));

        // Cập nhật placeholder của TableView
        if(productTableView != null) productTableView.setPlaceholder(new Label(lm.getString("table.placeholder.noProducts")));

        // Cập nhật lại items cho cmbFilterCategory nếu "Tất cả danh mục" cần dịch
        // Đoạn này cần cẩn thận để không làm mất lựa chọn hiện tại nếu có thể
        if (cmbFilterCategory != null) {
            Category currentFilterSelection = cmbFilterCategory.getValue();
            ObservableList<Category> filterCategories = FXCollections.observableArrayList();
            filterCategories.add(null); // Đại diện cho "Tất cả danh mục"
            if(categoryObservableList != null) filterCategories.addAll(categoryObservableList); // categoryObservableList đã được load các category

            cmbFilterCategory.setItems(null); // Xóa items cũ để trigger converter vẽ lại
            cmbFilterCategory.setItems(filterCategories);
            cmbFilterCategory.setConverter(new StringConverter<Category>() { // Converter phải được set lại sau khi set items mới nếu items thay đổi
                @Override
                public String toString(Category category) {
                    return category == null ? lm.getString("prompt.allCategories") : category.getCategoryName();
                }
                @Override
                public Category fromString(String string) { return null; }
            });
            // Khôi phục lựa chọn nếu có thể
            if (currentFilterSelection != null && filterCategories.contains(currentFilterSelection)) {
                cmbFilterCategory.setValue(currentFilterSelection);
            } else if (!filterCategories.isEmpty()){
                cmbFilterCategory.getSelectionModel().selectFirst();
            }
        }

        // Clear form để các prompt text (nếu có trên Label thay vì TextField) được cập nhật
        // và trạng thái nút đúng, hoặc populate lại nếu có selectedProduct
        if (selectedProduct != null) {
            populateFormWithProductData(selectedProduct);
        } else {
            clearForm();
        }
    }

    private void setupTableColumns() {
        colProductID.setCellValueFactory(new PropertyValueFactory<>("productID"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colCategoryName.setCellValueFactory(new PropertyValueFactory<>("categoryNameDisplay")); // Dùng trường đã JOIN
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPriceCost.setCellValueFactory(new PropertyValueFactory<>("priceCost"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Định dạng cột tiền tệ
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPriceCost.setCellValueFactory(new PropertyValueFactory<>("priceCost"));

        colPrice.setCellFactory(tc -> new TableCell<Product, Double>() { // << SỬA KIỂU
            @Override
            protected void updateItem(Double item, boolean empty) { // << SỬA KIỂU
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormatter.format(item)); // currencyFormatter nhận double
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
        colPriceCost.setCellFactory(tc -> new TableCell<Product, Double>() { // << SỬA KIỂU
            @Override
            protected void updateItem(Double item, boolean empty) { // << SỬA KIỂU
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormatter.format(item));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
    }

    private TableCell<Product, BigDecimal> formatCurrencyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormatter.format(item.doubleValue()));
                }
            }
        };
    }

    private void setupComboBoxes() {
        // Load categories cho ComboBox thêm/sửa sản phẩm
        List<Category> categories = categoryService.getAllCategories();
        categoryObservableList.setAll(categories);

        StringConverter<Category> categoryStringConverter = new StringConverter<>() {
            @Override public String toString(Category c) { return c == null ? null : c.getCategoryName(); }
            @Override public Category fromString(String s) { return null; /* Không cho nhập tự do */ }
        };

        cmbCategory.setItems(categoryObservableList);
        cmbCategory.setConverter(categoryStringConverter);

        // Load categories cho ComboBox lọc (thêm tùy chọn "Tất cả")
        ObservableList<Category> filterCategories = FXCollections.observableArrayList();
        filterCategories.add(null); // Đại diện cho "Tất cả danh mục"
        filterCategories.addAll(categories);

        cmbFilterCategory.setItems(filterCategories);
        cmbFilterCategory.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category == null ? "Tất cả danh mục" : category.getCategoryName();
            }
            @Override
            public Category fromString(String string) { return null; }
        });
        cmbFilterCategory.getSelectionModel().selectFirst(); // Chọn "Tất cả" làm mặc định

        // Listener cho ComboBox lọc
        cmbFilterCategory.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filterProductsByCategory(newVal);
        });
    }

    private void filterProductsByCategory(Category selectedCategory) {
        if (selectedCategory == null) { // "Tất cả danh mục" được chọn
            productTableView.setItems(FXCollections.observableList(productService.getAllProductsWithCategoryName()));
        } else {
            List<Product> filtered = productService.getProductsByCategoryIdWithCategoryName(selectedCategory.getCategoryID());
            productTableView.setItems(FXCollections.observableList(filtered));
        }
        if (productTableView.getItems().isEmpty()) {
            productTableView.setPlaceholder(new Label("Không có sản phẩm nào khớp."));
        } else {
            productTableView.setPlaceholder(null);
        }
    }


    private void loadProductsToTable() {
        try {
            List<Product> products = productService.getAllProductsWithCategoryName();
            productList.setAll(products);
            productTableView.setItems(productList);
            if (products.isEmpty()) {
                productTableView.setPlaceholder(new Label("Chưa có sản phẩm nào."));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Sản Phẩm", "Không thể tải danh sách sản phẩm.");
            e.printStackTrace();
        }
    }

    private void setupTableViewSelectionListener() {
        productTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedProduct = newSelection;
                populateFormWithProductData(selectedProduct);
                btnUpdateProduct.setDisable(false);
                btnDeleteProduct.setDisable(false);
                btnAddProduct.setDisable(true);
            }
        });
    }

    private void populateFormWithProductData(Product product) { // Bỏ warrantyMonths
        txtProductID.setText(product.getProductID());
        txtProductName.setText(product.getProductName());
        categoryObservableList.stream()
                .filter(c -> c.getCategoryID().equals(product.getCategoryID()))
                .findFirst().ifPresent(cmbCategory::setValue);
        txtPrice.setText(product.getPrice() != 0 ? String.format(Locale.US, "%.0f", product.getPrice()) : ""); // Hiển thị số nguyên, dùng Locale.US để dấu . là thập phân nếu có
        txtPriceCost.setText(product.getPriceCost() != 0 ? String.format(Locale.US, "%.0f", product.getPriceCost()) : "");
        txtQuantity.setText(String.valueOf(product.getQuantity()));
        txtDescription.setText(product.getDescription());
        loadImageToImageView(product.getImagePath());
        selectedImageFile = null;
    }

    // Trong ProductManagementController.java
    private void loadImageToImageView(String imagePathString) {
        Image image = null;
        if (imagePathString != null && !imagePathString.isEmpty()) {
            if (imagePathString.startsWith("/")) { // 1. Ưu tiên kiểm tra nếu là đường dẫn resource (bên trong JAR)
                try {
                    URL imageUrlResource = getClass().getResource(imagePathString);
                    if (imageUrlResource != null) {
                        image = new Image(imageUrlResource.toExternalForm());
                        System.out.println("Loaded image from resource: " + imagePathString);
                    } else {
                        System.err.println("Resource ảnh không tìm thấy: " + imagePathString);
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi tải resource ảnh: " + imagePathString + " - " + e.getMessage());
                }
            }

            // 2. Nếu không phải resource (hoặc resource lỗi), thử coi như đường dẫn file (có thể là tương đối từ APP_EXECUTION_DIRECTORY)
            if (image == null && !imagePathString.startsWith("/")) {
                // Giả sử imagePathString lưu đường dẫn tương đối như "user_images/products/abc.jpg"
                File imageFile = new File(APP_EXECUTION_DIRECTORY, imagePathString);
                // Hoặc nếu imagePathString đã là đường dẫn tuyệt đối thì không cần APP_EXECUTION_DIRECTORY
                // File imageFile = new File(imagePathString);

                if (imageFile.exists() && imageFile.isFile()) {
                    try {
                        image = new Image(imageFile.toURI().toURL().toString());
                        System.out.println("Loaded image from file system: " + imageFile.getAbsolutePath());
                    } catch (MalformedURLException e) {
                        System.err.println("Lỗi URL khi tải file ảnh: " + imageFile.getAbsolutePath() + " - " + e.getMessage());
                    }
                } else {
                    System.err.println("File ảnh không tìm thấy tại (file system): " + imageFile.getAbsolutePath());
                }
            }
        }

        if (image == null) { // Nếu tất cả đều thất bại, dùng ảnh mặc định
            System.out.println("Sử dụng ảnh mặc định.");
            loadDefaultProductImage();
        }
        imgProductPhoto.setImage(image);
    }

    private void loadDefaultProductImage() {
        try {
            URL defaultImageURL = getClass().getResource(DEFAULT_PRODUCT_IMAGE_PATH);
            if (defaultImageURL != null) {
                imgProductPhoto.setImage(new Image(defaultImageURL.toExternalForm()));
            } else {
                System.err.println("LỖI CRITICAL: Không tìm thấy ảnh mặc định cho sản phẩm tại: " + DEFAULT_PRODUCT_IMAGE_PATH);
                imgProductPhoto.setImage(null);
            }
        } catch (Exception ex) {
            System.err.println("Lỗi CRITICAL khi tải ảnh mặc định cho sản phẩm: " + ex.getMessage());
            imgProductPhoto.setImage(null);
        }
    }


    @FXML
    void handleAddProductAction(ActionEvent event) {
        try {
            Product newProduct = getProductFromForm();
            boolean success = productService.addProduct(newProduct);
            if (success) {
                // Sử dụng key từ resource bundle
                showAlert(Alert.AlertType.INFORMATION, "alert.title.success", "product.add.success");
                loadProductsToTable();
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "alert.title.error", "product.add.fail.triggerOrDb");
            }
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "alert.title.error", e.getMessage()); // e.getMessage() có thể là key hoặc text
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "alert.title.error", "error.system.generic", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleUpdateProductAction(ActionEvent event) {
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "alert.title.warning", "product.select.toUpdate");
            return;
        }
        try {
            Product updatedProductData = getProductFromForm();
            updatedProductData.setProductID(selectedProduct.getProductID());
            updatedProductData.setCreatedAt(selectedProduct.getCreatedAt());

            boolean success = productService.updateProduct(updatedProductData);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "alert.title.success", "product.update.success");
                loadProductsToTable();
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "alert.title.error", "product.update.fail");
            }
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "alert.title.error", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "alert.title.error", "error.system.generic", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleDeleteProductAction(ActionEvent event) {
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "alert.title.warning", "product.select.toDelete");
            return;
        }
        LanguageForManager lm = LanguageForManager.getInstance();
        String headerText = MessageFormat.format(lm.getString("product.delete.confirm.header"),
                selectedProduct.getProductName(), selectedProduct.getProductID());

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                lm.getString("product.delete.confirm.content"), // Content không có tham số
                ButtonType.YES, ButtonType.NO);
        confirmation.setTitle(lm.getString("product.delete.confirm.title"));
        confirmation.setHeaderText(headerText);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    boolean success = productService.deleteProduct(selectedProduct.getProductID());
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "alert.title.success", "product.delete.success");
                        loadProductsToTable();
                        clearForm();
                    }
                } catch (IllegalArgumentException e) { // Lỗi từ service (ví dụ: sản phẩm đang được dùng)
                    showAlert(Alert.AlertType.ERROR, "alert.title.error", e.getMessage());
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "alert.title.error", "error.system.generic", e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }


    @FXML
    void handleClearFormAction(ActionEvent event) {
        clearForm();
    }

    private void clearForm() {
        productTableView.getSelectionModel().clearSelection();

        selectedProduct = null;

        txtProductID.clear();
        txtProductName.clear();
        cmbCategory.getSelectionModel().clearSelection();
        txtPrice.clear();
        txtPriceCost.clear();
        txtQuantity.clear();
        txtDescription.clear();

        // Xử lý tải ảnh mặc định an toàn hơn
        try {
            URL defaultImageURL = getClass().getResource(DEFAULT_PRODUCT_IMAGE_PATH);
            if (defaultImageURL != null) {
                imgProductPhoto.setImage(new Image(defaultImageURL.toExternalForm()));
            } else {
                System.err.println("LỖI: Không tìm thấy ảnh mặc định cho sản phẩm tại: " + DEFAULT_PRODUCT_IMAGE_PATH);
                imgProductPhoto.setImage(null); // Hoặc một placeholder
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh mặc định cho sản phẩm: " + e.getMessage());
            imgProductPhoto.setImage(null);
        }

        loadDefaultProductImage();
        selectedImageFile = null;

        btnAddProduct.setDisable(false);
        btnUpdateProduct.setDisable(true);
        btnDeleteProduct.setDisable(true);
    }



    private Product getProductFromForm() throws IllegalArgumentException {
        LanguageForManager lm = LanguageForManager.getInstance();
        Product product = new Product();

        if (txtProductName.getText().trim().isEmpty()) throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        product.setProductName(txtProductName.getText().trim());

        Category selectedCat = cmbCategory.getValue();
        if (selectedCat == null) throw new IllegalArgumentException("Vui lòng chọn danh mục sản phẩm.");
        product.setCategoryID(selectedCat.getCategoryID());

        try {
            String priceStr = txtPrice.getText().trim().replace(".", "");
            if (priceStr.isEmpty()) throw new IllegalArgumentException("Giá bán không được để trống.");
            product.setPrice(Double.parseDouble(priceStr)); // << SỬA: Parse sang double

            String priceCostStr = txtPriceCost.getText().trim().replace(".", "");
            if (priceCostStr.isEmpty()) throw new IllegalArgumentException("Giá vốn không được để trống.");
            product.setPriceCost(Double.parseDouble(priceCostStr)); // << SỬA: Parse sang double

            String quantityStr = txtQuantity.getText().trim();
            if (quantityStr.isEmpty()) throw new IllegalArgumentException("Số lượng không được để trống.");
            int quantity = Integer.parseInt(quantityStr);
            if (quantity < 0) throw new IllegalArgumentException("Số lượng không được âm.");
            product.setQuantity(quantity);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá bán, giá vốn hoặc số lượng phải là số hợp lệ.");
        }

        product.setDescription(txtDescription.getText() != null ? txtDescription.getText().trim() : null);
        // KHÔNG CÒN LẤY warrantyMonths TỪ FORM

        // Xử lý ảnh (giữ nguyên logic như đã sửa ở lần trước)
        if (selectedImageFile != null) {
            try {
                File targetImageDirFile = new File(APP_EXECUTION_DIRECTORY, PRODUCT_IMAGES_DIRECTORY); // Sửa lại hằng số
                if (!targetImageDirFile.exists()) targetImageDirFile.mkdirs();

                String originalFileName = selectedImageFile.getName();
                String fileExtension = getFileExtension(originalFileName);
                String baseNameForImage;
                if (selectedProduct != null && selectedProduct.getProductID() != null && !selectedProduct.getProductID().isEmpty()) {
                    baseNameForImage = selectedProduct.getProductID().replaceAll("[^a-zA-Z0-9.-]", "_");
                } else {
                    baseNameForImage = product.getProductName().replaceAll("[^a-zA-Z0-9\\s_.-]", "").replaceAll("\\s+", "_").toLowerCase();
                    if (baseNameForImage.length() > 50) baseNameForImage = baseNameForImage.substring(0, 50);
                    if (baseNameForImage.isEmpty()) baseNameForImage = "product";
                }
                String newFileName = baseNameForImage + "_" + System.currentTimeMillis() + (fileExtension.isEmpty() ? "" : "." + fileExtension);
                Path targetPath = Paths.get(targetImageDirFile.getAbsolutePath(), newFileName);
                Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                product.setImagePath(PRODUCT_IMAGES_DIRECTORY + File.separator + newFileName);
            } catch (IOException e) {
                System.err.println("Lỗi lưu ảnh sản phẩm: " + e.getMessage());
                showAlert(Alert.AlertType.WARNING, "Lỗi Ảnh", "Không thể lưu ảnh sản phẩm.");
                if (selectedProduct != null) product.setImagePath(selectedProduct.getImagePath());
            }
        } else if (selectedProduct != null) {
            product.setImagePath(selectedProduct.getImagePath());
        } else {
            product.setImagePath(null);
        }
        return product;
    }


    @FXML
    void handleChangeProductImageAction(ActionEvent event) {
        // ... (Tương tự EmployeeManagementController) ...
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        Stage stage = (Stage) btnChangeProductImage.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedImageFile = file;
            try {
                imgProductPhoto.setImage(new Image(file.toURI().toURL().toString()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi Ảnh", "Không thể hiển thị ảnh đã chọn.");
            }
        }
    }

    @FXML
    void handleSearchProductAction(ActionEvent event) {
        String keyword = txtSearchProductKeyword.getText().trim();
        Category filterCat = cmbFilterCategory.getValue(); // Lấy category đang được chọn để lọc

        List<Product> searchResult;
        if (keyword.isEmpty() && filterCat == null) { // Không có keyword, không có filter category
            searchResult = productService.getAllProductsWithCategoryName();
        } else if (!keyword.isEmpty() && filterCat == null) { // Có keyword, không có filter category
            searchResult = productService.searchProductsWithCategoryName(keyword);
        } else if (keyword.isEmpty() && filterCat != null) { // Không có keyword, có filter category
            searchResult = productService.getProductsByCategoryIdWithCategoryName(filterCat.getCategoryID());
        } else { // Có cả keyword và filter category
            // Lọc theo category trước, rồi lọc theo keyword trên kết quả đó
            List<Product> categoryFiltered = productService.getProductsByCategoryIdWithCategoryName(filterCat.getCategoryID());
            String lowerKeyword = keyword.toLowerCase();
            searchResult = categoryFiltered.stream()
                    .filter(p -> p.getProductName().toLowerCase().contains(lowerKeyword) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerKeyword)) ||
                            p.getProductID().toLowerCase().contains(lowerKeyword) ) // Thêm tìm theo ID
                    .collect(Collectors.toList());
        }

        productList.setAll(searchResult);
        productTableView.setItems(productList);
        if (searchResult.isEmpty()) {
            productTableView.setPlaceholder(new Label("Không tìm thấy sản phẩm nào."));
        } else {
            productTableView.setPlaceholder(null);
        }
    }

    @FXML
    void handleRefreshProductTableAction(ActionEvent event) {
        txtSearchProductKeyword.clear();
        cmbFilterCategory.getSelectionModel().selectFirst(); // Chọn lại "Tất cả danh mục"
        loadProductsToTable(); // Load lại và áp dụng filter "Tất cả"
        clearForm();
        showAlert(Alert.AlertType.INFORMATION, "Làm mới", "Đã tải lại danh sách sản phẩm.");
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot == -1 || lastIndexOfDot == fileName.length() - 1) return "";
        return fileName.substring(lastIndexOfDot + 1);
    }

    private void showAlert(Alert.AlertType alertType, String titleKey, String messageKey, Object... messageParams) {
        LanguageForManager lm = LanguageForManager.getInstance();
        Alert alert = new Alert(alertType);
        alert.setTitle(lm.getString(titleKey));
        alert.setHeaderText(null);
        String message = lm.getString(messageKey);
        if (messageParams != null && messageParams.length > 0 && !(messageParams.length == 1 && messageParams[0] == null)) {
            try {
                message = MessageFormat.format(message, messageParams);
            } catch (IllegalArgumentException e) {
                System.err.println("Lỗi format message cho key: " + messageKey + " với params: " + Arrays.toString(messageParams) + " - " + e.getMessage());
            }
        }
        alert.setContentText(message);
        alert.showAndWait();
    }
}