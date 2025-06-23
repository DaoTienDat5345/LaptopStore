package com.example.stores.controller;

import com.example.stores.model.*; // Import các model cần thiết
import com.example.stores.repository.*; // Import các repository cần thiết
import com.example.stores.repository.impl.*; // Các impl repository
import com.example.stores.service.*; // Các service cần thiết
import com.example.stores.service.impl.*; // Các impl service
import com.example.stores.util.LanguageForManager;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*; // Thêm các import của Apache POI
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.text.MessageFormat; // Import MessageFormat
import java.util.Arrays; // Import Arrays (đã có thể có)

import java.io.File;
import java.io.FileOutputStream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.sun.org.apache.xml.internal.serializer.utils.Utils.messages;

public class SupplierAndImportManagementController {

    //<editor-fold desc="FXML Controls - Tab Nhà Cung Cấp">
    @FXML private TableView<Supplier> supplierTableView;
    @FXML private TableColumn<Supplier, Integer> colSupplierID;
    @FXML private TableColumn<Supplier, String> colSupplierName;
    @FXML private TableColumn<Supplier, String> colSupplierEmail;
    @FXML private TableColumn<Supplier, String> colSupplierPhone;
    @FXML private TableColumn<Supplier, String> colSupplierAddress;
    @FXML private TableColumn<Supplier, String> colSupplierTaxCode;

    @FXML private TextField txtSupplierID;
    @FXML private TextField txtSupplierName;
    @FXML private TextField txtSupplierEmail;
    @FXML private TextField txtSupplierPhone;
    @FXML private TextArea txtSupplierAddress;
    @FXML private TextField txtSupplierTaxCode;

    @FXML private Button btnAddSupplier;
    @FXML private Button btnUpdateSupplier;
    @FXML private Button btnDeleteSupplier;
    @FXML private Button btnClearSupplierForm;
    @FXML private Label lblSupplierStatus;
    @FXML private TextField txtSearchSupplier;
    @FXML private Button btnSearchSupplier;
    @FXML private Button btnRefreshSupplierTable;
    //</editor-fold>

    //<editor-fold desc="FXML Controls - Tab Phiếu Nhập Hàng">
    @FXML private TabPane mainTabPaneSupplierImport;
    @FXML private AnchorPane createImportReceiptAnchorPane; // Panel chính cho tạo/xem chi tiết
    @FXML private Label lblCreateImportTitle; // Tiêu đề của panel (Tạo mới/Chi tiết)

    // -- Controls cho lọc danh sách phiếu nhập --
    @FXML private DatePicker dpImportStartDate;
    @FXML private DatePicker dpImportEndDate;
    @FXML private ComboBox<Supplier> cmbFilterImportBySupplier;
    // @FXML private ComboBox<Warehouse> cmbFilterImportByWarehouse; // Đã bỏ trong FXML mới nhất của bạn
    @FXML private Button btnFilterImportReceipts;
    @FXML private Button btnRefreshImportReceiptTable; // Đã sửa tên fx:id trong FXML
    @FXML private Button btnShowCreateImportReceiptPane; // Đã sửa tên fx:id trong FXML

    // -- TableView hiển thị danh sách phiếu nhập --
    @FXML private TableView<ImportReceipt> importReceiptTableView;
    @FXML private TableColumn<ImportReceipt, Integer> colReceiptID;
    @FXML private TableColumn<ImportReceipt, LocalDateTime> colReceiptImportDate;
    @FXML private TableColumn<ImportReceipt, String> colReceiptSupplierName;
    @FXML private TableColumn<ImportReceipt, String> colReceiptEmployeeName;
    @FXML private TableColumn<ImportReceipt, String> colReceiptWarehouseName;
    @FXML private TableColumn<ImportReceipt, Double> colReceiptTotalAmount;
    @FXML private TableColumn<ImportReceipt, String> colReceiptNote;


    // -- Controls trong panel tạo/xem chi tiết phiếu nhập --
    @FXML private ComboBox<Supplier> cmbImportSupplier; // Chọn NCC cho phiếu nhập mới
    @FXML private ComboBox<Employee> cmbImportEmployee; // Chọn NV cho phiếu nhập mới
    @FXML private ComboBox<Warehouse> cmbImportWarehouse; // Chọn Kho cho phiếu nhập mới
    @FXML private DatePicker dpImportDate; // Ngày nhập cho phiếu mới
    @FXML private TextArea txtImportReceiptNote; // Ghi chú cho phiếu mới

    @FXML private ComboBox<Product> cmbImportProduct; // Chọn sản phẩm để thêm vào chi tiết
    @FXML private Spinner<Integer> spinnerImportQuantity;
    @FXML private TextField txtImportUnitCost; // Đơn giá nhập
    @FXML private Button btnAddImportDetail; // Nút thêm sản phẩm vào bảng chi tiết tạm thời

    @FXML private TableView<ImportReceiptDetail> importReceiptDetailTableView; // Bảng hiển thị chi tiết SP đang thêm
    @FXML private TableColumn<ImportReceiptDetail, String> colDetailProductName;
    @FXML private TableColumn<ImportReceiptDetail, Integer> colDetailQuantity;
    @FXML private TableColumn<ImportReceiptDetail, Double> colDetailUnitCost; // << SỬA KIỂU
    @FXML private TableColumn<ImportReceiptDetail, Double> colDetailSubtotal; // << SỬA KIỂU
    @FXML private TableColumn<com.example.stores.model.ImportReceipt, Void> colReceiptViewDetails;

    @FXML private Label lblImportTotalAmount; // Hiển thị tổng tiền của phiếu nhập đang tạo
    @FXML private Button btnSaveImportReceipt;
    @FXML private Button btnCancelImportReceipt;
    //</editor-fold>

    private SupplierService supplierService;
    private ImportReceiptService importReceiptService;
    private EmployeeService employeeService;
    private WarehouseService warehouseService;
    private ProductService productService;

    private final ObservableList<Supplier> supplierList = FXCollections.observableArrayList(); // Cho bảng NCC
    private final ObservableList<ImportReceipt> importReceiptList = FXCollections.observableArrayList(); // Cho bảng PN
    private final ObservableList<Supplier> suppliersForComboBox = FXCollections.observableArrayList(); // Cho các ComboBox NCC
    private final ObservableList<Employee> employeesForComboBox = FXCollections.observableArrayList(); // Cho ComboBox NV
    private final ObservableList<Warehouse> warehousesForComboBox = FXCollections.observableArrayList(); // Cho ComboBox Kho
    private final ObservableList<Product> productsForComboBox = FXCollections.observableArrayList();   // Cho ComboBox SP
    private final ObservableList<ImportReceiptDetail> currentImportDetails = FXCollections.observableArrayList(); // Chi tiết PN đang tạo

