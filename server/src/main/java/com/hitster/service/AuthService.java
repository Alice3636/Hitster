package com.hitster.service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private final static Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public static int register(String username, String email, String password, String picturePath) {
        String hash = PasswordUtil.hashPassword(password);
        return DatabaseService.registerUser(username, email, hash, picturePath);
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

    public static void processForgotPassword(String email) {
        boolean userExists = DatabaseService.emailExists(email);

        if (userExists) {
            String otpCode = String.format("%06d", new Random().nextInt(999999));

            otpStorage.put(email, otpCode);

            String subject = "Hitster - Your Password Reset Code";
            String text = "Hello,\n\nYou have requested to reset your password.\n" +
                    "Your verification code is: " + otpCode + "\n\n" +
                    "Please enter this code in the Hitster app to set a new password.";

            EmailSenderService.sendEmail(email, subject, text);
        }
    }

    public static boolean verifyCodeAndResetPassword(String email, String code, String newPassword) {
        String validCode = otpStorage.get(email);

        if (validCode != null && validCode.equals(code)) {

            String hashedPassword = PasswordUtil.hashPassword(newPassword);

            DatabaseService.updateUserPassword(email, hashedPassword);

            otpStorage.remove(email);
            return true;
        }
        return false;
    }

}
