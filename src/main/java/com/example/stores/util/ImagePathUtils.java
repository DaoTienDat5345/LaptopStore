package com.example.stores.util;

public class ImagePathUtils {

    /**
     * Chuẩn hóa đường dẫn hình ảnh để sử dụng với getResourceAsStream
     * @param imagePath Đường dẫn ban đầu từ database
     * @return Đường dẫn đã chuẩn hóa
     */
    public static String normalizeImagePath(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return "/com/example/stores/images/default_product.png";
        }

        // Xử lý đường dẫn cũ
        if (imagePath.startsWith("..\\images\\")) {
            return "/com/example/stores/images/products/" + imagePath.substring(11);
        }

        // Chuẩn hóa các định dạng khác
        String normalized = imagePath.replace("\\", "/");

        // Đảm bảo đường dẫn bắt đầu bằng /
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        return normalized;
    }

    /**
     * Tạo đường dẫn dự phòng chỉ lấy tên file
     * @param imagePath Đường dẫn ban đầu
     * @return Đường dẫn dự phòng
     */
    public static String getFallbackPath(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return "/com/example/stores/images/default_product.png";
        }

        // Trích xuất tên file từ đường dẫn
        String filename;
        if (imagePath.contains("\\")) {
            filename = imagePath.substring(imagePath.lastIndexOf("\\") + 1);
        } else if (imagePath.contains("/")) {
            filename = imagePath.substring(imagePath.lastIndexOf("/") + 1);
        } else {
            filename = imagePath;
        }

        return "/com/example/stores/images/products/" + filename;
    }
}