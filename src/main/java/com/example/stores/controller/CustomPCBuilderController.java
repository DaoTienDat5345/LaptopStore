package com.example.stores.controller;

import com.example.stores.util.AlertUtils;
import com.example.stores.util.LanguageManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class CustomPCBuilderController implements Initializable {

    @FXML private Label totalCostLabel;
    @FXML private VBox componentsContainer;

    // Component Selection Boxes
    @FXML private VBox cpuSelectionBox;
    @FXML private VBox mainboardSelectionBox;
    @FXML private VBox ramSelectionBox;
    @FXML private VBox hddSelectionBox;
    @FXML private VBox ssdSelectionBox;
    @FXML private VBox vgaSelectionBox;
    @FXML private VBox psuSelectionBox;
    @FXML private VBox coolingSelectionBox;
    @FXML private VBox caseSelectionBox;
    @FXML private VBox monitorSelectionBox;
    @FXML private VBox keyboardSelectionBox;
    @FXML private VBox mouseSelectionBox;
    @FXML private VBox headphoneSelectionBox;
    @FXML private Label titleLabel;  // Thêm mới
    @FXML private Label cartLabel;   // Thêm mới
    @FXML private ImageView languageIcon;

    @FXML private Button checkoutButton;

    // Menu items
    @FXML private MenuButton userMenuButton;
    @FXML private MenuItem accountInfoMenuItem;
    @FXML private MenuItem languageSwitchMenuItem;
    @FXML private MenuItem orderHistoryMenuItem;

    // Map to store VBox references by category
    private Map<String, VBox> componentBoxes = new HashMap<>();

    // Map to store selected products
    private Map<String, Product> selectedComponents = new HashMap<>();

    private double totalCost = 0;
    private boolean isVietnamese = true;
    private NumberFormat currencyFormat = new DecimalFormat("#,###");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        isVietnamese = LanguageManager.isVietnamese();

        // Initialize component boxes map
        initializeComponentBoxesMap();

        // Update UI language
        updateLanguage();

        // Thêm mới - khởi tạo biểu tượng cờ
        try {
            if (isVietnamese) {
                languageIcon.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/flag_en.png")));
            } else {
                languageIcon.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/flag_vn.png")));
            }
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu không tải được hình ảnh
        }

        // Setup user menu
        setupUserMenu();

        // Disable checkout button initially
        checkoutButton.setDisable(true);
        // Thêm dòng này vào hàm initialize(), sau updateLanguage()
        javafx.application.Platform.runLater(() -> {
            // Đảm bảo scene đã sẵn sàng rồi mới cập nhật label hướng dẫn
            if (totalCostLabel != null && totalCostLabel.getScene() != null) {
                updateSceneElements();
            }
        });
    }

    private void initializeComponentBoxesMap() {
        componentBoxes.put("CPU", cpuSelectionBox);
        componentBoxes.put("MainBoard", mainboardSelectionBox);
        componentBoxes.put("RAM", ramSelectionBox);
        componentBoxes.put("HDD", hddSelectionBox);
        componentBoxes.put("SSD", ssdSelectionBox);
        componentBoxes.put("VGA", vgaSelectionBox);
        componentBoxes.put("Nguồn", psuSelectionBox);
        componentBoxes.put("Tản nhiệt", coolingSelectionBox);
        componentBoxes.put("Case", caseSelectionBox);
        componentBoxes.put("Màn hình", monitorSelectionBox);
        componentBoxes.put("Bàn phím", keyboardSelectionBox);
        componentBoxes.put("Chuột", mouseSelectionBox);
        componentBoxes.put("Tai nghe", headphoneSelectionBox);
    }

    @FXML
    private void selectComponent(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        String category = (String) sourceButton.getUserData();

        // Get products for this category
        List<Product> products = ProductRepository.getProductsByCategory(category);

        // Create component selection dialog
        Stage dialogStage = new Stage();
        dialogStage.setTitle(isVietnamese ? "Chọn " + category : "Select " + category);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        // Add title
        Label titleLabel = new Label(isVietnamese ? "Chọn " + category : "Select " + category);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        content.getChildren().add(titleLabel);

        // Add products list
        VBox productsList = new VBox(5);
        productsList.setStyle("-fx-background-color: white;");

        for (Product product : products) {
            HBox productItem = createProductItem(product, category, dialogStage);
            productsList.getChildren().add(productItem);
        }

        ScrollPane scrollPane = new ScrollPane(productsList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: white;");

        content.getChildren().add(scrollPane);

        // Add cancel button - thêm cursor style
        Button cancelButton = new Button(isVietnamese ? "Đóng" : "Close");
        cancelButton.setOnAction(e -> dialogStage.close());
        cancelButton.setStyle("-fx-background-color: #e0e0e0; -fx-cursor: hand;");
        content.getChildren().add(cancelButton);

        Scene scene = new Scene(content, 600, 500);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private HBox createProductItem(Product product, String category, Stage dialogStage) {
        HBox productItem = new HBox(10);
        productItem.setPadding(new Insets(10));
        productItem.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        productItem.setAlignment(Pos.CENTER_LEFT);

        // Product image
        ImageView imageView = new ImageView();
        try {
            imageView.setImage(new Image(getClass().getResourceAsStream(product.getImagePath())));
        } catch (Exception e) {
            // Use a default image if the product image can't be loaded
            try {
                imageView.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/default_product.png")));
            } catch (Exception ex) {
                // If even default image fails, just leave it empty
            }
        }
        imageView.setFitHeight(60);
        imageView.setFitWidth(60);
        imageView.setPreserveRatio(true);

        // Product info
        VBox productInfo = new VBox(5);
        productInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(productInfo, Priority.ALWAYS);

        Label nameLabel = new Label(product.getProductName());
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label priceLabel = new Label(currencyFormat.format(product.getPrice()) + " đ");
        priceLabel.setStyle("-fx-text-fill: #e74c3c;");

        productInfo.getChildren().addAll(nameLabel, priceLabel);

        // Select button - thêm cursor style
        Button selectButton = new Button(isVietnamese ? "Chọn" : "Select");
        selectButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        selectButton.setOnAction(e -> {
            selectProductForCategory(product, category);
            dialogStage.close();
        });

        productItem.getChildren().addAll(imageView, productInfo, selectButton);
        return productItem;
    }

    private void selectProductForCategory(Product product, String category) {
        // Get the VBox for this category
        VBox selectionBox = componentBoxes.get(category);
        if (selectionBox == null) {
            return;
        }

        // Clear existing content
        selectionBox.getChildren().clear();

        // Create the selected component info
        HBox selectedComponent = new HBox(10);
        selectedComponent.getStyleClass().add("selected-component");
        selectedComponent.setAlignment(Pos.CENTER_LEFT);

        // Product image
        ImageView imageView = new ImageView();
        try {
            imageView.setImage(new Image(getClass().getResourceAsStream(product.getImagePath())));
        } catch (Exception e) {
            try {
                imageView.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/default_product.png")));
            } catch (Exception ex) {
                // If even default image fails, just leave it empty
            }
        }
        imageView.setFitHeight(50);
        imageView.setFitWidth(50);
        imageView.setPreserveRatio(true);

        // Product info
        VBox productInfo = new VBox(2);
        HBox.setHgrow(productInfo, Priority.ALWAYS);

        Label nameLabel = new Label(product.getProductName());
        nameLabel.getStyleClass().add("selected-component-name");

        Label priceLabel = new Label(currencyFormat.format(product.getPrice()) + " đ");
        priceLabel.getStyleClass().add("selected-component-price");

        productInfo.getChildren().addAll(nameLabel, priceLabel);

        // Remove button
        Button removeButton = new Button(isVietnamese ? "Xóa" : "Remove");
        removeButton.getStyleClass().add("remove-component-button");
        removeButton.setOnAction(e -> removeSelectedComponent(category));

        selectedComponent.getChildren().addAll(imageView, productInfo, removeButton);

        // Add to selectionBox
        selectionBox.getChildren().add(selectedComponent);

        // Store the selected product
        selectedComponents.put(category, product);

        // Update the total cost
        updateTotalCost();

        // Enable checkout if at least one component is selected
        checkoutButton.setDisable(selectedComponents.isEmpty());
    }

    private void removeSelectedComponent(String category) {
        // Get the VBox for this category
        VBox selectionBox = componentBoxes.get(category);
        if (selectionBox == null) {
            return;
        }

        // Clear existing content
        selectionBox.getChildren().clear();

        // Add back the "Select" button
        Button selectButton = new Button(isVietnamese ? "Chọn " + category : "Select " + category);
        selectButton.getStyleClass().add("component-select-button");
        selectButton.setUserData(category);
        selectButton.setOnAction(this::selectComponent);

        // Add icon to button
        try {
            ImageView addIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/Add.png")));
            addIcon.setFitHeight(16);
            addIcon.setFitWidth(16);
            selectButton.setGraphic(addIcon);
        } catch (Exception e) {
            // If icon loading fails, continue without icon
        }

        selectionBox.getChildren().add(selectButton);

        // Remove from selected components
        selectedComponents.remove(category);

        // Update the total cost
        updateTotalCost();

        // Disable checkout if no components are selected
        checkoutButton.setDisable(selectedComponents.isEmpty());
    }

    private void updateTotalCost() {
        totalCost = 0;
        for (Product product : selectedComponents.values()) {
            totalCost += product.getPrice();
        }

        totalCostLabel.setText((isVietnamese ? "Chi phí dự tính: " : "Estimated Cost: ") +
                currencyFormat.format(totalCost) + " đ");
    }

    @FXML
    private void resetSelection() {
        for (String category : componentBoxes.keySet()) {
            removeSelectedComponent(category);
        }

        selectedComponents.clear();
        totalCost = 0;
        updateTotalCost();
        checkoutButton.setDisable(true);
    }

    @FXML
    private void proceedToCheckout() {
        if (selectedComponents.isEmpty()) {
            AlertUtils.showWarning(
                    isVietnamese ? "Cảnh báo" : "Warning",
                    isVietnamese ? "Vui lòng chọn ít nhất một linh kiện để thanh toán" : "Please select at least one component to checkout"
            );
            return;
        }

        try {
            // Convert selected products to CartItem list
            List<CartItem> selectedItems = new ArrayList<>();
            for (Product product : selectedComponents.values()) {
                CartItem item = new CartItem();
                item.setProduct(product);
                item.setQuantity(1);
                item.setSelected(true);
                selectedItems.add(item);
            }

            // Load Orders.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Orders.fxml"));
            Parent root = loader.load();

            // Pass the selected products to OrderController
            OrderController controller = loader.getController();
            controller.setCartItems(selectedItems);

            // Show Orders scene
            Scene scene = new Scene(root);
            Stage stage = (Stage) checkoutButton.getScene().getWindow();
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Đơn hàng" : "CELLCOMP STORE - Order");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể chuyển đến trang thanh toán" : "Cannot navigate to checkout page"
            );
        }
    }

    private void updateLanguage() {
        if (isVietnamese) {
            // Menu items
            languageSwitchMenuItem.setText("Chuyển đổi ngôn ngữ");
            accountInfoMenuItem.setText("Thông tin tài khoản");
            orderHistoryMenuItem.setText("Lịch sử mua hàng");

            // Main UI elements
            titleLabel.setText("THIẾT KẾ MÁY TÍNH");
            cartLabel.setText("Giỏ hàng");
            totalCostLabel.setText("Chi phí dự tính: " + currencyFormat.format(totalCost) + " đ");
            checkoutButton.setText("THANH TOÁN CẤU HÌNH");

            // Cập nhật biểu tượng ngôn ngữ
            try {
                languageIcon.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/flag_en.png")));
            } catch (Exception e) {
                // Xử lý ngoại lệ
            }
        } else {
            // Menu items
            languageSwitchMenuItem.setText("Switch language");
            accountInfoMenuItem.setText("Account information");
            orderHistoryMenuItem.setText("Order history");

            // Main UI elements
            titleLabel.setText("PC BUILDER");
            cartLabel.setText("Cart");
            totalCostLabel.setText("Estimated Cost: " + currencyFormat.format(totalCost) + " đ");
            checkoutButton.setText("CHECKOUT CONFIGURATION");

            // Cập nhật biểu tượng ngôn ngữ
            try {
                languageIcon.setImage(new Image(getClass().getResourceAsStream("/com/example/stores/images/layout/flag_vn.png")));
            } catch (Exception e) {
                // Xử lý ngoại lệ
            }
        }

        // Update all component selection buttons
        updateComponentLabels(isVietnamese);

        // Refresh selected components language
        refreshSelectedComponentsLanguage();

        // Cập nhật scene elements sau khi scene đã được tạo
        javafx.application.Platform.runLater(this::updateSceneElements);
    }

    // Thêm phương thức mới để cập nhật các phần tử scene sau khi scene đã được tạo
    private void updateSceneElements() {
        if (totalCostLabel == null || totalCostLabel.getScene() == null) {
            return; // Nếu scene chưa sẵn sàng, thoát
        }

        // Reset button
        Button resetButton = (Button) totalCostLabel.getScene().lookup("#resetButton");
        if (resetButton != null) {
            resetButton.setText(isVietnamese ? "LÀM MỚI" : "RESET");
        }

        // Guide text - Thay đổi cách tìm label hướng dẫn
        totalCostLabel.getScene().getRoot().lookupAll(".label").forEach(node -> {
            if (node instanceof Label) {
                Label label = (Label) node;
                String text = label.getText();
                if (text != null && (
                        text.contains("Vui lòng chọn linh kiện") ||
                                text.contains("Please select components"))) {

                    label.setText(isVietnamese ?
                            "Vui lòng chọn linh kiện bạn cần để xây dựng cấu hình máy tính riêng cho bạn" :
                            "Please select components you need to build your custom PC configuration");
                }
            }
        });
    }
    private void updateComponentLabels(boolean isVietnamese) {
        // Danh sách các cặp dịch Việt-Anh cho các thành phần
        Map<String, String> categoryTranslations = new HashMap<>();
        categoryTranslations.put("CPU", "CPU");
        categoryTranslations.put("MainBoard", "MainBoard");
        categoryTranslations.put("RAM", "RAM");
        categoryTranslations.put("HDD", "HDD");
        categoryTranslations.put("SSD", "SSD");
        categoryTranslations.put("VGA", "VGA");
        categoryTranslations.put("Nguồn", "Power Supply");
        categoryTranslations.put("Tản nhiệt", "Cooling");
        categoryTranslations.put("Case", "Case");
        categoryTranslations.put("Màn hình", "Monitor");
        categoryTranslations.put("Bàn phím", "Keyboard");
        categoryTranslations.put("Chuột", "Mouse");
        categoryTranslations.put("Tai nghe", "Headphones");

        // Cập nhật tất cả các nhãn và nút trong container
        if (componentsContainer != null) {
            for (javafx.scene.Node row : componentsContainer.getChildren()) {
                if (row instanceof HBox) {
                    HBox hbox = (HBox) row;

                    // Tìm và cập nhật nhãn thành phần
                    for (javafx.scene.Node node : hbox.getChildren()) {
                        if (node instanceof Label) {
                            Label label = (Label) node;
                            String text = label.getText();

                            // Cập nhật nhãn dựa vào định dạng "X. Tên thành phần"
                            if (text != null && text.matches("\\d+\\. .*")) {
                                String numberPart = text.substring(0, text.indexOf(".") + 1);
                                String componentPart = text.substring(text.indexOf(".") + 2);

                                // Tìm bản dịch hoặc giữ nguyên nếu không có
                                String translatedComponent = componentPart;
                                for (Map.Entry<String, String> entry : categoryTranslations.entrySet()) {
                                    if (isVietnamese && componentPart.equals(entry.getValue())) {
                                        translatedComponent = entry.getKey();
                                        break;
                                    } else if (!isVietnamese && componentPart.equals(entry.getKey())) {
                                        translatedComponent = entry.getValue();
                                        break;
                                    }
                                }

                                label.setText(numberPart + " " + translatedComponent);
                            }
                        }
                    }

                    // Tìm VBox chứa nút chọn
                    for (javafx.scene.Node node : hbox.getChildren()) {
                        if (node instanceof VBox) {
                            VBox vbox = (VBox) node;
                            for (javafx.scene.Node child : vbox.getChildren()) {
                                if (child instanceof Button) {
                                    Button button = (Button) child;
                                    String userData = (String) button.getUserData();
                                    if (userData != null) {
                                        // Cập nhật text của nút
                                        if (isVietnamese) {
                                            button.setText("Chọn " + userData);
                                        } else {
                                            // Dùng bản dịch cho tên thành phần nếu có
                                            String translatedCategory = categoryTranslations.getOrDefault(userData, userData);
                                            button.setText("Select " + translatedCategory);
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
    private void refreshSelectedComponentsLanguage() {
        // Cập nhật lại tất cả các thành phần đã chọn để hiển thị đúng ngôn ngữ nút xóa
        for (Map.Entry<String, Product> entry : selectedComponents.entrySet()) {
            String category = entry.getKey();
            VBox selectionBox = componentBoxes.get(category);
            if (selectionBox != null && !selectionBox.getChildren().isEmpty()) {
                for (javafx.scene.Node node : selectionBox.getChildren()) {
                    if (node instanceof HBox) {
                        HBox productBox = (HBox) node;
                        for (javafx.scene.Node child : productBox.getChildren()) {
                            if (child instanceof Button) {
                                Button removeButton = (Button) child;
                                if (removeButton.getStyleClass().contains("remove-component-button")) {
                                    removeButton.setText(isVietnamese ? "Xóa" : "Remove");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @FXML
    private void switchLanguage() {
        isVietnamese = !isVietnamese;
        LanguageManager.setVietnamese(isVietnamese);
        updateLanguage();
    }
    @FXML
    private void goBackToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) totalCostLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Trang chủ" : "CELLCOMP STORE - Home");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể quay về trang chủ" : "Cannot navigate to home page"
            );
        }
    }

    @FXML
    private void goToCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/Cart.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) totalCostLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Giỏ hàng" : "CELLCOMP STORE - Cart");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể chuyển đến giỏ hàng" : "Cannot navigate to cart"
            );
        }
    }

    @FXML
    private void handleLogout() {
        // Implement logout functionality
    }

    private void setupUserMenu() {
        // Setup account info menu item
        if (accountInfoMenuItem != null) {
            accountInfoMenuItem.setOnAction(event -> openAccountInfo());
        }

        // Setup order history menu item
        if (orderHistoryMenuItem != null) {
            orderHistoryMenuItem.setOnAction(event -> openOrderHistory());
        }
    }

    private void openAccountInfo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/CustomerChange.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) totalCostLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Thông tin tài khoản" : "CELLCOMP STORE - Account Information");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở trang thông tin tài khoản" : "Cannot open account information page"
            );
        }
    }

    private void openOrderHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/OrderHistory.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) totalCostLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(isVietnamese ? "CELLCOMP STORE - Lịch sử mua hàng" : "CELLCOMP STORE - Order History");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError(
                    isVietnamese ? "Lỗi" : "Error",
                    isVietnamese ? "Không thể mở trang lịch sử mua hàng" : "Cannot open order history page"
            );
        }
    }
}