    private Supplier selectedSupplier; // NCC đang chọn trên form
    private ImportReceipt selectedImportReceipt; // Phiếu nhập đang chọn trên bảng PN (để xem chi tiết)
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public SupplierAndImportManagementController() {
        // Khởi tạo Repository
        SupplierRepository supplierRepository = new SupplierRepositoryImpl();
        ImportReceiptRepository importReceiptRepository = new ImportReceiptRepositoryImpl();
        EmployeeRepository employeeRepository = new EmployeeRepositoryImpl();
        WarehouseRepository warehouseRepository = new WarehouseRepositoryImpl();
        ProductRepository productRepository = new ProductRepositoryImpl();
        CategoryRepository categoryRepository = new CategoryRepositoryImpl();
        InventoryRepository inventoryRepository = new InventoryRepositoryImpl();
        WorkShiftScheduleRepository workShiftScheduleRepository = new WorkShiftScheduleRepositoryImpl();
        ShiftRepository shiftRepository = new ShiftRepositoryImpl();

        this.supplierService = new SupplierServiceImpl(supplierRepository);
        WorkShiftScheduleService localWSSService = new WorkShiftScheduleServiceImpl(workShiftScheduleRepository, employeeRepository, shiftRepository);
        this.employeeService = new EmployeeServiceImpl(employeeRepository, localWSSService);
        ManagerRepository managerRepository = new ManagerRepositoryImpl();
        this.warehouseService = new WarehouseServiceImpl(warehouseRepository, managerRepository);
        this.productService = new ProductServiceImpl(productRepository, categoryRepository);
        InventoryService localInventoryService = new InventoryServiceImpl(inventoryRepository, productRepository, warehouseRepository);
        this.importReceiptService = new ImportReceiptServiceImpl(importReceiptRepository, supplierRepository,
                employeeRepository, warehouseRepository, productRepository, localInventoryService);
    }

    private void exportImportReceiptToExcel(ImportReceipt receipt, File file) {
        if (receipt == null || file == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Xuất Excel", "Không có dữ liệu phiếu nhập hoặc file để xuất.");
            return;
        }

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream(file)) {

            Sheet sheet = workbook.createSheet("PhieuNhap_" + receipt.getReceiptID());

            // Tạo font cho header
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // Thông tin chung của phiếu nhập
            int rowNum = 0;
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("PHIẾU NHẬP HÀNG SỐ: " + receipt.getReceiptID());
            titleCell.setCellStyle(headerCellStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 4)); // Gộp ô cho tiêu đề

            rowNum++; // Dòng trống
            createRowWithData(sheet, rowNum++, "Ngày nhập:", receipt.getImportDate().format(dateTimeFormatter));
            createRowWithData(sheet, rowNum++, "Nhà cung cấp:", receipt.getSupplierNameDisplay() + " (ID: " + receipt.getSupplierID() + ")");
            createRowWithData(sheet, rowNum++, "Nhân viên nhập:", receipt.getEmployeeNameDisplay() + " (ID: " + receipt.getEmployeeID() + ")");
            createRowWithData(sheet, rowNum++, "Kho nhập:", receipt.getWarehouseNameDisplay() + " (ID: " + receipt.getWarehouseID() + ")");
            createRowWithData(sheet, rowNum++, "Ghi chú:", receipt.getNote());

            rowNum++; // Dòng trống
            // Header cho chi tiết sản phẩm
            Row detailHeaderRow = sheet.createRow(rowNum++);
            String[] columns = {"STT", "Mã SP", "Tên Sản Phẩm", "Số Lượng", "Đơn Giá Nhập", "Thành Tiền"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = detailHeaderRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Dữ liệu chi tiết sản phẩm
            int stt = 1;
            DataFormat excelFormat = null;
            for (ImportReceiptDetail detail : receipt.getDetails()) {
                Row detailRow = sheet.createRow(rowNum++);
                detailRow.createCell(0).setCellValue(stt++);
                detailRow.createCell(1).setCellValue(detail.getProductID());
                detailRow.createCell(2).setCellValue(detail.getProductNameDisplay()); // Cần đảm bảo trường này có dữ liệu
                detailRow.createCell(3).setCellValue(detail.getQuantity());
                detailRow.createCell(4).setCellValue(detail.getUnitCost()); // Xuất dạng số
                detailRow.createCell(5).setCellValue(detail.getSubtotal()); // Xuất dạng số

                // Định dạng tiền tệ cho cột đơn giá và thành tiền (tùy chọn)
                CellStyle currencyStyle = workbook.createCellStyle();
                excelFormat = workbook.createDataFormat();
                currencyStyle.setDataFormat(excelFormat.getFormat("#,##0 \"đ\"")); // Ví dụ định dạng VNĐ
                detailRow.getCell(4).setCellStyle(currencyStyle);
                detailRow.getCell(5).setCellStyle(currencyStyle);
            }

            rowNum++; // Dòng trống
            // Tổng tiền
            Row totalRow = sheet.createRow(rowNum++);
            totalRow.createCell(4).setCellValue("Tổng cộng:");
            totalRow.getCell(4).setCellStyle(headerCellStyle);
            Cell totalAmountCell = totalRow.createCell(5);
            totalAmountCell.setCellValue(receipt.getTotalAmount());
            CellStyle totalCurrencyStyle = workbook.createCellStyle();
            totalCurrencyStyle.setFont(headerFont); // In đậm tổng tiền
            totalCurrencyStyle.setDataFormat(excelFormat.getFormat("#,##0 \"đ\""));
            totalAmountCell.setCellStyle(totalCurrencyStyle);


            // Tự động điều chỉnh độ rộng cột
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(fileOut);
            showAlert(Alert.AlertType.INFORMATION, "Xuất Excel Thành Công", "Đã xuất phiếu nhập ra file: " + file.getAbsolutePath());

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Xuất Excel", "Đã xảy ra lỗi khi ghi file Excel: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Không Xác Định", "Đã xảy ra lỗi không mong muốn khi xuất Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createRowWithData(Sheet sheet, int rowNum, String label, String data) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(data);
    }

