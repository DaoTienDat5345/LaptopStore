package com.example.stores.controller;

import com.example.stores.dto.EmployeeSalesReportItem;
import com.example.stores.repository.CustomerRepository;
import com.example.stores.repository.OrderDetailRepository;
import com.example.stores.repository.OrderRepository;
import com.example.stores.repository.ProductReviewRepository;
import com.example.stores.repository.impl.*;
import com.example.stores.service.OrderDetailService;
import com.example.stores.service.OrderService;
import com.example.stores.service.ProductReviewService;
import com.example.stores.service.impl.*;

import com.example.stores.util.LanguageForManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class SalesReportController {

    //<editor-fold desc="FXML Controls">
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private Button btnApplyFilter;
    @FXML private Button btnExportExcel;

    @FXML private BarChart<String, Number> employeeSalesBarChart;
    @FXML private TableView<EmployeeSalesReportItem> salesReportTableView;
    @FXML private TableColumn<EmployeeSalesReportItem, Integer> colStt;
    @FXML private TableColumn<EmployeeSalesReportItem, Integer> colEmployeeIdReport;
    @FXML private TableColumn<EmployeeSalesReportItem, String> colEmployeeNameReport;
    @FXML private TableColumn<EmployeeSalesReportItem, Long> colTotalProductsSold;
    @FXML private TableColumn<EmployeeSalesReportItem, Double> colTotalRevenue;
    @FXML private javafx.scene.chart.CategoryAxis xAxisEmployee;
    @FXML private javafx.scene.chart.NumberAxis yAxisValue;

    @FXML private Label lblTotalRevenue;
    //</editor-fold>

    private OrderService orderService;
    private ObservableList<EmployeeSalesReportItem> reportDataList = FXCollections.observableArrayList();
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public SalesReportController() {
        // Khởi tạo các Repository cần thiết
        OrderRepository orderRepository = new OrderRepositoryImpl();
        OrderDetailRepository orderDetailRepository = new OrderDetailRepositoryImpl();
        ProductReviewRepository productReviewRepository = new ProductReviewRepositoryImpl();
        CustomerRepository customerRepository = new CustomerRepositoryImpl(); // << customerRepository đã được khởi tạo ở đây

        // Khởi tạo các Service
        OrderDetailService localOrderDetailService = new OrderDetailServiceImpl(orderDetailRepository);
        ProductReviewService localProductReviewService = new ProductReviewServiceImpl(productReviewRepository);
        // CustomerService localCustomerService = new CustomerServiceImpl(customerRepository); // Bạn có thể không cần biến local này nếu chỉ dùng repo

        this.orderService = new OrderServiceImpl(
                orderRepository,
                localOrderDetailService,
                localProductReviewService,
                customerRepository
        );
    }

    @FXML
    public void initialize() {
        // Thiết lập giá trị mặc định cho DatePicker
        dpStartDate.setValue(LocalDate.now().withDayOfMonth(1)); // Ngày đầu của tháng hiện tại
        dpEndDate.setValue(LocalDate.now()); // Ngày hiện tại

        setupSalesReportTableColumns();

        LanguageForManager.getInstance().currentLocaleProperty().addListener((obs, oldL, newL) -> updateUITexts());
        updateUITexts();
    }

    private void updateUITexts() {
        LanguageForManager lm = LanguageForManager.getInstance();

        // Cập nhật nút và prompt text
        if (btnApplyFilter != null) btnApplyFilter.setText(lm.getString("button.apply"));
        if (btnExportExcel != null) btnExportExcel.setText(lm.getString("button.exportExcel"));
        // (Label Từ ngày, Đến ngày đã dùng %key)

        // Cập nhật tiêu đề biểu đồ và trục (FXML đã dùng %key nên sẽ tự cập nhật khi FXML được reload)
        // Nhưng nếu muốn cập nhật ngay:
        if (employeeSalesBarChart != null) employeeSalesBarChart.setTitle(lm.getString("salesReport.employee.chartTitle"));
        if (xAxisEmployee != null) xAxisEmployee.setLabel(lm.getString("chart.label.employee"));
        if (yAxisValue != null) yAxisValue.setLabel(lm.getString("chart.label.value"));


        // Cập nhật header cột TableView (FXML đã dùng %key)
        if (colStt != null) colStt.setText(lm.getString("table.col.stt"));
        // ... các cột khác ...
        if (salesReportTableView != null) salesReportTableView.setPlaceholder(new Label(lm.getString("table.placeholder.noReportData")));

        // Cập nhật Label tổng kết
        if (lblTotalRevenue != null && lblTotalRevenue.getText().contains("đ")) { // Chỉ cập nhật nếu là tiền Việt
            // Cần tính lại và format lại tổng tiền nếu ngôn ngữ thay đổi (đơn vị tiền tệ)
            // Tạm thời giữ nguyên, hoặc tính lại từ reportDataList
        } else if (lblTotalRevenue != null) {
            // Có thể cần format lại nếu đơn vị tiền tệ thay đổi
        }


        // Tải lại dữ liệu báo cáo để các series name trong biểu đồ được cập nhật
        loadReportData();
    }



    private void setupSalesReportTableColumns() {
        // Cột STT (cần xử lý đặc biệt)
        colStt.setCellFactory(col -> new TableCell<EmployeeSalesReportItem, Integer>() {
            @Override
            public void updateIndex(int i) {
                super.updateIndex(i);
                if (isEmpty() || i < 0) {
                    setText(null);
                } else {
                    setText(Integer.toString(i + 1));
                }
            }
        });

        colEmployeeIdReport.setCellValueFactory(new PropertyValueFactory<>("employeeID"));
        colEmployeeNameReport.setCellValueFactory(new PropertyValueFactory<>("employeeFullName"));
        colTotalProductsSold.setCellValueFactory(new PropertyValueFactory<>("totalProductsSold"));
        colTotalRevenue.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));

        // Định dạng cột Doanh thu
        colTotalRevenue.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));
        colTotalRevenue.setCellFactory(tc -> new TableCell<EmployeeSalesReportItem, Double>() { // << KIỂU LÀ Double
            @Override
            protected void updateItem(Double item, boolean empty) { // << item LÀ Double
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormatter.format(item)); // currencyFormatter.format(double)
                }
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
    }

    private void loadReportData() {
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            // showAlert(Alert.AlertType.ERROR, "alert.title.error", "alert.message.invalidDateRange"); // Đa ngôn ngữ
            showAlert(Alert.AlertType.ERROR, "Ngày không hợp lệ", "Vui lòng chọn khoảng ngày hợp lệ.");
            reportDataList.clear(); // Xóa dữ liệu cũ trên bảng và biểu đồ
            employeeSalesBarChart.getData().clear();
            lblTotalRevenue.setText(currencyFormatter.format(0));
            salesReportTableView.setPlaceholder(new Label("Vui lòng chọn khoảng ngày hợp lệ."));
            return;
        }

        try {
            List<EmployeeSalesReportItem> reportData = orderService.getEmployeeSalesReport(startDate, endDate);
            reportDataList.setAll(reportData);
            salesReportTableView.setItems(reportDataList);

            updateBarChart(reportData);
            updateTotalRevenue(reportData);

            if (reportData.isEmpty()) {
                salesReportTableView.setPlaceholder(new Label("Không có dữ liệu báo cáo cho khoảng thời gian đã chọn."));
                // showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không có dữ liệu báo cáo cho khoảng ngày đã chọn.");
            } else {
                salesReportTableView.setPlaceholder(null);
            }

        } catch (Exception e) {
            // showAlert(Alert.AlertType.ERROR, "alert.error.loadReport", "alert.message.loadReportError"); // Đa ngôn ngữ
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Báo Cáo", "Không thể tải dữ liệu báo cáo.");
            e.printStackTrace();
        }
    }

    private void updateBarChart(List<EmployeeSalesReportItem> reportData) {
        employeeSalesBarChart.getData().clear(); // Xóa dữ liệu cũ

        // Lấy ngôn ngữ hiện tại để dịch tên series (nếu cần và đã có key)
        // LanguageForManager lm = LanguageForManager.getInstance();

        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        // revenueSeries.setName(lm.getString("chart.legend.revenue"));
        revenueSeries.setName("Doanh thu (triệu đồng)"); // Tạm thời text cứng

        XYChart.Series<String, Number> quantitySeries = new XYChart.Series<>();
        // quantitySeries.setName(lm.getString("chart.legend.quantity"));
        quantitySeries.setName("Số lượng sản phẩm"); // Tạm thời text cứng

        for (EmployeeSalesReportItem item : reportData) {
            // Doanh thu trong DTO giờ là double
            double revenueInMillions = item.getTotalRevenue() / 1_000_000.0;
            revenueSeries.getData().add(new XYChart.Data<>(item.getEmployeeFullName(), revenueInMillions));
            quantitySeries.getData().add(new XYChart.Data<>(item.getEmployeeFullName(), item.getTotalProductsSold()));
        }

        if (!revenueSeries.getData().isEmpty() || !quantitySeries.getData().isEmpty()) {
            employeeSalesBarChart.getData().addAll(revenueSeries, quantitySeries);
        }
    }

    private void updateTotalRevenue(List<EmployeeSalesReportItem> reportData) {
        double total = 0.0; // Dùng double
        for(EmployeeSalesReportItem item : reportData) {
            total += item.getTotalRevenue();
        }
        // NumberFormat currentCurrencyFormatter = NumberFormat.getCurrencyInstance(LanguageForManager.getInstance().getCurrentLocale()); // Nếu dùng LanguageForManager
        lblTotalRevenue.setText(currencyFormatter.format(total)); // Dùng currencyFormatter đã khai báo
    }



    @FXML
    void handleApplyFilterAction(ActionEvent event) {
        loadReportData();
    }

    @FXML
    void handleExportExcelAction(ActionEvent event) {
        // Logic xuất Excel sẽ được thêm sau
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng xuất Excel sẽ được phát triển sau.");
    }

    private void exportEmployeeSalesReportToExcel(ObservableList<EmployeeSalesReportItem> dataToExport) {
        if (dataToExport == null || dataToExport.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Không có dữ liệu", "Không có dữ liệu để xuất Excel.");
            return;
        }
        // Logic tạo file Excel với Apache POI, tương tự như đã làm với phiếu nhập
        // ... (Bạn sẽ cần tự triển khai phần này hoặc yêu cầu tôi làm) ...
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng Xuất Excel cho Báo cáo Doanh thu Nhân viên đang được phát triển.");
    }

    private void showAlert(Alert.AlertType alertType, String titleKey, String messageKey, Object... params) {
        LanguageForManager lm = LanguageForManager.getInstance();
        Alert alert = new Alert(alertType);
        alert.setTitle(lm.getString(titleKey));
        alert.setHeaderText(null);
        String message = lm.getString(messageKey);
        if (params != null && params.length > 0 && !(params.length == 1 && params[0] == null) ) {
            try {
                message = MessageFormat.format(message, params);
            } catch (IllegalArgumentException e) { /* ... */ }
        }
        alert.setContentText(message);
        alert.showAndWait();
    }
}