package com.example.stores.controller; // Đảm bảo package đúng

// Các import cần thiết

import com.example.stores.model.Employee;
import com.example.stores.repository.impl.EmployeeRepositoryImpl;
import com.example.stores.repository.impl.ShiftRepositoryImpl;
import com.example.stores.repository.impl.WorkShiftScheduleRepositoryImpl;
import com.example.stores.service.impl.EmployeeServiceImpl;
import com.example.stores.service.impl.WorkShiftScheduleServiceImpl;
import com.example.stores.util.LanguageForManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
// Bỏ các import ObservableList và FXCollections bị lặp lại

public class EmployeeManagementController {

    //<editor-fold desc="FXML Controls">
    @FXML private ImageView imgEmployeePhoto;
    @FXML private Button btnChangeEmployeeImage;
    @FXML private TextField txtEmployeeID;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private RadioButton rbMale;
    @FXML private RadioButton rbFemale;
    @FXML private RadioButton rbOther;
    @FXML private ToggleGroup genderGroup; // Đảm bảo fx:id này được gán cho ToggleGroup trong FXML
    @FXML private DatePicker dpBirthDate;
    @FXML private TextField txtPosition;
    @FXML private TextField txtSalary;
    @FXML private TextArea txtAddress;
    @FXML private ComboBox<String> cmbStatus; // Đã có 1 cmbStatus ở trên

    @FXML private Button btnAddEmployee;
    @FXML private Button btnUpdateEmployee;
    @FXML private Button btnDeleteEmployee;
    @FXML private Button btnClearForm;
    @FXML private Button btnExportExcel;

    @FXML private TextField txtSearchKeyword;
    @FXML private Button btnSearch;
    // @FXML private ComboBox<String> cmbStatus; // BỊ LẶP LẠI KHAI BÁO fx:id cho cmbStatus, XÓA DÒNG NÀY
    @FXML private ComboBox<String> cmbSortBy;
    @FXML private ComboBox<String> cmbSortOrder;
    @FXML private Button btnRefreshTable;

    @FXML private TableView<Employee> employeeTableView;
    @FXML private TableColumn<Employee, Integer> colEmployeeID;
    @FXML private TableColumn<Employee, String> colFullName;
    @FXML private TableColumn<Employee, String> colUsername;
    @FXML private TableColumn<Employee, String> colGender;
    @FXML private TableColumn<Employee, LocalDate> colBirthDate;
    @FXML private TableColumn<Employee, String> colPhone;
    @FXML private TableColumn<Employee, String> colEmail;
    @FXML private TableColumn<Employee, String> colPosition;
    @FXML private TableColumn<Employee, BigDecimal> colSalary;
    @FXML private TableColumn<Employee, String> colStatus;
    //</editor-fold>

    private EmployeeServiceImpl employeeService;
    private final ObservableList<Employee> employeeList = FXCollections.observableArrayList(); // Khởi tạo ngay
    private Employee selectedEmployee;
    private File selectedImageFile;
    private final DecimalFormat salaryTextFieldFormatter;
    private final NumberFormat currencyTableViewFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    // Đảm bảo đường dẫn này chính xác và file ảnh tồn tại
    private static final String DEFAULT_EMPLOYEE_IMAGE_PATH = "/com/example/stores/images/img.png";
    private static final String EMPLOYEE_IMAGES_DIRECTORY = "user_images/employees";

    private static final Pattern SALARY_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");

    public EmployeeManagementController() {
        EmployeeRepositoryImpl employeeRepository = new EmployeeRepositoryImpl();
        ShiftRepositoryImpl shiftRepository = new ShiftRepositoryImpl(); // Cần cho WorkShiftScheduleService
        WorkShiftScheduleRepositoryImpl workShiftScheduleRepository = new WorkShiftScheduleRepositoryImpl();

        // KHAI BÁO VÀ KHỞI TẠO workShiftScheduleRepository Ở ĐÂY
        WorkShiftScheduleServiceImpl actualWorkShiftScheduleService = new WorkShiftScheduleServiceImpl(
                workShiftScheduleRepository,
                employeeRepository,
                shiftRepository
        );

        this.employeeService = new EmployeeServiceImpl(
                employeeRepository,
                actualWorkShiftScheduleService // TRUYỀN ĐÚNG KIỂU WorkShiftScheduleService
        );


        // Khởi tạo salaryTextFieldFormatter
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        salaryTextFieldFormatter = new DecimalFormat("#,##0", symbols);
    }