    @FXML
    public void initialize() {
        // Lắng nghe sự thay đổi ngôn ngữ
        LanguageForManager.getInstance().currentLocaleProperty().addListener((obs, oldL, newL) -> updateUITexts());

        // --- Tab Nhà Cung Cấp ---
        setupSupplierTableColumns();
        // loadSuppliersToTable(); // Sẽ được gọi trong updateUITexts()
        setupSupplierTableSelectionListener();
        // clearSupplierForm(); // Sẽ được gọi trong updateUITexts()

        // --- Tab Phiếu Nhập Hàng ---
        if (createImportReceiptAnchorPane != null) {
            createImportReceiptAnchorPane.setVisible(false);
            createImportReceiptAnchorPane.setManaged(false);
        }
        // loadDataForImportTabComboBoxes(); // Sẽ được gọi trong updateUITexts()
        setupImportReceiptTableColumns();
        setupImportReceiptDetailTableColumns();
        if(importReceiptDetailTableView != null) importReceiptDetailTableView.setItems(currentImportDetails);
        // loadImportReceiptsToTable(); // Sẽ được gọi trong updateUITexts()

        if (dpImportStartDate != null) dpImportStartDate.setValue(LocalDate.now().minusMonths(1));
        if (dpImportEndDate != null) dpImportEndDate.setValue(LocalDate.now());
        if (spinnerImportQuantity != null) {
            SpinnerValueFactory<Integer> quantityFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1);
            spinnerImportQuantity.setValueFactory(quantityFactory);
            spinnerImportQuantity.setEditable(true);
        }
        setupImportReceiptTableSelectionListener(); // Listener cho bảng phiếu nhập

