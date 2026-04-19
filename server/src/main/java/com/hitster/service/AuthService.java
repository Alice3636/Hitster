package com.hitster.service;

public class AuthService {

    // Register user and return the new user ID (int)
    public static int register(String username, String email, String password, String picturePath) {
        String hash = PasswordUtil.hashPassword(password);

        // Calls your DB and returns the new user ID
        return DatabaseService.registerUser(username, email, hash, picturePath);
    }

    // Overloaded register method in case AuthController doesn't provide a
    // picturePath yet
    public static int register(String username, String email, String password) {
        return register(username, email, password, null);
    }

    // Login validation (returns true if email and password match)
    public static boolean login(String email, String password) {
        String storedHash = DatabaseService.getUserPasswordHash(email);

        if (storedHash != null && PasswordUtil.verifyPassword(password, storedHash)) {
            return true;
        }
        return false; // Login failed
    }

    // Forgot Password - Updates the hash in the database
    public static boolean resetPassword(String email, String newPassword) {
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