package com.hitster.controller;

import com.hitster.dto.auth.ForgotPasswordRequestDTO;
import com.hitster.dto.auth.LoginRequestDTO;
import com.hitster.dto.auth.LoginResponseDTO;
import com.hitster.dto.auth.RegisterRequestDTO;
import com.hitster.service.AuthService;
import com.hitster.service.DatabaseService;
import com.hitster.service.EmailValidationUtil;
import com.hitster.service.JwtUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/register")
    public LoginResponseDTO register(@RequestBody RegisterRequestDTO request) {
        if (request == null ||
                isBlank(request.username()) ||
                isBlank(request.email()) ||
                isBlank(request.password())) {
            throw new IllegalArgumentException("Username, email, and password are required.");
        }

        if (!EmailValidationUtil.isValidEmail(request.email())) {
            throw new InvalidEmailException("Invalid email format or domain.");
        }

        if (DatabaseService.usernameExists(request.username())) {
            throw new DuplicateFieldException("TAKEN_USERNAME_ERR", "Username is already taken.");
        }

        if (DatabaseService.emailExists(request.email())) {
            throw new DuplicateFieldException("TAKEN_EMAIL_ERR", "Email is already taken.");
        }

        int newUserId = AuthService.register(
                request.username(),
                request.email(),
                request.password()
        );

        if (newUserId <= 0) {
            throw new IllegalArgumentException("Registration failed.");
        }

        String token = JwtUtil.generateToken((long) newUserId, request.username(), false);

        return new LoginResponseDTO((long) newUserId, request.username(), false, token);
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO request) {
        if (request == null || isBlank(request.email()) || isBlank(request.password())) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        boolean ok = AuthService.login(request.email(), request.password());

        if (!ok) {
            throw new CredentialsInvalidException("Invalid email or password.");
        }

        Long userId = AuthService.getUserIdByEmail(request.email());
        String username = AuthService.getUsernameByEmail(request.email());
        boolean isAdmin = AuthService.isAdminByEmail(request.email());

        String token = JwtUtil.generateToken(userId, username, isAdmin);

        return new LoginResponseDTO(userId, username, isAdmin, token);
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        if (request == null || isBlank(request.email())) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (!EmailValidationUtil.isValidEmail(request.email())) {
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