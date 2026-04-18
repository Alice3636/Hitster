package com.hitster.service;

public class AuthService {

    public static int register(String username, String email, String password) {
        String hash = PasswordUtil.hashPassword(password);
        return DatabaseService.registerUser(username, email, hash);
    }

    public static boolean login(String email, String password) {
        String storedHash = DatabaseService.getUserPasswordHash(email);

        if (storedHash == null) {
            return false;
        }

        return PasswordUtil.verifyPassword(password, storedHash);
    }

    public static Long getUserIdByEmail(String email) {
        return DatabaseService.getUserIdByEmail(email);
    }

    public static String getUsernameByEmail(String email) {
        return DatabaseService.getUsernameByEmail(email);
    }

    public static boolean isAdminByEmail(String email) {
        return DatabaseService.isAdminByEmail(email);
    }
}