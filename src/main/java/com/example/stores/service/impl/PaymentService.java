package com.example.stores.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.image.Image;
import okhttp3.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.binary.Hex;

public class PaymentService {
    // URL giả định - thường được cung cấp bởi nhà cung cấp dịch vụ thanh toán
    private static final String ZALOPAY_CREATE_ORDER_URL = "https://sandbox.zalopay.com.vn/v001/tpe/createorder";
    private static final String MOMO_CREATE_ORDER_URL = "https://test-payment.momo.vn/v2/gateway/api/create";
    private static final String VIETINBANK_CREATE_ORDER_URL = "https://api.vietinbank.vn/payment/create";

    // Thông tin xác thực - trong môi trường thực tế, lấy từ tệp cấu hình
    private static final String ZALOPAY_APP_ID = "your_zalopay_app_id";
    private static final String ZALOPAY_KEY1 = "your_zalopay_key1";
    private static final String MOMO_PARTNER_CODE = "your_momo_partner_code";
    private static final String MOMO_ACCESS_KEY = "your_momo_access_key";
    private static final String MOMO_SECRET_KEY = "your_momo_secret_key";
    private static final String VIETINBANK_MERCHANT_ID = "your_vietinbank_merchant_id";
    private static final String VIETINBANK_SECRET_KEY = "your_vietinbank_secret_key";

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public PaymentService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Tạo yêu cầu thanh toán dựa trên phương thức được chọn
     */
    public Map<String, Object> createPaymentRequest(Order order, String paymentMethod) throws Exception {
        switch (paymentMethod) {
            case "zalopay":
                return createZaloPayRequest(order);
            case "momo":
                return createMomoRequest(order);
            case "vietinbank":
                return createVietinbankRequest(order);
            default:
                throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
    }

    /**
     * Tạo mã QR từ dữ liệu thanh toán
     */
    public Image generateQRCode(String qrData, int size) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, size, size, hints);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        byte[] imageBytes = outputStream.toByteArray();

