package com.example.stores.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.util.Duration;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Locale;

public class PaymentQRDialog extends Dialog<Boolean> {

    private final Label countdownLabel = new Label();
    private final ProgressBar timeProgressBar = new ProgressBar(1.0);
    private int remainingTime = 300; // 5 phút (300 giây)
    private Timeline timeline;

    public PaymentQRDialog(boolean isVietnamese, String paymentMethod, Image qrCodeImage, double totalAmount, String orderCode) {
        setTitle(getTitle(isVietnamese, paymentMethod));
        setHeaderText(null);
        
        // Đảm bảo dialog được hiển thị modal
        initModality(Modality.APPLICATION_MODAL);
        
        // Thêm debug
        setOnShowing(event -> {
            System.out.println("DEBUG: PaymentQRDialog showing");
        });
        
       setOnShown(event -> {
    System.out.println("DEBUG: PaymentQRDialog shown");
    // Cast to Stage before calling setAlwaysOnTop
    ((Stage) getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
});

        // Format số tiền đúng định dạng cho từng ngôn ngữ
        NumberFormat currencyFormatter;
        double displayAmount;

        if (isVietnamese) {
            // Tiếng Việt: Hiển thị VND
            currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            displayAmount = totalAmount;
        } else {
            // Tiếng Anh: Chuyển đổi sang USD (tỉ giá 24.000 VND = 1 USD)
            currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
            displayAmount = totalAmount / 24000.0;
        }

        String formattedAmount = currencyFormatter.format(displayAmount);

        // Layout chính - giảm spacing
        VBox mainLayout = new VBox(12);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: white;");

        // ----- HEADER AREA -----
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER);

        // Logo phương thức thanh toán
        ImageView paymentLogoView = new ImageView();
        paymentLogoView.setFitWidth(40);
        paymentLogoView.setFitHeight(40);
        paymentLogoView.setPreserveRatio(true);

        try {
            Image paymentLogo = new Image(getClass().getResourceAsStream(
                    "/com/example/stores/images/payment/" + paymentMethod + "_icon.png"));
            paymentLogoView.setImage(paymentLogo);
        } catch (Exception e) {
            System.out.println("Could not load payment logo: " + e.getMessage());
        }

        // Tiêu đề thanh toán
        Label headerLabel = new Label(getHeaderText(isVietnamese, paymentMethod));
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        headerBox.getChildren().addAll(paymentLogoView, headerLabel);

        // ----- PAYMENT AMOUNT -----
        HBox totalPaymentBox = new HBox(5);
        totalPaymentBox.setAlignment(Pos.CENTER);
        totalPaymentBox.setPadding(new Insets(8));
        totalPaymentBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        Label totalPaymentLabel = new Label(isVietnamese ? "Tổng thanh toán:" : "Total payment:");
        totalPaymentLabel.setFont(Font.font("System", FontWeight.NORMAL, 15));

        Label amountValueLabel = new Label(formattedAmount);
        amountValueLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        amountValueLabel.setTextFill(Color.valueOf("#865DFF"));

        totalPaymentBox.getChildren().addAll(totalPaymentLabel, amountValueLabel);

        // ----- TRANSFER NOTE BOX -----
        VBox transferNoteBox = new VBox(4);
        transferNoteBox.setAlignment(Pos.CENTER);
        transferNoteBox.setPadding(new Insets(8));
        transferNoteBox.setStyle("-fx-background-color: #f0f7ff; -fx-background-radius: 8; " +
                "-fx-border-color: #cce5ff; -fx-border-radius: 8; -fx-border-width: 1;");

        Label transferNoteLabel = new Label(isVietnamese ? "Nội dung chuyển khoản:" : "Transfer note:");
        transferNoteLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        Label orderCodeLabel = new Label("CELLCOMP-" + orderCode);
        orderCodeLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        orderCodeLabel.setStyle("-fx-padding: 5;");
        orderCodeLabel.setTextFill(Color.valueOf("#0066cc"));

        // Thêm nút copy
        Button copyButton = new Button(isVietnamese ? "Sao chép" : "Copy");
        copyButton.setStyle("-fx-background-color: #e7f3ff; -fx-text-fill: #0066cc; -fx-font-size: 12px;");
        copyButton.setOnAction(e -> {
            final javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            final javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString("CELLCOMP-" + orderCode);
            clipboard.setContent(content);

            // Hiện thông báo đã copy
            copyButton.setText(isVietnamese ? "Đã sao chép!" : "Copied!");
            copyButton.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-size: 12px;");

            // Đặt lại sau 2 giây
            Timeline resetButton = new Timeline(new KeyFrame(Duration.seconds(2), evt -> {
                copyButton.setText(isVietnamese ? "Sao chép" : "Copy");
                copyButton.setStyle("-fx-background-color: #e7f3ff; -fx-text-fill: #0066cc; -fx-font-size: 12px;");
            }));
            resetButton.setCycleCount(1);
            resetButton.play();
        });

        HBox noteContentBox = new HBox(8);
        noteContentBox.setAlignment(Pos.CENTER);
        noteContentBox.getChildren().addAll(orderCodeLabel, copyButton);

        transferNoteBox.getChildren().addAll(transferNoteLabel, noteContentBox);

        // ----- QR CODE AREA -----
        StackPane qrContainer = new StackPane();
        qrContainer.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-background-color: white;");
        qrContainer.setPadding(new Insets(8));

        ImageView qrImageView = new ImageView(qrCodeImage);
        qrImageView.setFitWidth(240);
        qrImageView.setFitHeight(240);
        qrImageView.setPreserveRatio(true);
        qrContainer.getChildren().add(qrImageView);

        // ----- COUNTDOWN AREA -----
        VBox timerBox = new VBox(4);
        timerBox.setAlignment(Pos.CENTER);

        updateCountdownLabel(isVietnamese);
        countdownLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        countdownLabel.setTextFill(Color.valueOf("#444444"));

        timeProgressBar.setPrefWidth(240);
        timeProgressBar.setStyle("-fx-accent: #865DFF;");

        timerBox.getChildren().addAll(countdownLabel, timeProgressBar);

        // ----- INSTRUCTION TEXT -----
        Label instructionText = new Label(getQRInstructionText(isVietnamese, paymentMethod));
        instructionText.setWrapText(true);
        instructionText.setTextAlignment(TextAlignment.CENTER);
        instructionText.setFont(Font.font("System", 12));
        instructionText.setTextFill(Color.valueOf("#666666"));

        // ----- BUTTONS -----
        ButtonType paidButtonType = new ButtonType(
                isVietnamese ? "Đã thanh toán" : "Payment Completed",
                ButtonBar.ButtonData.OK_DONE
        );
        ButtonType cancelButtonType = new ButtonType(
                isVietnamese ? "Hủy" : "Cancel",
                ButtonBar.ButtonData.CANCEL_CLOSE
        );

        // Thêm các thành phần vào layout
        mainLayout.getChildren().addAll(
                headerBox,
                totalPaymentBox,
                transferNoteBox,
                qrContainer,
                timerBox,
                instructionText
        );

        getDialogPane().getButtonTypes().addAll(paidButtonType, cancelButtonType);
        getDialogPane().setContent(mainLayout);

        // Tùy chỉnh buttons
        Button paidButton = (Button) getDialogPane().lookupButton(paidButtonType);
        paidButton.setStyle("-fx-background-color: #865DFF; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");

        Button cancelButton = (Button) getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setStyle("-fx-background-color: #f1f1f1; -fx-text-fill: #555; -fx-padding: 10 20;");

        // Xử lý kết quả
        setResultConverter(dialogButton -> {
            stopTimeline();
            if (dialogButton == paidButtonType) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        });

        // Bắt đầu đếm ngược
        startCountdown(isVietnamese);
    }

