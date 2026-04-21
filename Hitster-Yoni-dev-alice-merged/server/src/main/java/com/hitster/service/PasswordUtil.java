package com.hitster.service;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // create password hash
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // verify password against stored hash
    public static boolean verifyPassword(String password, String storedHash) {
        return BCrypt.checkpw(password, storedHash);
    }

}