        return new Image(new ByteArrayInputStream(imageBytes));
    }

    /**
     * Tạo yêu cầu thanh toán ZaloPay
     */
    private Map<String, Object> createZaloPayRequest(Order order) throws Exception {
        long transactionId = System.currentTimeMillis();
        String appTransId = String.format("%s%s", "CELL", transactionId); // Mã giao dịch

        Map<String, Object> embedData = new HashMap<>();
        Map<String, Object> order_info = new HashMap<>();
        order_info.put("order_id", order.getOrderId());
        order_info.put("customer_name", "Customer Name"); // Lấy từ order
        embedData.put("order_info", order_info);

        Map<String, Object> params = new HashMap<>();
        params.put("app_id", ZALOPAY_APP_ID);
        params.put("app_trans_id", appTransId);
        params.put("app_user", "user_" + order.getCustomerId());
        params.put("app_time", System.currentTimeMillis());
        params.put("amount", (long) order.getTotalAmount());
        params.put("description", "Payment for order " + order.getOrderId());
        params.put("bank_code", "");
        params.put("embed_data", objectMapper.writeValueAsString(embedData));

        // Tạo chữ ký
        String data = String.format("%s|%s|%s|%s|%s|%s|%s",
                ZALOPAY_APP_ID, appTransId, order.getCustomerId(), (long) order.getTotalAmount(),
                System.currentTimeMillis(), embedData, "");

        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(ZALOPAY_KEY1.getBytes(), "HmacSHA256");
        hmacSHA256.init(secretKeySpec);
        String signature = Hex.encodeHexString(hmacSHA256.doFinal(data.getBytes()));

        params.put("mac", signature);

        // Gửi yêu cầu API
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"),
                objectMapper.writeValueAsString(params)
        );

        Request request = new Request.Builder()
                .url(ZALOPAY_CREATE_ORDER_URL)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

            // Cho mục đích demo, chúng ta sẽ trả về một URL giả để tạo mã QR
            // Trong môi trường thực tế, lấy URL từ phản hồi API
            responseMap.put("qr_code_url", "https://zalopay.com.vn/qrcode?id=" + appTransId);
            return responseMap;
        }
    }

    /**
     * Tạo yêu cầu thanh toán MoMo
     */
    private Map<String, Object> createMomoRequest(Order order) throws Exception {
        String orderId = String.valueOf(System.currentTimeMillis());
        String requestId = String.valueOf(System.currentTimeMillis());

        Map<String, Object> params = new HashMap<>();
        params.put("partnerCode", MOMO_PARTNER_CODE);
        params.put("partnerName", "CELLCOMP STORE");
        params.put("storeId", "CELLCOMP_STORE_001");
        params.put("requestId", requestId);
        params.put("amount", (long) order.getTotalAmount());
        params.put("orderId", orderId);
        params.put("orderInfo", "Payment for order " + order.getOrderId());
        params.put("redirectUrl", "https://cellcomp.com/payment/result");
        params.put("ipnUrl", "https://cellcomp.com/payment/notify");
        params.put("requestType", "captureWallet");
        params.put("extraData", "");

        // Tạo chữ ký
        StringBuilder signDataBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            signDataBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }

        String signData = signDataBuilder.toString();
        signData = signData.substring(0, signData.length() - 1); // Loại bỏ dấu & cuối

        SecretKeySpec secretKeySpec = new SecretKeySpec(MOMO_SECRET_KEY.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        String signature = Hex.encodeHexString(mac.doFinal(signData.getBytes()));

        params.put("signature", signature);

        // Gửi yêu cầu API
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"),
                objectMapper.writeValueAsString(params)
        );

        Request request = new Request.Builder()
                .url(MOMO_CREATE_ORDER_URL)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

            // Cho mục đích demo, trả về URL giả
            responseMap.put("qr_code_url", "https://momo.vn/qrcode?id=" + orderId);
            return responseMap;
        }
    }

    /**
     * Tạo yêu cầu thanh toán VietinBank
     */
    private Map<String, Object> createVietinbankRequest(Order order) throws Exception {
        String orderId = String.valueOf(System.currentTimeMillis());

        Map<String, Object> params = new HashMap<>();
        params.put("merchant_id", VIETINBANK_MERCHANT_ID);
        params.put("order_id", orderId);
        params.put("amount", (long) order.getTotalAmount());
        params.put("currency", "VND");
        params.put("description", "Payment for order " + order.getOrderId());
        params.put("return_url", "https://cellcomp.com/payment/result");
        params.put("notify_url", "https://cellcomp.com/payment/notify");
        params.put("timestamp", System.currentTimeMillis());

        // Tạo chữ ký
        String dataToSign = String.format("%s|%s|%d|%s|%s",
                VIETINBANK_MERCHANT_ID, orderId, (long) order.getTotalAmount(),
                "VND", VIETINBANK_SECRET_KEY);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(dataToSign.getBytes());
        String signature = Hex.encodeHexString(hash);

        params.put("signature", signature);

        // Gửi yêu cầu API
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"),
                objectMapper.writeValueAsString(params)
        );

        Request request = new Request.Builder()
                .url(VIETINBANK_CREATE_ORDER_URL)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

            // Cho mục đích demo, trả về URL giả
            responseMap.put("qr_code_url", "https://vietinbank.vn/qrcode?id=" + orderId);
            return responseMap;
        }
    }
    public Image getCustomPaymentQR(String paymentMethod, double amount, String orderCode) {
        try {
            // Đường dẫn đến mã QR của bạn trong thư mục resources
            String imagePath = "/com/example/stores/images/payment/qr_" + paymentMethod + ".png";

            // Nếu bạn muốn đổi thành đường dẫn ngoài dự án:
            // String imagePath = "file:/đường/dẫn/đến/file/qr_" + paymentMethod + ".png";

            // Tải hình ảnh QR
            Image qrImage = new Image(getClass().getResourceAsStream(imagePath));
            return qrImage;
        } catch (Exception e) {
            System.err.println("Không thể tải mã QR tùy chỉnh: " + e.getMessage());
            // Quay lại mã QR tự tạo nếu không tìm thấy mã tùy chỉnh
            return generateFallbackQR(paymentMethod, amount, orderCode);
        }
    }

    // Phương thức tạo QR dự phòng nếu không tìm thấy mã QR tùy chỉnh
    private Image generateFallbackQR(String paymentMethod, double amount, String orderCode) {
        // Tạo dữ liệu QR có cấu trúc để ứng dụng thanh toán nhận dạng
        String qrData = "PAY|" + paymentMethod.toUpperCase() + "|" + orderCode + "|" + amount;

        // Sử dụng phương thức tạo QR hiện tại
        try {
            return generateQRCode(qrData, 300);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}