    private String getQRInstructionText(boolean isVietnamese, String paymentMethod) {
        String appName = getAppName(paymentMethod);

        if (isVietnamese) {
            return String.format("Quét mã QR bằng %s\n• Số tiền và nội dung chuyển khoản sẽ được điền tự động\n• Bạn chỉ cần xác nhận thanh toán", appName);
        } else {
            return String.format("Scan QR with %s\n• Payment amount and message will be auto-filled\n• You only need to confirm the payment", appName);
        }
    }

    private String getAppName(String paymentMethod) {
        switch (paymentMethod) {
            case "zalopay": return "ZaloPay";
            case "momo": return "MoMo";
            case "vietinbank": return "VietinBank";
            default: return "app";
        }
    }

    private String getTitle(boolean isVietnamese, String paymentMethod) {
        switch (paymentMethod) {
            case "zalopay":
                return isVietnamese ? "Thanh toán qua ZaloPay" : "Pay with ZaloPay";
            case "momo":
                return isVietnamese ? "Thanh toán qua MoMo" : "Pay with MoMo";
            case "vietinbank":
                return isVietnamese ? "Thanh toán qua VietinBank" : "Pay with VietinBank";
            default:
                return isVietnamese ? "Thanh toán" : "Payment";
        }
    }

    private String getHeaderText(boolean isVietnamese, String paymentMethod) {
        switch (paymentMethod) {
            case "zalopay":
                return isVietnamese ? "Quét mã QR bằng ZaloPay" : "Scan QR code with ZaloPay";
            case "momo":
                return isVietnamese ? "Quét mã QR bằng MoMo" : "Scan QR code with MoMo";
            case "vietinbank":
                return isVietnamese ? "Quét mã QR bằng VietinBank" : "Scan QR code with VietinBank";
            default:
                return isVietnamese ? "Quét mã QR để thanh toán" : "Scan QR code to pay";
        }
    }
    
