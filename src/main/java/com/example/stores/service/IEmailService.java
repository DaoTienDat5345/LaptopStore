package com.example.stores.service;

import java.util.concurrent.CompletableFuture;

public interface IEmailService {
    CompletableFuture<String> sendVerificationCode(String recipientEmail);
    CompletableFuture<Boolean> sendOrderConfirmation(String recipientEmail, int orderId, String customerName);
    CompletableFuture<Boolean> sendPasswordResetLink(String recipientEmail, String resetToken);
    CompletableFuture<Boolean> sendGenericEmail(String recipientEmail, String subject, String content);
}