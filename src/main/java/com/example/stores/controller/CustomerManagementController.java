package com.example.stores.controller;

import com.example.stores.model.Customer;
import com.example.stores.repository.CustomerRepository; // Cần để khởi tạo service
import com.example.stores.repository.impl.CustomerRepositoryImpl; // Cần để khởi tạo service
import com.example.stores.service.CustomerService;
import com.example.stores.service.impl.CustomerServiceImpl;

import com.example.stores.util.LanguageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CustomerManagementController {

    //<editor-fold desc="FXML Controls - Lọc và Bảng">
    @FXML private TextField txtSearchCustomer;
    @FXML private Button btnSearchCustomer;
    @FXML private ComboBox<String> cmbFilterCustomerStatus; // "Tất cả", "Hoạt động", "Không hoạt động"
    @FXML private Button btnRefreshCustomerTable;
    @FXML private TableView<Customer> customerTableView;
    @FXML private TableColumn<Customer, Integer> colCustomerID;
    @FXML private TableColumn<Customer, String> colCustomerFullName;
    @FXML private TableColumn<Customer, String> colCustomerUsername;
    @FXML private TableColumn<Customer, String> colCustomerEmail;
    @FXML private TableColumn<Customer, String> colCustomerPhone;
    @FXML private TableColumn<Customer, String> colCustomerGender;
    @FXML private TableColumn<Customer, LocalDate> colCustomerBirthDate;
    @FXML private TableColumn<Customer, Boolean> colCustomerIsActive; // Hiển thị True/False hoặc Có/Không
    //</editor-fold>

    //<editor-fold desc="FXML Controls - Form Chi Tiết">
    @FXML private TextField txtCustomerID;
    @FXML private TextField txtCustomerUsername;
    @FXML private TextField txtCustomerFullName;
    @FXML private TextField txtCustomerEmail;
    @FXML private TextField txtCustomerPhone;
    @FXML private RadioButton rbCustomerMale;
    @FXML private RadioButton rbCustomerFemale;
    @FXML private RadioButton rbCustomerOther;
    @FXML private ToggleGroup customerGenderGroup;
    @FXML private DatePicker dpCustomerBirthDate;
    @FXML private TextArea txtCustomerAddress;
    @FXML private CheckBox chkCustomerIsActive;
    @FXML private Button btnUpdateCustomer;
    @FXML private Button btnClearCustomerForm;
    @FXML private Label lblCustomerStatus; // Để hiển thị thông báo
    //</editor-fold>

    private CustomerService customerService;
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private Customer selectedCustomer;

    public CustomerManagementController() {
        CustomerRepository customerRepository = new CustomerRepositoryImpl();
        this.customerService = new CustomerServiceImpl(customerRepository);
    }

    @FXML
    public void initialize() {
        setupCustomerTableColumns();
        setupFilterComboBox();
        loadCustomersToTable();
        setupCustomerTableSelectionListener();
        clearCustomerForm(); // Đặt form về trạng thái ban đầu
        btnUpdateCustomer.setDisable(true); // Ban đầu không có KH nào được chọn để sửa
        LanguageManager.getInstance().currentLocaleProperty().addListener((obs, oldL, newL) -> updateUITexts());
        updateUITexts();
    }

    private void updateUITexts() {
        LanguageManager lm = LanguageManager.getInstance();

        // Cập nhật bộ lọc
        if (txtSearchCustomer != null) txtSearchCustomer.setPromptText(lm.getString("search.prompt.customer"));
        if (btnSearchCustomer != null) btnSearchCustomer.setText(lm.getString("button.search"));
        if (cmbFilterCustomerStatus != null) cmbFilterCustomerStatus.setPromptText(lm.getString("prompt.all"));
        if (btnRefreshCustomerTable != null) btnRefreshCustomerTable.setText(lm.getString("button.refreshTable"));

        // Cập nhật header cột TableView (nếu không dùng %key hoặc để chắc chắn)
        if (colCustomerID != null) colCustomerID.setText(lm.getString("table.col.customerId"));
        // ... các cột khác của customerTableView ...
        if (colCustomerIsActive != null) colCustomerIsActive.setText(lm.getString("table.col.status")); // Dùng chung key
        if (customerTableView != null) customerTableView.setPlaceholder(new Label(lm.getString("table.placeholder.noCustomers")));

        // Cập nhật Form chi tiết
        // (Label tiêu đề và các label tĩnh khác đã dùng %key trong FXML)
        if (txtCustomerFullName != null) txtCustomerFullName.setPromptText(lm.getString("prompt.fullName"));
        // ... các prompt text khác ...
        if (btnUpdateCustomer != null) btnUpdateCustomer.setText(lm.getString("button.updateInfo"));
        if (btnClearCustomerForm != null) btnClearCustomerForm.setText(lm.getString("button.clearForm"));

        // Cập nhật items cho ComboBox lọc trạng thái
        if (cmbFilterCustomerStatus != null) {
            ObservableList<String> statusOptions = FXCollections.observableArrayList(
                    lm.getString("prompt.all"), // "Tất cả"
                    lm.getString("status.active"),
                    lm.getString("status.inactive")
            );
            String currentFilterStatus = cmbFilterCustomerStatus.getValue();
            cmbFilterCustomerStatus.setItems(statusOptions);
            if (currentFilterStatus != null && statusOptions.contains(currentFilterStatus)) {
                cmbFilterCustomerStatus.setValue(currentFilterStatus);
            } else {
                cmbFilterCustomerStatus.getSelectionModel().selectFirst();
            }
        }

        // Tải lại dữ liệu bảng để áp dụng text mới cho cột (nếu cell factory có logic ngôn ngữ)
        // và để placeholder được cập nhật
        loadCustomersToTable();

        // Nếu có khách hàng đang được chọn, cập nhật lại form chi tiết
        if (selectedCustomer != null) {
            populateCustomerForm(selectedCustomer);
        } else {
            clearCustomerForm();
        }
    }

    private void setupCustomerTableColumns() {
        colCustomerID.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        colCustomerFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colCustomerUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colCustomerEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCustomerPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colCustomerGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colCustomerBirthDate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        // Định dạng cột isActive để hiển thị "Hoạt động" / "Không hoạt động"
        colCustomerIsActive.setCellValueFactory(new PropertyValueFactory<>("active")); // Chú ý: getter là isActive()
        colCustomerIsActive.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? "Hoạt động" : "Không HĐ"));
            }
        });
    }

    private void setupFilterComboBox() {
        ObservableList<String> statusOptions = FXCollections.observableArrayList("Tất cả", "Hoạt động", "Không hoạt động");
        cmbFilterCustomerStatus.setItems(statusOptions);
        cmbFilterCustomerStatus.getSelectionModel().select("Tất cả"); // Mặc định

        // Listener để lọc khi thay đổi lựa chọn
        cmbFilterCustomerStatus.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            handleSearchCustomerAction(null); // Gọi lại hàm tìm kiếm/lọc
        });
    }

    private void loadCustomersToTable() {
        String keyword = txtSearchCustomer.getText().trim();
        String statusFilterStr = cmbFilterCustomerStatus.getValue();
        Boolean isActiveFilter = null;
        if ("Hoạt động".equals(statusFilterStr)) {
            isActiveFilter = true;
        } else if ("Không hoạt động".equals(statusFilterStr)) {
            isActiveFilter = false;
        }

        try {
            List<Customer> customers = customerService.searchCustomers(keyword, isActiveFilter);
            customerList.setAll(customers);
            customerTableView.setItems(customerList);
            customerTableView.setPlaceholder(new Label(customers.isEmpty() ? "Không tìm thấy khách hàng nào." : null));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Khách Hàng", "Không thể tải danh sách khách hàng.");
            e.printStackTrace();
        }
    }

    private void setupCustomerTableSelectionListener() {
        customerTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedCustomer = newSelection;
                populateCustomerForm(selectedCustomer);
                btnUpdateCustomer.setDisable(false);
            } else {
                clearCustomerForm(); // Nếu bỏ chọn, xóa form
                btnUpdateCustomer.setDisable(true);
            }
        });
    }

    private void populateCustomerForm(Customer customer) {
        txtCustomerID.setText(String.valueOf(customer.getCustomerID()));
        txtCustomerUsername.setText(customer.getUsername());
        txtCustomerFullName.setText(customer.getFullName());
        txtCustomerEmail.setText(customer.getEmail());
        txtCustomerPhone.setText(customer.getPhone());

        if (customer.getGender() != null) {
            String genderLower = customer.getGender().toLowerCase();
            if (genderLower.equals(rbCustomerMale.getText().toLowerCase())) rbCustomerMale.setSelected(true);
            else if (genderLower.equals(rbCustomerFemale.getText().toLowerCase())) rbCustomerFemale.setSelected(true);
            else if (genderLower.equals(rbCustomerOther.getText().toLowerCase())) rbCustomerOther.setSelected(true);
            else customerGenderGroup.selectToggle(null);
        } else {
            customerGenderGroup.selectToggle(null);
        }
        dpCustomerBirthDate.setValue(customer.getBirthDate());
        txtCustomerAddress.setText(customer.getAddress());
        chkCustomerIsActive.setSelected(customer.isActive());
        lblCustomerStatus.setText("");
    }

    @FXML
    void handleUpdateCustomerAction(ActionEvent event) {
        if (selectedCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn khách hàng", "Vui lòng chọn một khách hàng để cập nhật.");
            return;
        }
        try {
            Customer customerToUpdate = getCustomerFromForm();
            customerToUpdate.setCustomerID(selectedCustomer.getCustomerID()); // ID không đổi
            customerToUpdate.setUsername(selectedCustomer.getUsername()); // Username không cho sửa
            customerToUpdate.setPassword(selectedCustomer.getPassword()); // Password không cho sửa từ đây
            customerToUpdate.setRegisteredAt(selectedCustomer.getRegisteredAt()); // Ngày đăng ký không đổi

            boolean success = customerService.updateCustomerInfo(customerToUpdate);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật thông tin khách hàng thành công!");
                loadCustomersToTable(); // Tải lại bảng
                clearCustomerForm();
            } else {
                lblCustomerStatus.setText("Lỗi: Không thể cập nhật thông tin khách hàng.");
                lblCustomerStatus.setTextFill(javafx.scene.paint.Color.RED);
            }
        } catch (IllegalArgumentException e) {
            lblCustomerStatus.setText("Lỗi dữ liệu: " + e.getMessage());
            lblCustomerStatus.setTextFill(javafx.scene.paint.Color.RED);
        } catch (Exception e) {
            lblCustomerStatus.setText("Lỗi hệ thống: " + e.getMessage());
            lblCustomerStatus.setTextFill(javafx.scene.paint.Color.RED);
            e.printStackTrace();
        }
    }

    @FXML
    void handleClearCustomerFormAction(ActionEvent event) {
        clearCustomerForm();
        customerTableView.getSelectionModel().clearSelection(); // Bỏ chọn trên bảng
        btnUpdateCustomer.setDisable(true);
    }

    private void clearCustomerForm() {
        selectedCustomer = null;
        txtCustomerID.clear();
        txtCustomerUsername.clear();
        txtCustomerFullName.clear();
        txtCustomerEmail.clear();
        txtCustomerPhone.clear();
        if (customerGenderGroup != null) customerGenderGroup.selectToggle(null);
        if (dpCustomerBirthDate != null) dpCustomerBirthDate.setValue(null);
        if (txtCustomerAddress != null) txtCustomerAddress.clear();
        if (chkCustomerIsActive != null) chkCustomerIsActive.setSelected(false); // Hoặc true tùy mặc định bạn muốn
        if (lblCustomerStatus != null) lblCustomerStatus.setText("");
    }

    private Customer getCustomerFromForm() throws IllegalArgumentException {
        Customer customer = new Customer();
        // ID và Username sẽ được lấy từ selectedCustomer khi update
        if (txtCustomerFullName.getText().trim().isEmpty()) throw new IllegalArgumentException("Họ tên không được trống.");
        customer.setFullName(txtCustomerFullName.getText().trim());
        if (txtCustomerEmail.getText().trim().isEmpty()) throw new IllegalArgumentException("Email không được trống.");
        customer.setEmail(txtCustomerEmail.getText().trim());
        if (txtCustomerPhone.getText().trim().isEmpty()) throw new IllegalArgumentException("SĐT không được trống.");
        customer.setPhone(txtCustomerPhone.getText().trim());

        RadioButton selectedRadio = (RadioButton) customerGenderGroup.getSelectedToggle();
        customer.setGender(selectedRadio != null ? selectedRadio.getText() : null);
        customer.setBirthDate(dpCustomerBirthDate.getValue());
        customer.setAddress(txtCustomerAddress.getText() != null ? txtCustomerAddress.getText().trim() : null);
        customer.setActive(chkCustomerIsActive.isSelected());
        return customer;
    }

    @FXML
    void handleSearchCustomerAction(ActionEvent event) {
        loadCustomersToTable(); // Hàm này đã bao gồm logic lấy keyword và status filter
    }

    @FXML
    void handleRefreshCustomerTableAction(ActionEvent event) {
        txtSearchCustomer.clear();
        if (cmbFilterCustomerStatus.getItems() != null && !cmbFilterCustomerStatus.getItems().isEmpty()) {
            cmbFilterCustomerStatus.getSelectionModel().select("Tất cả");
        }
        loadCustomersToTable();
        clearCustomerForm();
        btnUpdateCustomer.setDisable(true);
    }

    private void showAlert(Alert.AlertType alertType, String titleKey, String messageKey, Object... params) {
        LanguageManager lm = LanguageManager.getInstance();
        Alert alert = new Alert(alertType);
        alert.setTitle(lm.getString(titleKey));
        alert.setHeaderText(null);
        String message = lm.getString(messageKey);
        if (params != null && params.length > 0 && !(params.length == 1 && params[0] == null)) {
            try {
                message = MessageFormat.format(message, params);
            } catch (IllegalArgumentException e) { /* ... */ }
        }
        alert.setContentText(message);
        alert.showAndWait();
    }
}