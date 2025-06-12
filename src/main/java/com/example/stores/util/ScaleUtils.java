package com.example.stores.util;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.transform.Scale;

/**
 * Lớp tiện ích để xử lý phóng to/thu nhỏ giao diện theo tỷ lệ màn hình
 */
public class ScaleUtils {

    /**
     * Thiết lập phóng to/thu nhỏ cho scene theo tỷ lệ màn hình
     *
     * @param scene Scene cần xử lý
     * @param originalWidth Chiều rộng thiết kế ban đầu
     * @param originalHeight Chiều cao thiết kế ban đầu
     */
    public static void setupScaling(Scene scene, double originalWidth, double originalHeight) {
        Parent root = scene.getRoot();

        // Đặt điểm neo cho việc phóng to/thu nhỏ ở góc trên bên trái
        root.setScaleX(1);
        root.setScaleY(1);

        // Thêm listener cho sự kiện thay đổi kích thước
        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            updateScale(root, newValue.doubleValue(), scene.getHeight(), originalWidth, originalHeight);
        });

        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            updateScale(root, scene.getWidth(), newValue.doubleValue(), originalWidth, originalHeight);
        });

        // Áp dụng tỷ lệ ban đầu
        updateScale(root, scene.getWidth(), scene.getHeight(), originalWidth, originalHeight);
    }

    /**
     * Cập nhật tỷ lệ phóng to/thu nhỏ khi kích thước thay đổi
     */
    private static void updateScale(Parent root, double width, double height, double originalWidth, double originalHeight) {
        // Tính toán tỷ lệ phóng to/thu nhỏ
        double scaleX = width / originalWidth;
        double scaleY = height / originalHeight;

        // Sử dụng Scale transform thay vì thay đổi thuộc tính của từng phần tử
        Scale scale = new Scale(scaleX, scaleY, 0, 0);

        // Xóa các transform cũ nếu có
        root.getTransforms().clear();

        // Áp dụng transform mới
        root.getTransforms().add(scale);

        System.out.println("Đã cập nhật tỷ lệ: " + scaleX + "x" + scaleY);
    }
}