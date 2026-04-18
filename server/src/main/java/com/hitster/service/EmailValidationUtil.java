package com.hitster.service;

import org.apache.commons.validator.routines.EmailValidator;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

public class EmailValidationUtil {

    private static final EmailValidator validator = EmailValidator.getInstance();

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        email = email.trim();

        // 1. Format check
        if (!validator.isValid(email)) {
            return false;
        }

        // 2. Domain (MX) check
        return hasMXRecord(email);
    }

    private static boolean hasMXRecord(String email) {
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();

        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");

            InitialDirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(domain, new String[]{"MX"});

            return attrs != null && attrs.get("MX") != null;

        } catch (NamingException e) {
            return false;
        }
    }
}