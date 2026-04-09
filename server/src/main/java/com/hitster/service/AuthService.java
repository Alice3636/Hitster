package com.hitster.service;

public class AuthService {

    // register new user
    public static int register(String username, String email, String password) {

        String hash = PasswordUtil.hashPassword(password);

        return DatabaseService.registerUser(username, email, hash);
    }

    // login validation
    public static boolean login(String email, String password) {

        String storedHash = DatabaseService.getUserPasswordHash(email);

        if (storedHash == null) {
            return false;
        }
        return PasswordUtil.verifyPassword(password, storedHash);
    }

}