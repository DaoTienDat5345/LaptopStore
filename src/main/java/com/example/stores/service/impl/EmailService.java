package com.example.stores.service.impl;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class EmailService {

    // Thông tin tài khoản email để gửi mã xác thực
    private static final String USERNAME = "nhantran9qq5@gmail.com"; // Thay đổi thành email của bạn
    private static final String PASSWORD = "dvlpvbfdczrnqqka"; // Tạo app password từ tài khoản Google

    private static final Random random = new Random();

    /**
     * Gửi email chứa mã xác thực đến người dùng
     * @param recipientEmail Email người nhận
     * @return Mã xác thực đã gửi
     */
    public CompletableFuture<String> sendVerificationCode(String recipientEmail) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Tạo mã xác thực 6 chữ số
                String verificationCode = generateVerificationCode();

                // Cập nhật phần cấu hình SMTP
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
                props.put("mail.smtp.ssl.protocols", "TLSv1.2");

                // Tạo phiên đăng nhập
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(USERNAME, PASSWORD);
                    }
                });

                // Tạo nội dung email
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(USERNAME));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject("Mã xác thực đổi mật khẩu - CELLCOMP STORE");

                // Tạo nội dung email HTML đẹp hơn
                String htmlContent = "<!DOCTYPE html>"
                        + "<html>"
                        + "<head>"
                        + "<style>"
                        + "body { font-family: Arial, sans-serif; line-height: 1.6; }"
                        + ".container { padding: 20px; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 5px; }"
                        + ".header { background: linear-gradient(to right, #865DFF, #5CB8E4); color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }"
                        + ".content { padding: 20px; }"
                        + ".code { font-size: 24px; font-weight: bold; text-align: center; padding: 15px; margin: 20px 0; background-color: #f5f5f5; border-radius: 5px; letter-spacing: 5px; }"
                        + ".footer { text-align: center; margin-top: 20px; font-size: 12px; color: #777; }"
                        + "</style>"
                        + "</head>"
                        + "<body>"
                        + "<div class='container'>"
                        + "<div class='header'><h2>CELLCOMP STORE</h2></div>"
                        + "<div class='content'>"
                        + "<p>Chào bạn,</p>"
                        + "<p>Bạn vừa yêu cầu đổi mật khẩu tài khoản tại CELLCOMP STORE. Vui lòng sử dụng mã xác thực sau:</p>"
                        + "<div class='code'>" + verificationCode + "</div>"
                        + "<p>Mã xác thực có hiệu lực trong vòng 5 phút.</p>"
                        + "<p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>"
                        + "<p>Trân trọng,<br>Đội ngũ CELLCOMP STORE</p>"
                        + "</div>"
                        + "<div class='footer'>© 2025 CELLCOMP STORE. All rights reserved.</div>"
                        + "</div>"
                        + "</body>"
                        + "</html>";

                message.setContent(htmlContent, "text/html; charset=utf-8");

                // Gửi email
                Transport.send(message);

                return verificationCode;
            } catch (MessagingException e) {
                e.printStackTrace();
                throw new RuntimeException("Không thể gửi email xác thực: " + e.getMessage());
            }
        });
    }

    /**
     * Tạo mã xác thực ngẫu nhiên 6 chữ số
     * @return Mã xác thực
     */
    private String generateVerificationCode() {
        int code = 100000 + random.nextInt(900000); // Tạo số ngẫu nhiên 6 chữ số
        return String.valueOf(code);
    }
}