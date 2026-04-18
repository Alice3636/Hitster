package com.hitster.controller;

import com.hitster.dto.AuthResponseDTO;
import com.hitster.dto.ForgotPasswordRequestDTO;
import com.hitster.dto.LoginRequestDTO;
import com.hitster.dto.RegisterRequestDTO;
import com.hitster.service.AuthService;
import com.hitster.service.DatabaseService;
import com.hitster.service.EmailValidationUtil;
import com.hitster.service.JwtUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/register")
    public AuthResponseDTO register(@RequestBody RegisterRequestDTO request) {
        if (request == null ||
                isBlank(request.getUsername()) ||
                isBlank(request.getEmail()) ||
                isBlank(request.getPassword())) {
            throw new IllegalArgumentException("Username, email, and password are required.");
        }

        if (!EmailValidationUtil.isValidEmail(request.getEmail())) {
            throw new InvalidEmailException("Invalid email format or domain.");
        }

        if (DatabaseService.usernameExists(request.getUsername())) {
            throw new DuplicateFieldException("TAKEN_USERNAME_ERR", "Username is already taken.");
        }

        if (DatabaseService.emailExists(request.getEmail())) {
            throw new DuplicateFieldException("TAKEN_EMAIL_ERR", "Email is already taken.");
        }

        int newUserId = AuthService.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );

        if (newUserId <= 0) {
            throw new IllegalArgumentException("Registration failed.");
        }

        String token = JwtUtil.generateToken((long) newUserId, request.getUsername(), false);

        return new AuthResponseDTO((long) newUserId, request.getUsername(), false, token);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginRequestDTO request) {
        if (request == null || isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        boolean ok = AuthService.login(request.getEmail(), request.getPassword());

        if (!ok) {
            throw new CredentialsInvalidException("Invalid email or password.");
        }

        Long userId = AuthService.getUserIdByEmail(request.getEmail());
        String username = AuthService.getUsernameByEmail(request.getEmail());
        boolean isAdmin = AuthService.isAdminByEmail(request.getEmail());

        String token = JwtUtil.generateToken(userId, username, isAdmin);

        return new AuthResponseDTO(userId, username, isAdmin, token);
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        if (request == null || isBlank(request.getEmail())) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (!EmailValidationUtil.isValidEmail(request.getEmail())) {
            throw new InvalidEmailException("Invalid email format or domain.");
        }

        return "If the email exists, a recovery process has been triggered.";
    }

    @PostMapping("/logout")
    public String logout() {
        return "OK";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}