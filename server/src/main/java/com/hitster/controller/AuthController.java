package com.hitster.controller;

import com.hitster.service.AuthService;
import com.hitster.service.DatabaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        // 1. Verify the password using your existing AuthService
        if (AuthService.login(email, password)) {

            // 2. Generate a fresh, unique API Token
            String token = UUID.randomUUID().toString();

            // 3. Save the token to the Database so the server remembers it
            DatabaseService.updateUserToken(email, token);

            // 4. Send the token back to the JavaFX Client
            return ResponseEntity.ok(token);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");

        // 1. Register the user in the database
        int newUserId = AuthService.register(username, email, password);

        if (newUserId > 0) {
            // 2. Generate a token for the new user
            String token = UUID.randomUUID().toString();

            // 3. Save the token to the database
            DatabaseService.updateUserToken(email, token);

            // 4. Send it back to the JavaFX client so they are instantly logged in
            return ResponseEntity.ok(token);
        }

        return ResponseEntity.badRequest().body("Registration failed. Email might already exist.");
    }
}