    @FXML
    public void initialize() {
        File employeeImagesDir = new File(EMPLOYEE_IMAGES_DIRECTORY);
        if (!employeeImagesDir.exists()) {
            boolean created = employeeImagesDir.mkdirs();
            if (created) {
                System.out.println("Đã tạo thư mục: " + EMPLOYEE_IMAGES_DIRECTORY);
            } else {
                System.err.println("Không thể tạo thư mục: " + EMPLOYEE_IMAGES_DIRECTORY);
            }
        }

        setupTableColumns();
        setupComboBoxes();
        loadEmployeesToTable();
        setupTableViewSelectionListener();
        LanguageForManager.getInstance().currentLocaleProperty().addListener((obs, oldL, newL) -> updateUITexts());
        updateUITexts();
    }

    private void setupTableColumns() {
        colEmployeeID.setCellValueFactory(new PropertyValueFactory<>("employeeID"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colBirthDate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));
        colSalary.setCellFactory(tc -> new TableCell<Employee, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal salary, boolean empty) {
                super.updateItem(salary, empty);
                if (empty || salary == null) {
                    setText(null);
                } else {
                    setText(currencyTableViewFormatter.format(salary.doubleValue())); // Sử dụng formatter cho TableView
                }
            }
        });
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void updateUITexts() {
        LanguageForManager lm = LanguageForManager.getInstance();

        // Cập nhật text cho các nút
        if(btnAddEmployee != null) btnAddEmployee.setText(lm.getString("button.addEmployee"));
        if(btnUpdateEmployee != null) btnUpdateEmployee.setText(lm.getString("button.updateInfo"));
        if(btnDeleteEmployee != null) btnDeleteEmployee.setText(lm.getString("button.deleteOrDeactivate"));
        if(btnClearForm != null) btnClearForm.setText(lm.getString("button.clearForm"));
        if(btnExportExcel != null) btnExportExcel.setText(lm.getString("button.exportExcel"));
        if(btnSearch != null) btnSearch.setText(lm.getString("button.search"));
        if(btnRefreshTable != null) btnRefreshTable.setText(lm.getString("button.refreshTable"));
        if(btnChangeEmployeeImage != null) btnChangeEmployeeImage.setText(lm.getString("button.changePhoto"));


        // Cập nhật prompt text
        if(txtEmployeeID != null) txtEmployeeID.setPromptText(lm.getString("prompt.autoGenerated"));
        if(txtUsername != null) txtUsername.setPromptText(lm.getString("prompt.username.employee"));
        if(txtPassword != null) txtPassword.setPromptText(lm.getString("prompt.password.employee"));
        if(txtSearchKeyword != null) txtSearchKeyword.setPromptText(lm.getString("search.prompt.employee"));

        if(colEmployeeID != null) colEmployeeID.setText(lm.getString("table.col.employeeId"));
        if(colFullName != null) colFullName.setText(lm.getString("table.col.fullName"));

        if(employeeTableView != null) employeeTableView.setPlaceholder(new Label(lm.getString("table.placeholder.noEmployees")));

        if (cmbStatus != null) {
            ObservableList<String> statusOptions = FXCollections.observableArrayList(
                    lm.getString("status.working"),
                    lm.getString("status.resigned")
            );
            String selectedStatus = cmbStatus.getValue(); // Lưu lại lựa chọn hiện tại (nếu có)
            cmbStatus.setItems(statusOptions);
            if (selectedStatus != null && statusOptions.contains(selectedStatus)) {
                cmbStatus.setValue(selectedStatus); // Khôi phục lựa chọn nếu còn hợp lệ
            } else if (!statusOptions.isEmpty()){
                cmbStatus.getSelectionModel().selectFirst();
            }
        }

        if (selectedEmployee != null) {
            populateFormWithEmployeeData(selectedEmployee);
        } else {
            clearForm();
        }
    }

    private void loadEmployeesToTable() {
        // Thực hiện trên một luồng nền để không block UI nếu danh sách dài
        // Platform.runLater(() -> { // Hoặc dùng Task nếu cần tiến trình phức tạp hơn
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            employeeList.setAll(employees);
            employeeTableView.setItems(employeeList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Dữ Liệu", "Không thể tải danh sách nhân viên.");
            e.printStackTrace();
        }
        // });
    }

    private void setupTableViewSelectionListener() {
        employeeTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedEmployee = newSelection;
                populateFormWithEmployeeData(selectedEmployee);
                btnUpdateEmployee.setDisable(false);
                btnDeleteEmployee.setDisable(false);
                btnAddEmployee.setDisable(true);
            }
        });
    }

    private void setupComboBoxes() {
        cmbStatus.setItems(FXCollections.observableArrayList("Đang làm", "Nghỉ việc"));
        // Giá trị mặc định sẽ được set trong clearForm()

        ObservableList<String> sortByOptions = FXCollections.observableArrayList(
                "Mặc định", "Mã NV", "Họ và Tên", "Ngày sinh", "Chức vụ", "Lương"
        );
        cmbSortBy.setItems(sortByOptions);
        cmbSortBy.getSelectionModel().selectFirst();

        ObservableList<String> sortOrderOptions = FXCollections.observableArrayList(
                "Tăng dần", "Giảm dần"
        );
        cmbSortOrder.setItems(sortOrderOptions);
        cmbSortOrder.getSelectionModel().selectFirst();
        cmbSortOrder.setDisable(true); // Ban đầu vô hiệu hóa

        // Listener cho cmbSortBy để kích hoạt/vô hiệu hóa cmbSortOrder và thực hiện sort
        cmbSortBy.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean disabled = newVal == null || newVal.equals("Mặc định");
            cmbSortOrder.setDisable(disabled);
            if (!disabled) {
                sortEmployeeTable(); // Sắp xếp khi chọn tiêu chí
            } else {
                // Nếu chọn "Mặc định", tải lại danh sách gốc (không sắp xếp hoặc theo thứ tự từ DB)
                // Việc load lại này nên được thực hiện trong sortEmployeeTable nếu newVal là "Mặc định"
                // hoặc bạn có thể gọi loadEmployeesToTable() ở đây.
                // Hiện tại, sortEmployeeTable() sẽ return nếu là "Mặc định" nên bảng sẽ không thay đổi.
                // Nếu muốn quay về DS gốc khi chọn "Mặc định", bạn có thể gọi loadEmployeesToTable() trong nhánh else này.
                loadEmployeesToTable(); // Thêm dòng này để reset về DS gốc khi chọn "Mặc định"
            }
        });

        // Listener cho cmbSortOrder để thực hiện sort
        cmbSortOrder.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // Chỉ sắp xếp nếu cmbSortOrder không bị disable và có giá trị mới
            // và cmbSortBy cũng có giá trị và không phải là "Mặc định"
            if (!cmbSortOrder.isDisabled() && newVal != null &&
                    cmbSortBy.getValue() != null && !cmbSortBy.getValue().equals("Mặc định")) {
                sortEmployeeTable();
            }
        });
    }
    // Bỏ dấu ngoặc } thừa ở đây nếu có (từ code gốc của tôi có thể bị sót)
    private void sortEmployeeTable() {
        String sortBy = cmbSortBy.getValue();
        String sortOrder = cmbSortOrder.getValue();

        if (sortBy == null || sortBy.equals("Mặc định") || sortOrder == null) {
            // Nếu chọn "Mặc định" hoặc chưa đủ thông tin, có thể không làm gì hoặc tải lại DS gốc
            // loadEmployeesToTable(); // Đã xử lý trong listener của cmbSortBy
            return;
        }

        Comparator<Employee> comparator = null;
        boolean ascending = sortOrder.equals("Tăng dần");

        switch (sortBy) {
            case "Mã NV":
                comparator = Comparator.comparingInt(Employee::getEmployeeID);
                break;
            case "Họ và Tên":
                comparator = Comparator.comparing(Employee::getFullName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Ngày sinh":
                // Xử lý null cho ngày sinh khi so sánh
                comparator = Comparator.comparing(Employee::getBirthDate, Comparator.nullsLast(LocalDate::compareTo));
                break;
            case "Chức vụ":
                comparator = Comparator.comparing(Employee::getPosition, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Lương":
                comparator = Comparator.comparing(Employee::getSalary, Comparator.nullsLast(BigDecimal::compareTo));
                break;
            default:
                // Không làm gì nếu tiêu chí không hợp lệ
                return;
        }

        if (comparator != null) {
            if (!ascending) {
                comparator = comparator.reversed();
            }
            FXCollections.sort(employeeList, comparator); // Sắp xếp ObservableList trực tiếp
            // TableView sẽ tự cập nhật vì nó đang observe employeeList
        }
        System.out.println("Đã sắp xếp theo: " + sortBy + " - " + (ascending ? "Tăng dần" : "Giảm dần"));
    }

    private void populateFormWithEmployeeData(Employee employee) {
        if (employee == null) return; // Kiểm tra null an toàn
        txtEmployeeID.setText(String.valueOf(employee.getEmployeeID()));
        txtUsername.setText(employee.getUsername());
        txtPassword.clear();
        txtFullName.setText(employee.getFullName());
        txtEmail.setText(employee.getEmail());
        txtPhone.setText(employee.getPhone());

        if (employee.getGender() != null) {
            String genderLower = employee.getGender().toLowerCase();
            if (genderLower.equals(rbMale.getText().toLowerCase())) { // So sánh với text của RadioButton
                rbMale.setSelected(true);
            } else if (genderLower.equals(rbFemale.getText().toLowerCase())) {
                rbFemale.setSelected(true);
            } else if (genderLower.equals(rbOther.getText().toLowerCase())) {
                rbOther.setSelected(true);
            } else {
                genderGroup.selectToggle(null);
            }
        } else {
            genderGroup.selectToggle(null);
        }

        if (employee.getSalary() != null) {
            // Định dạng số cho TextField, không có ký hiệu tiền tệ
            txtSalary.setText(salaryTextFieldFormatter.format(employee.getSalary()));
        } else {
            txtSalary.clear();
        }

        dpBirthDate.setValue(employee.getBirthDate());
        txtPosition.setText(employee.getPosition());
        txtSalary.setText(employee.getSalary() != null ? employee.getSalary().toPlainString() : "");
        txtAddress.setText(employee.getAddress());
        cmbStatus.setValue(employee.getStatus());

        loadImageToImageView(employee.getImageUrl());
        selectedImageFile = null;
    }

    private void loadImageToImageView(String imagePathString) {
        // ... (Giữ nguyên phiên bản an toàn đã sửa ở lần trước) ...
        try {
            Image image = null;
            if (imagePathString != null && !imagePathString.isEmpty()) {
                File imageFile = new File(imagePathString);
                if (imageFile.exists() && imageFile.isFile()) {
                    image = new Image(imageFile.toURI().toURL().toString());
                } else {
                    System.err.println("Employee image not found at: " + imagePathString + ". Trying default.");
                }
            }

            if (image == null) {
                URL defaultImageURL = getClass().getResource(DEFAULT_EMPLOYEE_IMAGE_PATH);
                if (defaultImageURL != null) {
                    image = new Image(defaultImageURL.toExternalForm());
                } else {
                    System.err.println("LỖI: Không tìm thấy ảnh mặc định cho nhân viên (trong loadImageToImageView) tại: " + DEFAULT_EMPLOYEE_IMAGE_PATH);
                }
            }
            imgEmployeePhoto.setImage(image);

        } catch (MalformedURLException e) {
            System.err.println("Error loading employee image (MalformedURL): " + e.getMessage() + ". Trying default.");
            loadDefaultImageOnError();
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi tải ảnh nhân viên: " + e.getMessage() + ". Sử dụng ảnh mặc định.");
            loadDefaultImageOnError();
        }
    }

    private void loadDefaultImageOnError(){
        try {
            URL defaultImageURL = getClass().getResource(DEFAULT_EMPLOYEE_IMAGE_PATH);
            if (defaultImageURL != null) {
                imgEmployeePhoto.setImage(new Image(defaultImageURL.toExternalForm()));
            } else {
                System.err.println("LỖI CRITICAL: Không thể tải cả ảnh mặc định.");
                imgEmployeePhoto.setImage(null); // Hoặc một placeholder màu
            }
        } catch (Exception ex) {
            System.err.println("Lỗi CRITICAL khi tải ảnh mặc định sau lỗi: " + ex.getMessage());
            imgEmployeePhoto.setImage(null);
        }
    }



    @FXML
    void handleAddEmployeeAction(ActionEvent event) {
        try {
            Employee newEmployee = getEmployeeFromForm(true); // true for new employee (password required)
            Employee savedEmployee = employeeService.addEmployee(newEmployee);
            if (savedEmployee != null) {
                loadEmployeesToTable(); // Tải lại bảng
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm nhân viên thành công! ID: " + savedEmployee.getEmployeeID());
                clearFormAndSelection();
            }
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Dữ Liệu", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Đã xảy ra lỗi khi thêm nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleUpdateEmployeeAction(ActionEvent event) {
        if (selectedEmployee == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn nhân viên", "Vui lòng chọn một nhân viên từ danh sách để cập nhật.");
            return;
        }
        try {
            Employee updatedEmployeeData = getEmployeeFromForm(false); // false: password not required for update unless changed
            updatedEmployeeData.setEmployeeID(selectedEmployee.getEmployeeID()); // Gán lại ID cho đối tượng cập nhật
            // Giữ lại username cũ nếu không muốn cho thay đổi username sau khi tạo
            updatedEmployeeData.setUsername(selectedEmployee.getUsername());


            boolean success = employeeService.updateEmployee(updatedEmployeeData);
            if (success) {
                loadEmployeesToTable();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật thông tin nhân viên thành công!");
                clearFormAndSelection();
            }
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Dữ Liệu", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Đã xảy ra lỗi khi cập nhật nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleDeleteEmployeeAction(ActionEvent event) {
        if (selectedEmployee == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn nhân viên", "Vui lòng chọn một nhân viên từ danh sách.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận");
        confirmation.setHeaderText("Xác nhận thay đổi trạng thái nhân viên");
        confirmation.setContentText("Bạn có chắc chắn muốn đặt trạng thái 'Nghỉ việc' cho nhân viên '" + selectedEmployee.getFullName() + "' (ID: " + selectedEmployee.getEmployeeID() + ")?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = employeeService.deleteEmployee(selectedEmployee.getEmployeeID()); // Thực ra là đổi status
                if (success) {
                    loadEmployeesToTable();
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật trạng thái 'Nghỉ việc' cho nhân viên.");
                    clearFormAndSelection();
                }
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Đã xảy ra lỗi: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }



    @FXML
    void handleClearFormAction(ActionEvent event) {
        clearFormAndSelection();
    }

    private void clearFormAndSelection() {
        employeeTableView.getSelectionModel().clearSelection(); // Bỏ chọn trên bảng
        selectedEmployee = null;
        clearForm();
    }

    private void clearForm() {
        txtEmployeeID.clear();
        txtUsername.clear();
        txtPassword.clear();
        txtFullName.clear();
        txtEmail.clear();
        txtPhone.clear();
        genderGroup.selectToggle(null);
        dpBirthDate.setValue(null);
        txtPosition.clear();
        txtSalary.clear();
        txtAddress.clear();
        cmbStatus.getSelectionModel().select("Đang làm");

        // Xử lý tải ảnh mặc định một cách an toàn hơn
        try {
            URL defaultImageURL = getClass().getResource(DEFAULT_EMPLOYEE_IMAGE_PATH);
            if (defaultImageURL != null) {
                imgEmployeePhoto.setImage(new Image(defaultImageURL.toExternalForm()));
            } else {
                System.err.println("LỖI: Không tìm thấy ảnh mặc định cho nhân viên tại: " + DEFAULT_EMPLOYEE_IMAGE_PATH);
                // Có thể set một màu nền hoặc để trống ImageView nếu ảnh mặc định không tìm thấy
                imgEmployeePhoto.setImage(null); // Hoặc một placeholder khác
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh mặc định cho nhân viên: " + e.getMessage());
            imgEmployeePhoto.setImage(null);
        }

        selectedImageFile = null;

        btnAddEmployee.setDisable(false);
        btnUpdateEmployee.setDisable(true);
        btnDeleteEmployee.setDisable(true);
    }


    private Employee getEmployeeFromForm(boolean isNew) throws IllegalArgumentException {
        Employee employee = new Employee();
        // ID sẽ được set nếu là update, hoặc tự sinh nếu là new

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText(); // Không trim ở đây, service sẽ xử lý
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        RadioButton selectedRadio = (RadioButton) genderGroup.getSelectedToggle();
        String gender = selectedRadio != null ? selectedRadio.getText() : null;
        LocalDate birthDate = dpBirthDate.getValue();
        String position = txtPosition.getText().trim();
        String salaryStrInput = txtSalary.getText().trim();
        String address = txtAddress.getText().trim();
        String status = cmbStatus.getValue();

        // Basic validation (Service sẽ validate kỹ hơn)
        if (fullName.isEmpty()) throw new IllegalArgumentException("Họ và tên không được để trống.");
        if (username.isEmpty()) throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        if (isNew && (password == null || password.isEmpty())) throw new IllegalArgumentException("Mật khẩu không được để trống khi thêm mới.");
        if (email.isEmpty()) throw new IllegalArgumentException("Email không được để trống.");
        if (phone.isEmpty()) throw new IllegalArgumentException("Số điện thoại không được để trống.");
        if (position.isEmpty()) throw new IllegalArgumentException("Chức vụ không được để trống.");
        BigDecimal salaryValue;
        if (salaryStrInput.isEmpty()) {
            if (!isNew && selectedEmployee != null && selectedEmployee.getSalary() != null) {
                salaryValue = selectedEmployee.getSalary();
            } else {
                salaryValue = BigDecimal.ZERO;
                // if (isNew) throw new IllegalArgumentException("Lương không được để trống."); // Bỏ nếu lương có thể là 0 khi thêm mới
            }
        } else {
            try {
                String parsableSalaryStr = salaryStrInput.replace(".", "");
                if (parsableSalaryStr.isEmpty()){ // Nếu sau khi bỏ ký tự mà rỗng
                    salaryValue = (!isNew && selectedEmployee != null && selectedEmployee.getSalary() != null) ? selectedEmployee.getSalary() : BigDecimal.ZERO;
                } else {
                    salaryValue = new BigDecimal(parsableSalaryStr);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("lương trong ô nhập không hợp lệ. Vui lòng nhập số.");
            }
        }

        // Kiểm tra điều kiện lương tối thiểu (VẪN GIỮ NGUYÊN)
        if (salaryValue.compareTo(new BigDecimal("5000000")) < 0) {
            if (!(isNew && salaryValue.equals(BigDecimal.ZERO))) { // Cho phép lương = 0 khi thêm mới (nếu trường trống)
                if (!salaryValue.equals(BigDecimal.ZERO)) { // Chỉ báo lỗi nếu lương khác 0 mà < 5tr
                    throw new IllegalArgumentException("Mức lương không được dưới 5,000,000. Vui lòng nhập lại.");
                }
            }
        }
        employee.setSalary(salaryValue);


        employee.setUsername(username);
        if (password != null && !password.isEmpty()) {
            employee.setPassword(password);
        }
        employee.setFullName(fullName);
        employee.setEmail(email);
        employee.setPhone(phone);
        employee.setGender(gender);
        employee.setBirthDate(birthDate);
        employee.setPosition(position);
        employee.setSalary(new BigDecimal(salaryStrInput));
        employee.setAddress(address.isEmpty() ? null : address);
        employee.setStatus(status);

        // Xử lý ảnh
        if (selectedImageFile != null) {
            try {
                String fileName = "employee_" + (isNew ? "new" : txtEmployeeID.getText()) + "_" + System.currentTimeMillis() + "." + getFileExtension(selectedImageFile.getName());
                Path targetDir = Paths.get(EMPLOYEE_IMAGES_DIRECTORY);
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                Path targetPath = targetDir.resolve(fileName);
                Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                employee.setImageUrl(targetPath.toString());
            } catch (IOException e) {
                System.err.println("Lỗi lưu ảnh nhân viên: " + e.getMessage());
                // Có thể ném lỗi hoặc chỉ log và không set imageUrl
                showAlert(Alert.AlertType.WARNING, "Lỗi Ảnh", "Không thể lưu ảnh nhân viên. Ảnh sẽ không được cập nhật.");
            }
        } else if (!isNew && selectedEmployee != null) {
            // Nếu là update và không chọn ảnh mới, giữ lại ảnh cũ
            employee.setImageUrl(selectedEmployee.getImageUrl());
        }
        return employee;
    }

    @FXML
    void handleChangeEmployeeImageAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh nhân viên");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        Stage stage = (Stage) btnChangeEmployeeImage.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedImageFile = file;
            try {
                imgEmployeePhoto.setImage(new Image(file.toURI().toURL().toString()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleSearchAction(ActionEvent event) {
        String keyword = txtSearchKeyword.getText().trim();
        List<Employee> searchResult = employeeService.searchEmployees(keyword);
        employeeList.setAll(searchResult);
        employeeTableView.setItems(employeeList);
        if (searchResult.isEmpty() && !keyword.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Tìm kiếm", "Không tìm thấy nhân viên nào khớp với từ khóa '" + keyword + "'.");
        }
    }

    @FXML
    void handleRefreshTableAction(ActionEvent event) {
        txtSearchKeyword.clear();
        cmbSortBy.getSelectionModel().selectFirst(); // Reset về "Mặc định"
        // cmbSortOrder sẽ tự động disable do listener của cmbSortBy
        loadEmployeesToTable(); // Tải lại toàn bộ danh sách (sẽ là không sắp xếp)
        clearFormAndSelection();
        showAlert(Alert.AlertType.INFORMATION, "Làm mới", "Đã tải lại danh sách nhân viên.");
    }

    @FXML
    void handleExportExcelAction(ActionEvent event) {
        // Logic xuất Excel sẽ được thêm sau
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng xuất Excel sẽ được phát triển sau.");
    }

    private String getFileExtension(String fileName) {
        // ... (Như đã có ở ManagerProfileController)
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot == -1 || lastIndexOfDot == fileName.length() - 1) {
            return ""; // No extension or dot is the last character
        }
        return fileName.substring(lastIndexOfDot + 1);
    }

    private void showAlert(Alert.AlertType alertType, String titleKey, String messageKey, Object... messageParams) {
        LanguageForManager lm = LanguageForManager.getInstance();
        Alert alert = new Alert(alertType);
        alert.setTitle(lm.getString(titleKey));
        alert.setHeaderText(null);
        String message = lm.getString(messageKey);
        if (messageParams.length > 0) {
            message = MessageFormat.format(message, messageParams);
        }
        alert.setContentText(message);
        alert.showAndWait();
    }
}