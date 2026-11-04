package com.ems.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    /**
     * Hashes a password using SHA-256.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies a plain password against stored password.
     * Supports both plain-text (for easy SQL seed data) and SHA-256 hashed passwords.
     */
    public static boolean verify(String enteredPassword, String storedPassword) {
        if (enteredPassword == null || storedPassword == null) return false;
        // Direct match (for initial seed data or plain text in DB)
        if (enteredPassword.equals(storedPassword)) {
            return true;
        }
        // SHA-256 hash match
        String hashed = hashPassword(enteredPassword);
        return hashed.equalsIgnoreCase(storedPassword);
    }
}