        updateUITexts(); // Gọi lần đầu để load tất cả text và dữ liệu ban đầu
    }

    private void updateUITexts() {
        LanguageForManager lm = LanguageForManager.getInstance();

        // === Tab Nhà Cung Cấp ===
        if (txtSearchSupplier != null) txtSearchSupplier.setPromptText(lm.getString("search.prompt.supplier"));
        if (btnSearchSupplier != null) btnSearchSupplier.setText(lm.getString("button.searchSupplier"));
        if (btnRefreshSupplierTable != null) btnRefreshSupplierTable.setText(lm.getString("button.refreshTable"));

        if (colSupplierID != null) colSupplierID.setText(lm.getString("table.col.supplierId"));
        if (colSupplierName != null) colSupplierName.setText(lm.getString("table.col.supplierName"));
        // ... (các header cột khác cho supplierTableView) ...
        if (supplierTableView != null) supplierTableView.setPlaceholder(new Label(lm.getString("table.placeholder.noSuppliers")));

        // Form Nhà Cung Cấp
        // (Giả sử có Label tiêu đề cho form, ví dụ: lblSupplierFormTitle.setText(lm.getString("supplier.form.title"));)
        // Hoặc nếu tiêu đề nằm trong TitledPane, bạn cần fx:id cho TitledPane đó và set text.
        // Hiện tại FXML không có TitledPane cho form NCC.
        // Các Label bên cạnh TextField đã dùng %key trong FXML nên sẽ tự cập nhật khi FXML được reload với bundle mới.
        // Chúng ta có thể set lại prompt text nếu FXML không dùng %key cho prompt.
        if (txtSupplierID != null) txtSupplierID.setPromptText(lm.getString("prompt.autoGenerated"));
        if (txtSupplierName != null) txtSupplierName.setPromptText(lm.getString("prompt.supplierName"));
        // ... (các prompt text khác cho form NCC) ...
        if (btnAddSupplier != null) btnAddSupplier.setText(lm.getString("button.addSupplier"));
        if (btnUpdateSupplier != null) btnUpdateSupplier.setText(lm.getString("button.updateSupplier"));
        if (btnDeleteSupplier != null) btnDeleteSupplier.setText(lm.getString("button.deleteSupplier"));
        if (btnClearSupplierForm != null) btnClearSupplierForm.setText(lm.getString("button.clearForm"));

        // === Tab Phiếu Nhập Hàng ===
        if (btnShowCreateImportReceiptPane != null) btnShowCreateImportReceiptPane.setText(lm.getString("button.createImportReceipt"));
        // Bộ lọc phiếu nhập
        // (Các Label "Lọc phiếu từ ngày:", "đến ngày:", "Nhà cung cấp:" đã dùng %key trong FXML)
        if (cmbFilterImportBySupplier != null) cmbFilterImportBySupplier.setPromptText(lm.getString("prompt.allSuppliers"));
        if (btnFilterImportReceipts != null) btnFilterImportReceipts.setText(lm.getString("button.filterReceipts"));
        if (btnRefreshImportReceiptTable != null) btnRefreshImportReceiptTable.setText(lm.getString("button.refreshTable")); // Key này đã có, dùng lại

        // Bảng phiếu nhập
        if (colReceiptID != null) colReceiptID.setText(lm.getString("table.col.receiptId"));
        // ... (các header cột khác cho importReceiptTableView) ...
        if (importReceiptTableView != null) importReceiptTableView.setPlaceholder(new Label(lm.getString("table.placeholder.noImportReceipts")));

        // Panel Tạo/Xem Chi Tiết Phiếu Nhập
        if (lblCreateImportTitle != null) { // Cập nhật tiêu đề panel dựa trên trạng thái
            if (createImportReceiptAnchorPane.isVisible()) { // Chỉ cập nhật nếu panel đang hiện
                // Cần một biến để biết đang tạo mới hay xem chi tiết, hoặc dựa vào selectedImportReceipt
                if (selectedImportReceipt == null && "Tạo Phiếu Nhập Mới".equals(lblCreateImportTitle.getText()) || (messages != null && lm.getString("import.receipt.title.new").equals(lblCreateImportTitle.getText()))) {
                    lblCreateImportTitle.setText(lm.getString("import.receipt.title.new"));
                } else if (selectedImportReceipt != null) {
                    lblCreateImportTitle.setText(MessageFormat.format(lm.getString("import.receipt.title.details"), selectedImportReceipt.getReceiptID()));
                }
            }
        }
        // ComboBoxes trong panel tạo phiếu nhập (prompt text)
        if (cmbImportSupplier != null) cmbImportSupplier.setPromptText(lm.getString("prompt.selectSupplier"));
        if (cmbImportEmployee != null) cmbImportEmployee.setPromptText(lm.getString("prompt.selectEmployee"));
        if (cmbImportWarehouse != null) cmbImportWarehouse.setPromptText(lm.getString("prompt.selectWarehouse"));
        if (cmbImportProduct != null) cmbImportProduct.setPromptText(lm.getString("prompt.selectProduct"));
        // ... (các Label khác trong panel này đã dùng %key) ...
        if (btnAddImportDetail != null) btnAddImportDetail.setText(lm.getString("button.addProductShort"));
        if (btnSaveImportReceipt != null) btnSaveImportReceipt.setText(lm.getString("button.saveImportReceipt"));
        if (btnCancelImportReceipt != null) btnCancelImportReceipt.setText(lm.getString("button.cancelCreateReceipt")); // Key có thể là "button.cancel" chung

        // Header cột cho bảng chi tiết phiếu nhập
        if (colDetailProductName!=null) colDetailProductName.setText(lm.getString("table.col.productName"));
        // ... (các cột khác của importReceiptDetailTableView) ...


        // Tải lại dữ liệu cho các bảng và ComboBox (vì StringConverter có thể phụ thuộc vào Locale)
        loadSuppliersToTable();
        loadDataForImportTabComboBoxes(); // Load lại NCC, NV, Kho, SP vào các ComboBox của form nhập
        loadImportReceiptsToTable(); // Tải lại danh sách phiếu nhập

        // Clear form để áp dụng prompt text mới nếu có
        if (selectedSupplier == null && txtSupplierID != null && txtSupplierID.getText().isEmpty()) { // Chỉ clear nếu đang ở trạng thái thêm mới NCC
            clearSupplierForm();
        }
        if (createImportReceiptAnchorPane != null && createImportReceiptAnchorPane.isVisible() &&
                (selectedImportReceipt == null && "Tạo Phiếu Nhập Mới".equals(lblCreateImportTitle.getText()) || (messages != null && lm.getString("import.receipt.title.new").equals(lblCreateImportTitle.getText())) ) ) {
            clearImportReceiptForm();
        }
    }

    private void setupImportReceiptTableSelectionListener() {
        if (importReceiptTableView == null) return; // Kiểm tra null an toàn

        importReceiptTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedImportReceipt = newSelection; // Lưu lại phiếu nhập đang được chọn

                System.out.println("Đã chọn phiếu nhập ID: " + selectedImportReceipt.getReceiptID());

            } else {
                selectedImportReceipt = null;
                if (createImportReceiptAnchorPane != null && createImportReceiptAnchorPane.isVisible() &&
                        lblCreateImportTitle != null && !lblCreateImportTitle.getText().equals("Tạo Phiếu Nhập Mới")) {
                }
            }
        });
    }


    // ======================== TAB QUẢN LÝ NHÀ CUNG CẤP (Giữ nguyên từ trước) ========================
    private void setupSupplierTableColumns() { /* ... */ colSupplierID.setCellValueFactory(new PropertyValueFactory<>("supplierID")); colSupplierName.setCellValueFactory(new PropertyValueFactory<>("supplierName")); colSupplierEmail.setCellValueFactory(new PropertyValueFactory<>("email")); colSupplierPhone.setCellValueFactory(new PropertyValueFactory<>("phone")); colSupplierAddress.setCellValueFactory(new PropertyValueFactory<>("address")); colSupplierTaxCode.setCellValueFactory(new PropertyValueFactory<>("taxCode"));}
    private void loadSuppliersToTable() { /* ... */ try { List<Supplier> suppliers = supplierService.getAllSuppliers(); supplierList.setAll(suppliers); supplierTableView.setItems(supplierList); supplierTableView.setPlaceholder(new Label(suppliers.isEmpty() ? "Chưa có NCC." : null)); } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tải được DS NCC."); e.printStackTrace();}}
    private void setupSupplierTableSelectionListener() { /* ... */ supplierTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldS, newS) -> { if (newS != null) { selectedSupplier = newS; populateSupplierForm(selectedSupplier); btnUpdateSupplier.setDisable(false); btnDeleteSupplier.setDisable(false); btnAddSupplier.setDisable(true); txtSupplierID.setEditable(false); }});}
    private void populateSupplierForm(Supplier s) { /* ... */ txtSupplierID.setText(String.valueOf(s.getSupplierID())); txtSupplierName.setText(s.getSupplierName()); txtSupplierEmail.setText(s.getEmail()); txtSupplierPhone.setText(s.getPhone()); txtSupplierAddress.setText(s.getAddress()); txtSupplierTaxCode.setText(s.getTaxCode()); lblSupplierStatus.setText("");}
    @FXML
    void handleAddSupplierAction(ActionEvent event) {
        try {
            Supplier newSup = getSupplierFromForm();
            Supplier savedSup = supplierService.addSupplier(newSup);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm NCC ID: " + savedSup.getSupplierID());
            loadSuppliersToTable(); // Tải lại bảng NCC
            clearSupplierForm();
            loadDataForImportTabComboBoxes(); // << THÊM DÒNG NÀY: Cập nhật ComboBoxes ở tab phiếu nhập
        } catch (IllegalArgumentException e) {
            lblSupplierStatus.setText("Lỗi: " + e.getMessage());
            lblSupplierStatus.setTextFill(javafx.scene.paint.Color.RED);
        } catch (Exception e) {
            lblSupplierStatus.setText("Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleUpdateSupplierAction(ActionEvent event) {
        if (selectedSupplier == null) { /* ... */ return; }
        try {
            Supplier updatedData = getSupplierFromForm();
            updatedData.setSupplierID(selectedSupplier.getSupplierID());
            if (supplierService.updateSupplier(updatedData)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật NCC thành công.");
                loadSuppliersToTable();
                clearSupplierForm();
                loadDataForImportTabComboBoxes(); // << THÊM DÒNG NÀY
            } else { /* ... */ }
        } catch (Exception e) { /* ... */ }
    }
    @FXML
    void handleDeleteSupplierAction(ActionEvent event) {
        if (selectedSupplier == null) { /* ... */ return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, /* ... */ String.valueOf(ButtonType.YES), ButtonType.NO);
        /* ... */
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try {
                    if (supplierService.deleteSupplier(selectedSupplier.getSupplierID())) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa NCC.");
                        loadSuppliersToTable();
                        clearSupplierForm();
                        loadDataForImportTabComboBoxes(); // << THÊM DÒNG NÀY
                    }
                } catch (Exception e) { /* ... */ }
            }
        });
    }
    @FXML void handleClearSupplierFormAction(ActionEvent event) { clearSupplierForm(); }
    private void clearSupplierForm() { /* ... */ if(supplierTableView!=null)supplierTableView.getSelectionModel().clearSelection(); selectedSupplier=null; if(txtSupplierID!=null)txtSupplierID.clear(); if(txtSupplierName!=null)txtSupplierName.clear(); if(txtSupplierEmail!=null)txtSupplierEmail.clear(); if(txtSupplierPhone!=null)txtSupplierPhone.clear(); if(txtSupplierAddress!=null)txtSupplierAddress.clear(); if(txtSupplierTaxCode!=null)txtSupplierTaxCode.clear(); if(lblSupplierStatus!=null)lblSupplierStatus.setText(""); if(btnAddSupplier!=null)btnAddSupplier.setDisable(false); if(btnUpdateSupplier!=null)btnUpdateSupplier.setDisable(true); if(btnDeleteSupplier!=null)btnDeleteSupplier.setDisable(true); if(txtSupplierID!=null)txtSupplierID.setEditable(false);}
    private Supplier getSupplierFromForm() throws IllegalArgumentException { /* ... */ Supplier s=new Supplier(); if(txtSupplierName.getText().trim().isEmpty())throw new IllegalArgumentException("Tên NCC ko trống"); s.setSupplierName(txtSupplierName.getText().trim()); if(txtSupplierEmail.getText().trim().isEmpty())throw new IllegalArgumentException("Email ko trống"); s.setEmail(txtSupplierEmail.getText().trim()); if(txtSupplierPhone.getText().trim().isEmpty())throw new IllegalArgumentException("SĐT ko trống"); s.setPhone(txtSupplierPhone.getText().trim()); s.setAddress(txtSupplierAddress.getText()!=null?txtSupplierAddress.getText().trim():null); s.setTaxCode(txtSupplierTaxCode.getText()!=null?txtSupplierTaxCode.getText().trim():null); return s;}
    @FXML void handleSearchSupplierAction(ActionEvent event) { /* ... */ String kw=txtSearchSupplier.getText().trim(); try { List<Supplier> res=supplierService.searchSuppliers(kw); supplierList.setAll(res); supplierTableView.setItems(supplierList); supplierTableView.setPlaceholder(new Label(res.isEmpty()?"Ko tìm thấy.":null));} catch (Exception e){showAlert(Alert.AlertType.ERROR,"Lỗi","Lỗi tìm NCC: "+e.getMessage());}}
    @FXML void handleRefreshSupplierTableAction(ActionEvent event) {txtSearchSupplier.clear(); loadSuppliersToTable(); clearSupplierForm();}


    // ======================== TAB QUẢN LÝ PHIẾU NHẬP HÀNG ========================
    private void setupImportReceiptTableColumns() {
        if (colReceiptID == null || colReceiptImportDate == null || colReceiptSupplierName == null ||
                colReceiptEmployeeName == null || colReceiptWarehouseName == null ||
                colReceiptTotalAmount == null || colReceiptNote == null || colReceiptViewDetails == null) {
            System.err.println("LỖI: Một hoặc nhiều TableColumn cho importReceiptTableView chưa được inject (null).");
            return; // Thoát sớm nếu có lỗi inject FXML
        }

        // Cột Mã Phiếu
        colReceiptID.setCellValueFactory(new PropertyValueFactory<>("receiptID"));

        // Cột Ngày Nhập (Định dạng LocalDateTime)
        colReceiptImportDate.setCellValueFactory(new PropertyValueFactory<>("importDate")); // Cung cấp LocalDateTime
        colReceiptImportDate.setCellFactory(column -> {
            return new TableCell<ImportReceipt, LocalDateTime>() { // Cell xử lý LocalDateTime
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

        // Cột Tên Nhà Cung Cấp
        colReceiptSupplierName.setCellValueFactory(new PropertyValueFactory<>("supplierNameDisplay"));

        // Cột Tên Nhân Viên Nhập
        colReceiptEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeNameDisplay"));

        // Cột Tên Kho Nhập
        colReceiptWarehouseName.setCellValueFactory(new PropertyValueFactory<>("warehouseNameDisplay"));

        // Cột Tổng Tiền (Định dạng tiền tệ)
        colReceiptTotalAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colReceiptTotalAmount.setCellFactory(tc -> new TableCell<ImportReceipt, Double>() { // << SỬA KIỂU
            @Override
            protected void updateItem(Double item, boolean empty) { // << SỬA KIỂU
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormatter.format(item));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });

        // Cột Ghi Chú
        colReceiptNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        // Cột Xem Chi Tiết (Hoặc Hành động nói chung)
        Callback<TableColumn<ImportReceipt, Void>, TableCell<ImportReceipt, Void>> cellFactoryViewDetails = param -> {
            final TableCell<ImportReceipt, Void> cell = new TableCell<>() {
                private final Button btnView = new Button("Xem"); // Hoặc text từ ResourceBundle: lm.getString("button.viewDetails")
                {
                    // Style nút "Xem"
                    btnView.setStyle("-fx-background-color: #4299E1; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 7 3 7;");
                    btnView.setOnAction(event -> {
                        ImportReceipt receipt = getTableView().getItems().get(getIndex());
                        if (receipt != null) {
                            handleViewImportReceiptDetails(receipt); // Gọi hàm xử lý xem chi tiết
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(btnView);
                        setAlignment(Pos.CENTER); // Căn giữa nút trong cell
                    }
                }
            };
            return cell;
        };
        colReceiptViewDetails.setCellFactory(cellFactoryViewDetails);
    }

    private void handleDeleteSingleImportReceiptAction(ImportReceipt receiptToDelete) {
        if (receiptToDelete == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận xóa Phiếu Nhập");
        confirmation.setHeaderText("Xóa Phiếu Nhập ID: " + receiptToDelete.getReceiptID() +
                " (NCC: " + receiptToDelete.getSupplierNameDisplay() +
                ", Ngày: " + receiptToDelete.getImportDate().format(dateFormatter) + ")?");
        confirmation.setContentText("Hành động này sẽ xóa phiếu nhập và cố gắng cập nhật lại số lượng tồn kho. " +
                "Bạn có chắc chắn muốn tiếp tục?");
        confirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                boolean success = importReceiptService.deleteSingleImportReceipt(receiptToDelete.getReceiptID());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa phiếu nhập ID: " + receiptToDelete.getReceiptID() + " và cập nhật tồn kho.");
                    loadImportReceiptsToTable(); // Tải lại bảng phiếu nhập
                    // Nếu panel tạo/xem chi tiết đang hiển thị phiếu này, hãy ẩn nó đi
                    if (createImportReceiptAnchorPane.isVisible() && selectedImportReceipt != null && selectedImportReceipt.getReceiptID() == receiptToDelete.getReceiptID()) {
                        handleCancelImportReceiptAction(null);
                    }
                } else {
                    // Service nên ném lỗi nếu thất bại
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa phiếu nhập ID: " + receiptToDelete.getReceiptID() + ".");
                }
            } catch (RuntimeException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Xóa Phiếu", e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Lỗi khi xóa phiếu nhập: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void setupImportReceiptDetailTableColumns() {
        colDetailProductName.setCellValueFactory(new PropertyValueFactory<>("productNameDisplay"));
        colDetailQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colDetailUnitCost.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
        colDetailUnitCost.setCellFactory(tc -> new TableCell<ImportReceiptDetail, Double>() { // << SỬA KIỂU
            @Override
            protected void updateItem(Double item, boolean empty) { // << SỬA KIỂU
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormatter.format(item));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
        colDetailSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal")); // Dùng getter Subtotal từ model
        colDetailSubtotal.setCellFactory(tc -> new TableCell<ImportReceiptDetail, Double>() { // << SỬA KIỂU
            @Override
            protected void updateItem(Double item, boolean empty) { // << SỬA KIỂU
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormatter.format(item));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });

        // Cột xóa chi tiết khỏi phiếu đang tạo
        Callback<TableColumn<ImportReceiptDetail, Void>, TableCell<ImportReceiptDetail, Void>> cellFactoryRemove = param -> {
            return new TableCell<>() {
                private final Button btnRemove = new Button("Xóa");
                {
                    btnRemove.setStyle("-fx-background-color: #F56565; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 5 3 5;");
                    btnRemove.setOnAction(event -> {
                        ImportReceiptDetail detail = getTableView().getItems().get(getIndex());
                        currentImportDetails.remove(detail);
                        calculateAndDisplayImportTotalAmount(); // Cập nhật lại tổng tiền
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btnRemove);
                }
                };
            };
        }


    private void loadImportReceiptsToTable() {
        try {
            // Lọc theo ngày nếu dpImportStartDate và dpImportEndDate có giá trị
            LocalDate startDate = dpImportStartDate.getValue();
            LocalDate endDate = dpImportEndDate.getValue();
            Supplier filterSupplier = cmbFilterImportBySupplier.getValue();

            List<ImportReceipt> receipts;
            if (startDate != null && endDate != null) {
                if (startDate.isAfter(endDate)) {
                    showAlert(Alert.AlertType.ERROR, "Ngày không hợp lệ", "Ngày bắt đầu không thể sau ngày kết thúc.");
                    return;
                }
                receipts = importReceiptService.getImportReceiptsByDateRangeWithDetails(startDate, endDate);
            } else {
                receipts = importReceiptService.getAllImportReceiptsWithDetails();
            }

            // Lọc thêm theo NCC nếu được chọn
            if (filterSupplier != null) {
                receipts = receipts.stream()
                        .filter(r -> r.getSupplierID() == filterSupplier.getSupplierID())
                        .collect(Collectors.toList());
            }

            importReceiptList.setAll(receipts);
            importReceiptTableView.setItems(importReceiptList);
            importReceiptTableView.setPlaceholder(new Label(receipts.isEmpty() ? "Không có phiếu nhập nào khớp." : null));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Phiếu Nhập", "Không thể tải danh sách phiếu nhập.");
            e.printStackTrace();
        }
    }

    private void loadDataForImportTabComboBoxes() { /* Giữ nguyên từ trước, chỉ cần đảm bảo nó được gọi đúng lúc */
        // Load Nhà cung cấp
        try { suppliersForComboBox.setAll(supplierService.getAllSuppliers()); cmbImportSupplier.setItems(suppliersForComboBox); cmbImportSupplier.setConverter(new StringConverter<Supplier>() { @Override public String toString(Supplier s) { return s == null ? null : s.getSupplierName(); } @Override public Supplier fromString(String s) { return null; } }); ObservableList<Supplier> filterSuppliers = FXCollections.observableArrayList(); filterSuppliers.add(null); filterSuppliers.addAll(suppliersForComboBox); cmbFilterImportBySupplier.setItems(filterSuppliers); cmbFilterImportBySupplier.setConverter(new StringConverter<Supplier>() { @Override public String toString(Supplier s) { return s == null ? "Tất cả NCC" : s.getSupplierName(); } @Override public Supplier fromString(String s) { return null; } }); if(!filterSuppliers.isEmpty()) cmbFilterImportBySupplier.getSelectionModel().selectFirst(); } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Lỗi", "K tải được DS NCC.");}
        // Load Nhân viên
        try { List<Employee> activeEmp = employeeService.getAllEmployees().stream().filter(e -> "Đang làm".equalsIgnoreCase(e.getStatus())).collect(Collectors.toList()); employeesForComboBox.setAll(activeEmp); cmbImportEmployee.setItems(employeesForComboBox); cmbImportEmployee.setConverter(new StringConverter<Employee>() { @Override public String toString(Employee e) { return e == null ? null : e.getFullName(); } @Override public Employee fromString(String s) { return null; } }); } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Lỗi", "K tải được DS NV.");}
        // Load Kho hàng
        try { warehousesForComboBox.setAll(warehouseService.getAllWarehousesWithManagerName()); cmbImportWarehouse.setItems(warehousesForComboBox); cmbImportWarehouse.setConverter(new StringConverter<Warehouse>() { @Override public String toString(Warehouse w) { return w == null ? null : w.getWarehouseName(); } @Override public Warehouse fromString(String s) { return null; } }); } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Lỗi", "K tải được DS Kho.");}
        // Load Sản phẩm
        try { productsForComboBox.setAll(productService.getAllProductsWithCategoryName()); cmbImportProduct.setItems(productsForComboBox); cmbImportProduct.setConverter(new StringConverter<Product>() { @Override public String toString(Product p) { return p == null ? null : p.getProductName() + " (ID: "+p.getProductID()+")"; } @Override public Product fromString(String s) { return null; } }); } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Lỗi", "K tải được DS SP.");}
    }

    @FXML
    void handleShowCreateImportReceiptPaneAction(ActionEvent event) {
        createImportReceiptAnchorPane.setVisible(true);
        createImportReceiptAnchorPane.setManaged(true);
        lblCreateImportTitle.setText("Tạo Phiếu Nhập Mới");
        clearImportReceiptForm(); // Reset form
        // Không cần gọi loadDataForImportTabComboBoxes() ở đây nữa nếu đã gọi trong initialize()
    }

    @FXML
    void handleCancelImportReceiptAction(ActionEvent event) {
        createImportReceiptAnchorPane.setVisible(false);
        createImportReceiptAnchorPane.setManaged(false);
        currentImportDetails.clear();
    }

    private void clearImportReceiptForm() {
        if (cmbImportSupplier != null) cmbImportSupplier.getSelectionModel().clearSelection();
        if (cmbImportEmployee != null) cmbImportEmployee.getSelectionModel().clearSelection();
        if (cmbImportWarehouse != null) cmbImportWarehouse.getSelectionModel().clearSelection();
        if (dpImportDate != null) dpImportDate.setValue(LocalDate.now());
        if (txtImportReceiptNote != null) txtImportReceiptNote.clear();
        clearImportDetailForm();
        currentImportDetails.clear();
        if (lblImportTotalAmount != null) lblImportTotalAmount.setText(currencyFormatter.format(0));
    }

    private void clearImportDetailForm() {
        cmbImportProduct.getSelectionModel().clearSelection();
        if (spinnerImportQuantity.getValueFactory() != null) { // Kiểm tra null trước khi set value
            spinnerImportQuantity.getValueFactory().setValue(1); // Reset spinner về 1
        } else { // Nếu chưa có ValueFactory, tạo mới
            SpinnerValueFactory<Integer> quantityFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1);
            spinnerImportQuantity.setValueFactory(quantityFactory);
        }
        txtImportUnitCost.clear();
        cmbImportProduct.requestFocus(); // Focus vào ComboBox sản phẩm
    }

    @FXML
    void handleAddImportDetailAction(ActionEvent event) {
        Product selectedProd = cmbImportProduct.getValue();
        Integer quantity = spinnerImportQuantity.getValue(); // Lấy giá trị từ Spinner
        String unitCostStr = txtImportUnitCost.getText().trim();

        // --- VALIDATION ĐẦU VÀO CHO CHI TIẾT ---
        if (selectedProd == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn một sản phẩm.");
            return;
        }
        if (quantity == null || quantity <= 0) {
            // Spinner đã có min=1, nhưng vẫn nên kiểm tra
            showAlert(Alert.AlertType.WARNING, "Số lượng không hợp lệ", "Số lượng nhập phải lớn hơn 0.");
            return;
        }
        if (unitCostStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đơn giá nhập cho sản phẩm.");
            return;
        }

        double unitCost;
        try {
            String parsableCost = unitCostStr.replace(".", "").replace(",", "."); // Xử lý cả dấu '.' và ','
            unitCost = Double.parseDouble(parsableCost); // Parse sang double
            if (unitCost < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Đơn giá không hợp lệ", "Vui lòng nhập một số dương hợp lệ cho đơn giá nhập.");
            return;
        }
        // --- KẾT THÚC VALIDATION ---

        // Kiểm tra xem sản phẩm đã có trong danh sách chi tiết hiện tại chưa
        // Nếu có, có thể hỏi người dùng muốn cập nhật số lượng hay không, hoặc báo lỗi.
        // Hiện tại, chúng ta sẽ báo lỗi nếu đã tồn tại để giữ đơn giản.
        for (ImportReceiptDetail existingDetail : currentImportDetails) {
            if (existingDetail.getProductID().equals(selectedProd.getProductID())) {
                showAlert(Alert.AlertType.WARNING, "Sản phẩm đã tồn tại",
                        "Sản phẩm '" + selectedProd.getProductName() + "' đã có trong danh sách chi tiết của phiếu nhập này.\n" +
                                "Bạn có thể xóa sản phẩm cũ và thêm lại với số lượng mới nếu muốn thay đổi.");
                return;
            }
        }

        ImportReceiptDetail newDetail = new ImportReceiptDetail();
        newDetail.setProductID(selectedProd.getProductID());
        newDetail.setProductNameDisplay(selectedProd.getProductName()); // Để hiển thị ngay trên bảng tạm
        newDetail.setQuantity(quantity);
        newDetail.setUnitCost(unitCost);
        // newDetail.setReceiptID() sẽ được gán khi lưu phiếu nhập chính

        currentImportDetails.add(newDetail);
        // importReceiptDetailTableView.setItems(currentImportDetails); // Không cần nếu currentImportDetails là list của table
        // importReceiptDetailTableView.refresh(); // Có thể cần để refresh nếu dùng setItems riêng

        calculateAndDisplayImportTotalAmount(); // Tính lại tổng tiền
        clearImportDetailForm(); // Xóa các trường nhập chi tiết để chuẩn bị cho sản phẩm tiếp theo
    }




    private void calculateAndDisplayImportTotalAmount() {
        double total = 0.0; // << ĐỔI THÀNH double
        for (ImportReceiptDetail detail : currentImportDetails) {
            total += detail.getSubtotal(); // getSubtotal() trả về double
        }
        lblImportTotalAmount.setText(currencyFormatter.format(total));
    }


    @FXML
    void handleSaveImportReceiptAction(ActionEvent event) {
        Supplier supplier = cmbImportSupplier.getValue();
        Employee employee = cmbImportEmployee.getValue();
        Warehouse warehouse = cmbImportWarehouse.getValue();
        LocalDate importDateLD = dpImportDate.getValue();
        String note = txtImportReceiptNote.getText().trim();

        // --- VALIDATION ĐẦU VÀO CHO PHIẾU NHẬP (GIỮ NGUYÊN) ---
        if (supplier == null) { showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn Nhà cung cấp."); return; }
        if (employee == null) { showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn Nhân viên nhập hàng."); return; }
        if (warehouse == null) { showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn Kho nhập hàng."); return; }
        if (importDateLD == null) { showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn Ngày nhập hàng."); return; }
        if (currentImportDetails.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Phiếu nhập phải có ít nhất một sản phẩm chi tiết."); return; }

        ImportReceipt newReceipt = new ImportReceipt();
        newReceipt.setSupplierID(supplier.getSupplierID());
        newReceipt.setEmployeeID(employee.getEmployeeID());
        newReceipt.setWarehouseID(warehouse.getWarehouseID());
        newReceipt.setImportDate(importDateLD.atStartOfDay()); // Hoặc LocalDateTime.now()
        newReceipt.setNote(note);
        newReceipt.setDetails(new ArrayList<>(currentImportDetails)); // Quan trọng: tạo bản copy

        // Tính tổng tiền (Service cũng sẽ tính lại)
        double totalAmount = 0.0; // << ĐỔI THÀNH double
        for (ImportReceiptDetail detail : newReceipt.getDetails()) {
            totalAmount += detail.getSubtotal();
        }

        try {
            ImportReceipt savedReceiptFromService = importReceiptService.createImportReceipt(newReceipt);
            // savedReceiptFromService có thể chưa đủ thông tin display names

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã tạo và lưu Phiếu Nhập Hàng ID: " + savedReceiptFromService.getReceiptID() + " thành công!");
            loadImportReceiptsToTable();
            handleCancelImportReceiptAction(null);

            // --- HỎI XUẤT EXCEL ---
            Alert exportConfirmation = new Alert(Alert.AlertType.CONFIRMATION);
            exportConfirmation.setTitle("Xác nhận xuất Excel");
            exportConfirmation.setHeaderText("Xuất Phiếu Nhập vừa tạo ra Excel?");
            exportConfirmation.setContentText("Bạn có muốn xuất thông tin chi tiết của Phiếu Nhập ID: " + savedReceiptFromService.getReceiptID() + " ra file Excel không?");
            exportConfirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            Optional<ButtonType> result = exportConfirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                // Lấy lại thông tin phiếu nhập đầy đủ từ service để đảm bảo có tất cả details và display names
                Optional<ImportReceipt> fullReceiptOpt = importReceiptService.getImportReceiptById(savedReceiptFromService.getReceiptID());

                if (fullReceiptOpt.isPresent()) {
                    ImportReceipt receiptToExport = fullReceiptOpt.get();
                    // Bây giờ receiptToExport sẽ có đầy đủ thông tin cần thiết

                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Lưu file Excel Phiếu Nhập");
                    fileChooser.setInitialFileName("PhieuNhap_" + receiptToExport.getReceiptID() + ".xlsx");
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"));
                    Stage stage = (Stage) mainTabPaneSupplierImport.getScene().getWindow();
                    File file = fileChooser.showSaveDialog(stage);
                    if (file != null) {
                        exportImportReceiptToExcel(receiptToExport, file); // Gọi hàm xuất Excel
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải lại chi tiết phiếu nhập ID: " + savedReceiptFromService.getReceiptID() + " để xuất Excel.");
                }
            }
        } catch (RuntimeException e) { // Bắt lỗi từ Service
            showAlert(Alert.AlertType.ERROR, "Lỗi Lưu Phiếu Nhập", e.getMessage());
            // In ra console để debug thêm nếu Alert không đủ chi tiết
            System.err.println("Lỗi được bắt ở Controller khi lưu phiếu nhập: " + e.getMessage());
            e.printStackTrace(System.err);
        }
        // Bỏ khối catch (Exception e) riêng nếu không cần thiết, RuntimeException đã bao gồm nhiều lỗi
    }

    @FXML
    void handleFilterImportReceiptsAction(ActionEvent event) {
        loadImportReceiptsToTable(); // Hàm này đã có logic lọc theo ngày và NCC (nếu được chọn)
    }

    @FXML
    void handleRefreshImportReceiptTableAction(ActionEvent event) {
        dpImportStartDate.setValue(LocalDate.now().minusMonths(1));
        dpImportEndDate.setValue(LocalDate.now());
        if(cmbFilterImportBySupplier != null && !cmbFilterImportBySupplier.getItems().isEmpty()){
            cmbFilterImportBySupplier.getSelectionModel().selectFirst(); // Chọn "Tất cả NCC"
        }
        loadImportReceiptsToTable();
        showAlert(Alert.AlertType.INFORMATION, "Làm mới", "Đã làm mới danh sách phiếu nhập.");
    }

    private void handleViewImportReceiptDetails(ImportReceipt receipt) {
        if (receipt == null) return;
        createImportReceiptAnchorPane.setVisible(true);
        createImportReceiptAnchorPane.setManaged(true);
        lblCreateImportTitle.setText("Chi Tiết Phiếu Nhập ID: " + receipt.getReceiptID());

        // Điền thông tin phiếu nhập lên form (đặt các control ở chế độ chỉ đọc nếu cần)
        // Hiện tại chỉ hiển thị, không cho sửa phiếu đã lưu từ giao diện này
        Optional<Supplier> supOpt = suppliersForComboBox.stream().filter(s -> s.getSupplierID() == receipt.getSupplierID()).findFirst();
        supOpt.ifPresent(cmbImportSupplier::setValue);
        cmbImportSupplier.setDisable(true);

        Optional<Employee> empOpt = employeesForComboBox.stream().filter(e -> e.getEmployeeID() == receipt.getEmployeeID()).findFirst();
        empOpt.ifPresent(cmbImportEmployee::setValue);
        cmbImportEmployee.setDisable(true);

        Optional<Warehouse> whOpt = warehousesForComboBox.stream().filter(w -> w.getWarehouseID() == receipt.getWarehouseID()).findFirst();
        whOpt.ifPresent(cmbImportWarehouse::setValue);
        cmbImportWarehouse.setDisable(true);

        dpImportDate.setValue(receipt.getImportDate().toLocalDate());
        dpImportDate.setDisable(true);
        txtImportReceiptNote.setText(receipt.getNote());
        txtImportReceiptNote.setEditable(false);

        // Hiển thị chi tiết sản phẩm
        currentImportDetails.setAll(receipt.getDetails()); // Service đã load details khi getImportReceiptById
        importReceiptDetailTableView.setItems(currentImportDetails);
        calculateAndDisplayImportTotalAmount();

        // Ẩn/Vô hiệu hóa các nút không cần thiết khi xem
        btnAddImportDetail.setVisible(false);
        btnSaveImportReceipt.setVisible(false); // Không cho lưu lại
        // btnCancelImportReceipt vẫn dùng để đóng panel
    }


    // Tiện ích Alert
    private void showAlert(Alert.AlertType alertType, String titleKey, String messageKey, Object... params) {
        LanguageForManager lm = LanguageForManager.getInstance();
        Alert alert = new Alert(alertType);
        alert.setTitle(lm.getString(titleKey)); // Lấy title từ resource bundle
        alert.setHeaderText(null);
        String message = lm.getString(messageKey); // Lấy message từ resource bundle
        if (params != null && params.length > 0 && !(params.length == 1 && params[0] == null) ) {
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