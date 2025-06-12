package com.example.stores.controller;

import com.example.stores.model.Employee;
import com.example.stores.model.Shift;
import com.example.stores.model.WorkShiftSchedule;
import com.example.stores.repository.EmployeeRepository;
import com.example.stores.repository.ShiftRepository;
import com.example.stores.repository.WorkShiftScheduleRepository;
import com.example.stores.repository.impl.EmployeeRepositoryImpl;
import com.example.stores.repository.impl.ShiftRepositoryImpl;
import com.example.stores.repository.impl.WorkShiftScheduleRepositoryImpl;
import com.example.stores.service.EmployeeService;
import com.example.stores.service.ShiftService;
import com.example.stores.service.WorkShiftScheduleService;
import com.example.stores.service.impl.EmployeeServiceImpl;
import com.example.stores.service.impl.ShiftServiceImpl;
import com.example.stores.service.impl.WorkShiftScheduleServiceImpl;

import com.example.stores.util.LanguageManager;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Comparator; // Đã thêm
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShiftManagementController {

    //<editor-fold desc="FXML Controls - Filters and Main Actions">
    @FXML private ToggleButton btnFilterSang;
    @FXML private ToggleButton btnFilterChieu;
    @FXML private ToggleButton btnFilterToi;
    @FXML private ToggleButton btnFilterAllShifts;
    @FXML private ToggleGroup shiftFilterToggleGroup;
    @FXML private Button btnRefreshScheduleTable;
    //</editor-fold>

    //<editor-fold desc="FXML Controls - Employee List Panel">
    @FXML private TextField txtSearchEmployeeForSchedule;
    @FXML private ListView<Employee> employeeListViewForSchedule;
    //</editor-fold>

    //<editor-fold desc="FXML Controls - Schedule Assignment Form">
    @FXML private TitledPane assignmentPane;
    @FXML private ComboBox<Employee> cmbEmployeeForSchedule;
    @FXML private ComboBox<String> cmbDayOfWeekForSchedule;
    @FXML private ComboBox<Shift> cmbShiftTypeForSchedule;
    @FXML private TextArea txtScheduleNotes;
    @FXML private Button btnAddSchedule;
    @FXML private Button btnClearScheduleForm;
    @FXML private Button btnUpdateSchedule;
    //</editor-fold>

    //<editor-fold desc="FXML Controls - Work Schedule TableView">
    @FXML private TableView<WorkShiftSchedule> workScheduleTableView;
    @FXML private TableColumn<WorkShiftSchedule, Integer> colScheduleID;
    @FXML private TableColumn<WorkShiftSchedule, String> colScheduleEmployeeName;
    @FXML private TableColumn<WorkShiftSchedule, String> colScheduleDayOfWeek;
    @FXML private TableColumn<WorkShiftSchedule, String> colScheduleShiftDetails;
    @FXML private TableColumn<WorkShiftSchedule, String> colScheduleNotes;
    @FXML private TableColumn<WorkShiftSchedule, Void> colScheduleActions;
    //</editor-fold>

    private EmployeeService employeeService;
    private ShiftService shiftService;
    private WorkShiftScheduleService workShiftScheduleService;

    private final ObservableList<Employee> allActiveEmployeesList = FXCollections.observableArrayList();
    private final ObservableList<Shift> allShiftTypesList = FXCollections.observableArrayList();
    private final ObservableList<WorkShiftSchedule> workScheduleList = FXCollections.observableArrayList();

    private WorkShiftSchedule selectedWorkSchedule;

    private final List<String> daysOfWeekVietnamese = Arrays.asList(
            "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"
    );

    public ShiftManagementController() {
        // Khởi tạo các Repository
        EmployeeRepository employeeRepository = new EmployeeRepositoryImpl();
        ShiftRepository shiftRepository = new ShiftRepositoryImpl();
        WorkShiftScheduleRepository workShiftScheduleRepository = new WorkShiftScheduleRepositoryImpl(); // Đã khởi tạo Repo

        // Khởi tạo các Service
        // WorkShiftScheduleService
        this.workShiftScheduleService = new WorkShiftScheduleServiceImpl(
                workShiftScheduleRepository,
                employeeRepository,
                shiftRepository
        );

        // EmployeeService
        this.employeeService = new EmployeeServiceImpl(
                employeeRepository,
                this.workShiftScheduleService
        );

        // ShiftService <--- SỬA Ở ĐÂY
        this.shiftService = new ShiftServiceImpl(
                shiftRepository,
                workShiftScheduleRepository // TRUYỀN WorkShiftScheduleRepository
        );
    }

    @FXML
    public void initialize() {
        btnFilterAllShifts.setSelected(true);
        shiftFilterToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                btnFilterAllShifts.setSelected(true);
            }
            filterWorkScheduleTable();
        });

        setupEmployeeListViewAndComboBox();
        loadAllEmployeesToListAndComboBox();
        setupEmployeeSearchListener();

        setupScheduleAssignmentComboBoxes();

        setupWorkScheduleTableColumns();
        loadWorkSchedulesToTable();
        setupWorkScheduleTableSelectionListener();
        setupWorkScheduleTableActionColumn();

        clearScheduleFormAndSelection();
        btnUpdateSchedule.setDisable(true);
    }
    private void updateUITexts() {
        LanguageManager lm = LanguageManager.getInstance();

        // Cập nhật tiêu đề của TitledPane và các nút
        if(assignmentPane != null) assignmentPane.setText(lm.getString(selectedWorkSchedule == null ? "shift.assign.newTitle" : MessageFormat.format(lm.getString("shift.assign.updateTitle"), selectedWorkSchedule.getScheduleID())));
        if(btnFilterAllShifts != null) btnFilterAllShifts.setText(lm.getString("button.all"));
        if(btnFilterSang != null) btnFilterSang.setText(lm.getString("shiftType.morning"));
        if(btnFilterChieu != null) btnFilterChieu.setText(lm.getString("shiftType.afternoon"));
        if(btnFilterToi != null) btnFilterToi.setText(lm.getString("shiftType.evening"));
        if(btnRefreshScheduleTable != null) btnRefreshScheduleTable.setText(lm.getString("button.refreshScheduleTable"));
        if(btnAddSchedule != null) btnAddSchedule.setText(lm.getString("button.addSchedule"));
        if(btnUpdateSchedule != null) btnUpdateSchedule.setText(lm.getString("button.updateSchedule"));
        if(btnClearScheduleForm != null) btnClearScheduleForm.setText(lm.getString("button.clearForm"));


        // Cập nhật prompt text cho ComboBox và TextField
        if(cmbEmployeeForSchedule != null) cmbEmployeeForSchedule.setPromptText(lm.getString("prompt.selectEmployee"));
        if(cmbDayOfWeekForSchedule != null) cmbDayOfWeekForSchedule.setPromptText(lm.getString("prompt.selectDayOfWeek"));
        if(cmbShiftTypeForSchedule != null) cmbShiftTypeForSchedule.setPromptText(lm.getString("prompt.selectShiftType"));
        if(txtSearchEmployeeForSchedule != null) txtSearchEmployeeForSchedule.setPromptText(lm.getString("search.prompt.employeeShort"));

        // Cập nhật header cột TableView (FXML đã dùng %key nên sẽ tự cập nhật khi reload view)
        // Nhưng nếu muốn cập nhật ngay mà không reload view, có thể làm:
        if(colScheduleID != null) colScheduleID.setText(lm.getString("table.col.id"));
        if(colScheduleEmployeeName != null) colScheduleEmployeeName.setText(lm.getString("table.col.employeeName"));
        // ... làm tương tự cho các cột còn lại của workScheduleTableView ...

        // Cập nhật placeholder
        if(workScheduleTableView != null) workScheduleTableView.setPlaceholder(new Label(lm.getString("table.placeholder.noWorkSchedule")));

        // Cập nhật text cho Label panel danh sách nhân viên
        // (Giả sử bạn có một Label fx:id="lblEmployeeListTitle" trong FXML cho "Danh sách Nhân viên")
        // if(lblEmployeeListTitle != null) lblEmployeeListTitle.setText(lm.getString("label.employeeList"));
    }

    // ======================== EMPLOYEE LIST PANEL ========================
    private void setupEmployeeListViewAndComboBox() { /* Giữ nguyên từ trước */
        employeeListViewForSchedule.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Employee employee, boolean empty) {
                super.updateItem(employee, empty);
                setText(empty || employee == null ? null : employee.getFullName() + " (ID: " + employee.getEmployeeID() + ")");
            }
        });
        cmbEmployeeForSchedule.setConverter(new StringConverter<>() {
            @Override public String toString(Employee e) { return e == null ? null : e.getFullName() + " (ID: " + e.getEmployeeID() + ")";}
            @Override public Employee fromString(String s) { return null; }
        });
        employeeListViewForSchedule.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            if (nv != null) cmbEmployeeForSchedule.setValue(nv);
        });
    }
    private void loadAllEmployeesToListAndComboBox() { /* Giữ nguyên từ trước */
        try {
            List<Employee> activeEmployees = employeeService.getAllEmployees().stream()
                    .filter(emp -> "Đang làm".equalsIgnoreCase(emp.getStatus()))
                    .collect(Collectors.toList());
            allActiveEmployeesList.setAll(activeEmployees);
            employeeListViewForSchedule.setItems(allActiveEmployeesList);
            cmbEmployeeForSchedule.setItems(allActiveEmployeesList);
        } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tải được DS nhân viên."); e.printStackTrace(); }
    }
    private void setupEmployeeSearchListener() { /* Giữ nguyên từ trước */
        txtSearchEmployeeForSchedule.textProperty().addListener((obs, ov, nv) -> filterEmployeeList(nv));
    }
    private void filterEmployeeList(String keyword) { /* Giữ nguyên từ trước */
        ObservableList<Employee> sourceList = allActiveEmployeesList;
        if (keyword == null || keyword.trim().isEmpty()) {
            employeeListViewForSchedule.setItems(sourceList);
            cmbEmployeeForSchedule.setItems(sourceList);
        } else {
            ObservableList<Employee> filteredList = FXCollections.observableArrayList();
            String lowerKeyword = keyword.toLowerCase();
            for (Employee emp : sourceList) {
                if (emp.getFullName().toLowerCase().contains(lowerKeyword) ||
                        String.valueOf(emp.getEmployeeID()).contains(keyword) ||
                        (emp.getUsername() != null && emp.getUsername().toLowerCase().contains(lowerKeyword))) {
                    filteredList.add(emp);
                }
            }
            employeeListViewForSchedule.setItems(filteredList);
            cmbEmployeeForSchedule.setItems(filteredList);
        }
    }

    // ======================== SCHEDULE ASSIGNMENT FORM ========================
    private void setupScheduleAssignmentComboBoxes() {
        try {
            allShiftTypesList.setAll(shiftService.getAllShifts());
            cmbShiftTypeForSchedule.setItems(allShiftTypesList);
            cmbShiftTypeForSchedule.setConverter(new StringConverter<>() {
                @Override public String toString(Shift s) { return s == null ? null : s.getShiftName(); }
                @Override public Shift fromString(String s) { return null; }
            });
        } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tải được Loại ca."); e.printStackTrace(); }

        cmbDayOfWeekForSchedule.setItems(FXCollections.observableArrayList(daysOfWeekVietnamese));
    }

    @FXML
    void handleAddScheduleAction(ActionEvent event) {
        Employee selectedEmp = cmbEmployeeForSchedule.getValue();
        Shift selectedShift = cmbShiftTypeForSchedule.getValue();
        String selectedDayOfWeek = cmbDayOfWeekForSchedule.getValue();
        String notes = txtScheduleNotes.getText().trim();

        if (selectedEmp == null) { showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn nhân viên."); return; }
        if (selectedShift == null) { showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn loại ca."); return; }
        if (selectedDayOfWeek == null) { showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn thứ làm việc."); return; }

        WorkShiftSchedule newSchedule = new WorkShiftSchedule(
                selectedEmp.getEmployeeID(),
                selectedShift.getShiftID(),
                selectedDayOfWeek,
                notes
        );

        try {
            WorkShiftSchedule savedSchedule = workShiftScheduleService.addSchedule(newSchedule);
            // Không cần kiểm tra null cho savedSchedule vì addSchedule sẽ ném lỗi nếu thất bại
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm phân công ca thành công!");
            loadWorkSchedulesToTable();
            clearScheduleForm();
        } catch (RuntimeException e) { // Bắt cả RuntimeException từ service
            showAlert(Alert.AlertType.ERROR, "Lỗi Dữ Liệu hoặc Hệ Thống", e.getMessage());
        } catch (Exception e) { // Bắt lỗi không mong muốn khác
            showAlert(Alert.AlertType.ERROR, "Lỗi Không Mong Muốn", "Lỗi khi thêm phân công ca: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleUpdateScheduleAction(ActionEvent event) {
        if (selectedWorkSchedule == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn lịch", "Vui lòng chọn một lịch phân công từ bảng để cập nhật.");
            return;
        }
        Employee selectedEmp = cmbEmployeeForSchedule.getValue();
        Shift selectedShift = cmbShiftTypeForSchedule.getValue();
        String selectedDayOfWeek = cmbDayOfWeekForSchedule.getValue();
        String notes = txtScheduleNotes.getText().trim();

        if (selectedEmp == null) { showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn nhân viên."); return; }
        if (selectedShift == null) { showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn loại ca."); return; }
        if (selectedDayOfWeek == null) { showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn thứ làm việc."); return; }

        // Cập nhật thông tin cho selectedWorkSchedule
        selectedWorkSchedule.setEmployeeID(selectedEmp.getEmployeeID());
        selectedWorkSchedule.setShiftID(selectedShift.getShiftID());
        selectedWorkSchedule.setDayOfWeek(selectedDayOfWeek);
        selectedWorkSchedule.setNotes(notes);

        try {
            boolean success = workShiftScheduleService.updateSchedule(selectedWorkSchedule);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật lịch phân công thành công!");
                loadWorkSchedulesToTable();
                clearScheduleFormAndSelection();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật lịch phân công.");
            }
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Dữ Liệu hoặc Hệ Thống", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Không Mong Muốn", "Lỗi khi cập nhật lịch: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleClearScheduleFormAction(ActionEvent event) {
        clearScheduleFormAndSelection();
    }

    private void clearScheduleForm() {
        cmbEmployeeForSchedule.getSelectionModel().clearSelection();
        cmbShiftTypeForSchedule.getSelectionModel().clearSelection();
        cmbDayOfWeekForSchedule.getSelectionModel().clearSelection();
        txtScheduleNotes.clear();

        btnAddSchedule.setDisable(false);
        btnUpdateSchedule.setDisable(true);
        assignmentPane.setText("Phân Công Ca Mới");
        selectedWorkSchedule = null; // Quan trọng: reset selectedWorkSchedule
    }

    private void clearScheduleFormAndSelection() {
        clearScheduleForm();
        if (workScheduleTableView.getSelectionModel() != null) {
            workScheduleTableView.getSelectionModel().clearSelection();
        }
    }

    // ======================== WORK SCHEDULE TABLEVIEW ========================
    private void setupWorkScheduleTableColumns() {
        colScheduleID.setCellValueFactory(new PropertyValueFactory<>("scheduleID"));
        colScheduleEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeFullName"));
        colScheduleDayOfWeek.setCellValueFactory(new PropertyValueFactory<>("dayOfWeek"));
        colScheduleShiftDetails.setCellValueFactory(new PropertyValueFactory<>("shiftDetails"));
        colScheduleNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
    }

    private void setupWorkScheduleTableSelectionListener() {
        workScheduleTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedWorkSchedule = newVal;
                populateScheduleFormForUpdate(selectedWorkSchedule);
                btnAddSchedule.setDisable(true);
                btnUpdateSchedule.setDisable(false);
            } else {
                // Nếu không có dòng nào được chọn, và không phải đang trong quá trình update (btnUpdateSchedule đang disable)
                // thì mới reset form về trạng thái "Thêm mới"
                if (btnUpdateSchedule.isDisabled()) {
                    clearScheduleForm();
                }
            }
        });
    }

    private void populateScheduleFormForUpdate(WorkShiftSchedule schedule) {
        Optional<Employee> empOpt = allActiveEmployeesList.stream()
                .filter(e -> e.getEmployeeID() == schedule.getEmployeeID())
                .findFirst();
        empOpt.ifPresent(cmbEmployeeForSchedule::setValue);

        Optional<Shift> shiftOpt = allShiftTypesList.stream()
                .filter(s -> s.getShiftID().equals(schedule.getShiftID()))
                .findFirst();
        shiftOpt.ifPresent(cmbShiftTypeForSchedule::setValue);

        cmbDayOfWeekForSchedule.setValue(schedule.getDayOfWeek());
        txtScheduleNotes.setText(schedule.getNotes());
        assignmentPane.setText("Cập nhật Phân Công (ID: " + schedule.getScheduleID() + ")");
    }

    private void loadWorkSchedulesToTable() {
        try {
            List<WorkShiftSchedule> schedules = workShiftScheduleService.getAllSchedulesWithDetails();
            workScheduleList.setAll(schedules);
            filterWorkScheduleTable(); // Áp dụng bộ lọc hiện tại sau khi tải
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Lịch", "Không thể tải lịch làm việc.");
            e.printStackTrace();
        }
    }

    private void setupWorkScheduleTableActionColumn() { /* Giữ nguyên từ trước */
        Callback<TableColumn<WorkShiftSchedule, Void>, TableCell<WorkShiftSchedule, Void>> cellFactory = param -> {
            final TableCell<WorkShiftSchedule, Void> cell = new TableCell<>() { // Tạo biến cell để setAlignment
                private final Button btnDeleteSchedule = new Button("Xóa");
                {
                    btnDeleteSchedule.setStyle("-fx-background-color: #E53E3E; -fx-text-fill: white; -fx-font-size: 10px;");
                    // ... setOnAction ...
                    btnDeleteSchedule.setOnAction((ActionEvent event) -> {
                        WorkShiftSchedule schedule = getTableView().getItems().get(getIndex());
                        handleDeleteSpecificScheduleAction(schedule);
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(btnDeleteSchedule);
                        setAlignment(Pos.CENTER); // CĂN GIỮA NÚT TRONG CELL
                    }
                }
            };
            // cell.setAlignment(Pos.CENTER); // Hoặc set ở đây cũng được, nhưng set trong updateItem khi có graphic thì tốt hơn
            return cell;
        };
        colScheduleActions.setCellFactory(cellFactory);
    }

    private void handleDeleteSpecificScheduleAction(WorkShiftSchedule scheduleToDelete) { /* Giữ nguyên từ trước */
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa phân công ca cho '" + scheduleToDelete.getEmployeeFullName() +
                        "' (" + scheduleToDelete.getDayOfWeek() + ", " + scheduleToDelete.getShiftDetails() + ")?",
                ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Xác nhận xóa");
        confirmation.setHeaderText(null);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    boolean success = workShiftScheduleService.deleteSchedule(scheduleToDelete.getScheduleID());
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa lịch phân công.");
                        loadWorkSchedulesToTable();
                        clearScheduleFormAndSelection();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa lịch phân công.");
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Lỗi khi xóa lịch: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    // ======================== FILTER AND REFRESH FOR SCHEDULE TABLE ========================
    // Không cần @FXML cho các nút filter nếu chúng ta xử lý qua ToggleGroup listener
    // @FXML void handleFilterScheduleAction(ActionEvent event) { } // Không cần nữa

    @FXML
    void handleRefreshScheduleTableAction(ActionEvent event) {
        btnFilterAllShifts.setSelected(true);
        // loadWorkSchedulesToTable(); // filterWorkScheduleTable() sẽ được gọi bởi listener của ToggleGroup
        showAlert(Alert.AlertType.INFORMATION, "Làm mới", "Đã tải lại toàn bộ lịch làm việc.");
    }

    private void filterWorkScheduleTable() {
        Toggle selectedToggle = shiftFilterToggleGroup.getSelectedToggle();
        String filterShiftID = null;

        if (selectedToggle == btnFilterSang) filterShiftID = "SANG";
        else if (selectedToggle == btnFilterChieu) filterShiftID = "CHIEU";
        else if (selectedToggle == btnFilterToi) filterShiftID = "TOI";
        // Nếu là btnFilterAllShifts, filterShiftID sẽ là null (hoặc không cần kiểm tra cụ thể)

        final String finalFilterShiftID = filterShiftID;
        // Lấy danh sách gốc từ service một lần, sau đó lọc trên danh sách đó
        // Hoặc nếu bạn muốn query DB mỗi lần lọc thì gọi service với tham số filter
        List<WorkShiftSchedule> allSchedulesFromService = workShiftScheduleService.getAllSchedulesWithDetails();


        if (finalFilterShiftID == null) {
            workScheduleList.setAll(allSchedulesFromService);
        } else {
            List<WorkShiftSchedule> filtered = allSchedulesFromService.stream()
                    .filter(schedule -> schedule.getShiftID().equalsIgnoreCase(finalFilterShiftID))
                    .collect(Collectors.toList());
            workScheduleList.setAll(filtered);
        }
        workScheduleTableView.setItems(workScheduleList); // Gán lại items cho tableview

        if (workScheduleList.isEmpty()) {
            workScheduleTableView.setPlaceholder(new Label(finalFilterShiftID == null ? "Chưa có lịch làm việc nào được phân công." : "Không có lịch cho ca đã chọn."));
        } else {
            workScheduleTableView.setPlaceholder(null); // Xóa placeholder nếu có dữ liệu
        }
    }

    // Tiện ích Alert
    private void showAlert(Alert.AlertType alertType, String titleKey, String messageKey, Object... params) {
        LanguageManager lm = LanguageManager.getInstance();
        Alert alert = new Alert(alertType);
        alert.setTitle(lm.getString(titleKey));
        alert.setHeaderText(null);
        String message = lm.getString(messageKey);
        if (params.length > 0 && !(params.length == 1 && params[0] == null) ) {
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