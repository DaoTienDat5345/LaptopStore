// Ví dụ: src/main/java/com/example/stores/util/LanguageForManager.java
package com.example.stores.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageForManager {
    private static final LanguageForManager INSTANCE = new LanguageForManager();
    private final ObjectProperty<Locale> currentLocale = new SimpleObjectProperty<>();
    private ResourceBundle messages;
    private static final String BUNDLE_BASE_NAME = "com.example.stores.lang.messages"; // Đường dẫn đến resource bundle

    private LanguageForManager() {
        // Ngôn ngữ mặc định khi khởi động
        setLocale(new Locale("vi", "VN"));
    }

    public static LanguageForManager getInstance() {
        return INSTANCE;
    }

    public void setLocale(Locale locale) {
        try {
            messages = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale, new UTF8Control());
            currentLocale.set(locale); // Thông báo cho các listener rằng Locale đã thay đổi
            System.out.println("Đã chuyển ngôn ngữ sang: " + locale);
        } catch (Exception e) {
            System.err.println("Không thể load resource bundle cho locale: " + locale + ". Lỗi: " + e.getMessage());
            // Giữ lại messages cũ hoặc set về mặc định an toàn
            if (messages == null) { // Nếu chưa có messages nào được load
                try {
                    messages = ResourceBundle.getBundle(BUNDLE_BASE_NAME, new Locale("en", "US"), new UTF8Control()); // Fallback English
                    currentLocale.set(new Locale("en", "US"));
                } catch (Exception ex) {
                    System.err.println("Không thể load resource bundle mặc định (English): " + ex.getMessage());
                }
            }
        }
    }

    public Locale getCurrentLocale() {
        return currentLocale.get();
    }

    public ObjectProperty<Locale> currentLocaleProperty() {
        return currentLocale;
    }

    public String getString(String key) {
        if (messages != null && messages.containsKey(key)) {
            return messages.getString(key);
        }
        System.err.println("Không tìm thấy key '" + key + "' trong resource bundle cho locale: " + (currentLocale.get() != null ? currentLocale.get() : "unknown"));
        return "!" + key + "!"; // Trả về key với dấu ! để dễ nhận biết lỗi
    }

    public ResourceBundle getBundle() {
        return messages;
    }
}