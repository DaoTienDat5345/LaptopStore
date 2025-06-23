package com.example.stores.controller;

import com.example.stores.model.Inventory;
import com.example.stores.model.Manager;
import com.example.stores.model.Warehouse;
import com.example.stores.repository.CategoryRepository;
import com.example.stores.repository.ManagerRepository;
import com.example.stores.repository.ProductRepository;
import com.example.stores.repository.WarehouseRepository;
import com.example.stores.repository.InventoryRepository;
import com.example.stores.repository.impl.CategoryRepositoryImpl;
import com.example.stores.repository.impl.ManagerRepositoryImpl;
import com.example.stores.repository.impl.ProductRepositoryImpl;
import com.example.stores.repository.impl.WarehouseRepositoryImpl;
import com.example.stores.repository.impl.InventoryRepositoryImpl;
import com.example.stores.service.ManagerService;
import com.example.stores.service.WarehouseService;
import com.example.stores.service.InventoryService;
import com.example.stores.service.impl.ManagerServiceImpl;
import com.example.stores.service.impl.WarehouseServiceImpl;
import com.example.stores.service.impl.InventoryServiceImpl;

import com.example.stores.util.LanguageForManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WarehouseManagementController {

    //<editor-fold desc="FXML Controls - Tab Quản Lý Kho Hàng">
    @FXML private TableView<Warehouse> warehouseTableView;
    @FXML private TableColumn<Warehouse, Integer> colWarehouseID;
    @FXML private TableColumn<Warehouse, String> colWarehouseName;
    @FXML private TableColumn<Warehouse, String> colWarehouseAddress;
    @FXML private TableColumn<Warehouse, String> colWarehousePhone;
    @FXML private TableColumn<Warehouse, String> colWarehouseManager;

    @FXML private TextField txtWarehouseID;
    @FXML private TextField txtWarehouseName;
    @FXML private TextField txtWarehouseAddress;
    @FXML private TextField txtWarehousePhone;
    @FXML private ComboBox<Manager> cmbWarehouseManager;
    @FXML private Button btnAddWarehouse;
    @FXML private Button btnUpdateWarehouse;
    @FXML private Button btnDeleteWarehouse;
    @FXML private Button btnClearWarehouseForm;
    @FXML private Label lblWarehouseStatus;
    //</editor-fold>

    //<editor-fold desc="FXML Controls - Tab Quản Lý Tồn Kho">
    @FXML private ComboBox<Warehouse> cmbFilterInventoryByWarehouse;
    @FXML private TextField txtFilterInventoryByProduct;
    @FXML private Button btnFilterInventory;
    @FXML private Button btnRefreshInventoryTable;
    @FXML private TableView<Inventory> inventoryTableView;
    @FXML private TableColumn<Inventory, Integer> colInventoryID;
    @FXML private TableColumn<Inventory, String> colInventoryWarehouseName;
    @FXML private TableColumn<Inventory, String> colInventoryProductName;
    @FXML private TableColumn<Inventory, Integer> colInventoryQuantity;
    @FXML private TableColumn<Inventory, String> colInventoryProductStatus;
    @FXML private TableColumn<Inventory, LocalDateTime> colInventoryLastUpdate;
    @FXML private TableColumn<Inventory, Void> colInventoryAdjust;

    @FXML private Tab tabManageWarehouses;
    @FXML private Tab tabManageInventory;
    @FXML private Label lblWarehouseInfoTitle;
    @FXML private Label lblFilterByWarehouse;
    @FXML private Label lblFilterByProduct;
    @FXML private TitledPane assignmentPane;
    //</editor-fold>

    private WarehouseService warehouseService;
    private ManagerService managerService;
    private InventoryService inventoryService;
    // ProductService không cần trực tiếp nếu InventoryService đã xử lý tên SP

    private final ObservableList<Warehouse> warehouseList = FXCollections.observableArrayList();
    private final ObservableList<Manager> managerObservableListForComboBox = FXCollections.observableArrayList();
    private final ObservableList<Inventory> inventoryList = FXCollections.observableArrayList();
    private final ObservableList<Warehouse> filterWarehouseComboBoxList = FXCollections.observableArrayList();


    private Warehouse selectedWarehouse;
    private static final int SOLE_MANAGER_ID = 1;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public WarehouseManagementController() {
        WarehouseRepository warehouseRepository = new WarehouseRepositoryImpl();
        ManagerRepository managerRepository = new ManagerRepositoryImpl();
        InventoryRepository inventoryRepository = new InventoryRepositoryImpl();
        ProductRepository productRepository = new ProductRepositoryImpl();
        CategoryRepository categoryRepository = new CategoryRepositoryImpl(); // ProductService cần nó

        this.managerService = new ManagerServiceImpl(managerRepository);
        this.warehouseService = new WarehouseServiceImpl(warehouseRepository, managerRepository);
        // ProductService localProductService = new ProductServiceImpl(productRepository, categoryRepository); // Không cần nếu InventoryService tự lấy tên
        this.inventoryService = new InventoryServiceImpl(inventoryRepository, productRepository, warehouseRepository);
    }

    @FXML
    public void initialize() {
        setupWarehouseTableColumns();
        loadWarehousesToTable();
        setupWarehouseTableSelectionListener();
        setupInventoryTableColumns();
        loadInventoryToTable();
        setupInventoryTableActionColumn();

        if(btnFilterInventory != null) btnFilterInventory.setOnAction(event -> loadInventoryToTableFiltered());
        if(txtFilterInventoryByProduct != null) txtFilterInventoryByProduct.setOnAction(event -> loadInventoryToTableFiltered());
        if(cmbFilterInventoryByWarehouse != null) {
            cmbFilterInventoryByWarehouse.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> loadInventoryToTableFiltered());
        }

        // Lắng nghe sự thay đổi ngôn ngữ
        LanguageForManager.getInstance().currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> {
            updateUITexts();
            // Load lại dữ liệu cho các bảng và ComboBox để đảm bảo text được cập nhật
            loadWarehousesToTable(); // Điều này cũng sẽ gọi updateInventoryFilterComboBox
            loadInventoryToTable();
        });
        updateUITexts(); // Gọi lần đầu
    }

    private void updateUITexts() {
        LanguageForManager lm = LanguageForManager.getInstance();

        // Cập nhật tiêu đề màn hình (nếu có Label riêng, nếu không thì title của Stage đã được set ở MainApp/LoginController)
        // Ví dụ: lblMainTitle.setText(lm.getString("warehouse.management.title"));

        // Cập nhật text cho các Tabs
        if(tabManageWarehouses != null) tabManageWarehouses.setText(lm.getString("tab.manageWarehouses"));
        if(tabManageInventory != null) tabManageInventory.setText(lm.getString("tab.manageInventory"));

        // --- Tab Quản Lý Kho Hàng ---
        // Nút
        if(btnAddWarehouse != null) btnAddWarehouse.setText(lm.getString("button.addWarehouse"));
        if(btnUpdateWarehouse != null) btnUpdateWarehouse.setText(lm.getString("button.updateWarehouse"));
        if(btnDeleteWarehouse != null) btnDeleteWarehouse.setText(lm.getString("button.deleteWarehouse"));
        if(btnClearWarehouseForm != null) btnClearWarehouseForm.setText(lm.getString("button.clear")); // Dùng key chung

        // Label trong form
        if(lblWarehouseInfoTitle != null) lblWarehouseInfoTitle.setText(lm.getString("label.warehouseInfo"));
        // (Giả sử các label Mã Kho, Tên Kho,... trong GridPane đã dùng %key trong FXML)

        // Prompt text
        if(txtWarehouseID != null) txtWarehouseID.setPromptText(lm.getString("prompt.autoGenerated"));
        if(txtWarehouseName != null) txtWarehouseName.setPromptText(lm.getString("prompt.warehouseName"));
        if(txtWarehouseAddress != null) txtWarehouseAddress.setPromptText(lm.getString("prompt.address"));
        if(txtWarehousePhone != null) txtWarehousePhone.setPromptText(lm.getString("prompt.phone"));
        if(cmbWarehouseManager != null) {
            // Cập nhật lại items và converter cho cmbWarehouseManager nếu text "Không chọn" cần dịch
            Manager currentManagerSelection = cmbWarehouseManager.getValue();
            managerObservableListForComboBox.clear();
            Manager noManagerOption = new Manager();
            noManagerOption.setManagerID(0);
            noManagerOption.setFullName(lm.getString("prompt.selectManagerOptional.none")); // Key mới
            managerObservableListForComboBox.add(noManagerOption);
            Optional<Manager> managerOpt = managerService.getManagerProfile(SOLE_MANAGER_ID);
            managerOpt.ifPresent(managerObservableListForComboBox::add);
            cmbWarehouseManager.setItems(managerObservableListForComboBox);
            // Converter không đổi vì nó đã xử lý dựa trên ID và fullName
            if (currentManagerSelection != null && managerObservableListForComboBox.contains(currentManagerSelection)) {
                cmbWarehouseManager.setValue(currentManagerSelection);
            } else {
                cmbWarehouseManager.getSelectionModel().select(noManagerOption);
            }
        }


        // Header cột TableView Kho Hàng (FXML đã dùng %key, nhưng làm ở đây để chắc chắn)
        if(colWarehouseID != null) colWarehouseID.setText(lm.getString("table.col.warehouseId"));
        if(colWarehouseName != null) colWarehouseName.setText(lm.getString("table.col.warehouseName"));
        if(colWarehouseAddress != null) colWarehouseAddress.setText(lm.getString("table.col.address"));
        if(colWarehousePhone != null) colWarehousePhone.setText(lm.getString("table.col.phone"));
        if(colWarehouseManager != null) colWarehouseManager.setText(lm.getString("table.col.warehouseManager"));
        if(warehouseTableView != null) warehouseTableView.setPlaceholder(new Label(lm.getString("table.placeholder.noWarehouses")));


        // --- Tab Quản Lý Tồn Kho ---
        // Labels và Buttons
        if(lblFilterByWarehouse != null) lblFilterByWarehouse.setText(lm.getString("label.filterByWarehouse"));
        if(lblFilterByProduct != null) lblFilterByProduct.setText(lm.getString("label.filterByProduct"));
        if(btnFilterInventory != null) btnFilterInventory.setText(lm.getString("button.filterInventory"));
        if(btnRefreshInventoryTable != null) btnRefreshInventoryTable.setText(lm.getString("button.refreshTable"));

        // Prompt text
        if(cmbFilterInventoryByWarehouse != null) {
            // Cập nhật prompt và items nếu "Tất cả kho" cần dịch
            Warehouse currentFilterWhSelection = cmbFilterInventoryByWarehouse.getValue();
            filterWarehouseComboBoxList.clear();
            filterWarehouseComboBoxList.add(null); // null đại diện cho "Tất cả kho"
            try {
                filterWarehouseComboBoxList.addAll(warehouseService.getAllWarehousesWithManagerName());
            } catch (Exception e) { e.printStackTrace(); }
            cmbFilterInventoryByWarehouse.setItems(filterWarehouseComboBoxList);
            cmbFilterInventoryByWarehouse.setConverter(new StringConverter<Warehouse>() {
                @Override public String toString(Warehouse w) { return w == null ? lm.getString("prompt.allWarehouses") : w.getWarehouseName();}
                @Override public Warehouse fromString(String s) { return null; }
            });
            if (currentFilterWhSelection != null && filterWarehouseComboBoxList.contains(currentFilterWhSelection)) {
                cmbFilterInventoryByWarehouse.setValue(currentFilterWhSelection);
            } else if (!filterWarehouseComboBoxList.isEmpty()){
                cmbFilterInventoryByWarehouse.getSelectionModel().selectFirst();
            }
        }
        if(txtFilterInventoryByProduct != null) txtFilterInventoryByProduct.setPromptText(lm.getString("search.prompt.productOrId"));

        // Header cột TableView Tồn Kho (FXML đã dùng %key, nhưng làm ở đây để chắc chắn)
        if(colInventoryID != null) colInventoryID.setText(lm.getString("table.col.inventoryId"));
        if(colInventoryWarehouseName != null) colInventoryWarehouseName.setText(lm.getString("table.col.warehouseName"));
        if(colInventoryProductName != null) colInventoryProductName.setText(lm.getString("table.col.productName"));
        if(colInventoryQuantity != null) colInventoryQuantity.setText(lm.getString("table.col.quantity"));
        if(colInventoryProductStatus != null) colInventoryProductStatus.setText(lm.getString("table.col.productStatus"));
        if(colInventoryLastUpdate != null) colInventoryLastUpdate.setText(lm.getString("table.col.lastUpdate"));
        if(colInventoryAdjust != null) colInventoryAdjust.setText(lm.getString("table.col.adjustQuantity"));
        if(inventoryTableView != null) inventoryTableView.setPlaceholder(new Label(lm.getString("table.placeholder.noInventory")));

        // Cập nhật text cho nút "Lưu" trong cột điều chỉnh số lượng (nếu cần, vì nó tạo động)
        // Điều này cần làm mới TableView hoặc cell factory để nó vẽ lại
        if(inventoryTableView != null) inventoryTableView.refresh();


        // Cập nhật form nếu đang hiển thị (ví dụ: clearForm gọi lại để cập nhật prompt text)
        if (selectedWarehouse != null) {
            populateWarehouseForm(selectedWarehouse);
        } else {
            clearWarehouseForm();
        }
        // Tương tự cho form điều chỉnh tồn kho nếu bạn có một form riêng
    }

    // ======================== TAB QUẢN LÝ KHO HÀNG ========================
    private void setupWarehouseTableColumns() { /* Giữ nguyên từ trước */
        colWarehouseID.setCellValueFactory(new PropertyValueFactory<>("warehouseID"));
        colWarehouseName.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        colWarehouseAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colWarehousePhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colWarehouseManager.setCellValueFactory(new PropertyValueFactory<>("managerFullNameDisplay"));
    }
    private void setupWarehouseManagerComboBox() { /* Giữ nguyên từ trước */
        managerObservableListForComboBox.clear();
        Manager noManagerOption = new Manager();
        noManagerOption.setManagerID(0); noManagerOption.setFullName("-- Không chọn --");
        managerObservableListForComboBox.add(noManagerOption);
        Optional<Manager> managerOpt = managerService.getManagerProfile(SOLE_MANAGER_ID);
        managerOpt.ifPresent(managerObservableListForComboBox::add);
        cmbWarehouseManager.setItems(managerObservableListForComboBox);
        cmbWarehouseManager.setConverter(new StringConverter<>() {
            @Override public String toString(Manager m) { return m == null || m.getManagerID() == 0 ? "-- Không chọn --" : m.getFullName() + " (ID: " + m.getManagerID() + ")";}
            @Override public Manager fromString(String s) { return null; }
        });
        cmbWarehouseManager.getSelectionModel().select(noManagerOption);
    }
    private void loadWarehousesToTable() { /* Giữ nguyên từ trước, đảm bảo gọi updateInventoryFilterComboBox sau đó */
        try {
            List<Warehouse> warehouses = warehouseService.getAllWarehousesWithManagerName();
            warehouseList.setAll(warehouses);
            warehouseTableView.setItems(warehouseList);
            warehouseTableView.setPlaceholder(new Label(warehouses.isEmpty() ? "Chưa có kho hàng nào." : null));
            updateInventoryFilterComboBox(); // Cập nhật ComboBox lọc ở tab Tồn Kho
        } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tải được DS Kho."); e.printStackTrace(); }
    }
    private void setupWarehouseTableSelectionListener() { /* Giữ nguyên từ trước */
        warehouseTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) {
                selectedWarehouse = newS;
                populateWarehouseForm(selectedWarehouse);
                btnUpdateWarehouse.setDisable(false); btnDeleteWarehouse.setDisable(false); btnAddWarehouse.setDisable(true);
            }
        });
    }
    private void populateWarehouseForm(Warehouse wh) { /* Giữ nguyên từ trước */
        txtWarehouseID.setText(String.valueOf(wh.getWarehouseID()));
        txtWarehouseName.setText(wh.getWarehouseName());
        txtWarehouseAddress.setText(wh.getAddress());
        txtWarehousePhone.setText(wh.getPhone());
        if (wh.getManagerID() != null && wh.getManagerID() > 0) {
            managerObservableListForComboBox.stream().filter(m -> m.getManagerID() == wh.getManagerID()).findFirst().ifPresent(cmbWarehouseManager::setValue);
        } else {
            managerObservableListForComboBox.stream().filter(m -> m.getManagerID() == 0).findFirst().ifPresent(cmbWarehouseManager::setValue);
        }
        lblWarehouseStatus.setText("");
    }

    @FXML
    void handleFilterInventoryAction(ActionEvent event) {
        System.out.println("Nút Lọc Tồn Kho được nhấn!"); // Thêm log để kiểm tra
        loadInventoryToTableFiltered();
    }

    @FXML void handleAddWarehouseAction(ActionEvent event) { /* Giữ nguyên từ trước, chỉ gọi updateInventoryFilterComboBox */
        try {
            Warehouse newWh = getWarehouseFromForm(); Warehouse savedWh = warehouseService.addWarehouse(newWh);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm kho ID: " + savedWh.getWarehouseID());
            loadWarehousesToTable(); clearWarehouseForm();
        } catch (Exception e) { lblWarehouseStatus.setText("Lỗi: " + e.getMessage()); lblWarehouseStatus.setTextFill(javafx.scene.paint.Color.RED); e.printStackTrace(); }
    }
    @FXML void handleUpdateWarehouseAction(ActionEvent event) { /* Giữ nguyên từ trước, chỉ gọi updateInventoryFilterComboBox */
        if (selectedWarehouse == null) { showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chọn kho để sửa."); return; }
        try {
            Warehouse updatedData = getWarehouseFromForm(); updatedData.setWarehouseID(selectedWarehouse.getWarehouseID());
            if (warehouseService.updateWarehouse(updatedData)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật kho thành công.");
                loadWarehousesToTable(); clearWarehouseForm();
            } else { lblWarehouseStatus.setText("Lỗi: Không thể cập nhật."); }
        } catch (Exception e) { lblWarehouseStatus.setText("Lỗi: " + e.getMessage()); e.printStackTrace(); }
    }
    @FXML void handleDeleteWarehouseAction(ActionEvent event) { /* Giữ nguyên từ trước, chỉ gọi updateInventoryFilterComboBox */
        if (selectedWarehouse == null) { /* ... */ return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xóa kho: " + selectedWarehouse.getWarehouseName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try {
                    if (warehouseService.deleteWarehouse(selectedWarehouse.getWarehouseID())) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa kho.");
                        loadWarehousesToTable(); clearWarehouseForm();
                    }
                } catch (Exception e) { lblWarehouseStatus.setText("Lỗi: " + e.getMessage()); e.printStackTrace(); }
            }
        });
    }
    @FXML void handleClearWarehouseFormAction(ActionEvent event) { clearWarehouseForm(); }
    private void clearWarehouseForm() { /* Giữ nguyên từ trước */
        if (warehouseTableView != null) warehouseTableView.getSelectionModel().clearSelection();
        selectedWarehouse = null;
        if (txtWarehouseID != null) txtWarehouseID.clear(); if (txtWarehouseName != null) txtWarehouseName.clear();
        if (txtWarehouseAddress != null) txtWarehouseAddress.clear(); if (txtWarehousePhone != null) txtWarehousePhone.clear();
        if (cmbWarehouseManager != null && !managerObservableListForComboBox.isEmpty()) {
            managerObservableListForComboBox.stream().filter(m -> m.getManagerID() == 0).findFirst().ifPresent(cmbWarehouseManager::setValue);
        }
        if (lblWarehouseStatus != null) lblWarehouseStatus.setText("");
        if (btnAddWarehouse != null) btnAddWarehouse.setDisable(false);
        if (btnUpdateWarehouse != null) btnUpdateWarehouse.setDisable(true);
        if (btnDeleteWarehouse != null) btnDeleteWarehouse.setDisable(true);
    }
    private Warehouse getWarehouseFromForm() throws IllegalArgumentException { /* Giữ nguyên từ trước */
        Warehouse wh = new Warehouse();
        if (txtWarehouseName.getText().trim().isEmpty()) throw new IllegalArgumentException("Tên kho không trống.");
        wh.setWarehouseName(txtWarehouseName.getText().trim());
        if (txtWarehouseAddress.getText().trim().isEmpty()) throw new IllegalArgumentException("Địa chỉ không trống.");
        wh.setAddress(txtWarehouseAddress.getText().trim());
        wh.setPhone(txtWarehousePhone.getText() != null ? txtWarehousePhone.getText().trim() : null);
        Manager selMan = cmbWarehouseManager.getValue();
        if (selMan != null && selMan.getManagerID() == SOLE_MANAGER_ID) { wh.setManagerID(selMan.getManagerID()); }
        else { wh.setManagerID(null); }
        return wh;
    }

    // ======================== TAB QUẢN LÝ TỒN KHO ========================
    private void setupInventoryFilterComboBox() {
        updateInventoryFilterComboBox(); // Load và cập nhật danh sách kho
        cmbFilterInventoryByWarehouse.setConverter(new StringConverter<>() {
            @Override public String toString(Warehouse w) { return w == null ? "Tất cả kho" : w.getWarehouseName();}
            @Override public Warehouse fromString(String s) { return null; }
        });
        // Listener đã được thêm trong initialize()
    }

    private void updateInventoryFilterComboBox() {
        Warehouse currentSelection = cmbFilterInventoryByWarehouse.getValue();
        filterWarehouseComboBoxList.clear();
        filterWarehouseComboBoxList.add(null); // "Tất cả kho"
        try {
            filterWarehouseComboBoxList.addAll(warehouseService.getAllWarehousesWithManagerName());
        } catch (Exception e) { e.printStackTrace(); }
        cmbFilterInventoryByWarehouse.setItems(filterWarehouseComboBoxList);

        if (currentSelection != null && filterWarehouseComboBoxList.contains(currentSelection)) {
            cmbFilterInventoryByWarehouse.setValue(currentSelection);
        } else if (!filterWarehouseComboBoxList.isEmpty()){
            cmbFilterInventoryByWarehouse.getSelectionModel().selectFirst();
        }
    }

    private void setupInventoryTableColumns() {
        colInventoryID.setCellValueFactory(new PropertyValueFactory<>("inventoryID"));
        colInventoryWarehouseName.setCellValueFactory(new PropertyValueFactory<>("warehouseNameDisplay"));
        colInventoryProductName.setCellValueFactory(new PropertyValueFactory<>("productNameDisplay"));
        colInventoryQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colInventoryProductStatus.setCellValueFactory(new PropertyValueFactory<>("productStatusDisplay"));

        // Cấu hình cho colInventoryLastUpdate
        colInventoryLastUpdate.setCellValueFactory(new PropertyValueFactory<>("lastUpdate")); // Cung cấp LocalDateTime
        colInventoryLastUpdate.setCellFactory(column -> {
            return new TableCell<Inventory, LocalDateTime>() { // << SỬA Ở ĐÂY: Kiểu là LocalDateTime
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) { // << SỬA Ở ĐÂY: 'item' là LocalDateTime
                    super.updateItem(item, empty);
                    if (empty || item == null) { // Kiểm tra 'item' (chính là giá trị LocalDateTime của cell)
                        setText(null);
                    } else {
                        // 'item' ở đây chính là giá trị LocalDateTime của thuộc tính "lastUpdate"
                        setText(item.format(dateTimeFormatter)); // Định dạng trực tiếp 'item'
                    }
                }
            };
        });
    }
    private void loadInventoryToTable() { // Load toàn bộ ban đầu cho tab Inventory
        try {
            List<Inventory> allItems = inventoryService.getAllInventoryWithDetails();
            inventoryList.setAll(allItems);
            inventoryTableView.setItems(inventoryList); // Gán trực tiếp, filter sẽ xử lý sau
            inventoryTableView.setPlaceholder(new Label(allItems.isEmpty() ? "Chưa có dữ liệu tồn kho." : null));
            // Không gọi filter ở đây, listener của ComboBox và nút Lọc sẽ gọi
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Tồn Kho", "Không thể tải dữ liệu tồn kho.");
            e.printStackTrace();
        }
    }

    private void loadInventoryToTableFiltered() {
        Warehouse selectedWhFilter = cmbFilterInventoryByWarehouse.getValue();
        String productKeyword = txtFilterInventoryByProduct.getText().trim().toLowerCase();

        try {
            List<Inventory> baseList;
            if (selectedWhFilter == null) { // "Tất cả kho"
                baseList = inventoryService.getAllInventoryWithDetails();
            } else {
                baseList = inventoryService.getInventoryByWarehouseWithDetails(selectedWhFilter.getWarehouseID());
            }

            List<Inventory> filteredList = baseList.stream()
                    .filter(item -> productKeyword.isEmpty() ||
                            (item.getProductNameDisplay() != null && item.getProductNameDisplay().toLowerCase().contains(productKeyword)) ||
                            (item.getProductID() != null && item.getProductID().toLowerCase().contains(productKeyword)))
                    .collect(Collectors.toList());

            inventoryList.setAll(filteredList);
            // inventoryTableView.setItems(inventoryList); // Dòng này không cần vì inventoryList đã được observe
            inventoryTableView.setPlaceholder(new Label(filteredList.isEmpty() ? "Không có tồn kho khớp điều kiện." : null));

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Lọc Tồn Kho", "Không thể lọc dữ liệu tồn kho.");
            e.printStackTrace();
        }
    }

    @FXML
    void handleRefreshInventoryTableAction(ActionEvent event) {
        txtFilterInventoryByProduct.clear();
        if (cmbFilterInventoryByWarehouse != null && !filterWarehouseComboBoxList.isEmpty()) { // Sửa thành filterWarehouseComboBoxList
            cmbFilterInventoryByWarehouse.getSelectionModel().selectFirst(); // Chọn "Tất cả kho"
        }
        // loadInventoryToTable(); // Gọi hàm này sẽ load toàn bộ, listener của ComboBox sẽ tự filter
        loadInventoryToTableFiltered(); // Gọi trực tiếp hàm filter để nó lấy "Tất cả kho"
        showAlert(Alert.AlertType.INFORMATION, "Làm mới", "Đã tải lại bảng tồn kho.");
    }

    private void setupInventoryTableActionColumn() {
        Callback<TableColumn<Inventory, Void>, TableCell<Inventory, Void>> cellFactory = param -> {
            return new TableCell<>() {
                private final Spinner<Integer> quantitySpinner = new Spinner<>();
                private final Button btnSaveAdjustment = new Button("Lưu");
                private final HBox pane = new HBox(5, quantitySpinner, btnSaveAdjustment);
                {
                    pane.setAlignment(Pos.CENTER);
                    btnSaveAdjustment.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 9px;");
                    btnSaveAdjustment.setOnAction(event -> {
                        Inventory invItem = getTableView().getItems().get(getIndex());
                        int newQty = quantitySpinner.getValue();
                        handleAdjustInventoryAction(invItem, newQty);
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        Inventory invItem = (Inventory) getTableRow().getItem();
                        SpinnerValueFactory<Integer> valFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, invItem.getQuantity());
                        quantitySpinner.setValueFactory(valFactory);
                        quantitySpinner.setPrefWidth(70);
                        setGraphic(pane);
                    }
                }
            };
        };
        colInventoryAdjust.setCellFactory(cellFactory);
    }

    private void handleAdjustInventoryAction(Inventory inventoryItem, int newQuantity) {
        if (newQuantity < 0) {
            showAlert(Alert.AlertType.ERROR, "Số lượng không hợp lệ", "Số lượng điều chỉnh không thể âm.");
            loadInventoryToTableFiltered(); // Tải lại để reset spinner
            return;
        }
        TextInputDialog dialog = new TextInputDialog("Điều chỉnh tồn kho thủ công");
        dialog.setTitle("Xác nhận Điều Chỉnh Tồn Kho");
        dialog.setHeaderText("Sản phẩm: " + inventoryItem.getProductNameDisplay() + " tại Kho: " + inventoryItem.getWarehouseNameDisplay() +
                "\nSL cũ: " + inventoryItem.getQuantity() + " -> SL mới: " + newQuantity);
        dialog.setContentText("Lý do điều chỉnh:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                boolean success = inventoryService.adjustStockQuantityManually(inventoryItem.getInventoryID(), newQuantity, result.get());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật số lượng tồn kho.");
                    loadInventoryToTableFiltered();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật số lượng tồn kho.");
                }
            } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Lỗi khi điều chỉnh: " + e.getMessage()); e.printStackTrace(); }
        } else {
            loadInventoryToTableFiltered(); // Tải lại để reset spinner nếu cancel
        }
    }

    // Tiện ích Alert
    private void showAlert(Alert.AlertType alertType, String titleKey, String messageKey, Object... params) {
        LanguageForManager lm = LanguageForManager.getInstance();
        Alert alert = new Alert(alertType);
        alert.setTitle(lm.getString(titleKey));
        alert.setHeaderText(null);
        String message = lm.getString(messageKey);
        if (params != null && params.length > 0 && !(params.length == 1 && params[0] == null)) {
            try {
                message = MessageFormat.format(message, params);
            } catch (IllegalArgumentException e) {
                System.err.println("Lỗi format message cho key: " + messageKey + " với params: " + Arrays.toString(params) + " - " + e.getMessage());
            }
        }
        alert.setContentText(message);
        alert.showAndWait();
    }
}