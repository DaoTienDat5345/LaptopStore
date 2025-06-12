package com.example.stores.service;

import com.example.stores.model.Order;
import javafx.scene.image.Image;
import java.util.Map;

public interface IPaymentService {
    Map<String, Object> createPaymentRequest(Order order, String paymentMethod) throws Exception;
    Image generateQRCode(String qrData, int size) throws Exception;
    Image getCustomPaymentQR(String paymentMethod, double amount, String orderCode);
}