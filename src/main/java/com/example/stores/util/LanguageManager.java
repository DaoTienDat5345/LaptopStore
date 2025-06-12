package com.example.stores.util;

public class LanguageManager {
    private static boolean isVietnamese = false;
    
    public static boolean isVietnamese() {
        return isVietnamese;
    }
    
    public static void setVietnamese(boolean vietnamese) {
        isVietnamese = vietnamese;
    }
}