    // Thêm phương thức mới với hướng dẫn ngắn gọn hơn
    private String getShortInstructionText(boolean isVietnamese, String paymentMethod, String formattedAmount) {
        String appName;
        switch (paymentMethod) {
            case "zalopay":
                appName = "ZaloPay";
                break;
            case "momo":
                appName = "MoMo";
                break;
            case "vietinbank":
                appName = "VietinBank";
                break;
            default:
                appName = "";
                break;
        }

        if (isVietnamese) {
            return String.format("Mở %s → Quét QR → Xác nhận %s", appName, formattedAmount);
        } else {
            return String.format("Open %s → Scan QR → Confirm %s", appName, formattedAmount);
        }
    }

    // Cập nhật để nhận thêm tham số formattedAmount
    private String getInstructionText(boolean isVietnamese, String paymentMethod, String formattedAmount) {
        String appName;
        switch (paymentMethod) {
            case "zalopay":
                appName = "ZaloPay";
                break;
            case "momo":
                appName = "MoMo";
                break;
            case "vietinbank":
                appName = "VietinBank";
                break;
            default:
                appName = "";
                break;
        }

        if (isVietnamese) {
            return String.format("1. Mở ứng dụng %s trên điện thoại\n" +
                    "2. Chọn chức năng Quét mã QR\n" +
                    "3. Đưa camera đến mã QR trên màn hình\n" +
                    "4. Xác nhận số tiền thanh toán (%s)\n" +
                    "5. Nhấn nút \"Đã thanh toán\" sau khi hoàn tất", appName, formattedAmount);
        } else {
            return String.format("1. Open %s app on your phone\n" +
                    "2. Select Scan QR code function\n" +
                    "3. Point camera at the QR code\n" +
                    "4. Confirm payment amount (%s)\n" +
                    "5. Press \"Completed Payment\" after finishing", appName, formattedAmount);
        }
    }

    private void updateCountdownLabel(boolean isVietnamese) {
        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;
        if (isVietnamese) {
            countdownLabel.setText(String.format("Vui lòng thanh toán trong: %02d:%02d", minutes, seconds));
        } else {
            countdownLabel.setText(String.format("Please complete payment within: %02d:%02d", minutes, seconds));
        }

        // Cập nhật giá trị progress bar
        double progress = (double) remainingTime / 300.0;
        timeProgressBar.setProgress(progress);

        // Đổi màu khi gần hết thời gian
        if (remainingTime <= 60) { // dưới 1 phút
            timeProgressBar.setStyle("-fx-accent: #ff6b6b;"); // Đỏ
            countdownLabel.setTextFill(Color.valueOf("#ff6b6b"));
        } else if (remainingTime <= 120) { // dưới 2 phút
            timeProgressBar.setStyle("-fx-accent: #ffa94d;"); // Cam
            countdownLabel.setTextFill(Color.valueOf("#ffa94d"));
        }
    }

    private void startCountdown(boolean isVietnamese) {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (remainingTime > 0) {
                remainingTime--;
                updateCountdownLabel(isVietnamese);
            } else {
                stopTimeline();
                close();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void stopTimeline() {
        if (timeline != null) {
            timeline.stop();
        }
    }
}