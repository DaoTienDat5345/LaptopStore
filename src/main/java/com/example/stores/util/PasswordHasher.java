package com.example.stores.util; // Hoặc package khác tùy bạn

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    public static void main(String[] args) {
        String plainPassword = "password123";
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        System.out.println("Plain: " + plainPassword + " -> Hashed: " + hashedPassword);
    }
}