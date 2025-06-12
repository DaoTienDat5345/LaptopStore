package com.example.stores.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;

public class PaymentMethodDialog extends Dialog<String> {
    
    private final ToggleGroup toggleGroup = new ToggleGroup();
    
    public PaymentMethodDialog(boolean isVietnamese) {
        // Cài đặt dialog
        setTitle(isVietnamese ? "Chọn Phương Thức Thanh Toán" : "Select Payment Method");
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        
        // Tạo layout chính - kích thước lớn hơn
        VBox mainLayout = new VBox(15); // Tăng khoảng cách giữa các phần tử
        mainLayout.setPadding(new Insets(20)); // Tăng padding
        mainLayout.setPrefWidth(350); // Đặt chiều rộng cố định cho dialog
        mainLayout.setStyle("-fx-background-color: white;");
        
        // Tạo label tiêu đề
        Label titleLabel = new Label(isVietnamese ? "Chọn phương thức thanh toán" : "Select payment method");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        titleLabel.setPadding(new Insets(0, 0, 10, 0));
        
        // Tạo các radio button với kích thước lớn hơn
        RadioButton zalopayButton = createCompactPaymentOption(
                "ZaloPay", 
                "/com/example/stores/images/payment/zalopay_icon.png",
                "zalopay"
        );
        
        RadioButton momoButton = createCompactPaymentOption(
                "MoMo", 
                "/com/example/stores/images/payment/momo_icon.png",
                "momo"
        );
        
        RadioButton viettinButton = createCompactPaymentOption(
                "VietinBank", 
                "/com/example/stores/images/payment/vietinbank_icon.png",
                "vietinbank"
        );
        
        RadioButton codButton = createCompactPaymentOption(
                isVietnamese ? "Thanh toán khi nhận hàng" : "Cash on Delivery", 
                "/com/example/stores/images/payment/cod_icon.png",
                "cod"
        );
        
        // Thêm vào toggle group
        zalopayButton.setToggleGroup(toggleGroup);
        momoButton.setToggleGroup(toggleGroup);
        viettinButton.setToggleGroup(toggleGroup);
        codButton.setToggleGroup(toggleGroup);
        
        // Chọn mặc định
        codButton.setSelected(true);
        
        // Thêm vào layout
        mainLayout.getChildren().addAll(titleLabel, zalopayButton, momoButton, viettinButton, codButton);
        
        // Thêm nút OK và Cancel
        ButtonType confirmButtonType = new ButtonType(
                isVietnamese ? "Xác nhận" : "Confirm", 
                ButtonBar.ButtonData.OK_DONE
        );
        
        ButtonType cancelButtonType = new ButtonType(
                isVietnamese ? "Hủy" : "Cancel", 
                ButtonBar.ButtonData.CANCEL_CLOSE
        );
        
        getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);
        
        // Style cho các nút
        Button confirmButton = (Button) getDialogPane().lookupButton(confirmButtonType);
        confirmButton.setStyle("-fx-background-color: #865DFF; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        
        Button cancelButton = (Button) getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setStyle("-fx-padding: 8 15;");
        
        // Thêm stylesheet
        getDialogPane().getStylesheets().add(getClass().getResource("/com/example/stores/css/PaymentDialog.css").toExternalForm());
        getDialogPane().setContent(mainLayout);
        
        // Xử lý kết quả
        setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                RadioButton selected = (RadioButton) toggleGroup.getSelectedToggle();
                if (selected != null) {
                    return (String) selected.getUserData();
                }
            }
            return null;
        });
    }
    
    private RadioButton createCompactPaymentOption(String text, String imagePath, String userData) {
        RadioButton radioButton = new RadioButton(text);
        radioButton.setPadding(new Insets(10)); // Tăng padding
        radioButton.setUserData(userData);
        radioButton.getStyleClass().add("payment-radio-button");
        
        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(32); // Tăng kích thước icon
            imageView.setFitWidth(32);
            imageView.setPreserveRatio(true);
            radioButton.setGraphic(imageView);
            
            // Khoảng cách giữa icon và text
            radioButton.setGraphicTextGap(15);
            
        } catch (Exception e) {
            System.out.println("Could not load image: " + imagePath);
        }
        
        return radioButton;
    }
}