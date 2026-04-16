package com.hitster.service;

import java.util.UUID;

public class AuthService {

    // Register user and return an API token
    public static String register(String username, String email, String password, String picturePath) {
        String hash = PasswordUtil.hashPassword(password);
        int userId = DatabaseService.registerUser(username, email, hash, picturePath);

        if (userId > 0) {
            // Generate a simple unique token for API communication
            return UUID.randomUUID().toString();
        }
        return null; // Registration failed
    }

    // Login validation and return an API token
    public static String loginAndGetToken(String email, String password) {
        String storedHash = DatabaseService.getUserPasswordHash(email);

        if (storedHash != null && PasswordUtil.verifyPassword(password, storedHash)) {
            // Generate a simple unique token for API communication
            return UUID.randomUUID().toString();
        }
        return null; // Login failed
    }

    // Forgot Password - Updates the hash in the database
    public static boolean resetPassword(String email, String newPassword) {
        // In a production app, you would verify an email code first before doing this!
        String newHash = PasswordUtil.hashPassword(newPassword);
        return DatabaseService.updateUserPassword(email, newHash);
    }

    // Validates if the token actually belongs to an active user
    public static boolean isTokenValid(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        // Ask the database for the User ID attached to this token
        int userId = DatabaseService.getUserIdByToken(token);

        // If the ID is valid (greater than 0), the token is good!
        return userId > 0;
    }
}